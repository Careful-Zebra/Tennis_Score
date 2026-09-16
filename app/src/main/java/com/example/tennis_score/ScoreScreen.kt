package com.example.tennis_score


import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.wear.compose.material.MaterialTheme
import androidx.wear.compose.material.Text
import kotlin.collections.plus
import androidx.compose.foundation.border
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.Canvas
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.drawscope.Stroke

@Composable
fun ScoreScreen() {
    // History enables one-tap undo if you fat-finger a point mid-rally.
    var history by remember { mutableStateOf(listOf(MatchState())) }
    val state = history.last()

    Box(modifier = Modifier.fillMaxSize().background(Color.Black)) {

        // Left half = point for Player A, right half = point for Player B.
        // Big tap zones on purpose: sweaty fingers, quick glances mid point.
        Row(modifier = Modifier.fillMaxSize()) {
            TapZone(
                modifier = Modifier.weight(1f),
                enabled = state.winner == null,
                onTap = { history = history + TennisScore.pointWon(state, Player.A) }
            )
            TapZone(
                modifier = Modifier.weight(1f),
                enabled = state.winner == null,
                onTap = { history = history + TennisScore.pointWon(state, Player.B) }
            )
        }

        Canvas(modifier = Modifier.fillMaxSize()) {
            val stroke = 2.dp.toPx()
            val diameter = minOf(size.width, size.height) - stroke
            val topLeft = Offset((size.width - diameter) / 2f, (size.height - diameter) / 2f)
            val arcSize = Size(diameter, diameter)

            drawArc(
                color = Color.Blue.copy(alpha = 0.35f),
                startAngle = 90f, sweepAngle = 180f, useCenter = false,
                topLeft = topLeft, size = arcSize, style = Stroke(width = stroke)
            )
            drawArc(
                color = Color.Red.copy(alpha = 0.35f),
                startAngle = -90f, sweepAngle = 180f, useCenter = false,
                topLeft = topLeft, size = arcSize, style = Stroke(width = stroke)
            )
            drawLine(
                color = Color.White.copy(alpha = 0.25f),
                start = Offset(size.width / 2f, topLeft.y),
                end = Offset(size.width / 2f, topLeft.y + diameter),
                strokeWidth = stroke
            )
        }

        ScoreOverlay(state = state, modifier = Modifier.align(Alignment.Center))

        if (history.size > 1) {
            Text(
                text = "undo",
                fontSize = 12.sp,
                color = Color.Gray,
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 6.dp)
                    .pointerInput(Unit) {
                        detectTapGestures(onTap = { history = TennisScore.undo(history) })
                    }
                    .border(1.dp, Color.White.copy(alpha = 0.35f), RoundedCornerShape(50))
                    .padding(horizontal = 12.dp, vertical = 4.dp)
            )
        }
    }
}

@Composable
private fun TapZone(modifier: Modifier, enabled: Boolean, onTap: () -> Unit) {
    // pointerInput only restarts when its key (enabled) changes, so the gesture
    // detector captures onTap once. Route through rememberUpdatedState so each tap
    // calls the latest onTap (which closes over the current MatchState) instead of
    // the stale one captured at first composition.
    val currentOnTap by rememberUpdatedState(onTap)
    Box(
        modifier = modifier
            .fillMaxHeight()
            .pointerInput(enabled) {
                if (enabled) detectTapGestures(onTap = { currentOnTap() })
            }
    )
}

@Composable
private fun ScoreOverlay(state: MatchState, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Sets line, e.g. "6-4   3-6   2-1"
        val setsLine = (state.completedSets + state.currentSetGames)
            .joinToString("   ") { "${it.first}-${it.second}" }
        Text(text = setsLine, fontSize = 14.sp, color = Color.LightGray)

        Spacer(modifier = Modifier.height(6.dp))

        val pointsLine = if (state.inTiebreak) {
            "${state.tiebreakPoints.first} - ${state.tiebreakPoints.second}"
        } else {
            val (a, b) = state.currentGamePoints
            "${TennisScore.pointLabel(a, b)} - ${TennisScore.pointLabel(b, a)}"
        }
        Text(
            text = pointsLine,
            fontSize = 30.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colors.primary
        )

        state.winner?.let {
            Spacer(modifier = Modifier.height(4.dp))
            Text(text = "Player ${it.name} wins", fontSize = 14.sp, color = Color.Green)
        }
    }
}
