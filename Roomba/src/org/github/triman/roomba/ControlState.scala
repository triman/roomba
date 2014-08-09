package org.github.triman.roomba

sealed abstract class ControlState
object ControlState{
	case object Off extends ControlState
	case object Passive extends ControlState
	case object Full extends ControlState
	case object Safe extends ControlState
}

