package com.lambdarat.navalcombat.scenes.placement.viewmodel

import com.lambdarat.navalcombat.core.*

import indigoextras.trees.QuadTree

import indigo.*

final case class CellPosition(cell: Cell, position: Coord)

final case class SidebarBoatPosition(boatType: Ship, boat: Graphic)

final case class PlacementViewModel(
    bounds: Rectangle,
    startTime: Seconds,
    gridPoints: List[Point],
    placeMsgSignal: Signal[Point],
    grid: QuadTree[CellPosition],
    boats: List[SidebarBoatPosition],
    dragging: Option[Ship]
)
