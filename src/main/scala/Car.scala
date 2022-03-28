class Car(val game: Game, val pos: Pos) {

  val picOfCar: Pic
  var speed = 0

  // aikaan liittyvät
  val sectortimes = Vector[Int]
  var invalidLap = false
}