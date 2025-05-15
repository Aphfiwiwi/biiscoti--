package com.aphfiwiwi.biiscoti.ui.screens.bakery.buyer

import android.app.Application
import android.content.Context
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.*
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.room.*
import kotlinx.coroutines.launch

val Orange = Color(0xFFFF9800)

// ------------------ ENTITY (Shared) ------------------

@Entity(tableName = "bakery_items")
data class BakeryItem(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String,
    val description: String,
    val price: Double,
    val contact: String
)

// ------------------ DAO (Shared) ------------------

@Dao
interface BakeryDao {
    @Query("SELECT * FROM bakery_items")
    suspend fun getAll(): List<BakeryItem>
}

// ------------------ DATABASE (Shared) ------------------

@Database(entities = [BakeryItem::class], version = 1)
abstract class BakeryDatabase : RoomDatabase() {
    abstract fun dao(): BakeryDao

    companion object {
        @Volatile private var INSTANCE: BakeryDatabase? = null

        fun getDatabase(context: Context): BakeryDatabase {
            return INSTANCE ?: synchronized(this) {
                Room.databaseBuilder(
                    context.applicationContext,
                    BakeryDatabase::class.java,
                    "bakery_db"
                ).build().also { INSTANCE = it }
            }
        }
    }
}

// ------------------ VIEWMODEL ------------------

class BuyerBakeryViewModel(application: Application) : AndroidViewModel(application) {
    private val dao = BakeryDatabase.getDatabase(application).dao()
    private val _items = MutableLiveData<List<BakeryItem>>()
    val items: LiveData<List<BakeryItem>> = _items

    init { loadItems() }

    private fun loadItems() {
        viewModelScope.launch {
            _items.value = dao.getAll()
        }
    }
}

// ------------------ COMPOSABLE ------------------

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BuyerBakeryScreen(navController: NavHostController) {
    val context = LocalContext.current
    val viewModel: BuyerBakeryViewModel = viewModel(
        factory = ViewModelProvider.AndroidViewModelFactory(context.applicationContext as Application)
    )
    val items by viewModel.items.observeAsState(emptyList())

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Bakery Menu") },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Orange, titleContentColor = Color.White)
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .padding(16.dp)
                .verticalScroll(rememberScrollState())
        ) {
            Text("Available Bakery Items", fontWeight = FontWeight.Bold, fontSize = 20.sp)
            Spacer(Modifier.height(8.dp))

            if (items.isEmpty()) {
                Text("No items available.", color = Color.Gray)
            }

            items.forEach { item ->
                Card(
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFFFF3E0)),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp)
                ) {
                    Column(Modifier.padding(12.dp)) {
                        Text("Name: ${item.name}", fontWeight = FontWeight.Bold)
                        Text("Description: ${item.description}")
                        Text("Price: ${item.price} KES")
                        Text("Contact: ${item.contact}", color = Color.Gray)
                    }
                }
            }
        }
    }
}
