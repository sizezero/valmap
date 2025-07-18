
//import org.apache.pdfbox.pdmodel.PDDocument

// turn a single csv file into a pdf file
def processFile(src: os.Path): Unit = {
  val filename = src.baseName + "." + src.ext
  Csv.parse(filename.toString, os.read.lines.stream(src)) match {
    case Right(csv) => {
      val doc = Pdf.create(csv)
      val outfile = os.pwd / "out" / (src.baseName + ".pdf")
      doc.save(outfile.toString)
      doc.close()
    }
    case Left(error) => {
      println(error)
      sys.exit(1)
    }
  }
}

def usage(): Unit = {
  println("valmap ( <inputfile> | inputdir )")
  sys.exit(1)
}

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
  }
  else if (args.isEmpty) usage()
  else {
    val fileOrDir: os.Path = os.Path(args.head, os.pwd)
    if (os.isFile(fileOrDir)) {
      processFile(fileOrDir)
    } else if (os.isDir(fileOrDir)) {
      os.list(fileOrDir).foreach { file =>
        if (file.ext == "csv") processFile(file)
        else println(s"WARNING: skiping file ${file.toString}")
      }
    } else usage()
    sys.exit(0)
  }
}

