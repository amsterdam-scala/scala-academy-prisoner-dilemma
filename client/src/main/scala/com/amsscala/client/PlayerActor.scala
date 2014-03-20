package com.amsscala.client

import akka.actor._
import com.amsscala.common._

class PlayerActor(gameId: String, name: String, client: ActorRef) extends Actor with ActorLogging {
  import GameProtocol._

  private def randomAnswer = if (Math.random() >= 0.5) Talk else Silent

  override def receive = {
    case StartGame(_, _)     => sender ! PlayerReady(client)
    case StartRound(roundNr) => sender ! RoundAnswer(roundNr, randomAnswer)
    case r: RoundResult      => log.info(r.toString)
    case msg @ EndOfGame(_, _, _)  =>
      client ! msg // notify the client
      log.info("Done")
  }
}
