package org.github.triman.roomba.tests

import org.scalatest.Matchers
import org.scalatest.FlatSpec
import java.io.InputStream
import org.scalatest.BeforeAndAfter
import org.github.triman.roomba.PositionableRoomba
import org.github.triman.roomba.AbstractRoomba
import org.github.triman.roomba.simulator.communication.ByteStreamCommunicatorContainer
import org.github.triman.roomba.Controls
import java.io.PipedOutputStream
import java.io.PipedInputStream
import org.scalatest.concurrent.AsyncAssertions.Waiter
import org.scalatest.time.SpanSugar._

class PositionSpec  extends FlatSpec with Matchers with BeforeAndAfter{
	
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
	"A roomba starting at (0,0)" should "be at position (1000,1000) after having driven 1571mm with a 1000mm radius" in {
		roomba.drive(100, 1000)
		Thread.sleep(1000)
		roomba.state.angle = Option(Math.PI/2.0)
		roomba.state.distance = Option(1571)
		
		val w = new Waiter
		roomba.position.attend(p => {
			w {
					p.x should be (1000)
					p.y should be (1000)
				}
			w.dismiss()
		})
		roomba.sensor(Controls)
		
		w.await()
	}
	it should "be at position (1000,-1000) after having driven 1571mm with a -1000mm radius" in {
		roomba.drive(100, -1000)
		Thread.sleep(1000)
		roomba.state.angle = Option(Math.PI/2.0)
		roomba.state.distance = Option(1571)
		
		val w = new Waiter
		roomba.position.attend(p => {
			w {
					p.x should be (1000)
					p.y should be (-1000)
				}
			w.dismiss()
		})
		roomba.sensor(Controls)
		
		w.await()
	}
	it should "be at position (1000,0) after having driven 1000mm straight" in {
		roomba.drive(100)
		roomba.state.angle = Option(Math.PI/2.0)
		roomba.state.distance = Option(1000)
		
		val w = new Waiter
		roomba.position.attend(p => {
			w {
					p.x should be (1000)
					p.y should be (0)
				}
			w.dismiss()
		})
		roomba.sensor(Controls)
		
		w.await()
	}
}