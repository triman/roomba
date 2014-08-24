package com.github.triman.roomba.ai.environment

import scala.collection.mutable.HashMap
import java.awt.Point
import org.github.triman.utils.NullReplace._
import org.github.triman.utils.Notifier

class EnvironmentMap extends Notifier[EnvironmentMap, Symbol](null) {
	data.set(this)
	def id = 'EnvironmentMap
	
	def apply(p : Point) = environment(p) ?? UnknownEnvironment
	def update(p : Point, e : EnvironmentType) : Unit = {
		environment(p) = e ?? UnknownEnvironment
		// set size
		_minX = Math.min(_minX, p.x)
		_maxX = Math.max(_maxX, p.x)
		_minY = Math.min(_minY, p.y)
		_maxY = Math.max(_maxY, p.y)
	}
	
	// maped region
	private var _minX = 0
	def minX = _minX
	private var _maxX = 0
	def maxX = _maxX
	private var _minY = 0
	def minY = _minY
	private var _maxY = 0
	def maxY = _maxY
	
	// defines the environment
	private val environment = new HashMap[Point, EnvironmentType]();
	
	

}