package com.example.data.remote

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class HealthResponse(
    val status: String,
    val service: String? = null,
    val adbAvailable: Boolean = false,
    val activeDeviceCount: Int = 0,
    val timestamp: Long = 0L
)

@JsonClass(generateAdapter = true)
data class AuthRegisterRequest(
    val email: String,
    val password: String,
    val displayName: String
)

@JsonClass(generateAdapter = true)
data class AuthLoginRequest(
    val email: String,
    val password: String,
    val totpCode: String? = null
)

@JsonClass(generateAdapter = true)
data class AuthUserDto(
    val id: String,
    val email: String,
    val displayName: String,
    val emailVerified: Boolean = false,
    val twoFactorEnabled: Boolean = false,
    val plan: String = "Starter"
)

@JsonClass(generateAdapter = true)
data class AuthResponse(
    val token: String,
    val user: AuthUserDto,
    val totpOtpAuthUrl: String? = null
)

@JsonClass(generateAdapter = true)
data class VerifyTwoFactorRequest(
    val code: String,
    val enable: Boolean
)

@JsonClass(generateAdapter = true)
data class VerifyTwoFactorResponse(
    val verified: Boolean,
    val twoFactorEnabled: Boolean
)

@JsonClass(generateAdapter = true)
data class LogoutAllResponse(
    val revoked: Boolean
)

@JsonClass(generateAdapter = true)
data class CloudDeviceDto(
    val id: String,
    val ownerId: String? = null,
    val name: String,
    val androidVersion: String,
    val cpuCores: Int,
    val ramGb: Int,
    val storageGb: Int,
    val avdName: String? = null,
    val region: String? = null,
    val adbSerial: String? = null,
    val status: String,
    val createdAt: Long = 0L
)

@JsonClass(generateAdapter = true)
data class DevicesListResponse(
    val devices: List<CloudDeviceDto>
)

@JsonClass(generateAdapter = true)
data class SingleDeviceResponse(
    val device: CloudDeviceDto
)

@JsonClass(generateAdapter = true)
data class CreateDeviceRequest(
    val name: String,
    val androidVersion: String,
    val cpuCores: Int,
    val ramGb: Int,
    val storageGb: Int,
    val avdName: String,
    val region: String
)

@JsonClass(generateAdapter = true)
data class DeleteDeviceResponse(
    val deleted: Boolean
)

@JsonClass(generateAdapter = true)
data class FileUploadResponse(
    val fileId: String,
    val originalName: String,
    val size: Long,
    val mimeType: String? = null
)
