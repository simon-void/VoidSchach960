package voidchess.player.ki.concurrent;

import voidchess.board.ChessGameInterface;
import voidchess.player.ki.DynamicEvaluation;
import voidchess.player.ki.evaluation.EvaluatedMove;

import java.util.NavigableSet;

public interface ConcurrencyStrategy {
    /**
     * @return a sorted set of all possible moves sorted by a value of "how good it is for the computer voidchess.player".
     * The first element is the best choice for the computer voidchess.player and the last element being the worst.
     */
    NavigableSet<EvaluatedMove> evaluatePossibleMoves(
            final ChessGameInterface game,
            final DynamicEvaluation dynamicEvaluation);

}