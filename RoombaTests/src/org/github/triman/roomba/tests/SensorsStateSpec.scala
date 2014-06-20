package org.github.triman.roomba.tests

import org.scalatest._
import org.github.triman.roomba.simulator.MutableSensorsState
import org.github.triman.roomba.AllSensors
import org.github.triman.roomba.Detectors
import org.github.triman.roomba.Controls
import org.github.triman.roomba.Health

class SensorsStateSpec extends FlatSpec with Matchers{
	"A SensorState" should "be serialized to a total of 26 bytes" in {
		val s = new MutableSensorsState
		s.getByteArray(AllSensors).length should be (26)
	}
	it should "be 10 bytes long for the detectors" in {
		val s = new MutableSensorsState
		s.getByteArray(Detectors).length should be (10)
	}
	it should "be 6 bytes long for the controls" in {
		val s = new MutableSensorsState
		s.getByteArray(Controls).length should be (6)
	}
	it should "be 10 bytes long for the health" in {
		val s = new MutableSensorsState
		s.getByteArray(Health).length should be (10)
	}
}