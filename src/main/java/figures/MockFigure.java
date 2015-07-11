package figures;

import board.BasicChessGameInterface;
import board.SimpleChessBoardInterface;
import helper.Move;
import helper.Position;
import image.ImageType;

import java.util.List;

/**
 * @author stephan
 */
class MockFigure extends Figure {
    MockFigure(boolean isWhite, Position position) {
        super(isWhite, position, (byte) 7);
    }

    public boolean isReachable(Position to, BasicChessGameInterface game) {
        return false;
    }

    public void getReachableMoves(BasicChessGameInterface game, List<Move> result) {
    }

    public boolean isSelectable(SimpleChessBoardInterface game) {
        return true;
    }

    public int countReachableMoves(BasicChessGameInterface game) {
        return 0;
    }

    protected String getType() {
        return "MockFigure";
    }

    public ImageType getImageType() {
        throw new IllegalStateException("MockFigure has no Image(Type)");
    }
}
