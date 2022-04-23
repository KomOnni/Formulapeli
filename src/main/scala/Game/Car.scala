package Game

import scalafx.scene.image.{Image, ImageView}
import scalafx.scene.input.KeyCode
import Constants.Constants
import scalafx.scene.Node

import scala.collection.mutable.Buffer
import scala.math._

abstract class Car(val game: Game, var pos: Pos, val livery: Int) {

  //nopeus
  var speed: Double = 0

  //slipstream boolean
  var slipstreamBoolean = false

  // aikaan liittyvät
  val sectortimes = Buffer[Long]()
  var lastSector: Option[Double] = None
  var lastLap: Option[Double] = None
  var bestLap: Option[Double] = None

  //inputs
  var steeringInput: Double = 0
  var throttleInput: Double = 0
  var brakeInput: Double = 0

  //viime sijainnit
  var twoSecsOfPos = Buffer[(Double, Double, Double)]()

  //apufunktiot

  def savePos(pos: Pos) = {
    if (game.time % 5 == 0) {
      twoSecsOfPos += ((pos.getX, pos.getY, pos.getR))
      if (twoSecsOfPos.size > 2 / 5.0 * Constants.tickRate) twoSecsOfPos = twoSecsOfPos.tail
    }
  }

  //alun perin nollas nopeuden kokonaan, mutta näin parempi. metodia kutsutaan jos pelaajalla kaikki neljä rengasta ruoholla
  def resetSpeed() = speed = speed * 0.3

  //metodilla saa Pos oliolla pisteen radan kuvasta
  def gamePosToTrack(pos: Pos) = {
    (pos.getX * game.track.pixelsPerMeter, pos.getY * game.track.pixelsPerMeter)
  }

  //teleporttaa pelaajan sekunnin taaksepäin, i on renkaiden määrä ruoholla. Testasin muita lukemia mutta totesin että 4 on paras, jonka takia toteutettu näin
  def tpIfGrass(i: Int) = {
    if (i == 4) {
      val a = if (twoSecsOfPos.size > 100 / 5) twoSecsOfPos(100 / 5) else twoSecsOfPos.head
      pos.changeTo(a._1,a._2,a._3)
      resetSpeed()
    }
  }

  //Samalla tapahtuu sektorien ja ruohon tarkastus, palauttaa monta rengasta nurmella, hoitaa sektorien päivityksen.
  def checkForGrassAndSectors(positions: Buffer[Pos]): Int = {
    var count = 0
    val f = positions.map(a => gamePosToTrack(a))
    f.foreach(a => {
      game.track.img.pixelReader match {
        case None =>
        case Some(pr) => {
          val color = pr.getColor(a._1.round.toInt, a._2.round.toInt)
          if (color.green - 0.2 > color.blue) {
            count += 1
          } else if (color == game.track.sectorColors(sectortimes.size % game.track.amountOfSectors)) {
            sectortimes += game.time
            val print = game.track.sectorColors.indexOf(color) match {
              case 0 => if (sectortimes.size > 1) {
                val b = sectortimes.takeRight(4)
                val lastLapVal = (b(3) - b(0)) / Constants.tickRate.toDouble
                lastLap = Some(lastLapVal)
                if (bestLap.forall(a => a > lastLapVal)) bestLap = Some(lastLapVal)

                val a = sectortimes.takeRight(2)
                Some((a(1) - a(0)) / Constants.tickRate.toDouble)
              } else None
              case 1 => {
                val a = sectortimes.takeRight(2)
                Some((a(1) - a(0)) / Constants.tickRate.toDouble)
              }
              case 2 => {
                val a = sectortimes.takeRight(2)
                Some((a(1) - a(0)) / Constants.tickRate.toDouble)
              }
            }
            lastSector = print
          }
        }
      }
    })
    count
  }

  //Palauttaa kokoelman, jossa kaikki auton renkaiden paikat pelin Pos olioina
  def wheelPlaces(car: Car): Buffer[Pos] = {
    val ret = Buffer[Pos]()
    val wheelOffsetX = (51 - 28) / (145.0 / 5)
    val FWOffsetY = (110 - 73) / (145.0 / 5)
    val BWOffsetY = (73 - 23) / (145.0 / 5)

    val p = pos

    //Kulmat laskettu edellä olevian val:ien käytettyjen arvojen avulla
    ret += new Pos(p.getX + FWOffsetY * cos(toRadians(p.getR + 31.86)), p.getY + wheelOffsetX * sin(toRadians(p.getR + 31.86)))
    ret += new Pos(p.getX + FWOffsetY * cos(toRadians(p.getR - 31.86)), p.getY + wheelOffsetX * sin(toRadians(p.getR - 31.86)))
    ret += new Pos(p.getX - BWOffsetY * cos(toRadians(p.getR + 24.7)), p.getY + wheelOffsetX * sin(toRadians(p.getR + 24.7)))
    ret += new Pos(p.getX - BWOffsetY * cos(toRadians(p.getR - 24.7)), p.getY + wheelOffsetX * sin(toRadians(p.getR - 24.7)))

    ret
  }

  //Laskee "downforcen" eli kuinka paljon aerodynamiikka painaa autoa maahan
  def downforce = pow(speed,2) * Constants.downforce

  //Laskee kaasun aiheuttaman voiman
  def throttle(input: Double): Double = {
    val futureSpeed = sqrt(( Constants.mass / 2 * pow(speed, 2) + (input * Constants.motor)) / ( Constants.mass / 2 ))
    val accerlation = (futureSpeed - speed)
    accerlation * Constants.mass
  }

  //Laskee, onko autolla slipstreamiä
  def slipstremMultiplier() = {
    if (game.cars.size >= 2) {
      val collection = game.cars.filterNot(_ == this).flatMap(_.twoSecsOfPos.map(a => new Pos(a._1,a._2).difference(pos)))
      slipstreamBoolean = collection.nonEmpty && collection.exists(_ < 2)
    }
  }

  //Laskee ilmanvastuksen, jossa on slipstream mukaanluettuna
  def drag = {
    val slipstreamMul = if (slipstreamBoolean) Constants.slipstream else 1
    if (brakeInput == 0) pow(speed,1.7) * Constants.drag * slipstreamMul else 0
  }

  //Jarrujen aiheuttama voima
  def brake(input: Double): Double = {
    Constants.brake * input
  }

  //Estää autoa menemästä toisen läpi vähentämällä siitä nopeutta
  def checkForCollisions(): Unit = {
    val others = game.cars.filterNot(_ == this)
    if (others.nonEmpty) {
      val closestDistanceAndCar = others.map(a => (a.pos.difference(pos), a)).minBy(_._1)
      if (closestDistanceAndCar._1 < 1) {
        closestDistanceAndCar._2 match {
          case (ai: AICar) => {
            this match {
              case (thisAI: AICar) => if (!this.pos.isBehind(ai.pos)) speed = max(ai.speed - 10, 0)
              case (thisPl: PlayerCar) => speed = max(ai.speed - 10, 0)
            }
          }
          case _ =>
        }
      }
    }
  }

  //Yhteenlasketut voimat
  def totalForces(brakePedal: Double, gasPedal: Double): Double = ( throttle(gasPedal) - drag - brake(brakePedal) )

  //Maksimi kitka
  def maxTraction = (downforce + Constants.mass * 9.81) * Constants.tractionMultiplier

  //Laskee kääntymissäteen eturenkaiden kulmasta
  def turningCircleFunction(steeringAngle: Double): Double = if (abs(steeringAngle) < 0.00001) 0 else Constants.wheelBase/tan(toRadians(steeringAngle))

  //Laskee kääntymissäteen maksimikitka mukaanluettuna
  def realTurn(steeringAngle: Double) = {
    val turningCircle = turningCircleFunction(steeringAngle)
    val sign = if (turningCircle < 0) -1 else 1
    val absturningCircle = abs(turningCircle)
    val turningTraction = if (absturningCircle != 0) pow(speed, 2) / absturningCircle * Constants.mass else 0
    val noUndersteerThisTick = turningTraction <= maxTraction

    if (noUndersteerThisTick) {
      turningCircle
    } else if (steeringAngle == 0) {
      0
    } else {
      //Bugin korjaamiseen max
      max(absturningCircle,(pow(speed,2) / (maxTraction / Constants.mass))) * sign
    }
  }

  def drawAIRoute: Option[Buffer[Node]]

  //Käytetty voima käännöksessä newtoneina
  def usedTractionInTurning(steeringAngle: Double) = if (realTurn(steeringAngle) != 0) pow(speed,2) / abs(realTurn(steeringAngle)) * Constants.mass else 0

  //Lisätty nopeus, kun ideaalinen ABS ja luistonesto
  def addedSpeed(steeringAngle: Double, brakePedal: Double, gasPedal: Double) = {
    if (maxTraction - usedTractionInTurning(steeringAngle) >= 0) {
      min(totalForces(brakePedal, gasPedal), maxTraction - usedTractionInTurning(steeringAngle)) / Constants.mass / Constants.tickRate
    } else {
      0
    }
  }

  //Ajamisen funktio, vasemmalle neg. steeringanle.
  def drive(steeringAngle: Double, brakePedal: Double, gasPedal: Double) = {
    val turningCircle: Double = turningCircleFunction(steeringAngle)
    val usedTractionInTurning = if (turningCircle != 0) pow(speed,2) / abs(realTurn(steeringAngle)) * Constants.mass else 0
    val availableTractionForSpeed = maxTraction - usedTractionInTurning
    val totalF = totalForces(brakePedal, gasPedal)

    val realSpeedAdd: Double = addedSpeed(steeringAngle, brakePedal, gasPedal)

    //Näin päin, koska tämä on lähempänä matkan integraalia nopeuden suhteen
    pos.add(realTurn(steeringAngle), speed)
    speed = max(0, speed + realSpeedAdd)//totalForces(brakePedal, gasPedal) / Constants.Constants.mass / Constants.Constants.tickRate)
  }

  //AI:n kuvat
  val img = livery match {
    case 1 => new Image("/pics/Ferrari.png")
    case 2 => new Image("/pics/Mercedes.png")
    case 3 => new Image("/pics/RedBull.png")
  }

  def update(): Unit

  def updateInputs(): Unit

  //Piirtää auton scalafx:lle
  def draw = {
    if (game.followedCar == this) {
      new ImageView(img) {
        x = Constants.width / 2 - img.getWidth / 2
        y = Constants.height * 2 / 3 - img.getHeight / 2
        rotate = -180
      }
    } else {
      val d = game.followedCar.pos.realAngleBetween(pos)
      val c = pos.differenceFromOtherXY(game.followedCar.pos)
      val b = pos.difference(game.followedCar.pos)
      new ImageView(img) {
        x = Constants.width / 2 + cos(toRadians(d)) * b * game.track.pixelsPerMeter * game.followedScale - img.getWidth / 2
        y = Constants.height / 3 * 2 - sin(toRadians(d)) * b * game.track.pixelsPerMeter * game.followedScale - img.getHeight / 2
        rotate = -180 + c._3
      }
    }
  }
}




class PlayerCar(game: Game,pos: Pos) extends Car(game,pos,1) {

  def drawAIRoute = None

  //MouseX:stä tehdään renkaiden kulma
  def mouseXtoSteeringInput: Double = {
    val x = Controls.InputManager.mouseX
    val sign = if (x - Constants.width / 2 < 0) -1 else 1
    val absFromCenter = abs(x - Constants.width / 2)
    val r = if (absFromCenter <= Constants.mouseDeadzone) 0 else {
      (absFromCenter - Constants.mouseDeadzone) / (Constants.width / 2 - Constants.mouseDeadzone) * Constants.maxSteeringAngle * sign
    }
    r
  }

  def updateInputs(): Unit = {
    steeringInput = mouseXtoSteeringInput
    throttleInput = if (Controls.InputManager.keysPressed.contains(KeyCode.W)) 1 else 0
    brakeInput = if (Controls.InputManager.keysPressed.contains(KeyCode.S)) 1 else 0
  }

  def update() = {
    updateInputs()
    if (game.time % 100 == 0) slipstremMultiplier()
    if (game.time % 10 == 5) checkForCollisions()
    drive(steeringInput, brakeInput, throttleInput)
    savePos(pos)
    tpIfGrass(checkForGrassAndSectors(wheelPlaces(this)))
  }
}