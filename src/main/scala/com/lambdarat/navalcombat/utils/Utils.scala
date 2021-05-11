package com.lambdarat.navalcombat.utils

import com.lambdarat.navalcombat.scenes.landing.LandingEvents.*

import indigo.*
import indigo.Material.Bitmap
import indigoextras.geometry.Vertex

given CanEqual[Option[?], Option[?]]   = CanEqual.derived
given CanEqual[FrameTick, GlobalEvent] = CanEqual.derived

extension (point: Point)

  // Normalizes this point into the target x target coordinate system with a new origin (0,0) defined by a rectangle
  def transform(into: Rectangle, target: Int): Vertex =
    // (0,0) is (into.x, into.y)
    val newX = (point.x - into.x) * target / into.width
    val newY = (point.y - into.y) * target / into.height

    Vertex(newX, newY)

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
