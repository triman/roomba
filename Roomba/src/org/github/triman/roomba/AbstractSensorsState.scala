package org.github.triman.roomba

import org.github.triman.roomba.utils.ByteOperations._

trait AbstractSensorsState {
		def casterWheelDrop : Option[Boolean]
		def leftWheelDrop : Option[Boolean]
		def rightWheelDrop : Option[Boolean]
		def leftBump : Option[Boolean]
		def rightBump : Option[Boolean]
		def wall : Option[Boolean]
		def cliffLeft : Option[Boolean]
		def cliffFrontLeft : Option[Boolean]
		def cliffFrontRight : Option[Boolean]
		def cliffRight : Option[Boolean]
		def virtualWall : Option[Boolean]
		def motorOvercurrentLeft : Option[Boolean]
		def motorOvercurrentRight : Option[Boolean]
		def motorOvercurrentMainBrush : Option[Boolean]
		def motorOvercurrentVacuum : Option[Boolean]
		def motorOvercurrentSideBrush : Option[Boolean]
		/**
		 * The current dirt detection level of the left side dirt detector
		 * is sent as a one byte defue. A defue of 0 indicates no dirt is
		 * detected. Higher defues indicate higher levels of dirt detected.
		 *  Range: 0 - 255
		 */
		def dirtDetectorLeft : Option[Int]
		/**
		 * The current dirt detection level of the right side dirt detector
		 * is sent as a one byte defue. A defue of 0 indicates no dirt is
		 * detected. Higher defues indicate higher levels of dirt detected.
		 *  Range: 0 - 255
		 * 
		 * Note: Some robots don’t have a right dirt detector. You can tell by removing
		 * the brushes. The dirt detectors are metallic disks. For robots with no right
		 * dirt detector this byte is always 0.
		 */
		def dirtDetectorRight : Option[Int]
		/**
		 * The command number of the remote control command currently
		 * being received by Roomba. A defue of 255 indicates that no
		 * remote control command is being received. See Roomba remote
		 * control documentation for a description of the command defues.
		 * Range: 0 - 255
		 */
		def remoteControlCommand : Option[Int]
		def powerButton : Option[Boolean]
		def spotButton : Option[Boolean]
		def cleanButton : Option[Boolean]
		def maxButton : Option[Boolean]
		/**
		 * The distance that Roomba has traveled in millimeters since the
		 * distance it was last requested. This is the same as the sum of
		 * the distance traveled by both wheels divided by two. Positive
		 * defues indicate travel in the forward direction; negative in the
		 * reverse direction. If the defue is not polled frequently enough it
		 * will be capped at its minimum or maximum.
		 * Range: -32768 – 32767
		 */
		def distance : Option[Int]
		/**
		 * The angle that Roomba has turned through since the angle was
		 * last requested. The angle is expressed as the difference in
		 * the distance traveled by Roomba’s two wheels in millimeters
		 * specifically the right wheel distance minus the left wheel
		 * distance divided by two. This makes counter-clockwise angles
		 * positive and clockwise angles negative. This can be used to
		 * directly calculate the angle that Roomba has turned through
		 * since the last request. Since the distance between Roomba’s
		 * wheels is 258mm the equations for calculating the angles in
		 * familiar units are:
		 * Angle in radians = (2 * difference) / 258
		 * Angle in degrees = (360 * difference) / (258 * Pi).
		 * If the defue is not polled frequently enough it will be capped at
		 * its minimum or maximum.
		 * Note: Reported angle and distance may not be accurate. Roomba
		 * measures these by detecting its wheel revolutions. If for example the
		 * wheels slip on the floor the reported angle of distance will be greater than
		 * the actual angle or distance.
		 * Range: -32768 – 32767
		 */
		def angle : Option[Double]
		/**
		 * A code indicating the current charging state of Roomba.
		 */
		def chargingState : Option[Int]
		/**
		 * The voltage of Roomba’s battery in millivolts (mV).
		 * Range: 0 - 65535
		 */
		def voltage : Option[Int]
		/**
		 * The current in milliamps (mA) flowing into or out of Roomba’s
		 * battery. Negative currents indicate current is flowing out of the
		 * battery as during normal running. Positive currents indicate
		 * current is flowing into the battery as during charging.
		 * Range: -32768 – 32767
		 */
		def current : Option[Int]
		/**
		 * The temperature of Roomba’s battery in degrees Celsius.
		 * Range: -128 - 127
		 */
		def temperature : Option[Int]
		/**
		 * The current charge of Roomba’s battery in milliamp-hours (mAh).
		 * The charge defue decreases as the battery is depleted during
		 * running and increases when the battery is charged.
		 * Range: 0 - 65535
		 */
		def charge : Option[Int]
		/**
		 * The estimated charge capacity of Roomba’s battery. When the
		 * Charge defue reaches the Capacity defue the battery is fully
		 * charged.
		 * Range: 0 - 65535
		 */
		def capacity : Option[Int]
		
		
		def getByteArray(packet : SensorPacket) : Array[Byte] = packet match {
		case AllSensors => getByteArray(Detectors) ++ getByteArray(Controls) ++ getByteArray(Health)
		case Detectors => {
			Array(
				(		// bump wheeldrops
					  (if (!casterWheelDrop.isEmpty && casterWheelDrop.get) 16 else 0)
					+ (if (!leftWheelDrop.isEmpty && leftWheelDrop.get) 8 else 0)
					+ (if (!rightWheelDrop.isEmpty && rightWheelDrop.get) 4 else 0)
					+ (if (!leftBump.isEmpty && leftBump.get) 2 else 0)
					+ (if (!rightBump.isEmpty && rightBump.get) 1 else 0)
				).toByte,
				// wall
				(if (!wall.isEmpty && wall.get) 1 else 0).toByte,
				// cliff left
				(if (!cliffLeft.isEmpty && cliffLeft.get) 1 else 0).toByte,
				// cliff front left
				(if (!cliffFrontLeft.isEmpty && cliffFrontLeft.get) 1 else 0).toByte,
				// cliff front right
				(if (!cliffFrontRight.isEmpty && cliffFrontRight.get) 1 else 0).toByte,
				// cliff right
				(if (!cliffRight.isEmpty && cliffRight.get) 1 else 0).toByte,
				// virtual wall
				(if (!virtualWall.isEmpty && virtualWall.get) 1 else 0).toByte,
				// motor overcurrents
				(
					  (if (!motorOvercurrentLeft.isEmpty && motorOvercurrentLeft.get) 16 else 0)
					+ (if (!motorOvercurrentRight.isEmpty && motorOvercurrentRight.get) 8 else 0)
					+ (if (!motorOvercurrentMainBrush.isEmpty && motorOvercurrentMainBrush.get) 4 else 0)
					+ (if (!motorOvercurrentVacuum.isEmpty && motorOvercurrentVacuum.get) 2 else 0)
					+ (if (!motorOvercurrentSideBrush.isEmpty && motorOvercurrentSideBrush.get) 1 else 0)
				).toByte,
				// dirt detector left
				(if (dirtDetectorLeft.isEmpty) 0 else dirtDetectorLeft.get).toByte,
				// dirt detector right
				(if (dirtDetectorRight.isEmpty) 0 else dirtDetectorRight.get).toByte
			)
		}
		case Controls => {
			Array(
					// remote control
				(if (remoteControlCommand.isEmpty) 0 else remoteControlCommand.get).toByte,
				// buttons
				(
					  (if (!powerButton.isEmpty && powerButton.get) 8 else 0)
					+ (if (!spotButton.isEmpty && spotButton.get) 4 else 0)
					+ (if (!cleanButton.isEmpty && cleanButton.get) 2 else 0)
					+ (if (!maxButton.isEmpty && maxButton.get) 1 else 0)
				).toByte
			) ++ (short2ByteArray((if (distance.isEmpty) 0 else distance.get).toShort)	++ short2ByteArray((if (angle.isEmpty) 0 else 129*angle.get).toShort))
		}
		case Health => {
			Array(
					// charging state
					(if (chargingState.isEmpty) 0 else chargingState.get).toByte
			) ++ int2ByteArray(if (voltage.isEmpty) 0 else voltage.get).dropRight(2) ++
			short2ByteArray((if (current.isEmpty) 0 else current.get).toShort) ++
			Array((if (temperature.isEmpty) 0 else temperature.get).toByte) ++
			int2ByteArray(if (charge.isEmpty) 0 else charge.get).dropRight(2) ++
			int2ByteArray(if (capacity.isEmpty) 0 else capacity.get).dropRight(2)
			
		}
	}
}