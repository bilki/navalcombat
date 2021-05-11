package com.lambdarat.navalcombat.scenes.placement.viewmodel

import com.lambdarat.navalcombat.core.*

import indigoextras.trees.QuadTree

import indigo.*

enum Highlight derives CanEqual:
  case NotValid
  case Valid
  case Neutral

final case class CellPosition(cell: Cell, position: Coord, cellGraphic: Graphic, highlight: Highlight)

final case class SidebarShip(shipType: Ship, shipGraphic: Graphic)

final case class PlacingShip(sidebarShip: SidebarShip, rotation: Rotation)

final case class SceneSettings(sceneBounds: Rectangle, gridBounds: Rectangle)

final case class PlacementViewModel(
    sceneSettings: SceneSettings,
    startTime: Seconds,
    placeMsgSignal: Signal[Point],
    grid: QuadTree[CellPosition],
    highlightedCells: List[CellPosition],
    sidebarShips: List[SidebarShip],
    gridShips: List[SidebarShip],
    dragging: Option[PlacingShip]
)
