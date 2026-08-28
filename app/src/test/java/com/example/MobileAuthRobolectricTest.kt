package com.example

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.example.data.local.AppDatabase
import com.example.data.local.entity.CartItemEntity
import com.example.data.local.entity.FavoriteEntity
import com.example.data.local.entity.OrderEntity
import com.example.domain.model.Role
import com.example.domain.repository.UserRepository
import com.example.util.SecurityUtils
import com.example.util.SessionManager
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class MobileAuthRobolectricTest {

    private lateinit var database: AppDatabase
    private lateinit var context: Context
    private lateinit var sessionManager: SessionManager
    private lateinit var userRepository: UserRepository

    @Before
    fun setup() {
        context = ApplicationProvider.getApplicationContext()
        database = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        sessionManager = SessionManager(context)
        userRepository = UserRepository(database.userDao())
    }

    @After
    fun tearDown() {
        database.close()
    }

    @Test
    fun testMobileRegisterLogoutAndLoginFlow() = runBlocking {
        val testPhone = "9876543210"
        val testPassword = "securePassword123"
        val testName = "Aman Kumar"

        // 1. Sign up with mobile number
        val signupResult = userRepository.signupWithPhone(testName, testPhone, testPassword, Role.CUSTOMER)
        assertTrue("Signup should succeed", signupResult.isSuccess)
        val userId = signupResult.getOrThrow()
        assertTrue("User ID should be > 0", userId > 0)

        // Save session on signup
        sessionManager.saveUserId(userId)
        assertEquals(userId, sessionManager.currentUserId.first())

        // Add some customer data (cart, wishlist, order) linked to this userId
        database.cartDao().insertCartItem(
            CartItemEntity(
                userId = userId,
                productId = 101L,
                quantity = 2,
                selectedSize = "Standard",
                selectedColor = "Default"
            )
        )
        database.favoriteDao().insertFavorite(FavoriteEntity(userId = userId, productId = 202L))
        database.orderDao().insertOrder(
            OrderEntity(
                id = 0,
                orderNumber = "ORD-TEST-001",
                userId = userId,
                shopId = 1L,
                totalAmount = 599.0,
                deliveryAddress = "Main Market, Bisalpur",
                status = "Placed",
                paymentMethod = "Cash on Delivery"
            )
        )

        // Verify data exists for this user
        val cartItemsBefore = database.cartDao().getCartItems(userId).first()
        assertEquals(1, cartItemsBefore.size)
        val favoritesBefore = database.favoriteDao().getFavoriteProductIds(userId).first()
        assertEquals(listOf(202L), favoritesBefore)
        val ordersBefore = database.orderDao().getOrdersForUser(userId).first()
        assertEquals(1, ordersBefore.size)

        // 2. Logout - clears session without deleting account or user data
        sessionManager.clearSession()
        assertNull(sessionManager.currentUserId.first())

        // 3. Login again using 10-digit format
        val login10DigitResult = userRepository.login(testPhone, testPassword)
        assertTrue("Login with 10-digit phone should succeed", login10DigitResult.isSuccess)
        val loggedInUser = login10DigitResult.getOrThrow()
        assertEquals(userId, loggedInUser.id)
        assertEquals(testName, loggedInUser.name)

        // 4. Login again using +91 format
        val loginPlus91Result = userRepository.login("+91$testPhone", testPassword)
        assertTrue("Login with +91 phone should succeed", loginPlus91Result.isSuccess)
        assertEquals(userId, loginPlus91Result.getOrThrow().id)

        // 5. Login again with 91 prefix without plus
        val login91Result = userRepository.login("91$testPhone", testPassword)
        assertTrue("Login with 91 prefix should succeed", login91Result.isSuccess)
        assertEquals(userId, login91Result.getOrThrow().id)

        // 6. Login again with leading zero
        val loginZeroResult = userRepository.login("0$testPhone", testPassword)
        assertTrue("Login with leading 0 should succeed", loginZeroResult.isSuccess)
        assertEquals(userId, loginZeroResult.getOrThrow().id)

        // 7. Verify restore of user data after login
        sessionManager.saveUserId(loggedInUser.id)
        assertEquals(userId, sessionManager.currentUserId.first())

        val cartItemsAfter = database.cartDao().getCartItems(userId).first()
        assertEquals(1, cartItemsAfter.size)
        assertEquals(101L, cartItemsAfter[0].productId)
        assertEquals(2, cartItemsAfter[0].quantity)

        val favoritesAfter = database.favoriteDao().getFavoriteProductIds(userId).first()
        assertEquals(listOf(202L), favoritesAfter)

        val ordersAfter = database.orderDao().getOrdersForUser(userId).first()
        assertEquals(1, ordersAfter.size)
        assertEquals(599.0, ordersAfter[0].totalAmount, 0.01)
    }

    @Test
    fun testEmailRegisterLogoutAndLoginFlow() = runBlocking {
        val testEmail = "testuser@bisalpurhub.com"
        val testPassword = "mypassword123"
        val testName = "Test User"

        // 1. Sign up with email
        val signupResult = userRepository.signup(testName, testEmail, testPassword, Role.CUSTOMER)
        assertTrue("Email signup should succeed", signupResult.isSuccess)
        val userId = signupResult.getOrThrow()

        // 2. Logout
        sessionManager.clearSession()
        assertNull(sessionManager.currentUserId.first())

        // 3. Login with exact email
        val loginResult = userRepository.login(testEmail, testPassword)
        assertTrue("Email login should succeed", loginResult.isSuccess)
        assertEquals(userId, loginResult.getOrThrow().id)

        // 4. Login with uppercase/mixed case email
        val loginMixedCaseResult = userRepository.login("TestUser@BisalpurHub.com", testPassword)
        assertTrue("Email login case-insensitive should succeed", loginMixedCaseResult.isSuccess)
        assertEquals(userId, loginMixedCaseResult.getOrThrow().id)
    }

    @Test
    fun testDuplicateMobileSignUpPreventedAndLoginSucceeds() = runBlocking {
        val testPhone = "9876543210"
        val testPassword = "securePassword123"
        val testName = "Aman Kumar"

        // 1. Initial registration
        val firstSignup = userRepository.signupWithPhone(testName, testPhone, testPassword, Role.CUSTOMER)
        assertTrue(firstSignup.isSuccess)
        val originalUserId = firstSignup.getOrThrow()

        // 2. Attempt duplicate signup with raw 10-digit phone
        val duplicateSignup1 = userRepository.signupWithPhone("Another Name", testPhone, "otherpass", Role.CUSTOMER)
        assertTrue("Duplicate signup with 10 digits must fail", duplicateSignup1.isFailure)
        assertEquals(
            "An account with this mobile number already exists. Please login instead.",
            duplicateSignup1.exceptionOrNull()?.message
        )

        // 3. Attempt duplicate signup with +91 format
        val duplicateSignup2 = userRepository.signupWithPhone("Another Name", "+91$testPhone", "otherpass", Role.CUSTOMER)
        assertTrue("Duplicate signup with +91 must fail", duplicateSignup2.isFailure)
        assertEquals(
            "An account with this mobile number already exists. Please login instead.",
            duplicateSignup2.exceptionOrNull()?.message
        )

        // 4. Attempt duplicate signup with spaces
        val duplicateSignup3 = userRepository.signupWithPhone("Another Name", "+91 98765 43210", "otherpass", Role.CUSTOMER)
        assertTrue("Duplicate signup with formatted phone must fail", duplicateSignup3.isFailure)
        assertEquals(
            "An account with this mobile number already exists. Please login instead.",
            duplicateSignup3.exceptionOrNull()?.message
        )

        // 5. Verify Login recognizes the existing account across all phone formats
        val loginRaw = userRepository.login(testPhone, testPassword)
        assertTrue("Login with 10-digit must find existing account", loginRaw.isSuccess)
        assertEquals(originalUserId, loginRaw.getOrThrow().id)

        val loginFormattedWithPlus = userRepository.login("+91 $testPhone", testPassword)
        assertTrue("Login with +91 and space must find existing account", loginFormattedWithPlus.isSuccess)
        assertEquals(originalUserId, loginFormattedWithPlus.getOrThrow().id)

        val loginSpaced = userRepository.login("98765 43210", testPassword)
        assertTrue("Login with spaced phone must find existing account", loginSpaced.isSuccess)
        assertEquals(originalUserId, loginSpaced.getOrThrow().id)

        // 6. Test incorrect password on existing mobile account
        val loginWrongPass = userRepository.login(testPhone, "wrongPassword")
        assertTrue("Login with wrong password must fail", loginWrongPass.isFailure)
        assertEquals(
            "Incorrect password. Please try again or tap Forgot Password.",
            loginWrongPass.exceptionOrNull()?.message
        )

        // 7. Test non-existent mobile account
        val loginNonExistent = userRepository.login("9123456789", testPassword)
        assertTrue("Login with non-existent phone must fail", loginNonExistent.isFailure)
        assertEquals(
            "No account found with this email or mobile number. Please check your credentials or register.",
            loginNonExistent.exceptionOrNull()?.message
        )
    }
}
