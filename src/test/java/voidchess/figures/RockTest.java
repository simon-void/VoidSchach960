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
public class RockTest {
    @Test
    public void testIsReachable() {
        String des = "white 0 Pawn-white-c2-false Rock-white-c4-2 "
                + "Knight-black-g4";
        ChessGame game = new ChessGame(des);

        Position from = Position.Companion.get("c4");
        Position to1 = Position.Companion.get("c3");
        Position to2 = Position.Companion.get("g4");
        Position to3 = Position.Companion.get("c2");
        Position to4 = Position.Companion.get("h4");
        Position to5 = Position.Companion.get("d5");

        Rock rock = new Rock(true, from);
        assertTrue(rock.isReachable(to1, game));
        assertTrue(rock.isReachable(to2, game));
        assertFalse(rock.isReachable(to3, game));
        assertFalse(rock.isReachable(to4, game));
        assertFalse(rock.isReachable(to5, game));
        assertFalse(rock.isReachable(from, game));
    }

    @Test
    public void testUndoMove() {
        FigureFactory figureFactory = new FigureFactory();
        Rock rock = (Rock) figureFactory.getRock(Position.Companion.get("a1"), false);

        assertTrue(rock.canParticipateInRochade());
        rock.figureMoved(Move.get("a1-b1"));
        assertFalse(rock.canParticipateInRochade());
        rock.undoMove(Position.Companion.get("a1"));
        assertTrue(rock.canParticipateInRochade());
    }

    @Test
    public void testGetPossibleMoves() {
        String des = "white 0 King-white-e2-0 Pawn-black-b4-false "
                + "Rock-white-e4-2 King-black-e8-0";
        SimpleArrayBoard game = new SimpleArrayBoard(des, mock(LastMoveProvider.class));

        Figure rock = game.getFigure(Position.Companion.get("e4"));
        List<Move> moveIter = new LinkedList<>();
        rock.getPossibleMoves(game, moveIter);
        assertEquals(moveIter.size(), 11);
    }
}
