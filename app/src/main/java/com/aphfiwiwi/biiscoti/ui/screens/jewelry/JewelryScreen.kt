package com.aphfiwiwi.biiscoti.ui.screens.jewelry

import android.app.Application
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.room.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

// 1. Define the JewelryService Entity
@Entity(tableName = "jewelry_services")
data class JewelryService(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String,
    val price: Double,
    val description: String
)

// 2. Define DAO (Data Access Object)
@Dao
interface JewelryDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(service: JewelryService)

    @Query("SELECT * FROM jewelry_services")
    fun getAll(): Flow<List<JewelryService>>
}

// 3. Define the Jewelry Database
@Database(entities = [JewelryService::class], version = 1)
abstract class JewelryDatabase : RoomDatabase() {
    abstract fun jewelryDao(): JewelryDao

    companion object {
        @Volatile
        private var INSTANCE: JewelryDatabase? = null

        fun getDatabase(context: android.content.Context): JewelryDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    JewelryDatabase::class.java,
                    "jewelry_db"
                ).build()
                INSTANCE = instance
                instance
            }
        }
    }
}

// 4. ViewModel for managing data
class JewelryViewModel(application: Application) : AndroidViewModel(application) {
    private val dao = JewelryDatabase.getDatabase(application).jewelryDao()
    val services: Flow<List<JewelryService>> = dao.getAll()

    suspend fun addService(service: JewelryService) {
        dao.insert(service)
    }
}

// ViewModel Factory
class JewelryViewModelFactory(private val application: Application) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(JewelryViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return JewelryViewModel(application) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}

// 5. Define the Jewelry Screen UI
@Composable
fun JewelryScreen(navController: androidx.navigation.NavController) {
    val context = LocalContext.current
    val viewModel: JewelryViewModel = viewModel(
        factory = JewelryViewModelFactory(context.applicationContext as Application)
    )
    val coroutineScope = rememberCoroutineScope()

    var services by remember { mutableStateOf(emptyList<JewelryService>()) }

    // Observe the data from the database
    LaunchedEffect(Unit) {
        viewModel.services.collectLatest {
            services = it
        }
    }

    Column(
        modifier = Modifier
            .padding(16.dp)
            .fillMaxSize()
            .background(Color(0xFFFF9800)) // Orange background
    ) {
        Text(
            "Jewelry Services",
            style = MaterialTheme.typography.headlineSmall.copy(
                color = Color.White // White header text
            )
        )

        Spacer(modifier = Modifier.height(24.dp))

        // Display the list of jewelry services for buyers to view only
        LazyColumn {
            items(services) { service ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = Color.White // White background for cards
                    )
                ) {
                    Column(modifier = Modifier.padding(8.dp)) {
                        Text(
                            "Name: ${service.name}",
                            style = MaterialTheme.typography.titleMedium.copy(color = Color.Black)
                        )
                        Text(
                            "Price: \$${service.price}",
                            style = MaterialTheme.typography.bodyMedium.copy(color = Color.Black)
                        )
                        Text(
                            "Description: ${service.description}",
                            style = MaterialTheme.typography.bodySmall.copy(color = Color.Black)
                        )
                    }
                }
            }
        }
    }
}

@Composable
@Preview(showBackground = true)
fun JewelryScreenPreview() {
    JewelryScreen(navController = androidx.navigation.compose.rememberNavController())
}
