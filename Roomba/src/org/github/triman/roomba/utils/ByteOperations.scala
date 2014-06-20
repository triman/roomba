package org.github.triman.roomba.utils

import java.io.InputStream
import java.nio.ByteBuffer
import java.nio.ByteOrder

object ByteOperations {
	def short2ByteArray(s : Short) : Array[Byte] = {
		var bytes = new Array[Byte](2)
		ByteBuffer.wrap(bytes).order(ByteOrder.BIG_ENDIAN).asShortBuffer().put(s);
		bytes
	}
	def int2ByteArray(i : Int) : Array[Byte] = {
		var bytes = new Array[Byte](4)
		ByteBuffer.wrap(bytes).order(ByteOrder.BIG_ENDIAN).asIntBuffer().put(i);
		bytes
	}
	def byteArray2Int(a : Array[Byte]) = {
		val ar = Array.fill(Math.max(0,4 - a.length)){0.toByte} ++ a
		ar.foreach(b => println(b))
		ByteBuffer.wrap(ar).order(ByteOrder.BIG_ENDIAN).asIntBuffer().get()
	}
	def byteArray2Short(a : Array[Byte]) = {
		val ar = Array.fill(Math.max(0,2 - a.length)){0.toByte} ++ a
		ar.foreach(b => println(b))
		ByteBuffer.wrap(ar).order(ByteOrder.BIG_ENDIAN).asIntBuffer().get()
	}
}