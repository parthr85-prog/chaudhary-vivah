package com.example.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.*
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.R
import com.example.model.InterestRequest
import com.example.model.Profile
import com.example.ui.theme.*
import com.example.ui.viewmodel.MatrimonyViewModel
import com.example.util.LocaleStrings

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InterestsScreen(
    viewModel: MatrimonyViewModel,
    onBackClick: () -> Unit,
    onNavigateToProfileDetail: (String) -> Unit,
    onNavigateToChat: (String) -> Unit
) {
    val appLanguage by viewModel.appLanguage.collectAsState()
    val isDeletingAccount by viewModel.isDeletingAccount.collectAsState()

    if (isDeletingAccount) {
        androidx.compose.ui.window.Dialog(
            onDismissRequest = { /* strictly wait */ },
            properties = androidx.compose.ui.window.DialogProperties(dismissOnBackPress = false, dismissOnClickOutside = false)
        ) {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = SurfaceCream),
                border = androidx.compose.foundation.BorderStroke(2.dp, RoyalGold),
                modifier = Modifier.padding(16.dp).fillMaxWidth(0.88f)
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(48.dp),
                        color = Color.Red,
                        strokeWidth = 4.dp
                    )
                    Text(
                        text = if (appLanguage == "gu") "એકાઉન્ટ ડિલીટ થઈ રહ્યું છે..." else "Deleting Account...",
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        color = Color.Red,
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                    )
                    Text(
                        text = if (appLanguage == "gu") "કૃપા કરીને રાહ જુઓ, ફાયરબેઝમાંથી તમારો તમામ ડેટા કાયમી ધોરણે રદ થઈ રહ્યો છે." else "Please wait, all your data is being permanently deleted from Firebase.",
                        fontSize = 12.sp,
                        color = Color.DarkGray,
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                    )
                }
            }
        }
    }
    val strings = LocaleStrings.getStrings(appLanguage)
    val userInterests by viewModel.userInterests.collectAsState()
    val allProfiles by viewModel.allProfiles.collectAsState()
    val myProfile by viewModel.myProfile.collectAsState()
    val isAdmin by viewModel.isAdmin.collectAsState()
    val isRefreshing by viewModel.isRefreshing.collectAsState()

    var selectedTabIndex by remember { mutableStateOf(0) }
    var showChatLockDialog by remember { mutableStateOf<String?>(null) }
    var showBlockDialogForTarget by remember { mutableStateOf<Profile?>(null) }
    var showLockedBioDataForProfile by remember { mutableStateOf<Profile?>(null) }
    val context = androidx.compose.ui.platform.LocalContext.current

    val authUid = com.example.service.FirebaseAuthService.currentUser?.uid ?: ""
    val pid = myProfile.id
    val myIds = remember(myProfile, allProfiles, authUid) {
        val set = mutableSetOf<String>()
        if (pid.isNotBlank()) {
            set.add(pid)
            val cleanPid = pid.replace(Regex("[^0-9]"), "").takeLast(10)
            if (cleanPid.length == 10) {
                set.add(cleanPid)
                set.add("USER_$cleanPid")
            }
        }
        if (authUid.isNotBlank()) set.add(authUid)
        if (myProfile.phoneContact.isNotBlank()) {
            val cleanPhone = myProfile.phoneContact.replace(Regex("[^0-9]"), "").takeLast(10)
            set.add(myProfile.phoneContact)
            set.add("USER_${myProfile.phoneContact}")
            if (cleanPhone.length == 10) {
                set.add(cleanPhone)
                set.add("USER_$cleanPhone")
            }
        }
        if (myProfile.parentPhoneContact.isNotBlank()) {
            val cleanParent = myProfile.parentPhoneContact.replace(Regex("[^0-9]"), "").takeLast(10)
            set.add(myProfile.parentPhoneContact)
            set.add("USER_${myProfile.parentPhoneContact}")
            if (cleanParent.length == 10) {
                set.add(cleanParent)
                set.add("USER_$cleanParent")
            }
        }
        set.add("USER_ME")

        allProfiles.forEach { p ->
            val matchesAuth = authUid.isNotBlank() && (p.id == authUid || p.phoneContact == authUid)
            val matchesPid = pid.isNotBlank() && (p.id == pid || p.phoneContact == pid)
            val matchesPhone = myProfile.phoneContact.isNotBlank() && (
                p.phoneContact == myProfile.phoneContact || 
                (myProfile.phoneContact.length >= 10 && p.phoneContact.endsWith(myProfile.phoneContact.takeLast(10)))
            )
            if (matchesAuth || matchesPid || matchesPhone) {
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
            }
        }
        set
    }

    // Received Interests (others sent to me)
    val receivedInterests = userInterests.filter { req ->
        val matchesReceiver = myIds.contains(req.receiverId) ||
                (req.receiverPhone.isNotBlank() && myIds.contains(req.receiverPhone)) ||
                req.receiverId == "USER_ME"
        val matchesSender = myIds.contains(req.senderId) ||
                (req.senderPhone.isNotBlank() && myIds.contains(req.senderPhone))
        matchesReceiver && !matchesSender && req.status != "BLOCKED"
    }
    // Sent Interests (I sent to others)
    val sentInterests = userInterests.filter { req ->
        val matchesReceiver = myIds.contains(req.receiverId) ||
                (req.receiverPhone.isNotBlank() && myIds.contains(req.receiverPhone))
        val matchesSender = myIds.contains(req.senderId) ||
                (req.senderPhone.isNotBlank() && myIds.contains(req.senderPhone)) ||
                req.senderId == "USER_ME"
        matchesSender && !matchesReceiver && req.status != "BLOCKED"
    }
    // Blocked Profiles
    val blockedProfiles = remember(allProfiles, userInterests, myProfile) {
        allProfiles.filter { prof ->
            viewModel.isUserBlocked(prof.id) || myProfile.blockedUserIds.contains(prof.id)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = if (appLanguage == "gu") "રસ-પ્રસ્તાવ અને જોડાણ" else "Interests & Requests",
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp,
                            color = Color.White
                        )
                        Text(
                            text = if (appLanguage == "gu") "તમારા પરસ્પર પસંદગી સંબંધો" else "Manage your mutual matches & requests",
                            fontSize = 12.sp,
                            color = RoyalGold
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = RoyalMaroon)
            )
        }
    ) { padding ->
        PullToRefreshBox(
            isRefreshing = isRefreshing,
            onRefresh = { viewModel.refreshDashboard() },
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color(0xFFFBF8F5))
            ) {
            // Tab Header
            TabRow(
                selectedTabIndex = selectedTabIndex,
                containerColor = Color.White,
                contentColor = RoyalMaroon,
                indicator = { tabPositions ->
                    TabRowDefaults.SecondaryIndicator(
                        modifier = Modifier.tabIndicatorOffset(tabPositions[selectedTabIndex]),
                        color = RoyalMaroon,
                        height = 3.dp
                    )
                }
            ) {
                Tab(
                    selected = selectedTabIndex == 0,
                    onClick = { selectedTabIndex = 0 },
                    text = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.MoveToInbox, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = if (appLanguage == "gu") "મને મળેલ (${receivedInterests.size})" else "Received (${receivedInterests.size})",
                                fontWeight = if (selectedTabIndex == 0) FontWeight.Bold else FontWeight.Normal
                            )
                        }
                    }
                )
                Tab(
                    selected = selectedTabIndex == 1,
                    onClick = { selectedTabIndex = 1 },
                    text = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.AutoMirrored.Filled.Send, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = if (appLanguage == "gu") "મેં મોકલેલ (${sentInterests.size})" else "Sent (${sentInterests.size})",
                                fontWeight = if (selectedTabIndex == 1) FontWeight.Bold else FontWeight.Normal
                            )
                        }
                    }
                )
                Tab(
                    selected = selectedTabIndex == 2,
                    onClick = { selectedTabIndex = 2 },
                    text = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Block, contentDescription = null, modifier = Modifier.size(18.dp), tint = if (selectedTabIndex == 2) Color.Red else Color.Unspecified)
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = if (appLanguage == "gu") "બ્લોક કરેલ (${blockedProfiles.size})" else "Blocked (${blockedProfiles.size})",
                                fontWeight = if (selectedTabIndex == 2) FontWeight.Bold else FontWeight.Normal,
                                color = if (selectedTabIndex == 2) Color.Red else Color.Unspecified
                            )
                        }
                    }
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            if (selectedTabIndex == 2) {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = if (blockedProfiles.isEmpty()) PaddingValues(0.dp) else PaddingValues(16.dp),
                    verticalArrangement = if (blockedProfiles.isEmpty()) Arrangement.Center else Arrangement.spacedBy(12.dp)
                ) {
                    if (blockedProfiles.isEmpty()) {
                        item {
                            Box(
                                modifier = Modifier
                                    .fillParentMaxSize()
                                    .padding(32.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Icon(Icons.Default.Block, contentDescription = null, modifier = Modifier.size(64.dp), tint = Color.LightGray)
                                    Spacer(modifier = Modifier.height(16.dp))
                                    Text(
                                        text = if (appLanguage == "gu") "કોઈ બ્લોક કરેલ સભ્ય નથી." else "No blocked members.",
                                        fontWeight = FontWeight.Medium,
                                        fontSize = 15.sp,
                                        color = Color.Gray
                                    )
                                }
                            }
                        }
                    } else {
                        items(blockedProfiles, key = { it.id }) { prof ->
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { onNavigateToProfileDetail(prof.id) },
                                shape = RoundedCornerShape(16.dp),
                                colors = CardDefaults.cardColors(containerColor = Color.White),
                                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                            ) {
                                Row(
                                    modifier = Modifier.padding(16.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(56.dp)
                                            .clip(CircleShape)
                                            .border(1.5.dp, Color.Red, CircleShape)
                                    ) {
                                        val imgUrl = prof.getEffectiveProfileImageUrl()
                                        if (imgUrl.isNotBlank()) {
                                            AsyncImage(
                                                model = imgUrl,
                                                contentDescription = prof.fullName,
                                                contentScale = ContentScale.Crop,
                                                modifier = Modifier.fillMaxSize()
                                            )
                                        } else {
                                            Image(
                                                painter = painterResource(id = R.drawable.img_matrimony_hero_1784990427738),
                                                contentDescription = null,
                                                contentScale = ContentScale.Crop,
                                                modifier = Modifier.fillMaxSize()
                                            )
                                        }
                                    }

                                    Spacer(modifier = Modifier.width(12.dp))

                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(prof.fullName, fontWeight = FontWeight.Bold, fontSize = 16.sp, color = RoyalMaroon)
                                        Text("${prof.subCaste} • ગોત્ર: ${prof.gotra}", fontSize = 12.sp, color = Color.DarkGray)
                                        Spacer(modifier = Modifier.height(2.dp))
                                        Text("🚫 બ્લોક કરેલ સભ્ય", fontSize = 11.sp, color = Color.Red, fontWeight = FontWeight.Bold)
                                    }

                                    Button(
                                        onClick = {
                                            viewModel.unblockUser(prof.id)
                                            android.widget.Toast.makeText(context, "સભ્ય અનબ્લોક કરવામાં આવ્યો છે.", android.widget.Toast.LENGTH_SHORT).show()
                                        },
                                        colors = ButtonDefaults.buttonColors(containerColor = VerifiedGreen),
                                        shape = RoundedCornerShape(8.dp),
                                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp)
                                    ) {
                                        Text("અનબ્લોક", fontSize = 12.sp, color = Color.White, fontWeight = FontWeight.Bold)
                                    }
                                }
                            }
                        }
                    }
                }
            } else {
                val currentList = if (selectedTabIndex == 0) receivedInterests else sentInterests

            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = if (currentList.isEmpty()) PaddingValues(0.dp) else PaddingValues(16.dp),
                verticalArrangement = if (currentList.isEmpty()) Arrangement.Center else Arrangement.spacedBy(12.dp)
            ) {
                if (currentList.isEmpty()) {
                    item {
                        Box(
                            modifier = Modifier
                                .fillParentMaxSize()
                                .padding(32.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Icon(
                                    imageVector = Icons.Default.VolunteerActivism,
                                    contentDescription = null,
                                    modifier = Modifier.size(64.dp),
                                    tint = Color.LightGray
                                )
                                Spacer(modifier = Modifier.height(16.dp))
                                Text(
                                    text = if (selectedTabIndex == 0) {
                                        if (appLanguage == "gu") "હાલમાં કોઈ નવો રસ-પ્રસ્તાવ મળેલ નથી." else "No interest requests received yet."
                                    } else {
                                        if (appLanguage == "gu") "તમે હજુ સુધી કોઈ પ્રોફાઇલને રસ મોકલ્યો નથી." else "You haven't sent any interest requests yet."
                                    },
                                    fontWeight = FontWeight.Medium,
                                    fontSize = 15.sp,
                                    color = Color.Gray
                                )
                            }
                        }
                    }
                } else {
                    items(currentList, key = { it.id }) { item ->
                        val targetProfileId = if (selectedTabIndex == 0) item.senderId else item.receiverId
                        val targetProfile = allProfiles.find { it.id == targetProfileId }
                            ?: Profile(id = targetProfileId, fullName = if (selectedTabIndex == 0) item.senderName else item.receiverName)

                        val isAccepted = item.status == "ACCEPTED" || viewModel.isInterestAccepted(targetProfile.id)
                        val isOwnProfile = targetProfile.id == myProfile.id
                        val canViewFull = isAdmin || isOwnProfile || isAccepted

                        InterestCardItem(
                            request = item,
                            targetProfile = targetProfile,
                            isReceived = selectedTabIndex == 0,
                            appLanguage = appLanguage,
                            onAccept = { viewModel.acceptInterest(item.id) },
                            onReject = { viewModel.rejectInterest(item.id) },
                            onRemoveOrBlock = { showBlockDialogForTarget = targetProfile },
                            onViewDetail = {
                                if (canViewFull) {
                                    onNavigateToProfileDetail(targetProfile.id)
                                } else {
                                    showLockedBioDataForProfile = targetProfile
                                }
                            },
                            onChatClick = {
                                if (item.status == "ACCEPTED") {
                                    onNavigateToChat(targetProfile.id)
                                } else {
                                    showChatLockDialog = targetProfile.fullName.ifBlank { "આ સભ્ય" }
                                }
                            }
                        )
                    }
                }
            }
        }
    }
    }
    }

    showChatLockDialog?.let { name ->
        AlertDialog(
            onDismissRequest = { showChatLockDialog = null },
            icon = {
                Icon(Icons.Default.Lock, contentDescription = null, tint = RoyalMaroon, modifier = Modifier.size(36.dp))
            },
            title = {
                Text(
                    text = if (appLanguage == "gu") "ચેટિંગ લોક છે" else "Chat Locked",
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp
                )
            },
            text = {
                Text(
                    text = if (appLanguage == "gu")
                        "$name સાથે વાતચીત કરવા માટે બંને પક્ષ તરફથી રસ-પ્રસ્તાવ સ્વીકારાયેલ (Accepted) હોવો આવશ્યક છે. જ્યારે બંને પ્રોફાઇલ પ્રસ્તાવ સ્વીકારશે ત્યારે ચેટ બોક્સ આપોઆપ અનલૉક થશે."
                    else
                        "Chatting with $name is locked. Both profiles must accept the interest request before starting a conversation.",
                    fontSize = 14.sp
                )
            },
            confirmButton = {
                Button(
                    onClick = { showChatLockDialog = null },
                    colors = ButtonDefaults.buttonColors(containerColor = RoyalMaroon)
                ) {
                    Text(if (appLanguage == "gu") "સમજાયું" else "Got It")
                }
            }
        )
    }

    showBlockDialogForTarget?.let { target ->
        AlertDialog(
            onDismissRequest = { showBlockDialogForTarget = null },
            icon = {
                Icon(Icons.Default.Block, contentDescription = null, tint = Color.Red, modifier = Modifier.size(36.dp))
            },
            title = {
                Text(
                    text = if (appLanguage == "gu") "સભ્યને બ્લોક કરો અને રસ-પ્રસ્તાવ અટકાવો" else "Block User & Prevent Requests",
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp
                )
            },
            text = {
                Text(
                    text = if (appLanguage == "gu")
                        "શું તમે ખરેખર ${target.fullName.ifBlank { "આ સભ્ય" }} ને બ્લોક કરવા અને રસ-પ્રસ્તાવ આવતા અટકાવવા માંગો છો? આનાથી સંદેશાઓ અને રસ-પ્રસ્તાવ મોકલવાનું કાયમી ધોરણે અટકી જશે."
                    else
                        "Are you sure you want to block ${target.fullName.ifBlank { "this member" }} and prevent future interest requests? Messaging and requests will be blocked.",
                    fontSize = 14.sp
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.removeInterestAndBlock(target.id)
                        showBlockDialogForTarget = null
                        android.widget.Toast.makeText(
                            context,
                            if (appLanguage == "gu") "સભ્ય બ્લોક થયો છે અને રસ-પ્રસ્તાવ આવતા અટકાવી દીધા છે." else "User blocked and interest requests prevented.",
                            android.widget.Toast.LENGTH_SHORT
                        ).show()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Red)
                ) {
                    Text(
                        text = if (appLanguage == "gu") "હા, બ્લોક કરો અને પ્રસ્તાવ અટકાવો" else "Yes, Block & Prevent",
                        color = Color.White
                    )
                }
            },
            dismissButton = {
                OutlinedButton(onClick = { showBlockDialogForTarget = null }) {
                    Text(if (appLanguage == "gu") "રદ કરો" else "Cancel")
                }
            }
        )
    }

    showLockedBioDataForProfile?.let { targetProfile ->
        val isInterestSent = targetProfile.interestStatus == "SENT" || targetProfile.interestStatus == "ACCEPTED" || sentInterests.any { it.receiverId == targetProfile.id }
        AlertDialog(
            onDismissRequest = { showLockedBioDataForProfile = null },
            icon = {
                Icon(
                    imageVector = Icons.Default.Lock,
                    contentDescription = null,
                    tint = RoyalMaroon,
                    modifier = Modifier.size(36.dp)
                )
            },
            title = {
                Text(
                    text = if (appLanguage == "gu") "સંપૂર્ણ બાયોડેટા લોક છે (Bio-Data Locked)" else "Bio-Data Locked",
                    fontWeight = FontWeight.Bold,
                    fontSize = 17.sp,
                    color = RoyalMaroon,
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                )
            },
            text = {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = if (appLanguage == "gu")
                            "ચૌધરી વિવાહ પર ગોપનીયતા અને મર્યાદા જાળવવા માટે, ${targetProfile.fullName} નો સંપૂર્ણ બાયોડેટા અને સંચાલન વિગતો ફક્ત ત્યારે જ જોઈ શકાશે જ્યારે સામેના સભ્ય તમારો 'રસ/પ્રસ્તાવ (Interest Request)' સ્વીકારશે."
                        else
                            "To preserve privacy on Chaudhary Vivah, full bio-data and contact details of ${targetProfile.fullName} will only be visible after they accept your Interest Request.",
                        fontSize = 13.sp,
                        color = Color.DarkGray,
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                        lineHeight = 18.sp
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    if (isInterestSent) {
                        Surface(
                            color = VerifiedGreenContainer,
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = if (appLanguage == "gu")
                                    "✓ તમે આ સભ્યને લગ્ન પ્રસ્તાવ મોકલેલ છે. સામે પક્ષ દ્વારા સ્વીકાર થયા પછી સંપૂર્ણ પ્રોફાઇલ અનલોક થશે."
                                else
                                    "✓ You have sent an interest request to this member. The full profile will be unlocked after they accept.",
                                fontSize = 12.sp,
                                color = VerifiedGreen,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(10.dp),
                                textAlign = androidx.compose.ui.text.style.TextAlign.Center
                            )
                        }
                    } else {
                        Text(
                            text = if (appLanguage == "gu")
                                "કૃપા કરીને નીચે આપેલ બટન પર ક્લિક કરી પ્રથમ રસ દાખવો/પ્રસ્તાવ મોકલો."
                            else
                                "Please click the button below to send an interest request first.",
                            fontSize = 12.sp,
                            color = RoyalMaroon,
                            fontWeight = FontWeight.SemiBold,
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                        )
                    }
                }
            },
            confirmButton = {
                if (!isInterestSent) {
                    Button(
                        onClick = {
                            viewModel.sendInterest(targetProfile.id)
                            showLockedBioDataForProfile = null
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = RoyalMaroon)
                    ) {
                        Icon(Icons.AutoMirrored.Filled.Send, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(if (appLanguage == "gu") "રસ દાખવો (Send Interest)" else "Send Interest", fontWeight = FontWeight.Bold, color = Color.White)
                    }
                } else {
                    Button(
                        onClick = { showLockedBioDataForProfile = null },
                        colors = ButtonDefaults.buttonColors(containerColor = RoyalMaroon)
                    ) {
                        Text(if (appLanguage == "gu") "સમજાયું (OK)" else "OK", fontWeight = FontWeight.Bold, color = Color.White)
                    }
                }
            },
            dismissButton = {
                TextButton(onClick = { showLockedBioDataForProfile = null }) {
                    Text(if (appLanguage == "gu") "બંધ કરો (Close)" else "Close", color = Color.Gray)
                }
            }
        )
    }
}

@Composable
fun InterestCardItem(
    request: InterestRequest,
    targetProfile: Profile,
    isReceived: Boolean,
    appLanguage: String,
    onAccept: () -> Unit,
    onReject: () -> Unit,
    onRemoveOrBlock: () -> Unit,
    onViewDetail: () -> Unit,
    onChatClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onViewDetail() },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                // Profile Photo
                Box(
                    modifier = Modifier
                        .size(64.dp)
                        .clip(CircleShape)
                        .border(1.5.dp, RoyalGold, CircleShape)
                ) {
                    val imgUrl = targetProfile.getEffectiveProfileImageUrl()
                    if (imgUrl.isNotBlank()) {
                        AsyncImage(
                            model = imgUrl,
                            contentDescription = targetProfile.fullName,
                            contentScale = ContentScale.Crop,
                            placeholder = painterResource(id = R.drawable.img_matrimony_hero_1784990427738),
                            error = painterResource(id = R.drawable.img_matrimony_hero_1784990427738),
                            modifier = Modifier.fillMaxSize()
                        )
                    } else if (targetProfile.photoRes != 0) {
                        Image(
                            painter = painterResource(id = targetProfile.photoRes),
                            contentDescription = targetProfile.fullName,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize()
                        )
                    } else {
                        Image(
                            painter = painterResource(id = R.drawable.img_matrimony_hero_1784990427738),
                            contentDescription = null,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize()
                        )
                    }
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = targetProfile.fullName.ifBlank { if (isReceived) request.senderName else request.receiverName },
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        color = RoyalMaroon
                    )

                    val subInfo = listOfNotNull(
                        if (targetProfile.age > 0) "${targetProfile.age} વર્ષ" else null,
                        targetProfile.subCaste.takeIf { it.isNotBlank() },
                        targetProfile.gotra.takeIf { it.isNotBlank() }?.let { "ગોત્ર: $it" }
                    ).joinToString(" • ")

                    if (subInfo.isNotBlank()) {
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(text = subInfo, fontSize = 12.sp, color = Color.DarkGray)
                    }

                    if (targetProfile.currentCity.isNotBlank()) {
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(text = "શહેર: ${targetProfile.currentCity}", fontSize = 12.sp, color = Color.Gray)
                    }
                }

                // Status Badge
                StatusBadge(status = request.status, appLanguage = appLanguage)
            }

            Spacer(modifier = Modifier.height(12.dp))
            HorizontalDivider(color = Color(0xFFEEEEEE), thickness = 1.dp)
            Spacer(modifier = Modifier.height(10.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // If received & PENDING -> Show Accept & Decline Buttons
                if (isReceived && request.status == "PENDING") {
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        Button(
                            onClick = onAccept,
                            colors = ButtonDefaults.buttonColors(containerColor = VerifiedGreen),
                            shape = RoundedCornerShape(8.dp),
                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp)
                        ) {
                            Icon(Icons.Default.CheckCircle, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(if (appLanguage == "gu") "સ્વીકારો" else "Accept", fontSize = 12.sp)
                        }

                        OutlinedButton(
                            onClick = onReject,
                            shape = RoundedCornerShape(8.dp),
                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp)
                        ) {
                            Icon(Icons.Default.Close, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(if (appLanguage == "gu") "અસ્વીકાર" else "Decline", fontSize = 12.sp, color = Color.Red)
                        }
                    }
                } else {
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        OutlinedButton(
                            onClick = onViewDetail,
                            shape = RoundedCornerShape(8.dp),
                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp)
                        ) {
                            Text(if (appLanguage == "gu") "વિગત" else "View", fontSize = 12.sp)
                        }

                        OutlinedButton(
                            onClick = onRemoveOrBlock,
                            shape = RoundedCornerShape(8.dp),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.Red),
                            border = androidx.compose.foundation.BorderStroke(1.dp, Color.Red.copy(alpha = 0.5f)),
                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 6.dp)
                        ) {
                            Icon(Icons.Default.Block, contentDescription = null, modifier = Modifier.size(14.dp), tint = Color.Red)
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(if (appLanguage == "gu") "રદ/બ્લોક" else "Block", fontSize = 12.sp, color = Color.Red, fontWeight = FontWeight.Bold)
                        }
                    }
                }

                // Chat Action Button
                val isChatUnlocked = request.status == "ACCEPTED"
                Button(
                    onClick = onChatClick,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (isChatUnlocked) RoyalMaroon else Color.LightGray,
                        contentColor = if (isChatUnlocked) Color.White else Color.DarkGray
                    ),
                    shape = RoundedCornerShape(8.dp),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    Icon(
                        imageVector = if (isChatUnlocked) Icons.AutoMirrored.Filled.Chat else Icons.Default.Lock,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = if (isChatUnlocked) {
                            if (appLanguage == "gu") "ચેટ કરો" else "Chat Now"
                        } else {
                            if (appLanguage == "gu") "ચેટિંગ લોક" else "Chat Locked"
                        },
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

@Composable
fun StatusBadge(status: String, appLanguage: String) {
    val (bgColor, textColor, text) = when (status) {
        "ACCEPTED" -> Triple(
            Color(0xFFE8F5E9),
            VerifiedGreen,
            if (appLanguage == "gu") "સ્વીકારેલ" else "Accepted"
        )
        "REJECTED" -> Triple(
            Color(0xFFFFEBEE),
            Color.Red,
            if (appLanguage == "gu") "અસ્વીકૃત" else "Declined"
        )
        "BLOCKED" -> Triple(
            Color(0xFFFFEBEE),
            Color.Red,
            if (appLanguage == "gu") "અવરોધિત" else "Blocked"
        )
        else -> Triple(
            Color(0xFFFFF8E1),
            Color(0xFFE65100),
            if (appLanguage == "gu") "પેન્ડિંગ" else "Pending"
        )
    }

    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(12.dp))
            .background(bgColor)
            .padding(horizontal = 10.dp, vertical = 4.dp)
    ) {
        Text(
            text = text,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            color = textColor
        )
    }
}
