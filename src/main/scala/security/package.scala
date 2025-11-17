package org.aranadedoros

import scala.collection.immutable.HashMap

package object security {

  opaque type EntryDictionary = HashMap[String, Entry]

  case class User(username: String) {
    override def toString: String = username
  }

  object Encryption {
    def encrypt(plain: String) =
      "encriptado"

    def decrypt(encrypted: EncryptedPassword) =
      "descencriptado"
  }

  trait Encryptable

  case class DecryptedPassword(epassword: EncryptedPassword) extends Encryptable {
    def decrypted = Encryption.decrypt(epassword)
  }

  case class EncryptedPassword(plain: String) extends Encryptable {
    def encrypted = Encryption.encrypt(plain)
  }

  case class Entry(site: String, title: String, user: User, private val passw: String) {
    def password = DecryptedPassword(EncryptedPassword(passw))
  }

  case class Database(name: String, usr: User, entries: EntryDictionary = HashMap.empty):
    def addEntry(entry: Entry): Database =
      copy(entries = entries + (entry.title -> entry))

    override def toString: String =
      entries.map { case (k, v) => s"$k | ${v.site} | ${v.user}" }.mkString("\n")

    def searchEntry(entry: Entry): Either[String, Entry] = {
      val optionEntry = entries.find((k, v) => v.title == entry.title)
      optionEntry match {
        case Some((k, v)) => Right(v)
        case None         => Left("Not found")
      }
    }
}
