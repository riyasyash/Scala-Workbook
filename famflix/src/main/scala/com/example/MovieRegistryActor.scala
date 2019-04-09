package com.example

import akka.actor.{ Actor, ActorLogging, Props }

final case class Movie(name: String, releaseYear: Int, director: String, genre: String)
final case class Movies(movies: Seq[Movie])

object MovieRegistryActor {
  final case class ActionPerformed(description: String)
  final case object GetMovies
  final case class AddMovie(movie: Movie)
  final case class GetMovie(name: String)
  final case class DeleteMovie(name: String)

  def props: Props = Props[MovieRegistryActor]
}

class MovieRegistryActor extends Actor with ActorLogging {
  import MovieRegistryActor._

  var movies = Set.empty[Movie]

  def receive: Receive = {
    case GetMovies =>
      sender() ! Movies(movies.toSeq)
    case AddMovie(movie) =>
      movies += movie
      sender() ! ActionPerformed(s"Movie ${movie.name} created.")
    case GetMovie(name) =>
      sender() ! movies.find(_.name == name)
    case DeleteMovie(name) =>
      movies.find(_.name == name) foreach { user => movies -= user }
      sender() ! ActionPerformed(s"Movie ${name} deleted.")
  }
}
