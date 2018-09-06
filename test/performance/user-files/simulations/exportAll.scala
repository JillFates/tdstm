
import scala.concurrent.duration._

import io.gatling.core.Predef._
import io.gatling.http.Predef._
import io.gatling.jdbc.Predef._
import Headers._

class ExportAll extends Simulation {

    val baseURL = System.getenv("BASE_URL")

    val uri1 = baseURL.concat("/tdstm")

    object Login {

        val login = 
            exec(http("ExportAll Login")
            .get("/tdstm/auth/login")
            .headers(headers_0)
            .check(status.is(200))
            .check(regex("""<title>Login</title>""").exists)
            .resources(
                http("main.css")
                    .get(uri1 + "/css/main.css")
                    .headers(headers_1)
                    .check(status.is(200)),
                http("tds.css")
                    .get(uri1 + "/css/tds.css")
                    .headers(headers_1)
                    .check(status.is(200)),
                http("application.js")
                    .get(uri1 + "/js/application.js")
                    .check(status.is(200)),
                http("build.txt")
                    .get(uri1 + "/build.txt")
                    .headers(headers_0)
                    .check(status.is(404)),
                http("build.txt?GSdm")
                    .get(uri1 + "/build.txt?GSdm")
                    .headers(headers_0)
                    .check(status.is(404)),
                http("TMLoginLogo.gif")
                    .get(uri1 + "/images/TMLoginLogo.gif")
                    .headers(headers_6)
                    .check(status.is(200)),
                http("button_highlight.png")
                    .get(uri1 + "/images/button_highlight.png")
                    .headers(headers_6)
                    .check(status.is(200)))
            )

        val signIn =
            pause(7)
            .feed(csv("user_tds.csv").circular)
            .exec(http("signIn")
                .post("/tdstm/auth/signIn")
                .headers(headers_0)
                .formParam("targetUri", "")
                .formParam("username", "${username}")
                .formParam("password", "${password}")
                .resources(
                    http("css select2.js")
                        .get(uri1 + "/css/select2.css")
                        .headers(headers_1)
                        .check(status.is(200)),
                    http("js select2.js")
                        .get(uri1 + "/js/select2.js")
                        .check(status.is(200)),
                    http("tds-comon.js")
                        .get(uri1 + "/js/tds-common.js")
                        .check(status.is(200)),
                    http("entity.crud.js")
                        .get(uri1 + "/js/entity.crud.js")
                        .check(status.is(200)),
                    http("bootstrap.css")
                        .get(uri1 + "/css/bootstrap.css")
                        .headers(headers_1)
                        .check(status.is(200)),
                    http("iconApp.png")
                        .get(uri1 + "/images/iconApp.png")
                        .headers(headers_6)
                        .check(status.is(200)),
                    http("iconServer.png")
                        .get(uri1 + "/images/iconServer.png")
                        .headers(headers_6)
                        .check(status.is(200)),
                    http("iconDB.png")
                        .get(uri1 + "/images/iconDB.png")
                        .headers(headers_6)
                        .check(status.is(200)),
                    http("iconNetwork.png")
                        .get(uri1 + "/images/iconNetwork.png")
                        .headers(headers_6)
                        .check(status.is(200)),
                    http("tdsmenu.js")
                        .get(uri1 + "/js/tdsmenu.js")
                        .check(status.is(200)),
                    http("iconStorage.png")
                        .get(uri1 + "/images/iconStorage.png")
                        .headers(headers_6)
                        .check(status.is(200)),
                    http("checked-icon.png")
                        .get(uri1 + "/images/checked-icon.png")
                        .headers(headers_6)
                        .check(status.is(200)),
                    http("project show image 47")
                        .get(uri1 + "/project/showImage/47")
                        .headers(headers_6),
                    http("news_bg.jpg")
                        .get(uri1 + "/images/news_bg.jpg")
                        .headers(headers_6)
                        .check(status.is(200)),
                    http("updateLastPageLoad")
                        .post(uri1 + "/userLogin/updateLastPageLoad")
                        .headers(headers_23)
                        .formParam("url", "/tdstm/moveBundle/planningStats")))
    }

    object Assets {
        val export = 
            pause(5)
            .exec(http("exportAssets")
            .get("/tdstm/assetEntity/exportAssets")
            .headers(headers_0)
            .resources(
                http("progressbar.css")
                    .get(uri1 + "/css/progressbar.css")
                    .headers(headers_1),
                http("import.export.js")
                    .get(uri1 + "/js/import.export.js"),
                http("progressBar.js")
                    .get(uri1 + "/js/progressBar.js"),
                http("core.js")
                    .get(uri1 + "/components/core/core.js")
                    .check(status.is(200)),
                http("showImage 47")
                    .get(uri1 + "/project/showImage/47")
                    .headers(headers_6),
                http("updateLastPageLoad")
                    .post(uri1 + "/userLogin/updateLastPageLoad")
                    .headers(headers_23)
                    .formParam("url", "/tdstm/assetEntity/exportAssets"))
            )
            .pause(2)
            .exec(http("ImportApplication")
                .get("/tdstm/assetEntity/setImportPreferences?value=true&preference=ImportApplication")
                .headers(headers_31))
                .pause(1)
            .exec(http("ImportServer")
                .get("/tdstm/assetEntity/setImportPreferences?value=true&preference=ImportServer")
                .headers(headers_31))
            .pause(1)
            .exec(http("ImportDatabase")
                .get("/tdstm/assetEntity/setImportPreferences?value=true&preference=ImportDatabase")
                .headers(headers_31))
            .pause(1)
            .exec(http("ImportStorage")
                .get("/tdstm/assetEntity/setImportPreferences?value=true&preference=ImportStorage")
                .headers(headers_31))
            .pause(1)
            .exec(http("ImportRoom")
                .get("/tdstm/assetEntity/setImportPreferences?value=true&preference=ImportRoom")
                .headers(headers_31))
            .pause(1)
                .exec(http("ImportRack")
                .get("/tdstm/assetEntity/setImportPreferences?value=true&preference=ImportRack")
                .headers(headers_31)
    .resources(
        http("ImportDependency")
        .get(uri1 + "/assetEntity/setImportPreferences?value=true&preference=ImportDependency")
        .headers(headers_31)))
    .pause(1)
    .exec(http("ImportCabling")
        .get("/tdstm/assetEntity/setImportPreferences?value=true&preference=ImportCabling")
        .headers(headers_31))
    .pause(1)
    .exec(http("ImportComment")
        .get("/tdstm/assetEntity/setImportPreferences?value=true&preference=ImportComment")
        .headers(headers_31))
    .pause(1)
    .exec(http("exportForm")
        .post("/tdstm/assetEntity/export/exportForm")
        .headers(headers_40)
        .formParam("projectIdExport", "2445")
        .formParam("dataTransferSet", "1")
        .formParam("bundle", "")
        .formParam("application", "application")
        .formParam("asset", "asset")
        .formParam("database", "database")
        .formParam("files", "files")
        .formParam("room", "room")
        .formParam("rack", "rack")
        .formParam("dependency", "dependency")
        .formParam("cabling", "cable")
        .formParam("comment", "comment")
        .check(status.is(200))
        .check(jsonPath("$.data.key").exists.saveAs("expVal"))   
    )
    .asLongAs(session => !session.contains("progressStatus") || session("progressStatus").as[String].startsWith("In")) {
        pause(5)
        .exec(http("AssetExport1")
        .get("""/tdstm/ws/progress/${expVal}?_=1422547852624""")
        .headers(headers_31)
        .check(jsonPath("$.data.status").exists.saveAs("progressStatus"))
        )
    }
    .exec(http("downloadExport")
      .get("""/tdstm/assetEntity/downloadExport?key=${expVal}""")
      .headers(headers_0)
      .check(status.is(200)
        )

      )
    }
 
    object SingOut {
        val singOut = 
            pause(43)
            .exec(http("SingOut")
                .get("/tdstm/auth/signOut")
                .headers(headers_0)
                .resources(
                    http("build.txt")
                        .get(uri1 + "/build.txt")
                        .headers(headers_0)
                        .check(status.is(404)),
                    http("build.txt?y00E")
                        .get(uri1 + "/build.txt?yOOE")
                        .headers(headers_0)
                        .check(status.is(404))
                )
            .check(status.is(200))
            )
    }
    
  val httpProtocol = http
    .baseURL(baseURL)
    .inferHtmlResources()
    .acceptHeader("*/*")
    .acceptEncodingHeader("gzip, deflate")
    .acceptLanguageHeader("en-US,en;q=0.5")
    .connection("keep-alive")
    .contentTypeHeader("application/x-www-form-urlencoded; charset=UTF-8")
    .userAgentHeader("Mozilla/5.0 (X11; Ubuntu; Linux x86_64; rv:35.0) Gecko/20100101 Firefox/35.0")
  
    val exportAll = scenario("ExportAll").exec(Login.login,Login.signIn,Assets.export,SingOut.singOut)
    setUp(
        exportAll.inject(rampUsers(2) over (5 seconds))
    ).protocols(httpProtocol)
}