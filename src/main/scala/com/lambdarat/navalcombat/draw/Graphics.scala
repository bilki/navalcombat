package com.lambdarat.navalcombat.draw

import com.lambdarat.navalcombat.assets.Assets.*
import com.lambdarat.navalcombat.core.{Cell, Section, Ship}
import com.lambdarat.navalcombat.core.Ship.*
import com.lambdarat.navalcombat.scenes.placement.viewmodel.Highlight
import com.lambdarat.navalcombat.scenes.placement.viewmodel.Highlight.*

import indigo.*
import indigo.Material.ImageEffects
import indigo.Fill.Color

object Graphics:
  def graphicFor(ship: Ship): Graphic[ImageEffects] =
    ship match
      case Destroyer  => destroyer
      case Cruiser    => cruiser
      case Submarine  => submarine
      case Battleship => battleship
      case Carrier    => carrier

  private val SECTION_WIDTH_HEIGHT = 64

  def graphicFor(ship: Ship, section: Section): Graphic[ImageEffects] =
    val shipGraphic = graphicFor(ship)
    val cropFromX   = SECTION_WIDTH_HEIGHT * section.ordinal
    shipGraphic.withCrop(shipGraphic.crop.x + cropFromX, shipGraphic.crop.y, SECTION_WIDTH_HEIGHT, SECTION_WIDTH_HEIGHT)

  def graphicFor(ship: Ship, highlight: Highlight): Graphic[ImageEffects] =
    (ship, highlight) match
      case (Destroyer, NotValid)         => notValidDestroyer
      case (Destroyer, Neutral | Valid)  => destroyer
      case (Cruiser, NotValid)           => notValidCruiser
      case (Cruiser, Neutral | Valid)    => cruiser
      case (Submarine, NotValid)         => notValidSubmarine
      case (Submarine, Neutral | Valid)  => submarine
      case (Battleship, NotValid)        => notValidBattleship
      case (Battleship, Neutral | Valid) => battleship
      case (Carrier, NotValid)           => notValidCarrier
      case (Carrier, Neutral | Valid)    => carrier

  def empty(highlight: Highlight): Graphic[ImageEffects] =
    highlight match
      case Neutral  => emptyCell
      case Valid    => validEmptyCell
      case NotValid => notValidEmptyCell
