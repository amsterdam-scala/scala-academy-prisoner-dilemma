package com.amsscala
package common

object GameProtocol {

  case class StartGame(id: String, name: String)
  case class RoundAnswer(roundNr: Int, answer: Answer)
  case class RoundResult(otherAnswer: Answer, score: Int)
  case class EndOfGame(score: Int)

  sealed abstract class Answer
  case object Talk extends Answer
  case object Silent extends Answer
}