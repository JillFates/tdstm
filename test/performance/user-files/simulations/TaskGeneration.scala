
import scala.concurrent.duration._

import io.gatling.core.Predef._
import io.gatling.http.Predef._
import io.gatling.jdbc.Predef._

class TaskGeneration extends Simulation {
	
	var startingURL = "http://localhost:8080"
	var numUsers = 1
	var rampTime = 0
	if(!(sys.env.get("startingURL").isEmpty))
	{
		startingURL = sys.env.get("startingURL").get
	}
	if(!(sys.env.get("numUsers").isEmpty))
	{
		numUsers = sys.env.get("numUsers").get.toInt
	}
	if(!(sys.env.get("rampTime").isEmpty))
	{
		rampTime = sys.env.get("rampTime").get.toInt
	}
	
	val httpProtocol = http
		.baseURL(startingURL)
		.inferHtmlResources()
		.acceptHeader("application/json, text/plain, */*")
		.acceptEncodingHeader("gzip, deflate")
		.acceptLanguageHeader("en-US,en;q=0.5")
		.connection("keep-alive")
		.contentTypeHeader("application/x-www-form-urlencoded; charset=UTF-8")
		.userAgentHeader("Mozilla/5.0 (Windows NT 6.1; WOW64; rv:39.0) Gecko/20100101 Firefox/39.0")

	val headers_0 = Map("Accept" -> "text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8")

	val headers_2 = Map("Accept" -> "image/png,image/*;q=0.8,*/*;q=0.5")

	val headers_3 = Map(
		"Accept" -> "*/*",
		"Pragma" -> "no-cache",
		"X-Requested-With" -> "XMLHttpRequest")

	val headers_5 = Map("Accept" -> "text/css,*/*;q=0.1")

	val headers_8 = Map("Accept" -> "*/*")

	val headers_44 = Map("Pragma" -> "no-cache")

	val headers_64 = Map(
		"Accept" -> "application/font-woff2;q=1.0,application/font-woff;q=0.9,*/*;q=0.8",
		"Accept-Encoding" -> "identity")

    val uri1 = startingURL + "/tdstm"

	val scn = scenario("TaskGeneration")
		.exec(http("request_0")
			.get("/tdstm/auth/login")
			.headers(headers_0))
		.pause(2)
		.exec(http("request_1")
			.post("/tdstm/auth/signIn")
			.headers(headers_0)
			.formParam("targetUri", "")
			.formParam("username", "jmartin")
			.formParam("password", "xyzzy")
			.resources(http("request_2")
			.get(uri1 + "/project/showImage/47")
			.headers(headers_2)))
		.pause(2)
		.exec(http("request_3")
			.post("/tdstm/task/retrieveUserToDoCount")
			.headers(headers_3))
		.pause(1)
		.exec(http("request_4")
			.get("/tdstm/cookbook/index")
			.headers(headers_0)
			.resources(http("request_5")
			.get(uri1 + "/static/css/ng-grid.css")
			.headers(headers_5),
            http("request_6")
			.get(uri1 + "/static/css/mergely/mergely.css")
			.headers(headers_5),
            http("request_7")
			.get(uri1 + "/static/css/codemirror/addon/show-hint.css")
			.headers(headers_5),
            http("request_8")
			.get(uri1 + "/js/codemirror/addon/searchcursor.js")
			.headers(headers_8),
            http("request_9")
			.get(uri1 + "/js/codemirror/addon/dialog.js")
			.headers(headers_8),
            http("request_10")
			.get(uri1 + "/static/css/codemirror/addon/dialog.css")
			.headers(headers_5),
            http("request_11")
			.get(uri1 + "/static/css/codemirror/codemirror.css")
			.headers(headers_5),
            http("request_12")
			.get(uri1 + "/static/css/cookbook.css")
			.headers(headers_5),
            http("request_13")
			.get(uri1 + "/js/codemirror/codemirror.js")
			.headers(headers_8),
            http("request_14")
			.get(uri1 + "/js/codemirror/addon/show-hint.js")
			.headers(headers_8),
            http("request_15")
			.get(uri1 + "/js/codemirror/addon/fullscreen.js")
			.headers(headers_8),
            http("request_16")
			.get(uri1 + "/js/angular/plugins/angular-ui-router.min.js")
			.headers(headers_8),
            http("request_17")
			.get(uri1 + "/static/css/codemirror/addon/fullscreen.css")
			.headers(headers_5),
            http("request_18")
			.get(uri1 + "/static/css/tds-bootstrap.css")
			.headers(headers_5),
            http("request_19")
			.get(uri1 + "/js/codemirror/ui-codemirror.js")
			.headers(headers_8),
            http("request_20")
			.get(uri1 + "/js/codemirror/javascript.js")
			.headers(headers_8),
            http("request_21")
			.get(uri1 + "/js/mergely/mergely.js")
			.headers(headers_8),
            http("request_22")
			.get(uri1 + "/js/codemirror/addon/search.js")
			.headers(headers_8),
            http("request_23")
			.get(uri1 + "/js/moment-timezone-with-data-2010-2020.js")
			.headers(headers_8),
            http("request_24")
			.get(uri1 + "/js/codemirror/addon/javascript-hint.js")
			.headers(headers_8),
            http("request_25")
			.get(uri1 + "/js/controllers/cookbook.js")
			.headers(headers_8),
            http("request_26")
			.get(uri1 + "/project/showImage/47")
			.headers(headers_2),
            http("request_27")
			.get(uri1 + "/components/core/loading-indicator.html"),
            http("request_28")
			.get(uri1 + "/components/cookbook/recipes-template.html"),
            http("request_29")
			.get(uri1 + "/icons/script_add.png")
			.headers(headers_2),
            http("request_30")
			.get(uri1 + "/static/images/processing.gif")
			.headers(headers_2),
            http("request_31")
			.get(uri1 + "/ws/cookbook/recipe/list?archived=n&context=All&rand=5GfCqBS4YFP6aoO"),
            http("request_32")
			.get(uri1 + "/icons/script_edit.png")
			.headers(headers_2),
            http("request_33")
			.get(uri1 + "/icons/arrow_undo.png")
			.headers(headers_2),
            http("request_34")
			.get(uri1 + "/icons/delete.png")
			.headers(headers_2),
            http("request_35")
			.get(uri1 + "/icons/folder_go.png")
			.headers(headers_2),
            http("request_36")
			.get(uri1 + "/icons/folder.png")
			.headers(headers_2),
            http("request_37")
			.get(uri1 + "/components/cookbook/recipe-detail-template.html"),
            http("request_38")
			.get(uri1 + "/components/cookbook/generation/task-generation-template.html"),
            http("request_39")
			.get(uri1 + "/ws/cookbook/recipe/28?rand=v5Xts8l1lUIlPLg"),
            http("request_40")
			.get(uri1 + "/ws/event/listEventsAndBundles?rand=z9OJ4JoPdTkhNI9"),
            http("request_41")
			.get(uri1 + "/components/cookbook/generation/task-generation-start-template.html"),
            http("request_42")
			.get(uri1 + "/icons/table_gear.png")
			.headers(headers_2)))
		.pause(3)
		.exec(http("request_43")
			.get("/tdstm/ws/task/findTaskBatchByRecipeAndContext?contextId=349&logs=false&rand=WeT9ezIUUquxJUU&recipeId=28"))
		.pause(6)
		.exec(http("request_44")
			.post("/tdstm/ws/task/generateTasks?rand=")
			.headers(headers_44)
			.formParam("contextId", "349")
			.formParam("recipeId", "28")
			.formParam("recipeVersionId", "")
			.formParam("useWIP", "false")
			.formParam("autoPublish", "false")
			.formParam("deletePrevious", "true")
			.check(
				regex("jobId.*?:.*?\"TaskBatch-([0-9]*?)\"")
				.saveAs("jobId")
			)
			.resources(http("request_45")
			.get(uri1 + "/components/cookbook/generation/task-generation-progress-template.html")))
		.pause(1)
		
		.exec(http("request_status")
			.get(uri1 + "/ws/progress/TaskBatch-${jobId}?rand=")
			.check(
				regex("percentComp.*?:.*?([0-9]*)")
				.saveAs("percentComp")
			)
		)
		.pause(2)
		
		/** loop */
		.asLongAs(session => session("percentComp").as[String].toInt!=100) {
			exec(http("request_TaskBatch-${jobId}_Progress-${percentComp}%")
				.get(uri1 + "/ws/progress/TaskBatch-${jobId}?rand=")
				.check(
					regex("percentComp.*?:.*?([0-9]*)")
					.saveAs("percentComp")
				)
			)
			.pause(2)
		}

		.exec(http("request_finish_${jobId}")		
			.get(uri1 + "/components/cookbook/generation/task-generation-completed-template.html")
            .resources(http("request_64")
			.get(uri1 + "/static/fonts/glyphicons-halflings-regular.woff")
			.headers(headers_64),
            http("request_65")
			.get(uri1 + "/ws/task/${jobId}?rand=")
		))
			
		.pause(4)
		.exec(http("request_66")
			.get("/tdstm/components/cookbook/history/task-batch-history-template.html")
			.resources(http("request_67")
			.get(uri1 + "/components/cookbook/history/task-batch-history-detail-template.html"),
            http("request_68")
			.get(uri1 + "/components/cookbook/history/task-batch-history-actions-template.html"),
            http("request_69")
			.get(uri1 + "/ws/task/listTaskBatches?limitDays=All&rand=randVal&recipeId=28"),
            http("request_70")
			.get(uri1 + "/icons/table_refresh.png")
			.headers(headers_2)))
		.pause(3)
		.exec(http("request_tasks")
			.get("/tdstm/components/cookbook/history/task-batch-history-tasks-template.html")
			.resources(http("request_tasksView")
			.get(uri1 + "/ws/task/${jobId}/tasks?rand=")))
		.pause(10)
		.exec(http("request_73")
			.get("/tdstm/auth/signOut")
			.headers(headers_0))

	setUp(scn.inject(rampUsers(numUsers) over(rampTime seconds))).protocols(httpProtocol)
}