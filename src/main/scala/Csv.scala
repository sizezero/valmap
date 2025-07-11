import scala.annotation.tailrec

// we use Float values instead of Doubles since the PDFBox library uses floats.

// all x and y values are meters with x increasing the the right and y increasing upwards

case class Properties(
  title: Option[String] = None,
  orientation: Properties.Orientation = Properties.Orientation.Auto,
  bound: Boolean = true
)

object Properties {
  enum Orientation:
    case Portrait, Landscape, Auto
}

enum Glyph():
  case Stones, Boss, Compass, Shack, Base, LineUp, LineDiag1, LineRight, LineDiag2, Road

case class Location(
  glyph: Glyph,
  description: String,
  x: Float,
  y: Float
)

// this can't be the right way to do this
case class RoadLocation(
  glyph: Glyph,
  description: String,
  x1: Float,
  y1: Float,
  x2: Float,
  y2: Float)

case class Csv(
    properties: Properties,
    locations: List[Location | RoadLocation]
) {

  // dump all coordinates in a list (Locations have one coordinate, Roads have two coordinates)
  private val onlyCoordinates: List[(Float, Float)] = {
    locations.flatMap{ e =>
      if (e.isInstanceOf[Location]) {
        val loc = e.asInstanceOf[Location]
        List((loc.x, loc.y))
      } else {
        val road = e.asInstanceOf[RoadLocation]
        List((road.x1, road.y1), (road.x2, road.y2))
      }
    }
  }

  val minX: Float = onlyCoordinates.reduce{ (c1,c2) => if (c1._1 < c2._1) c1 else c2 }._1
  val minY: Float = onlyCoordinates.reduce{ (c1,c2) => if (c1._2 < c2._2) c1 else c2 }._2
  val maxX: Float = onlyCoordinates.reduce{ (c1,c2) => if (c1._1 > c2._1) c1 else c2 }._1
  val maxY: Float = onlyCoordinates.reduce{ (c1,c2) => if (c1._2 > c2._2) c1 else c2 }._2
  val boundX: Float = maxX-minX
  val boundY: Float = maxY-minY
}

object Csv {

  def parse(filename: String, g: os.Generator[String]): Either[String, Csv] = {

    @tailrec
    def processPropertyLine(in: Seq[String], prevLineNo: Int, p: Properties): Either[String, (Seq[String], Int, Properties)] = {
      if (in.isEmpty) Left(s"No locations found in file: $filename")
      else {
        val line = in.head
        val lineNo = prevLineNo + 1
        if (line.startsWith("#location")) Right(in.tail, lineNo, p)
        else {
          val a = line.split(",", 6) // line will never have more than six elements
          if (a.length != 6) Left(s"line does not have six elements: $line")
          else {
            a(0) match {
              case "Title" => processPropertyLine(in.tail, lineNo, p.copy(title = Some(a(1))))
              case "Orientation" => processPropertyLine(in.tail, lineNo, p.copy(orientation = Properties.Orientation.valueOf(a(1))))
              case "Bound" => if (a(1)=="yes") processPropertyLine(in.tail, lineNo, p.copy(bound = true))
                else if (a(1)=="no") processPropertyLine(in.tail, lineNo, p.copy(bound = false))
                else Left(s"($lineNo): Bound must be true or false")
              case s if (s=="" || s.startsWith("#")) => processPropertyLine(in.tail, lineNo, p)
              case _ => Left(s"($lineNo): unrecognized property name: ${a(0)}")
            }
          }
        }
      }
    }

    def mkError(lineNo: Int, msg: String): Either[String, List[Location | RoadLocation]] = Left(s"$filename($lineNo): $msg")

    @tailrec
    def processLocationLine(in: Seq[String], prevLineNo: Int, locations: List[Location | RoadLocation]): Either[String, List[Location | RoadLocation]] = {
      if (in.isEmpty) Right(locations)
      else {
        val line = in.head
        val lineNo = prevLineNo + 1
        val a = line.split(",", 6) // line will never have more than six elements
        if (a.length != 6) Left(s"line does not have six elements: $line")
        else {
          // TODO: add some error processing for image
          a(2).toFloatOption match {
            case None => mkError(lineNo, s"x meter value is not a double: ${a(2)}")
            case Some(xmeter) => {
              a(3).toFloatOption match {
                case None => mkError(lineNo, s"y meter value is not a double: ${a(3)}")
                case Some(ymeter) => {
                  val glyph = Glyph.valueOf(a(0)) // TODO: need to error check
                  if (glyph == Glyph.Road) {
                    a(4).toFloatOption match {
                      case None => mkError(lineNo, s"x2 meter value is not a double: ${a(4)}")
                      case Some(x2) => {
                        a(5).toFloatOption match {
                          case None => mkError(lineNo, s"y2 meter value is not a double: ${a(5)}")
                          case Some(y2) => {
                            processLocationLine(in.tail, lineNo, RoadLocation(glyph, a(1), xmeter, ymeter, x2, y2) :: locations)
                          }
                        }
                      }
                    }
                  } else 
                    processLocationLine(in.tail, lineNo, Location(glyph, a(1), xmeter, ymeter) :: locations)
                }
              }
            }
          }
        }
      }
    }

    processPropertyLine(g.toSeq, 0, Properties()) match {
        case Right((prevLineNo, input, properties)) => {
          processLocationLine(prevLineNo, input, Nil) match {
            case Right(locations) => Right(Csv(properties, locations.reverse))
            case Left(error) => Left(error)
          }
        }
        case Left(error) => Left(error)
    }
  }

}
