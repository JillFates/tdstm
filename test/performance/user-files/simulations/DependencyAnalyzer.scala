
import scala.concurrent.duration._

import io.gatling.core.Predef._
import io.gatling.http.Predef._
import io.gatling.jdbc.Predef._

class DependencyAnalyzerTest extends Simulation {

	val httpProtocol = http
		.baseURL("http://localhost:8080")
		.inferHtmlResources()
		.acceptHeader("*/*")
		.acceptEncodingHeader("gzip, deflate")
		.acceptLanguageHeader("en-US,en;q=0.8")
		.contentTypeHeader("application/x-www-form-urlencoded; charset=UTF-8")
		.userAgentHeader("Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/43.0.2357.130 Safari/537.36")

	val headers_0 = Map(
		"Accept" -> "application/json, text/javascript, */*; q=0.01",
		"Accept-Encoding" -> "gzip, deflate, sdch",
		"X-Requested-With" -> "XMLHttpRequest")

	val headers_1 = Map(
		"Accept" -> "image/webp,*/*;q=0.8",
		"Accept-Encoding" -> "gzip, deflate, sdch")

	val headers_2 = Map(
		"Accept" -> "text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,*/*;q=0.8",
		"Accept-Encoding" -> "gzip, deflate, sdch")

	val headers_3 = Map(
		"Accept" -> "text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,*/*;q=0.8",
		"Origin" -> "http://localhost:8080")

	val headers_4 = Map(
		"Origin" -> "http://localhost:8080",
		"X-Requested-With" -> "XMLHttpRequest")

    val uri1 = "http://localhost:8080/tdstm"

	val scn = scenario("DependencyAnalyzerTest")
		.exec(http("request_0")
			.get("/tdstm/assetEntity/listTaskJSON?moveEvent=0&justRemaining=0&justMyTasks=0&filter=&comment=&taskNumber=&assetEntity=&assetType=&dueDate=&status=&assignedTo=&role=&category=&viewUnpublished=1&_search=false&nd=1435775573640&rows=25&page=1&sidx=&sord=asc")
			.headers(headers_0)
			.resources(http("request_1")
			.get(uri1 + "/assetEntity/listTasks?initSession=true")
			.headers(headers_1)))
		.pause(8)
		.exec(http("request_2")
			.get("/tdstm/auth/login")
			.headers(headers_2))
		.pause(4)
		.exec(http("request_3")
			.post("/tdstm/auth/signIn")
			.headers(headers_3)
			.formParam("targetUri", "")
			.formParam("username", "Jmartin")
			.formParam("password", "Password7"))
		.pause(1)
		.exec(http("request_4")
			.post("/tdstm/task/retrieveUserToDoCount")
			.headers(headers_4))
		.pause(5)
		.exec(http("request_5")
			.get("/tdstm/moveBundle/dependencyConsole")
			.headers(headers_2))
		.pause(3)
		.exec(http("request_6")
			.post("/tdstm/assetEntity/retrieveLists")
			.headers(headers_4)
			.formParam("entity", "apps")
			.formParam("dependencyBundle", "0")
			.formParam("bundle", ""))
		.pause(2)
		.exec(http("request_7")
			.post("/tdstm/assetEntity/retrieveLists")
			.headers(headers_4)
			.formParam("entity", "all")
			.formParam("dependencyBundle", "0")
			.formParam("bundle", ""))
		.pause(9)
		.exec(http("request_8")
			.post("/tdstm/assetEntity/retrieveLists")
			.headers(headers_4)
			.formParam("entity", "graph")
			.formParam("dependencyBundle", "0")
			.formParam("force", "undefined")
			.formParam("distance", "undefined")
			.formParam("showControls", "hide")
			.formParam("blackBackground", "null")
			.formParam("bundle", ""))
		.pause(12)
		.exec(http("request_9")
			.post("/tdstm/moveBundle/setCompactControlPref")
			.headers(headers_4)
			.formParam("selected", "true")
			.formParam("prefFor", "depConsoleCompact"))
		.pause(3)
		.exec(http("request_10")
			.post("/tdstm/moveBundle/setCompactControlPref")
			.headers(headers_4)
			.formParam("selected", "false")
			.formParam("prefFor", "depConsoleCompact"))
		.pause(6)
		.exec(http("request_11")
			.post("/tdstm/assetEntity/retrieveLists")
			.headers(headers_4)
			.formParam("entity", "all")
			.formParam("dependencyBundle", "0")
			.formParam("bundle", ""))
		.pause(28)
		.exec(http("request_12")
			.get("/tdstm/moveBundle/dependencyConsole")
			.headers(headers_2))

	setUp(scn.inject(atOnceUsers(1))).protocols(httpProtocol)
}