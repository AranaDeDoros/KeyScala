package org.aranadedoros
package auth

import auth.Security.KeyProvider.given_SecretKey

object Security {

  case class User(name: String, passwd: String):
    override def toString: String = s"$name | $passwd"

  opaque type SecretKey = Array[Byte]

  private object SecretKey:
    def fromEnv(env: String): SecretKey =
      val base64 = sys.env.getOrElse(env, throw new Exception(s"$env not set"))
      java.util.Base64.getDecoder.decode(base64)

  object KeyProvider:
    given SecretKey =
      SecretKey.fromEnv("APP_SECRET")

  import java.security.SecureRandom
  import javax.crypto.Cipher
  import javax.crypto.spec.{GCMParameterSpec, SecretKeySpec}

  object Crypto:
    private val AES_KEY_SIZE   = 32  // 256 bits
    private val GCM_NONCE_SIZE = 12
    private val GCM_TAG_SIZE   = 128 // bits

    def encrypt(plain: Array[Byte])(using key: SecretKey): Array[Byte] =
      val rnd   = SecureRandom()
      val nonce = new Array[Byte](GCM_NONCE_SIZE)
      rnd.nextBytes(nonce)

      val cipher    = Cipher.getInstance("AES/GCM/NoPadding")
      val spec      = new GCMParameterSpec(GCM_TAG_SIZE, nonce)
      val secretKey = new SecretKeySpec(key, "AES")

      cipher.init(Cipher.ENCRYPT_MODE, secretKey, spec)
      val ciphertext = cipher.doFinal(plain)

      nonce ++ ciphertext

    def decrypt(data: Array[Byte])(using key: SecretKey): Array[Byte] =
      val (nonce, ciphertext) = data.splitAt(GCM_NONCE_SIZE)

      val cipher    = Cipher.getInstance("AES/GCM/NoPadding")
      val spec      = new GCMParameterSpec(GCM_TAG_SIZE, nonce)
      val secretKey = new SecretKeySpec(key, "AES")

      cipher.init(Cipher.DECRYPT_MODE, secretKey, spec)
      cipher.doFinal(ciphertext)

  trait Encryptable:
    def encrypted: Array[Byte]

  trait Decryptable:
    def decrypted: Array[Byte]

  private object EncryptionUtils:
    def encrypt(plain: String): Array[Byte] =
      val passw = plain.getBytes("UTF-8")
      Crypto.encrypt(passw)

    def decrypt(encrypted: EncryptedPassword): Array[Byte] =
      val passw = encrypted.plain.getBytes("UTF-8")
      Crypto.decrypt(passw)

  case class DecryptedPassword(encpassword: EncryptedPassword) extends Decryptable:
    def decrypted: Array[Byte] = EncryptionUtils.decrypt(encpassword)

  case class EncryptedPassword(plain: String) extends Encryptable:
    def encrypted: Array[Byte] = EncryptionUtils.encrypt(plain)

}
