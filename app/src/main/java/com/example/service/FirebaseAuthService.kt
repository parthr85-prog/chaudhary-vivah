package com.example.service

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import android.util.Log
import androidx.credentials.CredentialManager
import androidx.credentials.GetCredentialRequest
import androidx.credentials.exceptions.GetCredentialCancellationException
import androidx.credentials.exceptions.GetCredentialException
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GetSignInWithGoogleOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.firebase.FirebaseException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.PhoneAuthCredential
import com.google.firebase.auth.PhoneAuthOptions
import com.google.firebase.auth.PhoneAuthProvider
import java.util.concurrent.TimeUnit
import kotlinx.coroutines.tasks.await

object FirebaseAuthService {

    private val auth: FirebaseAuth?
        get() = try {
            FirebaseAuth.getInstance()
        } catch (e: Exception) {
            Log.e("FirebaseAuthService", "FirebaseAuth instance unavailable", e)
            null
        }

    val currentUser: FirebaseUser?
        get() = try {
            auth?.currentUser
        } catch (e: Exception) {
            Log.e("FirebaseAuthService", "Error accessing currentUser", e)
            null
        }

    /**
     * Send real-time OTP via Firebase Phone Authentication.
     */
    fun sendPhoneOtp(
        context: Context,
        phoneNumber: String,
        callbacks: PhoneAuthProvider.OnVerificationStateChangedCallbacks
    ) {
        val firebaseAuth = auth
        if (firebaseAuth == null) {
            callbacks.onVerificationFailed(FirebaseException("FirebaseAuth instance is unavailable on this device"))
            return
        }
        val activity = findActivity(context) ?: (context as? Activity)
        if (activity == null) {
            callbacks.onVerificationFailed(FirebaseException("Activity context required for Firebase Phone Auth"))
            return
        }
        val formattedNumber = if (phoneNumber.startsWith("+")) phoneNumber else "+91$phoneNumber"
        val options = PhoneAuthOptions.newBuilder(firebaseAuth)
            .setPhoneNumber(formattedNumber)
            .setTimeout(60L, TimeUnit.SECONDS)
            .setActivity(activity)
            .setCallbacks(callbacks)
            .build()
        PhoneAuthProvider.verifyPhoneNumber(options)
    }

    /**
     * Verify SMS OTP code entered by user with Firebase.
     */
    suspend fun verifyOtpAndSignIn(verificationId: String, code: String): Result<FirebaseUser> {
        val firebaseAuth = auth ?: return Result.failure(Exception("FirebaseAuth instance unavailable"))
        return try {
            val credential = PhoneAuthProvider.getCredential(verificationId, code)
            val authResult = firebaseAuth.signInWithCredential(credential).await()
            val user = authResult.user
            if (user != null) {
                Result.success(user)
            } else {
                Result.failure(Exception("Firebase Phone Auth returned null user"))
            }
        } catch (e: Exception) {
            Log.e("FirebaseAuthService", "Error verifying OTP credential", e)
            Result.failure(e)
        }
    }

    /**
     * Complete sign in when instant verification succeeds.
     */
    suspend fun signInWithPhoneCredential(credential: PhoneAuthCredential): Result<FirebaseUser> {
        val firebaseAuth = auth ?: return Result.failure(Exception("FirebaseAuth instance unavailable"))
        return try {
            val authResult = firebaseAuth.signInWithCredential(credential).await()
            val user = authResult.user
            if (user != null) {
                Result.success(user)
            } else {
                Result.failure(Exception("Firebase Phone Auth returned null user"))
            }
        } catch (e: Exception) {
            Log.e("FirebaseAuthService", "Error signing in with PhoneAuthCredential", e)
            Result.failure(e)
        }
    }

    private fun findActivity(context: Context): Activity? {
        var ctx = context
        while (ctx is ContextWrapper) {
            if (ctx is Activity) return ctx
            ctx = ctx.baseContext
        }
        return null
    }

    suspend fun signInWithGoogle(context: Context): Result<FirebaseUser> {
        val firebaseAuth = auth ?: return Result.failure(Exception("FirebaseAuth instance unavailable"))
        return try {
            val activity = findActivity(context) ?: (context as? Activity)
            val activityContext = activity ?: context

            val credentialManager = CredentialManager.create(activityContext)
            
            // Web Client ID from Firebase project application-vivah
            val webClientId = "685467333286-web.apps.googleusercontent.com"

            val signInWithGoogleOption = GetSignInWithGoogleOption.Builder(webClientId)
                .build()

            val googleIdOption = GetGoogleIdOption.Builder()
                .setFilterByAuthorizedAccounts(false)
                .setServerClientId(webClientId)
                .setAutoSelectEnabled(false)
                .build()

            val request = GetCredentialRequest.Builder()
                .addCredentialOption(signInWithGoogleOption)
                .addCredentialOption(googleIdOption)
                .build()

            val response = credentialManager.getCredential(activityContext, request)
            val credential = response.credential

            if (credential is GoogleIdTokenCredential) {
                val idToken = credential.idToken
                val authCredential = GoogleAuthProvider.getCredential(idToken, null)
                val authResult = firebaseAuth.signInWithCredential(authCredential).await()
                val user = authResult.user
                if (user != null) {
                    Result.success(user)
                } else {
                    Result.failure(Exception("ગૂગલ યુઝર ફાયરબેઝ ઓથ સાથે કનેક્ટ થયો નથી"))
                }
            } else {
                Result.failure(Exception("અમાન્ય ગૂગલ આઇડેન્ટિટી ક્રેડેન્શિયલ"))
            }
        } catch (e: GetCredentialCancellationException) {
            Log.i("FirebaseAuthService", "User cancelled Google Sign-In sheet")
            Result.failure(Exception("ગૂગલ લૉગિન રદ કરવામાં આવ્યું છે (Sign-In canceled)"))
        } catch (e: GetCredentialException) {
            Log.w("FirebaseAuthService", "Credential Manager exception: ${e.type} - ${e.message}")
            val msg = e.localizedMessage ?: e.message ?: ""
            if (msg.contains("cancel", ignoreCase = true)) {
                Result.failure(Exception("ગૂગલ લૉગિન રદ કરાયું (Sign-In canceled)"))
            } else {
                Result.failure(Exception("ગૂગલ ઓથેન્ટિકેશન ઉપલબ્ધ નથી અથવા સેટઅપ જરૂરી છે. કૃપા કરીને નીચે 'ઈમેલ સાથે લૉગિન' નો ઉપયોગ કરો."))
            }
        } catch (e: Exception) {
            Log.e("FirebaseAuthService", "Firebase Auth Exception", e)
            Result.failure(e)
        }
    }

    /**
     * Firebase Email Authentication: Sign in or register directly with real Email ID.
     */
    suspend fun signUpWithEmail(email: String, pass: String): Result<FirebaseUser> {
        val firebaseAuth = auth ?: return Result.failure(Exception("FirebaseAuth instance unavailable"))
        return try {
            val createResult = firebaseAuth.createUserWithEmailAndPassword(email, pass).await()
            val user = createResult.user
            if (user != null) {
                Result.success(user)
            } else {
                Result.failure(Exception("નવું એકાઉન્ટ બનાવવામાં નિષ્ફળતા"))
            }
        } catch (e: Exception) {
            // If account already exists in Firebase Auth, attempt sign-in with same credentials
            try {
                val signInResult = firebaseAuth.signInWithEmailAndPassword(email, pass).await()
                val user = signInResult.user
                if (user != null) {
                    Result.success(user)
                } else {
                    Result.failure(e)
                }
            } catch (signInEx: Exception) {
                Result.failure(e)
            }
        }
    }

    suspend fun signInWithEmail(email: String, pass: String): Result<FirebaseUser> {
        val firebaseAuth = auth ?: return Result.failure(Exception("FirebaseAuth instance unavailable"))
        return try {
            val authResult = firebaseAuth.signInWithEmailAndPassword(email, pass).await()
            val user = authResult.user
            if (user != null) {
                Result.success(user)
            } else {
                Result.failure(Exception("email not registered. Kindly apply for New Registration"))
            }
        } catch (e: Exception) {
            Log.e("FirebaseAuthService", "Sign in with email failed for $email", e)
            Result.failure(Exception("email not registered. Kindly apply for New Registration"))
        }
    }

    fun signOut() {
        try {
            auth?.signOut()
        } catch (e: Exception) {
            Log.e("FirebaseAuthService", "Error during signOut", e)
        }
    }
}
