package org.github.triman.roomba.simulator.communication

import java.net.ServerSocket
import java.io.IOException
import java.net.Socket
import java.net.SocketException
import org.github.triman.roomba._

/**
 * Network interface for the roomba simulator, used to replace the serial connector with a phisical roomba.
 */
class RoombaSocketServer extends Thread{
	override def run() {
		try {
      val listener = new ServerSocket(15000);
      println("[ RSS ] Listening on : " + listener.getInetAddress + " : " + listener.getLocalPort)
      while (true)
        new RoombaServerThread(listener.accept()).start();
      listener.close()
    }
    catch {
      case e: IOException =>
        System.err.println("[ " + Console.RED + "RSS" + Console.RESET +" ] Could not listen on port: 9999.");
        System.exit(-1)
    }
	}
	
	/**
	 * Handle for a single client
	 */
	class RoombaServerThread(val socket : Socket) extends Thread {
		override def run(){
			println("[ " + Console.GREEN + "RSS" + Console.RESET +" ] New client connected : " + socket.getInetAddress().toString())
			try {
				val out = socket.getOutputStream()
		    val in = socket.getInputStream()
		    
		    while(true){
		    	val b = in.read()
		    	b.toByte match {
		    		case Start.opcode => if (onStart.isDefined) onStart.get ()
		    		case Baud.opcode 	=> {
		    			assert(in.available >= 1, "The data byte(s) is/are not send")
		    			val a = new Array[Byte](1)
		    			in.read(a)
		    			if (onBaud.isDefined) onBaud.get (a(0))
		    		}
		    		case Control.opcode => if (onControl.isDefined) onControl.get ()
		    		case Safe.opcode	=> if (onSafe.isDefined) onSafe.get ()
		    		case Full.opcode => if (onFull.isDefined) onFull.get ()
		    		case Power.opcode => if (onPower.isDefined) onPower.get ()
		    		case Spot.opcode => if (onSpot.isDefined) onSpot.get ()
		    		case Clean.opcode => if (onClean.isDefined) onClean.get ()
		    		case Max.opcode => if (onMax.isDefined) onMax.get ()
		    		case Drive.opcode => {
		    			assert(in.available >= 4, "The data byte(s) is/are not send")
		    			val a = new Array[Byte](4)
		    			in.read(a)
		    			if (onDrive.isDefined) onDrive.get (a)
		    			}
		    		case Motors.opcode => {
		    			assert(in.available >= 1, "The data byte(s) is/are not send")
		    			val a = new Array[Byte](1)
		    			in.read(a)
		    			if (onMotors.isDefined) onMotors.get (a(0))
		    			}
		    		case Leds.opcode => {
		    			assert(in.available >= 3, "The data byte(s) is/are not send")
		    			val a = new Array[Byte](3)
		    			in.read(a)
		    			if (onLeds.isDefined) onLeds.get (a)
		    			}
		    		case Song.opcode => {
		    			assert(in.available >= 2, "The data byte(s) is/are not send")
		    			val a = new Array[Byte](2)
		    			in.read(a)
		    			val b = new Array[Byte](2*a(1))
		    			in.read(b)
		    			if (onSong.isDefined) onSong.get (a ++ b)
		    			}
		    		case Play.opcode => {
		    			assert(in.available >= 1, "The data byte(s) is/are not send")
		    			val a = new Array[Byte](1)
		    			in.read(a)
		    			if (onPlay.isDefined) onPlay.get (a(0))
		    			}
		    		case Sensors.opcode => {
		    			assert(in.available >= 1, "The data byte(s) is/are not send")
		    			val a = new Array[Byte](1)
		    			in.read(a)
		    			if (onSensors.isDefined) out write (onSensors.get (a(0)))
		    			}
		    		case ForceSeekingDock.opcode => if (onForceSeekingDock.isDefined) onForceSeekingDock.get ()
		    		case _ => println("[ " + Console.YELLOW + "RSS" + Console.RESET +" ] Unknown command recieved : " + b.toString)
		    	}
		    }
			}catch {
	      case e: SocketException => println("[ RSS ] Client at " + socket.getInetAddress() + " disconnected")
	      case e: IOException =>
	        e.printStackTrace();
	    }
		}
	}
	
	
	/**
	 * Callback for the Start command
	 */
	var onStart : Option[() => Unit] = None
	/**
	 * Callback for the Baud command
	 */
	var onBaud : Option[(Byte) => Unit] = None
	/**
	 * Callback for the Control command
	 */
	var onControl : Option[() => Unit] = None
	/**
	 * Callback for the Safe command
	 */
	var onSafe : Option[() => Unit] = None
	/**
	 * Callback for the Full command
	 */
	var onFull : Option[() => Unit] = None
	/**
	 * Callback for the Power command
	 */
	var onPower : Option[() => Unit] = None
	/**
	 * Callback for the Spot command
	 */
	var onSpot : Option[() => Unit] = None
	/**
	 * Callback for the Clean command
	 */
	var onClean : Option[() => Unit] = None
	/**
	 * Callback for the Max command
	 */
	var onMax : Option[() => Unit] = None
	/**
	 * Callback for the Drive command
	 */
	var onDrive : Option[(Array[Byte]) => Unit] = None
	/**
	 * Callback for the Motors command
	 */
	var onMotors : Option[(Byte) => Unit] = None
	/**
	 * Callback for the Leds command
	 */
	var onLeds : Option[(Array[Byte]) => Unit] = None
	/**
	 * Callback for the Song command
	 */
	var onSong : Option[(Array[Byte]) => Unit] = None
	/**
	 * Callback for the Play command
	 */
	var onPlay : Option[(Byte) => Unit] = None
	/**
	 * Callback for the Sensors command
	 */
	var onSensors : Option[(Byte) => Array[Byte]] = None
	/**
	 * Callback for the ForceSeekingDock command
	 */
	var onForceSeekingDock : Option[() => Unit] = None
}