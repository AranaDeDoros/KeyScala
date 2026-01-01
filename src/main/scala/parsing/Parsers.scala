package org.aranadedoros.keyscala
package parsing

import model.Model.{DomainError, EntryKey, Flag, FlagError, Site}

object Parsers {

  private type ArgsList = List[String]

  object FlagParser:
    def parse(args: ArgsList): Either[DomainError, Flag] =
      args match
        case "--add" :: rawSite :: rawKey :: Nil =>
          for
            site <- Site.fromRaw(rawSite)
            key  <- EntryKey.fromRaw(rawKey)
          yield Flag.Add(site, key)
        case "--del" :: rawSite :: rawKey :: Nil =>
          for
            site <- Site.fromRaw(rawSite)
            key  <- EntryKey.fromRaw(rawKey)
          yield Flag.Delete(site, key)
        case "--search" :: rawKey :: Nil =>
          EntryKey.fromRaw(rawKey).map(Flag.Search.apply)
        case "--list" :: Nil =>
          Right(Flag.ListAll)
        case "--init" :: Nil =>
          Right(Flag.Init)
        case "--help" :: Nil | Nil =>
          Right(Flag.Help)
        case other =>
          Left(FlagError(other))

}
