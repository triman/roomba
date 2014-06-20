package org.github.triman.roomba

import org.github.triman.roomba.utils.Notifier
import java.awt.Point

trait PositionableRoomba {
	
	val position = new Notifier[Point, Symbol](null){def id = 'RoombaPositionChange}
}