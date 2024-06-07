package components.login

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme.typography
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.navigator.Navigator
import components.UiComponentFactory

class LoginFormScreen {

    @Composable
    fun createLoginForm(screenOnSuccess: Screen, navigator: Navigator) {
        Column(Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
            Spacer(modifier = Modifier.height(35.dp))

            Text(text = "My Form", style = typography.titleLarge, modifier = Modifier.padding(8.dp))

            var username by remember { mutableStateOf(TextFieldValue("")) }
            var password by remember { mutableStateOf(TextFieldValue("")) }
            var email by remember { mutableStateOf(TextFieldValue("")) }
            var numberText by remember { mutableStateOf(TextFieldValue("")) }
            // for preview add same text to all the fields
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = Color.White,
                ),
                modifier = Modifier
                    .size(width = 400.dp, height = 400.dp)
            ) {
                Column(
                    Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Spacer(modifier = Modifier.height(35.dp))
                    /* Username */
                    OutlinedTextField(
                        value = username,
                        onValueChange = { newValue -> username = newValue },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Default.Person,
                                contentDescription = null
                            )
                        },
                        label = { Text("Username") },
                        placeholder = { Text("") },
                        )

                    /* Password */
                    OutlinedTextField(
                        value = password,
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Default.Lock,
                                contentDescription = null
                            )
                        },
                        label = { Text(text = "Password") },
                        placeholder = { Text(text = "") },
                        visualTransformation = PasswordVisualTransformation(),
                        onValueChange = {
                            password = it
                        },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password)
                    )

                    /* Email */
                    OutlinedTextField(
                        value = email,
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Default.Email,
                                contentDescription = null
                            )
                        },
                        trailingIcon = {
                            Icon(
                                imageVector = Icons.Default.Edit,
                                contentDescription = null
                            )
                        },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text),
                        label = { Text(text = "Email address") },
                        placeholder = { Text(text = "example@email.com") },
                        onValueChange = {
                            email = it
                        }
                    )

                    /* Phone number */
                    OutlinedTextField(
                        value = numberText,
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Default.Phone,
                                contentDescription = null
                            )
                        },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        label = { Text(text = "Phone number") },
                        placeholder = { Text(text = "+34-666-666-666") },
                        onValueChange = {
                            numberText = it
                        }
                    )

                    Spacer(modifier = Modifier.height(35.dp))
                    UiComponentFactory().createNavigationButton(screenOnSuccess, navigator, "Login")
                }

            }

        }
    }

}
