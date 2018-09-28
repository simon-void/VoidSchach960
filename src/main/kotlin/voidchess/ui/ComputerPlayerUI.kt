package voidchess.ui

import voidchess.helper.Move
import voidchess.player.ki.evaluation.Evaluated

import javax.swing.*

/**
 * @author stephan
 */
interface ComputerPlayerUI {
    fun setProgress(computedMoves: Int, totalMoves: Int)
    fun setValue(value: Evaluated, move: Move)
}
