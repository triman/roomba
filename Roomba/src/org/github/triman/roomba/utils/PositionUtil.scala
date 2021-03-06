package org.github.triman.roomba.utils

import java.awt.Point
import org.github.triman.roomba.RoombaProperties
/**
	 * Computes the new position after a displacement on a circular path.
	 * @param p0 Initial position [mm, mm]
	 * @param a0 Initial angle [rad]
	 * @param d Driven distance [mm]
	 * @param r Radius [mm]. A radius of 0x8000 is considered as infinite -> driving straight
	 */
object PositionUtil {
	def getPointAndAngleOnCircularPath(p0 : Point, t0 : Double, d : Int, r : Int) : (Point, Double) = {
		
		import Math._
		if(d == 0){
			(p0, t0)
		}
		else if (r == 0x8000.toShort) { // driving straight
			val p = new Point(
				(p0.x + d*cos(t0)).round.toInt,
				(p0.y + d*sin(t0)).round.toInt)
			(p, t0)
		} 
		else if(r == 0){
			(p0, t0)	// hack that should never happen
		}	else { // driving along an arc
			
			val dt = (d.toDouble*min(1.0, abs(2*r.toDouble/RoombaProperties.wheelDistance))) / r.toDouble
			val p = new Point(
				(p0.x - r*sin(t0) + r*sin(t0 + dt)).round.toInt,
				(p0.y + r*cos(t0) - r*cos(t0 + dt)).round.toInt)
			var t1 = t0 + dt
			t1 -= 2*PI*(floor(t1/2*PI))
			(p, t1)
		}
		
	}
}