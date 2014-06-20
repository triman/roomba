package org.github.triman.roomba.communication

import akka.actor.ActorRef
import akka.pattern.ask

trait CommunicatorContainer {
	val communicator : ActorRef
	def shutdown() : Unit
}