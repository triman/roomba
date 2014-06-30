package org.github.triman.roomba.tests

import org.scalatest._
import org.github.triman.roomba.AbstractRoomba
import org.github.triman.roomba.simulator.communication.ByteStreamCommunicatorContainer
import java.nio.ByteBuffer
import java.io.InputStream

class RoombaSpec extends FlatSpec with Matchers with BeforeAndAfter{
	
	class TestRoomba extends AbstractRoomba with ByteStreamCommunicatorContainer
	
	var roomba : TestRoomba = null
	var in : InputStream = null
	before {
		roomba = new TestRoomba
		in = roomba.asInstanceOf[ByteStreamCommunicatorContainer].in
		}
	after{
	}
	
	"A start command" should "send 128 as opcode with no data bytes" in {
		roomba.start
		in.read.toByte should be(128.toByte)
		in.available() should be (0)
	}
	
	"A baud command" should "send 129 as opcode with one data bytes" in {
		roomba.start
		in.read
		roomba.baud(300)
		in.read.toByte should be(129.toByte)
		in.available should be (1)
	}
	it should "throw an assertion error of the roomba hasn't been started before" in {
		a[AssertionError] should be thrownBy {
			roomba.baud(300)
		}
	}
	it should "throw an illegal argument exception if the baud rate is not in the accepted values" in {
		roomba.start
		a[IllegalArgumentException] should be thrownBy {
			roomba.baud(0)
		}
	}
	
	"A control command" should "send 130 as opcode with no data bytes" in {
		roomba.start
		in.read
		roomba.baud(300)
		in.read(new Array[Byte](2))
		roomba.control
		in.read.toByte should be (130.toByte)
	}
	it should "throw an assertion error of the roomba hasn't been started before" in {
		a[AssertionError] should be thrownBy {
			roomba.control
		}
	}
	
	"A safe command" should "send 131 as opcode with no data bytes" in {
		roomba.start
		in.read
		roomba.baud(300)
		in.read(new Array[Byte](2))
		roomba.control
		in.read()
		roomba.full
		in.read()
		roomba.safe()
		in.read.toByte should be (131.toByte)
	}
	it should "throw an assertion error of the roomba hasn't been started before" in {
		a[AssertionError] should be thrownBy {
			roomba.control
		}
	}
	it should "throw an assertion error of the roomba isn't in full mode before" in {
		a[AssertionError] should be thrownBy {
			roomba.start
			in.read
			roomba.baud(300)
			in.read(new Array[Byte](2))
			roomba.control
			in.read
			roomba.safe
		}
	}
	
	"A full command" should "send 132 as opcode with no data bytes" in {
		roomba.start
		in.read
		roomba.baud(300)
		in.read(new Array[Byte](2))
		roomba.control
		in.read()
		roomba.full
		in.read.toByte should be (132.toByte)
	}
	it should "throw an assertion error of the roomba hasn't been started before" in {
		a[AssertionError] should be thrownBy {
			roomba.control
		}
	}
	it should "throw an assertion error of the roomba isn't in safe mode before" in {
		a[AssertionError] should be thrownBy {
			roomba.start
			in.read
			roomba.baud(300)
			in.read(new Array[Byte](2))
			roomba.full
		}
		a[AssertionError] should be thrownBy {
			roomba.start
			in.read
			roomba.baud(300)
			in.read(new Array[Byte](2))
			roomba.control
			in.read
			roomba.full
			in.read
			roomba.full
		}
	}
	
	"A power command" should "send 133 as opcode with no data bytes" in {
		roomba.start
		in.read
		roomba.power
		in.read.toByte should be (133.toByte)
	}
	it should "throw an assertion error of the roomba hasn't been started before" in {
		a[AssertionError] should be thrownBy {
			roomba.power
		}
	}
	
	"A spot command" should "send 134 as opcode with no data bytes" in {
		roomba.start
		in.read
		roomba.baud(300)
		in.read(new Array[Byte](2))
		roomba.control
		in.read()
		roomba.spot
		in.read.toByte should be (134.toByte)
	}
	it should "throw an assertion error of the roomba hasn't been started before" in {
		a[AssertionError] should be thrownBy {
			roomba.spot
		}
	}
	it should "throw an assertion error of the roomba isn't in safe or full mode before" in {
		a[AssertionError] should be thrownBy {
			roomba.start
			in.read
			roomba.baud(300)
			in.read(new Array[Byte](2))
			roomba.spot
		}
	}
	
	"A clean command" should "send 135 as opcode with no data bytes" in {
		roomba.start
		in.read
		roomba.baud(300)
		in.read(new Array[Byte](2))
		roomba.control
		in.read()
		roomba.clean
		in.read.toByte should be (135.toByte)
	}
	it should "throw an assertion error of the roomba hasn't been started before" in {
		a[AssertionError] should be thrownBy {
			roomba.spot
		}
	}
	it should "throw an assertion error of the roomba isn't in safe or full mode before" in {
		a[AssertionError] should be thrownBy {
			roomba.start
			in.read
			roomba.baud(300)
			in.read(new Array[Byte](2))
			roomba.clean
		}
	}
	
	"A max command" should "send 136 as opcode with no data bytes" in {
		roomba.start
		in.read
		roomba.baud(300)
		in.read(new Array[Byte](2))
		roomba.control
		in.read()
		roomba.max
		in.read.toByte should be (136.toByte)
	}
	it should "throw an assertion error of the roomba hasn't been started before" in {
		a[AssertionError] should be thrownBy {
			roomba.max
		}
	}
	it should "throw an assertion error of the roomba isn't in safe or full mode before" in {
		a[AssertionError] should be thrownBy {
			roomba.start
			in.read
			roomba.baud(300)
			in.read(new Array[Byte](2))
			roomba.max
		}
	}
	
	"A drive command" should "send 137 as opcode with 4 data bytes" in {
		roomba.start
		in.read
		roomba.baud(300)
		in.read(new Array[Byte](2))
		roomba.control
		in.read()
		roomba.drive(100,100)
		in.read.toByte should be (137.toByte)
		in.available should be (4)
	}
	it should "throw an assertion error of the roomba hasn't been started before" in {
		a[AssertionError] should be thrownBy {
			roomba.drive(100,100)
		}
	}
	it should "throw an assertion error of the roomba isn't in safe or full mode before" in {
		a[AssertionError] should be thrownBy {
			roomba.start
			in.read
			roomba.baud(300)
			in.read(new Array[Byte](2))
			roomba.drive(100,100)
		}
	}
	it should "throw an IllegalArgumentException if the input values are not in the range [-500,500] and [-2000,2000]" in {
		a[IllegalArgumentException] should be thrownBy {
			roomba.start
			in.read
			roomba.baud(300)
			in.read(new Array[Byte](2))
			roomba.control
			in.read()
			roomba.drive(-1000,100)
		}
		a[IllegalArgumentException] should be thrownBy {
			roomba.start
			in.read
			roomba.baud(300)
			in.read(new Array[Byte](2))
			roomba.control
			in.read()
			roomba.drive(1000,100)
		}
		a[IllegalArgumentException] should be thrownBy {
			roomba.start
			in.read
			roomba.baud(300)
			in.read(new Array[Byte](2))
			roomba.control
			in.read()
			roomba.drive(100,-3000)
		}
		a[IllegalArgumentException] should be thrownBy {
			roomba.start
			in.read
			roomba.baud(300)
			in.read(new Array[Byte](2))
			roomba.control
			in.read()
			roomba.drive(100,3000)
		}
	}
	it should "send the sequence [137][255][56][1][244] when asked to drive -200mm/s with a radius of +500mm" in {
		roomba.start
		in.read
		roomba.baud(300)
		in.read(new Array[Byte](2))
		roomba.control
		in.read()
		roomba.drive(-200,500)
		in.read.toByte should be (137.toByte)
		in.available should be (4)
		in.read.toByte should be (255.toByte)
		in.read.toByte should be (56.toByte)
		in.read.toByte should be (1.toByte)
		in.read.toByte should be (244.toByte)
	}
	
	"A motors command" should "send 138 as opcode with 1 data bytes" in {
		roomba.start
		in.read
		roomba.baud(300)
		in.read(new Array[Byte](2))
		roomba.control
		in.read()
		roomba.motors(true, true, true)
		in.read.toByte should be (138.toByte)
		in.available should be (1)
	}
	it should "throw an assertion error of the roomba hasn't been started before" in {
		a[AssertionError] should be thrownBy {
			roomba.motors(true, true, true)
		}
	}
	it should "throw an assertion error of the roomba isn't in safe or full mode before" in {
		a[AssertionError] should be thrownBy {
			roomba.start
			in.read
			roomba.baud(300)
			in.read(new Array[Byte](2))
			roomba.motors(true, true, true)
		}
	}
	it should "send a 0 byte to stop all the motors" in {
		roomba.start
		in.read
		roomba.baud(300)
		in.read(new Array[Byte](2))
		roomba.control
		in.read()
		roomba.motors(false, false, false)
		in.read.toByte should be (138.toByte)
		in.read.toByte should be (0.toByte)
	}
	it should "use data bit 0 for side brush" in {
		roomba.start
		in.read
		roomba.baud(300)
		in.read(new Array[Byte](2))
		roomba.control
		in.read()
		roomba.motors(true, false, false)
		in.read.toByte should be (138.toByte)
		in.read.toByte should be (1.toByte)
	}
	it should "use data bit 1 for vacuum" in {
		roomba.start
		in.read
		roomba.baud(300)
		in.read(new Array[Byte](2))
		roomba.control
		in.read()
		roomba.motors(false, true, false)
		in.read.toByte should be (138.toByte)
		in.read.toByte should be (2.toByte)
	}
	it should "use data bit 2 for main brush" in {
		roomba.start
		in.read
		roomba.baud(300)
		in.read(new Array[Byte](2))
		roomba.control
		in.read()
		roomba.motors(false, false, true)
		in.read.toByte should be (138.toByte)
		in.read.toByte should be (4.toByte)
	}
	
	"A led command" should "send 139 as opcode with 3 data bytes" in {
		roomba.start
		in.read
		roomba.baud(300)
		in.read(new Array[Byte](2))
		roomba.control
		in.read()
		roomba.leds(0, false, false, false, false, 0, 0)
		in.read.toByte should be (139.toByte)
		in.available should be (3)
	}
	it should "throw an assertion error of the roomba hasn't been started before" in {
		a[AssertionError] should be thrownBy {
			roomba.leds(0, false, false, false, false, 0, 0)
		}
	}
	it should "throw an assertion error of the roomba isn't in safe or full mode before" in {
		a[AssertionError] should be thrownBy {
			roomba.start
			in.read
			roomba.baud(300)
			in.read(new Array[Byte](2))
			roomba.leds(0, false, false, false, false, 0, 0)
		}
	}
	it should "send byte sequence [139][25][0][128] for dirt detect, spot, a red status, power green with 50% intensity" in {
		roomba.start
		in.read
		roomba.baud(300)
		in.read(new Array[Byte](2))
		roomba.control
		in.read()
		roomba.leds(1, true, false, false, true, 0, 128.toByte)
		in.read.toByte should be (139.toByte)
		in.read.toByte should be (25.toByte)
		in.read.toByte should be (0.toByte)
		in.read.toByte should be (128.toByte)
	}
	
	var song = List((31.toByte, 64.toByte),(32.toByte, 16.toByte),(33.toByte, 8.toByte))
	"A song command" should "send 140 as opcode with 2N+2 data bytes (for n notes)" in {
		roomba.start
		in.read
		roomba.baud(300)
		in.read(new Array[Byte](2))
		roomba.song(1,song)
		in.read.toByte should be (140.toByte)
		in.available should be (2 + 2*song.length)
	}
	it should "throw an assertion error of the roomba hasn't been started before" in {
		a[AssertionError] should be thrownBy {
			roomba.song(1,song)
		}
	}
	it should "send the bytes [140][1][3][31][64][32][16][33][8] for the sample song" in {
		roomba.start
		in.read
		roomba.baud(300)
		in.read(new Array[Byte](2))
		roomba.song(1,song)
		in.read.toByte should be (140.toByte)
		in.read.toByte should be (1.toByte)
		in.read.toByte should be (3.toByte)
		in.read.toByte should be (31.toByte)
		in.read.toByte should be (64.toByte)
		in.read.toByte should be (32.toByte)
		in.read.toByte should be (16.toByte)
		in.read.toByte should be (33.toByte)
		in.read.toByte should be (8.toByte)
	}
	it should "throw an invalid argument exception if the song number is negative or >15" in {
		roomba.start
		in.read
		roomba.baud(300)
		in.read(new Array[Byte](2))
		a[IllegalArgumentException] should be thrownBy {
			roomba.song(-1,song)
		}
		a[IllegalArgumentException] should be thrownBy {
			roomba.song(16,song)
		}
	}
	it should "throw an invalid argument exception if the song length is >16 " in {
		roomba.start
		in.read
		roomba.baud(300)
		in.read(new Array[Byte](2))
		a[IllegalArgumentException] should be thrownBy {
			roomba.song(-1,List.fill(17)((31.toByte, 1)))
		}
	}
	it should "throw an invalid argument exception if the notes are <31 or >127" in {
		roomba.start
		in.read
		roomba.baud(300)
		in.read(new Array[Byte](2))
		a[IllegalArgumentException] should be thrownBy {
			roomba.song(-1,List((30.toByte, 1)))
		}
		a[IllegalArgumentException] should be thrownBy {
			roomba.song(-1,List((128.toByte, 1)))
		}
	}
	
	"A play command" should "send 141 as opcode with 1 data byte" in {
		roomba.start
		in.read
		roomba.baud(300)
		in.read(new Array[Byte](2))
		roomba.play(1)
		in.read.toByte should be (141.toByte)
		in.available should be (1)
	}
	it should "throw an assertion error of the roomba hasn't been started before" in {
		a[AssertionError] should be thrownBy {
			roomba.play(1)
		}
	}
	it should "send the bytes [141][1] when asked to play the 1st song" in {
		roomba.start
		in.read
		roomba.baud(300)
		in.read(new Array[Byte](2))
		roomba.play(1)
		in.read.toByte should be (141.toByte)
		in.read.toByte should be (1.toByte)
	}
	it should "throw an invalid argument exception if the song number is negative or >15" in {
		roomba.start
		in.read
		roomba.baud(300)
		in.read(new Array[Byte](2))
		a[IllegalArgumentException] should be thrownBy {
			roomba.play(-1)
		}
		a[IllegalArgumentException] should be thrownBy {
			roomba.play(16)
		}
	}
	
	"A force seeking dock command" should "send 143 as opcode with 0 data bytes" in {
		roomba.start
		in.read
		roomba.baud(300)
		in.read(new Array[Byte](2))
		roomba.control
		in.read()
		roomba.forceSeekingDock
		in.read.toByte should be (143.toByte)
		in.available should be (0)
	}
	it should "throw an assertion error of the roomba hasn't been started before" in {
		a[AssertionError] should be thrownBy {
			roomba.forceSeekingDock
		}
	}
	
}
