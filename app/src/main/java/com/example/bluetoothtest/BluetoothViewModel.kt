package com.example.bluetoothtest

import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import androidx.compose.runtime.State
import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.SharedFlow

abstract class BluetoothViewModel: ViewModel() {
    abstract fun registerAdapter(adapter: BluetoothAdapter)
    abstract fun addDevice(device: BluetoothDevice)
    abstract fun clear()
    abstract fun connectAsClientTo(device: BluetoothDevice)
    abstract fun startServer()
    abstract fun readFromServer()
    abstract fun sendAsServer(message: String)

    abstract val state : State<List<BluetoothDevice>>
    abstract val eventFlow: SharedFlow<NormalBluetoothViewModel.UiEvent>
    abstract val textState : State<String>
}
