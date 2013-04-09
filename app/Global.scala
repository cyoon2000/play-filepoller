import play.api._
import play.api.mvc._
import play.api.data._
import play.api.libs.concurrent.Akka

import akka.actor._
import akka.util.duration.intToDurationInt

import actors.JavaActor
import actors.JavaActor._

object Global extends GlobalSettings {

  override def onStart(app: Application) {
  	Logger.info("Application has started")

    val system = ActorSystem("MySystem")

    // scala version
    val masterActor = system.actorOf(Props(new MasterActor), name = "masterActor")
    system.scheduler.scheduleOnce(1 seconds, masterActor, Message("start"))

    //val fileMasterActor = system.actorOf(Props(new FileMasterActor), name = "fileMasterActor")
    //system.scheduler.scheduleOnce(5 seconds, fileMasterActor, Message("scanDB"))

    // java version
    //val javaActor = system.actorOf(Props(new JavaActor), name = "javaActorActor")
    //system.scheduler.scheduleOnce(1 seconds, javaActor, "scanDB")


  }




}