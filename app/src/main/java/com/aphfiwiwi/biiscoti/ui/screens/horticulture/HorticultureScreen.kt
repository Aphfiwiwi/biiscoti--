package com.aphfiwiwi.biiscoti.ui.screens.horticulture

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
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import androidx.room.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

@Entity(tableName = "horticulture_services")
data class HorticultureService(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String,
    val price: Double,
    val description: String
)

@Dao
interface HorticultureDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(service: HorticultureService)

    @Query("SELECT * FROM horticulture_services")
    fun getAll(): Flow<List<HorticultureService>>
}

@Database(entities = [HorticultureService::class], version = 1)
abstract class HorticultureDatabase : RoomDatabase() {
    abstract fun horticultureDao(): HorticultureDao

    companion object {
        @Volatile
        private var INSTANCE: HorticultureDatabase? = null

        fun getDatabase(context: android.content.Context): HorticultureDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    HorticultureDatabase::class.java,
                    "horticulture_db"
                ).build()
                INSTANCE = instance
                instance
            }
        }
    }
}

class HorticultureViewModel(application: Application) : AndroidViewModel(application) {
    private val dao = HorticultureDatabase.getDatabase(application).horticultureDao()
    val services: Flow<List<HorticultureService>> = dao.getAll()

    suspend fun addService(service: HorticultureService) {
        dao.insert(service)
    }
}

class HorticultureViewModelFactory(private val application: Application) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(HorticultureViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return HorticultureViewModel(application) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}

@Composable
fun HorticultureScreen(navController: NavHostController) {
    val context = LocalContext.current
    val viewModel: HorticultureViewModel = viewModel(
        factory = HorticultureViewModelFactory(context.applicationContext as Application)
    )

    var services by remember { mutableStateOf(emptyList<HorticultureService>()) }

    LaunchedEffect(Unit) {
        // Sample hardcoded services for testing
        services = listOf(
            HorticultureService(name = "Lawn Mowing", price = 20.0, description = "A professional lawn mowing service for your garden."),
            HorticultureService(name = "Tree Pruning", price = 25.0, description = "Expert pruning services to keep your trees healthy."),
            HorticultureService(name = "Garden Fertilization", price = 30.0, description = "Nutrient-rich fertilizers to boost garden growth."),
            HorticultureService(name = "Planting Shrubs", price = 15.0, description = "Planting a variety of beautiful shrubs to enhance your garden."),
            HorticultureService(name = "Irrigation Installation", price = 50.0, description = "Installing efficient irrigation systems for your garden.")
        )
    }

    Column(modifier = Modifier
        .padding(16.dp)
        .fillMaxSize()
        .background(Color(0xFFFF9800)) // Applying the orange background color
    ) {
        Text(
            text = "Available Horticulture Services",
            style = MaterialTheme.typography.headlineSmall,
            color = Color.White
        )

        Spacer(modifier = Modifier.height(24.dp))

        // Display the list of horticulture services for buyers to view only
        LazyColumn {
            items(services) { service ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp)
                        .background(MaterialTheme.colorScheme.surface) // Optional: customize card background
                ) {
                    Column(modifier = Modifier.padding(8.dp)) {
                        Text("Name: ${service.name}", style = MaterialTheme.typography.titleMedium, color = Color.Black)
                        Text("Price: \$${service.price}", style = MaterialTheme.typography.bodyMedium, color = Color.Black)
                        Text("Description: ${service.description}", style = MaterialTheme.typography.bodySmall, color = Color.Black)
                    }
                }
            }
        }
    }
}

@Composable
@Preview(showBackground = true)
fun HorticultureScreenPreview() {
    HorticultureScreen(rememberNavController())
}
