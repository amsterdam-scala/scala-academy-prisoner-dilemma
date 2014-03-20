package com.amsscala.server

import akka.actor._
import akka.testkit._
import org.specs2.mutable._
import org.specs2.time.NoTimeConversions
import com.amsscala.common._
import com.amsscala.common.GameProtocol._
import scala.concurrent.duration._
import com.amsscala.server.GameActor._

class GameActorTest extends Specification with NoTimeConversions {
  //  private implicit val WAIT = 5.seconds
  private val GAME_ID = randomId
  private val GAME_NAME = "test-game"

  "Game Actor" should {
    "send start game messages to actors after init" in new DefaultContext {
      gameActor ! InitGame(GAME_ID, GAME_NAME, client1.ref, client2.ref)
      client1.expectMsg(StartGame(GAME_ID, GAME_NAME))
      client2.expectMsg(StartGame(GAME_ID, GAME_NAME))
    }

    "not init twice" in new DefaultContext {
      gameActor ! InitGame(GAME_ID, GAME_NAME, client1.ref, client2.ref)
      client1.expectMsg(StartGame(GAME_ID, GAME_NAME))
      client2.expectMsg(StartGame(GAME_ID, GAME_NAME))

      gameActor ! InitGame(GAME_ID, GAME_NAME, client1.ref, client2.ref)
      client1.expectNoMsg
      client2.expectNoMsg
    }

    "init round when both players are ready" in new DefaultContext {
      gameActor ! InitGame(GAME_ID, GAME_NAME, client1.ref, client2.ref)
      client1.expectMsg(StartGame(GAME_ID, GAME_NAME))
      client2.expectMsg(StartGame(GAME_ID, GAME_NAME))

      p1Probe.send(gameActor, PlayerReady(client1.ref))
      p2Probe.send(gameActor, PlayerReady(client2.ref))

      p1Probe.expectMsg(StartRound(1))
      p2Probe.expectMsg(StartRound(1))
    }

    "send round result after both players answer" in new DefaultContext {
      doInit()

      p1Probe.expectMsg(StartRound(1))
      p2Probe.expectMsg(StartRound(1))

      p1Probe.send(gameActor, RoundAnswer(1, Talk))
      p2Probe.send(gameActor, RoundAnswer(1, Talk))

      p1Probe.expectMsg(RoundResult(1, Talk, Talk, 2, 2))
      p2Probe.expectMsg(RoundResult(1, Talk, Talk, 2, 2))
    }

    "send correct answer and score or other player" in new DefaultContext {
      doInit()

      p1Probe.expectMsg(StartRound(1))
      p2Probe.expectMsg(StartRound(1))

      p1Probe.send(gameActor, RoundAnswer(1, Talk))
      p2Probe.send(gameActor, RoundAnswer(1, Silent))

      p1Probe.expectMsg(RoundResult(1, Silent, Talk, 3, 0))
      p2Probe.expectMsg(RoundResult(1, Talk, Silent, 0, 3))

      p1Probe.expectMsg(StartRound(2))
      p2Probe.expectMsg(StartRound(2))
    }

    "play consecutive rounds and calculate score" in new DefaultContext {
      doInit()

      p1Probe.expectMsg(StartRound(1))
      p2Probe.expectMsg(StartRound(1))

      p1Probe.send(gameActor, RoundAnswer(1, Talk))
      p2Probe.send(gameActor, RoundAnswer(1, Talk))

      p1Probe.expectMsg(RoundResult(1, Talk, Talk, 2, 2))
      p2Probe.expectMsg(RoundResult(1, Talk, Talk, 2, 2))

      p1Probe.expectMsg(StartRound(2))
      p2Probe.expectMsg(StartRound(2))

      p1Probe.send(gameActor, RoundAnswer(2, Talk))
      p2Probe.send(gameActor, RoundAnswer(2, Talk))

      p1Probe.expectMsg(RoundResult(2, Talk, Talk, 4, 4))
      p2Probe.expectMsg(RoundResult(2, Talk, Talk, 4, 4))
    }

    "play full game" in new DefaultContext {
      doInit(Some(50), 1.millisecond)

      def doRound(roundNr: Int) {
        p1Probe.expectMsg(StartRound(roundNr))
        p2Probe.expectMsg(StartRound(roundNr))

        p1Probe.send(gameActor, RoundAnswer(roundNr, Talk))
        p2Probe.send(gameActor, RoundAnswer(roundNr, Talk))

        p1Probe.expectMsg(RoundResult(roundNr, Talk, Talk, roundNr * 2, roundNr * 2))
        p2Probe.expectMsg(RoundResult(roundNr, Talk, Talk, roundNr * 2, roundNr * 2))
      }

      (1 to 50).foreach(i => doRound(i))

      p1Probe.expectMsg(EndOfGame(GAME_ID, 100, 100))
      p2Probe.expectMsg(EndOfGame(GAME_ID, 100, 100))
    }
  }

  abstract class DefaultContext
    extends TestKit(ActorSystem("test-system"))
    with ImplicitSender with After {

    lazy val client1 = TestProbe()
    lazy val client2 = TestProbe()

    lazy val gameActor = system.actorOf(Props[GameActor])
    lazy val p1Probe = TestProbe()
    lazy val p1Actor = p1Probe.ref
    lazy val p2Probe = TestProbe()
    lazy val p2Actor = p2Probe.ref

    def after = system.shutdown()

    def doInit(nrOfRounds: Option[Int] = None, roundDelay: FiniteDuration = 1.second) = {
      gameActor ! InitGameConfigurable(InitGame(GAME_ID, GAME_NAME, client1.ref, client2.ref), nrOfRounds, roundDelay)
      client1.expectMsg(StartGame(GAME_ID, GAME_NAME))
      client2.expectMsg(StartGame(GAME_ID, GAME_NAME))
      p1Probe.send(gameActor, PlayerReady(client1.ref))
      p2Probe.send(gameActor, PlayerReady(client2.ref))
    }
  }
}