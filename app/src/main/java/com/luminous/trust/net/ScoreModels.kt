package com.luminous.trust.net

/** Parsed response from POST /score. Mirrors the InnaIT engine's RiskDecision. */
data class ScoreResult(
    val trustScore: Int,
    val riskLevel: String,      // LOW | MEDIUM | HIGH
    val action: String,         // ALLOW | STEP_UP | BLOCK
    val reasons: List<String>,
    val baselinePresent: Boolean
)

/** UI-facing state for the trust screen. */
sealed interface TrustState {
    data object Idle : TrustState
    data object Loading : TrustState
    data class Success(val result: ScoreResult) : TrustState
    data class Error(val message: String) : TrustState
}
