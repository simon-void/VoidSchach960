package voidchess.figures

import org.testng.annotations.Test
import voidchess.board.ChessGame
import voidchess.board.move.LastMoveProvider
import voidchess.board.SimpleArrayBoard

import java.util.LinkedList

import org.mockito.Mockito.mock
import org.testng.Assert.*
import org.testng.annotations.DataProvider
import voidchess.board.move.Move
import voidchess.board.move.Position


class KingTest {
    @Test
    fun testIsReachable() {
        var des = "white 0 Rook-white-a1-0 King-white-e1-0 Rook-white-h1-0"
        var game = ChessGame(des)

        var from = Position.byCode("e1")
        var to1 = Position.byCode("f1")
        var to2 = Position.byCode("g1")
        var to3 = Position.byCode("d1")
        var to4 = Position.byCode("c1")
        var to5 = Position.byCode("d2")
        var to6 = Position.byCode("e3")
        var to7 = Position.byCode("a1")
        val to8 = Position.byCode("h1")

        var king = King(true, from)
        assertTrue(king.isReachable(to1, game))
        assertFalse(king.isReachable(to2, game))
        assertTrue(king.isReachable(to3, game))
        assertFalse(king.isReachable(to4, game))
        assertTrue(king.isReachable(to5, game))
        assertFalse(king.isReachable(to6, game))
        assertFalse(king.isReachable(from, game))
        assertTrue(king.isReachable(to7, game))
        assertTrue(king.isReachable(to8, game))


        des = "black 0 Rook-black-a8-0 Knight-black-b8 King-black-c8-0 Rook-black-h8-0"
        game = ChessGame(des)

        from = Position.byCode("c8")
        to1 = Position.byCode("a8")
        to2 = Position.byCode("d8")
        to3 = Position.byCode("d7")
        to4 = Position.byCode("b8")
        to5 = Position.byCode("g8")
        to6 = Position.byCode("e6")
        to7 = Position.byCode("h8")

        king = King(false, from)
        assertFalse(king.isReachable(to1, game))
        assertTrue(king.isReachable(to2, game))
        assertTrue(king.isReachable(to3, game))
        assertFalse(king.isReachable(to4, game))
        assertFalse(king.isReachable(to5, game))
        assertFalse(king.isReachable(to6, game))
        assertTrue(king.isReachable(to7, game))
        assertFalse(king.isReachable(from, game))


        game = ChessGame(621)
        from = Position.byCode("e1")
        to1 = Position.byCode("f1")
        king = King(true, from)
        assertFalse(king.isReachable(to1, game))
    }

    @Test(dataProvider = "getReachableByCastelingData")
    fun testIsReachableByCasteling(gameDes: String, kingPosCode: String, rookPosCode: String, expectedCanCastle: Boolean) {
        val game = ChessGame(gameDes)
        val kingPos = Position.byCode(kingPosCode)
        val rookPos = Position.byCode(rookPosCode)
        val king = game.getFigure(kingPos) as King
        val rook = game.getFigure(rookPos) as Rook
        assertTrue(king.canCastle(), "king can castle")
        assertTrue(rook.canCastle(), "rook can castle")
        assertEquals(king.isReachable(rookPos, game), expectedCanCastle, "can castle")
    }

    @DataProvider
    fun getReachableByCastelingData(): Array<Array<Any>> {
        return arrayOf(
                arrayOf("white 0 Rook-white-a1-0 King-white-g1-0", "g1", "a1", true),
                arrayOf("white 0 Rook-white-c1-0 King-white-g1-0", "g1", "c1", true),
                arrayOf("white 0 Rook-white-f1-0 King-white-g1-0", "g1", "f1", true),
                arrayOf("white 0 Rook-white-a1-0 Bishop-white-b1 King-white-g1-0", "g1", "a1", false),
                arrayOf("white 0 Bishop-white-b1 Rook-white-c1-0 King-white-g1-0", "g1", "c1", true),
                arrayOf("white 0 Bishop-white-b1 Rook-white-f1-0 King-white-g1-0", "g1", "f1", true),
                arrayOf("white 0 Rook-white-a1-0 Bishop-white-e1 King-white-g1-0", "g1", "a1", false),
                arrayOf("white 0 Rook-white-c1-0 Bishop-white-e1 King-white-g1-0", "g1", "c1", false),
                arrayOf("white 0 Bishop-white-e1 Rook-white-f1-0 King-white-g1-0", "g1", "f1", false),
                arrayOf("white 0 Rook-white-a1-0 Bishop-black-e1 King-white-g1-0", "g1", "a1", false),
                arrayOf("white 0 Rook-white-c1-0 Bishop-black-e1 King-white-g1-0", "g1", "c1", false),
                arrayOf("white 0 Bishop-black-e1 Rook-white-f1-0 King-white-g1-0", "g1", "f1", false),
                arrayOf("white 0 Rook-white-a1-0 King-white-b1-0 Bishop-white-e1", "b1", "a1", true),
                arrayOf("white 0 Rook-white-a1-0 King-white-b1-0 Bishop-white-d1", "b1", "a1", false),
                arrayOf("white 0 Rook-white-a1-0 King-white-b1-0 Bishop-white-c1", "b1", "a1", false),
                arrayOf("white 0 Bishop-white-a1 Rook-white-b1-0 King-white-c1-0", "c1", "b1", true),
                arrayOf("white 0 Bishop-white-b1 Rook-white-c1-0 King-white-d1-0 Bishop-white-e1", "d1", "c1", true),

                arrayOf("black 0 Rook-black-h8-0 King-black-b8-0", "b8", "h8", true),
                arrayOf("black 0 Rook-black-g8-0 King-black-b8-0", "b8", "g8", true),
                arrayOf("black 0 Rook-black-c8-0 King-black-b8-0", "b8", "c8", true),
                arrayOf("black 0 Rook-black-h8-0 Bishop-black-g8 King-black-b8-0", "b8", "h8", false),
                arrayOf("black 0 Bishop-black-h8 Rook-black-g8-0 King-black-b8-0", "b8", "g8", true),
                arrayOf("black 0 Bishop-black-h8 Rook-black-c8-0 King-black-b8-0", "b8", "c8", true),
                arrayOf("black 0 Rook-black-h8-0 Bishop-black-e8 King-black-b8-0", "b8", "h8", false),
                arrayOf("black 0 Rook-black-f8-0 Bishop-black-e8 King-black-b8-0", "b8", "f8", false),
                arrayOf("black 0 Bishop-black-e8 Rook-black-c8-0 King-black-b8-0", "b8", "c8", false),
                arrayOf("black 0 Rook-black-h8-0 Bishop-white-e8 King-black-b8-0", "b8", "h8", false),
                arrayOf("black 0 Rook-black-f8-0 Bishop-white-e8 King-black-b8-0", "b8", "f8", false),
                arrayOf("black 0 Bishop-white-e8 Rook-black-c8-0 King-black-b8-0", "b8", "c8", false),
                arrayOf("black 0 Rook-black-h8-0 King-black-g8-0 Bishop-black-e8", "g8", "h8", true),
                arrayOf("black 0 Rook-black-h8-0 King-black-g8-0 Bishop-black-f8", "g8", "h8", false),
                arrayOf("black 0 Bishop-black-h8 Rook-black-g8-0 King-black-f8-0", "f8", "g8", true),
                arrayOf("black 0 Bishop-black-h8 Rook-black-g8-0 King-black-f8-0 Bishop-black-e8", "f8", "g8", true)
        )
    }

    @Test
    fun testGetReachableMoves() {
        val game = ChessGame(621)
        var from = Position.byCode("e1")
        var king = game.getFigure(from)!!

        val moveIter = LinkedList<Move>()
        king.getReachableMoves(game, moveIter)
        assertEquals(0, moveIter.size)


        val des = "white 0 Rook-white-a1-0 King-white-e1-0 Rook-white-h1-0"
        val board = SimpleArrayBoard(des, mock(LastMoveProvider::class.java))

        king = board.getFigure(Position.byCode("e1"))!!
        moveIter.clear()
        king.getReachableMoves(board, moveIter)
        assertEquals(moveIter.size, 7, "five regular king moves + long/short castling, therefor")
    }

    @Test
    fun testIsPassiveBound() {
        var des = "black 0 Knight-white-b6 Rook-black-a8-0 King-black-e8-0 Rook-black-h8-0"
        val game = SimpleArrayBoard(des, mock(LastMoveProvider::class.java))

        var from = Position.byCode("e8")
        var to1 = Position.byCode("c8")
        var to2 = Position.byCode("d7")
        var to3 = Position.byCode("g8")
        var to4 = Position.byCode("e7")

        var king = King(false, from)

        assertTrue(king.isPassiveBound(to1, game))
        assertTrue(king.isPassiveBound(to2, game))
        assertFalse(king.isPassiveBound(to3, game))
        assertFalse(king.isPassiveBound(to4, game))

        des = "black 0 Bishop-white-c6 Rook-black-a8-0 King-black-e8-0 Rook-black-h8-0"
        game.init(des)
        from = Position.byCode("e8")
        to1 = Position.byCode("a8")
        to2 = Position.byCode("d7")
        to3 = Position.byCode("h8")
        to4 = Position.byCode("e7")

        king = King(false, from)

        assertTrue(king.isPassiveBound(to1, game))
        assertTrue(king.isPassiveBound(to2, game))
        assertTrue(king.isPassiveBound(to3, game))
        assertFalse(king.isPassiveBound(to4, game))
    }

    @Test
    fun testGetPossibleMoves() {
        var des = "white 0 Rook-white-a1-0 King-white-e1-0 Rook-white-h1-0"
        var board = SimpleArrayBoard(des, mock(LastMoveProvider::class.java))
        var king = board.getFigure(Position.byCode("e1"))!!
        var moveIter: MutableList<Move> = LinkedList()
        king.getPossibleMoves(board, moveIter)
        assertEquals(moveIter.size, 7, "five regular king moves + long/short castling, therefor")

        des = "white 0 Rook-white-a1-0 King-white-e1-0 Rook-white-h1-0 Bishop-black-e4"
        board = SimpleArrayBoard(des, mock(LastMoveProvider::class.java))
        king = board.getFigure(Position.byCode("e1"))!!
        moveIter.clear()
        king.getPossibleMoves(board, moveIter)
        assertEquals(moveIter.size, 7, "five regular king moves + long/short castling, therefor")

        des = "white 0 Rook-white-a1-0 King-white-e1-0 Rook-white-h1-0 Bishop-black-e3"
        board = SimpleArrayBoard(des, mock(LastMoveProvider::class.java))
        king = board.getFigure(Position.byCode("e1"))!!
        moveIter.clear()
        king.getPossibleMoves(board, moveIter)
        assertEquals(moveIter.size, 3, "five regular king moves (long/short castling intercepted), therefor")

        des = "white 0 Rook-white-a1-0 King-white-e1-0 Rook-white-h1-0 Bishop-black-e2"
        board = SimpleArrayBoard(des, mock(LastMoveProvider::class.java))
        king = board.getFigure(Position.byCode("e1"))!!
        moveIter.clear()
        king.getPossibleMoves(board, moveIter)
        assertEquals(moveIter.size, 3, "three king moves (long/short castling intercepted), therefor")

        des = "white 0 Rook-white-a1-0 King-white-e1-0 Rook-white-h1-0 Knight-black-e3"
        board = SimpleArrayBoard(des, mock(LastMoveProvider::class.java))
        king = board.getFigure(Position.byCode("e1"))!!
        moveIter.clear()
        king.getPossibleMoves(board, moveIter)
        assertEquals(moveIter.size, 3, "three king moves (long/short castling intercepted), therefor")

        des = "white 0 Rook-white-a1-0 King-white-e1-0 Rook-white-h1-0 Knight-black-e2"
        board = SimpleArrayBoard(des, mock(LastMoveProvider::class.java))
        king = board.getFigure(Position.byCode("e1"))!!
        moveIter.clear()
        king.getPossibleMoves(board, moveIter)
        assertEquals(moveIter.size, 5, "five king moves (long/short castling intercepted), therefor")

        des = "white 0 Rook-white-a1-0 King-white-e1-0 Rook-white-h1-0 Rook-black-f2-2"
        board = SimpleArrayBoard(des, mock(LastMoveProvider::class.java))
        king = board.getFigure(Position.byCode("e1"))!!
        moveIter.clear()
        king.getPossibleMoves(board, moveIter)
        assertEquals(moveIter.size, 3, "d1, f2 and long castling, therefor")

        board.init(621)
        king = board.getFigure(board.getKingPosition(true))!!
        moveIter = LinkedList()
        king.getPossibleMoves(board, moveIter)
        assertEquals(moveIter.size, 0, "king shouldn't be able to castle because bishop blocks the king's end position")

        var game = ChessGame(621)
        game.move(Move.byCode("f2-f3"))
        game.move(Move.byCode("f7-f6"))
        game.move(Move.byCode("g1-f2"))
        game.move(Move.byCode("g8-f7"))
        var movesFrom = FigureTest.getPossibleMovesFrom("e1", game)
        assertEquals(movesFrom.size, 1, "king should be able to short castle")
        game.move(Move.byCode("e1-f1"))
        movesFrom = FigureTest.getPossibleMovesFrom("e8", game)
        assertEquals(movesFrom.size, 1, "king should be able to short castle")

        game = ChessGame(621)
        game.move(Move.byCode("c2-c3"))
        game.move(Move.byCode("f7-f6"))
        game.move(Move.byCode("d1-c2"))
        game.move(Move.byCode("g8-c4"))
        game.move(Move.byCode("c2-h7"))
        movesFrom = FigureTest.getPossibleMovesFrom("e8", game)
        assertEquals(movesFrom.size, 1, "king shouldn't be able to castle because queen could attack the king's end position")
    }

    @Test
    fun testDidCastling() {
        val des = "white 0 Rook-white-a1-0 King-white-e1-0 " + "King-black-e8-0"
        val game = ChessGame(des)

        val king = game.getFigure(Position.byCode("e1")) as King
        assertFalse(king.didCastling())
        game.move(Move.byCode("e1-a1"))
        assertTrue(king.didCastling())
    }

    @Test
    fun testDoesShortCastling() {
        var game = ChessGame(518)
        with(game) {
            move(Move.byCode("g2-g3"))
            move(Move.byCode("g7-g6"))
            move(Move.byCode("f1-g2"))
            move(Move.byCode("f8-g7"))
            move(Move.byCode("g1-f3"))
            move(Move.byCode("g8-f6"))
        }

        assertTrue(game.isMovable(Position.byCode("e1"), Position.byCode("h1"), true), "isMovable: king can move to h1 (short castling)")
        val whiteKingMoves = FigureTest.getPossibleMovesFrom("e1", game)
        assertEquals(whiteKingMoves.size,2, "king can go to f1 and h1 (short castling), therefore getPossibleMoves#")
        assertTrue(whiteKingMoves.contains(Move.byCode("e1-h1")), "possible move e1-h1")
        game.move(Move.byCode("e1-h1"))
        assertTrue(game.isMovable(Position.byCode("e8"), Position.byCode("h8"), false), "isMovable: king can move to h8 (short castling)")
        val blackKingMoves = FigureTest.getPossibleMovesFrom("e8", game)
        assertEquals(blackKingMoves.size, 2, "king can go to f8 and h8 (short castling), therefore getPossibleMoves#")
        assertTrue(blackKingMoves.contains(Move.byCode("e8-h8")), "possible move e8-h8")
    }

    @Test
    fun testDoesLongCastling() {
        var game = ChessGame(518)
        with(game) {
            move(Move.byCode("e2-e4"))
            move(Move.byCode("e7-e5"))
            move(Move.byCode("d1-e2"))
            move(Move.byCode("d8-e7"))
            move(Move.byCode("b1-c3"))
            move(Move.byCode("b8-c6"))
            move(Move.byCode("b2-b3"))
            move(Move.byCode("b7-b6"))
            move(Move.byCode("c1-b2"))
            move(Move.byCode("c8-b7"))
        }

        assertTrue(game.isMovable(Position.byCode("e1"), Position.byCode("a1"), true), "isMovable: king can move to a1 (long castling)")
        val whiteKingMoves = FigureTest.getPossibleMovesFrom("e1", game)
        assertEquals(whiteKingMoves.size, 2, "king can go to d1 and a1 (long castling), therefore getPossibleMoves#")
        assertTrue(whiteKingMoves.contains(Move.byCode("e1-a1")), "possible move e1-a1")
        game.move(Move.byCode("e1-a1"))
        assertTrue(game.isMovable(Position.byCode("e8"), Position.byCode("a8"), false), "isMovable: king can move to a8 (long castling)")
        val blackKingMoves = FigureTest.getPossibleMovesFrom("e8", game)
        assertEquals(blackKingMoves.size, 2, "king can go to d8 and a8 (long castling), therefore getPossibleMoves#")
        assertTrue(blackKingMoves.contains(Move.byCode("e8-a8")), "possible move e8-a8")
    }

    @Test
    fun testImmidiatlyCastlingInChess960Positions() {
        //siehe https://de.wikipedia.org/wiki/Chess960#Castlingregeln

        //Rook on a1, king on b1 so b1-a1 should be possible as a first move
        var game = ChessGame(314)
        val c1 = Position.byCode("c1")
        val d1 = Position.byCode("d1")
        var isCastlingPossible = game.isMovable(d1, c1, true)
        assertTrue(isCastlingPossible, "castling should be possible")

        //Rook on a1, king on b1 so b1-a1 should be be possible as a first move
        game = ChessGame(759)
        val a1 = Position.byCode("a1")
        val b1 = Position.byCode("b1")
        isCastlingPossible = game.isMovable(b1, a1, true)
        assertFalse(isCastlingPossible, "castling should be impossible possible")
    }
}
