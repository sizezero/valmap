
import org.apache.pdfbox.pdmodel.{PDDocument, PDPage, PDPageContentStream}
import org.apache.pdfbox.pdmodel.font.{PDType1Font, Standard14Fonts}

object ValMap {

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

  def create(csv: Csv): Either[String,PDDocument] = {
    val pdf = new PDDocument()
    val page: PDPage = new PDPage()
    pdf.addPage(page)
    pdf.addPage(new PDPage())
    addText(pdf, 1)
    Right(pdf)
  }
}