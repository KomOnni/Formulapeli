package Game

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

  // Paikan muuttaminen steerin anglen avulla, vasemmalle annetaan neg.
  def add(steeringRadius: Double, speed: Double) = {
    if (steeringRadius == 0) {
      x = x + cos(toRadians(rotation)) * speed / Constants.Constants.tickRate
      y = y + sin(toRadians(rotation)) * speed / Constants.Constants.tickRate
    } else {
      //Algoritmi laskemaan uuden position kun auto kääntyy tietyn verran, dokumentaatioon tulee kuva algoritmin toiminnasta
      val sign = if (steeringRadius > 0) 1 else -1
      val absSteeringRadius = abs(steeringRadius)
      val circleCX = getX + cos(toRadians(rotation + sign * 90)) * absSteeringRadius
      val circleCY = getY + sin(toRadians(rotation + sign * 90)) * absSteeringRadius
      val angleBetweenGoalAndCurrent = speed / Constants.Constants.tickRate / (2 * math.Pi * absSteeringRadius) * 360
      x = cos(toRadians(rotation - sign * 90) + toRadians(sign * angleBetweenGoalAndCurrent)) * absSteeringRadius + circleCX
      y = sin(toRadians(rotation - sign * 90) + toRadians(sign * angleBetweenGoalAndCurrent)) * absSteeringRadius + circleCY
      rotate(sign * angleBetweenGoalAndCurrent)
    }
  }

  override def toString: String = "(" + getX + ", " + getY + ", " + getR + ")"
}