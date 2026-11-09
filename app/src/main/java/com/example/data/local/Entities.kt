package com.example.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.UUID

@Entity(tableName = "cloud_phones")
data class CloudPhoneEntity(
    @PrimaryKey val id: String = UUID.randomUUID().toString(),
    val name: String,
    val androidVersion: String,
    val cpuCores: Int,
    val ramGb: Int,
    val storageGb: Int,
    val region: String,
    val avdProfile: String,
    val resolution: String,
    val status: String, // LOCAL_PROFILE, STOPPED, STARTING, RUNNING
    val serverSynced: Boolean = false,
    val remoteId: String? = null,
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "activity_logs")
data class ActivityLogEntity(
    @PrimaryKey val id: String = UUID.randomUUID().toString(),
    val category: String, // SECURITY, CONTROL, TELEMETRY, DEVICE, FILES, SYSTEM
    val action: String,
    val detail: String,
    val severity: String = "INFO", // INFO, WARN, ALERT
    val timestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "location_history")
data class LocationHistoryEntity(
    @PrimaryKey val id: String = UUID.randomUUID().toString(),
    val latitude: Double,
    val longitude: Double,
    val accuracyMeters: Float,
    val provider: String,
    val timestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "file_transfers")
data class FileTransferEntity(
    @PrimaryKey val id: String = UUID.randomUUID().toString(),
    val fileName: String,
    val mimeType: String,
    val sizeBytes: Long,
    val uriString: String,
    val status: String, // QUEUED_LOCAL, UPLOADED, FAILED
    val remoteFileId: String? = null,
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "device_sessions")
data class DeviceSessionEntity(
    @PrimaryKey val id: String = UUID.randomUUID().toString(),
    val deviceName: String,
    val sessionType: String, // WEBSOCKET_CONTROL, WEBRTC_STREAM, LOCAL_DIAGNOSTIC
    val approvalState: String, // PENDING_APPROVAL, APPROVED, DISCONNECTED
    val endpointOrTransport: String,
    val createdAt: Long = System.currentTimeMillis()
)
