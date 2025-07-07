
import java.awt.Color

import org.apache.pdfbox.pdmodel.{PDDocument, PDPage, PDPageContentStream}
import org.apache.pdfbox.pdmodel.font.{PDType1Font, Standard14Fonts}
import org.apache.pdfbox.pdmodel.common.PDRectangle
import org.apache.pdfbox.pdmodel.PDPageContentStream.AppendMode
import org.apache.pdfbox.util.Matrix

import Location.Icon._

object ValMap {

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
    cos.addRect(x-d, y-d, d*2, d*2)
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

  def create(csv: Csv): Either[String, PDDocument] = {
    val pdf = new PDDocument()
    val page = new PDPage(PDRectangle.LETTER)
    pdf.addPage(page)
    val cos = new PDPageContentStream(pdf, page)

    // start by creating bounds values that allow us to convert from meters to page units
    val buffer = 50f // buffer on top bottom, left and right
    val bound = page.getMediaBox()
    val m = new Matrix()
    // for now scale the map width to the width of the page
    val scale: Float = (bound.getWidth()-2*buffer) / csv.boundMetersX
    m.scale(scale, scale)
    m.translate(-csv.minMetersX+buffer, -csv.minMetersY+buffer)
    cos.saveGraphicsState();
    cos.transform(m)
    // I think now we can just use meters to draw

    // draw bounding box for the map
    cos.setLineWidth(10)
    cos.setStrokingColor(Color.GRAY)
    cos.addRect(csv.minMetersX, csv.minMetersY, csv.maxMetersX-csv.minMetersX, csv.maxMetersY-csv.minMetersY)
    cos.stroke()

    // draw simple cross compasses
    csv.locations.foreach{ (location) => location match {
      case Location(_, xMeter, yMeter, Compass) => drawCompass(cos, xMeter, yMeter)
      case Location(_, xMeter, yMeter, Shack) => drawShack(cos, xMeter, yMeter)
      case Location(_, xMeter, yMeter, LineUp | LineDiag1 | LineRight | LineDiag2) => drawLine(cos, location.icon, xMeter, yMeter)
      case _ =>
    }}

    cos.restoreGraphicsState()

    cos.close()
    Right(pdf)
  }
}