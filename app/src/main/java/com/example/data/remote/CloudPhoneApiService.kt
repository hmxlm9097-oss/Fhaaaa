package com.example.data.remote

import okhttp3.MultipartBody
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part
import retrofit2.http.Path

interface CloudPhoneApiService {
    @GET("api/health")
    suspend fun getHealth(): HealthResponse

    @POST("api/auth/register")
    suspend fun register(@Body request: AuthRegisterRequest): AuthResponse

    @POST("api/auth/login")
    suspend fun login(@Body request: AuthLoginRequest): AuthResponse

    @POST("api/auth/verify-2fa")
    suspend fun verifyTwoFactor(
        @Header("Authorization") bearer: String,
        @Body request: VerifyTwoFactorRequest
    ): VerifyTwoFactorResponse

    @POST("api/auth/logout-all")
    suspend fun logoutAll(@Header("Authorization") bearer: String): LogoutAllResponse

    @GET("api/devices")
    suspend fun listDevices(@Header("Authorization") bearer: String): DevicesListResponse

    @POST("api/devices")
    suspend fun createDevice(
        @Header("Authorization") bearer: String,
        @Body request: CreateDeviceRequest
    ): SingleDeviceResponse

    @POST("api/devices/{id}/start")
    suspend fun startDevice(
        @Header("Authorization") bearer: String,
        @Path("id") id: String
    ): SingleDeviceResponse

    @POST("api/devices/{id}/stop")
    suspend fun stopDevice(
        @Header("Authorization") bearer: String,
        @Path("id") id: String
    ): SingleDeviceResponse

    @POST("api/devices/{id}/restart")
    suspend fun restartDevice(
        @Header("Authorization") bearer: String,
        @Path("id") id: String
    ): SingleDeviceResponse

    @DELETE("api/devices/{id}")
    suspend fun deleteDevice(
        @Header("Authorization") bearer: String,
        @Path("id") id: String
    ): DeleteDeviceResponse

    @Multipart
    @POST("api/files/upload")
    suspend fun uploadFile(
        @Header("Authorization") bearer: String,
        @Part file: MultipartBody.Part
    ): FileUploadResponse
}
