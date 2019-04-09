package com.example

import akka.actor.{ ActorRef, ActorSystem }
import akka.event.Logging

import scala.concurrent.duration._
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.server.Route
import akka.http.scaladsl.server.directives.MethodDirectives.delete
import akka.http.scaladsl.server.directives.MethodDirectives.get
import akka.http.scaladsl.server.directives.MethodDirectives.post
import akka.http.scaladsl.server.directives.RouteDirectives.complete
import akka.http.scaladsl.server.directives.PathDirectives.path

import scala.concurrent.Future
import com.example.MovieRegistryActor._
import akka.pattern.ask
import akka.util.Timeout

trait MovieRoutes extends JsonSupport {

  implicit def system: ActorSystem

  lazy val log = Logging(system, classOf[MovieRoutes])

  def movieRegistryActor: ActorRef

  implicit lazy val timeout = Timeout(5.seconds)

  lazy val movieRoutes: Route =
    pathPrefix("movies") {
      concat(
        pathEnd {
          concat(
            get {
              val movie: Future[Movies] =
                (movieRegistryActor ? GetMovies).mapTo[Movies]
              complete(movie)
            },
            post {
              entity(as[Movie]) { movie =>
                val movieCreated: Future[ActionPerformed] =
                  (movieRegistryActor ? AddMovie(movie)).mapTo[ActionPerformed]
                onSuccess(movieCreated) { performed =>
                  log.info("Created movie [{}]: {}", movie.name, performed.description)
                  complete((StatusCodes.Created, performed))
                }
              }
            })
        },
        path(Segment) { name =>
          concat(
            get {
              val maybeMovie: Future[Option[Movie]] =
                (movieRegistryActor ? GetMovie(name)).mapTo[Option[Movie]]
              rejectEmptyResponse {
                complete(maybeMovie)
              }
            },
            delete {
              val movieDeleted: Future[ActionPerformed] =
                (movieRegistryActor ? DeleteMovie(name)).mapTo[ActionPerformed]
              onSuccess(movieDeleted) { performed =>
                log.info("Deleted movie [{}]: {}", name, performed.description)
                complete((StatusCodes.OK, performed))
              }
            })
        })
    }
}
