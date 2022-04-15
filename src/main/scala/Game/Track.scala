package Game

abstract class Track {
//  val image: Node

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
  val amountOfSectors = 0
  val timeTrialStart: Pos = new Pos(200,200)
  val raceStart = Vector(new Pos(200,200))
}