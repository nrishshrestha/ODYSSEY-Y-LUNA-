package com.example.odyssey.utils

import android.content.Context
import android.net.Uri
import android.provider.MediaStore
import android.util.Log
import com.cloudinary.android.MediaManager
import com.cloudinary.android.callback.ErrorInfo
import com.cloudinary.android.callback.UploadCallback
import java.io.File
import java.io.FileOutputStream

object CloudinaryManager {
    private const val TAG = "CloudinaryManager"

    private const val CLOUD_NAME = "dk1890isv"
    private const val UPLOAD_PRESET = "odyssey_profile_upload"
    private const val ROUTE_PHOTO_PRESET = "odyssey_route_photos"

    private fun ensureInit(context: Context) {
        try {
            val config = mutableMapOf<String, String>()
            config["cloud_name"] = CLOUD_NAME

            MediaManager.init(context, config)
            Log.d(TAG, "Cloudinary initialized")
        } catch (e: IllegalStateException) {
            // Already initialized — safe to ignore
            Log.d(TAG, "Cloudinary already initialized")
        } catch (e: Exception) {
            Log.e(TAG, "Cloudinary init failed: ${e.message}", e)
        }
    }


    fun uploadImage(
        context: Context,
        imageUri: Uri,
        onSuccess: (String) -> Unit,
        onError: (Exception) -> Unit = {}
    ) {
        try {
            ensureInit(context)
            val filePath = getFilePathForUpload(context, imageUri)
            MediaManager.get().upload(filePath)
                .unsigned(UPLOAD_PRESET)
                .option("resource_type", "image")
                .callback(object : UploadCallback {
                    override fun onStart(requestId: String?) {
                        Log.d(TAG, "upload start: $requestId")
                    }

                    override fun onProgress(requestId: String?, bytes: Long, totalBytes: Long) {
                    }

                    override fun onSuccess(requestId: String?, resultData: MutableMap<Any?, Any?>?) {
                        val url = (resultData?.get("secure_url") ?: resultData?.get("url")) as? String
                        if (!url.isNullOrEmpty()) {
                            Log.d(TAG, "upload success: $url")
                            onSuccess(url)
                        } else {
                            onError(Exception("Cloudinary returned no URL"))
                        }
                    }

                    override fun onError(requestId: String?, error: ErrorInfo?) {
                        val msg = error?.description ?: "Unknown Cloudinary error"
                        Log.e(TAG, "upload error: $msg")
                        onError(Exception(msg))
                    }

                    override fun onReschedule(requestId: String?, error: ErrorInfo?) {
                        val msg = error?.description ?: "Cloudinary reschedule"
                        Log.w(TAG, "upload rescheduled: $msg")
                        onError(Exception(msg))
                    }
                })
                .dispatch()
        } catch (e: Exception) {
            Log.e(TAG, "uploadImage exception: ${e.message}", e)
            onError(e)
        }
    }

    fun uploadRoutePhoto(
        context: Context,
        imageUri: Uri,
        onProgress: (Int) -> Unit = {},
        onSuccess: (String) -> Unit,
        onError: (Exception) -> Unit = {}
    ) {
        try {
            ensureInit(context)
            val filePath = getFilePathForUpload(context, imageUri)
            MediaManager.get().upload(filePath)
                .unsigned(ROUTE_PHOTO_PRESET) // Use route-specific preset
                .option("resource_type", "image")
                .option("folder", "odyssey/routes") // Organize in folders
                .callback(object : UploadCallback {
                    override fun onStart(requestId: String?) {
                        Log.d(TAG, "Route photo upload start: $requestId")
                        onProgress(0)
                    }

                    override fun onProgress(requestId: String?, bytes: Long, totalBytes: Long) {
                        val progress = ((bytes.toFloat() / totalBytes.toFloat()) * 100).toInt()
                        onProgress(progress)
                        Log.d(TAG, "Upload progress: $progress%")
                    }

                    override fun onSuccess(requestId: String?, resultData: MutableMap<Any?, Any?>?) {
                        val url = (resultData?.get("secure_url") ?: resultData?.get("url")) as? String
                        if (!url.isNullOrEmpty()) {
                            Log.d(TAG, "Route photo upload success: $url")
                            onProgress(100)
                            onSuccess(url)
                        } else {
                            onError(Exception("Cloudinary returned no URL"))
                        }
                    }

                    override fun onError(requestId: String?, error: ErrorInfo?) {
                        val msg = error?.description ?: "Unknown Cloudinary error"
                        Log.e(TAG, "Route photo upload error: $msg")
                        onError(Exception(msg))
                    }

                    override fun onReschedule(requestId: String?, error: ErrorInfo?) {
                        val msg = error?.description ?: "Cloudinary reschedule"
                        Log.w(TAG, "Route photo upload rescheduled: $msg")
                        onError(Exception(msg))
                    }
                })
                .dispatch()
        } catch (e: Exception) {
            Log.e(TAG, "uploadRoutePhoto exception: ${e.message}", e)
            onError(e)
        }
    }

    // Helper: attempt to resolve path; if that fails copy the content into cache and return file path.
    private fun getFilePathForUpload(context: Context, uri: Uri): String {
        // Try to get actual filesystem path (works for many URIs)
        try {
            val projection = arrayOf(MediaStore.Images.Media.DATA)
            context.contentResolver.query(uri, projection, null, null, null)?.use { cursor ->
                val index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA)
                if (cursor.moveToFirst()) {
                    val path = cursor.getString(index)
                    if (!path.isNullOrEmpty()) return path
                }
            }
        } catch (ignore: Exception) { /* fallback to copying */ }

        // Fallback: copy to cache and return its path
        val input = context.contentResolver.openInputStream(uri)
            ?: throw IllegalArgumentException("Unable to open input stream for URI")
        val file = File(context.cacheDir, "odyssey_upload_${System.currentTimeMillis()}.jpg")
        FileOutputStream(file).use { out ->
            input.copyTo(out)
        }
        return file.absolutePath
    }
}