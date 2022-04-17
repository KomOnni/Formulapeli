package Constants

object Constants {
  // Pelille
  val tickRate = 100
  val width = 1600
  val height = 1000

  //sivuboxille

  val sideWidth = 300
  val sideHeight = height

  //Pelaajan input
  val mouseDeadzone = 50

  // Autolle
  val wheelBase: Double = 3.5

  val downforce: Double = 2.5
  val motor: Double = 400000 //W
  val drag: Double = 3.5
  val mass: Double = 500
  val brake: Double = 30000
  val tractionMultiplier = 1.5
  val maxSteeringAngle = 23 // F1 auotilla 17
}
