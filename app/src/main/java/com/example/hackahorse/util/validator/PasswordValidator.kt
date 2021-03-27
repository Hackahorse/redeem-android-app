package com.example.life365.util.validator

/**
 * Validator of password strength
 */
object PasswordValidator :
    RegexValidator("^.{6,}$")