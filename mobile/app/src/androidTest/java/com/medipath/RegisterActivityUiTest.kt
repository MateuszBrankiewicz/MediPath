package com.medipath

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.hasClickAction
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.onRoot
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.test.performScrollToIndex
import androidx.compose.ui.test.performTextInput
import com.medipath.ui.auth.RegisterScreen
import com.medipath.ui.theme.MediPathTheme
import org.junit.Rule
import org.junit.Test

class RegisterActivityUiTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun testIfAllFieldsAreDisplayed() {
        composeTestRule.setContent {
            MediPathTheme {
                RegisterScreen()
            }
        }
        composeTestRule.onNodeWithText("Name").assertIsDisplayed()
        composeTestRule.onNodeWithText("Surname").assertIsDisplayed()
        composeTestRule.onNodeWithText("Government ID").assertIsDisplayed()
        composeTestRule.onNodeWithText("Birth Date").assertIsDisplayed()
        composeTestRule.onNodeWithText("Province").assertIsDisplayed()
        composeTestRule.onNodeWithText("Postal Code").assertIsDisplayed()
        composeTestRule.onNodeWithText("City").assertIsDisplayed()
        composeTestRule.onNodeWithTag("confirm_password_field").performScrollTo()
        composeTestRule.onNodeWithText("Number").assertIsDisplayed()
        composeTestRule.onNodeWithText("Street").assertIsDisplayed()
        composeTestRule.onNodeWithTag("phone_number").assertIsDisplayed()
        composeTestRule.onNodeWithTag("email_field").assertIsDisplayed()
        composeTestRule.onNodeWithTag("password_field").assertIsDisplayed()
        composeTestRule.onNodeWithTag("confirm_password_field").assertIsDisplayed()
    }

    @Test
    fun signUpButtonDisabledWhenFormIncomplete() {
        composeTestRule.setContent {
            MediPathTheme {
                RegisterScreen()
            }
        }
        composeTestRule.onNodeWithText("SIGN UP").assertIsNotEnabled()
    }

    @Test
    fun signUpButtonEnabledWhenFormComplete() {
        composeTestRule.setContent {
            MediPathTheme {
                RegisterScreen()
            }
        }
        composeTestRule.onNodeWithText("Name").performTextInput("Jan")
        composeTestRule.onNodeWithText("Surname").performTextInput("Kowalski")
        composeTestRule.onNodeWithText("Government ID").performTextInput("12345678901")
        composeTestRule.onNodeWithText("Birth Date").performTextInput("01-01-1990")

        composeTestRule.onNodeWithText("Province").performClick()
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithText("Lubelskie").performClick()
        composeTestRule.waitForIdle()

        composeTestRule.onNodeWithText("Postal Code").performTextInput("00-001")

        composeTestRule.onNodeWithText("City").performClick()
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithText("Lublin").performClick()
        composeTestRule.waitForIdle()

        composeTestRule.onNodeWithText("Number").performTextInput("1")
        composeTestRule.onNodeWithText("Street").performTextInput("Główna")
        composeTestRule.onNodeWithTag("phone_number").performTextInput("123456789")
        composeTestRule.onNodeWithTag("email_field").performTextInput("jan@example.com")
        composeTestRule.onNodeWithTag("password_field").performTextInput("Password123!")
        composeTestRule.onNodeWithTag("confirm_password_field").performTextInput("Password123!")

        composeTestRule.onNodeWithTag("conditions_checkbox").performScrollTo()
        composeTestRule.onNodeWithTag("conditions_checkbox").performClick()
        composeTestRule.waitForIdle()

        composeTestRule.onNodeWithText("SIGN UP").assertIsEnabled()
    }
}








