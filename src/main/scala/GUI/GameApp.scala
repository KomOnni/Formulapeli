package GUI

import scalafx.application.JFXApp
import scalafx.scene.{Node, Scene}
import Constants.Constants
import Game.{Pos, TimeTrial}
import scalafx.scene.control.Label
import scalafx.scene.input.KeyCode
import scalafx.scene.paint.Color
import scalafx.scene.paint.Color.{Black, DarkGray, DimGray, White}
import scalafx.scene.shape.Rectangle
import scala.math._


// Starting point for our ScalaFX application.
object GUI extends JFXApp {

  val game = new TimeTrial(Game.Blackwood)

  // Objects drawn in window are children of this Scene
  val root = new Scene
  Controls.InputManager.handleInput(root)
  root.setFill(Color.rgb(89,111,38))

  stage = new JFXApp.PrimaryStage {
    title.value = "Autopeli"
    width = Constants.width
    height = Constants.height
    scene = root
  }

  def gamePosToTrack(pos: Pos) = {
    (-game.player.pos.getX * game.track.pixelsPerMeter, -game.player.pos.getY * game.track.pixelsPerMeter)
  }

  val set = scala.collection.mutable.Set[String]()

  def update() = {
    game.player.update()

    //Voi saada paikat aloituspaikkoja ja AI:n reittiä varten
    def p = game.player.pos
    if (Controls.InputManager.keysPressed.contains(KeyCode.Q)) {
      set += s"(${p.getX}, ${p.getY}, ${p.getR})"
      println(set.mkString(" "))
    }

    val rotated: Node = game.track.image
    val car: Node = game.player.draw

    val info = Rectangle(50,200,200,Constants.height - 2 * 200)
    info.fill = DimGray
    info.opacity = 0.75

    //Informaatiolle labelit yms.

    def infoLabelPos(i: Int) = {
      (100,
      200 + 50 * i)
    }

    val l1 = new Label(s"${(game.player.speed * 3.6).toInt} km/h") {
      val pos = infoLabelPos(1)
      translateX = pos._1 + 25
      translateY = pos._2
      textFill = Color.White
      scaleX = 4
      scaleY = 4
    }

    val l2 = new Label(s"Last sector: ${game.player.lastSector.getOrElse("None")}") {
      val pos = infoLabelPos(2)
      translateX = pos._1
      translateY = pos._2
      textFill = Color.White
      scaleX = 2
      scaleY = 2
    }

    val l3 = new Label(s"Last lap: ${game.player.lastLap.getOrElse("None")}") {
      val pos = infoLabelPos(3)
      translateX = pos._1
      translateY = pos._2
      textFill = Color.White
      scaleX = 2
      scaleY = 2
    }

    val l4 = new Label(s"Best lap: ${game.player.bestLap.getOrElse("None")}") {
      val pos = infoLabelPos(4)
      translateX = pos._1
      translateY = pos._2
      textFill = Color.White
      scaleX = 2
      scaleY = 2
    }

    //Seuraavat kaksi näyttävät steeringanglen pelissä

    val steeringWhite = new Rectangle {
      x = Constants.width / 2 - 300
      y = 900
      width = 600
      height = 10
      fill = White //scalafx.scene.paint.Color
    }

    val steeringRed = new Rectangle {
      x = Constants.width / 2 - 300 + (game.player.steeringInput + Constants.maxSteeringAngle) * steeringWhite.width.value / 2 / Constants.maxSteeringAngle
      y = steeringWhite.y.value
      width = steeringWhite.height.value
      height = steeringWhite.height.value
      fill = Color.Red
    }

    //Seuraavien funktioiden avulla voi seurata autoa sen perspektiivistä.

    rotated.rotate = -90 - game.player.pos.getR

    val imgCenter = (game.track.img.width.value / 2, game.track.img.height.value / 2)
    val ogPoint = (game.player.pos.getX * game.track.pixelsPerMeter, game.player.pos.getY * game.track.pixelsPerMeter)
    val ogPointDiff = (game.player.pos.getX * game.track.pixelsPerMeter - imgCenter._1, game.player.pos.getY * game.track.pixelsPerMeter - imgCenter._2)
    val sign = if (ogPointDiff._1 < 0) 0 else 1
    val pointDiffDistance = sqrt(pow(ogPointDiff._1,2) + pow(ogPointDiff._2,2))
    val ogAngle = 180 * sign + toDegrees(atan(ogPointDiff._2/ogPointDiff._1))
    val newAngle = ogAngle - 90 - game.player.pos.getR
    val newPoint = (imgCenter._1 - cos(toRadians(newAngle)) * pointDiffDistance, imgCenter._2 - sin(toRadians(newAngle)) * pointDiffDistance)

    rotated.translateX = -newPoint._1 + Constants.width/2
    rotated.translateY = -newPoint._2 + Constants.height * 2 / 3


    //Scaalaa (hehe) auton

    val carScale: Double = game.track.pixelsPerMeter / (145.0 / 5)
    car.scaleX = carScale
    car.scaleY = carScale

    root.content = Array(rotated, car, steeringWhite, steeringRed, info, l1, l2, l3, l4)

  }

  val ticker = new Ticker(update)
  ticker.start()
}
