package com.lambdarat.navalcombat.generators

import com.lambdarat.navalcombat.core.*

import org.scalacheck.Arbitrary
import org.scalacheck.Arbitrary.*
import org.scalacheck.Gen

object ModelGen:
  given Arbitrary[Ship] = Arbitrary(arbitrary[Ship])

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
