package com.lambdarat.navalcombat.draw

import indigo._
import indigo.shared.materials.Material.ImageEffects
import com.lambdarat.navalcombat.core.{Coord, XCoord, YCoord}
import com.lambdarat.navalcombat.utils._

object Axis:
  private val FIRST_LETTER      = 'A'
  private val LAST_LETTER       = 'J'
  private val NUMBER_OF_NUMBERS = 10

  private val LETTER_MARGIN = 16
  private val NUMBER_MARGIN = 24

  def drawLetters(
      originSpace: Rectangle,
      targetSpace: Rectangle,
      toText: String => Text[ImageEffects]
  ): List[Text[ImageEffects]] =
    val letters =
      for (letter, y) <- (FIRST_LETTER to LAST_LETTER).zip(0 until originSpace.height)
      yield
        val cellCoord = Coord(XCoord(0), YCoord(y))
        val cellPoint = cellCoord.toPoint
        val position = cellPoint
          .transform(originSpace, targetSpace)
          .moveBy(-LETTER_MARGIN, LETTER_MARGIN)

        toText(letter.toString).withPosition(position)

    letters.toList
  end drawLetters

  def drawNumbers(
      originSpace: Rectangle,
      targetSpace: Rectangle,
      toText: String => Text[ImageEffects]
  ): List[Text[ImageEffects]] =
    val numbers =
      for (number, x) <- (1 to NUMBER_OF_NUMBERS).zip(0 until originSpace.width)
      yield
        val cellCoord = Coord(XCoord(x), YCoord(0))
        val cellPoint = cellCoord.toPoint
        val position = cellPoint
          .transform(originSpace, targetSpace)
          .moveBy(NUMBER_MARGIN * 2, -NUMBER_MARGIN)

        toText(number.toString).withPosition(position)

    numbers.toList
  end drawNumbers
