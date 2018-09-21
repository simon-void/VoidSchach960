package voidchess.ui

import voidchess.board.BasicChessGameInterface
import voidchess.helper.Move
import voidchess.helper.Position
import voidchess.image.FigureImage
import voidchess.image.ImageType
import voidchess.image.Images
import voidchess.player.HumanPlayerInterface

import javax.swing.*
import java.awt.*
import java.awt.image.ImageObserver
import java.util.HashMap

/**
 * @author stephan
 */
class ChessboardUI internal constructor(private val game: BasicChessGameInterface, imageObserver: ImageObserver) : JComponent() {
    val areaSize: Int = 50
    val borderSize: Int = 25
    private val imageTypeToImage: Map<ImageType, FigureImage>
    private val adapter: ChessGameAdapter
    var isWhiteView: Boolean = true
        private set(value) { field = value }
    private var from: Position? = null
    private var to: Position? = null

    init {
        preferredSize = Dimension(2 * borderSize + 8 * areaSize, 2 * borderSize + 8 * areaSize)
        border = BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(Color.black),
                BorderFactory.createBevelBorder(0, Color.gray, Color.darkGray)
        )
        adapter = ChessGameAdapter(this)
        addMouseListener(adapter)
        addMouseMotionListener(adapter)
        isDoubleBuffered = true

        imageTypeToImage = HashMap(20)
        for (imageType in ImageType.values()) {
            if (imageType.isFigure) {
                val figureImage = FigureImage(imageObserver, Images.get(imageType))
                imageTypeToImage[imageType] = figureImage
            }
        }
    }

    fun repaintAfterMove(move: Move) {
        repaintPositionAtOnce(move.from)
        repaintPositionAtOnce(move.to)

        val horizontalDiff = Math.abs(move.from.column - move.to.column)
        val verticalDiff = Math.abs(move.from.row - move.to.row)

        if (horizontalDiff == 1 && verticalDiff == 1) {                                //enpassant?
            repaintPositionAtOnce(Position.get(move.from.row, move.to.column))
            repaintPositionAtOnce(Position.get(move.to.row, move.from.column))
        } else if (verticalDiff == 0 && (move.to.row == 0 || move.to.row == 7)) {      //castling?muss auch für Schach960 funktionieren
            repaintRowAtOnce(move.from.row)
        }
    }

    private fun repaintPositionAtOnce(pos: Position) {
        val xPos = borderSize + areaSize * if (isWhiteView) pos.column else 7 - pos.column
        val yPos = borderSize + areaSize * if (isWhiteView) 7 - pos.row else pos.row

        val repaintSize = areaSize + 1
        paintImmediately(xPos, yPos, repaintSize, repaintSize)
    }

    private fun repaintRowAtOnce(row: Int) {
        val xPos = borderSize
        val yPos = borderSize + areaSize * if (isWhiteView) 7 - row else row

        val repaintSize = areaSize + 1
        paintImmediately(xPos, yPos, repaintSize * 8, repaintSize)
    }

    fun repaintAtOnce() {
        val dim = size
        paintImmediately(0, 0, dim.width, dim.height)
    }

    override fun paintComponent(g: Graphics) {
        paintBoard(g)
        paintActiveAreas(g)
        paintFigures(g)
    }

    private fun paintBoard(g: Graphics) {
        g.color = Color.white
        g.fillRect(0, 0, 2 * borderSize + 8 * areaSize, 2 * borderSize + 8 * areaSize)
        g.color = Color.lightGray
        g.drawRect(borderSize, borderSize, 8 * areaSize, 8 * areaSize)
        for (i in 0..7) {
            for (j in 0..7) {
                if ((i + j) % 2 == 1) {
                    g.fillRect(borderSize + i * areaSize, borderSize + j * areaSize, areaSize, areaSize)
                }
            }
        }
    }

    private fun paintFigures(g: Graphics) {
        for( figure in game.getFigures()) {
            val pos = figure.position
            val xPos = borderSize + areaSize * if (isWhiteView) pos.column else 7 - pos.column
            val yPos = borderSize + areaSize * if (isWhiteView) 7 - pos.row else pos.row

            val imageType = figure.imageType
            val figureImage = imageTypeToImage[imageType]!!
            figureImage.paint(g, xPos, yPos, areaSize)
        }
    }

    private fun paintActiveAreas(g: Graphics) {
        g.color = Color.darkGray
        if (from != null) {
            val xPos = borderSize + areaSize * if (isWhiteView) from!!.column else 7 - from!!.column
            val yPos = borderSize + areaSize * if (isWhiteView) 7 - from!!.row else from!!.row
            g.drawRect(xPos, yPos, areaSize, areaSize)
        }
        if (to != null) {
            val xPos = borderSize + areaSize * if (isWhiteView) to!!.column else 7 - to!!.column
            val yPos = borderSize + areaSize * if (isWhiteView) 7 - to!!.row else to!!.row
            g.drawRect(xPos, yPos, areaSize, areaSize)
        }
    }

    fun setViewPoint(fromWhite: Boolean) {
        isWhiteView = fromWhite
        repaint()
    }

    fun addPlayer(player: HumanPlayerInterface) {
        adapter.addPlayer(player)
    }

    fun markPosition(pos: Position?, isFromPosition: Boolean) {
        if (isFromPosition) {
            val temp = from
            if (from != null) {
                from = null
                if(temp!=null) {
                    repaintPositionAtOnce(temp)
                }
            }
            from = pos
        } else {
            val temp = to
            if (to != null) {
                to = null
                if(temp!=null) {
                    repaintPositionAtOnce(temp)
                }
            }
            to = pos
        }
        if (pos != null) repaintPositionAtOnce(pos)
    }
}
