package com.aphfiwiwi.biiscoti.ui.screens.restaurants

import android.app.Application
import android.content.Context
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.*
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import androidx.room.*
import com.aphfiwiwi.biiscoti.navigation.ROUT_ADMINBAKERY
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch

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
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(restaurant: Restaurant)

    @Delete
    suspend fun delete(restaurant: Restaurant)

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

        fun getDatabase(context: Context): RestaurantDatabase {
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
class RestaurantViewModel(application: Application) : AndroidViewModel(application) {
    private val dao = RestaurantDatabase.getDatabase(application).restaurantDao()
    val restaurants: Flow<List<Restaurant>> = dao.getAll()

    suspend fun addRestaurant(restaurant: Restaurant) {
        dao.insert(restaurant)
    }

    suspend fun deleteRestaurant(restaurant: Restaurant) {
        dao.delete(restaurant)
    }
}

class RestaurantViewModelFactory(private val application: Application) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(RestaurantViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return RestaurantViewModel(application) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}

// ----------- ADMIN SCREEN UI ------------
@Composable
fun AdminRestaurantScreen(navController: NavHostController) {
    val context = LocalContext.current
    val viewModel: RestaurantViewModel = viewModel(
        factory = RestaurantViewModelFactory(context.applicationContext as Application)
    )
    val coroutineScope = rememberCoroutineScope()

    var name by remember { mutableStateOf("") }
    var price by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }

    val restaurants by viewModel.restaurants.collectAsState(initial = emptyList())

    Column(
        modifier = Modifier
            .padding(16.dp)
            .fillMaxSize()
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text("Admin: Manage Restaurants", style = MaterialTheme.typography.headlineSmall)

            Button(
                onClick = {
                    navController.navigate(ROUT_ADMINBAKERY)
                },
                modifier = Modifier.padding(start = 8.dp)
            ) {
                Text("Go to Orders")
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        TextField(
            value = name,
            onValueChange = { name = it },
            label = { Text("Restaurant Name") },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(8.dp))

        TextField(
            value = price,
            onValueChange = { price = it },
            label = { Text("Price") },
            keyboardOptions = KeyboardOptions.Default.copy(imeAction = ImeAction.Next),
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(8.dp))

        TextField(
            value = description,
            onValueChange = { description = it },
            label = { Text("Description") },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = {
                if (name.isNotBlank() && price.toDoubleOrNull() != null && description.isNotBlank()) {
                    val newRestaurant = Restaurant(
                        name = name,
                        price = price.toDouble(),
                        description = description
                    )
                    coroutineScope.launch {
                        viewModel.addRestaurant(newRestaurant)
                        name = ""
                        price = ""
                        description = ""
                    }
                }
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Add Restaurant")
        }

        Spacer(modifier = Modifier.height(24.dp))

        Text("All Restaurants", style = MaterialTheme.typography.headlineSmall)

        LazyColumn(
            modifier = Modifier.fillMaxSize()
        ) {
            items(restaurants) { restaurant ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 6.dp)
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text("Name: ${restaurant.name}", style = MaterialTheme.typography.titleMedium)
                        Text("Price: ${restaurant.price}", style = MaterialTheme.typography.bodyMedium)
                        Text("Description: ${restaurant.description}", style = MaterialTheme.typography.bodySmall)
                        Spacer(modifier = Modifier.height(8.dp))
                        Button(
                            onClick = {
                                coroutineScope.launch {
                                    viewModel.deleteRestaurant(restaurant)
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                        ) {
                            Text("Delete", color = MaterialTheme.colorScheme.onError)
                        }
                    }
                }
            }
        }
    }
}
