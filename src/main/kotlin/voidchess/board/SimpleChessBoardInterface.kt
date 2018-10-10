package voidchess.board

import voidchess.figures.Figure
import voidchess.helper.CheckStatus
import voidchess.helper.Position


interface SimpleChessBoardInterface : BasicChessGameInterface {

    fun init()
    fun init(chess960: Int)
    fun init(des: String)
    fun setFigure(pos: Position, figure: Figure)
    fun clearFigure(pos: Position)
    // moves figure, returns the figure that was taken
    // "normal" moves only, no special cases (castling, enpassant, pawn promotion)
    fun move(figure: Figure, to: Position): Figure?
    fun undoMove(figure: Figure, from: Position, figureTaken: Figure?)
    fun isCheck(isWhite: Boolean): Boolean
    fun getCheckStatus(isWhite: Boolean): CheckStatus
}
