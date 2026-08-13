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
                .setLocalCacheSettings(
                    com.google.firebase.firestore.PersistentCacheSettings.newBuilder()
                        .setSizeBytes(com.google.firebase.firestore.FirebaseFirestoreSettings.CACHE_SIZE_UNLIMITED)
                        .build()
                )
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
    private val _firestoreFreeSubscriberCount = MutableStateFlow<Int>(0)

    val allInterests: StateFlow<List<InterestRequest>> = _firestoreInterests.asStateFlow()
    val freeSubscriberCount: StateFlow<Int> = _firestoreFreeSubscriberCount.asStateFlow()

    fun getMyUserIds(userId: String = ""): Set<String> {
        val set = mutableSetOf<String>()
        if (userId.isNotBlank()) {
            set.add(userId)
            val cleanDigits = userId.replace(Regex("[^0-9]"), "").takeLast(10)
            if (cleanDigits.length == 10) {
                set.add(cleanDigits)
                set.add("USER_$cleanDigits")
            }
        }
        val authUser = com.example.service.FirebaseAuthService.currentUser
        val authUid = authUser?.uid
        if (!authUid.isNullOrBlank()) {
            set.add(authUid)
        }
        val authPhone = authUser?.phoneNumber
        if (!authPhone.isNullOrBlank()) {
            set.add(authPhone)
            val cleanPhone = authPhone.replace(Regex("[^0-9]"), "").takeLast(10)
            if (cleanPhone.length == 10) {
                set.add(cleanPhone)
                set.add("USER_$cleanPhone")
            }
        }
        val authEmail = authUser?.email
        if (!authEmail.isNullOrBlank()) {
            set.add(authEmail)
            val cleanEmailDigits = authEmail.substringBefore("@").replace(Regex("[^0-9]"), "").takeLast(10)
            if (cleanEmailDigits.length == 10) {
                set.add(cleanEmailDigits)
                set.add("USER_$cleanEmailDigits")
            }
        }
        _firestoreProfiles.value.forEach { p ->
            val matchesAuth = !authUid.isNullOrBlank() && (p.id == authUid || p.phoneContact == authUid)
            val matchesUser = userId.isNotBlank() && (
                p.id == userId || 
                p.phoneContact == userId || 
                "USER_${p.phoneContact}" == userId ||
                (userId.length >= 10 && p.phoneContact.endsWith(userId.takeLast(10)))
            )
            val matchesPhone = !authPhone.isNullOrBlank() && (
                p.phoneContact == authPhone ||
                (authPhone.length >= 10 && p.phoneContact.endsWith(authPhone.takeLast(10)))
            )
            val matchesEmail = !authEmail.isNullOrBlank() && (
                p.phoneContact == authEmail.substringBefore("@") ||
                (authEmail.substringBefore("@").length >= 10 && p.phoneContact.endsWith(authEmail.substringBefore("@").takeLast(10)))
            )
            if (matchesAuth || matchesUser || matchesPhone || matchesEmail) {
                if (p.id.isNotBlank()) set.add(p.id)
                if (p.phoneContact.isNotBlank()) {
                    val cleanP = p.phoneContact.replace(Regex("[^0-9]"), "").takeLast(10)
                    set.add(p.phoneContact)
                    set.add("USER_${p.phoneContact}")
                    if (cleanP.length == 10) {
                        set.add(cleanP)
                        set.add("USER_$cleanP")
                    }
                }
                if (p.parentPhoneContact.isNotBlank()) {
                    val cleanParent = p.parentPhoneContact.replace(Regex("[^0-9]"), "").takeLast(10)
                    set.add(p.parentPhoneContact)
                    if (cleanParent.length == 10) {
                        set.add(cleanParent)
                        set.add("USER_$cleanParent")
                    }
                }
            }
        }
        set.add("USER_ME")
        return set
    }

    val allProfiles: Flow<List<Profile>> = combine(_firestoreProfiles, _shortlistedIds, _firestoreInterests) { profiles, shortlisted, interests ->
        val authUid = com.example.service.FirebaseAuthService.currentUser?.uid ?: ""
        val myIds = getMyUserIds(authUid)
        profiles.map { p ->
            val pIds = getMyUserIds(p.id)
            val relInterest = interests.firstOrNull { req ->
                val matchesP = pIds.contains(req.senderId) || pIds.contains(req.receiverId) ||
                        (req.senderPhone.isNotBlank() && pIds.contains(req.senderPhone)) ||
                        (req.receiverPhone.isNotBlank() && pIds.contains(req.receiverPhone)) ||
                        req.senderId == p.id || req.receiverId == p.id
                val matchesMe = myIds.contains(req.senderId) || myIds.contains(req.receiverId) ||
                        (req.senderPhone.isNotBlank() && myIds.contains(req.senderPhone)) ||
                        (req.receiverPhone.isNotBlank() && myIds.contains(req.receiverPhone)) ||
                        req.senderId == "USER_ME" || req.receiverId == "USER_ME"
                matchesP && matchesMe
            }
            val calculatedStatus = when {
                relInterest == null -> "NONE"
                relInterest.status == "REJECTED" -> "REJECTED"
                relInterest.status == "ACCEPTED" -> "ACCEPTED"
                relInterest.status == "BLOCKED" -> "BLOCKED"
                myIds.contains(relInterest.receiverId) || (relInterest.receiverPhone.isNotBlank() && myIds.contains(relInterest.receiverPhone)) || relInterest.receiverId == "USER_ME" -> "RECEIVED"
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
        val myIds = getMyUserIds(authUid)
        profiles.filter { shortlisted.contains(it.id) }.map { p ->
            val pIds = getMyUserIds(p.id)
            val relInterest = interests.firstOrNull { req ->
                val matchesP = pIds.contains(req.senderId) || pIds.contains(req.receiverId) ||
                        (req.senderPhone.isNotBlank() && pIds.contains(req.senderPhone)) ||
                        (req.receiverPhone.isNotBlank() && pIds.contains(req.receiverPhone)) ||
                        req.senderId == p.id || req.receiverId == p.id
                val matchesMe = myIds.contains(req.senderId) || myIds.contains(req.receiverId) ||
                        (req.senderPhone.isNotBlank() && myIds.contains(req.senderPhone)) ||
                        (req.receiverPhone.isNotBlank() && myIds.contains(req.receiverPhone)) ||
                        req.senderId == "USER_ME" || req.receiverId == "USER_ME"
                matchesP && matchesMe
            }
            val calculatedStatus = when {
                relInterest == null -> "NONE"
                relInterest.status == "REJECTED" -> "REJECTED"
                relInterest.status == "ACCEPTED" -> "ACCEPTED"
                relInterest.status == "BLOCKED" -> "BLOCKED"
                myIds.contains(relInterest.receiverId) || (relInterest.receiverPhone.isNotBlank() && myIds.contains(relInterest.receiverPhone)) || relInterest.receiverId == "USER_ME" -> "RECEIVED"
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
        return _firestoreInterests.map { list ->
            val myIds = getMyUserIds(userId)
            list.filter { p: InterestRequest ->
                myIds.contains(p.senderId) || myIds.contains(p.receiverId) ||
                (p.senderPhone.isNotBlank() && myIds.contains(p.senderPhone)) ||
                (p.receiverPhone.isNotBlank() && myIds.contains(p.receiverPhone)) ||
                p.senderId == "USER_ME" || p.receiverId == "USER_ME"
            }
        }
    }

    suspend fun getMutualInterestStatus(user1: String, user2: String): String {
        val user1Ids = getMyUserIds(user1)
        val user2Ids = getMyUserIds(user2)
        val request = _firestoreInterests.value.firstOrNull { p: InterestRequest ->
            val matches1Sender = user1Ids.contains(p.senderId) || (p.senderPhone.isNotBlank() && user1Ids.contains(p.senderPhone))
            val matches2Receiver = user2Ids.contains(p.receiverId) || (p.receiverPhone.isNotBlank() && user2Ids.contains(p.receiverPhone))
            val matches2Sender = user2Ids.contains(p.senderId) || (p.senderPhone.isNotBlank() && user2Ids.contains(p.senderPhone))
            val matches1Receiver = user1Ids.contains(p.receiverId) || (p.receiverPhone.isNotBlank() && user1Ids.contains(p.receiverPhone))
            (matches1Sender && matches2Receiver) || (matches2Sender && matches1Receiver)
        }
        return request?.status ?: "NONE"
    }

    suspend fun sendInterestRequest(senderId: String, receiverId: String, senderName: String, receiverName: String) {
        val authUser = com.example.service.FirebaseAuthService.ensureAuth()
        val authUid = authUser?.uid ?: ""
        val effectiveSenderId = if (senderId.isBlank() || senderId == "USER_ME") authUid.ifBlank { "USER_ME" } else senderId

        val senderProf = _firestoreProfiles.value.firstOrNull { it.id == effectiveSenderId || (it.phoneContact.isNotBlank() && it.phoneContact == effectiveSenderId) }
        val receiverProf = _firestoreProfiles.value.firstOrNull { it.id == receiverId || (it.phoneContact.isNotBlank() && it.phoneContact == receiverId) }
        val senderPhone = senderProf?.phoneContact ?: ""
        val receiverPhone = receiverProf?.phoneContact ?: ""

        val senderIds = getMyUserIds(effectiveSenderId)
        val receiverIds = getMyUserIds(receiverId)

        val existingInterest = _firestoreInterests.value.firstOrNull {
            (senderIds.contains(it.senderId) && receiverIds.contains(it.receiverId)) ||
            (receiverIds.contains(it.senderId) && senderIds.contains(it.receiverId))
        }
        if (senderProf?.blockedUserIds?.contains(receiverId) == true ||
            receiverProf?.blockedUserIds?.contains(effectiveSenderId) == true ||
            existingInterest?.status == "BLOCKED") {
            Log.w("MatrimonyRepository", "Cannot send interest request: User $receiverId is blocked")
            return
        }

        val id = if (effectiveSenderId < receiverId) "${effectiveSenderId}_${receiverId}" else "${receiverId}_${effectiveSenderId}"

        val realSenderName = senderName.ifBlank {
            senderProf?.fullName ?: "મારો બાયોડેટા"
        }
        val realReceiverName = receiverName.ifBlank {
            receiverProf?.fullName ?: "સભ્ય"
        }

        val req = InterestRequest(
            id = id,
            senderId = effectiveSenderId,
            receiverId = receiverId,
            senderPhone = senderPhone,
            receiverPhone = receiverPhone,
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

        kotlinx.coroutines.withContext(Dispatchers.IO) {
            try {
                val map = hashMapOf(
                    "id" to id,
                    "senderId" to effectiveSenderId,
                    "sender_id" to effectiveSenderId,
                    "receiverId" to receiverId,
                    "receiver_id" to receiverId,
                    "senderPhone" to senderPhone,
                    "sender_phone" to senderPhone,
                    "receiverPhone" to receiverPhone,
                    "receiver_phone" to receiverPhone,
                    "senderName" to realSenderName,
                    "sender_name" to realSenderName,
                    "receiverName" to realReceiverName,
                    "receiver_name" to realReceiverName,
                    "status" to "PENDING",
                    "timestamp" to System.currentTimeMillis()
                )
                Log.d("MatrimonyRepository", "Writing interest request $id to Firestore: $map")

                firestore.collection("interest_requests").document(id)
                    .set(map, SetOptions.merge())
                    .await()

                Log.i("MatrimonyRepository", "SUCCESS: Written interest request $id to Firestore")

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
                Log.e("MatrimonyRepository", "ERROR: Error saving interest request $id to Firestore", e)
            }
        }
    }

    suspend fun acceptInterestRequest(requestId: String) {
        updateInterestRequestStatus(requestId, "ACCEPTED")
    }

    suspend fun updateInterestRequestStatus(requestId: String, status: String) {
        com.example.service.FirebaseAuthService.ensureAuth()
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
                    map["senderPhone"] = existingReq.senderPhone
                    map["sender_phone"] = existingReq.senderPhone
                    map["receiverPhone"] = existingReq.receiverPhone
                    map["receiver_phone"] = existingReq.receiverPhone
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
        val authUser = com.example.service.FirebaseAuthService.ensureAuth()
        val authUid = authUser?.uid ?: ""
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
            senderPhone = existing?.senderPhone ?: "",
            receiverPhone = existing?.receiverPhone ?: "",
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
                    "senderPhone" to updatedReq.senderPhone,
                    "sender_phone" to updatedReq.senderPhone,
                    "receiverPhone" to updatedReq.receiverPhone,
                    "receiver_phone" to updatedReq.receiverPhone,
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
        val authUser = com.example.service.FirebaseAuthService.ensureAuth()
        val authUid = authUser?.uid ?: ""
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
    private val shownNotificationKeys = java.util.Collections.synchronizedSet(mutableSetOf<String>())
    private val activeListenerRegistrations = java.util.Collections.synchronizedList(mutableListOf<com.google.firebase.firestore.ListenerRegistration>())

    @Synchronized
    fun removeAuthenticatedListeners() {
        Log.i("MatrimonyRepository", "Removing active Firestore snapshot listeners (${activeListenerRegistrations.size} active)")
        activeListenerRegistrations.forEach { registration ->
            try {
                registration.remove()
            } catch (e: Exception) {
                Log.e("MatrimonyRepository", "Error removing listener registration", e)
            }
        }
        activeListenerRegistrations.clear()
        isListenerActive = false
    }

    @Synchronized
    fun startRealtimeSync(
        context: Context,
        coroutineScope: kotlinx.coroutines.CoroutineScope,
        currentUserIdProvider: () -> String,
        currentDeviceId: String
    ) {
        // 1. Remove previous listeners to ensure a clean state without duplicate listeners
        removeAuthenticatedListeners()

        // 2. Only attach protected listeners when authenticated
        val authUser = com.example.service.FirebaseAuthService.currentUser
        if (authUser == null) {
            Log.w("MatrimonyRepository", "Skipping realtime sync attachment: Firebase Auth currentUser is null (Unauthenticated)")
            return
        }

        isListenerActive = true
        NotificationHelper.initNotificationChannel(context)

        try {
            // 1. Profiles listener
            val reg1 = firestore.collection("profiles")
                .addSnapshotListener { snapshot, error ->
                    if (error != null) {
                        Log.w("MatrimonyRepository", "Firestore profile real-time sync warning: ${error.message}")
                        return@addSnapshotListener
                    }
                    if (snapshot != null && !snapshot.isEmpty) {
                        coroutineScope.launch(Dispatchers.IO) {
                            val remoteProfiles = snapshot.documents.mapNotNull { parseProfileFromDoc(it) }
                            _firestoreProfiles.value = remoteProfiles

                            val profileFreeCount = remoteProfiles.count { it.isFreeSchemeUsed }
                            if (profileFreeCount > _firestoreFreeSubscriberCount.value) {
                                _firestoreFreeSubscriberCount.value = profileFreeCount
                            }

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
                            }
                        }
                    }
                }
            activeListenerRegistrations.add(reg1)

            // 2. Interest Requests listener
            val reg2 = firestore.collection("interest_requests")
                .addSnapshotListener { snapshot, error ->
                    if (error != null) {
                        Log.w("MatrimonyRepository", "Interest listener warning: ${error.message}")
                        return@addSnapshotListener
                    }
                    if (snapshot != null) {
                        coroutineScope.launch(Dispatchers.IO) {
                            val remoteInterests = snapshot.documents.mapNotNull { parseInterestFromDoc(it) }
                            mergeAndSetInterests(remoteInterests)
                        }
                    }
                }
            activeListenerRegistrations.add(reg2)

            // 3. Chat Messages listener
            val reg3 = firestore.collection("chat_messages")
                .addSnapshotListener { snapshot, error ->
                    if (error != null) {
                        Log.w("MatrimonyRepository", "Chat messages listener note: ${error.message}")
                        return@addSnapshotListener
                    }
                    if (snapshot != null) {
                        coroutineScope.launch(Dispatchers.IO) {
                            val remoteMsgs = snapshot.documents.mapNotNull { parseChatMessageFromDoc(it) }
                            _firestoreChatMessages.value = remoteMsgs
                        }
                    }
                }
            activeListenerRegistrations.add(reg3)

            // 4. Shortlists listener
            val reg4 = firestore.collection("shortlists")
                .addSnapshotListener { snapshot, error ->
                    if (error != null) return@addSnapshotListener
                    if (snapshot != null) {
                        coroutineScope.launch(Dispatchers.IO) {
                            val currentUid = currentUserIdProvider()
                            val myIds = getMyUserIds(currentUid)
                            val shortlistedProfileIds = snapshot.documents
                                .filter { doc ->
                                    val uId = doc.get("userId")?.toString() ?: ""
                                    myIds.contains(uId) || uId == currentUid || uId == "USER_ME"
                                }
                                .mapNotNull { doc -> doc.get("profileId")?.toString()?.takeIf { it.isNotBlank() } }
                                .toSet()
                            _shortlistedIds.value = shortlistedProfileIds
                        }
                    }
                }
            activeListenerRegistrations.add(reg4)

            // 5. Notifications listener
            val reg5 = firestore.collection("notifications")
                .addSnapshotListener { snapshot, error ->
                    if (error != null) return@addSnapshotListener
                    if (snapshot != null) {
                        coroutineScope.launch(Dispatchers.IO) {
                            val remoteNotifs = snapshot.documents.mapNotNull { parseNotificationFromDoc(it) }
                            _firestoreNotifications.value = remoteNotifs
                        }
                    }
                }
            activeListenerRegistrations.add(reg5)

            // 6. Free Subscriptions Counter listener in Firestore
            val reg6 = firestore.collection("free_subscribers")
                .addSnapshotListener { snapshot, error ->
                    if (error != null) {
                        Log.d("MatrimonyRepository", "free_subscribers collection listener note: ${error.message}")
                        return@addSnapshotListener
                    }
                    if (snapshot != null) {
                        val cnt = snapshot.documents.size
                        if (cnt > _firestoreFreeSubscriberCount.value) {
                            _firestoreFreeSubscriberCount.value = cnt
                        }
                    }
                }
            activeListenerRegistrations.add(reg6)

            val reg7 = firestore.collection("app_stats").document("free_subscriptions")
                .addSnapshotListener { snapshot, error ->
                    if (error != null) {
                        Log.d("MatrimonyRepository", "app_stats document listener note: ${error.message}")
                        return@addSnapshotListener
                    }
                    if (snapshot != null && snapshot.exists()) {
                        val cnt = (snapshot.get("count") as? Number)?.toInt()
                            ?: (snapshot.get("freeSubscriberCount") as? Number)?.toInt()
                            ?: (snapshot.get("claimedCount") as? Number)?.toInt()
                            ?: 0
                        if (cnt > _firestoreFreeSubscriberCount.value) {
                            _firestoreFreeSubscriberCount.value = cnt
                        }
                    }
                }
            activeListenerRegistrations.add(reg7)

            val reg8 = firestore.collection("app_stats").document("vip_free_users_count")
                .addSnapshotListener { snapshot, error ->
                    if (error != null) return@addSnapshotListener
                    if (snapshot != null && snapshot.exists()) {
                        val cnt = (snapshot.get("count") as? Number)?.toInt()
                            ?: (snapshot.get("freeSubscriberCount") as? Number)?.toInt()
                            ?: 0
                        if (cnt > _firestoreFreeSubscriberCount.value) {
                            _firestoreFreeSubscriberCount.value = cnt
                        }
                    }
                }
            activeListenerRegistrations.add(reg8)

            // 7. Background Auto-Sync loop as a resilient fallback
            coroutineScope.launch(Dispatchers.IO) {
                while (isListenerActive) {
                    kotlinx.coroutines.delay(8000)
                    try {
                        syncFirestoreProfiles()
                    } catch (e: Exception) {
                        // Background sync fallback error silent handling
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
        val senderPhone = doc.get("senderPhone")?.toString() ?: doc.get("sender_phone")?.toString() ?: ""
        val receiverPhone = doc.get("receiverPhone")?.toString() ?: doc.get("receiver_phone")?.toString() ?: ""
        return InterestRequest(
            id = id,
            senderId = senderId,
            receiverId = receiverId,
            senderPhone = senderPhone,
            receiverPhone = receiverPhone,
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
            voiceDurationSec = (doc.get("voiceDurationSec") as? Number)?.toInt() ?: 0,
            audioUrl = doc.get("audioUrl")?.toString() ?: doc.get("voiceUrl")?.toString() ?: ""
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

        val docId = doc.id
        val rawProfileId = doc.get("profileId")?.toString() ?: doc.get("profile_id")?.toString() ?: ""
        val rawAadhar = doc.get("aadharNumber")?.toString() ?: doc.get("aadhar_number")?.toString() ?: ""

        val effectiveProfileId = if (rawProfileId.length == 10 && rawProfileId.all { it.isDigit() }) {
            rawProfileId
        } else {
            val seed = kotlin.math.abs(docId.hashCode().toLong())
            (1000000000L + (seed % 9000000000L)).toString()
        }

        val parsedProfile = Profile(
            id = docId,
            profileId = effectiveProfileId,
            aadharNumber = rawAadhar,
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
            profileImageUrl = run {
                val rawUrl = (doc.get("profileImageUrl")?.toString()
                    ?: doc.get("photoUrl")?.toString()
                    ?: doc.get("photo_url")?.toString()
                    ?: doc.get("imageUrl")?.toString()
                    ?: doc.get("image_url")?.toString()
                    ?: doc.get("photo")?.toString()
                    ?: doc.get("avatarUrl")?.toString()
                    ?: doc.get("avatar")?.toString()
                    ?: "").trim()
                if (rawUrl.startsWith("http://") || rawUrl.startsWith("https://")) rawUrl else ""
            },
            aadharFrontUrl = run {
                val rawUrl = (doc.get("aadharFrontUrl")?.toString() ?: "").trim()
                if (rawUrl.startsWith("http://") || rawUrl.startsWith("https://")) rawUrl else ""
            },
            aadharBackUrl = run {
                val rawUrl = (doc.get("aadharBackUrl")?.toString() ?: "").trim()
                if (rawUrl.startsWith("http://") || rawUrl.startsWith("https://")) rawUrl else ""
            },
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
                ?: emptyList(),
            isVipSubscribed = doc.getBoolean("isVipSubscribed") ?: false,
            subscriptionPlan = doc.get("subscriptionPlan")?.toString() ?: "",
            subscriptionTxnId = doc.get("subscriptionTxnId")?.toString() ?: "",
            subscriptionStartDate = doc.get("subscriptionStartDate")?.toString() ?: "",
            subscriptionStartTimestamp = (doc.get("subscriptionStartTimestamp") as? Number)?.toLong() ?: 0L,
            subscriptionExpiryDate = doc.get("subscriptionExpiryDate")?.toString() ?: "",
            subscriptionExpiryTimestamp = (doc.get("subscriptionExpiryTimestamp") as? Number)?.toLong() ?: 0L,
            isFreeSchemeUsed = doc.getBoolean("isFreeSchemeUsed") ?: false
        )
        return parsedProfile
    }

    fun isNetworkAvailable(context: Context): Boolean {
        return try {
            val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as? android.net.ConnectivityManager ?: return false
            val activeNetwork = connectivityManager.activeNetwork ?: return false
            val capabilities = connectivityManager.getNetworkCapabilities(activeNetwork) ?: return false
            capabilities.hasCapability(android.net.NetworkCapabilities.NET_CAPABILITY_INTERNET)
        } catch (e: Exception) {
            false
        }
    }

    enum class MobileServerCheckResult {
        REGISTERED,
        NOT_REGISTERED,
        NO_INTERNET
    }

    suspend fun checkMobileRegistrationOnServer(context: Context, mobileNumber: String): Pair<MobileServerCheckResult, Profile?> = kotlinx.coroutines.withContext(Dispatchers.IO) {
        if (!isNetworkAvailable(context)) {
            return@withContext Pair(MobileServerCheckResult.NO_INTERNET, null)
        }
        val clean = mobileNumber.trim().filter { it.isDigit() }
        if (clean.isBlank()) {
            return@withContext Pair(MobileServerCheckResult.NOT_REGISTERED, null)
        }
        val phone10 = if (clean.length >= 10) clean.takeLast(10) else clean

        if (phone10 == "9724327777") {
            val adminDoc = try {
                firestore.collection("profiles").document("ADMIN_SRUSHTI").get(com.google.firebase.firestore.Source.SERVER).await()
            } catch (e: Exception) { null }
            return@withContext Pair(MobileServerCheckResult.REGISTERED, if (adminDoc != null && adminDoc.exists()) parseProfileFromDoc(adminDoc) else null)
        }

        try {
            val snapshot = firestore.collection("profiles").get(com.google.firebase.firestore.Source.SERVER).await()
            val matchingDoc = snapshot.documents.firstOrNull { doc ->
                val phone = doc.get("phoneContact")?.toString()?.filter { it.isDigit() } ?: ""
                val parentPhone = doc.get("parentPhoneContact")?.toString()?.filter { it.isDigit() } ?: ""
                (phone.length >= 10 && phone.takeLast(10) == phone10) ||
                (parentPhone.length >= 10 && parentPhone.takeLast(10) == phone10) ||
                doc.id == "USER_$phone10" || doc.id == phone10
            }

            if (matchingDoc != null) {
                val profile = parseProfileFromDoc(matchingDoc)
                Pair(MobileServerCheckResult.REGISTERED, profile)
            } else {
                Pair(MobileServerCheckResult.NOT_REGISTERED, null)
            }
        } catch (e: Exception) {
            Log.e("MatrimonyRepository", "Server mobile check exception", e)
            Pair(MobileServerCheckResult.NO_INTERNET, null)
        }
    }

    fun clearLocalMemoryCache() {
        removeAuthenticatedListeners()
        _firestoreProfiles.value = emptyList()
        _firestoreInterests.value = emptyList()
        _firestoreChatMessages.value = emptyList()
        _shortlistedIds.value = emptySet()
        _firestoreNotifications.value = emptyList()
    }

    suspend fun forceServerSyncOnLogin(userId: String = ""): Boolean = kotlinx.coroutines.withContext(Dispatchers.IO) {
        clearLocalMemoryCache()
        try {
            // 1. Fetch profiles strictly from SERVER
            val snapshot = firestore.collection("profiles").get(com.google.firebase.firestore.Source.SERVER).await()
            val firestoreProfiles = snapshot.documents.mapNotNull { parseProfileFromDoc(it) }
            _firestoreProfiles.value = firestoreProfiles

            // 2. Fetch interest_requests strictly from SERVER
            val interestSnapshot = firestore.collection("interest_requests").get(com.google.firebase.firestore.Source.SERVER).await()
            val remoteInterests = interestSnapshot.documents.mapNotNull { parseInterestFromDoc(it) }
            mergeAndSetInterests(remoteInterests)

            // 3. Fetch shortlists strictly from SERVER
            val shortlistSnapshot = firestore.collection("shortlists").get(com.google.firebase.firestore.Source.SERVER).await()
            val currentUid = userId.ifBlank { com.example.service.FirebaseAuthService.currentUser?.uid ?: "" }
            val myIds = getMyUserIds(currentUid)
            val shortlistedProfileIds = shortlistSnapshot.documents
                .filter { doc ->
                    val uId = doc.get("userId")?.toString() ?: ""
                    myIds.contains(uId) || uId == currentUid || uId == "USER_ME"
                }
                .mapNotNull { doc -> doc.get("profileId")?.toString()?.takeIf { it.isNotBlank() } }
                .toSet()
            _shortlistedIds.value = shortlistedProfileIds

            // 4. Fetch chat_messages strictly from SERVER
            val chatSnapshot = firestore.collection("chat_messages").get(com.google.firebase.firestore.Source.SERVER).await()
            val remoteMsgs = chatSnapshot.documents.mapNotNull { parseChatMessageFromDoc(it) }
            _firestoreChatMessages.value = remoteMsgs

            // 5. Fetch notifications strictly from SERVER
            try {
                val notifSnapshot = firestore.collection("notifications").get(com.google.firebase.firestore.Source.SERVER).await()
                val remoteNotifs = notifSnapshot.documents.mapNotNull { parseNotificationFromDoc(it) }
                _firestoreNotifications.value = remoteNotifs
            } catch (e: Exception) {
                Log.w("MatrimonyRepository", "Note fetching notifications on login: ${e.message}")
            }

            true
        } catch (e: Exception) {
            Log.e("MatrimonyRepository", "forceServerSyncOnLogin failed", e)
            false
        }
    }

    /**
     * Synchronize live community profiles from Cloud Firestore backend.
     */
    suspend fun syncFirestoreProfiles() {
        try {
            val snapshot = firestore.collection("profiles").get(com.google.firebase.firestore.Source.SERVER).await()
            val firestoreProfiles = snapshot.documents.mapNotNull { parseProfileFromDoc(it) }
            _firestoreProfiles.value = firestoreProfiles
        } catch (e: Exception) {
            try {
                val cacheSnapshot = firestore.collection("profiles").get(com.google.firebase.firestore.Source.CACHE).await()
                val cachedProfiles = cacheSnapshot.documents.mapNotNull { parseProfileFromDoc(it) }
                _firestoreProfiles.value = cachedProfiles
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
     * Check if a 10-digit mobile number is already registered in Firestore.
     * Prevents duplicate accounts with the same phone number.
     */
    suspend fun isMobileNumberRegistered(mobileNumber: String, excludeProfileId: String? = null): Boolean = kotlinx.coroutines.withContext(Dispatchers.IO) {
        try {
            val clean = mobileNumber.trim().filter { it.isDigit() }
            if (clean.length < 10) return@withContext false
            val phone10 = clean.takeLast(10)

            val snapshot = firestore.collection("profiles").get().await()
            for (doc in snapshot.documents) {
                val docId = doc.id
                if (excludeProfileId != null && (docId == excludeProfileId || (excludeProfileId == "USER_ME" && com.example.service.FirebaseAuthService.currentUser?.uid == docId))) {
                    continue
                }
                val docPhone = doc.getString("phoneContact")?.filter { it.isDigit() } ?: ""
                val docParentPhone = doc.getString("parentPhoneContact")?.filter { it.isDigit() } ?: ""
                if ((docPhone.length >= 10 && docPhone.takeLast(10) == phone10) ||
                    (docParentPhone.length >= 10 && docParentPhone.takeLast(10) == phone10)) {
                    Log.w("MatrimonyRepository", "Duplicate mobile detected! Phone $phone10 matches profile document $docId")
                    return@withContext true
                }
            }
            false
        } catch (e: Exception) {
            Log.e("MatrimonyRepository", "Error checking duplicate mobile number in Firestore", e)
            false
        }
    }

    fun generateUnique10DigitProfileId(): String {
        var candidate = (1000000000L..9999999999L).random().toString()
        while (_firestoreProfiles.value.any { it.profileId == candidate }) {
            candidate = (1000000000L..9999999999L).random().toString()
        }
        return candidate
    }

    suspend fun isMobileRegisteredInFirestore(phone: String, excludeUid: String): Boolean = kotlinx.coroutines.withContext(Dispatchers.IO) {
        val cleanPhone = phone.replace(Regex("[^0-9]"), "").takeLast(10)
        if (cleanPhone.length < 10) return@withContext false

        val localDup = _firestoreProfiles.value.firstOrNull { p ->
            if (p.id == excludeUid) return@firstOrNull false
            val pMain = p.phoneContact.replace(Regex("[^0-9]"), "").takeLast(10)
            val pParent = p.parentPhoneContact.replace(Regex("[^0-9]"), "").takeLast(10)
            pMain == cleanPhone || pParent == cleanPhone
        }
        if (localDup != null) return@withContext true

        try {
            val doc = firestore.collection("mobile_index").document(cleanPhone).get().await()
            if (doc.exists()) {
                val registeredUid = doc.getString("uid") ?: ""
                if (registeredUid.isNotBlank() && registeredUid != excludeUid) {
                    return@withContext true
                }
            }
        } catch (e: Exception) {
            Log.w("MatrimonyRepository", "Error checking mobile_index", e)
        }
        false
    }

    suspend fun isAadharRegisteredInFirestore(aadhar: String, excludeUid: String): Boolean = kotlinx.coroutines.withContext(Dispatchers.IO) {
        val cleanAadhar = aadhar.replace(Regex("[^0-9]"), "")
        if (cleanAadhar.length < 12) return@withContext false

        val localDup = _firestoreProfiles.value.firstOrNull { p ->
            if (p.id == excludeUid) return@firstOrNull false
            val pAadhar = p.aadharNumber.replace(Regex("[^0-9]"), "")
            pAadhar == cleanAadhar
        }
        if (localDup != null) return@withContext true

        try {
            val doc = firestore.collection("aadhar_index").document(cleanAadhar).get().await()
            if (doc.exists()) {
                val registeredUid = doc.getString("uid") ?: ""
                if (registeredUid.isNotBlank() && registeredUid != excludeUid) {
                    return@withContext true
                }
            }
        } catch (e: Exception) {
            Log.w("MatrimonyRepository", "Error checking aadhar_index", e)
        }
        false
    }

    /**
     * Save user profile and upload to Cloud Firestore backend synchronously.
     */
    suspend fun saveProfile(profile: Profile): Result<Unit> = kotlinx.coroutines.withContext(Dispatchers.IO) {
        try {
            val effectiveId = if (profile.id.isNotBlank() && profile.id != "USER_ME") {
                profile.id
            } else {
                com.example.service.FirebaseAuthService.currentUser?.uid ?: ("user_" + System.currentTimeMillis())
            }

            val cleanMobile = profile.phoneContact.replace(Regex("[^0-9]"), "").ifBlank {
                profile.parentPhoneContact.replace(Regex("[^0-9]"), "")
            }.takeLast(10)
            val cleanAadhar = profile.aadharNumber.replace(Regex("[^0-9]"), "")

            // Ensure 10-digit profileId persists across edits and admin approvals
            var assignedProfileId = if (profile.profileId.length == 10 && profile.profileId.all { it.isDigit() }) {
                profile.profileId
            } else ""

            if (assignedProfileId.isBlank()) {
                val existingLocal = _firestoreProfiles.value.firstOrNull { p ->
                    p.id == effectiveId ||
                    (cleanMobile.length == 10 && (p.phoneContact.endsWith(cleanMobile) || p.parentPhoneContact.endsWith(cleanMobile))) ||
                    (cleanAadhar.length == 12 && p.aadharNumber.replace(Regex("[^0-9]"), "") == cleanAadhar)
                }
                if (existingLocal != null && existingLocal.profileId.length == 10 && existingLocal.profileId.all { it.isDigit() }) {
                    assignedProfileId = existingLocal.profileId
                }
            }

            if (assignedProfileId.isBlank()) {
                try {
                    if (cleanMobile.length == 10) {
                        val mDoc = firestore.collection("mobile_index").document(cleanMobile).get().await()
                        val pid = mDoc.getString("profileId")
                        if (!pid.isNullOrBlank() && pid.length == 10 && pid.all { it.isDigit() }) {
                            assignedProfileId = pid
                        }
                    }
                    if (assignedProfileId.isBlank() && cleanAadhar.length == 12) {
                        val aDoc = firestore.collection("aadhar_index").document(cleanAadhar).get().await()
                        val pid = aDoc.getString("profileId")
                        if (!pid.isNullOrBlank() && pid.length == 10 && pid.all { it.isDigit() }) {
                            assignedProfileId = pid
                        }
                    }
                } catch (e: Exception) {
                    Log.w("MatrimonyRepository", "Could not query index collection for profileId", e)
                }
            }

            if (assignedProfileId.isBlank()) {
                assignedProfileId = generateUnique10DigitProfileId()
            }

            val finalProfile = profile.copy(
                id = effectiveId,
                profileId = assignedProfileId,
                isAadharVerified = profile.isApproved || profile.isAadharVerified
            )

            val currentList = _firestoreProfiles.value.toMutableList()
            val existingIndex = currentList.indexOfFirst { p: Profile -> p.id == effectiveId }
            val isNewProfileCreation = existingIndex < 0
            if (existingIndex >= 0) {
                currentList[existingIndex] = finalProfile
            } else {
                currentList.add(finalProfile)
            }
            _firestoreProfiles.value = currentList

            // Do NOT upload incomplete draft profiles (with blank fullName) to Cloud Firestore.
            if (finalProfile.fullName.trim().isBlank()) {
                android.util.Log.i("MatrimonyRepository", "Profile fullName is blank (draft). Skipping Cloud Firestore upload for $effectiveId.")
                return@withContext Result.success(Unit)
            }

            // Increment VIP free users count in Firestore for brand-new profile creations
            if (isNewProfileCreation) {
                try {
                    val newCounter = _firestoreFreeSubscriberCount.value + 1
                    _firestoreFreeSubscriberCount.value = newCounter
                    val counterMap = hashMapOf(
                        "count" to com.google.firebase.firestore.FieldValue.increment(1),
                        "freeSubscriberCount" to newCounter,
                        "maxLimit" to 100,
                        "lastUpdated" to System.currentTimeMillis()
                    )
                    firestore.collection("app_stats").document("vip_free_users_count").set(counterMap, SetOptions.merge())
                    firestore.collection("app_stats").document("free_subscriptions").set(counterMap, SetOptions.merge())
                } catch (e: Exception) {
                    Log.e("MatrimonyRepository", "Failed to update realtime count on profile creation", e)
                }
            }

            val profileMap = mapOf(
                "id" to finalProfile.id,
                "profileId" to finalProfile.profileId,
                "profile_id" to finalProfile.profileId,
                "aadharNumber" to finalProfile.aadharNumber,
                "aadhar_number" to finalProfile.aadharNumber,
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
                "isVipSubscribed" to finalProfile.isVipSubscribed,
                "subscriptionPlan" to finalProfile.subscriptionPlan,
                "subscriptionTxnId" to finalProfile.subscriptionTxnId,
                "subscriptionStartDate" to finalProfile.subscriptionStartDate,
                "subscriptionStartTimestamp" to finalProfile.subscriptionStartTimestamp,
                "subscriptionExpiryDate" to finalProfile.subscriptionExpiryDate,
                "subscriptionExpiryTimestamp" to finalProfile.subscriptionExpiryTimestamp,
                "isFreeSchemeUsed" to finalProfile.isFreeSchemeUsed,
                "timestamp" to System.currentTimeMillis()
            )
            firestore.collection("profiles").document(effectiveId)
                .set(profileMap, SetOptions.merge())
                .await()

            // Maintain mobile_index and aadhar_index in Firestore to prevent duplicate entries
            if (cleanMobile.length == 10) {
                val mobileIndexMap = hashMapOf(
                    "profileId" to finalProfile.profileId,
                    "mobileNumber" to cleanMobile,
                    "aadharNumber" to cleanAadhar,
                    "uid" to effectiveId,
                    "fullName" to finalProfile.fullName,
                    "updatedAt" to System.currentTimeMillis()
                )
                firestore.collection("mobile_index").document(cleanMobile).set(mobileIndexMap, SetOptions.merge())
            }

            if (cleanAadhar.length == 12) {
                val aadharIndexMap = hashMapOf(
                    "profileId" to finalProfile.profileId,
                    "mobileNumber" to cleanMobile,
                    "aadharNumber" to cleanAadhar,
                    "uid" to effectiveId,
                    "fullName" to finalProfile.fullName,
                    "updatedAt" to System.currentTimeMillis()
                )
                firestore.collection("aadhar_index").document(cleanAadhar).set(aadharIndexMap, SetOptions.merge())
            }

            Log.i("MatrimonyRepository", "Uploaded profile $effectiveId (profileId: ${finalProfile.profileId}) to Firestore document synchronously")
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e("MatrimonyRepository", "Error uploading profile to Cloud Firestore", e)
            Result.failure(e)
        }
    }

    suspend fun recordFreeSubscriptionInFirestore(profile: Profile): Result<Unit> {
        kotlinx.coroutines.withContext(Dispatchers.IO) {
            try {
                val subscriberMap = hashMapOf(
                    "userId" to profile.id,
                    "fullName" to profile.fullName,
                    "gender" to profile.gender,
                    "phoneContact" to profile.phoneContact,
                    "subscriptionPlan" to profile.subscriptionPlan,
                    "subscriptionStartDate" to profile.subscriptionStartDate,
                    "subscriptionExpiryDate" to profile.subscriptionExpiryDate,
                    "subscriptionTxnId" to profile.subscriptionTxnId,
                    "claimedAt" to System.currentTimeMillis(),
                    "isFreeSchemeUsed" to true
                )

                // 1. Record subscriber profile entry in free_subscribers collection
                firestore.collection("free_subscribers")
                    .document(profile.id)
                    .set(subscriberMap, SetOptions.merge())
                    .await()

                // 2. Increment & update realtime counter in app_stats/free_subscriptions
                val currentProfilesCount = _firestoreProfiles.value.count { it.isFreeSchemeUsed }
                val newCounter = maxOf(_firestoreFreeSubscriberCount.value + 1, currentProfilesCount + 1)
                _firestoreFreeSubscriberCount.value = newCounter

                val counterMap = hashMapOf(
                    "count" to com.google.firebase.firestore.FieldValue.increment(1),
                    "freeSubscriberCount" to newCounter,
                    "maxLimit" to 100,
                    "lastUpdated" to System.currentTimeMillis()
                )
                firestore.collection("app_stats")
                    .document("free_subscriptions")
                    .set(counterMap, SetOptions.merge())
                    .await()

                Log.i("MatrimonyRepository", "Recorded free subscriber ${profile.id} in Firestore successfully (Total: $newCounter).")
            } catch (e: Exception) {
                Log.e("MatrimonyRepository", "Failed to record free subscriber in Firestore", e)
            }
        }
        return Result.success(Unit)
    }

    suspend fun approveProfile(profileId: String): Result<Unit> = kotlinx.coroutines.withContext(Dispatchers.IO) {
        try {
            com.example.service.FirebaseAuthService.ensureAuth()
            val updateData = mapOf(
                "isApproved" to true,
                "isAadharVerified" to true,
                "isRejected" to false,
                "rejectionReason" to "",
                "approvedTimestamp" to System.currentTimeMillis()
            )
            // 1. Direct update to target profile document in Firestore
            firestore.collection("profiles").document(profileId)
                .set(updateData, SetOptions.merge())
                .await()
            Log.i("MatrimonyRepository", "Successfully updated Firestore document profiles/$profileId to approved=true")

            // 2. Also update any duplicate documents matching the same mobile number if present
            val localMatch = _firestoreProfiles.value.firstOrNull { it.id == profileId }
            if (localMatch != null) {
                val cleanMobile = localMatch.phoneContact.replace(Regex("[^0-9]"), "").takeLast(10)
                if (cleanMobile.length == 10) {
                    try {
                        val docsByMobile = firestore.collection("profiles")
                            .whereEqualTo("phoneContact", localMatch.phoneContact)
                            .get().await()
                        for (doc in docsByMobile.documents) {
                            if (doc.id != profileId) {
                                doc.reference.set(updateData, SetOptions.merge())
                            }
                        }
                    } catch (e: Exception) {
                        Log.w("MatrimonyRepository", "Secondary mobile profile sync warning: ${e.message}")
                    }
                }
            }

            try {
                val counterMap = hashMapOf(
                    "count" to com.google.firebase.firestore.FieldValue.increment(1),
                    "freeSubscriberCount" to com.google.firebase.firestore.FieldValue.increment(1),
                    "maxLimit" to 100,
                    "lastUpdated" to System.currentTimeMillis()
                )
                firestore.collection("app_stats").document("vip_free_users_count").set(counterMap, SetOptions.merge())
                firestore.collection("app_stats").document("free_subscriptions").set(counterMap, SetOptions.merge())
            } catch (e: Exception) {
                Log.w("MatrimonyRepository", "Error updating VIP user count on profile approval: ${e.message}")
            }

            val currentList = _firestoreProfiles.value.toMutableList()
            val idx = currentList.indexOfFirst { p: Profile -> p.id == profileId }
            if (idx >= 0) {
                currentList[idx] = currentList[idx].copy(isApproved = true, isAadharVerified = true, isRejected = false, rejectionReason = "")
                _firestoreProfiles.value = currentList
            }
            Log.i("MatrimonyRepository", "Approved profile $profileId in Firestore successfully")
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e("MatrimonyRepository", "Error approving profile $profileId in Firestore", e)
            Result.failure(e)
        }
    }

    suspend fun rejectProfile(profileId: String, reason: String): Result<Unit> = kotlinx.coroutines.withContext(Dispatchers.IO) {
        try {
            com.example.service.FirebaseAuthService.ensureAuth()
            val updateData = mapOf(
                "isApproved" to false,
                "isRejected" to true,
                "rejectionReason" to reason,
                "rejectedTimestamp" to System.currentTimeMillis()
            )
            // 1. Direct update to target profile document in Firestore
            firestore.collection("profiles").document(profileId)
                .set(updateData, SetOptions.merge())
                .await()
            Log.i("MatrimonyRepository", "Successfully updated Firestore document profiles/$profileId to rejected=true")

            // 2. Also update any duplicate documents matching the same mobile number if present
            val localMatch = _firestoreProfiles.value.firstOrNull { it.id == profileId }
            if (localMatch != null) {
                val cleanMobile = localMatch.phoneContact.replace(Regex("[^0-9]"), "").takeLast(10)
                if (cleanMobile.length == 10) {
                    try {
                        val docsByMobile = firestore.collection("profiles")
                            .whereEqualTo("phoneContact", localMatch.phoneContact)
                            .get().await()
                        for (doc in docsByMobile.documents) {
                            if (doc.id != profileId) {
                                doc.reference.set(updateData, SetOptions.merge())
                            }
                        }
                    } catch (e: Exception) {
                        Log.w("MatrimonyRepository", "Secondary mobile profile sync warning: ${e.message}")
                    }
                }
            }

            val currentList = _firestoreProfiles.value.toMutableList()
            val idx = currentList.indexOfFirst { p: Profile -> p.id == profileId }
            if (idx >= 0) {
                currentList[idx] = currentList[idx].copy(isApproved = false, isRejected = true, rejectionReason = reason)
                _firestoreProfiles.value = currentList
            }
            Log.i("MatrimonyRepository", "Rejected profile $profileId in Firestore successfully")
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e("MatrimonyRepository", "Error rejecting profile $profileId in Firestore", e)
            Result.failure(e)
        }
    }

    suspend fun deleteProfile(profileId: String): Result<Unit> = kotlinx.coroutines.withContext(Dispatchers.IO) {
        try {
            com.example.service.FirebaseAuthService.ensureAuth()
            if (profileId.isNotBlank()) {
                // 1. Delete main profile document directly from Firestore
                firestore.collection("profiles").document(profileId).delete().await()
                Log.i("MatrimonyRepository", "Successfully deleted profiles/$profileId from Firestore")

                // 2. Delete free subscriber record
                try {
                    firestore.collection("free_subscribers").document(profileId).delete().await()
                } catch (e: Exception) { /* ignore */ }

                // 3. Delete all interest requests involving this user
                try {
                    val sent = firestore.collection("interest_requests").whereEqualTo("senderId", profileId).get().await()
                    for (d in sent.documents) { d.reference.delete() }
                    val recv = firestore.collection("interest_requests").whereEqualTo("receiverId", profileId).get().await()
                    for (d in recv.documents) { d.reference.delete() }
                } catch (e: Exception) { /* ignore */ }

                // 4. Delete shortlists
                try {
                    val sl = firestore.collection("shortlists").whereEqualTo("userId", profileId).get().await()
                    for (d in sl.documents) { d.reference.delete() }
                    val sl2 = firestore.collection("shortlists").whereEqualTo("profileId", profileId).get().await()
                    for (d in sl2.documents) { d.reference.delete() }
                } catch (e: Exception) { /* ignore */ }

                // 5. Delete chat messages
                try {
                    val msgs1 = firestore.collection("chat_messages").whereEqualTo("senderId", profileId).get().await()
                    for (d in msgs1.documents) { d.reference.delete() }
                    val msgs2 = firestore.collection("chat_messages").whereEqualTo("receiverId", profileId).get().await()
                    for (d in msgs2.documents) { d.reference.delete() }
                } catch (e: Exception) { /* ignore */ }

                // 6. Delete notifications
                try {
                    val notifs = firestore.collection("notifications").whereEqualTo("userId", profileId).get().await()
                    for (d in notifs.documents) { d.reference.delete() }
                } catch (e: Exception) { /* ignore */ }
            }

            _firestoreProfiles.value = _firestoreProfiles.value.filterNot { p: Profile -> p.id == profileId }
            _shortlistedIds.value = _shortlistedIds.value.filterNot { it == profileId }.toSet()
            _firestoreInterests.value = _firestoreInterests.value.filterNot { it.senderId == profileId || it.receiverId == profileId }
            Log.i("MatrimonyRepository", "Full deletion completed in Firestore for profile $profileId")
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e("MatrimonyRepository", "Error during full deletion of profile $profileId", e)
            Result.failure(e)
        }
    }

    suspend fun toggleShortlist(id: String, isShortlisted: Boolean) = kotlinx.coroutines.withContext(Dispatchers.IO) {
        if (id.isBlank()) return@withContext
        if (isShortlisted) {
            _shortlistedIds.value = _shortlistedIds.value + id
        } else {
            _shortlistedIds.value = _shortlistedIds.value - id
        }
        val authUid = com.example.service.FirebaseAuthService.currentUser?.uid ?: ""
        val myProfileId = _firestoreProfiles.value.firstOrNull { it.id == authUid || it.phoneContact == authUid }?.id
        val currentUid = if (!myProfileId.isNullOrBlank()) myProfileId else authUid.ifBlank { "USER_ME" }
        try {
            val docId = "${currentUid}_${id}"
            if (isShortlisted) {
                val map = mapOf(
                    "userId" to currentUid,
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

    suspend fun updateInterestStatus(id: String, status: String) {
        val currentList = _firestoreProfiles.value.toMutableList()
        val idx = currentList.indexOfFirst { p: Profile -> p.id == id }
        if (idx >= 0) {
            currentList[idx] = currentList[idx].copy(interestStatus = status)
            _firestoreProfiles.value = currentList
        }
    }

    fun getChatMessages(chatRoomId: String, user1: String = "", user2: String = ""): Flow<List<ChatMessage>> {
        return _firestoreChatMessages.map { list ->
            val user1Ids = if (user1.isNotBlank()) getMyUserIds(user1) else emptySet()
            val user2Ids = if (user2.isNotBlank()) getMyUserIds(user2) else emptySet()
            list.filter { p: ChatMessage ->
                p.chatRoomId == chatRoomId ||
                (user1Ids.isNotEmpty() && user2Ids.isNotEmpty() && (
                    (user1Ids.contains(p.senderId) && user2Ids.contains(p.receiverId)) ||
                    (user2Ids.contains(p.senderId) && user1Ids.contains(p.receiverId))
                ))
            }.sortedBy { p: ChatMessage -> p.timestampMs }
        }
    }

    suspend fun sendChatMessage(message: ChatMessage) = kotlinx.coroutines.withContext(Dispatchers.IO) {
        val current = _firestoreChatMessages.value.toMutableList()
        current.add(message)
        _firestoreChatMessages.value = current

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
                "voiceDurationSec" to message.voiceDurationSec,
                "audioUrl" to message.audioUrl
            )
            firestore.collection("chat_messages").document(message.id).set(map, SetOptions.merge()).await()
        } catch (e: Exception) {
            Log.e("MatrimonyRepository", "Error uploading chat message to Firestore", e)
        }
    }

    suspend fun deleteChatMessage(messageId: String) = kotlinx.coroutines.withContext(Dispatchers.IO) {
        val current = _firestoreChatMessages.value.filter { it.id != messageId }
        _firestoreChatMessages.value = current
        try {
            firestore.collection("chat_messages").document(messageId).delete().await()
        } catch (e: Exception) {
            Log.e("MatrimonyRepository", "Error deleting chat message from Firestore", e)
        }
    }

    suspend fun clearChatRoomMessages(chatRoomId: String) = kotlinx.coroutines.withContext(Dispatchers.IO) {
        val current = _firestoreChatMessages.value.filter { it.chatRoomId != chatRoomId }
        _firestoreChatMessages.value = current
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
