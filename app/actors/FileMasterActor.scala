import models.FileEntry
import actors.FileUtilities
import utils.JavaUtilities

import collection.JavaConversions._

import anorm.{NotAssigned, Id}

import akka.dispatch.Future
import akka.actor.{ActorSystem, Actor, Props, ActorPath, ActorLogging}
import akka.event.Logging
import akka.pattern.{ask, pipe}

import java.io.{FileInputStream, File}
import java.security.MessageDigest
import java.text.SimpleDateFormat
import java.util.Date

import org.apache.commons.io.{FilenameUtils, FileUtils, IOUtils}
import org.joda.time._
import org.joda.time.format._

// TODO - check migration Akka 2.1
import play.api.Play
import play.api.Play.current
import play.api.cache.{EhCachePlugin, Cache}

import scala.collection.mutable.HashSet



class FileMasterActor extends Actor with ActorLogging{

  val pollingInterval: Int = FileUtilities.getProperty("filepoller.interval").toInt
  log.info("Property loaded : filepoller.interval = " + pollingInterval)

  val maxNumWorkers: Int = FileUtilities.getProperty("filepoller.max_num_workers").toInt
  log.info("Property loaded : filepoller.max_num_workers = " + maxNumWorkers)

  val fileSet = new HashSet[String]

  override def preStart() {
    log.info("%s is running".format(self.path.name))
  }
  override def postStop() {
    log.info("%s has stopped".format(self.path.name))
  }

  def receive = {
    case Message(rootDir) => checkDB(rootDir)
    case default => log.error("Got a message I don't understand.")
  }

  def checkDB(rootDir: String) {

    FileEntry.findByRoot(rootDir).foreach(fileEntry => syncWithFiles(fileEntry))

    checkDirectory(rootDir)

    // use java objects
    //checkDirectoryJava(rootDir)

  }

  /**
   * 1. if fileEntry does not exist in file system, delete it from DB
   * 2. if signature changed in file system, delete from DB
   */
  def syncWithFiles(fileEntry: FileEntry) {

    val f = new File(fileEntry.name)
    if (!f.exists()) {
      log.info("This file does not exist any more. Removing from DB... " + fileEntry.name)
      removeFromDB(fileEntry)
    }
    else {
      //if (FileUtilities.getSignature(f) != fileEntry.signature) {
      if (JavaUtilities.getSignature(f) != fileEntry.signature) {
        log.info("This file has been modified. Removing from DB... " + fileEntry.name)
        removeFromDB(fileEntry)
      }
    }
  }


  /**
   * monitor the files -
   * FOR ( all the files in the directory)
   *   EXCLUDE ( the files done monitoring - exists in DB )
   *   EXCLUDE ( the files being monitored - exists in cache )
   */
  def checkDirectoryJava(rootDir: String) {
    //log.info("Checking file system inventory [" + rootDir + "]..............................")
    implicit val system = context.system

    // 1. get list of all files in the polling directory
    // 2. filter the file list - exclude ones already in DB (done monitoring)
    // 3. filter the file list again - exclude ones in cache (being monitored)
    // 4. take first N files (avoid starvation - take first N files in the ascending order by lastModified time)

    // we construct List[Future[File]]
    //val workers = FileUtilities.getAllFiles(dir).filterNot(file => filesInDb.contains(file.getPath)).filter(file => Cache.get(file.getPath) == None) sortBy {file => file.lastModified} take(maxNumWorkers) map { case file =>
    val workers = JavaUtilities.getFilesToPoll(rootDir) take(maxNumWorkers) map { case file =>
      Future(checkFile(file, rootDir))
      }
    val future = Future.sequence(workers)

    future onSuccess {
      case results =>
        checkDB(rootDir)
    }

    future onFailure {
      case failure =>
        log.error("Error!!!" + failure)
        checkDB(rootDir)
    }

    // if there was no files to process, sleep and yield to other processes
    if (workers.size == 0) {
      //log.info("No files in File system to process at this time...")
      Thread.sleep(2000)
    }

  }

  /**
   * monitor the files -
   * FOR ( all the files in the directory)
   *   EXCLUDE ( the files done monitoring - exists in DB )
   *   EXCLUDE ( the files being monitored - exists in cache )
   */
  def checkDirectory(rootDir: String) {
    //log.info("Checking file system inventory [" + rootDir + "]..............................")
    implicit val system = context.system
    val dir = new File(rootDir);

    // 1. get list of all files in the polling directory
    // 2. filter the file list - exclude ones already in DB (done monitoring)
    // 3. filter the file list again - exclude ones in cache (being monitored)
    // 4. take first N files (avoid starvation - take first N files in the ascending order by lastModified time)
    val filesInDb = FileEntry.findByRoot(rootDir).map(fileEntry => fileEntry.name)

    //val files1 =  Utilities.getAllFiles(dir).filterNot(file => filesInDb.contains(file.getPath))
    //val files2 = files1.filter(file => Cache.get(file.getPath) == None)
    //val filesFinal = files2 sortBy {file => file.lastModified} take(maxNumWorkers)
    //filesFinal.foreach( file => log.info(" **** final file list = " + file.getPath))
    // we construct List[Future[File]]
    //val futureList = filesFinal map { case file => Future(checkFile(file)) }

    val workers = FileUtilities.getAllFiles(dir).filterNot(file => filesInDb.contains(file.getPath)).filter(file => Cache.get(file.getPath) == None) sortBy {file => file.lastModified} take(maxNumWorkers) map { case file =>
      Future(checkFile(file, rootDir))
      }
    val future = Future.sequence(workers)

    future onSuccess {
      case results =>
        //results.foreach(println)
        checkDB(rootDir)
    }

    future onFailure {
      case failure =>
        log.error("Error!!!" + failure)
        checkDB(rootDir)
    }

    // if there was no files to process, sleep and yield to other processes
    if (workers.size == 0) {
      //log.info("No files in File system to process at this time...")
      Thread.sleep(2000)
    }

  }

  /**
   * monitor - check the file every x interval
   * 1. add (key : file path) to the cache
   * 2. monitor
   * 3. mark it as done-monitoring (add fileEntry to DB) if the digital signature has not changed for x interval
   * 4. remove from the cache
   */
  def checkFile(f: File, rootDir: String) = {
    // add to the cache and start monitoring
    addToCache(f.getPath)

    try {
      var i = 0
      //var checksum: String = FileUtilities.md5SumString(IOUtils.toByteArray(new FileInputStream(f)))
      var checksum: String = JavaUtilities.getSignature(f)
      var prevChecksum: String = ""
      log.info("......start monitoring file [" + f.getPath + "]")
      do {
        i =  i + 1
        prevChecksum = checksum
        Thread.sleep(pollingInterval)
        //checksum = FileUtilities.md5SumString(IOUtils.toByteArray(new FileInputStream(f)))
        checksum = JavaUtilities.getSignature(f)
      } while ( i < 5 && checksum != prevChecksum)

      // add to DB and remove from the memory
      addToDb(f, checksum, rootDir)

      //TODO send message to MQ

    } finally {
      removeFromCache(f.getPath)
    }

    f
  }


  def addToDb(f: File, signature: String, rootDir: String) {
    log.info("......(+) file ready. adding in DB : " + f + ", signature = " + signature)

    val size = f.length
    if (size > 0) {
      FileEntry.create(FileEntry(NotAssigned, f.getPath, signature, size, new DateTime((f.lastModified)), rootDir))
      //FileEntry.create(FileEntry(anorm.Id(f.getPath), signature, size, new DateTime((f.lastModified)), rootDir)))
    }
  }

  def removeFromDB(fileEntry: FileEntry) {
    log.info("......(-) deleting the record in DB : " + fileEntry)
    if (!FileEntry.delete(fileEntry.id)) log.error("Error deleting the record " + fileEntry)
  }

  def addToCache(name: String) {

    fileSet += name

    // --- EhCache usage example ---
    //if(Cache.get(f.getPath) == None) {
      //log.info("[cache] adding the key " + f.getPath + " to cache")
    //  Cache.set(f.getPath, f)
    //}
  }

  def removeFromCache(name: String) {

    fileSet -= name

    // --- EhCache usage example ---
    //log.info("[cache] removing the key " + key + " from cache")

    // workaround for Cache.remove
    //Play.current.plugin[EhCachePlugin].map {
    //      ehcache =>
    //        ehcache.cache.remove(key)
    //}.getOrElse(false)

    // TODO - later - check if this now works
    //Cache.remove("item.key")
  }

  def waitForNewFile(dir: File) = {
    var newfiles = FileUtilities.getAllFiles(dir)
    do {
          log.info("......no files in the directory. Waiting...")
          Thread.sleep(20000)
          newfiles = FileUtilities.getAllFiles(dir)
    }  while (newfiles.size < 1)
    log.info("......hey... %d new file(s) arrived!!!!".format(newfiles.size) )
    newfiles
  }

}

object FileMasterActor extends App {
  val system = ActorSystem("MySystem")
  val fileMasterActor = system.actorOf(Props[FileMasterActor], name = "fileMasterActor")
  fileMasterActor ! Message("scanFile")
}