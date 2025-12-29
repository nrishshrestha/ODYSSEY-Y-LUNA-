package com.example.odyssey.ViewModel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.odyssey.model.UserModel
import com.example.odyssey.repository.UserRepo

class UserViewModel (val repository: UserRepo) : ViewModel(){
    private val _user = MutableLiveData<UserModel?>()
    val user: LiveData<UserModel?>
        get() = _user

    private val _allUsers = MutableLiveData<List<UserModel?>>()
    val allUsers: LiveData<List<UserModel?>>
        get() = _allUsers

    private val _loading = MutableLiveData<Boolean>()
    val loading: LiveData<Boolean>
        get() = _loading

    fun login(email:String,password:String,callback:(Boolean,String) -> Unit) {
        repository.login(email,password,callback)
    }

    fun register(email:String,password:String,callback:(Boolean,String,String) -> Unit) {
        repository.register(email,password,callback)
    }

    fun forgetPassword(email: String,callback: (Boolean, String) -> Unit) {
        repository.forgetPassword(email,callback)
    }

    fun addUserToDatabase(userId:String, model: UserModel, callback: (Boolean, String) -> Unit) {
        repository.addUserToDatabase(userId,model,callback)
    }

    fun getUserByID(userId:String) {
        repository.getUserByID(userId) {
                success, message, data ->
            if (success) {
                _user.postValue(data)
            }
        }
    }

    fun getAllUser() {
        _loading.postValue(true)
        repository.getAllUser { success, message, data ->
            if (success) {
                _allUsers.postValue(data)
                _loading.postValue(false)
            }
            _loading.postValue(false)
        }
    }

    fun editProfile(userId: String,model: UserModel,callback: (Boolean, String) -> Unit) {
        repository.editProfile(userId,model,callback)
    }

    fun deleteAccount(userId: String,callback: (Boolean, String) -> Unit) {
        repository.deleteAccount(userId,callback)
    }
}