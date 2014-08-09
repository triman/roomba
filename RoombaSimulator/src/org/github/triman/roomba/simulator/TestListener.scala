package org.github.triman.roomba.simulator

import org.github.triman.roomba.simulator.gui.MainWindow
import scala.swing.event.MouseClicked
import java.awt.Point

object TestListener {
	def registerTest() : Unit = {
		MainWindow.canvas.reactions += {
			case e: MouseClicked  => {
				val inverse = MainWindow.canvas.currentTransform.createInverse()
				val p = new Point
				inverse.transform(e.point, p)
				println(p)
				
				println("in wall: " + RoombaSimulatorApplication.room.wallsDrawable.shape.contains(p))
				
      }
		}
	}
}