package actors

import models.FileEntry
import anorm.{NotAssigned, Id}

import akka.dispatch.Future
import akka.actor.{ActorSystem, Actor, Props, ActorPath, ActorLogging}
import akka.event.Logging
import akka.pattern.{ask, pipe}


// TODO - check migration play 2.1
import play.api.Play
import play.api.Play.current

import org.apache.commons.io.{FilenameUtils, FileUtils, IOUtils}

import java.io.{FileInputStream, File}
import java.security.MessageDigest


object FileUtilities {

  //case class Message(msg: String)

  def getAllFiles(dir : File) : List[File] = {
    var l = List[File]()
    dir.listFiles.foreach(f => {
      if (f.isFile) {
        l = f :: l
      } else {
        l = l ::: getAllFiles(f)
      }
    })

    l
  }


  def getSignature(f: File) : String = {
    md5SumString(IOUtils.toByteArray(new FileInputStream(f)))
  }

  def md5SumString(bytes : Array[Byte]) : String = {
    val md5 = MessageDigest.getInstance("MD5")
    md5.reset()
    md5.update(bytes)
    md5.digest().map(0xFF & _).map { "%02x".format(_) }.foldLeft(""){_ + _}
  }


  def getProperty(key: String) : String = {
      val value = Play.current.configuration.getString(key)
      value match {
        case Some(str) => value.getOrElse(null)
        case None => throw new Exception("No value defined for this property " + key)
      }
  }

}