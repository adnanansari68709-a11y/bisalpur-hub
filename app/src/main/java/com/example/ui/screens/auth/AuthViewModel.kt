package com.example.ui.screens.auth

import android.content.Context
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import androidx.credentials.exceptions.GetCredentialCancellationException
import androidx.credentials.exceptions.GetCredentialException
import androidx.credentials.exceptions.NoCredentialException
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.BuildConfig
import com.example.domain.model.Role
import com.example.domain.repository.ShopRepository
import com.example.domain.repository.UserRepository
import com.example.util.SecurityUtils
import com.example.util.SessionManager
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.android.libraries.identity.googleid.GoogleIdTokenParsingException
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.security.MessageDigest
import java.util.UUID

enum class AuthTab {
    EMAIL,
    MOBILE
}

enum class RecoveryMethod {
    EMAIL,
    MOBILE
}

enum class RecoveryStep {
    INPUT_IDENTIFIER,
    VERIFY_OTP,
    SET_NEW_PASSWORD,
    SUCCESS
}

data class AuthUiState(
    val isLogin: Boolean = true,
    val selectedTab: AuthTab = AuthTab.EMAIL,
    val isLoading: Boolean = false,
    val error: String? = null,
    val success: Boolean = false,
    // Forgot Password & Recovery State
    val showForgotPasswordModal: Boolean = false,
    val recoveryMethod: RecoveryMethod = RecoveryMethod.EMAIL,
    val recoveryStep: RecoveryStep = RecoveryStep.INPUT_IDENTIFIER,
    val recoveryLoading: Boolean = false,
    val recoveryError: String? = null,
    val recoverySuccessMessage: String? = null,
    val activeRecoveryTarget: String = "",
    val generatedOtpHint: String? = null,
    val otpResendCountdown: Int = 0
)

class AuthViewModel(
    private val userRepository: UserRepository,
    private val sessionManager: SessionManager,
    private val shopRepository: ShopRepository? = null
) : ViewModel() {

    private val _uiState = MutableStateFlow(AuthUiState())
    val uiState: StateFlow<AuthUiState> = _uiState

    private var countdownJob: Job? = null

    fun setAuthTab(tab: AuthTab) {
        _uiState.value = _uiState.value.copy(
            selectedTab = tab,
            error = null
        )
    }

    fun toggleMode() {
        _uiState.value = _uiState.value.copy(
            isLogin = !_uiState.value.isLogin,
            error = null
        )
    }

    fun clearError() {
        if (_uiState.value.error != null) {
            _uiState.value = _uiState.value.copy(error = null)
        }
    }

    // --- Sign In & Sign Up ---
    fun submit(
        name: String,
        email: String,
        phone: String,
        password: String,
        role: Role,
        shopName: String = "",
        shopDescription: String = "",
        shopAddress: String = "",
        shopCity: String = "Bisalpur",
        shopPincode: String = "262201"
    ) {
        val tab = _uiState.value.selectedTab
        val isLogin = _uiState.value.isLogin
        val trimmedName = name.trim()
        val trimmedEmail = email.trim()
        val trimmedPhone = phone.trim()
        val trimmedPassword = password.trim()
        val trimmedShopName = shopName.trim()
        val trimmedShopDesc = shopDescription.trim()
        val trimmedShopAddress = shopAddress.trim()
        val trimmedShopCity = shopCity.trim().ifBlank { "Bisalpur" }
        val trimmedShopPincode = shopPincode.trim().ifBlank { "262201" }

        if (!isLogin && trimmedName.isBlank()) {
            _uiState.value = _uiState.value.copy(error = "Please enter your full name")
            return
        }

        val identifier = if (isLogin) {
            val primary = if (tab == AuthTab.EMAIL) trimmedEmail else trimmedPhone
            if (primary.isNotBlank()) primary else if (trimmedEmail.isNotBlank()) trimmedEmail else trimmedPhone
        } else {
            if (tab == AuthTab.EMAIL) trimmedEmail else trimmedPhone
        }

        if (isLogin) {
            if (identifier.isBlank()) {
                val errorMsg = if (tab == AuthTab.EMAIL) "Please enter your email address or mobile number" else "Please enter your 10-digit mobile number"
                _uiState.value = _uiState.value.copy(error = errorMsg)
                return
            }
        } else {
            // Validation for Email tab or Mobile tab during Sign Up
            if (tab == AuthTab.EMAIL) {
                if (trimmedEmail.isBlank()) {
                    _uiState.value = _uiState.value.copy(error = "Please enter your email address")
                    return
                }
                if (!SecurityUtils.isValidEmail(trimmedEmail)) {
                    _uiState.value = _uiState.value.copy(error = "Please enter a valid email address (e.g. name@example.com)")
                    return
                }
            } else {
                // Mobile Tab
                if (trimmedPhone.isBlank()) {
                    _uiState.value = _uiState.value.copy(error = "Please enter your 10-digit mobile number")
                    return
                }
                if (!SecurityUtils.isValidPhoneNumber(trimmedPhone)) {
                    _uiState.value = _uiState.value.copy(error = "Please enter a valid 10-digit Indian mobile number")
                    return
                }
            }

            // Seller specifics validation
            if (role == Role.SELLER) {
                if (trimmedShopName.isBlank()) {
                    _uiState.value = _uiState.value.copy(error = "Please enter your Shop or Store name")
                    return
                }
                if (trimmedShopAddress.isBlank()) {
                    _uiState.value = _uiState.value.copy(error = "Please enter your Shop Address in Bisalpur")
                    return
                }
                if (trimmedShopPincode.length != 6 || !trimmedShopPincode.all { it.isDigit() }) {
                    _uiState.value = _uiState.value.copy(error = "Please enter a valid 6-digit PIN code (e.g. 262201)")
                    return
                }
            }
        }

        if (trimmedPassword.isBlank()) {
            _uiState.value = _uiState.value.copy(error = "Please enter your password")
            return
        }

        if (trimmedPassword.length < 6) {
            _uiState.value = _uiState.value.copy(error = "Password must be at least 6 characters long")
            return
        }

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)

            val result = if (isLogin) {
                userRepository.login(identifier, trimmedPassword).map { it.id }
            } else {
                if (tab == AuthTab.EMAIL) {
                    userRepository.signup(trimmedName, trimmedEmail, trimmedPassword, role)
                } else {
                    userRepository.signupWithPhone(trimmedName, trimmedPhone, trimmedPassword, role)
                }
            }

            result.onSuccess { userId ->
                if (!isLogin && role == Role.SELLER && shopRepository != null) {
                    shopRepository.createShopForSeller(
                        sellerId = userId,
                        shopName = trimmedShopName,
                        shopDescription = trimmedShopDesc.ifBlank { "Bisalpur Local Store" },
                        address = trimmedShopAddress,
                        city = trimmedShopCity,
                        pincode = trimmedShopPincode
                    )
                }
                sessionManager.saveUserId(userId)
                _uiState.value = _uiState.value.copy(isLoading = false, success = true)
            }.onFailure {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = it.message ?: "Authentication failed. Please check your details."
                )
            }
        }
    }

    // --- Forgot Password / Account Recovery ---
    fun openForgotPasswordModal() {
        _uiState.value = _uiState.value.copy(
            showForgotPasswordModal = true,
            recoveryMethod = if (_uiState.value.selectedTab == AuthTab.MOBILE) RecoveryMethod.MOBILE else RecoveryMethod.EMAIL,
            recoveryStep = RecoveryStep.INPUT_IDENTIFIER,
            recoveryError = null,
            recoverySuccessMessage = null,
            generatedOtpHint = null,
            otpResendCountdown = 0
        )
    }

    fun closeForgotPasswordModal() {
        countdownJob?.cancel()
        _uiState.value = _uiState.value.copy(
            showForgotPasswordModal = false,
            recoveryError = null,
            recoverySuccessMessage = null,
            generatedOtpHint = null
        )
    }

    fun setRecoveryMethod(method: RecoveryMethod) {
        countdownJob?.cancel()
        _uiState.value = _uiState.value.copy(
            recoveryMethod = method,
            recoveryStep = RecoveryStep.INPUT_IDENTIFIER,
            recoveryError = null,
            recoverySuccessMessage = null,
            generatedOtpHint = null,
            otpResendCountdown = 0
        )
    }

    fun clearRecoveryError() {
        if (_uiState.value.recoveryError != null) {
            _uiState.value = _uiState.value.copy(recoveryError = null)
        }
    }

    fun requestEmailRecovery(email: String) {
        val cleanEmail = email.trim()
        if (cleanEmail.isBlank()) {
            _uiState.value = _uiState.value.copy(recoveryError = "Please enter your registered email address")
            return
        }
        if (!SecurityUtils.isValidEmail(cleanEmail)) {
            _uiState.value = _uiState.value.copy(recoveryError = "Please enter a valid email address")
            return
        }

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(recoveryLoading = true, recoveryError = null)
            val res = userRepository.requestEmailPasswordReset(cleanEmail)
            res.onSuccess { msg ->
                _uiState.value = _uiState.value.copy(
                    recoveryLoading = false,
                    activeRecoveryTarget = cleanEmail,
                    recoveryStep = RecoveryStep.SET_NEW_PASSWORD,
                    recoverySuccessMessage = msg
                )
            }.onFailure { ex ->
                _uiState.value = _uiState.value.copy(
                    recoveryLoading = false,
                    recoveryError = ex.message ?: "Failed to process recovery request."
                )
            }
        }
    }

    fun requestMobileOtp(phone: String) {
        val cleanPhone = phone.trim()
        if (cleanPhone.isBlank()) {
            _uiState.value = _uiState.value.copy(recoveryError = "Please enter your registered mobile number")
            return
        }
        if (!SecurityUtils.isValidPhoneNumber(cleanPhone)) {
            _uiState.value = _uiState.value.copy(recoveryError = "Please enter a valid 10-digit Indian mobile number")
            return
        }

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(recoveryLoading = true, recoveryError = null)
            val res = userRepository.sendMobileOtp(cleanPhone)
            res.onSuccess { otpCode ->
                _uiState.value = _uiState.value.copy(
                    recoveryLoading = false,
                    activeRecoveryTarget = SecurityUtils.normalizePhoneNumber(cleanPhone),
                    recoveryStep = RecoveryStep.VERIFY_OTP,
                    generatedOtpHint = otpCode,
                    recoverySuccessMessage = "A 6-digit verification code has been dispatched to ${SecurityUtils.normalizePhoneNumber(cleanPhone)}."
                )
                startOtpCountdown()
            }.onFailure { ex ->
                _uiState.value = _uiState.value.copy(
                    recoveryLoading = false,
                    recoveryError = ex.message ?: "Failed to send OTP. Please try again."
                )
            }
        }
    }

    fun resendOtp() {
        val target = _uiState.value.activeRecoveryTarget
        if (target.isBlank() || _uiState.value.otpResendCountdown > 0) return

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(recoveryLoading = true, recoveryError = null)
            val res = userRepository.sendMobileOtp(target)
            res.onSuccess { otpCode ->
                _uiState.value = _uiState.value.copy(
                    recoveryLoading = false,
                    generatedOtpHint = otpCode,
                    recoverySuccessMessage = "Fresh OTP sent to $target"
                )
                startOtpCountdown()
            }.onFailure { ex ->
                _uiState.value = _uiState.value.copy(
                    recoveryLoading = false,
                    recoveryError = ex.message ?: "Could not resend OTP."
                )
            }
        }
    }

    private fun startOtpCountdown() {
        countdownJob?.cancel()
        countdownJob = viewModelScope.launch {
            _uiState.value = _uiState.value.copy(otpResendCountdown = 45)
            for (sec in 44 downTo 0) {
                delay(1000)
                _uiState.value = _uiState.value.copy(otpResendCountdown = sec)
            }
        }
    }

    fun verifyOtp(enteredOtp: String) {
        val cleanOtp = enteredOtp.trim()
        if (cleanOtp.length != 6) {
            _uiState.value = _uiState.value.copy(recoveryError = "Please enter the complete 6-digit OTP code")
            return
        }

        val hint = _uiState.value.generatedOtpHint
        if (hint != null && hint != cleanOtp) {
            _uiState.value = _uiState.value.copy(recoveryError = "Invalid OTP code. Please enter the 6-digit code received.")
            return
        }

        _uiState.value = _uiState.value.copy(
            recoveryStep = RecoveryStep.SET_NEW_PASSWORD,
            recoveryError = null,
            recoverySuccessMessage = "OTP verified successfully! Please choose a new secure password."
        )
    }

    fun completePasswordReset(newPassword: String, confirmPassword: String) {
        val cleanPass = newPassword.trim()
        val cleanConfirm = confirmPassword.trim()

        if (cleanPass.length < 6) {
            _uiState.value = _uiState.value.copy(recoveryError = "New password must be at least 6 characters long")
            return
        }
        if (cleanPass != cleanConfirm) {
            _uiState.value = _uiState.value.copy(recoveryError = "Passwords do not match. Please re-enter carefully.")
            return
        }

        val target = _uiState.value.activeRecoveryTarget
        val method = _uiState.value.recoveryMethod

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(recoveryLoading = true, recoveryError = null)

            val result = if (method == RecoveryMethod.EMAIL) {
                userRepository.resetPasswordByEmail(target, cleanPass)
            } else {
                val otpHint = _uiState.value.generatedOtpHint ?: ""
                userRepository.verifyOtpAndResetPassword(target, otpHint, cleanPass)
            }

            result.onSuccess {
                _uiState.value = _uiState.value.copy(
                    recoveryLoading = false,
                    recoveryStep = RecoveryStep.SUCCESS,
                    recoverySuccessMessage = "Your password has been successfully updated! You can now log in with your new credentials."
                )
            }.onFailure { ex ->
                _uiState.value = _uiState.value.copy(
                    recoveryLoading = false,
                    recoveryError = ex.message ?: "Failed to reset password. Please try again."
                )
            }
        }
    }

    // --- Google Sign-In ---
    fun continueWithGoogle(context: Context) {
        val serverClientId = BuildConfig.GOOGLE_WEB_CLIENT_ID.trim()
        if (serverClientId.isBlank() || serverClientId == "NOT_CONFIGURED") {
            _uiState.value = _uiState.value.copy(
                error = "Google Sign-In is ready! Please configure GOOGLE_WEB_CLIENT_ID in the Secrets panel or .env file to enable Google authentication."
            )
            return
        }

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            try {
                val rawNonce = UUID.randomUUID().toString()
                val md = MessageDigest.getInstance("SHA-256")
                val digest = md.digest(rawNonce.toByteArray())
                val hashedNonce = digest.fold("") { str, it -> str + "%02x".format(it) }

                val googleIdOption = GetGoogleIdOption.Builder()
                    .setFilterByAuthorizedAccounts(false)
                    .setServerClientId(serverClientId)
                    .setAutoSelectEnabled(false)
                    .setNonce(hashedNonce)
                    .build()

                val request = GetCredentialRequest.Builder()
                    .addCredentialOption(googleIdOption)
                    .build()

                val credentialManager = CredentialManager.create(context)
                val result = credentialManager.getCredential(
                    request = request,
                    context = context
                )

                val credential = result.credential
                if (credential is CustomCredential && credential.type == GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL) {
                    val googleIdTokenCredential = GoogleIdTokenCredential.createFrom(credential.data)
                    val email = googleIdTokenCredential.id
                    val displayName = googleIdTokenCredential.displayName 
                        ?: googleIdTokenCredential.givenName 
                        ?: "Customer"

                    val authResult = userRepository.signInOrSignUpWithGoogle(
                        name = displayName,
                        email = email,
                        role = Role.CUSTOMER
                    )

                    authResult.onSuccess { userId ->
                        sessionManager.saveUserId(userId)
                        _uiState.value = _uiState.value.copy(isLoading = false, success = true)
                    }.onFailure { ex ->
                        _uiState.value = _uiState.value.copy(
                            isLoading = false,
                            error = ex.message ?: "Failed to sign in with Google."
                        )
                    }
                } else {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = "Unexpected credential received from Google."
                    )
                }
            } catch (e: GetCredentialCancellationException) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = null
                )
            } catch (e: NoCredentialException) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = "No Google accounts available on this device."
                )
            } catch (e: GoogleIdTokenParsingException) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = "Invalid Google authentication token."
                )
            } catch (e: GetCredentialException) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = "Google Sign-In could not be completed: ${e.message ?: "Please try again."}"
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = "Sign in with Google error: ${e.message ?: "Please try again."}"
                )
            }
        }
    }
}
