
import java.awt.Color

import org.apache.pdfbox.pdmodel.{PDDocument, PDPage, PDPageContentStream}
import org.apache.pdfbox.pdmodel.font.{PDType1Font, Standard14Fonts}
import org.apache.pdfbox.pdmodel.common.PDRectangle
import org.apache.pdfbox.pdmodel.PDPageContentStream.AppendMode
import org.apache.pdfbox.util.Matrix
import org.apache.pdfbox.multipdf.LayerUtility
import org.apache.pdfbox.pdmodel.graphics.form.PDFormXObject

object PdfBoxExamples {

  /**
    * This example is taken from a tutorial.
    *
    * @param page
    */
  private def addText(document: PDDocument, pageNo: Int): Unit = {

    //Retrieving the pages of the document
    val page: PDPage = document.getPage(pageNo)
    val contentStream: PDPageContentStream = new PDPageContentStream(document, page)
    contentStream.beginText()

    //Setting the font to the Content stream
    contentStream.setFont(new PDType1Font(Standard14Fonts.FontName.TIMES_ROMAN), 12)

    //Setting the position for the line
    contentStream.newLineAtOffset(25, 500)

    val text: String = "This is the sample document and we are adding content to it."

    //Adding text in the form of string
    contentStream.showText(text)

    //Ending the content stream
    contentStream.endText()

    System.out.println("Content added")

    //Closing the content stream
    contentStream.close()
  }

  private def draw(document: PDDocument, pageNo: Int): Unit = {
    val page: PDPage = document.getPage(pageNo)
    val cos: PDPageContentStream = new PDPageContentStream(document, page)

    // good drawing examples found here
    // https://www.ulfdittmer.com/view?PdfBox

    // add two lines of different widths
    cos.setLineWidth(1)
    cos.moveTo(200, 250)
    cos.lineTo(400, 250)
    cos.closeAndStroke()
    cos.setLineWidth(5)
    cos.moveTo(200, 300)
    cos.lineTo(400, 300)
    cos.closeAndStroke()

    // draw a red box in the lower left hand corner
    cos.setNonStrokingColor(Color.RED)
    cos.addRect(10, 10, 100, 100)
    cos.fill()

    cos.close()
  }

  private def printWidthAndHeight(document: PDDocument, pageNo: Int): Unit = {
    val page: PDPage = document.getPage(pageNo)
    val rect: PDRectangle = page.getMediaBox()
    println(s"(${rect.getLowerLeftX()},${rect.getLowerLeftY()},${rect.getUpperRightX()},${rect.getUpperRightY()})")

    val cos: PDPageContentStream = new PDPageContentStream(document, page)
    cos.setLineWidth(5)
    cos.addRect(rect.getLowerLeftX(), rect.getLowerLeftY(), rect.getUpperRightX(), rect.getUpperRightY())
    cos.stroke()
    cos.close()

  }

  def drawDiagonal(cos: PDPageContentStream, dx: Float, dy: Float): Unit = {
    cos.setLineWidth(1)
    cos.moveTo(0, 0)
    cos.lineTo(dx, dy)
    cos.closeAndStroke()
  }

  def crossPage(): PDDocument = {
    val doc = new PDDocument()
    val page = new PDPage(PDRectangle.LETTER)
    doc.addPage(page)
    val rect: PDRectangle = page.getMediaBox()
    val cos: PDPageContentStream = new PDPageContentStream(doc, page)
    cos.setLineWidth(5)
    cos.addRect(rect.getLowerLeftX(), rect.getLowerLeftY(), rect.getUpperRightX(), rect.getUpperRightY())
    cos.stroke()
    cos.moveTo(rect.getLowerLeftX(), rect.getLowerLeftY())
    cos.lineTo(rect.getUpperRightX(), rect.getUpperRightY())
    cos.stroke()
    cos.moveTo(rect.getLowerLeftX(), rect.getUpperRightY())
    cos.lineTo(rect.getUpperRightX(), rect.getLowerLeftY())
    cos.stroke()
    cos.close()
    doc
  }

  def create(): PDDocument = {
    val pdf = new PDDocument()

    pdf.addPage(new PDPage())
    val page2: PDPage = new PDPage()
    pdf.addPage(page2)
    pdf.addPage(new PDPage(PDRectangle.LETTER))
    pdf.addPage(new PDPage(PDRectangle.A5))

    addText(pdf, 0)
    draw(pdf, 1)
    println("page 3")
    printWidthAndHeight(pdf, 2)
    println("page 4")
    printWidthAndHeight(pdf, 3)

    // landscape test
    val page5: PDPage = new PDPage(PDRectangle.LETTER)
    page5.setRotation(90)
    pdf.addPage(page5)
    addText(pdf, 4)
    val page6: PDPage = new PDPage(PDRectangle.LETTER)
    page6.setRotation(90)
    pdf.addPage(page6)
    println("page 6")
    printWidthAndHeight(pdf, 5)

    // correct landscape
    {
      // https://github.com/apache/pdfbox/tree/trunk/examples/src/main/java/org/apache/pdfbox/examples
      val page7: PDPage = new PDPage(PDRectangle.LETTER)
      page7.setRotation(90)
      pdf.addPage(page7)
      val pageSize: PDRectangle = page7.getMediaBox()
      val pageWidth: Float = pageSize.getWidth()
      val pageHeight: Float = pageSize.getHeight()
      // only change from default is first false which disables compression
      val cos: PDPageContentStream = new PDPageContentStream(pdf, page7, AppendMode.OVERWRITE, false, false)
      // add the rotation using the current transformation matrix
      // including a translation of pageWidth to use the lower left corner as 0,0 reference
      cos.transform(new Matrix(0, 1, -1, 0, pageWidth, 0))
      // page width and height are exchanged
      drawDiagonal(cos, pageHeight, pageWidth)
      cos.close()

      // non-landscape use of drawDiagonal()
      val page8: PDPage = new PDPage(PDRectangle.LETTER)
      pdf.addPage(page8)
      val cos2: PDPageContentStream = new PDPageContentStream(pdf, page8)
      drawDiagonal(cos2, pageWidth, pageHeight)
      cos2.close()
    }

    // embed pdf in pdf
    {
      val sourceDoc = crossPage()

      val page = new PDPage(PDRectangle.LETTER)
      pdf.addPage(page)
      val cos = new PDPageContentStream(pdf, page)

      // Create a Form XObject from the source document using LayerUtility
      val layerUtility: LayerUtility = new LayerUtility(pdf)
      val form: PDFormXObject = layerUtility.importPageAsForm(sourceDoc, 0) // 0 is page number in source doc

      // draw the full form
      cos.drawForm(form)

      // at the moment, bounds are the same for both the parent and embedded pdf
      val rect = sourceDoc.getPage(0).getMediaBox()
      val (boundx, boundy) = (rect.getUpperRightX(), rect.getUpperRightY())
      val m2 = Matrix()

      //val (sx, sy) = (0.5f, 0.5f)
      //val (sx, sy) = (0.25f, 0.25f)
      val (sx, sy) = (0.1f, 0.1f)
      m2.scale(sx, sy) // this aligns the lower left corners of the two pdfs
      cos.saveGraphicsState();
      cos.transform(m2)
      cos.setStrokingColor(Color.RED)
      cos.drawForm(form)
      cos.stroke()
      cos.restoreGraphicsState()

      // center the embeded pdf in the lower left corner of the parent
      m2.translate(-boundx/2f, -boundy/2f)
      cos.saveGraphicsState();
      cos.transform(m2)
      cos.setStrokingColor(Color.CYAN)
      cos.drawForm(form)
      cos.stroke()
      cos.restoreGraphicsState()

      val (dx, dy) = (0.5f, 0.5f) // percentage move across the parent canvas from the lower left corner
      val (parentx, parenty) = (boundx*dx, boundy*dy)
      m2.translate(parentx/sx, parenty/sy) // translate moves in units of the shrunkeen embedded pdf so we need to adjust
      cos.saveGraphicsState();
      cos.transform(m2)
      cos.setStrokingColor(Color.GREEN)
      cos.drawForm(form)
      cos.stroke()
      cos.restoreGraphicsState()

      // move up one quarter from center
      m2.translate(0, boundy*0.25f/sy) // translate moves in units of the shrunkeen embedded pdf so we need to adjust
      cos.saveGraphicsState();
      cos.transform(m2)
      cos.setStrokingColor(Color.MAGENTA)
      cos.drawForm(form)
      cos.stroke()
      cos.restoreGraphicsState()

      cos.close()
    }

    pdf
  }
}