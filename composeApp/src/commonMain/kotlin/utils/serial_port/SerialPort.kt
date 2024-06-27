package utils.serial_port

import kotlinx.coroutines.flow.StateFlow

class SystemComPort (name: String, baudRate: Int, isOpen: Boolean, description: String, friendlyName: String) {
    var name: String = name
    var baudRate: Int = baudRate
    var isOpen: Boolean = isOpen
    var description: String = description
    var friendlyName: String = friendlyName
}

interface SerialPortInterface {

    val receivedData: StateFlow<String>
    fun getAvailablePorts(): MutableList<SystemComPort>?
    fun open(portName: String, baudRate: Int)
    fun write(data: ByteArray)
    fun read(): ByteArray
    fun close()
    fun run()
    fun getSystemCommPorts(): MutableList<SystemComPort>
}

expect object SerialPortImpl: SerialPortInterface
