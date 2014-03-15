package com.amsscala.academy
package client

import common.GameServerSettings._

import akka.actor.{Props, Address, ActorSystem}
import com.typesafe.config.ConfigFactory
import akka.cluster.Cluster

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

    val clientMaster = system.actorOf(Props[ClientMaster], "clientmaster")
    // clientMaster ! JoinServer
  }
}
