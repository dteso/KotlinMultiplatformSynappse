package components.dashboard

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ElevatedButton
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import components.UiComponentFactory

@Composable
    fun Dashboard(onRouteChange: (String) -> Unit) {
        ElevatedCard {
            Column(
                modifier = Modifier
//                    .requiredWidthIn(400.dp)
//                    .widthIn(400.dp, 600.dp)
                    .padding(8.dp)
            ) {
                val textModifier: Modifier = Modifier.padding(horizontal = 20.dp)
                UiComponentFactory().Label("Control Panel", color = Color(0xFFFFFFFF), fontSize = 16.sp, textAlign = TextAlign.Left, textModifier)
                UiComponentFactory().Label("Select an option", color = Color(0xFFDDDDDD), fontSize = 10.sp, textAlign = TextAlign.Left, textModifier)
                Row {
                    ElevatedButton(
                        modifier = Modifier
                            .padding(horizontal = 10.dp, vertical = 4.dp)
                            .weight(1f),
                        onClick = { onRouteChange("devices") },
                        colors = ButtonDefaults.elevatedButtonColors(
                            containerColor = Color.DarkGray,
                            contentColor = Color(0xFFDFDFDF),
                            disabledContainerColor = Color.Gray,
                            disabledContentColor = Color.LightGray
                        ),
                        content = {
                            Text("Devices")
                        }
                    )
                    ElevatedButton(
                        modifier = Modifier
                            .padding(horizontal = 10.dp, vertical = 4.dp)
                            .weight(1f),
                        onClick = { onRouteChange("usb-terminal") },
                        colors = ButtonDefaults.elevatedButtonColors(
                            containerColor = Color.DarkGray,
                            contentColor = Color(0xFFDFDFDF),
                            disabledContainerColor = Color.Gray,
                            disabledContentColor = Color.LightGray
                        ),
                        content = {
                            Text("USB")
                        }
                    )
                    ElevatedButton(
                        modifier = Modifier
                            .padding(horizontal = 10.dp, vertical = 4.dp)
                            .weight(1f),
                        onClick = { onRouteChange("mqtt")  },
                        colors = ButtonDefaults.elevatedButtonColors(
                            containerColor = Color.DarkGray,
                            contentColor = Color(0xFFDFDFDF),
                            disabledContainerColor = Color.Gray,
                            disabledContentColor = Color.LightGray
                        ),
                        content = {
                            Text("MQTT")
                        }
                    )
                }
                Row {
                    Button(
                        modifier = Modifier
                            .padding(horizontal = 10.dp, vertical = 4.dp)
                            .weight(1f),
                        onClick = { onRouteChange("systems") },
                        colors = ButtonDefaults.elevatedButtonColors(
                            containerColor = Color.DarkGray,
                            contentColor = Color(0xFFDFDFDF),
                            disabledContainerColor = Color.Gray,
                            disabledContentColor = Color.LightGray
                        ),
                        content = { Text("Systems") }
                    )
                    Button(
                        modifier = Modifier
                            .padding(horizontal = 10.dp, vertical = 4.dp)
                            .weight(1f),
                        onClick = { onRouteChange("settings")},
                        colors = ButtonDefaults.elevatedButtonColors(
                            containerColor = Color.DarkGray,
                            contentColor = Color(0xFFDFDFDF),
                            disabledContainerColor = Color.Gray,
                            disabledContentColor = Color.LightGray
                        ),
                        content = { Text("Settings") }
                    )
                    Button(
                        modifier = Modifier
                            .padding(horizontal = 10.dp, vertical = 4.dp)
                            .weight(1f),
                        onClick = { onRouteChange("info") },
                        colors = ButtonDefaults.elevatedButtonColors(
                            containerColor = Color.DarkGray,
                            contentColor = Color(0xFFDFDFDF),
                            disabledContainerColor = Color.Gray,
                            disabledContentColor = Color.LightGray
                        ),
                        content = { Text("Info") }
                    )
                }
            }
        }
}