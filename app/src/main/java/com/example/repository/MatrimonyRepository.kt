package com.example.repository

import android.content.Context
import android.util.Log
import com.example.model.AppNotification
import com.example.model.ChatMessage
import com.example.model.InterestRequest
import com.example.model.Profile
import com.example.service.GeminiService
import com.example.service.NotificationHelper
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class MatrimonyRepository() {

    private val firestore: FirebaseFirestore by lazy {
        val instance = FirebaseFirestore.getInstance()
        try {
            val settings = com.google.firebase.firestore.FirebaseFirestoreSettings.Builder()
                .setPersistenceEnabled(true)
                .setCacheSizeBytes(com.google.firebase.firestore.FirebaseFirestoreSettings.CACHE_SIZE_UNLIMITED)
                .build()
            instance.firestoreSettings = settings
        } catch (e: Exception) {
            Log.w("MatrimonyRepository", "Firestore settings already set or error", e)
        }
        instance
    }

    private val _firestoreProfiles = MutableStateFlow<List<Profile>>(emptyList())
    private val _shortlistedIds = MutableStateFlow<Set<String>>(emptySet())
    private val _firestoreInterests = MutableStateFlow<List<InterestRequest>>(emptyList())
    private val _firestoreChatMessages = MutableStateFlow<List<ChatMessage>>(emptyList())
    private val _firestoreNotifications = MutableStateFlow<List<AppNotification>>(emptyList())

    val allProfiles: Flow<List<Profile>> = combine(_firestoreProfiles, _shortlistedIds, _firestoreInterests) { profiles, shortlisted, interests ->
        val authUid = com.example.service.FirebaseAuthService.currentUser?.uid ?: ""
        profiles.map { p ->
            val relInterest = interests.firstOrNull { req ->
                (req.senderId == p.id || req.receiverId == p.id) &&
                (req.senderId == authUid || req.receiverId == authUid || req.senderId == "USER_ME" || req.receiverId == "USER_ME")
            }
            val calculatedStatus = when {
                relInterest == null -> "NONE"
                relInterest.status == "REJECTED" -> "REJECTED"
                relInterest.status == "ACCEPTED" -> "ACCEPTED"
                relInterest.senderId == p.id -> "RECEIVED"
                else -> "SENT"
            }
            p.copy(
                isShortlisted = shortlisted.contains(p.id),
                interestStatus = calculatedStatus
            )
        }
    }

    val shortlistedProfiles: Flow<List<Profile>> = combine(_firestoreProfiles, _shortlistedIds, _firestoreInterests) { profiles, shortlisted, interests ->
        val authUid = com.example.service.FirebaseAuthService.currentUser?.uid ?: ""
        profiles.filter { shortlisted.contains(it.id) }.map { p ->
            val relInterest = interests.firstOrNull { req ->
                (req.senderId == p.id || req.receiverId == p.id) &&
                (req.senderId == authUid || req.receiverId == authUid || req.senderId == "USER_ME" || req.receiverId == "USER_ME")
            }
            val calculatedStatus = when {
                relInterest == null -> "NONE"
                relInterest.status == "REJECTED" -> "REJECTED"
                relInterest.status == "ACCEPTED" -> "ACCEPTED"
                relInterest.senderId == p.id -> "RECEIVED"
                else -> "SENT"
            }
            p.copy(
                isShortlisted = true,
                interestStatus = calculatedStatus
            )
        }
    }

    private fun mergeAndSetInterests(remoteInterests: List<InterestRequest>) {
        val currentLocal = _firestoreInterests.value
        val mergedMap = currentLocal.associateBy { it.id }.toMutableMap()
        remoteInterests.forEach { remote ->
            mergedMap[remote.id] = remote
        }
        _firestoreInterests.value = mergedMap.values.toList()
    }

    fun getAllInterestsForUser(userId: String): Flow<List<InterestRequest>> {
        val authUid = com.example.service.FirebaseAuthService.currentUser?.uid ?: ""
        return _firestoreInterests.map { list ->
            list.filter { p: InterestRequest ->
                p.senderId == userId || p.receiverId == userId ||
                (authUid.isNotBlank() && (p.senderId == authUid || p.receiverId == authUid)) ||
                (userId == "USER_ME" || p.senderId == "USER_ME" || p.receiverId == "USER_ME")
            }
        }
    }

    suspend fun getMutualInterestStatus(user1: String, user2: String): String {
        val authUid = com.example.service.FirebaseAuthService.currentUser?.uid ?: ""
        val request = _firestoreInterests.value.firstOrNull { p: InterestRequest ->
            (p.senderId == user1 && p.receiverId == user2) || (p.senderId == user2 && p.receiverId == user1) ||
            (authUid.isNotBlank() && ((p.senderId == authUid && (p.receiverId == user1 || p.receiverId == user2)) || (p.receiverId == authUid && (p.senderId == user1 || p.senderId == user2))))
        }
        return request?.status ?: "NONE"
    }

    suspend fun sendInterestRequest(senderId: String, receiverId: String, senderName: String, receiverName: String) {
        val authUid = com.example.service.FirebaseAuthService.currentUser?.uid ?: ""
        val effectiveSenderId = if (senderId.isBlank() || senderId == "USER_ME") authUid.ifBlank { "USER_ME" } else senderId

        val senderProf = _firestoreProfiles.value.firstOrNull { it.id == effectiveSenderId }
        val receiverProf = _firestoreProfiles.value.firstOrNull { it.id == receiverId }
        val existingInterest = _firestoreInterests.value.firstOrNull {
            (it.senderId == effectiveSenderId && it.receiverId == receiverId) || (it.senderId == receiverId && it.receiverId == effectiveSenderId)
        }
        if (senderProf?.blockedUserIds?.contains(receiverId) == true ||
            receiverProf?.blockedUserIds?.contains(effectiveSenderId) == true ||
            existingInterest?.status == "BLOCKED") {
            Log.w("MatrimonyRepository", "Cannot send interest request: User $receiverId is blocked")
            return
        }

        val id = if (effectiveSenderId < receiverId) "${effectiveSenderId}_${receiverId}" else "${receiverId}_${effectiveSenderId}"

        println("CONSOLE_LOG [sendInterestRequest] Called with senderId='$senderId', receiverId='$receiverId', senderName='$senderName', receiverName='$receiverName'")
        println("CONSOLE_LOG [sendInterestRequest] FirebaseAuth currentUser uid='$authUid'")
        println("CONSOLE_LOG [sendInterestRequest] Calculated effectiveSenderId='$effectiveSenderId', document ID='$id'")

        val realSenderName = senderName.ifBlank {
            _firestoreProfiles.value.find { it.id == effectiveSenderId }?.fullName ?: "મારો બાયોડેટા"
        }
        val realReceiverName = receiverName.ifBlank {
            _firestoreProfiles.value.find { it.id == receiverId }?.fullName ?: "સભ્ય"
        }

        val req = InterestRequest(
            id = id,
            senderId = effectiveSenderId,
            receiverId = receiverId,
            senderName = realSenderName,
            receiverName = realReceiverName,
            status = "PENDING",
            timestamp = System.currentTimeMillis()
        )
        val current = _firestoreInterests.value.toMutableList()
        current.removeAll { p: InterestRequest -> p.id == id }
        current.add(req)
        _firestoreInterests.value = current
        updateInterestStatus(receiverId, "SENT")

        println("CONSOLE_LOG [sendInterestRequest] Updated local _firestoreInterests state with new InterestRequest(id='$id', status='PENDING')")

        kotlinx.coroutines.withContext(Dispatchers.IO) {
            try {
                val map = mapOf(
                    "id" to id,
                    "senderId" to effectiveSenderId,
                    "sender_id" to effectiveSenderId,
                    "receiverId" to receiverId,
                    "receiver_id" to receiverId,
                    "senderName" to realSenderName,
                    "sender_name" to realSenderName,
                    "receiverName" to realReceiverName,
                    "receiver_name" to realReceiverName,
                    "status" to "PENDING",
                    "timestamp" to System.currentTimeMillis()
                )
                println("CONSOLE_LOG [sendInterestRequest] Attempting to write to Firestore collection 'interest_requests', docId='$id' with payload: $map")
                Log.d("MatrimonyRepository", "CONSOLE_LOG: Writing interest request $id to Firestore: $map")

                firestore.collection("interest_requests").document(id)
                    .set(map, SetOptions.merge())
                    .await()

                println("CONSOLE_LOG [sendInterestRequest] SUCCESS! Document '$id' successfully written to Firestore 'interest_requests' collection.")
                Log.i("MatrimonyRepository", "CONSOLE_LOG SUCCESS: Written interest request $id to Firestore interest_requests collection")

                val notif = AppNotification(
                    id = "notif_int_${id}_${System.currentTimeMillis()}",
                    userId = receiverId,
                    title = "❤️ નવો રસ-પ્રસ્તાવ (New Interest)",
                    message = "${realSenderName} એ તમારી પ્રોફાઇલમાં રસ દર્શાવ્યો છે.",
                    type = "INTEREST",
                    targetId = effectiveSenderId,
                    isRead = false,
                    timestamp = System.currentTimeMillis()
                )
                saveNotificationToFirestore(notif)
            } catch (e: Exception) {
                println("CONSOLE_LOG [sendInterestRequest] ERROR! Exception while writing to Firestore 'interest_requests' collection: ${e.localizedMessage}")
                e.printStackTrace()
                Log.e("MatrimonyRepository", "CONSOLE_LOG ERROR: Error saving interest request $id to Firestore", e)
            }
        }
    }

    suspend fun acceptInterestRequest(requestId: String) {
        updateInterestRequestStatus(requestId, "ACCEPTED")
    }

    suspend fun updateInterestRequestStatus(requestId: String, status: String) {
        val current = _firestoreInterests.value.toMutableList()
        val idx = current.indexOfFirst { p: InterestRequest -> p.id == requestId }
        val existingReq = if (idx >= 0) current[idx] else null
        if (idx >= 0) {
            current[idx] = current[idx].copy(status = status)
            _firestoreInterests.value = current
        }
        kotlinx.coroutines.withContext(Dispatchers.IO) {
            try {
                val map = mutableMapOf<String, Any>(
                    "id" to requestId,
                    "status" to status,
                    "timestamp" to System.currentTimeMillis()
                )
                if (existingReq != null) {
                    map["senderId"] = existingReq.senderId
                    map["sender_id"] = existingReq.senderId
                    map["receiverId"] = existingReq.receiverId
                    map["receiver_id"] = existingReq.receiverId
                    map["senderName"] = existingReq.senderName
                    map["sender_name"] = existingReq.senderName
                    map["receiverName"] = existingReq.receiverName
                    map["receiver_name"] = existingReq.receiverName
                }
                firestore.collection("interest_requests").document(requestId)
                    .set(map, SetOptions.merge())
                    .await()
                Log.i("MatrimonyRepository", "Successfully updated interest request status $requestId to $status in Firestore")
            } catch (e: Exception) {
                Log.e("MatrimonyRepository", "Error updating interest request status in Firestore", e)
            }
        }
    }

    suspend fun removeInterestAndBlockUser(myId: String, targetProfileId: String) {
        val authUid = com.example.service.FirebaseAuthService.currentUser?.uid ?: ""
        val effectiveMyId = if (myId.isBlank() || myId == "USER_ME") authUid.ifBlank { "USER_ME" } else myId

        val currentMyProf = _firestoreProfiles.value.firstOrNull { it.id == effectiveMyId }
        if (currentMyProf != null && !currentMyProf.blockedUserIds.contains(targetProfileId)) {
            val updatedBlocked = currentMyProf.blockedUserIds + targetProfileId
            val updatedProf = currentMyProf.copy(blockedUserIds = updatedBlocked)
            saveProfile(updatedProf)
        }

        val existing = _firestoreInterests.value.firstOrNull { p: InterestRequest ->
            (p.senderId == effectiveMyId && p.receiverId == targetProfileId) || (p.senderId == targetProfileId && p.receiverId == effectiveMyId)
        }
        val requestId = existing?.id ?: (if (effectiveMyId < targetProfileId) "${effectiveMyId}_${targetProfileId}" else "${targetProfileId}_${effectiveMyId}")
        val updatedReq = InterestRequest(
            id = requestId,
            senderId = existing?.senderId ?: effectiveMyId,
            receiverId = existing?.receiverId ?: targetProfileId,
            senderName = existing?.senderName ?: "",
            receiverName = existing?.receiverName ?: "",
            status = "BLOCKED",
            timestamp = System.currentTimeMillis()
        )
        val current = _firestoreInterests.value.toMutableList()
        current.removeAll { p: InterestRequest -> p.id == requestId }
        current.add(updatedReq)
        _firestoreInterests.value = current
        updateInterestStatus(targetProfileId, "NONE")

        kotlinx.coroutines.withContext(Dispatchers.IO) {
            try {
                val map = mapOf(
                    "id" to requestId,
                    "senderId" to updatedReq.senderId,
                    "sender_id" to updatedReq.senderId,
                    "receiverId" to updatedReq.receiverId,
                    "receiver_id" to updatedReq.receiverId,
                    "senderName" to updatedReq.senderName,
                    "sender_name" to updatedReq.senderName,
                    "receiverName" to updatedReq.receiverName,
                    "receiver_name" to updatedReq.receiverName,
                    "status" to "BLOCKED",
                    "timestamp" to System.currentTimeMillis()
                )
                firestore.collection("interest_requests").document(requestId)
                    .set(map, SetOptions.merge())
                    .await()
                Log.i("MatrimonyRepository", "Successfully blocked user $targetProfileId and set interest request status to BLOCKED")
            } catch (e: Exception) {
                Log.e("MatrimonyRepository", "Error blocking user in Firestore", e)
            }
        }
    }

    suspend fun unblockUser(myId: String, targetProfileId: String) {
        val authUid = com.example.service.FirebaseAuthService.currentUser?.uid ?: ""
        val effectiveMyId = if (myId.isBlank() || myId == "USER_ME") authUid.ifBlank { "USER_ME" } else myId

        val currentMyProf = _firestoreProfiles.value.firstOrNull { it.id == effectiveMyId }
        if (currentMyProf != null) {
            val updatedBlocked = currentMyProf.blockedUserIds.filter { it != targetProfileId }
            val updatedProf = currentMyProf.copy(blockedUserIds = updatedBlocked)
            saveProfile(updatedProf)
        }

        val requestId = if (effectiveMyId < targetProfileId) "${effectiveMyId}_${targetProfileId}" else "${targetProfileId}_${effectiveMyId}"
        val current = _firestoreInterests.value.toMutableList()
        current.removeAll { p: InterestRequest -> p.id == requestId }
        _firestoreInterests.value = current
        updateInterestStatus(targetProfileId, "NONE")

        kotlinx.coroutines.withContext(Dispatchers.IO) {
            try {
                firestore.collection("interest_requests").document(requestId).delete().await()
                Log.i("MatrimonyRepository", "Successfully unblocked user $targetProfileId in Firestore")
            } catch (e: Exception) {
                Log.e("MatrimonyRepository", "Error deleting blocked interest request on unblock", e)
            }
        }
    }

    private var isListenerActive = false
    private var syncStartTime = System.currentTimeMillis() - 5000L
    private var lastNotifiedLoginTimestamp = 0L

    fun startRealtimeSync(
        context: Context,
        coroutineScope: kotlinx.coroutines.CoroutineScope,
        currentUserIdProvider: () -> String,
        currentDeviceId: String
    ) {
        if (isListenerActive) return
        isListenerActive = true
        NotificationHelper.initNotificationChannel(context)

        try {
            // 1. Profiles listener
            firestore.collection("profiles")
                .addSnapshotListener { snapshot, error ->
                    if (error != null) {
                        Log.e("MatrimonyRepository", "Firestore profile real-time sync error", error)
                        return@addSnapshotListener
                    }
                    if (snapshot != null && !snapshot.isEmpty) {
                        coroutineScope.launch(Dispatchers.IO) {
                            val remoteProfiles = snapshot.documents.mapNotNull { parseProfileFromDoc(it) }
                            _firestoreProfiles.value = remoteProfiles

                            val myUid = currentUserIdProvider()
                            val myRemoteProfile = remoteProfiles.firstOrNull { it.id == myUid }
                            if (myRemoteProfile != null &&
                                myRemoteProfile.lastDeviceId.isNotBlank() &&
                                myRemoteProfile.lastDeviceId != currentDeviceId &&
                                myRemoteProfile.lastLoginTimestamp > syncStartTime &&
                                myRemoteProfile.lastLoginTimestamp != lastNotifiedLoginTimestamp
                            ) {
                                lastNotifiedLoginTimestamp = myRemoteProfile.lastLoginTimestamp
                                val title = "⚠️ નવું લૉગિન મળ્યું! (New Device Login)"
                                val deviceName = myRemoteProfile.lastDeviceName.ifBlank { "બીજી ડિવાઇસ" }
                                val body = "તમારું એકાઉન્ટ ${deviceName} પર લૉગિન થયેલું જણાયું છે."
                                NotificationHelper.showSystemPushNotification(context, title, body)
                                saveNotificationToFirestore(
                                    AppNotification(
                                        id = "notif_login_${myRemoteProfile.lastLoginTimestamp}",
                                        userId = myUid,
                                        title = title,
                                        message = body,
                                        type = "LOGIN_ALERT",
                                        targetId = "",
                                        isRead = false,
                                        timestamp = myRemoteProfile.lastLoginTimestamp
                                    )
                                )
                            }
                        }
                    }
                }

            // 2. Interest Requests listener
            firestore.collection("interest_requests")
                .addSnapshotListener { snapshot, error ->
                    if (error != null) return@addSnapshotListener
                    if (snapshot != null) {
                        coroutineScope.launch(Dispatchers.IO) {
                            val remoteInterests = snapshot.documents.mapNotNull { parseInterestFromDoc(it) }
                            mergeAndSetInterests(remoteInterests)

                            val myUid = currentUserIdProvider()
                            remoteInterests.forEach { req ->
                                if (req.timestamp > syncStartTime) {
                                    if (req.receiverId == myUid && req.status == "PENDING") {
                                        val title = "❤️ નવો રસ-પ્રસ્તાવ (New Interest Request)"
                                        val body = "${req.senderName.ifBlank { "એક સભ્ય" }} એ તમારી પ્રોફાઇલમાં રસ દર્શાવ્યો છે."
                                        NotificationHelper.showSystemPushNotification(context, title, body)
                                        saveNotificationToFirestore(
                                            AppNotification(
                                                id = "notif_int_${req.id}_${req.status}",
                                                userId = myUid,
                                                title = title,
                                                message = body,
                                                type = "INTEREST",
                                                targetId = req.senderId,
                                                isRead = false,
                                                timestamp = req.timestamp
                                            )
                                        )
                                    } else if (req.senderId == myUid && req.status == "ACCEPTED") {
                                        val title = "🎉 પ્રસ્તાવ સ્વીકારાયો! (Interest Accepted)"
                                        val body = "${req.receiverName.ifBlank { "સામેના સભ્ય" }} એ તમારો રસ-પ્રસ્તાવ સ્વીકાર્યો છે. હવે ચેટ કરો!"
                                        NotificationHelper.showSystemPushNotification(context, title, body)
                                        saveNotificationToFirestore(
                                            AppNotification(
                                                id = "notif_int_${req.id}_ACCEPTED",
                                                userId = myUid,
                                                title = title,
                                                message = body,
                                                type = "INTEREST",
                                                targetId = req.receiverId,
                                                isRead = false,
                                                timestamp = req.timestamp
                                            )
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

            // 3. Chat Messages listener
            firestore.collection("chat_messages")
                .addSnapshotListener { snapshot, error ->
                    if (error != null) return@addSnapshotListener
                    if (snapshot != null) {
                        coroutineScope.launch(Dispatchers.IO) {
                            val remoteMsgs = snapshot.documents.mapNotNull { parseChatMessageFromDoc(it) }
                            _firestoreChatMessages.value = remoteMsgs

                            val myUid = currentUserIdProvider()
                            remoteMsgs.forEach { msg ->
                                if (msg.receiverId == myUid && msg.senderId != myUid && msg.timestampMs > syncStartTime) {
                                    val title = "💬 નવો મેસેજ: ${msg.senderName.ifBlank { "સંદેશ" }}"
                                    val body = msg.text
                                    NotificationHelper.showSystemPushNotification(context, title, body)
                                    saveNotificationToFirestore(
                                        AppNotification(
                                            id = "notif_chat_${msg.id}",
                                            userId = myUid,
                                            title = title,
                                            message = body,
                                            type = "CHAT",
                                            targetId = msg.senderId,
                                            isRead = false,
                                            timestamp = msg.timestampMs
                                        )
                                    )
                                }
                            }
                        }
                    }
                }

            // 4. Shortlists listener
            firestore.collection("shortlists")
                .addSnapshotListener { snapshot, error ->
                    if (error != null) return@addSnapshotListener
                    if (snapshot != null) {
                        coroutineScope.launch(Dispatchers.IO) {
                            val currentUid = currentUserIdProvider()
                            val shortlistedProfileIds = snapshot.documents
                                .filter { doc -> doc.get("userId")?.toString() == currentUid }
                                .mapNotNull { doc -> doc.get("profileId")?.toString() }
                                .toSet()
                            _shortlistedIds.value = shortlistedProfileIds
                        }
                    }
                }

            // 5. Notifications listener
            firestore.collection("notifications")
                .addSnapshotListener { snapshot, error ->
                    if (error != null) return@addSnapshotListener
                    if (snapshot != null) {
                        coroutineScope.launch(Dispatchers.IO) {
                            val remoteNotifs = snapshot.documents.mapNotNull { parseNotificationFromDoc(it) }
                            _firestoreNotifications.value = remoteNotifs
                        }
                    }
                }
        } catch (e: Exception) {
            Log.e("MatrimonyRepository", "Error starting real-time sync", e)
        }
    }

    private fun parseInterestFromDoc(doc: com.google.firebase.firestore.DocumentSnapshot): InterestRequest? {
        val senderId = doc.get("senderId")?.toString() ?: doc.get("sender_id")?.toString() ?: ""
        val receiverId = doc.get("receiverId")?.toString() ?: doc.get("receiver_id")?.toString() ?: ""
        if (senderId.isBlank() || receiverId.isBlank()) return null
        val id = doc.get("id")?.toString() ?: doc.id
        val ts = (doc.get("timestamp") as? Number)?.toLong() ?: System.currentTimeMillis()
        return InterestRequest(
            id = id,
            senderId = senderId,
            receiverId = receiverId,
            senderName = doc.get("senderName")?.toString() ?: doc.get("sender_name")?.toString() ?: "",
            receiverName = doc.get("receiverName")?.toString() ?: doc.get("receiver_name")?.toString() ?: "",
            status = doc.get("status")?.toString() ?: "PENDING",
            timestamp = ts
        )
    }

    private fun parseChatMessageFromDoc(doc: com.google.firebase.firestore.DocumentSnapshot): ChatMessage? {
        val senderId = doc.get("senderId")?.toString() ?: ""
        val receiverId = doc.get("receiverId")?.toString() ?: ""
        val text = doc.get("text")?.toString() ?: ""
        if (senderId.isBlank() && text.isBlank()) return null
        val id = doc.get("id")?.toString() ?: doc.id
        val chatRoomId = doc.get("chatRoomId")?.toString() ?: (if (senderId < receiverId) "${senderId}_${receiverId}" else "${receiverId}_${senderId}")
        val tsMs = (doc.get("timestampMs") as? Number)?.toLong() ?: System.currentTimeMillis()

        val currentUid = com.example.service.FirebaseAuthService.currentUser?.uid
        val isUserSenderDoc = doc.get("isUserSender") as? Boolean ?: false
        val isUserSender = isUserSenderDoc || (senderId.isNotBlank() && currentUid != null && senderId == currentUid)

        return ChatMessage(
            id = id,
            chatRoomId = chatRoomId,
            senderId = senderId,
            receiverId = receiverId,
            profileId = receiverId,
            senderName = doc.get("senderName")?.toString() ?: "",
            isUserSender = isUserSender,
            text = text,
            timestamp = doc.get("formattedTime")?.toString() ?: doc.get("timestamp")?.toString() ?: "",
            timestampMs = tsMs,
            isVoiceNote = doc.get("isVoiceNote") as? Boolean ?: false,
            voiceDurationSec = (doc.get("voiceDurationSec") as? Number)?.toInt() ?: 0
        )
    }

    private fun parseProfileFromDoc(doc: com.google.firebase.firestore.DocumentSnapshot): Profile? {
        val fullName = doc.get("fullName")?.toString() ?: ""
        if (fullName.isBlank()) return null

        val isApprovedObj = doc.get("isApproved")
        val isApproved = when (isApprovedObj) {
            is Boolean -> isApprovedObj
            is String -> isApprovedObj.equals("true", ignoreCase = true) || isApprovedObj.equals("approved", ignoreCase = true) || isApprovedObj == "1"
            is Number -> isApprovedObj.toInt() == 1
            else -> false
        }

        val isRejectedObj = doc.get("isRejected")
        val isRejected = when (isRejectedObj) {
            is Boolean -> isRejectedObj
            is String -> isRejectedObj.equals("true", ignoreCase = true) || isRejectedObj.equals("rejected", ignoreCase = true) || isRejectedObj == "1"
            is Number -> isRejectedObj.toInt() == 1
            else -> false
        }
        val rejectionReason = doc.get("rejectionReason")?.toString() ?: ""

        val isAadharObj = doc.get("isAadharVerified")
        val isAadharVerified = isApproved || when (isAadharObj) {
            is Boolean -> isAadharObj
            is String -> isAadharObj.equals("true", ignoreCase = true)
            else -> false
        }

        val ageObj = doc.get("age")
        val age = when (ageObj) {
            is Number -> ageObj.toInt()
            is String -> ageObj.toIntOrNull() ?: 25
            else -> 25
        }

        val numBrothersObj = doc.get("numBrothers")
        val numBrothers = when (numBrothersObj) {
            is Number -> numBrothersObj.toInt()
            is String -> numBrothersObj.toIntOrNull() ?: 0
            else -> 0
        }
        val numSistersObj = doc.get("numSisters")
        val numSisters = when (numSistersObj) {
            is Number -> numSistersObj.toInt()
            is String -> numSistersObj.toIntOrNull() ?: 0
            else -> 0
        }

        val parsedProfile = Profile(
            id = doc.get("id")?.toString() ?: doc.id,
            fullName = fullName,
            fatherName = doc.get("fatherName")?.toString() ?: "",
            motherName = doc.get("motherName")?.toString() ?: "",
            grandfatherName = doc.get("grandfatherName")?.toString() ?: "",
            fatherOccupation = doc.get("fatherOccupation")?.toString() ?: "",
            motherOccupation = doc.get("motherOccupation")?.toString() ?: "",
            numBrothers = numBrothers,
            brothersNames = doc.get("brothersNames")?.toString() ?: "",
            numSisters = numSisters,
            sistersNames = doc.get("sistersNames")?.toString() ?: "",
            gol = doc.get("gol")?.toString() ?: "",
            hobbies = doc.get("hobbies")?.toString() ?: "",
            gender = doc.get("gender")?.toString() ?: "Bride",
            age = age,
            height = doc.get("height")?.toString() ?: "5'6\"",
            weight = doc.get("weight")?.toString() ?: "60 kg",
            bloodGroup = doc.get("bloodGroup")?.toString() ?: "",
            isNri = doc.getBoolean("isNri") ?: false,
            hasMaritalHistory = doc.getBoolean("hasMaritalHistory") ?: false,
            subCaste = doc.get("subCaste")?.toString() ?: "Jat Chaudhary",
            gotra = doc.get("gotra")?.toString() ?: "",
            motherGotra = doc.get("motherGotra")?.toString() ?: "",
            locality = doc.get("locality")?.toString() ?: "Haryana",
            nativeVillage = doc.get("nativeVillage")?.toString() ?: "",
            motherBirthVillage = doc.get("motherBirthVillage")?.toString() ?: "",
            education = doc.get("education")?.toString() ?: "",
            occupation = doc.get("occupation")?.toString() ?: "",
            currentCity = doc.get("currentCity")?.toString() ?: "",
            monthlyIncome = doc.get("monthlyIncome")?.toString() ?: "",
            maritalStatus = doc.get("maritalStatus")?.toString() ?: "Never Married",
            aboutMe = doc.get("aboutMe")?.toString() ?: "",
            familyDetails = doc.get("familyDetails")?.toString() ?: "",
            isAadharVerified = isAadharVerified,
            aadharMasked = doc.get("aadharMasked")?.toString() ?: "XXXX-XXXX-1234",
            phoneContact = doc.get("phoneContact")?.toString() ?: "",
            parentPhoneContact = doc.get("parentPhoneContact")?.toString() ?: "",
            profileImageUrl = (doc.get("profileImageUrl")?.toString()
                ?: doc.get("photoUrl")?.toString()
                ?: doc.get("photo_url")?.toString()
                ?: doc.get("imageUrl")?.toString()
                ?: doc.get("image_url")?.toString()
                ?: doc.get("photo")?.toString()
                ?: doc.get("avatarUrl")?.toString()
                ?: doc.get("avatar")?.toString()
                ?: "").trim(),
            aadharFrontUrl = doc.get("aadharFrontUrl")?.toString() ?: "",
            aadharBackUrl = doc.get("aadharBackUrl")?.toString() ?: "",
            rashi = doc.get("rashi")?.toString() ?: "",
            nakshatra = doc.get("nakshatra")?.toString() ?: "",
            manglikStatus = doc.get("manglikStatus")?.toString() ?: "Non-Manglik",
            birthDate = doc.get("birthDate")?.toString() ?: "",
            birthTime = doc.get("birthTime")?.toString() ?: "",
            birthPlace = doc.get("birthPlace")?.toString() ?: "",
            isApproved = isApproved,
            isRejected = isRejected,
            rejectionReason = rejectionReason,
            lastDeviceId = doc.get("lastDeviceId")?.toString() ?: "",
            lastLoginTimestamp = (doc.get("lastLoginTimestamp") as? Number)?.toLong() ?: 0L,
            lastDeviceName = doc.get("lastDeviceName")?.toString() ?: "",
            blockedUserIds = (doc.get("blockedUserIds") as? List<*>)?.mapNotNull { it?.toString() }
                ?: (doc.get("blocked_user_ids") as? List<*>)?.mapNotNull { it?.toString() }
                ?: emptyList()
        )
        return parsedProfile.copy(profileImageUrl = parsedProfile.getEffectiveProfileImageUrl())
    }

    /**
     * Synchronize live community profiles from Cloud Firestore backend.
     */
    suspend fun syncFirestoreProfiles() {
        try {
            val snapshot = firestore.collection("profiles").get(com.google.firebase.firestore.Source.SERVER).await()
            val firestoreProfiles = snapshot.documents.mapNotNull { parseProfileFromDoc(it) }
            if (firestoreProfiles.isNotEmpty()) {
                _firestoreProfiles.value = firestoreProfiles
            }
        } catch (e: Exception) {
            try {
                val cacheSnapshot = firestore.collection("profiles").get(com.google.firebase.firestore.Source.CACHE).await()
                val cachedProfiles = cacheSnapshot.documents.mapNotNull { parseProfileFromDoc(it) }
                if (cachedProfiles.isNotEmpty()) {
                    _firestoreProfiles.value = cachedProfiles
                }
            } catch (ex: Exception) {
                Log.e("MatrimonyRepository", "Error syncing profiles from Firestore", ex)
            }
        }

        try {
            val interestSnapshot = firestore.collection("interest_requests").get(com.google.firebase.firestore.Source.SERVER).await()
            val remoteInterests = interestSnapshot.documents.mapNotNull { parseInterestFromDoc(it) }
            mergeAndSetInterests(remoteInterests)
        } catch (e: Exception) {
            try {
                val cacheSnapshot = firestore.collection("interest_requests").get(com.google.firebase.firestore.Source.CACHE).await()
                val cachedInterests = cacheSnapshot.documents.mapNotNull { parseInterestFromDoc(it) }
                mergeAndSetInterests(cachedInterests)
            } catch (ex: Exception) {
                Log.e("MatrimonyRepository", "Error syncing interest requests from Firestore", ex)
            }
        }

        try {
            val chatSnapshot = firestore.collection("chat_messages").get(com.google.firebase.firestore.Source.SERVER).await()
            val remoteMsgs = chatSnapshot.documents.mapNotNull { parseChatMessageFromDoc(it) }
            _firestoreChatMessages.value = remoteMsgs
        } catch (e: Exception) {
            try {
                val cacheSnapshot = firestore.collection("chat_messages").get(com.google.firebase.firestore.Source.CACHE).await()
                val cachedMsgs = cacheSnapshot.documents.mapNotNull { parseChatMessageFromDoc(it) }
                _firestoreChatMessages.value = cachedMsgs
            } catch (ex: Exception) {
                Log.e("MatrimonyRepository", "Error syncing chat messages from Firestore", ex)
            }
        }
    }

    suspend fun fetchProfileDirectFromFirestore(id: String): Profile? {
        if (id.isBlank() || id == "USER_ME") return null
        return try {
            val doc = firestore.collection("profiles").document(id).get().await()
            if (doc.exists()) {
                parseProfileFromDoc(doc)
            } else null
        } catch (e: Exception) {
            try {
                val cacheDoc = firestore.collection("profiles").document(id).get(com.google.firebase.firestore.Source.CACHE).await()
                if (cacheDoc.exists()) {
                    parseProfileFromDoc(cacheDoc)
                } else null
            } catch (ex: Exception) {
                null
            }
        }
    }

    suspend fun getProfileById(id: String): Profile? {
        val memoryMatch = _firestoreProfiles.value.find { p: Profile -> p.id == id }
        if (memoryMatch != null) return memoryMatch
        return fetchProfileDirectFromFirestore(id)
    }

    suspend fun findProfileInFirestoreByContact(contactInfo: String): Profile? {
        if (contactInfo.isBlank()) return null
        val cleanContact = contactInfo.replace(Regex("[^0-9]"), "")

        val memoryMatch = _firestoreProfiles.value.firstOrNull { p: Profile ->
            val pPhone = p.phoneContact.replace(Regex("[^0-9]"), "")
            (pPhone.isNotBlank() && cleanContact.isNotBlank() && (pPhone.endsWith(cleanContact) || cleanContact.endsWith(pPhone) || pPhone == cleanContact)) ||
            (p.phoneContact.isNotBlank() && p.phoneContact.contains(contactInfo, ignoreCase = true))
        }
        if (memoryMatch != null) return memoryMatch

        return try {
            val snapshot = try {
                firestore.collection("profiles").get(com.google.firebase.firestore.Source.CACHE).await()
            } catch (e: Exception) {
                firestore.collection("profiles").get().await()
            }
            val doc = snapshot.documents.firstOrNull { d ->
                val phone = d.get("phoneContact")?.toString() ?: ""
                phone.isNotBlank() && (phone.contains(contactInfo, ignoreCase = true) || contactInfo.contains(phone, ignoreCase = true))
            }
            if (doc != null) parseProfileFromDoc(doc) else null
        } catch (e: Exception) {
            null
        }
    }

    /**
     * Save user profile and upload to Cloud Firestore backend.
     */
    suspend fun saveProfile(profile: Profile): Result<Unit> {
        val effectiveId = if (profile.id.isBlank() || profile.id == "USER_ME") {
            com.example.service.FirebaseAuthService.currentUser?.uid ?: ("user_" + System.currentTimeMillis())
        } else {
            profile.id
        }
        val finalProfile = profile.copy(id = effectiveId, isAadharVerified = profile.isApproved || profile.isAadharVerified)

        val currentList = _firestoreProfiles.value.toMutableList()
        val existingIndex = currentList.indexOfFirst { p: Profile -> p.id == effectiveId }
        if (existingIndex >= 0) {
            currentList[existingIndex] = finalProfile
        } else {
            currentList.add(finalProfile)
        }
        _firestoreProfiles.value = currentList

        // Cloud Firestore sync in background
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val profileMap = mapOf(
                    "id" to finalProfile.id,
                    "fullName" to finalProfile.fullName,
                    "fatherName" to finalProfile.fatherName,
                    "motherName" to finalProfile.motherName,
                    "grandfatherName" to finalProfile.grandfatherName,
                    "gender" to finalProfile.gender,
                    "age" to finalProfile.age,
                    "height" to finalProfile.height,
                    "weight" to finalProfile.weight,
                    "bloodGroup" to finalProfile.bloodGroup,
                    "isNri" to finalProfile.isNri,
                    "hasMaritalHistory" to finalProfile.hasMaritalHistory,
                    "subCaste" to finalProfile.subCaste,
                    "gotra" to finalProfile.gotra,
                    "motherGotra" to finalProfile.motherGotra,
                    "locality" to finalProfile.locality,
                    "nativeVillage" to finalProfile.nativeVillage,
                    "motherBirthVillage" to finalProfile.motherBirthVillage,
                    "education" to finalProfile.education,
                    "occupation" to finalProfile.occupation,
                    "currentCity" to finalProfile.currentCity,
                    "monthlyIncome" to finalProfile.monthlyIncome,
                    "maritalStatus" to finalProfile.maritalStatus,
                    "aboutMe" to finalProfile.aboutMe,
                    "familyDetails" to finalProfile.familyDetails,
                    "isAadharVerified" to finalProfile.isAadharVerified,
                    "aadharMasked" to finalProfile.aadharMasked,
                    "phoneContact" to finalProfile.phoneContact,
                    "parentPhoneContact" to finalProfile.parentPhoneContact,
                    "profileImageUrl" to finalProfile.profileImageUrl,
                    "photoUrl" to finalProfile.profileImageUrl,
                    "imageUrl" to finalProfile.profileImageUrl,
                    "aadharFrontUrl" to finalProfile.aadharFrontUrl,
                    "aadharBackUrl" to finalProfile.aadharBackUrl,
                    "rashi" to finalProfile.rashi,
                    "nakshatra" to finalProfile.nakshatra,
                    "manglikStatus" to finalProfile.manglikStatus,
                    "birthDate" to finalProfile.birthDate,
                    "birthTime" to finalProfile.birthTime,
                    "birthPlace" to finalProfile.birthPlace,
                    "isApproved" to finalProfile.isApproved,
                    "isRejected" to finalProfile.isRejected,
                    "rejectionReason" to finalProfile.rejectionReason,
                    "fatherOccupation" to finalProfile.fatherOccupation,
                    "motherOccupation" to finalProfile.motherOccupation,
                    "numBrothers" to finalProfile.numBrothers,
                    "brothersNames" to finalProfile.brothersNames,
                    "numSisters" to finalProfile.numSisters,
                    "sistersNames" to finalProfile.sistersNames,
                    "gol" to finalProfile.gol,
                    "hobbies" to finalProfile.hobbies,
                    "blockedUserIds" to finalProfile.blockedUserIds,
                    "blocked_user_ids" to finalProfile.blockedUserIds,
                    "timestamp" to System.currentTimeMillis()
                )
                firestore.collection("profiles").document(effectiveId)
                    .set(profileMap, SetOptions.merge())
                    .await()
                Log.i("MatrimonyRepository", "Uploaded profile to Firestore document: $effectiveId")
            } catch (e: Exception) {
                Log.e("MatrimonyRepository", "Error uploading profile to Cloud Firestore", e)
            }
        }

        return Result.success(Unit)
    }

    suspend fun approveProfile(profileId: String): Result<Unit> {
        try {
            val currentList = _firestoreProfiles.value.toMutableList()
            val idx = currentList.indexOfFirst { p: Profile -> p.id == profileId }
            if (idx >= 0) {
                currentList[idx] = currentList[idx].copy(isApproved = true, isAadharVerified = true, isRejected = false, rejectionReason = "")
                _firestoreProfiles.value = currentList
            }
            CoroutineScope(Dispatchers.IO).launch {
                try {
                    val updateData = mapOf(
                        "isApproved" to true,
                        "isAadharVerified" to true,
                        "isRejected" to false,
                        "rejectionReason" to "",
                        "approvedTimestamp" to System.currentTimeMillis()
                    )
                    firestore.collection("profiles").document(profileId)
                        .set(updateData, SetOptions.merge())
                        .await()
                } catch (e: Exception) {
                    Log.e("MatrimonyRepository", "Firestore set failed for approveProfile: $profileId", e)
                }
            }
            return Result.success(Unit)
        } catch (e: Exception) {
            Log.e("MatrimonyRepository", "Error approving profile $profileId", e)
            return Result.failure(e)
        }
    }

    suspend fun rejectProfile(profileId: String, reason: String): Result<Unit> {
        try {
            val currentList = _firestoreProfiles.value.toMutableList()
            val idx = currentList.indexOfFirst { p: Profile -> p.id == profileId }
            if (idx >= 0) {
                currentList[idx] = currentList[idx].copy(isApproved = false, isRejected = true, rejectionReason = reason)
                _firestoreProfiles.value = currentList
            }
            CoroutineScope(Dispatchers.IO).launch {
                try {
                    val updateData = mapOf(
                        "isApproved" to false,
                        "isRejected" to true,
                        "rejectionReason" to reason,
                        "rejectedTimestamp" to System.currentTimeMillis()
                    )
                    firestore.collection("profiles").document(profileId)
                        .set(updateData, SetOptions.merge())
                        .await()
                } catch (e: Exception) {
                    Log.e("MatrimonyRepository", "Firestore set failed for rejectProfile: $profileId", e)
                }
            }
            return Result.success(Unit)
        } catch (e: Exception) {
            Log.e("MatrimonyRepository", "Error rejecting profile $profileId", e)
            return Result.failure(e)
        }
    }

    suspend fun deleteProfile(profileId: String): Result<Unit> {
        try {
            _firestoreProfiles.value = _firestoreProfiles.value.filterNot { p: Profile -> p.id == profileId }
            CoroutineScope(Dispatchers.IO).launch {
                try {
                    firestore.collection("profiles").document(profileId).delete()
                } catch (e: Exception) {
                    Log.e("MatrimonyRepository", "Firestore delete failed for profile $profileId", e)
                }
            }
            return Result.success(Unit)
        } catch (e: Exception) {
            Log.e("MatrimonyRepository", "Error deleting profile $profileId", e)
            return Result.failure(e)
        }
    }

    suspend fun toggleShortlist(id: String, isShortlisted: Boolean) {
        if (isShortlisted) {
            _shortlistedIds.value = _shortlistedIds.value + id
        } else {
            _shortlistedIds.value = _shortlistedIds.value - id
        }
        val myUid = com.example.service.FirebaseAuthService.currentUser?.uid ?: "USER_ME"
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val docId = "${myUid}_${id}"
                if (isShortlisted) {
                    val map = mapOf(
                        "userId" to myUid,
                        "profileId" to id,
                        "timestamp" to System.currentTimeMillis()
                    )
                    firestore.collection("shortlists").document(docId).set(map, SetOptions.merge()).await()
                } else {
                    firestore.collection("shortlists").document(docId).delete().await()
                }
            } catch (e: Exception) {
                Log.e("MatrimonyRepository", "Error syncing shortlist to Firestore", e)
            }
        }
    }

    suspend fun updateInterestStatus(id: String, status: String) {
        val currentList = _firestoreProfiles.value.toMutableList()
        val idx = currentList.indexOfFirst { p: Profile -> p.id == id }
        if (idx >= 0) {
            currentList[idx] = currentList[idx].copy(interestStatus = status)
            _firestoreProfiles.value = currentList
        }
    }

    fun getChatMessages(chatRoomId: String): Flow<List<ChatMessage>> {
        return _firestoreChatMessages.map { list ->
            list.filter { p: ChatMessage -> p.chatRoomId == chatRoomId }.sortedBy { p: ChatMessage -> p.timestampMs }
        }
    }

    suspend fun sendChatMessage(message: ChatMessage) {
        val current = _firestoreChatMessages.value.toMutableList()
        current.add(message)
        _firestoreChatMessages.value = current

        CoroutineScope(Dispatchers.IO).launch {
            try {
                val map = mapOf(
                    "id" to message.id,
                    "chatRoomId" to message.chatRoomId,
                    "senderId" to message.senderId,
                    "receiverId" to message.receiverId,
                    "profileId" to message.receiverId,
                    "senderName" to message.senderName,
                    "text" to message.text,
                    "formattedTime" to message.timestamp,
                    "timestampMs" to message.timestampMs,
                    "isVoiceNote" to message.isVoiceNote,
                    "voiceDurationSec" to message.voiceDurationSec
                )
                firestore.collection("chat_messages").document(message.id).set(map, SetOptions.merge()).await()
            } catch (e: Exception) {
                Log.e("MatrimonyRepository", "Error uploading chat message to Firestore", e)
            }
        }
    }

    suspend fun deleteChatMessage(messageId: String) {
        val current = _firestoreChatMessages.value.filter { it.id != messageId }
        _firestoreChatMessages.value = current
        CoroutineScope(Dispatchers.IO).launch {
            try {
                firestore.collection("chat_messages").document(messageId).delete().await()
            } catch (e: Exception) {
                Log.e("MatrimonyRepository", "Error deleting chat message from Firestore", e)
            }
        }
    }

    suspend fun clearChatRoomMessages(chatRoomId: String) {
        val current = _firestoreChatMessages.value.filter { it.chatRoomId != chatRoomId }
        _firestoreChatMessages.value = current
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val snapshot = firestore.collection("chat_messages")
                    .whereEqualTo("chatRoomId", chatRoomId)
                    .get()
                    .await()
                for (doc in snapshot.documents) {
                    doc.reference.delete()
                }
            } catch (e: Exception) {
                Log.e("MatrimonyRepository", "Error clearing chat room messages from Firestore", e)
            }
        }
    }

    suspend fun resetPasswordForMobile(mobileNumber: String, newPass: String): Boolean {
        var success = false
        val cleanMobile = mobileNumber.trim()
        val matchProfile = _firestoreProfiles.value.firstOrNull { prof ->
            prof.phoneContact.contains(cleanMobile) || prof.parentPhoneContact.contains(cleanMobile)
        }
        if (matchProfile != null) {
            try {
                firestore.collection("profiles").document(matchProfile.id)
                    .update("password", newPass).await()
                success = true
            } catch (e: Exception) {
                Log.e("MatrimonyRepository", "Error updating profile password in Firestore", e)
            }
        }
        return success
    }

    suspend fun analyzeKundli(userProfile: Profile, partnerProfile: Profile): Pair<Int, String> {
        return GeminiService.analyzeKundliMatch(userProfile, partnerProfile)
    }

    suspend fun generateVoiceBio(notes: String, userProfile: Profile): String {
        return GeminiService.generateVoiceBiodata(notes, userProfile)
    }

    suspend fun getTopMatchReasons(userProfile: Profile, partnerProfile: Profile): String {
        return GeminiService.getTopMatchReasons(userProfile, partnerProfile)
    }

    private fun parseNotificationFromDoc(doc: com.google.firebase.firestore.DocumentSnapshot): AppNotification? {
        val userId = doc.get("userId")?.toString() ?: doc.get("user_id")?.toString() ?: ""
        if (userId.isBlank()) return null
        val id = doc.get("id")?.toString() ?: doc.id
        val title = doc.get("title")?.toString() ?: ""
        val message = doc.get("message")?.toString() ?: ""
        val type = doc.get("type")?.toString() ?: "SYSTEM"
        val targetId = doc.get("targetId")?.toString() ?: doc.get("target_id")?.toString() ?: ""
        val isRead = doc.getBoolean("isRead") ?: doc.getBoolean("is_read") ?: false
        val ts = (doc.get("timestamp") as? Number)?.toLong() ?: System.currentTimeMillis()
        return AppNotification(
            id = id,
            userId = userId,
            title = title,
            message = message,
            type = type,
            targetId = targetId,
            isRead = isRead,
            timestamp = ts
        )
    }

    suspend fun saveNotificationToFirestore(notification: AppNotification) {
        try {
            val map = mapOf(
                "id" to notification.id,
                "userId" to notification.userId,
                "title" to notification.title,
                "message" to notification.message,
                "type" to notification.type,
                "targetId" to notification.targetId,
                "isRead" to notification.isRead,
                "timestamp" to notification.timestamp
            )
            firestore.collection("notifications").document(notification.id).set(map, SetOptions.merge()).await()
        } catch (e: Exception) {
            Log.e("MatrimonyRepository", "Error saving notification to Firestore", e)
        }
    }

    fun getNotifications(userId: String): Flow<List<AppNotification>> {
        val authUid = com.example.service.FirebaseAuthService.currentUser?.uid ?: ""
        return _firestoreNotifications.map { list ->
            list.filter { it.userId == userId || (userId == "USER_ME" && it.userId == authUid) || (it.userId == "USER_ME") }
                .sortedByDescending { it.timestamp }
        }
    }

    fun getUnreadNotificationCount(userId: String): Flow<Int> {
        return getNotifications(userId).map { list -> list.count { !it.isRead } }
    }

    suspend fun markAllNotificationsAsRead(userId: String) {
        val authUid = com.example.service.FirebaseAuthService.currentUser?.uid ?: ""
        val matching = _firestoreNotifications.value.filter { it.userId == userId || (userId == "USER_ME" && it.userId == authUid) || (it.userId == "USER_ME") }
        matching.forEach { notif ->
            markNotificationAsRead(notif.id)
        }
    }

    suspend fun markNotificationAsRead(id: String) {
        try {
            firestore.collection("notifications").document(id).update("isRead", true).await()
        } catch (e: Exception) {
            Log.e("MatrimonyRepository", "Error marking notification as read in Firestore", e)
        }
    }

    suspend fun clearAllNotifications(userId: String) {
        val authUid = com.example.service.FirebaseAuthService.currentUser?.uid ?: ""
        val matching = _firestoreNotifications.value.filter { it.userId == userId || (userId == "USER_ME" && it.userId == authUid) || (it.userId == "USER_ME") }
        matching.forEach { notif ->
            deleteNotification(notif.id)
        }
    }

    suspend fun deleteNotification(id: String) {
        try {
            firestore.collection("notifications").document(id).delete().await()
        } catch (e: Exception) {
            Log.e("MatrimonyRepository", "Error deleting notification in Firestore", e)
        }
    }

    suspend fun recordDeviceLogin(userId: String, deviceId: String, deviceName: String) {
        if (userId.isBlank() || userId == "USER_ME") return
        try {
            val map = mapOf(
                "lastDeviceId" to deviceId,
                "lastLoginTimestamp" to System.currentTimeMillis(),
                "lastDeviceName" to deviceName
            )
            firestore.collection("profiles").document(userId).set(map, SetOptions.merge()).await()
        } catch (e: Exception) {
            Log.e("MatrimonyRepository", "Error recording device login in Firestore", e)
        }
    }
}
