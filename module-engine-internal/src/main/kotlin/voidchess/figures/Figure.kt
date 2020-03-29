package voidchess.figures

import voidchess.board.BasicChessBoard
import voidchess.board.ChessBoard
import voidchess.board.check.AttackLines
import voidchess.board.check.BoundLine
import voidchess.board.check.CheckLine
import voidchess.board.getFirstFigureInDir
import voidchess.board.move.Direction
import voidchess.board.move.Move
import voidchess.board.move.Position


internal abstract class Figure constructor(
        // a figure's color
        val isWhite: Boolean,
        var position: Position,
        val type: FigureType,
        val attacksDiagonalLine: Boolean,
        val attacksStraightLine: Boolean
) {

    // encodes type of class + color
    val typeInfo: Int = if (isWhite) type.index else (type.index + 7)

    fun isPawn() = type == FigureType.PAWN
    fun isRook() = type == FigureType.ROOK
    fun isKnight() = type == FigureType.KNIGHT
    fun isBishop() = type == FigureType.BISHOP
    fun isQueen() = type == FigureType.QUEEN
    fun isKing() = type == FigureType.KING

    fun hasDifferentColor(other: Figure) = isWhite != other.isWhite

    open fun canBeHitEnPassant() = false

    open fun canCastle(): Boolean {
        return false
    }

    open fun figureMoved(move: Move) {
        if (position.equalsPosition(move.from)) position = move.to
    }

    open fun undoMove(oldPosition: Position) {
        position = oldPosition
    }

    abstract fun isReachable(toPos: Position, game: BasicChessBoard): Boolean
    abstract fun countReachableMoves(game: BasicChessBoard): Int
    abstract fun isSelectable(game: ChessBoard): Boolean
    internal open fun getReachableMoves(game: BasicChessBoard, result: MutableCollection<Move>): Unit = throw NotImplementedError("not implemented in class ${javaClass.simpleName}")
    internal open fun getReachableTakingMoves(game: BasicChessBoard, result: MutableCollection<Move>): Unit = throw NotImplementedError("not implemented in class ${javaClass.simpleName}")
    internal open fun getReachableCheckingMoves(game: ChessBoard, result: MutableCollection<Move>): Unit = throw NotImplementedError("not implemented in class ${javaClass.simpleName}")

    fun isMovable(toPos: Position, game: ChessBoard): Boolean {
        return isReachable(toPos, game) && !isBound(toPos, game)
    }

    open fun getPossibleMoves(game: ChessBoard, result: MutableCollection<Move>) {
        val attackLines = game.getCachedAttackLines(isWhite)
        if(attackLines.noCheck) {
            val boundLine = attackLines.boundLineByBoundFigurePos[position]
            if(boundLine==null) {
                getReachableMoves(game, result)
            }else{
                getPossibleMovesWhileBoundAndNoCheck(game, boundLine, result)
            }
        } else if(attackLines.isSingleCheck) {
            val checkLine = attackLines.checkLines.first()
            val boundLine = attackLines.boundLineByBoundFigurePos[position]
            if(boundLine==null) {
                getPossibleMovesWhileUnboundAndCheck(game, checkLine, result)
            }
            // no need for else, a figure that is bound can't intercept a check
        }
    }

    /**
     * let's only look for checks to give if our king is not in check.
     * (for minimizing the code complexity)
     */
    open fun getPossibleTakingMoves(game: ChessBoard, result: MutableCollection<Move>) {
        val attackLines = game.getCachedAttackLines(isWhite)
        if(attackLines.noCheck) {
            val boundLine = attackLines.boundLineByBoundFigurePos[position]
            if(boundLine==null) {
                getReachableTakingMoves(game, result)
            }else{
                if(isReachable(boundLine.attackerPos, game)) {
                    result.add(Move[position, boundLine.attackerPos])
                }
            }
        } else if(attackLines.isSingleCheck) {
            val checkLine = attackLines.checkLines.first()
            val boundLine = attackLines.boundLineByBoundFigurePos[position]
            if(boundLine==null && isReachable(checkLine.attackerPos, game)) {
                result.add(Move[position, checkLine.attackerPos])
            }
            // no need for else, a figure that is bound can't intercept a check
        }
    }

    /**
     * moves are critical if they
     * 1) can't be reversed (e.g. a figure is taken, a king castles or a pawn move)
     * 2) if a knight forks
     * 3) if a check is given
     */
    open fun getCriticalMoves(game: ChessBoard, result: MutableSet<Move>) {
        getPossibleTakingMoves(game, result)

        // this doesn't make sense for king; and pawn and knight overwrite getCriticalMoves altogether.
        if(isPawn()||isKnight()||isKing()) return

        // for simplicity lets only consider checks while not in check and figure unbound
        val attackLines = game.getCachedAttackLines(isWhite)
        if(attackLines.noCheck) {
            if(attackLines.boundLineByBoundFigurePos[position]==null) {
                getReachableCheckingMoves(game, result)
            }
        }
    }

    protected abstract fun getPossibleMovesWhileUnboundAndCheck(game: ChessBoard, checkLine: CheckLine, result: MutableCollection<Move>)
    protected abstract fun getPossibleMovesWhileBoundAndNoCheck(game: ChessBoard, boundLine: BoundLine, result: MutableCollection<Move>)

    protected fun addMoveIfReachable(pos: Position, game: BasicChessBoard, result: MutableCollection<Move>) =
            if(isReachable(pos, game)) result.add(Move[position, pos])
            else false

    internal fun isBound(toPos: Position, game: ChessBoard): Boolean {
        assert(isReachable(toPos, game)) { "the assumption of isBound is that toPos is confirmed reachable" }
        val attackLinesStatus = game.getCachedAttackLines(isWhite)
        return isBound(toPos, game, attackLinesStatus)
    }

    private fun isBound(toPos: Position, game: ChessBoard, attackLines: AttackLines): Boolean {
        if( isKing()) {
            return (this as King).canNotMoveThereBecauseOfCheck(toPos, game, attackLines)
        }

        if(attackLines.noCheck) {
            attackLines.boundLineByBoundFigurePos[position]?.let { boundLine->
                val proposedDirection = position.getDirectionTo(toPos)
                val isProposedDirectionOnBindingLine = proposedDirection==boundLine.boundFigureToAttackerDirection
                        || proposedDirection==boundLine.boundFigureToAttackerDirection.reverse
                return if(isProposedDirectionOnBindingLine) {
                    !(boundLine.possibleMovesToAttacker.contains(toPos) || boundLine.possibleMovesToKing.contains(toPos))
                } else {
                    true
                }
            }
            return false
        }

        if (attackLines.isSingleCheck) {
            val checkLine = attackLines.checkLines.first()
            if(!checkLine.posProgression.contains(toPos)) {
                return true
            }
            return attackLines.boundLineByBoundFigurePos.containsKey(position)
        }

        // isDoubleCheck!
        return true
    }

    protected inline fun forEachReachablePos(game: BasicChessBoard, direction: Direction, informOf: (Position) -> Unit) {
        var currentPos: Position = position

        while (true) {
            currentPos = currentPos.step(direction) ?: return
            val figure = game.getFigureOrNull(currentPos)
            if (figure == null) {
                informOf(currentPos)
            } else {
                if (hasDifferentColor(figure)) {
                    informOf(currentPos)
                }
                return
            }
        }
    }

    protected inline fun forReachableTakeableEndPos(game: BasicChessBoard, direction: Direction, informOf: (Position) -> Unit) {
        game.getFirstFigureInDir(direction, position)?.let { figure ->
            if(figure.isWhite!=isWhite) {
                informOf(figure.position)
            }
        }
    }

    protected fun isAccessible(game: BasicChessBoard, position: Position) =
            game.getFigureOrNull(position).let { figure ->
                figure == null || hasDifferentColor(figure)
            }

    protected fun containsFigureToTake(game: BasicChessBoard, position: Position) =
            game.getFigureOrNull(position).let { figure ->
                figure != null && hasDifferentColor(figure)
            }

    override fun toString() = "${type.label}-${if (isWhite) "white" else "black"}-$position"
    override fun equals(other: Any?) = other is Figure && typeInfo == other.typeInfo && position.equalsPosition(other.position)
    override fun hashCode() = typeInfo
}