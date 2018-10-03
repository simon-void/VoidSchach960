package voidchess.player.ki;

import voidchess.board.ChessGameInterface;
import voidchess.board.MoveResult;
import voidchess.helper.Move;
import voidchess.helper.PawnPromotion;
import voidchess.helper.Position;
import voidchess.helper.RuntimeFacade;
import voidchess.player.PlayerInterface;
import voidchess.player.ki.concurrent.ConcurrencyStrategy;
import voidchess.player.ki.concurrent.ConcurrencyStrategyFactory;
import voidchess.player.ki.evaluation.Evaluated;
import voidchess.player.ki.evaluation.EvaluatedMove;
import voidchess.player.ki.openings.OpeningsLibrary;
import voidchess.ui.ComputerPlayerUI;
import voidchess.ui.TableInterface;

import java.util.Iterator;
import java.util.List;
import java.util.NavigableSet;
import java.util.Random;

/**
 * @author stephan
 */
public class ComputerPlayer
        implements PlayerInterface {
    private ChessGameInterface game;
    private TableInterface table;
    private ComputerPlayerUI ui;
    private DynamicEvaluation dynamicEvaluation;
    private SearchTreePruner standardPruner;
    private StaticEvaluationInterface standardEvaluation;
    private ConcurrencyStrategy concurrencyStrategy;
    private boolean usesStandardEvaluation;
    private OpeningsLibrary openingsLibrary;
    private boolean useLibrary;
    private Random randomNumberGenerator;
    private boolean isWhite = false;

    public ComputerPlayer(TableInterface table, ChessGameInterface game, ComputerPlayerUI ui) {
        this.ui = ui;
        this.game = game;
        this.table = table;

        standardPruner = new SimplePruner(1, 2, 2);
        standardEvaluation = new StaticEvaluation();
        dynamicEvaluation = new DynamicEvaluation(standardPruner, standardEvaluation);
        concurrencyStrategy = ConcurrencyStrategyFactory.getConcurrencyStrategy(ui, 1);
        openingsLibrary = new OpeningsLibrary("openings.txt");
        randomNumberGenerator = new Random();
        initEvaluation();
    }

    @Override
    public void play() {
        ui.setBubbleText(null);
        ui.showThoughts(true);
        EvaluatedMove chosenMove = getNextMove();
        ui.setValue(chosenMove.getValue());
        table.move(chosenMove.getMove());

        RuntimeFacade.collectGarbage();
    }

    private EvaluatedMove getNextMove() {
        //lets see if the library contains a next move
        EvaluatedMove nextMove = lookUpNextMove();
        //we can always compute a next move
        if (nextMove == null) {
            nextMove = computeNextMove();
        }
        return nextMove;
    }

    private EvaluatedMove lookUpNextMove() {
        if (useLibrary) {
            String history = game.getCompleteHistory();
            List<Move> possibleMoves = openingsLibrary.nextMove(history);
            if (!possibleMoves.isEmpty()) {
                //display that the computer is working
                ui.setProgress(0, 1);
                //pick a random move
                Move randomMove = possibleMoves.get(randomNumberGenerator.nextInt(possibleMoves.size()));
                //and evaluate it
                boolean isWhitePlayer = game.isWhiteTurn();
                game.move(randomMove);
                Evaluated evaluation = standardEvaluation.getPrimaryEvaluation(game, isWhitePlayer);
                standardEvaluation.addSecondaryEvaluation(game, isWhitePlayer, evaluation);
                game.undo();

                //wait before playing so that the user can clearly see the computer's move
                wait(300);

                return new EvaluatedMove(randomMove, evaluation);
            }
        }
        //the library has no more information on this sequence
        useLibrary = false;
        return null;
    }

    private EvaluatedMove computeNextMove() {
        //the game starts with a static game evaluation that considers many things
        //but in the endgame, when only the king of one side can move than the only important thing is
        //how many possible moves the king has left. So the Evaluation strategy has to be changed.
        pickStaticSpaceEvaluationIfNeccessary();

//      TODO  real Timer
//		long time = System.currentTimeMillis();

        NavigableSet<EvaluatedMove> sortedEvaluatedMoves = concurrencyStrategy.evaluatePossibleMoves(game, dynamicEvaluation);

//		final int calls = StaticEvaluation.getCallCounter();
//		final int totalNumberOfMoves = evaluation.totalNumberOfMoves();
//		long duration = System.currentTimeMillis()-time;
//		double timePerMove = duration/(double)totalNumberOfMoves;
//		double timePerCall = duration/(double)calls;
//		System.out.println( "ComputerPlayer.play()" );
//		System.out.println( "moves:       "+totalNumberOfMoves );
//		System.out.println( "staticEval:  "+calls );
//		System.out.println( "duration:    "+(duration)+"ms" );
//		System.out.println( "timePerMove: "+(timePerMove)+"ms" );
//		System.out.println( "timePerCall: "+(timePerCall)+"ms" );
//		System.out.println();

        EvaluatedMove chosenMove = pickNextMoveByEvaluation(sortedEvaluatedMoves);
        return chosenMove;
    }

    /**
     * pick the move to make given an evaluated Set of possible moves.
     * (Taking always the best move would lead to a limited number of openings!)
     *
     * @param sortedEvaluatedMoves (set.first is the best move for the ki, set.last the worst)
     * @return the move the ki will make next
     */
    private EvaluatedMove pickNextMoveByEvaluation(NavigableSet<EvaluatedMove> sortedEvaluatedMoves) {
        Iterator<EvaluatedMove> evaluation = sortedEvaluatedMoves.descendingIterator();
        EvaluatedMove bestMove = evaluation.next();

        //as long as the top moves are almost equally good, pick randomly one (with a higher chance for the better move)
        EvaluatedMove chosenMove = bestMove;
        while (evaluation.hasNext()) {
            if (Math.random() < 0.6) break;
            EvaluatedMove tempMove = evaluation.next();
            if (tempMove.getValue().isCloseToByCombined(bestMove.getValue())) {
                chosenMove = tempMove;
            } else {
                break;
            }
        }
        return chosenMove;
    }

    /**
     * the game starts with a static game evaluation that considers many things
     * but in the endgame, when only the king of one side can move than the
     * only important thing is how many possible moves the king has left.
     * So the Evaluation strategy has to be changed.
     */
    private void pickStaticSpaceEvaluationIfNeccessary() {
        if (usesStandardEvaluation && StaticSpaceEvaluation.shouldUseStaticSpaceEvaluation(game)) {
            //wird genau einmal pro Spiel ausgeführt
            dynamicEvaluation.setEvaluationStrategy(new StaticSpaceEvaluation());
            dynamicEvaluation.setSearchTreePruner(new FullMovePruner(2, 2, 2));
            usesStandardEvaluation = false;
        }
    }

    @Override
    public PawnPromotion askForPawnPromotionType(Position pawnPosition) {
        return PawnPromotion.QUEEN;
    }

    @Override
    public void gameStarts() {
        initEvaluation();
        ui.init();
    }

    @Override
    public void gameEnds(MoveResult endoption, boolean lastMoveByWhite) {
        ui.showThoughts(false);
        switch (endoption) {
            case DRAW:
                ui.setBubbleText("draw");
                break;
            case STALEMATE:
                ui.setBubbleText("stalemate");
                break;
            case CHECKMATE:
                String text = lastMoveByWhite == isWhite ? "checkmate" : "good game";
                ui.setBubbleText(text);
                break;
            case THREE_TIMES_SAME_POSITION:
                ui.setBubbleText("draw because of\n3x repetition");
                break;
            case FIFTY_MOVES_NO_HIT:
                ui.setBubbleText("draw because of\n50-move rule");
                break;
            case RESIGN:
                ui.setBubbleText("you resigned\nalready ?!");
                break;
            default:
                ui.setBubbleText(endoption.toString());
        }
    }

    @Override
    public void gaveCheck() {
        ui.setBubbleText("check");
    }

    @Override
    public void setColor(boolean isWhite) {
        this.isWhite = isWhite;
    }

    // initializes the default EvaluationStrategie
    private void initEvaluation() {
        dynamicEvaluation.setEvaluationStrategy(standardEvaluation);
        dynamicEvaluation.setSearchTreePruner(standardPruner);
        usesStandardEvaluation = true;
        //use the library only if the figures are used in the classical way (no Chess960)
        useLibrary = game.isStandardGame();
    }

    public void setSearchTreePruner(SearchTreePruner pruner) {
        standardPruner = pruner;
        dynamicEvaluation.setSearchTreePruner(pruner);
    }

    public void setNumberOfCoresToUse(int numberOfCoresToUse) {
        concurrencyStrategy = ConcurrencyStrategyFactory.getConcurrencyStrategy(ui, numberOfCoresToUse);
    }

    private void wait(final int milliseconds) {
        try {
            Thread.sleep(milliseconds);
        } catch (InterruptedException e) {
        }
    }
}
