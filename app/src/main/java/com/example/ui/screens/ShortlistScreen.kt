package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.example.R
import com.example.model.Profile
import com.example.ui.theme.*
import com.example.ui.viewmodel.MatrimonyViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ShortlistScreen(
    viewModel: MatrimonyViewModel,
    onBackClick: () -> Unit = {},
    onNavigateToProfileDetail: (String) -> Unit,
    onNavigateToChat: (String) -> Unit
) {
    val shortlistedProfiles by viewModel.shortlistedProfiles.collectAsStateWithLifecycle()
    val myProfile by viewModel.myProfile.collectAsStateWithLifecycle()
    val isAdmin by viewModel.isAdmin.collectAsStateWithLifecycle()
    val appLanguage by viewModel.appLanguage.collectAsStateWithLifecycle()

    var showLockedBioDataForProfile by remember { mutableStateOf<Profile?>(null) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = if (appLanguage == "gu") "પસંદ કરેલ પ્રોફાઇલ્સ" else "Shortlisted Profiles",
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp,
                            color = RoyalMaroon
                        )
                        Text(
                            text = if (appLanguage == "gu") "કુલ ${shortlistedProfiles.size} પ્રોફાઇલ્સ" else "${shortlistedProfiles.size} Saved Profiles",
                            fontSize = 12.sp,
                            color = Color.Gray
                        )
                    }
                },
                navigationIcon = {
                    IconButton(
                        onClick = onBackClick,
                        modifier = Modifier.testTag("shortlist_back_button")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = RoyalMaroon
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = CreamBackground)
            )
        },
        containerColor = CreamBackground
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            if (shortlistedProfiles.isEmpty()) {
                // Empty State
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(32.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Box(
                        modifier = Modifier
                            .size(90.dp)
                            .clip(CircleShape)
                            .background(LightRoseContainer),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.FavoriteBorder,
                            contentDescription = null,
                            tint = RoyalMaroon,
                            modifier = Modifier.size(48.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    Text(
                        text = if (appLanguage == "gu") "કોઈ પ્રોફાઇલ પસંદ કરેલ નથી" else "No Shortlisted Profiles Yet",
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.Bold,
                            color = RoyalMaroon
                        ),
                        textAlign = TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = if (appLanguage == "gu")
                            "તમે હજુ સુધી કોઈ પણ પ્રોફાઇલ પસંદ કરેલ નથી. હોમ સ્ક્રીન પર આપેલ હૃદય (❤️) આઇકન પર ક્લિક કરી તમારી પસંદગીની પ્રોફાઇલ્સ ઉમેરો."
                        else
                            "You haven't shortlisted any profiles yet. Tap the heart (❤️) icon on any profile card on the Home screen to save profiles here.",
                        fontSize = 14.sp,
                        color = Color.Gray,
                        textAlign = TextAlign.Center,
                        lineHeight = 20.sp
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 16.dp),
                    contentPadding = PaddingValues(vertical = 12.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    items(
                        items = shortlistedProfiles,
                        key = { it.id.ifBlank { "shortlist_prof_${it.hashCode()}" } }
                    ) { profile ->
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
            }
        }
    }

    // Locked Bio-Data Modal for Shortlisted Profiles
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
                    textAlign = TextAlign.Center
                )
            },
            text = {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "ચૌધરી વિવાહ પર ગોપનીયતા અને મર્યાદા જાળવવા માટે, ${targetProfile.fullName} નો સંપૂર્ણ બાયોડેટા અને સંચાલન વિગતો ફક્ત ત્યારે જ જોઈ શકાશે જ્યારે સામેના સભ્ય તમારો 'રસ/પ્રસ્તાવ (Interest Request)' સ્વીકારશે.",
                        fontSize = 13.sp,
                        color = Color.DarkGray,
                        textAlign = TextAlign.Center,
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
                                textAlign = TextAlign.Center
                            )
                        }
                    } else {
                        Text(
                            text = "કૃપા કરીને નીચે આપેલ બટન પર ક્લિક કરી પ્રથમ રસ દાખવો/પ્રસ્તાવ મોકલો.",
                            fontSize = 12.sp,
                            color = RoyalMaroon,
                            fontWeight = FontWeight.SemiBold,
                            textAlign = TextAlign.Center
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
