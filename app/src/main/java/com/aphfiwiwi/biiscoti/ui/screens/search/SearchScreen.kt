package com.aphfiwiwi.biiscoti.ui.screens.search

import android.app.Application
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.ImeAction
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

// Define the Searchable Entity (Example: Restaurant)
@Entity(tableName = "restaurants")
data class Restaurant(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String,
    val description: String,
    val price: Double
)

// Define the DAO for the Searchable Entity
@Dao
interface RestaurantDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(restaurant: Restaurant)

    @Query("SELECT * FROM restaurants WHERE name LIKE :query")
    fun searchByName(query: String): Flow<List<Restaurant>>
}

// Define the Database
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

// Define the ViewModel
class SearchViewModel(application: Application) : AndroidViewModel(application) {
    private val dao = RestaurantDatabase.getDatabase(application).restaurantDao()

    // This function provides a stream of restaurants filtered by name.
    fun searchRestaurants(query: String): Flow<List<Restaurant>> {
        return dao.searchByName("%$query%")
    }
}

// ViewModel Factory
class SearchViewModelFactory(private val application: Application) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(SearchViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return SearchViewModel(application) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}

// Define the Search Screen UI
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchScreen(navController: NavHostController) {
    val context = LocalContext.current
    val viewModel: SearchViewModel = viewModel(factory = SearchViewModelFactory(context.applicationContext as Application))
    val coroutineScope = rememberCoroutineScope()

    var searchQuery by remember { mutableStateOf("") }
    val restaurants by viewModel.searchRestaurants(searchQuery).collectAsState(initial = emptyList())

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Search Restaurants", color = MaterialTheme.colorScheme.onPrimary) },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color(0xFFFF9800)
                )
            )
        },
        containerColor = Color(0xFFFFF3E0) // Light background
    ) { paddingValues ->

        Column(
            modifier = Modifier
                .padding(paddingValues)
                .padding(16.dp)
                .fillMaxSize()
        ) {
            // Styled Search TextField
            TextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                label = { Text("Search by Name") },
                leadingIcon = {
                    Icon(Icons.Filled.Search, contentDescription = "Search Icon")
                },
                keyboardOptions = KeyboardOptions.Default.copy(imeAction = ImeAction.Search),
                keyboardActions = KeyboardActions(onSearch = { /* Optional */ }),
                modifier = Modifier.fillMaxWidth(),

                )



            Spacer(modifier = Modifier.height(16.dp))

            LazyColumn(modifier = Modifier.fillMaxSize()) {
                items(restaurants) { restaurant ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = Color.White,
                            contentColor = Color.Black
                        ),
                        elevation = CardDefaults.cardElevation(4.dp)
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Text("Name: ${restaurant.name}", style = MaterialTheme.typography.titleMedium)
                            Text("Description: ${restaurant.description}", style = MaterialTheme.typography.bodyMedium)
                            Text("Price: \$${restaurant.price}", style = MaterialTheme.typography.bodySmall)
                        }
                    }
                }
            }
        }
    }
}
