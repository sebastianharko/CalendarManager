package org.dctrl.calendar.actor

import java.time.{LocalDate, LocalTime}
import java.util.UUID
import java.util.UUID._

import akka.actor.{ActorLogging, Props}
import akka.persistence.PersistentActor
import org.dctrl.calendar.actor.CalendarEntryActor.{CalendarEntry, Cancel}
import org.dctrl.calendar.actor.CalendarManagementActor._
import org.dctrl.calendar.infrastructure.UsefulImplicits

object CalendarManagementActor {

  def props = Props[CalendarManagementActor]

  sealed trait Event {
    def name:String
  }
  case class CalendarEntryAdded(calendarEntry: CalendarEntry) extends Event {
    override def name = "CalendarEntryAdded"
  }
  case class CalendarEntryCancellationRequested(validEntryId: UUID) extends Event {
    override def name = "CalendarEntryCancellationRequested"
  }

  sealed trait Command
  case class CreateCalendarEntry(name: String, date: LocalDate, beginTime: LocalTime, endTime: LocalTime) extends Command
  case class CancelCalendarEntry(id: UUID) extends Command

  case object EntryNotFound
  case object EntryFound

}

class CalendarManagementActor extends PersistentActor with ActorLogging with UsefulImplicits {

  def onEvent(event: Event) = {
    event match {
      case calendarEntryAdded @ CalendarEntryAdded(entry) =>
        context.actorOf(CalendarEntryActor.props(entry), "entry-" + entry.id)

      case CalendarEntryCancellationRequested(id) =>
        context.child("entry-" + id).get ! Cancel

      case _ =>
        log.info("told to handle event I don't understand")
    }
  }

  override def receiveCommand: Receive = {

    case CreateCalendarEntry(name, date, beginTime, endTime) =>

      persist(CalendarEntryAdded(CalendarEntry(randomUUID(), name, date, beginTime, endTime))) {
        event => {
          onEvent(event)
          sender() ! event.calendarEntry
        }
      }

    case CancelCalendarEntry(id) =>
      context.child("entry-" + id) match {

        case Some(actorRef) =>
         persist(CalendarEntryCancellationRequested(id)) {
           event =>
             onEvent(event)
             sender() ! EntryFound
         }

        case None =>
          sender() ! EntryNotFound
      }

  }

  override def receiveRecover: Receive = {
    case e:Event =>
      onEvent(e)
  }

  override def persistenceId: String = "CalendarManagementActor"

}













