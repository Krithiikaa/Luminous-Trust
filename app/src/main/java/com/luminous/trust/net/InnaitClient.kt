package com.luminous.trust.net

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL

/**
 * Minimal client for the InnaIT scoring service. No third-party HTTP library —
 * just HttpURLConnection + org.json, so there's nothing extra to keep in sync.
 *
 * POSTs { "event": {...}, "user_id": "..." } to <baseUrl>/score and parses the
 * decision. Runs on the IO dispatcher; call it from a coroutine.
 */
class InnaitClient(private val baseUrl: String) {

    suspend fun score(event: JSONObject, userId: String): ScoreResult =
        withContext(Dispatchers.IO) {
            val body = JSONObject().put("event", event).put("user_id", userId).toString()
            val url = URL(baseUrl.trimEnd('/') + "/score")
            val conn = (url.openConnection() as HttpURLConnection).apply {
                requestMethod = "POST"
                doOutput = true
                connectTimeout = 5000
                readTimeout = 5000
                setRequestProperty("Content-Type", "application/json")
            }
            try {
                conn.outputStream.use { it.write(body.toByteArray()) }
                val code = conn.responseCode
                val stream = if (code in 200..299) conn.inputStream else conn.errorStream
                val text = stream.bufferedReader().use { it.readText() }
                if (code !in 200..299) error("Server returned $code: $text")
                parse(text)
            } finally {
                conn.disconnect()
            }
        }

    private fun parse(text: String): ScoreResult {
        val o = JSONObject(text)
        val reasonsArr = o.optJSONArray("reasons")
        val reasons = buildList {
            if (reasonsArr != null) for (i in 0 until reasonsArr.length()) add(reasonsArr.getString(i))
        }
        return ScoreResult(
            trustScore = o.optInt("trust_score", 0),
            riskLevel = o.optString("risk_level", "HIGH"),
            action = o.optString("action", "BLOCK"),
            reasons = reasons,
            baselinePresent = o.optBoolean("baseline_present", false)
        )
    }
}
