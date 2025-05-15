package com.aphfiwiwi.biiscoti.ui.screens.profile

import android.app.Application
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.*
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import androidx.room.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

// 1. Entity
@Entity(tableName = "user_profiles")
data class UserProfile(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String,
    val email: String,
    val phoneNumber: String
)

// 2. DAO
@Dao
interface UserProfileDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(profile: UserProfile)

    @Query("SELECT * FROM user_profiles LIMIT 1")
    fun getProfile(): Flow<UserProfile?>
}

// 3. DB
@Database(entities = [UserProfile::class], version = 1)
abstract class UserProfileDatabase : RoomDatabase() {
    abstract fun userProfileDao(): UserProfileDao

    companion object {
        @Volatile
        private var INSTANCE: UserProfileDatabase? = null

        fun getDatabase(context: android.content.Context): UserProfileDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    UserProfileDatabase::class.java,
                    "user_profile_db"
                ).build()
                INSTANCE = instance
                instance
            }
        }
    }
}

// 4. ViewModel
class UserProfileViewModel(application: Application) : AndroidViewModel(application) {
    private val dao = UserProfileDatabase.getDatabase(application).userProfileDao()
    val profile: Flow<UserProfile?> = dao.getProfile()

    suspend fun saveProfile(profile: UserProfile) {
        dao.insert(profile)
    }
}

// 5. ViewModel Factory
class UserProfileViewModelFactory(private val application: Application) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(UserProfileViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return UserProfileViewModel(application) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}

// 6. UI with Orange Theme
@Composable
fun ProfileScreen(navController: NavHostController) {
    val context = LocalContext.current
    val viewModel: UserProfileViewModel = viewModel(
        factory = UserProfileViewModelFactory(context.applicationContext as Application)
    )
    val coroutineScope = rememberCoroutineScope()

    var name by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var phoneNumber by remember { mutableStateOf("") }

    val orange = Color(0xFFFF9800)
    val deepOrange = Color(0xFFFF5722)

    // Load profile
    LaunchedEffect(Unit) {
        viewModel.profile.collectLatest { userProfile ->
            userProfile?.let {
                name = it.name
                email = it.email
                phoneNumber = it.phoneNumber
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
            .padding(16.dp)
    ) {
        Text(
            text = "User Profile",
            style = MaterialTheme.typography.headlineSmall.copy(color = deepOrange)
        )

        Spacer(modifier = Modifier.height(24.dp))

        // Name
        OutlinedTextField(
            value = name,
            onValueChange = { name = it },
            label = { Text("Name", color = deepOrange) },
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = deepOrange,
                unfocusedBorderColor = orange,
                focusedLabelColor = deepOrange,
                cursorColor = deepOrange
            )
        )

        // Email
        OutlinedTextField(
            value = email,
            onValueChange = { email = it },
            label = { Text("Email", color = deepOrange) },
            keyboardOptions = KeyboardOptions.Default.copy(imeAction = ImeAction.Next),
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = deepOrange,
                unfocusedBorderColor = orange,
                focusedLabelColor = deepOrange,
                cursorColor = deepOrange
            )
        )

        // Phone Number
        OutlinedTextField(
            value = phoneNumber,
            onValueChange = { phoneNumber = it },
            label = { Text("Phone Number", color = deepOrange) },
            keyboardOptions = KeyboardOptions.Default.copy(imeAction = ImeAction.Done),
            keyboardActions = KeyboardActions(onDone = { }),
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = deepOrange,
                unfocusedBorderColor = orange,
                focusedLabelColor = deepOrange,
                cursorColor = deepOrange
            )
        )

        Spacer(modifier = Modifier.height(24.dp))

        // Save Button
        Button(
            onClick = {
                if (name.isNotBlank() && email.isNotBlank() && phoneNumber.isNotBlank()) {
                    val newProfile = UserProfile(name = name, email = email, phoneNumber = phoneNumber)
                    coroutineScope.launch {
                        viewModel.saveProfile(newProfile)
                    }
                }
            },
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(containerColor = deepOrange)
        ) {
            Text("Save Profile", style = MaterialTheme.typography.bodyLarge, color = Color.White)
        }
    }
}

// 7. Preview
@Preview(showBackground = true)
@Composable
fun ProfileScreenPreview() {
    ProfileScreen(rememberNavController())
}
