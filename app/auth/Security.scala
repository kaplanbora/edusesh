package auth

import models.Account
import java.security.MessageDigest

object Security {
  val saltKey = "tsLM7x!PRYaYzj@jt2aUzrknHmwf6suNw7ZY6#YDyZQFu%V5GXFjtWn4So2zW7vVm7tSN&Qk$xV2@zI@rkk!Iuz$hk7wqmmnxoAh6^dXw&kuwoj%GJssfpbez&U%kYQM"
  val tokenKey = "2TFwXTmqrLRd8nH2aI2#%4Sr4kWTZtIPNsTWApkc364Y6&oBa7LbHQSa8KEcWC&cZikv8BshPR*Z6RuSbyndcjNPFEe&YHyAPcXmYf4I6uUnsErTWjLBmoyjRh3uLxoE"

  /**
    * Password is salted with a key before hashing.
    *
    * @param password Password of the account
    * @return SHA256 hash
    */
  def encodePassword(password: String): String = {
    // TODO: Add email for salting.
    MessageDigest.getInstance("SHA-256")
      .digest(s"$saltKey$password".getBytes("UTF-8"))
      .map("%02x".format(_)).mkString
  }

  def checkPassword(password: String, account: Account): Boolean = {
    account.password.equals(encodePassword(password))
  }
}