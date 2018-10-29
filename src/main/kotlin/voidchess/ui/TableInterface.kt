package voidchess.ui

import voidchess.board.move.MoveResult
import voidchess.board.move.Move
import voidchess.player.PlayerInterface


interface TableInterface {
    fun startGame()
    fun stopGame(endOption: MoveResult)
    fun move(move: Move)
    fun setWhitePlayer(player: PlayerInterface)
    fun setBlackPlayer(player: PlayerInterface)
    fun switchPlayer()
}
