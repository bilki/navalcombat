ThisBuild / scalaVersion := "3.0.1"

def indigoCommand(indigoTask: TaskKey[Unit], name: String) = Command.command(name) { state =>
  val indigoCmd = for {
    (compiled, result) <- Project.runTask(Compile / Keys.compile, state)
    _                  <- result.toEither.toOption
    (fastJS, _)        <- Project.runTask(Compile / fastOptJS, compiled)
    (indigo, _)        <- Project.runTask(indigoTask, fastJS)
  } yield indigo

  indigoCmd.getOrElse {
    println(s"Game command [$name] failed!")
    state.fail
  }
}

lazy val buildGame = indigoCommand(indigoBuild, "buildGame")
lazy val runGame   = indigoCommand(indigoRun, "runGame")

lazy val root = project
  .in(file("."))
  .enablePlugins(
    ScalaJSPlugin,
    SbtIndigo
  )
  .settings(
    name           := "Naval Combat",
    description    := "Battleship single player clone",
    organization   := "com.lambdarat",
    version        := "0.1.0",
    commands      ++= Seq(buildGame, runGame),
    scalacOptions ++= Seq("-language:strictEquality"),
    testFrameworks += new TestFramework("munit.Framework"),
    Test / scalaJSLinkerConfig ~= { _.withModuleKind(ModuleKind.CommonJSModule) },
    libraryDependencies ++= Seq(
      "org.scalameta" %%% "munit"            % "0.7.27" % Test,
      "org.scalameta" %%% "munit-scalacheck" % "0.7.27" % Test
    )
  )
  .settings(
    showCursor          := true,
    title               := "Naval Combat",
    gameAssetsDirectory := "assets",
    windowStartWidth    := 1280,
    windowStartHeight   := 720,
    libraryDependencies ++= Seq(
      "io.indigoengine" %%% "indigo-json-circe" % "0.9.0",
      "io.indigoengine" %%% "indigo"            % "0.9.0",
      "io.indigoengine" %%% "indigo-extras"     % "0.9.0"
    )
  )
