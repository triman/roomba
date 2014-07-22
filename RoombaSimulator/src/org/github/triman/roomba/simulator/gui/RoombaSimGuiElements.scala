package org.github.triman.roomba.simulator.gui

import scala.xml.XML
import org.github.triman.roomba.simulator.utils.NonValidatingSAXParserFactory
import scala.io.Source
import java.awt.geom.AffineTransform
import org.github.triman.roomba.simulator.gui.utils.SVGUtils
import org.github.triman.graphics._
import scala.xml.Elem

object RoombaSimGuiElements {
	/**
	 * Retrieves a drawable roomba from the resources embedded within the project.
	 */
	def roomba() = {
		val rx = XML.withSAXParser(NonValidatingSAXParserFactory.getInstance)
			.loadString(Source.fromURL(getClass().getResource("roomba.svg")).getLines().reduce(_+_))
			
		val t = new AffineTransform
		t.setToIdentity()
		
		// set initial roomba position (size is 100%, so the svg image should be aprox 500x500px).
		t.translate(-250, -250)
		val roombaPosition = new AffineTransform
		roombaPosition.setToIdentity
		new TransformableDrawable(new TransformableDrawable(SVGUtils.svg2Drawable(rx), t), roombaPosition)
	}
	
	/**
	 * Extracts the room map from an SVG file. The actual scale is 1px = 1mm.
	 * @param filePath Path to the SVG file.
	 */
	def room(filePath : String) : Drawable = {
		var r : Drawable = null;
		// ToDo: extract the shape with id="room" ou la premiere shape du groupe avec id="room".
		try{
			r = SVGUtils.svg2Drawable(
					SVGUtils.readXML(filePath))
		}catch{
			case _ : Throwable => println("An error occured while trying to read the room file.")
		}
		r
	}
}