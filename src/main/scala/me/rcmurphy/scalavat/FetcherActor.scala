package me.rcmurphy.scalavat

import java.net.URI

import akka.actor.{Actor, ActorRef}
import com.typesafe.scalalogging.Logger
import me.rcmurphy.scalavat.api.{VATSIM, VatsimInfo}
import me.rcmurphy.scalavat.model.{ATC, VatsimInfo}

import scala.util.Random

class FetcherActor(loginHandlerActor: ActorRef) extends Actor {
  private val logger = Logger(getClass)

  private implicit val airportDB = AirportDB.load("./airports.dat")
  private val vatsim = new VATSIM()

  private var previousData: Option[VatsimInfo] = None

  private lazy val servers = vatsim.getServers

  def receive: Receive = {
    case Fetch =>
      val server = Random.shuffle(servers ).headOption.getOrElse {
        throw new RuntimeException("Couldn't find any VATSIM servers")
      }
      logger.info("Fetching Data from: " + new URI(server.url).getHost)
      val currentData = vatsim.getData(server)
      logger.debug(s"Fetched Data from VATSIM, found ${currentData.clients.length} clients.")

      previousData match {
        case Some(previous) =>
          val currentClients = currentData.clients
          val previousClients = previous.clients
          val callsignChanges = currentClients.filter {
            current =>
              previousClients.exists(_.cid == current.cid) &&
                !previousClients.exists(_.callsign == current.callsign) &&
              !(current.isInstanceOf[ATC] || previousClients.find(_.cid == current.cid).exists(_.isInstanceOf[ATC]))
          }

          for(newClient <- currentClients if !previousClients.exists(previous => previous.cid == newClient.cid && previous.callsign == newClient.callsign) && !callsignChanges.exists(_.cid == newClient.cid)) {
            loginHandlerActor ! Connected(newClient)
          }
          for(oldClient <- previousClients if !currentClients.exists(current => current.cid == oldClient.cid && current.callsign == oldClient.callsign) && !callsignChanges.exists(_.cid == oldClient.cid))  {
            loginHandlerActor ! Disconnected(oldClient)
          }
          for(callsignChange <- callsignChanges) {
            loginHandlerActor ! CallsignChange(callsignChange)
          }
        case None =>
          logger.debug("No previous data found.")
      }
      previousData = Some(currentData)
  }
}

case object Fetch
