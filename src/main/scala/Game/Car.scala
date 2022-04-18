package Game

import scalafx.scene.Node
import scalafx.scene.image.{Image, ImageView}
import scalafx.scene.input.KeyCode

import scala.collection.mutable.Buffer
import scala.math._

sealed abstract class Car(val game: Game, var pos: Pos) {

  //val picOfCar: Pic
  var speed: Double = 0

  // aikaan liittyvät
  val sectortimes = Buffer[Long]()
  var invalidLap = false
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
    twoSecsOfPos += ((pos.getX, pos.getY, pos.getR))
    if (twoSecsOfPos.size > 2 * Constants.Constants.tickRate) twoSecsOfPos = twoSecsOfPos.tail
  }

  def resetSpeed = speed = 0

  def gamePosToTrack(pos: Pos) = {
    (pos.getX * game.track.pixelsPerMeter, pos.getY * game.track.pixelsPerMeter)
  }

  def tpIfGrass(i: Int) = {
    if (i == 4) {
      val a = twoSecsOfPos.head
      pos.changeTo(a._1,a._2,a._3)
      resetSpeed
    }
  }

  //Samalla tapahtuu sektorien ja ruohon tarkastus, palauttaa monta rengasta nurmella, hoitaa sektorien päivityksen
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
//            println("" + a + ", " + f.indexOf(a))
          } else if (color == game.track.sectorColors(sectortimes.size % game.track.amountOfSectors)) {
            sectortimes += game.time
            val print = game.track.sectorColors.indexOf(color) match {
              case 0 => if (sectortimes.size > 1) {
                val b = sectortimes.takeRight(4)
                val lastLapVal = (b(3) - b(0)) / 100.0
                lastLap = Some(lastLapVal)
                if (bestLap.forall(a => a > lastLapVal)) bestLap = Some(lastLapVal)

                val a = sectortimes.takeRight(2)
                Some((a(1) - a(0)) / 100.0)
              } else None
              case 1 => {
                val a = sectortimes.takeRight(2)
                Some((a(1) - a(0)) / 100.0)
              }
              case 2 => {
                val a = sectortimes.takeRight(2)
                Some((a(1) - a(0)) / 100.0)
              }
            }
            lastSector = print
          }
        }
      }
    })
    count
  }

  def wheelPlaces(car: Car): Buffer[Pos] = {
    val ret = Buffer[Pos]()
    val wheelOffsetX = (51 - 28) / (145.0 / 5)
    val FWOffsetY = (110 - 73) / (145.0 / 5)
    val BWOffsetY = (73 - 23) / (145.0 / 5)

    val p = game.player.pos

    //Kulmat laskettu edellä olevian val:ien käytettyjen arvojen avulla
    ret += new Pos(p.getX + FWOffsetY * cos(toRadians(p.getR + 31.86)), p.getY + wheelOffsetX * sin(toRadians(p.getR + 31.86)))
    ret += new Pos(p.getX + FWOffsetY * cos(toRadians(p.getR - 31.86)), p.getY + wheelOffsetX * sin(toRadians(p.getR - 31.86)))
    ret += new Pos(p.getX - BWOffsetY * cos(toRadians(p.getR + 24.7)), p.getY + wheelOffsetX * sin(toRadians(p.getR + 24.7)))
    ret += new Pos(p.getX - BWOffsetY * cos(toRadians(p.getR - 24.7)), p.getY + wheelOffsetX * sin(toRadians(p.getR - 24.7)))

    ret
  }

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

  def totalForces(brakePedal: Double, gasPedal: Double): Double = ( throttle(gasPedal) - drag - brake(brakePedal) )

  def maxTraction = (downforce + Constants.Constants.mass * 9.81) * Constants.Constants.tractionMultiplier

  def turningCircleFunction(steeringAngle: Double): Double = if (abs(steeringAngle) < 0.00001) 0 else Constants.Constants.wheelBase/tan(toRadians(steeringAngle))

  def realTurn(steeringAngle: Double) = {
    val turningCircle = turningCircleFunction(steeringAngle)
    val sign = if (turningCircle < 0) -1 else 1
    val absturningCircle = abs(turningCircle)
    val turningTraction = pow(speed, 2) / absturningCircle * Constants.Constants.mass
    val noUndersteerThisTick = turningTraction <= maxTraction

    if (noUndersteerThisTick) {
      turningCircle
    } else if (steeringAngle == 0) {
      0
    } else {
      //Bugin korjaamiseen max
      max(absturningCircle,(pow(speed,2) / (maxTraction / Constants.Constants.mass))) * sign
    }
  }


  def usedTractionInTurning(steeringAngle: Double) = if (realTurn(steeringAngle) != 0) pow(speed,2) / abs(realTurn(steeringAngle)) * Constants.Constants.mass else 0

  def addedSpeed(steeringAngle: Double, brakePedal: Double, gasPedal: Double) = {
    if (maxTraction - usedTractionInTurning(steeringAngle) >= 0) {
      min(totalForces(brakePedal, gasPedal), maxTraction - usedTractionInTurning(steeringAngle)) / Constants.Constants.mass / Constants.Constants.tickRate
    } else {
      0
    }
  }

  //Ajamisen funktio, vasemmalle neg. steeringanle.
  def drive(steeringAngle: Double, brakePedal: Double, gasPedal: Double) = {
    val turningCircle: Double = turningCircleFunction(steeringAngle)
    val usedTractionInTurning = if (turningCircle != 0) pow(speed,2) / abs(realTurn(steeringAngle)) * Constants.Constants.mass else 0
    val availableTractionForSpeed = maxTraction - usedTractionInTurning
    val totalF = totalForces(brakePedal, gasPedal)

    val realSpeedAdd: Double = addedSpeed(steeringAngle, brakePedal, gasPedal)

    //Näin päin, koska tämä on lähempänä matkan integraalia nopeuden suhteen
    pos.add(realTurn(steeringAngle), speed)
    speed = max(0, speed + realSpeedAdd)//totalForces(brakePedal, gasPedal) / Constants.Constants.mass / Constants.Constants.tickRate)
  }

  def update() = {
    updateInputs
    drive(steeringInput, brakeInput, throttleInput)
    draw
    savePos(pos)
    tpIfGrass(checkForGrassAndSectors(wheelPlaces(this)))
  }

  def updateInputs: Unit

  def draw = {
    if (game.followedCar == this) {
      val img = new Image("pics/Ferrari.png")
      new ImageView(img) {
        x = Constants.Constants.width / 2 - img.getWidth / 2
        y = Constants.Constants.height * 2 / 3 - img.getHeight / 2
        rotate = -180
      }
    } else {
      val img = new Image("pics/Ferrari.png")
      def diff = this.pos.differenceFromOtherXY(game.followedCar.pos)
      new ImageView(img) {
        x = Constants.Constants.width / 2 - img.getWidth / 2 + diff._1 * game.track.pixelsPerMeter
        y = Constants.Constants.height * 2 / 3 - img.getHeight / 2 + diff._2 * game.track.pixelsPerMeter
        rotate = -180 - diff._3
      }
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

/*
class AICar(game: Game, pos: Pos) extends Car(game,pos) {

  var nextCheckpointIndex = 0
  //var distanceFromCheckpoint = game.track.AI
}
 */