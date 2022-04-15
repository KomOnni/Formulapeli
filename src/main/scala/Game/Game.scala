package Game

abstract class Game(val track: Track, val amountOfAI: Int, val playerStart: Int) {

  //rata ja aloituspaikat
  val startPositionsAndTaken:Vector[(Pos, Boolean)] = track.raceStart.take(amountOfAI + 1).map((_, false))



  //Autot
  val player: PlayerCar
//  val carsOfAI = Vector[AICar]()
/*
  //metodit
  def addPlayer = ???
  def addAI = ???

  def start = ???

  def stopGame() = ???
  def pause() = ???
  def unpause() = ???
  def restart() = ???
  def teleportPlayerToLastSector = ???
  */
}


class TimeTrial(track: Track) extends Game(track, 0, 1) {
  override val startPositionsAndTaken: Vector[(Pos, Boolean)] = Vector((track.timeTrialStart, false))
  val player = new PlayerCar(this, startPositionsAndTaken.head._1)
}

//class Race(track: Game.Track, amountOfAI: Int, playerStart: Int, val amountOfLaps: Int) extends Game.Game(track, amountOfAI, playerStart) {}