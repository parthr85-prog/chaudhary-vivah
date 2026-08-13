package com.example.ui.screens

import android.widget.Toast
import androidx.compose.animation.*
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
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import coil.compose.AsyncImage
import com.example.R
import com.example.model.Profile
import com.example.ui.theme.*
import com.example.ui.viewmodel.MatrimonyViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminApprovalScreen(
    viewModel: MatrimonyViewModel,
    onBackClick: () -> Unit
) {
    val context = LocalContext.current
    val pendingProfiles by viewModel.pendingProfiles.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "એડમિન મંજૂરી પેનલ",
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp,
                            color = RoyalMaroon
                        )
                        Text(
                            text = "પેન્ડિંગ પ્રોફાઇલ્સ: ${pendingProfiles.size}",
                            fontSize = 12.sp,
                            color = Color.Gray
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = RoyalMaroon)
                    }
                },
                actions = {
                    IconButton(
                        onClick = {
                            viewModel.logout()
                            onBackClick()
                        },
                        modifier = Modifier.testTag("admin_logout_button")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.Logout,
                            contentDescription = "Logout",
                            tint = RoyalMaroon
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = SoftGold)
            )
        },
        containerColor = CreamBackground
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp)
        ) {
            // Header Banner
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = RoyalMaroon)
            ) {
                Row(
                    modifier = Modifier.padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        Icons.Default.AdminPanelSettings,
                        contentDescription = null,
                        tint = RoyalGold,
                        modifier = Modifier.size(36.dp)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = "સમાજ સંચાલક એડમિન ડેશબોર્ડ",
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                            fontSize = 15.sp
                        )
                        Text(
                            text = "નવા નોંધાયેલા સભ્યોની વિગતો ચકાસીને મંજૂરી આપો. મંજૂર થયેલા જ પ્રોફાઇલ સમાજ સમક્ષ દર્શાવાશે.",
                            color = Color.White.copy(alpha = 0.85f),
                            fontSize = 12.sp
                        )
                    }
                }
            }

            if (pendingProfiles.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            Icons.Default.VerifiedUser,
                            contentDescription = null,
                            tint = VerifiedGreen,
                            modifier = Modifier.size(64.dp)
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "બધા પ્રોફાઇલ્સ મંજૂર થયેલા છે!",
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp,
                            color = RoyalMaroon
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "હાલમાં કોઈ પેન્ડિંગ પ્રોફાઇલ નથી.",
                            fontSize = 14.sp,
                            color = Color.Gray
                        )
                    }
                }
            } else {
                var processingProfileId by remember { mutableStateOf<String?>(null) }
                var processingActionName by remember { mutableStateOf("") }

                if (processingProfileId != null) {
                    androidx.compose.ui.window.Dialog(
                        onDismissRequest = { /* strictly wait */ },
                        properties = androidx.compose.ui.window.DialogProperties(dismissOnBackPress = false, dismissOnClickOutside = false)
                    ) {
                        Card(
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(containerColor = SurfaceCream),
                            border = androidx.compose.foundation.BorderStroke(2.dp, RoyalGold),
                            modifier = Modifier.padding(16.dp).fillMaxWidth(0.85f)
                        ) {
                            Column(
                                modifier = Modifier.padding(24.dp),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(16.dp)
                            ) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(44.dp),
                                    color = RoyalMaroon,
                                    strokeWidth = 4.dp
                                )
                                Text(
                                    text = "$processingActionName...",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 15.sp,
                                    color = RoyalMaroon,
                                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                                )
                                Text(
                                    text = "કૃપા કરીને રાહ જુઓ, ફાયરબેઝમાં ડેટા અપડેટ થઈ રહ્યો છે.",
                                    fontSize = 12.sp,
                                    color = Color.DarkGray,
                                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                                )
                            }
                        }
                    }
                }

                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(14.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    items(pendingProfiles, key = { it.id }) { profile ->
                        AdminProfileCard(
                            profile = profile,
                            onApprove = {
                                processingProfileId = profile.id
                                processingActionName = "${profile.fullName} ની પ્રોફાઇલ મંજૂર કરી રહ્યા છીએ"
                                viewModel.approveProfile(profile.id) { success ->
                                    processingProfileId = null
                                    if (success) {
                                        Toast.makeText(context, "${profile.fullName} ની પ્રોફાઇલ મંજૂર કરવામાં આવી!", Toast.LENGTH_SHORT).show()
                                    } else {
                                        Toast.makeText(context, "મંજૂરી નિષ્ફળ! ફરી પ્રયાસ કરો.", Toast.LENGTH_LONG).show()
                                    }
                                }
                            },
                            onReject = { reason ->
                                processingProfileId = profile.id
                                processingActionName = "${profile.fullName} ની પ્રોફાઇલ અસ્વીકાર કરી રહ્યા છીએ"
                                viewModel.rejectProfile(profile.id, reason) { success ->
                                    processingProfileId = null
                                    if (success) {
                                        Toast.makeText(context, "${profile.fullName} ની પ્રોફાઇલ અસ્વીકાર કરવામાં આવી.", Toast.LENGTH_SHORT).show()
                                    } else {
                                        Toast.makeText(context, "અસ્વીકાર નિષ્ફળ! ફરી પ્રયાસ કરો.", Toast.LENGTH_LONG).show()
                                    }
                                }
                            },
                            onDelete = {
                                processingProfileId = profile.id
                                processingActionName = "${profile.fullName} ની અરજી રદ કરી રહ્યા છીએ"
                                viewModel.deleteProfile(profile.id) { success ->
                                    processingProfileId = null
                                    if (success) {
                                        Toast.makeText(context, "${profile.fullName} ની અરજી રદ કરવામાં આવી.", Toast.LENGTH_SHORT).show()
                                    } else {
                                        Toast.makeText(context, "રદ કરવું નિષ્ફળ! ફરી પ્રયાસ કરો.", Toast.LENGTH_LONG).show()
                                    }
                                }
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun AdminProfileCard(
    profile: Profile,
    onApprove: () -> Unit,
    onReject: (reason: String) -> Unit,
    onDelete: () -> Unit
) {
    var showRejectDialog by remember { mutableStateOf(false) }
    var rejectionReasonInput by remember { mutableStateOf("") }
    var previewImageUrl by remember { mutableStateOf<String?>(null) }
    var previewImageTitle by remember { mutableStateOf("") }

    if (previewImageUrl != null) {
        Dialog(onDismissRequest = { previewImageUrl = null }) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = previewImageTitle,
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp,
                            color = RoyalMaroon,
                            modifier = Modifier.weight(1f)
                        )
                        IconButton(onClick = { previewImageUrl = null }) {
                            Icon(Icons.Default.Close, contentDescription = "Close", tint = RoyalMaroon)
                        }
                    }

                    HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp), color = Color.LightGray)

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(min = 220.dp, max = 460.dp)
                            .background(Color.Black, RoundedCornerShape(10.dp))
                            .padding(4.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        AsyncImage(
                            model = previewImageUrl,
                            contentDescription = previewImageTitle,
                            modifier = Modifier.fillMaxWidth(),
                            contentScale = ContentScale.Fit
                        )
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    Button(
                        onClick = { previewImageUrl = null },
                        colors = ButtonDefaults.buttonColors(containerColor = RoyalMaroon),
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Text("બંધ કરો (Close)", fontWeight = FontWeight.Bold, color = Color.White)
                    }
                }
            }
        }
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("admin_profile_card_${profile.id}"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Header Row: Avatar, Name, Gender & Age
            Row(verticalAlignment = Alignment.CenterVertically) {
                val adminCardImg = profile.getEffectiveProfileImageUrl()
                if (adminCardImg.isNotBlank()) {
                    AsyncImage(
                        model = adminCardImg,
                        contentDescription = profile.fullName,
                        placeholder = painterResource(id = R.drawable.img_matrimony_hero_1784990427738),
                        error = painterResource(id = R.drawable.img_matrimony_hero_1784990427738),
                        modifier = Modifier
                            .size(54.dp)
                            .clip(CircleShape)
                            .border(2.dp, RoyalGold, CircleShape)
                            .clickable {
                                previewImageUrl = adminCardImg
                                previewImageTitle = "પ્રોફાઇલ ફોટો - ${profile.fullName}"
                            },
                        contentScale = ContentScale.Crop
                    )
                } else {
                    Box(
                        modifier = Modifier
                            .size(54.dp)
                            .clip(CircleShape)
                            .background(SoftGold)
                            .border(2.dp, RoyalGold, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = profile.fullName.take(1).ifBlank { "C" },
                            fontWeight = FontWeight.Bold,
                            color = RoyalMaroon,
                            fontSize = 20.sp
                        )
                    }
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = profile.fullName.ifBlank { "Unregistered User" },
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        color = RoyalMaroon
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "${profile.gender} • ${if (profile.age > 0) "${profile.age} વર્ષ" else "ઉંમર આપેલ નથી"} • ${profile.currentCity}",
                        fontSize = 13.sp,
                        color = Color.DarkGray
                    )
                }

                Surface(
                    color = Color(0xFFFFF3CD),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        text = "પેન્ડિંગ",
                        color = Color(0xFF856404),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }

            HorizontalDivider(
                modifier = Modifier.padding(vertical = 12.dp),
                color = Color.LightGray.copy(alpha = 0.4f)
            )

            // Profile Summary Grid
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                AdminDetailRow(label = "પિતાનું નામ:", value = profile.fatherName.ifBlank { "—" })
                AdminDetailRow(label = "દાદાનું નામ:", value = profile.grandfatherName.ifBlank { "—" })
                AdminDetailRow(label = "માતાનું નામ:", value = profile.motherName.ifBlank { "—" })
                AdminDetailRow(label = "સબ-કાસ્ટ:", value = profile.subCaste.ifBlank { "દર્શાવેલ નથી" })
                AdminDetailRow(label = "ગોત્ર / મોસાળ:", value = "${profile.gotra.ifBlank { "—" }} / ${profile.motherGotra.ifBlank { "—" }}")
                AdminDetailRow(label = "મૂળ ગામ:", value = profile.nativeVillage.ifBlank { "દર્શાવેલ નથી" })
                AdminDetailRow(label = "શિક્ષણ & વ્યવસાય:", value = "${profile.education} (${profile.occupation})".trim())
                AdminDetailRow(label = "સંપર્ક નંબર:", value = profile.phoneContact.ifBlank { "—" })
                AdminDetailRow(label = "વાલીનો મોબાઈલ:", value = profile.parentPhoneContact.ifBlank { "—" })
            }

            // Uploaded Aadhar Proof Cards
            Spacer(modifier = Modifier.height(8.dp))
            Surface(
                color = Color(0xFFFFF8E1),
                shape = RoundedCornerShape(10.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, RoyalGold),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(10.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Badge, contentDescription = null, tint = RoyalMaroon, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("અપલોડ કરેલ આધાર કાર્ડ ફોટા (ક્લિક કરી મોટો ફોટો જુઓ):", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = RoyalMaroon)
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        AdminAadharPhotoItem(
                            label = "આધાર આગળનો ફોટો",
                            url = profile.aadharFrontUrl,
                            modifier = Modifier.weight(1f),
                            onClick = {
                                if (profile.aadharFrontUrl.isNotBlank()) {
                                    previewImageUrl = profile.aadharFrontUrl
                                    previewImageTitle = "આધાર આગળનો ફોટો - ${profile.fullName}"
                                }
                            }
                        )
                        AdminAadharPhotoItem(
                            label = "આધાર પાછળનો ફોટો",
                            url = profile.aadharBackUrl,
                            modifier = Modifier.weight(1f),
                            onClick = {
                                if (profile.aadharBackUrl.isNotBlank()) {
                                    previewImageUrl = profile.aadharBackUrl
                                    previewImageTitle = "આધાર પાછળનો ફોટો - ${profile.fullName}"
                                }
                            }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Action Buttons Row: Approve & Reject
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                OutlinedButton(
                    onClick = { showRejectDialog = true },
                    modifier = Modifier
                        .weight(1f)
                        .height(44.dp)
                        .testTag("admin_reject_btn_${profile.id}"),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.Red),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color.Red),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Icon(Icons.Default.Close, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("અસ્વીકાર કરો", fontSize = 13.sp, fontWeight = FontWeight.Bold)
                }

                Button(
                    onClick = onApprove,
                    modifier = Modifier
                        .weight(1f)
                        .height(44.dp)
                        .testTag("admin_approve_btn_${profile.id}"),
                    colors = ButtonDefaults.buttonColors(containerColor = VerifiedGreen),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("મંજૂર કરો", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Color.White)
                }
            }

            if (showRejectDialog) {
                Spacer(modifier = Modifier.height(12.dp))
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFFFEBEE)),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color.Red)
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text(
                            text = "પ્રોફાઇલ અસ્વીકારવાનું કાયમી કારણ દાખલ કરો:",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.Red
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        OutlinedTextField(
                            value = rejectionReasonInput,
                            onValueChange = { rejectionReasonInput = it },
                            placeholder = { Text("ઉદા. આધાર કાર્ડ સ્પષ્ટ નથી / ફોટો અપલોડ કરો...") },
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("admin_rejection_reason_inline_input"),
                            shape = RoundedCornerShape(8.dp)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.End
                        ) {
                            TextButton(onClick = { showRejectDialog = false }) {
                                Text("રદ કરો", color = Color.Gray)
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            Button(
                                onClick = {
                                    val reason = rejectionReasonInput.ifBlank { "પ્રોફાઇલ વિગતો અપૂર્ણ અથવા અયોગ્ય છે." }
                                    showRejectDialog = false
                                    onReject(reason)
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = Color.Red)
                            ) {
                                Text("અસ્વીકાર મોકલો", color = Color.White, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun AdminDetailRow(label: String, value: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Text(
            text = label,
            fontSize = 12.sp,
            fontWeight = FontWeight.SemiBold,
            color = Color.Gray,
            modifier = Modifier.width(110.dp)
        )
        Text(
            text = value,
            fontSize = 12.sp,
            fontWeight = FontWeight.Medium,
            color = Color.Black
        )
    }
}

@Composable
fun AdminAadharPhotoItem(
    label: String,
    url: String,
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null
) {
    Card(
        modifier = modifier
            .height(100.dp)
            .then(
                if (url.isNotBlank() && onClick != null) {
                    Modifier.clickable { onClick() }
                } else Modifier
            ),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = androidx.compose.foundation.BorderStroke(1.5.dp, if (url.isNotBlank()) VerifiedGreen else Color.LightGray),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        if (url.isNotBlank()) {
            Box(modifier = Modifier.fillMaxSize()) {
                coil.compose.AsyncImage(
                    model = url,
                    contentDescription = label,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
                Surface(
                    color = Color.Black.copy(alpha = 0.7f),
                    modifier = Modifier.align(Alignment.BottomCenter).fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 4.dp, vertical = 3.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Icon(Icons.Default.ZoomIn, contentDescription = null, tint = Color.White, modifier = Modifier.size(12.dp))
                        Spacer(modifier = Modifier.width(3.dp))
                        Text(
                            text = "$label 🔍",
                            color = Color.White,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                        )
                    }
                }
            }
        } else {
            Column(
                modifier = Modifier.fillMaxSize().padding(4.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Icon(Icons.Default.ImageNotSupported, contentDescription = null, tint = Color.Gray, modifier = Modifier.size(20.dp))
                Spacer(modifier = Modifier.height(2.dp))
                Text(label, fontSize = 10.sp, color = Color.Gray)
                Text("અપલોડ નથી", fontSize = 9.sp, color = Color.Red, fontWeight = FontWeight.Bold)
            }
        }
    }
}
