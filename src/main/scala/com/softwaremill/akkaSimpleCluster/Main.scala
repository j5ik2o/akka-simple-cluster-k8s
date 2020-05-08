package com.softwaremill.akkaSimpleCluster

import akka.actor.ActorSystem
import akka.actor.typed.scaladsl.Behaviors
import akka.actor.typed.scaladsl.adapter._
import akka.actor.typed.{ActorRef, Behavior}
import akka.cluster.Cluster
import akka.cluster.sharding.typed.ShardingEnvelope
import akka.cluster.sharding.typed.scaladsl.{ClusterSharding, Entity, EntityTypeKey}
import akka.http.scaladsl.Http
import akka.http.scaladsl.model.{ContentTypes, HttpEntity, HttpResponse}
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import akka.management.cluster.bootstrap.ClusterBootstrap
import akka.management.scaladsl.AkkaManagement
import akka.stream.ActorMaterializer
import com.softwaremill.akkaSimpleCluster.Counter.{GetValue, Increment}
import com.typesafe.config.ConfigFactory

import scala.concurrent.duration._
import scala.util.{Failure, Success}

object Counter {

  sealed trait Command

  case object Increment extends Command

  final case class GetValue(replyTo: ActorRef[Int]) extends Command

  def apply(entityId: String): Behavior[Command] = {
    def updated(value: Int): Behavior[Command] = {
      Behaviors.receiveMessage[Command] {
        case Increment =>
          updated(value + 1)
        case GetValue(replyTo) =>
          replyTo ! value
          Behaviors.same
      }
    }

    updated(0)

  }
}


object Main extends App {

  lazy val config = ConfigFactory.load()
  implicit val system = ActorSystem("akka-simple-cluster")
  implicit val materializer = ActorMaterializer()
  implicit val executionContext = system.dispatcher
  implicit val cluster = Cluster(system)

  AkkaManagement(system).start()
  ClusterBootstrap(system).start()

  val sharding = ClusterSharding(system.toTyped)

  val TypeKey = EntityTypeKey[Counter.Command]("Counter")

  val shardRegion: ActorRef[ShardingEnvelope[Counter.Command]] =
    sharding.init(Entity(TypeKey, createBehavior = entityContext => Counter(entityContext.entityId)))


  def index(): Route = complete(
    HttpResponse(
      entity = HttpEntity(
        ContentTypes.`text/html(UTF-8)`,
        s"""<span>Wellcome to Sagrada Write API</span>
           |<span>${config.getString("application.api.hello-message")}</span>
           |""".stripMargin
      )
    )
  )

  val route = pathEndOrSingleSlash {
    get {
      index()
    }
  } ~ path("counter" / Segment) { counterName =>
    post {
      val entityRef = sharding.entityRefFor(TypeKey, counterName)
      entityRef ! Increment
      complete("ok")
    } ~ get {
      import akka.util.Timeout
      implicit val to = Timeout(3 seconds)
      val entityRef = sharding.entityRefFor(TypeKey, counterName)
      val future = entityRef.ask[Int](ref => GetValue(ref))
      onComplete(future) {
        case Success(v) => complete(v.toString)
        case Failure(ex) => complete(s"failed to get value: $ex")
      }
    }
  }

  val host = config.getString("application.api.host")
  val port = config.getInt("application.api.port")
  Http().bindAndHandle(route, host, port)

  Cluster(system).registerOnMemberUp {
    system.log.info("Cluster member is up!")
  }

}
