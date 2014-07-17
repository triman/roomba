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

class SVGGroupProperties(val opacity: Int, val fillOpacity: Int, val strokeOpacity: Int)

object SVGUtils {
	val TRANSPARENT = new Color(255, 255, 255, 0)

	/**
	 * Process an XML node. Extract the group content if necessary
	 * @param tag The XML node to be processed
	 * @param groupProperties The properties that should be inherited from the parent node.
	 * @ToDo: provide an implementation with default properties
	 */
	def processTagGroup(tag: Node, groupProperties: SVGGroupProperties): Drawable = {
			def processChilds(tag: Node, groupProperties: SVGGroupProperties): Drawable = {
				val drawables = new MutableList[Drawable]
				// process childs
				drawables ++= tag.child.map(n => processTagGroup(n, groupProperties)).filter(e => e != null)
				if (drawables.length == 1) {
					drawables.head
				}
				else {
					val cs = new CompositeDrawableShape()
					cs.shapes ++= drawables
					cs
				}
			}
		// process current tag
		tag.label match {
			case "svg" => {
				processChilds(tag, groupProperties)
			}
			case "g" => {
				// get group properties
				val opacity = tag.attribute("opacity")
				val fillOpacity = tag.attribute("fill-opacity")
				val strokeOpacity = tag.attribute("stroke-opacity")

				var o = groupProperties.opacity
				if (opacity.isDefined) {
					o = (opacity.get.head.text.toDouble * 255).toInt
				}
				var fo = groupProperties.fillOpacity
				if (fillOpacity.isDefined) {
					fo = (fillOpacity.get.text.toDouble * 255).toInt
				}
				var so = groupProperties.strokeOpacity
				if (strokeOpacity.isDefined) {
					so = (strokeOpacity.get.text.toDouble * 255).toInt
				}

				processChilds(tag, new SVGGroupProperties(o, fo, so))
			}
			case n => {
				computeShape(tag, groupProperties)
			}
		}
	}

	/**
	 * Process an XML node into a single Drawable object.
	 * Visibility set to private since it should only be called by processTagGroup
	 * @param node The XML node object
	 * @param groupProperties Properties that should be inherited from the parent container.
	 * @ToDo provide an implementation with default properties
	 */
	private def computeShape(node: Node, groupProperties: SVGGroupProperties): Drawable = {
		val fill = node.attribute("fill")
		val stroke = node.attribute("stroke")
		val opacity = node.attribute("opacity")
		val fillOpacity = node.attribute("fill-opacity")
		val strokeOpacity = node.attribute("stroke-opacity")
		val drawable: ColoredDrawableShape = node.label match {
			case "circle" => {
				var d: ColoredDrawableShape = null
				val cx = node.attribute("cx")
				val cy = node.attribute("cy")
				val r = node.attribute("r")

				if (cx.isDefined && cy.isDefined && r.isDefined) {
					var rd = r.head.text.toDouble
					val s = new Ellipse2D.Double(cx.head.text.toDouble - rd, cy.head.text.toDouble - rd, 2 * rd, 2 * rd)
					d = new ColoredDrawableShape(DrawableShapeCompanion.Shape2DrawableShape(s))
				}
				d
			}
			case "path" => {
				var dr: ColoredDrawableShape = null
				val d = node.attribute("d")
				if (d.isDefined) {
					val s = AWTPathProducer.createShape(new StringReader(d.get.head.text), 0)
					dr = new ColoredDrawableShape(DrawableShapeCompanion.Shape2DrawableShape(s))
				}
				dr
			}
			case _ => { /* Unknown */
				null
			}
		}

		if (drawable != null) {
			if (fill.isDefined) {
				var o = if(groupProperties.fillOpacity != 255) groupProperties.fillOpacity else groupProperties.opacity
				if (opacity.isDefined) {
					o = (opacity.get.head.text.toDouble * 255).toInt
				}
				if (fillOpacity.isDefined) {
					o = (fillOpacity.get.text.toDouble * 255).toInt
				}
				drawable.fill = if (fill.head.text == "none") TRANSPARENT else {
					var c = Color.decode(fill.head.text)
					new Color(c.getRed, c.getGreen, c.getBlue, o)
				}
			}
			else {
				drawable.fill = TRANSPARENT
			}
			if (stroke.isDefined) {
				var o = if(groupProperties.strokeOpacity != 255) groupProperties.strokeOpacity else groupProperties.opacity
				if (opacity.isDefined) {
					o = (opacity.get.text.toDouble * 255).toInt
				}
				if (strokeOpacity.isDefined) {
					o = (strokeOpacity.get.text.toDouble * 255).toInt
				}
				drawable.color = if (stroke.head.text == "none") TRANSPARENT else {
					var c = Color.decode(stroke.head.text)
					new Color(c.getRed, c.getGreen, c.getBlue, o)
				}
			}
			else {
				drawable.color = TRANSPARENT
			}
		}
		drawable
	}

	/**
	 * Process a full SVG tree into a Drawable element.
	 * @param svg the SVG tree
	 */
	def svg2Drawable(svg: Elem): Drawable = {
		assert(svg.label == "svg", "The root node of an SVG document should be an <svg></svg> tag")
		val properties = new SVGGroupProperties(255, 255, 255)
		processTagGroup(svg, properties)
	}
}