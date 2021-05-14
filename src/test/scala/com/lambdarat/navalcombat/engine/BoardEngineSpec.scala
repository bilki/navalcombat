package com.lambdarat.navalcombat.engine

import com.lambdarat.navalcombat.core.*

import munit.ScalaCheckSuite
import org.scalacheck.Arbitrary
import org.scalacheck.Gen
import org.scalacheck.Prop._

import com.lambdarat.navalcombat.generators.ModelGen
import com.lambdarat.navalcombat.generators.ModelGen.validCoord

class BoardEngineSpec extends ScalaCheckSuite {
  import BoardEngine.*

  property("valid coords must return a cell for the empty board") {
    forAll { (coord: Coord) =>
      val expected = Some(Cell.Unknown)

      val cell = Board.empty.get(coord.x, coord.y)

      assertEquals(clue(cell), expected)
    }
  }

  property("negative X coords must return a non-defined cell for the empty board") {
    forAll(ModelGen.invalidXcoord.arbitrary, ModelGen.validYCoord.arbitrary) { (x, y) =>
      val expected = None

      val cell = Board.empty.get(x, y)

      assertEquals(clue(cell), expected)
    }
  }

  property("negative Y coords must return a non-defined cell for the empty board") {
    forAll(ModelGen.validXcoord.arbitrary, ModelGen.invalidYcoord.arbitrary) { (x, y) =>
      val expected = None

      val cell = Board.empty.get(x, y)

      assertEquals(clue(cell), expected)
    }
  }

  property("X coords bigger than board size must return a non-defined cell for the empty board") {
    forAll(Gen.chooseNum(Board.BOARD_SIZE, Int.MaxValue).map(XCoord(_)), ModelGen.validYCoord.arbitrary) { (x, y) =>
      val expected = None

      val cell = Board.empty.get(x, y)

      assertEquals(clue(cell), expected)
    }
  }

  property("Y coords bigger than board size must return a non-defined cell for the empty board") {
    forAll(ModelGen.validXcoord.arbitrary, Gen.chooseNum(Board.BOARD_SIZE, Int.MaxValue).map(YCoord(_))) { (x, y) =>
      val expected = None

      val cell = Board.empty.get(x, y)

      assertEquals(clue(cell), expected)
    }
  }

}
