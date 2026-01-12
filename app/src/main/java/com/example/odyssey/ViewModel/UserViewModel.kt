package com.example.odyssey.ViewModel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.odyssey.model.FriendModel
import com.example.odyssey.model.UserModel
import com.example.odyssey.repository.FriendRepo
import com.example.odyssey.repository.UserRepo

class UserViewModel (val repository: UserRepo, val friendRepo: FriendRepo? = null) : ViewModel(){
    private val _user = MutableLiveData<UserModel?>()
    val user: LiveData<UserModel?>
        get() = _user

    private val _otherUser = MutableLiveData<UserModel?>()
    val otherUser: LiveData<UserModel?>
        get() = _otherUser

    private val _allUsers = MutableLiveData<List<UserModel?>>()
    val allUsers: LiveData<List<UserModel?>>
        get() = _allUsers

    private val _loading = MutableLiveData<Boolean>()
    val loading: LiveData<Boolean>
        get() = _loading

    private val _isFollowing = MutableLiveData<Boolean>()
    val isFollowing: LiveData<Boolean> = _isFollowing

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

    fun getOtherUserByID(userId:String) {
        repository.getUserByID(userId) {
                success, message, data ->
            if (success) {
                _otherUser.postValue(data)
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

    fun checkFollowingStatus(currentUserId: String, otherUserId: String) {
        friendRepo?.getFriends(currentUserId) { success, _, friends ->
            if (success) {
                val following = friends.any {
                    (it?.fromUserId == currentUserId && it.toUserId == otherUserId) ||
                    (it?.fromUserId == otherUserId && it.toUserId == currentUserId)
                }
                _isFollowing.postValue(following)
            }
        }
    }

    fun followUser(currentUserId: String, otherUserId: String, callback: (Boolean, String) -> Unit) {
        friendRepo?.sendFriendRequest(currentUserId, otherUserId) { success, message ->
            if (success) {
                // For "Follow", we might want to automatically accept it or just mark as followed
                // Here we use the existing friend request logic but treat "accepted" as "followed"
                // To simplify for "Follow", let's just send and then immediately check/update status if needed
                // Or you can create a specific follow function in Repo. 
                // For now, let's just send request and update UI state.
                _isFollowing.postValue(true)
            }
            callback(success, message)
        }
    }
}