package com.example.cs3318_finalproject

import android.os.Bundle // for saving/restoring state
import androidx.activity.ComponentActivity // base activity class for compose apps
import androidx.activity.compose.setContent // so you can set a Composable UI directly
import androidx.compose.foundation.Canvas // for drawing graph
import androidx.compose.ui.geometry.Offset // stores 2D points
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import net.objecthunter.exp4j.ExpressionBuilder
// for additional graphing calculator function
import androidx.compose.foundation.gestures.detectTransformGestures // for pinch/zoom
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.nativeCanvas
// math imports
import kotlin.math.floor
import kotlin.math.*
// allows for more colors in my theme
import androidx.compose.material3.MaterialTheme
// For appearance
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding

// Initialize the UI
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
    // controls zoom
    var scale by remember { mutableStateOf(1f) }
    // added to allow for pan / linear navigation
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

            // defines the gridlines
            val minX = -originX / worldScale
            val maxX = (width - originX) / worldScale
            val minY = -(height - originY) / worldScale
            val maxY = originY / worldScale

            // X Tick draws the vertical gridlines per 1 unit
            val step = 1f
            var xTick = floor(minX)
            while (xTick <= maxX) {
                val sx = originX + xTick * worldScale
                drawLine(Color.LightGray, Offset(sx, 0f), Offset(sx, height))
                xTick += step
            }
            // Y Tick draws the horizontal gridlines per 1 unit
            var yTick = floor(minY)
            while (yTick <= maxY) {
                val sy = originY - yTick * worldScale
                drawLine(Color.LightGray, Offset(0f, sy), Offset(width, sy))
                yTick += step
            }

            // draws the graph axes
            drawLine(Color.DarkGray, Offset(0f, originY), Offset(width, originY), strokeWidth = 3f)
            drawLine(Color.DarkGray, Offset(originX, 0f), Offset(originX, height), strokeWidth = 3f)

            // labels the graph axes
            val paint = android.graphics.Paint().apply {
                color = android.graphics.Color.DKGRAY
                textSize = 28f
                isAntiAlias = true
            }
            // set ceilings
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
            // labels the origin
            drawIntoCanvas { it.nativeCanvas.drawText("0", originX + 6f, originY + 30f, paint) }

            // draw the actual functions
            val sampledFunctions = mutableListOf<List<Pair<Float, Float>>>()
            // sets the appearance of each function (i.e,. choosing colors, etc)
            functionTokens.forEachIndexed { index, (lhs, rhs) ->
                val color = colors[index % colors.size]
                val points = mutableListOf<Pair<Float, Float>>()
                var prev: Offset? = null

                // loop for drawing graph, skips invalid points
                for (i in 0..width.toInt()) {
                    val xWorld = (i - originX) / worldScale
                    val yWorld: Float
                    val screenX: Float
                    val screenY: Float
                    // condition to ignore invalid point
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
                    // connect the points into a function
                    val point = Offset(screenX, screenY)
                    prev?.let { drawLine(color, it, point, strokeWidth = 3f) }
                    prev = point
                    points.add(xWorld to yWorld)
                }
                sampledFunctions.add(points)
            }
            // draw the labels for the points
            val userPaint = android.graphics.Paint().apply {
                color = android.graphics.Color.RED
                textSize = 28f
                isAntiAlias = true
            }
            // draw the actual points
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