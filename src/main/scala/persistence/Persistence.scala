package org.aranadedoros
package persistence

import auth.Security.{MasterHashStorage, MasterPasswordService, PasswordManager}
import model.Model.Database
import serialization.{BinarySerialization, JsonSerialization}

import cats.effect.IO

import java.io.File
import scala.io.StdIn.readLine

object Persistence:
  // TODO handle bette persistence errors
  private object FileUtils:
    def init(user: String, password: String): IO[Unit] =
      for
        service <- IO(new MasterPasswordService())
        hash    <- IO(service.generateHash(password))
        db      <- IO(Database())
        _       <- IO.fromEither(MasterHashStorage.saveHash(hash))
        bytes   <- IO.fromEither(JsonSerialization.serialize(db))
        _       <- BinarySerialization.writeBytes(bytes)
      yield ()

  object DatabaseManager:
    def loadOrCreate(password: String): cats.effect.IO[Database] =
      val dbFile = new File("db.enc")
      if !dbFile.exists() then
        for
          _   <- IO.println("Database does not exist. Creating...")
          _   <- IO.println("Enter username:")
          usr <- IO.readLine
          _   <- FileUtils.init(usr, password)
          db  <- JsonSerialization.readDatabase()
        yield db
      else
        for
          hash <- IO.fromEither(MasterHashStorage.loadHash())
          mngr = new PasswordManager(hash)
          _ <- mngr
            .validate(password) {
              password.trim.nonEmpty && password.trim.length >= 6
            }
            .fold(
              msg => IO.raiseError(new Exception(msg)),
              _ => IO.unit
            )
          db <- JsonSerialization.readDatabase()
        yield db
