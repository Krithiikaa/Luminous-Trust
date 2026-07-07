package com.luminous.trust.capture

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Build
import android.os.IBinder
import kotlin.math.sqrt

/**
 * Phase 1 — always-on motion capture.
 *
 * A foreground service that listens to the accelerometer and gyroscope at
 * ~50 Hz, keeps a short rolling window of samples, and exposes a live summary
 * (micro-tremor, tilt) via [LatestMotion]. This is the on-device "sensory
 * surface": it only ever keeps derived statistics, never raw content.
 *
 * A real human holding a phone produces small but non-zero tremor. Perfect
 * stillness => device on a table / bot / spoof — the same signal the InnaIT
 * engine keys on as `unnatural_stillness`.
 */
class SensorCaptureService : Service(), SensorEventListener {

    private lateinit var sensorManager: SensorManager
    private var accel: Sensor? = null
    private var gyro: Sensor? = null

    // Rolling windows of recent magnitudes (last ~2s at 50 Hz).
    private val accelMag = RingBuffer(100)
    private val gyroMag = RingBuffer(100)
    private var lastTilt = 0f

    override fun onCreate() {
        super.onCreate()
        sensorManager = getSystemService(Context.SENSOR_SERVICE) as SensorManager
        accel = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
        gyro = sensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE)
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        startAsForeground()
        // SENSOR_DELAY_GAME ~= 50 Hz. Tune later for the battery/accuracy trade-off.
        accel?.let { sensorManager.registerListener(this, it, SensorManager.SENSOR_DELAY_GAME) }
        gyro?.let { sensorManager.registerListener(this, it, SensorManager.SENSOR_DELAY_GAME) }
        return START_STICKY
    }

    override fun onSensorChanged(e: SensorEvent) {
        val x = e.values[0]; val y = e.values[1]; val z = e.values[2]
        val mag = sqrt(x * x + y * y + z * z)
        when (e.sensor.type) {
            Sensor.TYPE_ACCELEROMETER -> {
                accelMag.add(mag)
                // crude tilt: deviation of Y/Z posture from upright hold
                lastTilt = kotlin.math.abs(Math.toDegrees(Math.atan2(x.toDouble(), z.toDouble())).toFloat())
                publish()
            }
            Sensor.TYPE_GYROSCOPE -> {
                gyroMag.add(mag)
                publish()
            }
        }
    }

    private fun publish() {
        LatestMotion.snapshot = MotionSnapshot(
            accelMicroTremor = accelMag.std(),
            gyroMicroTremor = gyroMag.std(),
            tilt = lastTilt,
            samplingHz = 50
        )
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}

    override fun onDestroy() {
        sensorManager.unregisterListener(this)
        super.onDestroy()
    }

    override fun onBind(intent: Intent?): IBinder? = null

    private fun startAsForeground() {
        val channelId = "luminous_capture"
        val nm = getSystemService(NotificationManager::class.java)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            nm.createNotificationChannel(
                NotificationChannel(
                    channelId, "Behavioral capture",
                    NotificationManager.IMPORTANCE_LOW
                )
            )
        }
        val n: Notification = Notification.Builder(this, channelId)
            .setContentTitle("Luminous Trust")
            .setContentText("Protecting this session")
            .setSmallIcon(android.R.drawable.ic_lock_idle_lock)
            .build()
        startForeground(1, n)
    }

    companion object {
        fun start(ctx: Context) {
            val i = Intent(ctx, SensorCaptureService::class.java)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) ctx.startForegroundService(i)
            else ctx.startService(i)
        }
    }
}

/** Latest derived motion summary, read by SessionBuilder at score time. */
data class MotionSnapshot(
    val accelMicroTremor: Float,
    val gyroMicroTremor: Float,
    val tilt: Float,
    val samplingHz: Int
)

object LatestMotion {
    @Volatile var snapshot: MotionSnapshot? = null
}

/** Tiny fixed-size circular buffer with a running standard deviation. */
private class RingBuffer(val cap: Int) {
    private val data = FloatArray(cap)
    private var size = 0
    private var head = 0
    fun add(v: Float) { data[head] = v; head = (head + 1) % cap; if (size < cap) size++ }
    fun std(): Float {
        if (size < 2) return 0f
        var mean = 0f
        for (i in 0 until size) mean += data[i]
        mean /= size
        var v = 0f
        for (i in 0 until size) { val d = data[i] - mean; v += d * d }
        return sqrt(v / size)
    }
}
