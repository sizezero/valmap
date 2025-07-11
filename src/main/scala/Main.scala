
//import org.apache.pdfbox.pdmodel.PDDocument

/** A non functional main with all the IO and side effects
  * 
  * @param args all command line args
  */
@main def main(args: String*): Unit = {

  if (false) {
    val doc = PdfBoxExamples.create()
    val outfile = os.pwd / "valmap-output.pdf"
    doc.save(outfile.toString)
    doc.close()
  } else if (args.isEmpty) {
    println("valmap <csvfile>")
    sys.exit(1)
  } else {
    val file: os.Path = os.Path(args.head, os.pwd)
    Csv.parse(file.toString, os.read.lines.stream(file)) match {
      case Right(csv) => {
        Pdf.create(csv) match {
          case Right(doc) => {
            val outfile = os.pwd / "valmap-output.pdf"
            doc.save(outfile.toString)
            doc.close()
          }
          case Left(error) => {
            println(error)
            sys.exit(1)
          }
        }
      }
      case Left(error) => {
        println(error)
        sys.exit(1)
      }
    }

    sys.exit(0)
  }
}

