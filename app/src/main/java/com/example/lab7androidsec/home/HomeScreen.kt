package com.example.lab7androidsec.home


import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.BasicAlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.health.connect.client.PermissionController
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.lab7androidsec.NavigationDestination
import com.example.lab7androidsec.R
import com.example.lab7androidsec.data.AppRequired
import com.example.lab7androidsec.data.ViewModelProvider
import kotlinx.coroutines.launch
import java.time.ZoneId
import java.time.format.DateTimeFormatter

object HomeDestination : NavigationDestination {
    override val route = "home"
    override val titleResourceId: Int = R.string.app_name
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    modifier: Modifier = Modifier,
    viewModel: HomeViewModel = viewModel(factory = ViewModelProvider.Factory)
) {
    val uiState: HomeViewModel.UiState by viewModel.uiState

    val snackbarHostState = remember { SnackbarHostState() }


    val permissionRequestLauncher =
        rememberLauncherForActivityResult(contract = PermissionController.createRequestPermissionResultContract()) { granted ->
            if (granted.containsAll(AppRequired.requiredPermissions)) {
                viewModel.onChecksFinished()
            }
        }


    Scaffold(
        snackbarHost = {
            SnackbarHost(hostState = snackbarHostState)
        },
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text(stringResource(HomeDestination.titleResourceId)) },
                modifier = modifier
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = {
                viewModel.showAddDialog(true)
            },
                contentColor = Color.White,
                shape = CircleShape,
                containerColor = Color.DarkGray) {
                Icon(
                    imageVector = Icons.Filled.Add,
                    contentDescription = "Add"
                )
            }

        }
    ) { innerPadding ->
        if (uiState.isAddEntryDialogRequired) {
            AddEntryDialog(
                onSave = { interval, steps ->
                    viewModel.showAddDialog(false)
                    viewModel.saveEntry(interval, steps)
                },
                onCancel = {
                    viewModel.showAddDialog(false)
                }
            )
        }
        if (uiState.permissionDialogRequired) {
            AlertDialog(onDismissRequest = { },
                title = { Text("Permissions required") },
                text = { Text("In order to proceed you have to grant permissions to read & write steps data.") },
                modifier = modifier,
                dismissButton = {
                    TextButton(onClick = { viewModel.showPermissionDialog(false) }) {
                        Text("Cancel")
                    }
                },
                confirmButton = {
                    TextButton(onClick = {
                        viewModel.showPermissionDialog(false)
                        permissionRequestLauncher.launch(AppRequired.requiredPermissions)
                    }) {
                        Text("Grant")
                    }
                })
        }
        HomeBody(
            viewModel,
            modifier = modifier
                .padding(innerPadding)
                .fillMaxSize()
        )

    }
}

@Composable
fun HomeBody(
    viewModel: HomeViewModel,
    modifier: Modifier = Modifier,
) {
    val uiState: HomeViewModel.UiState by viewModel.uiState

    Column(
        verticalArrangement = Arrangement.Top,
        modifier = modifier.fillMaxSize()
    ) {
        val timePattern = remember {
            DateTimeFormatter.ofPattern("HH:mm").withZone(ZoneId.systemDefault())
        }
        Surface(
            color = Color.LightGray,
            tonalElevation = 8.dp,
            shadowElevation = 8.dp,
            modifier = modifier.fillMaxSize()
        ) {

            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Top,
                modifier = Modifier
                    .fillMaxSize()
            ) {
                Row(
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    Text(
                        uiState.day,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(stringResource(R.string.steps, uiState.totalSteps))
                }
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                ) {
                    items(uiState.stepsRecords) {

                        Card(
                            Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 8.dp)
                        ) {
                            Column(Modifier.padding(16.dp)) {
                                Text(
                                    "${timePattern.format(it.startTime)} - ${
                                        timePattern.format(it.endTime)
                                    }",
                                    fontWeight = FontWeight.Bold
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Text("${it.count} steps")
                            }
                        }

                        Spacer(Modifier.height(8.dp))
                    }
                }

            }
        }
    }
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddEntryDialog(
    onSave: (interval: Pair<Int, Int>, steps: Long) -> Unit,
    onCancel: () -> Unit,
    modifier: Modifier = Modifier
) {
    var steps by remember { mutableStateOf("") }
    var startHour by remember { mutableStateOf(0) }
    var startMinute by remember { mutableStateOf(0) }
    var endHour by remember { mutableStateOf(23) }
    var endMinute by remember { mutableStateOf(59) }

    val scope = rememberCoroutineScope()

    BasicAlertDialog(onDismissRequest = { onCancel() },
        modifier = modifier,
        content = {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.fillMaxWidth(),
            ) {
                Row(
                    horizontalArrangement = Arrangement.SpaceAround,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    OutlinedTextField(
                        value = startHour.toString(),
                        onValueChange = { startHour = it.toIntOrNull() ?: 0 },
                        label = { Text("Часы начала") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.width(100.dp)
                    )

                    OutlinedTextField(
                        value = startMinute.toString(),
                        onValueChange = { startMinute = it.toIntOrNull() ?: 0 },
                        label = { Text("Минуты начала") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.width(100.dp)
                    )
                }
                Spacer(Modifier.height(8.dp))

                Row(
                    horizontalArrangement = Arrangement.SpaceAround,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    OutlinedTextField(
                        value = endHour.toString(),
                        onValueChange = { endHour = it.toIntOrNull() ?: 0 },
                        label = { Text("Часы конца") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.width(100.dp)
                    )

                    OutlinedTextField(
                        value = endMinute.toString(),
                        onValueChange = { endMinute = it.toIntOrNull() ?: 0 },
                        label = { Text("Минуты конца") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.width(100.dp)
                    )
                }

                OutlinedTextField(
                    shape = RoundedCornerShape(8.dp),
                    value = steps,
                    onValueChange = {
                        steps = it
                    },
                    label = { Text("Количество шагов") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.width(200.dp)
                )
                Spacer(Modifier.height(8.dp))
                Button(
                    shape = RoundedCornerShape(8.dp),
                    enabled = (steps.toLongOrNull()
                        ?: 0) > 0 && (startHour * 60 + startMinute < endHour * 60 + endMinute),
                    onClick = {
                        scope.launch { onCancel() }.invokeOnCompletion {
                            onSave(
                                startHour * 60 + startMinute to endHour * 60 + endMinute,
                                steps.toLong()
                            )
                        }
                    },
                    modifier = Modifier.padding(bottom = 16.dp)
                ) {
                    Text("Сохранить")
                }
            }
        }
    )
}

