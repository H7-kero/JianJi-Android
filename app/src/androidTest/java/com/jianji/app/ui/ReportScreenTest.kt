package com.jianji.app.ui

import androidx.compose.runtime.Composable
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import com.jianji.app.ui.report.ReportScreen
import org.junit.Rule
import org.junit.Test

class ReportScreenTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun reportScreen_displaysEmptyState() {
        composeTestRule.setContent {
            ReportScreen()
        }

        composeTestRule.onNodeWithText("开发中").assertIsDisplayed()
    }
}
