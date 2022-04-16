package Constants

object Constants {
  // Pelille
  val tickRate = 100
  val width = 1920
  val height = 1080

  //Kartalle

  val mapPixelsPerMeter = 15
  val mapScale = 1
  val carScale = 0.75

  // Autolle
  val wheelBase: Double = 3.5

  // Ratkaistu niin, että downforce on 4x painovoima 72 m/s vauhdissa, joka on n. maksimi
  val downforce: Double = 1.33
  val motor: Double = 400000 //W
  val drag: Double = 3.5
  val mass: Double = 500
  val brake: Double = 30000
  val understeer = 0.6
  val understeerTimer = 30
  val tractionMultiplier = 3
  val maxSteeringAngle = 23 // F1 auotilla 17
}
