package com.example.pencatatanbuku

import android.Manifest
import android.content.Context
import android.net.Uri
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.Typography
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.rememberAsyncImagePainter
import com.journeyapps.barcodescanner.ScanContract
import com.journeyapps.barcodescanner.ScanOptions
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileOutputStream
import androidx.compose.material3.CardDefaults // Impor untuk CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.TopAppBarDefaults


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BookInventoryApp(viewModel: BookViewModel) {
    val books by viewModel.allBooks.collectAsState()
    var showAddDialog by remember { mutableStateOf(false) }
    var showEditDialog by remember { mutableStateOf<Book?>(null) }
    var scannedBarcode by remember { mutableStateOf<String?>(null) }
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val barcodeLauncher = androidx.activity.compose.rememberLauncherForActivityResult(ScanContract()) { result ->
        if (result.contents != null) {
            scope.launch {
                val existingBook = viewModel.repository.getBookByBarcode(result.contents)
                if (existingBook == null) {
                    scannedBarcode = result.contents
                    showAddDialog = true
                } else {
                    viewModel.scanAndAddBook(result.contents)
                    Toast.makeText(context, "Stock updated", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    AppTheme {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text("Book Inventory") },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.primary,
                        titleContentColor = MaterialTheme.colorScheme.onPrimary
                    )
                )
            },
            floatingActionButton = {
                FloatingActionButton(
                    onClick = {
                        val options = ScanOptions()
                        options.setDesiredBarcodeFormats(ScanOptions.ALL_CODE_TYPES)
                        options.setPrompt("Scan book barcode")
                        options.setBeepEnabled(true)
                        barcodeLauncher.launch(options)
                    },
                    containerColor = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.shadow(8.dp, shape = CircleShape)
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = "Scan QR Code",
                        tint = Color.White
                    )
                }
            }
        ) { padding ->
            BookList(
                books = books,
                onEdit = { book -> showEditDialog = book },
                onDelete = { book -> viewModel.deleteBook(book) },
                onBorrow = { book -> viewModel.borrowBook(book) },
                onReturn = { book -> viewModel.returnBook(book) },
                modifier = Modifier.padding(padding)
            )
        }

        if (showAddDialog && scannedBarcode != null) {
            AddBookDialog(
                barcode = scannedBarcode!!,
                onDismiss = { showAddDialog = false },
                onAdd = { book -> viewModel.scanAndAddBook(scannedBarcode!!, book) }
            )
        }

        showEditDialog?.let { book ->
            EditBookDialog(
                book = book,
                onDismiss = { showEditDialog = null },
                onUpdate = { updatedBook -> viewModel.updateBook(updatedBook) }
            )
        }
    }
}

@Composable
fun BookList(
    books: List<Book>,
    onEdit: (Book) -> Unit,
    onDelete: (Book) -> Unit,
    onBorrow: (Book) -> Unit,
    onReturn: (Book) -> Unit,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(8.dp)
    ) {
        items(books) { book ->
            BookItem(
                book = book,
                onEdit = onEdit,
                onDelete = onDelete,
                onBorrow = onBorrow,
                onReturn = onReturn
            )
        }
    }
}

@Composable
fun BookItem(
    book: Book,
    onEdit: (Book) -> Unit,
    onDelete: (Book) -> Unit,
    onBorrow: (Book) -> Unit,
    onReturn: (Book) -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp)
            .clickable { },
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Tampilkan gambar buku jika ada
                book.imagePath?.let { path ->
                    Image(
                        painter = rememberAsyncImagePainter(path),
                        contentDescription = "Book Image",
                        modifier = Modifier
                            .size(50.dp)
                            .padding(end = 8.dp)
                    )
                }
                Column {
                    Text(
                        text = book.title,
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = "by ${book.author}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.Gray
                    )
                    Text(
                        text = "ISBN: ${book.isbn}",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.Gray
                    )
                    Text(
                        text = "Year: ${book.publicationYear} | Stock: ${book.stock}",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.Gray
                    )
                }
            }
            Row {
                IconButton(onClick = { onBorrow(book) }) {
                    Icon(
                        imageVector = Icons.Default.Delete, // Ganti ikon jika perlu
                        contentDescription = "Borrow",
                        tint = Color.Blue
                    )
                }
                IconButton(onClick = { onReturn(book) }) {
                    Icon(
                        imageVector = Icons.Default.Add, // Ganti ikon jika perlu
                        contentDescription = "Return",
                        tint = Color.Green
                    )
                }
                IconButton(onClick = { onEdit(book) }) {
                    Icon(
                        imageVector = Icons.Default.Edit,
                        contentDescription = "Edit",
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
                IconButton(onClick = { onDelete(book) }) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Delete",
                        tint = Color.Red
                    )
                }
            }
        }
    }
}

@Composable
fun AddBookDialog(
    barcode: String,
    onDismiss: () -> Unit,
    onAdd: (Book) -> Unit
) {
    var title by remember { mutableStateOf("") }
    var author by remember { mutableStateOf("") }
    var isbn by remember { mutableStateOf("") }
    var year by remember { mutableStateOf("") }
    var imageUri by remember { mutableStateOf<Uri?>(null) }
    val context = LocalContext.current
    val imagePicker = androidx.activity.compose.rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        imageUri = uri
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add New Book", style = MaterialTheme.typography.headlineSmall) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Title") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(8.dp)
                )
                OutlinedTextField(
                    value = author,
                    onValueChange = { author = it },
                    label = { Text("Author") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(8.dp)
                )
                OutlinedTextField(
                    value = isbn,
                    onValueChange = { isbn = it },
                    label = { Text("ISBN") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(8.dp)
                )
                OutlinedTextField(
                    value = year,
                    onValueChange = { year = it },
                    label = { Text("Publication Year") },
                    modifier = Modifier.fillMaxWidth(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    shape = RoundedCornerShape(8.dp)
                )
                TextButton(onClick = { imagePicker.launch("image/*") }) {
                    Text("Upload Book Image")
                }
                imageUri?.let {
                    Image(
                        painter = rememberAsyncImagePainter(it),
                        contentDescription = "Selected Image",
                        modifier = Modifier.size(100.dp)
                    )
                }
            }
        },
        confirmButton = {
            TextButton(onClick = {
                val imagePath = imageUri?.let { uri ->
                    saveImageToInternalStorage(context, uri)
                }
                val book = Book(
                    barcode = barcode,
                    title = title,
                    author = author,
                    isbn = isbn,
                    publicationYear = year.toIntOrNull() ?: 0,
                    imagePath = imagePath
                )
                onAdd(book)
                onDismiss()
            }) {
                Text("Add", color = MaterialTheme.colorScheme.primary)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        },
        shape = RoundedCornerShape(16.dp)
    )
}

@Composable
fun EditBookDialog(
    book: Book,
    onDismiss: () -> Unit,
    onUpdate: (Book) -> Unit
) {
    var title by remember { mutableStateOf(book.title) }
    var author by remember { mutableStateOf(book.author) }
    var isbn by remember { mutableStateOf(book.isbn) }
    var year by remember { mutableStateOf(book.publicationYear.toString()) }
    var stock by remember { mutableStateOf(book.stock.toString()) }
    var imageUri by remember { mutableStateOf<Uri?>(null) }
    val context = LocalContext.current
    val imagePicker = androidx.activity.compose.rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        imageUri = uri
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Edit Book", style = MaterialTheme.typography.headlineSmall) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Title") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(8.dp)
                )
                OutlinedTextField(
                    value = author,
                    onValueChange = { author = it },
                    label = { Text("Author") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(8.dp)
                )
                OutlinedTextField(
                    value = isbn,
                    onValueChange = { isbn = it },
                    label = { Text("ISBN") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(8.dp)
                )
                OutlinedTextField(
                    value = year,
                    onValueChange = { year = it },
                    label = { Text("Year") },
                    modifier = Modifier.fillMaxWidth(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    shape = RoundedCornerShape(8.dp)
                )
                OutlinedTextField(
                    value = stock,
                    onValueChange = { stock = it },
                    label = { Text("Stock") },
                    modifier = Modifier.fillMaxWidth(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    shape = RoundedCornerShape(8.dp)
                )
                TextButton(onClick = { imagePicker.launch("image/*") }) {
                    Text("Change Book Image")
                }
                imageUri?.let {
                    Image(
                        painter = rememberAsyncImagePainter(it),
                        contentDescription = "Selected Image",
                        modifier = Modifier.size(100.dp)
                    )
                } ?: book.imagePath?.let {
                    Image(
                        painter = rememberAsyncImagePainter(it),
                        contentDescription = "Current Image",
                        modifier = Modifier.size(100.dp)
                    )
                }
            }
        },
        confirmButton = {
            TextButton(onClick = {
                val imagePath = imageUri?.let { uri ->
                    saveImageToInternalStorage(context, uri)
                } ?: book.imagePath
                val updatedBook = book.copy(
                    title = title,
                    author = author,
                    isbn = isbn,
                    publicationYear = year.toIntOrNull() ?: book.publicationYear,
                    stock = stock.toIntOrNull() ?: book.stock,
                    imagePath = imagePath
                )
                onUpdate(updatedBook)
                onDismiss()
            }) {
                Text("Update", color = MaterialTheme.colorScheme.primary)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        },
        shape = RoundedCornerShape(16.dp)
    )
}

fun saveImageToInternalStorage(context: Context, uri: Uri): String? {
    return try {
        val inputStream = context.contentResolver.openInputStream(uri)
        val file = File(context.filesDir, "book_${System.currentTimeMillis()}.jpg")
        val outputStream = FileOutputStream(file)
        inputStream?.copyTo(outputStream)
        inputStream?.close()
        outputStream.close()
        file.absolutePath
    } catch (e: Exception) {
        e.printStackTrace()
        null
    }
}

@Composable
fun AppTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = lightColorScheme(
            primary = Color(0xFF6200EE),
            primaryContainer = Color(0xFF3700B3), // Ganti primaryVariant dengan primaryContainer
            secondary = Color(0xFF03DAC6),
            background = Color(0xFFF5F5F5)
        ),
        typography = Typography(
            headlineSmall = TextStyle(fontWeight = FontWeight.Bold, fontSize = 20.sp), // Sesuaikan dengan Material 3
            bodyMedium = TextStyle(fontSize = 14.sp) // Sesuaikan dengan Material 3
        ),
        content = content
    )
}

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val database = BookDatabase.getDatabase(this)
        val repository = BookRepository(database.bookDao())
        val viewModel = BookViewModel(repository)

        val requestPermissionLauncher = registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
            if (isGranted) {
                setContent {
                    BookInventoryApp(viewModel)
                }
            } else {
                Toast.makeText(this, "Camera permission is required to scan QR codes", Toast.LENGTH_LONG).show()
                finish()
            }
        }

        requestPermissionLauncher.launch(Manifest.permission.CAMERA)
    }
}