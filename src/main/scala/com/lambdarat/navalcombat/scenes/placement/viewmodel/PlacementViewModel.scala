package com.lambdarat.navalcombat.scenes.placement.viewmodel

import com.lambdarat.navalcombat.core.*

import indigoextras.trees.QuadTree

import indigo.*

final case class CellPosition(cell: Cell, position: Coord, cellGraphic: Graphic)

final case class SidebarShip(shipType: Ship, shipGraphic: Graphic)

final case class PlacingShip(sidebarShip: SidebarShip, rotation: Rotation)

final case class PlacementViewModel(
    bounds: Rectangle,
    startTime: Seconds,
    placeMsgSignal: Signal[Point],
    grid: QuadTree[CellPosition],
    sidebarShips: List[SidebarShip],
    gridShips: List[SidebarShip],
    dragging: Option[PlacingShip]
)
