package org.github.triman.roomba

import org.github.triman.utils.Notifier
import java.awt.Point
import java.util.concurrent.atomic.AtomicReference
import scala.concurrent.Future
import scala.util.Try
import scala.concurrent.ExecutionContext
import scala.util.Success
import scala.util.Failure
import scala.compat.Platform
import org.github.triman.roomba.utils.PositionUtil

abstract sealed class PositionComputationType
case object InterpolatedPosition extends PositionComputationType
case object ComputedPosition extends PositionComputationType

trait PositionableRoomba extends IRoomba{
	
	/**
	 * Roomba's computed position
	 */
	val position = new Notifier[Point, Symbol](new Point(0,0)){def id = 'RoombaPositionChange}
	/**
	 * Roomba's computed angle
	 */
	val angle = new Notifier[Double, Symbol](0){def id = 'RoombaAngleChange}
	private var _positionComputationType: PositionComputationType = ComputedPosition
	def positionComputationType = _positionComputationType
	
	/**
	 * Target speed [mm/s]
	 */
	protected val speed = new AtomicReference[Int](0)
	/**
	 * Target radius [mm]
	 */
	protected val radius = new AtomicReference[Int](0)
	
	private var lastEnsuredAngle = 0.0
	private var lastEnsuredPosition = new Point(0,0)
	
	/**
	 * Time between 2 refresh of the position (computation) [ms]
	 */
	var positionRefreshDuration : Int = 100
	
	/// -- usage of the function calls to get informations --
	import ExecutionContext.Implicits.global
	
	// position computation callback
	private def sensorCallback(speed : Int, radius: Int, packet : SensorPacket, values : Try[Any]) : Unit = {
		values match {
			case Success(v) => {
				val sensorsState = SensorsState.getSensorState(packet, v.asInstanceOf[Array[Byte]])
				if(sensorsState.angle.isDefined && sensorsState.distance.isDefined){
					// compute position from radius, initial angle and distance
					val p = PositionUtil.getPointAndAngleOnCircularPath(lastEnsuredPosition, lastEnsuredAngle,
							Math.signum(speed).toInt * sensorsState.distance.get ,radius)
							println("s: last angle: " + lastEnsuredAngle)
							println("s: distance: " + sensorsState.distance.get)
							println("s: radius: " + radius)
							println("s: angle: " + sensorsState.angle.get)
					lastEnsuredAngle += sensorsState.angle.get
					lastEnsuredPosition = p._1
					_positionComputationType = ComputedPosition
					position.update(p._1)
					angle.update(lastEnsuredAngle)
				}
			}
			case Failure(v) => {}
		}
	}
	
	/**
	 * Get the sensors data.
	 * @param packet The sensors packet to get.
	 */
	abstract override def sensor(packet : SensorPacket) : Future[Any] = {
		val radius = this.radius.get
		val speed = this.speed.get
		val f = super.sensor(packet)
		val callback = (values : Try[Any]) => sensorCallback(speed, radius, packet, values)
		f onComplete callback
		return f
	}
	
	/**
	 * Drives the roomba
	 * @param speed The target speed [mm/s]
	 * @param radius The target radius [mm]
	 */
	abstract override def drive(speed : Short, radius : Short) : Unit = {
		// get the last position
		sensor(Controls)	// we don't need to process the response since it'll be handled by the sensorCallback
		// process the superclass
		super.drive(speed, radius)
		// set the current speed and radius
		this.speed.set(speed)
		this.radius.set(radius)
		if(speed != 0){
			// start computation
			positionComputationWorker.start
		}else{
			positionComputationWorker.stop
		}
	}
	
	/**
	 * Worker to compute the new position
	 */
	private object positionComputationWorker {
		private var t : Thread = null;
		/**
		 * Starts the worker
		 */
		def start(){
			synchronized{
				stop()
				
				t = new Thread{
					override def run(){
						val p0 = position()
						val a0 = angle()
						val t0 = Platform.currentTime
						// compute the new position every positionRefreshDuration ms
						while(true){
							// new distance
							val t1 = Platform.currentTime - t0
							val d = (speed.get() * t1).toInt / 1000
							
							val p1 = PositionUtil.getPointAndAngleOnCircularPath(p0, a0, d, radius.get())
							_positionComputationType = InterpolatedPosition
							position.update(p1._1)
							angle.update(p1._2)
							
							Thread.sleep(positionRefreshDuration)
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
					t.stop
				}
			}
		}
	}
	
	override def shutdown()  : Unit = {
		super.shutdown()
		positionComputationWorker stop
  	
	}
}