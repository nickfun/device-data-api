package gs.nick

import akka.actor.typed.ActorSystem
import akka.actor.typed.scaladsl.Behaviors
import akka.http.scaladsl.Http
import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport._
import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route

import java.util.concurrent.ConcurrentHashMap
import scala.util.{Failure, Success}

//#main-class
object QuickstartApp {
  def main(args: Array[String]): Unit = {
    implicit val system = ActorSystem(Behaviors.empty, "nicks-actor-system")
    val routes = myRoutes()
    startHttpServer(routes)
  }

  def myRoutes(): Route = {
    import gs.nick.JsonFormats._
    val deviceRegistry = new DataRegistry(new ConcurrentHashMap[String, Device]())
    path("healthcheck") {
      get {
        complete(StatusCodes.OK, "server is runner")
      }
    } ~
    path(Segment) { deviceId =>
      get {
        Option(deviceRegistry.registry.get(deviceId)) match {
          case Some(device) => {
            println(device)
            complete(StatusCodes.OK, device.recentReading.get())
          }
          case None => complete(StatusCodes.NotFound, s"$deviceId has not yet sent data")
        }
      } ~
      post {
        entity(as[IncomingPayload]) { payload =>
          deviceRegistry.accept(payload)
          complete(StatusCodes.OK, "Data Accepted")
        }
      }
    }
  }

  //#start-http-server
  def x_main(args: Array[String]): Unit = {
    //#server-bootstrapping
    val rootBehavior = Behaviors.setup[Nothing] { context =>
      val userRegistryActor = context.spawn(UserRegistry(), "UserRegistryActor")
      context.watch(userRegistryActor)

      val routes = new UserRoutes(userRegistryActor)(context.system)
      startHttpServer(routes.userRoutes)(context.system)

      Behaviors.empty
    }
    val system = ActorSystem[Nothing](rootBehavior, "HelloAkkaHttpServer")
    //#server-bootstrapping
  }

  //#start-http-server
  private def startHttpServer(routes: Route)(implicit system: ActorSystem[_]): Unit = {
    // Akka HTTP still needs a classic ActorSystem to start
    import system.executionContext

    val port: Int = Option(System.getenv("PORT")).map(_.toInt).getOrElse(8080)
    val futureBinding = Http().newServerAt("localhost", port).bind(routes)
    futureBinding.onComplete {
      case Success(binding) =>
        val address = binding.localAddress
        system.log.info("Server online at http://{}:{}/", address.getHostString, address.getPort)
      case Failure(ex) =>
        system.log.error("Failed to bind HTTP endpoint, terminating system", ex)
        system.terminate()
    }
  }
}
//#main-class
