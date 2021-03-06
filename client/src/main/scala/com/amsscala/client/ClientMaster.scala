package com.amsscala
package client

import akka.actor._
import common.LobbyProtocol.Register
import common.GameProtocol._

class ClientMaster(server: ActorSelection) extends Actor with ActorLogging with Stash {

  override def preStart(): Unit = {
    server ! Register
  }

  def playing(player: ActorRef): Receive = {
    case msg: InGameMsg => player.forward(msg)

    case cmd @ EndOfGame(_, _, _) =>
      log.debug("Game over. Waiting for another game...")
      context.become(waiting)
      unstashAll()
      player ! PoisonPill

    case _ => stash()
  }

  def waiting: Receive = {
    case cmd @ StartGame(id, name, opponent) =>
      val player = context.actorOf(Props(new PlayerActor(id, name, self)))
      context.become(playing(player))
      player.forward(cmd)
  }

  def receive = waiting
}
