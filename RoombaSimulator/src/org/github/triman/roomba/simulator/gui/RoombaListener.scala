package org.github.triman.roomba.simulator.gui

import org.github.triman.roomba.simulator.SimulatedRoomba
import org.github.triman.window.Canvas
import org.github.triman.roomba.PositionableRoomba
import org.github.triman.roomba.SensorsState

class RoombaListener(val simulatedRoomba : SimulatedRoomba, val canvas : Canvas) {
	// create listeners
	simulatedRoomba.simulatedPosition.attend(_ => onRoombaPositionChange)
	simulatedRoomba.simulatedAngle.attend(_ => onRoombaPositionChange)
	simulatedRoomba.onSensorsStateComputed += onRoombaSensorsStateComputed
	// get the roomba shape and transform
	val roombaShape = RoombaSimGuiElements.roomba(simulatedRoomba.simulatedPosition(), simulatedRoomba.simulatedAngle())
	// get the status display
	val roombaStatus = RoombaStatusDrawable
	// add the roomba and status display on the canvas
	if(canvas != null){
		canvas.shapes += roombaShape
		canvas.shapes += roombaStatus
	}
	
	def onRoombaPositionChange(){
		// move the roomba
		val position = simulatedRoomba.simulatedPosition()
		val angle = simulatedRoomba.simulatedAngle()
		
		roombaShape.transform.setToIdentity()
		roombaShape.transform.translate(position.getX(), -position.getY())
		roombaShape.transform.rotate(-angle)
		canvas.repaint()
	}
	
	def onRoombaSensorsStateComputed(s : SensorsState){
		// bumpers
		if(s.leftBump.isDefined && s.leftBump.get){
			roombaStatus.setOff('BumperLeft)
		}else{
			roombaStatus.setOn('BumperLeft)
		}
		if(s.rightBump.isDefined && s.rightBump.get){
			roombaStatus.setOff('BumperRight)
		}else{
			roombaStatus.setOn('BumperRight)
		}
		// cliffs
		if(s.cliffLeft.isDefined && s.cliffLeft.get){
			roombaStatus.setOff('CliffLeft)
		}else{
			roombaStatus.setOn('CliffLeft)
		}
		if(s.cliffFrontLeft.isDefined && s.cliffFrontLeft.get){
			roombaStatus.setOff('CliffFrontLeft)
		}else{
			roombaStatus.setOn('CliffFrontLeft)
		}
		if(s.cliffFrontRight.isDefined && s.cliffFrontRight.get){
			roombaStatus.setOff('CliffFrontRight)
		}else{
			roombaStatus.setOn('CliffFrontRight)
		}
		if(s.cliffRight.isDefined && s.cliffRight.get){
			roombaStatus.setOff('CliffRight)
		}else{
			roombaStatus.setOn('CliffRight)
		}
		
		// drops
		if(s.casterWheeldrop.isDefined && s.casterWheeldrop.get){
			roombaStatus.setOff('CasterWheel)
		}else{
			roombaStatus.setOn('CasterWheel)
		}
		if(s.leftWheelDrop.isDefined && s.leftWheelDrop.get){
			roombaStatus.setOff('WheelLeft)
		}else{
			roombaStatus.setOn('WheelLeft)
		}
		if(s.rightWheeldrop.isDefined && s.rightWheeldrop.get){
			roombaStatus.setOff('WheelRight)
		}else{
			roombaStatus.setOn('WheelRight)
		}
		
		if(s.wall.isDefined && s.wall.get){
			roombaStatus.setOn('Wall)
		}else{
			roombaStatus.setOff('Wall)
		}
		
		if(s.virtualWall.isDefined && s.virtualWall.get){
			roombaStatus.setOff('VirtualWall)
		}else{
			roombaStatus.setOn('VirtualWall)
		}
		
		canvas.repaint()
	}
	
	
}