import org.github.triman.roomba.AbstractRoomba
import scala.concurrent.Await
import scala.concurrent.duration._
import org.github.triman.roomba._
import org.github.triman.roomba.simulator.SimulatedRoomba
import org.github.triman.roomba.simulator.communication.NetworkCommunicatorContainer
import scala.compat.Platform
object Main {

	def main(args: Array[String]): Unit = {
		val s = new SimulatedRoomba()
		s.start
		
		object R extends AbstractRoomba with NetworkCommunicatorContainer with PositionableRoomba
		
		R.start
		R.baud(300)
		
		R.control()
		
		R.drive(100)
		println(Platform.currentTime)
		Thread.sleep(500)
		R.drive(0)
		println(Platform.currentTime)
		println("R: " + R.position())
		println("S: " + s.simulatedPosition())
		R.shutdown()
	}

}