package assetLists

import com.excilys.ebi.gatling.core.Predef._
import com.excilys.ebi.gatling.http.Predef._
import akka.util.duration._
import assertions._
import Headers._

object GetListStorages {

  val scn = scenario("Get List Storage")
    
    .exec(http("storage login")
          .get("/tdstm/auth/login")
          .check(status.is(200))
          .check(regex("""<title>Login</title>"""))
      )
    .pause(7)  
    .feed(csv("user_tds.csv"))
    .exec(http("storage signIn")
          .post("/tdstm/auth/signIn")
          .headers(headers_3)
            .param("""targetUri""", """""")
            .param("""username""", "${username}")
            .param("""password""", "${password}")
            .check(status.is(200))
      )
    .pause(1)
    .exec(http("storage userLogin updateLastPageLoad")
          .post("/tdstm/userLogin/updateLastPageLoad")
          .headers(headers_4)
          .param("""url""", """/tdstm/project/show""")
          .check(status.is(200))  
          .check(bodyString.is("SUCCESS"))
      )
    .pause(4)
    .exec(http("storage files index")
          .get("/tdstm/files/index")
          .check(status.is(200))
          .check(regex("""<title>Storage List</title>"""))
      )
    .pause(2)
    .exec(http("storage userLogin updateLastPageLoad_2")
          .post("/tdstm/userLogin/updateLastPageLoad")
          .headers(headers_4)
          .param("""url""", """/tdstm/files/list""")
          .check(status.is(200))  
          .check(bodyString.is("SUCCESS"))
      )
    .pause(81 milliseconds)
    .exec(http("storage files listJson")
          .get("/tdstm/files/listJson")
          .headers(headers_7)
          .queryParam("""assetName""", """""")
          .queryParam("""validation""", """""")
          .queryParam("""moveBundle""", """""")
          .queryParam("""sidx""", """assetName""")
          .queryParam("""rows""", """100""")
          .queryParam("""size""", """""")
          .queryParam("""sord""", """asc""")
          .queryParam("""moveBundleId""", """""")
          .queryParam("""plannedStatus""", """""")
          .queryParam("""page""", """1""")
          .queryParam("""event""", """""")
          .queryParam("""nd""", """1399986237476""")
          .queryParam("""planStatus""", """""")
          .queryParam("""filter""", """""")
          .queryParam("""fileFormat""", """""")
          .queryParam("""toValidate""", """""")
          .queryParam("""_search""", """false""")
          .check(status.is(200))
      )
    .pause(4)
    .exec(http("storage files show")
          .post("/tdstm/files/show")
          .headers(headers_4)
          .queryParam("""id""", """122153""")
          .queryParam("""redirectTo""", """files""")
          .check(status.is(200))
      )
    .pause(485 milliseconds)
    .exec(http("storage getTooltips")
          .post("/tdstm/common/getTooltips")
          .headers(headers_9)
          .param("""type""", """Storage""")
          .check(status.is(200))
    )
    .pause(4)
    .exec(http("storage signOut")
          .get("/tdstm/auth/signOut")
          .check(status.is(200))
          .check(regex("""<title>Login</title>"""))
      )
}