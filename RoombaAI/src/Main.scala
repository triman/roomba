import org.github.triman.roomba.AbstractRoomba
import org.github.triman.roomba.DriveExtension
import org.github.triman.roomba.PositionableRoomba
import org.github.triman.roomba.communication.NetworkCommunicatorContainer
import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global
import akka.actor.Status._
import scala.util.{Success, Failure}
object Main {
	def main(args : Array[String]){
		
		object R extends AbstractRoomba with NetworkCommunicatorContainer with PositionableRoomba with DriveExtension {
			override val refreshRate = 100
		}
		
		R.start
		R.baud(300)
		
		R.control
		R.tryDrive(-100) andThen {
			case r : Success[Any] => println("yeah!")
			case f : Failure[Any] => println("Nope: " + f.exception.getMessage())
			case r => println(r)
		} andThen {
			case _ => R.shutdown()
		}
	}
}