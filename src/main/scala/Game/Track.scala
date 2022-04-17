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
  val raceStart: Vector[Pos]

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
  val raceStart = Vector(new Pos(100,100))
}