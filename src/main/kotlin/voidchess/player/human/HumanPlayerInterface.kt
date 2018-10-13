package voidchess.player.human

import voidchess.board.move.Position
import voidchess.player.PlayerInterface


interface HumanPlayerInterface : PlayerInterface {
    fun mouseMovedOver(pos: Position?)
    fun mouseClickedOn(pos: Position)
}
