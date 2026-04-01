package com.example.cs3318_finalproject.ui.components

import androidx.compose.runtime.*
import androidx.compose.material3.*
import androidx.compose.foundation.layout.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

// data model for buttons
data class CalcButton(
    val display: String,
    val value: String
)
@Composable
fun CalculatorButtons(
    onPress: (String) -> Unit,
    onClear: () -> Unit,
    onEquals: () -> Unit
) {
    val rows = listOf(
        listOf(
            CalcButton("7", "7"),
            CalcButton("8", "8"),
            CalcButton("9", "9"),
            CalcButton("÷", "/")
        ),
        listOf(
            CalcButton("4", "4"),
            CalcButton("5", "5"),
            CalcButton("6", "6"),
            CalcButton("×", "*")
        ),
        listOf(
            CalcButton("1", "1"),
            CalcButton("2", "2"),
            CalcButton("3", "3"),
            CalcButton("−", "-")
        ),
        listOf(
            CalcButton("0", "0"),
            CalcButton(".", "."),
            CalcButton("+", "+"),
            CalcButton("xʸ", "^")
        ),
        listOf(
            CalcButton("sin", "sin("),
            CalcButton("cos", "cos("),
            CalcButton("tan", "tan("),
            CalcButton("√", "sqrt(")
        ),
        listOf(
            CalcButton("(", "("),
            CalcButton(")", ")"),
            CalcButton("C", "C"),
            CalcButton("=", "=")
        )
    )

    // stack the rows vertically
    Column {
        rows.forEach { row ->
            Row {
                row.forEach { button ->
                    // button click logic
                    Button(
                        onClick = {
                            when (button.value) {
                                "C" -> onClear()
                                "=" -> onEquals()
                                else -> onPress(button.value)
                            }
                        },
                        // make all buttons equally sized within row
                        modifier = Modifier
                            .weight(1f)
                            .padding(4.dp)
                    ) {
                        Text(button.display)
                    }
                }
            }
        }
    }
}