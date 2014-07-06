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
import org.github.triman.roomba.utils.Notifier
import org.github.triman.roomba.utils.PositionUtil
import org.github.triman.roomba.simulator.communication.RoombaSocketServer

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
	val simulatedAngle = new Notifier[Double, Symbol](0) { def id = 'RoombaSimulatedAngleChange }

	val speed = new AtomicReference[Int](0)
	val radius = new AtomicReference[Int](0)

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
								// compute displacement -> sensors data 
								// ToDo, depending on last refresh
								// compute position
								val dt = Platform.currentTime - t0
								val d = (speed.get() * dt).toInt / 1000
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
			//ToDo: implement this and remove this output
			println("Computing sensors for position: " + simulatedPosition())
		}
	}

	/// -- CALLBACKS --

	/**
	 * Callback for the Start command
	 */
	def onStart(): Unit = {
		println("[ " + Console.GREEN + "SIM" + Console.RESET +" ] Start")
	}
	/**
	 * Callback for the Baud command
	 */
	def onBaud(data: Byte): Unit = {
		println("[ " + Console.GREEN + "SIM" + Console.RESET +" ] Baud")
	}
	/**
	 * Callback for the Control command
	 */
	def onControl(): Unit = {
		println("[ " + Console.GREEN + "SIM" + Console.RESET +" ] Control")
	}
	/**
	 * Callback for the Safe command
	 */
	def onSafe(): Unit = {
		println("[ " + Console.GREEN + "SIM" + Console.RESET +" ] Safe")
	}
	/**
	 * Callback for the Full command
	 */
	def onFull(): Unit = {
		println("[ " + Console.GREEN + "SIM" + Console.RESET +" ] Full")
	}
	/**
	 * Callback for the Power command
	 */
	def onPower(): Unit = {
		println("[ " + Console.GREEN + "SIM" + Console.RESET +" ] Power")
	}
	/**
	 * Callback for the Spot command
	 */
	def onSpot(): Unit = {
		println("[ " + Console.GREEN + "SIM" + Console.RESET +" ] Spot")
	}
	/**
	 * Callback for the Clean command
	 */
	def onClean(): Unit = {
		println("[ " + Console.GREEN + "SIM" + Console.RESET +" ] Clean")
	}
	/**
	 * Callback for the Max command
	 */
	def onMax(): Unit = {
		println("[ " + Console.GREEN + "SIM" + Console.RESET +" ] Max")
	}
	/**
	 * Callback for the Drive command
	 */
	def onDrive(data: Array[Byte]): Unit = {
		println("[ " + Console.GREEN + "SIM" + Console.RESET +" ] Drive")
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
			lastControlsGetTimestamp set Platform.currentTime
			distanceAtLastDriveCommand set 0
			angleAtLastDriveCommand set 0
		}
		// ToDo: send the serialized sensor data
		null
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