package voidchess.figures;

import voidchess.board.BasicChessGameInterface;
import voidchess.board.SimpleChessBoardInterface;
import voidchess.helper.CheckSearch;
import voidchess.helper.CheckStatus;
import voidchess.helper.Move;
import voidchess.helper.Position;
import voidchess.image.ImageType;

import java.util.LinkedList;
import java.util.List;

/**
 * @author stephan
 */
public abstract class Figure {
    //Farbe der Figur
    final protected boolean isWhite;
    //kodiert Name der Figur + Farbe
    final protected byte typeInfo;
    //Position der Figur
    protected Position position;

    private List<Move> reachableMoves = new LinkedList<>();

    Figure(boolean isWhite, Position startPosition, byte typeIndex) {
        this.position = startPosition;
        this.isWhite = isWhite;
        this.typeInfo = computeTypeInfo(typeIndex, isWhite);

        assert typeInfo > 0 && typeInfo < 16
                : "TypeInfo out of Bounds";
    }

    public Position getPosition() {
        return position;
    }

    abstract public ImageType getImageType();

    final public boolean hasDifferentColor(Figure other) {
        return isWhite != other.isWhite;
    }

    public boolean canBeHitByEnpasent() {
        return false;
    }

    final public String toBasicString() {
        StringBuilder buffer = new StringBuilder(12);
        buffer.append(getType());
        buffer.append("-");
        if (isWhite) buffer.append("white");
        else buffer.append("black");
        buffer.append("-");
        buffer.append(position.toString());
        return buffer.toString();
    }

    //kodiert Name der Klasse + Farbe
    final public byte getTypeInfo() {
        return typeInfo;
    }

    final private byte computeTypeInfo(byte typeIndex, boolean isWhite) {
        final byte colorvalue = 7;
        assert typeIndex >= 1 && typeIndex <= colorvalue;

        return (byte) (typeIndex + (isWhite ? 0 : colorvalue));
    }

    //gibt nur den Name der Klasse zur�ck(ohne package-Namen)
    abstract protected String getType();

    public String toString() {
        return toBasicString();
    }

    final public boolean isWhite() {
        return isWhite;
    }

    public void figureMoved(Move move) {
        if (position.equalsPosition(move.from)) position = move.to;
    }

    public void undoMove(Position oldPosition) {
        position = oldPosition;
    }

    boolean canParticipateInRochade() {
        return false;
    }

    abstract public boolean isReachable(Position to, BasicChessGameInterface game);

    protected boolean isPassiveBound(Position to, SimpleChessBoardInterface game) {
        Position kingPos = game.getKingPosition(isWhite);
        //falls diese Figur nicht mit dem K�nig auf einer Vertikalen,Horizontalen oder
        //Diagonalen steht, ist sie nicht gebunden
        if (!CheckSearch.areDiagonalPositions(position, kingPos)
                && !CheckSearch.areStraightPositions(position, kingPos)) {
            return false;
        }

        final int row_step = CheckSearch.signum(kingPos.row - position.row);
        final int column_step = CheckSearch.signum(kingPos.column - position.column);

        int row = position.row + row_step;
        int column = position.column + column_step;
        boolean isToPositionInBetweenKingAndAttacker = false;

        //falls eine Figure zwischen K�nig und dieser steht, ist diese nicht gebunden
        while (row != kingPos.row || column != kingPos.column) {
            Position middlePos = Position.get(row, column);
            if (!game.isFreeArea(middlePos)) return false;
            if (middlePos.equalsPosition(to)) isToPositionInBetweenKingAndAttacker = true;
            row += row_step;
            column += column_step;
        }

        row = position.row - row_step;
        column = position.column - column_step;

        //nur falls in Verl�ngerung der K�nigslinie eine feindliche Figur steht(Dame,L�ufer,Turm)
        //und das Ziel des Zuges nicht auf dieser Linie liegt, ist diese Figur gebunden
        while (row >= 0 && row < 8 && column >= 0 && column < 8) {
            Position middlePos = Position.get(row, column);
            if (middlePos.equalsPosition(to)) isToPositionInBetweenKingAndAttacker = true;
            if (!game.isFreeArea(middlePos)) {
                Figure figure = game.getFigure(middlePos);
                if (hasDifferentColor(figure)) {
                    if (figure.isQueen() ||
                            ((row_step == 0 || column_step == 0) && figure.isRock()) ||
                            ((row_step != 0 && column_step != 0) && figure.isBishop())) {
                        return !isToPositionInBetweenKingAndAttacker;
                    }
                }
                return false;
            }
            row -= row_step;
            column -= column_step;
        }
        return false;
    }

    //isReachalble=true wird vorrausgesetzt
    final public boolean isBound(Position to, SimpleChessBoardInterface game) {
        CheckStatus checkStatus = game.getCheckStatus(isWhite);
        return isBound(to, game, checkStatus);
    }

    final public boolean isBound(Position to, SimpleChessBoardInterface game, CheckStatus checkStatus) {
        if (checkStatus.onlyKingCanMove()) { //Doppelschach
            return !isKing() || isPassiveBound(to, game);
        }
        if (checkStatus.isCheck()) {     //einfaches Schach
            if (isKing()) {
                return isPassiveBound(to, game);
            }
            return !checkStatus.getCheckInterceptPositions().contains(to) || isPassiveBound(to, game);
        } else {                 //kein Schach
            return isPassiveBound(to, game);
        }
    }

    final public boolean isMoveable(Position to, SimpleChessBoardInterface game) {
        return isReachable(to, game) && !isBound(to, game);
    }

    abstract public int countReachableMoves(BasicChessGameInterface game);

    abstract public void getReachableMoves(BasicChessGameInterface game, List<Move> result);

    final public void getReachableMoves(
            BasicChessGameInterface game,
            List<Position> restrictedPositions,
            List<Move> result) {
        for (Position to : restrictedPositions) {
            if (isReachable(to, game)) {
                result.add(Move.get(position, to));
            }
        }
    }

    final public void getPossibleMoves(SimpleChessBoardInterface game, List<Move> result) {
        reachableMoves.clear();
        getReachableMoves(game, reachableMoves);
        final CheckStatus checkStatus = game.getCheckStatus(isWhite);

        for (Move move : reachableMoves) {
            Position checkPosition = move.to;
            if (!isBound(checkPosition, game, checkStatus)) {
                result.add(move);
            }
        }
    }

    abstract public boolean isSelectable(SimpleChessBoardInterface game);

    public boolean equals(Figure other) {
        return toString().equals(other.toString());
    }

    public boolean isPawn() {
        return false;
    }

    public boolean isRock() {
        return false;
    }

    public boolean isKnight() {
        return false;
    }

    public boolean isBishop() {
        return false;
    }

    public boolean isQueen() {
        return false;
    }

    public boolean isKing() {
        return false;
    }
}
