package com.aphfiwiwi.biiscoti.ui.screens.bakery

import android.app.Application
import android.content.Context
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
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

// ------------------ ENTITY ------------------

@Entity(tableName = "bakery_items")
data class BakeryItem(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String,
    val description: String,
    val price: Double,
    val contact: String
)

// ------------------ DAO ------------------

@Dao
interface BakeryDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(item: BakeryItem)

    @Query("SELECT * FROM bakery_items")
    suspend fun getAll(): List<BakeryItem>

    @Delete
    suspend fun delete(item: BakeryItem)
}

// ------------------ DATABASE ------------------

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

class AdminBakeryViewModel(application: Application) : AndroidViewModel(application) {
    private val dao = BakeryDatabase.getDatabase(application).dao()
    private val _items = MutableLiveData<List<BakeryItem>>()
    val items: LiveData<List<BakeryItem>> = _items

    init { loadItems() }

    fun addOrUpdate(item: BakeryItem) {
        viewModelScope.launch {
            dao.insert(item)
            loadItems()
        }
    }

    fun delete(item: BakeryItem) {
        viewModelScope.launch {
            dao.delete(item)
            loadItems()
        }
    }

    private fun loadItems() {
        viewModelScope.launch {
            _items.value = dao.getAll()
        }
    }
}

// ------------------ COMPOSABLE ------------------

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminBakeryScreen(navController: NavHostController) {
    val context = LocalContext.current
    val viewModel: AdminBakeryViewModel = viewModel(
        factory = ViewModelProvider.AndroidViewModelFactory(context.applicationContext as Application)
    )
    val items by viewModel.items.observeAsState(emptyList())

    var name by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var price by remember { mutableStateOf("") }
    var contact by remember { mutableStateOf("") }
    var editingItemId by remember { mutableStateOf<Int?>(null) }
    var errorMessage by remember { mutableStateOf("") }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Admin Bakery Manager") },
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
            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text("Item Name") },
                modifier = Modifier.fillMaxWidth()
            )
            OutlinedTextField(
                value = description,
                onValueChange = { description = it },
                label = { Text("Description") },
                modifier = Modifier.fillMaxWidth()
            )
            OutlinedTextField(
                value = price,
                onValueChange = { price = it },
                label = { Text("Price") },
                modifier = Modifier.fillMaxWidth()
            )
            OutlinedTextField(
                value = contact,
                onValueChange = { contact = it },
                label = { Text("Contact Number") },
                modifier = Modifier.fillMaxWidth()
            )

            if (errorMessage.isNotEmpty()) {
                Text(errorMessage, color = Color.Red, modifier = Modifier.padding(4.dp))
            }

            Button(
                onClick = {
                    if (name.isBlank() || description.isBlank() || price.isBlank() || contact.isBlank()) {
                        errorMessage = "All fields are required."
                    } else {
                        price.toDoubleOrNull()?.let { p ->
                            val item = BakeryItem(
                                id = editingItemId ?: 0,
                                name = name,
                                description = description,
                                price = p,
                                contact = contact
                            )
                            viewModel.addOrUpdate(item)
                            name = ""; description = ""; price = ""; contact = ""
                            editingItemId = null
                            errorMessage = ""
                        } ?: run {
                            errorMessage = "Invalid price format"
                        }
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = Orange),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 12.dp)
            ) {
                Text(if (editingItemId != null) "Update Item" else "Add Item")
            }

            Divider(Modifier.padding(vertical = 16.dp))

            Text("Bakery Items", fontWeight = FontWeight.Bold, fontSize = 18.sp)
            Spacer(Modifier.height(8.dp))

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
                        Text("Price: ${item.price}")
                        Text("Contact: ${item.contact}")

                        Row(modifier = Modifier.padding(top = 8.dp)) {
                            IconButton(onClick = {
                                name = item.name
                                description = item.description
                                price = item.price.toString()
                                contact = item.contact
                                editingItemId = item.id
                            }) {
                                Icon(Icons.Default.Edit, contentDescription = "Edit")
                            }
                            IconButton(onClick = { viewModel.delete(item) }) {
                                Icon(Icons.Default.Delete, contentDescription = "Delete")
                            }
                        }
                    }
                }
            }
        }
    }
}
