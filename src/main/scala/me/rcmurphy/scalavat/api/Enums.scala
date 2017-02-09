package me.rcmurphy.scalavat.api

import enumeratum.{Enum, EnumEntry}

sealed abstract class DataBlock(override val entryName: String) extends EnumEntry

object DataBlock extends Enum[DataBlock] {

  val values = findValues

  case object Servers extends DataBlock("SERVERS")
  case object VoiceServers extends DataBlock("VOICE SERVERS")
  case object PreFile extends DataBlock("PREFILE")
  case object General extends DataBlock("GENERAL")
  case object Clients extends DataBlock("CLIENTS")
}

sealed abstract class ATCRole(override val entryName: String) extends EnumEntry

object ATCRole extends Enum[ATCRole] {

  val values = findValues

  case object Approach extends ATCRole("APP")
  case object ATIS extends ATCRole("ATIS")
  case object Center extends ATCRole("CTR")
  case object Delivery extends ATCRole("DEL")
  case object Departure extends ATCRole("DEP")
  case object FlightServices extends ATCRole("FSS")
  case object Ground extends ATCRole("GND")
  case object Observer extends ATCRole("OBS")
  case object Supervisor extends ATCRole("SUP")
  case object Tower extends ATCRole("TWR")
  case object Staff extends ATCRole("VATSTAFF")
  case object Unknown extends ATCRole("???")
}
