package org.github.triman.roomba.simulator.environment

import org.github.triman.graphics.Drawable
import org.github.triman.roomba.simulator.gui.utils.SVGUtils
import java.awt.geom.Point2D
import java.awt.geom.AffineTransform
import org.github.triman.graphics.TransformableDrawable
import java.awt.Graphics2D
import java.awt.Shape

class Room (filePath : String) extends Drawable{
	
	private val _startPositionDrawable : Drawable = SVGUtils.extractSVGShapeWithId(filePath, "StartPosition")
	// compute transform
	private val _startBounds = _startPositionDrawable.shape.getBounds2D()
	private val transform = new AffineTransform
	transform.setToIdentity()
	transform.translate(-_startBounds.getCenterX, -_startBounds.getCenterY)
	
	val floorDrawable : Drawable = new TransformableDrawable(SVGUtils.extractSVGShapeWithId(filePath, "Floor"), transform)
	val wallsDrawable : Drawable = new TransformableDrawable(SVGUtils.extractSVGShapeWithId(filePath, "Walls"), transform)
	val virtualWallsDrawable : Drawable = new TransformableDrawable(SVGUtils.extractSVGShapeWithId(filePath, "VirtualWalls"), transform)
	val objectsDrawable : Drawable = new TransformableDrawable(SVGUtils.extractSVGShapeWithId(filePath, "Objects"), transform)
	
	override def draw(g : Graphics2D, t : AffineTransform) = {
		floorDrawable.draw(g, t)
		wallsDrawable.draw(g, t)
		virtualWallsDrawable.draw(g, t)
		objectsDrawable.draw(g, t)
	}
	override def fill(g : Graphics2D, t : AffineTransform) = {
		floorDrawable.fill(g, t)
		wallsDrawable.fill(g, t)
		virtualWallsDrawable.fill(g, t)
		objectsDrawable.fill(g, t)
	}
	
	override def shape() = floorDrawable.shape
	
}