import org.specs2.mutable._

import play.api.db.DB
import play.api.Play.current

import anorm._

import play.api.test._
import play.api.test.Helpers._

class DBEvolutionsTest extends Specification {
 
  "Evolutions" should {
    "be applied without errors" in {
      evolutionFor("default")
      running(FakeApplication()) {
        DB.withConnection {
          implicit connection =>
            SQL("select count(1) from file_entry").execute()
        }
      }
      success
    }
  }
}

