package com.example.util

import java.security.MessageDigest

object SecurityUtils {
    private const val PASSWORD_SALT = "BisalpurHub#Marketplace2026@SecuritySalt"

    fun hashPassword(plainText: String): String {
        val md = MessageDigest.getInstance("SHA-256")
        val input = PASSWORD_SALT + plainText.trim()
        val digest = md.digest(input.toByteArray(Charsets.UTF_8))
        return digest.joinToString("") { "%02x".format(it) }
    }

    fun verifyPassword(plainText: String, storedHash: String): Boolean {
        if (storedHash == plainText) {
            // Backward compatibility for raw seed values if any
            return true
        }
        val computedHash = hashPassword(plainText)
        return computedHash == storedHash
    }

    fun normalizePhoneNumber(rawPhone: String): String {
        val digitsOnly = rawPhone.replace(Regex("[^0-9]"), "")
        val tenDigits = if (digitsOnly.length >= 10) digitsOnly.takeLast(10) else digitsOnly
        return if (tenDigits.length == 10) {
            "+91$tenDigits"
        } else {
            "+91$digitsOnly"
        }
    }

    fun get10DigitMobile(rawPhone: String): String {
        val digitsOnly = rawPhone.replace(Regex("[^0-9]"), "")
        return if (digitsOnly.length >= 10) {
            digitsOnly.takeLast(10)
        } else {
            digitsOnly
        }
    }

    fun isValidPhoneNumber(phone: String): Boolean {
        val digits = phone.replace(Regex("[^0-9]"), "")
        val tenDigits = if (digits.length >= 10) digits.takeLast(10) else digits
        return tenDigits.length == 10 && tenDigits.matches(Regex("^[6-9]\\d{9}$"))
    }

    fun isValidEmail(email: String): Boolean {
        return email.trim().matches(Regex("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$"))
    }
}
