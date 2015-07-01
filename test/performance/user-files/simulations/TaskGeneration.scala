
import scala.concurrent.duration._

import io.gatling.core.Predef._
import io.gatling.http.Predef._
import io.gatling.jdbc.Predef._

class TaskGenerationTest extends Simulation {

	val httpProtocol = http
		.baseURL("http://localhost:8080")
		.inferHtmlResources()
		.acceptHeader("*/*")
		.acceptEncodingHeader("gzip, deflate, sdch")
		.acceptLanguageHeader("en-US,en;q=0.8")
		.contentTypeHeader("application/x-www-form-urlencoded")
		.userAgentHeader("Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/43.0.2357.130 Safari/537.36")

	val headers_0 = Map("Accept" -> "text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,*/*;q=0.8")

	val headers_1 = Map(
		"Accept" -> "text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,*/*;q=0.8",
		"Accept-Encoding" -> "gzip, deflate",
		"Origin" -> "http://localhost:8080")

	val headers_2 = Map(
		"Accept-Encoding" -> "gzip, deflate",
		"Origin" -> "http://localhost:8080",
		"X-Requested-With" -> "XMLHttpRequest")

	val headers_4 = Map(
		"Accept" -> "application/json, text/javascript, */*; q=0.01",
		"X-Requested-With" -> "XMLHttpRequest")

	val headers_5 = Map("Accept" -> "image/webp,*/*;q=0.8")

	val headers_6 = Map("Accept" -> "application/json, text/plain, */*")

	val headers_10 = Map(
		"Accept" -> "application/json, text/plain, */*",
		"Accept-Encoding" -> "gzip, deflate",
		"Origin" -> "http://localhost:8080")

	val headers_17 = Map("Accept" -> "text/css,*/*;q=0.1")

	val headers_84 = Map("Pragma" -> "no-cache")

    val uri1 = "http://localhost:8080/tdstm"

	val scn = scenario("TaskGenerationTest")
		.exec(http("request_0")
			.get("/tdstm/auth/login")
			.headers(headers_0))
		.pause(3)
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
		.pause(1)
		.exec(http("request_3")
			.get("/tdstm/assetEntity/listTasks?initSession=true")
			.headers(headers_0)
			.resources(http("request_4")
			.get(uri1 + "/assetEntity/listTaskJSON?moveEvent=0&justRemaining=0&justMyTasks=0&filter=&comment=&taskNumber=&assetEntity=&assetType=&dueDate=&status=&assignedTo=&role=&category=&viewUnpublished=1&_search=false&nd=1435775429345&rows=25&page=1&sidx=&sord=asc")
			.headers(headers_4),
            http("request_5")
			.get(uri1 + "/assetEntity/listTasks?initSession=true")
			.headers(headers_5)))
		.pause(4)
		.exec(http("request_6")
			.get("/tdstm/task/editTask")
			.headers(headers_6)
			.resources(http("request_7")
			.get(uri1 + "/assetEntity/assetClasses")
			.headers(headers_6),
            http("request_8")
			.get(uri1 + "/assetEntity/assetsByClass?assetClass=")
			.headers(headers_6),
            http("request_9")
			.get(uri1 + "/task/retrieveStaffRoles")
			.headers(headers_6),
            http("request_10")
			.post(uri1 + "/assetEntity/retrieveWorkflowTransition?format=json&assetId=&category=general&assetCommentId=")
			.headers(headers_10),
            http("request_11")
			.post(uri1 + "/assetEntity/retrieveWorkflowTransition?format=json&assetId=&category=general&assetCommentId=")
			.headers(headers_10),
            http("request_12")
			.post(uri1 + "/assetEntity/updateStatusSelect?format=json&id=")
			.headers(headers_10),
            http("request_13")
			.get(uri1 + "/assetEntity/isAllowToChangeStatus?id=")
			.headers(headers_6),
            http("request_14")
			.post(uri1 + "/assetEntity/updateAssignedToSelect?format=json&forView=&id=")
			.headers(headers_10)))
		.pause(4)
		.exec(http("request_15")
			.post("/tdstm/assetEntity/saveComment")
			.headers(headers_10)
			.formParam("assetClass", "")
			.formParam("assetEntity", "")
			.formParam("assetType", "")
			.formParam("assignedTo", "")
			.formParam("category", "general")
			.formParam("comment", "Test Task 1")
			.formParam("commentFromId", "")
			.formParam("commentId", "")
			.formParam("commentType", "issue")
			.formParam("deletePredId", "")
			.formParam("dueDate", "")
			.formParam("duration", "")
			.formParam("durationScale", "M")
			.formParam("estFinish", "")
			.formParam("estStart", "")
			.formParam("forWhom", "")
			.formParam("hardAssigned", "0")
			.formParam("sendNotification", "0")
			.formParam("isResolved", "0")
			.formParam("instructionsLink", "")
			.formParam("manageDependency", "1")
			.formParam("moveEvent", "")
			.formParam("mustVerify", "0")
			.formParam("override", "0")
			.formParam("predCount", "-1")
			.formParam("predecessorCategory", "")
			.formParam("prevAsset", "")
			.formParam("priority", "3")
			.formParam("resolution", "")
			.formParam("role", "0")
			.formParam("status", "Ready")
			.formParam("workflowTransition", "")
			.formParam("id", "")
			.formParam("deletedPreds", ""))
		.pause(4)
		.exec(http("request_16")
			.get("/tdstm/assetEntity/listTasks?initSession=true")
			.headers(headers_0)
			.resources(http("request_17")
			.get(uri1 + "/static/css/main.css")
			.headers(headers_17),
            http("request_18")
			.get(uri1 + "/static/css/tds.css")
			.headers(headers_17),
            http("request_19")
			.get(uri1 + "/static/css/ui.dialog.css")
			.headers(headers_17),
            http("request_20")
			.get(uri1 + "/static/css/ui.core.css")
			.headers(headers_17),
            http("request_21")
			.get(uri1 + "/static/css/ui.datetimepicker.css")
			.headers(headers_17),
            http("request_22")
			.get(uri1 + "/static/css/ui.theme.css")
			.headers(headers_17),
            http("request_23")
			.get(uri1 + "/static/css/combox.css")
			.headers(headers_17),
            http("request_24")
			.get(uri1 + "/static/css/select2.css")
			.headers(headers_17),
            http("request_25")
			.get(uri1 + "/static/css/jquery-ui-smoothness.css")
			.headers(headers_17),
            http("request_26")
			.get(uri1 + "/static/plugins/jquery-ui-1.8.15/jquery-ui/themes/ui-lightness/jquery-ui-1.8.15.custom.css")
			.headers(headers_17),
            http("request_27")
			.get(uri1 + "/static/plugins/jqgrid-3.8.0.1/css/jqgrid/ui.jqgrid.css")
			.headers(headers_17),
            http("request_28")
			.get(uri1 + "/static/css/ui.datepicker.css")
			.headers(headers_17),
            http("request_29")
			.get(uri1 + "/static/plugins/jqgrid-3.8.0.1/css/jqgrid/jqgrid.css")
			.headers(headers_17),
            http("request_30")
			.get(uri1 + "/static/css/jqgrid/ui.jqgrid.css")
			.headers(headers_17),
            http("request_31")
			.get(uri1 + "/static/css/dropDown.css")
			.headers(headers_17),
            http("request_32")
			.get(uri1 + "/static/components/comment/comment.css")
			.headers(headers_17),
            http("request_33")
			.get(uri1 + "/static/css/daterangepicker-bs2.css")
			.headers(headers_17),
            http("request_34")
			.get(uri1 + "/js/jquery-1.9.1.js")
			.check(status.in(200,310)),
            http("request_35")
			.get(uri1 + "/js/prototype/prototype.js")
			.check(status.in(200,310)),
            http("request_36")
			.get(uri1 + "/js/jquery-1.9.1-ui.js")
			.check(status.in(200,310)),
            http("request_37")
			.get(uri1 + "/js/jquery-migrate-1.0.0.js")
			.check(status.in(200,310)),
            http("request_38")
			.get(uri1 + "/js/select2.js")
			.check(status.in(200,310)),
            http("request_39")
			.get(uri1 + "/js/datetimepicker.js")
			.check(status.in(200,310)),
            http("request_40")
			.get(uri1 + "/js/jquery.combox.js")
			.check(status.in(200,310)),
            http("request_41")
			.get(uri1 + "/js/moment.min.js")
			.check(status.in(200,310)),
            http("request_42")
			.get(uri1 + "/js/daterangepicker.js")
			.check(status.in(200,310)),
            http("request_43")
			.get(uri1 + "/js/tds-common.js")
			.check(status.in(200,310)),
            http("request_44")
			.get(uri1 + "/js/lodash/lodash.min.js")
			.check(status.in(200,310)),
            http("request_45")
			.get(uri1 + "/js/asset.tranman.js")
			.check(status.in(200,310)),
            http("request_46")
			.get(uri1 + "/js/entity.crud.js")
			.check(status.in(200,310)),
            http("request_47")
			.get(uri1 + "/js/angular/plugins/angular-ui.js")
			.check(status.in(200,310)),
            http("request_48")
			.get(uri1 + "/js/asset.comment.js")
			.check(status.in(200,310)),
            http("request_49")
			.get(uri1 + "/js/model.manufacturer.js")
			.check(status.in(200,310)),
            http("request_50")
			.get(uri1 + "/js/angular/angular.min.js")
			.check(status.in(200,310)),
            http("request_51")
			.get(uri1 + "/js/angular/plugins/angular-resource.js")
			.check(status.in(200,310)),
            http("request_52")
			.get(uri1 + "/static/components/core/core.js"),
            http("request_53")
			.get(uri1 + "/static/components/comment/comment.js"),
            http("request_54")
			.get(uri1 + "/static/components/asset/asset.js"),
            http("request_55")
			.get(uri1 + "/static/plugins/jqgrid-3.8.0.1/js/jqgrid/i18n/grid.locale-en.js"),
            http("request_56")
			.get(uri1 + "/js/cabling.js")
			.check(status.in(200,310)),
            http("request_57")
			.get(uri1 + "/static/plugins/jqgrid-3.8.0.1/js/jqgrid/jquery.jqGrid.fluid.js"),
            http("request_58")
			.get(uri1 + "/static/plugins/jqgrid-3.8.0.1/js/jqgrid/jquery.jqGrid.min.js"),
            http("request_59")
			.get(uri1 + "/js/angular/plugins/ui-bootstrap-tpls-0.10.0.min.js")
			.check(status.in(200,310)),
            http("request_60")
			.get(uri1 + "/js/bootstrap.js")
			.check(status.in(200,310)),
            http("request_61")
			.get(uri1 + "/js/angular/plugins/ngGrid/ng-grid-2.0.7.min.js")
			.check(status.in(200,310)),
            http("request_62")
			.get(uri1 + "/js/jqgrid-support.js")
			.check(status.in(200,310)),
            http("request_63")
			.get(uri1 + "/js/angular/plugins/ngGrid/ng-grid-layout.js")
			.check(status.in(200,310)),
            http("request_64")
			.get(uri1 + "/static/images/TMMenuLogo.png")
			.headers(headers_5),
            http("request_65")
			.get(uri1 + "/js/tdsmenu.js")
			.check(status.in(200,310)),
            http("request_66")
			.get(uri1 + "/static/icons/comment_add.png")
			.headers(headers_5),
            http("request_67")
			.get(uri1 + "/static/icons/information.png")
			.headers(headers_5),
            http("request_68")
			.get(uri1 + "/static/plugins/jquery-ui-1.8.15/jquery-ui/themes/ui-lightness/images/ui-bg_gloss-wave_35_f6a828_500x100.png")
			.headers(headers_5),
            http("request_69")
			.get(uri1 + "/static/plugins/jquery-ui-1.8.15/jquery-ui/themes/ui-lightness/images/ui-bg_highlight-soft_100_eeeeee_1x100.png")
			.headers(headers_5),
            http("request_70")
			.get(uri1 + "/static/plugins/jquery-ui-1.8.15/jquery-ui/themes/ui-lightness/images/ui-icons_222222_256x240.png")
			.headers(headers_5),
            http("request_71")
			.get(uri1 + "/static/images/skin/shadow.jpg")
			.headers(headers_5),
            http("request_72")
			.get(uri1 + "/static/icons/database_edit.png")
			.headers(headers_5),
            http("request_73")
			.get(uri1 + "/static/icons/database_save.png")
			.headers(headers_5),
            http("request_74")
			.get(uri1 + "/components/core/action-button-template.html")
			.headers(headers_6)
			.check(status.in(200,310)),
            http("request_75")
			.get(uri1 + "/static/plugins/jquery-ui-1.8.15/jquery-ui/themes/ui-lightness/images/ui-bg_glass_100_f6f6f6_1x400.png")
			.headers(headers_5),
            http("request_76")
			.get(uri1 + "/static/plugins/jquery-ui-1.8.15/jquery-ui/themes/ui-lightness/images/ui-bg_diagonals-thick_20_666666_40x40.png")
			.headers(headers_5),
            http("request_77")
			.get(uri1 + "/static/plugins/jquery-ui-1.8.15/jquery-ui/themes/ui-lightness/images/ui-bg_glass_65_ffffff_1x400.png")
			.headers(headers_5),
            http("request_78")
			.get(uri1 + "/static/plugins/jquery-ui-1.8.15/jquery-ui/themes/ui-lightness/images/ui-icons_ef8c08_256x240.png")
			.headers(headers_5),
            http("request_79")
			.get(uri1 + "/static/plugins/jquery-ui-1.8.15/jquery-ui/themes/ui-lightness/images/ui-icons_ffffff_256x240.png")
			.headers(headers_5),
            http("request_80")
			.get(uri1 + "/images/select2Arrow.png")
			.headers(headers_5)
			.check(status.in(200,310)),
            http("request_81")
			.get(uri1 + "/static/icons/database_delete.png")
			.headers(headers_5),
            http("request_82")
			.get(uri1 + "/icons/tds_task_graph.png")
			.headers(headers_5)
			.check(status.in(200,310)),
            http("request_83")
			.get(uri1 + "/icons/timeline_marker.png")
			.headers(headers_5)
			.check(status.in(200,310)),
            http("request_84")
			.get(uri1 + "/static/images/favicon.ico")
			.headers(headers_84),
            http("request_85")
			.get(uri1 + "/assetEntity/listTaskJSON?moveEvent=0&justRemaining=0&justMyTasks=0&filter=&comment=&taskNumber=&assetEntity=&assetType=&dueDate=&status=&assignedTo=&role=&category=&viewUnpublished=1&_search=false&nd=1435775445270&rows=25&page=1&sidx=&sord=asc")
			.headers(headers_4),
            http("request_86")
			.get(uri1 + "/assetEntity/listTasks?initSession=true")
			.headers(headers_5),
            http("request_87")
			.post(uri1 + "/task/genActionBarForShowViewJson")
			.headers(headers_10)
			.formParam("id", "503979")
			.formParam("includeDetails", "true")))
		.pause(1)
		.exec(http("request_88")
			.get("/tdstm/task/showTask?taskId=503979")
			.headers(headers_6)
			.resources(http("request_89")
			.post(uri1 + "/task/genActionBarForShowViewJson")
			.headers(headers_10)
			.formParam("id", "503979")
			.formParam("includeDetails", "false"),
            http("request_90")
			.get(uri1 + "/assetEntity/showComment?id=503979")
			.headers(headers_6)))
		.pause(2)
		.exec(http("request_91")
			.post("/tdstm/assetEntity/deleteComment")
			.headers(headers_10)
			.formParam("id", "503979")
			.formParam("assetEntity", ""))
		.pause(1)
		.exec(http("request_92")
			.get("/tdstm/assetEntity/listTasks?initSession=true")
			.headers(headers_0)
			.resources(http("request_93")
			.get(uri1 + "/static/css/main.css")
			.headers(headers_17),
            http("request_94")
			.get(uri1 + "/static/css/tds.css")
			.headers(headers_17),
            http("request_95")
			.get(uri1 + "/static/css/ui.core.css")
			.headers(headers_17),
            http("request_96")
			.get(uri1 + "/static/css/ui.dialog.css")
			.headers(headers_17),
            http("request_97")
			.get(uri1 + "/static/css/ui.datetimepicker.css")
			.headers(headers_17),
            http("request_98")
			.get(uri1 + "/static/css/ui.theme.css")
			.headers(headers_17),
            http("request_99")
			.get(uri1 + "/static/css/jquery-ui-smoothness.css")
			.headers(headers_17),
            http("request_100")
			.get(uri1 + "/static/css/combox.css")
			.headers(headers_17),
            http("request_101")
			.get(uri1 + "/static/plugins/jquery-ui-1.8.15/jquery-ui/themes/ui-lightness/jquery-ui-1.8.15.custom.css")
			.headers(headers_17),
            http("request_102")
			.get(uri1 + "/static/css/select2.css")
			.headers(headers_17),
            http("request_103")
			.get(uri1 + "/static/plugins/jqgrid-3.8.0.1/css/jqgrid/ui.jqgrid.css")
			.headers(headers_17),
            http("request_104")
			.get(uri1 + "/static/plugins/jqgrid-3.8.0.1/css/jqgrid/jqgrid.css")
			.headers(headers_17),
            http("request_105")
			.get(uri1 + "/static/css/ui.datepicker.css")
			.headers(headers_17),
            http("request_106")
			.get(uri1 + "/static/css/jqgrid/ui.jqgrid.css")
			.headers(headers_17),
            http("request_107")
			.get(uri1 + "/static/css/dropDown.css")
			.headers(headers_17),
            http("request_108")
			.get(uri1 + "/static/components/comment/comment.css")
			.headers(headers_17),
            http("request_109")
			.get(uri1 + "/static/css/daterangepicker-bs2.css")
			.headers(headers_17),
            http("request_110")
			.get(uri1 + "/js/prototype/prototype.js")
			.check(status.in(200,310)),
            http("request_111")
			.get(uri1 + "/js/jquery-1.9.1-ui.js")
			.check(status.in(200,310)),
            http("request_112")
			.get(uri1 + "/js/jquery-1.9.1.js")
			.check(status.in(200,310)),
            http("request_113")
			.get(uri1 + "/js/datetimepicker.js")
			.check(status.in(200,310)),
            http("request_114")
			.get(uri1 + "/js/select2.js")
			.check(status.in(200,310)),
            http("request_115")
			.get(uri1 + "/js/jquery-migrate-1.0.0.js")
			.check(status.in(200,310)),
            http("request_116")
			.get(uri1 + "/js/moment.min.js")
			.check(status.in(200,310)),
            http("request_117")
			.get(uri1 + "/js/daterangepicker.js")
			.check(status.in(200,310)),
            http("request_118")
			.get(uri1 + "/js/jquery.combox.js")
			.check(status.in(200,310)),
            http("request_119")
			.get(uri1 + "/js/lodash/lodash.min.js")
			.check(status.in(200,310)),
            http("request_120")
			.get(uri1 + "/js/asset.tranman.js")
			.check(status.in(200,310)),
            http("request_121")
			.get(uri1 + "/js/tds-common.js")
			.check(status.in(200,310)),
            http("request_122")
			.get(uri1 + "/js/asset.comment.js")
			.check(status.in(200,310)),
            http("request_123")
			.get(uri1 + "/js/entity.crud.js")
			.check(status.in(200,310)),
            http("request_124")
			.get(uri1 + "/js/model.manufacturer.js")
			.check(status.in(200,310)),
            http("request_125")
			.get(uri1 + "/js/angular/angular.min.js")
			.check(status.in(200,310)),
            http("request_126")
			.get(uri1 + "/js/angular/plugins/angular-resource.js")
			.check(status.in(200,310)),
            http("request_127")
			.get(uri1 + "/js/angular/plugins/angular-ui.js")
			.check(status.in(200,310)),
            http("request_128")
			.get(uri1 + "/static/components/core/core.js"),
            http("request_129")
			.get(uri1 + "/static/components/asset/asset.js"),
            http("request_130")
			.get(uri1 + "/static/plugins/jqgrid-3.8.0.1/js/jqgrid/i18n/grid.locale-en.js"),
            http("request_131")
			.get(uri1 + "/static/components/comment/comment.js"),
            http("request_132")
			.get(uri1 + "/js/cabling.js")
			.check(status.in(200,310)),
            http("request_133")
			.get(uri1 + "/static/plugins/jqgrid-3.8.0.1/js/jqgrid/jquery.jqGrid.fluid.js"),
            http("request_134")
			.get(uri1 + "/static/plugins/jqgrid-3.8.0.1/js/jqgrid/jquery.jqGrid.min.js"),
            http("request_135")
			.get(uri1 + "/js/angular/plugins/ui-bootstrap-tpls-0.10.0.min.js")
			.check(status.in(200,310)),
            http("request_136")
			.get(uri1 + "/js/jqgrid-support.js")
			.check(status.in(200,310)),
            http("request_137")
			.get(uri1 + "/js/bootstrap.js")
			.check(status.in(200,310)),
            http("request_138")
			.get(uri1 + "/js/angular/plugins/ngGrid/ng-grid-layout.js")
			.check(status.in(200,310)),
            http("request_139")
			.get(uri1 + "/js/angular/plugins/ngGrid/ng-grid-2.0.7.min.js")
			.check(status.in(200,310)),
            http("request_140")
			.get(uri1 + "/js/tdsmenu.js")
			.check(status.in(200,310)),
            http("request_141")
			.get(uri1 + "/static/images/TMMenuLogo.png")
			.headers(headers_5),
            http("request_142")
			.get(uri1 + "/static/icons/comment_add.png")
			.headers(headers_5),
            http("request_143")
			.get(uri1 + "/static/icons/information.png")
			.headers(headers_5),
            http("request_144")
			.get(uri1 + "/static/plugins/jquery-ui-1.8.15/jquery-ui/themes/ui-lightness/images/ui-bg_gloss-wave_35_f6a828_500x100.png")
			.headers(headers_5),
            http("request_145")
			.get(uri1 + "/static/plugins/jquery-ui-1.8.15/jquery-ui/themes/ui-lightness/images/ui-bg_highlight-soft_100_eeeeee_1x100.png")
			.headers(headers_5),
            http("request_146")
			.get(uri1 + "/static/plugins/jquery-ui-1.8.15/jquery-ui/themes/ui-lightness/images/ui-icons_222222_256x240.png")
			.headers(headers_5),
            http("request_147")
			.get(uri1 + "/static/images/skin/shadow.jpg")
			.headers(headers_5),
            http("request_148")
			.get(uri1 + "/static/icons/database_edit.png")
			.headers(headers_5),
            http("request_149")
			.get(uri1 + "/static/icons/database_save.png")
			.headers(headers_5),
            http("request_150")
			.get(uri1 + "/components/core/action-button-template.html")
			.headers(headers_6)
			.check(status.in(200,310)),
            http("request_151")
			.get(uri1 + "/static/plugins/jquery-ui-1.8.15/jquery-ui/themes/ui-lightness/images/ui-bg_glass_65_ffffff_1x400.png")
			.headers(headers_5),
            http("request_152")
			.get(uri1 + "/static/plugins/jquery-ui-1.8.15/jquery-ui/themes/ui-lightness/images/ui-bg_diagonals-thick_20_666666_40x40.png")
			.headers(headers_5),
            http("request_153")
			.get(uri1 + "/static/plugins/jquery-ui-1.8.15/jquery-ui/themes/ui-lightness/images/ui-bg_glass_100_f6f6f6_1x400.png")
			.headers(headers_5),
            http("request_154")
			.get(uri1 + "/static/plugins/jquery-ui-1.8.15/jquery-ui/themes/ui-lightness/images/ui-icons_ef8c08_256x240.png")
			.headers(headers_5),
            http("request_155")
			.get(uri1 + "/static/plugins/jquery-ui-1.8.15/jquery-ui/themes/ui-lightness/images/ui-icons_ffffff_256x240.png")
			.headers(headers_5),
            http("request_156")
			.get(uri1 + "/images/select2Arrow.png")
			.headers(headers_5)
			.check(status.in(200,310)),
            http("request_157")
			.get(uri1 + "/static/icons/database_delete.png")
			.headers(headers_5),
            http("request_158")
			.get(uri1 + "/icons/timeline_marker.png")
			.headers(headers_5)
			.check(status.in(200,310)),
            http("request_159")
			.get(uri1 + "/icons/tds_task_graph.png")
			.headers(headers_5)
			.check(status.in(200,310)),
            http("request_160")
			.get(uri1 + "/static/images/favicon.ico")
			.headers(headers_84),
            http("request_161")
			.get(uri1 + "/assetEntity/listTaskJSON?moveEvent=0&justRemaining=0&justMyTasks=0&filter=&comment=&taskNumber=&assetEntity=&assetType=&dueDate=&status=&assignedTo=&role=&category=&viewUnpublished=1&_search=false&nd=1435775453524&rows=25&page=1&sidx=&sord=asc")
			.headers(headers_4),
            http("request_162")
			.get(uri1 + "/assetEntity/listTasks?initSession=true")
			.headers(headers_5)))

	setUp(scn.inject(atOnceUsers(1))).protocols(httpProtocol)
}