package org.github.triman.roomba.simulator.physics

import java.awt.geom.Point2D
import Math._

/**
 * Position of sensors, based on measurments taken on a Roomba 770
 */
object PhysicalRoomba {	
	val rightBumper = 0 to 4 map(i => new Point2D.Double(175*sin(PI/10*i), 175*cos(PI/10*i)))	// 5 points on the right side
	val leftBumper	= 5 to 9 map(i => new Point2D.Double(175*sin(PI/10*i), 175*cos(PI/10*i))) // 5 points on the left side
	val wall				= new Point2D.Double(0, 195)	// 10mm outside on the right, on the horizontal axis
	val backBumper 	= 10 to 19 map(i => new Point2D.Double(175*sin(PI/10*i), 175*cos(PI/10*i)))	// 10 virtual back bumpers (not present on the roomba)
	
	val casterWheel = new Point2D.Double(130, 0)	// on the axis, 130mm from the center
	val rightWheel 	= new Point2D.Double(0, 129)	// space between wheels is 258mm (specs)
	val leftWheel		= new Point2D.Double(0, -129)
	
	val cliffFrontLeft = new Point2D.Double(150, -40)
	val cliffFrontRight = new Point2D.Double(150, 40)
	val cliffRight = List(new Point2D.Double(55, 150), new Point2D.Double(55, -150))
	val cliffLeft = List(new Point2D.Double(-55, 150), new Point2D.Double(-55, -150))
	
	// ToDo: add virtual wall
}