package voidchess.board.move

import voidchess.figures.Figure


class ExtendedMove(val move: Move,
                   val figureTaken: Figure?,
                   val enpassentPawnOrCastlingRook: Figure?,
                   val colorOfMove: Boolean,
                   val isCastling: Boolean,
                   val isEnpassent: Boolean,
                   val isPawnTransformation: Boolean) {

    @get:JvmName("wasFigureTaken")
    val wasFigureTaken = figureTaken != null || isEnpassent

    override fun toString() = move.toString()
}
