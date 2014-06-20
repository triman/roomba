package org.github.triman.roomba.utils

/**
 * Class used as a notifier property. It can be listened to by other objects using callbacks or actors.
 * Source : http://stackoverflow.com/questions/2916057/idiomatic-property-changed-notification-in-scala
 * @param T Type of the value stored in the Notifier
 * @param U Type of the identifier of the param (used when using Actors)
 * @param t0 Initial value of the property
 */
abstract class Notifier[T,U](t0: T) {
  import java.util.concurrent.atomic.AtomicReference
  import scala.actors.OutputChannel
  type OCUT = OutputChannel[(U,AtomicReference[T])]
  val data = new AtomicReference[T](t0)
  def id: U
  protected var callbacks = Nil:List[T => Unit]
  protected var listeners = Nil:List[OCUT]
  def apply() = data.get
  def update(t: T) {
    val told = data.getAndSet(t)
    if (t != told) {
      callbacks.foreach(_(t))
      listeners.foreach(_ ! (id,data))
    }
  }
  def attend(f: T=>Unit) { callbacks ::= f }
  def attend(oc: OCUT) { listeners ::= oc }
  def ignore(f: T=>Unit) { callbacks = callbacks.filter(_ != f) }
  def ignore(oc: OCUT) { listeners = listeners.filter(_ != oc) }
}