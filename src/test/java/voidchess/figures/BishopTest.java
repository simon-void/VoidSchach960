package voidchess.figures;

import org.testng.annotations.Test;
import voidchess.board.ChessGame;
import voidchess.board.LastMoveProvider;
import voidchess.board.SimpleArrayBoard;
import voidchess.helper.Move;
import voidchess.helper.Position;

import java.util.LinkedList;
import java.util.List;

import static org.mockito.Mockito.mock;
import static org.testng.Assert.*;

/**
 * @author stephan
 */
public class BishopTest {
    @Test
    public void testIsReachable() {
        String des = "white 0 Pawn-white-c2-false Bishop-white-d3- "
                + "Knight-black-b5";
        ChessGame game = new ChessGame(des);

        Position from = Position.Companion.byCode("d3");
        Position to1 = Position.Companion.byCode("b5");
        Position to2 = Position.Companion.byCode("h7");
        Position to3 = Position.Companion.byCode("f1");
        Position to4 = Position.Companion.byCode("c2");
        Position to5 = Position.Companion.byCode("a6");
        Position to6 = Position.Companion.byCode("b1");
        Position to7 = Position.Companion.byCode("d4");

        Bishop bishop = new Bishop(true, from);
        assertTrue(bishop.isReachable(to1, game));
        assertTrue(bishop.isReachable(to2, game));
        assertTrue(bishop.isReachable(to3, game));
        assertFalse(bishop.isReachable(to4, game));
        assertFalse(bishop.isReachable(to5, game));
        assertFalse(bishop.isReachable(to6, game));
        assertFalse(bishop.isReachable(to7, game));
        assertFalse(bishop.isReachable(from, game));
    }

    @Test
    public void testGetPossibleMoves() {
        String des = "white 0 Bishop-white-b1 King-white-e1-0 "
                + "Knight-white-f7";
        SimpleArrayBoard game = new SimpleArrayBoard(des, mock(LastMoveProvider.class));

        Figure bishop = game.getFigure(Position.Companion.byCode("b1"));
        List<Move> moveIter = new LinkedList<>();
        bishop.getPossibleMoves(game, moveIter);
        assertEquals(moveIter.size(), 7);
    }
}
