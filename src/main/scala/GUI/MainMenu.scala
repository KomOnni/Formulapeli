package GUI

import Game._
import scalafx.scene.layout.VBox
import scalafx.scene.control.Button
import scalafx.geometry.Pos
import scalafx.scene.control.Label
import scalafx.geometry.Insets
import scalafx.scene.text.Font
import scalafx.scene.layout.Background
import scalafx.scene.layout.BackgroundImage
import scalafx.scene.image.Image
import scalafx.scene.layout.BackgroundRepeat
import scalafx.scene.layout.BackgroundPosition
import scalafx.scene.layout.BackgroundSize

//Kopioitu aikalailla suoraan malliprojektista
class MainMenu extends VBox {

  val vbox = new VBox

  vbox.setMinWidth(1920)
  vbox.setMinHeight(1040)

  this.spacing = 10
  this.alignment = Pos.TopCenter

  this.background = new Background(
      Array(
          new BackgroundImage(
              new Image("pics/backgroundPic.png"),
              BackgroundRepeat.NoRepeat,
              BackgroundRepeat.NoRepeat,
              BackgroundPosition.Center,
              new BackgroundSize(1920, 1080, true, true, true, true)
          )
      )
  )

  val header = new Label("           Autopeli           ")
  header.padding = Insets(50, 25, 10, 25)
  header.font = Font(200)

  val buttonContainer = new VBox
  buttonContainer.padding = Insets(25, 100, 25, 100)
  buttonContainer.spacing = 135
  buttonContainer.alignment = Pos.TopCenter

  def newRace = new Race(Game.Blackwood)
  def newTimeTrial = new TimeTrial(Game.Blackwood)
  def newAIRace = new AIRaceTest(Game.Blackwood)
  def newAITimetrial = new AIRaceTest(Game.Blackwood)

  val race = new Button {
    text = "Race"
    maxWidth = 300
    prefHeight = 50
    onAction = (event) => GUI.toGame(newRace)
  }

  val timetrial = new Button {
    text = "Timetrial"
    maxWidth = 300
    prefHeight = 50
    onAction = (event) => GUI.toGame(newTimeTrial)
  }

  val AIRace = new Button {
    text = "AI Race"
    maxWidth = 300
    prefHeight = 50
    onAction = (event) => GUI.toGame(newAIRace)
  }

  val AITimeTrial = new Button {
    text = "AI Timetrial"
    maxWidth = 300
    prefHeight = 50
    onAction = (event) => GUI.toGame(newAITimetrial)
  }

  buttonContainer.children = Array(race, timetrial, AIRace, AITimeTrial)

  this.children = Array(header, buttonContainer)

}