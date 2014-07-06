import org.github.triman.roomba.AbstractRoomba
import scala.concurrent.Await
import scala.concurrent.duration._
import org.github.triman.roomba._
import org.github.triman.roomba.simulator.communication.ByteStreamCommunicatorContainer
import org.github.triman.roomba.simulator.SimulatedRoomba
object Main {

	def main(args: Array[String]): Unit = {
		new SimulatedRoomba().start
	}

}