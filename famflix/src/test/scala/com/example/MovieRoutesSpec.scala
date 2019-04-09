package com.example

import akka.actor.ActorRef
import akka.http.scaladsl.marshalling.Marshal
import akka.http.scaladsl.model._
import akka.http.scaladsl.testkit.ScalatestRouteTest
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.{ Matchers, WordSpec }

class MovieRoutesSpec extends WordSpec with Matchers with ScalaFutures with ScalatestRouteTest
  with MovieRoutes {
  override val movieRegistryActor: ActorRef =
    system.actorOf(MovieRegistryActor.props, "movieRegistry")

  lazy val routes = movieRoutes

  "MovieRoutes" should {
    "return no users if no present (GET /movies)" in {
      val request = HttpRequest(uri = "/movies")

      request ~> routes ~> check {
        status should ===(StatusCodes.OK)

        contentType should ===(ContentTypes.`application/json`)

        entityAs[String] should ===("""{"movies":[]}""")
      }
    }

    "be able to add users (POST /movies)" in {
      val movie = Movie("Titanic", 1994, "JC", "Romance")
      val movieEntity = Marshal(movie).to[MessageEntity].futureValue // futureValue is from ScalaFutures

      val request = Post("/movies").withEntity(movieEntity)

      request ~> routes ~> check {
        status should ===(StatusCodes.Created)

        contentType should ===(ContentTypes.`application/json`)

        entityAs[String] should ===("""{"description":"Movie Titanic created."}""")
      }
    }

    "be able to remove users (DELETE /movies)" in {
      val request = Delete(uri = "/movies/Titanic")

      request ~> routes ~> check {
        status should ===(StatusCodes.OK)

        contentType should ===(ContentTypes.`application/json`)

        entityAs[String] should ===("""{"description":"Movie Titanic deleted."}""")
      }
    }
  }

}
