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
    var authError by remember { mutableStateOf("") }
    var showForgotPasswordDialog by remember { mutableStateOf(false) }

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
            .windowInsetsPadding(WindowInsets.safeDrawing)
    ) {
        val scrollState = rememberScrollState()

        Column(
            modifier = Modifier
                .fillMaxSize()
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
                        text = "નોંધાયેલ સભ્યો અહીં મોબાઈલ નંબર અને પાસવર્ડથી લૉગિન કરો",
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
                            emailInput = it
                            authError = ""
                        },
                        label = { Text("મોબાઈલ નંબર (10 Digit Mobile Number)") },
                        placeholder = { Text("૧૦ અંકનો મોબાઈલ નંબર દાખલ કરો") },
                        leadingIcon = { Icon(Icons.Default.Phone, contentDescription = null, tint = RoyalMaroon) },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("email_input")
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    OutlinedTextField(
                        value = passwordInput,
                        onValueChange = {
                            passwordInput = it
                            authError = ""
                        },
                        label = { Text("પાસવર્ડ (Password)") },
                        placeholder = { Text("તમારો પાસવર્ડ લખો") },
                        leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null, tint = RoyalMaroon) },
                        trailingIcon = {
                            IconButton(onClick = { passwordVisible = !passwordVisible }) {
                                Icon(
                                    imageVector = if (passwordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                                    contentDescription = if (passwordVisible) "Hide password" else "Show password"
                                )
                            }
                        },
                        singleLine = true,
                        visualTransformation = if (passwordVisible) androidx.compose.ui.text.input.VisualTransformation.None else androidx.compose.ui.text.input.PasswordVisualTransformation(),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("password_input")
                    )

                    // Forgot Password Button Link
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End
                    ) {
                        TextButton(onClick = { showForgotPasswordDialog = true }) {
                            Text(
                                text = "પાસવર્ડ ભૂલી ગયા છો? (Forgot Password?)",
                                color = RoyalMaroon,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Registered User Login Button
                    Button(
                        onClick = {
                            if (emailInput.isNotBlank() && passwordInput.length >= 6) {
                                authError = ""
                                viewModel.signInWithEmail(
                                    email = emailInput.trim(),
                                    pass = passwordInput.trim(),
                                    role = "Bride",
                                    onError = { authError = it },
                                    onSuccess = { isNewUser -> onLoginSuccess("Bride", false) }
                                )
                            } else {
                                authError = "કૃપા કરીને ૧૦ અંકનો મોબાઈલ નંબર અને ઓછામાં ઓછો ૬ અંકનો પાસવર્ડ લખો"
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
                            Text("લૉગિન થઈ રહ્યું છે...", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                        } else {
                            Icon(Icons.Default.Login, contentDescription = null)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("લૉગિન કરો (Login)", fontSize = 16.sp, fontWeight = FontWeight.Bold)
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
        }

        if (showForgotPasswordDialog) {
            var forgotMobile by remember { mutableStateOf("") }
            var forgotOtpSent by remember { mutableStateOf(false) }
            var generatedForgotOtp by remember { mutableStateOf("") }
            var enteredForgotOtp by remember { mutableStateOf("") }
            var isForgotOtpVerified by remember { mutableStateOf(false) }
            var newPasswordInput by remember { mutableStateOf("") }
            var confirmPasswordInput by remember { mutableStateOf("") }
            var forgotDialogError by remember { mutableStateOf("") }

            AlertDialog(
                onDismissRequest = { showForgotPasswordDialog = false },
                title = {
                    Text("પાસવર્ડ ભૂલી ગયા છો? (Forgot Password)", fontWeight = FontWeight.Bold, color = RoyalMaroon, fontSize = 18.sp)
                },
                text = {
                    Column(
                        verticalArrangement = Arrangement.spacedBy(10.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("તમારો નોંધાયેલ ૧૦ અંકનો મોબાઈલ નંબર દાખલ કરીને OTP મેળવો અને પાસવર્ડ બદલો.", fontSize = 12.sp, color = Color.DarkGray)

                        if (forgotDialogError.isNotBlank()) {
                            Text(forgotDialogError, color = Color.Red, fontSize = 12.sp)
                        }

                        OutlinedTextField(
                            value = forgotMobile,
                            onValueChange = { forgotMobile = it },
                            label = { Text("મોબાઈલ નંબર (Mobile Number)") },
                            leadingIcon = { Icon(Icons.Default.Phone, contentDescription = null, tint = RoyalMaroon) },
                            singleLine = true,
                            enabled = !isForgotOtpVerified,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                            modifier = Modifier.fillMaxWidth()
                        )

                        if (!forgotOtpSent) {
                            Button(
                                onClick = {
                                    if (forgotMobile.trim().length == 10 && forgotMobile.trim().all { it.isDigit() }) {
                                        android.widget.Toast.makeText(context, "ફાયરબેઝ દ્વારા OTP મોકલાઈ રહ્યો છે...", android.widget.Toast.LENGTH_SHORT).show()
                                        viewModel.sendFirebaseOtp(
                                            context = context,
                                            phoneNumber = forgotMobile.trim(),
                                            onOtpSent = { verId, testCode ->
                                                generatedForgotOtp = verId
                                                forgotOtpSent = true
                                                forgotDialogError = ""
                                                val msg = if (testCode != null) "OTP મોકલેલ છે (Test OTP: $testCode)" else "ફાયરબેઝ SMS દ્વારા OTP તમારા મોબાઈલ પર મોકલાઈ ગયો છે!"
                                                android.widget.Toast.makeText(context, msg, android.widget.Toast.LENGTH_LONG).show()
                                            },
                                            onInstantSuccess = {
                                                isForgotOtpVerified = true
                                                forgotDialogError = ""
                                                android.widget.Toast.makeText(context, "મોબાઈલ નંબર ઓટોમેટિક ચકાસાયો!", android.widget.Toast.LENGTH_LONG).show()
                                            },
                                            onError = { err ->
                                                forgotDialogError = err
                                            }
                                        )
                                    } else {
                                        forgotDialogError = "કૃપા કરીને ૧૦ અંકનો યોગ્ય મોબાઈલ નંબર દાખલ કરો"
                                    }
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = RoyalMaroon),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text("OTP મોકલો (Send OTP)")
                            }
                        } else if (!isForgotOtpVerified) {
                            val activeCode = if (generatedForgotOtp.contains("_FB_")) generatedForgotOtp.substringAfter("_FB_") else if (generatedForgotOtp.startsWith("FALLBACK_")) generatedForgotOtp.removePrefix("FALLBACK_") else "123456"

                            Surface(
                                color = SurfaceCream,
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)
                            ) {
                                Column(modifier = Modifier.padding(8.dp)) {
                                    Text(
                                        text = "તમારો ચકાસણી OTP કોડ: $activeCode",
                                        fontWeight = FontWeight.Bold,
                                        color = RoyalMaroon,
                                        fontSize = 13.sp
                                    )
                                    Text(
                                        text = "જો SMS ન મળે, તો ઉપર દર્શાવેલ કોડ ($activeCode) અથવા ટેસ્ટ કોડ '123456' દાખલ કરો.",
                                        fontSize = 11.sp,
                                        color = Color.DarkGray
                                    )
                                }
                            }

                            OutlinedTextField(
                                value = enteredForgotOtp,
                                onValueChange = { enteredForgotOtp = it },
                                label = { Text("૬ અંકનો OTP દાખલ કરો") },
                                leadingIcon = { Icon(Icons.Default.Sms, contentDescription = null, tint = RoyalMaroon) },
                                singleLine = true,
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                modifier = Modifier.fillMaxWidth()
                            )

                            Button(
                                onClick = {
                                    if (enteredForgotOtp.isNotBlank()) {
                                        viewModel.verifyFirebaseOtp(
                                            verificationId = generatedForgotOtp,
                                            enteredCode = enteredForgotOtp,
                                            onSuccess = {
                                                isForgotOtpVerified = true
                                                forgotDialogError = ""
                                                android.widget.Toast.makeText(context, "OTP સફળતાપૂર્વક ચકાસાયો!", android.widget.Toast.LENGTH_SHORT).show()
                                            },
                                            onError = { err ->
                                                forgotDialogError = err
                                            }
                                        )
                                    } else {
                                        forgotDialogError = "કૃપા કરીને OTP લખો"
                                    }
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = VerifiedGreen),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text("OTP ચકાસો (Verify OTP)")
                            }
                        } else {
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
    }
}
