package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.*
import com.example.ui.viewmodel.MatrimonyViewModel
import com.example.util.LocaleStrings

@Composable
fun LanguageSelectionScreen(
    viewModel: MatrimonyViewModel,
    onLanguageSelected: () -> Unit
) {
    val currentLang by viewModel.appLanguage.collectAsState()
    var selectedLang by remember { mutableStateOf(currentLang) }
    val strings = remember(selectedLang) { LocaleStrings.getStrings(selectedLang) }

    val backgroundGradient = Brush.verticalGradient(
        colors = listOf(
            CreamBackground,
            SurfaceVariantLight,
            SoftGold
        )
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(backgroundGradient)
            .windowInsetsPadding(WindowInsets.safeDrawing)
            .testTag("language_selection_screen"),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.padding(top = 20.dp)
            ) {
                Surface(
                    color = RoyalGold.copy(alpha = 0.2f),
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier.padding(bottom = 12.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Translate,
                        contentDescription = "Language Selection",
                        tint = RoyalMaroon,
                        modifier = Modifier
                            .size(54.dp)
                            .padding(10.dp)
                    )
                }

                Text(
                    text = strings.selectLanguageTitle,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = RoyalMaroon,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(6.dp))

                Text(
                    text = strings.selectLanguageSubtitle,
                    fontSize = 13.sp,
                    color = Color.DarkGray,
                    textAlign = TextAlign.Center
                )
            }

            // Language Options Column
            Column(
                verticalArrangement = Arrangement.spacedBy(16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                // Gujarati Option Card
                LanguageOptionCard(
                    title = strings.gujaratiName,
                    subtitle = strings.gujaratiDesc,
                    isSelected = selectedLang == "gu",
                    badgeText = if (selectedLang == "gu") "પ્રાથમિક" else "Primary",
                    onClick = {
                        selectedLang = "gu"
                        viewModel.setAppLanguage("gu")
                    },
                    modifier = Modifier.testTag("lang_option_gujarati")
                )

                // English Option Card
                LanguageOptionCard(
                    title = strings.englishName,
                    subtitle = strings.englishDesc,
                    isSelected = selectedLang == "en",
                    badgeText = "English",
                    onClick = {
                        selectedLang = "en"
                        viewModel.setAppLanguage("en")
                    },
                    modifier = Modifier.testTag("lang_option_english")
                )
            }

            // Continue Button
            Button(
                onClick = {
                    viewModel.setAppLanguage(selectedLang)
                    onLanguageSelected()
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
                    .testTag("language_continue_button"),
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(containerColor = RoyalMaroon)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = strings.continueBtn,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Icon(
                        imageVector = Icons.Default.ArrowForward,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun LanguageOptionCard(
    title: String,
    subtitle: String,
    isSelected: Boolean,
    badgeText: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) Color(0xFFFFF8E1) else Color.White
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = if (isSelected) 6.dp else 2.dp),
        border = androidx.compose.foundation.BorderStroke(
            width = if (isSelected) 2.dp else 1.dp,
            color = if (isSelected) RoyalGold else Color.LightGray
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                RadioButton(
                    selected = isSelected,
                    onClick = onClick,
                    colors = RadioButtonDefaults.colors(
                        selectedColor = RoyalMaroon,
                        unselectedColor = Color.Gray
                    )
                )

                Spacer(modifier = Modifier.width(12.dp))

                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = title,
                            fontSize = 17.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (isSelected) RoyalMaroon else Color.Black
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Surface(
                            color = if (isSelected) RoyalMaroon.copy(alpha = 0.15f) else Color.LightGray.copy(alpha = 0.3f),
                            shape = RoundedCornerShape(6.dp)
                        ) {
                            Text(
                                text = badgeText,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (isSelected) RoyalMaroon else Color.DarkGray,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(2.dp))

                    Text(
                        text = subtitle,
                        fontSize = 12.sp,
                        color = Color.Gray
                    )
                }
            }

            if (isSelected) {
                Icon(
                    imageVector = Icons.Default.CheckCircle,
                    contentDescription = "Selected",
                    tint = RoyalMaroon,
                    modifier = Modifier.size(24.dp)
                )
            }
        }
    }
}
