package controllers

//import play.api._
//import play.api.mvc._

import models.FileEntry

import play.api.data.Form
import play.api.data.Forms.{single, nonEmptyText}
import play.api.mvc.{Action, Controller}
import anorm.NotAssigned

import org.joda.time._
import org.joda.time.format._

import com.codahale.jerkson.Json

object Application extends Controller {
  
  
  val fileEntryForm = Form(
    single("name" -> nonEmptyText)
  )  
  
//  def index = Action {
//    Ok(views.html.index("Your new application is ready."))
//  }
   
  def index = Action {
    Ok(views.html.index(fileEntryForm))
  }

  // TODO - remove this. temporary hard-coded value
  def addFileEntry() = Action { implicit request =>
    fileEntryForm.bindFromRequest.fold(
      errors => BadRequest,
      {
        case (name) =>
          FileEntry.create(FileEntry(NotAssigned, name, "11223344", 26555.toDouble, new DateTime(new java.util.Date(System.currentTimeMillis())),  "/www/a/data/test/filepoller"))
          Redirect(routes.Application.index())
      }
    )
  }

  def listFileEntries() = Action {
    val fileEntries = FileEntry.findAll()

    val json = Json.generate(fileEntries)

    Ok(json).as("application/json")
  }
  
}