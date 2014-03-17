package com.amsscala
package client

import common.GameServerSettings._

import akka.actor._
import akka.cluster.{ Member, Cluster }
import akka.cluster.ClusterEvent.InitialStateAsEvents
import akka.cluster.ClusterEvent.MemberUp

import com.typesafe.config.ConfigFactory

object ClientMain extends App {

  val config = ConfigFactory.load().getConfig("client")
  val system = ActorSystem(clusterName, config)

  println("Client is started.")

  val cluster = Cluster(system)
  val serverAddress = Address("akka.tcp", clusterName, "127.0.0.1", 2552)

  cluster.join(serverAddress)

  cluster.registerOnMemberUp {
    println()
    println("Client joined the game server.")
    println("Connecting to the server lobby.\n")

    cluster.subscribe(system.actorOf(Props(new Actor {
      def receive = {
        case MemberUp(member) if member.hasRole("server") => register(member)
      }
    })), initialStateMode = InitialStateAsEvents, classOf[MemberUp])
  }

  def register(serverNode: Member): Unit = {
    val server = system.actorSelection(RootActorPath(serverNode.address) / "user" / "serverMaster")
    system.actorOf(Props(new ClientMaster(server)), "clientMaster")
  }
}
