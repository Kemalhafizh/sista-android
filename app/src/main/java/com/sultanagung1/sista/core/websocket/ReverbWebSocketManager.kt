package com.sultanagung1.sista.core.websocket

import android.util.Log
import com.google.gson.Gson
import com.google.gson.JsonObject
import com.google.gson.JsonParser
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import okhttp3.*
import java.util.concurrent.TimeUnit

class ReverbWebSocketManager(
    private val host: String = "192.168.31.127",
    private val port: Int = 8080,
    private val appKey: String = "sulaone-super-key"
) : WebSocketListener() {

    private val TAG = "ReverbWebSocketManager"
    private val gson = Gson()
    private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())

    private var client: OkHttpClient = OkHttpClient.Builder()
        .readTimeout(0, TimeUnit.MILLISECONDS)
        .pingInterval(25, TimeUnit.SECONDS)
        .retryOnConnectionFailure(true)
        .build()

    private var webSocket: WebSocket? = null

    private val _connectionState = MutableStateFlow<ConnectionState>(ConnectionState.Disconnected)
    val connectionState: StateFlow<ConnectionState> = _connectionState.asStateFlow()

    private val _events = MutableSharedFlow<WebSocketEvent>(extraBufferCapacity = 64)
    val events: SharedFlow<WebSocketEvent> = _events.asSharedFlow()

    private var activeUserId: String? = null
    private var reconnectAttempts = 0
    private var isIntentionalClose = false

    fun connect(userId: String? = null) {
        activeUserId = userId
        isIntentionalClose = false
        _connectionState.value = ConnectionState.Connecting

        val wsUrl = "ws://$host:$port/app/$appKey?protocol=7&client=js&version=8.4.0-reverb&flash=false"
        val request = Request.Builder()
            .url(wsUrl)
            .build()

        webSocket = client.newWebSocket(request, this)
    }

    fun disconnect() {
        isIntentionalClose = true
        webSocket?.close(1000, "Normal closure")
        webSocket = null
        _connectionState.value = ConnectionState.Disconnected
    }

    override fun onOpen(webSocket: WebSocket, response: Response) {
        Log.d(TAG, "WebSocket Connected successfully to Laravel Reverb")
        _connectionState.value = ConnectionState.Connected
        reconnectAttempts = 0

        // Subscribe to public and emergency channels
        subscribeToChannel("school.announcements")
        subscribeToChannel("campus.emergency")

        // Subscribe to user personal channel if authenticated
        activeUserId?.let { uid ->
            subscribeToChannel("private-user.$uid")
        }
    }

    fun subscribeToChannel(channelName: String) {
        val subscribePayload = JsonObject().apply {
            addProperty("event", "pusher:subscribe")
            add("data", JsonObject().apply {
                addProperty("channel", channelName)
            })
        }
        webSocket?.send(subscribePayload.toString())
        Log.d(TAG, "Subscribed to channel: $channelName")
    }

    fun sendTypingStatus(conversationId: String, senderId: String, isTyping: Boolean) {
        val payload = JsonObject().apply {
            addProperty("event", "client-typing")
            addProperty("channel", "chat.$conversationId")
            add("data", JsonObject().apply {
                addProperty("conversation_id", conversationId)
                addProperty("sender_id", senderId)
                addProperty("is_typing", isTyping)
            })
        }
        webSocket?.send(payload.toString())
    }

    override fun onMessage(webSocket: WebSocket, text: String) {
        try {
            val json = JsonParser.parseString(text).asJsonObject
            val event = json.get("event")?.asString ?: return

            when (event) {
                "pusher:ping" -> {
                    val pong = JsonObject().apply { addProperty("event", "pusher:pong") }
                    webSocket.send(pong.toString())
                }
                "pusher:connection_established" -> {
                    Log.d(TAG, "Handshake established with socket_id: ${json.get("data")?.asString}")
                }
                "App\\Events\\NewChatMessageEvent", "new-message" -> {
                    val dataObj = parseDataPayload(json.get("data"))
                    scope.launch {
                        _events.emit(
                            WebSocketEvent.ChatMessageReceived(
                                messageId = dataObj.get("id")?.asString ?: System.currentTimeMillis().toString(),
                                conversationId = dataObj.get("conversation_id")?.asString ?: "1",
                                senderId = dataObj.get("sender_id")?.asString ?: "0",
                                senderName = dataObj.get("sender_name")?.asString ?: "Guru",
                                text = dataObj.get("text")?.asString ?: "",
                                timestamp = dataObj.get("timestamp")?.asString ?: "Baru saja"
                            )
                        )
                    }
                }
                "App\\Events\\LiveAttendanceLoggedEvent", "live-attendance" -> {
                    val dataObj = parseDataPayload(json.get("data"))
                    scope.launch {
                        _events.emit(
                            WebSocketEvent.LiveAttendanceRecorded(
                                studentId = dataObj.get("student_id")?.asString ?: "c1",
                                studentName = dataObj.get("student_name")?.asString ?: "Siswa",
                                checkInTime = dataObj.get("check_in_time")?.asString ?: "06:45 WIB",
                                status = dataObj.get("status")?.asString ?: "Hadir Tepat Waktu",
                                gate = dataObj.get("gate")?.asString ?: "Gerbang Utama"
                            )
                        )
                    }
                }
                "App\\Events\\EmergencyAlertEvent", "emergency-alert" -> {
                    val dataObj = parseDataPayload(json.get("data"))
                    scope.launch {
                        _events.emit(
                            WebSocketEvent.EmergencyAlertTriggered(
                                alertId = dataObj.get("id")?.asString ?: "em1",
                                title = dataObj.get("title")?.asString ?: "Peringatan Darurat",
                                message = dataObj.get("message")?.asString ?: "Harap tetap waspada.",
                                location = dataObj.get("location")?.asString ?: "Kampus SMA",
                                timestamp = dataObj.get("timestamp")?.asString ?: "Sekarang"
                            )
                        )
                    }
                }
                "App\\Events\\SchoolAnnouncementEvent", "new-announcement" -> {
                    val dataObj = parseDataPayload(json.get("data"))
                    scope.launch {
                        _events.emit(
                            WebSocketEvent.AnnouncementBroadcast(
                                announcementId = dataObj.get("id")?.asString ?: "ann1",
                                title = dataObj.get("title")?.asString ?: "Pengumuman Baru",
                                summary = dataObj.get("summary")?.asString ?: "",
                                category = dataObj.get("category")?.asString ?: "Umum",
                                priority = dataObj.get("priority")?.asString ?: "normal",
                                timestamp = dataObj.get("timestamp")?.asString ?: "Hari ini"
                            )
                        )
                    }
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error parsing WebSocket message: ${e.message}")
        }
    }

    private fun parseDataPayload(dataElement: com.google.gson.JsonElement?): JsonObject {
        if (dataElement == null) return JsonObject()
        return if (dataElement.isJsonObject) {
            dataElement.asJsonObject
        } else if (dataElement.isJsonPrimitive && dataElement.asJsonPrimitive.isString) {
            try {
                JsonParser.parseString(dataElement.asString).asJsonObject
            } catch (e: Exception) {
                JsonObject()
            }
        } else {
            JsonObject()
        }
    }

    override fun onFailure(webSocket: WebSocket, t: Throwable, response: Response?) {
        Log.e(TAG, "WebSocket Connection failed: ${t.message}")
        _connectionState.value = ConnectionState.Error(t.message ?: "Koneksi WebSocket terputus")
        if (!isIntentionalClose) {
            scheduleReconnect()
        }
    }

    override fun onClosed(webSocket: WebSocket, code: Int, reason: String) {
        Log.d(TAG, "WebSocket closed: $reason")
        _connectionState.value = ConnectionState.Disconnected
        if (!isIntentionalClose) {
            scheduleReconnect()
        }
    }

    private fun scheduleReconnect() {
        reconnectAttempts++
        val backoffSeconds = minOf(30L, (1L shl minOf(reconnectAttempts, 5)))
        Log.d(TAG, "Scheduling reconnect attempt $reconnectAttempts in $backoffSeconds seconds...")
        _connectionState.value = ConnectionState.Reconnecting(reconnectAttempts)

        scope.launch {
            delay(backoffSeconds * 1000)
            if (!isIntentionalClose) {
                connect(activeUserId)
            }
        }
    }
}
