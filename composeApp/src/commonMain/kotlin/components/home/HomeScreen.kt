package components.home

import MainScreen
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.padding
import androidx.compose.material.BottomNavigation
import androidx.compose.material.BottomNavigationItem
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ExitToApp
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.Divider
import androidx.compose.material3.DrawerState
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.TopAppBarScrollBehavior
import androidx.compose.material3.rememberDrawerState
import androidx.compose.material3.rememberTopAppBarState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.Navigator
import cafe.adriel.voyager.navigator.currentOrThrow
import components.dashboard.Dashboard
import components.devices.AddDevice
import components.login.LoginFormScreen
import components.serialTerminal.UsbPanel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

class HomeScreen : Screen {
    @Composable
    override fun Content() {
        val navigator: Navigator = LocalNavigator.currentOrThrow
        HomeLayout(navigator, MainScreen())
    }

    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    fun HomeLayout(navigator: Navigator, backScreen: Screen) {
        val scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior(rememberTopAppBarState())
        var innerRoute by remember { mutableStateOf<String>("home") }
        val drawerState = rememberDrawerState(DrawerValue.Closed)
        val scope = rememberCoroutineScope()

        fun onRouteSelected(route: String) {
            innerRoute = route
            scope.launch {
                drawerState.close()
            }
        }

        ModalNavigationDrawer(
            drawerState = drawerState,
            drawerContent = {
                ModalDrawerSheet {
                    Text("S Y N A P P S E",
                        modifier = Modifier.padding(20.dp),
                        style = TextStyle(fontWeight = FontWeight.Bold, fontSize = 20.sp))
                    Divider()
                    NavigationDrawerItem(
                        label = { Text(text = "Home") },
                        selected = false,
                        onClick = { onRouteSelected("home") }
                    )
                    NavigationDrawerItem(
                        label = { Text(text = "Devices") },
                        selected = false,
                        onClick = {onRouteSelected("devices") }
                    )
                    NavigationDrawerItem(
                        label = { Text(text = "USB") },
                        selected = false,
                        onClick = { onRouteSelected("usb-terminal") }
                    )
                    NavigationDrawerItem(
                        label = { Text(text = "MQTT") },
                        selected = false,
                        onClick = { onRouteSelected("mqtt") }
                    )
                    NavigationDrawerItem(
                        label = { Text(text = "System") },
                        selected = false,
                        onClick = { onRouteSelected("system") }
                    )
                    NavigationDrawerItem(
                        label = { Text(text = "Settings") },
                        selected = false,
                        onClick = { onRouteSelected("settings") }
                    )
                    NavigationDrawerItem(
                        label = { Text(text = "Info") },
                        selected = false,
                        onClick = { onRouteSelected("info") }
                    )
                }
            }
        ) {
            Scaffold(
                modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
                topBar = {
                    TopBar(scrollBehavior, drawerState, scope)
                },
                bottomBar = {
                    // Puedes agregar un bottom bar aquí si es necesario
                    BottomBar(innerRoute) { route -> innerRoute = route }
                }
            ) { innerPadding ->
                Column (
                    modifier = Modifier
                        .fillMaxHeight()
                        .padding(innerPadding)
                        .padding(8.dp)
                ) {
                        when (innerRoute) {
                            "home" -> {
                                Dashboard() { route -> innerRoute = route }
                            }

                            "usb-terminal" -> {
                                UsbPanel() { route -> innerRoute = route }
                            }

                            "devices" -> {
//                                Devices() { route -> innerRoute = route }
                            }

                            "add-device" -> {
                                AddDevice () { route -> innerRoute = route }
                            }

                            "systems" -> {
//                                Systems() { route -> innerRoute = route }
                            }

                            "settings" -> {
//                                Settings() { route -> innerRoute = route }
                            }

                            "mqtt" -> {
//                                Mqtt() { route -> innerRoute = route }
                            }

                            "info" -> {
//                                Info() { route -> innerRoute = route }
                            }
                        }
                }
            }
        }
    }

    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    fun TopBar(scrollBehavior: TopAppBarScrollBehavior, drawerState: DrawerState, scope: CoroutineScope) {
        CenterAlignedTopAppBar(
            colors = TopAppBarDefaults.topAppBarColors(
                containerColor = Color.White,
                titleContentColor = Color(0xFF223240),
            ),
            title = {
                Text(
                    "S Y N A P P S E",
                    style = TextStyle(fontWeight = FontWeight.Bold, fontSize = 25.sp),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            },
            navigationIcon = {
                IconButton(onClick = {
                    scope.launch {
                        if (drawerState.isOpen) {
                            drawerState.close()
                        } else {
                            drawerState.open()
                        }
                    }
                }) {
                    Icon(
                        imageVector = Icons.Filled.Menu,
                        contentDescription = "Menu"
                    )
                }
            },
            actions = {
                IconButton(onClick = { /* do something */ }) {
                    Icon(
                        imageVector = Icons.Filled.Notifications,
                        contentDescription = "Localized description"
                    )
                }
                var navigator =  LocalNavigator.currentOrThrow
                IconButton(onClick = { navigator.push(LoginFormScreen()) }) {
                    Icon(
                        imageVector = Icons.Filled.ExitToApp,
                        contentDescription = "Localized description"
                    )
                }
            },
            scrollBehavior = scrollBehavior,
        )
    }

    @Composable
    fun BottomBar(currentRoute: String, onRouteSelected: (String) -> Unit) {
        var navigator =  LocalNavigator.currentOrThrow
        BottomNavigation (
            backgroundColor = Color.White,
            contentColor = Color(0xFF223240)
        ) {
            BottomNavigationItem(
                icon = { Icon(Icons.Filled.Home, contentDescription = "Home") },
                label = { Text("Home") },
                selected = currentRoute == "home",
                onClick = { onRouteSelected("home") }
            )
            BottomNavigationItem(
                icon = { Icon(Icons.Filled.Settings, contentDescription = "Settings") },
                label = { Text("Settings") },
                selected = currentRoute == "settings",
                onClick = { onRouteSelected("settings") }
            )
            BottomNavigationItem(
                icon = { Icon(Icons.Filled.ExitToApp, contentDescription = "Logout") },
                label = { Text("Logout") },
                selected = currentRoute == "logout",
                onClick = { navigator.push(LoginFormScreen()) }
            )
        }
    }
}

