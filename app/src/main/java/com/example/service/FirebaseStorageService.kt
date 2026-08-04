package com.example.service

import android.net.Uri
import android.util.Log
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.tasks.await

object FirebaseStorageService {
    private val storage: FirebaseStorage by lazy { FirebaseStorage.getInstance() }

    /**
     * Upload profile picture Uri to Firebase Storage under profile_photos/{profileId}.jpg
     * and return the download URL string.
     */
    suspend fun uploadProfileImage(profileId: String, imageUri: Uri): String? {
        return try {
            val ref = storage.reference.child("profile_photos/$profileId.jpg")
            ref.putFile(imageUri).await()
            val downloadUrl = ref.downloadUrl.await().toString()
            Log.d("FirebaseStorageService", "Successfully uploaded image: $downloadUrl")
            downloadUrl
        } catch (e: Exception) {
            Log.e("FirebaseStorageService", "Firebase Storage upload error, using local uri", e)
            imageUri.toString()
        }
    }

    /**
     * Upload Aadhar front/back photo Uri to Firebase Storage under aadhar_documents/{profileId}_{side}.jpg
     */
    suspend fun uploadAadharImage(profileId: String, side: String, imageUri: Uri): String? {
        return try {
            val ref = storage.reference.child("aadhar_documents/${profileId}_${side}.jpg")
            ref.putFile(imageUri).await()
            val downloadUrl = ref.downloadUrl.await().toString()
            Log.d("FirebaseStorageService", "Successfully uploaded Aadhar $side: $downloadUrl")
            downloadUrl
        } catch (e: Exception) {
            Log.e("FirebaseStorageService", "Firebase Storage upload error for Aadhar $side, using local uri", e)
            imageUri.toString()
        }
    }
}
