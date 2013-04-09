import actors.FileUtilities

import akka.actor.{ActorSystem, Actor, Props, ActorLogging, ActorPath, Terminated}
import akka.event.Logging
import akka.routing.RoundRobinRouter
import akka.util.Timeout
import akka.util.duration._

import akka.util.duration._
import akka.util.Timeout

import java.io.File

//import play.api.Play.current
import play.api.Play


case class Message(msg: String)

class MasterActor extends Actor with ActorLogging{

  val rootsStr = FileUtilities.getProperty("filepoller.roots")
  val roots = rootsStr.split(",")
  log.info("Property loaded : filepoller.roots = " + rootsStr)

  // create child actors for each polling rootDir and register
  val router = context.actorOf(Props[FileMasterActor].withRouter(
                                  RoundRobinRouter(nrOfInstances = roots.size)
                               ), name = "router")
  context.watch(router)


  var lastSender = context.system.deadLetters

  override def preStart() {
    log.info("%s is running".format(self.path.name))
  }
  override def postStop() {
    log.info("%s has stopped".format(self.path.name))
  }

  def receive = {

    case Message("start") => startFilePollers()
    case Terminated(`router`) ⇒ lastSender ! "finished"
    case _ => log.error("Got a message I don't understand.")
  }

  def startFilePollers() {

      roots.foreach  { root =>
        val file = new File(root.trim)
        if (file.exists()) {
          if (file.isDirectory()) {
            log.info("Begin polling the directory [" + root + "]......")
            router ! Message(root.trim)
          }
          else log.error ("Oops, this root directory provided is not a directory [" + root + "]")
        }
        else log.error ("Oops, this root directory provided does not exist [" + root + "]")
      }
  }

}


object MasterActorMain extends App {
  val system = ActorSystem("MySystem")
  val masterActor = system.actorOf(Props[MasterActor], name = "master")

  masterActor ! Message("start") // toss a message into our actor with the "!" send op
}