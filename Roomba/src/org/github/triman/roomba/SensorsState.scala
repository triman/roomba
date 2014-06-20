package org.github.triman.roomba

import org.github.triman.roomba.utils.ByteOperations._

class SensorsState (
		val casterWheeldrop : Option[Boolean],
		val leftWheelDrop : Option[Boolean],
		val rightWheeldrop : Option[Boolean],
		val leftBump : Option[Boolean],
		val rightBump : Option[Boolean],
		val wall : Option[Boolean],
		val cliffLeft : Option[Boolean],
		val cliffFrontLeft : Option[Boolean],
		val cliffFrontRight : Option[Boolean],
		val cliffRight : Option[Boolean],
		val virtualWall : Option[Boolean],
		val motorOvercurrentLeft : Option[Boolean],
		val motorOvercurrentRight : Option[Boolean],
		val motorOvercurrentMainBrush : Option[Boolean],
		val motorOvercurrentVacuum : Option[Boolean],
		val motorOvercurrentSideBrush : Option[Boolean],
		/**
		 * The current dirt detection level of the left side dirt detector
		 * is sent as a one byte value. A value of 0 indicates no dirt is
		 * detected. Higher values indicate higher levels of dirt detected.
		 *  Range: 0 - 255
		 */
		val dirtDetectorLeft : Option[Int],
		/**
		 * The current dirt detection level of the right side dirt detector
		 * is sent as a one byte value. A value of 0 indicates no dirt is
		 * detected. Higher values indicate higher levels of dirt detected.
		 *  Range: 0 - 255
		 * 
		 * Note: Some robots don’t have a right dirt detector. You can tell by removing
		 * the brushes. The dirt detectors are metallic disks. For robots with no right
		 * dirt detector this byte is always 0.
		 */
		val dirtDetectorRight : Option[Int],
		/**
		 * The command number of the remote control command currently
		 * being received by Roomba. A value of 255 indicates that no
		 * remote control command is being received. See Roomba remote
		 * control documentation for a description of the command values.
		 * Range: 0 - 255
		 */
		val remoteControlCommand : Option[Int],
		val powerButton : Option[Boolean],
		val spotButton : Option[Boolean],
		val cleanButton : Option[Boolean],
		val maxButton : Option[Boolean],
		/**
		 * The distance that Roomba has traveled in millimeters since the
		 * distance it was last requested. This is the same as the sum of
		 * the distance traveled by both wheels divided by two. Positive
		 * values indicate travel in the forward direction; negative in the
		 * reverse direction. If the value is not polled frequently enough, it
		 * will be capped at its minimum or maximum.
		 * Range: -32768 – 32767
		 */
		val distance : Option[Int],
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
		 * If the value is not polled frequently enough, it will be capped at
		 * its minimum or maximum.
		 * Note: Reported angle and distance may not be accurate. Roomba
		 * measures these by detecting its wheel revolutions. If for example, the
		 * wheels slip on the floor, the reported angle of distance will be greater than
		 * the actual angle or distance.
		 * Range: -32768 – 32767
		 */
		val angle : Option[Int],
		/**
		 * A code indicating the current charging state of Roomba.
		 */
		val chargingState : Option[Int],
		/**
		 * The voltage of Roomba’s battery in millivolts (mV).
		 * Range: 0 - 65535
		 */
		val voltage : Option[Int],
		/**
		 * The current in milliamps (mA) flowing into or out of Roomba’s
		 * battery. Negative currents indicate current is flowing out of the
		 * battery, as during normal running. Positive currents indicate
		 * current is flowing into the battery, as during charging.
		 * Range: -32768 – 32767
		 */
		val current : Option[Int],
		/**
		 * The temperature of Roomba’s battery in degrees Celsius.
		 * Range: -128 - 127
		 */
		val temperature : Option[Int],
		/**
		 * The current charge of Roomba’s battery in milliamp-hours (mAh).
		 * The charge value decreases as the battery is depleted during
		 * running and increases when the battery is charged.
		 * Range: 0 - 65535
		 */
		val charge : Option[Int],
		/**
		 * The estimated charge capacity of Roomba’s battery. When the
		 * Charge value reaches the Capacity value, the battery is fully
		 * charged.
		 * Range: 0 - 65535
		 */
		val capacity : Option[Int]
		)
object SensorState {
	def getSensorState(sensorPacket: SensorPacket, values : Array[Byte]) : SensorsState = sensorPacket match {
		/**
		 * 26 bytes
		 */
		case AllSensors => {
			assert(values.length == 26, "The values array should be 26 bytes for all sensors")
			new SensorsState(
				// bumps and wheeldrops
				Option((values(0) & 0x10) > 0),
				Option((values(0) & 0x08) > 0),
				Option((values(0) & 0x04) > 0),
				Option((values(0) & 0x02) > 0),
				Option((values(0) & 0x01) > 0),
				// wall
				Option((values(1) & 0x01) > 0),
				// cliff left
				Option((values(2) & 0x01) > 0),
				// cliff front left
				Option((values(3) & 0x01) > 0),
				// cliff front right
				Option((values(4) & 0x01) > 0),
				// cliff right
				Option((values(5) & 0x01) > 0),
				// virtual wall
				Option((values(6) & 0x01) > 0),
				// motor overcurrents
				Option((values(7) & 0x10) > 0),
				Option((values(7) & 0x08) > 0),
				Option((values(7) & 0x04) > 0),
				Option((values(7) & 0x02) > 0),
				Option((values(7) & 0x01) > 0),
				// dirt detector left
				Option(byteArray2Int(Array(values(8)))),
				// dirt detector right
				Option(byteArray2Int(Array(values(9)))),
				// remote control
				Option(byteArray2Int(Array(values(10)))),
				// buttons
				Option((values(11) & 0x08) > 0),
				Option((values(11) & 0x04) > 0),
				Option((values(11) & 0x02) > 0),
				Option((values(11) & 0x01) > 0),
				// distance
				Option(byteArray2Short(values.slice(12,2))),
				// angle
				Option(byteArray2Short(values.slice(14,2))),
				// charging status
				Option(byteArray2Int(Array(values(16)))),
				// voltage
				Option(byteArray2Int(values.slice(17,2))),
				// current
				Option(byteArray2Short(values.slice(18,2))),
				// temperature
				Option(values(21).toInt),
				// charge
				Option(byteArray2Int(values.slice(22,2))),
				// capacity
				Option(byteArray2Int(values.slice(24,2)))
			)
		}
		case Detectors => {
			assert(values.length == 10,  "The values array should be 10 bytes for the detectors")
			new SensorsState(
				// bumps and wheeldrops
				Option((values(0) & 0x10) > 0),
				Option((values(0) & 0x08) > 0),
				Option((values(0) & 0x04) > 0),
				Option((values(0) & 0x02) > 0),
				Option((values(0) & 0x01) > 0),
				// wall
				Option((values(1) & 0x01) > 0),
				// cliff left
				Option((values(2) & 0x01) > 0),
				// cliff front left
				Option((values(3) & 0x01) > 0),
				// cliff front right
				Option((values(4) & 0x01) > 0),
				// cliff right
				Option((values(5) & 0x01) > 0),
				// virtual wall
				Option((values(6) & 0x01) > 0),
				// motor overcurrents
				Option((values(7) & 0x10) > 0),
				Option((values(7) & 0x08) > 0),
				Option((values(7) & 0x04) > 0),
				Option((values(7) & 0x02) > 0),
				Option((values(7) & 0x01) > 0),
				// dirt detector left
				Option(byteArray2Int(Array(values(8)))),
				// dirt detector right
				Option(byteArray2Int(Array(values(9)))),
				// remote control
				None,
				// buttons
				None,
				None,
				None,
				None,
				// distance
				None,
				// angle
				None,
				// charging status
				None,
				// voltage
				None,
				// current
				None,
				// temperature
				None,
				// charge
				None,
				// capacity
				None
			)
		}
		case Controls => {
			new SensorsState(
				// bumps and wheeldrops
				Option((values(0) & 0x10) > 0),
				Option((values(0) & 0x08) > 0),
				Option((values(0) & 0x04) > 0),
				Option((values(0) & 0x02) > 0),
				Option((values(0) & 0x01) > 0),
				// wall
				Option((values(1) & 0x01) > 0),
				// cliff left
				Option((values(2) & 0x01) > 0),
				// cliff front left
				Option((values(3) & 0x01) > 0),
				// cliff front right
				Option((values(4) & 0x01) > 0),
				// cliff right
				Option((values(5) & 0x01) > 0),
				// virtual wall
				Option((values(6) & 0x01) > 0),
				// motor overcurrents
				Option((values(7) & 0x10) > 0),
				Option((values(7) & 0x08) > 0),
				Option((values(7) & 0x04) > 0),
				Option((values(7) & 0x02) > 0),
				Option((values(7) & 0x01) > 0),
				// dirt detector left
				Option(byteArray2Int(Array(values(8)))),
				// dirt detector right
				Option(byteArray2Int(Array(values(9)))),
				// remote control
				Option(byteArray2Int(Array(values(10)))),
				// buttons
				Option((values(11) & 0x08) > 0),
				Option((values(11) & 0x04) > 0),
				Option((values(11) & 0x02) > 0),
				Option((values(11) & 0x01) > 0),
				// distance
				Option(byteArray2Short(values.slice(12,2))),
				// angle
				Option(byteArray2Short(values.slice(14,2))),
				// charging status
				Option(byteArray2Int(Array(values(16)))),
				// voltage
				Option(byteArray2Int(values.slice(17,2))),
				// current
				Option(byteArray2Short(values.slice(18,2))),
				// temperature
				Option(values(21).toInt),
				// charge
				Option(byteArray2Int(values.slice(22,2))),
				// capacity
				Option(byteArray2Int(values.slice(24,2)))
			)
		}
		case Health => {
			new SensorsState(
				// bumps and wheeldrops
				Option((values(0) & 0x10) > 0),
				Option((values(0) & 0x08) > 0),
				Option((values(0) & 0x04) > 0),
				Option((values(0) & 0x02) > 0),
				Option((values(0) & 0x01) > 0),
				// wall
				Option((values(1) & 0x01) > 0),
				// cliff left
				Option((values(2) & 0x01) > 0),
				// cliff front left
				Option((values(3) & 0x01) > 0),
				// cliff front right
				Option((values(4) & 0x01) > 0),
				// cliff right
				Option((values(5) & 0x01) > 0),
				// virtual wall
				Option((values(6) & 0x01) > 0),
				// motor overcurrents
				Option((values(7) & 0x10) > 0),
				Option((values(7) & 0x08) > 0),
				Option((values(7) & 0x04) > 0),
				Option((values(7) & 0x02) > 0),
				Option((values(7) & 0x01) > 0),
				// dirt detector left
				Option(byteArray2Int(Array(values(8)))),
				// dirt detector right
				Option(byteArray2Int(Array(values(9)))),
				// remote control
				Option(byteArray2Int(Array(values(10)))),
				// buttons
				Option((values(11) & 0x08) > 0),
				Option((values(11) & 0x04) > 0),
				Option((values(11) & 0x02) > 0),
				Option((values(11) & 0x01) > 0),
				// distance
				Option(byteArray2Short(values.slice(12,2))),
				// angle
				Option(byteArray2Short(values.slice(14,2))),
				// charging status
				Option(byteArray2Int(Array(values(16)))),
				// voltage
				Option(byteArray2Int(values.slice(17,2))),
				// current
				Option(byteArray2Short(values.slice(18,2))),
				// temperature
				Option(values(21).toInt),
				// charge
				Option(byteArray2Int(values.slice(22,2))),
				// capacity
				Option(byteArray2Int(values.slice(24,2)))
			)
		}
	}
}