package com.amsscala
package common

import akka.actor._

object GameProtocol {
  
  sealed trait InGameMsg
  
  case class InitGame(id: String, name: String, client1: ActorRef, client2: ActorRef)
  case class GameResult(p1Score: Int, p2Score: Int) 

  case class StartGame(id: String, name: String) extends InGameMsg
  case class PlayerReady(client: ActorRef)
  case class StartRound(roundNr: Int) extends InGameMsg
  case class RoundAnswer(roundNr: Int, answer: Answer)
  case class RoundResult(roundNr: Int, otherAnswer: Answer, ownAnswer: Answer, otherScore: Int, ownScore: Int) extends InGameMsg
  case class EndOfGame(id: String, otherScore: Int, ownScore: Int)

  case object AskForGame

  sealed trait Answer
  case object Talk extends Answer
  case object Silent extends Answer
}