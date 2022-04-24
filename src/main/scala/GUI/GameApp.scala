package GUI

import scalafx.application.JFXApp
import scalafx.scene.{Node, Scene}
import Constants.Constants
import Controls.InputManager
import Game._
import scalafx.scene.control.Label
import scalafx.scene.image.{Image, ImageView}
import scalafx.scene.input.KeyCode
import scalafx.scene.paint.Color
import scalafx.scene.paint.Color.{DimGray, White}
import scalafx.scene.shape.Rectangle
import scala.collection.mutable.Buffer
import scala.math._
import scala.util.Sorting

object GUI extends JFXApp {

  val mainMenu = new MainMenu
  var game: Game = null

  val root = new Scene()

  stage = new JFXApp.PrimaryStage {
    title.value = "Autopeli"
    width = Constants.width
    height = Constants.height
    scene = root
  }

  def toGame(newGame: Game) = {
    game = newGame
  }

  //Takaväri
  root.setFill(Color.rgb(89,111,38))

  //Kontrollit
  Controls.InputManager.handleInput(root)

  //Boolean AI:n checkpointtien näkyvyydelle
  var checkpointsVisible = false

  //Päivitä metodi
  def update() = {
    if (game != null) {
    //Voi saada paikat aloituspaikkoja ja AI:n reittiä varten
      def p = game.followedCar.pos
      if (Controls.InputManager.keyPressNow.contains(KeyCode.Q)) {
        println(s"(${p.getX}, ${p.getY}, ${p.getR})")
      }

      //Vaihtaa kameraa seuraavaan pelaajaan
      if (Controls.InputManager.keyPressNow.contains(KeyCode.C)) {
        game.followedCarIndex += 1
        if (game.followedCarIndex == game.cars.size) game.followedCarIndex = 0
      }

      //Laittaa pelin pauselle
      if (Controls.InputManager.keyPressNow.contains(KeyCode.P)) {
        if (game.pause) {
          game.ticker.start()
        } else {
          game.ticker.stop()
        }
        game.pause = !game.pause
      }

      //Näyttää AI:n checkpointit
      if (Controls.InputManager.keyPressNow.contains(KeyCode.D)) {
        checkpointsVisible = !checkpointsVisible
      }

      //Tyhjentää tämän
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

      //Nopeus kmh
      val l1 = new Label(s"${(game.followedCar.speed * 3.6).toInt} km/h") {
        val pos = infoLabelPos(1)
        translateX = pos._1 + 25
        translateY = pos._2
        textFill = Color.White
        scaleX = 4
        scaleY = 4
      }

      //Viime sektori
      val l2 = new Label(s"Last sector: ${game.followedCar.lastSector.getOrElse("None")}") {
        val pos = infoLabelPos(2)
        translateX = pos._1
        translateY = pos._2
        textFill = Color.White
        scaleX = 2
        scaleY = 2
      }

      //Viime kierros
      val l3 = new Label(s"Last lap: ${game.followedCar.lastLap.getOrElse("None")}") {
        val pos = infoLabelPos(3)
        translateX = pos._1
        translateY = pos._2
        textFill = Color.White
        scaleX = 2
        scaleY = 2
      }

      //Paras kierros
      val l4 = new Label(s"Best lap: ${game.followedCar.bestLap.getOrElse("None")}") {
        val pos = infoLabelPos(4)
        translateX = pos._1
        translateY = pos._2
        textFill = Color.White
        scaleX = 2
        scaleY = 2
      }

      val amountOfSectors = game.track.amountOfSectors
      //Kisan positio
      val l5 = new Label(s"Pos: ${
        var a = game.cars.toArray
         if (game.cars.map(_.sectortimes.size).max < 3*amountOfSectors+1) {
           Sorting.quickSort(a)(Ordering[(Int, Long)].on(a => (a.sectortimes.size, a.sectortimes.sum)))
         } else {
           a = a.filter(_.sectortimes.size >= 3*amountOfSectors+1)
           Sorting.quickSort(a)(Ordering[(Long)].on(a => (a.sectortimes.take(3*amountOfSectors+1).sum)))
         }
        a.indexOf(game.followedCar) + 1
      }") {
        val pos = infoLabelPos(5)
        translateX = pos._1
        translateY = pos._2
        textFill = Color.White
        scaleX = 3
        scaleY = 3
      }

      val l6 = new Label(s"Lap ${(game.followedCar.sectortimes.size + amountOfSectors - 1) / amountOfSectors }/3") {
        val pos = infoLabelPos(6)
        translateX = pos._1
        translateY = pos._2
        textFill = Color.White
        scaleX = 3
        scaleY = 3
      }

      val flagImage = new Image("/pics/flag.png")
      val flag = new ImageView(flagImage) {
        x = Constants.width / 2.0 - flagImage.width.value / 2
        y = 20
        scaleY = 0.3
        scaleX = 0.3
      }


      val labels: Array[Node] = if (game.isInstanceOf[AIRaceTest] || game.isInstanceOf[Race]) {
        if ((game.followedCar.sectortimes.size + amountOfSectors - 1) / amountOfSectors >= 4) Array(l1,l2,l3,l4,l5,l6,flag) else Array(l1,l2,l3,l4,l5,l6)
      } else Array(l1,l2,l3,l4)

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
      val AInext = if (checkpointsVisible) game.cars.flatMap(car => car.drawAIRoute.getOrElse(Buffer[Node]())) else Buffer[Node]()


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

      val content = Array(map) ++ AInext.toArray ++ cars.toArray ++ Array(steeringWhite, steeringRed, info) ++ labels
      root.content = content
    } else root.content = mainMenu
  }

  val ticker: Ticker = new Ticker(update)
  ticker.start()
}
