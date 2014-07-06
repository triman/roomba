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
import org.github.triman.roomba.Drive
import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global
import org.github.triman.roomba.utils.Notifier
import org.github.triman.roomba.SensorPacket

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
	 * This is the "real" position of the robot. It can be used to compare the estimated position
	 * returned by the PositionableRoomba trait and the "real" position as computed using the orders.
	 * This position is used to compute the sensors values (bumps, dirt, ...).
	 */
	val simulatedPosition = new Notifier[Point, Symbol](new Point(0,0)){def id = 'RoombaSimulatedPositionChange}
	val simulatedAngle = new Notifier[Double, Symbol](0){def id = 'RoombaSimulatedAngleChange}
	/**
	 * Timestamp when the last "Drive" command was issued
	 */
	private val lastControlsGetTimestamp = new AtomicReference[Long](Platform.currentTime)
	/**
	 * Distance remaining in the controls buffer when the last "Drive" command was issued.
	 */
	private val distanceAtLastDriveCommand = new AtomicReference[Int](0)
	/**
	 * Angle remaining in the controls buffer when the last "Drive" command was issued.
	 */
	private val angleAtLastDriveCommand = new AtomicReference[Double](0)
	/**
	 * Refresh rate for the simulated position (recomputation of new sensor data) [ms]
	 * Default : 10 ms
	 */
	val simulatedPositionRefreshInterval = 10;
	
	/**
	 * Shape representing the wurface where the roomba should drive.
	 * This shape is used in order to compute the state of the different sensors.
	 */
	val drivableSurface = new AtomicReference[Shape](null)
	
	/**
	 * Worker to compute the new simulated state
	 * 	- Computes the position every interval
	 *  - Compute bumps
	 *  - Compute sensor data
	 */
	private object simulationWorker {
		private var t : Thread = null;
		/**
		 * Starts the worker
		 */
		def start(){
			synchronized{
				stop
				
				t = new Thread{
					override def run(){
						val p0 = position()
						val a0 = angle()
						val t0 = Platform.currentTime
						// compute the new position every positionRefreshDuration ms
						while(true){
							Future{
								// compute displacement -> sensors data 
									// ToDo, depending on last refresh
								// compute position
								val dt = Platform.currentTime - t0
								val d = (speed.get() * dt).toInt / 1000
								val p1 = computePosition(p0, a0, d, radius.get())
								simulatedPosition update p1._1
								simulatedAngle update p1._2
								// compute bumps
								computeAndApplySensors
							}
							Thread.sleep(simulatedPositionRefreshInterval)	// sleep for 10ms
						}
					}
				}
				t.start
			}
		}
		/**
		 * Forces the worker to stop
		 */
		def stop(){
			synchronized{
				if(t != null && t.isAlive()){
					t stop
				}
			}
		}
		
		/**
		 * Compute the sensors state (bumpers etc...) from the surface.
		 * ToDo: if the roomba is in safe mode, it should stop if a bump occured.
		 */
		private def computeAndApplySensors() {
			//ToDo: implement this and remove this output
			println("Computing sensors for position: " + simulatedPosition())
		}
	}
	
	/**
	 * Get the sensors data.
	 * @param packet The sensors packet to get.
	 */
	override def sensor(packet : SensorPacket) : Future[Any] = {
		val r = super.sensor(packet)
		// set the last time we cot data = now, and the offset distance to 0
		if(packet == Controls || packet == AllSensors){
			lastControlsGetTimestamp set Platform.currentTime
			distanceAtLastDriveCommand set 0
			angleAtLastDriveCommand set 0
		}
		return r
	}
	
	override def shutdown()  : Unit = {
		simulationWorker stop
	}
	
}