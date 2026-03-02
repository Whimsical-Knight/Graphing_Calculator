package com.example.cs3318_finalproject.ui.components

import androidx.compose.ui.graphics.Color
import android.graphics.Paint

import androidx.compose.runtime.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.unit.dp

import kotlin.math.ceil
import kotlin.math.floor

import net.objecthunter.exp4j.ExpressionBuilder

@Composable
fun FunctionGraph(
    input: String,
    trigger: Int = 0,
    showIntersections: Boolean = true
) {
    // controls zoom
    var scale by remember { mutableStateOf(1f) }
    // added to allow for pan / linear navigation
    var offset by remember { mutableStateOf(Offset.Zero) }

    // Define generic colors for functions (so we can distinguish multiple functions)
    val colors = listOf(Color.Red, Color.Green, Color.Blue, Color.Yellow, Color.Cyan, Color.Magenta)

    // Split input into tokens: functions and points
    val pointRegex = Regex("""\(\s*-?\d+(\.\d+)?\s*,\s*-?\d+(\.\d+)?\s*\)""")
    val functionRegex = Regex("""\w+\s*=\s*.+""")

    // split components based on semi-colon
    val parts = input.split(";").map { it.trim() }

    // hold user points and functions
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
            val paint = Paint().apply {
                color = android.graphics.Color.DKGRAY
                textSize = 28f
                isAntiAlias = true
            }
            // set ceilings
            xTick = ceil(minX)
            while (xTick <= maxX) {
                if (xTick != 0f) {
                    val sx = originX + xTick * worldScale
                    drawIntoCanvas {
                        it.nativeCanvas.drawText(
                            xTick.toInt().toString(),
                            sx,
                            originY + 35f,
                            paint
                        )
                    }
                }
                xTick += step
            }

            yTick = ceil(minY)
            while (yTick <= maxY) {
                if (yTick != 0f) {
                    val sy = originY - yTick * worldScale
                    drawIntoCanvas {
                        it.nativeCanvas.drawText(
                            yTick.toInt().toString(),
                            originX + 8f,
                            sy + 8f,
                            paint
                        )
                    }
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
                    } catch (_: Exception) {
                        continue
                    }
                    // connect the points into a function
                    val point = Offset(screenX, screenY)
                    prev?.let { drawLine(color, it, point, strokeWidth = 3f) }
                    prev = point
                    points.add(xWorld to yWorld)
                }
                sampledFunctions.add(points)
            }
            // draw the labels for the points
            val userPaint = Paint().apply {
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
                    it.nativeCanvas.drawText(
                        "(${String.format("%.2f", x)},${
                            String.format(
                                "%.2f",
                                y
                            )
                        })", sx + 8f, sy - 8f, userPaint
                    )
                }
            }
        }
    }
}