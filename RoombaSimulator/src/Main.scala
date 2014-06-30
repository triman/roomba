import org.github.triman.roomba.AbstractRoomba
import scala.concurrent.Await
import scala.concurrent.duration._
import org.github.triman.roomba._
import org.github.triman.roomba.simulator.communication.ByteStreamCommunicatorContainer
object Main {

	def main(args: Array[String]): Unit = {
		object TestRoomba extends AbstractRoomba with ByteStreamCommunicatorContainer with PositionableRoomba
		
		TestRoomba.start
	
		TestRoomba.start
		TestRoomba.baud(300)
		TestRoomba.control
		val in = TestRoomba.asInstanceOf[ByteStreamCommunicatorContainer].in
		in.read(new Array[Byte](in.available()))
		
		TestRoomba.drive(100, 1000)
		Thread.sleep(1000)
		TestRoomba.state.angle = Option(Math.PI/2.0)
		TestRoomba.state.distance = Option(1570)
		
		TestRoomba.position.attend(p => {
			println("New position: " + p)
		})
		TestRoomba.sensor(Controls)
		
		Thread.sleep(1000)
		
		TestRoomba.shutdown
	}

}