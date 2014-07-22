package org.github.triman.roomba.simulator

import org.github.triman.graphics.Drawable
import org.github.triman.roomba.simulator.gui.RoombaSimGuiElements
import org.github.triman.roomba.simulator.gui.RoombaListener
import org.github.triman.roomba.simulator.gui.MainWindow
import javax.swing.UIManager
import org.github.triman.roomba.simulator.gui.utils.SVGUtils
import org.github.triman.roomba.simulator.gui.RoombaStatusDrawable

object RoombaSimulatorApplication {
	
	// enable macosx look and feel
	System.setProperty("apple.laf.useScreenMenuBar", "true")
	System.setProperty("com.apple.mrj.application.apple.menu.about.name", "Roomba simulator")
	UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName())
	
	val simulatedRoomba = new SimulatedRoomba()
	val listener = new RoombaListener(simulatedRoomba, MainWindow.canvas)
	
	// roomba status
	val roombaStatus = RoombaStatusDrawable
	MainWindow.canvas.shapes += roombaStatus
	
	var room : Drawable = null
	
	def main(args : Array[String]){
		// retrieve the room
		if (args.length > 0) {
			loadRoom(args(0))
		}
		// build the GUI
		MainWindow.visible = true
		
		// start the roomba
		simulatedRoomba start
		
	}
	
	def loadRoom(filePath : String){
		println("[ APP ] Loading room: " + filePath)
			SVGUtils.extractSVGShapeWithId(filePath, "room")
			room = RoombaSimGuiElements.room(filePath)
			MainWindow.room = room
			MainWindow.title = "Roomba simulator - " +filePath
	}
}