package com.sultanagung1.sista.core.websocket

import android.util.Log
import com.google.gson.Gson
import com.google.gson.JsonObject
import com.google.gson.JsonParser
import com.sultanagung1.sista.core.storage.SessionManager
import com.sultanagung1.sista.core.util.Constants
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import okhttp3.*
import java.util.concurrent.TimeUnit

class ReverbWebSocketManager(
    private val sessionManager: SessionManager
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
    private var socketId: String? = null
    private var reconnectAttempts = 0
    private var isIntentionalClose = false
    private val pendingPrivateSubscriptions = mutableListOf<String>()

    fun connect(userId: String? = null) {
        activeUserId = userId
        isIntentionalClose = false
        _connectionState.value = ConnectionState.Connecting

        val wsUrl = "ws://${Constants.REVERB_HOST}:${Constants.REVERB_PORT}/app/" +
            "${Constants.REVERB_APP_KEY}?protocol=7&client=js&version=8.4.0-reverb&flash=false"
        val request = Request.Builder()
            .url(wsUrl)
            .build()

        webSocket = client.newWebSocket(request, this)
    }

    fun disconnect() {
        isIntentionalClose = true
        webSocket?.close(1000, "Normal closure")
        webSocket = null
        socketId = null
        pendingPrivateSubscriptions.clear()
        _connectionState.value = ConnectionState.Disconnected
    }

    override fun onOpen(webSocket: WebSocket, response: Response) {
        Log.d(TAG, "WebSocket Connected successfully to Laravel Reverb")
        _connectionState.value = ConnectionState.Connected
        reconnectAttempts = 0

        // Public channels — no auth needed, safe to subscribe immediately.
        subscribeToChannel("school.announcements")
        subscribeToChannel("campus.emergency")
    }

    /**
     * Subscribes to a Reverb channel. Private channels (wire name prefixed
     * "private-") require a signed `auth` string from the backend's
     * /broadcasting/auth endpoint before Reverb accepts the subscription — see
     * routes/channels.php on the backend. That endpoint needs the socket_id
     * from the pusher:connection_established handshake, so a private-channel
     * request made before the handshake completes is queued and retried once
     * socketId is known.
     */
    fun subscribeToChannel(channelName: String) {
        if (channelName.startsWith("private-")) {
            val sid = socketId
            if (sid == null) {
                pendingPrivateSubscriptions.add(channelName)
                return
            }
            scope.launch { authorizeAndSubscribe(channelName, sid) }
            return
        }

        val subscribePayload = JsonObject().apply {
            addProperty("event", "pusher:subscribe")
            add("data", JsonObject().apply {
                addProperty("channel", channelName)
            })
        }
        webSocket?.send(subscribePayload.toString())
        Log.d(TAG, "Subscribed to channel: $channelName")
    }

    private suspend fun authorizeAndSubscribe(channelName: String, sid: String) {
        val token = sessionManager.authTokenFlow.first()
        if (token.isNullOrBlank()) {
            Log.w(TAG, "Cannot subscribe to $channelName: user is not authenticated")
            return
        }

        try {
            val authUrl = Constants.SERVER_ROOT_URL.trimEnd('/') + "/broadcasting/auth"
            val authRequest = Request.Builder()
                .url(authUrl)
                .header("Authorization", "Bearer $token")
                .header("Accept", "application/json")
                .post(
                    FormBody.Builder()
                        .add("socket_id", sid)
                        .add("channel_name", channelName)
                        .build()
                )
                .build()

            client.newCall(authRequest).execute().use { response ->
                if (!response.isSuccessful) {
                    Log.w(TAG, "broadcasting/auth rejected $channelName: HTTP ${response.code}")
                    return
                }
                val authSignature = response.body?.string()
                    ?.let { runCatching { JsonParser.parseString(it).asJsonObject.get("auth")?.asString }.getOrNull() }
                if (authSignature.isNullOrBlank()) {
                    Log.w(TAG, "broadcasting/auth returned no signature for $channelName")
                    return
                }

                val subscribePayload = JsonObject().apply {
                    addProperty("event", "pusher:subscribe")
                    add("data", JsonObject().apply {
                        addProperty("channel", channelName)
                        addProperty("auth", authSignature)
                    })
                }
                webSocket?.send(subscribePayload.toString())
                Log.d(TAG, "Subscribed to private channel: $channelName")
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error authorizing private channel $channelName: ${e.message}")
        }
    }

    /**
     * [studentId] must be the real numeric student PK — the channel a client
     * event targets has to be the exact one the socket is subscribed to
     * (Pusher/Reverb protocol requirement), which for chat is always
     * "private-chat.{studentId}" (see openConversation()'s subscribeToChannel
     * call), never a synthetic id or the bare "chat.{id}" without the
     * "private-" prefix — a client event sent to a channel the socket isn't
     * actually a member of is silently dropped by Reverb.
     */
    fun sendTypingStatus(studentId: String, senderId: String, isTyping: Boolean) {
        val channel = "private-chat.$studentId"
        val payload = JsonObject().apply {
            addProperty("event", "client-typing")
            addProperty("channel", channel)
            add("data", JsonObject().apply {
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
                    val handshake = parseDataPayload(json.get("data"))
                    socketId = handshake.get("socket_id")?.asString
                    Log.d(TAG, "Handshake established with socket_id: $socketId")

                    activeUserId?.let { uid -> subscribeToChannel("private-user.$uid") }

                    if (pendingPrivateSubscriptions.isNotEmpty()) {
                        val queued = pendingPrivateSubscriptions.toList()
                        pendingPrivateSubscriptions.clear()
                        queued.forEach { subscribeToChannel(it) }
                    }
                }
                // AttendanceLoggedEvent broadcasts its two constructor properties
                // (userId, attendanceRecord) as top-level keys — see
                // App\Events\AttendanceLoggedEvent::broadcastOn()/broadcastAs()
                // on the backend (FASE 71.3, "Status Gerbang Real-time").
                "App\\Events\\AttendanceLoggedEvent", "attendance.logged" -> {
                    val record = parseDataPayload(json.get("data")).getAsJsonObject("attendanceRecord")
                        ?: return
                    scope.launch {
                        _events.emit(
                            WebSocketEvent.LiveAttendanceRecorded(
                                studentId = record.get("student_id")?.asString ?: return@launch,
                                studentName = record.get("student_name")?.asString ?: "",
                                checkInTime = record.get("check_in_time")?.asString ?: "",
                                status = record.get("status")?.asString ?: "",
                                gate = record.get("gate")?.asString ?: ""
                            )
                        )
                    }
                }
                // EmergencyBroadcastEvent broadcasts its single "emergencyData"
                // array property as one top-level key — see
                // App\Events\EmergencyBroadcastEvent on the backend (FASE 71.4,
                // "Tombol Siaran Darurat").
                "App\\Events\\EmergencyBroadcastEvent", "emergency.alert" -> {
                    val alert = parseDataPayload(json.get("data")).getAsJsonObject("emergencyData")
                        ?: return
                    scope.launch {
                        _events.emit(
                            WebSocketEvent.EmergencyAlertTriggered(
                                alertId = alert.get("id")?.asString ?: return@launch,
                                title = alert.get("title")?.asString ?: "",
                                message = alert.get("message")?.asString ?: "",
                                location = alert.get("location")?.asString ?: "",
                                timestamp = alert.get("timestamp")?.asString ?: ""
                            )
                        )
                    }
                }
                // CampusAnnouncementEvent broadcasts its single "announcementData"
                // array property as one top-level key — see
                // App\Events\CampusAnnouncementEvent on the backend.
                "App\\Events\\CampusAnnouncementEvent", "announcement.published" -> {
                    val announcement = parseDataPayload(json.get("data")).getAsJsonObject("announcementData")
                        ?: return
                    scope.launch {
                        _events.emit(
                            WebSocketEvent.AnnouncementBroadcast(
                                announcementId = announcement.get("id")?.asString ?: return@launch,
                                title = announcement.get("title")?.asString ?: "",
                                summary = announcement.get("summary")?.asString ?: "",
                                category = announcement.get("category")?.asString ?: "",
                                priority = announcement.get("priority")?.asString ?: "",
                                timestamp = announcement.get("timestamp")?.asString ?: ""
                            )
                        )
                    }
                }
                // ChatMessageSent broadcasts its single "message" property, the
                // ParentMessage model serialized via toArray() (plus the eager
                // -loaded "sender" relation for sender_name) — see
                // App\Events\ChatMessageSent on the backend.
                "App\\Events\\ChatMessageSent", "message-sent" -> {
                    val message = parseDataPayload(json.get("data")).getAsJsonObject("message")
                        ?: return
                    val senderId = message.get("sender_id")?.asString ?: return
                    val recipientId = message.get("recipient_id")?.asString ?: return
                    val studentId = message.get("student_id")?.asString ?: return
                    // Mirrors ChatRepository.conversationKey() exactly — "the other
                    // party, scoped to this child" — so an incoming push matches
                    // the same ConversationItem.id the REST-derived list already
                    // uses, not just the raw studentId (which would collide across
                    // different teachers consulted about the same child).
                    val otherPartyId = if (senderId == activeUserId) recipientId else senderId
                    scope.launch {
                        _events.emit(
                            WebSocketEvent.ChatMessageReceived(
                                messageId = message.get("id")?.asString ?: return@launch,
                                conversationId = "${otherPartyId}_$studentId",
                                senderId = senderId,
                                senderName = message.getAsJsonObject("sender")?.get("name")?.asString ?: "",
                                text = message.get("message")?.asString ?: "",
                                timestamp = message.get("created_at")?.asString ?: "",
                                attachmentUrl = message.get("attachment_url")?.takeIf { it.isJsonPrimitive }?.asString,
                                attachmentType = message.get("attachment_type")?.takeIf { it.isJsonPrimitive }?.asString
                            )
                        )
                    }
                }
                // A Pusher/Reverb client event (whisper) — Reverb relays it to every
                // other subscriber on the channel except the sender itself, so
                // whoever we receive this from is, by definition, always "the other
                // party" (no need to compare against activeUserId like above).
                // sendTypingStatus() sends this same shape.
                "client-typing" -> {
                    val channelName = json.get("channel")?.asString ?: return
                    val studentId = channelName.removePrefix("private-chat.")
                    val data = parseDataPayload(json.get("data"))
                    val senderId = data.get("sender_id")?.asString ?: return
                    val isTyping = data.get("is_typing")?.asBoolean ?: false
                    scope.launch {
                        _events.emit(
                            WebSocketEvent.UserTypingStatus(
                                conversationId = "${senderId}_$studentId",
                                senderId = senderId,
                                isTyping = isTyping
                            )
                        )
                    }
                }
                // ProctorInterventionEvent has no broadcastWith() override, so its
                // public constructor properties (examId, studentId, actionType,
                // message, extraMinutes, timestamp) are the top-level data keys —
                // see App\Events\ProctorInterventionEvent on the backend.
                "App\\Events\\ProctorInterventionEvent", "proctor-intervention" -> {
                    val data = parseDataPayload(json.get("data"))
                    val examId = data.get("examId")?.takeIf { it.isJsonPrimitive }?.asLong ?: return
                    val actionType = data.get("actionType")?.takeIf { it.isJsonPrimitive }?.asString ?: return
                    scope.launch {
                        _events.emit(
                            WebSocketEvent.ProctorInterventionReceived(
                                examId = examId,
                                actionType = actionType,
                                message = data.get("message")?.takeIf { it.isJsonPrimitive }?.asString,
                                extraMinutes = data.get("extraMinutes")?.takeIf { it.isJsonPrimitive }?.asInt
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
