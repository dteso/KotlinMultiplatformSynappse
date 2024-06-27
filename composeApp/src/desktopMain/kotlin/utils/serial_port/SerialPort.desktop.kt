package utils.serial_port
import com.fazecast.jSerialComm.SerialPort
import com.fazecast.jSerialComm.SerialPortIOException
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

actual object SerialPortImpl : SerialPortInterface {

    private var systemCommPorts = mutableListOf<SystemComPort>()
    private var currentPort: SerialPort? = null
    private var job: Job? = null
    private var storedPorts: MutableList<SerialPort> = mutableListOf()

    // Variable pública accesible para almacenar los datos leídos
    // MutableStateFlow para almacenar los datos leídos
    private val _receivedData = MutableStateFlow("")
    override val receivedData: StateFlow<String> get() = _receivedData

    override fun getAvailablePorts(): MutableList<SystemComPort>? {
        var ports = SerialPort.getCommPorts()

        if(storedPorts.isEmpty()){
            storedPorts = ports.toMutableList()
        }else{
            ports = storedPorts.toTypedArray()
        }

        if (ports.isEmpty() && systemCommPorts.isEmpty()) {
            println("No se encontraron puertos serie.")
            return null
        } else if (systemCommPorts.isEmpty()){
            systemCommPorts.clear()
            println("Puertos disponibles:")
            for (port in ports) {
                val systemComPort = SystemComPort(
                    port.systemPortName,
                    port.baudRate,
                    port.isOpen,
                    port.portDescription,
                    port.descriptivePortName
                )
                systemCommPorts.add(systemComPort)
            }
        }
        return systemCommPorts
    }

    override fun open(portName: String, baudRate: Int) {
        currentPort?.closePort() // Cerrar el puerto actual si está abierto

        val port: SerialPort = SerialPort.getCommPort(portName)
        port.baudRate = baudRate // Configurar la velocidad de baudios
        port.openPort() // Abrir el puerto

        currentPort = port // Actualizar la referencia al puerto actual

        if (port.isOpen) {
            job = CoroutineScope(Dispatchers.IO).launch {
                val readBuffer = ByteArray(1024)
                systemCommPorts[0].isOpen = true
                while (isActive) {
                    val numBytesRead = port.readBytes(readBuffer, readBuffer.size.toLong())
                    if (numBytesRead > 0) {
                        _receivedData.value = String(readBuffer, 0, numBytesRead) + _receivedData.value
                        print(String(readBuffer, 0, numBytesRead))
                    }
                    delay(1000) // Pequeña pausa para evitar bucle ocupado
                }
            }
        } else {
            println("No se pudo abrir el puerto.")
        }
    }

    override fun close() {
        job?.cancel()
        currentPort?.closePort()
        systemCommPorts[0].isOpen = false
        _receivedData.value = ""
        println("Puerto ${currentPort?.systemPortName} cerrado.")
    }

    override fun write(data: ByteArray) {
        try {
            currentPort?.writeBytes(data, data.size.toLong())
        } catch (e: SerialPortIOException) {
            e.printStackTrace()
        }
    }

    override fun read(): ByteArray {
        TODO("Not yet implemented")
    }

    override fun run() {
        TODO("Not yet implemented")
    }

    override fun getSystemCommPorts(): MutableList<SystemComPort> {
        TODO("Not yet implemented")
    }
}

