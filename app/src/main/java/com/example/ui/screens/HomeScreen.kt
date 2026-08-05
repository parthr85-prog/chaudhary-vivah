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
import androidx.compose.material.icons.automirrored.filled.Logout
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
    onNavigateToKundli: (Profile) -> Unit,
    onNavigateToChat: (String) -> Unit,
    onNavigateToTopPicks: () -> Unit,
    onOpenFilterSheet: () -> Unit,
    onNavigateToProfileSetup: () -> Unit = {},
    onNavigateToAdmin: () -> Unit = {},
    onLogout: () -> Unit = {}
) {
    val searchGender by viewModel.searchGender.collectAsState()
    val profiles by viewModel.filteredProfiles.collectAsState()
    val isRefreshing by viewModel.isRefreshing.collectAsState()
    val selectedSubCaste by viewModel.selectedSubCaste.collectAsState()
    val selectedLocality by viewModel.selectedLocality.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()
    val onlyAadharVerified by viewModel.onlyAadharVerified.collectAsState()
    val myProfile by viewModel.myProfile.collectAsState()
    val top5AIPicks by viewModel.top5AIPicks.collectAsState()
    val pendingProfiles by viewModel.pendingProfiles.collectAsState()
    val isAdmin by viewModel.isAdmin.collectAsState()

    val appLanguage by viewModel.appLanguage.collectAsState()
    val strings = remember(appLanguage) { com.example.util.LocaleStrings.getStrings(appLanguage) }

    val notifications by viewModel.notifications.collectAsState()
    val unreadNotificationCount by viewModel.unreadNotificationCount.collectAsState()
    var showNotificationsSheet by remember { mutableStateOf(false) }
    var showMatchCompleteDeleteDialog by remember { mutableStateOf(false) }
    var showPolicyDialog by remember { mutableStateOf(false) }
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

                    if (isAdmin || myProfile.phoneContact.equals("srushtichaudhary11@gmail.com", ignoreCase = true)) {
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
                            onClick = onNavigateToTopPicks,
                            modifier = Modifier.testTag("top_picks_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.AutoAwesome,
                                contentDescription = "Top 5 AI Picks",
                                tint = WarmSaffron
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

            // Quick Match Complete & Account Delete Banner Card
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { showMatchCompleteDeleteDialog = true },
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFFFF8E1)),
                    border = androidx.compose.foundation.BorderStroke(1.dp, RoyalGold),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(Icons.Default.Celebration, contentDescription = null, tint = RoyalMaroon, modifier = Modifier.size(26.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Column {
                                Text(
                                    text = if (appLanguage == "gu") "🎉 સગાઈ/લગ્ન નક્કી થઈ ગયા? (Match Complete)" else "🎉 Match Completed?",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp,
                                    color = RoyalMaroon
                                )
                                Text(
                                    text = if (appLanguage == "gu") "અહીં ક્લિક કરી તમારું એકાઉન્ટ અને બાયોડેટા ડિલીટ કરો." else "Click here to delete profile after match finalization.",
                                    fontSize = 11.sp,
                                    color = Color.DarkGray
                                )
                            }
                        }
                        Text(
                            text = if (appLanguage == "gu") "ડિલીટ કરો" else "Delete",
                            color = Color.Red,
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp,
                            modifier = Modifier.padding(start = 4.dp)
                        )
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
                                text = "સચ્ચા પારિવારિક સંસ્કારો અને ગોત્ર મર્યાદા સાથે સુયોગ્ય જીવનસાથી શોધો.",
                                fontSize = 12.sp,
                                color = DarkMaroon,
                                lineHeight = 16.sp,
                                modifier = Modifier.padding(top = 3.dp)
                            )
                        }
                    }
                }
            }

            // Top 5 AI Picks Section with Matching Algorithm Cards
            if (!isAdmin) {
                item {
                    Column(modifier = Modifier.fillMaxWidth()) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    Icons.Default.AutoAwesome,
                                    contentDescription = null,
                                    tint = WarmSaffron,
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "તમારા માટે શ્રેષ્ઠ 5 AI પિક્સ",
                                    style = MaterialTheme.typography.titleMedium.copy(
                                        fontWeight = FontWeight.Bold,
                                        color = RoyalMaroon
                                    )
                                )
                            }

                            TextButton(onClick = onNavigateToTopPicks) {
                                Text("બધા જુઓ", color = RoyalMaroon, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                            }
                        }

                        if (top5AIPicks.isEmpty()) {
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(100.dp),
                                shape = RoundedCornerShape(16.dp),
                                colors = CardDefaults.cardColors(containerColor = SoftGold)
                            ) {
                                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                                    Text("ટોપ 5 AI પસંદગીઓનું વિશ્લેષણ થઈ રહ્યું છે...", fontSize = 12.sp, color = RoyalMaroon)
                                }
                            }
                        } else {
                            LazyRow(
                                horizontalArrangement = Arrangement.spacedBy(12.dp),
                                contentPadding = PaddingValues(vertical = 4.dp)
                            ) {
                                items(top5AIPicks, key = { it.profile.id }) { pick ->
                                    Card(
                                        modifier = Modifier
                                            .width(260.dp)
                                            .clickable { onNavigateToProfileDetail(pick.profile.id) }
                                            .testTag("ai_pick_card_${pick.profile.id}"),
                                        shape = RoundedCornerShape(18.dp),
                                        colors = CardDefaults.cardColors(containerColor = SurfaceCream),
                                        border = androidx.compose.foundation.BorderStroke(1.5.dp, RoyalGold),
                                        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                                    ) {
                                        Column(
                                            modifier = Modifier.padding(12.dp),
                                            verticalArrangement = Arrangement.spacedBy(8.dp)
                                        ) {
                                            // Top Row: Match Percentage Badge & Verified Icon
                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                horizontalArrangement = Arrangement.SpaceBetween,
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Surface(
                                                    color = RoyalMaroon,
                                                    shape = RoundedCornerShape(8.dp)
                                                ) {
                                                    Row(
                                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                                        verticalAlignment = Alignment.CenterVertically
                                                    ) {
                                                        Icon(
                                                            Icons.Default.AutoAwesome,
                                                            contentDescription = null,
                                                            tint = SoftGold,
                                                            modifier = Modifier.size(14.dp)
                                                        )
                                                        Spacer(modifier = Modifier.width(4.dp))
                                                        Text(
                                                            text = "${pick.matchPercentage}% મિલન",
                                                            color = SoftGold,
                                                            fontWeight = FontWeight.Bold,
                                                            fontSize = 12.sp
                                                        )
                                                    }
                                                }

                                                if (pick.profile.isAadharVerified) {
                                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                                        Icon(
                                                            Icons.Default.Verified,
                                                            contentDescription = "Verified",
                                                            tint = VerifiedGreen,
                                                            modifier = Modifier.size(16.dp)
                                                        )
                                                        Spacer(modifier = Modifier.width(2.dp))
                                                        Text("પ્રમાણિત", fontSize = 10.sp, color = VerifiedGreen, fontWeight = FontWeight.Bold)
                                                    }
                                                }
                                            }

                                            // Candidate Summary Info with Profile Avatar
                                            Row(
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Box(
                                                    modifier = Modifier
                                                        .size(44.dp)
                                                        .clip(CircleShape)
                                                        .border(1.5.dp, RoyalGold, CircleShape)
                                                ) {
                                                    val pickImgUrl = pick.profile.getEffectiveProfileImageUrl()
                                                    if (pickImgUrl.isNotBlank()) {
                                                        AsyncImage(
                                                            model = pickImgUrl,
                                                            contentDescription = pick.profile.fullName,
                                                            contentScale = ContentScale.Crop,
                                                            placeholder = androidx.compose.ui.res.painterResource(id = R.drawable.img_matrimony_hero_1784990427738),
                                                            error = androidx.compose.ui.res.painterResource(id = R.drawable.img_matrimony_hero_1784990427738),
                                                            modifier = Modifier.fillMaxSize()
                                                        )
                                                    } else if (pick.profile.photoRes != 0) {
                                                        androidx.compose.foundation.Image(
                                                            painter = androidx.compose.ui.res.painterResource(id = pick.profile.photoRes),
                                                            contentDescription = pick.profile.fullName,
                                                            contentScale = ContentScale.Crop,
                                                            modifier = Modifier.fillMaxSize()
                                                        )
                                                    } else {
                                                        AsyncImage(
                                                            model = R.drawable.img_matrimony_hero_1784990427738,
                                                            contentDescription = pick.profile.fullName,
                                                            contentScale = ContentScale.Crop,
                                                            modifier = Modifier.fillMaxSize()
                                                        )
                                                    }
                                                }

                                                Spacer(modifier = Modifier.width(10.dp))

                                                Column {
                                                    Text(
                                                        text = "${pick.profile.fullName}, ${pick.profile.age}",
                                                        fontWeight = FontWeight.Bold,
                                                        fontSize = 15.sp,
                                                        color = RoyalMaroon
                                                    )

                                                    Text(
                                                        text = "${pick.profile.subCaste} • ગોત્ર: ${pick.profile.gotra}",
                                                        fontSize = 12.sp,
                                                        color = DarkMaroon,
                                                        fontWeight = FontWeight.SemiBold
                                                    )
                                                }
                                            }

                                            Text(
                                                text = "📍 ${pick.profile.currentCity} (${pick.profile.locality})",
                                                fontSize = 11.sp,
                                                color = Color.Gray
                                            )

                                            // Match Reasons Chips
                                            Row(
                                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                                            ) {
                                                pick.matchReasons.take(2).forEach { reason ->
                                                    Surface(
                                                        color = SoftGold.copy(alpha = 0.8f),
                                                        shape = RoundedCornerShape(6.dp)
                                                    ) {
                                                        Text(
                                                            text = reason,
                                                            fontSize = 10.sp,
                                                            fontWeight = FontWeight.Medium,
                                                            color = RoyalMaroon,
                                                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                                        )
                                                    }
                                                }
                                            }

                                            // Action Button Row
                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                                            ) {
                                                OutlinedButton(
                                                    onClick = { onNavigateToKundli(pick.profile) },
                                                    modifier = Modifier
                                                        .weight(1f)
                                                        .height(34.dp),
                                                    contentPadding = PaddingValues(2.dp),
                                                    shape = RoundedCornerShape(8.dp),
                                                    border = androidx.compose.foundation.BorderStroke(1.dp, RoyalMaroon)
                                                ) {
                                                    Text("Kundli", fontSize = 11.sp, color = RoyalMaroon, fontWeight = FontWeight.Bold)
                                                }

                                                Button(
                                                    onClick = { onNavigateToProfileDetail(pick.profile.id) },
                                                    modifier = Modifier
                                                        .weight(1f)
                                                        .height(34.dp),
                                                    contentPadding = PaddingValues(2.dp),
                                                    shape = RoundedCornerShape(8.dp),
                                                    colors = ButtonDefaults.buttonColors(containerColor = RoyalMaroon)
                                                ) {
                                                    Text("View", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                                }
                                            }
                                        }
                                    }
                                }
                            }
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
                    ProfileCard(
                        profile = profile,
                        isAdmin = isAdmin,
                        onCardClick = { onNavigateToProfileDetail(profile.id) },
                        onSendInterest = { viewModel.sendInterest(profile.id) },
                        onToggleShortlist = { viewModel.toggleShortlist(profile.id, profile.isShortlisted) },
                        onKundliClick = { onNavigateToKundli(profile) },
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
        AlertDialog(
            onDismissRequest = { showMatchCompleteDeleteDialog = false },
            icon = {
                Icon(Icons.Default.Celebration, contentDescription = null, tint = RoyalMaroon, modifier = Modifier.size(40.dp))
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
                            "હાર્દિક અભિનંદન! જો તમારું સગાઈ કે લગ્ન નક્કી થઈ ગયું હોય, તો તમારું એકાઉન્ટ ડિલીટ કરવાથી તમારી પ્રોફાઇલ, બાયોડેટા અને તમામ ફોટા ચૌધરી મેટ્રિમોની પોર્ટલ પરથી કાયમી ધોરણે રદ થઈ જશે જેથી અન્ય સભ્યો તમને કોલ કે રિક્વેસ્ટ ન મોકલે."
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
                                    if (appLanguage == "gu") "અભિનંદન! તમારું એકાઉન્ટ સફળતાપૂર્વક ડિલીટ કરાયું છે. સુખી દામ્પત્ય જીવનની શુભકામનાઓ!" else "Congratulations! Your account has been deleted. Wish you a happy married life!",
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
                "Chaudhary Vivah Privacy Policy:\n\n1. Information Collection: We collect bio-data, Gothra details, contact info, and optional Aadhar ID strictly for matrimonial verification within Chaudhary community.\n\n2. Data Usage: Used solely for Gothra exogamy validation, 36-Guna Kundli matching, and profile discovery.\n\n3. Match Complete Deletion: You can permanently delete your profile, bio-data, and photos with 1-click once your match is finalized.\n\n4. Contact Support: srushtichaudhary11@gmail.com"),
            Triple(if (appLanguage == "gu") "રિફંડ પોલિસી" else "Refund Policy", Icons.Default.Payments,
                "Chaudhary Vivah Refund & Cancellation Policy:\n\n1. 7-Day Refund Window: Full refund for unutilized paid subscriptions before viewing contact details or profile rejection.\n\n2. Non-Refundable: Once contact phone numbers are unlocked or after match completion account deletion.\n\n3. Processing Time: Approved refunds credited back within 5 to 7 business days to original payment method.\n\n4. Contact Support: srushtichaudhary11@gmail.com"),
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
}

@Composable
fun ProfileCard(
    profile: Profile,
    isAdmin: Boolean = false,
    onCardClick: () -> Unit,
    onSendInterest: () -> Unit,
    onToggleShortlist: () -> Unit,
    onKundliClick: () -> Unit,
    onChatClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onCardClick() }
            .testTag("profile_card_${profile.id}"),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                verticalAlignment = Alignment.Top
            ) {
                // Profile Avatar with Cultural Border
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
                        Text(
                            text = profile.fullName,
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = RoyalMaroon
                            ),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.weight(1f)
                        )

                        IconButton(
                            onClick = onToggleShortlist,
                            modifier = Modifier.size(32.dp)
                        ) {
                            Icon(
                                imageVector = if (profile.isShortlisted) Icons.Default.Favorite else Icons.Outlined.FavoriteBorder,
                                contentDescription = "Shortlist",
                                tint = if (profile.isShortlisted) Color.Red else Color.Gray
                            )
                        }
                    }

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "${profile.age} વર્ષ, ${profile.height}",
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 13.sp
                        )

                        if (profile.isAadharVerified) {
                            Spacer(modifier = Modifier.width(8.dp))
                            Surface(
                                color = VerifiedGreenContainer,
                                shape = RoundedCornerShape(6.dp)
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Verified,
                                        contentDescription = null,
                                        tint = VerifiedGreen,
                                        modifier = Modifier.size(12.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("આધાર પ્રમાણિત", fontSize = 10.sp, color = VerifiedGreen, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }

                    // Sub-caste & Gotra Tag
                    Text(
                        text = "${profile.subCaste} • ગોત્ર: ${profile.gotra}",
                        fontSize = 12.sp,
                        color = WarmSaffron,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(top = 2.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))
            HorizontalDivider(color = Color.LightGray.copy(alpha = 0.4f))
            Spacer(modifier = Modifier.height(10.dp))

            // Cultural Key Details Grid
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(text = "મૂળ વતન (ગામ):", fontSize = 10.sp, color = Color.Gray)
                    Text(
                        text = profile.nativeVillage,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                Column(modifier = Modifier.weight(1f)) {
                    Text(text = "મોસાળ (માતાનું વતન):", fontSize = 10.sp, color = Color.Gray)
                    Text(
                        text = profile.motherBirthVillage,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = RoyalMaroon,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(text = "વ્યવસાય અને આવક:", fontSize = 10.sp, color = Color.Gray)
                    Text(
                        text = "${profile.occupation} (${profile.monthlyIncome})",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                Column(modifier = Modifier.weight(0.8f)) {
                    Text(text = "હાલનું સ્થળ:", fontSize = 10.sp, color = Color.Gray)
                    Text(
                        text = profile.currentCity,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
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
                if (!isAdmin) {
                    // Swaagat (Send Interest) Button
                    Button(
                        onClick = onSendInterest,
                        modifier = Modifier.weight(1.2f),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (profile.interestStatus == "SENT") VerifiedGreen else RoyalMaroon
                        ),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Icon(
                            imageVector = if (profile.interestStatus == "SENT") Icons.Default.CheckCircle else Icons.Default.Send,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = if (profile.interestStatus == "SENT") "પ્રસ્તાવ મોકલ્યો" else "રસ દાખવો (સ્વાગત)",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    // AI Kundli Button
                    OutlinedButton(
                        onClick = onKundliClick,
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = WarmSaffron),
                        border = androidx.compose.foundation.BorderStroke(1.dp, WarmSaffron)
                    ) {
                        Icon(Icons.Default.AutoAwesome, contentDescription = null, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("કુંડળી", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                } else {
                    // View Profile Button for Admin
                    Button(
                        onClick = onCardClick,
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(containerColor = RoyalMaroon),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Icon(Icons.Default.Visibility, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("પ્રોફાઇલ જુઓ", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                }

                // Chat Button
                IconButton(
                    onClick = onChatClick,
                    modifier = Modifier
                        .size(40.dp)
                        .background(SoftGold, RoundedCornerShape(10.dp))
                ) {
                    Icon(Icons.Default.Chat, contentDescription = "Chat", tint = RoyalMaroon)
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
        "CHAT" -> Triple(Icons.Default.Chat, WarmSaffron, SoftGold)
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
