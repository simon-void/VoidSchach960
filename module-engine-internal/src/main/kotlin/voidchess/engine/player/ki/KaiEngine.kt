package voidchess.engine.player.ki

import voidchess.common.board.BasicChessGame
import voidchess.common.board.BasicChessGameImpl
import voidchess.common.board.other.StartConfig
import voidchess.common.board.move.Move
import voidchess.common.player.ki.*
import voidchess.common.player.ki.evaluation.EvaluatedMove
import voidchess.common.player.ki.evaluation.NumericalEvaluation
import voidchess.engine.player.ki.concurrent.ConcurrencyStrategy
import voidchess.engine.player.ki.concurrent.MultiThreadStrategy
import voidchess.engine.player.ki.concurrent.SingleThreadStrategy
import voidchess.engine.player.ki.evaluation.*
import voidchess.engine.player.ki.openings.OpeningsLibrary
import java.util.*
import kotlin.math.pow
import kotlin.random.Random
import kotlin.IllegalArgumentException

class KaiEngine(private val progressCallback: ProgressCallback): Engine {

    private val concurrencyStrategyCache = ConcurrencyStrategyContainer(progressCallback)
    private val openingsLibrary = OpeningsLibrary("openings.txt")
    private val staticEvaluation: EvaluatingStatically = EvaluatingAsIsNow

    override fun getSpec(): EngineSpec =
        EngineSpec(
            name = "Kai",
            coresToUseOption = CoresToUseOption,
            difficultyOption = DifficultyOption,
            supportsChess960 = true
        )

    override fun setOption(name: String, value: String) {
        when(name) {
            CoresToUseOption.name -> CoresToUseOption.setCoresToUse(value)
            DifficultyOption.name -> DifficultyOption.setDifficulty(value)
            else -> throw IllegalArgumentException("unknown option name: $name")
        }
    }

    override fun evaluateMovesBestMoveFirst(movesSoFar: List<String>, startConfig: StartConfig): EngineAnswer = try {
        val moves = movesSoFar.map { Move.byCheckedCode(it) }
        validateMovesOrReturnErrorMsg(moves, startConfig)?.let { validatorErrorMsg->
            return EngineAnswer.Error(validatorErrorMsg)
        }

        //display that the computer is working
        progressCallback(0, 1)

        val pruner = DifficultyOption.pruner
        val coresToUse = CoresToUseOption.coresToUse
        val evaluatingMinMax = EvaluatingMinMax(pruner, staticEvaluation)

        val evaluatedMove = lookUpNextMove(startConfig, moves, evaluatingMinMax)
            ?: concurrencyStrategyCache.get(coresToUse)
                .evaluateMovesBestMoveFirst(startConfig, moves, evaluatingMinMax, okDistanceToBest).pickOkMove()

        EngineAnswer.Success(evaluatedMove)
    }catch (e: Exception) {
        EngineAnswer.Error(e.toString())
    }

    private fun validateMovesOrReturnErrorMsg(movesSoFar: List<Move>, startConfig: StartConfig): String? {
        val basicGame: BasicChessGame = BasicChessGameImpl(startConfig)
        val movesApplied = mutableListOf<Move>()
        for(move in movesSoFar) {
            if(!basicGame.isMovable(move.from, move.to)) {
                return "$move is illegal with $startConfig after moves: ${movesApplied.joinToString()}"
            }
            basicGame.move(move)
            movesApplied.add(move)
        }
        return null
    }

    private fun lookUpNextMove(
        startConfig: StartConfig,
        movesSoFar: List<Move>,
        evaluatingMinMax: EvaluatingMinMax
    ): EvaluatedMove? {

        val chess960StartIndex: Int? = when (startConfig) {
            is StartConfig.ClassicConfig -> startConfig.chess960Index
            is StartConfig.Chess960Config -> startConfig.chess960Index
            is StartConfig.ManualConfig -> null
        }

        if (chess960StartIndex == null || movesSoFar.size >= openingsLibrary.maxDepth) {
            return null
        }

        return openingsLibrary.nextMove(chess960StartIndex, movesSoFar)?.let { libraryMove ->
            concurrencyStrategyCache.singleThreadStrategy.evaluateMove(
                StartConfig.Chess960Config(chess960StartIndex),
                movesSoFar,
                libraryMove,
                evaluatingMinMax
            )
        }
    }

    /**
     * pick the move to make given an evaluated Set of possible moves.
     * (Taking always the best move would lead to a limited number of openings!)
     * @return the move the ki will make next
     */
    private fun List<EvaluatedMove>.pickOkMove(): EvaluatedMove {
        val sortedEvaluatedMoves = this
        val bestEval: NumericalEvaluation = sortedEvaluatedMoves.first().let { bestEvaluatedMove ->
            if (bestEvaluatedMove.value !is NumericalEvaluation) {
                return bestEvaluatedMove
            }
            bestEvaluatedMove.value as NumericalEvaluation
        }



        fun pickEvaluateMoveBy(move: Move) = sortedEvaluatedMoves.first { it.move.equalsMove(move) }

        // the weight lies between (0-1]
        // with bestMove will have a weight of 1
        // and a move that is almost okDistanceToBest apart will have a weight of almost 0
        val moveAndLinearWeight: List<Pair<Move, Double>> = LinkedList<Pair<Move, Double>>().apply {
            val bestFullEvaluation = bestEval.numericValue
            for((move, evaluation) in sortedEvaluatedMoves) {
                if(evaluation !is NumericalEvaluation) break
                val distanceToBest = bestFullEvaluation-evaluation.numericValue
                if(distanceToBest>=okDistanceToBest) break
                add(move to (okDistanceToBest-distanceToBest)/okDistanceToBest)
            }
        }

        require(moveAndLinearWeight.isNotEmpty())
        if(moveAndLinearWeight.size==1) {
            return pickEvaluateMoveBy(moveAndLinearWeight.first().first)
        }

        // make it more than linear probable to pick a better move (the bigger the factor, the more preferable better solutions are)
        val moveAndWeight = moveAndLinearWeight.map { Pair(it.first, it.second.pow(1.8)) }
        val weightSum = moveAndWeight.map { it.second }.sum()
        // the sum of all percentages will be 1.0 (or close to it because of rounding errors)
        val moveAndPercentage = moveAndWeight.map { Pair(it.first, it.second/weightSum) }

        var randomValueInbetween0and1 = Random.nextDouble()
        val moveAndPercentageIter = moveAndPercentage.iterator()
        var moveWithPercentage = moveAndPercentageIter.next()
        while(moveAndPercentageIter.hasNext() && randomValueInbetween0and1>moveWithPercentage.second) {
            randomValueInbetween0and1 -= moveWithPercentage.second
            moveWithPercentage = moveAndPercentageIter.next()
        }

        return pickEvaluateMoveBy(moveWithPercentage.first)
    }

    companion object {
        const val okDistanceToBest = .2
    }
}


private class ConcurrencyStrategyContainer(private val progressCallback: ProgressCallback) {
    var singleThreadStrategy = SingleThreadStrategy(progressCallback)
    private var coresAndStrategy: Pair<Int, ConcurrencyStrategy> =
        CoresToUseOption.coresToUse.let { it to MultiThreadStrategy(it, progressCallback) }

    fun get(numberOfCoresToUse: Int): ConcurrencyStrategy {
        if(numberOfCoresToUse==1) return singleThreadStrategy
        if( numberOfCoresToUse!=coresAndStrategy.first) {
            val oldStrategy = coresAndStrategy.second
            coresAndStrategy = numberOfCoresToUse to MultiThreadStrategy(numberOfCoresToUse, progressCallback)
            oldStrategy.shutdown()
        }
        return coresAndStrategy.second
    }
}
