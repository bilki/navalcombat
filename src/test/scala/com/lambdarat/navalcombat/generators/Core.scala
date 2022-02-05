package com.lambdarat.navalcombat.generators

import com.lambdarat.navalcombat.core.*
import com.lambdarat.navalcombat.engine.BoardEngine.*

import org.scalacheck.*
import org.scalacheck.Arbitrary.*

object ModelGen:
  given Arbitrary[Ship] = Arbitrary(Gen.oneOf(Ship.values.toList))

  given validXcoord: Arbitrary[XCoord] = Arbitrary(Gen.chooseNum(0, Board.BOARD_SIZE - 1).map(XCoord(_)))
  given validYCoord: Arbitrary[YCoord] = Arbitrary(Gen.chooseNum(0, Board.BOARD_SIZE - 1).map(YCoord(_)))

  given invalidXcoord: Arbitrary[XCoord] = Arbitrary(Gen.negNum[Int].map(XCoord(_)))
  given invalidYcoord: Arbitrary[YCoord] = Arbitrary(Gen.negNum[Int].map(YCoord(_)))

  given validCoord: Arbitrary[Coord] =
    val coord = for
      x <- validXcoord.arbitrary
      y <- validYCoord.arbitrary
    yield Coord(x, y)

    Arbitrary(coord)

  given Arbitrary[Rotation] = Arbitrary(Gen.oneOf(Rotation.Vertical, Rotation.Horizontal))

  def validPlacement(board: Board): Arbitrary[(Ship, Coord, Rotation)] =
    val validPlaceGen = for
      ship     <- arbitrary[Ship]
      rotation <- arbitrary[Rotation]
      coord    <- arbitrary[Coord].filter(c => board.canPlace(ship, rotation, c.x, c.y))
    yield (ship, coord, rotation)

    Arbitrary(validPlaceGen)

  def validPlacementFor(ship: Ship, board: Board): Arbitrary[(Coord, Rotation)] =
    val validPlaceGen = for
      rotation <- arbitrary[Rotation]
      coord    <- arbitrary[Coord].filter(c => board.canPlace(ship, rotation, c.x, c.y))
    yield (coord, rotation)

    Arbitrary(validPlaceGen)

  def invalidPlacement(board: Board): Arbitrary[(Ship, Coord, Rotation)] =
    val invalidPlaceGen = for
      ship     <- arbitrary[Ship]
      rotation <- arbitrary[Rotation]
      coord    <- arbitrary[Coord].filterNot(c => board.canPlace(ship, rotation, c.x, c.y))
    yield (ship, coord, rotation)

    Arbitrary(invalidPlaceGen)

  def overlapping(board: Board): Arbitrary[(Ship, Coord, Rotation)] =
    val overlappingPlaceGen = for
      ship                        <- arbitrary[Ship].filterNot(board.ships.keySet.contains)
      rotation                    <- arbitrary[Rotation]
      (overShip, overOrientation) <- Gen.oneOf(board.ships)
      overSection                 <- Gen.oneOf(overOrientation.sections(overShip))
    yield (ship, overSection, rotation)

    Arbitrary(overlappingPlaceGen)

  given Arbitrary[ShipOrientation] =
    val shipOrientationGen = for
      coord    <- arbitrary[Coord]
      rotation <- arbitrary[Rotation]
    yield ShipOrientation(coord, rotation)

    Arbitrary(shipOrientationGen)
