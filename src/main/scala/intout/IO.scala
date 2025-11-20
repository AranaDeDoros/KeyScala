package org.aranadedoros
package intout

import auth.Security.EncryptedPassword
import intout.IO.{checkDB, enteredPassword}
import model.Model.{Database, Entry, Flag}
import persistence.Persistence.FileUtils
import serialization.BinarySerialization.writeBytes
import serialization.JsonSerialization

import java.io.File
import scala.io.StdIn.readLine

object IO {
  def checkDB(): Unit =
    try
      val dbFile = new File("db.enc")
      if !dbFile.exists() then
        println("creating...")
        println("enter username")
        val usr = readLine()
        println("enter master password")
        val password: String = enteredPassword
        println(s"entered password (hidden): $password")
        FileUtils.init(usr, password) match
          case Right(db) =>
            println(s"database created !")
          case Left(err) =>
            println(s"error creating database: ${err.getMessage}")
      else
        println("database already exists")
    catch
      case e: NullPointerException => println(e.getMessage)

  def enteredPassword: String =
    val console = System.console()
    if console == null then System.exit(1)
    val passwordChars = console.readPassword("Password: ")
    val password      = String(passwordChars)
    password
}

object CommandHandler:

  def handle(flag: Flag): Unit = flag match
    case Flag.Add(site, key) =>
      println(s"adding entry: ${key.value}")
      println("enter password")
      val password  = enteredPassword
      val encrypted = EncryptedPassword(password.getBytes("UTF-8"))
      val entry     = Entry(site, key, encrypted)
      val db = JsonSerialization.readDatabase() match
        case Right(d) => d
        case Left(_)  => Database()

      val newDb = db + entry
      JsonSerialization.writeDatabase(newDb) match
        case Right(_)  => println(newDb)
        case Left(err) => println(s"error writing database: ${err.getMessage}")

    case Flag.Delete(_, key) =>
      println(s"deleting entry: ${key.value}")
      val db = JsonSerialization.readDatabase() match
        case Right(d) => d
        case Left(_)  => Database()

      val newDb = db - key
      JsonSerialization.writeDatabase(newDb) match
        case Right(_)  => println(newDb)
        case Left(err) => println(s"error writing database: ${err.getMessage}")

    case Flag.ListAll =>
      JsonSerialization.readDatabase() match
        case Right(db) => println(db)
        case Left(err) => println(s"error reading DB: ${err.getMessage}")

    case Flag.Search(key) =>
      val db = JsonSerialization.readDatabase() match
        case Right(d) => d
        case Left(_)  => Database()
      println(db / key)

    case Flag.Init =>
      checkDB()

    case Flag.Help =>
      println("use a valid option (--add, --del, --list, --search, --init)")
