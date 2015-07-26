package voidchess.player.ki.concurrent;

import org.testng.annotations.*;
import voidchess.board.ChessGame;
import voidchess.board.ChessGameInterface;
import voidchess.helper.Move;
import voidchess.player.ki.*;
import voidchess.player.ki.evaluation.Evaluated;
import voidchess.player.ki.evaluation.EvaluatedMove;

import java.util.Iterator;
import java.util.NavigableSet;
import java.util.SortedSet;

import static org.testng.Assert.*;
import static org.mockito.Mockito.*;

/**
 * Created by stephan on 17.07.2015.
 */
public class SingleThreadStrategyTest {
    private SingleThreadStrategy strategy;
    private AbstractComputerPlayerUI uiMock;

    @BeforeMethod
    public void setup() {
        uiMock = mock(AbstractComputerPlayerUI.class);
        strategy = new SingleThreadStrategy(uiMock);
    }

    @Test
    public void testSortOrder() {
        ChessGame game = new ChessGame(518);
        Iterator<EvaluatedMove> moves = evaluate(1, game).descendingIterator();

        EvaluatedMove betterMove = moves.next();
        while(moves.hasNext()) {
            EvaluatedMove worseMove = moves.next();
            assertTrue(
                    betterMove.getValue().compareTo(worseMove.getValue())>=0,
                    "earlier move should be at least as good"
            );
            betterMove = worseMove;
        }
    }

    @Test(dataProvider = "bestMoveProvider")
    public void testFindBestMoveIn(int depth, String desc, Move bestMove) {
        ChessGame game = new ChessGame(desc);
        Move computedMove = getBestMoveInIn(depth, game);
        assertEquals(computedMove, bestMove, "best move");
    }

    @Test(dataProvider = "worstMoveProvider")
    public void testFindWorstMoveIn(int depth, String desc, Move worstMove) {
        ChessGame game = new ChessGame(desc);
        Move computedMove = getWorstMoveIn(depth, game);
        assertEquals(computedMove, worstMove, "worst move");
    }

    @DataProvider(name = "bestMoveProvider")
    public Object[][] getBestMoveBoardsProvider() {
        return new Object[][] {
                //best move with matt
                new Object[]{
                        1,
                        "black 0 King-white-h1-3 Pawn-white-g2-false Pawn-white-h2-false Rock-black-f4-2 King-black-e8-0",
                        Move.get("f4-f1")
                },
                //color inverted matt
                new Object[]{
                        1,
                        "white 0 King-black-h8-3 Pawn-black-g7-false Pawn-black-h7-false Rock-white-f4-2 King-white-e1-0",
                        Move.get("f4-f8")
                },
                //best move with value
                new Object[]{
                        1,
                        "white 0 King-white-e1-0 Pawn-white-g2-false Pawn-white-h2-false Rock-black-f2-2 King-black-e8-0",
                        Move.get("e1-f2")
                },
                //best matt move in 2
                new Object[]{
                        2,
                        "white 0 Bishop-white-a1 Rock-black-b2-2 Knight-black-c2 Pawn-white-d6-false King-white-g6-8  King-black-h8-4",
                        Move.get("d6-d7")
                },
        };
    }

    @DataProvider(name = "worstMoveProvider")
    public Object[][] getWorstMoveBoardsProvider() {
        return new Object[][] {
                //worst move goes into matt
                new Object[]{
                        1,
                        "white 0 King-white-g1-2 Pawn-white-g2-false Pawn-white-h2-false Rock-black-f4-2 King-black-e8-0",
                        Move.get("g1-h1")
                },
                //color inverted worst move goes into matt
                new Object[]{
                        1,
                        "black 0 King-black-g8-2 Pawn-black-g7-false Pawn-black-h7-false Rock-white-f4-2 King-white-e1-0",
                        Move.get("g8-h8")
                },
                //worst move by value
                new Object[]{
                        1,
                        "white 0 King-white-e1-0 Rock-black-f2-2 Queen-black-d1 King-black-e8-0",
                        Move.get("e1-f2")
                },
        };
    }

    private Move getBestMoveInIn(int depth, ChessGameInterface game) {
        SortedSet<EvaluatedMove> moves = evaluate(depth, game);
        Move bestMove = moves.last().getMove();
        return bestMove;
    }

    private Move getWorstMoveIn(int depth, ChessGameInterface game) {
        SortedSet<EvaluatedMove> moves = evaluate(depth, game);
        Move worstMove = moves.first().getMove();
        return worstMove;
    }

    private NavigableSet<EvaluatedMove> evaluate(int depth, ChessGameInterface game) {
        SearchTreePruner pruner = new SimplePruner(depth, depth, depth);
        StaticEvaluationInterface staticEval = new StaticEvaluation();
        DynamicEvaluation dynEval = new DynamicEvaluation(pruner, staticEval);
        NavigableSet<EvaluatedMove> sortedMoves = strategy.evaluatePossibleMoves(game, dynEval);
        return sortedMoves;
    }
}
