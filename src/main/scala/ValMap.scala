
import org.apache.pdfbox.pdmodel.{PDDocument,PDPage}

object ValMap {
  def create(csv: Csv): Either[String,PDDocument] = {
    val pdf = new PDDocument()
    val page: PDPage = new PDPage()
    pdf.addPage(page)
    Right(pdf)
  }
}