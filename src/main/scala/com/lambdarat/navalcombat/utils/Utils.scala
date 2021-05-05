package com.lambdarat.navalcombat.utils

import indigo.*
import indigo.Material.Bitmap

import com.lambdarat.navalcombat.scenes.LandingEvents.*

given CanEqual[Option[?], Option[?]]         = CanEqual.derived
given CanEqual[FrameTick, GlobalEvent]       = CanEqual.derived
given CanEqual[PlayCombat.type, GlobalEvent] = CanEqual.derived

extension (graphic: Graphic)
  def alignCenter: Graphic = graphic.moveBy(graphic.bounds.width / 2, graphic.bounds.height / 2)

extension (bm: Bitmap) def toZeroGraphic: Graphic = Graphic(0, 0, bm)

extension (rectangle: Rectangle)
  // Beware!! This function is not commutative with others like scaleBy!
  def alignCenter: Rectangle = rectangle.moveBy(Point(-rectangle.width / 2, rectangle.height / 2))
  def scaleBy(x: Double, y: Double): Rectangle =
    rectangle.copy(size = Point((rectangle.width * x).toInt, (rectangle.height.toDouble * y).toInt))

object ExtraColors:
  val LightGrey: RGBA = RGBA.fromColorInts(225, 225, 225)
  val Grey: RGBA      = RGBA.fromColorInts(150, 150, 150)
