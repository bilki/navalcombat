package com.lambdarat.navalcombat.utils

import com.lambdarat.navalcombat.core.*
import com.lambdarat.navalcombat.scenes.landing.LandingEvents.*

import indigo.*
import indigo.Material.Bitmap
import indigoextras.geometry.Vertex

given CanEqual[Option[?], Option[?]]   = CanEqual.derived
given CanEqual[FrameTick, GlobalEvent] = CanEqual.derived

extension (coord: Coord) def toPoint: Point = Point(coord.x.toInt, coord.y.toInt)

extension (point: Point)

  // Transforms a point from coordinate system origin into target
  def transform(origin: Rectangle, target: Rectangle): Point =
    // (0,0) is (origin.x, origin.y)
    val newX = (point.x - origin.x) * target.width / origin.width
    val newY = (point.y - origin.y) * target.height / origin.height

    Point(target.x + Math.floor(newX).toInt, target.y + Math.floor(newY).toInt)

  def toCoord: Coord = Coord(XCoord(point.x), YCoord(point.y))
end extension

private[utils] val PIplusHalf = Radians(Math.PI * 1.5)

extension (graphic: Graphic[Bitmap])
  def scaledHeight: Int = (graphic.bounds.height * graphic.scale.y).toInt
  def scaledWidth: Int  = (graphic.bounds.width * graphic.scale.x).toInt
  def center: Point     = Point(graphic.bounds.width / 2, graphic.bounds.height / 2)

  def alignRight: Graphic[Bitmap] =
    graphic.moveTo(graphic.position.x - graphic.bounds.width, graphic.bounds.y)
  def alignBottom: Graphic[Bitmap] =
    graphic.moveTo(graphic.position.x, graphic.bounds.y - (graphic.bounds.height * graphic.scale.y).toInt)

extension (bm: Bitmap) def toZeroGraphic: Graphic[Bitmap] = Graphic(0, 0, bm)

extension (rectangle: Rectangle)
  def alignCenter: Rectangle = rectangle.moveBy(Point(-rectangle.width / 2, rectangle.height / 2))
  def scaleBy(x: Double, y: Double): Rectangle =
    rectangle.copy(size = Size((rectangle.width * x).toInt, (rectangle.height.toDouble * y).toInt))

extension [A](signal: Signal[A])
  def when[B](pred: A => Boolean, positive: B, negative: B): Signal[B] =
    signal.map(s => if pred(s) then positive else negative)

object ExtraColors:
  val LightGrey: RGBA = RGBA.fromColorInts(225, 225, 225)
  val Grey: RGBA      = RGBA.fromColorInts(150, 150, 150)
