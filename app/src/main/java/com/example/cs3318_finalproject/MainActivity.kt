package com.example.cs3318_finalproject

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import com.example.cs3318_finalproject.ui.screens.CalculatorTabsApp

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // starts Jetpack compose UI
        setContent {
            MaterialTheme { // apply theme styling
                Surface {
                    CalculatorTabsApp() // load my two-tab interface
                }
            }
        }
    }
}