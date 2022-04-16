package GUI

import scalafx.application.JFXApp
import scalafx.scene.{Camera, Node, ParallelCamera, Scene, SnapshotParameters}
import Constants.Constants
import Game.{Pos, TimeTrial}
import scalafx.geometry.{Point2D, Point3D}
import scalafx.scene.image.{Image, ImageView}
import scalafx.scene.input.KeyCode
import scalafx.scene.transform.Rotate


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

  val set = scala.collection.mutable.Set[String]()

  def update() = {
    game.player.update()

    def p = game.player.pos
    if (Controls.InputManager.keysPressed.contains(KeyCode.Q)) {
      set += s"(${p.getX}, ${p.getY}, ${p.getR})"
      println(set.mkString(" "))
    }

    val rotated: Node = game.track.image
    val car = game.player.draw()
    rotated.scaleX = Constants.mapScale
    rotated.scaleY = Constants.mapScale
    rotated.relocate(-game.player.pos.getX * game.track.pixelsPerMeter + Constants.width/2, -game.player.pos.getY * game.track.pixelsPerMeter + Constants.height/2)

    val carScale:Double = game.track.pixelsPerMeter / (145.0 / 5)
    car.scaleX = carScale
    car.scaleY = carScale

//    val a = new Rotate()
//    a.setPivotX(-game.player.pos.getX * Constants.mapPixelsPerMeter)
//    a.setPivotY(-game.player.pos.getY * Constants.mapPixelsPerMeter)
//    rotated.getTransforms.add(a)
//    a.setAngle(game.player.pos.getR)
    root.content = Array(rotated, car)
//    println(game.player.mouseXtoSteeringInput)
//    println("" + game.player.speed + ", " + game.player.pos)
  }

  // Add rectangle to scene

  val ticker = new Ticker(update)
  ticker.start()
}
