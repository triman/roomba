package org.github.triman.roomba.simulator.gui

import scala.xml.XML
import org.github.triman.roomba.simulator.utils.NonValidatingSAXParserFactory
import scala.io.Source
import java.awt.geom.AffineTransform
import org.github.triman.roomba.simulator.gui.utils.SVGUtils
import org.github.triman.graphics._
import scala.xml.Elem
import org.github.triman.roomba.simulator.environment.Room
import java.awt.Point

object RoombaSimGuiElements {
	/**
	 * Retrieves the pattern for the canvas background
	 */
	def canvasBackground() = {
		val xml = XML.withSAXParser(NonValidatingSAXParserFactory.getInstance)
			.loadString(Source.fromURL(getClass().getResource("transparent_background.svg")).getLines().reduce(_ + _))

		SVGUtils.svg2Drawable(xml)
	}

	/**
	 * Retrieves a drawable roomba from the resources embedded within the project.
	 */
	def roomba(position : Point, angle : Double) = {
		val rx = XML.withSAXParser(NonValidatingSAXParserFactory.getInstance)
			.loadString(Source.fromURL(getClass().getResource("roomba.svg")).getLines().reduce(_ + _))

		val t = new AffineTransform
		t.setToIdentity()

		// set initial roomba position (size is 100%, so the svg image should be aprox 500x500px).
		t.rotate(-3*Math.PI/2)
		t.translate(-175, -175)
		val roombaPosition = new AffineTransform
		roombaPosition.setToIdentity
		
		roombaPosition.translate(position.getX(), -position.getY())
		roombaPosition.rotate(-angle)
		
		new TransformableDrawable(new TransformableDrawable(SVGUtils.svg2Drawable(rx), t), roombaPosition)
	}

	/**
	 * Extracts the room map from an SVG file. The actual scale is 1px = 1mm.
	 * @param filePath Path to the SVG file.
	 */
	def room(filePath: String): Room = {
		var r: Room = null;
		// ToDo: extract the shape with id="room" ou la premiere shape du groupe avec id="room".
		try {
			r = new Room(filePath)
		}
		catch {
			case _: Throwable => println("An error occured while trying to read the room file.")
		}
		r
	}
}