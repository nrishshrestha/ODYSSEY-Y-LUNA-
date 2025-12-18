import android.net.Uri
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch


data class ProfileUiState(
    val imageUri: Uri? = null,
    val isLoading: Boolean = false,
    val errorMessage: String? = null
)

class ProfileViewModel : ViewModel() {

    private val profileRepository: ProfileRepository = ProfileRepository()


    var uiState by mutableStateOf(ProfileUiState())
        private set


    fun onImageSelected(newUri: Uri?) {
        uiState = uiState.copy(imageUri = newUri)

        if (newUri != null) {
            uploadProfilePicture(newUri)
        }
    }


    private fun uploadProfilePicture(uri: Uri) {
        viewModelScope.launch {
            uiState = uiState.copy(isLoading = true, errorMessage = null)
            try {

                val downloadUrl = profileRepository.uploadProfileImage(uri)


                uiState = uiState.copy(isLoading = false)

            } catch (e: Exception) {
                uiState = uiState.copy(
                    isLoading = false,
                    errorMessage = e.message ?: "An unknown error occurred"
                )
            }
        }
    }
}