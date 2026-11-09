package com.example.data.system

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationManager
import androidx.core.content.ContextCompat

data class CapturedLocationResult(
    val latitude: Double,
    val longitude: Double,
    val accuracyMeters: Float,
    val provider: String,
    val timestampMs: Long
)

class LocationCaptureProvider(private val context: Context) {

    fun hasLocationPermission(): Boolean {
        val fine = ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) ==
            PackageManager.PERMISSION_GRANTED
        val coarse = ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) ==
            PackageManager.PERMISSION_GRANTED
        return fine || coarse
    }

    @SuppressLint("MissingPermission")
    fun captureLastKnownLocation(): Result<CapturedLocationResult> {
        if (!hasLocationPermission()) {
            return Result.failure(SecurityException("Android Location permission has not been granted"))
        }
        val lm = context.getSystemService(Context.LOCATION_SERVICE) as? LocationManager
            ?: return Result.failure(IllegalStateException("LocationManager unavailable on this device"))

        val providers = lm.getProviders(true)
        var best: Location? = null
        for (provider in providers) {
            val candidate = runCatching { lm.getLastKnownLocation(provider) }.getOrNull() ?: continue
            if (best == null || candidate.accuracy < best.accuracy) {
                best = candidate
            }
        }

        val resolved = best ?: return Result.failure(
            IllegalStateException("No cached GPS/network fix available yet on this device or emulator. Set an emulator GPS coordinate or wait for a fix.")
        )

        return Result.success(
            CapturedLocationResult(
                latitude = resolved.latitude,
                longitude = resolved.longitude,
                accuracyMeters = resolved.accuracy,
                provider = resolved.provider ?: "system",
                timestampMs = resolved.time.takeIf { it > 0L } ?: System.currentTimeMillis()
            )
        )
    }
}
