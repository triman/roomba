package org.github.triman.roomba.simulator.gui

import org.github.triman.roomba.simulator.RoombaSimulatorApplication
import scala.swing.Action

object ToolbarListener {
	def init() : Unit = {
		val pauseAction = Action(""){
			RoombaSimulatorApplication.simulatedRoomba.pause
			MainWindow.pauseButton.enabled = false
			MainWindow.startButton.enabled = true
		}
		pauseAction.icon = MainWindow.pauseButton.icon
		MainWindow.pauseButton.action = pauseAction
		
		val startAction = Action(""){
			RoombaSimulatorApplication.simulatedRoomba.unPause
			MainWindow.pauseButton.enabled = true
			MainWindow.startButton.enabled = false
		}
		startAction.enabled = false
		startAction.icon = MainWindow.startButton.icon
		MainWindow.startButton.action = startAction
	}
	
	val resetAction = Action(""){
		RoombaSimulatorApplication.simulatedRoomba.reset
	}
	resetAction.icon = MainWindow.resetButton.icon
	MainWindow.resetButton.action = resetAction
	
}