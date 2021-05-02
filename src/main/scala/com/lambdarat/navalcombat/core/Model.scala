package com.lambdarat.navalcombat.core

opaque type XCoord = Int
opaque type YCoord = Int

final case class Coord(x: XCoord, y: YCoord)

opaque type ShipSize = Int

object ShipSize:
  def apply(size: Int): ShipSize = size

enum Ship(val size: ShipSize):
  case Destroyer  extends Ship(ShipSize(2))
  case Cruiser    extends Ship(ShipSize(3))
  case Battleship extends Ship(ShipSize(4))
  case Carrier    extends Ship(ShipSize(5))

enum Cell:
  case Unknown
  case Miss
  case Sunk(partOf: Ship)
  case Floating(partOf: Ship)

final case class NavalCombatModel()
