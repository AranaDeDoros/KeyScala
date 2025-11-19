package org.aranadedoros

import auth.Security.EncryptedPassword
import model.Model.{Database, Entry, EntryKey, Site as Website}
import persistence.Persistence.FileUtils
import serialization.Serialization.{readBytes, writeBytes}

import java.io.File
import scala.collection.immutable.HashMap
import scala.io.StdIn.readLine

object Main {

  private def parseCommand(args: Array[String]): Unit =
    args.toList match
      case "--add" :: site :: title :: Nil =>
        println(s"adding entry: $title")
        println("enter password")
        val password: String = enterPassword
        val encPassword: EncryptedPassword =
          EncryptedPassword(password.getBytes(java.nio.charset.Charset.forName("UTF-8")))
        val newEntry = for {
          site <- Website(site)
          newSite = site
          title <- EntryKey(title)
          newKey = title
        } yield Entry(newSite, newKey, encPassword)

        val added = newEntry match {
          case Left(err) =>
            println(s"Error: $err")
          case Right(entry) =>
            val bytes = readBytes()
            val db    = Database()
            val newDb = db.copy(entries = HashMap.empty) // db + current entries
            newDb + entry

        }
        writeBytes(data = added.toString.getBytes("UTF-8"))
        println(added)

      case "--del" :: title :: Nil =>
        println(s"deleting entry: $title")
        println("enter password")
        val bytes    = readBytes()                      // read
        val db       = Database()                       // db
        val newDb    = db.copy(entries = HashMap.empty) // db + current entries
        val entryKey = EntryKey(title)
        val edited = entryKey match {
          case Left(err)  => println(s"$err")
          case Right(key) => newDb - key
        }
        writeBytes(data = edited.toString.getBytes("UTF-8"))

      case "--list" :: Nil =>
        println("listing entries...")
        val bytes = readBytes()                      // read
        val db    = Database()
        val newDb = db.copy(entries = HashMap.empty) // db + current entries
        println(newDb)

      case "--search" :: title :: Nil =>
        println(s"searching for: $title")
        val bytes = readBytes()                      // read
        val db    = Database()                       // deserialize
        val newDb = db.copy(entries = HashMap.empty) // db + current entries

        val entryKey = EntryKey(title)
        val found = entryKey match {
          case Left(err)  => println(s"$err")
          case Right(key) => newDb / key
        }
        println(found)

      case "--init" :: Nil =>
        println("init flag received: forcing database creation")
        checkDB()
      case "--help" :: Nil | Nil | List(_, _*) =>
        println("use a valid option (--add, --del, --list, --search, --init")

  private def enterPassword = {
    val console = System.console()
    if console == null then System.exit(1)
    val passwordChars = console.readPassword("Password: ")
    val password      = String(passwordChars)
    password
  }

  private def checkDB(): Unit = {
    try
      val dbFile = new File("db.enc")
      if !dbFile.exists() then
        println("creating...")
        println("enter username")
        val usr = readLine()
        println("enter master password")
        val password: String = enterPassword
        println(s"entered password (hidden): $password")
        FileUtils.init(usr, password) match
          case Right(db) =>
            println(s"database '${db.name}' created !")
          case Left(err) =>
            println(s"error creating database: ${err.getMessage}")
      else
        println("database already exists")
    catch
      case e: NullPointerException => println(e.getMessage)
  }

  def main(args: Array[String]): Unit =

    val key = sys.env.getOrElse("APP_SECRET", throw new Exception("APP_SECRET not set"))
    println(s"clave $key")

    parseCommand(Array[String]("--init"))

}
