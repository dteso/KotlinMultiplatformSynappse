package components

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.navigator.Navigator

class UiComponentFactory {

    @Composable
    fun createNavigationButton(screen: Screen, navigator: Navigator, caption: String) {
        Button(onClick = {navigator.push(screen)}) {
            Text(caption)
        }
    }

    @Composable
    fun createTextHeader(text: String, color: Color = Color.Black) {
        Text(
            text = text,
            modifier = Modifier.padding(20.dp),
            fontSize = 24.sp,
            textAlign = TextAlign.Center,
            color = color
        )
    }

    @Composable
    fun Label(text: String, color: Color = Color.Black, fontSize: TextUnit = 24.sp, textAlign: TextAlign, modifier: Modifier) {
        Text(
            modifier = modifier,
            text = text,
            fontSize = fontSize,
            textAlign = textAlign,
            color = color
        )
    }

    @Composable
    fun createFilledCard(text: String = "Filled") {
        Card(
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant,
            ),
            modifier = Modifier
                .fillMaxWidth()
        ) {
            Text(
                text = text,
                modifier = Modifier
                    .padding(16.dp),
                textAlign = TextAlign.Center,
            )
        }
    }
}