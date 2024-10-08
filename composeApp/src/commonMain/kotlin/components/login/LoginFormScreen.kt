package components.login

import FailureScreen
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
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme.typography
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusProperties
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onKeyEvent
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.Navigator
import cafe.adriel.voyager.navigator.currentOrThrow
import components.home.HomeScreen
import io.ktor.client.HttpClient
import io.ktor.client.engine.cio.CIO
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.statement.HttpResponse
import io.ktor.client.statement.bodyAsText
import io.ktor.http.ContentType
import io.ktor.http.HttpStatusCode
import io.ktor.http.contentType
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

@Serializable
class LoginModel(
    val name: String,
    val password: String
){}

class LoginFormScreen : Screen{

    @Composable
    override fun Content() {
        createLoginForm(HomeScreen(), FailureScreen(), navigator = LocalNavigator.currentOrThrow )
    }

    @Composable
    fun createLoginForm(screenOnSuccess: Screen, screenOnFailure: Screen, navigator: Navigator) {
        Column(Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
            Spacer(modifier = Modifier.height(35.dp))

            Text(text = "My Form", style = typography.titleLarge, modifier = Modifier.padding(8.dp))

            var username by remember { mutableStateOf(TextFieldValue("")) }
            var password by remember { mutableStateOf(TextFieldValue("")) }
            var email by remember { mutableStateOf(TextFieldValue("")) }
            var numberText by remember { mutableStateOf(TextFieldValue("")) }

            val focusRequester1 = remember { FocusRequester() }
            val focusRequester2 = remember { FocusRequester() }
            val scope = rememberCoroutineScope()

            Card(
//                colors = CardDefaults.cardColors(
//                    containerColor = Color.White,
//                ),
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
                        modifier = Modifier
                            .focusRequester(focusRequester1)
                            .focusProperties {
                                next = focusRequester2
                            }
                            .onKeyEvent {
                                if (it.key == Key.Tab) {
                                    scope.launch {
                                        focusRequester2.requestFocus()
                                    }
                                    true
                                } else {
                                    false
                                }
                            },
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
                    Button( onClick = {
                        GlobalScope.launch{
//                            val loginModel = LoginModel(username.text, password.text)
//                            val response: String = HttpService().postRequest<LoginModel, String>("", loginModel)
                            if(requestLogin(username, password)){
                                navigator.push(screenOnSuccess)
                            }else{
                                navigator.push(screenOnFailure)
                            }
                        }
                    }){
                        Text("LOGIN")
                    }
                }
            }
        }
    }

    private suspend fun requestLogin(name: TextFieldValue, password: TextFieldValue): Boolean {
        val client = HttpClient(CIO)

        // Codificar a través del modelo a Json en formato String
        val body: String =  Json.encodeToString(LoginModel(name.text, password.text))
        println(" REQUESTING LOGIN WITH BODY: \n ${Json.encodeToString(body)}")

        // Hacemos el proceso inverso, sólo para si queremos depurar ver que
        // hemos decodificado al modelo correctametne
        val loginModel = Json.decodeFromString<LoginModel>(body)
        print(loginModel)

        // Realizamos la petición al servidor
        val url = "http://192.168.1.41:4411/api/auth/login"
        val response: HttpResponse = client.post(url) {
            contentType(ContentType.Application.Json)  // Establecemos el tipo de contenido
            setBody(body)
        }

        // Verificamos la respuesta
        if(response.status == HttpStatusCode.OK){
            println("RESPONSE OK: \n ${response.bodyAsText()}")
            return true
        }
        println("ERROR PERFORMING REQUEST [${response.status}]: \n ${response.bodyAsText()}")
        return false
    }

}
