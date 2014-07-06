package org.github.triman.roomba.tests

import org.scalatest._
import org.github.triman.roomba.simulator.MutableSensorsState
import org.github.triman.roomba.AllSensors
import org.github.triman.roomba.Detectors
import org.github.triman.roomba.Controls
import org.github.triman.roomba.Health
import org.github.triman.roomba.PositionableRoomba
import org.github.triman.roomba.simulator.communication.ByteStreamCommunicatorContainer
import java.io.PipedInputStream
import org.github.triman.roomba.AbstractRoomba
import scala.concurrent.Await
import scala.concurrent.duration._
import org.github.triman.roomba.AbstractSensorsState
import org.github.triman.roomba.SensorsState

class SensorsDataSpec extends FlatSpec with Matchers with BeforeAndAfter{
	
	class TestRoomba extends AbstractRoomba with ByteStreamCommunicatorContainer with PositionableRoomba

	var roomba : TestRoomba = null
	var in : PipedInputStream = null
	before {
		roomba = new TestRoomba
		roomba.start
		roomba.baud(300)
		roomba.control
		in = roomba.asInstanceOf[ByteStreamCommunicatorContainer].in
		in.read(new Array[Byte](in.available()))
		}
	after{
	}
	
	"A roomba with a specified angle of PI/6" should "return PI/6 when asked for its angle" in {
		roomba.state.angle = Option(Math.PI/6.0)
		val s = roomba.sensor(Controls)
		val state = Await.result(s, 5 seconds).asInstanceOf[Array[Byte]]
		SensorsState.getSensorState(Controls, state).angle.get should be (Math.PI/6.0 +- 0.01)
	}
}