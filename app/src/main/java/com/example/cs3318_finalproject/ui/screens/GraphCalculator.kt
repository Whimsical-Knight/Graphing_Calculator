package com.example.cs3318_finalproject.ui.screens

import androidx.compose.runtime.*
import androidx.compose.material3.*
import androidx.compose.foundation.layout.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.cs3318_finalproject.ui.components.FunctionGraph

@Composable
fun GraphCalculator() {
    // set default
    var input by remember { mutableStateOf("y=sin(x)") }
    // spacing for vertical elements
    Column(Modifier.padding(16.dp)) { // define header, set theme
        Text("Graphing Calculator", style = MaterialTheme.typography.headlineSmall)
        Spacer(Modifier.height(12.dp)) // vertical spacing
        OutlinedTextField(
            value = input,
            onValueChange = { input = it }, // set text to state variable
            label = { Text("Function") }, // hint for text field
            modifier = Modifier.fillMaxWidth() // allow text field to expand to match screen width
        )
        Spacer(Modifier.height(24.dp)) // vertical gap between input field and graph
        FunctionGraph(input)
    }
}