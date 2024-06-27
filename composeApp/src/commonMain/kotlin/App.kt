import Theme.MyApplicationTheme
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.Navigator
import cafe.adriel.voyager.navigator.currentOrThrow
import cafe.adriel.voyager.transitions.SlideTransition
import components.UiComponentFactory
import components.home.HomeScreen
import components.login.LoginFormScreen
import org.jetbrains.compose.ui.tooling.preview.Preview

@Composable
@Preview
fun App() {
    MyApplicationTheme (){
        Navigator(screen = MainScreen()){navigator: Navigator ->  SlideTransition(navigator) }
    }
}

class MainScreen: Screen {
    @Composable
    override fun Content() {
        var navigator: Navigator = LocalNavigator.currentOrThrow
        LoginFormScreen().createLoginForm(HomeScreen(), FailureScreen(), navigator)
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
