package assetLists

import io.gatling.core.Predef._
import io.gatling.http.Predef._
import scala.concurrent.duration._
import Headers._

object GetListStorages {

  val scn = scenario("Get List Storage")
    
    .exec(http("storage login")
        .get("/tdstm/auth/login")
        .check(status.is(200))
        .check(regex("<title>Login</title>"))
    )
    .pause(7)  
    .feed(csv("user_tds.csv").circular)
    .exec(http("storage signIn")
        .post("/tdstm/auth/signIn")
        .headers(headers_3)
        .formParam("targetUri", "")
        .formParam("username", "${username}")
        .formParam("password", "${password}")
        .check(status.is(200))
      )
    .pause(1)
    .exec(http("storage userLogin updateLastPageLoad")
        .post("/tdstm/userLogin/updateLastPageLoad")
        .headers(headers_4)
        .formParam("url", "/tdstm/project/show")
        .check(status.is(200))  
        .check(bodyString.is("SUCCESS"))
    )
    .pause(4)
    .exec(http("storage files index")
        .get("/tdstm/files/index")
        .check(status.is(200))
        .check(regex("<title>Storage List</title>"))
    )
    .pause(2)
    .exec(http("storage userLogin updateLastPageLoad_2")
        .post("/tdstm/userLogin/updateLastPageLoad")
        .headers(headers_4)
        .formParam("url", "/tdstm/files/list")
        .check(status.is(200))  
        .check(bodyString.is("SUCCESS"))
    )
    .pause(81 milliseconds)
    .exec(http("storage files listJson")
        .get("/tdstm/files/listJson")
        .headers(headers_7)
        .queryParam("assetName", "")
        .queryParam("validation", "")
        .queryParam("moveBundle", "")
        .queryParam("sidx", "assetName")
        .queryParam("rows", "100")
        .queryParam("size", "")
        .queryParam("sord", "asc")
        .queryParam("moveBundleId", "")
        .queryParam("plannedStatus", "")
        .queryParam("page", "1")
        .queryParam("event", "")
        .queryParam("nd", "1399986237476")
        .queryParam("planStatus", "")
        .queryParam("filter", "")
        .queryParam("fileFormat", "")
        .queryParam("toValidate", "")
        .queryParam("_search", "false")
        .check(status.is(200))
    )
    .pause(4)
    .exec(http("storage files show")
        .post("/tdstm/files/show")
        .headers(headers_4)
        .queryParam("id", "122153")
        .queryParam("redirectTo", "files")
        .check(status.is(200))
    )
    .pause(485 milliseconds)
    .exec(http("storage retrieveTooltips")
        .post("/tdstm/common/retrieveTooltips")
        .headers(headers_9)
        .formParam("type", "Storage")
        .check(status.is(200))
    )
    .pause(4)
    .exec(http("storage signOut")
        .get("/tdstm/auth/signOut")
        .check(status.is(200))
        .check(regex("<title>Login</title>"))
    )
}