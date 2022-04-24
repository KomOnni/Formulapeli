package Constants

object Constants {
  // Pelille
  val tickRate = 100
  val width = 1920
  val height = 1040

  //AI:n alt
  val altDefault = 3

  //Muuten GUI:lle
  val zoomScale = 1.8

  //Pelaajan input
  val mouseDeadzone = 20

  // Autolle
  val wheelBase: Double = 3.5
  val slipstream: Double = 0.87676
  val downforce: Double = 2.5
  val motor: Double = 400000 //W
  val drag: Double = 3.5
  val mass: Double = 500
  val brake: Double = 30000
  val tractionMultiplier = 1.5
  val maxSteeringAngle = 23 // F1 auotilla 17
}
