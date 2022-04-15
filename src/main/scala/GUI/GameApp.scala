package GUI

import scalafx.application.JFXApp
import scalafx.scene.Scene
import Constants.Constants
import Game.TimeTrial


// Starting point for our ScalaFX application.
object GUI extends JFXApp {

  val game = new TimeTrial(Game.TestTrack)


  // Objects drawn in window are children of this Scene
  val root = new Scene
  Controls.InputManager.handleInput(root)


  stage = new JFXApp.PrimaryStage {
    title.value = "Autopeli"
    width = Constants.width
    height = Constants.height
    scene = root
  }

    def update() = {
      game.player.update()
      root.content = game.player.draw()
      println("" + game.player.speed + ", " + game.player.pos)
    }

    // Add rectangle to scene

    val ticker = new Ticker(update)
    ticker.start()
}
