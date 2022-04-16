package Game

import scalafx.scene.Node
import scalafx.scene.image.{Image, ImageView}


abstract class Track {
  val image: Node
  val pixelsPerMeter: Double


  //Aloituspaikat
  val timeTrialStart: Pos
  val raceStart: Vector[Pos]

  //Sektorit/maali
  val amountOfSectors: Int
//  val sectorColors: Vector[Color]

  //AI
//  val routeOfAIandAlt: Vector[(Game.Pos, Boolean)]
}

object TestTrack extends Track {
  val image = new ImageView(new Image("/pics/blackwoodPix3C3G.png"))
  val pixelsPerMeter = 10
  val amountOfSectors = 0
  val timeTrialStart: Pos = new Pos(57.73648439916815, 812.592310281429, -16.874563631786398)
  val raceStart = Vector(new Pos(100,100))
}