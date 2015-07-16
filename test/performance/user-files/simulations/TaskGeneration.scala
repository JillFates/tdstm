
import scala.concurrent.duration._

import io.gatling.core.Predef._
import io.gatling.http.Predef._
import io.gatling.jdbc.Predef._

class TaskGeneration extends Simulation {
	var startingURL = "http://localhost:8080"
	var numUsers = 1
	if(System.getProperty("startingURL") != null)
	{
		startingURL = System.getProperty("startingURL")
	}
	if(System.getProperty("numUsers") != null)
	{
		numUsers = Integer.getInteger("numUsers", 1)
	}

	val httpProtocol = http
		.baseURL(startingURL)
		.inferHtmlResources()
		.acceptHeader("application/json, text/plain, */*")
		.acceptEncodingHeader("gzip, deflate, sdch")
		.acceptLanguageHeader("en-US,en;q=0.8")
		.contentTypeHeader("application/x-www-form-urlencoded")
		.userAgentHeader("Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/43.0.2357.134 Safari/537.36")

	val headers_0 = Map("Accept" -> "text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,*/*;q=0.8")

	val headers_1 = Map("Accept" -> "text/css,*/*;q=0.1")

	val headers_3 = Map("Accept" -> "image/webp,*/*;q=0.8")

	val headers_6 = Map(
		"Accept" -> "text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,*/*;q=0.8",
		"Accept-Encoding" -> "gzip, deflate",
		"Origin" -> startingURL)

	val headers_13 = Map(
		"Accept" -> "*/*",
		"Accept-Encoding" -> "gzip, deflate",
		"Origin" -> startingURL,
		"X-Requested-With" -> "XMLHttpRequest")

	val headers_20 = Map(
		"Accept-Encoding" -> "gzip, deflate",
		"Origin" -> startingURL)

	val headers_39 = Map("Accept" -> "*/*")

	val headers_43 = Map(
		"Accept" -> "application/json, text/javascript, */*; q=0.01",
		"X-Requested-With" -> "XMLHttpRequest")

	val headers_45 = Map(
		"Accept" -> "*/*",
		"Accept-Encoding" -> "gzip, deflate",
		"Content-Type" -> "application/x-www-form-urlencoded; charset=UTF-8",
		"Origin" -> startingURL,
		"X-Requested-With" -> "XMLHttpRequest")

    val uri1 = startingURL + "/tdstm"

	val scn = scenario("TaskGeneration")
		.exec(http("request_0")
			.get("/tdstm/auth/signOut")
			.headers(headers_0)
			.resources(http("request_1")
			.get(uri1 + "/static/css/main.css")
			.headers(headers_1),
            http("request_2")
			.get(uri1 + "/static/css/tds.css")
			.headers(headers_1),
            http("request_3")
			.get(uri1 + "/static/images/spinner.gif")
			.headers(headers_3),
            http("request_4")
			.get(uri1 + "/static/images/TMLoginLogo.gif")
			.headers(headers_3),
            http("request_5")
			.get(uri1 + "/static/images/button_highlight.png")
			.headers(headers_3)))
		.pause(4)
		.exec(http("request_6")
			.post("/tdstm/auth/signIn")
			.headers(headers_6)
			.formParam("targetUri", "")
			.formParam("username", "jmartin")
			.formParam("password", "password7")
			.resources(http("request_7")
			.get(uri1 + "/static/images/iconApp.png")
			.headers(headers_3),
            http("request_8")
			.get(uri1 + "/static/images/iconDB.png")
			.headers(headers_3),
            http("request_9")
			.get(uri1 + "/static/images/iconServer.png")
			.headers(headers_3),
            http("request_10")
			.get(uri1 + "/static/images/iconStorage.png")
			.headers(headers_3),
            http("request_11")
			.get(uri1 + "/static/images/checked-icon.png")
			.headers(headers_3),
            http("request_12")
			.get(uri1 + "/static/images/iconNetwork.png")
			.headers(headers_3)))
		.pause(5)
		.exec(http("request_13")
			.post("/tdstm/task/retrieveUserToDoCount")
			.headers(headers_13))
		.pause(4)
		.exec(http("request_14")
			.get("/tdstm/cookbook/index")
			.headers(headers_0)
			.resources(http("request_15")
			.get(uri1 + "/ws/cookbook/recipe/list?archived=n&context=All&rand=Rh2JRUVxNXLk59E"),
            http("request_16")
			.get(uri1 + "/ws/cookbook/recipe/28?rand=mKR7C57QIlBo4xR"),
            http("request_17")
			.get(uri1 + "/ws/event/listEventsAndBundles?rand=BICxMW7bbRq82Je"),
            http("request_18")
			.get(uri1 + "/ws/task/findTaskBatchByRecipeAndContext?contextId=349&logs=false&rand=tE3YytS6zZkwuEH&recipeId=28")))
		.pause(4)
		.exec(http("request_19")
			.get("/tdstm/ws/task/findTaskBatchByRecipeAndContext?contextId=364&logs=false&rand=3ronvcTy7bp3yFS&recipeId=28"))
		.pause(6)
		.exec(http("request_20")
			.post("/tdstm/ws/task/generateTasks?rand=")
			.headers(headers_20)
			.formParam("contextId", "364")
			.formParam("recipeId", "28")
			.formParam("recipeVersionId", "")
			.formParam("useWIP", "false")
			.formParam("autoPublish", "false")
			.formParam("deletePrevious", "true")
			.resources(http("request_21")
			.get(uri1 + "/components/cookbook/generation/task-generation-progress-template.html")))
		.pause(1)
		.exec(http("request_22")
			.get("/tdstm/ws/progress/TaskBatch-3546?rand=jty0GTy4zfoDiRG")
			.resources(http("request_23")
			.get(uri1 + "/ws/progress/TaskBatch-3546?rand=1nN3aTRbZNFzJ1N"),
            http("request_24")
			.get(uri1 + "/ws/progress/TaskBatch-3546?rand=Kd2TkQu7nii9gFI"),
            http("request_25")
			.get(uri1 + "/ws/progress/TaskBatch-3546?rand=Ku3zuegyU95axIT"),
            http("request_26")
			.get(uri1 + "/ws/progress/TaskBatch-3546?rand=sUUYgO7OMIHIVqE"),
            http("request_27")
			.get(uri1 + "/ws/progress/TaskBatch-3546?rand=104ePyFv46eLLLR"),
            http("request_28")
			.get(uri1 + "/ws/progress/TaskBatch-3546?rand=6txg7SoC13rSdOg"),
            http("request_29")
			.get(uri1 + "/ws/progress/TaskBatch-3546?rand=CSh2ajaFrUYXEm9"),
            http("request_30")
			.get(uri1 + "/ws/progress/TaskBatch-3546?rand=0sVKwTqA5ryktjm"),
            http("request_31")
			.get(uri1 + "/ws/progress/TaskBatch-3546?rand=Mb85lCPxxIWlm7V"),
            http("request_32")
			.get(uri1 + "/ws/progress/TaskBatch-3546?rand=8V4LMEyCCf318RK"),
            http("request_33")
			.get(uri1 + "/ws/progress/TaskBatch-3546?rand=0kep7CsZB7GxO9B"),
            http("request_34")
			.get(uri1 + "/ws/progress/TaskBatch-3546?rand=1nrOse3ZNkuZrs3"),
            http("request_35")
			.get(uri1 + "/ws/progress/TaskBatch-3546?rand=UepGZRoBGpLYwLJ"),
            http("request_36")
			.get(uri1 + "/ws/progress/TaskBatch-3546?rand=Gb1oylTIz7pAqO8"),
            http("request_37")
			.get(uri1 + "/ws/progress/TaskBatch-3546?rand=CxEDkJ6BziQ3arK"),
            http("request_38")
			.get(uri1 + "/components/cookbook/generation/task-generation-completed-template.html"),
            http("request_39")
			.get(uri1 + "/static/fonts/glyphicons-halflings-regular.woff")
			.headers(headers_39),
            http("request_40")
			.get(uri1 + "/ws/task/3546?rand=UXRQ0FRLAbLN2rE")))
		.pause(4)
		.exec(http("request_41")
			.post("/tdstm/task/retrieveUserToDoCount")
			.headers(headers_13))
		.pause(2)
		.exec(http("request_42")
			.get("/tdstm/assetEntity/listTasks?initSession=true")
			.headers(headers_0)
			.resources(http("request_43")
			.get(uri1 + "/assetEntity/listTaskJSON?moveEvent=0&justRemaining=0&justMyTasks=0&filter=&comment=&taskNumber=&assetEntity=&assetType=&dueDate=&status=&assignedTo=&role=&category=&viewUnpublished=0&_search=false&nd=1437061300790&rows=25&page=1&sidx=&sord=asc")
			.headers(headers_43),
            http("request_44")
			.get(uri1 + "/assetEntity/listTasks?initSession=true")
			.headers(headers_3)))
		.pause(10)
		.exec(http("request_45")
			.post("/tdstm/application/columnAssetPref")
			.headers(headers_45)
			.formParam("columnValue", "dateCreated")
			.formParam("from", "1")
			.formParam("previousValue", "assetName")
			.formParam("type", "Task_Columns")
			.resources(http("request_46")
			.post(uri1 + "/assetEntity/listTasks")
			.headers(headers_6)
			.formParam("justRemaining", "0")
			.formParam("justMyTasks", "0")
			.formParam("viewUnpublished", "0")
			.formParam("moveEvent", "0")
			.formParam("coloumnSelector_assetName", "dateCreated")
			.formParam("taskNumber", "")
			.formParam("comment", "")
			.formParam("assetName", "")
			.formParam("assetType", "")
			.formParam("dueDate", "")
			.formParam("status", "")
			.formParam("assignedTo", "")
			.formParam("role", "")
			.formParam("category", "")
			.formParam("id", "")
			.formParam("statusCss", "")
			.formParam("coloumnSelector_assetType", "assetType")
			.formParam("coloumnSelector_assignedTo", "assignedTo")
			.formParam("coloumnSelector_role", "role")
			.formParam("coloumnSelector_category", "category"),
            http("request_47")
			.get(uri1 + "/assetEntity/listTaskJSON?moveEvent=0&justRemaining=0&justMyTasks=0&filter=&comment=&taskNumber=&assetEntity=&assetType=&dueDate=&status=&assignedTo=&role=&category=&viewUnpublished=0&_search=false&nd=1437061314141&rows=25&page=1&sidx=&sord=asc")
			.headers(headers_43),
            http("request_48")
			.get(uri1 + "/assetEntity/listTasks")
			.headers(headers_3)))
		.pause(2)
		.exec(http("request_49")
			.get("/tdstm/assetEntity/listTaskJSON?moveEvent=0&justRemaining=0&justMyTasks=0&filter=&comment=&taskNumber=&assetEntity=&assetType=&dueDate=&status=&assignedTo=&role=&category=&viewUnpublished=0&_search=false&nd=1437061317628&rows=25&page=1&sidx=dateCreated&sord=asc")
			.headers(headers_43)
			.resources(http("request_50")
			.get(uri1 + "/assetEntity/listTasks")
			.headers(headers_3),
            http("request_51")
			.get(uri1 + "/assetEntity/listTaskJSON?moveEvent=0&justRemaining=0&justMyTasks=0&filter=&comment=&taskNumber=&assetEntity=&assetType=&dueDate=&status=&assignedTo=&role=&category=&viewUnpublished=0&_search=false&nd=1437061318859&rows=25&page=1&sidx=dateCreated&sord=desc")
			.headers(headers_43),
            http("request_52")
			.get(uri1 + "/assetEntity/listTasks")
			.headers(headers_3)))

	setUp(scn.inject(atOnceUsers(numUsers))).protocols(httpProtocol)
}