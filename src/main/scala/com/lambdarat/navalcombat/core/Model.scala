package com.lambdarat.navalcombat.core

import scala.collection.immutable.ArraySeq
import scala.annotation.targetName

// X is the horizontal axis
opaque type XCoord = Int
given CanEqual[XCoord, XCoord] = CanEqual.derived

object XCoord:
  def apply(x: Int): XCoord = x

  extension (x: XCoord)
    @targetName("addX")
    def +(dx: Int): XCoord        = XCoord(x + dx)
    def -(dx: Int): XCoord        = XCoord(x - dx)
    def <(other: XCoord): Boolean = x < other
    def toInt: Int                = x.toInt

// Y is the vertical axis
opaque type YCoord = Int
given CanEqual[YCoord, YCoord] = CanEqual.derived

object YCoord:
  def apply(y: Int): YCoord = y

  extension (y: YCoord)
    @targetName("addY")
    def +(dy: Int): YCoord        = YCoord(y + dy)
    def -(dy: Int): YCoord        = YCoord(y - dy)
    def <(other: YCoord): Boolean = y < other
    def toInt: Int                = y.toInt

final case class Coord(x: XCoord, y: YCoord) derives CanEqual

opaque type ShipSize = Int
given CanEqual[ShipSize, Int] = CanEqual.derived

object ShipSize:
  def apply(size: Int): ShipSize = size

  extension (shipShize: ShipSize)
    def toInt: Int       = shipShize.toInt
    def toDouble: Double = shipShize.toDouble

enum Ship(val size: ShipSize) derives CanEqual:
  case Destroyer  extends Ship(ShipSize(2))
  case Submarine  extends Ship(ShipSize(3))
  case Cruiser    extends Ship(ShipSize(3))
  case Battleship extends Ship(ShipSize(4))
  case Carrier    extends Ship(ShipSize(5))

enum Cell derives CanEqual:
  case Unknown
  case Miss
  case Sunk(partOf: Ship)
  case Floating(partOf: Ship)

enum Rotation derives CanEqual:
  case Horizontal, Vertical

extension (rotation: Rotation)
  def reverse: Rotation =
    rotation match
      case Rotation.Horizontal => Rotation.Vertical
      case Rotation.Vertical   => Rotation.Horizontal

final case class Board(cells: ArraySeq[ArraySeq[Cell]], ships: Map[Ship, ShipOrientation])

object Board:
  val BOARD_SIZE   = 10
  def empty: Board = Board(ArraySeq.fill(BOARD_SIZE, BOARD_SIZE)(Cell.Unknown), Map.empty)

final case class ShipOrientation(coord: Coord, rotation: Rotation):

  def sections(ship: Ship): List[Coord] = rotation match
    case Rotation.Horizontal =>
      coord :: (1 until ship.size.toInt).toList
        .map(shiftX => coord.copy(x = coord.x + shiftX))
    case Rotation.Vertical =>
      coord :: (1 until ship.size.toInt).toList
        .map(shiftY => coord.copy(y = coord.y - shiftY))

final case class NavalCombatModel(player: Board, enemy: Board)
