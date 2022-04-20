package Game

import Constants.Constants
import scalafx.scene.paint.Color.Red
import scalafx.scene.shape.Rectangle

import scala.collection.mutable.Buffer
import scala.math._

class AICar(game: Game, pos: Pos) extends Car(game,pos) {

  var nextCheckpointIndex: Int = 0

  var miniCheckpoints: Buffer[(Pos, Int)] = Buffer()
  var brakePointOn = false
  var miniCheckpointIndex: Int = 0

  var nextTurningRadius: Double = 0

  var alt = false
  var routeCalculated = false


  //Tarkistetaan ajamisfunktion jälkeen
  def checkCheckpoint() = {
    if (pos.isBehind(miniCheckpoints(miniCheckpointIndex)._1)) {
      miniCheckpointIndex += 1
      if (miniCheckpointIndex >= miniCheckpoints.size) {
        miniCheckpointIndex = 0
        nextCheckpointIndex += 1
        routeCalculated = false
        if (nextCheckpointIndex >= game.track.routeAndAlt.size) {
          nextCheckpointIndex = 0
          pos.rotate(-360)
        }
      }
    }
  }

  def turningCircle(one: Pos, two: Pos): Double = {
    val aone: (Double, Double) = (-1 / tan(toRadians(-one.getR)), -one.getY - (-1) / tan(toRadians(-one.getR)) * one.getX)
    val atwo: (Double, Double) = (-1 / tan(toRadians(-two.getR)), -two.getY - (-1) / tan(toRadians(-two.getR)) * two.getX)

    val sign = if (one.getR < two.getR) 1 else -1
    val intersectionPoint = new Pos((atwo._2 - aone._2) / (aone._1 - atwo._1), -(aone._1 * (atwo._2 - aone._2) / (aone._1 - atwo._1) + aone._2))

    intersectionPoint.difference(one) * sign
  }

  //Lasketaan kääntymispiste suorien analyysin avulla
  def turningPointForLate(targetPos: Pos): Pos = {
    val car = (tan(toRadians(-pos.getR)), -pos.getY - tan(toRadians(-pos.getR)) * pos.getX)
    val target = (tan(toRadians(-targetPos.getR)), -targetPos.getY - tan(toRadians(-targetPos.getR)) * targetPos.getX)

    val intersectionPoint = new Pos((target._2 - car._2) / (car._1 - target._1), -(car._1 * (target._2 - car._2) / (car._1 - target._1) + car._2))
    val d = intersectionPoint.difference(targetPos)

    new Pos(intersectionPoint.getX + cos(toRadians(pos.getR - 180)) * d, intersectionPoint.getY + sin(toRadians(pos.getR - 180)) * d, pos.getR)
  }

  //eri samoilla kaavoilla
  def stopTurningPointForEarly(targetPos: Pos): Pos = {
    val car = (tan(toRadians(-pos.getR)), -pos.getY - tan(toRadians(-pos.getR)) * pos.getX)
    val target = (tan(toRadians(-targetPos.getR)), -targetPos.getY - tan(toRadians(-targetPos.getR)) * targetPos.getX)

    val intersectionPoint = new Pos((target._2 - car._2) / (car._1 - target._1), -(car._1 * (target._2 - car._2) / (car._1 - target._1) + car._2))
    val d = intersectionPoint.difference(pos)

    new Pos(intersectionPoint.getX + cos(toRadians(targetPos.getR)) * d, intersectionPoint.getY + sin(toRadians(targetPos.getR)) * d, targetPos.getR)
  }

  def brakingPointForLate(turningPoint: Pos, turningRadius: Double): Pos = {
    if (true) {
      val absturningRadius = abs(turningRadius)
      val absInside = abs((9.81 * Constants.mass * absturningRadius * Constants.tractionMultiplier) / (Constants.downforce * absturningRadius * Constants.tractionMultiplier - Constants.mass) )
      val neededSpeed = sqrt(absInside)
      //println("max speed for turn: " + neededSpeed)
      val deaccerlation = Constants.brake / Constants.mass
      val time = if (neededSpeed < speed) (speed - neededSpeed) / deaccerlation else 0
      //println(time)
      //println(1.0 / 2 * deaccerlation * sin(toRadians(pos.getR - 180)) * pow(time,2))
      new Pos(
        turningPoint.getX + speed * cos(toRadians(pos.getR - 180)) * time - 1.0 / 2 * deaccerlation * cos(toRadians(pos.getR - 180)) * pow(time,2),
        turningPoint.getY + speed * sin(toRadians(pos.getR - 180)) * time - 1.0 / 2 * deaccerlation * sin(toRadians(pos.getR - 180)) * pow(time,2),
        turningPoint.getR
      )
    } else turningPoint
  }

  def recalculateBrakePoint() = {
    if (game.track.routeAndAlt(nextCheckpointIndex)._2 == 1 && miniCheckpointIndex == 0) {
      miniCheckpoints(0) = (brakingPointForLate(miniCheckpoints(1)._1, nextTurningRadius), 1)
    }
  }

  def calculateMiniCheckpoints() = {
    if (!routeCalculated) {
      game.track.routeAndAlt(nextCheckpointIndex)._2 match {
        case 1 => {
          val turningPoint = turningPointForLate(game.track.routeAndAlt(nextCheckpointIndex)._1)
          println(turningPoint.difference(game.track.routeAndAlt(nextCheckpointIndex)._1))
          val turningRadius = turningCircle(turningPoint, game.track.routeAndAlt(nextCheckpointIndex)._1)
          val brakePoint = brakingPointForLate(turningPoint, turningRadius)

          nextTurningRadius = turningRadius

          println(brakePoint.difference(turningPoint))

          miniCheckpoints = Buffer()
          miniCheckpoints += ((brakePoint, 1))
          miniCheckpoints += ((turningPoint, 3))
          miniCheckpoints += ((game.track.routeAndAlt(nextCheckpointIndex)._1, 2))
        }
        case 2 => {
          val stopTurningPoint = stopTurningPointForEarly(game.track.routeAndAlt(nextCheckpointIndex)._1)
          val turningRadius = turningCircle(pos, stopTurningPoint)

          nextTurningRadius = turningRadius
          println(nextTurningRadius)

          miniCheckpoints = Buffer()
          miniCheckpoints += ((stopTurningPoint, 2))
          miniCheckpoints += ((game.track.routeAndAlt(nextCheckpointIndex)._1, 1))
        }
      }
    routeCalculated = true
    }
  }

  def calculateInputs: (Double, Double, Double) = {
    val inputs: (Double, Double, Double) = miniCheckpoints(miniCheckpointIndex)._2 match {
      case 1 => (0,0,1)
      case 2 => {
        val steeringAngle = toDegrees(atan(Constants.wheelBase / nextTurningRadius))
        if (steeringAngle > Constants.maxSteeringAngle) println("WARNING: AI STEERING ANGLE")
        (steeringAngle,0,1)
      }
      case 3 => (0,1,0)
    }
    inputs
  }

  def updateInputs() = {
    val inputs = calculateInputs
    steeringInput = inputs._1
    brakeInput = inputs._2
    throttleInput = inputs._3
  }

  def drawAIRoute = {
    Some(miniCheckpoints.map(a => new Rectangle{
      height = 10
      width = 10
      fill = Red
      val d = game.followedCar.pos.angleBetween(a._1)
      val c = a._1.differenceFromOtherXY(game.followedCar.pos)
      val b = a._1.difference(game.followedCar.pos)
      x = Constants.width / 2 + cos(toRadians(d + 90)) * b * game.track.pixelsPerMeter - 5
      y = Constants.height / 3 * 2 - sin(toRadians(d+ 90)) * b * game.track.pixelsPerMeter - 5
    }))
  }

  def update() = {
    calculateMiniCheckpoints()
    checkCheckpoint()
    updateInputs()
    drive(steeringInput, brakeInput, throttleInput)
    recalculateBrakePoint()
    draw
    drawAIRoute
    savePos(pos)
    tpIfGrass(checkForGrassAndSectors(wheelPlaces(this)))
//    println(pos.getR)
//    println(miniCheckpoints)
//    println(pos)
    println(miniCheckpointIndex)
    println(nextCheckpointIndex)
  }
}
