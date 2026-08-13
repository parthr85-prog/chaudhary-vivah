package com.example.ui.screens

import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.*
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import coil.compose.AsyncImage
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.data.SampleData
import com.example.model.AppNotification
import com.example.model.Profile
import com.example.ui.theme.*
import com.example.ui.viewmodel.MatrimonyViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    viewModel: MatrimonyViewModel,
    onNavigateToProfileDetail: (String) -> Unit,
    onNavigateToChat: (String) -> Unit,
    onOpenFilterSheet: () -> Unit,
    onNavigateToProfileSetup: () -> Unit = {},
    onNavigateToShortlist: () -> Unit = {},
    onNavigateToAdmin: () -> Unit = {},
    onNavigateToSubscription: () -> Unit = {},
    onLogout: () -> Unit = {}
) {
    val searchGender by viewModel.searchGender.collectAsState()
    val profiles by viewModel.filteredProfiles.collectAsState()
    val shortlistedProfiles by viewModel.shortlistedProfiles.collectAsState()
    val isRefreshing by viewModel.isRefreshing.collectAsState()
    val selectedSubCaste by viewModel.selectedSubCaste.collectAsState()
    val selectedLocality by viewModel.selectedLocality.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()
    val onlyAadharVerified by viewModel.onlyAadharVerified.collectAsState()
    val myProfile by viewModel.myProfile.collectAsState()
    val pendingProfiles by viewModel.pendingProfiles.collectAsState()
    val isAdmin by viewModel.isAdmin.collectAsState()
    val freeSchemeClaimedCount by viewModel.freeSchemeClaimedCount.collectAsState()

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
    val strings = remember(appLanguage) { com.example.util.LocaleStrings.getStrings(appLanguage) }

    val notifications by viewModel.notifications.collectAsState()
    val unreadNotificationCount by viewModel.unreadNotificationCount.collectAsState()
    var showNotificationsSheet by remember { mutableStateOf(false) }
    var showMatchCompleteDeleteDialog by remember { mutableStateOf(false) }
    var showPolicyDialog by remember { mutableStateOf(false) }
    var showLockedBioDataForProfile by remember { mutableStateOf<Profile?>(null) }
    var partnerNameInput by remember { mutableStateOf("") }

    val context = LocalContext.current
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        val permissionLauncher = rememberLauncherForActivityResult(
            contract = ActivityResultContracts.RequestPermission()
        ) { }
        LaunchedEffect(Unit) {
            if (context.checkSelfPermission(android.Manifest.permission.POST_NOTIFICATIONS) != android.content.pm.PackageManager.PERMISSION_GRANTED) {
                permissionLauncher.launch(android.Manifest.permission.POST_NOTIFICATIONS)
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        AsyncImage(
                            model = R.drawable.img_app_icon_fg_1784990412652,
                            contentDescription = "Logo",
                            modifier = Modifier
                                .size(38.dp)
                                .clip(CircleShape)
                                .background(RoyalGold)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = strings.appTitle,
                                fontWeight = FontWeight.Bold,
                                fontSize = 18.sp,
                                color = RoyalMaroon
                            )
                            Text(
                                text = strings.arbudaBlessing,
                                fontSize = 10.sp,
                                color = Color.Gray
                            )
                        }
                    }
                },
                actions = {
                    // Notification Icon with badge
                    BadgedBox(
                        badge = {
                            if (unreadNotificationCount > 0) {
                                Badge(
                                    containerColor = RoyalMaroon,
                                    contentColor = Color.White
                                ) {
                                    Text(
                                        text = if (unreadNotificationCount > 99) "99+" else "$unreadNotificationCount",
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }
                    ) {
                        IconButton(
                            onClick = {
                                viewModel.markAllNotificationsAsRead()
                                showNotificationsSheet = true
                            },
                            modifier = Modifier.testTag("notifications_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Notifications,
                                contentDescription = "Notifications",
                                tint = RoyalMaroon
                            )
                        }
                    }

                    if (isAdmin || myProfile.phoneContact == "9724327777" || myProfile.phoneContact.equals("srushtichaudhary11@gmail.com", ignoreCase = true)) {
                        BadgedBox(
                            badge = {
                                if (pendingProfiles.isNotEmpty()) {
                                    Badge(containerColor = RoyalMaroon) {
                                        Text("${pendingProfiles.size}", color = Color.White, fontSize = 10.sp)
                                    }
                                }
                            }
                        ) {
                            IconButton(
                                onClick = onNavigateToAdmin,
                                modifier = Modifier.testTag("admin_panel_button")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.AdminPanelSettings,
                                    contentDescription = "Admin Approval Panel",
                                    tint = RoyalMaroon
                                )
                            }
                        }
                    }

                    if (!isAdmin) {
                        IconButton(
                            onClick = onNavigateToShortlist,
                            modifier = Modifier.testTag("shortlist_top_button")
                        ) {
                            BadgedBox(
                                badge = {
                                    if (shortlistedProfiles.isNotEmpty()) {
                                        Badge(containerColor = Color.Red, contentColor = Color.White) {
                                            Text("${shortlistedProfiles.size}", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                        }
                                    }
                                }
                            ) {
                                Icon(
                                    imageVector = if (shortlistedProfiles.isNotEmpty()) Icons.Default.Favorite else Icons.Outlined.FavoriteBorder,
                                    contentDescription = "Shortlisted Profiles",
                                    tint = if (shortlistedProfiles.isNotEmpty()) Color.Red else RoyalMaroon
                                )
                            }
                        }
                    }

                    if (!isAdmin) {
                        IconButton(
                            onClick = onNavigateToSubscription,
                            modifier = Modifier.testTag("vip_subscription_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.WorkspacePremium,
                                contentDescription = "VIP Subscription",
                                tint = RoyalGold
                            )
                        }
                    }

                    IconButton(
                        onClick = { showPolicyDialog = true },
                        modifier = Modifier.testTag("policy_info_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.PrivacyTip,
                            contentDescription = "Legal Policies & Website",
                            tint = RoyalMaroon
                        )
                    }

                    IconButton(
                        onClick = onLogout,
                        modifier = Modifier.testTag("logout_button")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.Logout,
                            contentDescription = "Logout",
                            tint = RoyalMaroon
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = CreamBackground
                )
            )
        },
        containerColor = CreamBackground
    ) { paddingValues ->
        PullToRefreshBox(
            isRefreshing = isRefreshing,
            onRefresh = { viewModel.refreshDashboard() },
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .windowInsetsPadding(WindowInsets.navigationBars)
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
            // Rejection / Approval Warning Banner
            if (myProfile.isRejected) {
                item {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 4.dp),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFFFFEBEE)),
                        border = androidx.compose.foundation.BorderStroke(1.dp, Color.Red),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Error, contentDescription = null, tint = Color.Red, modifier = Modifier.size(24.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "તમારી પ્રોફાઇલ અસ્વીકાર કરવામાં આવી છે",
                                    fontWeight = FontWeight.Bold,
                                    color = Color.Red,
                                    fontSize = 14.sp
                                )
                            }
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = "અસ્વીકારનું કારણ: ${myProfile.rejectionReason.ifBlank { "માહિતી અપૂર્ણ છે." }}",
                                fontSize = 13.sp,
                                color = Color.DarkGray
                            )
                            Spacer(modifier = Modifier.height(10.dp))
                            Button(
                                onClick = onNavigateToProfileSetup,
                                colors = ButtonDefaults.buttonColors(containerColor = RoyalMaroon),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Text("પ્રોફાઇલ સુધારો અને ફરી મોકલો", fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            } else if (!myProfile.isApproved && !isAdmin && myProfile.fullName.isNotBlank()) {
                item {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 4.dp),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFFFFF8E1)),
                        border = androidx.compose.foundation.BorderStroke(1.dp, WarmSaffron),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Default.HourglassTop, contentDescription = null, tint = WarmSaffron, modifier = Modifier.size(24.dp))
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(
                                text = "તમારી પ્રોફાઇલ ચકાસણી હેઠળ છે. એડમિન મંજૂરી બાદ અન્ય સભ્યો જોઈ શકશે.",
                                fontSize = 12.sp,
                                color = Color.DarkGray
                            )
                        }
                    }
                }
            }

            // VIP Subscription Expired Banner or First 100 Free Scheme Banner
            if (!myProfile.isVipSubscribed && myProfile.subscriptionExpiryDate.isNotBlank()) {
                item {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onNavigateToSubscription() }
                            .testTag("expired_subscription_banner"),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFFFFEBEE)),
                        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFC62828)),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Default.Warning, contentDescription = null, tint = Color(0xFFC62828), modifier = Modifier.size(24.dp))
                            Spacer(modifier = Modifier.width(10.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = if (appLanguage == "gu") "⚠️ VIP સબ્સ્ક્રિપ્શન સમાપ્ત થઈ ગયું છે!" else "⚠️ VIP Membership Expired!",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp,
                                    color = Color(0xFFC62828)
                                )
                                Text(
                                    text = if (appLanguage == "gu") "સમાપ્તિ તારીખ: ${myProfile.subscriptionExpiryDate}. ૩ મહિના માટે ₹૫૯૦ માં સરળતાથી નવીનીકરણ કરવા અહીં ક્લિક કરો." else "Expired on ${myProfile.subscriptionExpiryDate}. Click to easily renew for 3 months (₹590).",
                                    fontSize = 11.5.sp,
                                    color = Color.DarkGray
                                )
                            }
                            Spacer(modifier = Modifier.width(6.dp))
                            Icon(Icons.Default.ChevronRight, contentDescription = null, tint = Color(0xFFC62828))
                        }
                    }
                }
            } else if (!myProfile.isVipSubscribed && !myProfile.isFreeSchemeUsed && freeSchemeClaimedCount < 100) {
                item {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onNavigateToSubscription() }
                            .testTag("free_100_scheme_banner"),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFFFFF3E0)),
                        border = androidx.compose.foundation.BorderStroke(1.5.dp, WarmSaffron),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Default.CardGiftcard, contentDescription = null, tint = RoyalMaroon, modifier = Modifier.size(26.dp))
                            Spacer(modifier = Modifier.width(10.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = if (appLanguage == "gu") "🎁 પ્રથમ ૧૦૦ સભ્યો માટે ૩ મહિના મફત VIP!" else "🎁 First 100 Members 3 Months Free VIP!",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp,
                                    color = DarkMaroon
                                )
                                Text(
                                    text = if (appLanguage == "gu") "ક્લેમ થયેલ: $freeSchemeClaimedCount/૧૦૦ • હમણાં જ ફ્રી સબ્સ્ક્રિપ્શન મેળવવા અહીં ક્લિક કરો." else "Claimed: $freeSchemeClaimedCount/100 • Click to claim free 3-month VIP now.",
                                    fontSize = 11.5.sp,
                                    color = Color.DarkGray
                                )
                            }
                            Spacer(modifier = Modifier.width(6.dp))
                            Icon(Icons.Default.ChevronRight, contentDescription = null, tint = RoyalMaroon)
                        }
                    }
                }
            }

            // Search Bar & Filter Action
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = { viewModel.searchQuery.value = it },
                        placeholder = { Text("ગોત્ર, મૂળ વતન, શહેર અથવા વ્યવસાય શોધો...") },
                        leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = RoyalMaroon) },
                        trailingIcon = {
                            if (searchQuery.isNotEmpty()) {
                                IconButton(onClick = { viewModel.searchQuery.value = "" }) {
                                    Icon(Icons.Default.Clear, contentDescription = "Clear")
                                }
                            }
                        },
                        singleLine = true,
                        shape = RoundedCornerShape(16.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = RoyalMaroon,
                            unfocusedBorderColor = RoyalGold.copy(alpha = 0.5f)
                        ),
                        modifier = Modifier
                            .weight(1f)
                            .testTag("search_bar_input")
                    )

                    IconButton(
                        onClick = onOpenFilterSheet,
                        modifier = Modifier
                            .size(52.dp)
                            .background(RoyalGold, RoundedCornerShape(16.dp))
                            .testTag("open_filters_button")
                    ) {
                        Icon(Icons.Default.FilterList, contentDescription = "Filters", tint = Color.White)
                    }
                }
            }

            // Quick Filter Chips (Sub-caste & Region)
            item {
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(SampleData.subCastesList) { sub ->
                        FilterChip(
                            selected = selectedSubCaste == sub,
                            onClick = { viewModel.selectedSubCaste.value = sub },
                            label = { Text(sub, fontSize = 12.sp) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = RoyalMaroon,
                                selectedLabelColor = Color.White
                            )
                        )
                    }
                }
            }

            // Maa Arbuda Devotional Blessing Header Banner
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("maa_arbuda_home_header"),
                    shape = RoundedCornerShape(22.dp),
                    colors = CardDefaults.cardColors(containerColor = SurfaceCream),
                    border = androidx.compose.foundation.BorderStroke(1.5.dp, RoyalGold),
                    elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(
                                Brush.horizontalGradient(
                                    colors = listOf(
                                        SoftGold,
                                        SurfaceCream,
                                        LightRoseContainer
                                    )
                                )
                            )
                            .padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(72.dp)
                                .clip(RoundedCornerShape(16.dp))
                                .border(2.dp, RoyalGold, RoundedCornerShape(16.dp))
                        ) {
                            AsyncImage(
                                model = R.drawable.img_maa_arbuda_1785298366627,
                                contentDescription = "Maa Arbuda Devi Blessing",
                                contentScale = ContentScale.Crop,
                                modifier = Modifier.fillMaxSize()
                            )
                        }

                        Spacer(modifier = Modifier.width(14.dp))

                        Column(modifier = Modifier.weight(1f)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = "જય મા અર્બુદા દેવી",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 16.sp,
                                    color = RoyalMaroon
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Surface(
                                    color = RoyalGold.copy(alpha = 0.2f),
                                    shape = RoundedCornerShape(6.dp)
                                ) {
                                    Text(
                                        text = "કુલદેવી આશીર્વાદ",
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = RoyalMaroon,
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                    )
                                }
                            }

                            Text(
                                text = "સાચા પારિવારિક સંસ્કારો અને ગોત્ર મર્યાદા સાથે સુયોગ્ય જીવનસાથી શોધો.",
                                fontSize = 12.sp,
                                color = DarkMaroon,
                                lineHeight = 16.sp,
                                modifier = Modifier.padding(top = 3.dp)
                            )
                        }
                    }
                }
            }

            // Results Heading
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    val displayGenderTitle = when {
                        isAdmin -> "બધી પ્રોફાઇલ્સ (All Profiles)"
                        viewModel.isMaleGender(myProfile.gender) || searchGender.contains("Bride", ignoreCase = true) -> "કન્યાઓ (Brides)"
                        viewModel.isFemaleGender(myProfile.gender) || searchGender.contains("Groom", ignoreCase = true) -> "વરરાજાઓ (Grooms)"
                        else -> "પ્રોફાઇલ્સ (Profiles)"
                    }
                    Text(
                        text = "પ્રમાણિત ચૌધરી $displayGenderTitle (${profiles.size})",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = RoyalMaroon
                        )
                    )

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .clickable { viewModel.onlyAadharVerified.value = !onlyAadharVerified }
                            .padding(4.dp)
                    ) {
                        Checkbox(
                            checked = onlyAadharVerified,
                            onCheckedChange = { viewModel.onlyAadharVerified.value = it },
                            colors = CheckboxDefaults.colors(checkedColor = VerifiedGreen)
                        )
                        Text("Aadhar Only", fontSize = 12.sp, color = VerifiedGreen, fontWeight = FontWeight.SemiBold)
                    }
                }
            }

            if (profiles.isEmpty()) {
                item {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 32.dp),
                        colors = CardDefaults.cardColors(containerColor = SurfaceCream)
                    ) {
                        Column(
                            modifier = Modifier.padding(24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(Icons.Default.SearchOff, contentDescription = null, tint = RoyalMaroon, modifier = Modifier.size(48.dp))
                            Spacer(modifier = Modifier.height(12.dp))
                            Text("No Matched Chaudhary Profiles", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                            Text("Try adjusting your sub-caste, gotra exclusion or locality filters.", fontSize = 13.sp, color = Color.Gray)
                        }
                    }
                }
            } else {
                items(profiles, key = { it.id }) { profile ->
                    val isAccepted = viewModel.isInterestAccepted(profile.id)
                    val isOwnProfile = profile.id == myProfile.id
                    ProfileCard(
                        profile = profile,
                        isAdmin = isAdmin,
                        onCardClick = {
                            if (isAdmin || isOwnProfile || isAccepted) {
                                onNavigateToProfileDetail(profile.id)
                            } else {
                                showLockedBioDataForProfile = profile
                            }
                        },
                        onSendInterest = { viewModel.sendInterest(profile.id) },
                        onToggleShortlist = { viewModel.toggleShortlist(profile.id, profile.isShortlisted) },
                        onChatClick = { onNavigateToChat(profile.id) }
                    )
                }
            }

            item {
                Spacer(modifier = Modifier.height(32.dp))
            }
        }
    }
}

    if (showNotificationsSheet) {
        NotificationsBottomSheet(
            notifications = notifications,
            onDismiss = { showNotificationsSheet = false },
            onNotificationClick = { notification ->
                showNotificationsSheet = false
                viewModel.markNotificationAsRead(notification.id)
                when (notification.type) {
                    "CHAT" -> {
                        if (notification.targetId.isNotBlank()) {
                            onNavigateToChat(notification.targetId)
                        }
                    }
                    "INTEREST" -> {
                        if (notification.targetId.isNotBlank()) {
                            onNavigateToProfileDetail(notification.targetId)
                        }
                    }
                    "LOGIN_ALERT" -> {
                        onNavigateToProfileSetup()
                    }
                }
            },
            onClearAll = {
                viewModel.clearAllNotifications()
            }
        )
    }

    if (showMatchCompleteDeleteDialog) {
        var selectedOption by remember { mutableStateOf(0) } // 0: Match Found, 1: Other Reason
        var partnerNameInput by remember { mutableStateOf("") }
        var otherReasonInput by remember { mutableStateOf("") }

        AlertDialog(
            onDismissRequest = { showMatchCompleteDeleteDialog = false },
            icon = {
                Icon(Icons.Default.Celebration, contentDescription = null, tint = RoyalMaroon, modifier = Modifier.size(40.dp))
            },
            title = {
                Text(
                    text = if (appLanguage == "gu") "એકાઉન્ટ ડિલીટ કરો (Delete Account)" else "Delete Account",
                    fontWeight = FontWeight.Bold,
                    fontSize = 17.sp,
                    color = RoyalMaroon
                )
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = if (appLanguage == "gu")
                            "એકાઉન્ટ ડિલીટ કરવા માટેનું કારણ પસંદ કરો:"
                        else
                            "Select reason for deleting your account:",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = RoyalMaroon
                    )

                    // Option 1: Match Found
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .clickable { selectedOption = 0 }
                            .padding(vertical = 2.dp)
                    ) {
                        RadioButton(
                            selected = (selectedOption == 0),
                            onClick = { selectedOption = 0 },
                            colors = RadioButtonDefaults.colors(selectedColor = RoyalMaroon)
                        )
                        Text(
                            text = if (appLanguage == "gu") "1) સગાઈ/લગ્ન નક્કી થઈ ગયા (Match Found)" else "1) Match Found",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Medium,
                            color = Color.DarkGray
                        )
                    }

                    if (selectedOption == 0) {
                        OutlinedTextField(
                            value = partnerNameInput,
                            onValueChange = { partnerNameInput = it },
                            label = { Text(if (appLanguage == "gu") "જીવનસાથીનું નામ (ઓપ્શનલ)" else "Partner Name (Optional)") },
                            placeholder = { Text("જેમ કે: રમેશભાઈ ચૌધરી") },
                            singleLine = true,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(start = 12.dp)
                        )
                    }

                    // Option 2: Other Reason
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .clickable { selectedOption = 1 }
                            .padding(vertical = 2.dp)
                    ) {
                        RadioButton(
                            selected = (selectedOption == 1),
                            onClick = { selectedOption = 1 },
                            colors = RadioButtonDefaults.colors(selectedColor = RoyalMaroon)
                        )
                        Text(
                            text = if (appLanguage == "gu") "2) અન્ય કારણ (Other Reason)" else "2) Other Reason",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Medium,
                            color = Color.DarkGray
                        )
                    }

                    if (selectedOption == 1) {
                        OutlinedTextField(
                            value = otherReasonInput,
                            onValueChange = { otherReasonInput = it },
                            label = { Text(if (appLanguage == "gu") "ડિલીટ કરવાનું કારણ (ઓપ્શનલ)" else "Reason for Deletion (Optional)") },
                            placeholder = { Text(if (appLanguage == "gu") "જેમ કે: સમય અભાવ / અન્ય" else "e.g. Other reason") },
                            singleLine = true,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(start = 12.dp)
                        )
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        showMatchCompleteDeleteDialog = false
                        val finalPartnerName = if (selectedOption == 0) partnerNameInput.trim() else ""
                        val finalReason = if (selectedOption == 0) "Match Found / Got Married" else otherReasonInput.ifBlank { "Other Reason" }
                        viewModel.deleteAccountOnMatchComplete(
                            partnerName = finalPartnerName,
                            reason = finalReason,
                            onSuccess = {
                                android.widget.Toast.makeText(
                                    context,
                                    if (appLanguage == "gu") "તમારું એકાઉન્ટ સફળતાપૂર્વક ડિલીટ કરાયું છે." else "Your account has been deleted successfully.",
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

    if (showPolicyDialog) {
        var selectedTab by remember { mutableStateOf(0) }
        val policies = listOf(
            Triple(if (appLanguage == "gu") "પ્રાઇવસી પોલિસી" else "Privacy Policy", Icons.Default.Security,
                "Chaudhary Vivah Privacy Policy:\n\n1. Information Collection: We collect bio-data, Gothra details, contact info, and optional Aadhar ID strictly for matrimonial verification within Chaudhary community.\n\n2. Data Usage: Used solely for Gothra exogamy validation, 36-Guna Kundli matching, and profile discovery.\n\n3. Match Complete Deletion: You can permanently delete your profile, bio-data, and photos with 1-click once your match is finalized.\n\n4. Contact Support: info@chaudharyvivah.in | 9016607483"),
            Triple(if (appLanguage == "gu") "રિફંડ પોલિસી" else "Refund Policy", Icons.Default.Payments,
                "Chaudhary Vivah Refund & Cancellation Policy:\n\n1. 7-Day Refund Window: Full refund for unutilized paid subscriptions before viewing contact details or profile rejection.\n\n2. Non-Refundable: Once contact phone numbers are unlocked or after match completion account deletion.\n\n3. Processing Time: Approved refunds credited back within 5 to 7 business days to original payment method.\n\n4. Contact Support: info@chaudharyvivah.in | 9016607483"),
            Triple(if (appLanguage == "gu") "નિયમો અને શરતો" else "Terms & Conditions", Icons.Default.Gavel,
                "Chaudhary Vivah Terms & Conditions:\n\n1. Legal Marriageable Age: Minimum 18 years for females and 21 years for males under Indian Marriage Act.\n\n2. Verified Chaudhary Profiles: Exclusive platform for Chaudhary community families.\n\n3. Authenticity: Submitting false bio-data or fake documents is prohibited.\n\n4. Admin Verification: All profiles undergo manual admin approval before public discovery."),
            Triple(if (appLanguage == "gu") "ડિસ્ક્લેમર" else "Disclaimer", Icons.Default.Info,
                "Chaudhary Vivah Disclaimer:\n\n1. Independent Check: Platform acts as matchmaking introductory portal. Families are advised to perform independent background checks.\n\n2. Kundli Milan: 36-Guna Kundli scores are for reference based on traditional astrological calculations.\n\n3. No Guaranteed Marriage Outcome: Platform facilitates introductions but does not guarantee matrimonial outcomes.")
        )

        AlertDialog(
            onDismissRequest = { showPolicyDialog = false },
            icon = {
                Icon(Icons.Default.PrivacyTip, contentDescription = null, tint = RoyalMaroon, modifier = Modifier.size(36.dp))
            },
            title = {
                Text(
                    text = if (appLanguage == "gu") "ચૌધરી વિવાહ પોલિસી અને વેબસાઇટ" else "Legal Policies & Web Portal",
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    color = RoyalMaroon
                )
            },
            text = {
                Column(modifier = Modifier.fillMaxWidth()) {
                    ScrollableTabRow(
                        selectedTabIndex = selectedTab,
                        edgePadding = 0.dp,
                        containerColor = Color.Transparent,
                        contentColor = RoyalMaroon
                    ) {
                        policies.forEachIndexed { index, item ->
                            Tab(
                                selected = selectedTab == index,
                                onClick = { selectedTab = index },
                                text = { Text(item.first, fontSize = 11.sp, fontWeight = if (selectedTab == index) FontWeight.Bold else FontWeight.Normal) }
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Card(
                        colors = CardDefaults.cardColors(containerColor = LightRoseContainer),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(policies[selectedTab].second, contentDescription = null, tint = RoyalMaroon, modifier = Modifier.size(20.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = policies[selectedTab].first,
                                    fontWeight = FontWeight.Bold,
                                    color = RoyalMaroon,
                                    fontSize = 13.sp
                                )
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = policies[selectedTab].third,
                                fontSize = 11.5.sp,
                                color = Color.DarkGray,
                                lineHeight = 16.sp
                            )
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = { showPolicyDialog = false },
                    colors = ButtonDefaults.buttonColors(containerColor = RoyalMaroon)
                ) {
                    Text(if (appLanguage == "gu") "બંધ કરો" else "Close", color = Color.White)
                }
            }
        )
    }

    showLockedBioDataForProfile?.let { targetProfile ->
        val isInterestSent = targetProfile.interestStatus == "SENT" || targetProfile.interestStatus == "ACCEPTED"
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
                    text = "સંપૂર્ણ બાયોડેટા લોક છે (Bio-Data Locked)",
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
                        text = "ચૌધરી વિવાહ પર ગોપનીયતા અને મર્યાદા જાળવવા માટે, ${targetProfile.fullName} નો સંપૂર્ણ બાયોડેટા અને વિગતો ફક્ત ત્યારે જ જોઈ શકાશે જ્યારે સામેના સભ્ય તમારો 'રસ/પ્રસ્તાવ (Interest Request)' સ્વીકારશે.",
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
                                text = "✓ તમે આ સભ્યને લગ્ન પ્રસ્તાવ મોકલેલ છે. સામે પક્ષ દ્વારા સ્વીકાર થયા પછી સંપૂર્ણ પ્રોફાઇલ અનલોક થશે.",
                                fontSize = 12.sp,
                                color = VerifiedGreen,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(10.dp),
                                textAlign = androidx.compose.ui.text.style.TextAlign.Center
                            )
                        }
                    } else {
                        Text(
                            text = "કૃપા કરીને નીચે આપેલ બટન પર ક્લિક કરી પ્રથમ રસ દાખવો/પ્રસ્તાવ મોકલો.",
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
                        Text("રસ દાખવો (Send Interest)", fontWeight = FontWeight.Bold, color = Color.White)
                    }
                } else {
                    Button(
                        onClick = { showLockedBioDataForProfile = null },
                        colors = ButtonDefaults.buttonColors(containerColor = RoyalMaroon)
                    ) {
                        Text("સમજાયું (OK)", fontWeight = FontWeight.Bold, color = Color.White)
                    }
                }
            },
            dismissButton = {
                TextButton(onClick = { showLockedBioDataForProfile = null }) {
                    Text("બંધ કરો (Close)", color = Color.Gray)
                }
            }
        )
    }
}

@Composable
fun ProfileCard(
    profile: Profile,
    isAdmin: Boolean = false,
    onCardClick: () -> Unit,
    onSendInterest: () -> Unit,
    onToggleShortlist: () -> Unit,
    onChatClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onCardClick() }
            .testTag("profile_card_${profile.id}"),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = androidx.compose.foundation.BorderStroke(1.dp, BorderLightGold),
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                verticalAlignment = Alignment.Top
            ) {
                // 1. Profile Avatar / Photo
                Box(
                    modifier = Modifier
                        .size(80.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .border(2.dp, RoyalGold, RoundedCornerShape(16.dp))
                ) {
                    val cardImgUrl = profile.getEffectiveProfileImageUrl()
                    if (cardImgUrl.isNotBlank()) {
                        AsyncImage(
                            model = cardImgUrl,
                            contentDescription = profile.fullName,
                            contentScale = ContentScale.Crop,
                            placeholder = androidx.compose.ui.res.painterResource(id = R.drawable.img_matrimony_hero_1784990427738),
                            error = androidx.compose.ui.res.painterResource(id = R.drawable.img_matrimony_hero_1784990427738),
                            modifier = Modifier.fillMaxSize()
                        )
                    } else if (profile.photoRes != 0) {
                        androidx.compose.foundation.Image(
                            painter = androidx.compose.ui.res.painterResource(id = profile.photoRes),
                            contentDescription = profile.fullName,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize()
                        )
                    } else {
                        AsyncImage(
                            model = R.drawable.img_matrimony_hero_1784990427738,
                            contentDescription = profile.fullName,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize()
                        )
                    }
                }

                Spacer(modifier = Modifier.width(14.dp))

                Column(
                    modifier = Modifier.weight(1f)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        // 2. Name
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = profile.fullName.ifBlank { "Unregistered User" },
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = RoyalMaroon
                                ),
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            if (profile.profileId.isNotBlank()) {
                                Text(
                                    text = "ID: ${profile.profileId}",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = RoyalMaroon.copy(alpha = 0.8f)
                                )
                            }
                        }

                        if (!isAdmin) {
                            IconButton(
                                onClick = onToggleShortlist,
                                modifier = Modifier
                                    .size(40.dp)
                                    .testTag("shortlist_button_${profile.id}")
                            ) {
                                Icon(
                                    imageVector = if (profile.isShortlisted) Icons.Default.Favorite else Icons.Outlined.FavoriteBorder,
                                    contentDescription = "Shortlist",
                                    tint = if (profile.isShortlisted) Color.Red else Color.Gray,
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    // 3. Age
                    Text(
                        text = "ઉંમર: ${if (profile.age > 0) "${profile.age} વર્ષ" else "દર્શાવેલ નથી"}",
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 13.sp,
                        color = Color.DarkGray
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))
            HorizontalDivider(color = Color.LightGray.copy(alpha = 0.4f))
            Spacer(modifier = Modifier.height(10.dp))

            // 4. Native Village (મૂળ વતન) & 6. Current City
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(text = "મૂળ વતન (ગામ):", fontSize = 10.sp, color = Color.Gray)
                    Text(
                        text = profile.nativeVillage.ifBlank { "દર્શાવેલ નથી" },
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color.Black,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                Column(modifier = Modifier.weight(1f)) {
                    Text(text = "હાલનું શહેર:", fontSize = 10.sp, color = Color.Gray)
                    Text(
                        text = profile.currentCity.ifBlank { "દર્શાવેલ નથી" },
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = RoyalMaroon,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // 5. Business / Occupation
            Row(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.fillMaxWidth()) {
                    Text(text = "વ્યવસાય / નોકરી:", fontSize = 10.sp, color = Color.Gray)
                    Text(
                        text = profile.occupation.ifBlank { "દર્શાવેલ નથી" },
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color.Black,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Action Buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // View Full Profile Button
                Button(
                    onClick = onCardClick,
                    modifier = Modifier.weight(1.2f),
                    colors = ButtonDefaults.buttonColors(containerColor = RoyalMaroon),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Icon(Icons.Default.Visibility, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "સંપૂર્ણ પ્રોફાઇલ જુઓ",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                if (!isAdmin) {
                    // Send Interest Button
                    OutlinedButton(
                        onClick = onSendInterest,
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = if (profile.interestStatus == "SENT") VerifiedGreen else RoyalMaroon
                        ),
                        border = androidx.compose.foundation.BorderStroke(
                            1.dp,
                            if (profile.interestStatus == "SENT") VerifiedGreen else RoyalMaroon
                        ),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Icon(
                            imageVector = if (profile.interestStatus == "SENT") Icons.Default.CheckCircle else Icons.AutoMirrored.Filled.Send,
                            contentDescription = null,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(3.dp))
                        Text(
                            text = if (profile.interestStatus == "SENT") "મોકલેલ" else "રસ દાખવો",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                // Chat Button
                IconButton(
                    onClick = onChatClick,
                    modifier = Modifier
                        .size(40.dp)
                        .background(SoftGold, RoundedCornerShape(10.dp))
                ) {
                    Icon(Icons.AutoMirrored.Filled.Chat, contentDescription = "Chat", tint = RoyalMaroon)
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotificationsBottomSheet(
    notifications: List<AppNotification>,
    onDismiss: () -> Unit,
    onNotificationClick: (AppNotification) -> Unit,
    onClearAll: () -> Unit
) {
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = CreamBackground,
        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
                .padding(bottom = 32.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Default.Notifications,
                        contentDescription = null,
                        tint = RoyalMaroon,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "સૂચનાઓ (Notifications)",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = RoyalMaroon
                    )
                }

                if (notifications.isNotEmpty()) {
                    TextButton(onClick = onClearAll) {
                        Text("બધું ભૂંસી નાખો", color = RoyalMaroon, fontSize = 12.sp)
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            if (notifications.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 40.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            Icons.Default.NotificationsNone,
                            contentDescription = null,
                            tint = Color.Gray,
                            modifier = Modifier.size(48.dp)
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = "કોઈ નવી સૂચના નથી (No Notifications)",
                            fontSize = 14.sp,
                            color = Color.Gray
                        )
                    }
                }
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.fillMaxWidth().heightIn(max = 450.dp)
                ) {
                    items(notifications, key = { it.id }) { item ->
                        NotificationItemRow(
                            notification = item,
                            onClick = { onNotificationClick(item) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun NotificationItemRow(
    notification: AppNotification,
    onClick: () -> Unit
) {
    val (icon, iconTint, bgGradient) = when (notification.type) {
        "CHAT" -> Triple(Icons.AutoMirrored.Filled.Chat, WarmSaffron, SoftGold)
        "INTEREST" -> Triple(Icons.Default.Favorite, RoyalMaroon, LightRoseContainer)
        "LOGIN_ALERT" -> Triple(Icons.Default.Devices, DarkMaroon, SoftGold)
        else -> Triple(Icons.Default.Info, RoyalMaroon, SurfaceCream)
    }

    val timeStr = remember(notification.timestamp) {
        val sdf = SimpleDateFormat("dd MMM, hh:mm a", Locale.getDefault())
        sdf.format(Date(notification.timestamp))
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (!notification.isRead) SurfaceCream else MaterialTheme.colorScheme.surface
        ),
        border = if (!notification.isRead) androidx.compose.foundation.BorderStroke(1.dp, RoyalGold) else null,
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(bgGradient),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = iconTint,
                    modifier = Modifier.size(20.dp)
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = notification.title,
                    fontWeight = if (!notification.isRead) FontWeight.Bold else FontWeight.SemiBold,
                    fontSize = 14.sp,
                    color = RoyalMaroon
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = notification.message,
                    fontSize = 12.sp,
                    color = Color.DarkGray,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = timeStr,
                    fontSize = 10.sp,
                    color = Color.Gray
                )
            }

            if (!notification.isRead) {
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .clip(CircleShape)
                        .background(RoyalMaroon)
                )
            }
        }
    }
}
