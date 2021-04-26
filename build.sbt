lazy val root = project
  .in(file("."))
  .enablePlugins(
    ScalaJSPlugin,
    SbtIndigo
  )
  .settings(
    name         := "Naval Combat",
    description  := "Battleship single player clone",
    organization := "com.lambdarat",
    version      := "0.1.0",
    scalaVersion := "3.0.0-RC2"
  )
  .settings(
    showCursor          := true,
    title               := "Naval Combat",
    gameAssetsDirectory := "assets",
    windowStartWidth    := 1280,
    windowStartHeight   := 720,
    libraryDependencies ++= Seq(
      "io.indigoengine" %%% "indigo-json-circe" % "0.7.1",
      "io.indigoengine" %%% "indigo"            % "0.7.1",
      "io.indigoengine" %%% "indigo-extras"     % "0.7.1"
    )
  )

addCommandAlias("buildGame", ";compile;fastOptJS;indigoBuild")
addCommandAlias("runGame", ";compile;fastOptJS;indigoRun")
