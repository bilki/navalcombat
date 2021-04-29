package com.lambdarat.navalcombat.assets

import indigo.*

object Assets:
  private val baseUrl = "assets"

  val ponderosaImgName  = AssetName("ponderosa_image")
  val ponderosaImgAsset = AssetType.Image(ponderosaImgName, AssetPath(s"$baseUrl/ponderosa.png"))

  val ponderosaJsonName  = AssetName("ponderosa_json")
  val ponderosaJsonAsset = AssetType.Text(ponderosaJsonName, AssetPath(s"$baseUrl/ponderosa.json"))
  val ponderosaFontKey   = FontKey("ponderosa")

  val assets: Set[AssetType] = Set(ponderosaImgAsset, ponderosaJsonAsset)
