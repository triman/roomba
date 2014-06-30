package org.github.triman.roomba

import org.github.triman.roomba.communication.RoombaCommunicator
import org.github.triman.roomba.utils.ByteOperations._
import org.github.triman.roomba.communication.CommunicatorContainer
import scala.actors.Future
import akka.pattern.ask
import scala.concurrent.duration._
import akka.util.Timeout
import akka.actor.PoisonPill


abstract class AbstractRoomba extends IRoomba{
	self : CommunicatorContainer =>
	
	private var isStarted = false
	private var isUserControlEnabled = false
	private var fullModeEnabled = false
	
	/**
	 * Starts the SCI interface. This must be called before any other command
	 */
	def start() : Unit = {
		communicator ! Start.opcode
		isStarted = true
	}
	/** 
	 *  Sets the baud rate of the sci interface
	 *  @param rate The baud rate to set. The value must be one of (300, 600, 1200, 2400, 4800, 9600, 14400, 19200, 28800, 38400, 57600, 115200)
	 */
	def baud(rate : Int) : Unit = {
		val rates = List(300, 600, 1200, 2400, 4800, 9600, 14400, 19200, 28800, 38400, 57600, 115200)
		require(rates.contains(rate), "The baud rate is invalid")
		assume(isStarted, "The interface should be started first")
		communicator ! Array(Baud.opcode, rates.indexOf(rate).toByte)
	}
	/**
	 * Enables user control of Roomba. This command must be sent
	 * after the start command and before any control commands are
	 * sent to the SCI. The SCI must be in passive mode to accept this
	 * command. This command puts the SCI in safe mode.
	 */
	def control() : Unit = {
		assume(isStarted, "The interface should be started first")
		communicator ! Control.opcode
		isUserControlEnabled = true
	}
	/**
	 * This command puts the SCI in safe mode. The SCI must be in
	 * full mode to accept this command.
	 * Note: In order to go from passive mode to safe mode, use the Control
	 * command.
	 */
	def safe() : Unit = {
		assume(isStarted, "The interface should be started first")
		assume(fullModeEnabled, "The roomba should be in full mode in order to be put back in safe mode")
		communicator ! Safe.opcode
		fullModeEnabled = false
	}
	/**
	 * Enables unrestricted control of Roomba through the SCI and
	 * turns off the safety features. The SCI must be in safe mode to
	 * accept this command. This command puts the SCI in full mode.
	 */
	def full() : Unit = {
		assume(isStarted, "The interface should be started first")
		assume(isUserControlEnabled && !fullModeEnabled, "The roomba should be in safe mode in order to be put in full mode")
		communicator ! Full.opcode
		fullModeEnabled = true
	}
	/**
	 * Puts Roomba to sleep, the same as a normal “power” button
	 * press. The Device Detect line must be held low for 500 ms to
	 * wake up Roomba from sleep. The SCI must be in safe or full
	 * mode to accept this command. This command puts the SCI in
	 * passive mode.
	 */
	def power() : Unit = {
		assume(isStarted, "The interface should be started first")
		communicator ! Power.opcode
	}
	/**
	 * Starts a spot cleaning cycle, the same as a normal “spot”
	 * button press. The SCI must be in safe or full mode to accept this
	 * command. This command puts the SCI in passive mode.
	 */
	def spot() : Unit = {
		assume(isStarted, "The interface should be started first")
		assume(isUserControlEnabled, "The roomba should be in safe or full mode before isuing the spot command")
		communicator ! Spot.opcode
	}
	/**
	 * Starts a normal cleaning cycle, the same as a normal “clean”
	 * button press. The SCI must be in safe or full mode to accept this
	 * command. This command puts the SCI in passive mode.
	 */
	def clean() : Unit = {
		assume(isStarted, "The interface should be started first")
		assume(isUserControlEnabled, "The roomba should be in safe or full mode before isuing the clean command")
		communicator ! Clean.opcode
	}
	/**
	 * Starts a maximum time cleaning cycle, the same as a normal
	 * “max” button press. The SCI must be in safe or full mode to
	 * accept this command. This command puts the SCI in passive
	 * mode.
	 */
	def max() : Unit = {
		assume(isStarted, "The interface should be started first")
		assume(isUserControlEnabled, "The roomba should be in safe or full mode before isuing the max command")
		communicator ! Max.opcode
	}
	/**
	 * Drive in a curve with a certain speed and a certain radius
	 * @param speed The average speed of the wheels [mm/s], in range [-500,500]
	 * @param radius The radius to turm [mm], in range [-2000,2000]
	 */
	def drive(speed : Short, radius : Short) : Unit = {
		assume(isStarted, "The interface should be started first")
		assume(isUserControlEnabled, "The roomba should be in safe or full mode before isuing the max command")
		require(speed >= -500 && speed <= 500 && (radius == 0x8000.toShort || (radius >= -2000 && radius <= 2000)))
		
		communicator ! (Drive.opcode +: short2ByteArray(speed)) ++ short2ByteArray(radius)
	}
	/**
	 * Controls the cleaning motors
	 * @param sideBrush The side brush should be turned on.
	 * @param vacuum The vacuum should be turned on.
	 * @param mainBrush The main brush should be turned on.
	 */
	def motors(sideBrush : Boolean, vacuum : Boolean, mainBrush : Boolean) : Unit = {
		assume(isStarted, "The interface should be started first")
		assume(isUserControlEnabled, "The roomba should be in safe or full mode before isuing the max command")
		val b = ((if (mainBrush) 4 else 0) + (if (vacuum) 2 else 0) + (if (sideBrush) 1 else 0)) toByte
		
		communicator ! Array(Motors.opcode, b)
	}
	/**Controls Roomba’s LEDs. The state of each of the spot, clean,
	 * max, and dirt detect LEDs is specified by one bit in the first data
	 * byte. The color of the status LED is specified by two bits in the
	 * first data byte. The power LED is specified by two data bytes, one
	 * for the color and one for the intensity. The SCI must be in safe
	 * or full mode to accept this command. This command does not
	 * change the mode.
	 * @param status The status led state
	 * @param spot The spot led status
	 * @param max the max led status
	 * @param dirtDetect The dirt detect led status
	 * @param powerColor The color of the power led [0-255]
	 * @param powerIntensity The intensity of the power led [0-255]
	 */
	def leds(status : Int, spot : Boolean, clean : Boolean, max : Boolean, dirtDetect : Boolean, powerColor : Byte, powerIntensity : Byte){
		assume(isStarted, "The interface should be started first")
		assume(isUserControlEnabled, "The roomba should be in safe or full mode before isuing the max command")
		require(status >=0 && status < 4,"The status is a 2 bits integer (i.e. included between 0 and 3 included).")
		
		val b = int2ByteArray((status << 4) + (if (spot) 8 else 0) + (if(clean) 4 else 0) + (if (max) 2 else 0) + (if (dirtDetect)1 else 0)).last
		
		communicator ! Array(Leds.opcode, b, powerColor, powerIntensity)
	}
	/**
	 * Specifies a song to the SCI to be played later. Each song is
	 * associated with a song number which the Play command uses
	 * to select the song to play. Users can specify up to 16 songs
	 * with up to 16 notes per song. Each note is specified by a note
	 * number using MIDI note definitions and a duration specified
	 * in fractions of a second. The number of data bytes varies
	 * depending on the length of the song specified. A one note song
	 * is specified by four data bytes. For each additional note, two data
	 * bytes must be added. The SCI must be in passive, safe, or full
	 * mode to accept this command. This command does not change
	 * the mode.
	 * @param songNumber The song number [0-15]
	 * @param notes The notes of the song, as tuples of Byte, the first byte is the chord, the second one the duration
	 */
	def song(songNumber : Int, notes : List[Tuple2[Byte, Byte]]) : Unit = {
		require(songNumber >= 0 && songNumber <= 15,"The song number should be between 0 and 15 included.")
		require(notes.length < 16,"The song can have up to 16 notes")
		require(notes.forall(c => c._1 >=31 && c._1 <=127), "All the chords should be between 31 and 127 included.")
		assume(isStarted, "The interface should be started first")
		
		var song = notes.flatMap(t => Array(t._1, t._2)).toArray
		communicator ! Array(Song.opcode ,songNumber toByte ,notes.length toByte) ++ song
	}
	/**
	 * Plays one of 16 songs, as specified by an earlier Song
	 * command. If the requested song has not been specified yet,
	 * the Play command does nothing. The SCI must be in safe or full
	 * mode to accept this command. This command does not change
	 * the mode.
	 * @param songNumber Number of the song to play
	 */
	def play(songNumber : Int) : Unit = {
		require(songNumber >= 0 && songNumber <= 15,"The song number should be between 0 and 15 included.")
		assume(isStarted, "The interface should be started first")
		
		communicator ! Array(Play.opcode, songNumber toByte)
	}
	/**
	 * Requests the SCI to send a packet of sensor data bytes. The
	 * user can select one of four different sensor packets. The sensor
	 * data packets are explained in more detail in the next section.
	 * The SCI must be in passive, safe, or full mode to accept this
	 * command. This command does not change the mode.
	 */
	def sensor(packet : SensorPacket) = {
		assume(isStarted, "The interface should be started first")
		// ToDo: Implement this
		implicit val timeout = Timeout(5 seconds)
		communicator ? Array(Sensors.opcode, packet.code)
	}
	/**
	 * Turns on force-seeking-dock mode, which causes the robot
	 * to immediately attempt to dock during its cleaning cycle if it
	 * encounters the docking beams from the Home Base. (Note,
	 * however, that if the robot was not active in a clean, spot or max
	 * cycle it will not attempt to execute the docking.) Normally the
	 * robot attempts to dock only if the cleaning cycle has completed
	 * or the battery is nearing depletion. This command can be sent
	 * anytime, but the mode will be cancelled if the robot turns off,
	 * begins charging, or is commanded into SCI safe or full modes.
	 */
	def forceSeekingDock() : Unit = {
		assume(isStarted, "The interface should be started first")
		
		communicator ! ForceSeekingDock.opcode
	}
	
	/**
	 * Terminates the system.
	 */
	def shutdown() : Unit
}