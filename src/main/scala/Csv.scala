import scala.annotation.tailrec

case class Location(
  description: String,
  xMeter: Double,
  yMeter: Double,
  image: String
)

case class Csv(
    locations: List[Location]

    // TODO: see if min max and bounds can be lazy vals

)

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
          a(1).toDoubleOption match {
            case None => mkError(lineNo, s"x meter value is not a double: ${a(1)}")
            case Some(xmeter) => {
              a(2).toDoubleOption match {
                case None => mkError(lineNo, s"y meter value is not a double: ${a(2)}")
                case Some(ymeter) => processLine(in.tail, lineNo, Location(a(0), xmeter, ymeter, a(5)) :: locations)
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
