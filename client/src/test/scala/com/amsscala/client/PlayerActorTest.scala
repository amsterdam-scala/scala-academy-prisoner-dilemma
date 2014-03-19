package com.amsscala.client

import akka.actor._
import akka.testkit._
import org.specs2.mutable._
import org.specs2.time.NoTimeConversions
import com.amsscala.common._
import com.amsscala.common.GameProtocol._
import scala.concurrent.duration._

class PlayerActorTest extends Specification with NoTimeConversions {
  //  private implicit val WAIT = 5.seconds
  private val GAME_ID = randomId
  private val GAME_NAME = "test-game"

  "Player basic protocol" should {
    "reply with ready after game start" in new DefaultContext {
      playerActor ! StartGame(GAME_ID, GAME_NAME)
      expectMsg(PlayerReady)
    }

    "reply with answer on a new round" in new DefaultContext {
      playerActor ! StartGame(GAME_ID, GAME_NAME)
      expectMsg(PlayerReady)

      playerActor ! StartRound(1)
      val answer = expectMsgType[RoundAnswer]
      answer.roundNr mustEqual 1
      answer.answer mustNotEqual null
    }

    "reply with with answer on consecutive rounds" in new DefaultContext {
      playerActor ! StartGame(GAME_ID, GAME_NAME)
      expectMsg(PlayerReady)

      playerActor ! StartRound(1)
      val r1answer = expectMsgType[RoundAnswer]
      r1answer.roundNr mustEqual 1
      playerActor ! RoundResult(1, Talk, r1answer.answer, 2, 2)

      playerActor ! StartRound(2)
      val r2answer = expectMsgType[RoundAnswer]
      r2answer.roundNr mustEqual 2
      playerActor ! RoundResult(2, Talk, r2answer.answer, 4, 4)
    }

    "reset and work on new game" in new DefaultContext {
      def checkFullGame() {
        playerActor ! StartGame(GAME_ID, GAME_NAME)
        expectMsg(PlayerReady)

        playerActor ! StartRound(1)
        val r1answer = expectMsgType[RoundAnswer]
        r1answer.roundNr mustEqual 1
        playerActor ! RoundResult(1, Talk, r1answer.answer, 2, 2)

        playerActor ! EndOfGame(2, 2)
      }

      checkFullGame();
      checkFullGame();
      checkFullGame();
    }

  }

  abstract class DefaultContext
    extends TestKit(ActorSystem("test-system"))
    with ImplicitSender with After {

    lazy val playerActor = system.actorOf(Props[PlayerActor])

    def after = system.shutdown()
  }
}