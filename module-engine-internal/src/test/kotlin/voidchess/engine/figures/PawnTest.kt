package voidchess.engine.figures

import org.testng.annotations.Test
import voidchess.engine.board.ChessGame
import voidchess.common.board.move.Move
import voidchess.common.board.move.Position

import org.testng.annotations.DataProvider
import voidchess.engine.board.getFigure
import voidchess.initSimpleChessBoard
import voidchess.toTargetPosAsStringSet
import java.util.*
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue


class PawnTest {

    @Test(dataProvider = "getIsReachableData")
    fun testIsReachable(des: String, fromPosCode: String, reachablePosCodes: Set<String>, unreachablePosCodes: Set<String>) {
        val game = ChessGame(des)
        val from = Position.byCode(fromPosCode)
        val pawn = game.getFigure(from) as Pawn
        val shouldBeReachableButAreNot = reachablePosCodes.map { Position.byCode(it) }.filter { pos -> !pawn.isReachable(pos, game)}.map { it.toString() }
        val shouldNotBeReachableButAre = unreachablePosCodes.map { Position.byCode(it) }.filter { pos -> pawn.isReachable(pos, game)}.map { it.toString() }
        assertTrue(
            shouldBeReachableButAreNot.isEmpty() && shouldNotBeReachableButAre.isEmpty(),
            "shouldBeReachableButAreNot: $shouldBeReachableButAreNot, shouldNotBeReachableButAre$shouldNotBeReachableButAre"
        )
    }

    @DataProvider
    fun getIsReachableData(): Array<Array<Any>> = arrayOf(
        arrayOf("white 0 Pawn-white-a2-false Pawn-white-b3-false King-white-e1-0 King-black-e8-0", "a2", setOf("a3", "a4"), setOf("a5", "b3", "b2", "a1", "a2")),
        arrayOf("black 0 Pawn-white-e4-true Pawn-white-c3-false Pawn-black-d4-false King-white-e1-0 King-black-e8-0", "d4", setOf("c3", "d3", "e3"), setOf("d2", "d4", "d5", "d6")),
        arrayOf("white 0 King-white-e1-0 Pawn-white-b5-false Pawn-black-c5-true King-black-e8-0", "b5", setOf("b6", "c6"), setOf("b7", "a5"))
    )

    @Test(dataProvider = "getGetReachableMovesData")
    fun testGetReachableMoves(gameDes: String, pawnPosCode: String, expectedPossibleToCodes: Set<String>) {
        val game = initSimpleChessBoard(gameDes)

        val pawn = game.getFigure(Position.byCode(pawnPosCode)) as Pawn
        val moveIter1 = LinkedList<Move>()
        pawn.getReachableMoves(game, moveIter1)
        val actualPossibleToCodes = moveIter1.asSequence().map { it.to.toString() }.toSet()
        assertEquals(expectedPossibleToCodes, actualPossibleToCodes, "reachable positions the pawn can move to")
    }

    @DataProvider
    fun getGetReachableMovesData(): Array<Array<Any>> = arrayOf(
        arrayOf("black 0 King-black-g7-2 Pawn-black-e4-false Queen-white-d4 King-white-e1-0", "e4", setOf("e3")),
        arrayOf("black 0 King-black-g7-2 Pawn-black-e4-false Pawn-white-d4-true King-white-e1-0", "e4", setOf("e3", "d3"))
    )

    @Test(dataProvider = "getGetPossibleMovesData")
    fun testGetPossibleMoves(gameDes: String, pawnPosCode: String, expectedPossibleToCodes: Set<String>) {
        val game = initSimpleChessBoard(gameDes)

        val pawn = game.getFigure(Position.byCode(pawnPosCode)) as Pawn
        val moveIter1 = LinkedList<Move>()
        pawn.getPossibleMoves(game, moveIter1)
        val actualPossibleToCodes = moveIter1.asSequence().map { it.to.toString() }.toSet()
        assertEquals(expectedPossibleToCodes, actualPossibleToCodes, "possible positions the pawn can move to")
    }

    @DataProvider
    fun getGetPossibleMovesData(): Array<Array<Any>> = arrayOf(
        arrayOf("black 0 Pawn-white-a4-true Pawn-black-b4-false King-black-e8-0 King-white-e1-0", "b4", setOf("a3", "b3")),
        arrayOf("black 0 King-black-e8-0 Pawn-black-h7-false King-white-e1-0", "h7", setOf("h6", "h5")),
        arrayOf("black 0 King-black-h8-2 Pawn-black-h7-false Queen-white-h4 King-white-e1-0", "h7", setOf("h6", "h5")),
        arrayOf("black 0 King-black-h8-2 Pawn-black-h7-false Queen-white-h5 King-white-e1-0", "h7", setOf("h6")),
        arrayOf("black 0 King-black-h8-2 Pawn-black-h7-false Queen-white-h6 King-white-e1-0", "h7", setOf<String>()),
        arrayOf("black 0 King-black-h8-2 Pawn-black-g7-false Queen-white-e5 King-white-e1-0", "g7", setOf<String>()),
        arrayOf("black 0 King-black-h8-2 Pawn-black-g7-false Queen-white-f6 King-white-e1-0", "g7", setOf("f6")),
        arrayOf("black 0 King-black-h7-2 Pawn-black-g7-false Queen-white-f5 King-white-e1-0", "g7", setOf("g6")),
        arrayOf("black 0 King-black-h6-2 Pawn-black-g7-false Queen-white-f4 King-white-e1-0", "g7", setOf("g5")),
        arrayOf("black 0 King-black-h7-2 Pawn-black-g7-false Queen-white-a7 King-white-e1-0", "g7", setOf<String>()),
        arrayOf("white 0 King-black-h7-2 Pawn-white-a4-false Pawn-black-b5-true King-white-c4-8", "a4", setOf("b5")),
        arrayOf("white 0 King-black-h7-2 Pawn-white-a5-false Pawn-black-b5-true King-white-c4-8", "a5", setOf("b6")),
        arrayOf("white 0 Pawn-white-b2-false King-white-d3-2 Rook-black-a3-1 King-black-e8-0", "b2", setOf("a3", "b3")),
        arrayOf("white 0 Pawn-white-b2-false King-white-d4-2 Rook-black-a4-1 King-black-e8-0", "b2", setOf("b4")),
        arrayOf("white 0 Pawn-white-b3-false King-white-d5-4 Rook-black-a5-1 King-black-e8-0", "b3", setOf<String>()),
        arrayOf("white 0 King-white-g2-2 Pawn-white-e5-false Queen-black-d5 King-black-e8-0", "e5", setOf<String>()),
        arrayOf("white 0 King-white-g2-2 Pawn-white-e6-false Queen-black-d5 King-black-e8-0", "e6", setOf<String>()),
        arrayOf("white 0 King-white-g2-2 Pawn-white-e7-false Queen-black-d5 King-black-e8-0", "e7", setOf<String>()),
        arrayOf("black 0 King-black-g7-2 Pawn-black-e4-false Queen-white-d4 King-white-e1-0", "e4", setOf<String>()),
        arrayOf("black 0 King-black-g7-2 Pawn-black-e3-false Queen-white-d4 King-white-e1-0", "e3", setOf<String>())
    )

    @Test(dataProvider = "getGetCriticalMovesData")
    fun testGetPossibleIrreversibleMoves(gameDes: String, posCode: String) {
        val game = initSimpleChessBoard(gameDes)
        val pawn = game.getFigure(Position.byCode(posCode)) as Pawn
        val actualResults = TreeSet<Move>()
        pawn.getCriticalMoves(game, actualResults)
        val expectedResults = LinkedList<Move>()
        pawn.getPossibleMoves(game, expectedResults)
        assertEquals(expectedResults.toTargetPosAsStringSet(), actualResults.toTargetPosAsStringSet(), "pawn can move to irreversibly")
    }

    @DataProvider
    fun getGetCriticalMovesData(): Array<Array<Any>> = arrayOf(
        arrayOf<Any>("white 0 King-black-e8-0 King-white-e1-0 Pawn-white-d5-false Pawn-black-e5-true Pawn-black-c6-false", "d5"),
        arrayOf<Any>("white 0 King-black-e8-0 King-white-e1-0 Pawn-white-a2-false", "a2")
    )

    @Test(dataProvider = "getGetPossibleTakingMovesData")
    fun testGetPossibleTakingMoves(gameDes: String, posCode: String, expectedMoveToCodes: Set<String>) {
        val game = initSimpleChessBoard(gameDes)
        val pawn = game.getFigure(Position.byCode(posCode)) as Pawn
        val results = LinkedList<Move>()
        pawn.getPossibleTakingMoves(game, results)
        val actualMoveToCodes = results.toTargetPosAsStringSet()
        assertEquals(expectedMoveToCodes, actualMoveToCodes, "pawn can take on")
    }

    @DataProvider
    fun getGetPossibleTakingMovesData(): Array<Array<Any>> = arrayOf(
        arrayOf<Any>("white 0 King-black-e8-0 King-white-e1-0 Pawn-white-d5-false Pawn-black-e5-true Pawn-black-c6-false", "d5", setOf("c6", "e6")),
        arrayOf<Any>("white 0 King-black-e8-0 King-white-e1-0 Pawn-white-a2-false", "a2", setOf<String>())
    )
}
