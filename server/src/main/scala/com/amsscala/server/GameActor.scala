package com.amsscala.server

import akka.actor._
import scala.concurrent.duration._
import com.amsscala.PrisonersDilemma

class GameActor extends Actor with ActorLogging {
  import context._

  import GameActor._
  import PlayerActor._

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

  private var currentP1Answer: Option[RoundAnswer] = None
  private var currentP2Answer: Option[RoundAnswer] = None

  override def receive = {
    case init: InitGame => {
      gameId = init.id
      gameName = init.name
      p1 = init.p1
      p2 = init.p2
      p1 ! StartGame(gameId, gameName)
      p2 ! StartGame(gameId, gameName)
      become(prepare)
    }
  }

  private def prepare: Receive = {
    case PlayerReady => {
      if (sender == p1) {
        p1Ready == true
      } else if (sender == p2) {
        p2Ready == true
      } else {
        log.warning("Received ready message from player that is not in game: " + sender)
      }
      if (p1Ready && p2Ready) {
        currentRound = 0
        totalRounds = (Math.random() * (MAX_ROUNDS - MIN_ROUNDS)).toInt + MIN_ROUNDS
        p1Score = 0
        p2Score = 0

        become(game)
        system.scheduler.scheduleOnce(1.second, self, RoundTrigger)
      }
    }
  }

  private def game: Receive = {
    case RoundTrigger => {
      currentRound += 1
      currentP1Answer = None
      currentP2Answer = None

      p1 ! StartRound(currentRound)
      p2 ! StartRound(currentRound)
    }
    case answer: RoundAnswer => {
      (sender, answer) match {
        case (p1, answer) if (currentP1Answer.isEmpty) =>
          currentP1Answer = Some(answer)
        case (p2, answer) if (currentP2Answer.isEmpty) =>
          currentP2Answer = Some(answer)
        case (p, _) =>
          log.warning("Result already received for round " + currentRound + " from player " + p)
      }

      if (currentP1Answer.isDefined && currentP2Answer.isDefined) {
        val (p1RoundScore, p2RoundScore) = PrisonersDilemma.engine(currentP1Answer.get.defect, currentP2Answer.get.defect)
        p1Score += p1RoundScore
        p2Score += p2RoundScore

        p1 ! RoundResult(currentRound, currentP1Answer.get.defect, currentP2Answer.get.defect, p1Score, p2Score)
        p2 ! RoundResult(currentRound, currentP2Answer.get.defect, currentP1Answer.get.defect, p2Score, p1Score)

        if (currentRound < totalRounds) {
          system.scheduler.scheduleOnce(1.second, self, RoundTrigger)
        } else {
          p1 ! FinishGame
          p2 ! FinishGame
          become(shutdown)
          self ! ShutdownGame
        }
      }
    }
  }

  private def shutdown: Receive = {
    case ShutdownGame => {
      parent ! GameResult(gameId, p1Score, p2Score)
      context stop self
    }
  }
}

object GameActor {
  val MAX_ROUNDS = 100
  val MIN_ROUNDS = 60

  sealed trait GameActorProtocol
  case class InitGame(id: String, name: String, p1: ActorRef, p2: ActorRef) extends GameActorProtocol
  case object PlayerReady extends GameActorProtocol
  case class RoundAnswer(defect: Boolean) extends GameActorProtocol
  private case object RoundTrigger extends GameActorProtocol
  private case object ShutdownGame extends GameActorProtocol

  // TODO: move to other protocol
  case class GameResult(id: String, p1Score: Int, p2Score: Int)
}

class PlayerActor extends Actor with ActorLogging {
  import GameActor._
  import PlayerActor._

  override def receive = {
    case StartGame(_, _) => sender ! PlayerReady
    case StartRound(_)   => sender ! RoundAnswer(Math.random() >= 0.5)
    case r: RoundResult  => log.info(r.toString)
    case FinishGame      => log.info("Done")
  }
}

object PlayerActor {
  sealed trait PlayerActorProtocol
  case class StartGame(id: String, name: String) extends PlayerActorProtocol
  case class StartRound(number: Int) extends PlayerActorProtocol
  case class RoundResult(number: Int, selfDefect: Boolean, otherDefect: Boolean, selfScore: Int, otherScore: Int) extends PlayerActorProtocol
  case object FinishGame
}
