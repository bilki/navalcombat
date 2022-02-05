package com.lambdarat.navalcombat.scenes.placement.viewmodel

import com.lambdarat.navalcombat.core.*

import indigo.*
import indigo.Material.ImageEffects

enum Highlight derives CanEqual:
  case NotValid
  case Valid
  case Neutral

final case class SceneSettings(sceneBounds: Rectangle, gridBounds: Rectangle, modelSpace: Rectangle)

final case class Highlighted(position: Coord, highlight: Highlight)

final case class PlacingShip(ship: Ship, rotation: Rotation)

final case class SidebarShipGraphics(
    destroyer: Graphic[ImageEffects],
    cruiser: Graphic[ImageEffects],
    submarine: Graphic[ImageEffects],
    battleship: Graphic[ImageEffects],
    carrier: Graphic[ImageEffects]
)

final case class PlacementViewModel(
    sceneSettings: SceneSettings,
    sidebarShipGraphics: SidebarShipGraphics,
    highlightedCells: List[Highlighted],
    sidebarShips: List[Ship],
    dragging: Option[PlacingShip]
)
