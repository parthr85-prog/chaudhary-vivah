package com.example.service

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.util.Log
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageMetadata
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import java.io.ByteArrayOutputStream
import java.util.UUID
import kotlin.coroutines.resume

object FirebaseStorageService {
    private val storage: FirebaseStorage by lazy { FirebaseStorage.getInstance() }

    /**
     * Compress an image from Uri to optimized JPEG bytes (max size 1200px, 82% quality)
     */
    suspend fun compressImageUri(
        context: Context,
        imageUri: Uri,
        maxDimension: Int = 1200,
        quality: Int = 82
    ): ByteArray = withContext(Dispatchers.IO) {
        try {
            val inputStream = context.contentResolver.openInputStream(imageUri)
            val originalBitmap = BitmapFactory.decodeStream(inputStream)
            inputStream?.close()

            if (originalBitmap == null) {
                return@withContext context.contentResolver.openInputStream(imageUri)?.use { it.readBytes() } ?: ByteArray(0)
            }

            val width = originalBitmap.width
            val height = originalBitmap.height
            val scale = if (width > maxDimension || height > maxDimension) {
                val max = maxOf(width, height)
                maxDimension.toFloat() / max
            } else 1f

            val scaledBitmap = if (scale < 1f) {
                Bitmap.createScaledBitmap(originalBitmap, (width * scale).toInt(), (height * scale).toInt(), true)
            } else {
                originalBitmap
            }

            val outputStream = ByteArrayOutputStream()
            scaledBitmap.compress(Bitmap.CompressFormat.JPEG, quality, outputStream)
            val bytes = outputStream.toByteArray()
            if (scaledBitmap != originalBitmap) {
                scaledBitmap.recycle()
            }
            originalBitmap.recycle()
            bytes
        } catch (e: Exception) {
            Log.e("FirebaseStorageService", "Error compressing image, reading raw stream", e)
            context.contentResolver.openInputStream(imageUri)?.use { it.readBytes() } ?: ByteArray(0)
        }
    }

    /**
     * Upload profile picture with real-time percentage progress callback (0..100%)
     * and unique immutable storage path to prevent cache collisions across users.
     */
    suspend fun uploadProfileImageWithProgress(
        context: Context,
        profileId: String,
        imageUri: Uri,
        onProgress: (Int) -> Unit = {}
    ): String? = withContext(Dispatchers.IO) {
        try {
            onProgress(5)
            val compressedBytes = compressImageUri(context, imageUri, maxDimension = 1080, quality = 82)
            if (compressedBytes.isEmpty()) {
                Log.e("FirebaseStorageService", "Compressed image bytes are empty")
                return@withContext null
            }
            onProgress(15)

            val cleanId = profileId.ifBlank { "usr_${System.currentTimeMillis()}" }.replace("/", "_")
            val timestamp = System.currentTimeMillis()
            val randomSuffix = UUID.randomUUID().toString().take(6)
            val ref = storage.reference.child("profile_photos/${cleanId}_${timestamp}_${randomSuffix}.jpg")

            val metadata = StorageMetadata.Builder()
                .setContentType("image/jpeg")
                .setCustomMetadata("uploadedAt", timestamp.toString())
                .setCustomMetadata("profileId", cleanId)
                .build()

            val uploadTask = ref.putBytes(compressedBytes, metadata)
            uploadTask.addOnProgressListener { snapshot ->
                val total = snapshot.totalByteCount
                val transferred = snapshot.bytesTransferred
                if (total > 0) {
                    val pct = 15 + ((transferred.toDouble() / total) * 80).toInt()
                    onProgress(pct.coerceIn(15, 95))
                }
            }

            suspendCancellableCoroutine { continuation ->
                uploadTask.addOnSuccessListener {
                    ref.downloadUrl.addOnSuccessListener { downloadUri ->
                        val url = downloadUri.toString()
                        onProgress(100)
                        Log.d("FirebaseStorageService", "Successfully uploaded profile photo: $url")
                        continuation.resume(url)
                    }.addOnFailureListener { downloadErr ->
                        Log.e("FirebaseStorageService", "Failed to retrieve download URL", downloadErr)
                        continuation.resume(null)
                    }
                }.addOnFailureListener { uploadErr ->
                    Log.e("FirebaseStorageService", "Upload task failed", uploadErr)
                    continuation.resume(null)
                }

                continuation.invokeOnCancellation {
                    try { uploadTask.cancel() } catch (e: Exception) {}
                }
            }
        } catch (e: Exception) {
            Log.e("FirebaseStorageService", "Firebase Storage upload error", e)
            null
        }
    }

    /**
     * Upload Aadhar front/back photo with real-time percentage progress callback (0..100%)
     */
    suspend fun uploadAadharImageWithProgress(
        context: Context,
        profileId: String,
        side: String,
        imageUri: Uri,
        onProgress: (Int) -> Unit = {}
    ): String? = withContext(Dispatchers.IO) {
        try {
            onProgress(5)
            val compressedBytes = compressImageUri(context, imageUri, maxDimension = 1280, quality = 85)
            if (compressedBytes.isEmpty()) {
                Log.e("FirebaseStorageService", "Compressed Aadhar bytes are empty")
                return@withContext null
            }
            onProgress(15)

            val cleanId = profileId.ifBlank { "usr_${System.currentTimeMillis()}" }.replace("/", "_")
            val timestamp = System.currentTimeMillis()
            val randomSuffix = UUID.randomUUID().toString().take(6)
            val ref = storage.reference.child("aadhar_documents/${cleanId}_${side}_${timestamp}_${randomSuffix}.jpg")

            val metadata = StorageMetadata.Builder()
                .setContentType("image/jpeg")
                .setCustomMetadata("uploadedAt", timestamp.toString())
                .setCustomMetadata("profileId", cleanId)
                .setCustomMetadata("side", side)
                .build()

            val uploadTask = ref.putBytes(compressedBytes, metadata)
            uploadTask.addOnProgressListener { snapshot ->
                val total = snapshot.totalByteCount
                val transferred = snapshot.bytesTransferred
                if (total > 0) {
                    val pct = 15 + ((transferred.toDouble() / total) * 80).toInt()
                    onProgress(pct.coerceIn(15, 95))
                }
            }

            suspendCancellableCoroutine { continuation ->
                uploadTask.addOnSuccessListener {
                    ref.downloadUrl.addOnSuccessListener { downloadUri ->
                        val url = downloadUri.toString()
                        onProgress(100)
                        Log.d("FirebaseStorageService", "Successfully uploaded Aadhar $side: $url")
                        continuation.resume(url)
                    }.addOnFailureListener { downloadErr ->
                        Log.e("FirebaseStorageService", "Failed to retrieve Aadhar download URL", downloadErr)
                        continuation.resume(null)
                    }
                }.addOnFailureListener { uploadErr ->
                    Log.e("FirebaseStorageService", "Aadhar upload task failed", uploadErr)
                    continuation.resume(null)
                }

                continuation.invokeOnCancellation {
                    try { uploadTask.cancel() } catch (e: Exception) {}
                }
            }
        } catch (e: Exception) {
            Log.e("FirebaseStorageService", "Firebase Storage Aadhar upload error", e)
            null
        }
    }
}
