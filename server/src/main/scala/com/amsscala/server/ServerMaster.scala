package com.amsscala.server

import akka.actor.{ Actor, ActorLogging }

class ServerMaster extends Actor with ActorLogging {

  def receive = {
    case _ ⇒
  }
}

case class Client(ref: ActorRef, state: ClientState)
