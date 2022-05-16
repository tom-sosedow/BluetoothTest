package com.example.bluetoothtest

import android.annotation.SuppressLint
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothServerSocket
import android.bluetooth.BluetoothSocket
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch
import java.io.IOException
import java.util.*


class MainViewModel : ViewModel() {
    private lateinit var bluetoothAdapter: BluetoothAdapter

    private val _state = mutableStateOf<List<BluetoothDevice>>(emptyList())
    val state : State<List<BluetoothDevice>> = _state

    private val _textState = mutableStateOf("")
    val textState : State<String> = _textState

    private val _eventFlow = MutableSharedFlow<UiEvent>()
    val eventFlow = _eventFlow.asSharedFlow()

    private lateinit var clientSocket: BluetoothSocket
    private lateinit var serverSocket: BluetoothServerSocket

    fun registerAdapter(adapter: BluetoothAdapter){
        bluetoothAdapter = adapter
    }

    fun addDevice(device: BluetoothDevice) {
        _state.value = state.value.plus(device)
    }

    fun clear() {
        _state.value = emptyList()
        viewModelScope.launch {
            _eventFlow.emit(UiEvent.showSnackBar("Cleared"))
        }
    }

    @SuppressLint("MissingPermission")
    fun connectAsClientTo(device: BluetoothDevice) {
        clientSocket = device.createRfcommSocketToServiceRecord(UUID(1L, 2L))
        viewModelScope.launch(Dispatchers.IO) {
            clientSocket.connect()
            _eventFlow.emit(UiEvent.showSnackBar("Connected to Server"))
            readFromServer()
        }
    }

    @SuppressLint("MissingPermission")
    fun startServer() {
        serverSocket = bluetoothAdapter.listenUsingRfcommWithServiceRecord("tomtest", UUID(1L, 2L))
        viewModelScope.launch(Dispatchers.IO) {
            try {
                clientSocket = serverSocket.accept()
                serverSocket.close()
                _eventFlow.emit(UiEvent.showSnackBar("Started Server"))
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }
    }

    fun readFromServer() {
        viewModelScope.launch(Dispatchers.IO) {
            _eventFlow.emit(UiEvent.showSnackBar("Waiting for Message from Server"))
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

    fun sendAsServer(message: String){
        viewModelScope.launch(Dispatchers.IO) {
            val outputStream = clientSocket.outputStream
            outputStream.write(message.toByteArray())
            _eventFlow.emit(UiEvent.showSnackBar("Sent message to client"))
        }
    }

    sealed class UiEvent {
        data class showSnackBar(val message: String): UiEvent()
    }
}