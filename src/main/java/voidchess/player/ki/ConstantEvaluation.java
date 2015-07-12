package voidchess.player.ki;

import voidchess.board.ChessGameInterface;

/**
 * usefull for Profile purposes (only DynamicEvaluation remains)
 *
 * @author Stephan Schr�der
 */

public class ConstantEvaluation implements StaticEvaluationInterface {
    @Override
    public Evaluaded evaluate(ChessGameInterface game, boolean forWhite) {
        return Evaluaded.DRAW;
    }
}
