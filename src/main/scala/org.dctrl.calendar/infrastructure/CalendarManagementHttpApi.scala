package org.dctrl.calendar.infrastructure

import java.time.{LocalDate, LocalTime}
import java.util.UUID

import akka.actor.ActorRef
import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.server.Directives._
import akka.pattern.ask
import de.heikoseeberger.akkahttpjson4s.Json4sSupport
import org.dctrl.calendar.actor.CalendarEntryActor.CalendarEntry
import org.dctrl.calendar.actor.CalendarEventsView.Get
import org.dctrl.calendar.actor.CalendarManagementActor.{CancelCalendarEntry, CreateCalendarEntry, EntryFound, EntryNotFound}
import org.dctrl.calendar.dto
import org.dctrl.calendar.dto.NewCalendarEntry
import org.dctrl.calendar.infrastructure.CalendarManagementHttpApi.{Error, UnknownError}

import scala.language.postfixOps
import scala.util.{Failure, Success, Try}


object CalendarManagementHttpApi {

  val UnknownError = Error("unknown error")

  def apply(calendarManagementActor: ActorRef, calendarEventsView: ActorRef) =
    new CalendarManagementHttpApi(calendarManagementActor, calendarEventsView)

  final case class Error(error: String)


}

class CalendarManagementHttpApi(calendarManagementActor: ActorRef, calendarEventsView: ActorRef) extends Json4sSupport with UsefulImplicits {

  val routes =
    path("events") {
      get {
        onComplete(calendarEventsView ? Get) {
          case Success(result: AnyRef) => complete(result)
          case Failure(_) => complete(StatusCodes.InternalServerError -> UnknownError)
        }
      }
    } ~
      path("entries") {
        post {
          entity(as[NewCalendarEntry]) {
            nce => {

              val maybeCommand = for {
                validName <- if (nce.name.length < 42) Success(nce.name) else Failure("calendar entry name too long")
                parsedDate <- Try(LocalDate.parse(nce.date))
                parsedBeginTime <- Try(LocalTime.parse(nce.beginTime))
                parsedEndTime <- Try(LocalTime.parse(nce.endTime))
                validDate <- if (parsedDate.isAfter(LocalDate.now().minusDays(1))) Success(parsedDate) else Failure("event date must be today or after today")
                validEndTime <- if (parsedEndTime.isAfter(parsedBeginTime)) Success(parsedEndTime) else Failure("end time must be after begin time")
              } yield CreateCalendarEntry(validName, validDate, parsedBeginTime, validEndTime)

              maybeCommand match {

                case Success(command) =>

                  onComplete(calendarManagementActor ? command) {
                    case Success(CalendarEntry(id, name, date, beginTime, endTime)) =>
                      complete(dto.CalendarEntry(id, name, date, beginTime, endTime))
                    case Failure(_) =>
                      complete(StatusCodes.InternalServerError -> UnknownError)
                  }

                case Failure(e) =>
                  complete(StatusCodes.NotAcceptable -> Error(e.getMessage))

              }


            }
          }
        } ~ delete {
          parameters('entryId.as[String]) {
            entryId => {
              Try(UUID.fromString(entryId)) match {
                case Success(id) =>
                  onComplete(calendarManagementActor ? CancelCalendarEntry(id)) {
                    case Success(EntryFound) => complete(StatusCodes.Accepted)
                    case Success(EntryNotFound) => complete(StatusCodes.NotFound -> Error(s"was not able to find a calendar entry with id $entryId"))
                    case Failure(_) => complete(StatusCodes.InternalServerError -> UnknownError)
                  }

                case Failure(_) => complete(StatusCodes.NotAcceptable -> Error(s"id $entryId not a valid uuid"))
              }

            }
          }
        }
      }

}