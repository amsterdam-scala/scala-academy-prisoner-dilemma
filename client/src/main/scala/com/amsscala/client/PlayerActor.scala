package com.amsscala.client

import akka.actor._
import scala.concurrent.duration._
import com.amsscala.common._

class PlayerActor extends Actor with ActorLogging {
  import GameProtocol._

  private def randomAnswer = if (Math.random() >= 0.5) Talk else Silent

  override def receive = {
    case StartGame(_, _)     => sender ! PlayerReady
    case StartRound(roundNr) => sender ! RoundAnswer(roundNr, randomAnswer)
    case r: RoundResult      => log.info(r.toString)
    case EndOfGame(_, _, _)  => log.info("Done")
  }
}
