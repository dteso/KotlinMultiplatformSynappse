package components.devices

import Event
import Port
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.text.selection.TextSelectionColors
import androidx.compose.material.Divider
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.ExposedDropdownMenuBox
import androidx.compose.material.ExposedDropdownMenuDefaults
import androidx.compose.material.Switch
import androidx.compose.material.SwitchDefaults
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.sharp.AddCircle
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import components.UiComponentFactory
import io.ktor.utils.io.charsets.Charsets
import io.ktor.utils.io.core.toByteArray
import kotlinx.serialization.json.Json
import kotlinx.serialization.encodeToString
import utils.serial_port.SerialPortImpl

@Composable
fun UsbDeviceSetup(onRouteChange: (String) -> Unit) {
    val receivedData by SerialPortImpl.receivedData.collectAsState()
    var sendText by remember { mutableStateOf(TextFieldValue("")) }

    NewDeviceForm(
        sendText = sendText,
        onTextChange = { newValue -> sendText = newValue },
        receivedData = receivedData,
        onRouteChange = onRouteChange
    )
}

@Composable
fun NewDeviceForm(
    sendText: TextFieldValue,
    onTextChange: (TextFieldValue) -> Unit,
    receivedData: String,
    onRouteChange: (String) -> Unit
) {
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
    var ports by remember { mutableStateOf<MutableList<Port>>(mutableListOf()) }
    var event by remember { mutableStateOf(Event(null, null, null)) }

    val dataLines = receivedData.split("\n").asReversed()
    var isInitialized by remember { mutableStateOf(false) }

    for (line in dataLines) {
        if (!isInitialized) {
            if (line.startsWith("[SYNAPPSE.STATUS]:")) {
                val statusJson = line.removePrefix("[SYNAPPSE.STATUS]:").trim()
                try {
                    val json = Json {
                        ignoreUnknownKeys = true
                    }
                    val newEvent = json.decodeFromString<Event>(statusJson)
                    deviceName = newEvent.config!!.name
                    wifiSsid = newEvent.config!!.staSsid
                    wifiPassword = newEvent.config!!.staPass
                    mac = newEvent.config!!.MAC
                    ip = newEvent.config!!.ip
                    timeAlive = newEvent.status!!.timeAlive
                    mqttServer = newEvent.config!!.mqttServer
                    mqttPort = newEvent.config!!.mqttPort
                    mqttUser = newEvent.config!!.mqttUser
                    mqttPassword = newEvent.config!!.mqttPassword
                    ports = newEvent.config!!.ports?.toMutableList() ?: mutableListOf()
                    event = newEvent
                    isInitialized = true
                } catch (e: Exception) {
                    println("Failed to decode JSON: ${e.message}")
                }
                break
            }
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
        LazyColumn(
            Modifier.fillMaxWidth(),
            userScrollEnabled = true,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            val textModifier: Modifier = Modifier.padding(horizontal = 5.dp)

            item {
                UiComponentFactory().Label(
                    "Device",
                    color = Color(0xFFFFFFFF),
                    fontSize = 16.sp,
                    textAlign = TextAlign.Left,
                    textModifier
                )

                deviceName = if (deviceName == "null") "" else deviceName
                OutlinedTextField(
                    modifier = Modifier.padding(3.dp),
                    value = deviceName,
                    onValueChange = { newValue ->
                        deviceName = newValue
                        event = event.copy(config = event.config?.copy(name = newValue))
                    },
                    label = { Text("Name") },
                    placeholder = { Text("e.g., Garage") },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedLabelColor = Color.Cyan,
                        unfocusedLabelColor = Color.White,
                        focusedBorderColor = Color.Cyan,
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        unfocusedBorderColor = Color.White,
                        cursorColor = Color.Cyan,
                        selectionColors = TextSelectionColors(
                            handleColor = Color.Black,
                            backgroundColor = Color(0x5500DDFF),
                        )
                    )
                )

                OutlinedTextField(
                    modifier = Modifier.padding(3.dp),
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
                        disabledBorderColor = Color.Gray,
                        disabledLabelColor = Color.Gray,
                        disabledTextColor = Color.Gray,
                        selectionColors = TextSelectionColors(
                            handleColor = Color.Black,
                            backgroundColor = Color(0x5500DDFF),
                        )
                    )
                )

                Divider(
                    color = Color.DarkGray,
                    thickness = 1.dp,
                    modifier = Modifier.padding(16.dp)
                )

                UiComponentFactory().Label(
                    "Wifi Configuration",
                    color = Color(0xFFFFFFFF),
                    fontSize = 16.sp,
                    textAlign = TextAlign.Left,
                    textModifier
                )

                OutlinedTextField(
                    modifier = Modifier.padding(3.dp),
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
                        disabledBorderColor = Color.Gray,
                        disabledLabelColor = Color.Gray,
                        disabledTextColor = Color.Gray,
                        selectionColors = TextSelectionColors(
                            handleColor = Color.Black,
                            backgroundColor = Color(0x5500DDFF),
                        )
                    )
                )

                wifiSsid = if (wifiSsid == "null") "" else wifiSsid
                OutlinedTextField(
                    modifier = Modifier.padding(3.dp),
                    value = wifiSsid,
                    onValueChange = { newValue ->
                        wifiSsid = newValue
                        event = event.copy(config = event.config?.copy(staSsid = newValue))
                    },
                    label = { Text("SSID") },
                    placeholder = { Text("e.g., MiFibra-9B0C") },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedLabelColor = Color.Cyan,
                        unfocusedLabelColor = Color.White,
                        focusedBorderColor = Color.Cyan,
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        unfocusedBorderColor = Color.White,
                        cursorColor = Color.Cyan,
                        selectionColors = TextSelectionColors(
                            handleColor = Color.Black,
                            backgroundColor = Color(0x5500DDFF),
                        )
                    )
                )

                wifiPassword = if (wifiPassword == "null") "" else wifiPassword
                OutlinedTextField(
                    modifier = Modifier.padding(3.dp),
                    value = wifiPassword,
                    onValueChange = { newValue ->
                        wifiPassword = newValue
                        event = event.copy(config = event.config?.copy(staPass = newValue))
                    },
                    label = { Text("Password") },
                    placeholder = { Text("e.g., xxxxxxxxxxx") },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedLabelColor = Color.Cyan,
                        unfocusedLabelColor = Color.White,
                        focusedBorderColor = Color.Cyan,
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        unfocusedBorderColor = Color.White,
                        cursorColor = Color.Cyan,
                        selectionColors = TextSelectionColors(
                            handleColor = Color.Black,
                            backgroundColor = Color(0x5500DDFF),
                        )
                    )
                )

                Divider(
                    color = Color.DarkGray,
                    thickness = 1.dp,
                    modifier = Modifier.padding(16.dp)
                )

                UiComponentFactory().Label(
                    "MQTT Configuration",
                    color = Color(0xFFFFFFFF),
                    fontSize = 16.sp,
                    textAlign = TextAlign.Left,
                    textModifier
                )

                OutlinedTextField(
                    modifier = Modifier.padding(3.dp),
                    value = mqttServer,
                    onValueChange = { newValue ->
                        mqttServer = newValue
                        event = event.copy(config = event.config?.copy(mqttServer = newValue))
                    },
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
                        selectionColors = TextSelectionColors(
                            handleColor = Color.Black,
                            backgroundColor = Color(0x5500DDFF),
                        )
                    )
                )

                OutlinedTextField(
                    modifier = Modifier.padding(3.dp),
                    value = mqttPort.toString(),
                    onValueChange = { newValue ->
                        mqttPort = newValue.toInt()
                        event = event.copy(config = event.config?.copy(mqttPort = newValue.toInt()))
                    },
                    label = { Text("MQTT Port") },
                    placeholder = { Text("e.g., myuser") },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedLabelColor = Color.Cyan,
                        unfocusedLabelColor = Color.White,
                        focusedBorderColor = Color.Cyan,
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        unfocusedBorderColor = Color.White,
                        cursorColor = Color.Cyan,
                        selectionColors = TextSelectionColors(
                            handleColor = Color.Black,
                            backgroundColor = Color(0x5500DDFF),
                        )
                    )
                )

                OutlinedTextField(
                    modifier = Modifier.padding(3.dp),
                    value = mqttUser,
                    onValueChange = { newValue ->
                        mqttUser = newValue
                        event = event.copy(config = event.config?.copy(mqttUser = newValue))
                    },
                    label = { Text("MQTT User") },
                    placeholder = { Text("e.g., mymqttuser") },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedLabelColor = Color.Cyan,
                        unfocusedLabelColor = Color.White,
                        focusedBorderColor = Color.Cyan,
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        unfocusedBorderColor = Color.White,
                        cursorColor = Color.Cyan,
                        selectionColors = TextSelectionColors(
                            handleColor = Color.Black,
                            backgroundColor = Color(0x5500DDFF),
                        )
                    )
                )

                OutlinedTextField(
                    modifier = Modifier.padding(3.dp),
                    value = mqttPassword,
                    onValueChange = { newValue ->
                        mqttPassword = newValue
                        event = event.copy(config = event.config?.copy(mqttPassword = newValue))
                    },
                    label = { Text("MQTT Password") },
                    placeholder = { Text("e.g., *************") },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedLabelColor = Color.Cyan,
                        unfocusedLabelColor = Color.White,
                        focusedBorderColor = Color.Cyan,
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        unfocusedBorderColor = Color.White,
                        cursorColor = Color.Cyan,
                        selectionColors = TextSelectionColors(
                            handleColor = Color.Black,
                            backgroundColor = Color(0x5500DDFF),
                        )
                    )
                )

                Divider(
                    color = Color.DarkGray,
                    thickness = 1.dp,
                    modifier = Modifier.padding(16.dp)
                )

                UiComponentFactory().Label(
                    "Ports",
                    color = Color(0xFFFFFFFF),
                    fontSize = 16.sp,
                    textAlign = TextAlign.Left,
                    textModifier
                )

                Row(
                    Modifier
                        .padding(4.dp)
                        .fillMaxWidth()
                        .background(Color(0xFF004D4D)),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    Column(
                        Modifier.width(40.dp)
                    ) {
                        Text("Pin")
                    }
                    Column(
                        Modifier.width(100.dp)
                    ) {
                        Text("Alias")
                    }
                    Column(
                        Modifier.width(60.dp)
                    ) {
                        Text("Mode")
                    }
                    Column(
                        Modifier.width(40.dp)
                    ) {
                        Text("I/O")
                    }
                    Column(
                        Modifier.width(60.dp)
                    ) {
                        Text("Value")
                    }
                    Column(
                        Modifier.width(20.dp)
                    ) {
                        // Empty
                    }
                }

                for ((index, port) in ports.withIndex()) {
                    Row(
                        Modifier
                            .padding(2.dp)
                            .fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        Column(
                            Modifier.width(40.dp)
                        ) {
                            Text(port.pin.toString())
                        }
                        Column(
                            Modifier.width(100.dp)
                        ) {
                            Text(port.alias.trim())
                        }
                        Column(
                            Modifier.width(60.dp)
                        ) {
                            Text(port.mode.trim())
                        }
                        Column(
                            Modifier.width(40.dp)
                        ) {
                            Text(port.type.toString())
                        }
                        Column(
                            Modifier.width(60.dp)
                        ) {
                            var checked by remember { mutableStateOf(port.value == "H") }
                            Switch(
                                checked = checked,
                                onCheckedChange = {
                                    checked = it
                                    val value = if (it) "H" else "L"
                                    val portConfigurationString =
                                        "---SET_PORT: {\"pin\":\"${port.pin}\",\"value\":\"${value}\"}"
                                    SerialPortImpl.write(
                                        portConfigurationString.toByteArray(
                                            Charsets.UTF_8
                                        )
                                    )
                                    ports[index] = port.copy(value = value)
                                    event = event.copy(config = event.config!!.copy(ports = ports))
                                },
                                colors = SwitchDefaults.colors(
                                    checkedThumbColor = Color.Green,
                                    checkedTrackColor = Color.DarkGray,
                                    uncheckedThumbColor = Color.White,
                                    uncheckedTrackColor = Color.LightGray,
                                )
                            )
                        }
                        Column(
                            Modifier.width(20.dp)
                        ) {
                            IconButton(onClick = {
                                // None
                            }) {
                                Icon(
                                    imageVector = Icons.Filled.Edit,
                                    contentDescription = "Edit"
                                )
                            }
                        }
                    }
                    Divider(color = Color.DarkGray, thickness = 1.dp)
                }

                var isAdding by remember { mutableStateOf(false) }
                var portFormValue: Any = {}

                if (isAdding) {
                    PortsFormDialog(
                        onDismissRequest = { isAdding = false },
                        onConfirmation = { port ->
                            ports.add(port)
                            event = event.copy(config = event.config!!.copy(ports = ports))
                            isAdding = false
                        }
                    )
                }

                Row(
                    Modifier
                        .padding(2.dp)
                        .fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    IconButton(onClick = {
                        isAdding = true
                    }) {
                        Icon(
                            imageVector = Icons.Sharp.AddCircle,
                            contentDescription = "Add Element"
                        )
                    }
                }
                Divider(color = Color.DarkGray, thickness = 1.dp)

                Row {
                    Button(
                        onClick = {
                            var strConfig = Json.encodeToString(event.config)
                            val setConfigCommand = "---set_config:${strConfig}"
                            SerialPortImpl.write(
                                setConfigCommand.toByteArray(
                                    Charsets.UTF_8
                                )
                            )
                            println(event)
                            isInitialized = false
                          },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF00DDFF),
                            contentColor = Color(0xFF223240)
                        ),
                        modifier = Modifier
                            .width(150.dp)
                            .background(Color.Black)
                            .padding(20.dp)
                            .height(36.dp) // Ajusta el tamaño aquí
                    ) {
                        Icon(
                            imageVector = Icons.Filled.PlayArrow,
                            contentDescription = "Set configuration"
                        )
                    }
                }
            }
        }
    }
}


@Composable
fun FormRow(onPortFormChanged: (Port) -> Unit) {
    var pin by remember { mutableStateOf("0") }
    var alias by remember { mutableStateOf("") }
    var selectedMode by remember { mutableStateOf("DIGITAL") }
    var selectedType by remember { mutableStateOf("OUTPUT") }

    Row(
        Modifier
            .padding(2.dp)
            .fillMaxWidth().background(Color(0xFF223240)),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceEvenly
    ) {
        Column(
            Modifier.width(100.dp)
        ) {
            OutlinedTextField(
                modifier = Modifier.padding(3.dp).width(100.dp),
                value = pin.toString(),
                onValueChange = {
                    newValue -> pin = newValue
                    val port = setFormValueAsPort(pin, alias, selectedMode, selectedType)
                    onPortFormChanged(port)
                                },
                label = { Text("Pin") },
                placeholder = { Text("1") },
                colors = OutlinedTextFieldDefaults.colors(
                    focusedLabelColor = Color.Cyan,
                    unfocusedLabelColor = Color.White,
                    focusedBorderColor = Color.Cyan,
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White,
                    unfocusedBorderColor = Color.White,
                    cursorColor = Color.Cyan,
                    selectionColors = TextSelectionColors(
                        handleColor = Color.Black,
                        backgroundColor = Color(0x5500DDFF),
                    )
                )
            )
        }
        Column(
            Modifier.width(300.dp)
        ) {
            OutlinedTextField(
                modifier = Modifier.padding(3.dp).width(300.dp),
                value = alias,
                onValueChange = { newValue ->
                    alias = newValue
                    val port = setFormValueAsPort(pin, alias, selectedMode, selectedType)
                    onPortFormChanged(port)
                                },
                label = { Text("Alias") },
                placeholder = { Text("ALIAS") },
                colors = OutlinedTextFieldDefaults.colors(
                    focusedLabelColor = Color.Cyan,
                    unfocusedLabelColor = Color.White,
                    focusedBorderColor = Color.Cyan,
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White,
                    unfocusedBorderColor = Color.White,
                    cursorColor = Color.Cyan,
                    selectionColors = TextSelectionColors(
                        handleColor = Color.Black,
                        backgroundColor = Color(0x5500DDFF),
                    )
                )
            )
        }
    }

    Row(
        Modifier
            .padding(5.dp)
            .fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        DynamicSelectTextField(
            selectedValue = selectedMode,
            options = arrayListOf("DIGITAL", "ANALOG"),
            label = "Mode",
            onValueChangedEvent = { newValue ->
                selectedMode = newValue
                val port = setFormValueAsPort(pin, alias, selectedMode, selectedType)
                onPortFormChanged(port)
            }
        )
    }
    Row(
        Modifier
            .padding(5.dp)
            .fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        DynamicSelectTextField(
            selectedValue = selectedType,
            options = arrayListOf("INPUT", "OUTPUT"),
            label = "Type",
            onValueChangedEvent = { newValue ->
                selectedType = newValue
                val port = setFormValueAsPort(pin, alias, selectedMode, selectedType)
                onPortFormChanged(port)
            }
        )
    }
}

fun setFormValueAsPort(pin: String, alias: String, mode:String, type:String): Port {
    var decodedType = if(type == "INPUT") 1 else 0
    var decodedMode = if(mode == "ANALOG" || mode =="A") "A" else "D"
    var decodedPin = if(pin == "") 0 else pin.toInt()
    var port = Port(pin=decodedPin, alias=alias, mode=decodedMode, type=decodedType, value="L")
    return port
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterialApi::class)
@Composable
fun DynamicSelectTextField(
    selectedValue: String,
    options: List<String>,
    label: String,
    onValueChangedEvent: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    var expanded by remember { mutableStateOf(false) }

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = !expanded },
        modifier = modifier
    ) {
        OutlinedTextField(
            readOnly = true,
            value = selectedValue,
            onValueChange = { newValue -> onValueChangedEvent(newValue) },
            label = { Text(text = label) },
            trailingIcon = {
                ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded)
            },
            colors = OutlinedTextFieldDefaults.colors(
                focusedLabelColor = Color.Cyan,
                unfocusedLabelColor = Color.White,
                focusedBorderColor = Color.Cyan,
                focusedTextColor = Color.White,
                unfocusedTextColor = Color.White,
                unfocusedBorderColor = Color.White,
                cursorColor = Color.Cyan,
                selectionColors = TextSelectionColors(
                    handleColor = Color.Black,
                    backgroundColor = Color(0x5500DDFF),
                )
            ),
            modifier = Modifier
                .fillMaxWidth()
        )

        ExposedDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            options.forEach { option: String ->
                DropdownMenuItem(
                    text = { Text(text = option) },
                    onClick = {
                        expanded = false
                        onValueChangedEvent(option)
                    }
                )
            }
        }
    }
}

@Composable
fun PortsFormDialog(
    onDismissRequest: () -> Unit,
    onConfirmation: (Port) -> Unit,
) {
    var portFormValue = setFormValueAsPort("0", "", "D", "1")
    Dialog(onDismissRequest = { onDismissRequest() }) {
        // Draw a rectangle shape with rounded corners inside the dialog
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .height(400.dp)
                .width(290.dp)
                .padding(16.dp)
                .background(Color(0xFF223240))
        ) {

            Column(Modifier.background(Color(0xBB223240)).fillMaxWidth()) {
                val textModifier: Modifier = Modifier.padding(10.dp)
                UiComponentFactory().Label(
                    "Port Configuration",
                    color = Color(0xFFFFFFFF),
                    fontSize = 16.sp,
                    textAlign = TextAlign.Left,
                    textModifier
                )
            }

            Column(
                modifier = Modifier
                    .fillMaxSize().background(Color(0xFF223240)),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                /** PORTS FORM ***/
                FormRow(
                    onPortFormChanged={
                        portFormValue = it

                    }
                )

                Row(
                    modifier = Modifier
                        .fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center,
                ) {
                    TextButton(
                        onClick = { onDismissRequest() },
                        modifier = Modifier.padding(2.dp),
                        colors = ButtonDefaults.textButtonColors(
                            contentColor = Color.Red
                        )
                    ) {
                        Text("Dismiss")
                    }
                    TextButton(
                        onClick = {
                            onConfirmation(portFormValue)
                            print("PORT OPTIONS: $portFormValue")
                                  },
                        modifier = Modifier.padding(2.dp),
                        colors = ButtonDefaults.textButtonColors(
                            contentColor = Color.Cyan
                        )
                    ) {
                        Text("Confirm")
                    }
                }
            }
        }
    }
}

