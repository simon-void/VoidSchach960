package voidchess.figures

import voidchess.board.BasicChessGameInterface
import voidchess.board.SimpleChessBoardInterface
import voidchess.board.move.Direction
import voidchess.board.move.Move
import voidchess.board.move.Position


class Bishop(isWhite: Boolean, startPosition: Position) : Figure(isWhite, startPosition, FigureType.BISHOP) {

    override fun isReachable(to: Position, game: BasicChessGameInterface): Boolean {
        val direction = position.getDirectionTo(to)

        if (direction == null || direction.isStraight) {
            return false
        }

        forEachReachablePos(game, direction) {
            if (it.equalsPosition(to)) return true
        }

        return false
    }

    private inline fun forEachReachablePos(game: BasicChessGameInterface, informOf: (Position) -> Unit) {
        forEachReachablePos(game, Direction.UP_RIGHT, informOf)
        forEachReachablePos(game, Direction.UP_LEFT, informOf)
        forEachReachablePos(game, Direction.DOWN_RIGHT, informOf)
        forEachReachablePos(game, Direction.DOWN_LEFT, informOf)
    }

    override fun getReachableMoves(game: BasicChessGameInterface, result: MutableList<Move>) {
        forEachReachablePos(game) {
            result.add(Move.get(position, it))
        }
    }

    override fun isSelectable(game: SimpleChessBoardInterface): Boolean {
        forEachReachablePos(game) {
            if (!isBound(it, game)) return true
        }
        return false
    }

    override fun countReachableMoves(game: BasicChessGameInterface): Int {
        var reachableMovesCount = 0
        forEachReachablePos(game) {
            reachableMovesCount++
        }
        return reachableMovesCount
    }
}