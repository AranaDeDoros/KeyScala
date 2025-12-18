package org.aranadedoros

import inout.CommandHandler
import inout.IO.{enteredPassword, promptFlag}
import persistence.Persistence.DatabaseManager

import cats.effect.unsafe.implicits.global

object Main {

  def main(args: Array[String]): Unit =

    // val key = sys.env.getOrElse("APP_SECRET", throw new Exception("APP_SECRET not set"))
    // println(key)
    // val raw      = Array[String]("--list")
    val program =
      for
        _        <- cats.effect.IO.println(args.mkString("Array(", ", ", ")"))
        password <- cats.effect.IO(enteredPassword)
        db       <- DatabaseManager.loadOrCreate(password)
        flag <- cats.effect.IO.fromEither(
          promptFlag(args)
            .left.map(err => new Exception(err.toString))
        )
        _ <- CommandHandler.execute(db, flag, enteredPassword)
        _ <- cats.effect.IO.println("Done")
      yield ()

    program
      .handleErrorWith { err =>
        cats.effect.IO.println(s"Error: ${err.getMessage}")
      }
      .unsafeRunSync()
}
