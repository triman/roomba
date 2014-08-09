package org.github.triman.roomba.simulator

import akka.actor.Props
import java.util.concurrent.atomic.AtomicBoolean
import java.util.concurrent.atomic.AtomicReference
import java.awt.Point
import java.awt.Shape
import scala.compat.Platform
import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global
import org.github.triman.roomba.SensorPacket
import org.github.triman.roomba.Drive
import org.github.triman.roomba.Sensors
import org.github.triman.roomba.AllSensors
import org.github.triman.roomba.Detectors
import org.github.triman.roomba.Controls
import org.github.triman.roomba.Health
import org.github.triman.utils.Notifier
import org.github.triman.utils.NullReplace._
import org.github.triman.roomba.utils.PositionUtil
import org.github.triman.roomba.simulator.communication.RoombaSocketServer
import org.github.triman.roomba.utils.ByteOperations
import org.github.triman.roomba.SensorsState
import scala.collection.mutable.MutableList
import scala.concurrent.future
import org.github.triman.roomba.AbstractSensorsState
import org.github.triman.roomba.ControlState
import java.awt.geom.AffineTransform
import org.github.triman.roomba.simulator.physics.PhysicalRoomba
import org.github.triman.roomba.simulator.environment.Room
import java.awt.geom.Area
/**
 * Class that describes the simulated roomba.
 */
class SimulatedRoomba {

	private val isRunning = new AtomicBoolean(false)

	/**
	 * This is the "real" position of the robot. It can be used to compare the estimated position
	 * returned by the PositionableRoomba trait and the "real" position as computed using the orders.
	 * This position is used to compute the sensors values (bumps, dirt, ...).
	 */
	val simulatedPosition = new Notifier[Point, Symbol](new Point(0, 0)) { def id = 'RoombaSimulatedPositionChange }
	val simulatedAngle = new Notifier[Double, Symbol](-Math.PI) { def id = 'RoombaSimulatedAngleChange }	// initial angle is π since we usually start facing a wall (-> room will probably be mapped in positive directions then).

	val speed = new AtomicReference[Int](0)
	val radius = new AtomicReference[Int](0)

	/**
	 * Timestamp when the last "Drive" command was issued
	 */
	private val lastDriveCommandTimestamp = new AtomicReference[Long](Platform.currentTime)
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

	val room = new AtomicReference[Room](null)
	private val lastFrontCollisionTimestamp = new AtomicReference[Long](0)
	private val lastBackCollisionTimestamp = new AtomicReference[Long](0)
	/**
	 * Worker to compute the new simulated state
	 * 	- Computes the position every interval
	 *  - Compute bumps
	 *  - Compute sensor data
	 */
	private object simulationWorker {
		private var t: Thread = null;
		/**
		 * Starts the worker
		 */
		def start() {
			synchronized {
				stop

				t = new Thread {
					override def run() {
						val p0 = simulatedPosition()
						val a0 = simulatedAngle()
						val t0 = Platform.currentTime
						// compute the new position every positionRefreshDuration ms
						while (true) {
							Future {
								// compute position
								val dt = ( if(lastFrontCollisionTimestamp.get > t0 && speed.get > 0 ) // we cannot drive forward after collision
										lastFrontCollisionTimestamp.get else 
											if(lastBackCollisionTimestamp.get > t0 && speed.get < 0) lastBackCollisionTimestamp.get else
										Platform.currentTime ) - t0
								val d = (speed.get() * dt).toInt / 1000
								if((sensorsState.leftBump.get || sensorsState.rightBump.get) && lastFrontCollisionTimestamp.get < t0){
									// we have a new collision
									lastFrontCollisionTimestamp.set(Platform.currentTime)
								}
								if(backBump.get && lastBackCollisionTimestamp.get < t0){
									// we have a new collision
									lastBackCollisionTimestamp.set(Platform.currentTime)
								}
								
								val p1 = PositionUtil.getPointAndAngleOnCircularPath(p0, a0, d, radius.get())
								simulatedPosition update p1._1
								simulatedAngle update p1._2
								// compute bumps
								computeAndApplySensors
							}
							Thread.sleep(simulatedPositionRefreshInterval) // sleep for 10ms
						}
					}
				}
				t.start
			}
		}
		/**
		 * Forces the worker to stop
		 */
		def stop() {
			synchronized {
				if (t != null && t.isAlive()) {
					t stop
				}
			}
		}

		/**
		 * Compute the sensors state (bumpers etc...) from the surface.
		 * ToDo: if the roomba is in safe mode, it should stop if a bump occured.
		 */
		private def computeAndApplySensors() {
			val d = (((Platform.currentTime - lastDriveCommandTimestamp.get) * speed.get())/1000).toInt
			sensorsState.distance = Some((distanceAtLastDriveCommand.get + d))
			sensorsState.angle = Some(angleAtLastDriveCommand.get + d.toDouble/radius.get.toDouble)
			
			val walls : Shape = if(room.get == null) new Area() else room.get().wallsDrawable.shape

			val transform = new AffineTransform()
			// set to represent the roomba position
			transform.translate(simulatedPosition().getX(), simulatedPosition().getY())
			transform.rotate(simulatedAngle())
			// compute bumpers, may bump only when driving forward
			val rightBumper = speed.get > 0 && !PhysicalRoomba.rightBumper.forall(p => {
				val p2 = transform.transform(p, null)
				!walls.contains(p2.getX, p2.getY)
				})
			val leftBumper  = speed.get > 0 && !PhysicalRoomba.leftBumper.forall(p => {
				val p2 = transform.transform(p, null)
				!walls.contains(p2.getX, p2.getY)
			})
			backBump.set(speed.get < 0 && !PhysicalRoomba.backBumper.forall(p => {
				val p2 = transform.transform(p, null)
				!walls.contains(p2.getX, p2.getY)
			}))
			
			if(state == ControlState.Safe && (leftBumper || rightBumper)){
				setSpeedAndRadius(0, 0x8000)
			}
			sensorsState.leftBump = Some(leftBumper)
			sensorsState.rightBump = Some(rightBumper)
			// if we are in safe mode -> we know that we had a collision and stop
			
			
			// compute wall detector
			
			// compute drops
			
			// ToDo: dirt etc...
			
			
			
			sensorsStateNotify(sensorsState.asImmutable)
		}
	}
	
	var state : ControlState = ControlState.Off
	
	// virtual sensors
	private val backBump = new AtomicReference[Boolean](false)
	
	val sensorsState = new MutableSensorsState
	private def sensorsStateNotify(state : SensorsState){
		onSensorsStateComputed.foreach(s => future{s(state)})
	}
	val onSensorsStateComputed = new MutableList[(SensorsState) => Unit]
	/// -- CALLBACKS --

	/**
	 * Callback for the Start command
	 */
	def onStart(): Unit = {
		state = ControlState.Passive
		println("[ " + Console.GREEN + "SIM" + Console.RESET +" ] Start")
	}
	/**
	 * Callback for the Baud command
	 */
	def onBaud(data: Byte): Unit = {
		state = ControlState.Passive
		println("[ " + Console.GREEN + "SIM" + Console.RESET +" ] Baud")
	}
	/**
	 * Callback for the Control command
	 */
	def onControl(): Unit = {
		state = ControlState.Safe
		println("[ " + Console.GREEN + "SIM" + Console.RESET +" ] Control")
	}
	/**
	 * Callback for the Safe command
	 */
	def onSafe(): Unit = {
		state = ControlState.Safe
		println("[ " + Console.GREEN + "SIM" + Console.RESET +" ] Safe")
	}
	/**
	 * Callback for the Full command
	 */
	def onFull(): Unit = {
		state = ControlState.Full
		println("[ " + Console.GREEN + "SIM" + Console.RESET +" ] Full")
	}
	/**
	 * Callback for the Power command
	 */
	def onPower(): Unit = {
		state = ControlState.Passive
		println("[ " + Console.GREEN + "SIM" + Console.RESET +" ] Power")
	}
	/**
	 * Callback for the Spot command
	 */
	def onSpot(): Unit = {
		state = ControlState.Passive
		println("[ " + Console.GREEN + "SIM" + Console.RESET +" ] Spot")
	}
	/**
	 * Callback for the Clean command
	 */
	def onClean(): Unit = {
		state = ControlState.Passive
		println("[ " + Console.GREEN + "SIM" + Console.RESET +" ] Clean")
	}
	/**
	 * Callback for the Max command
	 */
	def onMax(): Unit = {
		state = ControlState.Passive
		println("[ " + Console.GREEN + "SIM" + Console.RESET +" ] Max")
	}
	
	private def setSpeedAndRadius(speed : Int, radius: Int){
		simulationWorker.stop
		distanceAtLastDriveCommand.set(sensorsState.distance.get)
		angleAtLastDriveCommand.set(sensorsState.angle.get)
		lastDriveCommandTimestamp.set(Platform.currentTime)
		this.speed.set(speed)
		this.radius.set(radius)
	}
	/**
	 * Callback for the Drive command
	 */
	def onDrive(data: Array[Byte]): Unit = {
		println("[ " + Console.GREEN + "SIM" + Console.RESET +" ] Drive")
		setSpeedAndRadius(
				ByteOperations.byteArray2Short(data.slice(0,2)),
				ByteOperations.byteArray2Short(data.slice(2,4)))
		println("\t speed: " + speed.get + " , radius: " + radius.get)
		if(speed.get != 0){
			simulationWorker start
		}
		
	}
	/**
	 * Callback for the Motors command
	 */
	def onMotors(data: Byte): Unit = {
		println("[ " + Console.GREEN + "SIM" + Console.RESET +" ] Motors")
	}
	/**
	 * Callback for the Leds command
	 */
	def onLeds(data: Array[Byte]): Unit = {
		println("[ " + Console.GREEN + "SIM" + Console.RESET +" ] Leds")
	}
	/**
	 * Callback for the Song command
	 */
	def onSong(data: Array[Byte]): Unit = {
		println("[ " + Console.GREEN + "SIM" + Console.RESET +" ] Song")
	}
	/**
	 * Callback for the Play command
	 */
	def onPlay(data: Byte): Unit = {
		println("[ " + Console.GREEN + "SIM" + Console.RESET +" ] Play")
	}
	/**
	 * callback for the
	 * @param packet The sensors packet to get.
	 */
	def onSensors(code: Byte): Array[Byte] = {
		println("[ " + Console.GREEN + "SIM" + Console.RESET +" ] Sensors")
		// set the last time we cot data = now, and the offset distance to 0
		if (code == Controls.code || code == AllSensors.code) {
			lastDriveCommandTimestamp set Platform.currentTime
			distanceAtLastDriveCommand set 0
			angleAtLastDriveCommand set 0
		}
		code match {
			case AllSensors.code => sensorsState.getByteArray(AllSensors)
			case Detectors.code => sensorsState.getByteArray(Detectors)
			case Controls.code => sensorsState.getByteArray(Controls)
			case Health.code => sensorsState.getByteArray(Health)
		}
	}
	/**
	 * Callback for the ForceSeekingDock command
	 */
	def onForceSeekingDock(): Unit = {
		println("[ " + Console.GREEN + "SIM" + Console.RESET +" ] Force Seeking Dock")
	}
	
	/// -- INET SERVER --
	private val roombaSocketServer = new RoombaSocketServer
	
	// register callbacks
	roombaSocketServer.onStart = Some(onStart)
	roombaSocketServer.onBaud = Some(onBaud)
	roombaSocketServer.onControl = Some(onControl)
	roombaSocketServer.onSafe = Some(onSafe)
	roombaSocketServer.onFull = Some(onFull)
	roombaSocketServer.onPower = Some(onPower)
	roombaSocketServer.onSpot = Some(onSpot)
	roombaSocketServer.onClean = Some(onClean)
	roombaSocketServer.onMax = Some(onMax)
	roombaSocketServer.onDrive = Some(onDrive)
	roombaSocketServer.onMotors = Some(onMotors)
	roombaSocketServer.onLeds = Some(onLeds)
	roombaSocketServer.onSong = Some(onSong)
	roombaSocketServer.onPlay = Some(onPlay)
	roombaSocketServer.onSensors = Some(onSensors)
	roombaSocketServer.onForceSeekingDock = Some(onForceSeekingDock)
	/**
	 * Starts the roomba
	 */
	def start(): Unit = {
		roombaSocketServer.start
	}

	/**
	 * Shutdown the simulated roomba
	 */
	def shutdown(): Unit = {
		simulationWorker.stop
		roombaSocketServer.stop
	}

}