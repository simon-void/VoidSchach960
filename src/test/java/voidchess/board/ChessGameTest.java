package voidchess.board;

import org.testng.annotations.BeforeMethod;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;
import voidchess.figures.Bishop;
import voidchess.figures.Figure;
import voidchess.figures.King;
import voidchess.helper.*;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

import static org.testng.Assert.*;


public class ChessGameTest {
    private ChessGame game;

    @BeforeMethod
    public void setUp() {
        game = new ChessGame(ChessGameSupervisorDummy.INSTANCE);
    }

    @Test
    public void testIsFreeArea() {
        assertTrue(game.isFreeArea(Position.byCode("a3")));
        assertFalse(game.isFreeArea(Position.byCode("c1")));
        assertFalse(game.isFreeArea(Position.byCode("d7")));
    }

    @Test
    public void testGetFigure() {
        assertTrue(game.getFigure(Position.byCode("e3")) == null);
        Figure king = game.getFigure(Position.byCode("e1"));
        assertTrue(king instanceof King);
        assertTrue(king.isWhite());
        Figure bishop = game.getFigure(Position.byCode("c8"));
        assertTrue(bishop instanceof Bishop);
        assertFalse(bishop.isWhite());
    }

    @Test
    public void testToString() {
        String code = "white 0 "
                + "Rook-white-a1-0 Knight-white-b1 Bishop-white-c1 "
                + "Queen-white-d1 King-white-e1-0 "
                + "Bishop-white-f1 Knight-white-g1 Rook-white-h1-0 "
                + "Pawn-white-a2-false Pawn-white-b2-false Pawn-white-c2-false "
                + "Pawn-white-d2-false Pawn-white-e2-false Pawn-white-f2-false "
                + "Pawn-white-g2-false Pawn-white-h2-false "
                + "Pawn-black-a7-false Pawn-black-b7-false Pawn-black-c7-false "
                + "Pawn-black-d7-false Pawn-black-e7-false Pawn-black-f7-false "
                + "Pawn-black-g7-false Pawn-black-h7-false "
                + "Rook-black-a8-0 Knight-black-b8 Bishop-black-c8 "
                + "Queen-black-d8 King-black-e8-0 "
                + "Bishop-black-f8 Knight-black-g8 Rook-black-h8-0";
        assertEquals(game.toString(), code);
    }

    @Test
    public void testEquals() {
        ChessGame copy = new ChessGame(game.toString());
        assertTrue(game.equals(copy));
    }

    @Test
    public void testCopy() {
        List<ChessGameInterface> copies = game.copyGame(4);

        for (ChessGameInterface copy : copies) {
            assertEquals(copy.toString(), game.toString(), "copy and game");
        }
    }

    @Test(dependsOnMethods = {"testIsDrawBecauseOfThreeTimesSamePosition", "testCopy"})
    public void testDeepCopy() {
        Move whiteMove = Move.byCode("g1-f3");
        Move whiteReturn = Move.byCode("f3-g1");
        Move blackMove = Move.byCode("b8-c6");
        Move blackReturn = Move.byCode("c6-b8");

        game.move(whiteMove);
        game.move(blackMove);
        game.move(whiteReturn);
        game.move(blackReturn);
        game.move(whiteMove);
        game.move(blackMove);
        game.move(whiteReturn);
        game.move(blackReturn);
        game.move(whiteMove);
        game.move(blackMove);
        game.move(whiteReturn);

        ChessGameInterface copy = game.copyGame(3).get(1);
        MoveResult gameState = copy.move(blackReturn);

        assertEquals(gameState, MoveResult.THREE_TIMES_SAME_POSITION, "game state");
    }

    @Test
    public void testGetFigures() {
        String des = "white 0 King-white-e1-0 Bishop-black-g2 Bishop-white-b2 "
                + "Knight-white-c2 Knight-white-e7 King-black-e8-0";
        ChessGame game = new ChessGame(des);
        assertEquals(game.getFigures().size(), 6);
    }

    @Test
    public void testMove() {
        String des = "white 0 King-white-e1-0 Pawn-white-c2-false King-black-e8-0";
        ChessGame game = new ChessGame(des);
        Move move = Move.get(Position.byCode("c2"), Position.byCode("c4"));
        game.move(move);
        String new_des = "black 1 King-white-e1-0 Pawn-white-c4-true King-black-e8-0";
        assertEquals(game.toString(), new_des);
    }

    @Test
    public void testUndo() {
        String des = "white 0 King-white-e1-0 Rook-white-h1-0 King-black-e8-0";
        ChessGame game = new ChessGame(des);
        Position pos1 = Position.byCode("e1");
        Position pos2 = Position.byCode("h1");
        Move move = Move.get(pos1, pos2);
        game.move(move);
        String new_des = "black 1 Rook-white-f1-1 King-white-g1-1-true King-black-e8-0";

        assertEquals(game.toString(), new_des);
        game.undo();
        assertEquals(game.toString(), des);
        assertTrue(game.isMovable(pos1, pos2, true));


        des = "black 0 King-white-e1-0 Rook-black-a8-0 King-black-f8-0";
        game = new ChessGame(des);
        pos1 = Position.byCode("f8");
        pos2 = Position.byCode("g8");
        move = Move.get(pos1, pos2);
        game.move(move);
        game.undo();

        assertEquals(game.toString(), des);
        assertTrue(game.isMovable(pos1, pos2, false));


        des = "white 0 King-white-e1-0 Pawn-white-b5-false Pawn-black-c5-true King-black-e8-0";
        game = new ChessGame(des);
        pos1 = Position.byCode("b5");
        pos2 = Position.byCode("c6");
        move = Move.get(pos1, pos2);

        assertTrue(game.isMovable(pos1, pos2, true));
        game.move(move);
        game.undo();
        assertTrue(game.isMovable(pos1, pos2, true));


        des = "white 4 Rook-white-a1-0 King-white-e1-0 Bishop-white-f1 " +
                "Rook-white-h1-0 Pawn-white-b2-false Queen-white-d2 " +
                "Pawn-white-f2-false Pawn-white-g2-false Pawn-white-h2-false " +
                "Pawn-white-a3-false Knight-white-c3 Knight-white-f3 " +
                "Pawn-white-c4-false Pawn-black-h4-false Pawn-white-e5-false " +
                "Bishop-black-a6 Pawn-black-b6-false Knight-black-c6 " +
                "Pawn-black-e6-false Pawn-black-h6-false Pawn-black-a7-false " +
                "Pawn-black-c7-false Pawn-black-d7-false Pawn-black-f7-false " +
                "Rook-black-a8-0 Queen-black-d8 King-black-e8-0 " +
                "Rook-black-h8-0";
        game = new ChessGame(des);
        pos1 = Position.byCode("e1");
        pos2 = Position.byCode("a1");
        move = Move.get(pos1, pos2);

        assertTrue(game.isMovable(pos1, pos2, true));
        game.move(move);
        game.undo();
        assertTrue(game.isMovable(pos1, pos2, true));


        des = "black 1 King-white-h1-4 King-black-a6-6 Pawn-white-b6-false";
        game = new ChessGame(des);
        move = Move.byCode("a6-b6");
        game.move(move);
        game.undo();

        assertEquals(game.toString(), des);


        des = "white 1 King-white-h1-4 Pawn-white-a2-false King-black-a6-6";
        game = new ChessGame(des);
        move = Move.byCode("a2-a4");
        game.move(move);
        game.undo();

        assertEquals(game.toString(), des);


        des = "white 0 King-white-h1-4 Pawn-white-a2-false Pawn-black-b4-false King-black-a6-6";
        game = new ChessGame(des);
        move = Move.byCode("a2-a4");
        game.move(move);
        String newDes = "black 1 King-white-h1-4 Pawn-white-a4-true Pawn-black-b4-false King-black-a6-6";

        move = Move.byCode("b4-a3");
        game.move(move);
        move = Move.byCode("h1-h2");
        game.move(move);
        move = Move.byCode("a3-a2");
        game.move(move);
        game.undo();
        game.undo();
        game.undo();

        assertEquals(game.toString(), newDes);

        move = Move.byCode("b4-b3");
        game.move(move);
        move = Move.byCode("h1-h2");
        game.move(move);
        move = Move.byCode("b3-b2");
        game.move(move);
        game.undo();
        game.undo();
        game.undo();

        assertEquals(game.toString(), newDes);


        des = "white 0 King-white-h1-3 King-black-b7-3 Pawn-white-c7-false";
        game = new ChessGame(des);
        move = Move.byCode("c7-c8");
        game.move(move);
        game.undo();

        assertEquals(game.toString(), des);


        game = new ChessGame(621);
        move = Move.byCode("f2-f3");
        game.move(move);
        move = Move.byCode("b7-b6");
        game.move(move);
        move = Move.byCode("g1-b6");
        game.move(move);
        move = Move.byCode("a7-a6");
        game.move(move);
        des = game.toString();
        move = Move.byCode("e1-f1");
        game.move(move);
        game.undo();
        assertEquals(game.toString(), des);
        move = Move.byCode("f1-f2");
        game.move(move);

        game = new ChessGame(314);
        des = game.toString();
        move = Move.byCode("d1-c1");
        game.move(move);
        game.undo();
        assertEquals(game.toString(), des);

        game = new ChessGame(707);
        game.move(Move.byCode("e1-f3"));
        game.move(Move.byCode("b7-b6"));
        game.move(Move.byCode("f1-e3"));
        game.move(Move.byCode("g7-g6"));
        game.move(Move.byCode("d1-e1"));
        game.move(Move.byCode("a8-f3"));
        des = game.toString();
        game.move(Move.byCode("c1-b1"));
        game.undo();
        assertEquals(game.toString(), des);
    }

    @Test
    public void testHandleEnpasent() {
        String des = "black 0 Pawn-white-c4-true Pawn-black-b4-false "
                + "King-white-e1-0 King-black-e8-0";
        ChessGame game = new ChessGame(des);
        Move move = Move.get(Position.byCode("b4"), Position.byCode("c3"));
        game.move(move);
        String new_des = "white 0 King-white-e1-0 Pawn-black-c3-false "
                + "King-black-e8-0";
        assertEquals(game.toString(), new_des);
    }

    @Test
    public void testHandleCastling() {
        String des = "black 0 King-white-e1-0 Rook-black-a8-0 King-black-e8-0 ";
        ChessGame game = new ChessGame(des);
        Move move = Move.get(Position.byCode("e8"), Position.byCode("a8"));
        game.move(move);
        String new_des = "white 1 King-white-e1-0 King-black-c8-1-true Rook-black-d8-1";
        assertEquals(game.toString(), new_des);

        des = "black 0 King-white-e1-0 Rook-black-a8-0 King-black-f8-0 ";
        game = new ChessGame(des);
        move = Move.get(Position.byCode("f8"), Position.byCode("a8"));
        game.move(move);
        new_des = "white 1 King-white-e1-0 King-black-c8-1-true Rook-black-d8-1";
        assertEquals(game.toString(), new_des);

        des = "white 0 King-white-e1-0 Rook-white-h1-0 King-black-e8-0 ";
        game = new ChessGame(des);
        move = Move.get(Position.byCode("e1"), Position.byCode("h1"));
        game.move(move);
        new_des = "black 1 Rook-white-f1-1 King-white-g1-1-true King-black-e8-0";
        assertEquals(game.toString(), new_des);

        des = "white 0 King-white-g1-0 Rook-white-h1-0 King-black-e8-0 ";
        game = new ChessGame(des);
        move = Move.get(Position.byCode("g1"), Position.byCode("h1"));
        game.move(move);
        new_des = "black 1 Rook-white-f1-1 King-white-g1-1-true King-black-e8-0";
        assertEquals(game.toString(), new_des);

        des = "white 0 King-white-f1-0 Rook-white-g1-0 King-black-e8-0 ";
        game = new ChessGame(des);
        move = Move.get(Position.byCode("f1"), Position.byCode("g1"));
        game.move(move);
        new_des = "black 1 Rook-white-f1-1 King-white-g1-1-true King-black-e8-0";
        assertEquals(game.toString(), new_des);
    }

    @Test
    public void testIsMatt() {
        String des = "black 0 King-white-e1-0 Queen-black-h2 "
                + "Pawn-black-f3-false King-black-e8-0";
        ChessGame game = new ChessGame(des);
        MoveResult endoption = game.move(Move.byCode("h2-e2"));
        assertTrue(endoption == MoveResult.CHECKMATE);
    }

    @Test
    public void testIsDrawBecauseOfNoMoves() {
        String des = "black 0 King-white-e1-0 Queen-black-h2 "
                + "Pawn-black-c2-false Pawn-white-e7-false King-black-e8-0";
        ChessGame game = new ChessGame(des);
        MoveResult endoption = game.move(Move.byCode("h2-g2"));
        assertTrue(endoption == MoveResult.STALEMATE);
    }

    @Test
    public void testIsDrawBecauseOfLowMaterial() {
        String des = "white 0 King-white-e1-0 Bishop-black-g2 "
                + "Knight-white-c2 Knight-white-e7 King-black-e8-0";
        ChessGame game = new ChessGame(des);
        MoveResult endoption = game.move(Move.byCode("e1-f2"));
        assertTrue(endoption == MoveResult.DRAW);
    }

    @Test
    public void testIsDrawBecauseOfThreeTimesSamePosition() {
        String des = "white 0 King-white-e1-0 Bishop-black-g2 Bishop-white-b2 Knight-white-c2 Knight-white-e7 King-black-e8-0";
        ChessGame game = new ChessGame(des);
        Move whiteMove = Move.byCode("c2-a1");
        Move whiteReturn = Move.byCode("a1-c2");
        Move blackMove = Move.byCode("g2-h3");
        Move blackReturn = Move.byCode("h3-g2");

        assertEquals(game.move(whiteMove), MoveResult.NO_END);
        assertEquals(game.move(blackMove), MoveResult.NO_END);
        assertEquals(game.move(whiteReturn), MoveResult.NO_END);
        assertEquals(game.move(blackReturn), MoveResult.NO_END);

        assertEquals(game.move(whiteMove), MoveResult.NO_END);
        assertEquals(game.move(blackMove), MoveResult.NO_END);
        assertEquals(game.move(whiteReturn), MoveResult.NO_END);
//        assertEquals(game.move(blackReturn), MoveResult.NO_END);
        assertEquals(game.move(blackReturn), MoveResult.THREE_TIMES_SAME_POSITION);

//        assertEquals(game.move(whiteMove), MoveResult.THREE_TIMES_SAME_POSITION);
    }

    @Test
    public void testIsDrawBecauseOf50HitlessMoves() {
        String des = "white 98 King-white-e1-0 Pawn-white-a2-false "
                + "Pawn-black-b4-false King-black-e8-0";
        ChessGame game = new ChessGame(des);
        MoveResult endoption;
        endoption = game.move(Move.byCode("a2-a4"));
        assertFalse(endoption == MoveResult.FIFTY_MOVES_NO_HIT);
        endoption = game.move(Move.byCode("b4-a3"));
        assertFalse(endoption == MoveResult.FIFTY_MOVES_NO_HIT);

        des = "white 98 King-white-e1-0 Pawn-white-a2-false "
                + "Pawn-black-b4-false King-black-e8-0";
        game = new ChessGame(des);
        endoption = game.move(Move.byCode("a2-a4"));
        assertFalse(endoption == MoveResult.FIFTY_MOVES_NO_HIT);
        endoption = game.move(Move.byCode("b4-b3"));
        assertTrue(endoption == MoveResult.FIFTY_MOVES_NO_HIT);
    }

    @Test
    public void testHandleTransformPawn() {
        ChessGameSupervisorMock mock = new ChessGameSupervisorMock(PawnPromotion.KNIGHT);
        String des = "black 0 King-white-e1-0 Pawn-black-g2-false "
                + "King-black-e8-0";
        ChessGame game = new ChessGame(des);
        game.useSupervisor(mock);
        game.move(Move.byCode("g2-g1"));
        String new_des = "white 1 King-white-e1-0 Knight-black-g1 "
                + "King-black-e8-0";
        assertEquals(game.toString(), new_des);
    }

    @Test
    public void testIsSelectable() {
        String des = "white 0 King-white-e1-0 Queen-black-g2 "
                + "Pawn-black-c2-false Pawn-white-e6-false King-black-e8-0";
        ChessGame game = new ChessGame(des);
        assertFalse(game.isSelectable(Position.byCode("e1"), true));
        assertTrue(game.isSelectable(Position.byCode("e6"), true));
        assertFalse(game.isSelectable(Position.byCode("e6"), false));

        des = "black 0 King-white-e1-0 Queen-white-e2 "
                + "Bishop-black-c8 King-black-e8-0";
        game = new ChessGame(des);
        assertTrue(game.isSelectable(Position.byCode("c8"), false));

        des = "black 0 King-white-e1-0 Queen-white-e2 "
                + "Rook-black-a6-1 King-black-e8-0";
        game = new ChessGame(des);
        assertTrue(game.isSelectable(Position.byCode("a6"), false));
    }

    @Test
    public void testIsMovable() {
        String des = "white 0 King-white-e1-0 Queen-black-g2 "
                + "Pawn-black-c2-false Pawn-white-e6-false King-black-e8-0";
        ChessGame game = new ChessGame(des);


        assertTrue(game.isMovable(Position.byCode("e6"), Position.byCode("e7"), true));

        des = "black 0 King-white-e1-0 Pawn-black-a5-false "
                + "King-black-g6-2 Rook-white-h6-1";
        game = new ChessGame(des);
        assertFalse(game.isMovable(Position.byCode("a5"), Position.byCode("a4"), false));

        des = "black 0 King-white-g7-6 King-black-e8-0 Rook-black-h8-0";
        game = new ChessGame(des);
        assertFalse(game.isMovable(Position.byCode("e8"), Position.byCode("g8"), false));

        game = new ChessGame(621);
        game.move(Move.byCode("f2-f3"));
        game.move(Move.byCode("a7-a6"));
        Position from = Position.byCode("f1");
        Position to = Position.byCode("f2");
        assertTrue(game.isMovable(from, to, true));
        assertFalse(game.isFreeArea(from));
    }

    @Test
    public void testColorChangedBetweenMoves() {
        String des = "white 0 King-white-e1-0 Pawn-black-g3-false "
                + "King-black-e8-0";
        ChessGame game = new ChessGame(des);
        game.move(Move.byCode("e1-d1"));
        try {
            game.move(Move.byCode("d1-c1"));
            fail();
        } catch (AssertionError e) {
        }
    }

    @Test
    public void testChecksForMoveMovesFigureNotNull() {
        String des = "white 0 King-white-e1-0 Pawn-black-g3-false "
                + "King-black-e8-0";
        ChessGame game = new ChessGame(des);
        try {
            game.move(Move.byCode("a1-b1"));
            fail();
        } catch (AssertionError e) {
        }
    }

    @Test
    public void testHasHitBiggerFigure() {
        String des = "white 0 King-white-h2-3 Queen-black-h3 "
                + "Pawn-white-g2-false Bishop-white-e7 King-black-e8-0 "
                + "Knight-black-g5 Pawn-white-a6-false";
        ChessGame game = new ChessGame(des);
        assertTrue(game.hasHitFigure());            //da numberWithoutHit=0 ist in 'des'
        game.move(Move.byCode("g2-h3"));
        assertTrue(game.hasHitFigure());
        game.move(Move.byCode("g5-h3"));
        assertTrue(game.hasHitFigure());
        game.move(Move.byCode("h2-h3"));
        assertTrue(game.hasHitFigure());
        game.move(Move.byCode("e8-e7"));
        assertTrue(game.hasHitFigure());
        game.move(Move.byCode("a6-a7"));
        assertFalse(game.hasHitFigure());
        game.move(Move.byCode("e7-d7"));
        assertFalse(game.hasHitFigure());
    }

    @Test
    public void testCountFigures() {
        String des = "white 0 King-white-e1-0 Pawn-black-a5-true "
                + "Pawn-white-b5-false Pawn-white-e7-false King-black-e8-0";
        ChessGame game = new ChessGame(des);
        assertTrue(game.countFigures() == 5);
        game.move(Move.byCode("b5-a6"));
        assertTrue(game.countFigures() == 4);
        game.move(Move.byCode("e8-e7"));
        assertTrue(game.countFigures() == 3);
        game.undo();
        assertTrue(game.countFigures() == 4);
        game.undo();
        assertTrue(game.countFigures() == 5);
    }

    @Test(dataProvider = "getTestGetPossibleMovesData")
    public void testGetPossibleMoves(ChessGame game, List<String> moveCodes, int expectedPossibleMovesCount) {
        List<Move> moves = moveCodes.stream().map(Move.Companion::byCode).collect(Collectors.toList());
        for(Move move: moves) {
            Position from = move.from;
            Position to = move.to;
            boolean isWhiteTurn = game.isWhiteTurn();
            boolean isMovable = game.isMovable(from, to, isWhiteTurn);
            assertTrue(isMovable, move + " should be valid");
            game.move(move);
        }
        List<Move> possibleMoves = getPossibleMoves(game);
        assertEquals(possibleMoves.size(), expectedPossibleMovesCount, "possible move count");
    }

    @DataProvider
    public Object[][] getTestGetPossibleMovesData() {
        return new Object[][] {
                new Object[] {new ChessGame(518), Arrays.asList("g1-f3", "b8-c6", "f3-g1", "c6-b4", "g1-f3", "b4-c2"), 1},
                new Object[] {new ChessGame("black 0 King-white-g1-2 Bishop-black-b6 King-black-e8-0"), Arrays.asList("b6-c5"), 4},
                new Object[] {new ChessGame("black 0 Rook-white-a1-0 Rook-white-f1-1 King-white-g1-1-true "
                        + "Pawn-white-a2-false Pawn-white-b2-false Bishop-white-d2 Bishop-white-e2 "
                        + "Pawn-white-f2-false Pawn-white-h2-false Queen-white-b3 Pawn-white-g3-false "
                        + "Pawn-white-e4-false Pawn-black-b5-false Pawn-black-a6-false Bishop-black-b6 "
                        + "Pawn-black-h6-false Bishop-black-b7 Pawn-black-f7-false Pawn-black-g7-false "
                        + "Rook-black-c8-1 Queen-black-d8 Rook-black-f8-1 King-black-g8-1"), Arrays.asList("b6-f2"), 4},
                new Object[] {new ChessGame("black 0 Pawn-white-b2-false King-white-d3-2 Rook-black-h4-1 " +
                        "Rook-black-a8-0 King-black-e8-0"), Arrays.asList("a8-a3"), 5},
                new Object[] {new ChessGame("black 0 King-white-d3-2 Knight-black-e5 Bishop-black-g8 King-black-e8-0"), Arrays.asList("g8-h7"), 5},
                new Object[] {new ChessGame("white 2 Rook-white-a1-0 Knight-white-b1 Bishop-white-c1 King-white-e1-0 " +
                        "Queen-white-d1 Bishop-white-f1 Knight-white-g1 Rook-white-h1-0 Pawn-white-a2-false " +
                        "Pawn-white-b2-false Pawn-white-d2-false Pawn-white-e2-false " +
                        "Pawn-white-f2-false Pawn-white-g2-false Pawn-white-h2-false " +
                        "Pawn-white-c3-false Pawn-black-d6-false Pawn-black-a7-false " +
                        "Pawn-black-b7-false Pawn-black-c7-false Pawn-black-e7-false Pawn-black-f7-false " +
                        "Pawn-black-g7-false Pawn-black-h7-false Rook-black-a8-0 Knight-black-b8 " +
                        "Bishop-black-c8 Queen-black-d8 King-black-e8-0 Bishop-black-f8 Knight-black-g8 " +
                        "Rook-black-h8-0"), Arrays.asList("d1-a4"), 6},
                new Object[] {new ChessGame("black 0 King-white-e1-0 Rook-white-d2-2 Queen-black-e2 " +
                        "Bishop-black-b4 King-black-e8-0"), Arrays.asList("b4-c3"), 1},
                new Object[] {new ChessGame("black 0 King-white-g1-2 Pawn-black-c4-false Pawn-white-d4-true " +
                        "Bishop-black-b6 King-black-e8-0"), Arrays.asList("c4-d3"), 4},
                new Object[] {new ChessGame("black 0 King-white-h1-3 Pawn-white-c7-false "
                        + "Pawn-black-b5-false Pawn-black-d5-false Pawn-black-b6-false Pawn-black-d6-false "
                        + "Knight-black-a7 King-black-b7-3-false"), Arrays.asList("b7-c6", "c7-c8"), 1},
                new Object[] {new ChessGame("black 0 King-white-g7-6 King-black-e8-0 Rook-black-h8-0"), Arrays.asList(), 12},
                new Object[] {new ChessGame("white 0 King-white-g6-6 Pawn-white-g7-false King-black-e8-0 Knight-black-h8"), Arrays.asList(), 7},
                new Object[] {new ChessGame("white 0 Rook-white-b1-0 King-white-d1-0 Rook-white-e1-0 Rook-black-h1-1 Rook-black-a2-1 Knight-black-d3 King-black-d8-0"), Arrays.asList(), 12},
                new Object[] {new ChessGame(518), Arrays.asList("e2-e4", "d7-d5", "f1-b5", "c7-c6", "b5-c6", "b8-d7", "c6-b5"), 19},
                new Object[] {new ChessGame(621), Arrays.asList("g2-g3", "f7-f6", "c2-c3", "g8-f7", "d1-c2", "e8-f8", "c2-h7"), 1},
                new Object[] {new ChessGame("white 0 Rook-black-e1-8 "
                        + "Pawn-black-e2-false King-white-f2-3 Bishop-white-f1 "
                        + "Knight-white-g4 Queen-black-e8 King-black-g7-3"), Arrays.asList("f2-e1", "e2-f1"), 2},
                new Object[] {new ChessGame("white 0 Rook-white-b1-0 King-white-d1-0 Rook-white-e1-0 Bishop-black-d3 King-black-d8-0"), Arrays.asList(), 22}, //don't forget to count short castling
        };
    }

    @Test
    public void testGetPossibleMovesAfterIndirectChessAfterEnpassent() {
        game.move(Move.byCode("e2-e4"));
        game.move(Move.byCode("d7-d5"));
        game.move(Move.byCode("e4-e5"));
        game.move(Move.byCode("e8-d7"));
        game.move(Move.byCode("d1-g4"));
        game.move(Move.byCode("f7-f5"));
        game.move(Move.byCode("e5-f6")); //en-passant creates indirect chess path
        assertTrue(game.getLastMove().isEnpassent());
        List<Move> possibleMoves = getPossibleMoves(game);
        assertEquals(possibleMoves.size(), 4);
    }

    @Test
    public void testIsCheck() {
        String des = "white 0 King-white-g1-2 Bishop-black-f2 King-black-e8-0";
        ChessGame game = new ChessGame(des);
        assertTrue(game.isCheck(true));
    }

    @Test
    public void testGetHistory() {
        ChessGame game = new ChessGame(621);
        game.move(Move.byCode("g2-g3"));
        game.move(Move.byCode("f7-f6"));
        assertEquals(game.getHistory(), "g2-g3,f7-f6");
        game.move(Move.byCode("c2-c3"));
        game.move(Move.byCode("g8-f7"));
        assertEquals(game.getHistory(), "g2-g3,f7-f6,c2-c3,g8-f7");
        game.move(Move.byCode("d1-c2"));
        game.move(Move.byCode("a7-a6"));
        game.move(Move.byCode("c2-h7"));
        assertEquals(game.getHistory(), "g8-f7,d1-c2,a7-a6,c2-h7");
    }

    @Test
    public void testGetCompleteHistory() {
        ChessGame game = new ChessGame(621);
        game.move(Move.byCode("g2-g3"));
        game.move(Move.byCode("f7-f6"));
        assertEquals(game.getCompleteHistory(), "g2-g3,f7-f6");
        game.move(Move.byCode("c2-c3"));
        game.move(Move.byCode("g8-f7"));
        assertEquals(game.getCompleteHistory(), "g2-g3,f7-f6,c2-c3,g8-f7");
        game.move(Move.byCode("d1-c2"));
        game.move(Move.byCode("a7-a6"));
        game.move(Move.byCode("c2-h7"));
        assertEquals(game.getCompleteHistory(), "g2-g3,f7-f6,c2-c3,g8-f7,d1-c2,a7-a6,c2-h7");
    }

    private List<Move> getPossibleMoves(ChessGame game) {
        List<Move> possibleMoves = new LinkedList<Move>();
        game.getPossibleMoves(possibleMoves);
        return possibleMoves;
    }
}
