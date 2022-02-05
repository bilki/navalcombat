package com.lambdarat.navalcombat.draw

import indigo._
import indigo.shared.materials.Material.ImageEffects
import com.lambdarat.navalcombat.core.{Board, Cell, Coord, XCoord, YCoord}
import com.lambdarat.navalcombat.engine.BoardEngine._
import com.lambdarat.navalcombat.utils._

object Grid:
  def draw(
      originSpace: Rectangle,
      targetSpace: Rectangle,
      board: Board,
      toGraphic: (Cell, Coord, Point) => Option[Graphic[ImageEffects]]
  ): List[Option[Graphic[ImageEffects]]] =
    val cellsGraphics = for
      x <- 0 until originSpace.width
      y <- 0 until originSpace.height
    yield
      val cellCoord      = Coord(XCoord(x), YCoord(y))
      val cellPoint      = cellCoord.toPoint
      val gridSpacePoint = cellPoint.transform(originSpace, targetSpace)

      val maybeCell = board.get(cellCoord.x, cellCoord.y)

      for
        cell    <- maybeCell
        graphic <- toGraphic(cell, cellCoord, gridSpacePoint)
      yield graphic

    cellsGraphics.toList
  end draw
