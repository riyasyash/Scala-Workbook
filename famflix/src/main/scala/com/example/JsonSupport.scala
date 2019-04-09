package com.example

import com.example.MovieRegistryActor.ActionPerformed

import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport
import spray.json.DefaultJsonProtocol

trait JsonSupport extends SprayJsonSupport {
  import DefaultJsonProtocol._

  implicit val movieJsonFormat = jsonFormat4(Movie)
  implicit val moviesJsonFormat = jsonFormat1(Movies)

  implicit val actionPerformedJsonFormat = jsonFormat1(ActionPerformed)
}
