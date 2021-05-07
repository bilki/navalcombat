package com.lambdarat.navalcombat.scenes.placement.viewmodel

import com.lambdarat.navalcombat.core.Cell

import indigoextras.trees.QuadTree

import indigo.*

final case class CellPosition(cell: Cell, position: Point)

final case class PlacementViewModel(
    bounds: Rectangle,
    startTime: Seconds,
    gridPoints: List[Point],
    placeMsgSignal: Signal[Point],
    grid: QuadTree[CellPosition]
)
