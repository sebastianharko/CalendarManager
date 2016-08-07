package org.dctrl.calendar

import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.Http.ServerBinding
import akka.stream.ActorMaterializer
import org.dctrl.calendar.actor.{CalendarEventsView, CalendarManagementActor}
import org.dctrl.calendar.infrastructure.CalendarManagementHttpApi

import scala.concurrent.Future
import scala.util.{Failure, Success}

class Boot

object Boot extends App {

  val logger = org.slf4j.LoggerFactory.getLogger(classOf[Boot])

  implicit val actorSystem = ActorSystem("calendar")
  implicit val materializer = ActorMaterializer()

  import actorSystem.dispatcher

  val calendarManagementActor = actorSystem.actorOf(CalendarManagementActor.props)
  val calendarEventsView = actorSystem.actorOf(CalendarEventsView.props)

  val routes = CalendarManagementHttpApi(calendarManagementActor, calendarEventsView).routes

  val bindingFuture: Future[ServerBinding] = Http().bindAndHandle(routes, "localhost", 8080)

  bindingFuture.onComplete {
    case Success(_) => logger.info("started successfully")
    case Failure(ex) => logger.error("failed to start server", ex)
  }


}