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
		c: (SensorsState) => Boolean,
		interruptionHandler: (Boolean) => Unit): Future[Any] = {
		Future {
			synchronized {
				var interrupted = false;
				executionThread = new Thread(new Runnable {
					def run() {
						try {
							var hasToRun = true;
							while (hasToRun && !executionThread.isInterrupted()) {
								Thread.sleep(refreshRate)
								val r = DriveExtension.this.sensor(Detectors)

								val callback = (values: Try[Any]) => {
									values match {
										case Success(v) => {
											val sensorsState = SensorsState.getSensorState(Detectors, v.asInstanceOf[Array[Byte]])
											if(!c(sensorsState)){
												hasToRun = false;
												stop
											}
										}
										case _ => {}
									}
								}
								r onComplete callback
							}
						}catch {
							case e: InterruptedException => interrupted = true
							case _: Throwable => {} // swallow exceptions
						}
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
		val startTime = Platform.currentTime
			def check(s: SensorsState): Boolean = {
				Platform.currentTime - startTime < (1000 * distance / speed)
			}
		executeWithStateCheck(
			check(_),
			t => if (t) { throw new Exception("interrupted") })
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