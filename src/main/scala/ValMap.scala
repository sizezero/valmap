
import java.awt.Color

import org.apache.pdfbox.pdmodel.{PDDocument, PDPage, PDPageContentStream}
import org.apache.pdfbox.pdmodel.font.{PDType1Font, Standard14Fonts}
import org.apache.pdfbox.pdmodel.common.PDRectangle
import org.apache.pdfbox.pdmodel.PDPageContentStream.AppendMode
import org.apache.pdfbox.util.Matrix

object ValMap {

  def create(csv: Csv): Either[String, PDDocument] = {
    val pdf = new PDDocument()
    pdf.addPage(new PDPage())
    Right(pdf)
  }
}