
import scala.concurrent.duration._

import io.gatling.core.Predef._
import io.gatling.http.Predef._
import io.gatling.jdbc.Predef._

class GenerateTasks extends Simulation {
 
	val headers_0 = Map("Accept" -> "text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8")

	val headers_1 = Map("Accept" -> "text/css,*/*;q=0.1")

	val headers_7 = Map("Accept" -> "*/*")

	val headers_10 = Map("Accept" -> "image/png,image/*;q=0.8,*/*;q=0.5")

	val headers_11 = Map(
		"Accept" -> "text/javascript, text/html, application/xml, text/xml, */*",
		"Pragma" -> "no-cache",
		"X-Prototype-Version" -> "1.6.0",
		"X-Requested-With" -> "XMLHttpRequest")

	val headers_12 = Map(
		"Accept" -> "*/*",
		"Pragma" -> "no-cache",
		"X-Requested-With" -> "XMLHttpRequest")

	val headers_56 = Map("Pragma" -> "no-cache")

	val headers_111 = Map(
		"Accept" -> "application/font-woff;q=0.9,*/*;q=0.8",
		"Accept-Encoding" -> "identity")

  val baseURL = System.getenv("BASE_URL")

  val uri1 = baseURL.concat("/tdstm")

  object Login {      
      
    val login = exec(
      http("App Login")
        .get("/tdstm/auth/login")
        .headers(headers_0)
        .check(status.is(200))
        .check(regex("""<title>Login</title>"""))
        .resources(
          http("tds.css")
            .get(uri1 + "/css/tds.css")
            .headers(headers_1)
        // .check(status.is(304)),
            .check(status.is(200)),
          http("main.css")
            .get(uri1 + "/css/main.css")
            .headers(headers_1)
            .check(status.is(200)),
      // .check(status.is(304)),
          http("build.txt")
            .get(uri1 + "/build.txt")
            .headers(headers_0)
            .check(status.is(404)),
          http("build.txt?31JA")
            .get(uri1 + "/build.txt?31JA")
            .headers(headers_0)
            .check(status.is(404))
          )
        )
      .pause(13)
      .feed(csv("user_tds.csv").circular)
      .exec(http("SingIn")
        .post("/tdstm/auth/signIn")
        .headers(headers_0)
        .formParam("targetUri", "")
        .formParam("username", "${username}")
        .formParam("password", "${password}")
        .resources(
          http("select2.css")
            .get(uri1 + "/css/select2.css")
            .headers(headers_1)
            .check(status.is(200)),
          http("select2.css")
            .get(uri1 + "/js/select2.js")
            .headers(headers_7)
            .check(status.is(200)),
          http("tds-common.js")
            .get(uri1 + "/js/tds-common.js")
            .headers(headers_7)
            .check(status.is(200)),
          http("entity.crud.js")
            .get(uri1 + "/js/entity.crud.js")
            .headers(headers_7)
            .check(status.is(200)),
          http("projectShowImage")
            .get(uri1 + "/project/showImage/47")
            .headers(headers_10),
          http("updateLastPageLoad")
            .post(uri1 + "/userLogin/updateLastPageLoad")
            .headers(headers_11)
            .formParam("url", "/tdstm/moveBundle/planningStats")
            .check(status.is(200))  
            .check(bodyString.is("SUCCESS"))
        )
      )
    .pause(10)  
      
  }

  object Cookbook {
 
    val cookbook =  exec(http("getTaskCount -when open task menu")
        .post("/tdstm/clientTeams/getToDoCount")
        .headers(headers_12))
        .pause(1)
        .exec(http("cookbook index")
            .get("/tdstm/cookbook/index")
            .headers(headers_0)
            .resources(
                http("tds-bootstrap.css")
                    .get(uri1 + "/css/tds-bootstrap.css")
                    .headers(headers_1),
                http("ng-grid.css")
                    .get(uri1 + "/css/ng-grid.css")
                    .headers(headers_1),
                http("mergely.css")
                    .get(uri1 + "/css/mergely/mergely.css")
                    .headers(headers_1),
                http("cookbook.css")
                    .get(uri1 + "/css/cookbook.css")
                    .headers(headers_1),
                http("codemirror.css")
                    .get(uri1 + "/css/codemirror/codemirror.css")
                    .headers(headers_1),
                http("codemirrorAddonShow-hint.css")
                    .get(uri1 + "/css/codemirror/addon/show-hint.css")
                    .headers(headers_1),
                http("addonDialog.css")
                    .get(uri1 + "/css/codemirror/addon/dialog.css")
                    .headers(headers_1),
                http("ui-codemirror.js")
                    .get(uri1 + "/js/codemirror/ui-codemirror.js")
                    .headers(headers_7),
                http("codemirrorCodemirror.js")
                    .get(uri1 + "/js/codemirror/codemirror.js")
                    .headers(headers_7),
                http("addonDialog.js2")
                    .get(uri1 + "/js/codemirror/addon/dialog.js")
                    .headers(headers_7),
                http("codemirrorAddonSearch")
                    .get(uri1 + "/js/codemirror/addon/search.js")
                    .headers(headers_7),
                http("codemirrorAddonShow-hint.js")
                    .get(uri1 + "/js/codemirror/addon/show-hint.js")
                    .headers(headers_7),
                http("codemirrorAddonJavascript-hint.js")
                    .get(uri1 + "/js/codemirror/addon/javascript-hint.js")
                    .headers(headers_7),
                http("codemirrorAddonJavascript.js")
                    .get(uri1 + "/js/codemirror/javascript.js")
                    .headers(headers_7),
                http("boostrap.js")
                    .get(uri1 + "/js/bootstrap.js")
                    .headers(headers_7),
                http("mergelyMergely.js")
                    .get(uri1 + "/js/mergely/mergely.js")
                    .headers(headers_7),
                http("controllersCookbook.js")
                    .get(uri1 + "/js/controllers/cookbook.js")
                    .headers(headers_7),
                http("angular-ui-router.min.js")
                    .get(uri1 + "/js/angular/plugins/angular-ui-router.min.js")
                    .headers(headers_7),
                http("core.js")
                    .get(uri1 + "/components/core/core.js")
                    .headers(headers_7)
                    .check(status.is(200)),
                http("commentComment.js")
                    .get(uri1 + "/components/comment/comment.js")
                    .headers(headers_7),
                http("assetTranman.js")
                    .get(uri1 + "/js/asset.tranman.js")
                    .headers(headers_7)
                    .check(status.is(200)),
                http("moment-timezone-with-data-2010-2020.js")
                    .get(uri1 + "/js/moment-timezone-with-data-2010-2020.js")
                    .headers(headers_7),
                http("asset.comment.js")
                    .get(uri1 + "/js/asset.comment.js")
                    .headers(headers_7)
                    .check(status.is(200)),
                http("ProjectshowImage47")
                    .get(uri1 + "/project/showImage/47")
                    .headers(headers_10),
                http("loading-indicator.html")
                    .get(uri1 + "/components/core/loading-indicator.html"),
                http("recipes-template.html")
                    .get(uri1 + "/components/cookbook/recipes-template.html"),
                http("updateLastPageLoad")
                    .post(uri1 + "/userLogin/updateLastPageLoad")
                    .headers(headers_11)
                    .formParam("url", "/tdstm/cookbook/index"),
                http("script_add.png")
                    .get(uri1 + "/icons/script_add.png")
                    .headers(headers_10),
                http("listRecipe")
                    .get(uri1 + "/ws/cookbook/recipe/list?archived=n&context=All&rand=r81FU2oEjn0l2RM"),
                http("delete.png")
                    .get(uri1 + "/icons/delete.png")
                    .headers(headers_10),
                http("script_edit.png")
                    .get(uri1 + "/icons/script_edit.png")
                    .headers(headers_10),
                http("arrow_undo.png")
                    .get(uri1 + "/icons/arrow_undo.png")
                    .headers(headers_10),
                http("recipe-detail-template.htnl")
                    .get(uri1 + "/components/cookbook/recipe-detail-template.html"),
                http("folder.png")
                    .get(uri1 + "/icons/folder.png")
                    .headers(headers_10),
                http("folder_go.png")
                    .get(uri1 + "/icons/folder_go.png")
                    .headers(headers_10),
                http("taskGenerationTemplate.html")
                    .get(uri1 + "/components/cookbook/generation/task-generation-template.html"),
                http("cookbookrecipe")
                    .get(uri1 + "/ws/cookbook/recipe/28?rand=mRXOLBQF7M9VUxZ"),
                http("task-generation-start-template")
                    .get(uri1 + "/components/cookbook/generation/task-generation-start-template.html"),
                http("tds.ico")
                    .get(uri1 + "/images/tds.ico")
                    .headers(headers_0)
                    .check(status.is(404)),
                http("listEventsAndBundles")
                    .get(uri1 + "/ws/event/listEventsAndBundles?rand=4wfnvv8YnOi3lnX"),
                http("table_gear.png")
                    .get(uri1 + "/icons/table_gear.png")
                    .headers(headers_10),
                http("findTaskBatchByRecipeAndContext")
                    .get(uri1 + "/ws/task/findTaskBatchByRecipeAndContext?contextId=348&logs=false&rand=cSom3KpaKX02hV8&recipeId=28")
            )
        )
        .pause(9)
        .exec(http("generateTasks")
            .post("/tdstm/ws/task/generateTasks?rand=")
            .headers(headers_56)
            .formParam("contextId", "348")
            .formParam("recipeId", "28")
            .formParam("recipeVersionId", "")
            .formParam("useWIP", "false")
            .formParam("autoPublish", "false")
            .formParam("deletePrevious", "true")
            .resources(http("task-generation-progress-template")
            .get(uri1 + "/components/cookbook/generation/task-generation-progress-template.html")))
        .pause(1)
            .exec(http("progressTaskBatch1")
                .get("/tdstm/ws/progress/TaskBatch-1789?rand=LEQoyVkWyhowmHU")
            .resources(
                http("progreesTaskBatch2")
                    .get(uri1 + "/ws/progress/TaskBatch-1789?rand=WVmGW5nMNRe4RDF"),
                http("progreesTaskBatch3")
                    .get(uri1 + "/ws/progress/TaskBatch-1789?rand=ShRTHm49dh6ISso"),
                http("progreesTaskBatch4")
                    .get(uri1 + "/ws/progress/TaskBatch-1789?rand=kj1ldVZUJijbelz"),
                http("progreesTaskBatch5")
                    .get(uri1 + "/ws/progress/TaskBatch-1789?rand=yvanc5zakMXLKbA"),
                http("progreesTaskBatch6")
                    .get(uri1 + "/ws/progress/TaskBatch-1789?rand=hnVKNzq7XJ8Dlza"),
                http("progreesTaskBatch7")
                    .get(uri1 + "/ws/progress/TaskBatch-1789?rand=KH28thYkZ7dczvt"),
                http("progreesTaskBatch8")
                    .get(uri1 + "/ws/progress/TaskBatch-1789?rand=DD463PXXTEtIxQu"),
                http("progreesTaskBatch9")
                    .get(uri1 + "/ws/progress/TaskBatch-1789?rand=RoKeHQBI4VmcbRJ"),
                http("progreesTaskBatch10")
                    .get(uri1 + "/ws/progress/TaskBatch-1789?rand=1V0kW19ZQWUJXN4"),
                http("progreesTaskBatch11")
                    .get(uri1 + "/ws/progress/TaskBatch-1789?rand=lqtuR9NIPMPUVEW"),
                http("progreesTaskBatch12")
                    .get(uri1 + "/ws/progress/TaskBatch-1789?rand=2w6gYMeatHGDioK"),
                http("progreesTaskBatch13")
                    .get(uri1 + "/ws/progress/TaskBatch-1789?rand=Qx5Nl30jlVF3j0L"),
                http("progreesTaskBatch14")
                    .get(uri1 + "/ws/progress/TaskBatch-1789?rand=nJw7c9fdLe6cIWv"),
                http("progreesTaskBatch15")
                    .get(uri1 + "/ws/progress/TaskBatch-1789?rand=i4QHTdZoqdkawZj"),
                http("progreesTaskBatch16")
                    .get(uri1 + "/ws/progress/TaskBatch-1789?rand=fSp7BiFk75SArDV"),
                http("progreesTaskBatch17")
                    .get(uri1 + "/ws/progress/TaskBatch-1789?rand=wHcZqzBVw4GE3RI"),
                http("progreesTaskBatch18")
                    .get(uri1 + "/ws/progress/TaskBatch-1789?rand=Ff0hnsTRVLsMXjR"),
                http("progreesTaskBatch19")
                    .get(uri1 + "/ws/progress/TaskBatch-1789?rand=RoZqw5DhuJb6KqO"),
                http("progreesTaskBatch20")
                    .get(uri1 + "/ws/progress/TaskBatch-1789?rand=P7hY9ueyo8UBGe1"),
                http("progreesTaskBatch21")
                    .get(uri1 + "/ws/progress/TaskBatch-1789?rand=QJrPTZNv2RKhvBT"),
                http("progreesTaskBatch22")
                    .get(uri1 + "/ws/progress/TaskBatch-1789?rand=2RRdKetaYGAUvOZ"),
                http("progreesTaskBatch23")
                    .get(uri1 + "/ws/progress/TaskBatch-1789?rand=JRqVvOEMgOtSqoU"),
                http("progreesTaskBatch24")
                    .get(uri1 + "/ws/progress/TaskBatch-1789?rand=8E03aU87rhxPesW"),
                http("progreesTaskBatch25")
                    .get(uri1 + "/ws/progress/TaskBatch-1789?rand=kum6PIuaqTZsnTa"),
                http("progreesTaskBatch26")
                    .get(uri1 + "/ws/progress/TaskBatch-1789?rand=lzKHKbkfY23mnCe"),
                http("progreesTaskBatch27")
                    .get(uri1 + "/ws/progress/TaskBatch-1789?rand=URgEprut6gm6zGp"),
                http("progreesTaskBatch28")
                    .get(uri1 + "/ws/progress/TaskBatch-1789?rand=ygHSrlwmXsracK4"),
                http("progreesTaskBatch29")
                    .get(uri1 + "/ws/progress/TaskBatch-1789?rand=STkBZerDMgIZIKb"),
                http("progreesTaskBatch30")
                    .get(uri1 + "/ws/progress/TaskBatch-1789?rand=b01kaW1oFPe9VWq"),
                http("progreesTaskBatch31")
                    .get(uri1 + "/ws/progress/TaskBatch-1789?rand=Kau5vXJ1eezUR03"),
                http("progreesTaskBatch32")
                    .get(uri1 + "/ws/progress/TaskBatch-1789?rand=CVwOzefDvKfhCax"),
                http("progreesTaskBatch33")
                    .get(uri1 + "/ws/progress/TaskBatch-1789?rand=qLDxX70y1jFCbBa"),
                http("progreesTaskBatch34")
                    .get(uri1 + "/ws/progress/TaskBatch-1789?rand=Ow4BXRqZakU1IfV"),
                http("progreesTaskBatch35")
                    .get(uri1 + "/ws/progress/TaskBatch-1789?rand=WcrgssyEr73k6E4"),
                http("progreesTaskBatch36")
                    .get(uri1 + "/ws/progress/TaskBatch-1789?rand=W7x9KlAfW8Elcis"),
                http("progreesTaskBatch37")
                    .get(uri1 + "/ws/progress/TaskBatch-1789?rand=o5k6H02fIXbbM2Y"),
                http("progreesTaskBatch38")
                    .get(uri1 + "/ws/progress/TaskBatch-1789?rand=eEIC5ZAVqTZ5Dce"),
                http("progreesTaskBatch39")
                    .get(uri1 + "/ws/progress/TaskBatch-1789?rand=tyzn9jj6uz1SD4x"),
                http("progreesTaskBatch40")
                    .get(uri1 + "/ws/progress/TaskBatch-1789?rand=xQ6oaNuHmUgI2u4"),
                http("progreesTaskBatch41")
                    .get(uri1 + "/ws/progress/TaskBatch-1789?rand=9zSThMWCWdxvLkT"),
                http("progreesTaskBatch42")
                    .get(uri1 + "/ws/progress/TaskBatch-1789?rand=UhDwbuQgEUhd1xg"),
                http("progreesTaskBatch43")
                    .get(uri1 + "/ws/progress/TaskBatch-1789?rand=vbU8nFR6ccUg4Bb"),
                http("progreesTaskBatch44")
                    .get(uri1 + "/ws/progress/TaskBatch-1789?rand=l1kHtzMMehXWRsp"),
                http("progreesTaskBatch45")
                    .get(uri1 + "/ws/progress/TaskBatch-1789?rand=GLTeHm49QqTlJPu"),
                http("progreesTaskBatch46")
                    .get(uri1 + "/ws/progress/TaskBatch-1789?rand=e3zoLbgOthu3TFM"),
                http("progreesTaskBatch47")
                    .get(uri1 + "/ws/progress/TaskBatch-1789?rand=hMubVyzKj8TCzVv"),
                http("progreesTaskBatch48")
                    .get(uri1 + "/ws/progress/TaskBatch-1789?rand=AQAtphXAagI687L"),
                http("progreesTaskBatch49")
                    .get(uri1 + "/ws/progress/TaskBatch-1789?rand=bqq2chGOdXLpD0m"),
                http("progreesTaskBatch50")
                    .get(uri1 + "/ws/progress/TaskBatch-1789?rand=aDesVOdNbzbxqm0"),
                http("progreesTaskBatch51")
                    .get(uri1 + "/ws/progress/TaskBatch-1789?rand=z5RYFXOgOFgRc9Y"),
                http("progreesTaskBatch52")
                    .get(uri1 + "/components/cookbook/generation/task-generation-completed-template.html"),
                http("task")
                    .get(uri1 + "/ws/task/1789?rand=39b3TjgW0POUs5q"),
                http("glyphicons-halflings-regular.woff")
                    .get(uri1 + "/fonts/glyphicons-halflings-regular.woff")
                    .headers(headers_111)
            )
        )
        .pause(18)
        .exec(http("cookbookHistory task-batch-history-template")
            .get("/tdstm/components/cookbook/history/task-batch-history-template.html")
            .resources(
                http("task-batch-history-detail-template")
                    .get(uri1 + "/components/cookbook/history/task-batch-history-detail-template.html"),
                http("task-batch-history-actions-template")
                    .get(uri1 + "/components/cookbook/history/task-batch-history-actions-template.html"),
                http("list Task batches")
                    .get(uri1 + "/ws/task/listTaskBatches?limitDays=30&rand=TtZEdBLJJo259bf&recipeId=28"),
                http("icon table refresh")
                    .get(uri1 + "/icons/table_refresh.png")
                    .headers(headers_10)
            )
        )
    .pause(11)
    .exec(http("task-batch-history-tasks-template")
        .get("/tdstm/components/cookbook/history/task-batch-history-tasks-template.html")
        .resources(
            http("request_118")
                .get(uri1 + "/ws/task/1789/tasks?rand=II3XCQsDPbJk5sx")
            )
        )
    .pause(14)
    .exec(http("signOut")
        .get("/tdstm/auth/signOut")
        .headers(headers_0)
        .resources(
            http("build.txt")
                .get(uri1 + "/build.txt")
                .headers(headers_0)
                .check(status.is(404)),
            http("build.txt qlTx")
                .get(uri1 + "/build.txt?qlTx")
                .headers(headers_0)
                .check(status.is(404))
        )
    )
  }
	


  val httpProtocol = http
    .baseURL(baseURL)
    .inferHtmlResources()
    .acceptHeader("application/json, text/plain, */*")
    .acceptEncodingHeader("gzip, deflate")
    .acceptLanguageHeader("en-US,en;q=0.5")
    .connection("keep-alive")
    .contentTypeHeader("application/x-www-form-urlencoded; charset=UTF-8")
    .userAgentHeader("Mozilla/5.0 (X11; Ubuntu; Linux x86_64; rv:35.0) Gecko/20100101 Firefox/35.0")

  val genTasks = scenario("GenTasks").exec(Login.login,Cookbook.cookbook)
  // val genTasks = scenario("GenTasks").exec(Login.login)

	setUp(
    // scn.inject(atOnceUsers(1))
    genTasks.inject((rampUsers(1) over (1 seconds))
    ).protocols(httpProtocol)
}