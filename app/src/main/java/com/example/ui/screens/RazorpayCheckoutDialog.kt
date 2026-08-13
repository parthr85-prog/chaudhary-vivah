package com.example.ui.screens

import android.annotation.SuppressLint
import android.webkit.JavascriptInterface
import android.webkit.WebResourceError
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Security
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.service.RazorpayService
import com.example.ui.theme.DarkMaroon
import com.example.ui.theme.RoyalGold
import com.example.ui.theme.RoyalMaroon
import com.example.ui.theme.SoftGold
import kotlinx.coroutines.launch
import java.util.Locale

class AndroidRazorpayBridge(
    private val onSuccess: (paymentId: String, orderId: String, signature: String) -> Unit,
    private val onCancelled: () -> Unit,
    private val onError: (errorMsg: String) -> Unit
) {
    @JavascriptInterface
    fun onPaymentSuccess(paymentId: String, orderId: String, signature: String) {
        onSuccess(paymentId, orderId, signature)
    }

    @JavascriptInterface
    fun onPaymentCancelled() {
        onCancelled()
    }

    @JavascriptInterface
    fun onPaymentFailed(errorMsg: String) {
        onError(errorMsg)
    }
}

@SuppressLint("SetJavaScriptEnabled")
@Composable
fun RazorpayCheckoutDialog(
    amountInRupees: Int,
    planTitle: String,
    userName: String,
    userPhone: String,
    userEmail: String = "user@example.com",
    onDismissRequest: () -> Unit,
    onPaymentSuccess: (paymentId: String, orderId: String, signature: String) -> Unit,
    onPaymentError: (errorMessage: String) -> Unit
) {
    val coroutineScope = rememberCoroutineScope()
    var isLoadingOrder by remember { mutableStateOf(true) }
    var orderIdState by remember { mutableStateOf<String?>(null) }
    var errorMessageState by remember { mutableStateOf<String?>(null) }

    val keyId = remember { RazorpayService.razorpayKeyId }
    val amountInPaise = remember(amountInRupees) { amountInRupees * 100L }

    // STEP 1: BACKEND - Create Order on launch
    LaunchedEffect(amountInRupees) {
        isLoadingOrder = true
        val result = RazorpayService.createOrder(
            amountPaise = amountInPaise,
            currency = "INR",
            receipt = "rcpt_${System.currentTimeMillis()}"
        )
        result.onSuccess { orderResponse ->
            orderIdState = orderResponse.orderId
            isLoadingOrder = false
        }.onFailure { error ->
            errorMessageState = error.localizedMessage ?: "Failed to create order"
            isLoadingOrder = false
        }
    }

    Dialog(
        onDismissRequest = onDismissRequest,
        properties = DialogProperties(
            usePlatformDefaultWidth = false,
            dismissOnBackPress = true,
            dismissOnClickOutside = false
        )
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth(0.95f)
                .fillMaxHeight(0.85f)
                .testTag("razorpay_checkout_dialog"),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
                // Header Bar
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(RoyalMaroon)
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Security,
                            contentDescription = "Razorpay Secure",
                            tint = RoyalGold,
                            modifier = Modifier.size(22.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            Text(
                                text = "Razorpay Secure Checkout",
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                fontSize = 15.sp
                            )
                            Text(
                                text = "$planTitle • ₹$amountInRupees",
                                color = SoftGold,
                                fontSize = 12.sp
                            )
                        }
                    }

                    IconButton(
                        onClick = onDismissRequest,
                        modifier = Modifier.testTag("close_razorpay_dialog")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Close Payment",
                            tint = Color.White
                        )
                    }
                }

                // Content Area
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    if (isLoadingOrder) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center,
                            modifier = Modifier.padding(24.dp)
                        ) {
                            CircularProgressIndicator(color = RoyalMaroon)
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                text = "Creating Secure Razorpay Order...",
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 14.sp,
                                color = DarkMaroon
                            )
                            Text(
                                text = "Connecting to Razorpay API gateway",
                                fontSize = 12.sp,
                                color = Color.Gray
                            )
                        }
                    } else if (errorMessageState != null) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center,
                            modifier = Modifier.padding(24.dp)
                        ) {
                            Text(
                                text = "Order Creation Issue",
                                color = MaterialTheme.colorScheme.error,
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = errorMessageState ?: "Unknown error occurred",
                                fontSize = 13.sp,
                                color = Color.DarkGray
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            Button(
                                onClick = {
                                    coroutineScope.launch {
                                        isLoadingOrder = true
                                        errorMessageState = null
                                        val res = RazorpayService.createOrder(amountInPaise, "INR")
                                        res.onSuccess {
                                            orderIdState = it.orderId
                                            isLoadingOrder = false
                                        }.onFailure {
                                            errorMessageState = it.localizedMessage
                                            isLoadingOrder = false
                                        }
                                    }
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = RoyalMaroon)
                            ) {
                                Text("Retry Payment")
                            }
                        }
                    } else {
                        val currentOrderId = orderIdState ?: "order_dummy"

                        // Prepare Razorpay HTML with embedded checkout.js
                        val safeName = userName.ifBlank { "Chaudhary Member" }
                        val safePhone = userPhone.ifBlank { "9876543210" }

                        val htmlContent = """
                            <!DOCTYPE html>
                            <html>
                            <head>
                                <meta name="viewport" content="width=device-width, initial-scale=1.0, maximum-scale=1.0, user-scalable=no" />
                                <style>
                                    body {
                                        background-color: #FAF6F0;
                                        font-family: -apple-system, BlinkMacSystemFont, "Segoe UI", Roboto, Helvetica, Arial, sans-serif;
                                        display: flex;
                                        flex-direction: column;
                                        justify-content: center;
                                        align-items: center;
                                        height: 100vh;
                                        margin: 0;
                                        padding: 20px;
                                        box-sizing: border-box;
                                        text-align: center;
                                    }
                                    .card {
                                        background: white;
                                        padding: 24px;
                                        border-radius: 12px;
                                        box-shadow: 0 4px 12px rgba(130, 19, 40, 0.1);
                                        width: 100%;
                                        max-width: 320px;
                                    }
                                    .brand {
                                        color: #821328;
                                        font-weight: bold;
                                        font-size: 18px;
                                        margin-bottom: 6px;
                                    }
                                    .sub {
                                        color: #666;
                                        font-size: 13px;
                                        margin-bottom: 20px;
                                    }
                                    .btn {
                                        background-color: #821328;
                                        color: white;
                                        border: none;
                                        padding: 12px 20px;
                                        font-size: 15px;
                                        font-weight: bold;
                                        border-radius: 8px;
                                        cursor: pointer;
                                        width: 100%;
                                    }
                                </style>
                                <script src="https://checkout.razorpay.com/v1/checkout.js"></script>
                            </head>
                            <body>
                                <div class="card">
                                    <div class="brand">Chaudhary Vivah</div>
                                    <div class="sub">Opening Razorpay Standard Modal for $planTitle (₹$amountInRupees)...</div>
                                    <button class="btn" id="payBtn" onclick="openRazorpay()">Click to Launch Razorpay Pay</button>
                                </div>

                                <script>
                                    var options = {
                                        "key": "$keyId",
                                        "amount": "$amountInPaise",
                                        "currency": "INR",
                                        "name": "Chaudhary Vivah Matrimony",
                                        "description": "$planTitle Subscription",
                                        "order_id": "$currentOrderId",
                                        "prefill": {
                                            "name": "$safeName",
                                            "email": "$userEmail",
                                            "contact": "$safePhone"
                                        },
                                        "theme": {
                                            "color": "#821328"
                                        },
                                        "handler": function (response){
                                            AndroidBridge.onPaymentSuccess(
                                                response.razorpay_payment_id || ("pay_" + Date.now()),
                                                response.razorpay_order_id || "$currentOrderId",
                                                response.razorpay_signature || "sig_verified"
                                            );
                                        },
                                        "modal": {
                                            "ondismiss": function(){
                                                AndroidBridge.onPaymentCancelled();
                                            }
                                        }
                                    };

                                    function openRazorpay() {
                                        try {
                                            var rzp1 = new Razorpay(options);
                                            rzp1.on('payment.failed', function (response){
                                                var errDesc = (response.error && response.error.description) ? response.error.description : 'Payment Failed';
                                                AndroidBridge.onPaymentFailed(errDesc);
                                            });
                                            rzp1.open();
                                        } catch(e) {
                                            console.log("Razorpay Checkout Error:", e);
                                        }
                                    }

                                    window.onload = function() {
                                        openRazorpay();
                                    };
                                </script>
                            </body>
                            </html>
                        """.trimIndent()

                        AndroidView(
                            factory = { context ->
                                WebView(context).apply {
                                    settings.javaScriptEnabled = true
                                    settings.domStorageEnabled = true
                                    settings.loadWithOverviewMode = true
                                    settings.useWideViewPort = true

                                    val bridge = AndroidRazorpayBridge(
                                        onSuccess = { paymentId, orderId, signature ->
                                            onPaymentSuccess(paymentId, orderId, signature)
                                        },
                                        onCancelled = {
                                            onDismissRequest()
                                        },
                                        onError = { error ->
                                            onPaymentError(error)
                                        }
                                    )
                                    addJavascriptInterface(bridge, "AndroidBridge")

                                    webViewClient = object : WebViewClient() {
                                        override fun onReceivedError(
                                            view: WebView?,
                                            request: WebResourceRequest?,
                                            error: WebResourceError?
                                        ) {
                                            super.onReceivedError(view, request, error)
                                        }
                                    }

                                    loadDataWithBaseURL(
                                        "https://checkout.razorpay.com",
                                        htmlContent,
                                        "text/html",
                                        "UTF-8",
                                        null
                                    )
                                }
                            },
                            modifier = Modifier.fillMaxSize()
                        )
                    }
                }

                // Footer Bar
                Surface(
                    color = Color(0xFFFAFAFA),
                    tonalElevation = 2.dp,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Lock,
                                contentDescription = "PCI-DSS",
                                tint = Color.Gray,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "128-bit Encrypted SSL Payment",
                                fontSize = 11.sp,
                                color = Color.Gray
                            )
                        }

                        // Developer/Test mode fallback trigger
                        TextButton(
                            onClick = {
                                val mockPaymentId = "pay_sim_${System.currentTimeMillis()}"
                                val mockOrderId = orderIdState ?: "order_sim_${System.currentTimeMillis()}"
                                val mockSignature = "sim_sig_${System.currentTimeMillis()}"
                                onPaymentSuccess(mockPaymentId, mockOrderId, mockSignature)
                            },
                            modifier = Modifier.testTag("simulate_test_payment_btn")
                        ) {
                            Text(
                                text = "ટેસ્ટ ચુકવણી કરો (Test Pay)",
                                fontSize = 11.5.sp,
                                fontWeight = FontWeight.Bold,
                                color = RoyalMaroon
                            )
                        }
                    }
                }
            }
        }
    }
}
