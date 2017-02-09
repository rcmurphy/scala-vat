package me.rcmurphy.scalavat.model

import com.typesafe.scalalogging.Logger
import me.rcmurphy.scalavat.AirportDB
import me.rcmurphy.scalavat.api.{ATCRole}


trait ATCLocation
case class Airport(icao: String, iata: String, name: String) extends ATCLocation {
  override def toString: String = {
    icao + ": " + name + " Airport"
  }
}

case class Center(code: String, name: Option[String]) extends ATCLocation {
  override def toString: String = {
    name.getOrElse(code) + " Center"
  }
}

object Center {
  def apply(code: String, name: String): Center = new Center(code, Some(name))
  val KnownCenters = Seq(
    Center("ZDC", "Washington"),
    Center("ZNY", "New York"),
    Center("ZMA", "Miami"),
    Center("ZTL", "Atlanta")
  )
}

case class UnrecognizedATCLocation(location: String) extends ATCLocation {

  override def toString: String = location  + " [Unknown Type]"

}

case object Nowhere extends ATCLocation {
  override def toString: String = "Nowhere"
}

object ATCLocation {

  private val logger = Logger(classOf[ATCLocation])
  def apply(raw: Seq[String])(implicit airportDb: AirportDB): ATCLocation = {
    val locationOpt = raw.headOption collect {
      case knownAirport if airportDb.get(knownAirport).isDefined => airportDb.get(knownAirport)
      case knownCenter if Center.KnownCenters.exists(_.code == knownCenter) => Center.KnownCenters.find(_.code == knownCenter)
      case unknownCenter if raw.last == ATCRole.Center.entryName || raw.last == ATCRole.Observer.entryName => Some(Center(s"'$unknownCenter' Center", unknownCenter))
      case initials if (initials.length == 2 || initials.length == 3) && (raw.last == ATCRole.Observer.entryName || raw.last == ATCRole.Supervisor.entryName) => Some(Nowhere)
    }
    //for(location <- locationOpt)
    //  logger.info(s"Recognized location: \t${raw.dropRight(1).map(_.trim)}")
    locationOpt.flatten.getOrElse {
      logger.warn(s"Unrecognized location: \t$raw")
      UnrecognizedATCLocation(raw.take(raw.length - 1).mkString("_"))
    }
  }
}
