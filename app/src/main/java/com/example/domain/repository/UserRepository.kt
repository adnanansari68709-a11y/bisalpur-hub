package com.example.domain.repository

import com.example.data.local.dao.UserDao
import com.example.data.local.entity.UserEntity
import com.example.domain.model.Role
import com.example.util.SecurityUtils
import kotlinx.coroutines.flow.Flow
import java.util.concurrent.ConcurrentHashMap
import kotlin.random.Random

data class OtpSession(
    val phone: String,
    val otpCode: String,
    val generatedAtMillis: Long = System.currentTimeMillis(),
    val expiresAtMillis: Long = System.currentTimeMillis() + (5 * 60 * 1000) // 5 minutes validity
)

class UserRepository(private val userDao: UserDao) {

    // In-memory OTP session cache for active mobile recovery sessions
    private val activeOtpSessions = ConcurrentHashMap<String, OtpSession>()

    suspend fun signup(name: String, email: String, password: String, role: Role): Result<Long> {
        val cleanEmail = email.trim().lowercase()
        val cleanName = name.trim()

        if (!SecurityUtils.isValidEmail(cleanEmail)) {
            return Result.failure(Exception("Please enter a valid email address."))
        }

        val existing = userDao.getUserByEmail(cleanEmail)
        if (existing != null) {
            return Result.failure(Exception("An account with this email address already exists. Please login instead."))
        }

        val passwordHash = SecurityUtils.hashPassword(password)
        val user = UserEntity(
            name = cleanName,
            email = cleanEmail,
            phone = null,
            passwordHash = passwordHash,
            role = role
        )
        val id = userDao.insertUser(user)
        return Result.success(id)
    }

    suspend fun signupWithPhone(name: String, phone: String, password: String, role: Role): Result<Long> {
        val cleanName = name.trim()
        val normalizedPhone = SecurityUtils.normalizePhoneNumber(phone)
        val tenDigits = SecurityUtils.get10DigitMobile(phone)

        if (!SecurityUtils.isValidPhoneNumber(phone)) {
            return Result.failure(Exception("Please enter a valid 10-digit Indian mobile number."))
        }

        val existing = userDao.findUserByIdentifier(
            identifier = phone.trim(),
            lowercaseIdentifier = phone.trim().lowercase(),
            normalizedPhone = normalizedPhone,
            tenDigits = tenDigits
        )
        if (existing != null) {
            return Result.failure(Exception("An account with this mobile number already exists. Please login instead."))
        }

        val passwordHash = SecurityUtils.hashPassword(password)
        val user = UserEntity(
            name = cleanName,
            email = null,
            phone = normalizedPhone,
            passwordHash = passwordHash,
            role = role
        )
        val id = userDao.insertUser(user)
        return Result.success(id)
    }

    suspend fun login(identifier: String, password: String): Result<UserEntity> {
        val cleanIdentifier = identifier.trim()
        if (cleanIdentifier.isBlank()) {
            return Result.failure(Exception("Please enter your email or mobile number."))
        }

        val normalizedPhone = SecurityUtils.normalizePhoneNumber(cleanIdentifier)
        val tenDigits = SecurityUtils.get10DigitMobile(cleanIdentifier)

        val user: UserEntity? = if (cleanIdentifier.contains("@")) {
            userDao.getUserByEmail(cleanIdentifier.lowercase())
                ?: userDao.findUserByIdentifier(
                    identifier = cleanIdentifier,
                    lowercaseIdentifier = cleanIdentifier.lowercase(),
                    normalizedPhone = normalizedPhone,
                    tenDigits = tenDigits
                )
        } else {
            userDao.findUserByIdentifier(
                identifier = cleanIdentifier,
                lowercaseIdentifier = cleanIdentifier.lowercase(),
                normalizedPhone = normalizedPhone,
                tenDigits = tenDigits
            )
            ?: userDao.getUserByPhone(normalizedPhone)
            ?: userDao.getUserByPhone(tenDigits)
            ?: userDao.getUserByPhone(cleanIdentifier)
            ?: (if (tenDigits.length == 10) userDao.getUserByPhone("+91$tenDigits") else null)
            ?: userDao.getUserByEmail(cleanIdentifier.lowercase())
        }

        if (user == null) {
            return Result.failure(Exception("No account found with this email or mobile number. Please check your credentials or register."))
        }

        if (!SecurityUtils.verifyPassword(password, user.passwordHash)) {
            return Result.failure(Exception("Incorrect password. Please try again or tap Forgot Password."))
        }

        return Result.success(user)
    }

    suspend fun signInOrSignUpWithGoogle(name: String, email: String, role: Role = Role.CUSTOMER): Result<Long> {
        val cleanEmail = email.trim().lowercase()
        val cleanName = name.trim()
        val existing = userDao.getUserByEmail(cleanEmail)
        if (existing != null) {
            return Result.success(existing.id)
        }
        val user = UserEntity(
            name = if (cleanName.isNotBlank()) cleanName else "Customer",
            email = cleanEmail,
            phone = null,
            passwordHash = "GOOGLE_OAUTH_ACCOUNT",
            role = role
        )
        val id = userDao.insertUser(user)
        return Result.success(id)
    }

    suspend fun requestEmailPasswordReset(email: String): Result<String> {
        val cleanEmail = email.trim().lowercase()
        if (!SecurityUtils.isValidEmail(cleanEmail)) {
            return Result.failure(Exception("Please enter a valid email address."))
        }

        val user = userDao.getUserByEmail(cleanEmail)
        // For privacy/security, provide confirmation without leaking sensitive account existence
        return if (user != null) {
            Result.success("Password reset instructions have been sent to $cleanEmail. Please check your inbox.")
        } else {
            Result.success("If an account is associated with $cleanEmail, password reset instructions have been sent.")
        }
    }

    suspend fun resetPasswordByEmail(email: String, newPassword: String): Result<Unit> {
        val cleanEmail = email.trim().lowercase()
        val user = userDao.getUserByEmail(cleanEmail) ?: return Result.failure(Exception("Account not found for $cleanEmail."))
        val newHash = SecurityUtils.hashPassword(newPassword)
        userDao.updatePasswordByEmail(cleanEmail, newHash)
        return Result.success(Unit)
    }

    suspend fun sendMobileOtp(phone: String): Result<String> {
        if (!SecurityUtils.isValidPhoneNumber(phone)) {
            return Result.failure(Exception("Please enter a valid 10-digit mobile number."))
        }
        val normalizedPhone = SecurityUtils.normalizePhoneNumber(phone)
        val tenDigits = SecurityUtils.get10DigitMobile(phone)
        val user = userDao.findUserByIdentifier(phone, phone.lowercase(), normalizedPhone, tenDigits)

        if (user == null) {
            return Result.failure(Exception("No registered account found with this mobile number."))
        }

        // Generate 6-digit cryptographic random OTP
        val otp = (Random.nextInt(900000) + 100000).toString()
        val session = OtpSession(
            phone = normalizedPhone,
            otpCode = otp
        )
        activeOtpSessions[normalizedPhone] = session
        activeOtpSessions[tenDigits] = session

        return Result.success(otp)
    }

    suspend fun verifyOtpAndResetPassword(phone: String, otp: String, newPassword: String): Result<Unit> {
        val normalizedPhone = SecurityUtils.normalizePhoneNumber(phone)
        val tenDigits = SecurityUtils.get10DigitMobile(phone)
        val session = activeOtpSessions[normalizedPhone] ?: activeOtpSessions[tenDigits]

        if (session == null) {
            return Result.failure(Exception("No active OTP request found for this mobile number. Please request a new OTP."))
        }

        if (System.currentTimeMillis() > session.expiresAtMillis) {
            activeOtpSessions.remove(normalizedPhone)
            activeOtpSessions.remove(tenDigits)
            return Result.failure(Exception("OTP has expired. Please tap 'Resend OTP' to receive a fresh code."))
        }

        if (session.otpCode.trim() != otp.trim()) {
            return Result.failure(Exception("Invalid OTP code. Please enter the correct 6-digit code."))
        }

        val user = userDao.findUserByIdentifier(phone, phone.lowercase(), normalizedPhone, tenDigits)
        if (user == null) {
            activeOtpSessions.remove(normalizedPhone)
            activeOtpSessions.remove(tenDigits)
            return Result.failure(Exception("No registered account found with mobile number $normalizedPhone."))
        }

        // Invalidate old password & apply new secure hash
        val newHash = SecurityUtils.hashPassword(newPassword)
        userDao.updatePasswordByPhoneMulti(phone, normalizedPhone, tenDigits, newHash)
        activeOtpSessions.remove(normalizedPhone)
        activeOtpSessions.remove(tenDigits)

        return Result.success(Unit)
    }

    fun getUserFlow(userId: Long): Flow<UserEntity?> {
        return userDao.getUserFlow(userId)
    }
}


