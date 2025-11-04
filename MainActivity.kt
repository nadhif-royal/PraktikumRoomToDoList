package com.example.praktikumroom

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.praktikumroom.ui.theme.PraktikumRoomTheme

class MainActivity : ComponentActivity() {
    private val database by lazy { TaskDatabase.getDatabase(application) }
    private val repository by lazy { TaskRepository(database.taskDao()) }
    private val viewModelFactory by lazy { TaskViewModelFactory(repository) }
    private val viewModel: TaskViewModel by viewModels { viewModelFactory }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            PraktikumRoomTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    val tasks = viewModel.allTasks.collectAsState(initial = emptyList()).value
                    TaskScreen(
                        tasks = tasks,
                        onAddTask = { title -> viewModel.addNewTask(title) },
                        onUpdateTask = { task, completed ->
                            viewModel.updateTaskStatus(task, completed)
                        },
                        onDeleteTask = { task -> viewModel.deleteTask(task) },
                        // MODIFIKASI: Teruskan fungsi edit dari viewmodel
                        onEditTask = { task, newTitle ->
                            viewModel.updateTaskTitle(task, newTitle)
                        }
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TaskScreen(
    tasks: List<Task>,
    onAddTask: (String) -> Unit,
    onUpdateTask: (Task, Boolean) -> Unit,
    onDeleteTask: (Task) -> Unit,
    onEditTask: (Task, String) -> Unit
) {
    var showEditDialog by remember { mutableStateOf(false) }
    var taskToEdit by remember { mutableStateOf<Task?>(null) }

    Scaffold(
        topBar = { TopAppBar(title = { Text("Daftar Tugas (Room Compose)") }) }
    ) { paddingValues ->
        Column(modifier = Modifier.padding(paddingValues).fillMaxSize()) {
            TaskInput(onAddTask)
            Spacer(modifier = Modifier.height(8.dp))
            TaskList(
                tasks = tasks,
                onUpdateTask = onUpdateTask,
                onDeleteTask = onDeleteTask,
                onEditTask = { task ->
                    taskToEdit = task
                    showEditDialog = true
                }
            )
        }

        if (showEditDialog && taskToEdit != null) {
            EditTaskDialog(
                task = taskToEdit!!,
                onDismiss = {
                    showEditDialog = false
                    taskToEdit = null
                },
                onConfirm = { newTitle ->
                    onEditTask(taskToEdit!!, newTitle)
                    showEditDialog = false
                    taskToEdit = null
                }
            )
        }
    }
}

@Composable
fun TaskInput(onAddTask: (String) -> Unit) {
    var text by remember { mutableStateOf("") }
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        OutlinedTextField(
            value = text,
            onValueChange = { text = it },
            label = { Text("Tugas Baru") },
            modifier = Modifier.weight(1f)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Button(
            onClick = {
                if (text.isNotBlank()) {
                    onAddTask(text)
                    text = ""
                }
            },
            enabled = text.isNotBlank()
        ) {
            Icon(Icons.Filled.Add, contentDescription = "Tambah Tugas")
        }
    }
}

@Composable
fun TaskList(
    tasks: List<Task>,
    onUpdateTask: (Task, Boolean) -> Unit,
    onDeleteTask: (Task) -> Unit,
    onEditTask: (Task) -> Unit
) {
    LazyColumn(contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp)) {
        items(tasks, key = { it.id }) { task ->
            TaskItem(
                task = task,
                onCheckedChange = { isChecked -> onUpdateTask(task, isChecked) },
                onDelete = { onDeleteTask(task) },
                onEdit = { onEditTask(task) }
            )
            Divider()
        }
    }
}

@Composable
fun TaskItem(
    task: Task,
    onCheckedChange: (Boolean) -> Unit,
    onDelete: () -> Unit,
    onEdit: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Checkbox(
            checked = task.isCompleted,
            onCheckedChange = onCheckedChange,
        )
        Spacer(Modifier.width(8.dp))
        Text(
            text = task.title,
            style = MaterialTheme.typography.bodyLarge,
            color = if (task.isCompleted) MaterialTheme.colorScheme.onSurfaceVariant
            else MaterialTheme.colorScheme.onSurface,
            modifier = Modifier
                .weight(1f)
                .clickable { onEdit() }
        )
        IconButton(onClick = onDelete) {
            Icon(Icons.Filled.Delete, contentDescription = "Hapus Tugas")
        }
    }
}

// Tugas Laprak no 2: Composable untuk AlertDialog
@Composable
fun EditTaskDialog(
    task: Task,
    onDismiss: () -> Unit,
    onConfirm: (String) -> Unit
) {
    var text by remember(task) { mutableStateOf(task.title) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Edit Nama Tugas") },
        text = {
            OutlinedTextField(
                value = text,
                onValueChange = { text = it },
                label = { Text("Nama Tugas") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )
        },
        confirmButton = {
            Button(
                onClick = {
                    if (text.isNotBlank()) {
                        onConfirm(text)
                    }
                },
                enabled = text.isNotBlank()
            ) {
                Text("Simpan")
            }
        },
        dismissButton = {
            Button(onClick = onDismiss) {
                Text("Batal")
            }
        }
    )
}

