
import scala.concurrent.duration._

import io.gatling.core.Predef._
import io.gatling.http.Predef._
import io.gatling.jdbc.Predef._

class ViewReports extends Simulation {
	var startingURL = "http://localhost:8080"
	var numUsers = 50
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
		.acceptHeader("text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,*/*;q=0.8")
		.acceptEncodingHeader("gzip, deflate")
		.acceptLanguageHeader("en-US,en;q=0.8")
		.contentTypeHeader("application/x-www-form-urlencoded")
		.userAgentHeader("Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/43.0.2357.130 Safari/537.36")

	val headers_0 = Map("Accept-Encoding" -> "gzip, deflate, sdch")

	val headers_1 = Map("Origin" -> startingURL)

	val headers_2 = Map(
		"Accept" -> "*/*",
		"Origin" -> startingURL,
		"X-Requested-With" -> "XMLHttpRequest")

    val uri1 = startingURL + "/tdstm"

	val scn = scenario("ViewReports")
		.exec(http("request_0")
			.get("/tdstm/auth/login")
			.headers(headers_0))
		.pause(4)
		.exec(http("request_1")
			.post("/tdstm/auth/signIn")
			.headers(headers_1)
			.formParam("targetUri", "")
			.formParam("username", "jmartin")
			.formParam("password", "Password7"))
		.pause(1)
		.exec(http("request_2")
			.post("/tdstm/task/retrieveUserToDoCount")
			.headers(headers_2))
		.pause(2)
		.exec(http("request_3")
			.get("/tdstm/reports/applicationConflicts")
			.headers(headers_0))
		.pause(1)
		.exec(http("request_4")
			.post("/tdstm/reports/generateApplicationConflicts")
			.headers(headers_1)
			.formParam("moveBundle", "3644")
			.formParam("appOwner", "null")
			.formParam("conflicts", "on")
			.formParam("unresolved", "on")
			.formParam("missing", "on"))
		.pause(5)
		.exec(http("request_5")
			.get("/tdstm/reports/serverConflicts")
			.headers(headers_0)
			.resources(http("request_6")
			.post(uri1 + "/reports/generateServerConflicts")
			.headers(headers_1)
			.formParam("moveBundle", "3644")
			.formParam("bundleConflicts", "on")
			.formParam("unresolvedDep", "on")
			.formParam("noRuns", "on")
			.formParam("vmWithNoSupport", "on")))
		.pause(4)
		.exec(http("request_7")
			.get("/tdstm/reports/databaseConflicts")
			.headers(headers_0)
			.resources(http("request_8")
			.post(uri1 + "/reports/generateDatabaseConflicts")
			.headers(headers_1)
			.formParam("moveBundle", "3644")
			.formParam("bundleConflicts", "on")
			.formParam("unresolvedDep", "on")
			.formParam("noApps", "on")
			.formParam("dbWithNoSupport", "on")))
		.pause(3)
		.exec(http("request_9")
			.get("/tdstm/reports/retrieveBundleListForReportDialog?reportId=Task+Report")
			.headers(headers_0))
		.pause(1)
		.exec(http("request_10")
			.post("/tdstm/reports/index")
			.headers(headers_1)
			.formParam("moveEvent", "all")
			.formParam("wComment", "on")
			.formParam("wUnresolved", "on")
			.formParam("viewUnpublished", "on")
			.formParam("_action_tasksReport", "Generate Web"))

	setUp(scn.inject(atOnceUsers(numUsers))).protocols(httpProtocol)
}