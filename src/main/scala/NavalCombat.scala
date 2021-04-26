import indigo._
import indigo.scenes._

import scala.scalajs.js.annotation.JSExportTopLevel

@JSExportTopLevel("IndigoGame")
object NavalCombat extends IndigoSandbox[Unit, Unit] {
  val animations: Set[Animation] = Set.empty
  val assets: Set[AssetType]     = Set.empty

  val config: GameConfig = GameConfig.default
    .withViewport(GameViewport.at720p)
    .withMagnification(2)
  val fonts: Set[FontInfo] = Set.empty
  val shaders: Set[Shader] = Set.empty

  def initialModel(startupData: Unit): Outcome[Unit] = Outcome(())

  def present(context: FrameContext[Unit], model: Unit): Outcome[SceneUpdateFragment] =
    Outcome(SceneUpdateFragment.empty)

  def setup(assetCollection: AssetCollection, dice: Dice): Outcome[Startup[Unit]] =
    Outcome(Startup.Success(()))

  def updateModel(context: FrameContext[Unit], model: Unit): GlobalEvent => Outcome[Unit] =
    _ => Outcome(())
}
