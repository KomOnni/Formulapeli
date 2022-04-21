package Game

import scala.collection.mutable.Buffer
import scala.math.pow
import Constants.Constants

abstract class Game(val track: Track, val amountOfAI: Int, val playerStart: Int) {

  //rata ja aloituspaikat
  var startPositionsAndTaken:Vector[Pos] = track.raceStart

  var time: Long = 0

  val ticker = new Ticker(() => {time += 1})
  var pause = false
  ticker.start()


  //Seurattu auto ja sen aiheuuttama zoomaus
  var followedCarIndex = 0
  val cars = Buffer[Car]()

  def followedCar: Car = cars(followedCarIndex)
  def followedScale = Constants.zoomScale - followedCar.speed / (262/3.6) * (Constants.zoomScale - 1)

  //Autot

}
/*
  //metodit
  def addPlayer = ???
  def addAI = ???

  def start = ???

  def stopGame() = ???
  def pause() = ???
  def unpause() = ???
  def restart() = ???
  */


class TimeTrial(track: Track) extends Game(track, 0, 1) {
  startPositionsAndTaken = Vector(track.timeTrialStart)
  cars += new PlayerCar(this, startPositionsAndTaken.head)
}

//class Race(track: Game.Track, amountOfAI: Int, playerStart: Int, val amountOfLaps: Int) extends Game.Game(track, amountOfAI, playerStart) {}

class AITest(track: Track) extends Game(track, 1, -1) {
  val AICar = new AICar(this, startPositionsAndTaken.head)
  cars += AICar
}

class AIRaceTest(track: Track) extends Game(track, 1, -1) {
  cars += new AICar(this, startPositionsAndTaken(2))
  cars += new AICar(this, startPositionsAndTaken(3))
}