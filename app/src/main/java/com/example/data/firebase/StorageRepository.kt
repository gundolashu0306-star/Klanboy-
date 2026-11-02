package com.example.data.firebase

import android.content.Context
import com.example.core.common.Result
import com.example.core.error.AppError
import com.example.core.logging.AppLogger
import com.google.firebase.Firebase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.storage
import kotlinx.coroutines.tasks.await

/**
 * Storage repository contract for student proof-of-registration documents,
 * profile photos, accommodation photos, and marketplace uploads.
 */
interface StorageRepository {
    suspend fun uploadStudentDocument(
        userId: String,
        fileName: String,
        bytes: ByteArray,
        mimeType: String
    ): Result<String>

    suspend fun uploadProfilePhoto(
        userId: String,
        fileName: String,
        bytes: ByteArray
    ): Result<String>

    suspend fun uploadMarketplaceImage(
        sellerId: String,
        fileName: String,
        bytes: ByteArray
    ): Result<String>

    suspend fun uploadAccommodationImage(
        landlordId: String,
        fileName: String,
        bytes: ByteArray
    ): Result<String>

    suspend fun uploadFeedImage(
        userId: String,
        fileName: String,
        bytes: ByteArray
    ): Result<String>

    fun isConfigured(): Boolean
}

class FirebaseStorageRepository(private val context: Context) : StorageRepository {

    private val storage: FirebaseStorage
        get() = Firebase.storage

    override fun isConfigured(): Boolean {
        return try {
            storage.app != null
        } catch (e: Exception) {
            false
        }
    }

    override suspend fun uploadProfilePhoto(
        userId: String,
        fileName: String,
        bytes: ByteArray
    ): Result<String> {
        val path = "profile_photos/$userId/$fileName"
        return uploadBytes(path, bytes)
    }

    override suspend fun uploadStudentDocument(
        userId: String,
        fileName: String,
        bytes: ByteArray,
        mimeType: String
    ): Result<String> {
        val path = "student_verifications/$userId/$fileName"
        return uploadBytes(path, bytes)
    }

    override suspend fun uploadMarketplaceImage(
        sellerId: String,
        fileName: String,
        bytes: ByteArray
    ): Result<String> {
        val path = "marketplace/$sellerId/$fileName"
        return uploadBytes(path, bytes)
    }

    override suspend fun uploadAccommodationImage(
        landlordId: String,
        fileName: String,
        bytes: ByteArray
    ): Result<String> {
        val path = "accommodations/$landlordId/$fileName"
        return uploadBytes(path, bytes)
    }

    override suspend fun uploadFeedImage(
        userId: String,
        fileName: String,
        bytes: ByteArray
    ): Result<String> {
        val path = "feed_images/$userId/$fileName"
        return uploadBytes(path, bytes)
    }

    private suspend fun uploadBytes(path: String, bytes: ByteArray): Result<String> {
        if (!isConfigured()) {
            return Result.Error(
                AppError.Configuration(
                    userMessage = "Firebase Storage is not configured for this project. Please configure Firebase Storage in Firebase Console.",
                    missingService = "Firebase Storage Bucket"
                )
            )
        }

        return try {
            val ref = storage.reference.child(path)
            ref.putBytes(bytes).await()
            val downloadUrl = ref.downloadUrl.await().toString()
            AppLogger.i("File uploaded successfully to: $path -> $downloadUrl")
            Result.Success(downloadUrl)
        } catch (e: Exception) {
            AppLogger.e("Storage upload error at $path: ${e.message}", e)
            Result.Error(
                AppError.Storage(
                    userMessage = "Failed to upload file. Please check connection and retry.",
                    operation = "UPLOAD",
                    path = path
                )
            )
        }
    }
}
