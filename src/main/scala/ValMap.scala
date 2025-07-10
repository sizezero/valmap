
import java.awt.Color

import org.apache.pdfbox.pdmodel.{PDDocument, PDPage, PDPageContentStream}
import org.apache.pdfbox.pdmodel.font.{PDType1Font, Standard14Fonts}
import org.apache.pdfbox.pdmodel.common.PDRectangle
import org.apache.pdfbox.pdmodel.PDPageContentStream.AppendMode
import org.apache.pdfbox.util.Matrix

import Location.Icon._
import Properties.Orientation

object ValMap {

  def drawGridlines(cos: PDPageContentStream, csv: Csv) = {
    // try light grey every hundred meters, darker one every kilometer
    val lightGray = Color(240,240,240)
    val darkGray = Color(200,200,200)
    val lineWidth = 2

    // vertical lines
    var i = 0
    var x = csv.minMetersX.toInt + i*100
    while (x < csv.maxMetersX) {
      if (i % 10 == 0) {
        cos.setLineWidth(lineWidth)
        cos.setStrokingColor(darkGray)
      } else {
        cos.setLineWidth(lineWidth)
        cos.setStrokingColor(lightGray)
      }
      cos.moveTo(x, csv.minMetersY)
      cos.lineTo(x, csv.maxMetersY)
      cos.stroke()
      i = i + 1
      x = csv.minMetersX.toInt + i*100
    }

    // horizontal lines
    i = 0
    var y = csv.minMetersY.toInt + i*100
    while (y < csv.maxMetersY) {
      if (i % 10 == 0) {
        cos.setLineWidth(lineWidth)
        cos.setStrokingColor(darkGray)
      } else {
        cos.setLineWidth(lineWidth)
        cos.setStrokingColor(lightGray)
      }
      cos.moveTo(csv.minMetersX, y)
      cos.lineTo(csv.maxMetersX, y)
      cos.stroke()
      i = i + 1
      y = csv.minMetersY.toInt + i*100
    }
  }

  def drawCompass(cos: PDPageContentStream, x: Float, y: Float): Unit = {
    // TODO: may want to save state. I'm not sure if they can stack.
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

  def drawShack(cos: PDPageContentStream, x: Float, y: Float): Unit = {
    // TODO: may want to save state. I'm not sure if they can stack.
    cos.setLineWidth(5)
    cos.setStrokingColor(Color.BLACK)
    val d = 7f
    cos.moveTo(x-d, y-d)
    cos.lineTo(x, y+d)
    cos.lineTo(x+d, y-d)
    cos.lineTo(x-d, y-d)
    cos.stroke()
  }

  def drawBase(cos: PDPageContentStream, x: Float, y: Float): Unit = {
    // TODO: may want to save state. I'm not sure if they can stack.
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

  def drawLine(cos: PDPageContentStream, icon: Location.Icon, x: Float, y: Float): Unit = {
    cos.setLineWidth(5)
    cos.setStrokingColor(Color.BLACK)
    val d = 15f
    icon match {
      case LineUp => {
        cos.moveTo(x,y-d)
        cos.lineTo(x,y+d)
      }
      case LineDiag1 => {
        cos.moveTo(x+d,y-d)
        cos.lineTo(x-d,y-d)
      }
      case LineRight => {
        cos.moveTo(x-d,y)
        cos.lineTo(x+d,y)
      }
      case LineDiag2 => {
        cos.moveTo(x-d,y+d)
        cos.lineTo(x+d,y-d)
      }
      case _ =>
    }
    cos.stroke()
  }

  def drawCircle(cos: PDPageContentStream, x: Float, y: Float, r: Float): Unit = {
    val k: Float = 0.552284749831f
    cos.moveTo(x - r, y)
    cos.curveTo(x - r, y + k * r, x - k * r, y + r, x, y + r)
    cos.curveTo(x + k * r, y + r, x + r, y + k * r, x + r, y)
    cos.curveTo(x + r, y - k * r, x + k * r, y - r, x, y - r)
    cos.curveTo(x - k * r, y - r, x - r, y - k * r, x - r, y)
    cos.fill()
  }

  def drawBoss(cos: PDPageContentStream, x: Float, y: Float): Unit = {
    cos.setLineWidth(5)
    cos.setStrokingColor(Color.BLACK)
    cos.setNonStrokingColor(Color.BLACK)
    val r = 7f
    drawCircle(cos, x, y, r)
    cos.moveTo(x-r*1.5f, y+r*1.5f)
    cos.lineTo(x, y)
    cos.lineTo(x+r*1.5f, y+r*1.5f)
    cos.stroke()
  }

  def drawRoad(cos: PDPageContentStream, road: RoadLocation): Unit = {
    cos.setLineWidth(3)
    cos.setLineDashPattern(Array(3f), 0)
    cos.setStrokingColor(Color.BLACK)
    cos.moveTo(road.x1, road.y1)
    cos.lineTo(road.x2, road.y2)
    cos.stroke()
  }

  def create(csv: Csv): Either[String, PDDocument] = {
    val page = new PDPage(PDRectangle.LETTER)

    // start by creating bounds values that allow us to convert from meters to page units
    val buffer = 50f // buffer on top bottom, left and right
    //val bound = page.getMediaBox()

    //val m = new Matrix()

    // figure out whether we're portrait or landscape
    val (bound, m: Matrix) = csv.properties.orientation match {
      case Orientation.Portrait => {
        // this is the default so I guess we don't do anything
        (page.getMediaBox(), new Matrix())
      }
      case Orientation.Landscape => {
        page.setRotation(90)
        //val pageWidth: Float = pageSize.getWidth()
        val portraitBound = page.getMediaBox()
        val landscapeBound = PDRectangle(portraitBound.getHeight(), portraitBound.getWidth())
        (landscapeBound, new Matrix(0, 1, -1, 0, portraitBound.getWidth(), 0))
      }
      case Orientation.Auto => {
        val portraitBound = page.getMediaBox()
        val portraitRatio = portraitBound.getWidth() / portraitBound.getHeight()
        val landscapeRatio = 1f / portraitRatio
        val mapRatio = csv.boundMetersX / csv.boundMetersY
        if (Math.abs(mapRatio-portraitRatio) < Math.abs(mapRatio-landscapeRatio)) {
          (page.getMediaBox(), new Matrix())
        } else {
          page.setRotation(90)
          //val pageWidth: Float = pageSize.getWidth()
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

    // for now scale the map width to the width of the page
    val scale: Float = bound.getWidth() / (csv.boundMetersX+2*buffer)
    m.scale(scale, scale)
    m.translate(-csv.minMetersX+buffer, -csv.minMetersY+buffer)
    cos.transform(m)
    // I think now we can just use meters to draw

    // draw bounding box for the map
    if (csv.properties.bound) {
      cos.saveGraphicsState()
      cos.setLineWidth(10)
      cos.setStrokingColor(Color.CYAN)
      cos.addRect(csv.minMetersX, csv.minMetersY, csv.maxMetersX-csv.minMetersX, csv.maxMetersY-csv.minMetersY)
      cos.stroke()
      cos.restoreGraphicsState()
    }

    // draw gridlines
    drawGridlines(cos, csv)

    // draw simple cross compasses
    csv.locations.foreach{ (location) => {
      cos.saveGraphicsState()
      location match {
        case Location(Compass, _, xMeter, yMeter) => drawCompass(cos, xMeter, yMeter)
        case Location(Shack, _, xMeter, yMeter) => drawShack(cos, xMeter, yMeter)
        case Location(Base, _, xMeter, yMeter) => drawBase(cos, xMeter, yMeter)
        case Location(LineUp, _, xMeter, yMeter) => drawLine(cos, LineUp, xMeter, yMeter)
        case Location(LineDiag1, _, xMeter, yMeter) => drawLine(cos, LineDiag1, xMeter, yMeter)
        case Location(LineRight, _, xMeter, yMeter) => drawLine(cos, LineRight, xMeter, yMeter)
        case Location(LineDiag2, _, xMeter, yMeter) => drawLine(cos, LineDiag2, xMeter, yMeter)
        case Location(Boss, _, xMeter, yMeter) => drawBoss(cos, xMeter, yMeter)
        case road: RoadLocation => drawRoad(cos, road)
        case _ =>
      }
      cos.restoreGraphicsState()
    }}

    cos.close()
    Right(pdf)
  }
}