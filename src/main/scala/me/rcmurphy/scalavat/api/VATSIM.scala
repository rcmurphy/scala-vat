package me.rcmurphy.scalavat.api

import com.typesafe.scalalogging.Logger
import me.rcmurphy.scalavat.AirportDB
import me.rcmurphy.scalavat.model.{Server, VatsimInfo}

import scalaj.http.Http


class VATSIM()(implicit airportDb: AirportDB) {
  private val logger = Logger(getClass)


  private final val ServerRegex = "url0=(.*)".r
  private final val SectionRegex = """!(.*):""".r

  private def sanitizeAndSplitResponse(resp: String): Seq[String] = {
    resp.split("\n").collect {
      case l if !l.startsWith(";") => l.trim
    }
  }


  def getServers: Seq[Server] = {
    val serverList = Http("http://status.vatsim.net/status.txt").asString

    sanitizeAndSplitResponse(serverList.body) collect {
      case ServerRegex(server) =>
        logger.debug(s"Found Server: $server")
        Server(server)
    }
  }

  def getData(server: Server): VatsimInfo = {

    val rawData = sanitizeAndSplitResponse(Http(server.url).asString.body)

    def parseData(data: Seq[String]) = {
      var section: Option[DataBlock] = None
      (for {
        line <- data
      } yield {
        line match {
          case SectionRegex(sectionNameStr) =>
            DataBlock.withNameOption(sectionNameStr) match {
              case Some(dataBlock) =>
                section = Some(dataBlock)
              case _ =>
                throw new Exception()
            }

            Seq()
          case _ if section.isDefined =>
            Seq((section.get, line))
        }
      }).flatten.groupBy(_._1).mapValues(_.map(_._2))
    }

    VatsimInfo.fromMap(parseData(rawData))
  }
}
