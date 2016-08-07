package org.dctrl.calendar.actor

import java.time.{LocalDate, LocalDateTime, LocalTime}
import java.util.UUID

import akka.actor.{Actor, ActorLogging, Props}
import org.dctrl.calendar.actor.CalendarEntryActor.{CalendarEntry, Cancel, Cancelled}


object CalendarEntryActor {

  def props(calendarEntry: CalendarEntry) = Props(new CalendarEntryActor(calendarEntry))

  // represents state of this actor
  case class CalendarEntry(id: UUID, name: String, date: LocalDate, beginTime: LocalTime, endTime: LocalTime)

  sealed trait Event
  case class Cancelled(calendarEntry: CalendarEntry) extends Command

  sealed trait Command
  case object Cancel extends Command

}

class CalendarEntryActor(var calendarEntry: CalendarEntry) extends Actor with ActorLogging {

  def cancelled: Receive = {
    case _ =>
  }

  def receive = {
    case Cancel =>
      context.parent ! Cancelled(calendarEntry)
      context.become(cancelled)

    case _ =>
  }

}