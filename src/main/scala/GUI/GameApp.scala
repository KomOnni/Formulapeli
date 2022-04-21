package GUI

import scalafx.application.JFXApp
import scalafx.scene.{Node, Scene}
import Constants.Constants
import Controls.InputManager
import Game._
import scalafx.scene.control.Label
import scalafx.scene.input.KeyCode
import scalafx.scene.paint.Color
import scalafx.scene.paint.Color.{Black, DarkGray, DimGray, White}
import scalafx.scene.shape.Rectangle

import scala.collection.mutable.Buffer
import scala.math._

object GUI extends JFXApp {

  var game: Game = new AIRaceTest(Game.Blackwood)
//  game = new TimeTrial(Game.Blackwood)

  val root = new Scene
  
  //Takaväri
  root.setFill(Color.rgb(89,111,38))
  
  //Kontrollit
  Controls.InputManager.handleInput(root)
  
  stage = new JFXApp.PrimaryStage {
    title.value = "Autopeli"
    width = Constants.width
    height = Constants.height
    scene = root
  }

  //Päivitä metodi
  def update() = {
    game.cars.foreach(_.update())

    //Voi saada paikat aloituspaikkoja ja AI:n reittiä varten
    def p = game.cars.head.pos
    if (Controls.InputManager.keyPressNow.contains(KeyCode.Q)) {
      println(s"(${p.getX}, ${p.getY}, ${p.getR})")
    }
    if (Controls.InputManager.keyPressNow.contains(KeyCode.C)) {
      game.followedCarIndex += 1
      if (game.followedCarIndex == game.cars.size) game.followedCarIndex = 0
    }

    if (Controls.InputManager.keyPressNow.contains(KeyCode.P)) {
      if (game.pause) {
        game.ticker.start()
        ticker.start()
      } else {
        game.ticker.stop()
        ticker.stop()
      }
      game.pause = !game.pause
    }

    InputManager.keyPressNow = scala.collection.mutable.Set[KeyCode]()

    //kartta ja autot
    val map: Node = game.track.image
    val cars = game.cars.map(_.draw)

    //Laatikko informaatiolle
    val info = Rectangle(50,200,200,Constants.height - 2 * 200)
    info.fill = DimGray
    info.opacity = 1

    //Informaatiolle labelit yms.

    def infoLabelPos(i: Int) = {
      (100,
      200 + 50 * i)
    }

    val l1 = new Label(s"${(game.followedCar.speed * 3.6).toInt} km/h") {
      val pos = infoLabelPos(1)
      translateX = pos._1 + 25
      translateY = pos._2
      textFill = Color.White
      scaleX = 4
      scaleY = 4
    }

    val l2 = new Label(s"Last sector: ${game.followedCar.lastSector.getOrElse("None")}") {
      val pos = infoLabelPos(2)
      translateX = pos._1
      translateY = pos._2
      textFill = Color.White
      scaleX = 2
      scaleY = 2
    }

    val l3 = new Label(s"Last lap: ${game.followedCar.lastLap.getOrElse("None")}") {
      val pos = infoLabelPos(3)
      translateX = pos._1
      translateY = pos._2
      textFill = Color.White
      scaleX = 2
      scaleY = 2
    }

    val l4 = new Label(s"Best lap: ${game.followedCar.bestLap.getOrElse("None")}") {
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
      x = Constants.width / 2 - 300 + (game.followedCar.steeringInput + Constants.maxSteeringAngle) * steeringWhite.width.value / 2 / Constants.maxSteeringAngle
      y = steeringWhite.y.value
      width = steeringWhite.height.value
      height = steeringWhite.height.value
      fill = Color.Red
    }

    //AI:n seuraavat checkpointit kartalla
    val AInext = game.cars.flatMap(car => car.drawAIRoute.getOrElse(Buffer[Node]()))


    //Seuraavan funktion avulla voi seurata autoa sen perspektiivistä.
    def followCar(car: Car) = {
        map.rotate = -90 - car.pos.getR

      val imgCenter = (game.track.img.width.value / 2.0, game.track.img.height.value / 2.0)
      val ogPoint = (car.pos.getX * game.track.pixelsPerMeter, car.pos.getY * game.track.pixelsPerMeter)
      val ogPointDiff = (ogPoint._1 - imgCenter._1, ogPoint._2 - imgCenter._2)
      val sign = if (ogPointDiff._1 < 0) 0 else 1
      val pointDiffDistance = sqrt(pow(ogPointDiff._1,2) + pow(ogPointDiff._2,2)) * game.followedScale
      val ogAngle = 180 * sign + toDegrees(atan(ogPointDiff._2/ogPointDiff._1))
      val newAngle = ogAngle - 90 - car.pos.getR
      val newPoint = (imgCenter._1 - cos(toRadians(newAngle)) * pointDiffDistance, imgCenter._2 - sin(toRadians(newAngle)) * pointDiffDistance)

        map.translateX = -newPoint._1 + Constants.width / 2
        map.translateY = -newPoint._2 + Constants.height * 2 / 3
    }

    //Seurataan game oliossa määriteltyä autoa
    followCar(game.followedCar)

    //Scaalaa (hehe) auton ja kartan
    val carScale: Double = game.track.pixelsPerMeter / (145.0 / 5)

    cars.foreach(car => {
        car.scaleX = carScale * game.followedScale
        car.scaleY = carScale * game.followedScale
    })

    map.scaleX = game.followedScale
    map.scaleY = game.followedScale

    val content = Array(map) ++ AInext.toArray ++ cars.toArray ++ Array(steeringWhite, steeringRed, info, l1, l2, l3, l4)
    root.content = content

  }

  val ticker: Ticker = new Ticker(update)
  ticker.start()
}
