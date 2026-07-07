package com.luminous.trust.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.luminous.trust.capture.SessionBuilder
import com.luminous.trust.net.InnaitClient
import com.luminous.trust.net.ScoreResult
import com.luminous.trust.net.TrustState
import com.luminous.trust.ui.theme.*
import kotlinx.coroutines.launch

/**
 * The Trust Console — Luminous Bio-Tech styled.
 * White cards on a near-white ground, hairline outlines (no heavy shadows),
 * Growth Green as the single "spark of life" accent on the primary action.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TrustScreen() {
    val ctx = LocalContext.current
    val scope = rememberCoroutineScope()

    var serverUrl by remember { mutableStateOf("http://10.0.2.2:8080") }
    var userId by remember { mutableStateOf("PB0366") }
    var state by remember { mutableStateOf<TrustState>(TrustState.Idle) }

    Scaffold(containerColor = Background) { pad ->
        Column(
            Modifier
                .padding(pad)
                .padding(horizontal = 24.dp)
                .verticalScroll(rememberScrollState()),
        ) {
            Spacer(Modifier.height(32.dp))
            Text("LUMINOUS", style = MaterialTheme.typography.labelLarge, color = Primary)
            Text(
                "Trust Console",
                style = MaterialTheme.typography.headlineMedium,
                color = OnBackground
            )
            Spacer(Modifier.height(4.dp))
            Text(
                "Continuous behavioral verification for this session.",
                style = MaterialTheme.typography.bodyMedium,
                color = OnSurfaceVariant
            )

            Spacer(Modifier.height(24.dp))
            ScoreCard(state)

            Spacer(Modifier.height(16.dp))
            OutlinedTextField(
                value = userId, onValueChange = { userId = it },
                label = { Text("User ID") },
                singleLine = true,
                shape = MaterialTheme.shapes.small,
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(Modifier.height(8.dp))
            OutlinedTextField(
                value = serverUrl, onValueChange = { serverUrl = it },
                label = { Text("Scoring server") },
                singleLine = true,
                shape = MaterialTheme.shapes.small,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(Modifier.height(16.dp))
            Button(
                onClick = {
                    state = TrustState.Loading
                    scope.launch {
                        state = try {
                            val event = SessionBuilder.buildEvent(ctx, userId.trim())
                            val result = InnaitClient(serverUrl.trim()).score(event, userId.trim())
                            TrustState.Success(result)
                        } catch (e: Exception) {
                            TrustState.Error(e.message ?: "Could not reach the scoring server.")
                        }
                    }
                },
                enabled = state !is TrustState.Loading,
                shape = MaterialTheme.shapes.small,
                colors = ButtonDefaults.buttonColors(
                    containerColor = PrimaryContainer, contentColor = OnPrimaryContainer
                ),
                modifier = Modifier.fillMaxWidth().height(52.dp)
            ) {
                Text(
                    if (state is TrustState.Loading) "Scoring…" else "Run trust check",
                    style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Bold
                )
            }
            Spacer(Modifier.height(40.dp))
        }
    }
}

@Composable
private fun ScoreCard(state: TrustState) {
    Card(
        colors = CardDefaults.cardColors(containerColor = SurfaceContainerLowest),
        border = BorderStroke(1.dp, OutlineVariant),
        shape = MaterialTheme.shapes.large,
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(Modifier.padding(24.dp)) {
            when (state) {
                is TrustState.Idle -> Text(
                    "Run a check to score this session.",
                    style = MaterialTheme.typography.bodyLarge, color = OnSurfaceVariant
                )
                is TrustState.Loading -> Row(verticalAlignment = Alignment.CenterVertically) {
                    CircularProgressIndicator(color = Primary, strokeWidth = 2.dp,
                        modifier = Modifier.size(20.dp))
                    Spacer(Modifier.width(12.dp))
                    Text("Analysing behavior…", style = MaterialTheme.typography.bodyLarge)
                }
                is TrustState.Error -> {
                    Text("Couldn't score", style = MaterialTheme.typography.headlineSmall,
                        color = OnBackground)
                    Spacer(Modifier.height(8.dp))
                    Text(state.message, style = MaterialTheme.typography.bodyMedium,
                        color = OnErrorContainer)
                }
                is TrustState.Success -> ScoreResultView(state.result)
            }
        }
    }
}

@Composable
private fun ScoreResultView(r: ScoreResult) {
    Row(verticalAlignment = Alignment.Bottom) {
        Text("${r.trustScore}", fontSize = 64.sp, fontWeight = FontWeight.Bold,
            color = Primary, style = MaterialTheme.typography.displayLarge)
        Spacer(Modifier.width(6.dp))
        Text("/100", style = MaterialTheme.typography.bodyLarge, color = OnSurfaceVariant,
            modifier = Modifier.padding(bottom = 14.dp))
        Spacer(Modifier.weight(1f))
        ActionChip(r.action)
    }
    Spacer(Modifier.height(4.dp))
    Text("Risk: ${r.riskLevel}" + if (!r.baselinePresent) "  ·  cold start" else "",
        style = MaterialTheme.typography.labelSmall, color = OnSurfaceVariant)

    if (r.reasons.isNotEmpty()) {
        Spacer(Modifier.height(16.dp))
        Text("WHY", style = MaterialTheme.typography.labelLarge, color = OnSurfaceVariant)
        Spacer(Modifier.height(6.dp))
        r.reasons.take(6).forEach {
            Text("•  $it", style = MaterialTheme.typography.bodyMedium, color = OnSurface)
        }
    }
}

/** Chip coloured by action, 8px rounding per DESIGN.md chip spec. */
@Composable
private fun ActionChip(action: String) {
    val (bg, fg) = when (action) {
        "ALLOW" -> PrimaryContainer to OnPrimaryContainer
        "STEP_UP" -> TertiaryContainer to OnTertiaryContainer
        else -> ErrorContainer to OnErrorContainer
    }
    Surface(color = bg, shape = RoundedCornerShape(8.dp)) {
        Text(
            action,
            style = MaterialTheme.typography.labelLarge,
            color = fg,
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp)
        )
    }
}
