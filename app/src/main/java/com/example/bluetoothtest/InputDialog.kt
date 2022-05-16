package com.example.bluetoothtest

import android.annotation.SuppressLint
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.R
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp

@SuppressLint("UnrememberedMutableState")
@Composable
fun InputDialog(
    isOpen: Boolean,
    isTwoLined: Boolean = true,
    firstLabel: String = "",
    secondLabel: String = "",
    initialValueFirst: String = "",
    initialValueSecond: String = "",
    title: String,
    onDismissRequest: () -> Unit,
    postValues: (String, String) -> Unit,
) {
    val firstState = mutableStateOf(initialValueFirst)
    val secondState = mutableStateOf(initialValueSecond)
    if (isOpen) {
        AlertDialog(
            onDismissRequest = onDismissRequest,
            title = { Text(text = title) },
            shape = RoundedCornerShape(15.dp),
            text = {
                Surface {
                    Column(modifier = Modifier.padding(10.dp)) {
                        Spacer(Modifier.height(10.dp))
                        OutlinedTextField(
                            value = firstState.value,
                            onValueChange = { firstState.value = it },
                            singleLine = true,
                            shape = RoundedCornerShape(10.dp),
                            label = { Text(text = firstLabel) })
                        if(isTwoLined){
                            Spacer(Modifier.height(10.dp))
                            OutlinedTextField(
                                value = secondState.value,
                                onValueChange = { secondState.value = it },
                                singleLine = true,
                                shape = RoundedCornerShape(10.dp),
                                label = { Text(text = secondLabel) })
                        }
                    }
                }
            },
            buttons = {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                    Button(onClick = onDismissRequest) {
                        Text(text = "Cancel")
                    }
                    Button(
                        onClick = {
                            postValues(firstState.value, secondState.value)
                        }
                    ){
                        Text(text = "Ok")
                    }
                }
            }
        )
    }
}