package com.example

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import com.example.data.local.AppDatabase
import com.example.domain.repository.UserRepository
import com.example.ui.screens.auth.AuthScreen
import com.example.ui.screens.auth.AuthViewModel
import com.example.ui.theme.MyApplicationTheme
import com.example.util.SessionManager
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import android.content.Context

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class PasswordInputRobolectricTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    private lateinit var database: AppDatabase
    private lateinit var context: Context
    private lateinit var sessionManager: SessionManager
    private lateinit var userRepository: UserRepository
    private lateinit var viewModel: AuthViewModel

    @Before
    fun setup() {
        context = ApplicationProvider.getApplicationContext()
        database = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        sessionManager = SessionManager(context)
        userRepository = UserRepository(database.userDao())
        viewModel = AuthViewModel(userRepository, sessionManager)
    }

    @After
    fun tearDown() {
        database.close()
    }

    @Test
    fun testPasswordInputSingleCharacterTyping() {
        composeTestRule.setContent {
            MyApplicationTheme {
                AuthScreen(onAuthSuccess = {}, viewModel = viewModel)
            }
        }

        val passwordInput = composeTestRule.onNodeWithTag("password_input")
        passwordInput.assertIsDisplayed()

        // 1 tap of key 'a' -> masked as exactly 1 bullet
        passwordInput.performTextInput("a")
        passwordInput.assertTextContains("\u2022")

        // 1 tap of key 'b' -> masked as exactly 2 bullets
        passwordInput.performTextInput("b")
        passwordInput.assertTextContains("\u2022\u2022")

        // 1 tap of key 'c' -> masked as exactly 3 bullets
        passwordInput.performTextInput("c")
        passwordInput.assertTextContains("\u2022\u2022\u2022")
    }

    @Test
    fun testPasswordVisibilityStableToggle() {
        composeTestRule.setContent {
            MyApplicationTheme {
                AuthScreen(onAuthSuccess = {}, viewModel = viewModel)
            }
        }

        val passwordInput = composeTestRule.onNodeWithTag("password_input")
        val visibilityToggle = composeTestRule.onNodeWithTag("password_visibility_toggle")

        // Type password "secret123" (9 characters)
        passwordInput.performTextInput("secret123")
        passwordInput.assertTextContains("\u2022".repeat(9))

        // Initially content description is "Show password"
        composeTestRule.onNodeWithContentDescription("Show password").assertIsDisplayed()

        // First tap on eye icon: toggles to visible and stays visible
        visibilityToggle.performClick()
        composeTestRule.onNodeWithContentDescription("Hide password").assertIsDisplayed()
        passwordInput.assertTextContains("secret123")

        // Typing more characters keeps visibility state and appends exactly 1 char
        passwordInput.performTextInput("4")
        passwordInput.assertTextContains("secret1234")
        composeTestRule.onNodeWithContentDescription("Hide password").assertIsDisplayed()

        // Second tap on eye icon: toggles back to masked (hidden state)
        visibilityToggle.performClick()
        composeTestRule.onNodeWithContentDescription("Show password").assertIsDisplayed()
        passwordInput.assertTextContains("\u2022".repeat(10))

        // Typing more characters in hidden state
        passwordInput.performTextInput("5")
        passwordInput.assertTextContains("\u2022".repeat(11))
        composeTestRule.onNodeWithContentDescription("Show password").assertIsDisplayed()

        // Third tap on eye icon: toggles back to visible
        visibilityToggle.performClick()
        composeTestRule.onNodeWithContentDescription("Hide password").assertIsDisplayed()
        passwordInput.assertTextContains("secret12345")
    }
}
