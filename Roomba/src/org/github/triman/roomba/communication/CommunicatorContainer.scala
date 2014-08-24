package org.github.triman.roomba.communication

import akka.actor.ActorRef
import akka.pattern.ask
import org.github.triman.roomba.Shutdownable

trait CommunicatorContainer extends Shutdownable{
	val communicator : ActorRef
	def shutdown() : Unit
}