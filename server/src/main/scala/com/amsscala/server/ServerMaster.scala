package com.amsscala
package server

import common.LobbyProtocol.Register
import common.{ Waiting, ClientState }

import scala.collection.mutable.Set
import scala.util.Random

import akka.actor.{ActorRef, Actor, ActorLogging}


class ServerMaster extends Actor with ActorLogging {

  private[this] val clients: Set[Client] = Set()

  def receive = {
    case Register ⇒
      val newClient = Client(sender, Waiting)
      clients += newClient

      log.info("Registered a new client {}", newClient)
      log.info("Finding a match for {}",     newClient)

      findMatch(newClient).foreach { matched =>
        newClient.ref ! "You got a match :)"
        matched.ref   ! "You got a match :)"

        // TODO setup a new game here =)
      }
  }

  private[this] def findMatch(client: Client): Option[Client] = {
    val allMatches = clients.filter(c ⇒ c.state == Waiting && c.ref != client.ref).toList

    if (allMatches.size > 0)
      Some(allMatches(Random.nextInt(allMatches.size)))
    else
      None
  }
}

case class Client(ref: ActorRef, state: ClientState)
