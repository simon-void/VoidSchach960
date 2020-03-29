package voidchess.board.check

import voidchess.board.BasicChessBoard
import voidchess.board.getFirstFigureInDir
import voidchess.common.board.move.Direction
import voidchess.common.board.move.Position
import voidchess.engine.figures.*
import voidchess.figures.Bishop
import voidchess.figures.King
import voidchess.figures.Knight
import voidchess.figures.Pawn
import voidchess.figures.Queen
import voidchess.figures.Rook


object CheckSearch {

    fun isCheck(game: BasicChessBoard, king: King): Boolean {
        val isWhite = king.isWhite
        val kingPos = king.position

        if (isCheckByBishopOrQueen(game, kingPos, isWhite)) return true
        if (isCheckByRookOrQueen(game, kingPos, isWhite)) return true
        if (isCheckByKnight(game, kingPos, isWhite)) return true
        return if (isCheckByKing(game, kingPos)) true
        else isCheckByPawn(game, kingPos, isWhite)
    }

    private fun isCheckByKing(game: BasicChessBoard, kingPos: Position): Boolean {
        Direction.values().forEach {
            kingPos.step(it)?.let { pos ->
                game.getFigureOrNull(pos)?.let { figure ->
                    if (figure is King) {
                        return true
                    }
                }
            }
        }

        return false
    }

    private fun isCheckByPawn(game: BasicChessBoard, kingPos: Position, isWhite: Boolean): Boolean {
        val forwardDir = Direction.getForward(isWhite)

        kingPos.step(Direction.getDiagonal(forwardDir, Direction.RIGHT))?.let { pos ->
            game.getFigureOrNull(pos)?.let { figure ->
                if (figure is Pawn && figure.isWhite != isWhite) {
                    return true
                }
            }
        }
        kingPos.step(Direction.getDiagonal(forwardDir, Direction.LEFT))?.let { pos ->
            game.getFigureOrNull(pos)?.let { figure ->
                if (figure is Pawn && figure.isWhite != isWhite) {
                    return true
                }
            }
        }

        return false
    }

    private fun isCheckByKnight(game: BasicChessBoard, kingPos: Position, isWhite: Boolean): Boolean {
        kingPos.forEachKnightPos { pos ->
            game.getFigureOrNull(pos)?.let { figure ->
                if (figure.isWhite != isWhite && figure is Knight) {
                    return true
                }
            }
        }

        return false
    }

    private fun isCheckByBishopOrQueen(game: BasicChessBoard, kingPos: Position, isWhite: Boolean): Boolean {
        Direction.diagonalDirs.forEach { diagonal ->
            game.getFirstFigureInDir(diagonal, kingPos)?.let { figure ->
                if (figure.isWhite != isWhite && (figure is Queen || figure is Bishop)) {
                    return true
                }
            }
        }
        return false
    }

    private fun isCheckByRookOrQueen(game: BasicChessBoard, kingPos: Position, isWhite: Boolean): Boolean {

        if (isDoubleHorizontalCheckAfterPawnPromotion(
                game,
                kingPos,
                isWhite
            )
        ) return true
        // now that a straight double attack after pawn promotion is no longer an issue,
        // we can stop looking for a second attacker from straight lines

        Direction.straightDirs.forEach { diagonal ->
            game.getFirstFigureInDir(diagonal, kingPos)?.let { figure ->
                if (figure.isWhite != isWhite && (figure is Queen || figure is Rook)) {
                    return true
                }
            }
        }
        return false
    }

    private fun isDoubleHorizontalCheckAfterPawnPromotion(
        game: BasicChessBoard, kingPos: Position, isWhite: Boolean): Boolean {
        //only possible if the king stod in the columnshadow of a pawn which transformed in the last move to Rook or queen
        val groundRow = if (isWhite) 0 else 7
        if (kingPos.row != groundRow) return false

        var kingSideAttackerPos: Position? = null

        kingPos.step(Direction.LEFT)?.let { sidePos ->
            game.getFigureOrNull(sidePos)?.let { figure ->
                if (figure.isWhite != isWhite && (figure is Queen || figure is Rook)) {
                    kingSideAttackerPos = sidePos
                }
            }
        }
        if (kingSideAttackerPos == null) {
            kingPos.step(Direction.RIGHT)?.let { sidePos ->
                game.getFigureOrNull(sidePos)?.let { figure ->
                    if (figure.isWhite != isWhite && (figure is Queen || figure is Rook)) {
                        kingSideAttackerPos = sidePos
                    }
                }
            }
        }

        if (kingSideAttackerPos == null) return false

        val dirOfPossibleSecondStraightAttacker = if (isWhite) Direction.UP else Direction.DOWN

        game.getFirstFigureInDir(dirOfPossibleSecondStraightAttacker, kingPos)?.let { figure ->
            if (figure.isWhite != isWhite && (figure is Queen || figure is Rook)) {
                return true
            }
        }

        return false
    }
}
