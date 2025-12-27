package com.example.odyssey.repository

import android.content.Context
import android.net.Uri

interface CommonRepo {
    fun uploadImage(context: Context, imageUri: Uri, callback: (String?) -> Unit)
    fun getFileNameFromUri(context: Context, uri: Uri): String?
}