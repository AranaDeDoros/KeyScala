package org.aranadedoros.keyscala
package model

import auth.Security.{Crypto, DecryptedPassword, EncryptedPassword}

import scala.collection.immutable.{HashMap, List}
object Model {

  // domain errors
  sealed trait DomainError:
    def message: String
    override def toString: String = message

  case class FlagError(args: List[String]) extends DomainError:
    def message = s"Unknown command: ${args.mkString(" ")}"

  case class InvalidEntryKey(raw: String) extends DomainError:
    def message = "Key cannot be empty."

  case class NotFoundEntryKey(key: String) extends DomainError:
    def message = "Entry not found."

  case class InvalidSite(raw: String) extends DomainError:
    def message = "Key cannot be empty."

  case class WrongPassword(password: String = "<password/>"):
    def message = "Wrong password"

  final case class EntryKey private (value: String) extends AnyVal

  object EntryKey:
    def fromRaw(raw: String): Either[InvalidEntryKey, EntryKey] =
      if raw.trim.nonEmpty then Right(EntryKey(raw.trim))
      else Left(InvalidEntryKey(raw))

  final case class Site private (value: String) extends AnyVal

  object Site:
    def fromRaw(raw: String): Either[InvalidSite, Site] =
      if raw.trim.nonEmpty then Right(Site(raw.trim))
      else Left(InvalidSite(raw))

  case class Entry(site: Site, key: EntryKey, encrypted: EncryptedPassword):
    def password: DecryptedPassword =
      Crypto.decrypt(encrypted)

  sealed trait Flag:
    def asString: String

    def asArgs: List[String]

  sealed trait AuthorizedCommand

  object Flag:
    case class Add(site: Site, key: EntryKey) extends Flag with AuthorizedCommand:
      private val prefix       = "--add"
      def asArgs: List[String] = List(prefix, site.value, key.value)
      def asString: String     = asArgs.mkString(" ")

    case class Edit(site: Site, key: EntryKey) extends Flag with AuthorizedCommand:
      private val prefix       = "--edit"
      def asArgs: List[String] = List(prefix, site.value, key.value)
      def asString: String     = asArgs.mkString(" ")

    case class Delete(site: Site, key: EntryKey) extends Flag with AuthorizedCommand:
      private val prefix       = "--del"
      def asArgs: List[String] = List(prefix, site.value, key.value)
      def asString: String     = asArgs.mkString(" ")

    case object ListAll extends Flag with AuthorizedCommand:
      def asArgs           = List("--list")
      def asString: String = "--list"

    case class Search(key: EntryKey) extends Flag with AuthorizedCommand:
      def asArgs: List[String] = List("--search", key.value)
      def asString: String     = asArgs.mkString(" ")

    case object Init extends Flag with AuthorizedCommand:
      def asArgs   = List("--init")
      def asString = "--init"

    case object Help extends Flag:
      def asArgs   = List("--help")
      def asString = "--help"

  private type EntriesDictionary = HashMap[EntryKey, Entry]

  case class Database(
      name: String = "db",
      entries: EntriesDictionary = HashMap.empty
  ):

    def +(entry: Entry): Database =
      copy(entries = entries + (entry.key -> entry))

    def -(key: EntryKey): Database =
      copy(entries = entries - key)

    override def toString: String =
      entries.map { case (k, v) => s" ${v.site.value} | ${v.key.value}" }.mkString("\n")

    def /(key: EntryKey): Either[NotFoundEntryKey, Entry] =
      entries.get(key).toRight(NotFoundEntryKey(key.value))

    def searchEntry(key: EntryKey): Either[NotFoundEntryKey, Entry] =
      /(key)
}
