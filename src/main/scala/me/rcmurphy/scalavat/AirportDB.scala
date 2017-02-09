package me.rcmurphy.scalavat

import java.io.FileReader

import com.opencsv.CSVReader
import com.typesafe.scalalogging.Logger
import me.rcmurphy.scalavat.api.Airport

import scala.collection.JavaConversions._

class AirportDB(airports: Seq[Airport]) {

  private val byIcao = airports.map { a => (a.icao, a) } .toMap

  def get(icao: String): Option[Airport] = byIcao.get(icao).orElse {
    if(icao.length == 3) {
      byIcao.get("K" + icao)
    } else {
      None
    }
  }
}

object AirportDB {
  private val logger = Logger(classOf[AirportDB])
  def load(path: String): AirportDB = {
    val reader = new CSVReader(new FileReader(path))

    val airports = reader.readAll().map { airport =>
      // Format: 3520,"Ronald Reagan Washington Natl","Washington","United States","DCA","KDCA",38.852083,-77.037722,15,-5,"A","America/New_York"

      Airport(airport(5), airport(4), airport(1))
    }
    reader.close()
    logger.info(s"Read ${reader.getRecordsRead} airports")
    new AirportDB(airports)
  }
}
