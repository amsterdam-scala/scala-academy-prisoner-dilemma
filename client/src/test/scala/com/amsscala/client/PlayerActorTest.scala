package com.amsscala
package client

import java.util.UUID._

import akka.actor._
import akka.testkit._
import org.specs2.mutable._
import org.specs2.time.NoTimeConversions

import common._
import GameProtocol._

class PlayerActorTest extends Specification with NoTimeConversions {
  //  private implicit val WAIT = 5.seconds
  private val GAME_ID = randomId
  private val GAME_NAME = "test-game"
  private val OPPONENT_ID = randomId

  "Player basic protocol" should {
    "reply with ready after game start" in new DefaultContext {
      playerActor ! StartGame(GAME_ID, GAME_NAME, OPPONENT_ID)
      expectMsg(PlayerReady(client.ref))
    }

    "reply with answer on a new round" in new DefaultContext {
      playerActor ! StartGame(GAME_ID, GAME_NAME, OPPONENT_ID)
      expectMsg(PlayerReady(client.ref))

      playerActor ! StartRound(1)
      val answer = expectMsgType[RoundAnswer]
      answer.roundNr mustEqual 1
      answer.answer mustNotEqual null
    }

    "reply with with answer on consecutive rounds" in new DefaultContext {
      playerActor ! StartGame(GAME_ID, GAME_NAME, OPPONENT_ID)
      expectMsg(PlayerReady(client.ref))

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
        playerActor ! StartGame(GAME_ID, GAME_NAME, OPPONENT_ID)
        expectMsg(PlayerReady(client.ref))

        playerActor ! StartRound(1)
        val r1answer = expectMsgType[RoundAnswer]
        r1answer.roundNr mustEqual 1
        playerActor ! RoundResult(1, Talk, r1answer.answer, 2, 2)

        playerActor ! EndOfGame(GAME_ID, 2, 2)
      }

      checkFullGame();
      checkFullGame();
      checkFullGame();
    }

  }

  abstract class DefaultContext
    extends TestKit(ActorSystem("test-system"))
    with ImplicitSender with After {

    lazy val client = TestProbe()

    lazy val playerActor = system.actorOf(Props(new PlayerActor(randomUUID.toString, randomUUID.toString, client.ref)))

    def after = system.shutdown()
  }
}