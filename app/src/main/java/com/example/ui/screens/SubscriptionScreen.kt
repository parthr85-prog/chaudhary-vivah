package com.example.ui.screens

import android.widget.Toast
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.service.RazorpayService
import com.example.ui.theme.DarkMaroon
import com.example.ui.theme.RoyalGold
import com.example.ui.theme.RoyalMaroon
import com.example.ui.theme.SoftGold
import com.example.ui.theme.WarmSaffron
import com.example.ui.viewmodel.MatrimonyViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SubscriptionScreen(
    viewModel: MatrimonyViewModel,
    onBackClick: () -> Unit
) {
    val context = LocalContext.current
    val myProfile by viewModel.myProfile.collectAsState()
    val appLanguage by viewModel.appLanguage.collectAsState()
    val freeSchemeClaimedCount by viewModel.freeSchemeClaimedCount.collectAsState()
    val isGu = appLanguage == "gu"

    var isProcessingFreeClaim by remember { mutableStateOf(false) }
    var showCheckoutDialog by remember { mutableStateOf(false) }
    var showPaymentSuccessDialog by remember { mutableStateOf<Pair<String, String>?>(null) } // paymentId, planTitle
    var showVerificationError by remember { mutableStateOf<String?>(null) }

    // Fee breakdown: Base ₹500 + 18% GST (₹90) = ₹590 Total for 3 Months
    val basePriceRupees = 500
    val gstAmountRupees = 90
    val totalPriceRupees = 590
    val durationText = "3 Months (૯૦ દિવસ)"

    // Free Scheme stats
    val totalFreeSlots = 100
    val remainingFreeSlots = (totalFreeSlots - freeSchemeClaimedCount).coerceAtLeast(0)
    val isFreeSchemeEligible = !myProfile.isFreeSchemeUsed && !myProfile.isVipSubscribed && freeSchemeClaimedCount < totalFreeSlots

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = if (isGu) "VIP સબ્સ્ક્રિપ્શન અને નવીનીકરણ" else "VIP Membership & Renewal",
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        fontSize = 18.sp
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBackClick, modifier = Modifier.testTag("subscription_back_btn")) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = RoyalMaroon)
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(Color(0xFFFAF6F0))
                .verticalScroll(rememberScrollState())
                .padding(16.dp)
        ) {
            // Header Banner
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = RoyalMaroon)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            Brush.verticalGradient(
                                colors = listOf(RoyalMaroon, DarkMaroon)
                            )
                        )
                        .padding(20.dp)
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Box(
                            modifier = Modifier
                                .size(56.dp)
                                .background(SoftGold.copy(alpha = 0.2f), CircleShape)
                                .border(1.5.dp, RoyalGold, CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.WorkspacePremium,
                                contentDescription = "VIP Badge",
                                tint = RoyalGold,
                                modifier = Modifier.size(36.dp)
                            )
                        }
                        Spacer(modifier = Modifier.height(10.dp))
                        Text(
                            text = if (isGu) "ચૌધરી વિવાહ ૩ મહિના VIP સદસ્યતા" else "Chaudhary Vivah 3-Month VIP Membership",
                            fontSize = 19.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = if (isGu) "ચૌધરી સમાજના યોગ્ય પાત્રોના વાલી અને ઉમેદવારના સીધા સંપર્ક નંબર મેળવો" else "Connect directly with candidates and parents across the Chaudhary community",
                            fontSize = 12.5.sp,
                            color = SoftGold,
                            textAlign = TextAlign.Center,
                            lineHeight = 17.sp
                        )

                        // Live Subscription Status Pill
                        Spacer(modifier = Modifier.height(14.dp))
                        if (myProfile.isVipSubscribed) {
                            Surface(
                                shape = RoundedCornerShape(20.dp),
                                color = RoyalGold,
                                contentColor = DarkMaroon
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.CheckCircle,
                                        contentDescription = null,
                                        modifier = Modifier.size(16.dp),
                                        tint = DarkMaroon
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = if (isGu) "VIP સક્રિય • માન્યતા: ${myProfile.subscriptionExpiryDate}" else "VIP Active • Exp: ${myProfile.subscriptionExpiryDate}",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 12.sp
                                    )
                                }
                            }
                        } else if (myProfile.subscriptionExpiryDate.isNotBlank()) {
                            Surface(
                                shape = RoundedCornerShape(20.dp),
                                color = Color(0xFFFFCDD2),
                                contentColor = Color(0xFFB71C1C)
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Warning,
                                        contentDescription = null,
                                        modifier = Modifier.size(16.dp),
                                        tint = Color(0xFFB71C1C)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = if (isGu) "સબ્સ્ક્રિપ્શન સમાપ્ત થઈ ગયું (${myProfile.subscriptionExpiryDate})" else "Subscription Expired (${myProfile.subscriptionExpiryDate})",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 12.sp
                                    )
                                }
                            }
                        } else {
                            Surface(
                                shape = RoundedCornerShape(20.dp),
                                color = Color.White.copy(alpha = 0.15f),
                                contentColor = Color.White
                            ) {
                                Text(
                                    text = if (isGu) "ફ્રી ટ્રાએલ મર્યાદિત • VIP પ્લાન ચૂંટો" else "Free Plan • Upgrade to VIP",
                                    fontWeight = FontWeight.SemiBold,
                                    fontSize = 12.sp,
                                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp)
                                )
                            }
                        }
                    }
                }
            }

            // SCHEME 1: FIRST 100 USERS FREE SCHEME CARD
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp)
                    .testTag("free_100_scheme_card"),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                border = androidx.compose.foundation.BorderStroke(2.dp, WarmSaffron),
                elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
            ) {
                Column(modifier = Modifier.fillMaxWidth()) {
                    // Header Bar
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(
                                Brush.horizontalGradient(
                                    colors = listOf(WarmSaffron, RoyalGold)
                                )
                            )
                            .padding(vertical = 8.dp, horizontal = 12.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.CardGiftcard,
                                contentDescription = null,
                                tint = DarkMaroon,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = if (isGu) "🎉 પ્રથમ ૧૦૦ સભ્યો માટે વિશેષ મફત યોજના!" else "🎉 FIRST 100 USERS EARLY BIRD SPECIAL OFFER!",
                                color = DarkMaroon,
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 12.5.sp
                            )
                        }
                    }

                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = if (isGu) "પ્રથમ ૧૦૦ નોંધાયેલા સભ્યો માટે ૩ મહિનાનું VIP સબ્સ્ક્રિપ્શન સંપૂર્ણપણે મફત મેળવો!" else "Get 3 Months VIP Membership 100% FREE for the first 100 registered members!",
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            color = DarkMaroon
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        // Realtime Counter & Progress Bar
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = if (isGu) "લાઇવ નોંધણી પ્રગતિ:" else "Real-time Offer Progress:",
                                fontSize = 12.sp,
                                color = Color.Gray,
                                fontWeight = FontWeight.SemiBold
                            )
                            Text(
                                text = "$freeSchemeClaimedCount / $totalFreeSlots " + (if (isGu) "ક્લેમ થયેલ" else "Claimed"),
                                fontSize = 13.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = RoyalMaroon
                            )
                        }

                        Spacer(modifier = Modifier.height(6.dp))

                        // Progress Bar
                        LinearProgressIndicator(
                            progress = { (freeSchemeClaimedCount.toFloat() / totalFreeSlots.toFloat()).coerceIn(0f, 1f) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(10.dp)
                                .clip(CircleShape),
                            color = RoyalMaroon,
                            trackColor = Color(0xFFEEEEEE),
                        )

                        Spacer(modifier = Modifier.height(6.dp))

                        Text(
                            text = if (remainingFreeSlots > 0) {
                                if (isGu) "હજી $remainingFreeSlots મફત સ્લોટ બાકી છે! તરત જ ક્લેમ કરો." else "$remainingFreeSlots free slots remaining! Claim before offer expires."
                            } else {
                                if (isGu) "બધા ૧૦૦ મફત સ્લોટ ભરાઈ ગયા છે!" else "All 100 free offer slots have been claimed!"
                            },
                            fontSize = 11.5.sp,
                            color = if (remainingFreeSlots > 0) Color(0xFF2E7D32) else Color.Red,
                            fontWeight = FontWeight.Medium
                        )

                        Spacer(modifier = Modifier.height(14.dp))

                        // Claim Button
                        if (myProfile.isFreeSchemeUsed) {
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = Color(0xFFE8F5E9),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(
                                    modifier = Modifier.padding(12.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.CheckCircle,
                                        contentDescription = null,
                                        tint = Color(0xFF2E7D32),
                                        modifier = Modifier.size(20.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = if (isGu) "તમે પ્રથમ ૧૦૦ મફત સ્કીમ ક્લેમ કરી લીધી છે" else "You have claimed the First 100 Free Offer",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 13.sp,
                                        color = Color(0xFF2E7D32)
                                    )
                                }
                            }
                        } else if (isFreeSchemeEligible) {
                            Button(
                                onClick = {
                                    isProcessingFreeClaim = true
                                    viewModel.claimFreeVipScheme { success, msg ->
                                        isProcessingFreeClaim = false
                                        Toast.makeText(context, msg, Toast.LENGTH_LONG).show()
                                    }
                                },
                                enabled = !isProcessingFreeClaim,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(46.dp)
                                    .testTag("claim_free_scheme_btn"),
                                shape = RoundedCornerShape(10.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = DarkMaroon)
                            ) {
                                if (isProcessingFreeClaim) {
                                    CircularProgressIndicator(color = Color.White, modifier = Modifier.size(20.dp))
                                } else {
                                    Icon(
                                        imageVector = Icons.Default.CardGiftcard,
                                        contentDescription = null,
                                        tint = RoyalGold,
                                        modifier = Modifier.size(20.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = if (isGu) "૩ મહિનાનું મફત VIP ક્લેમ કરો (₹૦)" else "Claim 3 Months FREE VIP (₹0)",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 14.sp,
                                        color = RoyalGold
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // SCHEME 2: STANDARD SUBSCRIPTION & RENEWAL CARD
            Text(
                text = if (myProfile.isVipSubscribed) {
                    if (isGu) "તમારું વર્તમાન પ્લાન અને નવીનીકરણ (Renewal):" else "Your Active Plan & Extension/Renewal:"
                } else if (myProfile.subscriptionExpiryDate.isNotBlank()) {
                    if (isGu) "સબ્સ્ક્રિપ્શન નવીનીકરણ કરો (Renew Plan):" else "Renew Expired Subscription:"
                } else {
                    if (isGu) "સબ્સ્ક્રિપ્શન પ્લાન (Subscription Plan):" else "Standard Subscription Plan:"
                },
                fontWeight = FontWeight.Bold,
                fontSize = 15.5.sp,
                color = DarkMaroon,
                modifier = Modifier.padding(vertical = 8.dp)
            )

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp)
                    .testTag("standard_subscription_card"),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                border = androidx.compose.foundation.BorderStroke(1.5.dp, RoyalMaroon),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.Top
                    ) {
                        Column {
                            Text(
                                text = if (isGu) "૩ મહિના VIP સબ્સ્ક્રિપ્શન" else "3 Months VIP Subscription",
                                fontWeight = FontWeight.Bold,
                                fontSize = 18.sp,
                                color = DarkMaroon
                            )
                            Text(
                                text = if (isGu) "૯૦ દિવસની અમર્યાદિત એક્સેસ" else "90 Days Unlimited Validity",
                                fontSize = 12.5.sp,
                                color = Color.Gray
                            )
                        }

                        Column(horizontalAlignment = Alignment.End) {
                            Text(
                                text = "₹$totalPriceRupees",
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 24.sp,
                                color = RoyalMaroon
                            )
                            Text(
                                text = if (isGu) "૧૮% GST સમાવિષ્ટ" else "incl. 18% GST",
                                fontSize = 11.sp,
                                color = Color.Gray
                            )
                        }
                    }

                    HorizontalDivider(
                        modifier = Modifier.padding(vertical = 12.dp),
                        color = Color(0xFFEEEEEE)
                    )

                    // ITEMIZED FEE BREAKDOWN TABLE
                    Surface(
                        color = Color(0xFFFAFAFA),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 12.dp)
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Text(
                                text = if (isGu) "ચુકવણી વિગત (Fee Breakdown):" else "Fee Structure Breakdown:",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = DarkMaroon
                            )
                            Spacer(modifier = Modifier.height(6.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = if (isGu) "સબ્સ્ક્રિપ્શન ફી (૩ મહિના):" else "Subscription Fee (3 Months):",
                                    fontSize = 12.sp,
                                    color = Color.DarkGray
                                )
                                Text(text = "₹$basePriceRupees", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                            }
                            Spacer(modifier = Modifier.height(4.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = if (isGu) "GST (૧૮% ટેક્સ):" else "GST (18% Govt Tax):",
                                    fontSize = 12.sp,
                                    color = Color.DarkGray
                                )
                                Text(text = "₹$gstAmountRupees", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                            }
                            HorizontalDivider(modifier = Modifier.padding(vertical = 6.dp), color = Color(0xFFE0E0E0))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = if (isGu) "કુલ ચૂકવવાપાત્ર રકમ:" else "Total Amount Payable:",
                                    fontSize = 12.5.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = DarkMaroon
                                )
                                Text(
                                    text = "₹$totalPriceRupees",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = RoyalMaroon
                                )
                            }
                        }
                    }

                    // Stored Dates Info (if user previously subscribed)
                    if (myProfile.subscriptionStartDate.isNotBlank() || myProfile.subscriptionExpiryDate.isNotBlank()) {
                        Surface(
                            color = Color(0xFFFFF8E1),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 12.dp)
                        ) {
                            Column(modifier = Modifier.padding(10.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(
                                        text = if (isGu) "શરૂઆત તારીખ:" else "Start Date:",
                                        fontSize = 11.5.sp,
                                        color = Color.DarkGray
                                    )
                                    Text(
                                        text = myProfile.subscriptionStartDate.ifBlank { "N/A" },
                                        fontSize = 11.5.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                                Spacer(modifier = Modifier.height(3.dp))
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(
                                        text = if (isGu) "સમાપ્તિ તારીખ (Expiry):" else "Expiry Date:",
                                        fontSize = 11.5.sp,
                                        color = Color.DarkGray
                                    )
                                    Text(
                                        text = myProfile.subscriptionExpiryDate.ifBlank { "N/A" },
                                        fontSize = 11.5.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (myProfile.isVipSubscribed) Color(0xFF2E7D32) else Color(0xFFC62828)
                                    )
                                }
                                if (myProfile.subscriptionTxnId.isNotBlank()) {
                                    Spacer(modifier = Modifier.height(3.dp))
                                    Text(
                                        text = "Txn ID: ${myProfile.subscriptionTxnId}",
                                        fontSize = 10.5.sp,
                                        color = Color.Gray
                                    )
                                }
                            }
                        }
                    }

                    // Features List
                    val features = if (isGu) listOf(
                        "સંપૂર્ણ સંપર્ક નંબર અને વાલીઓના ફોન નંબર અનલૉક",
                        "સંપૂર્ણ પ્રમાણિત બાયો-ડેટા અને કુટુંબ વિગતો દર્શન",
                        "પસંદગી મુજબ યોગ્ય પાત્રોની શોધ અને ફિલ્ટર્સ",
                        "ચૌધરી સમાજના સભ્યો સાથે ડાયરેક્ટ મેસેજિંગ",
                        "VIP ગોલ્ડન બેજ અને એડમિન પ્રાયોરિટી સપોર્ટ"
                    ) else listOf(
                        "Unlock Full Phone & Parent Contact Details",
                        "View Full Verified Bio-Data & Family Details",
                        "Advanced Community Search & Locality Filters",
                        "Direct In-App Messaging with Community Members",
                        "Verified VIP Crown Badge & Priority Admin Support"
                    )

                    features.forEach { feature ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.CheckCircle,
                                contentDescription = null,
                                tint = RoyalMaroon,
                                modifier = Modifier.size(17.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = feature,
                                fontSize = 12.5.sp,
                                color = Color.DarkGray
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Razorpay Pay / Renew Button
                    Button(
                        onClick = { showCheckoutDialog = true },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                            .testTag("pay_razorpay_590_btn"),
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = RoyalMaroon)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Payment,
                            contentDescription = "Pay",
                            tint = SoftGold,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = if (myProfile.isVipSubscribed) {
                                if (isGu) "સબ્સ્ક્રિપ્શન ૩ મહિના માટે રિન્યૂ કરો (₹૫૯૦)" else "Renew Plan for +3 Months (₹590)"
                            } else if (myProfile.subscriptionExpiryDate.isNotBlank()) {
                                if (isGu) "હવે નવીનીકરણ કરો (Renew for ₹૫૯૦)" else "Renew Expired Plan (₹590)"
                            } else {
                                if (isGu) "Razorpay વડે ₹૫૯૦ ચૂકવો (૩ મહિના)" else "Pay ₹590 via Razorpay (3 Months)"
                            },
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            color = Color.White
                        )
                    }
                }
            }

            // Trust Security Footer Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE0E0E0))
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Lock,
                        contentDescription = "Security",
                        tint = RoyalMaroon,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = if (isGu) "100% સુરક્ષિત Razorpay ચુકવણી ગેટવે" else "100% Secure Razorpay Checkout",
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.5.sp,
                            color = DarkMaroon
                        )
                        Text(
                            text = if (isGu) "UPI (GPay/PhonePe/Paytm), ડેબિટ/ક્રેડિટ કાર્ડ તથા નેટબેંકિંગ સ્વીકાર્ય છે." else "Supports UPI, Cards, NetBanking with 128-bit SSL Encryption.",
                            fontSize = 11.sp,
                            color = Color.Gray
                        )
                    }
                }
            }
        }
    }

    // Razorpay Checkout Modal Dialog for ₹500 + 18% GST = ₹590
    if (showCheckoutDialog) {
        RazorpayCheckoutDialog(
            amountInRupees = totalPriceRupees, // ₹590
            planTitle = "3 Months VIP Plan (₹500 + 18% GST)",
            userName = myProfile.fullName,
            userPhone = myProfile.phoneContact,
            onDismissRequest = {
                showCheckoutDialog = false
                Toast.makeText(context, if (isGu) "ચુકવણી પ્રક્રિયા રદ કરાઈ" else "Payment cancelled", Toast.LENGTH_SHORT).show()
            },
            onPaymentSuccess = { paymentId, orderId, signature ->
                showCheckoutDialog = false

                val isSignatureValid = RazorpayService.verifyPaymentSignature(
                    orderId = orderId,
                    paymentId = paymentId,
                    signature = signature
                )

                if (isSignatureValid) {
                    viewModel.subscribeVipPlan(
                        planName = "3 Months VIP Plan (₹500 + 18% GST)",
                        paymentId = paymentId,
                        orderId = orderId
                    )
                    showPaymentSuccessDialog = Pair(paymentId, "3 Months VIP Subscription")
                } else {
                    showVerificationError = "Payment signature verification failed. Please contact support if debited."
                }
            },
            onPaymentError = { errorMsg ->
                showCheckoutDialog = false
                Toast.makeText(context, "Payment Error: $errorMsg", Toast.LENGTH_LONG).show()
            }
        )
    }

    // Success Confirmation Alert Dialog
    showPaymentSuccessDialog?.let { (paymentId, planTitle) ->
        AlertDialog(
            onDismissRequest = { showPaymentSuccessDialog = null },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.CheckCircle,
                        contentDescription = "Success",
                        tint = Color(0xFF4CAF50),
                        modifier = Modifier.size(28.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = if (isGu) "ચુકવણી સફળ રહી! 🎉" else "Payment Successful! 🎉",
                        fontWeight = FontWeight.Bold,
                        color = DarkMaroon,
                        fontSize = 18.sp
                    )
                }
            },
            text = {
                Column {
                    Text(
                        text = if (isGu) "અભિનંદન! તમારો $planTitle સફળતાપૂર્વક રિન્યૂ/સક્રિય થઈ ગયો છે." else "Congratulations! Your $planTitle has been activated/renewed for 3 months.",
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 13.5.sp,
                        color = Color.DarkGray
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = "Razorpay Txn ID: $paymentId",
                        fontSize = 11.5.sp,
                        color = Color.Gray
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = if (isGu) "સમાપ્તિ તારીખ: ${myProfile.subscriptionExpiryDate}" else "Valid Until: ${myProfile.subscriptionExpiryDate}",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF2E7D32)
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        showPaymentSuccessDialog = null
                        onBackClick()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = RoyalMaroon),
                    modifier = Modifier.testTag("confirm_payment_success_dialog_btn")
                ) {
                    Text("OK (બરાબર છે)")
                }
            }
        )
    }

    // Verification Error Alert
    showVerificationError?.let { err ->
        AlertDialog(
            onDismissRequest = { showVerificationError = null },
            title = {
                Text("Payment Verification Failed", color = MaterialTheme.colorScheme.error, fontWeight = FontWeight.Bold)
            },
            text = {
                Text(err, fontSize = 13.sp, color = Color.DarkGray)
            },
            confirmButton = {
                Button(
                    onClick = { showVerificationError = null },
                    colors = ButtonDefaults.buttonColors(containerColor = RoyalMaroon)
                ) {
                    Text("Close")
                }
            }
        )
    }
}
