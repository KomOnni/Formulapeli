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
  val cars = Buffer[Car]()
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
  cars += new PlayerCar(this, startPositionsAndTaken.head._1)
  followedCar = cars.head
}

//class Race(track: Game.Track, amountOfAI: Int, playerStart: Int, val amountOfLaps: Int) extends Game.Game(track, amountOfAI, playerStart) {}

class AITest(track: Track) extends Game(track, 1, -1) {
  val AICar = new AICar(this, startPositionsAndTaken.head._1)
  cars += AICar
  followedCar = AICar
}