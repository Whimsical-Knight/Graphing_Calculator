package com.example.cs3318_finalproject

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import net.objecthunter.exp4j.ExpressionBuilder

// for additional graphing calculator function (to enable pinch zoom and auto scaling)
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.nativeCanvas
import android.graphics.Paint

// allows me to use clipToBounds so graph doesn't bleed into rest of interface
import androidx.compose.ui.draw.clipToBounds

// allows for graphing calculator calculations
import kotlin.math.pow
import kotlin.math.floor
import kotlin.math.*
import kotlin.math.log10
import kotlin.math.abs

// allows for more colors
import androidx.compose.material3.MaterialTheme

// allows for using canvas
import androidx.compose.foundation.Canvas

// allow for rounding
import kotlin.math.roundToInt

import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.dp
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.runtime.*

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent { CalculatorTabsApp() }
    }
}

@Composable
fun CalculatorTabsApp() {

    var selectedTab by remember { mutableStateOf(0) }
    val tabs = listOf("Graphing", "Scientific")

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

        when (selectedTab) {
            0 -> GraphCalculator()
            1 -> ScientificCalculator()
        }
    }
}

@Composable
fun GraphCalculator() {

    var input by remember { mutableStateOf("y=sin(x)") }
    var result by remember { mutableStateOf("") }
    var trigger by remember { mutableStateOf(0) }

    Column(Modifier.padding(16.dp)) {

        Text("Graphing Calculator", style = MaterialTheme.typography.headlineSmall)

        Spacer(Modifier.height(12.dp))

        OutlinedTextField(
            value = input,
            onValueChange = { input = it },
            label = { Text("Function") },
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(Modifier.height(8.dp))
        Spacer(Modifier.height(16.dp))
        Spacer(Modifier.height(24.dp))
        FunctionGraph(input, trigger)
    }
}

@Composable
fun ScientificCalculator() {

    var expression by remember { mutableStateOf("") }
    var result by remember { mutableStateOf("") }

    Column(Modifier.padding(16.dp)) {

        Text("Scientific Calculator", style = MaterialTheme.typography.headlineSmall)

        Spacer(Modifier.height(12.dp))

        OutlinedTextField(
            value = expression,
            onValueChange = { expression = it },
            label = { Text("Expression") },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(Modifier.height(12.dp))

        CalculatorButtons(
            onPress = { expression += it },
            onClear = {
                expression = ""
                result = ""
            },
            onEquals = {
                result = evaluate(expression)
            }
        )

        Spacer(Modifier.height(12.dp))

        Text("Result: $result")
    }
}

@Composable
fun CalculatorButtons(onPress: (String) -> Unit, onClear: () -> Unit, onEquals: () -> Unit) {

    val rows = listOf(
        listOf("7", "8", "9", "/"),
        listOf("4", "5", "6", "*"),
        listOf("1", "2", "3", "-"),
        listOf("0", ".", "+", "^"),
        listOf("sin(", "cos(", "tan(", "sqrt("),
        listOf("(", ")", "C", "=")
    )

    Column {
        rows.forEach { row ->
            Row {
                row.forEach { label ->
                    Button(
                        onClick = {
                            when (label) {
                                "C" -> onClear()
                                "=" -> onEquals()
                                else -> onPress(label)
                            }
                        },
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

fun evaluate(expr: String): String {
    return try {
        val e = ExpressionBuilder(expr).build()
        e.evaluate().toString()
    } catch (e: Exception) {
        "Error"
    }
}

@Composable
fun FunctionGraph(
    input: String,
    trigger: Int,
    showIntersections: Boolean = true
) {
    var scale by remember { mutableStateOf(1f) }
    // added to allow for up/down and linear navigation (not just pinch/zoom)
    var offset by remember { mutableStateOf(Offset.Zero) }

    // Generic colors for functions
    val colors = listOf(Color.Red, Color.Green, Color.Blue, Color.Yellow, Color.Cyan, Color.Magenta)

    // Split input into tokens: functions and points
    val pointRegex = Regex("""\(\s*-?\d+(\.\d+)?\s*,\s*-?\d+(\.\d+)?\s*\)""")
    val functionRegex = Regex("""\w+\s*=\s*.+""")

    val parts = input.split(";").map { it.trim() }

    val userPoints = mutableListOf<Pair<Float, Float>>()
    val functionTokens = mutableListOf<Pair<String, String>>() // lhs=rhs

    parts.forEach { part ->
        when {
            pointRegex.matches(part) -> {
                val numbers = part.trim('(', ')').split(",").map { it.trim().toFloat() }
                if (numbers.size == 2) userPoints.add(numbers[0] to numbers[1])
            }
            functionRegex.matches(part) -> {
                val (lhs, rhs) = part.split("=").map { it.trim() }
                functionTokens.add(lhs to rhs)
            }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(360.dp)
            .padding(top = 16.dp)
    ) {
        Canvas(
            modifier = Modifier
                .fillMaxSize()
                .pointerInput(Unit) {
                    detectTransformGestures { _, pan, zoom, _ ->
                        scale *= zoom
                        scale = scale.coerceIn(0.2f, 20f)
                        // this little guy (and the pan inclusion in detectTransformGestures)
                        // allow for vertical and linear zoom/navigation (not just pinch/zoom)
                        offset += pan
                    }
                }



        ) {
            val width = size.width
            val height = size.height
            // added to allow for side-to-side and linear navigation (not just pinch/zoom)
            val originX = width / 2 + offset.x
            val originY = height / 2 + offset.y
            val worldScale = (width / 20f) * scale

            // ===== GRIDLINES =====
            val minX = -originX / worldScale
            val maxX = (width - originX) / worldScale
            val minY = -(height - originY) / worldScale
            val maxY = originY / worldScale

            val step = 1f
            var xTick = floor(minX)
            while (xTick <= maxX) {
                val sx = originX + xTick * worldScale
                drawLine(Color.LightGray, Offset(sx, 0f), Offset(sx, height))
                xTick += step
            }

            var yTick = floor(minY)
            while (yTick <= maxY) {
                val sy = originY - yTick * worldScale
                drawLine(Color.LightGray, Offset(0f, sy), Offset(width, sy))
                yTick += step
            }

            // ===== AXES =====
            drawLine(Color.DarkGray, Offset(0f, originY), Offset(width, originY), strokeWidth = 3f)
            drawLine(Color.DarkGray, Offset(originX, 0f), Offset(originX, height), strokeWidth = 3f)

            // ===== AXES LABELS =====
            val paint = android.graphics.Paint().apply {
                color = android.graphics.Color.DKGRAY
                textSize = 28f
                isAntiAlias = true
            }

            xTick = ceil(minX)
            while (xTick <= maxX) {
                if (xTick != 0f) {
                    val sx = originX + xTick * worldScale
                    drawIntoCanvas { it.nativeCanvas.drawText(xTick.toInt().toString(), sx, originY + 35f, paint) }
                }
                xTick += step
            }

            yTick = ceil(minY)
            while (yTick <= maxY) {
                if (yTick != 0f) {
                    val sy = originY - yTick * worldScale
                    drawIntoCanvas { it.nativeCanvas.drawText(yTick.toInt().toString(), originX + 8f, sy + 8f, paint) }
                }
                yTick += step
            }

            drawIntoCanvas { it.nativeCanvas.drawText("0", originX + 6f, originY + 30f, paint) }

            // ===== DRAW FUNCTIONS =====
            val sampledFunctions = mutableListOf<List<Pair<Float, Float>>>()
            functionTokens.forEachIndexed { index, (lhs, rhs) ->
                val color = colors[index % colors.size]
                val points = mutableListOf<Pair<Float, Float>>()
                var prev: Offset? = null

                for (i in 0..width.toInt()) {
                    val xWorld = (i - originX) / worldScale
                    val yWorld: Float
                    val screenX: Float
                    val screenY: Float

                    try {
                        when (lhs) {
                            "y" -> {
                                yWorld = ExpressionBuilder(rhs).variable("x").build()
                                    .setVariable("x", xWorld.toDouble()).evaluate().toFloat()
                                if (!yWorld.isFinite()) continue
                                screenX = i.toFloat()
                                screenY = originY - yWorld * worldScale
                            }
                            "x" -> {
                                val yVal = xWorld
                                val xVal = ExpressionBuilder(rhs).variable("y").build()
                                    .setVariable("y", yVal.toDouble()).evaluate().toFloat()
                                if (!xVal.isFinite()) continue
                                screenX = originX + xVal * worldScale
                                screenY = originY - yVal * worldScale
                                yWorld = yVal
                            }
                            else -> continue
                        }
                    } catch (_: Exception) { continue }

                    val point = Offset(screenX, screenY)
                    prev?.let { drawLine(color, it, point, strokeWidth = 3f) }
                    prev = point

                    points.add(xWorld to yWorld)
                }

                sampledFunctions.add(points)
            }

            // ===== DRAW USER POINTS =====
            val userPaint = android.graphics.Paint().apply {
                color = android.graphics.Color.RED
                textSize = 28f
                isAntiAlias = true
            }

            userPoints.forEach { (x, y) ->
                val sx = originX + x * worldScale
                val sy = originY - y * worldScale
                drawCircle(Color.Red, 6f, Offset(sx, sy))
                drawIntoCanvas {
                    it.nativeCanvas.drawText("(${String.format("%.2f", x)},${String.format("%.2f", y)})", sx + 8f, sy - 8f, userPaint)
                }
            }
        }
    }
}






