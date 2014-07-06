package org.github.triman.roomba.simulator.communication

import java.net.ServerSocket
import java.io.IOException
import java.net.Socket
import java.net.SocketException
import org.github.triman.roomba.Start

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
        System.err.println("Could not listen on port: 9999.");
        System.exit(-1)
    }
	}
	
	/**
	 * Handle for a single client
	 */
	class RoombaServerThread(val socket : Socket) extends Thread {
		override def run(){
			println("[ RSS ] New client connected : " + socket.getInetAddress().toString())
			try {
				val out = socket.getOutputStream()
		    val in = socket.getInputStream()
		    
		    while(true){
		    	val b = in.read()
		    	b.toByte match {
		    		case Start.opcode => if (onStart.isDefined) onStart.get ()
		    		case _ => println("[ RSS ] Unknown command recieved : " + b.toString)
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