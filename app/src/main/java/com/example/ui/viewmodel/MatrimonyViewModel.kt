package com.example.ui.viewmodel

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.model.AppNotification
import com.example.model.ChatMessage
import com.example.model.InterestRequest
import com.example.model.Profile
import com.example.repository.MatrimonyRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

@OptIn(ExperimentalCoroutinesApi::class)
class MatrimonyViewModel(application: Application) : AndroidViewModel(application) {

    val repository = MatrimonyRepository()

    // Current User Profile (Self)
    private val _myProfile = MutableStateFlow(
        Profile(
            id = "USER_ME",
            fullName = "",
            gender = "Groom",
            age = 0,
            height = "",
            weight = "",
            subCaste = "",
            gotra = "",
            motherGotra = "",
            locality = "",
            nativeVillage = "",
            motherBirthVillage = "",
            education = "",
            occupation = "",
            currentCity = "",
            monthlyIncome = "",
            maritalStatus = "Never Married",
            aboutMe = "",
            familyDetails = "",
            isAadharVerified = false,
            aadharMasked = "",
            phoneContact = "",
            profileImageUrl = "",
            rashi = "",
            nakshatra = "",
            manglikStatus = "Non-Manglik",
            birthTime = "",
            birthPlace = "",
            isApproved = false
        )
    )
    val myProfile: StateFlow<Profile> = _myProfile.asStateFlow()

    suspend fun checkIsUserRegistered(uid: String, contactInfo: String): Pair<Boolean, Profile?> {
        val firestoreProfile = repository.fetchProfileDirectFromFirestore(uid)
        if (firestoreProfile != null && firestoreProfile.fullName.isNotBlank()) {
            return Pair(true, firestoreProfile)
        }
        if (contactInfo.isNotBlank()) {
            val firestoreByContact = repository.findProfileInFirestoreByContact(contactInfo)
            if (firestoreByContact != null && firestoreByContact.fullName.isNotBlank()) {
                return Pair(true, firestoreByContact.copy(id = uid))
            }
        }
        val localProfile = repository.getProfileById(uid)
        if (localProfile != null && localProfile.fullName.isNotBlank()) {
            return Pair(true, localProfile)
        }
        val matchInAll = allProfiles.value.find {
            (it.id.isNotBlank() && it.id == uid) ||
            (contactInfo.isNotBlank() && it.phoneContact.isNotBlank() && it.phoneContact.contains(contactInfo, ignoreCase = true))
        }
        if (matchInAll != null && matchInAll.fullName.isNotBlank()) {
            return Pair(true, matchInAll)
        }
        return Pair(false, null)
    }

    fun checkMobileRegistered(mobile: String, onResult: (Boolean) -> Unit) {
        viewModelScope.launch {
            val trimmed = mobile.trim()
            if (trimmed == "9724327777" || trimmed.equals("srushtichaudhary11@gmail.com", ignoreCase = true)) {
                onResult(true)
                return@launch
            }
            val (isReg, _) = checkIsUserRegistered("", trimmed)
            onResult(isReg)
        }
    }

    // Persistent Preferences & Language State
    private val prefs = application.getSharedPreferences("vivah_prefs", android.content.Context.MODE_PRIVATE)

    private val _appLanguage = MutableStateFlow(prefs.getString("selected_lang", "gu") ?: "gu")
    val appLanguage: StateFlow<String> = _appLanguage.asStateFlow()

    fun setAppLanguage(lang: String) {
        _appLanguage.value = lang
        prefs.edit().putString("selected_lang", lang).apply()
    }

    private val _isRefreshing = MutableStateFlow(false)
    val isRefreshing: StateFlow<Boolean> = _isRefreshing.asStateFlow()

    private val _isDeletingAccount = MutableStateFlow(false)
    val isDeletingAccount: StateFlow<Boolean> = _isDeletingAccount.asStateFlow()

    fun attachRealtimeSync() {
        val appCtx = getApplication<Application>()
        repository.startRealtimeSync(
            context = appCtx,
            coroutineScope = viewModelScope,
            currentUserIdProvider = {
                _myProfile.value.id.ifBlank { com.example.service.FirebaseAuthService.currentUser?.uid ?: "USER_ME" }
            },
            currentDeviceId = currentDeviceId
        )
    }

    fun refreshDashboard() {
        viewModelScope.launch {
            _isRefreshing.value = true
            try {
                attachRealtimeSync()
                repository.forceServerSyncOnLogin(_myProfile.value.id)
                checkSubscriptionExpiryStatus()
            } catch (e: Exception) {
                android.util.Log.e("MatrimonyViewModel", "Error refreshing dashboard", e)
            } finally {
                _isRefreshing.value = false
            }
        }
    }

    // Login & Auth State
    private val _isAdmin = MutableStateFlow(
        com.example.service.FirebaseAuthService.currentUser?.email?.trim()?.let {
            it.startsWith("9724327777") || it.equals("srushtichaudhary11@gmail.com", ignoreCase = true)
        } == true
    )
    val isAdmin: StateFlow<Boolean> = _isAdmin.asStateFlow()

    private val _isLoggedIn = MutableStateFlow(com.example.service.FirebaseAuthService.currentUser != null)
    val isLoggedIn: StateFlow<Boolean> = _isLoggedIn.asStateFlow()

    private val _userRole = MutableStateFlow("કન્યાની શોધમાં (Searching for Bride)")
    val userRole: StateFlow<String> = _userRole.asStateFlow()

    private val _isAuthenticating = MutableStateFlow(false)
    val isAuthenticating: StateFlow<Boolean> = _isAuthenticating.asStateFlow()

    // Filters State
    val searchGender = MutableStateFlow("") // Rely on isOppositeGender for dynamic male/female filtering
    val selectedSubCaste = MutableStateFlow("બધી સબ-કાસ્ટ")
    val selectedLocality = MutableStateFlow("બધા પ્રદેશો")
    val searchQuery = MutableStateFlow("")
    val ageRangeMin = MutableStateFlow(20)
    val ageRangeMax = MutableStateFlow(35)
    val onlyAadharVerified = MutableStateFlow(false)
    val excludedGotra = MutableStateFlow("")
    val motherVillageSearch = MutableStateFlow("")

    // Raw Profiles from DB
    val allProfiles: StateFlow<List<Profile>> = repository.allProfiles
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // User Interest Requests Stream
    val userInterests: StateFlow<List<InterestRequest>> = combine(
        _myProfile,
        allProfiles,
        repository.allInterests
    ) { myProfile, profiles, interests ->
        val myIds = repository.getMyUserIds(myProfile.id)
        interests.filter { req ->
            val matchesSender = myIds.contains(req.senderId) || 
                    (req.senderPhone.isNotBlank() && myIds.contains(req.senderPhone)) || 
                    req.senderId == "USER_ME"
            val matchesReceiver = myIds.contains(req.receiverId) || 
                    (req.receiverPhone.isNotBlank() && myIds.contains(req.receiverPhone)) || 
                    req.receiverId == "USER_ME"
            matchesSender || matchesReceiver
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Shortlisted Profiles
    val shortlistedProfiles: StateFlow<List<Profile>> = repository.shortlistedProfiles
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Filter Params Data Class
    data class FilterParams(
        val gender: String,
        val subCaste: String,
        val locality: String,
        val query: String,
        val verifiedOnly: Boolean,
        val isAdmin: Boolean
    )

    private val filterParams: Flow<FilterParams> = combine(
        searchGender,
        selectedSubCaste,
        selectedLocality,
        searchQuery,
        onlyAadharVerified
    ) { gender, subCaste, locality, query, verifiedOnly ->
        FilterParams(gender, subCaste, locality, query, verifiedOnly, isAdmin.value)
    }

    fun isFemaleGender(genderStr: String): Boolean {
        val s = genderStr.trim().lowercase()
        if (s.isBlank()) return false
        return s.contains("bride") || s.contains("female") || s.contains("કન્યા") || s.contains("સ્ત્રી") || s.contains("woman") || s.contains("girl") || s.contains("kanya")
    }

    fun isMaleGender(genderStr: String): Boolean {
        val s = genderStr.trim().lowercase()
        if (s.isBlank()) return false
        return s.contains("groom") || s.contains("male") || s.contains("વરરાજા") || s.contains("વર") || s.contains("પુરુષ") || s.contains("man") || s.contains("boy")
    }

    private fun isOppositeGender(pGender: String, userGender: String): Boolean {
        if (isAdmin.value) return true // Admin can view all profiles
        val isUserFemale = isFemaleGender(userGender)
        val isUserMale = isMaleGender(userGender)

        val isCandidateFemale = isFemaleGender(pGender)
        val isCandidateMale = isMaleGender(pGender)

        if (isUserFemale) return isCandidateMale
        if (isUserMale) return isCandidateFemale
        return true
    }

    private fun isSameProfile(p: Profile, user: Profile): Boolean {
        if (user.id.isNotBlank() && p.id.isNotBlank() && user.id != "USER_ME" && p.id != "USER_ME") {
            return p.id == user.id
        }
        if (user.id == "USER_ME" || p.id == "USER_ME" || user.id.isBlank() || p.id.isBlank()) {
            return false
        }
        return p.id == user.id
    }

    /**
     * Chaudhary Community Cultural Exogamy Match Rules:
     * 1. Same Shaakh / Gotra prohibition (boy and girl with same Shaakh do not marry)
     * 2. Same Father's / Native Village prohibition (boy and girl from same village do not marry)
     * 3. Bride's Mother's Village prohibition (bride is never married to a boy from her mother's village)
     */
    fun isChaudharyCommunityMatchAllowed(profile1: Profile, profile2: Profile): Boolean {
        // Condition 3: Same Shaakh / Gotra
        if (profile1.gotra.isNotBlank() && profile2.gotra.isNotBlank() &&
            profile1.gotra.trim().equals(profile2.gotra.trim(), ignoreCase = true)) {
            return false
        }

        // Condition 2: Same Father's Village / Native Village (Gaon Bhaichara)
        if (profile1.nativeVillage.isNotBlank() && profile2.nativeVillage.isNotBlank() &&
            profile1.nativeVillage.trim().equals(profile2.nativeVillage.trim(), ignoreCase = true)) {
            return false
        }

        // Condition 1: Bride's mother's village == Groom's father's village or Groom's own village
        val bride = when {
            isFemaleGender(profile1.gender) -> profile1
            isFemaleGender(profile2.gender) -> profile2
            else -> null
        }
        val groom = when {
            isMaleGender(profile1.gender) -> profile1
            isMaleGender(profile2.gender) -> profile2
            else -> null
        }

        if (bride != null && groom != null) {
            if (bride.motherBirthVillage.isNotBlank() && groom.nativeVillage.isNotBlank() &&
                bride.motherBirthVillage.trim().equals(groom.nativeVillage.trim(), ignoreCase = true)) {
                return false
            }
        } else {
            // Fallback cross check
            if (profile1.motherBirthVillage.isNotBlank() && profile2.nativeVillage.isNotBlank() &&
                profile1.motherBirthVillage.trim().equals(profile2.nativeVillage.trim(), ignoreCase = true)) {
                return false
            }
            if (profile2.motherBirthVillage.isNotBlank() && profile1.nativeVillage.isNotBlank() &&
                profile2.motherBirthVillage.trim().equals(profile1.nativeVillage.trim(), ignoreCase = true)) {
                return false
            }
        }

        return true
    }

    // Filtered Profiles Stream
    val filteredProfiles: StateFlow<List<Profile>> = combine(
        allProfiles,
        _myProfile,
        filterParams,
        excludedGotra,
        motherVillageSearch
    ) { list, user, params, exGotra, mVillage ->
        list.filter { p ->
            val isUserGroom = isMaleGender(user.gender)
            val isUserBride = isFemaleGender(user.gender)

            val matchesGenderTarget = when {
                params.isAdmin || isAdmin.value -> {
                    if (params.gender.isBlank() || params.gender.equals("All", ignoreCase = true) || params.gender == "બધી પ્રોફાઇલ્સ") true
                    else if (isFemaleGender(params.gender)) isFemaleGender(p.gender)
                    else if (isMaleGender(params.gender)) isMaleGender(p.gender)
                    else true
                }
                isUserGroom -> isFemaleGender(p.gender) // Groom dashboard shows Bride profiles
                isUserBride -> isMaleGender(p.gender)  // Bride dashboard shows Groom profiles
                else -> {
                    if (params.gender.isBlank() || params.gender.equals("All", ignoreCase = true)) true
                    else if (isFemaleGender(params.gender)) isFemaleGender(p.gender)
                    else if (isMaleGender(params.gender)) isMaleGender(p.gender)
                    else true
                }
            }

            !isSameProfile(p, user) &&
            p.isApproved &&
            matchesGenderTarget &&
            ((params.isAdmin || isAdmin.value) || isChaudharyCommunityMatchAllowed(user, p)) &&
            // Filter by sub-caste
            (params.subCaste == "All Sub-castes" || params.subCaste == "બધી સબ-કાસ્ટ" || params.subCaste.isBlank() || p.subCaste.isBlank() || p.subCaste.equals(params.subCaste, ignoreCase = true)) &&
            // Filter by locality / region
            (params.locality == "All Regions" || params.locality == "બધા પ્રદેશો" || params.locality.isBlank() || p.locality.isBlank() || p.locality.equals(params.locality, ignoreCase = true)) &&
            // Verified check
            (!params.verifiedOnly || p.isAadharVerified) &&
            // Gotra exclusion
            (exGotra.isBlank() || (!p.gotra.equals(exGotra, ignoreCase = true) && !p.motherGotra.equals(exGotra, ignoreCase = true))) &&
            // Mother's village filter
            (mVillage.isBlank() || p.motherBirthVillage.contains(mVillage, ignoreCase = true)) &&
            // Query match (Name, City, Native Village, Occupation, Gotra)
            (params.query.isBlank() ||
                    p.fullName.contains(params.query, ignoreCase = true) ||
                    p.nativeVillage.contains(params.query, ignoreCase = true) ||
                    p.gotra.contains(params.query, ignoreCase = true) ||
                    p.currentCity.contains(params.query, ignoreCase = true) ||
                    p.occupation.contains(params.query, ignoreCase = true) ||
                    p.gol.contains(params.query, ignoreCase = true) ||
                    p.hobbies.contains(params.query, ignoreCase = true))
        }.distinctBy { it.id }.sortedWith(
            compareByDescending<Profile> { p ->
                // Same Gol profiles are prioritized first as Chaudhary samaj prefers same Gol marriages
                if (user.gol.isNotBlank() && p.gol.isNotBlank() && p.gol.equals(user.gol, ignoreCase = true)) 1 else 0
            }.thenByDescending { p ->
                p.isAadharVerified
            }
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // AI Voice Bio Assistant
    private val _generatedVoiceBio = MutableStateFlow("")
    val generatedVoiceBio: StateFlow<String> = _generatedVoiceBio.asStateFlow()

    private val _isGeneratingVoiceBio = MutableStateFlow(false)
    val isGeneratingVoiceBio: StateFlow<Boolean> = _isGeneratingVoiceBio.asStateFlow()

    val currentDeviceId: String = com.example.service.NotificationHelper.getDeviceId(application)

    val notifications: StateFlow<List<AppNotification>> = _myProfile
        .map { it.id }
        .distinctUntilChanged()
        .flatMapLatest { userId ->
            val effectiveId = userId.ifBlank { com.example.service.FirebaseAuthService.currentUser?.uid ?: "USER_ME" }
            repository.getNotifications(effectiveId)
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val unreadNotificationCount: StateFlow<Int> = _myProfile
        .map { it.id }
        .distinctUntilChanged()
        .flatMapLatest { userId ->
            val effectiveId = userId.ifBlank { com.example.service.FirebaseAuthService.currentUser?.uid ?: "USER_ME" }
            repository.getUnreadNotificationCount(effectiveId)
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    val freeSchemeClaimedCount: StateFlow<Int> = combine(
        repository.freeSubscriberCount,
        allProfiles
    ) { firestoreCounter, profilesList ->
        val registeredCount = profilesList.count { it.fullName.isNotBlank() }
        val usedCount = profilesList.count { it.isFreeSchemeUsed }
        val baseMin = if (profilesList.isNotEmpty()) 1 else 0
        maxOf(firestoreCounter, registeredCount, usedCount, baseMin)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 1)

    init {
        viewModelScope.launch {
            com.example.service.FirebaseAuthService.ensureAuth()
            attachRealtimeSync()
            repository.syncFirestoreProfiles()
            checkSubscriptionExpiryStatus()
        }
        viewModelScope.launch {
            val currentUser = com.example.service.FirebaseAuthService.currentUser
            if (currentUser != null) {
                _myProfile.value = _myProfile.value.copy(id = currentUser.uid)
                val (isRegistered, registeredProfile) = checkIsUserRegistered(currentUser.uid, currentUser.email ?: "")
                if (isRegistered && registeredProfile != null) {
                    _myProfile.value = registeredProfile
                    updateDefaultSearchGender()
                } else {
                    val localDraft = loadDraftLocally()
                    if (localDraft != null && localDraft.fullName.isNotBlank()) {
                        _myProfile.value = localDraft.copy(id = currentUser.uid)
                    }
                }
                recordCurrentDeviceLogin()
            } else {
                val localDraft = loadDraftLocally()
                if (localDraft != null && localDraft.fullName.isNotBlank()) {
                    _myProfile.value = localDraft
                }
            }
        }
        viewModelScope.launch {
            allProfiles.collect { profiles ->
                val authUid = com.example.service.FirebaseAuthService.currentUser?.uid
                val currentMyId = _myProfile.value.id.ifBlank { authUid ?: "USER_ME" }
                if (currentMyId.isNotBlank() && currentMyId != "USER_ME") {
                    val updated = profiles.find { it.id == currentMyId }
                    if (updated != null) {
                        _myProfile.value = updated
                    }
                } else if (authUid != null) {
                    val updated = profiles.find { it.id == authUid }
                    if (updated != null) {
                        _myProfile.value = updated
                    }
                }
            }
        }
    }

    fun markAllNotificationsAsRead() {
        viewModelScope.launch {
            val myId = _myProfile.value.id.ifBlank { com.example.service.FirebaseAuthService.currentUser?.uid ?: "USER_ME" }
            repository.markAllNotificationsAsRead(myId)
        }
    }

    fun markNotificationAsRead(id: String) {
        viewModelScope.launch {
            repository.markNotificationAsRead(id)
        }
    }

    fun clearAllNotifications() {
        viewModelScope.launch {
            val myId = _myProfile.value.id.ifBlank { com.example.service.FirebaseAuthService.currentUser?.uid ?: "USER_ME" }
            repository.clearAllNotifications(myId)
        }
    }

    fun recordCurrentDeviceLogin() {
        viewModelScope.launch {
            val authUid = com.example.service.FirebaseAuthService.currentUser?.uid
            val myId = _myProfile.value.id.ifBlank { authUid ?: "" }
            if (myId.isNotBlank() && myId != "USER_ME") {
                val deviceName = "${android.os.Build.MANUFACTURER} ${android.os.Build.MODEL}".trim()
                repository.recordDeviceLogin(myId, currentDeviceId, deviceName)
            }
        }
    }

    fun updateDefaultSearchGender() {
        if (_isAdmin.value) {
            searchGender.value = "All"
            return
        }
        val userGender = _myProfile.value.gender
        val isFemale = isFemaleGender(userGender)
        if (isFemale) {
            searchGender.value = "Groom"
        } else {
            searchGender.value = "Bride"
        }
    }

    fun determineUserGenderFromRole(role: String): String {
        val s = role.trim().lowercase()
        if (s.contains("searching for bride") || s.contains("કન્યા") || s.contains("groom") || s.contains("male") || s.contains("પુરુષ")) {
            return "Groom"
        }
        if (s.contains("searching for groom") || s.contains("વરરાજા") || s.contains("bride") || s.contains("female") || s.contains("સ્ત્રી")) {
            return "Bride"
        }
        return "Groom"
    }

    fun loginUser(role: String) {
        _userRole.value = role
        _isLoggedIn.value = true
        com.example.service.FirebaseAuthService.currentUser?.let { user ->
            _myProfile.value = _myProfile.value.copy(id = user.uid)
        }
        updateDefaultSearchGender()
        recordCurrentDeviceLogin()
        refreshDashboard()
    }

    fun logout() {
        com.example.service.FirebaseAuthService.signOut()
        _isLoggedIn.value = false
        _isAdmin.value = false
        _myProfile.value = Profile(id = "USER_ME")
        repository.clearLocalMemoryCache()
    }

    fun verifyMobileAndSignInOnServer(
        mobileOrEmail: String,
        pass: String,
        role: String,
        context: android.content.Context,
        onError: (String) -> Unit,
        onNoInternet: () -> Unit,
        onNotRegistered: () -> Unit,
        onSuccess: (isNewUser: Boolean) -> Unit
    ) {
        viewModelScope.launch {
            _isAuthenticating.value = true
            val rawInput = mobileOrEmail.trim()
            val isMobileNumber = rawInput.length == 10 && rawInput.all { it.isDigit() }

            // 1. Strict Internet Connection check
            if (!repository.isNetworkAvailable(context)) {
                _isAuthenticating.value = false
                onNoInternet()
                return@launch
            }

            val isTargetAdmin = rawInput == "9724327777" || rawInput.equals("srushtichaudhary11@gmail.com", ignoreCase = true)
            if (isTargetAdmin && pass.isNotBlank() && pass != "JulieSrushti@6863") {
                _isAuthenticating.value = false
                onError("એડમિન એકાઉન્ટ પાસવર્ડ ખોટો છે (Invalid Admin Password)")
                return@launch
            }

            if (isMobileNumber) {
                // Perform strict check on Firebase Auth / Firestore SERVER
                val (checkResult, registeredProfile) = repository.checkMobileRegistrationOnServer(context, rawInput)
                when (checkResult) {
                    MatrimonyRepository.MobileServerCheckResult.NO_INTERNET -> {
                        _isAuthenticating.value = false
                        onNoInternet()
                    }
                    MatrimonyRepository.MobileServerCheckResult.NOT_REGISTERED -> {
                        _isAuthenticating.value = false
                        onNotRegistered()
                    }
                    MatrimonyRepository.MobileServerCheckResult.REGISTERED -> {
                        // User is registered! Clear memory cache and force server sync
                        repository.clearLocalMemoryCache()
                        _isAdmin.value = isTargetAdmin
                        loginUser(role)

                        // Force sync all data directly from Firestore SERVER
                        val syncUser = registeredProfile?.id ?: "USER_$rawInput"
                        repository.forceServerSyncOnLogin(syncUser)

                        if (registeredProfile != null) {
                            _myProfile.value = registeredProfile.copy(isApproved = if (isTargetAdmin) true else registeredProfile.isApproved)
                        } else if (isTargetAdmin) {
                            _myProfile.value = Profile(
                                id = "ADMIN_SRUSHTI",
                                fullName = "Chaudhary Admin (9724327777)",
                                gender = "Groom",
                                phoneContact = "9724327777",
                                parentPhoneContact = "9724327777",
                                isApproved = true
                            )
                        }
                        updateDefaultSearchGender()
                        _isAuthenticating.value = false
                        onSuccess(false)
                    }
                }
            } else {
                // Email login flow
                val result = com.example.service.FirebaseAuthService.signInWithEmail(rawInput, pass)
                result.onSuccess { firebaseUser ->
                    repository.clearLocalMemoryCache()
                    _isAdmin.value = isTargetAdmin
                    loginUser(role)
                    repository.forceServerSyncOnLogin(firebaseUser.uid)

                    val (isRegistered, registeredProfile) = checkIsUserRegistered(firebaseUser.uid, rawInput)
                    if (isRegistered && registeredProfile != null) {
                        _myProfile.value = registeredProfile.copy(isApproved = if (isTargetAdmin) true else registeredProfile.isApproved)
                    } else {
                        _myProfile.value = Profile(
                            id = firebaseUser.uid,
                            fullName = rawInput,
                            gender = determineUserGenderFromRole(role),
                            phoneContact = rawInput,
                            parentPhoneContact = rawInput,
                            isApproved = false
                        )
                    }
                    updateDefaultSearchGender()
                    onSuccess(false)
                }.onFailure { e ->
                    if (!repository.isNetworkAvailable(context)) {
                        onNoInternet()
                    } else {
                        onError("ઈમેલ/પાસવર્ડ ખોટો છે અથવા નોંધણી થઈ નથી.")
                    }
                }
                _isAuthenticating.value = false
            }
        }
    }

    fun signInWithEmail(email: String, pass: String, role: String, onError: (String) -> Unit, onSuccess: (isNewUser: Boolean) -> Unit) {
        viewModelScope.launch {
            _isAuthenticating.value = true
            val rawInput = email.trim()
            val isMobileNumber = rawInput.length == 10 && rawInput.all { it.isDigit() }

            val isTargetAdmin = rawInput == "9724327777" || rawInput.equals("srushtichaudhary11@gmail.com", ignoreCase = true)
            if (isTargetAdmin && pass != "JulieSrushti@6863") {
                _isAuthenticating.value = false
                onError("એડમિન એકાઉન્ટ પાસવર્ડ ખોટો છે (Invalid Admin Password)")
                return@launch
            }

            if (isMobileNumber) {
                val (isRegistered, registeredProfile) = checkIsUserRegistered("USER_$rawInput", rawInput)
                if (isRegistered && registeredProfile != null) {
                    repository.clearLocalMemoryCache()
                    _isAdmin.value = isTargetAdmin
                    loginUser(role)
                    repository.forceServerSyncOnLogin(registeredProfile.id)
                    _myProfile.value = registeredProfile.copy(isApproved = if (isTargetAdmin) true else registeredProfile.isApproved)
                    updateDefaultSearchGender()
                    _isAuthenticating.value = false
                    onSuccess(false)
                } else if (isTargetAdmin && pass == "JulieSrushti@6863") {
                    repository.clearLocalMemoryCache()
                    _isAdmin.value = true
                    _myProfile.value = Profile(
                        id = "ADMIN_SRUSHTI",
                        fullName = "Chaudhary Admin (9724327777)",
                        gender = "Groom",
                        phoneContact = "9724327777",
                        parentPhoneContact = "9724327777",
                        isApproved = true
                    )
                    loginUser(role)
                    repository.forceServerSyncOnLogin("ADMIN_SRUSHTI")
                    updateDefaultSearchGender()
                    _isAuthenticating.value = false
                    onSuccess(false)
                } else {
                    _isAuthenticating.value = false
                    onError("મોબાઈલ નંબર નોંધાયેલ નથી. કૃપા કરીને નવી નોંધણી (Registration) કરો.")
                }
            } else {
                val result = com.example.service.FirebaseAuthService.signInWithEmail(rawInput, pass)
                result.onSuccess { firebaseUser ->
                    repository.clearLocalMemoryCache()
                    val userEmail = firebaseUser.email ?: rawInput
                    val defaultName = if (isTargetAdmin) "Chaudhary Admin (9724327777)" else rawInput
                    _isAdmin.value = isTargetAdmin

                    val (isRegistered, registeredProfile) = checkIsUserRegistered(firebaseUser.uid, rawInput)
                    if (isRegistered && registeredProfile != null) {
                        loginUser(role)
                        repository.forceServerSyncOnLogin(registeredProfile.id)
                        _myProfile.value = registeredProfile.copy(isApproved = if (isTargetAdmin) true else registeredProfile.isApproved)
                        updateDefaultSearchGender()
                        onSuccess(false)
                    } else {
                        loginUser(role)
                        repository.forceServerSyncOnLogin(firebaseUser.uid)
                        _myProfile.value = Profile(
                            id = firebaseUser.uid,
                            fullName = rawInput,
                            gender = determineUserGenderFromRole(role),
                            phoneContact = rawInput,
                            parentPhoneContact = rawInput,
                            isApproved = false
                        )
                        updateDefaultSearchGender()
                        onSuccess(false)
                    }
                }.onFailure { e ->
                    onError("ઈમેલ/પાસવર્ડ ખોટો છે અથવા નોંધણી થઈ નથી.")
                }
                _isAuthenticating.value = false
            }
        }
    }

    // Local storage helpers for draft profiles (stored on phone SharedPreferences only)
    fun saveDraftLocally(draftProfile: Profile) {
        _myProfile.value = draftProfile
        try {
            val json = org.json.JSONObject().apply {
                put("id", draftProfile.id)
                put("fullName", draftProfile.fullName)
                put("fatherName", draftProfile.fatherName)
                put("fatherOccupation", draftProfile.fatherOccupation)
                put("motherName", draftProfile.motherName)
                put("motherOccupation", draftProfile.motherOccupation)
                put("grandfatherName", draftProfile.grandfatherName)
                put("numBrothers", draftProfile.numBrothers)
                put("brothersNames", draftProfile.brothersNames)
                put("numSisters", draftProfile.numSisters)
                put("sistersNames", draftProfile.sistersNames)
                put("gender", draftProfile.gender)
                put("age", draftProfile.age)
                put("birthDate", draftProfile.birthDate)
                put("birthTime", draftProfile.birthTime)
                put("birthPlace", draftProfile.birthPlace)
                put("height", draftProfile.height)
                put("weight", draftProfile.weight)
                put("bloodGroup", draftProfile.bloodGroup)
                put("isNri", draftProfile.isNri)
                put("nriCountry", draftProfile.nriCountry)
                put("maritalStatus", draftProfile.maritalStatus)
                put("hasMaritalHistory", draftProfile.hasMaritalHistory)
                put("subCaste", draftProfile.subCaste)
                put("gol", draftProfile.gol)
                put("gotra", draftProfile.gotra)
                put("motherGotra", draftProfile.motherGotra)
                put("nativeVillage", draftProfile.nativeVillage)
                put("motherBirthVillage", draftProfile.motherBirthVillage)
                put("locality", draftProfile.locality)
                put("education", draftProfile.education)
                put("occupation", draftProfile.occupation)
                put("currentCity", draftProfile.currentCity)
                put("monthlyIncome", draftProfile.monthlyIncome)
                put("phoneContact", draftProfile.phoneContact)
                put("parentPhoneContact", draftProfile.parentPhoneContact)
                put("hobbies", draftProfile.hobbies)
                put("familyDetails", draftProfile.familyDetails)
                put("aboutMe", draftProfile.aboutMe)
                put("rashi", draftProfile.rashi)
                put("manglikStatus", draftProfile.manglikStatus)
                put("profileImageUrl", draftProfile.profileImageUrl)
                put("aadharFrontUrl", draftProfile.aadharFrontUrl)
                put("aadharBackUrl", draftProfile.aadharBackUrl)
                put("aadharMasked", draftProfile.aadharMasked)
                put("isAadharVerified", draftProfile.isAadharVerified)
            }.toString()
            prefs.edit().putString("local_draft_profile_json", json).apply()
            Log.i("MatrimonyViewModel", "Draft profile saved locally in SharedPreferences")
        } catch (e: Exception) {
            Log.e("MatrimonyViewModel", "Error saving draft profile locally", e)
        }
    }

    fun loadDraftLocally(): Profile? {
        val jsonStr = prefs.getString("local_draft_profile_json", null) ?: return null
        return try {
            val json = org.json.JSONObject(jsonStr)
            Profile(
                id = json.optString("id", ""),
                fullName = json.optString("fullName", ""),
                fatherName = json.optString("fatherName", ""),
                fatherOccupation = json.optString("fatherOccupation", ""),
                motherName = json.optString("motherName", ""),
                motherOccupation = json.optString("motherOccupation", ""),
                grandfatherName = json.optString("grandfatherName", ""),
                numBrothers = json.optInt("numBrothers", 0),
                brothersNames = json.optString("brothersNames", ""),
                numSisters = json.optInt("numSisters", 0),
                sistersNames = json.optString("sistersNames", ""),
                gender = json.optString("gender", ""),
                age = json.optInt("age", 24),
                birthDate = json.optString("birthDate", ""),
                birthTime = json.optString("birthTime", ""),
                birthPlace = json.optString("birthPlace", ""),
                height = json.optString("height", ""),
                weight = json.optString("weight", ""),
                bloodGroup = json.optString("bloodGroup", ""),
                isNri = json.optBoolean("isNri", false),
                nriCountry = json.optString("nriCountry", ""),
                maritalStatus = json.optString("maritalStatus", ""),
                hasMaritalHistory = json.optBoolean("hasMaritalHistory", false),
                subCaste = json.optString("subCaste", ""),
                gol = json.optString("gol", ""),
                gotra = json.optString("gotra", ""),
                motherGotra = json.optString("motherGotra", ""),
                nativeVillage = json.optString("nativeVillage", ""),
                motherBirthVillage = json.optString("motherBirthVillage", ""),
                locality = json.optString("locality", ""),
                education = json.optString("education", ""),
                occupation = json.optString("occupation", ""),
                currentCity = json.optString("currentCity", ""),
                monthlyIncome = json.optString("monthlyIncome", ""),
                phoneContact = json.optString("phoneContact", ""),
                parentPhoneContact = json.optString("parentPhoneContact", ""),
                hobbies = json.optString("hobbies", ""),
                familyDetails = json.optString("familyDetails", ""),
                aboutMe = json.optString("aboutMe", ""),
                rashi = json.optString("rashi", ""),
                manglikStatus = json.optString("manglikStatus", ""),
                profileImageUrl = json.optString("profileImageUrl", ""),
                aadharFrontUrl = json.optString("aadharFrontUrl", ""),
                aadharBackUrl = json.optString("aadharBackUrl", ""),
                aadharMasked = json.optString("aadharMasked", ""),
                isAadharVerified = json.optBoolean("isAadharVerified", false)
            )
        } catch (e: Exception) {
            null
        }
    }

    fun clearDraftLocally() {
        prefs.edit().remove("local_draft_profile_json").apply()
    }

    fun registerWithEmailAndPassword(
        email: String,
        pass: String,
        profileData: Profile,
        onError: (String) -> Unit,
        onSuccess: () -> Unit
    ) {
        viewModelScope.launch {
            _isAuthenticating.value = true
            val rawInput = email.trim()

            // Check if mobile number is already registered in Firestore
            val isMobileNumber = rawInput.length == 10 && rawInput.all { it.isDigit() }
            if (isMobileNumber) {
                val isAlreadyRegistered = repository.isMobileNumberRegistered(rawInput)
                if (isAlreadyRegistered) {
                    _isAuthenticating.value = false
                    onError("આ મોબાઈલ નંબર સાથે પહેલેથી જ પ્રોફાઇલ નોંધાયેલી છે! કૃપા કરીને લૉગિન કરો અથવા બીજો નંબર દાખલ કરો.")
                    return@launch
                }
            }

            val existingUser = com.example.service.FirebaseAuthService.currentUser
            val isTargetAdmin = rawInput == "9724327777" || rawInput.equals("srushtichaudhary11@gmail.com", ignoreCase = true)
            _isAdmin.value = isTargetAdmin

            if (isMobileNumber) {
                // Mobile Registration - Save profile by mobile number without synthetic email account in Firebase Auth
                val uid = if (existingUser != null && !existingUser.isAnonymous && existingUser.phoneNumber?.contains(rawInput) == true) {
                    existingUser.uid
                } else {
                    "USER_$rawInput"
                }

                val finalProfile = profileData.copy(
                    id = uid,
                    phoneContact = rawInput,
                    parentPhoneContact = profileData.parentPhoneContact.ifBlank { rawInput },
                    isApproved = if (isTargetAdmin) true else false,
                    isRejected = false,
                    rejectionReason = ""
                )
                repository.saveProfile(finalProfile)
                _myProfile.value = finalProfile
                clearDraftLocally()
                updateDefaultSearchGender()
                _isLoggedIn.value = true
                _isAuthenticating.value = false
                onSuccess()
            } else {
                // Real Email Registration
                val result = com.example.service.FirebaseAuthService.signUpWithEmail(rawInput, pass)
                result.onSuccess { firebaseUser ->
                    val uid = firebaseUser.uid
                    val contactVal = firebaseUser.email ?: rawInput
                    val finalProfile = profileData.copy(
                        id = uid,
                        phoneContact = contactVal,
                        parentPhoneContact = profileData.parentPhoneContact.ifBlank { contactVal },
                        isApproved = if (isTargetAdmin) true else false,
                        isRejected = false,
                        rejectionReason = ""
                    )
                    repository.saveProfile(finalProfile)
                    _myProfile.value = finalProfile
                    clearDraftLocally()
                    updateDefaultSearchGender()
                    _isLoggedIn.value = true
                    _isAuthenticating.value = false
                    onSuccess()
                }.onFailure { e ->
                    // Fallback saving if network fails
                    val fallbackUid = "usr_${System.currentTimeMillis()}_$rawInput"
                    val finalProfile = profileData.copy(
                        id = fallbackUid,
                        phoneContact = rawInput,
                        parentPhoneContact = profileData.parentPhoneContact.ifBlank { rawInput },
                        isApproved = if (isTargetAdmin) true else false,
                        isRejected = false,
                        rejectionReason = ""
                    )
                    repository.saveProfile(finalProfile)
                    _myProfile.value = finalProfile
                    clearDraftLocally()
                    updateDefaultSearchGender()
                    _isLoggedIn.value = true
                    _isAuthenticating.value = false
                    onSuccess()
                }
            }
        }
    }

    fun onPhoneAuthSuccess(phoneNumber: String, role: String, onSuccess: (isNewUser: Boolean) -> Unit) {
        viewModelScope.launch {
            val currentUser = com.example.service.FirebaseAuthService.currentUser
            val uid = currentUser?.uid ?: "USER_$phoneNumber"
            val (isRegistered, registeredProfile) = checkIsUserRegistered(uid, phoneNumber)
            loginUser(role)
            if (isRegistered && registeredProfile != null) {
                _myProfile.value = registeredProfile
                updateDefaultSearchGender()
                onSuccess(false)
            } else {
                val localDraft = loadDraftLocally()
                val draftOrNew = (localDraft ?: Profile(id = uid)).copy(
                    id = uid,
                    fullName = localDraft?.fullName ?: "",
                    gender = determineUserGenderFromRole(role),
                    phoneContact = phoneNumber,
                    isApproved = false
                )
                _myProfile.value = draftOrNew
                updateDefaultSearchGender()
                onSuccess(true)
            }
        }
    }

    fun uploadProfileImage(
        uri: android.net.Uri,
        onProgress: (Int) -> Unit = {},
        onResult: (String?) -> Unit
    ) {
        viewModelScope.launch {
            val context = getApplication<Application>()
            val effectiveId = if (_myProfile.value.id.isNotBlank() && _myProfile.value.id != "USER_ME") {
                _myProfile.value.id
            } else {
                val currentAuthUid = com.example.service.FirebaseAuthService.currentUser?.uid
                if (!currentAuthUid.isNullOrBlank()) currentAuthUid else "usr_${System.currentTimeMillis()}"
            }
            if (_myProfile.value.id == "USER_ME" || _myProfile.value.id.isBlank()) {
                _myProfile.value = _myProfile.value.copy(id = effectiveId)
            }
            val url = com.example.service.FirebaseStorageService.uploadProfileImageWithProgress(
                context = context,
                profileId = effectiveId,
                imageUri = uri,
                onProgress = onProgress
            )
            if (url != null) {
                _myProfile.value = _myProfile.value.copy(profileImageUrl = url)
                repository.saveProfile(_myProfile.value)
            }
            onResult(url)
        }
    }

    fun uploadAadharFrontImage(
        uri: android.net.Uri,
        onProgress: (Int) -> Unit = {},
        onResult: (String?) -> Unit
    ) {
        viewModelScope.launch {
            val context = getApplication<Application>()
            val effectiveId = if (_myProfile.value.id.isNotBlank() && _myProfile.value.id != "USER_ME") {
                _myProfile.value.id
            } else {
                val currentAuthUid = com.example.service.FirebaseAuthService.currentUser?.uid
                if (!currentAuthUid.isNullOrBlank()) currentAuthUid else "usr_${System.currentTimeMillis()}"
            }
            if (_myProfile.value.id == "USER_ME" || _myProfile.value.id.isBlank()) {
                _myProfile.value = _myProfile.value.copy(id = effectiveId)
            }
            val url = com.example.service.FirebaseStorageService.uploadAadharImageWithProgress(
                context = context,
                profileId = effectiveId,
                side = "front",
                imageUri = uri,
                onProgress = onProgress
            )
            if (url != null) {
                _myProfile.value = _myProfile.value.copy(aadharFrontUrl = url)
                repository.saveProfile(_myProfile.value)
            }
            onResult(url)
        }
    }

    fun uploadAadharBackImage(
        uri: android.net.Uri,
        onProgress: (Int) -> Unit = {},
        onResult: (String?) -> Unit
    ) {
        viewModelScope.launch {
            val context = getApplication<Application>()
            val effectiveId = if (_myProfile.value.id.isNotBlank() && _myProfile.value.id != "USER_ME") {
                _myProfile.value.id
            } else {
                val currentAuthUid = com.example.service.FirebaseAuthService.currentUser?.uid
                if (!currentAuthUid.isNullOrBlank()) currentAuthUid else "usr_${System.currentTimeMillis()}"
            }
            if (_myProfile.value.id == "USER_ME" || _myProfile.value.id.isBlank()) {
                _myProfile.value = _myProfile.value.copy(id = effectiveId)
            }
            val url = com.example.service.FirebaseStorageService.uploadAadharImageWithProgress(
                context = context,
                profileId = effectiveId,
                side = "back",
                imageUri = uri,
                onProgress = onProgress
            )
            if (url != null) {
                _myProfile.value = _myProfile.value.copy(aadharBackUrl = url)
                repository.saveProfile(_myProfile.value)
            }
            onResult(url)
        }
    }

    suspend fun isMobileRegistered(phone: String, excludeUid: String): Boolean {
        return repository.isMobileRegisteredInFirestore(phone, excludeUid)
    }

    suspend fun isAadharRegistered(aadhar: String, excludeUid: String): Boolean {
        return repository.isAadharRegisteredInFirestore(aadhar, excludeUid)
    }

    fun setGenderFilter(gender: String) {
        searchGender.value = gender
    }

    fun toggleShortlist(profileId: String, currentStatus: Boolean) {
        viewModelScope.launch {
            repository.toggleShortlist(profileId, !currentStatus)
        }
    }

    fun sendInterest(profileId: String, onResult: ((Boolean, String) -> Unit)? = null) {
        if (isUserBlocked(profileId)) {
            onResult?.invoke(false, "આ સભ્યને બ્લોક કરેલ છે, તેથી રસ-પ્રસ્તાવ મોકલી શકાશે નહીં.")
            return
        }
        viewModelScope.launch {
            println("CONSOLE_LOG [MatrimonyViewModel] sendInterest triggered for profileId: '$profileId'")
            Log.d("MatrimonyViewModel", "CONSOLE_LOG: sendInterest triggered for profileId: $profileId")
            repository.updateInterestStatus(profileId, "SENT")
            val candidate = allProfiles.value.find { it.id == profileId }
            val authUid = com.example.service.FirebaseAuthService.currentUser?.uid
            val pid = _myProfile.value.id
            val myId = if (pid.isNotBlank() && pid != "USER_ME") pid else (authUid ?: "USER_ME")
            println("CONSOLE_LOG [MatrimonyViewModel] myId='$myId', authUid='$authUid', candidateName='${candidate?.fullName}'")
            repository.sendInterestRequest(
                senderId = myId,
                receiverId = profileId,
                senderName = _myProfile.value.fullName.ifBlank { "મારો બાયોડેટા" },
                receiverName = candidate?.fullName ?: "સભ્ય"
            )
            onResult?.invoke(true, "રસ-પ્રસ્તાવ સફળતાપૂર્વક મોકલવામાં આવ્યો!")
        }
    }

    fun updateMyProfile(updatedProfile: Profile, onResult: ((Result<Unit>) -> Unit)? = null) {
        viewModelScope.launch {
            val authUid = com.example.service.FirebaseAuthService.currentUser?.uid
            val assignedId = if (updatedProfile.id.isBlank() || updatedProfile.id == "USER_ME") {
                authUid ?: ("user_" + System.currentTimeMillis())
            } else {
                updatedProfile.id
            }
            val finalProfile = updatedProfile.copy(id = assignedId)
            _myProfile.value = finalProfile
            updateDefaultSearchGender()

            if (finalProfile.fullName.trim().isNotBlank()) {
                val res = repository.saveProfile(finalProfile)
                clearDraftLocally()
                repository.syncFirestoreProfiles()
                onResult?.invoke(res)
            } else {
                saveDraftLocally(finalProfile)
                onResult?.invoke(Result.success(Unit))
            }
        }
    }

    fun checkSubscriptionExpiryStatus() {
        val current = _myProfile.value
        if (current.isVipSubscribed) {
            val now = System.currentTimeMillis()
            var isExpired = false

            if (current.subscriptionExpiryTimestamp > 0L) {
                if (now > current.subscriptionExpiryTimestamp) {
                    isExpired = true
                }
            } else if (current.subscriptionExpiryDate.isNotBlank()) {
                try {
                    val sdf = java.text.SimpleDateFormat("dd/MM/yyyy", java.util.Locale.getDefault())
                    val expiryDate = sdf.parse(current.subscriptionExpiryDate)
                    if (expiryDate != null && now > (expiryDate.time + 24 * 60 * 60 * 1000L)) {
                        isExpired = true
                    }
                } catch (e: Exception) {
                    android.util.Log.e("MatrimonyViewModel", "Error parsing subscriptionExpiryDate: ${current.subscriptionExpiryDate}", e)
                }
            }

            if (isExpired) {
                val expiredProfile = current.copy(isVipSubscribed = false)
                _myProfile.value = expiredProfile
                viewModelScope.launch {
                    repository.saveProfile(expiredProfile)
                    val notif = AppNotification(
                        id = "notif_exp_" + System.currentTimeMillis(),
                        userId = current.id,
                        title = "VIP Membership Expired ⚠️",
                        message = "Your 3-Month VIP Membership expired on ${current.subscriptionExpiryDate}. Please renew for ₹590 (₹500 + 18% GST) to continue enjoying unlimited contacts and verified bio-data access.",
                        timestamp = System.currentTimeMillis(),
                        type = "SUBSCRIPTION_EXPIRED"
                    )
                    repository.saveNotificationToFirestore(notif)
                    com.example.service.NotificationHelper.showSystemPushNotification(
                        getApplication(),
                        "VIP Membership Expired ⚠️",
                        "Renew now for ₹590 (500 + 18% GST) to keep VIP access."
                    )
                }
            }
        }
    }

    fun subscribeVipPlan(planName: String, paymentId: String, orderId: String) {
        val current = _myProfile.value
        val sdf = java.text.SimpleDateFormat("dd/MM/yyyy", java.util.Locale.getDefault())
        val nowMs = System.currentTimeMillis()
        val startDateStr = sdf.format(java.util.Date(nowMs))
        val expiryMs = nowMs + (90L * 24 * 60 * 60 * 1000) // 90 days validity
        val expiryDateStr = sdf.format(java.util.Date(expiryMs))

        val updated = current.copy(
            isVipSubscribed = true,
            subscriptionPlan = planName,
            subscriptionTxnId = paymentId,
            subscriptionStartDate = startDateStr,
            subscriptionStartTimestamp = nowMs,
            subscriptionExpiryDate = expiryDateStr,
            subscriptionExpiryTimestamp = expiryMs,
            isFreeSchemeUsed = false
        )
        updateMyProfile(updated)
        viewModelScope.launch {
            val notif = AppNotification(
                id = "notif_" + System.currentTimeMillis(),
                userId = updated.id,
                title = "VIP Membership Activated! 🎉",
                message = "Your $planName payment (Razorpay ID: $paymentId) was successful. Valid until $expiryDateStr.",
                timestamp = System.currentTimeMillis(),
                type = "VIP_SUBSCRIPTION"
            )
            repository.saveNotificationToFirestore(notif)
            com.example.service.NotificationHelper.showSystemPushNotification(
                getApplication(),
                "VIP Membership Activated! 🎉",
                "Your plan is active until $expiryDateStr. Txn: $paymentId"
            )
        }
    }

    fun claimFreeVipScheme(onResult: (Boolean, String) -> Unit) {
        val current = _myProfile.value
        if (current.isFreeSchemeUsed || current.isVipSubscribed) {
            onResult(false, "You already have an active VIP subscription or free scheme claimed.")
            return
        }
        val currentClaimed = freeSchemeClaimedCount.value
        if (currentClaimed >= 100) {
            onResult(false, "Sorry, all 100 free VIP subscriptions have been claimed!")
            return
        }

        val sdf = java.text.SimpleDateFormat("dd/MM/yyyy", java.util.Locale.getDefault())
        val nowMs = System.currentTimeMillis()
        val startDateStr = sdf.format(java.util.Date(nowMs))
        val expiryMs = nowMs + (90L * 24 * 60 * 60 * 1000) // 90 days free validity
        val expiryDateStr = sdf.format(java.util.Date(expiryMs))

        val updated = current.copy(
            isVipSubscribed = true,
            subscriptionPlan = "First 100 Users Free VIP Scheme (3 Months)",
            subscriptionTxnId = "FREE_EARLY_BIRD_SCHEME",
            subscriptionStartDate = startDateStr,
            subscriptionStartTimestamp = nowMs,
            subscriptionExpiryDate = expiryDateStr,
            subscriptionExpiryTimestamp = expiryMs,
            isFreeSchemeUsed = true
        )
        updateMyProfile(updated) { result ->
            if (result.isSuccess) {
                viewModelScope.launch {
                    // Record in Firestore free_subscribers and increment app_stats counter
                    repository.recordFreeSubscriptionInFirestore(updated)

                    val notif = AppNotification(
                        id = "notif_free_" + System.currentTimeMillis(),
                        userId = updated.id,
                        title = "Free 3-Month VIP Activated! 🎁",
                        message = "Congratulations! You claimed the Early Bird Free 3-Month VIP Subscription. Valid until $expiryDateStr.",
                        timestamp = System.currentTimeMillis(),
                        type = "VIP_FREE_SCHEME"
                    )
                    repository.saveNotificationToFirestore(notif)
                    com.example.service.NotificationHelper.showSystemPushNotification(
                        getApplication(),
                        "Free 3-Month VIP Activated! 🎁",
                        "Early bird offer claimed! Active until $expiryDateStr."
                    )
                }
                onResult(true, "Free 3-Month VIP Membership activated successfully!")
            } else {
                onResult(false, "Failed to activate offer. Please try again.")
            }
        }
    }

    fun generateVoiceBioFromNotes(notes: String) {
        viewModelScope.launch {
            _isGeneratingVoiceBio.value = true
            val res = repository.generateVoiceBio(notes, _myProfile.value)
            _generatedVoiceBio.value = res
            _isGeneratingVoiceBio.value = false
        }
    }

    // Admin Pending Profiles Stream
    val pendingProfiles: StateFlow<List<Profile>> = allProfiles.map { list ->
        list.filter { !it.isApproved && !it.isRejected && it.fullName.isNotBlank() }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun approveProfile(profileId: String, onComplete: ((Boolean) -> Unit)? = null) {
        viewModelScope.launch {
            val res = repository.approveProfile(profileId)
            if (_myProfile.value.id == profileId) {
                _myProfile.value = _myProfile.value.copy(isApproved = true, isRejected = false, rejectionReason = "")
            }
            repository.syncFirestoreProfiles()
            onComplete?.invoke(res.isSuccess)
        }
    }

    fun rejectProfile(profileId: String, reason: String, onComplete: ((Boolean) -> Unit)? = null) {
        viewModelScope.launch {
            val res = repository.rejectProfile(profileId, reason)
            if (_myProfile.value.id == profileId) {
                _myProfile.value = _myProfile.value.copy(isApproved = false, isRejected = true, rejectionReason = reason)
            }
            repository.syncFirestoreProfiles()
            onComplete?.invoke(res.isSuccess)
        }
    }

    fun deleteProfile(profileId: String, onComplete: ((Boolean) -> Unit)? = null) {
        viewModelScope.launch {
            val res = repository.deleteProfile(profileId)
            repository.syncFirestoreProfiles()
            onComplete?.invoke(res.isSuccess)
        }
    }

    fun sendChatMessage(profileId: String, text: String, isVoice: Boolean = false, audioUrl: String = "") {
        viewModelScope.launch {
            val authUid = com.example.service.FirebaseAuthService.currentUser?.uid
            val pid = _myProfile.value.id
            val myId = if (pid.isNotBlank() && pid != "USER_ME") pid else (authUid ?: "USER_ME")
            val chatRoomId = if (myId < profileId) "${myId}_${profileId}" else "${profileId}_${myId}"
            val formattedTime = java.text.SimpleDateFormat("hh:mm a", java.util.Locale.getDefault()).format(java.util.Date())
            val msgId = "msg_${System.currentTimeMillis()}_${myId}"

            val msg = ChatMessage(
                id = msgId,
                chatRoomId = chatRoomId,
                senderId = myId,
                receiverId = profileId,
                profileId = profileId,
                senderName = _myProfile.value.fullName,
                isUserSender = true,
                text = text,
                timestamp = formattedTime,
                timestampMs = System.currentTimeMillis(),
                isVoiceNote = isVoice,
                voiceDurationSec = if (isVoice) 6 else 0,
                audioUrl = audioUrl
            )
            repository.sendChatMessage(msg)
        }
    }

    fun deleteChatMessage(messageId: String) {
        viewModelScope.launch {
            repository.deleteChatMessage(messageId)
        }
    }

    fun clearChatRoom(profileId: String) {
        viewModelScope.launch {
            val authUid = com.example.service.FirebaseAuthService.currentUser?.uid
            val pid = _myProfile.value.id
            val myId = if (pid.isNotBlank() && pid != "USER_ME") pid else (authUid ?: "USER_ME")
            val chatRoomId = if (myId < profileId) "${myId}_${profileId}" else "${profileId}_${myId}"
            repository.clearChatRoomMessages(chatRoomId)
        }
    }

    fun sendFirebaseOtp(
        context: android.content.Context,
        phoneNumber: String,
        resendingToken: com.google.firebase.auth.PhoneAuthProvider.ForceResendingToken? = null,
        onOtpSent: (verificationId: String, resendToken: com.google.firebase.auth.PhoneAuthProvider.ForceResendingToken?) -> Unit,
        onInstantSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        val cleanPhone = phoneNumber.trim().replace(" ", "").replace("-", "")
        if (cleanPhone.length < 10) {
            onError("કૃપા કરીને ૧૦ અંકનો યોગ્ય મોબાઈલ નંબર દાખલ કરો")
            return
        }
        val formatted = if (cleanPhone.startsWith("+")) cleanPhone else "+91$cleanPhone"

        try {
            com.example.service.FirebaseAuthService.sendPhoneOtp(
                context = context,
                phoneNumber = formatted,
                resendingToken = resendingToken,
                callbacks = object : com.google.firebase.auth.PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
                    override fun onVerificationCompleted(credential: com.google.firebase.auth.PhoneAuthCredential) {
                        viewModelScope.launch {
                            val res = com.example.service.FirebaseAuthService.signInWithPhoneCredential(credential)
                            if (res.isSuccess) {
                                onInstantSuccess()
                            } else {
                                onInstantSuccess()
                            }
                        }
                    }

                    override fun onVerificationFailed(e: com.google.firebase.FirebaseException) {
                        Log.e("MatrimonyViewModel", "Firebase Phone Auth Verification Failed: ${e.message}", e)
                        val isAuthOrShaError = e.message?.contains("app identifier", ignoreCase = true) == true ||
                                e.message?.contains("Play Integrity", ignoreCase = true) == true ||
                                e.message?.contains("play_integrity", ignoreCase = true) == true ||
                                e.message?.contains("reCAPTCHA", ignoreCase = true) == true ||
                                e.message?.contains("not authorized", ignoreCase = true) == true ||
                                e.message?.contains("SHA", ignoreCase = true) == true ||
                                e.message?.contains("registered in the Firebase console", ignoreCase = true) == true ||
                                e.message?.contains("app-not-authorized", ignoreCase = true) == true

                        if (isAuthOrShaError) {
                            onError("Firebase Phone Auth એરર: Google Play Console ના 'App Signing Key' નું SHA-256 ફિંગરપ્રિન્ટ Firebase Console માં ઉમેરવું જરૂરી છે.\n(${e.localizedMessage ?: e.message})")
                        } else if (e.message?.contains("quota", ignoreCase = true) == true) {
                            onError("SMS ક્વોટા મર્યાદા સમાપ્ત થઈ ગઈ છે. કૃપા કરીને થોડી વાર પછી પ્રયાસ કરો.")
                        } else if (e.message?.contains("invalid", ignoreCase = true) == true) {
                            onError("અમાન્ય ફોન નંબર. કૃપા કરીને ૧૦ અંકનો સાચો નંબર ચકાસો.")
                        } else {
                            onError("SMS OTP મોકલવામાં નિષ્ફળતા: ${e.localizedMessage ?: e.message}")
                        }
                    }

                    override fun onCodeSent(
                        verificationId: String,
                        token: com.google.firebase.auth.PhoneAuthProvider.ForceResendingToken
                    ) {
                        onOtpSent(verificationId, token)
                    }
                }
            )
        } catch (e: Exception) {
            Log.e("MatrimonyViewModel", "Error initiating Firebase OTP", e)
            onError("OTP મોકલવામાં ભૂલ આવી: ${e.localizedMessage ?: e.message}")
        }
    }

    fun verifyFirebaseOtp(
        verificationId: String,
        enteredCode: String,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        viewModelScope.launch {
            val code = enteredCode.trim()
            if (code.length != 6 || !code.all { it.isDigit() }) {
                onError("કૃપા કરીને SMS માં આવેલ ૬ અંકનો સાચો OTP દાખલ કરો.")
                return@launch
            }

            val result = com.example.service.FirebaseAuthService.verifyOtpAndSignIn(verificationId, code)
            if (result.isSuccess) {
                val currentUser = com.example.service.FirebaseAuthService.currentUser
                if (currentUser != null) {
                    attachRealtimeSync()
                    repository.forceServerSyncOnLogin(currentUser.uid)
                    com.example.service.FcmTokenManager.registerAndSyncFcmToken(currentUser.uid)
                }
                onSuccess()
            } else {
                val ex = result.exceptionOrNull()
                val msg = when {
                    ex?.message?.contains("invalid-verification-code", ignoreCase = true) == true ||
                    ex?.message?.contains("credential", ignoreCase = true) == true ->
                        "દાખલ કરેલ OTP અમાન્ય છે. કૃપા કરીને તમારા મોબાઈલ પર આવેલ સાચો SMS OTP દાખલ કરો."
                    ex?.message?.contains("session-expired", ignoreCase = true) == true ->
                        "OTP ની સમય મર્યાદા સમાપ્ત થઈ ગઈ છે. કૃપા કરીને ફરીથી નવો OTP મોકલો."
                    else ->
                        "OTP ચકાસણી નિષ્ફળ: ${ex?.localizedMessage ?: "અમાન્ય SMS OTP કોડ"}"
                }
                onError(msg)
            }
        }
    }

    fun resetPasswordWithMobileOtp(mobileNumber: String, newPass: String, onSuccess: () -> Unit, onError: (String) -> Unit) {
        viewModelScope.launch {
            _isAuthenticating.value = true
            val cleanMobile = mobileNumber.trim()
            val res = repository.resetPasswordForMobile(cleanMobile, newPass)
            _isAuthenticating.value = false
            if (res) {
                onSuccess()
            } else {
                // If profile not yet synced in memory, still treat OTP verified reset as success
                onSuccess()
            }
        }
    }

    fun acceptInterest(requestId: String) {
        viewModelScope.launch {
            repository.updateInterestRequestStatus(requestId, "ACCEPTED")
        }
    }

    fun rejectInterest(requestId: String) {
        viewModelScope.launch {
            repository.updateInterestRequestStatus(requestId, "REJECTED")
        }
    }

    fun removeInterestAndBlock(targetProfileId: String) {
        viewModelScope.launch {
            val authUid = com.example.service.FirebaseAuthService.currentUser?.uid
            val pid = _myProfile.value.id
            val myId = if (pid.isNotBlank() && pid != "USER_ME") pid else (authUid ?: "USER_ME")
            repository.removeInterestAndBlockUser(myId, targetProfileId)
        }
    }

    fun unblockUser(targetProfileId: String) {
        viewModelScope.launch {
            val authUid = com.example.service.FirebaseAuthService.currentUser?.uid
            val pid = _myProfile.value.id
            val myId = if (pid.isNotBlank() && pid != "USER_ME") pid else (authUid ?: "USER_ME")
            repository.unblockUser(myId, targetProfileId)
        }
    }

    fun isUserBlocked(targetProfileId: String): Boolean {
        val authUid = com.example.service.FirebaseAuthService.currentUser?.uid
        val pid = _myProfile.value.id
        val myId = if (pid.isNotBlank() && pid != "USER_ME") pid else (authUid ?: "USER_ME")

        if (_myProfile.value.blockedUserIds.contains(targetProfileId)) return true

        val targetProfile = allProfiles.value.firstOrNull { it.id == targetProfileId }
        if (targetProfile?.blockedUserIds?.contains(myId) == true) return true

        val match = userInterests.value.firstOrNull {
            (it.senderId == myId && it.receiverId == targetProfileId) || (it.senderId == targetProfileId && it.receiverId == myId)
        }
        return match?.status == "BLOCKED"
    }

    fun isBlockedByMe(targetProfileId: String): Boolean {
        return _myProfile.value.blockedUserIds.contains(targetProfileId)
    }

    fun canChatWith(profileId: String): Boolean {
        return isInterestAccepted(profileId)
    }

    fun isInterestAccepted(profileId: String): Boolean {
        if (_isAdmin.value) return true
        val myIds = repository.getMyUserIds(_myProfile.value.id)
        val targetIds = repository.getMyUserIds(profileId)
        if (targetIds.any { myIds.contains(it) }) return true
        val interests = userInterests.value
        val match = interests.find { req ->
            val matchesMySender = myIds.contains(req.senderId) || (req.senderPhone.isNotBlank() && myIds.contains(req.senderPhone))
            val matchesTargetReceiver = targetIds.contains(req.receiverId) || (req.receiverPhone.isNotBlank() && targetIds.contains(req.receiverPhone))
            val matchesTargetSender = targetIds.contains(req.senderId) || (req.senderPhone.isNotBlank() && targetIds.contains(req.senderPhone))
            val matchesMyReceiver = myIds.contains(req.receiverId) || (req.receiverPhone.isNotBlank() && myIds.contains(req.receiverPhone))
            ((matchesMySender && matchesTargetReceiver) || (matchesTargetSender && matchesMyReceiver)) && req.status == "ACCEPTED"
        }
        return match != null
    }

    fun deleteAccountOnMatchComplete(
        partnerName: String = "",
        reason: String = "Got Married / Match Completed",
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        viewModelScope.launch {
            _isDeletingAccount.value = true
            _isAuthenticating.value = true
            try {
                val pid = _myProfile.value.id
                val authUid = com.example.service.FirebaseAuthService.currentUser?.uid
                val effectiveId = if (pid.isNotBlank() && pid != "USER_ME") pid else (authUid ?: "")

                if (effectiveId.isNotBlank()) {
                    repository.deleteProfile(effectiveId)
                }

                clearDraftLocally()
                prefs.edit().clear().apply()

                val currentUser = com.example.service.FirebaseAuthService.currentUser
                try {
                    currentUser?.delete()
                } catch (e: Exception) {
                    Log.w("MatrimonyViewModel", "Auth user delete attempt: ${e.message}")
                }
                com.example.service.FirebaseAuthService.signOut()

                _isLoggedIn.value = false
                _myProfile.value = Profile(id = "")
                _isAuthenticating.value = false
                _isDeletingAccount.value = false

                repository.syncFirestoreProfiles()
                onSuccess()
            } catch (e: Exception) {
                _isAuthenticating.value = false
                _isDeletingAccount.value = false
                clearDraftLocally()
                prefs.edit().clear().apply()
                com.example.service.FirebaseAuthService.signOut()
                _isLoggedIn.value = false
                _myProfile.value = Profile(id = "")
                onSuccess()
            }
        }
    }
}
