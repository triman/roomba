package org.github.triman.roomba.simulator.gui.utils

import org.github.triman.graphics.Drawable
import java.io.InputStream
import scala.collection.mutable.MutableList
import org.github.triman.graphics.CompositeDrawableShape
import scala.xml.Node
import scala.xml.Elem
import java.awt.geom.Ellipse2D
import org.github.triman.graphics._
import java.awt.Color
import org.apache.batik.parser.AWTPathProducer
import java.io.StringReader
import java.awt.geom.GeneralPath

object SVGUtils {
	val TRANSPARENT = new Color(255,255,255,0)
	def svg2Drawable(svg : Elem) : Drawable = {
		assert(svg.label == "svg", "The root node of an SVG document should be an <svg></svg> tag")
		val drawables = new MutableList[Drawable]
		
		// process the xml into shapes
		svg.descendant.foreach(computeShape)
		def computeShape(node : Node) = {
			val fill = node.attribute("fill")
			val stroke = node.attribute("stroke")
			val opacity = node.attribute("opacity")
			val fillOpacity = node.attribute("fill-opacity")
			val strokeOpacity = node.attribute("stroke-opacity")
			val drawable : ColoredDrawableShape = node.label match {
				case "circle" => {
					var d : ColoredDrawableShape = null
					val cx = node.attribute("cx")
					val cy = node.attribute("cy")
					val r = node.attribute("r")
					
					if(cx.isDefined && cy.isDefined && r.isDefined){
						var rd = r.head.text.toDouble
						var s = new Ellipse2D.Double(cx.head.text.toDouble -rd, cy.head.text.toDouble -rd, 2*rd, 2*rd)
						d = new ColoredDrawableShape(DrawableShapeCompanion.Shape2DrawableShape(s))
					}
					d
				}
				case "path" => {
					var dr : ColoredDrawableShape = null
					val d = node.attribute("d")
					if(d.isDefined){
						val s = AWTPathProducer.createShape(new StringReader(d.get.head.text), 0)
						dr = new ColoredDrawableShape(DrawableShapeCompanion.Shape2DrawableShape(s))
					}
					dr
				}
				case _ => {/* Unknown */
					null
				}
			}
			if(drawable != null){
				if(fill.isDefined){
							var o = 255
							if(opacity.isDefined){
								o = (opacity.get.head.text.toDouble * 255).toInt
							}
							if(fillOpacity.isDefined){
								o = (fillOpacity.get.text.toDouble * 255).toInt
							}
							drawable.fill = if(fill.head.text == "none") TRANSPARENT else {
								var c = Color.decode(fill.head.text)
								new Color(c.getRed, c.getGreen, c.getBlue, o)
								}
						}else{
							drawable.fill = TRANSPARENT
						}
						if(stroke.isDefined){
							var o = 255
							if(opacity.isDefined){
								o = (opacity.get.text.toDouble * 255).toInt
							}
							if(strokeOpacity.isDefined){
								o = (strokeOpacity.get.text.toDouble * 255).toInt
							}
							drawable.color = if(stroke.head.text == "none") TRANSPARENT else {
								var c = Color.decode(stroke.head.text)
								new Color(c.getRed, c.getGreen, c.getBlue, o)
								}
						}else{
							drawable.color = TRANSPARENT
						}
						drawables += drawable
			}
		}
		
		if (drawables.length == 1 ) {
			drawables.head
		}else {
			val cs = new CompositeDrawableShape()
			cs.shapes ++= drawables
			cs
		}
	}
}