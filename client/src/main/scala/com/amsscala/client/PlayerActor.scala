package com.amsscala
package client

import akka.actor._
import com.amsscala.common._

class PlayerActor(gameId: String, name: String, client: ActorRef) extends Actor with ActorLogging {
  import GameProtocol._

  override def receive = {
    case StartGame(id, name, opponent) =>
      sender ! PlayerReady(client)
  }
}
