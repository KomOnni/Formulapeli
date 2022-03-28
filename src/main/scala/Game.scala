abstract class Game(val track: Track, val amountOfAI: Int, val playerStart: Int) {

  //rata ja aloituspaikat
  val startPositionsAndTaken:Vector[(Pos, Boolean)] = track.raceStart.take(amountOfAI + 1).map((_, false))

  //Autot
  val player: PlayerCar
  val carsOfAI = Vector[AICar]()

  //metodit
  def addPlayer = ???
  def addAI = ???

  def start = ???

  def stopGame() = ???
  def pause() = ???
  def unpause() = ???
  def restart() = ???
  def teleportPlayerToLastSector = ???
}

class TimeTrial(track: Track) extends Game(track, 0, 1) {
  override val startPositionsAndTaken: Vector[(Pos, Boolean)] = Vector(track.timeTrialStart, false)
}

class Race(track: Track, amountOfAI: Int, playerStart: Int, val amountOfLaps: Int) extends Game(track, amountOfAI, playerStart) {

}