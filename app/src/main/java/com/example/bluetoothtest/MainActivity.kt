package com.example.bluetoothtest

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContract
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Alignment.Companion.CenterHorizontally
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.app.ActivityCompat
import com.example.bluetoothtest.ui.theme.BluetoothTestTheme

class MainActivity : ComponentActivity() {
    private val vm by viewModels<MainViewModel>()

    // Create a BroadcastReceiver for ACTION_FOUND.
    private val receiver = object : BroadcastReceiver() {

        @SuppressLint("MissingPermission")
        override fun onReceive(context: Context, intent: Intent) {
            println("XYZ received")
            val action: String? = intent.action
            when(action) {
                BluetoothDevice.ACTION_FOUND -> {
                    // Discovery has found a device. Get the BluetoothDevice
                    // object and its info from the Intent.
                    val device: BluetoothDevice? =
                        intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE)
                    device?.let { vm.addDevice(it) }
                }
            }
        }
    }

    @OptIn(ExperimentalMaterialApi::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val bluetoothManager: BluetoothManager = getSystemService(BluetoothManager::class.java)
        val bluetoothAdapter: BluetoothAdapter? = bluetoothManager.adapter
        bluetoothAdapter?.let { vm.registerAdapter(it)}
        val permissionLauncher = registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()){ map ->
            if(map.any { !it.value }) {
                Toast.makeText(this, "Fehlende Berechtigungen", Toast.LENGTH_SHORT).show()
            }
        }
        if (ActivityCompat.checkSelfPermission(this,Manifest.permission.BLUETOOTH) != PackageManager.PERMISSION_GRANTED
            || ActivityCompat.checkSelfPermission(this,Manifest.permission.BLUETOOTH_SCAN) != PackageManager.PERMISSION_GRANTED
            || ActivityCompat.checkSelfPermission(this,Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED
            || ActivityCompat.checkSelfPermission(this,Manifest.permission.BLUETOOTH_ADMIN) != PackageManager.PERMISSION_GRANTED
        ) {
            permissionLauncher.launch(arrayOf(Manifest.permission.BLUETOOTH, Manifest.permission.BLUETOOTH_ADMIN, Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION))
        }

        if (bluetoothAdapter?.isEnabled == false) {
            val launcher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
                if(result.resultCode == Activity.RESULT_OK) {
                    val data = result.data
                }
            }
            val enableBtIntent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
            launcher.launch(enableBtIntent)
        }

        val filter = IntentFilter(BluetoothDevice.ACTION_FOUND)
        registerReceiver(receiver, filter)
        setContent {
            BluetoothTestTheme {
                // A surface container using the 'background' color from the theme
                var searchStatus by remember { mutableStateOf(false) }
                var isDropdownMenuOpen by remember { mutableStateOf(false)}
                var isDialogOpen by remember { mutableStateOf(false)}

                InputDialog(isOpen = isDialogOpen, title = "Input Text", isTwoLined = false, firstLabel = "Message", onDismissRequest = { isDialogOpen = false }){ a, _ ->
                    vm.sendAsServer(a)
                }
                Scaffold(
                    modifier = Modifier.fillMaxSize(),
                    topBar = {
                             Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                                 IconButton(onClick = { isDropdownMenuOpen = true }) {
                                     Icon(Icons.Default.MoreVert, "")
                                     DropdownMenu(expanded = isDropdownMenuOpen, onDismissRequest = { isDropdownMenuOpen = false }) {
                                         DropdownMenuItem(
                                             onClick = {
                                                 searchStatus = bluetoothAdapter?.startDiscovery() == true
                                             }
                                         ) {
                                             Text(text = "Start Search")
                                         }
                                         DropdownMenuItem(onClick = { bluetoothAdapter?.cancelDiscovery(); searchStatus = false}) {
                                             Text(text = "End Search")
                                         }
                                         DropdownMenuItem(onClick = { vm.clear() }) {
                                             Text(text = "Clear Results")
                                         }
                                         DropdownMenuItem(onClick = { vm.readFromServer() }) {
                                             Text(text = "Read as Client")
                                         }
                                         DropdownMenuItem(onClick = { vm.startServer() }) {
                                             Text(text = "Start Server")
                                         }
                                         DropdownMenuItem(onClick = { isDialogOpen = true }) {
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
                                .weight(1f), horizontalAlignment = CenterHorizontally) {
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
                                    Column(horizontalAlignment = CenterHorizontally) {
                                        Text(text = item.name ?: "null")
                                        Text(text = item.address)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()

        unregisterReceiver(receiver)
    }
}



@Preview(showBackground = true)
@Composable
fun DefaultPreview() {
    BluetoothTestTheme {

    }
}