import scala.annotation.tailrec

// we use Float values instead of Doubles since the PDFBox library uses floats.

case class Location(
  description: String,
  xMeter: Float,
  yMeter: Float,
  icon: Location.Icon
)

object Location {
  enum Icon():
    case Stones, Boss, Compass, Shack, Base, LineUp, LineDiag1, LineRight, LineDiag2
}

case class Csv(
    locations: List[Location]

) {
  lazy val minMetersX: Float = locations.reduce{ (l1,l2) => if (l1.xMeter < l2.xMeter) l1 else l2 }.xMeter
  lazy val minMetersY: Float = locations.reduce{ (l1,l2) => if (l1.yMeter < l2.yMeter) l1 else l2 }.yMeter
  lazy val maxMetersX: Float = locations.reduce{ (l1,l2) => if (l1.xMeter > l2.xMeter) l1 else l2 }.xMeter
  lazy val maxMetersY: Float = locations.reduce{ (l1,l2) => if (l1.yMeter > l2.yMeter) l1 else l2 }.yMeter
  lazy val boundMetersX: Float = maxMetersX-minMetersX
  lazy val boundMetersY: Float = maxMetersY-minMetersY
}

object Csv {

  def parse(filename: String, g: os.Generator[String]): Either[String, Csv] = {

    @tailrec
    def skipToLocations(in: Seq[String], prevLineNo: Int): Either[String, (Seq[String], Int)] = {
      if (in.isEmpty) Left(s"No locations found in file: $filename")
      else {
        val line = in.head
        val lineNo = prevLineNo + 1
        if (line.startsWith("#location")) Right(in.tail, lineNo)
        else skipToLocations(in.tail, lineNo)
      }
    }

    def mkError(lineNo: Int, msg: String): Either[String, List[Location]] = Left(s"$filename($lineNo): $msg")

    @tailrec
    def processLine(in: Seq[String], prevLineNo: Int, locations: List[Location]): Either[String, List[Location]] = {
      if (in.isEmpty) Right(locations)
      else {
        val line = in.head
        val lineNo = prevLineNo + 1
        val a = line.split(",", 6) // line will never have more than six elements
        if (a.length != 6) Left(s"line does not have six elements: $line")
        else {
          // TODO: add some error processing for image
          a(1).toFloatOption match {
            case None => mkError(lineNo, s"x meter value is not a double: ${a(1)}")
            case Some(xmeter) => {
              a(2).toFloatOption match {
                case None => mkError(lineNo, s"y meter value is not a double: ${a(2)}")
                case Some(ymeter) => {
                  val icon = Location.Icon.valueOf(a(5)) // TODO: need to error check
                  processLine(in.tail, lineNo, Location(a(0), xmeter, ymeter, icon) :: locations)
                }
              }
            }
          }
        }
      }
    }

    skipToLocations(g.toSeq, 0) match {
        case Right((prevLineNo, input)) => {
          processLine(prevLineNo, input, Nil) match {
            case Right(locations) => Right(Csv(locations))
            case Left(error) => Left(error)
          }
        }
        case Left(error) => Left(error)
    }
  }

}
