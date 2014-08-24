package org.github.triman.roomba.communication

import akka.actor._
import org.github.triman.roomba.SensorsState
import org.github.triman.roomba.Sensors
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
import scala.concurrent.duration._
import scala.concurrent.ExecutionContext.Implicits.global

trait NetworkCommunicatorContainer extends CommunicatorContainer {
	var system = ActorSystem()
	override val communicator = system.actorOf(NetworkCommunicator.props)

	object NetworkCommunicator {
		def props(): Props = Props(new NetworkCommunicator)
	}
	
	val hostName = "localhost"
	val portNumber = 15000

	val socket = new Socket(hostName, portNumber)
	val out = socket.getOutputStream()
	val in = socket.getInputStream()
		
	class NetworkCommunicator extends RoombaCommunicator {
		def receive = {
			case b : Byte => out write Array(b)
			case Array(Sensors.opcode,s : Byte) => {
				out write Array(Sensors.opcode, s)
				s match {
					case AllSensors.code => sender ! {
						val s = new Array[Byte](26)
						in.read(s)
						s
					}
					case Detectors.code	 => sender ! {
						val s = new Array[Byte](10)
						println("[ NCC ] recieved: " + in.read(s) + " bytes (expected: " + s.length + " )")
						s
					}
					case Controls.code	 => sender ! {
						val s = new Array[Byte](6)
						println("[ NCC ] recieved: " + in.read(s) + " bytes (expected: " + s.length + " )")
						s
					}
					case Health.code	 => sender ! {
						val s = new Array[Byte](10)
						println("[ NCC ] recieved: " + in.read(s) + " bytes (expected: " + s.length + " )")
						s
					}
					case _ => throw new IllegalArgumentException
				}
			}
			case a : Array[Byte] => out write a
		}
		
		override def postStop() = {
			super.postStop()
			context.system.shutdown()
		}
		
	}
	
	override def shutdown()  : Unit = {
		communicator ! PoisonPill
		system.awaitTermination()
  	
	}

}