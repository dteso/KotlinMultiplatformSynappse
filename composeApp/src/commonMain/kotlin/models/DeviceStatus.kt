import kotlinx.serialization.Serializable

@Serializable
data class Event(
    val event: String,
    val config: Config,
    val status: Status
)

@Serializable
data class Config(
    val rssi: String,
    val ip: String,
    val appKey: String,
    val ssid: String,
    val staPass:String,
    val name: String,
    val deviceType: String,
    val MAC: String,
    val ports: List<Port>,
    val mqttServer: String,
    val mqttPort: Int,
    val mqttUser: String,
    val mqttPassword: String
)

@Serializable
data class Port(
    val pin: String,
    val mode: String,
    val type: String,
    val alias: String,
    val value: String
)

@Serializable
data class Status(
    val timeAlive: String,
    val temperature: String
)
