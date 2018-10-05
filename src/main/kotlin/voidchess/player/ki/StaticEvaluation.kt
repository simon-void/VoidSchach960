package voidchess.player.ki

import voidchess.board.ChessGameInterface
import voidchess.figures.King
import voidchess.helper.Position
import voidchess.player.ki.evaluation.CheckmateSelf
import voidchess.player.ki.evaluation.Evaluated
import voidchess.player.ki.evaluation.Ongoing


class StaticEvaluation : StaticEvaluationInterface {

    override fun getPrimaryEvaluation(game: ChessGameInterface, forWhite: Boolean): Evaluated {
        val primaryEvaluation = evaluateFigures(game, forWhite)
        return Ongoing(primaryEvaluation)
    }

    override fun addSecondaryEvaluation(game: ChessGameInterface, forWhite: Boolean, withPrimaryEvaluation: Evaluated) {
        if (withPrimaryEvaluation.needsSecondaryEvaluation()) {
            when (withPrimaryEvaluation) {
                is Ongoing -> withPrimaryEvaluation.setSecondaryEvaluation(getSecondaryEvaluation(game, forWhite))
                is CheckmateSelf -> withPrimaryEvaluation.setSecondaryEvaluation(
                        evaluateFigures(game, forWhite) + getSecondaryEvaluation(game, forWhite)
                )
                else -> throw IllegalStateException("unexpected class which requests a secondary evaluation: " + withPrimaryEvaluation.javaClass.simpleName)
            }
        }
    }

    private fun getSecondaryEvaluation(game: ChessGameInterface, forWhite: Boolean) = evaluateRuledArea(game, forWhite) + evaluatePosition(game, forWhite)

    private fun evaluateFigures(game: ChessGameInterface, forWhite: Boolean): Double {
        var whiteFigures = 0.0
        var blackFigures = 0.0

        for (figure in game.getFigures()) {
            if (figure.isWhite) {
                when {
                    figure.isPawn() -> whiteFigures += PAWN_VALUE
                    figure.isRook() -> whiteFigures += Rook_VALUE
                    figure.isKnight() -> whiteFigures += KNIGHT_VALUE
                    figure.isBishop() -> whiteFigures += BISHOP_VALUE
                    figure.isQueen() -> whiteFigures += QUEEN_VALUE
                }
            } else {
                when {
                    figure.isPawn() -> blackFigures += PAWN_VALUE
                    figure.isRook() -> blackFigures += Rook_VALUE
                    figure.isKnight() -> blackFigures += KNIGHT_VALUE
                    figure.isBishop() -> blackFigures += BISHOP_VALUE
                    figure.isQueen() -> blackFigures += QUEEN_VALUE
                }
            }
        }

        return if (forWhite)
            whiteFigures - blackFigures
        else
            blackFigures - whiteFigures
    }

    private fun evaluateRuledArea(game: ChessGameInterface, forWhite: Boolean): Double {
        val whiteMoves = game.countReachableMoves(true).toDouble()
        val blackMoves = game.countReachableMoves(false).toDouble()

        return VALUE_OF_AREA * if (forWhite)
            whiteMoves - blackMoves
        else
            blackMoves - whiteMoves
    }

    private fun evaluatePosition(game: ChessGameInterface, forWhite: Boolean): Double {
        var whiteEvaluation = 0.0
        var blackEvaluation = 0.0

        val figures = game.getFigures()
        for (figure in figures) {
            val pos = figure.position
            if (figure.isPawn()) {
                if (figure.isWhite)
                    whiteEvaluation += evaluatePawn(game, pos, true)
                else
                    blackEvaluation += evaluatePawn(game, pos, false)
            } else if (figure.isKnight()) {
                if (figure.isWhite)
                    whiteEvaluation += evaluateKnight(pos)
                else
                    blackEvaluation += evaluateKnight(pos)
            } else if (figure.isBishop()) {
                if (figure.isWhite)
                    whiteEvaluation += evaluateBishop(game, pos)
                else
                    blackEvaluation += evaluateBishop(game, pos)
            } else if (figure.isKing()) {
                if (figure.isWhite)
                    whiteEvaluation += evaluateKing(game, figure as King)
                else
                    blackEvaluation += evaluateKing(game, figure as King)
            }
        }

        return if (forWhite)
            whiteEvaluation - blackEvaluation
        else
            blackEvaluation - whiteEvaluation
    }

    private fun evaluateBishop(game: ChessGameInterface, pos: Position): Double {

        val isWhite = game.getFigure(pos)!!.isWhite
        val startRow = if (isWhite) 0 else 7

        if (pos.row == startRow && (pos.column == 2 || pos.column == 5)) {
            return BISHOP_ON_START_POSITION_PUNISHMENT
        }


        val blockingRow = if (isWhite) 2 else 5
        val blockedPawnRow = if (isWhite) 1 else 6
        val possiblePawn = Position[blockedPawnRow, pos.column]

        return if (pos.row == blockingRow &&
                (pos.column == 3 || pos.column == 4) &&
                containsPawnOfColor(game, possiblePawn, isWhite)) {
            BISHOP_BLOCKS_MIDDLE_PAWN_PUNISHMENT
        } else 0.0

    }

    private fun evaluateKnight(pos: Position) =
            if (pos.row == 0 || pos.row == 7 || pos.column == 0 || pos.column == 7) BORDER_KNIGHT_PUNISHMENT
            else 0.0

    private fun evaluatePawn(game: ChessGameInterface, pos: Position, isWhite: Boolean) =
            evaluatePawnPosition(pos, isWhite) + evaluatePawnDefense(game, pos, isWhite)

    private fun evaluatePawnPosition(pos: Position, isWhite: Boolean) =
            MOVES_GONE_VALUE * if( isWhite) pos.row-1 else 7-pos.row

    private fun evaluatePawnDefense(game: ChessGameInterface, pos: Position, isWhite: Boolean): Double {

        val forwardRow = if (isWhite) pos.row + 1 else pos.row - 1
        val backwardRow = if (isWhite) pos.row - 1 else pos.row + 1

        if (pos.column != 0) {
            val leftForwardPosition = Position[forwardRow, pos.column - 1]
            val leftBackwardPosition = Position[backwardRow, pos.column - 1]
            val leftPosition = Position[pos.row, pos.column - 1]
            if (containsPawnOfColor(game, leftForwardPosition, isWhite)) {
                return DEFENSE_VALUE
            }
            if (containsPawnOfColor(game, leftBackwardPosition, isWhite)) {
                return DEFENSE_VALUE
            }
            if (containsPawnOfColor(game, leftPosition, isWhite)) {
                return NEXT_TO_VALUE
            }
        } else {
            val rightPosition = Position[pos.row, 1]
            val rightForwardPosition = Position[forwardRow, 1]
            val rightBackwardPosition = Position[backwardRow, 1]
            if (!(containsPawnOfColor(game, rightPosition, isWhite)
                            || containsPawnOfColor(game, rightForwardPosition, isWhite)
                            || containsPawnOfColor(game, rightBackwardPosition, isWhite))) {
                return UNPROTECTED_BORDER_PAWN_VALUE
            }
        }

        if (pos.column != 7) {
            val rightForwardPosition = Position[forwardRow, pos.column + 1]
            val rightBackwardPosition = Position[backwardRow, pos.column + 1]
            val rightPosition = Position[pos.row, pos.column + 1]
            if (containsPawnOfColor(game, rightForwardPosition, isWhite)) {
                return DEFENSE_VALUE
            }
            if (containsPawnOfColor(game, rightBackwardPosition, isWhite)) {
                return DEFENSE_VALUE
            }
            if (containsPawnOfColor(game, rightPosition, isWhite)) {
                return NEXT_TO_VALUE
            }
        } else {
            val leftPosition = Position[pos.row, 6]
            val leftForwardPosition = Position[forwardRow, 6]
            val leftBackwardPosition = Position[backwardRow, 6]
            if (!(containsPawnOfColor(game, leftPosition, isWhite)
                            || containsPawnOfColor(game, leftForwardPosition, isWhite)
                            || containsPawnOfColor(game, leftBackwardPosition, isWhite))) {
                return UNPROTECTED_BORDER_PAWN_VALUE
            }
        }

        return 0.0
    }

    private fun containsPawnOfColor(game: ChessGameInterface, pos: Position, white: Boolean): Boolean {
        val figure = game.getFigure(pos)
        return figure!=null && figure.isWhite == white && figure.isPawn()
    }


    private fun evaluateKing(game: ChessGameInterface, king: King): Double {
        val colorOfOppositeQueen = !king.isWhite
        val queenOfOppositeColorStillOnBoard = game.getFigures().any { figure -> figure.isQueen() && figure.isWhite==colorOfOppositeQueen }
        var value = evaluateCastling(king, queenOfOppositeColorStillOnBoard)
        value += evaluateKingDefense(game, king, queenOfOppositeColorStillOnBoard)
        return value
    }

    private fun evaluateCastling(king: King, queenOfOppositeColorStillOnBoard: Boolean) =
            if (queenOfOppositeColorStillOnBoard && !king.didCastling()) NOT_YET_CASTLED_PUNISHMENT
            else 0.0

    private fun evaluateKingDefense(game: ChessGameInterface, king: King, queenOfOppositeColorStillOnBoard: Boolean): Double {
        val isWhite = king.isWhite
        val kingsRow = king.position.row
        var value = 0.0
        val groundRow = if (isWhite) 0 else 7
        val secondRow = if (isWhite) 1 else 6

        if (king.didCastling() && kingsRow == groundRow) {
            val defenseValue = if (queenOfOppositeColorStillOnBoard) {
                BIG_KING_DEFENSE_VALUE
            } else {
                SMALL_KING_DEFENSE_VALUE
            }
            val minColumn = Math.max(0, kingsRow - 1)
            val maxColumn = Math.min(7, kingsRow + 1)
            for (column in minColumn..maxColumn) {
                if (containsPawnOfColor(game, Position[secondRow, column], isWhite)) {
                    value += defenseValue
                }
            }
        }

        return value
    }

    companion object {
        private const val VALUE_OF_AREA = 0.015

        private const val PAWN_VALUE = 1.0
        private const val Rook_VALUE = 4.5
        private const val KNIGHT_VALUE = 3.0
        private const val BISHOP_VALUE = 3.0
        private const val QUEEN_VALUE = 9.0

        private const val BISHOP_ON_START_POSITION_PUNISHMENT = -0.45
        private const val BISHOP_BLOCKS_MIDDLE_PAWN_PUNISHMENT = -0.2

        private const val BORDER_KNIGHT_PUNISHMENT = -0.45

        private const val MOVES_GONE_VALUE = 0.2

        private const val DEFENSE_VALUE = 0.10                 // bonus, if a pawn is covered by or is covering another pawn
        private const val NEXT_TO_VALUE = 0.06                 // bonus, if a pawn has a neighbour
        private const val UNPROTECTED_BORDER_PAWN_VALUE = -0.2 // malus for an uncovered pawn on the sideline (a or h)

        private const val NOT_YET_CASTLED_PUNISHMENT = -0.4

        private const val BIG_KING_DEFENSE_VALUE = 0.6
        private const val SMALL_KING_DEFENSE_VALUE = 0.2
    }
}
