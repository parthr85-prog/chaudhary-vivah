package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.model.Profile
import com.example.ui.theme.*
import com.example.ui.viewmodel.MatrimonyViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun KundliMatchScreen(
    viewModel: MatrimonyViewModel,
    onBackClick: () -> Unit
) {
    val myProfile by viewModel.myProfile.collectAsState()
    val allProfiles by viewModel.filteredProfiles.collectAsState()
    val selectedPartner by viewModel.selectedKundliPartner.collectAsState()
    val kundliResult by viewModel.kundliResult.collectAsState()
    val isCalculating by viewModel.isCalculatingKundli.collectAsState()

    var dropdownExpanded by remember { mutableStateOf(false) }

    val appLanguage by viewModel.appLanguage.collectAsState()
    val strings = remember(appLanguage) { com.example.util.LocaleStrings.getStrings(appLanguage) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(strings.kundliTitle, fontWeight = FontWeight.Bold, fontSize = 16.sp) },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = strings.backBtn)
                    }
                },
                actions = {},
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
            // Mandap Kundli Banner Card
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("kundli_banner_card"),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = DarkMaroon)
            ) {
                Box(modifier = Modifier.height(130.dp)) {
                    Image(
                        painter = painterResource(id = R.drawable.img_kundli_mandap_1784990442314),
                        contentDescription = "Kundli Banner",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )

                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(DarkMaroon.copy(alpha = 0.75f))
                            .padding(16.dp),
                        verticalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = "૩૬ ગુણ વૈદિક જ્યોતિષ મિલન",
                            color = SoftGold,
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp
                        )
                        Text(
                            text = "ચૌધરી સમાજના શાખ નિયમો અને જન્માક્ષર સુસંગતતા ચકાસો",
                            color = Color.White,
                            fontSize = 12.sp,
                            modifier = Modifier.padding(top = 4.dp)
                        )
                    }
                }
            }

            // Candidate Selector & Birth Details Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = "૧. કુંડળી મિલન માટે પાત્ર પસંદ કરો:",
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        color = RoyalMaroon
                    )

                    OutlinedCard(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { dropdownExpanded = true }
                            .testTag("select_partner_dropdown"),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = selectedPartner?.fullName ?: "પ્રોફાઇલ પસંદ કરવા અહીં ટેપ કરો...",
                                    fontWeight = FontWeight.Bold,
                                    color = if (selectedPartner != null) RoyalMaroon else Color.Gray
                                )
                                if (selectedPartner != null) {
                                    Text(
                                        text = "${selectedPartner!!.subCaste} | શાખ: ${selectedPartner!!.gotra} | જન્મસ્થળ: ${selectedPartner!!.birthPlace}",
                                        fontSize = 12.sp,
                                        color = DarkMaroon
                                    )
                                }
                            }
                            Icon(Icons.Default.ArrowDropDown, contentDescription = null, tint = RoyalMaroon)
                        }
                    }

                    DropdownMenu(
                        expanded = dropdownExpanded,
                        onDismissRequest = { dropdownExpanded = false },
                        modifier = Modifier
                            .fillMaxWidth(0.9f)
                            .background(SurfaceCream)
                    ) {
                        allProfiles.forEach { candidate ->
                            DropdownMenuItem(
                                text = {
                                    Column {
                                        Text(
                                            "${candidate.fullName} (${candidate.gender}, ${candidate.age} yrs)",
                                            fontWeight = FontWeight.Bold
                                        )
                                        Text(
                                            "Gotra: ${candidate.gotra} | Birth: ${candidate.birthPlace}, ${candidate.birthTime}",
                                            fontSize = 11.sp,
                                            color = Color.Gray
                                        )
                                    }
                                },
                                onClick = {
                                    dropdownExpanded = false
                                    viewModel.selectPartnerForKundli(candidate)
                                }
                            )
                        }
                    }

                    // Comparison Birth Charts Grid
                    if (selectedPartner != null) {
                        HorizontalDivider(color = Color.LightGray.copy(alpha = 0.4f))

                        Text(
                            text = "૨. જન્મ વિગતોની સરખામણી:",
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp,
                            color = DarkMaroon
                        )

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            // User Profile Birth Details
                            Card(
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(12.dp),
                                colors = CardDefaults.cardColors(containerColor = SoftGold)
                            ) {
                                Column(
                                    modifier = Modifier.padding(10.dp),
                                    verticalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    Text(
                                        text = "તમે (${myProfile.fullName})",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 12.sp,
                                        color = RoyalMaroon
                                    )
                                    Text("🎂 ઉંમર: ${myProfile.age} વર્ષ", fontSize = 11.sp)
                                    Text("⏰ સમય: ${myProfile.birthTime}", fontSize = 11.sp)
                                    Text("📍 જન્મસ્થળ: ${myProfile.birthPlace}", fontSize = 11.sp)
                                    Text("⭐ રાશિ: ${myProfile.rashi}", fontSize = 11.sp)
                                    Text("🌌 નક્ષત્ર: ${myProfile.nakshatra}", fontSize = 11.sp)
                                    Text("🔴 માંગલિક: ${myProfile.manglikStatus}", fontSize = 11.sp)
                                }
                            }

                            // Partner Profile Birth Details
                            Card(
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(12.dp),
                                colors = CardDefaults.cardColors(containerColor = LightRoseContainer)
                            ) {
                                Column(
                                    modifier = Modifier.padding(10.dp),
                                    verticalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    Text(
                                        text = selectedPartner!!.fullName,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 12.sp,
                                        color = DarkMaroon
                                    )
                                    Text("🎂 ઉંમર: ${selectedPartner!!.age} વર્ષ", fontSize = 11.sp)
                                    Text("⏰ સમય: ${selectedPartner!!.birthTime}", fontSize = 11.sp)
                                    Text("📍 જન્મસ્થળ: ${selectedPartner!!.birthPlace}", fontSize = 11.sp)
                                    Text("⭐ રાશિ: ${selectedPartner!!.rashi}", fontSize = 11.sp)
                                    Text("🌌 નક્ષત્ર: ${selectedPartner!!.nakshatra}", fontSize = 11.sp)
                                    Text("🔴 માંગલિક: ${selectedPartner!!.manglikStatus}", fontSize = 11.sp)
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(4.dp))

                        Button(
                            onClick = { viewModel.selectPartnerForKundli(selectedPartner!!) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(48.dp)
                                .testTag("analyze_kundli_button"),
                            colors = ButtonDefaults.buttonColors(containerColor = RoyalMaroon),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Icon(Icons.Default.AutoAwesome, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("કુંડળી મિલન અને ૩૬ ગુણ ચકાસો (Gemini AI)", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }

            // Calculations Display
            if (selectedPartner != null) {
                if (isCalculating) {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(18.dp),
                        colors = CardDefaults.cardColors(containerColor = SurfaceCream)
                    ) {
                        Column(
                            modifier = Modifier.padding(24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            CircularProgressIndicator(color = RoyalMaroon)
                            Spacer(modifier = Modifier.height(12.dp))
                            Text("૩૬ ગુણ અને ગોત્ર મર્યાદાનું ગણતરી વિશ્લેષણ થઈ રહ્યું છે...", fontWeight = FontWeight.Bold)
                        }
                    }
                } else if (kundliResult != null) {
                    val score = kundliResult!!.first
                    val report = kundliResult!!.second

                    // Score Circle Card
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(20.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = if (score > 20) SoftGold else Color(0xFFFFEBEE)
                        ),
                        border = androidx.compose.foundation.BorderStroke(
                            2.dp,
                            if (score > 20) RoyalGold else Color.Red
                        )
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(20.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(100.dp)
                                    .clip(CircleShape)
                                    .background(if (score > 20) RoyalMaroon else Color.Red),
                                contentAlignment = Alignment.Center
                            ) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text(
                                        text = "$score",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 32.sp,
                                        color = Color.White
                                    )
                                    Text(
                                        text = "/ ૩૬ ગુણ",
                                        fontSize = 11.sp,
                                        color = SoftGold
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(12.dp))

                            Text(
                                text = if (score >= 25) "🌸 ઉત્તમ મિલન (શુભ ગુણ મિલન)"
                                else if (score >= 18) "✅ મધ્યમ મિલન (સામાન્ય અનુકૂળતા)"
                                else "⚠️ ગોત્ર / જ્યોતિષીય સાવધાની (ગોત્ર દોષ)",
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp,
                                color = if (score > 18) RoyalMaroon else Color.Red,
                                textAlign = TextAlign.Center
                            )
                        }
                    }

                    // Report Details
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(18.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.AutoAwesome, contentDescription = null, tint = WarmSaffron)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("વૈદિક તથા સાંસ્કૃતિક મિલન વિશ્લેષણ", fontWeight = FontWeight.Bold, fontSize = 15.sp, color = RoyalMaroon)
                            }

                            HorizontalDivider(modifier = Modifier.padding(vertical = 10.dp), color = Color.LightGray.copy(alpha = 0.5f))

                            Text(
                                text = report,
                                fontSize = 13.sp,
                                lineHeight = 20.sp,
                                color = Color.DarkGray
                            )
                        }
                    }
                }
            }
        }
    }
}
