package com.example.odyssey.ViewModel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.odyssey.model.UserModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class   ProfileViewModel : ViewModel() {
    private val auth = FirebaseAuth.getInstance()
    private val database = FirebaseDatabase.getInstance().getReference("User")

    // Use a StateFlow to hold the user data, so Composables can observe it
    private val _user = MutableStateFlow<UserModel?>(null)
    val user: StateFlow<UserModel?> = _user

    init {
        fetchCurrentUser()
    }

    private fun fetchCurrentUser() {
        viewModelScope.launch {
            val userId = auth.currentUser?.uid
            if (userId != null) {
                // 2. LOG THAT WE HAVE A USER ID
                Log.d("ProfileViewModel", "User is logged in. Fetching data for UID: $userId")
                try {
                    val snapshot = database.child(userId).get().await()

                    if (snapshot.exists()) { // <-- 3. ADD A CHECK TO SEE IF DATA EXISTS
                        val userModel = snapshot.getValue(UserModel::class.java)
                        if (userModel != null) {
                            _user.value = userModel
                            // 4. LOG SUCCESS
                            Log.d("ProfileViewModel", "Successfully fetched and parsed user data: ${userModel.email}")
                        } else {
                            // 5. LOG PARSING FAILURE
                            Log.e("ProfileViewModel", "Failed to parse snapshot into UserModel. Check data structure.")
                        }
                    } else {
                        // 6. LOG THAT NO DATA WAS FOUND AT THE PATH
                        Log.e("ProfileViewModel", "No data found at database path for user: $userId")
                    }

                } catch (e: Exception) {
                    // 7. LOG ANY OTHER EXCEPTIONS
                    Log.e("ProfileViewModel", "An error occurred while fetching user data.", e)
                    _user.value = null // Or set an error state
                }
            } else {
                // 8. LOG THAT NO USER IS LOGGED IN
                Log.d("ProfileViewModel", "No user is currently logged in. (auth.currentUser is null)")
            }
        }
        }
}