package com.lambdarat.navalcombat.scenes.placement.viewmodel

import com.lambdarat.navalcombat.core.*

import indigoextras.trees.QuadTree

import indigo.*

enum Highlight derives CanEqual:
  case NotValid
  case Valid
  case Neutral

final case class SceneSettings(sceneBounds: Rectangle, gridBounds: Rectangle, modelSpace: Rectangle)

final case class Highlighted(position: Coord, highlight: Highlight)

final case class PlacingShip(ship: Ship, rotation: Rotation)

final case class SidebarShipGraphics(
    destroyer: Graphic,
    cruiser: Graphic,
    submarine: Graphic,
    battleship: Graphic,
    carrier: Graphic
)

final case class PlacementViewModel(
    sceneSettings: SceneSettings,
    sidebarShipGraphics: SidebarShipGraphics,
    startTime: Seconds,
    placeMsgSignal: Signal[Point],
    highlightedCells: List[Highlighted],
    sidebarShips: List[Ship],
    dragging: Option[PlacingShip]
)
