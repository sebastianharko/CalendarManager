package org.dctrl.calendar.actor

import akka.actor.Props
import akka.persistence.PersistentView
import org.dctrl.calendar.actor.CalendarEventsView.Get

object CalendarEventsView {

  def props = Props(classOf[CalendarEventsView])

  sealed trait Query
  case object Get extends Query

}

class CalendarEventsView extends PersistentView {

  var allEvents = List[(String, CalendarManagementActor.Event)]()

  override def persistenceId: String = "CalendarManagementActor"

  override def viewId: String = "CalendarManagementActor-EventsView"

  def receive: Receive = {

    case Get =>
      sender() ! allEvents
    case e: CalendarManagementActor.Event =>
      allEvents = (e.name, e) :: allEvents

  }

}
