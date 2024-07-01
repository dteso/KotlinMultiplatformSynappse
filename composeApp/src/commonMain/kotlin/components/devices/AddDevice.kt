package components.devices

import Event
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
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
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
import kotlinx.serialization.json.Json
import utils.serial_port.SerialPortImpl
import utils.serial_port.SystemComPort

@Composable
fun AddDevice(onRouteChange: (String) -> Unit) {
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
                            if (port.isOpen) {
                                NewDeviceForm(
                                    sendText = sendText,
                                    onTextChange = { newValue -> sendText = newValue },
                                    receivedData = receivedData,
                                    onRouteChange = onRouteChange
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
fun UsbAvailablePortItem(port: SystemComPort, onClickOpen: () -> Unit, onClickClose: () -> Unit) {
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
fun NewDeviceForm(sendText: TextFieldValue, onTextChange: (TextFieldValue) -> Unit, receivedData: String, onRouteChange: (String) -> Unit) {

    var deviceName by remember { mutableStateOf("") }
    var wifiSsid by remember { mutableStateOf("") }
    var wifiPassword by remember { mutableStateOf("") }
    var timeAlive by remember { mutableStateOf("") }
    var ip by remember { mutableStateOf("") }
    var mac by remember { mutableStateOf("") }
    var mqttServer by remember { mutableStateOf("") }
    var mqttPort by remember { mutableStateOf(0) }
    var mqttUser by remember { mutableStateOf("") }
    var mqttPassword by remember { mutableStateOf("") }

    val dataLines = receivedData.split("\n").asReversed()
    for (line in dataLines) {
        if (line.startsWith("[SYNAPPSE.STATUS]:")) {
            val statusJson = line.removePrefix("[SYNAPPSE.STATUS]:").trim()
            try {
                val event = Json.decodeFromString<Event>(statusJson)
                deviceName = event.config.name
                wifiSsid = event.config.ssid
                wifiPassword = event.config.staPass
                mac = event.config.MAC
                ip = event.config.ip
                timeAlive = event.status.timeAlive
                mqttServer = event.config.mqttServer
                mqttPort = event.config.mqttPort
                mqttUser = event.config.mqttUser
                mqttPassword = event.config.mqttPassword
//                print(event)
            } catch (e: Exception) {
                println("Failed to decode JSON: ${e.message}")
            }
            break
        }
    }

    Divider(color = Color.DarkGray, thickness = 1.dp, modifier = Modifier.padding(top = 5.dp))
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.Black)
            .padding(8.dp)
    ) {
        LazyColumn (Modifier.fillMaxWidth(), userScrollEnabled = true, horizontalAlignment = Alignment.CenterHorizontally) {
            val textModifier: Modifier = Modifier.padding(horizontal = 5.dp)

            item{
                UiComponentFactory().Label(
                    "Device",
                    color = Color(0xFFFFFFFF),
                    fontSize = 14.sp,
                    textAlign = TextAlign.Left,
                    textModifier
                )

                OutlinedTextField(
                    value = deviceName,
                    onValueChange = { newValue -> deviceName = newValue },
                    label = { Text("Name") },
                    placeholder = { Text("e.g., Garage") },
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

                OutlinedTextField(
                    value = mac,
                    onValueChange = { newValue -> mac = newValue },
                    label = { Text("MAC") },
                    placeholder = { Text("e.g., Garage") },
                    enabled = false,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedLabelColor = Color.Cyan,
                        unfocusedLabelColor = Color.White,
                        focusedBorderColor = Color.Cyan,
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        unfocusedBorderColor = Color.White,
                        cursorColor = Color.Cyan,
                        disabledBorderColor = Color.Gray, // Color del borde cuando el campo está deshabilitado
                        disabledLabelColor = Color.Gray, // Color del label cuando el campo está deshabilitado
                        disabledTextColor = Color.Gray // Color del texto cuando el campo está deshabilitado
                    )
                )

                Divider( color = Color.DarkGray, thickness = 1.dp, modifier = Modifier.padding(16.dp))

                UiComponentFactory().Label(
                    "Wifi Configuration",
                    color = Color(0xFFFFFFFF),
                    fontSize = 14.sp,
                    textAlign = TextAlign.Left,
                    textModifier
                )

                OutlinedTextField(
                    value = ip,
                    onValueChange = { newValue -> ip = newValue },
                    label = { Text("IP") },
                    placeholder = { Text("e.g., Garage") },
                    enabled = false,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedLabelColor = Color.Cyan,
                        unfocusedLabelColor = Color.White,
                        focusedBorderColor = Color.Cyan,
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        unfocusedBorderColor = Color.White,
                        cursorColor = Color.Cyan,
                        disabledBorderColor = Color.Gray, // Color del borde cuando el campo está deshabilitado
                        disabledLabelColor = Color.Gray, // Color del label cuando el campo está deshabilitado
                        disabledTextColor = Color.Gray // Color del texto cuando el campo está deshabilitado
                    )
                )

                OutlinedTextField(
                    value = wifiSsid,
                    onValueChange = { newValue -> wifiSsid = newValue },
                    label = { Text("SSID") },
                    placeholder = { Text("e.g., MiFibra-9B0C") },
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

                OutlinedTextField(
                    value = wifiPassword,
                    onValueChange = { newValue -> wifiPassword = newValue },
                    label = { Text("Password") },
                    placeholder = { Text("e.g., xxxxxxxxxxx") },
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



                Divider( color = Color.DarkGray, thickness = 1.dp, modifier = Modifier.padding(16.dp))

                UiComponentFactory().Label(
                    "MQTT Configuration",
                    color = Color(0xFFFFFFFF),
                    fontSize = 14.sp,
                    textAlign = TextAlign.Left,
                    textModifier
                )

                OutlinedTextField(
                    value = mqttServer,
                    onValueChange = { newValue -> mqttServer = newValue },
                    label = { Text("MQTT Broker") },
                    placeholder = { Text("e.g., Garage") },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedLabelColor = Color.Cyan,
                        unfocusedLabelColor = Color.White,
                        focusedBorderColor = Color.Cyan,
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        unfocusedBorderColor = Color.White,
                        cursorColor = Color.Cyan,
                        disabledBorderColor = Color.Gray, // Color del borde cuando el campo está deshabilitado
                        disabledLabelColor = Color.Gray, // Color del label cuando el campo está deshabilitado
                        disabledTextColor = Color.Gray // Color del texto cuando el campo está deshabilitado
                    )
                )

                OutlinedTextField(
                    value = mqttPort.toString(),
                    onValueChange = { newValue -> mqttPort = newValue.toInt() },
                    label = { Text("MQTT Port") },
                    placeholder = { Text("e.g., myuser") },
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

                OutlinedTextField(
                    value = mqttUser,
                    onValueChange = { newValue -> mqttUser = newValue },
                    label = { Text("MQTT User") },
                    placeholder = { Text("e.g., mymqttuser") },
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

                OutlinedTextField(
                    value = mqttPassword,
                    onValueChange = { newValue -> mqttPassword = newValue },
                    label = { Text("MQTT Password") },
                    placeholder = { Text("e.g., *************") },
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
}


