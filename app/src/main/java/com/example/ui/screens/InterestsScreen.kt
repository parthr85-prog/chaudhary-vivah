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
import com.example.ui.theme.RoyalGold
import com.example.ui.theme.RoyalMaroon
import com.example.ui.theme.VerifiedGreen
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
    val strings = LocaleStrings.getStrings(appLanguage)
    val userInterests by viewModel.userInterests.collectAsState()
    val allProfiles by viewModel.allProfiles.collectAsState()
    val myProfile by viewModel.myProfile.collectAsState()
    val isRefreshing by viewModel.isRefreshing.collectAsState()

    var selectedTabIndex by remember { mutableStateOf(0) }
    var showChatLockDialog by remember { mutableStateOf<String?>(null) }
    var showBlockDialogForTarget by remember { mutableStateOf<Profile?>(null) }
    var showMatchCompleteDeleteDialog by remember { mutableStateOf(false) }
    var partnerNameInput by remember { mutableStateOf("") }
    val context = androidx.compose.ui.platform.LocalContext.current

    val authUid = com.example.service.FirebaseAuthService.currentUser?.uid ?: ""
    val pid = myProfile.id
    val myIds = remember(pid, authUid) {
        setOfNotNull(
            pid.takeIf { it.isNotBlank() },
            authUid.takeIf { it.isNotBlank() },
            "USER_ME"
        )
    }

    // Received Interests (others sent to me)
    val receivedInterests = userInterests.filter { req ->
        (myIds.contains(req.receiverId) || req.receiverId == "USER_ME") &&
        !myIds.contains(req.senderId)
    }
    // Sent Interests (I sent to others)
    val sentInterests = userInterests.filter { req ->
        myIds.contains(req.senderId) || req.senderId == "USER_ME"
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
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = Color.White)
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
            // Match Finalized & Delete Account Card Banner
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFFFF8E1)),
                border = androidx.compose.foundation.BorderStroke(1.dp, RoyalGold),
                shape = RoundedCornerShape(12.dp)
            ) {
                Row(
                    modifier = Modifier.padding(10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(Icons.Default.VolunteerActivism, contentDescription = null, tint = RoyalMaroon, modifier = Modifier.size(28.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            Text(
                                text = if (appLanguage == "gu") "🎉 લગ્ન / સગાઈ નક્કી થઈ ગઈ?" else "🎉 Match Completed?",
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp,
                                color = RoyalMaroon
                            )
                            Text(
                                text = if (appLanguage == "gu") "સગાઈ/લગ્ન નક્કી થવા પર એકાઉન્ટ ડિલીટ કરો" else "Delete profile on match finalization",
                                fontSize = 11.sp,
                                color = Color.DarkGray
                            )
                        }
                    }
                    Button(
                        onClick = { showMatchCompleteDeleteDialog = true },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFD32F2F)),
                        shape = RoundedCornerShape(8.dp),
                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp)
                    ) {
                        Icon(Icons.Default.DeleteForever, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = if (appLanguage == "gu") "એકાઉન્ટ રદ કરો" else "Delete Account",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            // Tab Header
            TabRow(
                selectedTabIndex = selectedTabIndex,
                containerColor = Color.White,
                contentColor = RoyalMaroon,
                indicator = { tabPositions ->
                    TabRowDefaults.Indicator(
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
                            Icon(Icons.Default.Send, contentDescription = null, modifier = Modifier.size(18.dp))
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

                        InterestCardItem(
                            request = item,
                            targetProfile = targetProfile,
                            isReceived = selectedTabIndex == 0,
                            appLanguage = appLanguage,
                            onAccept = { viewModel.acceptInterest(item.id) },
                            onReject = { viewModel.rejectInterest(item.id) },
                            onRemoveOrBlock = { showBlockDialogForTarget = targetProfile },
                            onViewDetail = { onNavigateToProfileDetail(targetProfile.id) },
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

    if (showMatchCompleteDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showMatchCompleteDeleteDialog = false },
            icon = {
                Icon(Icons.Default.VolunteerActivism, contentDescription = null, tint = RoyalMaroon, modifier = Modifier.size(40.dp))
            },
            title = {
                Text(
                    text = if (appLanguage == "gu") "સગાઈ/લગ્ન નક્કી થવા પર એકાઉન્ટ ડિલીટ કરો" else "Delete Account - Match Completed",
                    fontWeight = FontWeight.Bold,
                    fontSize = 17.sp,
                    color = RoyalMaroon
                )
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = if (appLanguage == "gu")
                            "અભિનંદન! જો તમારું સગાઈ કે લગ્ન નક્કી થઈ ગયું હોય, તો તમારું એકાઉન્ટ ડિલીટ કરવાથી તમારી પ્રોફાઇલ, બાયોડેટા અને ફોટા ચૌધરી મેટ્રિમોની પોર્ટલ પરથી કાયમી ધોરણે દૂર થઈ જશે જેથી તમને વધુ પ્રસ્તાવો ન મળે."
                        else
                            "Congratulations on finding your match! Deleting your account will permanently remove your profile, bio-data, and photos from the portal so you no longer receive proposals.",
                        fontSize = 13.sp,
                        color = Color.DarkGray
                    )

                    OutlinedTextField(
                        value = partnerNameInput,
                        onValueChange = { partnerNameInput = it },
                        label = { Text(if (appLanguage == "gu") "જીવનસાથીનું નામ (ઓપ્શનલ)" else "Partner Name (Optional)") },
                        placeholder = { Text("જેમ કે: રમેશભાઈ ચૌધરી") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        showMatchCompleteDeleteDialog = false
                        viewModel.deleteAccountOnMatchComplete(
                            partnerName = partnerNameInput,
                            onSuccess = {
                                android.widget.Toast.makeText(
                                    context,
                                    if (appLanguage == "gu") "હાર્દિક વાધાઈ! તમારું એકાઉન્ટ સફળતાપૂર્વક ડિલીટ કરાયું છે. સુખી દામ્પત્ય જીવનની શુભકામનાઓ!" else "Congratulations! Your account has been deleted. Wish you a happy married life!",
                                    android.widget.Toast.LENGTH_LONG
                                ).show()
                            },
                            onError = { err ->
                                android.widget.Toast.makeText(context, err, android.widget.Toast.LENGTH_SHORT).show()
                            }
                        )
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Red)
                ) {
                    Icon(Icons.Default.DeleteForever, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(if (appLanguage == "gu") "હા, એકાઉન્ટ ડિલીટ કરો" else "Yes, Delete Account", color = Color.White, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                OutlinedButton(onClick = { showMatchCompleteDeleteDialog = false }) {
                    Text(if (appLanguage == "gu") "રદ કરો" else "Cancel")
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
            Divider(color = Color(0xFFEEEEEE), thickness = 1.dp)
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
                        imageVector = if (isChatUnlocked) Icons.Default.Chat else Icons.Default.Lock,
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
