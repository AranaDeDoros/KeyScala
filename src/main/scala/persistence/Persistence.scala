package org.aranadedoros
package persistence

import model.Model.Database
import serialization.JsonSerialization

object Persistence {
  object FileUtils:
    def init(user: String, password: String): Either[Throwable, Array[Byte]] =
      // val usr          = User(user, password)
      val db         = Database()
      val serialized = JsonSerialization.serialize(db)
      for
        ndb <- serialized
      yield ndb
}
