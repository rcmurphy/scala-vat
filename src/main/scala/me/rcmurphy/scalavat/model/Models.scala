package me.rcmurphy.scalavat.model

import com.typesafe.scalalogging.Logger
import me.rcmurphy.scalavat.AirportDB
import me.rcmurphy.scalavat.api.{ATCRole, DataBlock}

case class Server(url: String) extends AnyVal

sealed trait Client {
  def callsign: String
  def cid: String
  def name: String
}

object Client {
  private val logger = Logger(classOf[Client])

  private val StaffRegex = """VAT([\w]{3,5})""".r
  def apply(clientStr: String)(implicit airportDb: AirportDB): Option[Client] = {
    val segments = clientStr.split(":")
    val callsign = segments(0)
    val cid = segments(1)
    val name = segments(2)
    segments(3) match {
      case "PILOT" => Some(Pilot(callsign, cid, name))
      case "ATC" =>
        val callsignSegments = callsign.split("[_-]")
        val location = ATCLocation(callsignSegments)
        val role = ATCRole.withNameOption(callsignSegments.last).toRight(callsign.split("_").last) match {
          case Right(r) =>
            logger.trace(s"Found Controller '$name' working '$r' at '$location'")
            r
          case Left(StaffRegex(staff)) => ATCRole.Staff
          case Left(unknown) if callsignSegments.length < 2 =>
            logger.debug(s"Misconfigured client (probably) '$unknown' for '$name' @ '$callsign'")
            ATCRole.Unknown
          case Left(unknown) =>
            logger.info(s"Unknown ATC role '$unknown' for '$name' @ '$callsign'")
            ATCRole.Unknown
        }
        Some(ATC(callsign, cid, name, role, location))
      case other =>
        logger.warn(s"Unknown client type: $other")
        None
    }
  }
}

case class Pilot(callsign: String, cid: String, name: String) extends Client

case class ATC(callsign: String, cid: String, name: String, role: ATCRole, location: ATCLocation) extends Client


case class VatsimInfo(
  servers: Seq[String],
  voiceServers: Seq[String],
  prefile: Seq[String],
  general: Seq[String],
  clients: Seq[Client]
)

object VatsimInfo {

  def fromMap(map: Map[DataBlock, Seq[String]])(implicit airportDb: AirportDB): VatsimInfo = {
    VatsimInfo(
      map(DataBlock.Servers),
      map(DataBlock.VoiceServers),
      map(DataBlock.PreFile),
      map(DataBlock.General),
      map(DataBlock.Clients).flatMap(Client.apply(_))
    )
  }
}
