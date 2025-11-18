package org.aranadedoros
package db

import auth.Security.{DecryptedPassword, EncryptedPassword, User}

import java.nio.file.{Files, Paths, StandardOpenOption}
import scala.collection.immutable.HashMap

object FileUtils:
  def writeBytes(path: String, data: Array[Byte]): Unit =
    Files.write(
      Paths.get(path),
      data,
      StandardOpenOption.CREATE,
      StandardOpenOption.APPEND,
      StandardOpenOption.WRITE
    )

  def readBytes(path: String): Array[Byte] =
    Files.readAllBytes(Paths.get(path))

object Persistence {
  opaque type EntryDictionary = HashMap[String, Entry]

  case class Entry(site: String, title: String, user: User, private val passw: String) {
    def password = DecryptedPassword(EncryptedPassword(passw))
  }

  case class Database(name: String, usr: User, entries: EntryDictionary = HashMap.empty):

    def +(entry: Entry): Database =
      copy(entries = entries + (entry.title -> entry))

    def -(title: String): Database =
      copy(entries = entries - title)

    override def toString: String =
      entries.map { case (k, v) => s"$k | ${v.site} | ${v.user}" }.mkString("\n")

    def /(entry: Entry): Either[String, Entry] =
      val optionEntry = entries.find((k, v) => v.title == entry.title)
      optionEntry match {
        case Some((k, v)) => Right(v)
        case None         => Left("Not found")
      }

    def searchEntry(entry: Entry): Either[String, Entry] =
      /(entry)
}
