import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonObject

@Serializable
data class Event(
    val event: String?,
    var config: Config?,
    var status: JsonObject?
)

@Serializable
data class Config(
    var rssi: String,
    var ip: String,
    var appKey: String,
    var staSsid: String,
    var staPass:String,
    var name: String,
    var deviceType: String,
    var MAC: String,
    var mqttServer: String,
    var mqttPort: Int,
    var mqttUser: String,
    var mqttPassword: String,
    var mqttEnabled: Boolean,
    var staEnabled: Boolean,
    var wifiConnected: Boolean,
    var mqttConnected: Boolean,
    var ports: List<Port>? = null, // De esta forma es nullable o puede no venir en la definición del json
    var actions: List<Action>? = null, // De esta forma es nullable o puede no venir en la definición del json

)

@Serializable
data class Port(
    val pin: Int,
    val mode: String,
    val type: Int,
    val alias: String,
    val value: String
)

@Serializable
data class Action(
    val actionName: String? = null,
    val arguments: String? = null,
    val callbackName: String? = null,
    val scheduled: String? = null
)

