package voidchess.player.ki.concurrent;

import voidchess.player.ki.AbstractComputerPlayerUI;

public class ConcurrencyStrategyFactory {
    public static ConcurrencyStrategy getConcurrencyStrategy(AbstractComputerPlayerUI ui, int numberOfCoresToUse) {
        if (numberOfCoresToUse == 1) {
            return new SingleThreadStrategy(ui);
        } else {
            return new MultiThreadStrategy(ui, numberOfCoresToUse);
        }
    }
}