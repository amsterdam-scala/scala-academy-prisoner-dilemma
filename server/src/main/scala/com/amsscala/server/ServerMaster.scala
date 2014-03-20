package com.amsscala
package server

import java.util.UUID._

import scala.collection.mutable.{ Set => MutableSet, Map => MutableMap }
import scala.util.Random

import akka.actor.{ Props, ActorRef, Actor, ActorLogging }

import common.LobbyProtocol.Register
import common.{ Waiting, ClientState }
import common.GameProtocol.{EndOfGame, InitGame}


class ServerMaster extends Actor with ActorLogging {

  private[this] val clients = MutableSet[Client]()
  private[this] val games   = MutableMap[String, Game]()

  private[this] def startGame(player1: ActorRef, player2: ActorRef): Game = {
    val game = context.actorOf(Props(new GameActor))
    val gameId: String = randomUUID().toString
    game ! InitGame(gameId, randomUUID().toString, player1, player2)

    Game(gameId, player1, player2)
  }

  def receive = {
    case Register =>
      val newClient = Client(sender, Waiting)
      clients += newClient

      log.info("Registered a new client {}", newClient)
      log.info("Finding a match for {}", newClient)

      findMatch(newClient).foreach { matched =>
        val newGame = startGame(newClient.ref, matched.ref)
        games += newGame.id -> newGame
      }

    case EndOfGame(id, p1, p2) =>
      games.get(id).map { game =>
        updateClient(game.player1, newState = Waiting)
        updateClient(game.player2, newState = Waiting)
      }
  }

  private[this] def findMatch(client: Client): Option[Client] = {
    val allMatches = clients.filter(c => c.state == Waiting && c.ref != client.ref).toList

    if (allMatches.size > 0)
      Some(allMatches(Random.nextInt(allMatches.size)))
    else
      None
  }

  private[this] def updateClient(ref: ActorRef, newState: ClientState): Unit = {
    clients.find(_.ref == ref).map { client =>
      clients -= client
      clients += client.copy(state = newState)
    }
  }
}

case class Client(ref: ActorRef, state: ClientState)
case class Game(id: String, player1: ActorRef, player2: ActorRef)
