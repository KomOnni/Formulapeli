package Constants

object Constants {
  // Pelille
  val tickRate = 100
  val width = 1920
  val height = 1080

  //Pelaajan input
  val mouseDeadzone = 50

  // Autolle
  val wheelBase: Double = 3.5

  val downforce: Double = 3.3
  val motor: Double = 400000 //W
  val drag: Double = 3.5
  val mass: Double = 500
  val brake: Double = 30000
  val understeer = 0.6
  val understeerTimer = 30
  val tractionMultiplier = 1.3
  val maxSteeringAngle = 23 // F1 auotilla 17
}
