package voidchess.image

import voidchess.figures.Figure
import voidchess.figures.FigureType
import java.awt.image.ImageObserver

class FigureGallery(imageObserver: ImageObserver, imageWidthHeight: Int) {
    private val whitePaintables: Array<Paintable> = Array(FigureType.values().size) {
        Paintable(imageObserver, ImageLoader.getFigureImage(FigureType.values()[it], true, imageWidthHeight))
    }
    private val blackPaintables: Array<Paintable> = Array(FigureType.values().size) {
        Paintable(imageObserver, ImageLoader.getFigureImage(FigureType.values()[it], false, imageWidthHeight))
    }

    fun getPaintable(figure: Figure): Paintable {
        val paintableIndex = figure.type.ordinal
        return if(figure.isWhite) {
            whitePaintables[paintableIndex]
        }else{
            blackPaintables[paintableIndex]
        }
    }
}