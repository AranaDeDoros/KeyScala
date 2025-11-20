package org.aranadedoros

import io.CommandHandler
import model.Model.Flag
import parsing.Parsers.FlagParser

object Main {

  def main(args: Array[String]): Unit =

    val key = sys.env.getOrElse("APP_SECRET", throw new Exception("APP_SECRET not set"))
    println(s"clave $key") // don't do this irl
    val raw = args.toList
    FlagParser.parse(raw) match
      case Right(flag) => CommandHandler.handle(flag)
      case Left(error) =>
        println(error)
        CommandHandler.handle(Flag.Help)

}
