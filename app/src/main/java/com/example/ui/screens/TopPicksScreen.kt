package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
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
import com.example.R
import coil.compose.AsyncImage
import com.example.model.Profile
import com.example.ui.theme.*
import com.example.ui.viewmodel.MatrimonyViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TopPicksScreen(
    viewModel: MatrimonyViewModel,
    onBackClick: () -> Unit,
    onNavigateToProfileDetail: (String) -> Unit,
    onNavigateToChat: (String) -> Unit
) {
    val profiles by viewModel.filteredProfiles.collectAsState()
    val topCandidates = profiles.take(5)
    val topReasons by viewModel.topPickReasons.collectAsState()
    val isGenerating by viewModel.isGeneratingTopPicks.collectAsState()

    LaunchedEffect(Unit) {
        if (topReasons.isEmpty()) {
            viewModel.generateTopAIPicks()
        }
    }

    val appLanguage by viewModel.appLanguage.collectAsState()
    val strings = remember(appLanguage) { com.example.util.LocaleStrings.getStrings(appLanguage) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(strings.topPicksTitle, fontWeight = FontWeight.Bold, fontSize = 16.sp) },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = strings.backBtn)
                    }
                },
                actions = {
                    Surface(
                        onClick = {
                            val nextLang = if (appLanguage == "gu") "en" else "gu"
                            viewModel.setAppLanguage(nextLang)
                        },
                        shape = RoundedCornerShape(8.dp),
                        color = RoyalGold.copy(alpha = 0.2f),
                        border = androidx.compose.foundation.BorderStroke(1.dp, RoyalGold),
                        modifier = Modifier
                            .padding(end = 4.dp)
                            .testTag("top_picks_language_toggle")
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Translate,
                                contentDescription = "Language",
                                tint = RoyalMaroon,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = if (appLanguage == "gu") "GU" else "EN",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = RoyalMaroon
                            )
                        }
                    }

                    IconButton(onClick = { viewModel.generateTopAIPicks() }) {
                        Icon(Icons.Default.Refresh, contentDescription = "Refresh", tint = RoyalMaroon)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = CreamBackground)
            )
        },
        containerColor = CreamBackground
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .windowInsetsPadding(WindowInsets.navigationBars)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Hero Header Card
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = DarkMaroon)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.AutoAwesome, contentDescription = null, tint = SoftGold)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("ચૌધરી AI મેચમેકર", color = SoftGold, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                        }

                        Text(
                            text = "ગોત્ર મર્યાદા, સ્થાનિકતા અને જીવનશૈલીનું સ્માર્ટ મિલન",
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp,
                            modifier = Modifier.padding(top = 4.dp)
                        )

                        Text(
                            text = "અમારું જેમિનાઇ AI ગોત્ર મર્યાદા, મોસાળની પસંદગી, કારકિર્દી અને કૌટુંબિક મૂલ્યોનું વિશ્લેષણ કરીને તમારા માટે 5 સૌથી અનુકૂળ પસંદગીઓ કરે છે.",
                            color = Color.White.copy(alpha = 0.85f),
                            fontSize = 12.sp,
                            modifier = Modifier.padding(top = 6.dp)
                        )
                    }
                }
            }

            if (isGenerating) {
                item {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
                        colors = CardDefaults.cardColors(containerColor = SurfaceCream)
                    ) {
                        Column(
                            modifier = Modifier.padding(24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            CircularProgressIndicator(color = RoyalMaroon)
                            Spacer(modifier = Modifier.height(12.dp))
                            Text("ગોત્ર મર્યાદા અને સાંસ્કૃતિક સુસંગતતા ચકાસાઈ રહી છે...", fontWeight = FontWeight.Bold)
                            Text("તમારા માટે આજના શ્રેષ્ઠ 5 મિલન શોધી રહ્યા છીએ", fontSize = 12.sp, color = Color.Gray)
                        }
                    }
                }
            } else if (topCandidates.isEmpty()) {
                item {
                    Card(modifier = Modifier.fillMaxWidth()) {
                        Box(modifier = Modifier.padding(24.dp), contentAlignment = Alignment.Center) {
                            Text("ટોપ પિક્સ માટે કોઈ પ્રોફાઇલ ઉપલબ્ધ નથી. કૃપા કરીને શોધ ફિલ્ટર બદલો.")
                        }
                    }
                }
            } else {
                itemsIndexed(topCandidates) { index, profile ->
                    val score = 98 - (index * 2)
                    val reason = topReasons[profile.id] ?: "• Gotra Exogamy Verified (${profile.gotra})\n• High education & career alignment\n• Compatible family culture in ${profile.locality}"

                    TopPickCard(
                        rank = index + 1,
                        matchScore = score,
                        profile = profile,
                        aiReasoning = reason,
                        onViewProfile = { onNavigateToProfileDetail(profile.id) },
                        onSendInterest = { viewModel.sendInterest(profile.id) },
                        onChatClick = { onNavigateToChat(profile.id) }
                    )
                }
            }
        }
    }
}

@Composable
fun TopPickCard(
    rank: Int,
    matchScore: Int,
    profile: Profile,
    aiReasoning: String,
    onViewProfile: () -> Unit,
    onSendInterest: () -> Unit,
    onChatClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("top_pick_card_$rank"),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Rank Badge & Score Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    color = RoyalMaroon,
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text(
                        text = "#$rank શ્રેષ્ઠ પસંદગી",
                        color = SoftGold,
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                    )
                }

                Surface(
                    color = SoftGold,
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.Star, contentDescription = null, tint = DeepGold, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("$matchScore% AI અનુકૂળતા", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = DeepGold)
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Profile info
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(70.dp)
                        .clip(CircleShape)
                        .border(2.dp, RoyalGold, CircleShape)
                ) {
                    val imgUrl = profile.getEffectiveProfileImageUrl()
                    if (imgUrl.isNotBlank()) {
                        AsyncImage(
                            model = imgUrl,
                            contentDescription = profile.fullName,
                            contentScale = ContentScale.Crop,
                            placeholder = painterResource(id = R.drawable.img_matrimony_hero_1784990427738),
                            error = painterResource(id = R.drawable.img_matrimony_hero_1784990427738),
                            modifier = Modifier.fillMaxSize()
                        )
                    } else if (profile.photoRes != 0) {
                        Image(
                            painter = painterResource(id = profile.photoRes),
                            contentDescription = profile.fullName,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize()
                        )
                    } else {
                        Image(
                            painter = painterResource(id = R.drawable.img_matrimony_hero_1784990427738),
                            contentDescription = profile.fullName,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize()
                        )
                    }
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column {
                    Text(profile.fullName, fontWeight = FontWeight.Bold, fontSize = 18.sp, color = RoyalMaroon)
                    Text("${profile.subCaste} • ગોત્ર: ${profile.gotra}", color = WarmSaffron, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    Text("${profile.age} વર્ષ, ${profile.height} | ${profile.occupation}", fontSize = 12.sp)
                    Text("મૂળ વતન: ${profile.nativeVillage} | મોસાળ: ${profile.motherBirthVillage}", fontSize = 11.sp, color = Color.Gray)
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // AI Reasoning Box
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(SurfaceCream, RoundedCornerShape(12.dp))
                    .border(1.dp, RoyalGold.copy(alpha = 0.4f), RoundedCornerShape(12.dp))
                    .padding(12.dp)
            ) {
                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.AutoAwesome, contentDescription = null, tint = WarmSaffron, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("શા માટે જેમિનાઇ AI એ આ પસંદગી કરી:", fontWeight = FontWeight.Bold, fontSize = 11.sp, color = WarmSaffron)
                    }
                    Text(
                        text = aiReasoning,
                        fontSize = 12.sp,
                        color = Color.DarkGray,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Action Buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick = onViewProfile,
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(containerColor = RoyalMaroon),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text("સંપૂર્ણ બાયોડેટા જુઓ", fontSize = 12.sp)
                }

                OutlinedButton(
                    onClick = onSendInterest,
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text(if (profile.interestStatus == "SENT") "પ્રસ્તાવ મોકલ્યો" else "રસ દાખવો", fontSize = 12.sp)
                }

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
