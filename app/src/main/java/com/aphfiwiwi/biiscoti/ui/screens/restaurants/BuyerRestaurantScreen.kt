package com.aphfiwiwi.biiscoti.ui.screens.buyer

import android.app.Application
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.*
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import androidx.room.*
import com.aphfiwiwi.biiscoti.navigation.ROUT_CONTACT
import kotlinx.coroutines.flow.Flow

// ----------- ENTITY ------------
@Entity(tableName = "restaurants")
data class Restaurant(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String,
    val price: Double,
    val description: String
)

// ----------- DAO ------------
@Dao
interface RestaurantDao {
    @Query("SELECT * FROM restaurants")
    fun getAll(): Flow<List<Restaurant>>
}

// ----------- DATABASE ------------
@Database(entities = [Restaurant::class], version = 1)
abstract class RestaurantDatabase : RoomDatabase() {
    abstract fun restaurantDao(): RestaurantDao

    companion object {
        @Volatile
        private var INSTANCE: RestaurantDatabase? = null

        fun getDatabase(context: android.content.Context): RestaurantDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    RestaurantDatabase::class.java,
                    "restaurant_db"
                ).build()
                INSTANCE = instance
                instance
            }
        }
    }
}

// ----------- VIEWMODEL ------------
class BuyerRestaurantViewModel(application: Application) : AndroidViewModel(application) {
    private val dao = RestaurantDatabase.getDatabase(application).restaurantDao()
    val restaurants: Flow<List<Restaurant>> = dao.getAll()
}

class BuyerRestaurantViewModelFactory(private val application: Application) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(BuyerRestaurantViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return BuyerRestaurantViewModel(application) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}

// ----------- BUYER SCREEN UI ------------
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BuyerRestaurantScreen(navController: NavHostController) {
    val context = LocalContext.current
    val viewModel: BuyerRestaurantViewModel = viewModel(
        factory = BuyerRestaurantViewModelFactory(context.applicationContext as Application)
    )

    val restaurants by viewModel.restaurants.collectAsState(initial = emptyList())

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Available Food Items", color = MaterialTheme.colorScheme.onPrimary) },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color(0xFFFF9800) // Orange tone
                )
            )
        },
        containerColor = Color(0xFFFFF3E0) // Light orange background
    ) { paddingValues ->
        LazyColumn(
            contentPadding = paddingValues,
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            items(restaurants) { restaurant ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 6.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = Color.White,
                        contentColor = Color.Black
                    ),
                    elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text("Name: ${restaurant.name}", style = MaterialTheme.typography.titleMedium)
                        Text("Price: ${restaurant.price}", style = MaterialTheme.typography.bodyMedium)
                        Text("Description: ${restaurant.description}", style = MaterialTheme.typography.bodySmall)
                        Spacer(modifier = Modifier.height(8.dp))
                        Button(
                            onClick = {
                                // TODO: Trigger order screen or add to cart
                                 navController.navigate(ROUT_CONTACT)
                            },
                            modifier = Modifier.fillMaxWidth(),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0xFFFF5722), // Deeper orange
                                contentColor = Color.White
                            )
                        ) {
                            Text("Order")
                        }
                    }
                }
            }
        }
    }
}
