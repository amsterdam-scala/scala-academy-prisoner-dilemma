package com.amsscala
package common

import akka.actor._

object GameProtocol {
  case class InitGame(id: String, name: String, p1: ActorRef, p2: ActorRef)
  case class GameResult(p1Score: Int, p2Score: Int)

  case class StartGame(id: String, name: String)
  case object PlayerReady
  case class StartRound(roundNr: Int)
  case class RoundAnswer(roundNr: Int, answer: Answer)
  case class RoundResult(roundNr: Int, otherAnswer: Answer, ownAnswer: Answer, otherScore: Int, ownScore: Int)
  case class EndOfGame(otherScore: Int, ownScore: Int)

  sealed abstract trait Answer
  case object Talk extends Answer
  case object Silent extends Answer
}