package com.lambdarat.navalcombat.utils

import com.lambdarat.navalcombat.scenes.landing.LandingEvents.*

import indigo.*
import indigo.Material.Bitmap

given CanEqual[Option[?], Option[?]]         = CanEqual.derived
given CanEqual[FrameTick, GlobalEvent]       = CanEqual.derived
given CanEqual[PlayCombat.type, GlobalEvent] = CanEqual.derived

extension (graphic: Graphic)
  def scaledHeight: Int    = (graphic.bounds.height * graphic.scale.y).toInt
  def scaledWidth: Int     = (graphic.bounds.width * graphic.scale.x).toInt
  def alignCenter: Graphic = graphic.moveBy(-graphic.scaledWidth / 2, graphic.scaledHeight / 2)
  def alignRight: Graphic =
    graphic.moveTo(graphic.position.x - (graphic.bounds.width * graphic.scale.x).toInt, graphic.bounds.y)

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
