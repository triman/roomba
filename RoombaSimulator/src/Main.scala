import org.github.triman.roomba.AbstractRoomba
import scala.concurrent.Await
import scala.concurrent.duration._
import org.github.triman.roomba._
import org.github.triman.roomba.simulator.communication.ByteStreamCommunicatorContainer
object Main {

	def main(args: Array[String]): Unit = {
		object TestRoomba extends AbstractRoomba with ByteStreamCommunicatorContainer
		
		TestRoomba.start
		var v = TestRoomba.sensor(AllSensors)
		println(Await.result(v,5 seconds).asInstanceOf[String])
		TestRoomba.shutdown
	}

}