package Game

import scalafx.scene.Node
import scalafx.scene.image.{Image, ImageView}


abstract class Track {
  val image: Node
  val width: Int
  val height: Int

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
  val image = new ImageView(new Image("/pics/testTrack2.png"))
  val width = 5504
  val height = 6032
  val amountOfSectors = 0
  val timeTrialStart: Pos = new Pos(100,100)
  val raceStart = Vector(new Pos(100,100))
}