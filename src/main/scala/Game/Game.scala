package Game

import GUI.Ticker

import scala.collection.mutable.Buffer

abstract class Game(val track: Track, val amountOfAI: Int, val playerStart: Int) {

  //rata ja aloituspaikat
  var startPositionsAndTaken:Vector[(Pos, Boolean)] = track.raceStart.take(amountOfAI + 1).map((_, false))

  var time: Long = 0
  val ticker = new Ticker(() => {time += 1})
  ticker.start()

  var followedCar: Car = null

  //Autot
  val player: PlayerCar
//  val carsOfAI = Buffer[AICar]()
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
}


class TimeTrial(track: Track) extends Game(track, 0, 1) {
  startPositionsAndTaken = Vector((track.timeTrialStart, false))
  val player = new PlayerCar(this, startPositionsAndTaken.head._1)
  followedCar = player
}

//class Race(track: Game.Track, amountOfAI: Int, playerStart: Int, val amountOfLaps: Int) extends Game.Game(track, amountOfAI, playerStart) {}