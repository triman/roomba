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
import org.github.triman.graphics.Drawable
import scala.swing.BorderPanel
import org.github.triman.window.StatusBar
import scala.swing.Label
import scala.swing.Alignment
import scala.swing.MenuBar
import scala.swing.Menu
import scala.swing.MenuItem
import scala.swing.Action
import scala.swing.FileChooser
import org.github.triman.roomba.simulator.RoombaSimulatorApplication
/**
 * http://www.jasperpotts.com/blog/2007/07/svg-shape-2-java2d-code/
 */
object MainWindow extends Frame {
	
	private var _room: Drawable = null

	def room = _room
	def room_=(d: Drawable): Unit = {
		// the room is ALWAYS the head of the shapes list -> it's drawed on the bottom layer
		if (_room != null) {
			canvas.shapes.update(0, d)
		}
		else {
			d +=: canvas.shapes
		}
		_room = d
		canvas.repaint()
	}

	private val container = new BorderPanel
	contents = container
	title = "Roomba simulator"
	peer.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE)
	val canvas = new Canvas

	size = new Dimension(1000, 500)
	canvas.background = Color.GRAY
	container.layout(canvas) = BorderPanel.Position.Center

	val statusBar = new StatusBar
	container.layout(statusBar) = BorderPanel.Position.South

	val zoomLevelLabel = new Label("zoom: " + (canvas.zoom() * 100).round.toString() + "%")
	zoomLevelLabel.preferredSize = new Dimension(70, 15)
	zoomLevelLabel.horizontalAlignment = Alignment.Left
	canvas.zoom.attend(z => { zoomLevelLabel.text = "zoom: " + (z * 100).round.toString() + "%" })
	statusBar.add(zoomLevelLabel)
	
	// roomba status
	val roombaStatus = RoombaSimGuiElements.roombaStatus
	canvas.shapes += roombaStatus
	
	
	// menu
	menuBar = new MenuBar
    {
       contents += new Menu("File")
       {
			contents += new MenuItem(new Action("Load room...")
         {
          def apply
          {
          	val chooser = new FileChooser
          	val r = chooser.showOpenDialog(container)
          	r match{
          		case FileChooser.Result.Approve => {
          				RoombaSimulatorApplication.loadRoom(chooser.selectedFile.getAbsolutePath())
          			}
          		case _ => {}
          	}
          }
         })
       }
    }

}