package com.example.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.NativeCanvas
import androidx.compose.ui.graphics.Paint
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.*
import kotlin.math.*

/**
 * Custom Premium Container Card for input fields, following 16dp design guidelines.
 */
@Composable
fun PremiumFormCard(
    modifier: Modifier = Modifier,
    title: String? = null,
    content: @Composable ColumnScope.() -> Unit
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .shadow(4.dp, shape = RoundedCornerShape(12.dp))
            .border(1.dp, SlateMuted, RoundedCornerShape(12.dp)),
        colors = CardDefaults.cardColors(
            containerColor = DarkCharcoal
        ),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            if (title != null) {
                Text(
                    text = title.uppercase(),
                    color = PrimaryAmber,
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.5.sp,
                    modifier = Modifier.padding(bottom = 4.dp)
                )
            }
            content()
        }
    }
}

/**
 * Segmented touch rating buttons (1 to 7) that glow with an amber fill when active.
 */
@Composable
fun CircularRatingRow(
    selectedScore: Int,
    onScoreSelected: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        for (score in 1..7) {
            val isSelected = selectedScore == score
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .size(44.dp)
                    .clip(CircleShape)
                    .background(
                        if (isSelected) {
                            Brush.radialGradient(
                                colors = listOf(PrimaryAmber, Color(0xFFC47B00))
                            )
                        } else {
                            Brush.verticalGradient(
                                colors = listOf(SlateMuted, DarkCharcoal)
                            )
                        }
                    )
                    .border(
                        width = if (isSelected) 2.dp else 1.dp,
                        color = if (isSelected) SecondaryAmber else TextSoftGray.copy(alpha = 0.3f),
                        shape = CircleShape
                    )
                    .clickable { onScoreSelected(score) }
            ) {
                Text(
                    text = score.toString(),
                    color = if (isSelected) ObsidianBlack else TextWarmWhite,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = if (isSelected) FontWeight.ExtraBold else FontWeight.Bold,
                    fontSize = 16.sp
                )
            }
        }
    }
}

/**
 * Radar canvas component modeling 10 signature flavor dimensions.
 * Drag inward to center for a 0/7 (flavor note absent) or outward up to 7/7.
 */
@Composable
fun InteractiveFlavorWheel(
    scores: Map<String, Int>,
    onScoresChanged: (Map<String, Int>) -> Unit,
    modifier: Modifier = Modifier
) {
    val dimensions = listOf(
        "Nutty", "Woody", "Heat", "Herbal", "Citrusy",
        "Stone Fruity", "Smokey", "Sour", "Sweet", "Savory"
    )

    val currentScores = remember(scores) {
        mutableStateMapOf<String, Int>().apply {
            dimensions.forEach { put(it, scores[it] ?: 4) }
        }
    }

    val density = LocalDensity.current

    BoxWithConstraints(
        modifier = modifier
            .fillMaxWidth()
            .aspectRatio(1f)
            .padding(16.dp),
        contentAlignment = Alignment.Center
    ) {
        val sizePx = with(density) { maxWidth.toPx() }
        val radius = sizePx * 0.38f
        val center = Offset(sizePx / 2f, sizePx / 2f)

        Canvas(
            modifier = Modifier
                .fillMaxSize()
                .pointerInput(dimensions) {
                    var activeSpokeIndex: Int? = null
                    detectDragGestures(
                        onDragStart = { offset ->
                            val dx = offset.x - center.x
                            val dy = offset.y - center.y
                            var angle = atan2(dy, dx)
                            if (angle < 0) angle += 2 * PI.toFloat()

                            var minAngleDiff = Float.MAX_VALUE
                            var nearestIdx = 0
                            for (i in dimensions.indices) {
                                val spokeAngle = i * (2f * PI.toFloat() / dimensions.size)
                                var diff = abs(angle - spokeAngle)
                                if (diff > PI) diff = (2 * PI.toFloat()) - diff
                                if (diff < minAngleDiff) {
                                    minAngleDiff = diff
                                    nearestIdx = i
                                }
                            }
                            activeSpokeIndex = nearestIdx

                            val dist = sqrt(dx * dx + dy * dy)
                            val normDist = dist / radius
                            val score = (normDist * 7.0f).roundToInt().coerceIn(1, 7)
                            val dimKey = dimensions[nearestIdx]
                            currentScores[dimKey] = score
                            onScoresChanged(currentScores.toMap())
                        },
                        onDrag = { change, _ ->
                            change.consume()
                            val idx = activeSpokeIndex
                            if (idx != null) {
                                val dx = change.position.x - center.x
                                val dy = change.position.y - center.y
                                val dist = sqrt(dx * dx + dy * dy)
                                val normDist = dist / radius
                                val score = (normDist * 7.0f).roundToInt().coerceIn(1, 7)
                                val dimKey = dimensions[idx]
                                currentScores[dimKey] = score
                                onScoresChanged(currentScores.toMap())
                            }
                        },
                        onDragEnd = {
                            activeSpokeIndex = null
                        },
                        onDragCancel = {
                            activeSpokeIndex = null
                        }
                    )
                }
                .pointerInput(dimensions) {
                    detectTapGestures(
                        onTap = { offset ->
                            val dx = offset.x - center.x
                            val dy = offset.y - center.y
                            val dist = sqrt(dx * dx + dy * dy)
                            var angle = atan2(dy, dx)
                            if (angle < 0) angle += 2 * PI.toFloat()

                            var minAngleDiff = Float.MAX_VALUE
                            var nearestIdx = 0
                            for (i in dimensions.indices) {
                                val spokeAngle = i * (2f * PI.toFloat() / dimensions.size)
                                var diff = abs(angle - spokeAngle)
                                if (diff > PI) diff = (2 * PI.toFloat()) - diff
                                if (diff < minAngleDiff) {
                                    minAngleDiff = diff
                                    nearestIdx = i
                                }
                            }

                            val normDist = dist / radius
                            val score = (normDist * 7.0f).roundToInt().coerceIn(1, 7)

                            val dimKey = dimensions[nearestIdx]
                            currentScores[dimKey] = score
                            onScoresChanged(currentScores.toMap())
                        }
                    )
                }
        ) {
            // Background ring colors
            val gridStrokeColor = SlateMuted.copy(alpha = 0.6f)
            val glowColor = PrimaryAmber

            // 1. Draw 6 concentric grid circles starting from level 2
            for (level in 2..7) {
                val r = (level / 7.0f) * radius
                drawCircle(
                    color = gridStrokeColor,
                    radius = r,
                    center = center,
                    style = Stroke(width = if (level == 7) 2.dp.toPx() else 1.dp.toPx())
                )
            }

            // 2. Draw 10 ray spokes representing dimensions (spokes)
            val angles = dimensions.indices.map { i ->
                i * (2f * PI.toFloat() / dimensions.size)
            }

            for (i in dimensions.indices) {
                val angle = angles[i]
                val endX = center.x + radius * cos(angle)
                val endY = center.y + radius * sin(angle)
                
                // Draw spoke line
                drawLine(
                    color = gridStrokeColor,
                    start = center,
                    end = Offset(endX, endY),
                    strokeWidth = 1.dp.toPx()
                )

                // Render Labels beautifully around the perimeter
                // Calculate position slightly beyond outer radius
                val textRadius = radius + 25.dp.toPx()
                val labelX = center.x + textRadius * cos(angle)
                val labelY = center.y + textRadius * sin(angle)

                // Native canvas draw text for accurate alignment
                val paint = Paint().asFrameworkPaint().apply {
                    color = TextWarmWhite.toArgb()
                    textSize = 12.sp.toPx()
                    textAlign = android.graphics.Paint.Align.CENTER
                    typeface = android.graphics.Typeface.create(android.graphics.Typeface.DEFAULT, android.graphics.Typeface.BOLD)
                }
                
                // Shift visual height of text slightly based on trigonometry to center vertically
                val textShift = if (sin(angle) > 0.1f) 4.dp.toPx() else if (sin(angle) < -0.1f) -4.dp.toPx() else 0f
                drawContext.canvas.nativeCanvas.drawText(
                    dimensions[i],
                    labelX,
                    labelY + textShift,
                    paint
                )
            }

            // 3. Draw radar filled polygon connecting score handles
            val pathPoints = dimensions.indices.map { i ->
                val score = currentScores[dimensions[i]] ?: 0
                val spokeRadius = (score.toFloat() / 7.0f) * radius
                val angle = angles[i]
                Offset(
                    x = center.x + spokeRadius * cos(angle),
                    y = center.y + spokeRadius * sin(angle)
                )
            }

            val path = androidx.compose.ui.graphics.Path().apply {
                if (pathPoints.isNotEmpty()) {
                    moveTo(pathPoints[0].x, pathPoints[0].y)
                    for (i in 1 until pathPoints.size) {
                        lineTo(pathPoints[i].x, pathPoints[i].y)
                    }
                    close()
                }
            }

            // Fill polygon with semi-transparent glowing amber
            drawPath(
                path = path,
                color = glowColor.copy(alpha = 0.25f)
            )

            // Outline polygon
            drawPath(
                path = path,
                color = glowColor,
                style = Stroke(width = 2.dp.toPx())
            )

            // 4. Draw hollow handles on outer score intersections, allowing smooth dragging
            for (i in dimensions.indices) {
                val score = currentScores[dimensions[i]] ?: 0
                val angle = angles[i]
                val handleRadius = (score.toFloat() / 7.0f) * radius
                val handleOffset = Offset(
                    x = center.x + handleRadius * cos(angle),
                    y = center.y + handleRadius * sin(angle)
                )

                // Amber halo underlay glow if active
                if (score > 0) {
                    drawCircle(
                        color = glowColor.copy(alpha = 0.4f),
                        radius = 8.dp.toPx(),
                        center = handleOffset
                    )
                }

                // Draw central hollow dot
                drawCircle(
                    color = if (score == 0) TextSoftGray else SecondaryAmber,
                    radius = 4.5.dp.toPx(),
                    center = handleOffset,
                    style = Stroke(width = 2.dp.toPx())
                )
            }
        }
    }
}
