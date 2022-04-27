package Game

import scala.math._

class Pos(setX: Double, setY: Double, setRotation: Double = 0) {

  //paikat privatena, yksiköt metrejä (fysiikan vuoksi määritelty)
  private var x: Double = setX
  private var y: Double = setY
  private var rotation: Double = setRotation

  def getX = x
  def getY = y
  def getR = rotation

  //kääntäää
  def rotate(angle: Double) = rotation = rotation + angle

  //Pos:ien välinen etäisyys
  def difference(other: Pos) = sqrt(pow(other.getX - this.getX,2) + pow(other.getY - this.getY,2))

 //Positioneiden ero
  def differenceFromOtherXY(other: Pos) = (this.getX - other.getX, this.getY - other.getY, this.getR - other.getR)

  //Logiikka on väärä, mutta toimii oikein jossain, joten jää
  def angleBetween(other: Pos) = {
    val diff = other.differenceFromOtherXY(this)
    val m = if (diff._1 < 0) 1 else 0
    toDegrees(atan(-diff._2/diff._1)) + 180 * m + this.getR
  }

  //Oikeasti toimiva funktio
  def realAngleBetween(other: Pos) = {
    val diff = other.differenceFromOtherXY(this)
    val m = if (diff._2 < 0) 1 else 0
    toDegrees(atan(diff._1/diff._2)) + 180 * m + this.getR
  }

  //Tarkistaa onko joku muu (behind) tämän takana
  def isBehind(behind: Pos): Boolean = {
    val a = abs(this.angleBetween(behind)) % 360
    !(a < 90 || a > 270)
  }

  //Vaihtaa position johonkin muuhun
  def changeTo(nx: Double, ny: Double, nr: Double) = {
    x = nx
    y = ny
    rotation = nr
  }

  def makeNew: Pos = new Pos(getX,getY,getR)

  // Paikan muuttaminen steerin anglen avulla, vasemmalle annetaan neg.
  def add(steeringRadius: Double, speed: Double) = {
    if (steeringRadius == 0) {
      x = x + cos(toRadians(rotation)) * speed / Constants.Constants.tickRate
      y = y + sin(toRadians(rotation)) * speed / Constants.Constants.tickRate
    } else {
      //Algoritmi laskemaan uuden position ympyrän geometrian avulla
      val sign = if (steeringRadius > 0) 1 else -1
      val absSteeringRadius = abs(steeringRadius)
      val circleCX = getX + cos(toRadians(rotation + sign * 90)) * absSteeringRadius
      val circleCY = getY + sin(toRadians(rotation + sign * 90)) * absSteeringRadius
      val angleBetweenGoalAndCurrent = if (absSteeringRadius > 0 && absSteeringRadius < pow(10,10)) speed / Constants.Constants.tickRate / (2 * math.Pi * absSteeringRadius) * 360 else 0
      x = cos(toRadians(rotation - sign * 90) + toRadians(sign * angleBetweenGoalAndCurrent)) * absSteeringRadius + circleCX
      y = sin(toRadians(rotation - sign * 90) + toRadians(sign * angleBetweenGoalAndCurrent)) * absSteeringRadius + circleCY
      rotate(sign * angleBetweenGoalAndCurrent)
    }
  }

  override def toString: String = "(" + getX + ", " + getY + ", " + getR + ")"
}