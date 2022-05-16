package com.example.bluetoothtest

import android.annotation.SuppressLint
import android.bluetooth.*
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch
import java.io.IOException
import java.util.*

class LeBluetoothViewModel: BluetoothViewModel(){
    private lateinit var bluetoothAdapter: BluetoothAdapter

    private val _state = mutableStateOf<List<BluetoothDevice>>(emptyList())
    override val state : State<List<BluetoothDevice>> = _state

    private val _eventFlow = MutableSharedFlow<NormalBluetoothViewModel.UiEvent>()
    override val eventFlow = _eventFlow.asSharedFlow()

    private val _textState = mutableStateOf("")
    override val textState : State<String> = _textState

    private lateinit var clientSocket: BluetoothSocket
    private lateinit var serverSocket: BluetoothServerSocket

    override fun registerAdapter(adapter: BluetoothAdapter){
        bluetoothAdapter = adapter
    }

    override fun addDevice(device: BluetoothDevice) {
        _state.value = state.value.plus(device)
    }

    override fun clear() {
        _state.value = emptyList()
        viewModelScope.launch {
            _eventFlow.emit(NormalBluetoothViewModel.UiEvent.showSnackBar("Cleared"))
        }
    }

    @SuppressLint("MissingPermission")
    override fun connectAsClientTo(device: BluetoothDevice) {
        var btGatt: BluetoothGatt? = null

    }

    @SuppressLint("MissingPermission")
    override fun startServer() {
        serverSocket = bluetoothAdapter.listenUsingRfcommWithServiceRecord("tomtest", UUID(1L, 2L))
        viewModelScope.launch(Dispatchers.IO) {
            try {
                clientSocket = serverSocket.accept()
                serverSocket.close()
                _eventFlow.emit(NormalBluetoothViewModel.UiEvent.showSnackBar("Started Server"))
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }
    }

    override fun readFromServer() {
        viewModelScope.launch(Dispatchers.IO) {
            _eventFlow.emit(NormalBluetoothViewModel.UiEvent.showSnackBar("Waiting for Message from Server"))
            try {
                var inputStream= clientSocket.inputStream
                while (inputStream.available() == 0) {
                    inputStream = clientSocket.inputStream
                }
                val available: Int = inputStream.available()
                val bytes = ByteArray(available)
                inputStream.read(bytes, 0, available)
                val string = String(bytes)
                _textState.value = string
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }
    }

    override fun sendAsServer(message: String){
        viewModelScope.launch(Dispatchers.IO) {
            val outputStream = clientSocket.outputStream
            outputStream.write(message.toByteArray())
            _eventFlow.emit(NormalBluetoothViewModel.UiEvent.showSnackBar("Sent message to client"))
        }
    }
}