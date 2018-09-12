package voidchess.board;

import org.testng.annotations.BeforeMethod;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;
import voidchess.figures.Figure;
import voidchess.helper.CheckStatus;
import voidchess.helper.Position;

import java.util.List;

import static org.mockito.Mockito.mock;
import static org.testng.Assert.*;

/**
 * @author stephan
 */
public class SimpleBoardTest {
    private SimpleChessBoardInterface board;
    final private String initial = "Rock-white-a1-0 Knight-white-b1 Bishop-white-c1 " +
            "Queen-white-d1 King-white-e1-0 Bishop-white-f1 Knight-white-g1 Rock-white-h1-0 " +
            "Pawn-white-a2-false Pawn-white-b2-false Pawn-white-c2-false Pawn-white-d2-false " +
            "Pawn-white-e2-false Pawn-white-f2-false Pawn-white-g2-false Pawn-white-h2-false " +
            "Pawn-black-a7-false Pawn-black-b7-false Pawn-black-c7-false Pawn-black-d7-false " +
            "Pawn-black-e7-false Pawn-black-f7-false Pawn-black-g7-false Pawn-black-h7-false " +
            "Rock-black-a8-0 Knight-black-b8 Bishop-black-c8 " +
            "Queen-black-d8 King-black-e8-0 Bishop-black-f8 Knight-black-g8 Rock-black-h8-0";

    @BeforeMethod
    public void setUp() {
        board = new SimpleArrayBoard(mock(LastMoveProvider.class));
    }

    @Test
    public void testInit() {
        Figure figure = board.getFigure(Position.Companion.byCode("a1"));
        assertTrue(figure.isRock());
        assertTrue(figure.isWhite());

        figure = board.getFigure(Position.Companion.byCode("e8"));
        assertTrue(figure.isKing());
        assertFalse(figure.isWhite());

        figure = board.getFigure(Position.Companion.byCode("e3"));
        assertNull(figure);
    }

    @Test
    public void testInit960() {
        board.init(518);
        assertEquals(board.toString(), initial);

        board.init(0);
        board.init(314);
        board.init(959);
        try {
            board.init(960);
            fail("AssertionError should have been thrown:Argument bigger than 959");
        } catch (AssertionError e) {
        }
        try {
            board.init(-1);
            fail("AssertionError should have been thrown:Argument smaller than 0");
        } catch (AssertionError e) {
        }
    }

    @Test
    public void testIsFreeArea() {
        assertTrue(board.isFreeArea(Position.Companion.byCode("a3")));
        assertFalse(board.isFreeArea(Position.Companion.byCode("a2")));
    }

    @Test
    public void testSetFigure() {
        Figure figure = board.getFigure(Position.Companion.byCode("b1"));
        Position to = Position.Companion.byCode("e4");
        board.setFigure(to, figure);
        assertTrue(board.getFigure(to).isKnight());
    }

    @Test
    public void testGetFigures() {
        board.setFigure(Position.Companion.byCode("c2"), null);
        List<Figure> figures = board.getFigures();
        assertEquals(figures.size(), 31);
    }

    @Test
    public void testGetCheckStatus() {
        CheckStatus status = board.getCheckStatus(true);
        assertFalse(status.isCheck());

        String des = "white 0 King-white-e1-0 Rock-white-h2-1 Queen-black-b4";
        board.init(des);
        status = board.getCheckStatus(true);
        assertTrue(status.isCheck());
        assertFalse(status.onlyKingCanMove());
        assertEquals(status.getCheckInterceptPositions().size(), 3);

        des = "white 0 King-white-e2-3 Knight-black-g1 "
                + "Knight-white-f3 Rock-black-h2-12 King-black-g7-3";
        board.init(des);
        status = board.getCheckStatus(true);
        assertTrue(status.isCheck());
        assertTrue(status.onlyKingCanMove());
    }

    @Test(dataProvider = "getTestMoveUndoMoveInvarianceData")
    public void testMoveUndoMoveInvariance(String fromCode, String toCode, String gameDes) {
        board.init(gameDes);
        String initialLongGameDescription = board.toString();
        int initialFigureCount = board.getFigures().size();
        Position from = Position.Companion.byCode(fromCode);
        Position to = Position.Companion.byCode(toCode);

        Figure figure = board.getFigure(from);
        assertNotNull(figure, "figure to move");
        boolean canDoRochadeInitially = figure.canParticipateInRochade();
        boolean willTakeFigure = !board.isFreeArea(to);

        Figure figureTaken = board.move(figure, to);

        boolean wasFigureTaken = figureTaken != null;
        assertEquals(wasFigureTaken, willTakeFigure, "figure taken");
        assertEquals(board.getFigures().size(), wasFigureTaken?initialFigureCount-1:initialFigureCount);
        assertEquals(board.getFigure(to), figure, "figure after move");
        assertEquals(figure.getPosition(), to, "figure position after move");
        assertFalse(figure.canParticipateInRochade(), "after a move, yuo can't do rochade garanteed");

        board.undoMove(figure, from, figureTaken);

        assertEquals(board.toString(), initialLongGameDescription, "game state after move-undo");
        assertEquals(figure.getPosition(), from);
        assertEquals(figure.canParticipateInRochade(), canDoRochadeInitially, "same rochade state as before");
    }

    @DataProvider
    public Object[][] getTestMoveUndoMoveInvarianceData() {
        return new Object[][] {
                new Object[] {"e1", "d2", "white 0 King-white-e1-0 Queen-black-d2"},
                new Object[] {"e1", "f1", "white 0 King-white-e1-0 Queen-black-d2"},
        };
    }

    @Test
    public void testToString() {
        assertEquals(board.toString(), initial);
    }
}
