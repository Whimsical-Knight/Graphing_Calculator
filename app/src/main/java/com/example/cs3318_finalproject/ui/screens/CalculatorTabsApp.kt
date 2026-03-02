package com.example.cs3318_finalproject.ui.screens

import androidx.compose.runtime.*
import androidx.compose.material3.*
import androidx.compose.foundation.layout.*

@Composable
fun CalculatorTabsApp() {
    // track which tab is selected
    var selectedTab by remember { mutableStateOf(0) }
    val tabs = listOf("Graphing", "Scientific") // tab labels
    // defines the horizontal tabs bar
    Column {
        TabRow(selectedTabIndex = selectedTab) {
            tabs.forEachIndexed { index, title ->
                Tab(
                    selected = selectedTab == index,
                    onClick = { selectedTab = index },
                    text = { Text(title) }
                )
            }
        }
        // condition for, when selected tab changes, compose reloads
        when (selectedTab) {
            0 -> GraphCalculator()
            1 -> ScientificCalculator()
        }
    }
}