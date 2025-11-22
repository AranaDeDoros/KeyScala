package org.aranadedoros
package persistence

import auth.Security.{MasterHashStorage, MasterPasswordService}
import model.Model.Database
import serialization.{BinarySerialization, JsonSerialization}

object Persistence {
  object FileUtils:
    def init(user: String, password: String): Either[Throwable, Array[Byte]] =
      // write for the 1st time
      val service = new MasterPasswordService()
      val hash    = service.generateHash(password)
      val db      = Database()
      for
        _          <- MasterHashStorage.saveHash(hash)
        serialized <- JsonSerialization.serialize(db)
        _          <- BinarySerialization.writeBytes(serialized)
      yield serialized
}
