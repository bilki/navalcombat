ThisBuild / scalaVersion := "3.1.1"

def indigoCommand(indigoTask: TaskKey[Unit], name: String, full: Boolean = false) = Command.command(name) { state =>
  val indigoCmd = for {
    (compiled, result) <- Project.runTask(Compile / Keys.compile, state)
    _                  <- result.toEither.toOption
    optJS = if (full) Compile / fullOptJS else Compile / fastOptJS
    (jsResult, _) <- Project.runTask(optJS, compiled)
    (indigo, _)   <- Project.runTask(indigoTask, jsResult)
    (output, _)   <- if (full) Project.runTask(removeMap, indigo) else Option((indigo, Value(())))
  } yield output

  indigoCmd.getOrElse {
    println(s"Game command [$name] failed!")
    state.fail
  }
}

lazy val buildFullFolder =
  Def.setting[File](baseDirectory.value / "target" / "indigoBuildFull") // TODO change indigo sbt task output type

lazy val removeMap = taskKey[Unit]("Removes the .map file from the full output folder")
removeMap := io.IO.delete(buildFullFolder.value / "scripts" / s"${name.value}-opt.js.map")

lazy val buildGame     = indigoCommand(indigoBuild, "buildGame")
lazy val buildFullGame = indigoCommand(indigoBuildFull, "buildFullGame", full = true)
lazy val runGame       = indigoCommand(indigoRun, "runGame")

lazy val root = project
  .in(file("."))
  .enablePlugins(
    ScalaJSPlugin,
    SbtIndigo,
    GitHubPagesPlugin
  )
  .settings(
    name           := "naval-combat",
    description    := "Naval Combat: a Battleship single player clone",
    organization   := "com.lambdarat",
    version        := "0.1.0",
    commands      ++= Seq(buildGame, buildFullGame, runGame),
    scalacOptions ++= Seq("-language:strictEquality"),
    testFrameworks += new TestFramework("munit.Framework"),
    Test / scalaJSLinkerConfig ~= { _.withModuleKind(ModuleKind.CommonJSModule) },
    libraryDependencies ++= Seq(
      "org.scalameta" %%% "munit"            % "0.7.29" % Test,
      "org.scalameta" %%% "munit-scalacheck" % "0.7.29" % Test
    ),
    gitHubPagesSiteDir := buildFullFolder.value
  )
  .settings(
    showCursor          := true,
    title               := "Naval Combat",
    gameAssetsDirectory := "assets",
    windowStartWidth    := 1280,
    windowStartHeight   := 720,
    libraryDependencies ++= Seq(
      "io.indigoengine" %%% "indigo-json-circe" % "0.12.0",
      "io.indigoengine" %%% "indigo"            % "0.12.0",
      "io.indigoengine" %%% "indigo-extras"     % "0.12.0"
    )
  )
