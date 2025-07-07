/** A non functional main with all the IO and side effects
  * 
  * @param args all command line args
  */
@main def main(args: String*): Unit = {

  if (args.isEmpty) {
    println("valmap <csvfile>")
    sys.exit(1)
  } else {
    val file: os.Path = os.Path(args.head)
    Csv.parse(os.read.lines.stream(file)) match {
      case Right(csv) => {
        println("parsed")

      }
      case Left(error) => {
        println(error)
        sys.exit(1)
      }
    }

    sys.exit(0)
  }
}

