package org.github.triman.roomba

import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global
import scala.util.Try
import scala.util.Success
import scala.util.Failure
import scala.compat.Platform
trait DriveExtension extends IRoomba {
	private var executionThread: Thread = null
	// time interval (in ms) between 2 checks of the sensors state
	val refreshRate: Int

	private def executeWithStateCheck(
		maxDuration : Option[Long],
		c: (SensorsState) => Boolean,
		interruptionHandler: (Boolean) => Unit): Future[Any] = {
		Future {
			synchronized {
				val startTime = Platform.currentTime
				var interrupted = false;
				executionThread = new Thread(new Runnable {
					def run() {
						try {
							var hasToRun = true;
							while (hasToRun && !executionThread.isInterrupted()) {
								val r = DriveExtension.this.sensor(Detectors)
								val callback = (values: Try[Any]) => {
									values match {
										case Success(v) => {
											val sensorsState = SensorsState.getSensorState(Detectors, v.asInstanceOf[Array[Byte]])
											maxDuration match{
												case None => {}
												case Some(m) => hasToRun = m > (Platform.currentTime - startTime)
											}
											if(!c(sensorsState)){
												hasToRun = false;
											}
										}
										case _ => {}
									}
								}
								r onComplete callback
								Thread.sleep(maxDuration match{
									case None => refreshRate
									case Some(m) => Math.max(0,
											Math.min(
												m - (Platform.currentTime - startTime)
												, refreshRate		
											))
								})
							}
						}catch {
							case e: InterruptedException => interrupted = true
							case _: Throwable => {} // swallow exceptions
						}
						stop
					}
				})
				executionThread.start // run the thread
				executionThread.join
				interruptionHandler(interrupted)
			}
		}
	}

	/**
	 * Drive straight until getting blocked
	 * @param speed Speed [mm/s]
	 */
	def tryDrive(speed: Short): Future[Any] = {
		drive(speed)
		executeWithStateCheck(
				None,
			_ => true,
			_ => {})
	}

	/**
	 * Drive straight for a maximum distance
	 * @param speed Speed [mm/s]
	 * @param distance Distance [mm]
	 */
	def tryDrive(speed: Short, distance: Int): Future[Any] = {
		drive(speed)
		executeWithStateCheck(
			Some(Math.abs((1000 * distance) / speed)),
			_ => true,
			t => if (t) { throw new Exception("interrupted") })
	}
	
	/**
	 * Turns as long as it can
	 * @param speed Speed [mm/s]
	 * @param radius Radius [mm]
	 */
	def tryTurn(speed : Short, radius: Short) : Future[Any] = {
		drive(speed, radius)
		executeWithStateCheck(None, _ => true, _ => {})
	}
	
	/**
	 * Tries to turn with a certain angle
	 * @param angle Angle we should try tu turn
	 * @param speed Speed [mm/s]
	 * @param radius Radius [mm]
	 */
	def tryTurn(angle : Double, speed : Short, radius : Short) : Future[Any] = {
		drive(speed, radius)
		executeWithStateCheck(
				Some(Math.abs((1000 * radius.signum * Math.max(radius.abs, 129) * angle).toInt / speed)),
			_ => true,
			t => {
				if (t) {
				throw new Exception("interrupted") }}
		)
	}
	

	/**
	 * Get the sensors data.
	 * @param packet The sensors packet to get.
	 */
	abstract override def sensor(packet: SensorPacket): Future[Any] = {
		val f = super.sensor(packet)
		val callback = (values: Try[Any]) => {
			values match {
				case Success(v) => {
					val sensorsState = SensorsState.getSensorState(packet, v.asInstanceOf[Array[Byte]])
					List(sensorsState.cliffFrontLeft, sensorsState.cliffFrontRight, sensorsState.cliffLeft, sensorsState.cliffRight, sensorsState.casterWheeldrop, sensorsState.leftWheelDrop, sensorsState.rightWheeldrop, sensorsState.leftBump, sensorsState.rightBump).foreach(_ match {
						case Some(true) => {
							this.executionThread.interrupt // interrupt in case of drop / bump / cliff
						}
						case _ => {}
					})
				}
				case Failure(v) => {}
			}
		}
		if (executionThread != null) {
			f onComplete callback
		}
		return f
	}
}