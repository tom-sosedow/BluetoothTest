package com.example.bluetoothtest

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothManager
import android.bluetooth.le.ScanCallback
import android.bluetooth.le.ScanResult
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.os.Bundle
import android.os.Handler
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.tooling.preview.Preview
import androidx.core.app.ActivityCompat
import com.example.bluetoothtest.ui.theme.BluetoothTestTheme

class MainActivity : ComponentActivity() {
    private val normalVm by viewModels<NormalBluetoothViewModel>()
    private val leBtVm by viewModels<NormalBluetoothViewModel>()

    val bluetoothManager: BluetoothManager = getSystemService(BluetoothManager::class.java)
    val bluetoothAdapter: BluetoothAdapter? = bluetoothManager.adapter
    val bluetoothLeScanner = bluetoothAdapter?.bluetoothLeScanner

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
                    device?.let { normalVm.addDevice(it) }
                }
            }
        }
    }

    @OptIn(ExperimentalMaterialApi::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        bluetoothAdapter?.let { normalVm.registerAdapter(it)}
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
        val enableDiscoverableLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if(result.resultCode == Activity.RESULT_OK) {
                val data = result.data
            }
        }
        val enableDiscoverableIntent = Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE)


        val filter = IntentFilter(BluetoothDevice.ACTION_FOUND)
        registerReceiver(receiver, filter)

        setContent {
            BluetoothTestTheme {
                // A surface container using the 'background' color from the theme
                var isLeBt by remember{mutableStateOf(false)}
                if(!isLeBt) {
                    BluetoothTestScreen(
                        vm = normalVm,
                        startDiscovery = { bluetoothAdapter?.startDiscovery() == true },
                        endDiscovery = { bluetoothAdapter?.cancelDiscovery() == true },
                        enableDiscoverable = {enableDiscoverableLauncher.launch(enableDiscoverableIntent)}
                    )
                } else {
                    BluetoothTestScreen(
                        vm = leBtVm,
                        startDiscovery = { bluetoothAdapter?.startDiscovery() == true },
                        endDiscovery = { bluetoothAdapter?.cancelDiscovery() == true },
                        enableDiscoverable = {enableDiscoverableLauncher.launch(enableDiscoverableIntent)}
                    )
                }
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()

        unregisterReceiver(receiver)
    }

    /*
            Low Energy bluetooth Section
         */
    // Device scan callback.
    private val leScanCallback: ScanCallback = object : ScanCallback() {
        override fun onScanResult(callbackType: Int, result: ScanResult) {
            super.onScanResult(callbackType, result)
            leBtVm.addDevice(result.device)
        }
    }

    private var scanning = false
    private val handler = Handler()

    // Stops scanning after 10 seconds.
    private val SCAN_PERIOD: Long = 10000

    @SuppressLint("MissingPermission")
    private fun scanLeDevice() {
        if (!scanning) { // Stops scanning after a pre-defined scan period.
            handler.postDelayed({
                scanning = false
                bluetoothLeScanner?.stopScan(leScanCallback)
            }, SCAN_PERIOD)
            scanning = true
            bluetoothLeScanner?.startScan(leScanCallback)
        } else {
            scanning = false
            bluetoothLeScanner?.stopScan(leScanCallback)
        }
    }
}



@Preview(showBackground = true)
@Composable
fun DefaultPreview() {
    BluetoothTestTheme {

    }
}