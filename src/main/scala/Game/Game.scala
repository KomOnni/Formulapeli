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
  cars += new PlayerCar(this, startPositionsAndTaken.head)
}

//AITimetrial
class AITest(track: Track) extends Game(track) {
  val AICar = new AICar(this, startPositionsAndTaken.head,3)
  cars += AICar
}

//AI:n välinen kisa
class AIRaceTest(track: Track) extends Game(track) {
  cars += new AICar(this, startPositionsAndTaken(2),2)
  cars += new AICar(this, startPositionsAndTaken(3),3)
}

//Kisa
class Race(track: Track) extends Game(track) {
  cars += new PlayerCar(this, startPositionsAndTaken(3))
  cars += new AICar(this, startPositionsAndTaken(2), 3)
}