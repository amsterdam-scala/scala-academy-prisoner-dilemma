package com.amsscala
package server

import common.GameServerSettings._

import akka.actor.{ Actor, ActorLogging, Props, ActorSystem }
import akka.cluster.ClusterEvent.{ ClusterDomainEvent, UnreachableMember, MemberUp, CurrentClusterState }
import akka.cluster.Cluster
import com.typesafe.config.ConfigFactory

object ServerMain extends App {

  val config = ConfigFactory.load().getConfig(clusterName)
  val system = ActorSystem(clusterName, config)

  startup(system)

  println("Server is started.")

  def startup(system: ActorSystem) {
    system.actorOf(Props[ServerMaster], "serverMaster")
    val listener = system.actorOf(Props(new ClusterListener))

    Cluster(system).subscribe(listener, classOf[ClusterDomainEvent])
  }
}

class ClusterListener extends Actor with ActorLogging {
  def receive = {
    case s: CurrentClusterState    ⇒ log.info("Current cluster members: {}", s.members)
    case MemberUp(member)          ⇒ log.info("Member is up: {}", member)
    case UnreachableMember(member) ⇒ log.info("Member is unreachable: {}", member)
    case e: ClusterDomainEvent     ⇒ // ignore
  }
}
