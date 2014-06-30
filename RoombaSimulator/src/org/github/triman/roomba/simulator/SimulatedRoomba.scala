package org.github.triman.roomba.simulator

import org.github.triman.roomba.AbstractRoomba
import org.github.triman.roomba.PositionableRoomba
import org.github.triman.roomba.communication.CommunicatorContainer
import akka.actor.ActorSystem
import akka.actor.Props
import org.github.triman.roomba.communication.RoombaCommunicator
import org.github.triman.roomba.Sensors
import org.github.triman.roomba.AllSensors
import org.github.triman.roomba.Detectors
import org.github.triman.roomba.Controls
import org.github.triman.roomba.Health
import akka.actor.PoisonPill

/**
 * Communication container for the simulated roomba
 */
trait SimulatorCommunicationContainer extends CommunicatorContainer{
	val system = ActorSystem("ActorSystem")
	override val communicator = system.actorOf(SimulatorCommunicator.props)

	object SimulatorCommunicator {
		def props(): Props = Props(new SimulatorCommunicator)
	}
	
	val state = new MutableSensorsState()
	
	/**
	 * Message handler for the simulated roomba's actions.
	 * ToDo: add logic to drive the roomba. This should be used along with some physical simulation layer
	 */
	class SimulatorCommunicator extends RoombaCommunicator {
		
		def receive = {
			case b : Byte => { /* ToDo: implement this */ }
			case Array(Sensors.opcode,s : Byte) => {
				s match {
					case AllSensors.code => sender ! state.getByteArray(AllSensors)
					case Detectors.code	 => sender ! state.getByteArray(Detectors)
					case Controls.code	 => sender ! state.getByteArray(Controls)
					case Health.code	 => sender ! state.getByteArray(Health)
					case _ => throw new IllegalArgumentException
				}
			}
			case a : Array[Byte] => { /* ToDo: implement this */}
		}
	}
	override def shutdown()  : Unit = {
		communicator ! PoisonPill
		system shutdown
	}
	
} 

/**
 * Class that describes the simulated roomba.
 */
class SimulatedRoomba extends AbstractRoomba with SimulatorCommunicationContainer with PositionableRoomba