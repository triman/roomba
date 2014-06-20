package org.github.triman.roomba

trait ISensorsState {
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
		def angle : Option[Int]
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
}