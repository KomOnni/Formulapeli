import javax.swing.plaf.metal.MetalIconFactory.PaletteCloseIcon

class Track {
  val image: Pos

  //Aloituspaikat
  val timeTrialStart: Pos
  val raceStart: Vector[Pos]

  //Sektorit/maali
  val amountOfSectors:Int
  val sectorColors: Vector[Color]

  //AI
  val routeOfAIandAlt: Vector[(Pos, Boolean)]
}
