package org.github.triman.roomba.simulator.gui

import scala.xml.XML
import org.github.triman.roomba.simulator.utils.NonValidatingSAXParserFactory
import scala.io.Source
import java.awt.geom.AffineTransform
import org.github.triman.graphics.TransformableDrawable
import org.github.triman.roomba.simulator.gui.utils.SVGUtils
import org.github.triman.graphics.Drawable
import org.github.triman.graphics.UntransformedDrawable

object RoombaSimGuiElements {
	// roomba
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
	 */
	def room(filePath : String) : Drawable = {
		var r : Drawable = null;
		// ToDo: extract the shape with id="room" ou la premiere shape du groupe avec id="room".
		try{
			r = SVGUtils.svg2Drawable(
					XML.withSAXParser(NonValidatingSAXParserFactory.getInstance)
					.loadString(Source.fromFile(
							filePath).getLines().reduce(_ + _)))
		}catch{
			case _ : Throwable => println("An error occured while trying to read the room file.")
		}
		r
	}
	
	/**
	 * gets the shapes to draw the status of the roomba (detectors, ...).
	 * ToDo: refactor into proper structure with captors etc...
	 */
	def roombaStatus() : Drawable = {
		val rx = XML.withSAXParser(NonValidatingSAXParserFactory.getInstance)
			.loadString(Source.fromURL(getClass().getResource("roomba_status.svg")).getLines().reduce(_+_))
		val roombaPosition = new AffineTransform
		roombaPosition.setToIdentity
		new TransformableDrawable(new UntransformedDrawable(SVGUtils.svg2Drawable(rx)), roombaPosition)
	}
}