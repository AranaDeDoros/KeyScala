package org.aranadedoros.keyscala
package inout

import auth.Security.{Crypto, DecryptedPassword}
import concurrency.SecureClipboard
import model.Model.{Database, DomainError, Entry, EntryKey, Flag}
import parsing.Parsers
import serialization.JsonSerialization

import cats.effect.unsafe.implicits.global

import scala.concurrent.duration.DurationInt

object IO:
  def enteredPassword: String =
    val console = System.console()
    if console != null then
      val passwordChars = console.readPassword("Password: ")
      String(passwordChars)
    else
      println("Console not available. Using StdIn.readLine as fallback:")
      scala.io.StdIn.readLine("Password: ")

  def promptFlag(args: Array[String]): Either[DomainError, Flag] =
    Parsers.FlagParser.parse(args.toList)

object CommandHandler:

  def execute(db: Database, flag: Flag, passwordPrompt: => String): cats.effect.IO[Database] =
    flag match
      case Flag.Add(site, key) =>
        val password  = passwordPrompt
        val encrypted = Crypto.encrypt(DecryptedPassword(password))
        val entry     = Entry(site, key, encrypted)
        val newDb     = db + entry
        for
          _ <- cats.effect.IO.println(s"""adding entry "${key.value}"""")
          _ <- JsonSerialization.writeDatabase(newDb)
        yield newDb
      case Flag.Edit(site, key) =>
        val password  = passwordPrompt
        val encrypted = Crypto.encrypt(DecryptedPassword(password))
        for
          dbLoaded <- JsonSerialization
            .readDatabase()
            .handleError(_ => Database())

          result <- dbLoaded / key match
            case Right(entry) =>
              val updatedEntry = Entry(site, key, encrypted)
              cats.effect.IO.pure(dbLoaded + updatedEntry)

            case Left(err) =>
              cats.effect.IO.println(s"entry not found: $err").as(dbLoaded)
        yield result
      case Flag.Delete(_, key) =>
        val newDb = db - key
        for
          _ <- JsonSerialization.writeDatabase(newDb)
        yield newDb

      case Flag.ListAll =>
        cats.effect.IO.println(db).as(db)

      case Flag.Search(key) =>
        for
          dbLoaded <- JsonSerialization
            .readDatabase()
            .handleError(_ => Database())

          result <- dbLoaded / key match
            case Right(entry) =>
              for
                _ <- SecureClipboard
                  .resource(entry.password.value)
                  .use { _ =>
                    cats.effect.IO.println(
                      s"Password copied to clipboard for 10 seconds. Press Ctrl+C to exit."
                    ) >>
                      cats.effect.IO.sleep(10.seconds)
                  }.onCancel {
                    cats.effect.IO.println("cancelled")
                  }
              yield dbLoaded

            case Left(err) =>
              cats.effect.IO.println(s"Entry not found: $err").as(dbLoaded)
        yield result

      case Flag.Init =>
        cats.effect.IO.pure(db)

      case Flag.Help =>
        cats.effect.IO.println("use a valid option (--add, --del, --list, --search, --init)")
          .as(db)
