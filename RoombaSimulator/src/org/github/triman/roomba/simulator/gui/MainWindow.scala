package org.github.triman.roomba.simulator.gui

import scala.xml.XML
import org.github.triman.roomba.simulator.gui.utils.SVGUtils
import scala.io.Source
import scala.swing.Frame
import java.awt.Dimension
import javax.swing.JFrame
import org.github.triman.window.Canvas
import org.github.triman.graphics.DrawableShapeCompanion
import org.github.triman.graphics.ColoredDrawableShape
import java.awt.geom.Ellipse2D
import java.awt.Color
import org.github.triman.graphics.DrawableShape
import org.github.triman.window.Canvas
import org.github.triman.graphics.TransformableDrawable
import java.awt.geom.AffineTransform
import javax.xml.parsers.SAXParserFactory
import org.github.triman.roomba.simulator.utils.NonValidatingSAXParserFactory
import scala.xml.Elem
/**
 * http://www.jasperpotts.com/blog/2007/07/svg-shape-2-java2d-code/
 */
object MainWindow extends Frame {
	def main(args: Array[String]): Unit = {

		var ry: Elem = null
		if (args.length > 0) {
			ry = XML.withSAXParser(NonValidatingSAXParserFactory.getInstance).loadString(Source.fromFile(args(0)).getLines().reduce(_ + _))
		}

		title = "Roomba simulator"
		visible = true
		peer.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE)
		val canvas = new Canvas
		canvas.preferredSize = new Dimension(1000, 500)
		canvas.background = Color.GRAY
		contents = canvas

		if (ry != null) {
			val tr = new AffineTransform
			tr.setToIdentity()
			tr.scale(0.1, 0.1)
			canvas.shapes += new TransformableDrawable(SVGUtils.svg2Drawable(ry), tr)
			title += " - " + args(0)
		}
		else {
			println("No room")
		}

		val positionnedRoomba = RoombaSimGuiElements.roomba()
		positionnedRoomba.transform.setToIdentity()

		positionnedRoomba.transform.translate(100, 100)
		positionnedRoomba.transform.rotate(-Math.PI / 2)

		canvas.shapes += positionnedRoomba
		canvas.shapes +=
			new ColoredDrawableShape(new DrawableShape(new Ellipse2D.Double(-2, -2, 4, 4)), new Color(255,0,0,128), Color.RED)
	}
}