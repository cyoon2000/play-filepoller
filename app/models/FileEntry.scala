package models
 
import play.api.db._
import play.api.Play.current

import anorm._
import anorm.SqlParser._

import anorm.NotAssigned
import anorm.Id

//import java.util.Date

import org.joda.time._
import org.joda.time.format._


case class FileEntry(id: Pk[Long], name: String, signature: String, size: Double, lastModified: DateTime, root: String) {
  def this(name: String, signature: String, size: Double, lastModified: DateTime, root: String) {
      this(NotAssigned, name, signature, size: Double, lastModified: DateTime, root: String);
    }
}

object FileEntry {
 
  val parser = {
    get[Pk[Long]]("id") ~
    get[String]("name") ~
    get[String]("signature") ~
    get[Double]("size") ~
    get[DateTime]("last_modified") ~
    get[String]("root") map {
      case id ~ name ~ signature ~ size ~ lastModified ~ root => FileEntry(id, name, signature, size, lastModified, root)
    }
  }

  val dateFormatGeneration: DateTimeFormatter = DateTimeFormat.forPattern("dd-MM-yyyy HH:mm:ss");
  //val dateFormatGeneration: DateTimeFormatter = ISODateTimeFormat.dateTime();

  implicit def rowToDateTime: Column[DateTime] = Column.nonNull {
    (value, meta) =>
      val MetaDataItem(qualified, nullable, clazz) = meta
      value match {
        case ts: java.sql.Timestamp => Right(new DateTime(ts.getTime))
        case d: java.sql.Date => Right(new DateTime(d.getTime))
        case str: java.lang.String => Right(dateFormatGeneration.parseDateTime(str))
        case _ => Left(TypeDoesNotMatch("Cannot convert " + value + ":" + value.asInstanceOf[AnyRef].getClass) )
      }
  }

  implicit val dateTimeToStatement = new ToStatement[DateTime] {
    def set(s: java.sql.PreparedStatement, index: Int, aValue: DateTime): Unit = {
        s.setTimestamp(index, new java.sql.Timestamp(aValue.withMillisOfSecond(0).getMillis()) )
    }
  }

  def findAll(): Seq[FileEntry] = {
    DB.withConnection { implicit connection =>
      SQL("select * from file_entry").as(FileEntry.parser *)
    }
  }

  def findByRoot(root: String): Seq[FileEntry] = {
    DB.withConnection { implicit connection =>
	  SQL("select * from file_entry where root = {root}").on("root" -> root).as(FileEntry.parser *)
	}
  }

  /**
   * return true if exist, false otherwise
   */
  //def findByName(name: String): Option[FileEntry] = {
  def findByName(name: String): Boolean = {
    DB.withConnection { implicit connection =>
	  SQL("select * from file_entry where name = {name}").on("name" -> name).using(FileEntry.parser).singleOpt().isEmpty
	}
  }

  //def create(fileEntry: FileEntry): Unit = {
  def create(fileEntry: FileEntry): Boolean = {
    DB.withConnection { implicit connection =>
      SQL("insert into file_entry(name, signature, size, last_modified, root) values ({name}, {signature}, {size}, {last_modified}, {root})").on(
        'name -> fileEntry.name,
        'signature -> fileEntry.signature,
        'size -> fileEntry.size,
        'last_modified -> fileEntry.lastModified,
        'root -> fileEntry.root
      ).executeUpdate() == 1
    }
  }

  def delete(id: Pk[Long]): Boolean = {
    DB.withConnection { implicit connection =>
      //SQL("delete from file_entry where id = {id}").on("id" -> id.get).using(FileEntry.parser).single().executeUpdate()
      SQL("delete from file_entry where id = {id}").on("id" -> id.get).executeUpdate() == 1
      //SQL("delete from file_entry where id = 9").executeUpdate() == 1
    }

  }

  def deleteJava(fileEntry: FileEntry): Boolean = {
    DB.withConnection { implicit connection =>
      SQL("delete from file_entry where i = {id}").on("id" -> fileEntry.id).executeUpdate() == 1
      //SQL("delete from file_entry where id = 9").executeUpdate() == 1
    }

  }

}



