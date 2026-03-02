package com.example.cs3318_finalproject.ui.screens

import androidx.compose.runtime.*
import androidx.compose.material3.*
import androidx.compose.foundation.layout.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.cs3318_finalproject.ui.components.FunctionGraph

@Composable
fun GraphCalculator() {

    var input by remember { mutableStateOf("y=sin(x)") }

    Column(Modifier.padding(16.dp)) {

        Text("Graphing Calculator", style = MaterialTheme.typography.headlineSmall)

        Spacer(Modifier.height(12.dp))

        OutlinedTextField(
            value = input,
            onValueChange = { input = it },
            label = { Text("Function") },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(Modifier.height(24.dp))

        FunctionGraph(input)
    }
}