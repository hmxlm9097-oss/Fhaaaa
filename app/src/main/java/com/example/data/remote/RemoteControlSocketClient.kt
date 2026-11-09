package com.example.data.remote

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import okhttp3.Request
import okhttp3.Response
import okhttp3.WebSocket
import okhttp3.WebSocketListener
import org.json.JSONObject

sealed interface SocketStatus {
    data object Disconnected : SocketStatus
    data object Connecting : SocketStatus
    data class Connected(val endpoint: String, val sessionId: String?) : SocketStatus
    data class Error(val message: String) : SocketStatus
}

data class RemoteSocketTelemetry(
    val status: SocketStatus = SocketStatus.Disconnected,
    val txPacketCount: Int = 0,
    val rxPacketCount: Int = 0,
    val lastMessage: String = "No active WebSocket stream",
    val activeDeviceName: String? = null
)

class RemoteControlSocketClient {
    private val _state = MutableStateFlow(RemoteSocketTelemetry())
    val state: StateFlow<RemoteSocketTelemetry> = _state.asStateFlow()

    private var webSocket: WebSocket? = null

    fun connect(wsUrl: String, authToken: String, deviceName: String) {
        val cleanUrl = wsUrl.trim()
        if (!cleanUrl.startsWith("ws://") && !cleanUrl.startsWith("wss://")) {
            _state.value = _state.value.copy(
                status = SocketStatus.Error("Invalid WebSocket URL (must start with ws:// or wss://)"),
                lastMessage = "Connection blocked: configure ws:// or wss:// endpoint in User Settings"
            )
            return
        }

        disconnect()
        _state.value = RemoteSocketTelemetry(
            status = SocketStatus.Connecting,
            lastMessage = "Opening WebSocket tunnel to $cleanUrl...",
            activeDeviceName = deviceName
        )

        val requestBuilder = Request.Builder().url(cleanUrl)
        if (authToken.isNotBlank()) {
            requestBuilder.addHeader("Authorization", "Bearer $authToken")
        }

        webSocket = NetworkClientFactory.okHttpClient.newWebSocket(
            requestBuilder.build(),
            object : WebSocketListener() {
                override fun onOpen(webSocket: WebSocket, response: Response) {
                    _state.value = _state.value.copy(
                        status = SocketStatus.Connected(cleanUrl, null),
                        lastMessage = "Tunnel open (${response.code})"
                    )
                }

                override fun onMessage(webSocket: WebSocket, text: String) {
                    val parsedSession = runCatching {
                        val json = JSONObject(text)
                        if (json.optString("type") == "session_ready") json.optString("sessionId") else null
                    }.getOrNull()

                    val currentStatus = _state.value.status
                    val nextStatus = if (parsedSession != null && currentStatus is SocketStatus.Connected) {
                        currentStatus.copy(sessionId = parsedSession)
                    } else currentStatus

                    _state.value = _state.value.copy(
                        status = nextStatus,
                        rxPacketCount = _state.value.rxPacketCount + 1,
                        lastMessage = text.take(180)
                    )
                }

                override fun onFailure(webSocket: WebSocket, t: Throwable, response: Response?) {
                    _state.value = _state.value.copy(
                        status = SocketStatus.Error(t.localizedMessage ?: "WebSocket connection failed"),
                        lastMessage = "Transport error: ${t.localizedMessage ?: response?.message ?: "Unreachable"}"
                    )
                }

                override fun onClosed(webSocket: WebSocket, code: Int, reason: String) {
                    _state.value = _state.value.copy(
                        status = SocketStatus.Disconnected,
                        lastMessage = "Closed ($code): ${reason.ifBlank { "Session ended" }}"
                    )
                }
            }
        )
    }

    fun sendCommand(payload: JSONObject): Boolean {
        val ws = webSocket
        val isConnected = _state.value.status is SocketStatus.Connected
        if (ws == null || !isConnected) {
            _state.value = _state.value.copy(
                lastMessage = "Command '${payload.optString("type")}' not dispatched — connect WebSocket gateway first"
            )
            return false
        }
        val sent = ws.send(payload.toString())
        if (sent) {
            _state.value = _state.value.copy(
                txPacketCount = _state.value.txPacketCount + 1,
                lastMessage = "TX: ${payload.optString("type")} -> dispatched"
            )
        }
        return sent
    }

    fun disconnect() {
        webSocket?.close(1000, "User disconnected remote session")
        webSocket = null
        _state.value = RemoteSocketTelemetry(
            status = SocketStatus.Disconnected,
            lastMessage = "Remote WebSocket session disconnected"
        )
    }
}
