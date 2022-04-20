package Controls

import scalafx.scene.Scene
import scalafx.scene.input._
import scalafx.Includes._
import scala.collection.mutable.Set

object InputManager {

  // Sets do not allow duplicates, so they are useful here
  val keysPressed = Set[KeyCode]()
  var keyPressNow = Set[KeyCode]()
  var mouseX: Double = 0

  // Handle all
  def handleInput(scene: Scene) = {
    scene.onKeyPressed = (event) => {
      keysPressed += event.getCode
      keyPressNow += event.getCode
    }
    scene.onKeyReleased = event => {
      keysPressed -= event.getCode
    }
    scene.onMouseMoved = event => {
      mouseX = event.getX
    }
  }
}
