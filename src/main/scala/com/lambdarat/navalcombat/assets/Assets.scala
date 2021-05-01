package com.lambdarat.navalcombat.assets

import indigo.*
import indigoextras.ui.*

object Assets:
  private val baseUrl = "assets"

  val ponderosaImgName  = AssetName("ponderosa_image")
  val ponderosaImgAsset = AssetType.Image(ponderosaImgName, AssetPath(s"$baseUrl/ponderosa.png"))

  val ponderosaJsonName  = AssetName("ponderosa_json")
  val ponderosaJsonAsset = AssetType.Text(ponderosaJsonName, AssetPath(s"$baseUrl/ponderosa.json"))
  val ponderosaFontKey   = FontKey("ponderosa")

  val simpleButtonName = AssetName("simple_button")
  val simpleButtonImg  = AssetType.Image(simpleButtonName, AssetPath(s"$baseUrl/two_simple_buttons.png"))

  val simpleButtonGraphic =
    Graphic(0, 0, 34, 10, 2, Material.Bitmap(simpleButtonName).toImageEffects.withSaturation(0))

  val simpleButtonAssets =
    ButtonAssets(
      up = simpleButtonGraphic.withCrop(0, 0, 34, 10).scaleBy(4, 4),
      over = simpleButtonGraphic.withCrop(0, 0, 34, 10).scaleBy(4, 4),
      down = simpleButtonGraphic.withCrop(0, 10, 34, 10).scaleBy(4, 4)
    )

  val assets: Set[AssetType] = Set(ponderosaImgAsset, ponderosaJsonAsset, simpleButtonImg)
