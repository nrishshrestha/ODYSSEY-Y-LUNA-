package com.example.odyssey.ViewModel

import android.content.Context
import android.net.Uri
import com.example.odyssey.repository.CommonRepo

class CommonViewModel(val repository: CommonRepo) {
    fun uploadImage(context: Context,imageUri: Uri,callback: (String?) -> Unit){
        repository.uploadImage(context,imageUri,callback)
    }
}