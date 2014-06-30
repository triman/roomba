package org.github.triman.roomba.simulator

import org.github.triman.roomba.utils.ByteOperations._

import org.github.triman.roomba.SensorsState
import org.github.triman.roomba.ISensorsState
import org.github.triman.roomba.SensorPacket
import org.github.triman.roomba.AllSensors
import org.github.triman.roomba.Detectors
import org.github.triman.roomba.Controls
import org.github.triman.roomba.Health

class MutableSensorsState(
		var casterWheelDrop : Option[Boolean],
		var leftWheelDrop : Option[Boolean],
		var rightWheelDrop : Option[Boolean],
		var leftBump : Option[Boolean],
		var rightBump : Option[Boolean],
		var wall : Option[Boolean],
		var cliffLeft : Option[Boolean],
		var cliffFrontLeft : Option[Boolean],
		var cliffFrontRight : Option[Boolean],
		var cliffRight : Option[Boolean],
		var virtualWall : Option[Boolean],
		var motorOvercurrentLeft : Option[Boolean],
		var motorOvercurrentRight : Option[Boolean],
		var motorOvercurrentMainBrush : Option[Boolean],
		var motorOvercurrentVacuum : Option[Boolean],
		var motorOvercurrentSideBrush : Option[Boolean],
		/**
		 * The current dirt detection level of the left side dirt detector
		 * is sent as a one byte varue. A varue of 0 indicates no dirt is
		 * detected. Higher varues indicate higher levels of dirt detected.
		 *  Range: 0 - 255
		 */
		var dirtDetectorLeft : Option[Int],
		/**
		 * The current dirt detection level of the right side dirt detector
		 * is sent as a one byte varue. A varue of 0 indicates no dirt is
		 * detected. Higher varues indicate higher levels of dirt detected.
		 *  Range: 0 - 255
		 * 
		 * Note: Some robots don’t have a right dirt detector. You can tell by removing
		 * the brushes. The dirt detectors are metallic disks. For robots with no right
		 * dirt detector this byte is always 0.
		 */
		var dirtDetectorRight : Option[Int],
		/**
		 * The command number of the remote control command currently
		 * being received by Roomba. A varue of 255 indicates that no
		 * remote control command is being received. See Roomba remote
		 * control documentation for a description of the command varues.
		 * Range: 0 - 255
		 */
		var remoteControlCommand : Option[Int],
		var powerButton : Option[Boolean],
		var spotButton : Option[Boolean],
		var cleanButton : Option[Boolean],
		var maxButton : Option[Boolean],
		/**
		 * The distance that Roomba has traveled in millimeters since the
		 * distance it was last requested. This is the same as the sum of
		 * the distance traveled by both wheels divided by two. Positive
		 * varues indicate travel in the forward direction; negative in the
		 * reverse direction. If the varue is not polled frequently enough, it
		 * will be capped at its minimum or maximum.
		 * Range: -32768 – 32767
		 */
		var distance : Option[Int],
		/**
		 * The angle that Roomba has turned through since the angle was
		 * last requested. The angle is expressed as the difference in
		 * the distance traveled by Roomba’s two wheels in millimeters,
		 * specifically the right wheel distance minus the left wheel
		 * distance, divided by two. This makes counter-clockwise angles
		 * positive and clockwise angles negative. This can be used to
		 * directly calculate the angle that Roomba has turned through
		 * since the last request. Since the distance between Roomba’s
		 * wheels is 258mm, the equations for calculating the angles in
		 * familiar units are:
		 * Angle in radians = (2 * difference) / 258
		 * Angle in degrees = (360 * difference) / (258 * Pi).
		 * If the varue is not polled frequently enough, it will be capped at
		 * its minimum or maximum.
		 * Note: Reported angle and distance may not be accurate. Roomba
		 * measures these by detecting its wheel revolutions. If for example, the
		 * wheels slip on the floor, the reported angle of distance will be greater than
		 * the actual angle or distance.
		 * Range: -32768 – 32767
		 */
		var angle : Option[Double],
		/**
		 * A code indicating the current charging state of Roomba.
		 */
		var chargingState : Option[Int],
		/**
		 * The voltage of Roomba’s battery in millivolts (mV).
		 * Range: 0 - 65535
		 */
		var voltage : Option[Int],
		/**
		 * The current in milliamps (mA) flowing into or out of Roomba’s
		 * battery. Negative currents indicate current is flowing out of the
		 * battery, as during normal running. Positive currents indicate
		 * current is flowing into the battery, as during charging.
		 * Range: -32768 – 32767
		 */
		var current : Option[Int],
		/**
		 * The temperature of Roomba’s battery in degrees Celsius.
		 * Range: -128 - 127
		 */
		var temperature : Option[Int],
		/**
		 * The current charge of Roomba’s battery in milliamp-hours (mAh).
		 * The charge varue decreases as the battery is depleted during
		 * running and increases when the battery is charged.
		 * Range: 0 - 65535
		 */
		var charge : Option[Int],
		/**
		 * The estimated charge capacity of Roomba’s battery. When the
		 * Charge varue reaches the Capacity varue, the battery is fully
		 * charged.
		 * Range: 0 - 65535
		 */
		var capacity : Option[Int]) extends ISensorsState{
	def this() = this(
		Option(false), // casterWheelDrop
		Option(false), // leftWheelDrop
		Option(false), // rightWheelDrop
		Option(false), // leftBump
		Option(false), // rightBump
		Option(false), // wall
		Option(false), // cliffLeft
		Option(false), // cliffFrontLeft
		Option(false), // cliffFrontRight
		Option(false), // cliffRight
		Option(false), // virtualWal
		Option(false), // motorOvercurrentLeft
		Option(false), // motorOvercurrentRight
		Option(false), // motorOvercurrentMainBrush
		Option(false), // motorOvercurrentVacuum
		Option(false), // motorOvercurrentSideBrush
		Option(0), 		 // dirtDetectorLeft
		Option(0), 	 	 // dirtDetectorRight
		Option(0),		 // remoteControlCommand
		Option(false), // powerButton
		Option(false), // spotButton
		Option(false), // cleanButton
		Option(false), // maxButton
		Option(0), 	   // distance
		Option(0), 		 // angle
		Option(0), 		 // chargingState
		Option(0), 		 // voltage
		Option(0),		 // current
		Option(0), 		 // temperature
		Option(0), 		 // charge
		Option(0)			 // capacity
	)
	
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