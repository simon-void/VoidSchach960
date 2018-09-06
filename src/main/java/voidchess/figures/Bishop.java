package voidchess.figures;


import voidchess.board.BasicChessGameInterface;
import voidchess.board.SimpleChessBoardInterface;
import voidchess.helper.Move;
import voidchess.helper.Position;
import voidchess.image.ImageType;

import java.util.ArrayList;
import java.util.List;

import static voidchess.helper.CheckSearch.signum;

/**
 * @author stephan
 */
public class Bishop extends Figure {
    private List<Position> positions = new ArrayList<Position>(13);

    public Bishop(boolean isWhite, Position startPosition) {
        super(isWhite, startPosition, FigureType.BISHOP);
    }

    public boolean isReachable(Position to, BasicChessGameInterface game) {
        final int row_difference = to.getRow() - getPosition().getRow();
        final int column_difference = to.getColumn() - getPosition().getColumn();

        final int abs_row_difference = Math.abs(row_difference);
        final int abs_column_difference = Math.abs(column_difference);

        if (abs_row_difference != abs_column_difference || abs_row_difference == 0) {
            return false;
        }

        final int row_step = signum(row_difference);
        final int column_step = signum(column_difference);

        int row = getPosition().getRow() + row_step;
        int column = getPosition().getColumn() + column_step;

        while (row != to.getRow()) {
            final Position pos = Position.Companion.get(row, column);
            if (!game.isFreeArea(pos)) {
                return false;
            }
            row += row_step;
            column += column_step;
        }

        final Figure hitFigure = game.getFigure(to);
        return hitFigure == null || hasDifferentColor(hitFigure);
    }

    private void getNorthEastIterator(BasicChessGameInterface game, List<Position> result) {
        int row, column;

        row = getPosition().getRow();
        column = getPosition().getColumn();
        while (true) {
            row++;
            column++;
            if (row == 8 || column == 8) break;
            Position checkPosition = Position.Companion.get(row, column);
            Figure figure = game.getFigure(checkPosition);
            if (figure == null) {
                result.add(checkPosition);
            } else {
                if (hasDifferentColor(figure)) {
                    result.add(checkPosition);
                }
                break;
            }
        }
    }

    private void getSouthEastIterator(BasicChessGameInterface game, List<Position> result) {
        int row, column;

        row = getPosition().getRow();
        column = getPosition().getColumn();
        while (true) {
            row--;
            column++;
            if (row < 0 || column == 8) break;
            Position checkPosition = Position.Companion.get(row, column);
            Figure figure = game.getFigure(checkPosition);
            if (figure == null) {
                result.add(checkPosition);
            } else {
                if (hasDifferentColor(figure)) {
                    result.add(checkPosition);
                }
                break;
            }
        }
    }

    private void getNorthWestIterator(BasicChessGameInterface game, List<Position> result) {
        int row, column;

        row = getPosition().getRow();
        column = getPosition().getColumn();
        while (true) {
            row++;
            column--;
            if (row == 8 || column < 0) break;
            Position checkPosition = Position.Companion.get(row, column);
            Figure figure = game.getFigure(checkPosition);
            if (figure == null) {
                result.add(checkPosition);
            } else {
                if (hasDifferentColor(figure)) {
                    result.add(checkPosition);
                }
                break;
            }
        }
    }

    private void getSouthWestIterator(BasicChessGameInterface game, List<Position> result) {
        int row, column;

        row = getPosition().getRow();
        column = getPosition().getColumn();
        while (true) {
            row--;
            column--;
            if (row < 0 || column < 0) break;
            Position checkPosition = Position.Companion.get(row, column);
            Figure figure = game.getFigure(checkPosition);
            if (figure == null) {
                result.add(checkPosition);
            } else {
                if (hasDifferentColor(figure)) {
                    result.add(checkPosition);
                }
                break;
            }
        }
    }

    public void getReachableMoves(BasicChessGameInterface game, List<Move> result) {
        positions.clear();
        getNorthEastIterator(game, positions);
        getSouthEastIterator(game, positions);
        getNorthWestIterator(game, positions);
        getSouthWestIterator(game, positions);

        for (Position pos : positions) {
            result.add(Move.Companion.get(getPosition(), pos));
        }
    }

    public boolean isSelectable(SimpleChessBoardInterface game) {
        positions.clear();
        getNorthEastIterator(game, positions);
        for (Position pos : positions) {
            if (!isBound(pos, game)) {
                return true;
            }
        }
        positions.clear();

        getSouthEastIterator(game, positions);
        for (Position pos : positions) {
            if (!isBound(pos, game)) {
                return true;
            }
        }
        positions.clear();

        getNorthWestIterator(game, positions);
        for (Position pos : positions) {
            if (!isBound(pos, game)) {
                return true;
            }
        }
        positions.clear();

        getSouthWestIterator(game, positions);
        for (Position pos : positions) {
            if (!isBound(pos, game)) {
                return true;
            }
        }
        positions.clear();

        return false;
    }

    public int countReachableMoves(BasicChessGameInterface game) {
        positions.clear();
        getNorthEastIterator(game, positions);
        getSouthEastIterator(game, positions);
        getNorthWestIterator(game, positions);
        getSouthWestIterator(game, positions);

        return positions.size();
    }
}
