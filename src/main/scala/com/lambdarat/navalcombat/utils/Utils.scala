package com.lambdarat.navalcombat.utils

import indigo.*

given CanEqual[Option[_], Option[_]]   = CanEqual.derived
given CanEqual[FrameTick, GlobalEvent] = CanEqual.derived

extension (graphic: Graphic)
  def alignCenter: Graphic = graphic.moveBy(graphic.bounds.width / 2, graphic.bounds.height / 2)

extension (rectangle: Rectangle)
  // Beware!! This function is not commutative with others like scaleBy!
  def alignCenter: Rectangle = rectangle.moveBy(Point(-rectangle.width / 2, rectangle.height / 2))
  def scaleBy(x: Double, y: Double): Rectangle =
    rectangle.copy(size = Point((rectangle.width * x).toInt, (rectangle.height.toDouble * y).toInt))
