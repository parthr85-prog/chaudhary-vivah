package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.*
import androidx.compose.material.icons.filled.*
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.R
import com.example.ui.theme.*
import com.example.ui.viewmodel.MatrimonyViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AuthScreen(
    viewModel: MatrimonyViewModel,
    onLoginSuccess: (role: String, isNewUser: Boolean) -> Unit,
    onNavigateToAdmin: () -> Unit = {}
) {
    val context = LocalContext.current
    val isAuthenticating by viewModel.isAuthenticating.collectAsState()

    var emailInput by remember { mutableStateOf("") }
    var passwordInput by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    var otpInput by remember { mutableStateOf("") }
    var isOtpSent by remember { mutableStateOf(false) }
    var verificationIdState by remember { mutableStateOf("") }
    var resendingTokenState by remember { mutableStateOf<com.google.firebase.auth.PhoneAuthProvider.ForceResendingToken?>(null) }
    var authError by remember { mutableStateOf("") }
    var showForgotPasswordDialog by remember { mutableStateOf(false) }
    var showNoInternetDialog by remember { mutableStateOf(false) }
    var showNotRegisteredDialog by remember { mutableStateOf(false) }

    val appLanguage by viewModel.appLanguage.collectAsState()
    val strings = remember(appLanguage) { com.example.util.LocaleStrings.getStrings(appLanguage) }

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
            .windowInsetsPadding(WindowInsets.safeDrawing),
        contentAlignment = Alignment.TopCenter
    ) {
        val scrollState = rememberScrollState()

        Column(
            modifier = Modifier
                .fillMaxSize()
                .widthIn(max = 520.dp)
                .navigationBarsPadding()
                .imePadding()
                .verticalScroll(scrollState)
                .padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Top
        ) {
            Spacer(modifier = Modifier.height(10.dp))

            // Maa Arbuda Startup Blessing Card
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("maa_arbuda_startup_card"),
                shape = RoundedCornerShape(22.dp),
                colors = CardDefaults.cardColors(containerColor = SurfaceCream),
                elevation = CardDefaults.cardElevation(defaultElevation = 6.dp),
                border = androidx.compose.foundation.BorderStroke(1.5.dp, RoyalGold)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(76.dp)
                            .clip(RoundedCornerShape(16.dp))
                            .border(2.dp, RoyalGold, RoundedCornerShape(16.dp))
                    ) {
                        AsyncImage(
                            model = R.drawable.img_maa_arbuda_1785298366627,
                            contentDescription = "Maa Arbuda Devi",
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize()
                        )
                    }

                    Spacer(modifier = Modifier.width(14.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "જય મા અર્બુદા દેવી",
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = RoyalMaroon,
                                    fontSize = 17.sp
                                )
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "🌺",
                                fontSize = 14.sp
                            )
                        }

                        Text(
                            text = "કુલદેવી મા અર્બુદાના પાવન આશીર્વાદ",
                            style = MaterialTheme.typography.bodySmall.copy(
                                color = WarmSaffron,
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 12.sp
                            )
                        )

                        Text(
                            text = "ચૌધરી વિવાહ - વિશ્વસનીય વૈવાહિક મિલન",
                            style = MaterialTheme.typography.bodySmall.copy(
                                color = Color.Gray,
                                fontSize = 11.sp
                            ),
                            modifier = Modifier.padding(top = 2.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // App Title Banner
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center,
                modifier = Modifier.fillMaxWidth()
            ) {
                Box(
                    modifier = Modifier
                        .size(46.dp)
                        .clip(CircleShape)
                        .background(RoyalGold.copy(alpha = 0.2f))
                        .border(1.5.dp, RoyalGold, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    AsyncImage(
                        model = R.drawable.img_app_icon_fg_1784990412652,
                        contentDescription = "Logo",
                        modifier = Modifier.size(36.dp)
                    )
                }

                Spacer(modifier = Modifier.width(10.dp))

                Column {
                    Text(
                        text = "ચૌધરી વિવાહ",
                        style = MaterialTheme.typography.headlineMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = RoyalMaroon,
                            fontSize = 24.sp
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Main Auth Container Card
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("auth_card"),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.95f)
                ),
                border = androidx.compose.foundation.BorderStroke(1.dp, BorderLightGold),
                elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Login Header
                    Text(
                        text = "સભ્ય લૉગિન (Member Login)",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = RoyalMaroon
                        ),
                        modifier = Modifier.align(Alignment.Start)
                    )

                    Text(
                        text = if (!isOtpSent) "નોંધાયેલ સભ્યો અહીં મોબાઈલ નંબરથી લૉગિન કરો" else "તમારા મોબાઈલ પર મોકલેલ ૬ અંકનો OTP દાખલ કરો",
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = Color.Gray,
                            fontSize = 12.sp
                        ),
                        modifier = Modifier
                            .align(Alignment.Start)
                            .padding(bottom = 12.dp)
                    )

                    if (authError.isNotBlank()) {
                        Card(
                            colors = CardDefaults.cardColors(containerColor = Color(0xFFFFEBEE)),
                            border = androidx.compose.foundation.BorderStroke(1.dp, Color.Red),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 12.dp)
                        ) {
                            Text(
                                text = authError,
                                color = Color.Red,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Medium,
                                modifier = Modifier.padding(10.dp)
                            )
                        }
                    }

                    OutlinedTextField(
                        value = emailInput,
                        onValueChange = {
                            if (!isOtpSent) {
                                emailInput = it.filter { char -> char.isDigit() }.take(10)
                                authError = ""
                            }
                        },
                        enabled = !isOtpSent && !isAuthenticating,
                        label = { Text("મોબાઈલ નંબર (10 Digit Mobile Number)") },
                        placeholder = { Text("૧૦ અંકનો મોબાઈલ નંબર દાખલ કરો") },
                        leadingIcon = { Icon(Icons.Default.Phone, contentDescription = null, tint = RoyalMaroon) },
                        trailingIcon = {
                            if (isOtpSent) {
                                TextButton(onClick = {
                                    isOtpSent = false
                                    otpInput = ""
                                    verificationIdState = ""
                                    authError = ""
                                }) {
                                    Text("બદલો (Change)", fontSize = 12.sp, color = RoyalMaroon)
                                }
                            }
                        },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("email_input")
                    )

                    if (isOtpSent) {
                        Spacer(modifier = Modifier.height(12.dp))

                        OutlinedTextField(
                            value = otpInput,
                            onValueChange = {
                                otpInput = it.filter { char -> char.isDigit() }.take(6)
                                authError = ""
                            },
                            enabled = !isAuthenticating,
                            label = { Text("૬ અંકનો OTP (Enter 6-digit OTP)") },
                            placeholder = { Text("SMS માં આવેલ OTP લખો") },
                            leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null, tint = RoyalMaroon) },
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("otp_input")
                        )

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.End
                        ) {
                            TextButton(
                                onClick = {
                                    if (emailInput.length == 10 && !isAuthenticating) {
                                        authError = ""
                                        viewModel.sendFirebaseOtp(
                                            context = context,
                                            phoneNumber = emailInput.trim(),
                                            resendingToken = resendingTokenState,
                                            onOtpSent = { vId, resendToken ->
                                                verificationIdState = vId
                                                resendingTokenState = resendToken
                                                android.widget.Toast.makeText(context, "OTP ફરીથી મોકલવામાં આવ્યો છે", android.widget.Toast.LENGTH_SHORT).show()
                                            },
                                            onInstantSuccess = {
                                                viewModel.verifyMobileAndSignInOnServer(
                                                    mobileOrEmail = emailInput.trim(),
                                                    pass = "",
                                                    role = "Bride",
                                                    context = context,
                                                    onError = { authError = it },
                                                    onNoInternet = { showNoInternetDialog = true },
                                                    onNotRegistered = { showNotRegisteredDialog = true },
                                                    onSuccess = { onLoginSuccess("Bride", false) }
                                                )
                                            },
                                            onError = { authError = it }
                                        )
                                    }
                                },
                                enabled = !isAuthenticating
                            ) {
                                Text(
                                    text = "ફરીથી OTP મોકલો (Resend OTP)",
                                    color = RoyalMaroon,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Registered User OTP Login Button
                    Button(
                        onClick = {
                            val cleanMobile = emailInput.trim()
                            if (cleanMobile.length != 10) {
                                authError = "કૃપા કરીને ૧૦ અંકનો સાચો મોબાઈલ નંબર દાખલ કરો."
                                return@Button
                            }

                            if (!isOtpSent) {
                                authError = ""
                                viewModel.sendFirebaseOtp(
                                    context = context,
                                    phoneNumber = cleanMobile,
                                    onOtpSent = { vId, resendToken ->
                                        isOtpSent = true
                                        verificationIdState = vId
                                        resendingTokenState = resendToken
                                    },
                                    onInstantSuccess = {
                                        viewModel.verifyMobileAndSignInOnServer(
                                            mobileOrEmail = cleanMobile,
                                            pass = "",
                                            role = "Bride",
                                            context = context,
                                            onError = { authError = it },
                                            onNoInternet = { showNoInternetDialog = true },
                                            onNotRegistered = { showNotRegisteredDialog = true },
                                            onSuccess = { onLoginSuccess("Bride", false) }
                                        )
                                    },
                                    onError = { authError = it }
                                )
                            } else {
                                if (otpInput.length != 6) {
                                    authError = "કૃપા કરીને ૬ અંકનો સાચો OTP દાખલ કરો."
                                    return@Button
                                }
                                authError = ""
                                viewModel.verifyFirebaseOtp(
                                    verificationId = verificationIdState,
                                    enteredCode = otpInput.trim(),
                                    onSuccess = {
                                        viewModel.verifyMobileAndSignInOnServer(
                                            mobileOrEmail = cleanMobile,
                                            pass = "",
                                            role = "Bride",
                                            context = context,
                                            onError = { authError = it },
                                            onNoInternet = { showNoInternetDialog = true },
                                            onNotRegistered = { showNotRegisteredDialog = true },
                                            onSuccess = { onLoginSuccess("Bride", false) }
                                        )
                                    },
                                    onError = { authError = it }
                                )
                            }
                        },
                        enabled = !isAuthenticating,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp)
                            .testTag("email_login_button"),
                        colors = ButtonDefaults.buttonColors(containerColor = RoyalMaroon),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        if (isAuthenticating) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(22.dp),
                                color = Color.White,
                                strokeWidth = 2.dp
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(if (!isOtpSent) "OTP મોકલાઈ રહ્યો છે..." else "ચકાસણી થઈ રહી છે...", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                        } else {
                            Icon(if (!isOtpSent) Icons.Default.Sms else Icons.AutoMirrored.Filled.Login, contentDescription = null)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(if (!isOtpSent) "OTP મોકલો (Send OTP)" else "ચકાસો અને લૉગિન કરો (Verify & Login)", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Divider with OR
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        HorizontalDivider(modifier = Modifier.weight(1f), color = Color.LightGray)
                        Text(" અથવા / OR ", fontSize = 12.sp, color = Color.Gray, modifier = Modifier.padding(horizontal = 8.dp))
                        HorizontalDivider(modifier = Modifier.weight(1f), color = Color.LightGray)
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // New User Register Button directly BELOW Login Button - Directly navigates to Registration form!
                    OutlinedButton(
                        onClick = {
                            onLoginSuccess("Bride", true) // Directly navigate to Registration Form
                        },
                        enabled = !isAuthenticating,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp)
                            .testTag("new_user_register_button"),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = RoyalMaroon),
                        border = androidx.compose.foundation.BorderStroke(1.5.dp, RoyalGold),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(Icons.Default.PersonAdd, contentDescription = null, tint = RoyalMaroon)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "નવા યુઝર? અહીં નોંધણી (રજીસ્ટ્રેશન) કરો",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = RoyalMaroon
                        )
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    // Security Badge
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(VerifiedGreenContainer, RoundedCornerShape(12.dp))
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.VerifiedUser,
                            contentDescription = null,
                            tint = VerifiedGreen,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "સુરક્ષિત મંચ - સુરક્ષિત મોબાઈલ લૉગિન, OTP ચકાસણી અને એડમિન દ્વારા ચકાસણી.",
                            style = MaterialTheme.typography.bodySmall.copy(
                                color = VerifiedGreen,
                                fontWeight = FontWeight.SemiBold
                            )
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Clickable Customer Support Card
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("login_support_card"),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(
                    containerColor = SurfaceCream
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, RoyalGold)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.SupportAgent,
                            contentDescription = null,
                            tint = RoyalMaroon,
                            modifier = Modifier.size(22.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "મદદ અને સહાયતા (Help & Support)",
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            color = RoyalMaroon
                        )
                    }

                    Text(
                        text = "કોઈપણ સમસ્યા અથવા સપોર્ટ માટે નીચેના ઈમેલ અથવા મોબાઈલ પર ક્લિક કરો:",
                        fontSize = 11.5.sp,
                        color = Color.DarkGray,
                        textAlign = TextAlign.Center
                    )

                    HorizontalDivider(color = RoyalGold.copy(alpha = 0.3f))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Clickable Email Button
                        Card(
                            onClick = {
                                try {
                                    val intent = android.content.Intent(android.content.Intent.ACTION_SENDTO).apply {
                                        data = android.net.Uri.parse("mailto:info@chaudharyvivah.in")
                                    }
                                    context.startActivity(intent)
                                } catch (e: Exception) {
                                    android.widget.Toast.makeText(context, "Support Email: info@chaudharyvivah.in", android.widget.Toast.LENGTH_LONG).show()
                                }
                            },
                            colors = CardDefaults.cardColors(containerColor = Color.White),
                            border = androidx.compose.foundation.BorderStroke(1.dp, RoyalMaroon.copy(alpha = 0.25f)),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .weight(1f)
                                .testTag("support_email_card")
                        ) {
                            Row(
                                modifier = Modifier.padding(vertical = 10.dp, horizontal = 10.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(34.dp)
                                        .clip(CircleShape)
                                        .background(RoyalMaroon.copy(alpha = 0.1f)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Email,
                                        contentDescription = "Email Support",
                                        tint = RoyalMaroon,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.width(8.dp))
                                Column {
                                    Text("ઈમેલ (Email)", fontSize = 10.sp, color = Color.Gray, fontWeight = FontWeight.Medium)
                                    Text("info@chaudharyvivah.in", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = RoyalMaroon)
                                }
                            }
                        }

                        // Clickable WhatsApp Support Button
                        Card(
                            onClick = {
                                try {
                                    val whatsappUrl = "https://wa.me/919016607483"
                                    val intent = android.content.Intent(android.content.Intent.ACTION_VIEW, android.net.Uri.parse(whatsappUrl))
                                    context.startActivity(intent)
                                } catch (e: Exception) {
                                    android.widget.Toast.makeText(context, "WhatsApp Support: 9016607483", android.widget.Toast.LENGTH_LONG).show()
                                }
                            },
                            colors = CardDefaults.cardColors(containerColor = Color.White),
                            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF25D366).copy(alpha = 0.4f)),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .weight(1f)
                                .testTag("support_phone_card")
                        ) {
                            Row(
                                modifier = Modifier.padding(vertical = 10.dp, horizontal = 10.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(34.dp)
                                        .clip(CircleShape)
                                        .background(Color(0xFF25D366).copy(alpha = 0.15f)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Chat,
                                        contentDescription = "WhatsApp Support",
                                        tint = Color(0xFF25D366),
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.width(8.dp))
                                Column {
                                    Text("વોટ્સએપ (WhatsApp)", fontSize = 10.sp, color = Color.Gray, fontWeight = FontWeight.Medium)
                                    Text("9016607483", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = RoyalMaroon)
                                }
                            }
                        }
                    }
                }
            }
        }

        if (showForgotPasswordDialog) {
            var forgotMobile by remember { mutableStateOf("") }
            var forgotOtpSent by remember { mutableStateOf(false) }
            var generatedForgotOtp by remember { mutableStateOf("") }
            var forgotResendToken by remember { mutableStateOf<com.google.firebase.auth.PhoneAuthProvider.ForceResendingToken?>(null) }
            var enteredForgotOtp by remember { mutableStateOf("") }
            var isForgotOtpVerified by remember { mutableStateOf(false) }
            var isSendingForgotOtp by remember { mutableStateOf(false) }
            var isVerifyingForgotOtp by remember { mutableStateOf(false) }
            var forgotTimerSeconds by remember { mutableStateOf(60) }
            var newPasswordInput by remember { mutableStateOf("") }
            var confirmPasswordInput by remember { mutableStateOf("") }
            var forgotDialogError by remember { mutableStateOf("") }

            LaunchedEffect(forgotOtpSent, forgotTimerSeconds) {
                if (forgotOtpSent && !isForgotOtpVerified && forgotTimerSeconds > 0) {
                    kotlinx.coroutines.delay(1000L)
                    forgotTimerSeconds -= 1
                }
            }

            AlertDialog(
                onDismissRequest = { showForgotPasswordDialog = false },
                title = {
                    Text("પાસવર્ડ ભૂલી ગયા છો?", fontWeight = FontWeight.Bold, color = RoyalMaroon, fontSize = 17.sp)
                },
                text = {
                    Column(
                        verticalArrangement = Arrangement.spacedBy(10.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("તમારો નોંધાયેલ ૧૦ અંકનો મોબાઈલ નંબર દાખલ કરો. તમારા નંબર પર SMS દ્વારા સુરક્ષિત OTP મોકલવામાં આવશે.", fontSize = 12.sp, color = Color.DarkGray)

                        if (forgotDialogError.isNotBlank()) {
                            Surface(
                                color = Color(0xFFFFEBEE),
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(modifier = Modifier.padding(8.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                    Text(
                                        text = forgotDialogError,
                                        color = Color(0xFFD32F2F),
                                        fontSize = 12.sp
                                    )
                                }
                            }
                        }

                        OutlinedTextField(
                            value = forgotMobile,
                            onValueChange = {
                                forgotMobile = it
                                if (forgotOtpSent) {
                                    forgotOtpSent = false
                                    isForgotOtpVerified = false
                                    enteredForgotOtp = ""
                                }
                            },
                            label = { Text("૧૦ અંકનો મોબાઈલ નંબર (Mobile Number)") },
                            leadingIcon = { Icon(Icons.Default.Phone, contentDescription = null, tint = RoyalMaroon) },
                            singleLine = true,
                            enabled = !isForgotOtpVerified && !isSendingForgotOtp,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                            modifier = Modifier.fillMaxWidth().testTag("forgot_mobile_input")
                        )

                        if (!forgotOtpSent) {
                            Button(
                                onClick = {
                                    val trimmedMobile = forgotMobile.trim()
                                    if (trimmedMobile.length == 10 && trimmedMobile.all { it.isDigit() }) {
                                        isSendingForgotOtp = true
                                        forgotDialogError = ""
                                        viewModel.checkMobileRegistered(trimmedMobile) { isRegistered ->
                                            if (!isRegistered) {
                                                isSendingForgotOtp = false
                                                forgotDialogError = "આ મોબાઈલ નંબર નોંધાયેલ નથી. કૃપા કરીને પ્રથમ તમારો નોંધાયેલ મોબાઈલ નંબર દાખલ કરો. (This mobile number is not registered. Please enter a registered mobile number.)"
                                            } else {
                                                viewModel.sendFirebaseOtp(
                                                    context = context,
                                                    phoneNumber = trimmedMobile,
                                                    resendingToken = forgotResendToken,
                                                    onOtpSent = { verId, token ->
                                                        generatedForgotOtp = verId
                                                        forgotResendToken = token
                                                        forgotOtpSent = true
                                                        isSendingForgotOtp = false
                                                        forgotTimerSeconds = 60
                                                        forgotDialogError = ""
                                                        android.widget.Toast.makeText(context, "SMS દ્વારા ૬ અંકનો OTP મોકલવામાં આવ્યો છે!", android.widget.Toast.LENGTH_LONG).show()
                                                    },
                                                    onInstantSuccess = {
                                                        isForgotOtpVerified = true
                                                        forgotOtpSent = true
                                                        isSendingForgotOtp = false
                                                        forgotDialogError = ""
                                                        android.widget.Toast.makeText(context, "મોબાઈલ નંબર ઓટોમેટિક ચકાસાયો!", android.widget.Toast.LENGTH_LONG).show()
                                                    },
                                                    onError = { err ->
                                                        isSendingForgotOtp = false
                                                        forgotDialogError = err
                                                    }
                                                )
                                            }
                                        }
                                    } else {
                                        forgotDialogError = "કૃપા કરીને ૧૦ અંકનો યોગ્ય મોબાઈલ નંબર દાખલ કરો"
                                    }
                                },
                                enabled = !isSendingForgotOtp,
                                colors = ButtonDefaults.buttonColors(containerColor = RoyalMaroon),
                                modifier = Modifier.fillMaxWidth().testTag("forgot_send_otp_button")
                            ) {
                                if (isSendingForgotOtp) {
                                    CircularProgressIndicator(modifier = Modifier.size(18.dp), color = Color.White, strokeWidth = 2.dp)
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text("OTP મોકલાઈ રહ્યો છે...")
                                } else {
                                    Icon(Icons.Default.Sms, contentDescription = null, modifier = Modifier.size(18.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("SMS OTP મોકલો (Send Realtime OTP)")
                                }
                            }
                        } else if (!isForgotOtpVerified) {
                            Card(
                                colors = CardDefaults.cardColors(containerColor = SurfaceCream),
                                border = androidx.compose.foundation.BorderStroke(1.dp, RoyalGold),
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(modifier = Modifier.padding(10.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(Icons.Default.MarkEmailRead, contentDescription = null, tint = RoyalMaroon, modifier = Modifier.size(18.dp))
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(
                                            text = "+91 $forgotMobile પર OTP મોકલાયો છે",
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 12.sp,
                                            color = RoyalMaroon
                                        )
                                    }
                                    Text(
                                        text = "તમારા મોબાઈલ પર આવેલ ૬ અંકનો SMS સુરક્ષા કોડ અહીં દાખલ કરો.",
                                        fontSize = 11.sp,
                                        color = Color.DarkGray
                                    )
                                }
                            }

                            OutlinedTextField(
                                value = enteredForgotOtp,
                                onValueChange = { if (it.length <= 6) enteredForgotOtp = it },
                                label = { Text("૬ અંકનો SMS OTP (Enter 6-digit OTP)") },
                                placeholder = { Text("SMS કોડ દાખલ કરો") },
                                leadingIcon = { Icon(Icons.Default.Pin, contentDescription = null, tint = RoyalMaroon) },
                                singleLine = true,
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                modifier = Modifier.fillMaxWidth().testTag("forgot_otp_input")
                            )

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                if (forgotTimerSeconds > 0) {
                                    Text(
                                        text = "ફરીથી મોકલો: ${forgotTimerSeconds}s",
                                        fontSize = 12.sp,
                                        color = Color.Gray
                                    )
                                } else {
                                    TextButton(
                                        onClick = {
                                            isSendingForgotOtp = true
                                            forgotDialogError = ""
                                            viewModel.sendFirebaseOtp(
                                                context = context,
                                                phoneNumber = forgotMobile.trim(),
                                                resendingToken = forgotResendToken,
                                                onOtpSent = { verId, token ->
                                                    generatedForgotOtp = verId
                                                    forgotResendToken = token
                                                    isSendingForgotOtp = false
                                                    forgotTimerSeconds = 60
                                                    android.widget.Toast.makeText(context, "નવો SMS OTP મોકલવામાં આવ્યો છે!", android.widget.Toast.LENGTH_SHORT).show()
                                                },
                                                onInstantSuccess = {
                                                    isForgotOtpVerified = true
                                                    isSendingForgotOtp = false
                                                },
                                                onError = { err ->
                                                    isSendingForgotOtp = false
                                                    forgotDialogError = err
                                                }
                                            )
                                        },
                                        enabled = !isSendingForgotOtp
                                    ) {
                                        Icon(Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(16.dp), tint = RoyalMaroon)
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text("ફરીથી OTP મોકલો (Resend)", fontSize = 12.sp, color = RoyalMaroon, fontWeight = FontWeight.Bold)
                                    }
                                }

                                TextButton(
                                    onClick = {
                                        forgotOtpSent = false
                                        enteredForgotOtp = ""
                                        forgotDialogError = ""
                                    }
                                ) {
                                    Text("નંબર બદલો (Change Number)", fontSize = 12.sp, color = Color.Gray)
                                }
                            }

                            Button(
                                onClick = {
                                    if (enteredForgotOtp.trim().length == 6) {
                                        isVerifyingForgotOtp = true
                                        forgotDialogError = ""
                                        viewModel.verifyFirebaseOtp(
                                            verificationId = generatedForgotOtp,
                                            enteredCode = enteredForgotOtp.trim(),
                                            onSuccess = {
                                                isForgotOtpVerified = true
                                                isVerifyingForgotOtp = false
                                                forgotDialogError = ""
                                                android.widget.Toast.makeText(context, "મોબાઈલ નંબર સફળતાપૂર્વક ચકાસાયો!", android.widget.Toast.LENGTH_SHORT).show()
                                            },
                                            onError = { err ->
                                                isVerifyingForgotOtp = false
                                                forgotDialogError = err
                                            }
                                        )
                                    } else {
                                        forgotDialogError = "કૃપા કરીને SMS માં આવેલ ૬ અંકનો OTP દાખલ કરો"
                                    }
                                },
                                enabled = !isVerifyingForgotOtp,
                                colors = ButtonDefaults.buttonColors(containerColor = VerifiedGreen),
                                modifier = Modifier.fillMaxWidth().testTag("forgot_verify_otp_button")
                            ) {
                                if (isVerifyingForgotOtp) {
                                    CircularProgressIndicator(modifier = Modifier.size(18.dp), color = Color.White, strokeWidth = 2.dp)
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text("ચકાસણી ચાલુ છે...")
                                } else {
                                    Icon(Icons.Default.Verified, contentDescription = null, modifier = Modifier.size(18.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("OTP ચકાસો (Verify OTP)", fontWeight = FontWeight.Bold)
                                }
                            }
                        } else {
                            Surface(
                                color = Color(0xFFE8F5E9),
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(
                                    modifier = Modifier.padding(10.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(Icons.Default.CheckCircle, contentDescription = null, tint = VerifiedGreen, modifier = Modifier.size(20.dp))
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text("મોબાઈલ નંબર ચકાસાયો! હવે નવો પાસવર્ડ સેટ કરો.", color = Color(0xFF2E7D32), fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                }
                            }

                            OutlinedTextField(
                                value = newPasswordInput,
                                onValueChange = { newPasswordInput = it },
                                label = { Text("નવો પાસવર્ડ (New Password)") },
                                leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null, tint = RoyalMaroon) },
                                singleLine = true,
                                visualTransformation = androidx.compose.ui.text.input.PasswordVisualTransformation(),
                                modifier = Modifier.fillMaxWidth()
                            )

                            OutlinedTextField(
                                value = confirmPasswordInput,
                                onValueChange = { confirmPasswordInput = it },
                                label = { Text("પાસવર્ડ કન્ફર્મ કરો (Confirm Password)") },
                                leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null, tint = RoyalMaroon) },
                                singleLine = true,
                                visualTransformation = androidx.compose.ui.text.input.PasswordVisualTransformation(),
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    }
                },
                confirmButton = {
                    if (isForgotOtpVerified) {
                        Button(
                            onClick = {
                                if (newPasswordInput.length >= 6 && newPasswordInput == confirmPasswordInput) {
                                    viewModel.resetPasswordWithMobileOtp(
                                        mobileNumber = forgotMobile.trim(),
                                        newPass = newPasswordInput.trim(),
                                        onSuccess = {
                                            android.widget.Toast.makeText(context, "પાસવર્ડ સફળતાપૂર્વક બદલાઈ ગયો છે! હવે નવા પાસવર્ડથી લૉગિન કરો.", android.widget.Toast.LENGTH_LONG).show()
                                            showForgotPasswordDialog = false
                                        },
                                        onError = { err -> forgotDialogError = err }
                                    )
                                } else if (newPasswordInput.length < 6) {
                                    forgotDialogError = "પાસવર્ડ ઓછામાં ઓછો ૬ અંકનો હોવો જોઈએ"
                                } else {
                                    forgotDialogError = "બંને પાસવર્ડ સરખા હોવા જોઈએ"
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = RoyalMaroon)
                        ) {
                            Text("પાસવર્ડ બદલો (Reset Password)")
                        }
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showForgotPasswordDialog = false }) {
                        Text("રદ કરો (Cancel)", color = Color.Gray)
                    }
                }
            )
        }

        if (showNoInternetDialog) {
            AlertDialog(
                onDismissRequest = { showNoInternetDialog = false },
                icon = { Icon(Icons.Default.WifiOff, contentDescription = null, tint = Color.Red, modifier = Modifier.size(36.dp)) },
                title = { Text("ઇન્ટરનેટ કનેક્શન પ્રોબ્લેમ", fontWeight = FontWeight.Bold, fontSize = 18.sp, color = RoyalMaroon) },
                text = {
                    Text(
                        text = "Internet connection not available\n\nકૃપા કરીને તમારું ઇન્ટરનેટ કનેક્શન તપાસો અને ફરી પ્રયાસ કરો.",
                        fontSize = 15.sp,
                        color = Color.DarkGray
                    )
                },
                confirmButton = {
                    Button(
                        onClick = { showNoInternetDialog = false },
                        colors = ButtonDefaults.buttonColors(containerColor = RoyalMaroon)
                    ) {
                        Text("OK")
                    }
                },
                shape = RoundedCornerShape(16.dp)
            )
        }

        if (showNotRegisteredDialog) {
            AlertDialog(
                onDismissRequest = { showNotRegisteredDialog = false },
                icon = { Icon(Icons.Default.PersonAdd, contentDescription = null, tint = WarmSaffron, modifier = Modifier.size(36.dp)) },
                title = { Text("મોબાઈલ નંબર નોંધાયેલ નથી", fontWeight = FontWeight.Bold, fontSize = 18.sp, color = RoyalMaroon) },
                text = {
                    Text(
                        text = "Please register using New User? Register here button\n\nઆ મોબાઈલ નંબર સિસ્ટમમાં નોંધાયેલ નથી. કૃપા કરીને નવી નોંધણી કરો.",
                        fontSize = 15.sp,
                        color = Color.DarkGray
                    )
                },
                confirmButton = {
                    Button(
                        onClick = {
                            showNotRegisteredDialog = false
                            onLoginSuccess("Bride", true)
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = RoyalMaroon)
                    ) {
                        Text("New User? Register here")
                    }
                },
                dismissButton = {
                    OutlinedButton(
                        onClick = { showNotRegisteredDialog = false }
                    ) {
                        Text("OK / બંધ કરો")
                    }
                },
                shape = RoundedCornerShape(16.dp)
            )
        }
    }
}
