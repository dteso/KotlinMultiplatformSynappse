package components.devices

import Action
import Event
import Port
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
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
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DateRange
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import components.UiComponentFactory
import io.ktor.utils.io.charsets.Charsets
import io.ktor.utils.io.core.toByteArray
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import utils.serial_port.SerialPortImpl

@Composable
fun UsbDeviceSetup() {
    val receivedData by SerialPortImpl.receivedData.collectAsState()

    NewDeviceForm(
        receivedData = receivedData,
    )
}

@Composable
fun NewDeviceForm(
    receivedData: String,
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
    var mqttEnabled by remember { mutableStateOf(false) }
    var mqttConnected by remember { mutableStateOf(false) }
    var wifiEnabled by remember { mutableStateOf(false) }
    var wifiConnected by remember { mutableStateOf(false) }

    var ports by remember { mutableStateOf<MutableList<Port>>(mutableListOf()) }
    var actions by remember { mutableStateOf<MutableList<Action>>(mutableListOf()) }

    var event by remember { mutableStateOf(Event(null, null, null)) }

    var onProcessingModalOpen by remember { mutableStateOf(false) }
    var editing by remember { mutableStateOf(false) }

    val dataLines = receivedData.split("\n").asReversed()
    var isInitialized by remember { mutableStateOf(false) }

    var statusReceivedIndex = dataLines.indexOfFirst { it.startsWith("[SYNAPPSE.STATUS]:") }
    var confirmedBootIndex = dataLines.indexOfFirst { it.startsWith("[SYNAPPSE.START]") }

    var updateFormStatus =
        statusReceivedIndex in 0..2 && dataLines[statusReceivedIndex].startsWith("[SYNAPPSE.STATUS]:") && !editing;
    var bootConfirmationReceived =
        confirmedBootIndex in 0..4 && dataLines[confirmedBootIndex].startsWith("[SYNAPPSE.START]");

    if (bootConfirmationReceived) {
        isInitialized = true
        editing = false
    }

    if (updateFormStatus) {
        val statusJson = dataLines[statusReceivedIndex].removePrefix("[SYNAPPSE.STATUS]:").trim()
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
//            timeAlive = newEvent.status!!["timeAlive"].toString()
            mqttServer = newEvent.config!!.mqttServer
            mqttPort = newEvent.config!!.mqttPort
            mqttUser = newEvent.config!!.mqttUser
            mqttPassword = newEvent.config!!.mqttPassword
            mqttEnabled = newEvent.config!!.mqttEnabled
            mqttConnected = newEvent.config!!.mqttConnected
            wifiConnected = newEvent.config!!.wifiConnected
            wifiEnabled = newEvent.config!!.staEnabled
            ports = newEvent.config!!.ports?.toMutableList() ?: mutableListOf()
            actions = newEvent.config!!.actions?.toMutableList() ?: mutableListOf()
            event = newEvent
            println("STATUS UPDATE SUCCESS!!!" + event)
        } catch (e: Exception) {
            println("Failed to decode JSON: ${e.message}")
        }
    }

    Divider(color = Color.DarkGray, thickness = 1.dp, modifier = Modifier.padding(top = 5.dp))
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .background(Color(0xFF0E131C))
            .padding(8.dp)
    ) {
        LazyColumn(
            Modifier.fillMaxWidth(),
            userScrollEnabled = true,
            horizontalAlignment = Alignment.Start
        ) {
            val textModifier: Modifier = Modifier.padding(horizontal = 5.dp).width(200.dp)
            val titleTextModifier: Modifier = Modifier.padding(vertical = 15.dp).width(300.dp)

            item {
                Row(
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    verticalAlignment = Alignment.CenterVertically){
                    UiComponentFactory().Label(
                        "Device setup",
                        color = Color(0xFF2277AA),
                        fontSize = 25.sp,
                        textAlign = TextAlign.Left,
                        titleTextModifier
                    )

                    Spacer(modifier = Modifier.weight(1f))

                    Button(
                        onClick = {
                            var deviceConfig = event.config?.copy()
                            deviceConfig!!.ports = mutableListOf()
                            deviceConfig!!.actions = mutableListOf()
                            var strConfig = Json.encodeToString(deviceConfig)
                            strConfig =
                                strConfig.replace("true", "\"true\"").replace("false", "\"false\"")
                            val setConfigCommand = "---set_config:${strConfig}"
                            SerialPortImpl.write(
                                setConfigCommand.toByteArray(
                                    Charsets.UTF_8
                                )
                            )
                            println(event)
                            isInitialized = false
                            onProcessingModalOpen = true
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF00DDFF),
                            contentColor = Color(0xFF0E131C)
                        ),
                        modifier = Modifier
                            .background(Color(0xFF0E131C))
                            .height(36.dp)
                    ) {
                        Text("SET CONFIGURATION")
                    }
                }


                deviceName = if (deviceName == "null") "" else deviceName
                OutlinedTextField(
                    modifier = Modifier.padding(3.dp),
                    value = deviceName,
                    onValueChange = { newValue ->
                        editing = true
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
                            handleColor = Color(0xFF0E131C),
                            backgroundColor = Color(0x5500DDFF),
                        )
                    )
                )

                OutlinedTextField(
                    modifier = Modifier.padding(3.dp),
                    value = mac,
                    onValueChange = { newValue ->
                        mac = newValue
                        editing = true
                    },
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
                            handleColor = Color(0xFF0E131C),
                            backgroundColor = Color(0x5500DDFF),
                        )
                    )
                )

                Divider(
                    color = Color.DarkGray,
                    thickness = 1.dp,
                    modifier = Modifier.padding(16.dp)
                )

                Row(
                    modifier = Modifier.fillParentMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    UiComponentFactory().Label(
                        "WiFi Enabled",
                        color = Color(0xFFFFFFFF),
                        fontSize = 16.sp,
                        textAlign = TextAlign.Left,
                        textModifier
                    )

                    Spacer(modifier = Modifier.weight(1f))

                    Column(horizontalAlignment = Alignment.End) {
                        Switch(
                            checked = wifiEnabled,
                            onCheckedChange = {
                                wifiEnabled = it
                                event = event.copy(config = event.config!!.copy(staEnabled = it))
                            },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = Color.Green,
                                checkedTrackColor = Color.DarkGray,
                                uncheckedThumbColor = Color.White,
                                uncheckedTrackColor = Color.LightGray,
                            )
                        )
                    }
                }

                Row(
                    modifier = Modifier.fillParentMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    UiComponentFactory().Label(
                        "Online",
                        color = Color(0xFFFFFFFF),
                        fontSize = 16.sp,
                        textAlign = TextAlign.Left,
                        textModifier
                    )
                    Spacer(modifier = Modifier.weight(1f))

                    Column(horizontalAlignment = Alignment.End) {
                        Switch(
                            checked = wifiConnected,
                            enabled = false,
                            onCheckedChange = {
                                wifiConnected = it
                                event = event.copy(config = event.config!!.copy(wifiConnected = it))
                            },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = Color.Green,
                                checkedTrackColor = Color.DarkGray,
                                uncheckedThumbColor = Color.White,
                                uncheckedTrackColor = Color.LightGray,
                            )
                        )
                    }
                }

                OutlinedTextField(
                    modifier = Modifier.padding(3.dp),
                    value = ip,
                    onValueChange = { newValue ->
                        ip = newValue
                        editing = true
                    },
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
                            handleColor = Color(0xFF0E131C),
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
                        editing = true
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
                            handleColor = Color(0xFF0E131C),
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
                        editing = true
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
                            handleColor = Color(0xFF0E131C),
                            backgroundColor = Color(0x5500DDFF),
                        )
                    )
                )

                Divider(
                    color = Color.DarkGray,
                    thickness = 1.dp,
                    modifier = Modifier.padding(16.dp)
                )

                Row(
                    modifier = Modifier.fillParentMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    UiComponentFactory().Label(
                        "MQTT Enabled",
                        color = Color(0xFFFFFFFF),
                        fontSize = 16.sp,
                        textAlign = TextAlign.Left,
                        textModifier
                    )

                    Spacer(modifier = Modifier.weight(1f))

                    Column(horizontalAlignment = Alignment.End) {
                        Switch(
                            checked = mqttEnabled,
                            onCheckedChange = {
                                mqttEnabled = it
                                event = event.copy(config = event.config!!.copy(mqttEnabled = it))
                            },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = Color(0xFFFF00FF),
                                checkedTrackColor = Color.DarkGray,
                                uncheckedThumbColor = Color.White,
                                uncheckedTrackColor = Color.LightGray,
                            )
                        )
                    }
                }

                Row(
                    modifier = Modifier.fillParentMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    UiComponentFactory().Label(
                        "MQTT Online",
                        color = Color(0xFFFFFFFF),
                        fontSize = 16.sp,
                        textAlign = TextAlign.Left,
                        textModifier
                    )

                    Spacer(modifier = Modifier.weight(1f))

                    Column(horizontalAlignment = Alignment.End) {
                        Switch(
                            checked = mqttConnected,
                            enabled = false,
                            onCheckedChange = {
                                mqttConnected = it
                                event = event.copy(config = event.config!!.copy(mqttConnected = it))
                            },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = Color(0xFFFF00FF),
                                checkedTrackColor = Color.DarkGray,
                                uncheckedThumbColor = Color.White,
                                uncheckedTrackColor = Color.LightGray,
                            )
                        )
                    }
                }

                OutlinedTextField(
                    modifier = Modifier.padding(3.dp),
                    value = mqttServer,
                    onValueChange = { newValue ->
                        mqttServer = newValue
                        event = event.copy(config = event.config?.copy(mqttServer = newValue))
                        editing = true
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
                            handleColor = Color(0xFF0E131C),
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
                        editing = true
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
                            handleColor = Color(0xFF0E131C),
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
                        editing = true
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
                            handleColor = Color(0xFF0E131C),
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
                        editing = true
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
                            handleColor = Color(0xFF0E131C),
                            backgroundColor = Color(0x5500DDFF),
                        )
                    )
                )

                Divider(
                    color = Color.White,
                    thickness = 1.dp,
                    modifier = Modifier.padding(top = 16.dp, bottom = 16.dp)
                )

                /*******************
                 *  PORTS
                 * *****************/
                PortsManager(
                    event,
                    ports,
                    titleTextModifier,
                    onEditing = {
                        editing = it
                    },
                    onInitialized = {
                        isInitialized = it
                    },
                    onProcessingModalOpen = {
                        onProcessingModalOpen = it
                    },
                    onCurrentPortsDelete = {
                        ports = it
                    }
                )

                /*******************
                 *  ACTIONS
                 * *****************/
                ActionsManager(
                    event,
                    actions,
                    titleTextModifier,
                    onEditing = {
                        editing = it
                    },
                    onInitialized = {
                        isInitialized = it
                    },
                    onProcessingModalOpen = {
                        onProcessingModalOpen = it
                    },
                    onCurrentActionsDelete = {
                        actions = it
                    }
                )



                /*******************
                 *  STATUS
                 * *****************/
                UiComponentFactory().Label(
                    "Status",
                    color = Color(0xFF2277AA),
                    fontSize = 25.sp,
                    textAlign = TextAlign.Left,
                    titleTextModifier
                )

                Divider(color = Color.DarkGray, thickness = 1.dp)
                val status: JsonObject? = event.status
                status?.let {
                    for ((key, value) in it.entries) {
                        println("key: $key, value: $value")
                        Row(
                            Modifier
                                .padding(2.dp)
                                .fillMaxWidth()
                                .height(60.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceEvenly
                        ) {
                            Column(
                                Modifier.width(200.dp),
                                horizontalAlignment = Alignment.Start
                            ) {
                                Text(text = key.replace("\"", ""))
                            }
                            Column(
                                Modifier.width(1080.dp),
                                horizontalAlignment = Alignment.End
                            ) {
                                Text(value.toString().replace("\"", ""))
                            }
                        }
                        Divider(color = Color.DarkGray, thickness = 1.dp)
                    }
                }

                if (onProcessingModalOpen) {
                    OnProcessingDialog(
                        onDismissRequest = { onProcessingModalOpen = false },
                        onConfirmation = {
                            onProcessingModalOpen = false
                            isInitialized = false
                            val readStatusCmd: String = "---read_status"
                            SerialPortImpl.write(
                                readStatusCmd.toByteArray(
                                    Charsets.UTF_8
                                )
                            )
                            editing = false
                        },
                        isInitialized = isInitialized
                    )
                }
            }
        }
    }
}


@Composable
fun PortsManager(event: Event, ports: MutableList<Port>, titleTextModifier: Modifier, onEditing: (Boolean) -> Unit, onInitialized: (Boolean) -> Unit, onProcessingModalOpen: (Boolean) -> Unit, onCurrentPortsDelete: (MutableList<Port>) -> Unit){
    UiComponentFactory().Label(
        "GPIO definition",
        color = Color(0xFF2277AA),
        fontSize = 25.sp,
        textAlign = TextAlign.Left,
        titleTextModifier
    )

    Row(
        Modifier
            .padding(4.dp)
            .fillMaxWidth()
            .background(Color(0xFF2277AA)),
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
                        event.config = event.config!!.copy(ports = ports)
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
                Modifier.width(30.dp)
            ) {
                IconButton(onClick = {
                    // None
                }) {
                    Icon(
                        imageVector = Icons.Filled.Edit,
                        contentDescription = "Edit",
                        tint = Color.Cyan
                    )
                }
            }
            Column(
                Modifier.width(30.dp)
            ) {
                IconButton(onClick = {
                    var currentPorts = ports.filterIndexed { i, _ -> i != index }.toMutableList()
                    onCurrentPortsDelete(currentPorts)
                    event.config = event.config!!.copy(ports = currentPorts)
                }) {
                    Icon(
                        imageVector = Icons.Filled.Close,
                        contentDescription = "Close",
                        tint = Color.Red
                    )
                }
            }
        }
        Divider(color = Color.DarkGray, thickness = 1.dp)
    }
    var isAdding by remember { mutableStateOf(false) }
    if (isAdding) {
        onEditing(true)
        PortsFormDialog(
            onDismissRequest = { isAdding = false },
            onConfirmation = { port ->
                ports.add(port)
                event.config = event.config!!.copy(ports = ports)
                isAdding = false
            }
        )
    }
    Row(
        Modifier
            .padding(2.dp)
            .fillMaxWidth()
            .background(Color.Transparent),
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
//    Divider(color = Color.DarkGray, thickness = 1.dp)
    Row {
        Button(
            onClick = {
                var strPortsConfig = Json.encodeToString(event.config!!.ports)
                strPortsConfig =
                    strPortsConfig.replace("true", "\"true\"").replace("false", "\"false\"")
                val setConfigCommand = "---set_config:{ports:${strPortsConfig}}"
                SerialPortImpl.write(
                    setConfigCommand.toByteArray(
                        Charsets.UTF_8
                    )
                )
                println(event)
                onInitialized(false)
                onProcessingModalOpen(true)
            },
            colors = ButtonDefaults.buttonColors(
                containerColor = Color(0xFF00DDFF),
                contentColor = Color(0xFF0E131C)
            ),
            modifier = Modifier
                .fillMaxWidth()
                .background(Color(0xFF0E131C))
                .padding(20.dp)
                .height(36.dp)
        ) {
            Text("SET PORTS")
        }
    }
    Divider(
        color = Color.White,
        thickness = 1.dp,
        modifier = Modifier.padding(top = 16.dp, bottom = 16.dp)
    )
}


@Composable
fun ActionsManager(event: Event, actions: MutableList<Action>, titleTextModifier: Modifier, onEditing: (Boolean) -> Unit, onInitialized: (Boolean) -> Unit, onProcessingModalOpen: (Boolean) -> Unit, onCurrentActionsDelete: (MutableList<Action>) -> Unit){
    UiComponentFactory().Label(
        "Actions configuration",
        color = Color(0xFF2277AA),
        fontSize = 25.sp,
        textAlign = TextAlign.Left,
        titleTextModifier
    )

    var selectedAction by remember { mutableStateOf(Action()) }
    var selectedIndex by remember { mutableStateOf(-1) }
    var isAddingAction by remember { mutableStateOf(false) }
    for ((index, action) in actions.withIndex()) {
        Row(
            Modifier
                .padding(2.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            Column(
                Modifier.width(200.dp),
                horizontalAlignment = Alignment.Start
            ) {
                Text(action.actionName!!.uppercase())
            }
            if(action.scheduled != null && action.scheduled != "" && action.scheduled.uppercase() != "NONE"){
                Column(
                    Modifier.width(30.dp)
                ) {
                    IconButton(onClick = {
                        //None
                    }) {
                        Icon(
                            imageVector = Icons.Filled.DateRange,
                            contentDescription = "Scheduled",
                            tint = Color.Cyan
                        )
                    }
                }
            }
            Spacer(Modifier.weight(1f))
            Column(
                Modifier.width(30.dp)
            ) {
                IconButton(onClick = {
                    selectedIndex = index
                    selectedAction = actions[index]
                    isAddingAction = true
                }) {
                    Icon(
                        imageVector = Icons.Filled.Edit,
                        contentDescription = "Edit",
                        tint = Color.Cyan
                    )
                }
            }
            Column(
                Modifier.width(30.dp)
            ) {
                IconButton(onClick = {
                    val executeCommand = "---exec:${action.callbackName}|${action.arguments}"
                    SerialPortImpl.write(
                        executeCommand.toByteArray(
                            Charsets.UTF_8
                        )
                    )
                }) {
                    Icon(
                        imageVector = Icons.Filled.PlayArrow,
                        contentDescription = "Run",
                        tint = Color.Green
                    )
                }
            }
            Column(
                Modifier.width(30.dp)
            ) {
                IconButton(onClick = {
                    var currentActions = actions.filterIndexed { i, _ -> i != index }.toMutableList()
                    onCurrentActionsDelete(currentActions)
                    event.config = event.config!!.copy(actions = currentActions)
                }) {
                    Icon(
                        imageVector = Icons.Filled.Close,
                        contentDescription = "Close",
                        tint = Color.Red
                    )
                }
            }
        }
        Divider(color = Color.DarkGray, thickness = 1.dp)
    }

    if (isAddingAction) {
        onEditing(true)
        ActionsFormDialog(
            selectedAction= selectedAction,
            onDismissRequest = {
                isAddingAction = false
                selectedAction = Action()
                selectedIndex = -1
                               },
            onConfirmation = { action ->
                if (selectedAction != null && selectedIndex != -1){
                    actions[selectedIndex] = action
                    selectedAction = Action()
                    selectedIndex = -1
                }else{
                    actions.add(action)
                }
                event.config = event.config!!.copy(actions = actions)
                isAddingAction = false
            }
        )
    }
    Row(
        Modifier
            .padding(2.dp)
            .fillMaxWidth()
            .background(Color.Transparent),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center
    ) {
        IconButton(onClick = {
            isAddingAction = true
        }) {
            Icon(
                imageVector = Icons.Sharp.AddCircle,
                contentDescription = "Add Element"
            )
        }
    }
//    Divider(color = Color.DarkGray, thickness = 1.dp)
    Row {
        Button(
            onClick = {
                var strActionsConfig = Json.encodeToString(event.config!!.actions)
                strActionsConfig =
                    strActionsConfig.replace("true", "\"true\"").replace("false", "\"false\"")
                val setConfigCommand = "---set_config:{actions:${strActionsConfig}}"
                SerialPortImpl.write(
                    setConfigCommand.toByteArray(
                        Charsets.UTF_8
                    )
                )
                println(event)
                onInitialized(false)
                onProcessingModalOpen(true)
            },
            colors = ButtonDefaults.buttonColors(
                containerColor = Color(0xFF00DDFF),
                contentColor = Color(0xFF0E131C)
            ),
            modifier = Modifier
                .fillMaxWidth()
                .background(Color(0xFF0E131C))
                .padding(20.dp)
                .height(36.dp)
        ) {
            Text("SET ACTIONS")
        }
    }
    Divider(
        color = Color.White,
        thickness = 1.dp,
        modifier = Modifier.padding(top = 16.dp, bottom = 16.dp)
    )
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
            .fillMaxWidth().background(Color(0xFF0E131C)),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceEvenly
    ) {
        Column(
            Modifier.width(100.dp)
        ) {
            OutlinedTextField(
                modifier = Modifier.padding(3.dp).width(100.dp),
                value = pin.toString(),
                onValueChange = { newValue ->
                    pin = newValue
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
                        handleColor = Color(0xFF0E131C),
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
                        handleColor = Color(0xFF0E131C),
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

@Composable
fun FormActionRow(selectedAction: Action, onActionFormChanged: (Action) -> Unit) {
    var actionName by remember { if (selectedAction!=null) mutableStateOf(selectedAction.actionName.toString()) else mutableStateOf("SET_WHATEVER") }
    var scheduled by remember { if (selectedAction!=null) mutableStateOf(selectedAction.scheduled.toString()) else mutableStateOf("XX:XX:XXXX:XX:XX:00") }
    var callbackName by remember { if (selectedAction!=null) mutableStateOf(selectedAction.callbackName.toString()) else mutableStateOf("my_function") }
    var arguments by remember { if (selectedAction!=null) mutableStateOf(selectedAction.arguments.toString()) else mutableStateOf("{'key':'value'}") }

    Row(
        Modifier
            .padding(2.dp)
            .fillMaxWidth().background(Color(0xFF0E131C)),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center
    ) {
        OutlinedTextField(
            modifier = Modifier.padding(3.dp),
            value = actionName,
            onValueChange = { newValue ->
                actionName = newValue
                val action = setFormValueAsAction(actionName, scheduled, arguments, callbackName)
                onActionFormChanged(action)
            },
            label = { Text("Name") },
            placeholder = { Text("MY_ACTION") },
            colors = OutlinedTextFieldDefaults.colors(
                focusedLabelColor = Color.Cyan,
                unfocusedLabelColor = Color.White,
                focusedBorderColor = Color.Cyan,
                focusedTextColor = Color.White,
                unfocusedTextColor = Color.White,
                unfocusedBorderColor = Color.White,
                cursorColor = Color.Cyan,
                selectionColors = TextSelectionColors(
                    handleColor = Color(0xFF0E131C),
                    backgroundColor = Color(0x5500DDFF),
                )
            )
        )
    }
    Row(
        Modifier
            .padding(2.dp)
            .fillMaxWidth().background(Color(0xFF0E131C)),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center
    ) {
        OutlinedTextField(
            modifier = Modifier.padding(3.dp),
            value = scheduled,
            onValueChange = { newValue ->
                scheduled = newValue
                val action = setFormValueAsAction(actionName, scheduled, arguments, callbackName)
                onActionFormChanged(action)
            },
            label = { Text("Scheduled") },
            placeholder = { Text("XX:XX:XXXX:XX:XX:00") },
            colors = OutlinedTextFieldDefaults.colors(
                focusedLabelColor = Color.Cyan,
                unfocusedLabelColor = Color.White,
                focusedBorderColor = Color.Cyan,
                focusedTextColor = Color.White,
                unfocusedTextColor = Color.White,
                unfocusedBorderColor = Color.White,
                cursorColor = Color.Cyan,
                selectionColors = TextSelectionColors(
                    handleColor = Color(0xFF0E131C),
                    backgroundColor = Color(0x5500DDFF),
                )
            )
        )
    }
    Row(
        Modifier
            .padding(2.dp)
            .fillMaxWidth().background(Color(0xFF0E131C)),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center
    ) {
        OutlinedTextField(
            modifier = Modifier.padding(3.dp),
            value = arguments,
            onValueChange = { newValue ->
                arguments = newValue
                val action = setFormValueAsAction(actionName, scheduled, arguments, callbackName)
                onActionFormChanged(action)
            },
            label = { Text("Arguments") },
            placeholder = { Text("{'key':'value'}") },
            colors = OutlinedTextFieldDefaults.colors(
                focusedLabelColor = Color.Cyan,
                unfocusedLabelColor = Color.White,
                focusedBorderColor = Color.Cyan,
                focusedTextColor = Color.White,
                unfocusedTextColor = Color.White,
                unfocusedBorderColor = Color.White,
                cursorColor = Color.Cyan,
                selectionColors = TextSelectionColors(
                    handleColor = Color(0xFF0E131C),
                    backgroundColor = Color(0x5500DDFF),
                )
            )
        )
    }
    Row(
        Modifier
            .padding(2.dp)
            .fillMaxWidth().background(Color(0xFF0E131C)),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center
    ) {
        OutlinedTextField(
            modifier = Modifier.padding(3.dp),
            value = callbackName,
            onValueChange = { newValue ->
                callbackName = newValue
                val action = setFormValueAsAction(actionName, scheduled, arguments, callbackName)
                onActionFormChanged(action)
            },
            label = { Text("Function") },
            placeholder = { Text("my_function") },
            colors = OutlinedTextFieldDefaults.colors(
                focusedLabelColor = Color.Cyan,
                unfocusedLabelColor = Color.White,
                focusedBorderColor = Color.Cyan,
                focusedTextColor = Color.White,
                unfocusedTextColor = Color.White,
                unfocusedBorderColor = Color.White,
                cursorColor = Color.Cyan,
                selectionColors = TextSelectionColors(
                    handleColor = Color(0xFF0E131C),
                    backgroundColor = Color(0x5500DDFF),
                )
            )
        )
    }
}

fun setFormValueAsPort(pin: String, alias: String, mode: String, type: String): Port {
    var decodedType = if (type == "INPUT") 1 else 0
    var decodedMode = if (mode == "ANALOG" || mode == "A") "A" else "D"
    var decodedPin = if (pin == "") 0 else pin.toInt()
    var port =
        Port(pin = decodedPin, alias = alias, mode = decodedMode, type = decodedType, value = "L")
    return port
}

fun setFormValueAsAction(actionName: String, scheduled: String, arguments: String, callbackName: String): Action {
    var action =
        Action(actionName = actionName, scheduled = scheduled, arguments = arguments, callbackName = callbackName)
    return action
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
                    handleColor = Color(0xFF0E131C),
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
                .background(Color(0xFF0E131C))
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
                    .fillMaxSize().background(Color(0xFF0E131C)),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                /** PORTS FORM ***/
                FormRow(
                    onPortFormChanged = {
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


@Composable
fun ActionsFormDialog(
    selectedAction: Action,
    onDismissRequest: () -> Unit,
    onConfirmation: (Action) -> Unit,
) {
    var actionFormValue = setFormValueAsAction("", "", "", "")
    Dialog(onDismissRequest = { onDismissRequest() }) {
        // Draw a rectangle shape with rounded corners inside the dialog
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .height(500.dp)
                .width(290.dp)
                .padding(16.dp)
                .background(Color(0xFF0E131C))
        ) {

            Column(Modifier.background(Color(0xBB223240)).fillMaxWidth()) {
                val textModifier: Modifier = Modifier.padding(10.dp)
                UiComponentFactory().Label(
                    "Action Configuration",
                    color = Color(0xFFFFFFFF),
                    fontSize = 16.sp,
                    textAlign = TextAlign.Left,
                    textModifier
                )
            }

            Column(
                modifier = Modifier
                    .fillMaxSize().background(Color(0xFF0E131C)),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                /** ACTIONS FORM ***/
                FormActionRow (
                    selectedAction = selectedAction,
                    onActionFormChanged = {
                        actionFormValue = it
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
                            onConfirmation(actionFormValue)
                            print("ACTION OPTIONS: $actionFormValue")
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



@Composable
fun OnProcessingDialog(
    onDismissRequest: () -> Unit,
    onConfirmation: () -> Unit,
    isInitialized: Boolean
) {
    Dialog(onDismissRequest = { onDismissRequest() }) {
        // Draw a rectangle shape with rounded corners inside the dialog
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .height(400.dp)
                .width(290.dp)
                .padding(16.dp)
                .background(Color(0xFF0E131C))
        ) {

            Column(Modifier.background(Color(0xBB223240)).fillMaxWidth()) {
                val textModifier: Modifier = Modifier.padding(10.dp)
                UiComponentFactory().Label(
                    "Programming device",
                    color = Color(0xFFFFFFFF),
                    fontSize = 16.sp,
                    textAlign = TextAlign.Left,
                    textModifier
                )
            }
            Column(
                modifier = Modifier
                    .fillMaxSize().background(Color(0xFF0E131C)),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {


                when (isInitialized) {
                    true -> {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth(),
                            horizontalArrangement = Arrangement.Center,
                        ) {
                            Text(text = "Configuration completed!", color = Color(0xFF00FF77))
                        }
                    }

                    false -> {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth(),
                            horizontalArrangement = Arrangement.Center,
                        ) {
                            Text(text = "Performing setup action...", color = Color(0xFFFF7700))
                        }
                    }
                }

                Row(
                    modifier = Modifier
                        .fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center,
                ) {
                    if (isInitialized) {
                        TextButton(
                            onClick = {
                                onConfirmation()
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
}

