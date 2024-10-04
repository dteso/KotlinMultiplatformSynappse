package components.serialTerminal.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.Divider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun UsbTerminal(receivedData: String){
    Divider( color = Color.DarkGray, thickness = 1.dp, modifier = Modifier.padding(top = 5.dp) )
    Row(
        modifier = Modifier.fillMaxWidth().background(Color(0xFF0E131C))
    ) {
        val dataLines = receivedData.split("\n").asReversed()
        TerminalConsole(dataLines)
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
                        && !receivedDataSplitted.contains("[0.0.0.0][null]") -> Color(0x5500AAFF)
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
                        && !receivedDataSplitted.contains("WAIT]") -> Color.Cyan
                // serial
                receivedDataSplitted.contains("SYNAPPSE.SERIAL") -> Color.Green
                // offline
                receivedDataSplitted.contains("SYNAPPSE.AP")
                        || receivedDataSplitted.contains("SYNAPPSE.NETWORK.INFO")
                        || receivedDataSplitted.contains("[0.0.0.0][null]") -> Color.Gray
                // warning
                receivedDataSplitted.contains("WARNING]")
                        || receivedDataSplitted.contains("WAIT]") -> Color(0xFFFF6600)
                // error
                receivedDataSplitted.contains("ERROR]") -> Color.Red
                receivedDataSplitted.contains("SYNAPPSE.SIZE]") -> Color(0XAAFF0066)
                receivedDataSplitted.contains("SYNAPPSE.EXECUTION]") -> Color.Blue
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