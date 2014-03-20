package com.amsscala
package client

import akka.actor._
import common.LobbyProtocol.Register
import common.GameProtocol._

class ClientMaster(server: ActorSelection) extends Actor with ActorLogging with Stash {
  import scala.concurrent.duration._
  import context.dispatcher

  val delayBetweenGames: FiniteDuration = 5.seconds

  override def preStart(): Unit = {
    server ! Register
  }

  private[this] def playing(player: ActorRef): Receive = {
    case msg: InGameMsg           => player.forward(msg)

    case cmd @ EndOfGame(_, _, _) =>
      log.info("Game over. Waiting for another game...")
      scheduleAskForGame()
      context.become(waiting)
      unstashAll()
      player ! PoisonPill

    case AskForGame               => // Already playing. Do nothing.

    case _                        => stash()
  }

  private[this] def waiting: Receive = {
    case cmd @ StartGame(id, name) =>
      val player = context.actorOf(Props(new PlayerActor(id, name, self)))
      context.become(playing(player))
      player.forward(cmd)

    case cmd @ AskForGame          =>
      log.info("Let's start another game...")
      server ! cmd
  }

  private[this] def scheduleAskForGame(): Unit = {
    context.system.scheduler.scheduleOnce(delayBetweenGames, self, AskForGame)
  }

  def receive = waiting
}
