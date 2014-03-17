package com.amsscala.common

sealed abstract class ClientState

case object Waiting extends ClientState
case object Playing extends ClientState
