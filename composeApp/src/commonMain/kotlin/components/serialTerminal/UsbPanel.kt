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
import io.ktor.utils.io.charsets.Charsets
import io.ktor.utils.io.core.toByteArray
import utils.serial_port.SerialPortImpl

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
                                            onClick = {
                                                SerialPortImpl.open(port.name, 115200)
                                                updatePorts = true
                                            },
                                            colors = ButtonDefaults.buttonColors(
                                                containerColor = Color.DarkGray,
                                                contentColor = Color.White
                                            ),
                                            modifier = Modifier
                                                .width(110.dp)
                                                .height(36.dp) // Ajusta el tamaño aquí
                                        ) {
                                            Text("Connect")
                                        }
                                    } else {
                                        Button(
                                            onClick = {
                                                SerialPortImpl.close()
                                                updatePorts = true
                                            },
                                            colors = ButtonDefaults.buttonColors(
                                                containerColor = Color.DarkGray,
                                                contentColor = Color.White
                                            ),
                                            modifier = Modifier
                                                .width(110.dp)
                                                .height(36.dp) // Ajusta el tamaño aquí
                                        ) {
                                            Text("Close")
                                        }
                                    }
                                }
                            }
                            if( port.isOpen ) {
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
                                            onValueChange = { newValue -> sendText = newValue },
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
                                Spacer(modifier = Modifier.weight(1f))
                                Row(Modifier.fillMaxWidth()){
                                    Column(
                                        horizontalAlignment = Alignment.End,
                                        modifier = Modifier
                                            .fillMaxWidth()
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
                                                contentColor = Color.White
                                            ),
                                            modifier = Modifier
                                                .width(110.dp)
                                                .background(Color.Black)
                                                .height(36.dp) // Ajusta el tamaño aquí
                                        ) {
                                            Text("Send")
                                        }
                                    }
                                }
                                Divider( color = Color.DarkGray, thickness = 1.dp, modifier = Modifier.padding(top = 5.dp) )
                                Row(
                                    modifier = Modifier.fillMaxWidth().background(Color.Black)
                                ) {
                                    Column(
                                        modifier = Modifier
                                            .padding(8.dp)
                                            .fillMaxWidth()
                                    ) {
                                        receivedData.split("\n").forEach { receivedDataSplitted ->
                                            val isInfo = receivedDataSplitted.contains("SYNAPPSE.INFO")
                                                    && !receivedDataSplitted.contains("[0.0.0.0][null]")

                                            val isStatus = receivedDataSplitted.contains("SYNAPPSE.STATUS")

                                            val isMemory = receivedDataSplitted.contains("SYNAPPSE.MEMORY")

                                            val isMqtt = receivedDataSplitted.contains("SYNAPPSE.MQTT")
                                                    && !receivedDataSplitted.contains("ERROR]")
                                                    && !receivedDataSplitted.contains("WARNING]")
                                                    && !receivedDataSplitted.contains("WAIT]")
                                            val isSerialInput = receivedDataSplitted.contains("SYNAPPSE.SERIAL")
                                                    && !receivedDataSplitted.contains("ERROR]")
                                                    && !receivedDataSplitted.contains("WARNING]")
                                                    && !receivedDataSplitted.contains("WAIT]")
                                            val isOffline =  receivedDataSplitted.contains("SYNAPPSE.AP")
                                                    || receivedDataSplitted.contains("SYNAPPSE.NETWORK.INFO")
                                                    || receivedDataSplitted.contains("[0.0.0.0][null]")
                                            val isWarning = receivedDataSplitted.contains("WARNING]")
                                                    || receivedDataSplitted.contains("WAIT]")
                                            val isError = receivedDataSplitted.contains("ERROR]")

                                            var color: Color = Color.White

                                            if (isInfo){
                                                color = Color.Cyan
                                            }else if (isStatus){
                                                color = Color.Magenta
                                            }else if (isMemory){
                                                color = Color.Yellow
                                            }else if (isMqtt){
                                                color = Color.Green
                                            }else if (isSerialInput){
                                                color = Color.LightGray
                                            }else if (isOffline){
                                                color = Color.Black
                                            }else if (isWarning){
                                                color = Color(0xFFFF6600)
                                            }else if (isError){
                                                color = Color.Red
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
