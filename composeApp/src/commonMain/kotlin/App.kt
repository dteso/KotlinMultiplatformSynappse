import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.Navigator
import cafe.adriel.voyager.navigator.currentOrThrow
import cafe.adriel.voyager.transitions.SlideTransition
import components.UiComponentFactory
import components.login.LoginFormScreen
import org.jetbrains.compose.ui.tooling.preview.Preview
import utils.date_utils.DateUtils

@Composable
@Preview
fun App() {
    MaterialTheme(){
        Navigator(screen = MainScreen()){navigator: Navigator ->  SlideTransition(navigator) }
    }
}

class MainScreen: Screen {
    @Composable
    override fun Content() {
        var navigator: Navigator = LocalNavigator.currentOrThrow
        LoginFormScreen().createLoginForm(SecondScreen(), FailureScreen(), navigator)
    }
}

class SecondScreen: Screen {
    @Composable
    override fun Content(){
        var navigator: Navigator = LocalNavigator.currentOrThrow
        Column(Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
            UiComponentFactory().createTextHeader("W E L C O M E !")
            UiComponentFactory().createNavigationButton(MainScreen(), navigator, "Back to Login")
        }
    }
}

class FailureScreen: Screen {
    @Composable
    override fun Content(){
        var navigator: Navigator = LocalNavigator.currentOrThrow
        Column(Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
            UiComponentFactory().createTextHeader("H E Y !  W H O   A R E   Y O U  ?")
            UiComponentFactory().createNavigationButton(MainScreen(), navigator, "Get out from here!")
        }
    }
}
