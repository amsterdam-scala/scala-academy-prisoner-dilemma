package com.amsscala
package server

import akka.actor._
import scala.concurrent.duration._

import common._
import common.GameProtocol._

class GameActor extends Actor with ActorLogging {
  import context._

  import GameActor._

  private var roundDelay: FiniteDuration = _
  private var initNrOfRounds: Option[Int] = _

  private var gameId: String = _
  private var gameName: String = _
  private var p1: ActorRef = _
  private var p1Ready = false
  private var p2: ActorRef = _
  private var p2Ready = false

  private var currentRound: Int = _
  private var totalRounds: Int = _
  private var p1Score: Int = _
  private var p2Score: Int = _

  private var client1: ActorRef = _
  private var client2: ActorRef = _

  private var currentP1Answer: Option[RoundAnswer] = None
  private var currentP2Answer: Option[RoundAnswer] = None

  private def gameLog(msg: String) = s"$gameName/$gameId:$msg"

  override def receive = {
    case InitGameConfigurable(init, nrOfRounds, delay) => {
      roundDelay = delay
      initNrOfRounds = nrOfRounds
      gameId = init.id
      gameName = init.name
      client1 = init.client1
      client2 = init.client2
      log.info(gameLog("Initialized game"))
      init.client1 ! StartGame(gameId, gameName)
      init.client2 ! StartGame(gameId, gameName)
      log.info(gameLog("Started game"))
      become(prepare)
    }
    case init: InitGame => {
      self ! InitGameConfigurable(init)
    }
  }

  private def prepare: Receive = {
    case PlayerReady(client) => {
      log.info(gameLog("Player " + sender + " send ready"))
      if (client == client1) {
        p1 = sender
        p1Ready = true
      } else if (client == client2) {
        p2 = sender
        p2Ready = true
      } else {
        log.warning(gameLog("Received ready message from player that is not in game: " + sender))
      }
      if (p1Ready && p2Ready) {
        currentRound = 0
        totalRounds = initNrOfRounds.getOrElse((Math.random() * (MAX_ROUNDS - MIN_ROUNDS)).toInt + MIN_ROUNDS)
        p1Score = 0
        p2Score = 0

        log.info(gameLog("Players ready, starting game with " + totalRounds + " rounds"))
        become(game)
        system.scheduler.scheduleOnce(roundDelay, self, RoundTrigger)
      }
    }
  }

  private def game: Receive = {
    case RoundTrigger => {
      currentRound += 1
      currentP1Answer = None
      currentP2Answer = None

      log.info(gameLog("Starting round " + currentRound))

      p1 ! StartRound(currentRound)
      p2 ! StartRound(currentRound)
    }
    case answer: RoundAnswer => {
      log.debug(gameLog("Received answer from " + sender + ": " + answer))
      (sender, answer) match {
        case (p1, answer) if currentP1Answer.isEmpty =>
          currentP1Answer = Some(answer)
        case (p2, answer) if currentP2Answer.isEmpty =>
          currentP2Answer = Some(answer)
        case (p, _) =>
          log.warning("Result already received for round " + currentRound + " from player " + p)
      }

      if (currentP1Answer.isDefined && currentP2Answer.isDefined) {
        val (p1RoundScore, p2RoundScore) = PrisonersDilemma.engine(currentP1Answer.get.answer, currentP2Answer.get.answer)
        p1Score += p1RoundScore
        p2Score += p2RoundScore

        p1 ! RoundResult(currentRound, currentP2Answer.get.answer, currentP1Answer.get.answer, p2Score, p1Score)
        p2 ! RoundResult(currentRound, currentP1Answer.get.answer, currentP2Answer.get.answer, p1Score, p2Score)

        if (currentRound < totalRounds) {
          system.scheduler.scheduleOnce(roundDelay, self, RoundTrigger)
        } else {
          log.info(gameLog("Ended, final scores " + p1Score + " : " + p2Score))
          p1 ! EndOfGame(gameId, p2Score, p1Score)
          p2 ! EndOfGame(gameId, p1Score, p2Score)
          become(shutdown)
          self ! ShutdownGame
        }
      }
    }
  }

  private def shutdown: Receive = {
    case ShutdownGame => {
      parent ! EndOfGame(gameId, p1Score, p2Score)
      log.info(gameLog("Shutting down"))
      context stop self
    }
  }
}

object GameActor {
  val MAX_ROUNDS = 10
  val MIN_ROUNDS = 6

  private[server] sealed trait GameActorProtocol
  private[GameActor] case object RoundTrigger extends GameActorProtocol
  private[GameActor] case object ShutdownGame extends GameActorProtocol
  private[server] case class InitGameConfigurable(init: InitGame, nrOfROunds: Option[Int] = None, roundDelay: FiniteDuration = 1.second) extends GameActorProtocol
}
