package auth

import models.Account
import java.security.MessageDigest

object Security {
  val saltKey = "tsLM7x!PRYaYzj@jt2aUzrknHm"
  val tokenKey = "2TFwXTmqrLRd8nH2aI2#%4Sr4k"

  /**
    * Password is salted with a key before hashing.
    *
    * @param password Password of the account
    * @return SHA256 hash
    */
  def encodePassword(password: String, email: String): String = {
    MessageDigest.getInstance("SHA-256")
      .digest(s"$saltKey$email$password".getBytes("UTF-8"))
      .map("%02x".format(_)).mkString
  }

  def checkPassword(password: String, account: Account): Boolean = {
    account.password.equals(encodePassword(password, account.email))
  }
}
