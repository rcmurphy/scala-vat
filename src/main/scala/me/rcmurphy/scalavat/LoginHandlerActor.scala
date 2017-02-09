package me.rcmurphy.scalavat

import akka.actor.Actor
import com.typesafe.scalalogging.Logger
import me.rcmurphy.scalavat.model.Client

class LoginHandlerActor extends Actor {
  private val logger = Logger(getClass)
  def receive: Receive = {
    case Connected(client) =>
      logger.info(s"Client connected: $client")
    case Disconnected(client) =>
      logger.info(s"Client disconnected: $client")
    case CallsignChange(client) =>
      logger.info(s"Client changed callsign: $client")
  }
}

sealed trait Event {
  def client: Client
}

case class Connected(client: Client) extends Event
case class Disconnected(client: Client) extends Event
case class CallsignChange(client: Client) extends Event
