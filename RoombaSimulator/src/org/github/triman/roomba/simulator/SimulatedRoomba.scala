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
import java.util.concurrent.atomic.AtomicBoolean
import java.awt.Point
import java.util.concurrent.atomic.AtomicReference
import scala.compat.Platform
import java.awt.Shape

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
class SimulatedRoomba extends AbstractRoomba with SimulatorCommunicationContainer with PositionableRoomba{
	
	private val isRunning = new AtomicBoolean(false)
	
	/**
	 * Computes a new position. This is assigned when a drive() command is issued
	 */
	val positionComputationFunction = new AtomicReference[(Long) => Point](null)
	
	/**
	 * This is the "real" position of the robot. It can be used to compare the estimated position
	 * returned by the PositionableRoomba trait and the "real" position as computed using the orders.
	 * This position is used to compute the sensors values (bumps, dirt, ...).
	 */
	val simulatedPosition = new AtomicReference[Point](new Point(0,0))
	
	val drivableSurface = new AtomicReference[Shape](null)
	
	def run() = {
		// mark the thread as being run
		isRunning.set(true)
		// run a new thread
		val t = new Thread(){
			override def run() : Unit = {
				val initialTimestamp = Platform.currentTime
				while(isRunning.get() && positionComputationFunction.get != null){
					// process actions
					//1. compute displacement (new sensors data)
					
					//2. compute internal position
					simulatedPosition.set(positionComputationFunction.get()(Platform.currentTime - initialTimestamp))
					//3. compute sensors
					computeAndApplySensors
					Thread.sleep(10)	// sleep for 10ms
				}
			}
		}
	}
	
	/**
	 * Compute the sensors state (bumpers etc...) from the surface.
	 * ToDo: if the roomba is in safe mode, it should stop if a bump occured.
	 */
	private def computeAndApplySensors(){
		//ToDo: implement this and remove this output
		println("Computing sensors for position: " + simulatedPosition.get)
	}
	
}