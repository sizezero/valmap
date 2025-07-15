
import java.awt.Color

import org.apache.pdfbox.pdmodel.{PDDocument, PDPage, PDPageContentStream}
import org.apache.pdfbox.pdmodel.font.{PDType1Font, Standard14Fonts}
import org.apache.pdfbox.pdmodel.common.PDRectangle
import org.apache.pdfbox.pdmodel.PDPageContentStream.AppendMode
import org.apache.pdfbox.util.Matrix

import Properties.Orientation

object Pdf {

  private def drawGridlines(cos: PDPageContentStream, csv: Csv) = {
    // try light grey every hundred meters, darker one every kilometer
    val lightGray = Color(240,240,240)
    val darkGray = Color(200,200,200)
    val lineWidth = 2

    // vertical lines
    var i = 0
    var x = csv.minX.toInt + i*100
    while (x < csv.maxX) {
      if (i % 10 == 0) {
        cos.setLineWidth(lineWidth)
        cos.setStrokingColor(darkGray)
      } else {
        cos.setLineWidth(lineWidth)
        cos.setStrokingColor(lightGray)
      }
      cos.moveTo(x, csv.minY)
      cos.lineTo(x, csv.maxY)
      cos.stroke()
      i = i + 1
      x = csv.minX.toInt + i*100
    }

    // horizontal lines
    i = 0
    var y = csv.minY.toInt + i*100
    while (y < csv.maxY) {
      if (i % 10 == 0) {
        cos.setLineWidth(lineWidth)
        cos.setStrokingColor(darkGray)
      } else {
        cos.setLineWidth(lineWidth)
        cos.setStrokingColor(lightGray)
      }
      cos.moveTo(csv.minX, y)
      cos.lineTo(csv.maxX, y)
      cos.stroke()
      i = i + 1
      y = csv.minY.toInt + i*100
    }
  }

  private def drawCompass(cos: PDPageContentStream, x: Float, y: Float): Unit = {
    cos.setLineWidth(5)
    cos.setStrokingColor(Color.BLACK)
    val d = 7f
    cos.moveTo(x-d,y)
    cos.lineTo(x+d,y)
    cos.stroke()
    cos.moveTo(x,y-d)
    cos.lineTo(x,y+d)
    cos.stroke()
  }

  private def drawShack(cos: PDPageContentStream, x: Float, y: Float): Unit = {
    cos.setLineWidth(5)
    cos.setStrokingColor(Color.BLACK)
    val d = 7f
    cos.moveTo(x-d, y-d)
    cos.lineTo(x, y+d)
    cos.lineTo(x+d, y-d)
    cos.lineTo(x-d, y-d)
    cos.stroke()
  }

  private def drawBase(cos: PDPageContentStream, x: Float, y: Float): Unit = {
    cos.setLineWidth(5)
    cos.setStrokingColor(Color.BLACK)
    val d = 7f
    cos.moveTo(x-1.5f*d, y-d)
    cos.lineTo(x-1.5f*d, y+d)
    cos.lineTo(x-0.5f*d, y+d)
    cos.lineTo(x-0.5f*d, y)
    cos.lineTo(x+0.5f*d, y)
    cos.lineTo(x+0.5f*d, y+d)
    cos.lineTo(x+1.5f*d, y+d)
    cos.lineTo(x+1.5f*d, y-d)
    cos.lineTo(x-1.5f*d, y-d)
    cos.stroke()
  }

  private def drawLine(cos: PDPageContentStream, glyph: Glyph, x: Float, y: Float): Unit = {
    cos.setLineWidth(5)
    cos.setStrokingColor(Color.BLACK)
    val d = 15f
    glyph match {
      case Glyph.LineUp => {
        cos.moveTo(x,y-d)
        cos.lineTo(x,y+d)
      }
      case Glyph.LineDiag1 => {
        cos.moveTo(x+d,y-d)
        cos.lineTo(x-d,y-d)
      }
      case Glyph.LineRight => {
        cos.moveTo(x-d,y)
        cos.lineTo(x+d,y)
      }
      case Glyph.LineDiag2 => {
        cos.moveTo(x-d,y+d)
        cos.lineTo(x+d,y-d)
      }
      case _ =>
    }
    cos.stroke()
  }

  // PDFs don't do circles so you have to use multiple elipses; this is magic code from the interwebs
  private def pathCircle(cos: PDPageContentStream, x: Float, y: Float, r: Float): Unit = {
    val k: Float = 0.552284749831f
    cos.moveTo(x - r, y)
    cos.curveTo(x - r, y + k * r, x - k * r, y + r, x, y + r)
    cos.curveTo(x + k * r, y + r, x + r, y + k * r, x + r, y)
    cos.curveTo(x + r, y - k * r, x + k * r, y - r, x, y - r)
    cos.curveTo(x - k * r, y - r, x - r, y - k * r, x - r, y)
  }

  private def drawBoss(cos: PDPageContentStream, x: Float, y: Float): Unit = {
    cos.setLineWidth(5)
    cos.setStrokingColor(Color.BLACK)
    cos.setNonStrokingColor(Color.BLACK)
    val r = 7f
    pathCircle(cos, x, y, r)
    cos.fill()
    cos.moveTo(x-r*1.5f, y+r*1.5f)
    cos.lineTo(x, y)
    cos.lineTo(x+r*1.5f, y+r*1.5f)
    cos.stroke()
  }

  private def drawStones(cos: PDPageContentStream, x: Float, y: Float): Unit = {
    val r = 5
    val d = 8
    cos.setLineWidth(1)
    cos.setStrokingColor(Color.BLACK)
    cos.setNonStrokingColor(Color.BLACK)
    pathCircle(cos, x, y+d, r)
    cos.fill()
    pathCircle(cos, x, y-d, r)
    cos.fill()
    pathCircle(cos, x+d, y, r)
    cos.fill()
    pathCircle(cos, x-d, y, r)
    cos.fill()
    cos.stroke()
  }

  private def drawRoad(cos: PDPageContentStream, road: RoadLocation): Unit = {
    cos.setLineWidth(3)
    cos.setLineDashPattern(Array(3f), 0)
    cos.setStrokingColor(Color.BLACK)
    cos.moveTo(road.x1, road.y1)
    cos.lineTo(road.x2, road.y2)
    cos.stroke()
  }

  private def drawCircle(cos: PDPageContentStream, x: Float, y: Float): Unit = {
    cos.setLineWidth(2)
    cos.setStrokingColor(Color.BLACK)
    val r = 10f
    pathCircle(cos, x, y, r)
    cos.stroke()
  }

  def create(csv: Csv): Either[String, PDDocument] = {
    val page = new PDPage(PDRectangle.LETTER)

    val buffer = 50f // buffer on top bottom, left and right

    // start by creating bounds values that allow us to convert from meters to page units
    // figure out whether we're portrait or landscape
    val (bound: PDRectangle, m: Matrix) = csv.properties.orientation match {
      case Orientation.Portrait => {
        // this is the default so I guess we don't do anything
        (page.getMediaBox(), new Matrix())
      }
      case Orientation.Landscape => {
        page.setRotation(90)
        val portraitBound = page.getMediaBox()
        val landscapeBound = PDRectangle(portraitBound.getHeight(), portraitBound.getWidth())
        (landscapeBound, new Matrix(0, 1, -1, 0, portraitBound.getWidth(), 0))
      }
      case Orientation.Auto => {
        val portraitBound = page.getMediaBox()
        val portraitRatio = portraitBound.getWidth() / portraitBound.getHeight()
        val landscapeRatio = 1f / portraitRatio
        val mapRatio = csv.boundX / csv.boundY
        if (Math.abs(mapRatio-portraitRatio) < Math.abs(mapRatio-landscapeRatio)) {
          (page.getMediaBox(), new Matrix())
        } else {
          page.setRotation(90)
          val portraitBound = page.getMediaBox()
          val landscapeBound = PDRectangle(portraitBound.getHeight(), portraitBound.getWidth())
          (landscapeBound, new Matrix(0, 1, -1, 0, portraitBound.getWidth(), 0))
        }
      }
    }

    // set up the document with the single page
    val pdf = new PDDocument()
    pdf.addPage(page)
    val cos = new PDPageContentStream(pdf, page)

    // TODO: need to choose whether to match width or height
    // for now scale the map width to the width of the page
    {
      val scale: Float = {
        val pageRatio = bound.getWidth() / bound.getHeight()
        val mapRatio = csv.boundX / csv.boundY
        if (mapRatio > pageRatio)
          bound.getWidth() / (csv.boundX+2*buffer) // scale the width of the map to the page
        else
          bound.getHeight() / (csv.boundY+2*buffer) // scale the height of the map to the page
      }
      m.scale(scale, scale)
      m.translate(-csv.minX+buffer, -csv.minY+buffer)
      cos.transform(m)
    }
    // all drawing primitives now use meters in the map coordinates

    // draw bounding box for the map
    if (csv.properties.bound) {
      cos.saveGraphicsState()
      cos.setLineWidth(7)
      cos.setStrokingColor(Color.MAGENTA)
      cos.addRect(csv.minX, csv.minY, csv.maxX-csv.minX, csv.maxY-csv.minY)
      cos.stroke()
      cos.restoreGraphicsState()
    }

    // draw gridlines
    drawGridlines(cos, csv)

    // draw simple cross compasses
    csv.locations.foreach{ (location) => {
      cos.saveGraphicsState()
      location match {
        case Location(Glyph.Stones,    _, x, y) => drawStones(cos, x, y)
        case Location(Glyph.Compass,   _, x, y) => drawCompass(cos, x, y)
        case Location(Glyph.Shack,     _, x, y) => drawShack(cos, x, y)
        case Location(Glyph.Base,      _, x, y) => drawBase(cos, x, y)
        case Location(Glyph.LineUp,    _, x, y) => drawLine(cos, Glyph.LineUp, x, y)
        case Location(Glyph.LineDiag1, _, x, y) => drawLine(cos, Glyph.LineDiag1, x, y)
        case Location(Glyph.LineRight, _, x, y) => drawLine(cos, Glyph.LineRight, x, y)
        case Location(Glyph.LineDiag2, _, x, y) => drawLine(cos, Glyph.LineDiag2, x, y)
        case Location(Glyph.Boss,      _, x, y) => drawBoss(cos, x, y)
        case road: RoadLocation                 => drawRoad(cos, road)
        case Location(Glyph.Circle,    _, x, y) => drawCircle(cos, x, y)
        case Location(Glyph.Road, _, _, _)      => throw new RuntimeException("Locations should never be Glyph.Road")
      }
      cos.restoreGraphicsState()
    }}

    cos.close()
    Right(pdf)
  }
}