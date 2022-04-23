package Game

import scalafx.scene.Node
import scalafx.scene.image.{Image, ImageView}
import scalafx.scene.paint.Color


abstract class Track {

  val img: Image
  val image: Node
  //Suhteuttaa kuvan pelin pos systeemiin
  val pixelsPerMeter: Double


  //Aloituspaikat
  val timeTrialStart: Pos
  var raceStart: Vector[Pos]

  //Sektorit/maali
  val amountOfSectors = 3
  val sectorColors: Vector[Color]

  //AI checkpointit, positio, tyyppi, väistää oikealle, paljon väistää (kerroin))
  val routeAndAlt: Vector[(Pos, Int, Boolean, Double)]
}

object Blackwood extends Track {
  val img = new Image("/pics/blackwoodPixReal.png")
  val image = new ImageView(img)
  val pixelsPerMeter = 10
  val sectorColors = Vector(Color.rgb(255,255,255), Color.rgb(255,255,252), Color.rgb(255,255,250))
  val timeTrialStart: Pos = new Pos(57.73648439916815, 822.592310281429, -16.874563631786398)
  var raceStart = Vector(new Pos(221.8147254188017, 447.6946940014501, -69.8460646983609), new Pos(222.18152358387715, 448.08202239176103, -69.94425581914885), new Pos(218.9341218587001, 458.31280390639495, -73.26581378993792), new Pos(223.76053827537982, 464.71557057832524, -71.32384157884067))

  val routeAndAlt = Vector(
    //Alkusuora
    (new Pos(278.0037219656939, 349.9697098245176, -56.58008390215798), 2, true, 1),
    (new Pos(365.59046144802596, 201.9002016333386, -431.97230017175275 + 360), 1, true, 1),
    //Turn 1
    (new Pos(433.45826756811135, 68.95699176513524, -356.2590618741222 + 360), 1, false, 1),
    (new Pos(467.2274801446425, 99.72639957551709, 68.96232153008296), 2, true, 1),
    //Shikaani
    (new Pos(464.0784371955626, 146.80923130896076, 111.87412649301223), 2, false, 1),
    (new Pos(454.26690834075333, 187.2151246219126, -271.265933768716 + 360), 1, true, 0.8),
    (new Pos(468.3976640351184, 229.96108381817308, -309.01964360102835 + 360), 2, false, 1),
    (new Pos(485.95587557307044, 314.6401853038484, -270.00781580636897 + 360), 2, true, 1),
    //takasuora
    (new Pos(486.87505734606776, 477.35676175225984, 89.2035818316454), 3, true, 1),
    (new Pos(492.72090688650167, 824.925975271545, 86.7977362789358), 3, true, 1),
    (new Pos(506.4630973093915, 1051.3821678920717, 87.13438698021362), 3, true, 1),
    (new Pos(498.11268051987673, 1268.2459172713016, 93.22000252970021), 3, true, 1),
    //turn 5
    (new Pos(441.9786003434324, 1411.151001849395, 172.6174302445483), 1, false, 1),
    (new Pos(287.6037608672948, 1349.6012221567637, 208.96631216987564), 2, true, 1),
    //turn 6
    (new Pos(219.80252404598056, 1280.714371163161, 265.24129952581495), 1, false, 1),
    (new Pos(228.93360820951875, 1229.492508861636, 295.12598382157006), 2, true, 1),
    //turn 7
    (new Pos(225.13936057638557, 1129.24157964566, 245.42902365545845), 2, false, 1),
    // turn 8
    (new Pos(75.50571277405993, 942.9411621878821, 223.51632149403676), 2, true, 1),
    //turn 9
    (new Pos(32.57007567054109, 854.8029548796192, -787.978142547892 + 360 + 360 + 360), 1, false, 1),
    (new Pos(98.08073337662972, 799.1428552784843, -740.3439495246964 + 360 + 360 + 360), 2, true, 1),
    //turn 10
    (new Pos(221.09315669492875, 720.5419275909805, -80.68078502629342 + 360), 1, true, 1),
    (new Pos(212.69789218503365, 653.8371756608983, -1189.2044917572948 + 360 + 360 + 360 + 360), 2, false, 1),
    (new Pos(221.61021212665293, 463.6835186225621, -1154.3000556758911 + 360 + 360 + 360 + 360), 2, true, 1)
  )
}