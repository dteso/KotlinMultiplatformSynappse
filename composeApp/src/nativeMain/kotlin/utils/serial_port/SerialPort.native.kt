package utils.serial_port

import kotlinx.coroutines.flow.StateFlow

actual object SerialPortImpl : SerialPortInterface {

    override val receivedData: StateFlow<String>
        get() = TODO("Not yet implemented")

    override fun getAvailablePorts(): MutableList<SystemComPort>? {
        TODO("Not yet implemented")
    }

    override fun open(portName: String, baudRate: Int) {
        TODO("Not yet implemented")
    }

    override fun write(data: ByteArray) {
        TODO("Not yet implemented")
    }

    override fun read(): ByteArray {
        TODO("Not yet implemented")
    }

    override fun close() {
        TODO("Not yet implemented")
    }

    override fun run() {
        TODO("Not yet implemented")
    }

    override fun getSystemCommPorts(): MutableList<SystemComPort> {
        TODO("Not yet implemented")
    }

}