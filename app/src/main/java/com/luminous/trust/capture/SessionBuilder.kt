package com.luminous.trust.capture

import android.content.Context
import android.os.Build
import android.provider.Settings
import org.json.JSONArray
import org.json.JSONObject
import java.util.UUID

/**
 * Builds the session JSON that the InnaIT /score endpoint expects.
 *
 * v1 fills the motionHold block from live sensor data and leaves the touch /
 * keystroke blocks as zeros — matching the current server data shape. Wiring
 * real touch capture is the next milestone (it's the biggest signal gap).
 */
object SessionBuilder {

    @Suppress("HardwareIds")
    private fun installId(ctx: Context): String =
        Settings.Secure.getString(ctx.contentResolver, Settings.Secure.ANDROID_ID) ?: "unknown"

    fun buildEvent(ctx: Context, userId: String): JSONObject {
        val id = installId(ctx)
        val m = LatestMotion.snapshot

        val deviceFingerprint = JSONObject()
            .put("installId", id)
            .put("locale", "en-IN")
            .put("manufacturer", Build.MANUFACTURER)
            .put("model", Build.MODEL)
            .put("osVersion", "Android ${Build.VERSION.RELEASE} (API ${Build.VERSION.SDK_INT})")

        val integrity = JSONObject()
            .put("rooted", false)
            .put("emulator", false)
            .put("fridaDetected", false)
            .put("xposedDetected", false)
            .put("playIntegrityVerdict", "pending")

        val network = JSONObject()
            .put("proxyFlag", false).put("torFlag", false).put("vpnFlag", false)

        val geo = JSONObject()
            .put("accuracyMeters", 0.0)
            .put("impossibleTravelVelocityKmh", 0.0)

        val sim = JSONObject().put("simSwapDeltaHours", 0)

        val context = JSONObject()
            .put("deviceFingerprint", deviceFingerprint)
            .put("integrity", integrity)
            .put("network", network)
            .put("geo", geo)
            .put("sim", sim)

        val accelerometer = JSONObject()
            .put("microTremor", m?.accelMicroTremor ?: 0.0)
            .put("tilt", m?.tilt ?: 0.0)
            .put("samplingHz", m?.samplingHz ?: 0)
        val gyroscope = JSONObject()
            .put("microTremor", m?.gyroMicroTremor ?: 0.0)
            .put("tilt", 0.0)
            .put("samplingHz", m?.samplingHz ?: 0)

        val motionHold = JSONObject()
            .put("accelerometer", accelerometer)
            .put("gyroscope", gyroscope)
            .put("activityInference", "stationary")
            .put("handInference", "unknown")

        val session = JSONObject()
            .put("anomalyFlags", JSONObject()
                .put("accessibilityServiceActive", false)
                .put("screenShareActive", false)
                .put("screenshotDetected", false))
            .put("copyPasteSensitive", JSONObject()
                .put("amountFieldCopyPaste", false)
                .put("payeeFieldCopyPaste", false))
            .put("formFillCadence", JSONObject().put("pauseBeforeConfirmMs", 0))
            .put("navigationGraph", JSONObject().put("dwellTimePerScreenMs", JSONObject()))
            .put("transactionProfile", JSONObject()
                .put("amountBand", "5k-50k")
                .put("timeOfDayBucket", "evening"))

        // touch/keystroke — zeros for now (see README "Known gap: touch capture")
        val touchType = JSONObject()
            .put("touchGeometry", JSONObject()
                .put("sampleCount", 0).put("avgPressure", 0.0).put("avgContactArea", 0.0))
            .put("swipeKinematics", JSONObject()
                .put("swipeCount", 0).put("velocityX", 0.0).put("velocityY", 0.0).put("curvature", 0.0))
            .put("keystrokeDynamics", JSONObject()
                .put("keyDwellMs", JSONArray()).put("interKeyFlightMs", JSONArray()))
            .put("multiTouch", JSONObject().put("pinchZoomEvents", 0))

        return JSONObject()
            .put("installId", id)
            .put("userId", userId)
            .put("sessionId", UUID.randomUUID().toString())
            .put("timestampMs", System.currentTimeMillis())
            .put("context", context)
            .put("motionHold", motionHold)
            .put("session", session)
            .put("touchType", touchType)
    }
}
