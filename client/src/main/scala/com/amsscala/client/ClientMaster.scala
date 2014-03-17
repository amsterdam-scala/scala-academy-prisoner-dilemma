package com.amsscala
package client

import common.LobbyProtocol.Register

import akka.actor.{ ActorSelection, Actor, ActorLogging }

class ClientMaster(server: ActorSelection) extends Actor with ActorLogging {

  override def preStart(): Unit = {
    server ! Register
  }

  def receive = {
    case msg => log.info("Received a msg: {}", msg)
  }
}
