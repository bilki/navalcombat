package com.lambdarat.navalcombat.draw

import indigo.*
import indigo.Material.ImageEffects
import com.lambdarat.navalcombat.core.{Coord, XCoord, YCoord}
import com.lambdarat.navalcombat.utils.*

object Axis:
  private val FIRST_LETTER      = 'A'
  private val LAST_LETTER       = 'J'
  private val NUMBER_OF_NUMBERS = 10

  private val LETTER_MARGIN = 16
  private val NUMBER_MARGIN = 32

  def drawLetters(
      originSpace: Rectangle,
      targetSpace: Rectangle,
      toText: String => Text[ImageEffects],
      scale: Vector2 = Vector2(1, 1)
  ): List[Text[ImageEffects]] =
    val letters =
      for (letter, y) <- (FIRST_LETTER to LAST_LETTER).zip(0 until originSpace.height)
      yield
        val cellCoord = Coord(XCoord(0), YCoord(y))
        val cellPoint = cellCoord.toPoint
        val position = cellPoint
          .transform(originSpace, targetSpace)
          .moveBy(-(LETTER_MARGIN * scale.x).toInt, (LETTER_MARGIN * scale.y).toInt)

        toText(letter.toString).withPosition(position)

    letters.toList
  end drawLetters

  def drawNumbers(
      originSpace: Rectangle,
      targetSpace: Rectangle,
      toText: String => Text[ImageEffects],
      scale: Vector2 = Vector2(1, 1)
  ): List[Text[ImageEffects]] =
    val numbers =
      for (number, x) <- (1 to NUMBER_OF_NUMBERS).zip(0 until originSpace.width)
      yield
        val cellCoord = Coord(XCoord(x), YCoord(0))
        val cellPoint = cellCoord.toPoint
        val position = cellPoint
          .transform(originSpace, targetSpace)
          .moveBy((NUMBER_MARGIN * scale.x).toInt, -NUMBER_MARGIN.toInt)

        toText(number.toString).withPosition(position)

    numbers.toList
  end drawNumbers
