package Game

import Constants.Constants
import scalafx.scene.paint.Color.{Blue, Red}
import scalafx.scene.shape.Rectangle

import scala.collection.mutable.Buffer
import scala.math._

class AICar(game: Game, pos: Pos, livery: Int) extends Car(game,pos,livery) {

  //Checkpointteja
  var nextCheckpointIndex: Int = 0
  var miniCheckpoints: Buffer[(Pos, Int)] = Buffer()
  var miniCheckpointIndex: Int = 0

  //Seuraavien käännöksen arvoja
  var nextTurningRadius: Double = 0
  var nextTurnMaxSpeed: Double = 0

  //AI:n booleaneja
  var failsafeBrake = false
  var alt = false
  var routeCalculated = false


  //Tarkistetaan ajamisfunktion jälkeen
  def checkCheckpoint() = {
    failsafeBrake = false
    //Bugin korjaus, jotta ensimmäisellä tickillä ei tule OutOfBoundsError, pitää kuitenkin olla updatessa ekana failsafen takia
    if (miniCheckpoints.nonEmpty) {
      if (pos.isBehind(miniCheckpoints(miniCheckpointIndex)._1)) {
        miniCheckpointIndex += 1
        if (miniCheckpointIndex >= miniCheckpoints.size) {
          miniCheckpointIndex = 0
          alt = false
          nextCheckpointIndex += 1
          routeCalculated = false
          if (nextCheckpointIndex >= game.track.routeAndAlt.size) {
            nextCheckpointIndex = 0
            pos.rotate(-360)
          }
        }
      }
    }
  }

  //Laskee kääntymissäteen, kun kääntymis ja päättymispiste on tiedossa
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

  //Laskee jarrutuspisteen checkpoint tyyypille 1 käyttäen fysiikan kaavoja v = v_0 + at ja x = x_0 + v_0*t + 1/2 * a * t^2
  def brakingPointForLate(turningPoint: Pos, turningRadius: Double): Pos = {
    val absturningRadius = abs(turningRadius)
    val absInside = abs((9.81 * Constants.mass * absturningRadius * Constants.tractionMultiplier) / (Constants.downforce * absturningRadius * Constants.tractionMultiplier - Constants.mass) )
    val neededSpeed = sqrt(absInside)
    nextTurnMaxSpeed = neededSpeed
    val deaccerlation = Constants.brake / Constants.mass
    val time = if (neededSpeed < speed) (speed - neededSpeed) / deaccerlation else 0
    new Pos(
      turningPoint.getX + (speed + 1) * cos(toRadians(pos.getR - 180)) * time - 1.0 / 2 * deaccerlation * cos(toRadians(pos.getR - 180)) * pow(time,2),
      turningPoint.getY + (speed + 1) * sin(toRadians(pos.getR - 180)) * time - 1.0 / 2 * deaccerlation * sin(toRadians(pos.getR - 180)) * pow(time,2),
      turningPoint.getR
    )
  }

  //Laskee jarrutuspistettä uudestaan kokoajan, koska nopeuden kasvaessa jarrutuspiste pitenee
  def recalculateBrakePoint() = {
    if (game.track.routeAndAlt(nextCheckpointIndex)._2 == 1 && miniCheckpointIndex == 0) {
      miniCheckpoints(0) = (brakingPointForLate(miniCheckpoints(1)._1, nextTurningRadius), 1)
    }
  }

  //Laskee pelaajan laittamasta checkpointista tarkemmat ohjeet AI:lle
  def calculateMiniCheckpoints() = {
    if (!routeCalculated) {
      val checkpoint = game.track.routeAndAlt(nextCheckpointIndex)
      val sign = if (checkpoint._3) 1 else -1
      val nextCheckpoint = if (alt) {
        new Pos(checkpoint._1.getX + cos(toRadians(checkpoint._1.getR + 90)) * sign * checkpoint._4 * Constants.altDefault, checkpoint._1.getY + sin(toRadians(checkpoint._1.getR + 90)) * sign * checkpoint._4 * Constants.altDefault, checkpoint._1.getR)
      } else checkpoint._1
      val what = {
        if (game.track.routeAndAlt(nextCheckpointIndex)._2 == 3) 3 else {
          val turningPoint = turningPointForLate(nextCheckpoint)
          if (pos.isBehind(turningPoint)) 2 else 1
        }
      }
      game.track.routeAndAlt(nextCheckpointIndex)._2 match {
        case 1 => {
          val turningPoint = turningPointForLate(nextCheckpoint)
          val turningRadius = turningCircle(turningPoint, nextCheckpoint)
          val brakePoint = brakingPointForLate(turningPoint, turningRadius)

          nextTurningRadius = turningRadius

          miniCheckpoints = Buffer()
          if (miniCheckpointIndex == 0) miniCheckpoints += ((brakePoint, 1)) else miniCheckpoints += ((pos, 3))
          if (miniCheckpointIndex <= 1) miniCheckpoints += ((turningPoint, 3)) else miniCheckpoints += ((pos, 3))
          miniCheckpoints += ((nextCheckpoint, 2))
        }
        case 2 => {
          val stopTurningPoint = stopTurningPointForEarly(nextCheckpoint)
          val turningRadius = turningCircle(pos, stopTurningPoint)

          //failsafea varten
          val absturningRadius = abs(turningRadius)
          val absInside = abs((9.81 * Constants.mass * absturningRadius * Constants.tractionMultiplier) / (Constants.downforce * absturningRadius * Constants.tractionMultiplier - Constants.mass) )
          val maxSpeedForTurn = sqrt(absInside)
          if (maxSpeedForTurn < speed * 0.9 && miniCheckpoints(miniCheckpointIndex)._2 != 2) {
            failsafeBrake = true
          }

          nextTurningRadius = turningRadius

          miniCheckpoints = Buffer()
          if (miniCheckpointIndex == 0) miniCheckpoints += ((stopTurningPoint, 2)) else miniCheckpoints += ((pos, 3))
          miniCheckpoints += ((nextCheckpoint, 1))
        }
        case 3 => {
          miniCheckpoints = Buffer()
          miniCheckpoints += ((nextCheckpoint, 1))
        }
      }
    if (!failsafeBrake) routeCalculated = true
    }
  }

  //Tarkistaa muiden sijaintia, jotta antaa tilaa tarvittaessa
  def checkForOthers() = {
    def newPosOnSide(d: Double) = new Pos(pos.getX + cos(toRadians(pos.getR + 90)) * d, pos.getY + sin(toRadians(pos.getR + 90)) * d)
    def newPosHeading(d: Double) = new Pos(pos.getX + cos(toRadians(pos.getR)) * d, pos.getY + sin(toRadians(pos.getR)) * d)
    val otherPlayers = game.cars.filterNot(_ == this)
    if (otherPlayers.nonEmpty) {
      //Katsoo sivulta viiden kolmen ja yhden metrin päästä kummaltakin puolilta ja edestä neljän metrin päästä ja ottaa toista autoa lähimpänä olevan position.
      val least = (Vector(-5,-3,-1,1,3,5).map(a => newPosOnSide(a)) ++ Vector(4).map(a => newPosHeading(a))).map(a => otherPlayers.map(_.pos.difference(a)).min).zipWithIndex.minBy(_._1)
      //Jos lähin on alle 3.5m päässä, AI varmistaa, ettei se kiilaa sitä radalta ulos tai jos edessä, AI lähtee sivulle ohittamaan
      if (least._1 < 3.5 && miniCheckpoints.nonEmpty) {
        val altSide = game.track.routeAndAlt(nextCheckpointIndex)._3
        if (least._2 <= 2) {
        val altvalue = alt
          if (altSide) {
            alt = true
            if (!altvalue) routeCalculated = false
          } else {
            alt = false
            if (altvalue) routeCalculated = false
          }
        }
        else if (least._2 >= 3 && least._2 <=5) {
          val altvalue = alt
          if (!altSide) {
            alt = true
            if (!altvalue) routeCalculated = false
          } else {
            alt = false
            if (altvalue) routeCalculated = false
          }
        } else if (least._2 == 6) {
          val closestCar = game.cars.filterNot(this == _).minBy(_.pos.difference(pos))
          val speedDifference = speed - closestCar.speed
          val closestCarNoAlt = closestCar match {
            case (ai: AICar) => !alt
            case _ => true
          }
          if ((speedDifference > 0.5 || least._1 < 2) && closestCarNoAlt) {
            val altvalue = alt
            alt = true
            if (!altvalue && miniCheckpoints(miniCheckpointIndex)._2 != 2) routeCalculated = false
          }
        }
      }
    }
  }

  //Laskee AI:n inputit
  def calculateInputs: (Double, Double, Double) = {
    val inputs: (Double, Double, Double) = if (failsafeBrake) (0,1,0) else miniCheckpoints(miniCheckpointIndex)._2 match {
      case 1 => {
        val steeringAngle = 0.3
        val nextMiniPos = miniCheckpoints(miniCheckpointIndex)._1
        val angle = abs(pos.realAngleBetween(nextMiniPos) - 90 + 360 * 10000) % 360
        val steeringInput = if (angle > 270 && angle < 359.93) steeringAngle else if (angle < 90 && angle > 0.07) -steeringAngle else 0
        (steeringInput,0,1)
      }
      case 2 => {
        val sign = if (nextTurningRadius < 0) -1 else 1
        val absSteeringAngle = toDegrees(atan(Constants.wheelBase / abs(nextTurningRadius)))
        val steeringAngle = min(absSteeringAngle, Constants.maxSteeringAngle) * sign
        (steeringAngle,0,1)
      }
      case 3 => if (speed > nextTurnMaxSpeed) (0,1,0) else (0,0,1)
    }
    inputs
  }

  def updateInputs() = {
    val inputs = calculateInputs
    steeringInput = inputs._1
    brakeInput = inputs._2
    throttleInput = inputs._3
  }

  //Painamalla D:tä saa näkyviin AI:n minicheckpointit
  def drawAIRoute = {
    Some(miniCheckpoints.filter(a => miniCheckpoints.indexOf(a) >= miniCheckpointIndex).map(a => new Rectangle{
      height = 10
      width = 10
      fill = if (a == miniCheckpoints.last) Blue else Red
      val d = game.followedCar.pos.angleBetween(a._1)
      val c = a._1.differenceFromOtherXY(game.followedCar.pos)
      val b = a._1.difference(game.followedCar.pos)
      x = Constants.width / 2 + cos(toRadians(d + 90)) * b * game.followedScale * game.track.pixelsPerMeter - 5 * game.followedScale
      y = Constants.height / 3 * 2 - sin(toRadians(d+ 90)) * b * game.followedScale * game.track.pixelsPerMeter - 5 * game.followedScale
    }))
  }

  def update() = {
    checkCheckpoint()
    checkForOthers()
    if (game.time % 20 == 3) checkForCollisions()
    if (game.time % 100 == 1) slipstremMultiplier()
    calculateMiniCheckpoints()
    recalculateBrakePoint()
    updateInputs()
    drive(steeringInput, brakeInput, throttleInput)
    savePos(pos)
    checkForGrassAndSectors(wheelPlaces(this))
  }
}