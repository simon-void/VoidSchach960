package voidchess.figures

import voidchess.board.BasicChessBoard
import voidchess.board.ChessBoard
import voidchess.common.board.move.Direction
import voidchess.common.board.move.Move
import voidchess.common.board.move.Position
import kotlin.collections.ArrayList


class Bishop(isWhite: Boolean, startPosition: Position) : Figure(isWhite, startPosition,
    FigureType.BISHOP, true, false) {

    override fun isReachable(toPos: Position, game: BasicChessBoard): Boolean {
        if(!position.hasSameColor(toPos)) {
            return false
        }
        val direction = position.getDirectionTo(toPos)

        if (direction == null || direction.isStraight) {
            return false
        }

        forEachReachablePos(game, direction) {
            if (it.equalsPosition(toPos)) return true
        }

        return false
    }

    private inline fun forEachReachablePos(game: BasicChessBoard, informOf: (Position) -> Unit) {
        forEachReachablePos(game, Direction.UP_RIGHT, informOf)
        forEachReachablePos(game, Direction.UP_LEFT, informOf)
        forEachReachablePos(game, Direction.DOWN_RIGHT, informOf)
        forEachReachablePos(game, Direction.DOWN_LEFT, informOf)
    }

    override fun getReachableMoves(game: BasicChessBoard): Collection<Move> = ArrayList<Move>(13).apply {
        forEachReachablePos(game) {
            add(Move[position, it])
        }
    }

    override fun isSelectable(game: ChessBoard): Boolean {
        forEachReachablePos(game) {
            if (!isBound(it, game)) return true
        }
        return false
    }
}