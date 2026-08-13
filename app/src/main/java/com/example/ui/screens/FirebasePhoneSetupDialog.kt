package com.example.ui.screens

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.*
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.ui.theme.RoyalGold
import com.example.ui.theme.RoyalMaroon
import com.example.ui.theme.SurfaceCream

object FirebaseAppCredentials {
    const val PACKAGE_NAME = "com.aistudio.chaudharyvivah.app"
    const val PROJECT_ID = "application-vivah"
    const val PROJECT_NUMBER = "685467333286"
    const val SHA1_FINGERPRINT = "1A:F8:57:AB:21:D2:6E:96:D3:60:D7:96:BB:F8:CA:AC:47:2F:FE:4C"
    const val SHA256_FINGERPRINT = "5D:1C:2C:3D:8B:ED:40:92:0E:A3:D9:A4:79:46:5E:AD:2F:D1:A6:BA:EA:F5:BE:AC:34:73:F2:4B:A5:8B:E3:FB"
    const val FIREBASE_CONSOLE_URL = "https://console.firebase.google.com/project/application-vivah/settings/general/android:com.aistudio.chaudharyvivah.app"
    const val AUTH_SIGN_IN_URL = "https://console.firebase.google.com/project/application-vivah/authentication/providers"
}

@Composable
fun FirebasePhoneSetupDialog(
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val scrollState = rememberScrollState()

    fun copyToClipboard(label: String, text: String) {
        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        val clip = ClipData.newPlainText(label, text)
        clipboard.setPrimaryClip(clip)
        Toast.makeText(context, "$label ક્લિપબોર્ડ પર કોપી થઈ ગયું!", Toast.LENGTH_SHORT).show()
    }

    fun openUrl(url: String) {
        try {
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
            context.startActivity(intent)
        } catch (e: Exception) {
            Toast.makeText(context, "બ્રાઉઝર ઓપન થઈ શક્યું નથી", Toast.LENGTH_SHORT).show()
        }
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth(0.95f)
                .fillMaxHeight(0.90f)
                .padding(8.dp),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(18.dp)
            ) {
                // Header
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.VpnKey,
                            contentDescription = null,
                            tint = RoyalMaroon,
                            modifier = Modifier.size(28.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            Text(
                                text = "Firebase Phone Auth સેટઅપ",
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp,
                                color = RoyalMaroon
                            )
                            Text(
                                text = "Realtime SMS OTP & Play Integrity Setup",
                                fontSize = 11.sp,
                                color = Color.Gray
                            )
                        }
                    }
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "Close", tint = Color.Gray)
                    }
                }

                HorizontalDivider(modifier = Modifier.padding(vertical = 10.dp), color = RoyalGold.copy(alpha = 0.5f))

                Column(
                    modifier = Modifier
                        .weight(1f)
                        .verticalScroll(scrollState),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    // Reason Box
                    Surface(
                        color = Color(0xFFFFF3E0),
                        shape = RoundedCornerShape(10.dp),
                        border = BorderStroke(1.dp, Color(0xFFFFB74D)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Info, contentDescription = null, tint = Color(0xFFE65100), modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "શા માટે 'Play Integrity' એરર આવી?",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp,
                                    color = Color(0xFFE65100)
                                )
                            }
                            Text(
                                text = "Firebase Phone Authentication સુરક્ષા માટે Google Play Integrity / SafetyNet નો ઉપયોગ કરે છે. રીઅલ-ટાઇમ SMS મોકલવા માટે તમારા Firebase Console માં નીચેના SHA-1 અને SHA-256 ફિંગરપ્રિન્ટ ઉમેરવા જરૂરી છે.",
                                fontSize = 11.5.sp,
                                color = Color.DarkGray,
                                lineHeight = 16.sp
                            )
                        }
                    }

                    // SHA-1 Card
                    Card(
                        colors = CardDefaults.cardColors(containerColor = SurfaceCream),
                        shape = RoundedCornerShape(12.dp),
                        border = BorderStroke(1.dp, RoyalGold),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text("૧. SHA-1 Fingerprint (જરૂરી)", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = RoyalMaroon)
                                TextButton(
                                    onClick = { copyToClipboard("SHA-1 Fingerprint", FirebaseAppCredentials.SHA1_FINGERPRINT) },
                                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp)
                                ) {
                                    Icon(Icons.Default.ContentCopy, contentDescription = null, modifier = Modifier.size(14.dp), tint = RoyalMaroon)
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Copy SHA-1", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = RoyalMaroon)
                                }
                            }
                            Surface(
                                color = Color.White,
                                shape = RoundedCornerShape(6.dp),
                                border = BorderStroke(1.dp, Color.LightGray),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text(
                                    text = FirebaseAppCredentials.SHA1_FINGERPRINT,
                                    fontSize = 10.5.sp,
                                    fontFamily = FontFamily.Monospace,
                                    color = Color.Black,
                                    modifier = Modifier.padding(8.dp)
                                )
                            }
                        }
                    }

                    // SHA-256 Card
                    Card(
                        colors = CardDefaults.cardColors(containerColor = SurfaceCream),
                        shape = RoundedCornerShape(12.dp),
                        border = BorderStroke(1.dp, RoyalGold),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text("૨. SHA-256 Fingerprint (Play Integrity)", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = RoyalMaroon)
                                TextButton(
                                    onClick = { copyToClipboard("SHA-256 Fingerprint", FirebaseAppCredentials.SHA256_FINGERPRINT) },
                                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp)
                                ) {
                                    Icon(Icons.Default.ContentCopy, contentDescription = null, modifier = Modifier.size(14.dp), tint = RoyalMaroon)
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Copy SHA-256", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = RoyalMaroon)
                                }
                            }
                            Surface(
                                color = Color.White,
                                shape = RoundedCornerShape(6.dp),
                                border = BorderStroke(1.dp, Color.LightGray),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text(
                                    text = FirebaseAppCredentials.SHA256_FINGERPRINT,
                                    fontSize = 10.5.sp,
                                    fontFamily = FontFamily.Monospace,
                                    color = Color.Black,
                                    modifier = Modifier.padding(8.dp)
                                )
                            }
                        }
                    }

                    // 4-Step Instructions
                    Card(
                        colors = CardDefaults.cardColors(containerColor = Color(0xFFF1F8E9)),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Text("સરળ ૪ સ્ટેપમાં એક્ટિવેટ કરો:", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = Color(0xFF2E7D32))

                            val steps = listOf(
                                "૧. નીચે આપેલ બટન પર ક્લિક કરી Firebase Console ખોલો.",
                                "૨. 'Your apps' સેક્શનમાં Android app (${FirebaseAppCredentials.PACKAGE_NAME}) પસંદ કરો.",
                                "૩. 'Add fingerprint' પર ક્લિક કરીને ઉપર દર્શાવેલ SHA-1 અને SHA-256 સેવ કરો.",
                                "૪. Authentication -> Sign-in method માં 'Phone' પ્રોવાઇડર Enable કરો."
                            )

                            steps.forEach { step ->
                                Text(text = step, fontSize = 11.5.sp, color = Color.DarkGray, lineHeight = 16.sp)
                            }
                        }
                    }

                    // Firebase Console Link Button
                    Button(
                        onClick = { openUrl(FirebaseAppCredentials.FIREBASE_CONSOLE_URL) },
                        colors = ButtonDefaults.buttonColors(containerColor = RoyalMaroon),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(Icons.AutoMirrored.Filled.OpenInNew, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Firebase Console ઓપન કરો", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    }

                    // Test Phone Numbers Option Box
                    Card(
                        colors = CardDefaults.cardColors(containerColor = Color(0xFFEDE7F6)),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.BugReport, contentDescription = null, tint = Color(0xFF512DA8), modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("ઝડપી ટેસ્ટિંગ (Firebase Test Numbers)", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = Color(0xFF512DA8))
                            }
                            Text(
                                text = "તમે Firebase Console -> Authentication -> Sign-in method -> Phone -> 'Phone numbers for testing' માં તમારો મોબાઈલ અને ફિક્સ કોડ (દા.ત. 123456) પણ સેવ કરી શકો છો. આનાથી SMS ખર્ચ વિના તરત જ લોગિન ટેસ્ટ થઈ શકે છે.",
                                fontSize = 11.5.sp,
                                color = Color.DarkGray,
                                lineHeight = 16.sp
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                Button(
                    onClick = onDismiss,
                    colors = ButtonDefaults.buttonColors(containerColor = Color.DarkGray),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("સમજાયું (Close)", color = Color.White, fontSize = 13.sp)
                }
            }
        }
    }
}
