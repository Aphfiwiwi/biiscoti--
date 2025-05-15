package com.aphfiwiwi.biiscoti.ui.screens.thrift

import android.app.Application
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.room.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Face
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Star
import androidx.compose.ui.graphics.Color
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import com.aphfiwiwi.biiscoti.navigation.ROUT_HAIR
import com.aphfiwiwi.biiscoti.navigation.ROUT_HOME
import com.aphfiwiwi.biiscoti.navigation.ROUT_HORTICULTURE
import com.aphfiwiwi.biiscoti.navigation.ROUT_JEWELRY
import com.aphfiwiwi.biiscoti.navigation.ROUT_SEARCH


// 1. Define the Thrift Data Class
@Entity(tableName = "thrift")
data class Thrift(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String,
    val amount: Double,
    val description: String
)

// 2. Define the DAO (Data Access Object)
@Dao
interface ThriftDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(thrift: Thrift)

    @Query("SELECT * FROM thrift")
    fun getAll(): Flow<List<Thrift>>
}

// 3. Define the Database
@Database(entities = [Thrift::class], version = 1)
abstract class ThriftDatabase : RoomDatabase() {
    abstract fun thriftDao(): ThriftDao

    companion object {
        @Volatile
        private var INSTANCE: ThriftDatabase? = null

        fun getDatabase(context: android.content.Context): ThriftDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    ThriftDatabase::class.java,
                    "thrift_db"
                ).build()
                INSTANCE = instance
                instance
            }
        }
    }
}

// 4. Define the ViewModel
class ThriftViewModel(application: Application) : AndroidViewModel(application) {
    private val dao = ThriftDatabase.getDatabase(application).thriftDao()
    val thrifts: Flow<List<Thrift>> = dao.getAll()

    suspend fun addThrift(thrift: Thrift) {
        dao.insert(thrift)
    }
}

// ViewModel Factory
class ThriftViewModelFactory(private val application: Application) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ThriftViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return ThriftViewModel(application) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}

// 5. Thrift Screen UI
@Composable
fun ThriftScreen(navController: NavHostController) {
    val orange = Color(0xFFFF9800)
    val deepOrange = Color(0xFFFF5722)

    val sampleThrifts = listOf(
        Thrift(1, "Chama Women Group", 500.0, "Weekly savings group"),
        Thrift(2, "Youth Empowerment Fund", 1000.0, "Monthly youth thrift contribution"),
        Thrift(3, "Village Support Scheme", 750.0, "Supporting local farmers"),
        Thrift(4, "Church Welfare", 300.0, "Monthly church welfare contributions"),
        Thrift(5, "Business Boost Circle", 1200.0, "Business capital group savings")
    )

    Scaffold(
        bottomBar = {
            BottomNavigationBar(navController, orange, deepOrange)
        },
        containerColor = Color.White
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .padding(16.dp)
                .fillMaxSize()
        ) {
            Text("Available Thrifts", style = MaterialTheme.typography.headlineSmall, color = deepOrange)

            Spacer(modifier = Modifier.height(16.dp))

            LazyColumn {
                items(sampleThrifts) { thrift ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp),
                        colors = CardDefaults.cardColors(containerColor = orange.copy(alpha = 0.1f))
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Text("Name: ${thrift.name}", style = MaterialTheme.typography.titleMedium, color = deepOrange)
                            Text("Amount: Ksh ${thrift.amount}", style = MaterialTheme.typography.bodyMedium)
                            Text("Description: ${thrift.description}", style = MaterialTheme.typography.bodySmall)

                            Spacer(modifier = Modifier.height(8.dp))
                            Button(
                                onClick = { /* Navigate to order confirmation or perform order action */ },
                                colors = ButtonDefaults.buttonColors(containerColor = deepOrange),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text("Join Thrift", color = Color.White)
                            }
                        }
                    }
                }
            }
        }
    }
}
@Composable
fun BottomNavigationBar(navController: NavHostController, orange: Color, deepOrange: Color) {
    NavigationBar(containerColor = orange) {
        NavigationBarItem(
            selected = false,
            onClick = { navController.navigate(ROUT_HOME)  },
            icon = { Icon(Icons.Default.Home, contentDescription = "Home") },
            label = { Text("Home") },
            colors = NavigationBarItemDefaults.colors(indicatorColor = deepOrange)
        )
        NavigationBarItem(
            selected = false,
            onClick = {  navController.navigate(ROUT_SEARCH)  },
            icon = { Icon(Icons.Default.Search, contentDescription = "Search") },
            label = { Text("Search") },
            colors = NavigationBarItemDefaults.colors(indicatorColor = deepOrange)
        )
        NavigationBarItem(
            selected = false,
            onClick = {  navController.navigate(ROUT_HORTICULTURE)  },
            icon = { Icon(Icons.Default.Favorite, contentDescription = "Horticulture") },
            label = { Text("Horticulture") },
            colors = NavigationBarItemDefaults.colors(indicatorColor = deepOrange)
        )
        NavigationBarItem(
            selected = false,
            onClick = {  navController.navigate(ROUT_HAIR)  },
            icon = { Icon(Icons.Default.Face, contentDescription = "Hair") },
            label = { Text("Hair") },
            colors = NavigationBarItemDefaults.colors(indicatorColor = deepOrange)
        )
        NavigationBarItem(
            selected = false,
            onClick = {  navController.navigate(ROUT_JEWELRY)  },
            icon = { Icon(Icons.Default.Star, contentDescription = "Jewelry") },
            label = { Text("Jewelry") },
            colors = NavigationBarItemDefaults.colors(indicatorColor = deepOrange)
        )
    }
}
