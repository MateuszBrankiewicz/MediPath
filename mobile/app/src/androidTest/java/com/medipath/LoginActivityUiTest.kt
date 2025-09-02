package com.medipath

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performTextInput
import com.medipath.ui.auth.LoginScreen
import com.medipath.ui.theme.MediPathTheme
import org.junit.Rule
import org.junit.Test

class LoginActivityUiTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun testIfAllFieldsAreDisplayed() {
        composeTestRule.setContent {
            MediPathTheme {
                LoginScreen()
            }
        }
        composeTestRule.onNodeWithTag("email_field").assertIsDisplayed()
        composeTestRule.onNodeWithTag("password_field").assertIsDisplayed()
    }

    @Test
    fun signInButtonDisabledWhenFormIncomplete() {
        composeTestRule.setContent {
            MediPathTheme {
                LoginScreen()
            }
        }
        composeTestRule.onNodeWithText("SIGN IN").assertIsNotEnabled()
    }

    @Test
    fun signInButtonEnabledWhenFormComplete() {
        composeTestRule.setContent {
            MediPathTheme {
                LoginScreen()
            }
        }
        composeTestRule.onNodeWithTag("email_field").performTextInput("jan@example.com")
        composeTestRule.onNodeWithTag("password_field").performTextInput("Password123!")
        composeTestRule.onNodeWithText("SIGN IN").assertIsEnabled()
    }
}








