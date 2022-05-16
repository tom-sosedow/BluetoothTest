package com.example.bluetoothtest

import android.annotation.SuppressLint
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.flow.collectLatest

@SuppressLint("MissingPermission")
@OptIn(ExperimentalMaterialApi::class)
@Composable
fun BluetoothTestScreen(
    vm: BluetoothViewModel,
    startDiscovery: () -> Boolean,
    endDiscovery: () -> Boolean,
    enableDiscoverable: () -> Unit
){
    var isDropdownMenuOpen by remember { mutableStateOf(false) }
    var isDialogOpen by remember { mutableStateOf(false) }
    val scaffoldState = rememberScaffoldState()

    InputDialog(isOpen = isDialogOpen, title = "Input Text", isTwoLined = false, firstLabel = "Message", onDismissRequest = { isDialogOpen = false }){ a, _ ->
        vm.sendAsServer(a)
        isDialogOpen = false
    }

    LaunchedEffect(key1 = Unit) {
        vm.eventFlow.collectLatest { event ->
            when(event) {
                is NormalBluetoothViewModel.UiEvent.showSnackBar -> {
                    scaffoldState.snackbarHostState.showSnackbar(event.message)
                }
            }
        }
    }
    Scaffold(
        modifier = Modifier.fillMaxSize(),
        scaffoldState = scaffoldState,
        topBar = {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                IconButton(onClick = { isDropdownMenuOpen = true }) {
                    Icon(Icons.Default.MoreVert, "")
                    DropdownMenu(expanded = isDropdownMenuOpen, onDismissRequest = { isDropdownMenuOpen = false }) {
                        DropdownMenuItem(
                            onClick = {
                                startDiscovery()
                                isDropdownMenuOpen = false
                            }
                        ) {
                            Text(text = "Start Search")
                        }
                        DropdownMenuItem(onClick = { endDiscovery() ; isDropdownMenuOpen = false}) {
                            Text(text = "End Search")
                        }
                        DropdownMenuItem(onClick = { vm.clear(); isDropdownMenuOpen = false }) {
                            Text(text = "Clear Results")
                        }
                        DropdownMenuItem(onClick = { vm.readFromServer(); isDropdownMenuOpen = false }) {
                            Text(text = "Read as Client")
                        }
                        DropdownMenuItem(
                            onClick = {
                                enableDiscoverable()
                                vm.startServer()
                                isDropdownMenuOpen = false
                            }
                        ) {
                            Text(text = "Start Server")
                        }
                        DropdownMenuItem(onClick = { isDialogOpen = true ; isDropdownMenuOpen = false}) {
                            Text(text = "Send Message as Server")
                        }
                    }
                }
            }
        },

        ) {
        Column(Modifier.fillMaxSize()) {
            Text(text = "Read:" + vm.textState.value)
            LazyColumn(
                Modifier
                    .fillMaxWidth()
                    .weight(1f), horizontalAlignment = Alignment.CenterHorizontally
            ) {
                items(vm.state.value) { item ->
                    Card(
                        onClick = {vm.connectAsClientTo(item)},
                        backgroundColor = MaterialTheme.colors.surface,
                        modifier = Modifier
                            .padding(5.dp)
                            .fillMaxWidth(0.7f),
                        elevation = 5.dp,
                        shape = CircleShape
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(text = item.name ?: "null")
                            Text(text = item.address)
                        }
                    }
                }
            }
        }
    }
}