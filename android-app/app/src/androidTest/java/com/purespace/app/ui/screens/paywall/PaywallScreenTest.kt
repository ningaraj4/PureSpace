package com.purespace.app.ui.screens.paywall

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.purespace.app.billing.PremiumProduct
import com.purespace.app.billing.PremiumProductType
import com.purespace.app.ui.theme.PureSpaceTheme
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class PaywallScreenTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    private val mockProducts = listOf(
        PremiumProduct(
            productId = "premium_monthly",
            title = "Premium Monthly",
            description = "Monthly subscription",
            price = "$4.99/month",
            productType = PremiumProductType.SUBSCRIPTION
        ),
        PremiumProduct(
            productId = "premium_yearly",
            title = "Premium Yearly",
            description = "Yearly subscription",
            price = "$39.99/year",
            productType = PremiumProductType.SUBSCRIPTION
        )
    )

    @Test
    fun paywallScreen_displaysTitle() {
        composeTestRule.setContent {
            PureSpaceTheme {
                PaywallScreen(
                    onDismiss = {},
                    onPurchaseSuccess = {}
                )
            }
        }

        composeTestRule
            .onNodeWithText("Upgrade to Premium")
            .assertIsDisplayed()
    }

    @Test
    fun paywallScreen_displaysCloseButton() {
        composeTestRule.setContent {
            PureSpaceTheme {
                PaywallScreen(
                    onDismiss = {},
                    onPurchaseSuccess = {}
                )
            }
        }

        composeTestRule
            .onNodeWithContentDescription("Close")
            .assertIsDisplayed()
            .assertHasClickAction()
    }

    @Test
    fun paywallScreen_displaysPremiumFeatures() {
        composeTestRule.setContent {
            PureSpaceTheme {
                PaywallScreen(
                    onDismiss = {},
                    onPurchaseSuccess = {}
                )
            }
        }

        // Check that key premium features are displayed
        composeTestRule
            .onNodeWithText("Unlimited Duplicate Detection")
            .assertIsDisplayed()

        composeTestRule
            .onNodeWithText("Advanced Detection Algorithms")
            .assertIsDisplayed()

        composeTestRule
            .onNodeWithText("Multi-Device Cloud Sync")
            .assertIsDisplayed()
    }

    @Test
    fun paywallScreen_displaysPurchaseButton() {
        composeTestRule.setContent {
            PureSpaceTheme {
                PaywallScreen(
                    onDismiss = {},
                    onPurchaseSuccess = {}
                )
            }
        }

        composeTestRule
            .onNodeWithText("Start Premium")
            .assertIsDisplayed()
    }

    @Test
    fun paywallScreen_displaysBenefits() {
        composeTestRule.setContent {
            PureSpaceTheme {
                PaywallScreen(
                    onDismiss = {},
                    onPurchaseSuccess = {}
                )
            }
        }

        composeTestRule
            .onNodeWithText("• Cancel anytime\n• 7-day free trial\n• Secure payment")
            .assertIsDisplayed()
    }
}
