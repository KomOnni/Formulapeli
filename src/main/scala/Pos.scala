import scala.math._

class Pos(setX: Double, setY: Double, setRotation: Double = 0) {
  //paikat privatena, yksiköt merejä (fysiikan vuoksi määritelty)
  private var x: Double = setX
  private var y: Double = setY
  private var rotation: Double = setRotation

  def getX = x
  def getY = y
  def getR = rotation

  def rotate(angle: Double) = rotation = rotation + angle

  // Paikan muuttaminen steerin anglen avulla
  def add(steeringRadius: Double, right: Boolean, speed: Double) = {
    if (steeringRadius == 0) {
      x = x + cos(toRadians(rotation)) * speed / Constants.tickRate
      y = y + sin(toRadians(rotation)) * speed / Constants.tickRate
    } else {
      //Algoritmi laskemaan uuden position kun auto kääntyy tietyn verran, dokumentaatioon tulee kuva algoritmin toiminnasta
      val sign = if (right) -1 else 1
      val circleCX = getX + cos(toRadians(rotation + sign * 90)) * steeringRadius
      val circleCY = getY + sin(toRadians(rotation + sign * 90)) * steeringRadius
      val angleBetweenGoalAndCurrent = speed / Constants.tickRate / (2 * math.Pi * steeringRadius) * 360
      x = cos(toRadians(rotation)) * steeringRadius + circleCX
      y = sin(toRadians(rotation)) * steeringRadius + circleCY
      rotate(sign * angleBetweenGoalAndCurrent)
    }
  }
}