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

private[utils] val PIby3q = Radians(Math.PI * 1.5)

extension (graphic: Graphic)
  def scaledHeight: Int = (graphic.bounds.height * graphic.scale.y).toInt
  def scaledWidth: Int  = (graphic.bounds.width * graphic.scale.x).toInt
  def alignCenter: Graphic =
    val rotationWidth =
      Math.abs(Math.cos(graphic.rotation.value) * graphic.bounds.width) +
        Math.abs(Math.sin(graphic.rotation.value) * graphic.bounds.height)
    val rotationHeight =
      Math.abs(Math.sin(graphic.rotation.value) * graphic.bounds.width) +
        Math.abs(Math.cos(graphic.rotation.value) * graphic.bounds.height)
    val xTranslation =
      if graphic.rotation.value >= Radians.PIby2.value && graphic.rotation.value < PIby3q.value then
        (rotationWidth / 2).toInt
      else -(rotationWidth / 2).toInt
    val yTranslation =
      if graphic.rotation.value >= Radians.PI.value && graphic.rotation.value < Radians.`2PI`.value then
        (rotationHeight / 2).toInt
      else -(rotationHeight / 2).toInt

    graphic.moveBy(xTranslation, yTranslation)
  def alignRight: Graphic =
    graphic.moveTo(graphic.position.x - (graphic.bounds.width * graphic.scale.x).toInt, graphic.bounds.y)
  def alignBottom: Graphic =
    graphic.moveTo(graphic.position.x, graphic.bounds.y - (graphic.bounds.height * graphic.scale.y).toInt)
  def centerAt(position: Point): Graphic = graphic.moveTo(position).alignCenter

extension (bm: Bitmap) def toZeroGraphic: Graphic = Graphic(0, 0, bm)

extension (rectangle: Rectangle)
  // Beware!! This function is not commutative with others like scaleBy!
  def alignCenter: Rectangle = rectangle.moveBy(Point(-rectangle.width / 2, rectangle.height / 2))
  def scaleBy(x: Double, y: Double): Rectangle =
    rectangle.copy(size = Point((rectangle.width * x).toInt, (rectangle.height.toDouble * y).toInt))

extension [A](signal: Signal[A])

  def when[B](pred: A => Boolean, positive: B, negative: B): Signal[B] =
    signal.map(s => if pred(s) then positive else negative)

object ExtraColors:
  val LightGrey: RGBA = RGBA.fromColorInts(225, 225, 225)
  val Grey: RGBA      = RGBA.fromColorInts(150, 150, 150)
