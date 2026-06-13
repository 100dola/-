package com.example.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

import com.example.ui.theme.*

// Helper values inside package namespace pointing to our Sophisticated Dark tokens
private val PrimaryColor = EmeraldPrimary
private val SecondaryColor = TealAccent
private val AccentColor = GoldPremium
private val SystemCoral = CoralDeduction

@Composable
fun CardSection(
    title: String,
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Bold,
                    fontSize = 17.sp,
                    color = MaterialTheme.colorScheme.secondary
                ),
                textAlign = TextAlign.Right,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 12.0.dp)
            )
            content()
        }
    }
}

/**
 * Custom circular indicator showing financial score in a gorgeous high-contrast ring format.
 */
@Composable
fun FinancialGauge(
    score: Int, // 0 to 100
    modifier: Modifier = Modifier
) {
    val animatedScore by animateFloatAsState(
        targetValue = score.toFloat(),
        animationSpec = tween(durationMillis = 1000)
    )

    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier.size(130.dp)
    ) {
        Canvas(modifier = Modifier.size(110.dp)) {
            // Shadow backdrop circle
            drawCircle(
                color = BankSurfaceVariant,
                radius = size.minDimension / 2f,
                style = Stroke(width = 10.dp.toPx())
            )

            // Value arc
            val sweepAngle = (animatedScore / 100f) * 360f
            val strokeColor = when {
                score < 50 -> SystemCoral
                score < 80 -> AccentColor
                else -> PrimaryColor
            }

            drawArc(
                color = strokeColor,
                startAngle = -90f,
                sweepAngle = sweepAngle,
                useCenter = false,
                style = Stroke(width = 12.dp.toPx(), cap = StrokeCap.Round)
            )
        }
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = "${animatedScore.toInt()}%",
                style = MaterialTheme.typography.headlineMedium.copy(
                    fontFamily = MaterialTheme.typography.titleLarge.fontFamily,
                    fontWeight = FontWeight.Bold,
                    color = OnBackgroundWhite_val()
                )
            )
            Text(
                text = "مؤشر الصحة",
                style = MaterialTheme.typography.bodySmall.copy(
                    fontSize = 11.sp,
                    color = TextSecondaryMuted
                )
            )
        }
    }
}

/**
 * Styled Interactive Projections Line Chart using Compose Canvas
 */
@Composable
fun ProjectionLineChart(
    points: List<Double>, // Projections over intervals
    labels: List<String>, // Year labels e.g., ["الان", "بعد سنة", "بعد 3 سنوات", "بعد 5 سنوات"]
    modifier: Modifier = Modifier
) {
    if (points.isEmpty()) return

    val maxVal = points.maxOrNull() ?: 1.0
    val minVal = points.minOrNull() ?: 0.0
    val diff = (maxVal - minVal).let { if (it == 0.0) 1.0 else it }

    Card(
        colors = CardDefaults.cardColors(containerColor = BankSurface),
        shape = RoundedCornerShape(16.dp),
        modifier = modifier
            .fillMaxWidth()
            .height(220.dp)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Text(
                text = "الرصيد المالي المقدر تراكمياً (مليون دج)",
                style = MaterialTheme.typography.bodySmall.copy(color = TextSecondaryMuted),
                modifier = Modifier.fillMaxWidth(),
                color = TextSecondaryMuted,
                textAlign = TextAlign.Right
            )

            Spacer(modifier = Modifier.height(10.dp))

            Box(modifier = Modifier.weight(1f).fillMaxWidth()) {
                Canvas(modifier = Modifier.fillMaxSize()) {
                    val width = size.width
                    val height = size.height
                    val paddingLeft = 14f
                    val paddingRight = 14f
                    val paddingTop = 14f
                    val paddingBottom = 20f

                    val innerWidth = width - (paddingLeft + paddingRight)
                    val innerHeight = height - (paddingTop + paddingBottom)

                    val stepX = innerWidth / (points.size - 1).coerceAtLeast(1)

                    // Draw GRID lines
                    val gridLines = 3
                    for (i in 0..gridLines) {
                        val y = paddingTop + (innerHeight / gridLines) * i
                        drawLine(
                            color = BankSurfaceVariant,
                            start = Offset(paddingLeft, y),
                            end = Offset(width - paddingRight, y),
                            strokeWidth = 1f
                        )
                    }

                    // Build path
                    val path = Path()
                    val fillingPath = Path()

                    points.forEachIndexed { idx, valD ->
                        val ratio = (valD - minVal) / diff
                        val x = paddingLeft + idx * stepX
                        // Flip Y axis
                        val y = paddingTop + innerHeight - (ratio * innerHeight).toFloat()

                        if (idx == 0) {
                            path.moveTo(x, y)
                            fillingPath.moveTo(x, paddingTop + innerHeight)
                            fillingPath.lineTo(x, y)
                        } else {
                            path.lineTo(x, y)
                            fillingPath.lineTo(x, y)
                        }

                        if (idx == points.size - 1) {
                            fillingPath.lineTo(x, paddingTop + innerHeight)
                            fillingPath.close()
                        }

                        // Draw Point dots
                        drawCircle(
                            color = PrimaryColor,
                            radius = 5.dp.toPx(),
                            center = Offset(x, y)
                        )
                    }

                    // Stroke Path
                    drawPath(
                        path = path,
                        color = PrimaryColor,
                        style = Stroke(width = 3.dp.toPx(), cap = StrokeCap.Round)
                    )

                    // Fill under line
                    drawPath(
                        path = fillingPath,
                        brush = Brush.verticalGradient(
                            colors = listOf(
                                PrimaryColor.copy(alpha = 0.35f),
                                Color.Transparent
                            )
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // Reverse labels to match RTL orientation
                labels.forEach { label ->
                    Text(
                        text = label,
                        style = MaterialTheme.typography.bodySmall.copy(
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium,
                            color = OnBackgroundWhite
                        ),
                        modifier = Modifier.weight(1f),
                        textAlign = TextAlign.Center
                    )
                }
            }
        }
    }
}

// Compat methods to avoid direct import unresolved problems if context resets
private fun OnBackgroundWhite_val(): Color = OnBackgroundWhite
