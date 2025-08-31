package com.purespace.app.ui.screens.auth

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.purespace.app.ui.theme.PureSpaceTheme
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class AuthScreenTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun authScreen_displaysWelcomeMessage() {
        composeTestRule.setContent {
            PureSpaceTheme {
                AuthScreen(
                    onAuthSuccess = {}
                )
            }
        }

        composeTestRule
            .onNodeWithText("Welcome to PureSpace")
            .assertIsDisplayed()
    }

    @Test
    fun authScreen_displaysGoogleSignInButton() {
        composeTestRule.setContent {
            PureSpaceTheme {
                AuthScreen(
                    onAuthSuccess = {}
                )
            }
        }

        composeTestRule
            .onNodeWithText("Continue with Google")
            .assertIsDisplayed()
            .assertHasClickAction()
    }

    @Test
    fun authScreen_displaysFeatures() {
        composeTestRule.setContent {
            PureSpaceTheme {
                AuthScreen(
                    onAuthSuccess = {}
                )
            }
        }

        // Check that key features are displayed
        composeTestRule
            .onNodeWithText("Smart Duplicate Detection")
            .assertIsDisplayed()

        composeTestRule
            .onNodeWithText("Storage Analytics")
            .assertIsDisplayed()

        composeTestRule
            .onNodeWithText("Cloud Sync")
            .assertIsDisplayed()

        composeTestRule
            .onNodeWithText("Privacy First")
            .assertIsDisplayed()
    }

    @Test
    fun authScreen_displaysPrivacyNotice() {
        composeTestRule.setContent {
            PureSpaceTheme {
                AuthScreen(
                    onAuthSuccess = {}
                )
            }
        }

        composeTestRule
            .onNodeWithText("Secure authentication with Google")
            .assertIsDisplayed()
    }
}
