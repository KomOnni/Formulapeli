package Game

import scala.collection.mutable.Buffer
import Constants.Constants

abstract class Game(val track: Track) {

  //rata ja aloituspaikat
  var startPositionsAndTaken:Vector[Pos] = track.raceStart

  //kello
  var time: Long = 0
  var pause = false

  val ticker = new Ticker(() => {
    time += 1
    cars.foreach(_.update())
  })
  ticker.start()

  val cars = Buffer[Car]()

  //Seurattu auto ja sen aiheuuttama zoomaus
  var followedCarIndex = 0
  def followedCar: Car = cars(followedCarIndex)
  def followedScale = Constants.zoomScale - followedCar.speed / (262/3.6) * (Constants.zoomScale - 1)
}

//Timetrial
class TimeTrial(track: Track) extends Game(track) {
  startPositionsAndTaken = Vector(track.timeTrialStart)
  def addP(pos: Pos) = new PlayerCar(this, pos)
  cars += addP(startPositionsAndTaken(0).makeNew)
}

//AITimetrial
class AITest(track: Track) extends Game(track) {
  def add(pos: Pos, int: Int) = new AICar(this, pos, int)
  cars += add(startPositionsAndTaken(0).makeNew, 3)
}

//AI:n välinen kisa
class AIRaceTest(track: Track) extends Game(track) {
  def add(pos: Pos, int: Int) = new AICar(this, pos, int)
  cars += add(startPositionsAndTaken(0).makeNew, 2)
  cars += add(startPositionsAndTaken(1).makeNew, 3)
}

//Kisa
class Race(track: Track) extends Game(track) {
  def add(pos: Pos, int: Int) = new AICar(this, pos, int)
  def addP(pos: Pos) = new PlayerCar(this, pos)
  cars += addP(startPositionsAndTaken(1).makeNew)
  cars += add(startPositionsAndTaken(0).makeNew, 3)
}