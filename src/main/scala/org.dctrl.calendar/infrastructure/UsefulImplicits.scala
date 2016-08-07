package org.dctrl.calendar.infrastructure

import org.json4s.{DefaultFormats, jackson}

import scala.concurrent.duration._

trait UsefulImplicits {

  implicit val formats = DefaultFormats

  implicit val serialization = jackson.Serialization

  implicit val timeout = akka.util.Timeout(250 milliseconds)


  implicit def uuid2String(v: java.util.UUID):String = v.toString

  implicit def localTime2String(v: java.time.LocalTime):String = v.toString

  implicit def localDate2String(v: java.time.LocalDate):String = v.toString

  implicit def localDateTime2String(v: java.time.LocalDateTime):String = v.toString

  implicit def string2Exception(v: String): Throwable = new Exception(v)

}
