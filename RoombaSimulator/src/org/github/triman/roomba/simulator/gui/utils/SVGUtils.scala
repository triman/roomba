package org.github.triman.roomba.simulator.gui.utils

import java.awt.Color
import java.awt.geom.AffineTransform
import java.awt.geom.Ellipse2D
import java.awt.geom.Rectangle2D
import java.io.StringReader
import scala.collection.mutable.MutableList
import scala.io.Source
import scala.util.matching.Regex
import scala.xml.Elem
import scala.xml.Node
import scala.xml.NodeSeq.seqToNodeSeq
import scala.xml.XML
import org.apache.batik.parser.AWTPathProducer
import org.github.triman.graphics.ColoredDrawableShape
import org.github.triman.graphics.CompositeDrawableShape
import org.github.triman.graphics.Drawable
import org.github.triman.graphics.DrawableShape
import org.github.triman.graphics.DrawableShapeCompanion
import org.github.triman.graphics.TransformableDrawable
import org.github.triman.roomba.simulator.utils.NonValidatingSAXParserFactory
import java.awt.geom.Path2D

class SVGGroupProperties(val opacity: Int, val fillOpacity: Int, val strokeOpacity: Int, val transform: AffineTransform)

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
				val transform = tag.attribute("transform")

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
				var at = groupProperties.transform
				if (transform.isDefined) {
					val pattern = new Regex("""^matrix\((-?\d*.?\d*)\s+(-?\d*.?\d*)\s+(-?\d*.?\d*)\s+(-?\d*.?\d*)\s+(-?\d*.?\d*)\s+(-?\d*.?\d*)\)$""")
					val matches = pattern.findAllMatchIn(transform.get.text).toList

					if (matches.length == 1) {
						at = new AffineTransform(matches.head.group(1).toDouble,
							matches.head.group(2).toDouble,
							matches.head.group(3).toDouble,
							matches.head.group(4).toDouble,
							matches.head.group(5).toDouble,
							matches.head.group(6).toDouble)
					}
				}

				processChilds(tag, new SVGGroupProperties(o, fo, so, at))
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
		val transform = node.attribute("transform")
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
			case "rect" => {
				var d: ColoredDrawableShape = null
				val x = node.attribute("x")
				val y = node.attribute("y")
				val w = node.attribute("width")
				val h = node.attribute("height")

				if (x.isDefined && y.isDefined && w.isDefined && h.isDefined) {
					val s = new Rectangle2D.Double(x.head.text.toDouble, y.head.text.toDouble, w.head.text.toDouble, h.head.text.toDouble)
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
			case "polygon" => {
				var dr: ColoredDrawableShape = null
				val points = node.attribute("points")
				if(points.isDefined){
					val pattern = new Regex("""(-?\d*\.?\d*),(-?\d*\.?\d*)""")
					val pts = pattern.findAllMatchIn(points.get.head.text).toList.map(m => (m.group(1).toDouble, m.group(2).toDouble))
					val path = new Path2D.Double()
					if(pts.length == 0){
						return dr
					}
					// move head
					path.moveTo(pts.head._1, pts.head._2)
					pts.tail.foreach(p => path.lineTo(p._1, p._2))
					path.closePath
					
					dr = new ColoredDrawableShape(new DrawableShape(path))
				}
				dr
			}
			case _ => { /* Unknown */
				null
			}
		}

		if (drawable != null) {
			if (fill.isDefined) {
				var o = if (groupProperties.fillOpacity != 255) groupProperties.fillOpacity else groupProperties.opacity
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
				var o = if (groupProperties.strokeOpacity != 255) groupProperties.strokeOpacity else groupProperties.opacity
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
			var at = groupProperties.transform
			if (transform.isDefined) {
				val pattern = new Regex("""^matrix\((-?\d*.?\d*)\s+(-?\d*.?\d*)\s+(-?\d*.?\d*)\s+(-?\d*.?\d*)\s+(-?\d*.?\d*)\s+(-?\d*.?\d*)\)$""")
				val matches = pattern.findAllMatchIn(transform.get.text).toList
				if (matches.length == 1) {
					at = new AffineTransform(matches.head.group(1).toDouble,
						matches.head.group(2).toDouble,
						matches.head.group(3).toDouble,
						matches.head.group(4).toDouble,
						matches.head.group(5).toDouble,
						matches.head.group(6).toDouble)
				}
			}
			if (at != null) {
				return new TransformableDrawable(drawable, at)
			}
		}
		drawable
	}

	/**
	 * Process a full SVG tree into a Drawable element.
	 * @param svg the SVG tree
	 */
	def svg2Drawable(svg: Node): Drawable = {
		val properties = new SVGGroupProperties(255, 255, 255, null)
		processTagGroup(svg, properties)
	}

	/**
	 * Extracts a shape from the given SVG file for the given ID.
	 * If the ID is not unique, then the first occurence is retrieved.
	 * @param filePath Path of the SVG file
	 * @param id ID of the shape to retrieve
	 */
	def extractSVGShapeWithId(filePath: String, id: String): Drawable = {
		val xml = readXML(filePath)
		extractSVGShapeWithId(xml, id)
	}

	/**
	 * Extracts a shape from the given SVG file for the given ID.
	 * If the ID is not unique, then the first occurence is retrieved.
	 * @param url URL of the SVG file
	 * @param id ID of the shape to retrieve
	 */
	def extractSVGShapeWithId(url: java.net.URL, id: String): Drawable = {
		val xml = XML.withSAXParser(NonValidatingSAXParserFactory.getInstance)
			.loadString(Source.fromURL(url).getLines().reduce(_ + _))
		extractSVGShapeWithId(xml, id)
	}
	/**
	 * Extracts a shape from the given SVG file for the given ID.
	 * If the ID is not unique, then the first occurence is retrieved.
	 * @param xml Content of the SVG file
	 * @param id ID of the shape to retrieve
	 */
	def extractSVGShapeWithId(xml: Elem, id: String): Drawable = {
		// extract element
		val elems = xml \\ "_" filter (n => n.attribute("id").exists(v => v.head.text == id))
		if (elems.length == 0) {
			return null
		}
		svg2Drawable(elems.head)
	}

	/**
	 * Reads an XML file
	 * @param filePath Path to the file which should be read.
	 */
	def readXML(filePath: String) = {
		XML.withSAXParser(NonValidatingSAXParserFactory.getInstance)
			.loadString(Source.fromFile(
				filePath).getLines().reduce(_ + _))
	}
}