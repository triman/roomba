package org.github.triman.roomba.simulator.communication

import akka.actor._
import org.github.triman.roomba.SensorsState
import org.github.triman.roomba.communication._
import org.github.triman.roomba.Sensors
import org.github.triman.roomba.simulator._
import java.nio.ByteBuffer
import java.io.PipedOutputStream
import java.io.PipedInputStream
import org.github.triman.roomba.AllSensors
import org.github.triman.roomba.Detectors
import org.github.triman.roomba.Controls
import org.github.triman.roomba.Health
import java.io.PrintWriter
import java.net.Socket
import java.io.BufferedReader
import java.io.InputStreamReader

trait NetworkCommunicatorContainer extends CommunicatorContainer {
	val system = ActorSystem("ActorSystem")
	override val communicator = system.actorOf(NetworkCommunicator.props)

	object NetworkCommunicator {
		def props(): Props = Props(new NetworkCommunicator)
	}
	
	val hostName = "localhost"
	val portNumber = 15000

	val socket = new Socket(hostName, portNumber)
	val out = socket.getOutputStream()
	val in = new InputStreamReader(socket.getInputStream())
	
	val state = new MutableSensorsState()
	
	class NetworkCommunicator extends RoombaCommunicator {
		
		def receive = {
			case b : Byte => out write Array(b)
			case Array(Sensors.opcode,s : Byte) => {
				out write Array(Sensors.opcode, s)
				s match {
					case AllSensors.code => sender ! state.getByteArray(AllSensors)
					case Detectors.code	 => sender ! state.getByteArray(Detectors)
					case Controls.code	 => sender ! state.getByteArray(Controls)
					case Health.code	 => sender ! state.getByteArray(Health)
					case _ => throw new IllegalArgumentException
				}
			}
			case a : Array[Byte] => out write a
		}
	}
	
	override def shutdown()  : Unit = {
		communicator ! PoisonPill
		system shutdown
	}

}