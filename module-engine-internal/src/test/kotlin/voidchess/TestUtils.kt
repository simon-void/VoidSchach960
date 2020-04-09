package voidchess

import voidchess.common.board.StartConfig
import voidchess.common.board.move.Move
import voidchess.common.board.move.PawnPromotion
import voidchess.common.board.move.Position
import voidchess.common.board.move.PositionProgression
import voidchess.engine.board.*
import voidchess.common.helper.splitAndTrim
import voidchess.engine.board.other.ChessGameSupervisor
import voidchess.engine.board.other.ChessGameSupervisorDummy
import voidchess.engine.figures.Figure
import java.util.*


internal class ChessGameSupervisorMock(private val defaultPawnTransform: PawnPromotion) :
    ChessGameSupervisor {

    override fun askForPawnChange(pawnPosition: Position): PawnPromotion {
        return defaultPawnTransform
    }
}

internal fun initChessBoard(gameDes: String): ChessBoard = ArrayChessBoard(gameDes.toManualConfig())

internal fun initChessBoard(chess960: Int, vararg moveCodes: String): ChessBoard = ArrayChessBoard(StartConfig.Chess960Config(chess960)).apply {
    for(moveCode in moveCodes) {
        move(Move.byCode(moveCode), ChessGameSupervisorDummy)
    }
}

internal fun initChessGame(chess960: Int, vararg moveCodes: String): ChessGameInterface = ChessGame(StartConfig.Chess960Config(chess960)).apply {
    for(moveCode in moveCodes) {
        move(Move.byCode(moveCode))
    }
}

internal fun ChessBoard.getPossibleMovesFrom(posCode: String): List<Move> {
    val moveList = LinkedList<Move>()
    val figure = getFigure(Position.byCode(posCode))
    figure.getPossibleMoves(this, moveList)
    return moveList
}

internal fun ChessGameInterface.getPossibleMovesFrom(posCode: String): List<Move> {
    val allMovesList = getAllMoves()
    val fromPos = Position.byCode(posCode)
    return allMovesList.filter { move -> move.from==fromPos }
}

internal fun ChessGameInterface.moves(moveCodes: Iterable<String>) {
    for(moveCode in moveCodes) {
        move(Move.byCode(moveCode))
    }
}

internal fun BasicChessBoard.countFigures(): Int {
    var figureCount = 0
    for(row in 0..7) {
        for(column in 0..7) {
            if(this.getFigureOrNull(Position[row, column])!=null) {
                figureCount++
            }
        }
    }
    return figureCount
}

internal fun BasicChessBoard.assertFiguresKnowTherePosition() {
    val figuresWhichAreWrongAboutTheirPosition = mutableListOf<Pair<Position, Figure>>()
    for(row in 0..7) {
        for(column in 0..7) {
            val pos = Position[row, column]
            getFigureOrNull(pos)?.let { figure ->
                if(pos.notEqualsPosition(figure.position)) {
                    figuresWhichAreWrongAboutTheirPosition.add(pos to figure)
                }
            }
        }
    }
    if(figuresWhichAreWrongAboutTheirPosition.isNotEmpty()) {
        throw AssertionError("these figures are wrong about their position: "+ figuresWhichAreWrongAboutTheirPosition.joinToString())
    }
}

internal fun BasicChessBoard.allFigures(): List<Figure> {
    val figures = mutableListOf<Figure>()
    for(row in 0..7) {
        for(column in 0..7) {
            getFigureOrNull(Position[row, column])?.let { figure ->
                figures.add(figure)
            }
        }
    }
    return figures
}

internal fun PositionProgression.toList(): List<Position> {
    val list = LinkedList<Position>()
    forEachReachablePos { position -> list.add(position) }
    return list
}

internal fun Collection<Move>.toFromPosAsStringSet(): Set<String> = asSequence().map { it.from.toString() }.toSet()
internal fun Collection<Move>.toTargetPosAsStringSet(): Set<String> = asSequence().map { it.to.toString() }.toSet()

internal fun Position.mirrorRow() = Position[7-row, column]

internal fun Move.mirrorRow() = Move[from.mirrorRow(), to.mirrorRow()]

internal fun ChessGame.copyGameWithInvertedColors(): ChessGame {
    val intermediate = "switching"
    val copyDef = toString()
            // switch white and black
            .replace("white", intermediate)
            .replace("black", "white")
            .replace(intermediate, "black")
            // mirror positions
            .splitAndTrim(' ')
            .joinToString(" ") { token ->
                if (token.contains('-')) {
                    val figureDef = ArrayList<String>(token.splitAndTrim('-'))
                    figureDef[2] = Position.byCode(figureDef[2]).mirrorRow().toString()
                    figureDef.joinToString("-")
                } else token
            }

    return ChessGame(copyDef.toManualConfig())
}

internal fun String.toManualConfig(): StartConfig.ManualConfig {
    val gameDesc = this
    val gameDescParts = this.split(" ").filter { it.isNotEmpty() }
    check(gameDescParts.size>=4) {"expected gameDescription, found something else: $gameDesc"}
    val isWhiteTurn = gameDescParts[0]=="white"
    val numberOfMovesSinceHitFigure = gameDescParts[1].toInt()
    val figureStates = gameDescParts.filterIndexed{ index, _ ->index>1}
    return StartConfig.ManualConfig(isWhiteTurn, numberOfMovesSinceHitFigure, figureStates)
}

internal fun Int.toChess960Config(): StartConfig.Chess960Config {
    val chess960Index = this
    check(chess960Index in 0 until 960) {"expected value to be within 0-959 but was: $chess960Index"}
    return StartConfig.Chess960Config(chess960Index)
}