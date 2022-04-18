package Game

import scalafx.scene.Node
import scalafx.scene.image.{Image, ImageView}
import scalafx.scene.paint.Color


abstract class Track {
  val img: Image
  val image: Node
  val pixelsPerMeter: Double


  //Aloituspaikat
  val timeTrialStart: Pos
  var raceStart: Vector[Pos]

  //Sektorit/maali
  val amountOfSectors = 3
  val sectorColors: Vector[Color]

  //AI
//  val routeOfAIandAlt: Vector[(Game.Pos, Boolean)]
}

object Blackwood extends Track {
  val img = new Image("/pics/blackwoodPix3C4G1.png")
  val image = new ImageView(img)
  val pixelsPerMeter = 10
  val sectorColors = Vector(Color.rgb(255,255,255), Color.rgb(255,255,252), Color.rgb(255,255,250))
  val timeTrialStart: Pos = new Pos(57.73648439916815, 822.592310281429, -16.874563631786398)
  var raceStart = Vector(new Pos(221.8147254188017, 447.6946940014501, -69.8460646983609), new Pos(222.18152358387715, 448.08202239176103, -69.94425581914885), new Pos(218.9341218587001, 458.31280390639495, -73.26581378993792), new Pos(223.76053827537982, 464.71557057832524, -71.32384157884067))
}