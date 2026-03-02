package com.example.cs3318_finalproject.ui.screens

import androidx.compose.runtime.*
import androidx.compose.material3.*
import androidx.compose.foundation.layout.*
import com.example.cs3318_finalproject.ui.components.CalculatorButtons
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.cs3318_finalproject.viewmodel.CalculatorViewModel

@Composable
fun ScientificCalculator( // take viewModel parameter
    viewModel: CalculatorViewModel = androidx.lifecycle.viewmodel.compose.viewModel()
) {
    // whenever expression and result are changed in ViewModel, update the UI
    // makes it so we won't lose data when device is rotated
    val expression by viewModel.expression.collectAsState()
    val result by viewModel.result.collectAsState()

    // arranges children vertically
    Column(Modifier.padding(16.dp)) { // set title and theme
        Text("Scientific Calculator", style = MaterialTheme.typography.headlineSmall)
        Spacer(Modifier.height(12.dp)) // added spacing
        OutlinedTextField(
            value = expression, // link field to viewmodel state
            onValueChange = { }, // change if I want to allow for typing input
            label = { Text("Expression") },
            modifier = Modifier.fillMaxWidth() // stretch to fill screen
        )
        Spacer(Modifier.height(12.dp))
        // button conditions for viewModel updating
        CalculatorButtons(
            onPress = { viewModel.onInput(it) },
            onClear = { viewModel.onClear() },
            onEquals = { viewModel.onEquals() }
        )
        Spacer(Modifier.height(12.dp))
        Text("Result: $result")
    }
}