package voidchess.common.player.ki.evaluation

import voidchess.common.board.move.Move
import java.util.*

data class EvaluatedMove(val move: Move, val value: Evaluation)

object LowestEvalFirst: Comparator<EvaluatedMove> {
    override fun compare(o1: EvaluatedMove, o2: EvaluatedMove) = o1.value.compareTo(o2.value)
}

object HighestEvalFirst: Comparator<EvaluatedMove> {
    override fun compare(o1: EvaluatedMove, o2: EvaluatedMove) = o2.value.compareTo(o1.value)
}
