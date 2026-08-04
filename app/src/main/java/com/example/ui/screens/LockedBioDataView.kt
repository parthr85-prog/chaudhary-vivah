package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.model.Profile
import com.example.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LockedBioDataView(
    profile: Profile,
    onEditClick: () -> Unit,
    onBackClick: () -> Unit,
    onLogoutClick: () -> Unit = {},
    onRefreshClick: () -> Unit = {}
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("તમારો લોક થયેલ બાયોડેટા", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                        Text("એડમિન ચકાસણી અને સુરક્ષિત બાયોડેટા", fontSize = 11.sp, color = DarkMaroon)
                    }
                },
                actions = {
                    Button(
                        onClick = onEditClick,
                        colors = ButtonDefaults.buttonColors(containerColor = RoyalMaroon),
                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier
                            .padding(end = 4.dp)
                            .testTag("edit_biodata_top_button")
                    ) {
                        Icon(Icons.Default.Edit, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("એડિટ કરો", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    }

                    IconButton(
                        onClick = onLogoutClick,
                        modifier = Modifier.testTag("logout_top_button")
                    ) {
                        Icon(Icons.Default.Logout, contentDescription = "Logout", tint = RoyalMaroon)
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
            // Approval Status Banner
            if (profile.isApproved) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFE8F5E9)),
                    border = androidx.compose.foundation.BorderStroke(1.dp, VerifiedGreen)
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.Verified, contentDescription = null, tint = VerifiedGreen, modifier = Modifier.size(28.dp))
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text("પ્રોફાઇલ એડમિન મંજૂર થયેલ છે", fontWeight = FontWeight.Bold, color = VerifiedGreen, fontSize = 15.sp)
                            Text("તમારો બાયોડેટા અન્ય યુઝર્સના ડેશબોર્ડ પર સક્રિય અને દ્રશ્યમાન છે.", fontSize = 12.5.sp, color = Color.DarkGray)
                        }
                    }
                }
            } else if (profile.isRejected) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFFFEBEE)),
                    border = androidx.compose.foundation.BorderStroke(1.5.dp, Color(0xFFD32F2F)),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(14.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.weight(1f)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Cancel,
                                    contentDescription = null,
                                    tint = Color(0xFFD32F2F),
                                    modifier = Modifier.size(28.dp)
                                )
                                Spacer(modifier = Modifier.width(10.dp))
                                Column {
                                    Text(
                                        text = "પ્રોફાઇલ અસ્વીકૃત થયેલ છે (Profile Rejected)",
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFFD32F2F),
                                        fontSize = 15.sp
                                    )
                                    Text(
                                        text = "એડમિન દ્વારા બાયોડેટા અથવા દસ્તાવેજમાં ક્ષતિ મળેલ છે.",
                                        fontSize = 12.sp,
                                        color = Color(0xFFB71C1C)
                                    )
                                }
                            }

                            IconButton(
                                onClick = onRefreshClick,
                                modifier = Modifier.testTag("refresh_rejection_status_locked_icon")
                            ) {
                                Icon(Icons.Default.Refresh, contentDescription = "Refresh Status", tint = Color(0xFFD32F2F))
                            }
                        }

                        Surface(
                            color = Color.White,
                            shape = RoundedCornerShape(10.dp),
                            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFFFCDD2)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(10.dp)) {
                                Text(
                                    text = "અસ્વીકારનું કારણ (Rejection Reason):",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp,
                                    color = Color(0xFFB71C1C)
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = profile.rejectionReason.ifBlank { "પ્રોફાઇલ વિગતો અથવા આપેલ દસ્તાવેજો/ફોટો અપૂર્ણ છે." },
                                    fontSize = 13.5.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = Color(0xFF212121),
                                    lineHeight = 18.sp
                                )
                            }
                        }

                        Button(
                            onClick = onEditClick,
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFD32F2F)),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(Icons.Default.Edit, contentDescription = null, tint = Color.White, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("પ્રોફાઇલ સુધારો અને ફરી સબમિટ કરો (Edit Profile)", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                        }
                    }
                }
            } else {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFFFF8E1)),
                    border = androidx.compose.foundation.BorderStroke(1.5.dp, RoyalGold)
                ) {
                    Column(
                        modifier = Modifier.padding(14.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.weight(1f)
                            ) {
                                Icon(Icons.Default.HourglassTop, contentDescription = null, tint = Color(0xFFF57F17), modifier = Modifier.size(28.dp))
                                Spacer(modifier = Modifier.width(10.dp))
                                Column {
                                    Text("એડમિન મંજૂરી માટે પેન્ડિંગ (Pending Admin Approval)", fontWeight = FontWeight.Bold, color = Color(0xFFF57F17), fontSize = 14.sp)
                                    Text("તમારું રજીસ્ટ્રેશન એડમિન ચકાસણી હેઠળ છે. એડમિન દ્વારા પ્રોફાઇલ મંજૂર (Approve) થયા બાદ જ ડેશબોર્ડ અને અન્ય સ્ક્રીન સક્રિય થશે.", fontSize = 12.sp, color = Color.DarkGray)
                                }
                            }

                            IconButton(
                                onClick = onRefreshClick,
                                modifier = Modifier.testTag("refresh_approval_status_locked_icon")
                            ) {
                                Icon(Icons.Default.Refresh, contentDescription = "Refresh Approval Status", tint = Color(0xFFF57F17))
                            }
                        }
                    }
                }
            }

            // Header Profile Card with Edit Button
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = SurfaceCream),
                border = androidx.compose.foundation.BorderStroke(1.5.dp, RoyalGold),
                elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(72.dp)
                                .clip(CircleShape)
                                .background(RoyalMaroon.copy(alpha = 0.15f))
                                .border(2.dp, RoyalGold, CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            val imgUrl = profile.getEffectiveProfileImageUrl()
                            if (imgUrl.isNotBlank()) {
                                AsyncImage(
                                    model = imgUrl,
                                    contentDescription = "Profile Photo",
                                    placeholder = androidx.compose.ui.res.painterResource(id = com.example.R.drawable.img_matrimony_hero_1784990427738),
                                    error = androidx.compose.ui.res.painterResource(id = com.example.R.drawable.img_matrimony_hero_1784990427738),
                                    modifier = Modifier.fillMaxSize(),
                                    contentScale = ContentScale.Crop
                                )
                            } else {
                                Icon(Icons.Default.Person, contentDescription = null, tint = RoyalMaroon, modifier = Modifier.size(36.dp))
                            }
                        }

                        Spacer(modifier = Modifier.width(12.dp))

                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = if (profile.fullName.isNotBlank()) profile.fullName else "નામ ઉમેરેલ નથી",
                                fontWeight = FontWeight.Bold,
                                fontSize = 18.sp,
                                color = RoyalMaroon
                            )
                            Text(
                                text = "${if (profile.gender.contains("Bride", ignoreCase = true) || profile.gender.contains("કન્યા", ignoreCase = true) || profile.gender.contains("Female", ignoreCase = true) || profile.gender.contains("સ્ત્રી", ignoreCase = true)) "કન્યા / સ્ત્રી" else "વરરાજા / પુરુષ"} • ${profile.subCaste}",
                                fontSize = 13.sp,
                                color = DarkMaroon
                            )
                            Text(
                                text = "સંપર્ક: ${profile.phoneContact}",
                                fontSize = 12.sp,
                                color = Color.Gray
                            )
                        }
                    }

                    Button(
                        onClick = onEditClick,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(46.dp)
                            .testTag("edit_biodata_primary_button"),
                        colors = ButtonDefaults.buttonColors(containerColor = RoyalMaroon),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(Icons.Default.Edit, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("બાયોડેટા એડિટ કરો (Edit Bio-Data)", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    }
                }
            }

            // Section 1: Basic & Birth Details for Kundli
            BioDataSectionCard(title = "૧. જન્મ વિગતો અને વૈયક્તિક માહિતી") {
                BioDataItem(label = "પસંદગી / લિંગ", value = if (profile.gender.contains("Bride", ignoreCase = true) || profile.gender.contains("કન્યા", ignoreCase = true) || profile.gender.contains("Female", ignoreCase = true) || profile.gender.contains("સ્ત્રી", ignoreCase = true)) "કન્યા (સ્ત્રી)" else "વરરાજા (પુરુષ)")
                BioDataItem(label = "ઉંમર અને ઊંચાઈ", value = "${profile.age} વર્ષ | ${profile.height}")
                BioDataItem(label = "બ્લડ ગ્રુપ (Blood Group)", value = profile.bloodGroup.ifBlank { "A+" })
                BioDataItem(label = "NRI ઉમેદવાર (NRI Candidate)", value = if (profile.isNri) "હા - NRI (Abroad)" else "ના - Resident in India")
                BioDataItem(label = "અગાઉનો વૈવાહિક ઇતિહાસ", value = if (profile.hasMaritalHistory) "હા - અગાઉ લગ્ન થયેલ છે" else "ના - ક્યારેય લગ્ન નથી થયેલ (Never Married)")
                BioDataItem(label = "જન્મ તારીખ (Birth Date)", value = if (profile.birthDate.isNotBlank()) profile.birthDate else "15/08/1998")
                BioDataItem(label = "જન્મ સમય (Birth Time)", value = if (profile.birthTime.isNotBlank()) profile.birthTime else "08:30 AM")
                BioDataItem(label = "જન્મ સ્થળ (Birth Place)", value = if (profile.birthPlace.isNotBlank()) profile.birthPlace else "Palanpur, Banaskantha, Gujarat")
                BioDataItem(label = "રાશિ", value = if (profile.rashi.isNotBlank()) profile.rashi else "Mesha")
            }

            // Section 2: Gotra & Village
            BioDataSectionCard(title = "૨. ગોત્ર અને મોસાળ (Community & Gotra)") {
                BioDataItem(label = "સબ-કાસ્ટ", value = profile.subCaste)
                BioDataItem(label = "પોતાનું ગોત્ર (Self Gotra)", value = profile.gotra)
                BioDataItem(label = "માતાનું ગોત્ર (Mother's Gotra)", value = profile.motherGotra)
                BioDataItem(label = "મૂળ ગામ (Native Village)", value = profile.nativeVillage)
                BioDataItem(label = "માતાનું વતન (Nanihal)", value = profile.motherBirthVillage)
                BioDataItem(label = "પ્રદેશ / પ્રભાવ ક્ષેત્ર", value = profile.locality)
            }

            // Section 3: Career & Income
            BioDataSectionCard(title = "૩. શિક્ષણ અને વ્યવસાય (Career & Income)") {
                BioDataItem(label = "શિક્ષણ (Education)", value = profile.education)
                BioDataItem(label = "વ્યવસાય (Occupation)", value = profile.occupation)
                BioDataItem(label = "માસિક આવક (Monthly Income)", value = profile.monthlyIncome)
                BioDataItem(label = "હાલનું શહેર / રહેઠાણ", value = profile.currentCity)
            }

            // Section 4: Family & Bio
            BioDataSectionCard(title = "૪. પરિવાર અને પરિચય (Family & Bio)") {
                BioDataItem(label = "કૌટુંબિક માહિતી", value = profile.familyDetails)
                BioDataItem(label = "પોતાના વિશે પરિચય", value = profile.aboutMe)
            }

            // Section 5: Aadhaar Photo Status
            BioDataSectionCard(title = "૫. આધાર કાર્ડ ઓળખ ચકાસણી") {
                BioDataItem(
                    label = "આધાર ફ્રન્ટ ફોટો",
                    value = if (profile.aadharFrontUrl.isNotBlank()) "અપલોડ થયેલ છે ✓" else "અપલોડ બાકી ✗"
                )
                BioDataItem(
                    label = "આધાર બેક ફોટો",
                    value = if (profile.aadharBackUrl.isNotBlank()) "અપલોડ થયેલ છે ✓" else "અપલોડ બાકી ✗"
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Bottom Edit Action Button
            Button(
                onClick = onEditClick,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
                    .testTag("edit_biodata_bottom_button"),
                colors = ButtonDefaults.buttonColors(containerColor = RoyalMaroon),
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(Icons.Default.Edit, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("બાયોડેટા એડિટ કરો અને અપડેટ મોકલો", fontWeight = FontWeight.Bold, fontSize = 15.sp)
            }
        }
    }
}

@Composable
private fun BioDataSectionCard(
    title: String,
    content: @Composable ColumnScope.() -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = SurfaceCream),
        border = androidx.compose.foundation.BorderStroke(1.dp, BorderLightGold)
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = title,
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp,
                color = RoyalMaroon
            )
            HorizontalDivider(color = Color.LightGray.copy(alpha = 0.4f))
            content()
        }
    }
}

@Composable
private fun BioDataItem(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(text = label, fontSize = 12.sp, color = Color.Gray)
        Text(
            text = if (value.isNotBlank()) value else "દાખલ કરેલ નથી",
            fontSize = 12.sp,
            fontWeight = FontWeight.SemiBold,
            color = DarkMaroon
        )
    }
}
