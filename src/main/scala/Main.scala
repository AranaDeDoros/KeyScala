package org.aranadedoros

import security.*

import scala.io.StdIn.{readBoolean, readLine}

object Main {

  def main(args: Array[String]): Unit =
    // new database
    import java.io.Console

    // search local filename if it does not exist ask
    println("create database? Y/N")
    val createDatabase = readLine()
    val databaseExists: Unit = createDatabase match {
      case "Y" | "y" => println("creating")
      case _         => System.exit(1)

    }

    // else it's on disk, ask for a command
    println("database already exists, select an option \n" +
      "add an entry (+) | delete an entry (-) | list all (*)")

    val console: Console = System.console()

    if console == null then {
      System.exit(1)
    }

    val passwordChars = console.readPassword("Password: ")
    val password      = String(passwordChars)
    println(s"entered password (hidden): $password")
    val user    = User("yo")
    val db      = Database("test", user)
    val gh      = Entry("github.com", "github", user, password)
    val entries = db.addEntry(gh)
    println(entries)
    // search entry
    val dp = entries.searchEntry(gh)
    val found = dp match {
      case Right(e)  => e
      case Left(err) => err
    }
    println(found)
}
