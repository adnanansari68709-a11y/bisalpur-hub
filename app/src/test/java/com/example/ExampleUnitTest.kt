package com.example

import com.example.util.SecurityUtils
import org.junit.Assert.*
import org.junit.Test

class ExampleUnitTest {

    @Test
    fun testPasswordHashingAndVerification() {
        val rawPassword = "mypassword123"
        val hash = SecurityUtils.hashPassword(rawPassword)

        assertNotEquals(rawPassword, hash)
        assertTrue(SecurityUtils.verifyPassword(rawPassword, hash))
        assertFalse(SecurityUtils.verifyPassword("wrongpass", hash))
    }

    @Test
    fun testPhoneNormalization() {
        assertEquals("+919876543210", SecurityUtils.normalizePhoneNumber("9876543210"))
        assertEquals("+919876543210", SecurityUtils.normalizePhoneNumber("+91 9876543210"))
        assertEquals("+919876543210", SecurityUtils.normalizePhoneNumber("919876543210"))
        assertEquals("9876543210", SecurityUtils.get10DigitMobile("9876543210"))
    }

    @Test
    fun testPhoneValidation() {
        assertTrue(SecurityUtils.isValidPhoneNumber("9876543210"))
        assertTrue(SecurityUtils.isValidPhoneNumber("8876543210"))
        assertTrue(SecurityUtils.isValidPhoneNumber("7876543210"))
        assertTrue(SecurityUtils.isValidPhoneNumber("6876543210"))
        assertFalse(SecurityUtils.isValidPhoneNumber("12345")) // Too short
        assertFalse(SecurityUtils.isValidPhoneNumber("1234567890")) // Doesn't start with 6-9
    }

    @Test
    fun testEmailValidation() {
        assertTrue(SecurityUtils.isValidEmail("aman@example.com"))
        assertTrue(SecurityUtils.isValidEmail("test.user+tag@domain.co.in"))
        assertFalse(SecurityUtils.isValidEmail("invalid-email"))
        assertFalse(SecurityUtils.isValidEmail("user@"))
    }
}
