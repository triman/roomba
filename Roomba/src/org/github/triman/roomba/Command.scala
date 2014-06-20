package org.github.triman.roomba

sealed abstract class Command(val opcode : Byte) {
	override def toString() = opcode.toString
}

case object Start extends Command(128.toByte)
case object Baud extends Command(129.toByte)
case object Control extends Command(130.toByte)
case object Safe extends Command(131.toByte)
case object Full extends Command(132.toByte)
case object Power extends Command(133.toByte)
case object Spot extends Command(134.toByte)
case object Clean extends Command(135.toByte)
case object Max extends Command(136.toByte)
case object Drive extends Command(137.toByte)
case object Motors extends Command(138.toByte)
case object Leds extends Command(139.toByte)
case object Song extends Command(140.toByte)
case object Play extends Command(141.toByte)
case object Sensors extends Command(142.toByte)
case object ForceSeekingDock extends Command(143.toByte)

sealed abstract class SensorPacket(val code : Byte){
	override def toString() = "Sensor packet " + code
}
case object AllSensors extends SensorPacket(0 toByte)
case object Detectors extends SensorPacket(1 toByte)
case object Controls extends SensorPacket(2 toByte)
case object Health extends SensorPacket(3 toByte)