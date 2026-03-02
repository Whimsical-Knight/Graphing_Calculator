package com.example.cs3318_finalproject.engine

import net.objecthunter.exp4j.ExpressionBuilder

class GraphEngine {

    // defines structure to hold functions and user points
    data class GraphData(
        val functions: List<List<Pair<Float, Float>>>,
        val userPoints: List<Pair<Float, Float>>
    )

    // parse the input
    // determine what is a point and what is a function so we know how to handle it
    fun parseInput(input: String): Pair<
            List<Pair<String, String>>,
            List<Pair<Float, Float>>
            > {
        // detects valid point
        val pointRegex = Regex("""\(\s*-?\d+(\.\d+)?\s*,\s*-?\d+(\.\d+)?\s*\)""")
        // detects valid function
        val functionRegex = Regex("""\w+\s*=\s*.+""")
        // split functions and points
        val parts = input.split(";").map { it.trim() }

        // hold all functions and user points
        val userPoints = mutableListOf<Pair<Float, Float>>()
        val functionTokens = mutableListOf<Pair<String, String>>()

        parts.forEach { part ->
            when {
                // remove () and convert to float
                pointRegex.matches(part) -> {
                    val numbers = part.trim('(', ')')
                        .split(",")
                        .map { it.trim().toFloat() }

                    if (numbers.size == 2)
                        userPoints.add(numbers[0] to numbers[1])
                }
                // trim functions and distinguish lhs from rhs
                functionRegex.matches(part) -> {
                    val (lhs, rhs) = part.split("=")
                        .map { it.trim() }
                    functionTokens.add(lhs to rhs)
                }
            }
        }
        return functionTokens to userPoints
    }

    // evaluates my functions
    // start with Y-evaluation and see if it fails
    fun evaluateY(rhs: String, x: Double): Double? {
        return try {
            ExpressionBuilder(rhs)
                .variable("x")
                .build()
                .setVariable("x", x)
                .evaluate()
        // catch for if graphing calculation fails
        // move to next expression if it does
        } catch (e: Exception) {
            null
        }
    }
}