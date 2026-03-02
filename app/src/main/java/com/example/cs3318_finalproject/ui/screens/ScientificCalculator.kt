package com.example.cs3318_finalproject.ui.screens

import androidx.compose.runtime.*
import androidx.compose.material3.*
import androidx.compose.foundation.layout.*
import com.example.cs3318_finalproject.ui.components.CalculatorButtons
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.cs3318_finalproject.viewmodel.CalculatorViewModel
import androidx.lifecycle.viewmodel.compose.viewModel

@Composable
fun ScientificCalculator(
    viewModel: CalculatorViewModel = androidx.lifecycle.viewmodel.compose.viewModel()
) {
    val expression by viewModel.expression.collectAsState()
    val result by viewModel.result.collectAsState()

    Column(Modifier.padding(16.dp)) {
        Text("Scientific Calculator", style = MaterialTheme.typography.headlineSmall)

        Spacer(Modifier.height(12.dp))

        OutlinedTextField(
            value = expression,
            onValueChange = { },
            label = { Text("Expression") },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(Modifier.height(12.dp))

        CalculatorButtons(
            onPress = { viewModel.onInput(it) },
            onClear = { viewModel.onClear() },
            onEquals = { viewModel.onEquals() }
        )

        Spacer(Modifier.height(12.dp))

        Text("Result: $result")
    }
}