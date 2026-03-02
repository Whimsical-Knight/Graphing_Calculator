package com.example.cs3318_finalproject.engine

// for parsing mathematical strings
import net.objecthunter.exp4j.ExpressionBuilder

class CalculatorEngine {
    fun evaluate(expression: String): String {
        // try to convert the expression to a string
        return try {
            // expression builder builds the equation according to PEMDAS
            val result = ExpressionBuilder(expression).build().evaluate()
            result.toString()
        } catch (e: Exception) { // error catch
            "Error"
        }
    }
}