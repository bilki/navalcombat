package com.lambdarat.navalcombat.draw

import indigo._
import indigo.shared.materials.Material.Bitmap
import com.lambdarat.navalcombat.assets.Assets.*
import com.lambdarat.navalcombat.core.Ship
import com.lambdarat.navalcombat.core.Ship.*

object Graphics:
  def graphicFor(ship: Ship): Graphic[Bitmap] =
    ship match
      case Destroyer  => destroyer
      case Cruiser    => cruiser
      case Submarine  => submarine
      case Battleship => battleship
      case Carrier    => carrier
