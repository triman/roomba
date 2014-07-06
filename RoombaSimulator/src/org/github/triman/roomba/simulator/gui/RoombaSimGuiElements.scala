package org.github.triman.roomba.simulator.gui

import scala.xml.XML
import org.github.triman.roomba.simulator.utils.NonValidatingSAXParserFactory
import scala.io.Source
import java.awt.geom.AffineTransform
import org.github.triman.graphics.TransformableDrawable
import org.github.triman.roomba.simulator.gui.utils.SVGUtils

object RoombaSimGuiElements {
	// roomba
	def roomba() = {
		val r = getClass().getResourceAsStream("roomba.svg")
		val rx = XML.withSAXParser(NonValidatingSAXParserFactory.getInstance)
			.loadString(Source.fromURL(getClass().getResource("roomba.svg")).getLines().reduce(_+_))
			
		val t = new AffineTransform
		t.setToIdentity()
		
		// set initial roomba position and size
		t.scale(0.1, 0.1)
		t.translate(-250, -250)
		val roombaPosition = new AffineTransform
		roombaPosition.setToIdentity
		new TransformableDrawable(new TransformableDrawable(SVGUtils.svg2Drawable(rx), t), roombaPosition)
	}
}