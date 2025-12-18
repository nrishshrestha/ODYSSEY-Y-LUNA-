import android.net.Uri
import com.example.odyssey.model.UserProfile
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.tasks.await

class ProfileRepository {


    private val auth: FirebaseAuth = FirebaseAuth.getInstance()

    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()

    private val storage: FirebaseStorage = FirebaseStorage.getInstance()

    suspend fun getUserProfile(): UserProfile? {

        val uid = auth.currentUser?.uid ?: return null

        return try {
            val snapshot = firestore
                .collection("users")
                .document(uid)
                .get()
                .await()

            snapshot.toObject(UserProfile::class.java)
        } catch (e: Exception) {

            e.printStackTrace()
            null
        }
    }


    suspend fun uploadProfileImage(imageUri: Uri): String {
        return try {
            val uid = auth.currentUser?.uid ?: throw IllegalStateException("User not logged in")
            val storageRef = storage.reference.child("profile_pictures/$uid")


            storageRef.putFile(imageUri).await()


            storageRef.downloadUrl.await().toString()
        } catch (e: Exception) {
            e.printStackTrace()
            throw e
        }
    }
}