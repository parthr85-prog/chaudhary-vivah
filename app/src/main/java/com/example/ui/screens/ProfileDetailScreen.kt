package com.example.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.*
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
fun ProfileDetailScreen(
    profileId: String,
    viewModel: MatrimonyViewModel,
    onBackClick: () -> Unit,
    onChatClick: (String) -> Unit
) {
    val profiles by viewModel.allProfiles.collectAsState()
    val profile = profiles.find { it.id == profileId }

    if (profile == null) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("પ્રોફાઇલ મળી નથી")
        }
        return
    }

    val myProfile by viewModel.myProfile.collectAsState()
    val isAdmin by viewModel.isAdmin.collectAsState()
    val isInterestAccepted = viewModel.isInterestAccepted(profile.id)
    val isBlocked = viewModel.isUserBlocked(profile.id)
    val isOwnProfile = profile.id == myProfile.id
    val canViewFull = isAdmin || isOwnProfile || isInterestAccepted

    if (!canViewFull) {
        val isInterestSent = profile.interestStatus == "SENT" || profile.interestStatus == "ACCEPTED"
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text("સંપૂર્ણ બાયોડેટા લોક છે", fontWeight = FontWeight.Bold) },
                    navigationIcon = {
                        IconButton(onClick = onBackClick) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "પાછા જાઓ")
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
                    .padding(24.dp),
                contentAlignment = Alignment.Center
            ) {
                Card(
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = androidx.compose.foundation.BorderStroke(1.dp, BorderLightGold),
                    elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(70.dp)
                                .clip(CircleShape)
                                .background(LightRoseContainer),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Lock,
                                contentDescription = null,
                                tint = RoyalMaroon,
                                modifier = Modifier.size(36.dp)
                            )
                        }

                        Text(
                            text = "સંપૂર્ણ બાયોડેટા લોક છે\n(Bio-Data Locked)",
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp,
                            color = RoyalMaroon,
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                        )

                        Text(
                            text = "ચૌધરી વિવાહ પર ગોપનીયતા અને મર્યાદા જાળવવા માટે, ${profile.fullName} નો સંપૂર્ણ બાયોડેટા અને સંચાલન વિગતો ફક્ત ત્યારે જ જોઈ શકાશે જ્યારે સામેના સભ્ય તમારો 'રસ/પ્રસ્તાવ (Interest Request)' સ્વીકારશે.",
                            fontSize = 13.5.sp,
                            color = Color.DarkGray,
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                            lineHeight = 20.sp
                        )

                        if (isInterestSent) {
                            Surface(
                                color = VerifiedGreenContainer,
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text(
                                    text = "✓ તમે આ સભ્યને લગ્ન પ્રસ્તાવ મોકલેલ છે. સામે પક્ષ દ્વારા સ્વીકાર થયા પછી સંપૂર્ણ પ્રોફાઇલ અનલોક થશે.",
                                    fontSize = 12.5.sp,
                                    color = VerifiedGreen,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(12.dp),
                                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                                )
                            }
                        } else {
                            Button(
                                onClick = { viewModel.sendInterest(profile.id) },
                                colors = ButtonDefaults.buttonColors(containerColor = RoyalMaroon),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Icon(Icons.AutoMirrored.Filled.Send, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("રસ દાખવો (Send Interest)", fontWeight = FontWeight.Bold, color = Color.White)
                            }
                        }

                        OutlinedButton(
                            onClick = onBackClick,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("પાછા જાઓ (Go Back)")
                        }
                    }
                }
            }
        }
        return
    }

    var showContactModal by remember { mutableStateOf(false) }
    var showBlockConfirmDialog by remember { mutableStateOf(false) }
    val context = androidx.compose.ui.platform.LocalContext.current

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("ચૌધરી મિલન બાયોડેટા", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "પાછા જાઓ")
                    }
                },
                actions = {
                    if (!isAdmin) {
                        IconButton(onClick = { viewModel.toggleShortlist(profile.id, profile.isShortlisted) }) {
                            Icon(
                                imageVector = if (profile.isShortlisted) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                                contentDescription = "પસંદ કરો",
                                tint = if (profile.isShortlisted) Color.Red else Color.Gray
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = CreamBackground)
            )
        },
        containerColor = CreamBackground
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .windowInsetsPadding(WindowInsets.navigationBars)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Header Image & Badge Card
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("profile_detail_header"),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = androidx.compose.foundation.BorderStroke(1.dp, BorderLightGold),
                elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(200.dp)
                            .clip(RoundedCornerShape(16.dp))
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

                        if (profile.isAadharVerified) {
                            Surface(
                                color = VerifiedGreen,
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier
                                    .align(Alignment.TopEnd)
                                    .padding(12.dp)
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(Icons.Default.Verified, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("આધાર પ્રમાણિત", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        text = profile.fullName,
                        style = MaterialTheme.typography.headlineMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = RoyalMaroon
                        )
                    )

                    if (profile.profileId.isNotBlank()) {
                        Surface(
                            color = RoyalGold.copy(alpha = 0.15f),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.padding(top = 4.dp, bottom = 4.dp)
                        ) {
                            Text(
                                text = "પ્રોફાઇલ ID: ${profile.profileId}",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = RoyalMaroon,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                            )
                        }
                    }

                    Text(
                        text = "${profile.subCaste} • શાખ: ${profile.gotra}",
                        style = MaterialTheme.typography.titleMedium.copy(
                            color = WarmSaffron,
                            fontWeight = FontWeight.Bold
                        )
                    )

                    Text(
                        text = "${profile.age} વર્ષ, ${profile.height} | ${profile.maritalStatus}",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color.Gray
                    )

                    // Profile Completeness Bar
                    val profileFields = listOf(
                        profile.fullName.isNotBlank(),
                        profile.phoneContact.isNotBlank(),
                        profile.gotra.isNotBlank(),
                        profile.motherGotra.isNotBlank(),
                        profile.nativeVillage.isNotBlank(),
                        profile.motherBirthVillage.isNotBlank(),
                        profile.education.isNotBlank(),
                        profile.occupation.isNotBlank(),
                        profile.monthlyIncome.isNotBlank(),
                        profile.familyDetails.isNotBlank(),
                        profile.aboutMe.isNotBlank(),
                        profile.rashi.isNotBlank()
                    )
                    val compPct = (profileFields.count { it } * 100) / profileFields.size

                    Spacer(modifier = Modifier.height(12.dp))

                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(SurfaceCream, RoundedCornerShape(12.dp))
                            .border(1.dp, BorderLightGold, RoundedCornerShape(12.dp))
                            .padding(10.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.TaskAlt, contentDescription = null, tint = RoyalMaroon, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "પ્રોફાઇલ પૂર્ણતા ટકાવારી",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = RoyalMaroon
                                )
                            }
                            Text(
                                text = "$compPct%",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (compPct >= 80) VerifiedGreen else WarmSaffron
                            )
                        }

                        LinearProgressIndicator(
                            progress = { (compPct / 100f).coerceIn(0f, 1f) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(6.dp)
                                .clip(RoundedCornerShape(4.dp)),
                            color = if (compPct >= 80) VerifiedGreen else WarmSaffron,
                            trackColor = Color.LightGray.copy(alpha = 0.3f)
                        )
                    }
                }
            }

            // Personal Details & Health Section
            DetailSectionCard(
                title = "વૈયક્તિક માહિતી અને સ્થિતિ (Personal Details)",
                icon = Icons.Default.Person
            ) {
                DetailRow("બ્લડ ગ્રુપ (Blood Group):", profile.bloodGroup.ifBlank { "A+" })
                DetailRow(
                    "NRI ઉમેદવાર સ્થિતિ:",
                    if (profile.isNri) {
                        if (profile.nriCountry.isNotBlank()) "હા - NRI ઉમેદવાર (${profile.nriCountry})" else "હા - NRI ઉમેદવાર (Foreign Resident)"
                    } else "ના - ભારતમાં નિવાસી (Resident)",
                    isHighlight = profile.isNri
                )
                DetailRow(
                    "અગાઉનો વૈવાહિક ઇતિહાસ:",
                    if (profile.hasMaritalHistory) "હા - અગાઉ લગ્ન થયેલ છે" else "ના - પ્રથમ વાર લગ્ન (Never Married)"
                )
            }

            // Community & Exogamy Gotra Info Section
            DetailSectionCard(
                title = "સમાજ અને શાખ વિગતો (શાખ, ગોળ અને ગામ)",
                icon = Icons.Default.Groups
            ) {
                DetailRow("સબ-કાસ્ટ (શાખા):", profile.subCaste)
                if (profile.gol.isNotBlank()) DetailRow("ગોળ (Gol):", profile.gol, isHighlight = true)
                DetailRow("પોતાની શાખ (સ્વયંની શાખ):", profile.gotra, isHighlight = true)
                DetailRow("માતાની શાખ (માતાની શાખ):", profile.motherGotra, isHighlight = true)
                DetailRow("મૂળ ગામ (પિતૃ વતન):", profile.nativeVillage)
                DetailRow("મોસાળ (માતાનું જન્મ વતન):", profile.motherBirthVillage, isHighlight = true)
                DetailRow("સાંસ્કૃતિક પ્રદેશ / રાજ્ય:", profile.locality)
            }

            // Education & Career Section
            DetailSectionCard(
                title = "શિક્ષણ, વ્યવસાય અને શોખ",
                icon = Icons.Default.Work
            ) {
                DetailRow("ઉચ્ચતમ શિક્ષણ:", profile.education)
                DetailRow("વ્યવસાય / નોકરી:", profile.occupation)
                DetailRow("માસિક આવક:", profile.monthlyIncome)
                if (profile.hobbies.isNotBlank()) DetailRow("હોબીઝ / શોખ (Hobbies):", profile.hobbies)
                DetailRow("હાલનું શહેર:", profile.currentCity)
            }

            // Family & Background Section
            DetailSectionCard(
                title = "કૌટુંબિક પૃષ્ઠભૂમિ અને સભ્યો (પરિવાર)",
                icon = Icons.Default.Home
            ) {
                if (profile.fatherName.isNotBlank()) DetailRow("પિતાનું નામ:", profile.fatherName)
                if (profile.fatherOccupation.isNotBlank()) DetailRow("પિતાનો વ્યવસાય:", profile.fatherOccupation)
                if (profile.grandfatherName.isNotBlank()) DetailRow("દાદાનું નામ:", profile.grandfatherName)
                if (profile.motherName.isNotBlank()) DetailRow("માતાનું નામ:", profile.motherName)
                if (profile.motherOccupation.isNotBlank()) DetailRow("માતાનો વ્યવસાય:", profile.motherOccupation)
                if (profile.numBrothers > 0 || profile.brothersNames.isNotBlank()) {
                    DetailRow("ભાઈઓની વિગત:", "${profile.numBrothers} ભાઈ ${if (profile.brothersNames.isNotBlank()) "(${profile.brothersNames})" else ""}")
                }
                if (profile.numSisters > 0 || profile.sistersNames.isNotBlank()) {
                    DetailRow("બહેનોની વિગત:", "${profile.numSisters} બહેન ${if (profile.sistersNames.isNotBlank()) "(${profile.sistersNames})" else ""}")
                }
                val parentPhoneText = if (isInterestAccepted) {
                    if (profile.parentPhoneContact.isNotBlank()) profile.parentPhoneContact else if (profile.phoneContact.isNotBlank()) profile.phoneContact else "—"
                } else {
                    "🔒 વાલીનો નંબર પસંદગી સ્વીકાર્યા બાદ જ જોવા મળશે"
                }
                DetailRow("વાલીનો મોબાઈલ નંબર:", parentPhoneText, isHighlight = isInterestAccepted)
                DetailRow("કૌટુંબિક વિગત:", profile.familyDetails)
                DetailRow("પોતાના વિશે પરિચય:", profile.aboutMe)
            }

            // Astrology Details Section
            DetailSectionCard(
                title = "જ્યોતિષ અને જન્મ વિગત (જન્માક્ષર)",
                icon = Icons.Default.AutoAwesome
            ) {
                DetailRow("રાશિ:", profile.rashi)
                DetailRow("નક્ષત્ર:", profile.nakshatra)
                DetailRow("માંગલિક સ્થિતિ:", profile.manglikStatus)
                DetailRow("જન્મ સ્થળ અને સમય:", "${profile.birthPlace}, ${profile.birthTime}")
            }

            // Partner Preferences Section
            DetailSectionCard(
                title = "અપેક્ષિત જીવનસાથી પસંદગી (ઈચ્છિત પાત્ર)",
                icon = Icons.Default.Tune
            ) {
                DetailRow("અપેક્ષિત ઉંમર:", "${profile.prefAgeMin} - ${profile.prefAgeMax} વર્ષ")
                DetailRow("અપેક્ષિત ઊંચાઈ:", "${profile.prefHeightMin} - ${profile.prefHeightMax}")
                DetailRow("ન્યૂનતમ આવક અપેક્ષા:", profile.prefMinIncome)
                DetailRow("શાખ મર્યાદા નિયમ:", "શાખ વર્જિત નિયમનું પાલન")
            }

            if (isBlocked) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFFFEBEE)),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color.Red),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.Block, contentDescription = null, tint = Color.Red)
                        Spacer(modifier = Modifier.width(8.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text("આ સભ્યને બ્લોક કરવામાં આવ્યા છે", fontWeight = FontWeight.Bold, color = Color.Red, fontSize = 14.sp)
                            Text("આ સભ્ય તમને રસ-પ્રસ્તાવ અથવા મેસેજ મોકલી શકશે નહીં.", fontSize = 12.sp, color = Color.DarkGray)
                        }
                    }
                }
            }

            // Action Bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 12.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Button(
                    onClick = {
                        if (isBlocked) {
                            android.widget.Toast.makeText(context, "આ સભ્ય બ્લોક કરેલ છે, તેથી રસ-પ્રસ્તાવ મોકલી શકાશે નહીં.", android.widget.Toast.LENGTH_SHORT).show()
                        } else {
                            viewModel.sendInterest(profile.id) { success, msg ->
                                android.widget.Toast.makeText(context, msg, android.widget.Toast.LENGTH_SHORT).show()
                            }
                        }
                    },
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = when {
                            isBlocked -> Color.Gray
                            profile.interestStatus == "SENT" -> VerifiedGreen
                            else -> RoyalMaroon
                        }
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(
                        imageVector = when {
                            isBlocked -> Icons.Default.Block
                            profile.interestStatus == "SENT" -> Icons.Default.CheckCircle
                            else -> Icons.AutoMirrored.Filled.Send
                        },
                        contentDescription = null
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        when {
                            isBlocked -> "અવરોધિત (Blocked)"
                            profile.interestStatus == "SENT" -> "પ્રસ્તાવ મોકલ્યો"
                            else -> "રસ દાખવો"
                        }
                    )
                }

                Button(
                    onClick = {
                        if (isBlocked) {
                            android.widget.Toast.makeText(context, "આ સભ્ય બ્લોક કરેલ છે.", android.widget.Toast.LENGTH_SHORT).show()
                        } else {
                            showContactModal = true
                        }
                    },
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(containerColor = if (isBlocked) Color.LightGray else RoyalGold),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(Icons.Default.Phone, contentDescription = null)
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("પરિવારને કોલ કરો")
                }

                IconButton(
                    onClick = {
                        if (isBlocked) {
                            android.widget.Toast.makeText(context, "આ સભ્ય બ્લોક કરેલ છે.", android.widget.Toast.LENGTH_SHORT).show()
                        } else {
                            onChatClick(profile.id)
                        }
                    },
                    modifier = Modifier
                        .size(52.dp)
                        .background(if (isBlocked) Color.LightGray else SoftGold, RoundedCornerShape(12.dp))
                ) {
                    Icon(Icons.AutoMirrored.Filled.Chat, contentDescription = "ચેટ કરો", tint = if (isBlocked) Color.Gray else RoyalMaroon)
                }
            }

            if (isBlocked) {
                OutlinedButton(
                    onClick = {
                        viewModel.unblockUser(profile.id)
                        android.widget.Toast.makeText(context, "સભ્ય અનબ્લોક કરવામાં આવ્યો છે.", android.widget.Toast.LENGTH_SHORT).show()
                    },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = VerifiedGreen),
                    border = androidx.compose.foundation.BorderStroke(1.dp, VerifiedGreen),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(Icons.Default.CheckCircle, contentDescription = null, tint = VerifiedGreen, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("સભ્યને અનબ્લોક કરો (Unblock User)", color = VerifiedGreen, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                }
            } else {
                OutlinedButton(
                    onClick = { showBlockConfirmDialog = true },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.Red),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color.Red.copy(alpha = 0.5f)),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(Icons.Default.Block, contentDescription = null, tint = Color.Red, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("સભ્યને બ્લોક કરો અને રસ-પ્રસ્તાવ આવતા અટકાવો", color = Color.Red, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                }
            }
        }
    }

    if (showBlockConfirmDialog) {
        AlertDialog(
            onDismissRequest = { showBlockConfirmDialog = false },
            icon = {
                Icon(Icons.Default.Block, contentDescription = null, tint = Color.Red, modifier = Modifier.size(36.dp))
            },
            title = {
                Text("સભ્યને બ્લોક કરો અને રસ-પ્રસ્તાવ અટકાવો", fontWeight = FontWeight.Bold, fontSize = 18.sp)
            },
            text = {
                Text(
                    "શું તમે ખરેખર ${profile.fullName} ને બ્લોક કરવા અને રસ-પ્રસ્તાવ આવતા અટકાવવા માંગો છો? આનાથી સંદેશાઓ તથા રસ-પ્રસ્તાવ મોકલવાનું બંધ થઈ જશે.",
                    fontSize = 14.sp
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.removeInterestAndBlock(profile.id)
                        showBlockConfirmDialog = false
                        android.widget.Toast.makeText(
                            context,
                            "સભ્ય સફળતાપૂર્વક બ્લોક કરાયો છે. આ સભ્ય તમને રસ-પ્રસ્તાવ મોકલી શકશે નહીં.",
                            android.widget.Toast.LENGTH_LONG
                        ).show()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Red)
                ) {
                    Text("હા, બ્લોક કરો અને પ્રસ્તાવ અટકાવો", color = Color.White)
                }
            },
            dismissButton = {
                OutlinedButton(onClick = { showBlockConfirmDialog = false }) {
                    Text("રદ કરો")
                }
            }
        )
    }

    if (showContactModal) {
        AlertDialog(
            onDismissRequest = { showContactModal = false },
            title = { Text("ચૌધરી પરિવાર સંપર્ક વિગતો", fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("ઉમેદવારનો સંપર્ક: ${profile.phoneContact.ifBlank { "—" }}")
                    
                    if (isInterestAccepted) {
                        Text("વાલીનો મોબાઈલ નંબર (Parent's Contact):", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                        Text(
                            text = if (profile.parentPhoneContact.isNotBlank()) profile.parentPhoneContact else profile.phoneContact.ifBlank { "—" },
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp,
                            color = RoyalMaroon
                        )
                    } else {
                        Surface(
                            color = SoftGold,
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.padding(10.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(Icons.Default.Lock, contentDescription = null, tint = RoyalMaroon, modifier = Modifier.size(20.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "વાલીનો મોબાઈલ નંબર માત્ર બંને પક્ષ વચ્ચે પસંદગી (Interest) સ્વીકારાયા બાદ જ જોઈ શકાશે.",
                                    fontSize = 12.sp,
                                    color = DarkMaroon,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                        }
                    }

                    Text("મૂળ ગામ: ${profile.nativeVillage}")
                    Text("મોસાળ ગામ: ${profile.motherBirthVillage}")
                    Text("નોંધ: કૃપા કરીને પરિવાર સાથે વાત કરતી વખતે સમાજ મર્યાદાનું પાલન કરો.", fontSize = 12.sp, color = Color.Gray)
                }
            },
            confirmButton = {
                Button(onClick = { showContactModal = false }) {
                    Text("બંધ કરો")
                }
            }
        )
    }
}

@Composable
fun DetailSectionCard(
    title: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    content: @Composable ColumnScope.() -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = androidx.compose.foundation.BorderStroke(1.dp, BorderLightGold),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(icon, contentDescription = null, tint = RoyalMaroon, modifier = Modifier.size(20.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text(text = title, fontWeight = FontWeight.Bold, fontSize = 15.sp, color = RoyalMaroon)
            }
            HorizontalDivider(modifier = Modifier.padding(vertical = 10.dp), color = Color.LightGray.copy(alpha = 0.5f))
            content()
        }
    }
}

@Composable
fun DetailRow(label: String, value: String, isHighlight: Boolean = false) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(text = label, fontSize = 13.sp, color = Color.Gray, modifier = Modifier.weight(1f))
        Text(
            text = value,
            fontSize = 13.sp,
            fontWeight = if (isHighlight) FontWeight.Bold else FontWeight.SemiBold,
            color = if (isHighlight) RoyalMaroon else Color.Unspecified,
            modifier = Modifier.weight(1.2f)
        )
    }
}
