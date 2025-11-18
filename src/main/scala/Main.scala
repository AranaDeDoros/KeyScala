package org.aranadedoros

import auth.Security.*
import auth.Security.KeyProvider.given_SecretKey
import db.FileUtils

import java.io.File
import scala.io.StdIn.readLine

object Main {

  private def parseCommand(args: Array[String]): Unit =
    args.toList match
      case "--add" :: title :: Nil =>
        println(s"adding entry: $title")

      case "--del" :: title :: Nil =>
        println(s"deleting entry: $title")

      case "--list" :: Nil =>
        println("listing entries...")

      case "--search" :: title :: Nil =>
        println(s"searching for: $title")

      case "--init" :: Nil =>
        println("init flag received: forcing database creation")

      case _ =>
        println("invalid command")
        println("Valid commands:")
        println("--init | --add <title> | --del <title> | --list | --search <title>")

  def main(args: Array[String]): Unit =

    val key = sys.env.getOrElse("APP_SECRET", throw new Exception("APP_SECRET not set"))
    println(s"clave $key")

    try
      val dbFile = new File("db.enc")

      if args.nonEmpty then
        parseCommand(args)
        return

      if !dbFile.exists() then
        println("creating...")

        println("enter username")
        val usr = readLine()

        println("enter master password")
        val console = System.console()
        if console == null then System.exit(1)

        val passwordChars = console.readPassword("Password: ")
        val password      = String(passwordChars)
        println(s"entered password (hidden): $password")

        val content      = "^".getBytes("UTF-8")
        val userInfo     = s"$usr|$password".getBytes("UTF-8")
        val usrEncrypted = Crypto.encrypt(userInfo)
        val encrypted    = Crypto.encrypt(content)

        FileUtils.writeBytes("usr.enc", usrEncrypted)
        FileUtils.writeBytes("db.enc", encrypted)

        println("database created")
      else
        println("database already exists, select an option \n" +
          "add an entry (+) | delete an entry (-) | list all (*)   search (/)")
        val cmd = readLine("command: ")
        cmd match
          case "+" =>
            println("title?")
            val title = readLine()
            println(s"adding: $title") // integrate DB
          case "-" =>
            println("title?")
            val title = readLine()
            println(s"deleting: $title")
          case "*" =>
            println("listing entries...")
          case "/" =>
            println("search what?")
            val title = readLine()
            println(s"searching: $title")
          case _ =>
            println("unknown")
    catch
      case e: NullPointerException => println(e.getMessage)

//    val user    = User("yo")
//    val db      = Database("test", user)
//    val gh      = Entry("GitHub.com", "github", user, "password")
//    val entries = db.addEntry(gh)
//    println(entries)
//    // search entry
//    val dp = entries.searchEntry(gh)
//    val found = dp match {
//      case Right(e)  => e
//      case Left(err) => err
//    }
//    println(found)
}
