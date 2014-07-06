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
			node.label match {
				case "circle" => {
					val cx = node.attribute("cx")
					val cy = node.attribute("cy")
					val r = node.attribute("r")
					
					if(cx.isDefined && cy.isDefined && r.isDefined){
						var rd = r.head.text.toDouble
						var s = new Ellipse2D.Double(cx.head.text.toDouble -rd, cy.head.text.toDouble -rd, 2*rd, 2*rd)
						val d = new ColoredDrawableShape(DrawableShapeCompanion.Shape2DrawableShape(s))
						
						if(fill.isDefined){
							d.fill = if(fill.head.text == "none") TRANSPARENT else Color.decode(fill.head.text)
						}else{
							d.fill = TRANSPARENT
						}
						if(stroke.isDefined){
							d.color = if(stroke.head.text == "none") TRANSPARENT else Color.decode(stroke.head.text)
						}else{
							d.color = TRANSPARENT
						}
						
						drawables += d
					}
					
				}
				case "path" => {
					val d = node.attribute("d")
					if(d.isDefined){
						val s = AWTPathProducer.createShape(new StringReader(d.get.head.text), 0)
						val dr = new ColoredDrawableShape(DrawableShapeCompanion.Shape2DrawableShape(s))
						
						if(fill.isDefined){
							dr.fill = if(fill.head.text == "none") TRANSPARENT else Color.decode(fill.head.text)
						}else{
							dr.fill = TRANSPARENT
						}
						if(stroke.isDefined){
							dr.color = if(stroke.head.text == "none") TRANSPARENT else Color.decode(stroke.head.text)
						}else{
							dr.color = TRANSPARENT
						}
						
						drawables += dr
					}
					
				}
				case _ => {/* Unknown */}
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