package org.aranadedoros

import auth.Security.*
import auth.Security.KeyProvider.given_SecretKey
import db.FileUtils

import java.io.File
import scala.io.StdIn.readLine
import java.io.Console

object Main {

  def main(args: Array[String]): Unit =
    // new database

    val key = sys.env.getOrElse("APP_SECRET", throw new Exception("APP_SECRET not set"))
    println(s"clave $key")

    try {
      val dbFile = new File("db.enc")
      if (!dbFile.exists()) {
        // search local filename if it does not exist, ask
        println("create database? Y/N")
        val createDatabase = readLine()
        val databaseExists: Unit = createDatabase match {
          case "Y" | "y" =>
            println("creating...")
            println("enter username")
            val usr = readLine()
            println("enter master password")
            val console: Console = System.console()

            if console == null then {
              System.exit(1)
            }

            val passwordChars = console.readPassword("Password: ")
            val password      = String(passwordChars)
            println(s"entered password (hidden): $password")
            // create
            val content      = "^".getBytes("UTF-8")
            val userInfo     = s"$usr|$password".getBytes("UTF-8")
            val usrEncrypted = Crypto.encrypt(userInfo)
            val encrypted    = Crypto.encrypt(content)
            // user info
            FileUtils.writeBytes("usr.enc", usrEncrypted)
            // entries
            FileUtils.writeBytes("db.enc", encrypted)
            println("database created")
          case _ => System.exit(1)
        }
      } else {
        // else it's on disk, ask for a command
        println("database already exists, select an option \n" +
          "add an entry (+) | delete an entry (-) | list all (*)")

      }
    } catch {
      case e: NullPointerException => println(e.getMessage)
    }

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
