package player.ki;

import board.ChessGameInterface;

/**
 * usefull for Profile purposes (only DynamicEvaluation remains)
 *
 * @author Stephan Schr�der
 */

public class ConstantEvaluation implements StaticEvaluationInterface {
    @Override
    public float evaluate(ChessGameInterface game, boolean forWhite) {
        return 0;
    }
}
