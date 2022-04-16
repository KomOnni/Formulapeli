package Game

import scalafx.scene.image.{Image, ImageView}
import scalafx.scene.input.KeyCode

import scala.math._

sealed abstract class Car(val game: Game, val pos: Pos) {

  //val picOfCar: Pic
  var speed: Double = 0

  // aikaan liittyvät
  val sectortimes = Vector[Int]()
  var invalidLap = false

  //inputs
  var steeringInput: Double = 0
  var throttleInput: Double = 0
  var brakeInput: Double = 0

  def resetSpeed = speed = 0

  //apufunktiot
  def downforce = pow(speed,2) * Constants.Constants.downforce

  def throttle(input: Double): Double = {
    val futureSpeed = sqrt(( Constants.Constants.mass / 2 * pow(speed, 2) + (input * Constants.Constants.motor)) / ( Constants.Constants.mass / 2 ))
    val accerlation = (futureSpeed - speed)
    accerlation * Constants.Constants.mass
  }

  def drag = pow(speed,1.7) * Constants.Constants.drag

  def brake(input: Double): Double = {
    Constants.Constants.brake * input
  }

  def totalForces(brakePedal: Double, gasPedal: Double) = ( throttle(gasPedal) - drag - brake(brakePedal) )

  def maxTraction = (downforce + Constants.Constants.mass * 9.81) * Constants.Constants.tractionMultiplier
// * Constants.Constants.understeer
  //Ajamisen funktio, vasemmalle neg. steeringanle, yritetty tehdy aliohjauksen kanssa mutta en vielä onnistunu. Nyt päätin että teen aluksi muut valmiiksi ennen fysiikkamoottorin kanssa leikkimistä
  def drive(steeringAngle: Double, brakePedal: Double, gasPedal: Double) = {
    val turningCircle = if (steeringAngle == 0) 0 else Constants.Constants.wheelBase/tan(toRadians(steeringAngle))

    val sign = if (turningCircle < 0) -1 else 1
    val absturningCircle = abs(turningCircle)

    val turningTraction = pow(speed, 2) / absturningCircle * Constants.Constants.mass
    val noUndersteerThisTick = turningTraction <= maxTraction

    val realTurn = if (noUndersteerThisTick) {
      turningCircle
    } else if (steeringAngle == 0){
      0
    } else {
      (pow(speed,2) / (maxTraction / Constants.Constants.mass)) * sign
    }

    val usedTractionInTurning = if (turningCircle != 0) pow(speed,2) / abs(realTurn) * Constants.Constants.mass else 0
    val availableTractionForSpeed = maxTraction - usedTractionInTurning
    val totalF = totalForces(brakePedal, gasPedal)

    val realSpeedAdd = if (availableTractionForSpeed >= 0) {
      min(totalF, availableTractionForSpeed) / Constants.Constants.mass / Constants.Constants.tickRate
    } else {
      0
    }

    //Näin päin, koska tämä on lähempänä matkan integraalia nopeuden suhteen
    pos.add(realTurn, speed)
    speed = max(0, speed + realSpeedAdd)//totalForces(brakePedal, gasPedal) / Constants.Constants.mass / Constants.Constants.tickRate)
  }

  def update() = {
    updateInputs
    drive(steeringInput, brakeInput, throttleInput)
  }

  def updateInputs: Unit

  def draw() = {
    new ImageView(new Image("pics/Ferrari.png")) {
      x = Constants.Constants.width / 2
      y = Constants.Constants.height / 2
      rotate = -90 + pos.getR

    }
  }
}




class PlayerCar(game: Game,pos: Pos) extends Car(game,pos) {

  def mouseXtoSteeringInput: Double = {
    val x = Controls.InputManager.mouseX
    val sign = if (x - Constants.Constants.width / 2 < 0) -1 else 1
    val absFromCenter = abs(x - Constants.Constants.width / 2)
    val r = if (absFromCenter <= Constants.Constants.mouseDeadzone) 0 else {
      (absFromCenter - Constants.Constants.mouseDeadzone) / (Constants.Constants.width / 2 - Constants.Constants.mouseDeadzone) * Constants.Constants.maxSteeringAngle * sign
    }
    r
  }

  override def updateInputs: Unit = {
    steeringInput = mouseXtoSteeringInput
    throttleInput = if (Controls.InputManager.keysPressed.contains(KeyCode.W)) 1 else 0
    brakeInput = if (Controls.InputManager.keysPressed.contains(KeyCode.S)) 1 else 0
  }
}