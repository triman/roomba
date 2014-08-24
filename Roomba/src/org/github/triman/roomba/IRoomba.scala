package org.github.triman.roomba

import scala.concurrent.Future

trait IRoomba extends Shutdownable {
	/**
	 * Starts the SCI interface. This must be called before any other command
	 */
	def start() : Unit
	/** 
	 *  Sets the baud rate of the sci interface
	 *  @param rate The baud rate to set. The value must be one of (300, 600, 1200, 2400, 4800, 9600, 14400, 19200, 28800, 38400, 57600, 115200)
	 */
	def baud(rate : Int) : Unit
	/**
	 * Enables user control of Roomba. This command must be sent
	 * after the start command and before any control commands are
	 * sent to the SCI. The SCI must be in passive mode to accept this
	 * command. This command puts the SCI in safe mode.
	 */
	def control() : Unit 
	/**
	 * This command puts the SCI in safe mode. The SCI must be in
	 * full mode to accept this command.
	 * Note: In order to go from passive mode to safe mode, use the Control
	 * command.
	 */
	def safe() : Unit
	/**
	 * Enables unrestricted control of Roomba through the SCI and
	 * turns off the safety features. The SCI must be in safe mode to
	 * accept this command. This command puts the SCI in full mode.
	 */
	def full() : Unit
	/**
	 * Puts Roomba to sleep, the same as a normal “power” button
	 * press. The Device Detect line must be held low for 500 ms to
	 * wake up Roomba from sleep. The SCI must be in safe or full
	 * mode to accept this command. This command puts the SCI in
	 * passive mode.
	 */
	def power() : Unit
	/**
	 * Starts a spot cleaning cycle, the same as a normal “spot”
	 * button press. The SCI must be in safe or full mode to accept this
	 * command. This command puts the SCI in passive mode.
	 */
	def spot() : Unit 
	/**
	 * Starts a normal cleaning cycle, the same as a normal “clean”
	 * button press. The SCI must be in safe or full mode to accept this
	 * command. This command puts the SCI in passive mode.
	 */
	def clean() : Unit 
	/**
	 * Starts a maximum time cleaning cycle, the same as a normal
	 * “max” button press. The SCI must be in safe or full mode to
	 * accept this command. This command puts the SCI in passive
	 * mode.
	 */
	def max() : Unit
	/**
	 * Drive straight at a certain speed
	 * @param speed The drive speed [mm/s]
	 */
	def drive(speed : Short) : Unit = drive(speed,0x8000.toShort)
	/**
	 * Drive in a curve with a certain speed and a certain radius
	 * @param speed The average speed of the wheels [mm/s], in range [-500,500]
	 * @param radius The radius to turm [mm], in range [-2000,2000]
	 */
	def drive(speed : Short, radius : Short) : Unit
	/**
	 * Stops the roomba
	 */
	def stop() = drive(0)
	/**
	 * Controls the cleaning motors
	 * @param sideBrush The side brush should be turned on.
	 * @param vacuum The vacuum should be turned on.
	 * @param mainBrush The main brush should be turned on.
	 */
	def motors(sideBrush : Boolean, vacuum : Boolean, mainBrush : Boolean) : Unit
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
	def leds(status : Int, spot : Boolean, clean : Boolean, max : Boolean, dirtDetect : Boolean, powerColor : Byte, powerIntensity : Byte)
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
	def song(songNumber : Int, notes : List[Tuple2[Byte, Byte]]) : Unit
	/**
	 * Plays one of 16 songs, as specified by an earlier Song
	 * command. If the requested song has not been specified yet,
	 * the Play command does nothing. The SCI must be in safe or full
	 * mode to accept this command. This command does not change
	 * the mode.
	 * @param songNumber Number of the song to play
	 */
	def play(songNumber : Int) : Unit
	/**
	 * Requests the SCI to send a packet of sensor data bytes. The
	 * user can select one of four different sensor packets. The sensor
	 * data packets are explained in more detail in the next section.
	 * The SCI must be in passive, safe, or full mode to accept this
	 * command. This command does not change the mode.
	 */
	def sensor(packet : SensorPacket) : Future[Any]
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
	def forceSeekingDock() : Unit
	
}