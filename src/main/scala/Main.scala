/** A non functional main with all the IO and side effects
  * 
  * @param args all command line args
  */
@main def main(args: String*): Unit = {

  if (args.isEmpty) {
    println("valmap <csvfile>")
    sys.exit(1)
  } else {
    val fname = args.head
    println(fname)
    sys.exit(0)
  }
}

