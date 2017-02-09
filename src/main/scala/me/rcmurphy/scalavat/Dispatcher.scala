package me.rcmurphy.scalavat

import akka.actor.{ActorSystem, Props}

import scala.concurrent.Await
import scala.concurrent.duration._
import scala.concurrent.ExecutionContext.Implicits.global
object Dispatcher {

  def main(args: Array[String]): Unit = {
    val system = ActorSystem("scala-vat")

    val loginHandlerActor = system.actorOf(Props(classOf[LoginHandlerActor]))
    val fetcherActor = system.actorOf(Props(classOf[FetcherActor], loginHandlerActor))

    system.scheduler.schedule(0.seconds, 2.minutes, fetcherActor, Fetch)


    Await.result(system.whenTerminated, Duration.Inf)
  }
}
