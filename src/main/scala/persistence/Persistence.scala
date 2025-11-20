package org.aranadedoros
package persistence

import auth.Security.KeyProvider.given_SecretKey
import auth.Security.{Crypto, User}
import model.Model.Database
import serialization.BinarySerialization.writeBytes

object Persistence {
  object FileUtils:
    def init(user: String, password: String): Either[Throwable, Database] =
      val usr          = User(user, password)
      val db           = Database()
      val usrEncrypted = Crypto.encrypt(s"$usr".getBytes("UTF-8"))
      val contentEnc   = Crypto.encrypt("^".getBytes("UTF-8"))
      for
        _ <- writeBytes(data = usrEncrypted)
        _ <- writeBytes(data = contentEnc)
      yield db
}
