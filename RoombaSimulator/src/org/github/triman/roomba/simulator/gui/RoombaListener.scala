package org.github.triman.roomba.simulator.gui

import org.github.triman.roomba.simulator.SimulatedRoomba
import org.github.triman.window.Canvas
import org.github.triman.roomba.PositionableRoomba

class RoombaListener(val simulatedRoomba : SimulatedRoomba, val canvas : Canvas) {
	// create listeners
	simulatedRoomba.simulatedPosition.attend(_ => onRoombaPositionChange)
	simulatedRoomba.simulatedAngle.attend(_ => onRoombaPositionChange)
	
	// get the roomba shape and transform
	val roombaShape = RoombaSimGuiElements.roomba()
	
	// add the roomba on the canvas
	if(canvas != null){
		canvas.shapes += roombaShape
	}
	
	def onRoombaPositionChange(){
		println("Roomba simulated position or angle changed")
		// move the roomba
		val position = simulatedRoomba.simulatedPosition()
		val angle = simulatedRoomba.simulatedAngle()
		
		roombaShape.transform.setToIdentity()
		roombaShape.transform.translate(position.getX(), position.getY())
		roombaShape.transform.rotate(angle)
		
		// draw a symbol for the estimated position
	}
	
}