
import scala.concurrent.duration._

import io.gatling.core.Predef._
import io.gatling.http.Predef._
import io.gatling.jdbc.Predef._

class ViewReports extends Simulation {

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
		.acceptHeader("text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8")
		.acceptEncodingHeader("gzip, deflate")
		.acceptLanguageHeader("en-US,en;q=0.5")
		.connection("keep-alive")
		.contentTypeHeader("application/x-www-form-urlencoded")
		.userAgentHeader("Mozilla/5.0 (Windows NT 6.1; WOW64; rv:39.0) Gecko/20100101 Firefox/39.0")

	val headers_1 = Map("Accept" -> "text/css,*/*;q=0.1")

	val headers_4 = Map("Accept" -> "image/png,image/*;q=0.8,*/*;q=0.5")

	val headers_13 = Map("Accept" -> "*/*")

	val headers_15 = Map(
		"Accept" -> "*/*",
		"Content-Type" -> "application/x-www-form-urlencoded; charset=UTF-8",
		"Pragma" -> "no-cache",
		"X-Requested-With" -> "XMLHttpRequest")

    val uri1 = startingURL + "/tdstm"

	val scn = scenario("ViewReports")
		.exec(http("rAuth")
			.get("/tdstm/auth/login")
			.resources(http("request_1")
			.get(uri1 + "/static/css/main.css")
			.headers(headers_1),
            http("request_2")
			.get(uri1 + "/static/css/tds.css")
			.headers(headers_1)))
		.pause(1)
		.exec(http("rSignIn")
			.post("/tdstm/auth/signIn")
			.formParam("targetUri", "")
			.formParam("username", "jmartin")
			.formParam("password", "xyzzy")
			.resources(http("request_4")
			.get(uri1 + "/project/showImage/47")
			.headers(headers_4)))
		.pause(6)
		.exec(http("rCablingConflict")
			.get("/tdstm/reports/retrieveBundleListForReportDialog?reportId=CablingConflict")
			.resources(http("request_6")
			.get(uri1 + "/project/showImage/47")
			.headers(headers_4)))
		.pause(1)
		.exec(http("rCablingConflictPDF")
			.get("/tdstm/reports/cablingQAReport?_format=PDF&_name=Generate&_file=CablingQAReport&reportName=cablingConflict&moveBundle=&cableType="))
		.pause(8)
		.exec(http("rCablingData")
			.get("/tdstm/reports/retrieveBundleListForReportDialog?reportId=CablingData")
			.resources(http("request_9")
			.get(uri1 + "/project/showImage/47")
			.headers(headers_4)))
		.pause(2)
		.exec(http("rCablingDataXLS")
			.get("/tdstm/reports/cablingDataReport?_format=XLS&_name=Generate&_file=CablingDataReport&reportName=cablingData&moveBundle=&cableType="))
		.pause(6)
		.exec(http("rPower")
			.get("/tdstm/reports/powerReport")
			.resources(http("request_12")
			.get(uri1 + "/static/css/jquery.autocomplete.css")
			.headers(headers_1),
            http("request_13")
			.get(uri1 + "/js/asset.tranman.js")
			.headers(headers_13),
            http("request_14")
			.get(uri1 + "/project/showImage/47")
			.headers(headers_4),
            http("request_15")
			.post(uri1 + "/rackLayouts/retrieveRackDetails")
			.headers(headers_15)
			.formParam("bundles", "all"),
            http("rPowerGenerate")
			.post(uri1 + "/reports/powerReportDetails")
			.headers(headers_15)
			.formParam("moveBundle", "all")
			.formParam("sourcerack", "none")
			.formParam("targetrack", "")
			.formParam("output", "web")
			.formParam("powerType", "Watts")))
		.pause(4)
		.exec(http("rApplicationProfiles")
			.get("/tdstm/reports/applicationProfiles")
			.resources(http("request_19")
			.get(uri1 + "/project/showImage/47")
			.headers(headers_4)))
		.pause(2)
		.exec(http("rApplicationProfilesGenerate")
			.post("/tdstm/reports/generateApplicationProfiles")
			.formParam("moveBundle", "3239")
			.formParam("smeByModel", "null")
			.formParam("appOwner", "null")
			.resources(http("request_21")
			.get(uri1 + "/static/components/core/core.js")
			.headers(headers_13),
            http("request_22")
			.get(uri1 + "/static/components/comment/comment.js")
			.headers(headers_13),
            http("request_23")
			.get(uri1 + "/js/asset.comment.js")
			.headers(headers_13),
            http("request_24")
			.get(uri1 + "/project/showImage/47")
			.headers(headers_4)))
		.pause(5)
		.exec(http("rApplicationConflicts")
			.get("/tdstm/reports/applicationConflicts")
			.resources(http("request_26")
			.get(uri1 + "/project/showImage/47")
			.headers(headers_4)))
		.pause(3)
		.exec(http("rApplicationConflictsGenerate")
			.post("/tdstm/reports/generateApplicationConflicts")
			.formParam("moveBundle", "3239")
			.formParam("appOwner", "null")
			.formParam("conflicts", "on")
			.formParam("unresolved", "on")
			.formParam("missing", "on")
			.resources(http("request_28")
			.get(uri1 + "/project/showImage/47")
			.headers(headers_4)))
		.pause(5)
		.exec(http("rServerConflicts")
			.get("/tdstm/reports/serverConflicts")
			.resources(http("request_30")
			.get(uri1 + "/project/showImage/47")
			.headers(headers_4)))
		.pause(2)
		.exec(http("rServerConflictsGenerate")
			.post("/tdstm/reports/generateServerConflicts")
			.formParam("moveBundle", "3239")
			.formParam("bundleConflicts", "on")
			.formParam("unresolvedDep", "on")
			.formParam("noRuns", "on")
			.formParam("vmWithNoSupport", "on")
			.resources(http("request_32")
			.get(uri1 + "/project/showImage/47")
			.headers(headers_4)))
		.pause(4)
		.exec(http("rDatabaseConflicts")
			.get("/tdstm/reports/databaseConflicts")
			.resources(http("request_34")
			.get(uri1 + "/project/showImage/47")
			.headers(headers_4)))
		.pause(2)
		.exec(http("rDatabaseConflictsGenerate")
			.post("/tdstm/reports/generateDatabaseConflicts")
			.formParam("moveBundle", "3239")
			.formParam("bundleConflicts", "on")
			.formParam("unresolvedDep", "on")
			.formParam("noApps", "on")
			.formParam("dbWithNoSupport", "on")
			.resources(http("request_36")
			.get(uri1 + "/project/showImage/47")
			.headers(headers_4)))
		.pause(4)
		.exec(http("rTaskReport")
			.get("/tdstm/reports/retrieveBundleListForReportDialog?reportId=Task+Report")
			.resources(http("request_38")
			.get(uri1 + "/project/showImage/47")
			.headers(headers_4)))
		.pause(2)
		.exec(http("rTaskReportView")
			.post("/tdstm/reports/index")
			.formParam("moveEvent", "all")
			.formParam("wComment", "on")
			.formParam("wUnresolved", "on")
			.formParam("viewUnpublished", "on")
			.formParam("_action_tasksReport", "Generate Web")
			.resources(http("request_40")
			.get(uri1 + "/project/showImage/47")
			.headers(headers_4)))
		.pause(6)
		.exec(http("rReportSummary")
			.get("/tdstm/reports/index?projectId=2445")
			.resources(http("request_42")
			.get(uri1 + "/project/showImage/47")
			.headers(headers_4)))
		.pause(7)
		.exec(http("rTransportWorksheets")
			.get("/tdstm/reports/retrieveBundleListForReportDialog?reportId=Transportation+Asset+List")
			.resources(http("request_44")
			.get(uri1 + "/project/showImage/47")
			.headers(headers_4)))
		.pause(5)
		.exec(http("rTransportWorksheetsPDF")
			.get("/tdstm/reports/cartAssetReport?_format=PDF&_name=Generate&_file=transportationAssetReport&reportName=transportationAsset&moveBundle="))
		.pause(13)
		.exec(http("rApplicationMigration")
			.get("/tdstm/reports/applicationMigrationReport")
			.resources(http("request_47")
			.get(uri1 + "/project/showImage/47")
			.headers(headers_4)))
		.pause(3)
		.exec(http("rApplicationMigrationGenerate")
			.post("/tdstm/reports/generateApplicationMigration")
			.formParam("moveBundle", "3239")
			.formParam("smeByModel", "null")
			.formParam("startCateory", "shutdown")
			.formParam("workflowTransId", "")
			.formParam("stopCateory", "startup")
			.formParam("outageWindow", "drRtoDesc")
			.resources(http("request_49")
			.get(uri1 + "/js/model.manufacturer.js")
			.headers(headers_13),
            http("request_50")
			.get(uri1 + "/static/css/ui.resizable.css")
			.headers(headers_1),
            http("request_51")
			.get(uri1 + "/js/jqgrid-support.js")
			.headers(headers_13),
            http("request_52")
			.get(uri1 + "/static/css/ui.slider.css")
			.headers(headers_1),
            http("request_53")
			.get(uri1 + "/static/css/ui.datepicker.css")
			.headers(headers_1),
            http("request_54")
			.get(uri1 + "/static/components/comment/comment.css")
			.headers(headers_1),
            http("request_55")
			.get(uri1 + "/static/css/ui.accordion.css")
			.headers(headers_1),
            http("request_56")
			.get(uri1 + "/static/css/ui.tabs.css")
			.headers(headers_1),
            http("request_57")
			.get(uri1 + "/static/css/jqgrid/ui.jqgrid.css")
			.headers(headers_1),
            http("request_58")
			.get(uri1 + "/css/jqgrid/ui.jqgrid.css")
			.headers(headers_1),
            http("request_59")
			.get(uri1 + "/static/icons/comment_add.png")
			.headers(headers_4),
            http("request_60")
			.get(uri1 + "/project/showImage/47")
			.headers(headers_4),
            http("request_61")
			.get(uri1 + "/static/icons/database_save.png")
			.headers(headers_4)))
		.pause(3)
		.exec(http("rIssueReport")
			.get("/tdstm/reports/retrieveBundleListForReportDialog?reportId=Issue+Report")
			.resources(http("request_63")
			.get(uri1 + "/project/showImage/47")
			.headers(headers_4)))
		.pause(7)
		.exec(http("rIssueReportPDF")
			.get("/tdstm/reports/issueReport?_format=PDF&_name=Generate&_file=issueReport&moveBundle=%5B3239%2C%5D&reportSort=id&reportResolveInfo=true&commentCode=&commentInfo=true&newsInfo=true"))
		.pause(6)
		.exec(http("rCablingQa")
			.get("/tdstm/reports/retrieveBundleListForReportDialog?reportId=CablingQA")
			.resources(http("request_66")
			.get(uri1 + "/project/showImage/47")
			.headers(headers_4)))
		.pause(5)
		.exec(http("rCablingQaPDF")
			.get("/tdstm/reports/cablingQAReport?_format=PDF&_name=Generate&_file=CablingQAReport&reportName=cablingQA&moveBundle=3239&cableType="))
		.pause(7)
		.exec(http("rSignOut")
			.get("/tdstm/auth/signOut"))

	setUp(scn.inject(rampUsers(numUsers) over(rampTime seconds))).protocols(httpProtocol)
}