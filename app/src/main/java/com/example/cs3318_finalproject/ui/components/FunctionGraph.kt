package com.example.cs3318_finalproject.ui.components

import androidx.compose.runtime.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.dp
import android.graphics.Paint
import kotlin.math.ceil
import kotlin.math.floor
import com.example.cs3318_finalproject.engine.GraphEngine
import androidx.compose.ui.graphics.nativeCanvas

@Composable
fun FunctionGraph(
    input: String, // this is the raw user input
    trigger: Int = 0, // dummy parameter to force recomposition if needed
    showIntersections: Boolean = true // currently unused (trying to show graph intersections)
) {
    // allows for zoom and pan. Tracks zoom level.
    var scale by remember { mutableStateOf(1f) }
    // allows for linear navigation (drag)
    var offset by remember { mutableStateOf(Offset.Zero) }

    // Generic colors for functions
    val colors = listOf(Color.Red, Color.Green, Color.Blue, Color.Yellow, Color.Cyan, Color.Magenta)

    // Create instance of GraphEngine to parse and evaluate
    val graphEngine = remember { GraphEngine() }
    val (functionTokens, userPoints) = remember(input, trigger) {
        graphEngine.parseInput(input)
    }

    // Sets a container for the graph
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(360.dp)
            .padding(top = 16.dp)
    ) { // fill the box with canvas
        Canvas(
            modifier = Modifier
                .fillMaxSize()
                // detect gestures (like pan / zoom, drag)
                .pointerInput(Unit) {
                    detectTransformGestures { _, pan, zoom, _ ->
                        scale *= zoom // update zoom
                        scale = scale.coerceIn(0.2f, 20f) // update zoom scale
                        offset += pan // update offset
                    }
                }
        ) {
            // set the size of the canvas drawing
            val width = size.width
            val height = size.height

            // set the origin of the graph w / pan offset
            val originX = width / 2 + offset.x
            val originY = height / 2 + offset.y
            val worldScale = (width / 20f) * scale // pixels per unit in teh graph

            // Define the visible math range (i.e., function range)
            val minX = -originX / worldScale
            val maxX = (width - originX) / worldScale
            val minY = -(height - originY) / worldScale
            val maxY = originY / worldScale

            // draw the vertical gridlines
            val step = 1f
            var xTick = floor(minX)
            while (xTick <= maxX) {
                val sx = originX + xTick * worldScale
                drawLine(Color.LightGray, Offset(sx, 0f), Offset(sx, height))
                xTick += step
            }
            // draw the horizontal gridlines
            var yTick = floor(minY)
            while (yTick <= maxY) {
                val sy = originY - yTick * worldScale
                drawLine(Color.LightGray, Offset(0f, sy), Offset(width, sy))
                yTick += step
            }

            // Define the axes (with different color)
            drawLine(Color.DarkGray, Offset(0f, originY), Offset(width, originY), strokeWidth = 3f)
            drawLine(Color.DarkGray, Offset(originX, 0f), Offset(originX, height), strokeWidth = 3f)

            // Define the axial labeling
            val paint = Paint().apply {
                color = android.graphics.Color.DKGRAY
                textSize = 28f
                isAntiAlias = true
            }

            // label each X tick with numbers
            xTick = ceil(minX)
            while (xTick <= maxX) {
                if (xTick != 0f) { // omit origin
                    val sx = originX + xTick * worldScale
                    // draw the text on the canvas
                    drawIntoCanvas {
                        it.nativeCanvas.drawText(xTick.toInt().toString(), sx, originY + 35f, paint)
                    }
                }
                xTick += step
            }

            // label each Y tick with numbers
            yTick = ceil(minY)
            while (yTick <= maxY) {
                if (yTick != 0f) { // omit origin
                    val sy = originY - yTick * worldScale
                    drawIntoCanvas {
                        it.nativeCanvas.drawText(yTick.toInt().toString(), originX + 8f, sy + 8f, paint)
                    }
                }
                yTick += step
            }

            // draw origin separately
            drawIntoCanvas { it.nativeCanvas.drawText("0", originX + 6f, originY + 30f, paint) }

            // Draw the functions
            functionTokens.forEachIndexed { index, (lhs, rhs) ->
                val color = colors[index % colors.size]
                var prev: Offset? = null
                // iterate across the canvas by pixel (horizontally)
                for (i in 0..width.toInt()) {
                    val xWorld = (i - originX) / worldScale
                    val yWorld: Float
                    val screenX: Float
                    val screenY: Float
                    try {
                        // for y, set our independent variable to x
                        when (lhs) {
                            "y" -> {
                                val yVal = graphEngine.evaluateY(rhs, xWorld.toDouble()) ?: continue
                                yWorld = yVal.toFloat()
                                if (!yWorld.isFinite()) continue
                                screenX = i.toFloat()
                                screenY = originY - yWorld * worldScale
                            }
                        // for x, set our independent variable to y
                            "x" -> {
                                val yVal = xWorld
                                val xVal = graphEngine.evaluateY(rhs, yVal.toDouble()) ?: continue
                                if (!xVal.isFinite()) continue
                                screenX = originX + (xVal.toFloat() * worldScale)  // convert Double -> Float
                                screenY = originY - yVal * worldScale
                                yWorld = yVal
                            }
                            else -> continue
                        } // exception in case a point fails
                    } catch (_: Exception) { continue }

                    // draw a line to connect all the points
                    val point = Offset(screenX, screenY)
                    prev?.let { drawLine(color, it, point, strokeWidth = 3f) }
                    prev = point
                }
            }

            // Draw the labeling for points
            val userPaint = Paint().apply {
                color = android.graphics.Color.RED
                textSize = 28f
                isAntiAlias = true
            }
            // draw the actual point graphics
            userPoints.forEach { (x, y) ->
                // convert world coordinates to screen coordinates
                val sx = originX + x * worldScale
                val sy = originY - y * worldScale
                drawCircle(Color.Red, 6f, Offset(sx, sy))
                drawIntoCanvas {
                    it.nativeCanvas.drawText("(${String.format("%.2f", x)},${String.format("%.2f", y)})",
                        sx + 8f, sy - 8f, userPaint)
                }
            }
        }
    }
}
