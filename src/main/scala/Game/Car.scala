package Game

import scalafx.scene.image.{Image, ImageView}
import scalafx.scene.input.KeyCode

import scala.math._

sealed abstract class Car(val game: Game, val pos: Pos) {

  //val picOfCar: Pic
  var speed: Double = 0
  var understeerTimer = 0

  // aikaan liittyvät
  val sectortimes = Vector[Int]()
  var invalidLap = false

  //inputs
  var steeringInput: Double = 0
  var throttleInput: Double = 0
  var brakeInput: Double = 0

  def resetSpeed = speed = 0

  //apufunktiot
  def activateUndersteer = understeerTimer = Constants.Constants.understeerTimer

  def downforce = pow(speed,2) * Constants.Constants.downforce

  def throttle(input: Double): Double = {
    val futureSpeed = sqrt(( Constants.Constants.mass / 2 * pow(speed, 2) + (input * Constants.Constants.motor)) / ( Constants.Constants.mass / 2 ))
    val accerlation = (futureSpeed - speed)
    accerlation * Constants.Constants.mass
  }

  def drag = pow(speed,2) * Constants.Constants.drag

  def brake(input: Double): Double = {
    Constants.Constants.brake * input
  }

  def totalForces(brakePedal: Double, gasPedal: Double) = ( throttle(gasPedal) - drag - brake(brakePedal) )

  def maxTraction = if (understeerTimer == 0) (downforce + Constants.Constants.mass * 9.81) * Constants.Constants.tractionMultiplier else (downforce + Constants.Constants.mass * 9.81) * Constants.Constants.understeer * Constants.Constants.tractionMultiplier

  //Ajamisen funktio, vasemmalle neg. steeringanle, yritetty tehdy aliohjauksen kanssa mutta en vielä onnistunu. Nyt päätin että teen aluksi muut valmiiksi ennen fysiikkamoottorin kanssa leikkimistä
  def drive(steeringAngle: Double, brakePedal: Double, gasPedal: Double) = {
    val turningCircle = if (steeringAngle == 0) 0 else Constants.Constants.wheelBase/tan(toRadians(steeringAngle))

/*    val turningTraction = pow(speed, 2) / turningCircle * Constants.Constants.mass
    val noUndersteerThisTick = turningTraction <= maxTraction

    val realTurn = if (noUndersteerThisTick && understeerTimer == 0) {
      turningCircle
    } else if (!noUndersteerThisTick) {
      activateUndersteer
      pow(speed,2) / (maxTraction * Constants.Constants.understeer / Constants.Constants.mass)
    } else {
      understeerTimer -= 1
      val sign = if (turningCircle < 0) -1 else 1
      min(pow(speed,2) / (maxTraction * Constants.Constants.understeer / Constants.Constants.mass) * sign, sign * turningCircle) * sign
    }


    val availableTractionForSpeed = maxTraction - pow(speed,2) / realTurn * Constants.Constants.mass
    val totalF = totalForces(brakePedal, gasPedal)

    val realSpeedAdd = if (availableTractionForSpeed >= 0) {
      min(totalF, availableTractionForSpeed) / Constants.Constants.mass / Constants.Constants.tickRate
    } else {
      max(totalF, availableTractionForSpeed) / Constants.Constants.mass / Constants.Constants.tickRate
    }

 */

    //Näin päin, koska tämä on lähempänä matkan integraalia nopeuden suhteen
    pos.add(turningCircle, speed)
    speed = max(0, speed + totalForces(brakePedal, gasPedal) / Constants.Constants.mass / Constants.Constants.tickRate)
  }

  def update() = {
    updateInputs
    drive(steeringInput, brakeInput, throttleInput)
  }

  def updateInputs: Unit

  def draw() = {
    new ImageView(new Image("pics/Ferrari.png")) {
      x = pos.getX
      y = pos.getY
      rotate = -90 + pos.getR

    }
  }
}




class PlayerCar(game: Game,pos: Pos) extends Car(game,pos) {
  override def updateInputs: Unit = {
    steeringInput = Controls.InputManager.mouseX * Constants.Constants.maxSteeringAngle * 2 / Constants.Constants.width - Constants.Constants.maxSteeringAngle
    throttleInput = if (Controls.InputManager.keysPressed.contains(KeyCode.W)) 1 else 0
    brakeInput = if (Controls.InputManager.keysPressed.contains(KeyCode.S)) 1 else 0
  }
}