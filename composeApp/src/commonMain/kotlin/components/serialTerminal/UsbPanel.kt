package components.serialTerminal

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.Divider
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.sharp.Star
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import components.UiComponentFactory
import io.ktor.utils.io.charsets.Charsets
import io.ktor.utils.io.core.toByteArray
import utils.serial_port.SerialPortImpl
import utils.serial_port.SystemComPort

@Composable
fun UsbPanel(onRouteChange: (String) -> Unit) {
    var ports by remember { mutableStateOf(SerialPortImpl.getAvailablePorts()) }
    val receivedData by SerialPortImpl.receivedData.collectAsState()
    var updatePorts by remember { mutableStateOf(false) }
    var sendText by remember { mutableStateOf(TextFieldValue("")) }

    if (updatePorts) {
        ports = SerialPortImpl.getAvailablePorts()
        updatePorts = false
    }

    ElevatedCard {
        Column(
            modifier = Modifier
                .fillMaxHeight()
                .padding(8.dp)
        ) {
            val textModifier: Modifier = Modifier.padding(horizontal = 20.dp)
            UiComponentFactory().Label(
                "Available COM ports",
                color = Color(0xFFFFFFFF),
                fontSize = 16.sp,
                textAlign = TextAlign.Left,
                textModifier
            )
            UiComponentFactory().Label(
                "List of connected devices to your USB ports",
                color = Color(0xFFDDDDDD),
                fontSize = 10.sp,
                textAlign = TextAlign.Left,
                textModifier
            )
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(4.dp),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                if (ports != null) {
                    for (port in ports!!) {
                        Card(
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.onSurface,
                                contentColor = Color.White
                            ),
                            modifier = Modifier
                                .padding(4.dp)
                                .fillMaxWidth()
                        ) {
                            UsbAvailablePortItem(
                                port,
                                onClickOpen = {
                                SerialPortImpl.open(port.name, 115200)
                                updatePorts = true
                                },
                                onClickClose = {
                                    SerialPortImpl.close()
                                    updatePorts = true
                                }
                            )
                            if( port.isOpen ) {
                                UsbTerminal(
                                    sendText = sendText,
                                    onTextChange = { newValue -> sendText = newValue },
                                    receivedData = receivedData,
                                    onRouteChange
                                )
                            }
                        }
                    }
                } else {
                    UiComponentFactory().createFilledCard("No USB device connection detected.")
                }
            }
        }
    }
}


@Composable
fun UsbAvailablePortItem(port: SystemComPort, onClickOpen: () -> Unit, onClickClose: () -> Unit){
    Row(
        modifier = Modifier
            .padding(8.dp)
            .fillMaxWidth()
    ) {
        Column(
            Modifier
                .width(120.dp)
                .align(Alignment.CenterVertically)
        ) {
            Text(
                text = port.name,
                modifier = Modifier
                    .padding(2.dp),
                textAlign = TextAlign.Center,
                style = TextStyle(fontWeight = FontWeight.Bold, fontSize = 20.sp),
                color = Color.Green
            )
        }
        Spacer(modifier = Modifier.weight(1f))
        Column(
            horizontalAlignment = Alignment.End,
            modifier = Modifier.align(Alignment.CenterVertically)
        ) {
            if (!port.isOpen) {
                Button(
                    onClick = onClickOpen,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color.DarkGray,
                        contentColor = Color.White
                    ),
                    modifier = Modifier
                        .width(110.dp)
                        .height(36.dp)
                ) {
                    Text("Connect")
                }
            } else {
                Button(
                    onClick = onClickClose,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color.DarkGray,
                        contentColor = Color.White
                    ),
                    modifier = Modifier
                        .width(110.dp)
                        .height(36.dp)
                ) {
                    Text("Close")
                }
            }
        }
    }
}

@Composable
fun UsbTerminal(sendText: TextFieldValue, onTextChange: (TextFieldValue) -> Unit, receivedData: String, onRouteChange: (String) -> Unit){
    UsbTerminalInput(sendText, onTextChange)
    UsbOptionsButtons(sendText, onRouteChange)
    Divider( color = Color.DarkGray, thickness = 1.dp, modifier = Modifier.padding(top = 5.dp) )
    Row(
        modifier = Modifier.fillMaxWidth().background(Color.Black)
    ) {
        val dataLines = receivedData.split("\n").asReversed()
        TerminalConsole(dataLines)
    }
}

@Composable
fun UsbTerminalInput(
    sendText: TextFieldValue,
    onTextChange: (TextFieldValue) -> Unit){
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.Black),
        horizontalArrangement = Arrangement.SpaceEvenly
    ) {
        Column(
            modifier = Modifier
                .padding(5.dp)
                .fillMaxWidth()
                .align(Alignment.CenterVertically)
                .background(Color.Black)
        ) {
            OutlinedTextField(
                modifier = Modifier.fillMaxWidth().padding(5.dp).width(380.dp),
                value = sendText,
                onValueChange = onTextChange,
                singleLine = true,
                label = { Text("Command") },
                placeholder = { Text("Your command here...") },
                colors = OutlinedTextFieldDefaults.colors(
                    focusedLabelColor = Color.Cyan,
                    unfocusedLabelColor = Color.White,
                    focusedBorderColor = Color.Cyan,
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White,
                    unfocusedBorderColor = Color.White,
                    cursorColor = Color.Cyan
                )
            )
        }
    }
}

@Composable
fun UsbOptionsButtons(sendText: TextFieldValue, onRouteChange: (String) -> Unit) {
    var showOptions by remember { mutableStateOf(false) }
    Row(
        Modifier.fillMaxWidth()
    ) {
        Row(
            horizontalArrangement = Arrangement.Start,
            modifier = Modifier
                .align(Alignment.CenterVertically)
                .background(Color.Black)
                .padding(5.dp)
        ) {

            IconButton(onClick = {
                    SerialPortImpl.write(
                        sendText.text.toByteArray(
                            Charsets.UTF_8
                        )
                    )
            }) {
                Icon(
                    imageVector = Icons.Sharp.Star,
                    contentDescription = "Commands"
                )
            }

            IconButton(onClick = {
                    SerialPortImpl.write(
                        sendText.text.toByteArray(
                            Charsets.UTF_8
                        )
                    )
                    onRouteChange("add-device")
            }) {
                Icon(
                    imageVector = Icons.Filled.Add,
                    contentDescription = "Add"
                )
            }
        }

        Spacer(modifier = Modifier.weight(1f))


        Column(
            horizontalAlignment = Alignment.End,
            modifier = Modifier
                .align(Alignment.CenterVertically)
                .background(Color.Black)
                .padding(5.dp)
        ) {
            Button(
                onClick = {
                    SerialPortImpl.write(
                        sendText.text.toByteArray(
                            Charsets.UTF_8
                        )
                    )
                },
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF33FF66),
                    contentColor = Color(0xFF223240)
                ),
                modifier = Modifier
                    .width(80.dp)
                    .background(Color.Black)
                    .height(36.dp) // Ajusta el tamaño aquí
            ) {
                Icon(
                    imageVector = Icons.Filled.Send,
                    contentDescription = "Send"
                )
            }
        }
    }
}



@Composable
fun TerminalConsole(dataLines: List<String>){
    LazyColumn(
        modifier = Modifier
            .padding(8.dp)
            .fillMaxWidth(),
        userScrollEnabled = true
    ) {
        items(dataLines) { receivedDataSplitted ->
            val color = when {
                // info
                receivedDataSplitted.contains("SYNAPPSE.INFO")
                        && !receivedDataSplitted.contains("[0.0.0.0][null]") -> Color.Cyan
                // status
                receivedDataSplitted.contains("SYNAPPSE.STATUS") -> Color.Magenta
                // memory
                receivedDataSplitted.contains("SYNAPPSE.MEMORY")
                        && !receivedDataSplitted.contains("ERROR]")
                        && !receivedDataSplitted.contains("WARNING]")
                        && !receivedDataSplitted.contains("WAIT]") -> Color.Yellow
                // mqtt
                receivedDataSplitted.contains("SYNAPPSE.MQTT")
                        && !receivedDataSplitted.contains("ERROR]")
                        && !receivedDataSplitted.contains("WARNING]")
                        && !receivedDataSplitted.contains("WAIT]") -> Color.Green
                // serial
                receivedDataSplitted.contains("SYNAPPSE.SERIAL") -> Color.LightGray
                // offline
                receivedDataSplitted.contains("SYNAPPSE.AP")
                        || receivedDataSplitted.contains("SYNAPPSE.NETWORK.INFO")
                        || receivedDataSplitted.contains("[0.0.0.0][null]") -> Color.Black
                // warning
                receivedDataSplitted.contains("WARNING]")
                        || receivedDataSplitted.contains("WAIT]") -> Color(0xFFFF6600)
                // error
                receivedDataSplitted.contains("ERROR]") -> Color.Red
                // default
                else -> Color.White
            }

            Text(
                modifier = Modifier.padding(4.dp),
                text = receivedDataSplitted,
                style = TextStyle(
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 14.sp
                ),
                color = color
            )
        }
    }
}
