package org.github.triman.roomba.simulator.gui

import org.github.triman.graphics.Drawable
import java.awt.geom.AffineTransform
import java.awt.Graphics2D
import java.awt.Shape
import org.github.triman.graphics.CompositeDrawableShape
import org.github.triman.roomba.simulator.gui.utils.SVGUtils
import org.github.triman.graphics.AnchoredDrawable
import org.github.triman.graphics.TransformableDrawable
import org.github.triman.graphics.Top
import org.github.triman.graphics.Right
import java.awt.Color
import org.github.triman.graphics.ColoredDrawableShape

object RoombaStatusDrawable extends Drawable{
	// initialize 
	val regions = List('Background, 'CasterWheel, 'WheelLeft, 'WheelRight,
			'CliffLeft, 'CliffFrontLeft, 'CliffFrontRight, 'CliffRight, 'VirtualWall,
			'MainBrush, 'Vacuum, 'SideBrush, 'Wall, 'BumperLeft, 'BumperRight,
			'Roomba)
			
			
	val drawables = new scala.collection.mutable.HashMap[Symbol, Drawable]
	// retrieve objects
	private val aggregartedDrawable = new CompositeDrawableShape()
	regions.foreach(r => {
		val d = SVGUtils.extractSVGShapeWithId(getClass().getResource("roomba_status.svg"), r.name)
		drawables put (r, d)
		if(d != null){
			aggregartedDrawable.shapes += d
		}
	})
	
	private val roombaPosition = new AffineTransform
		roombaPosition.setToIdentity
		roombaPosition.scale(0.4, 0.4)
	val drawable = new AnchoredDrawable(Top, Right, new TransformableDrawable(aggregartedDrawable, roombaPosition))
	regions.foreach(r => this.setOn(r))
	List('MainBrush, 'Vacuum, 'SideBrush, 'Wall).foreach(r => this.setOff(r))
	
	override def draw(g : Graphics2D, t : AffineTransform){
		val t2 = new AffineTransform()
		t2.setToIdentity()
		drawable.draw(g, t2)
	}
	override def fill(g : Graphics2D, t : AffineTransform){
		val t2 = new AffineTransform()
		t2.setToIdentity()
		drawable.fill(g, t2)
	}
	
	override def shape() : Shape = {
		throw new UnsupportedOperationException("This operation is not supported on the RoombaStatus object!")
	}
		
	def setOn(s : Symbol){
		drawables(s) match{
				case d : ColoredDrawableShape => d.fill = new Color(0, 104, 55, 190)
				case d => 
		}
	}
	def setOff(s : Symbol){
		drawables(s) match{
				case d : ColoredDrawableShape => d.fill = new Color(193, 39, 45, 190)
				case d => 
		}
	}
}