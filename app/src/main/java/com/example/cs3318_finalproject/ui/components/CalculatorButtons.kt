package com.example.cs3318_finalproject.ui.components

import androidx.compose.runtime.*
import androidx.compose.material3.*
import androidx.compose.foundation.layout.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun CalculatorButtons(
    onPress: (String) -> Unit,
    onClear: () -> Unit,
    onEquals: () -> Unit
) {
    val rows = listOf(
        listOf("7", "8", "9", "/"),
        listOf("4", "5", "6", "*"),
        listOf("1", "2", "3", "-"),
        listOf("0", ".", "+", "^"),
        listOf("sin(", "cos(", "tan(", "sqrt("),
        listOf("(", ")", "C", "=")
    )

    // stack the rows vertically
    Column {
        rows.forEach { row ->
            Row {
                row.forEach { label ->
                    // button click logic
                    Button(
                        onClick = {
                            when (label) {
                                "C" -> onClear()
                                "=" -> onEquals()
                                else -> onPress(label)
                            }
                        },
                        // make all buttons equally sized within row
                        modifier = Modifier
                            .weight(1f)
                            .padding(4.dp)
                    ) {
                        Text(label)
                    }
                }
            }
        }
    }
}