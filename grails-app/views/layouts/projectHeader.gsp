<%@ page import="net.transitionmanager.security.Permission" %>

<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
	<head>
		<meta http-equiv="Content-Type" content="text/html; charset=iso-8859-1" />
		<title><g:layoutTitle default="Grails" /></title>

		<g:render template="../layouts/standardResources" />

		<g:layoutHead />

		<asset:stylesheet href="css/dropDown.css" />
		<asset:stylesheet href="css/daterangepicker-bs3.css" />
		<g:javascript src="tdsmenu.js" />
		<g:javascript src="PasswordValidation.js" />

		<script type="text/javascript">
			$(document).ready(function() {
				$("#personDialog").dialog({ autoOpen: false });
				$("#userPrefDivId").dialog({ autoOpen: false });

				// Due to some issue with textarea overriding the value at intial load
				$('textarea').each(function(){
					$(this).val($(this).text());
				});
				$(".headerClass").mouseover(function(){
					$(this).parent().find('a').addClass('mouseover');
					$(this).parent().find('a').removeClass('mouseout');
				});
				$(".headerClass").mouseout(function(){
					if (!$(this).parent().find(".megamenu").is(":visible")) {
						$(this).parent().find('a').removeClass('mouseover');
					} else {
						$('.headerClass').removeClass('mouseover');
					}
				});

				$.datepicker.setDefaults({dateFormat: tdsCommon.jQueryDateFormat()});
				$.datetimepicker.setDefaults({dateFormat: tdsCommon.jQueryDateFormat()});
			});

			function updateEventHeader( e ){
				var newsAndStatus = eval("(" + e.responseText + ")")
				$("#head_mycrawlerId").html(newsAndStatus[0].news);
				$("#head_crawler").addClass(newsAndStatus[0].cssClass)
				$("#moveEventStatus").html(newsAndStatus[0].status)
			}
		</script>
	</head>
<%
def moveBundle = tds.currentMoveBundle() ?: null
def moveEvent = tds.currentMoveEvent() ?: null
def room = tds.currentRoom() ?: null
def currProject = tds.currentProject() ?: null
def person = tds.currentPerson() ?: null
String partyGroup = tds.partyGroup() ?: null
boolean isIE6 = tds.isIE6()
boolean isIE7 = tds.isIE7()
String setImage = tds.setImage() ?: null
def userLogin = tds.userLogin() ?: null
int minPasswordLength = tds.minPasswordLength()
%>

	<body>
		<div class="main_body">
			<input id="contextPath" type="hidden" value="${request.contextPath}" >
			<input id="tzId" type="hidden" value="${tds.timeZone()}"/>
			<input id="userDTFormat" type="hidden" value="${tds.dateFormat()}"/>
			<div class="tds_header">
				<div class="header_left">
					<g:if test="${setImage}">
						<img src="${createLink(controller:'project', action:'showImage', id:setImage)}" style="height: 30px;"/>
					</g:if>
					<g:else>
						<asset:image src="images/TMMenuLogo.png" style="float: left;border: 0px;height: 30px;" />
					</g:else>
				</div>
				<div class="title">&nbsp;TransitionManager&trade;
					<g:if test="${currProject}"> - ${currProject.name} </g:if>
					<g:if test="${moveEvent}"> : ${moveEvent.name}</g:if>
					<g:if test="${moveBundle}"> : ${moveBundle.name}</g:if>
				</div>
				<div class="header_right" id="userMenuId"><br />
					<div style="font-weight: bold;">
						<sec:ifLoggedIn>
							<strong>
								<div style="float: left;">
									<g:if test="${isIE6 || isIE7}">
										<span><asset:image src="images/skin/warning.png" style="width: 14px;height: 14px;float: left;padding-right: 3px;"/></span>
									</g:if>
									<a class="headerClass" onmouseover="hoverMegaMenu('#userMegaMenu')" href="javascript:showMegaMenu('#userMegaMenu')" style="float:left;text-decoration:none;display:inline">
										&nbsp;<span id="loginUserId">${tds.currentUsername()}</span>
									</a>
								</div>
							</strong>
							&nbsp;&nbsp;<g:link controller="auth" action="signOut"></g:link>
						</sec:ifLoggedIn>
					</div>
				</div>
			</div>

      <g:if test="${currProject}">
	      <div class="menu2">
	      <ul>
			<tds:hasPermission permission="${Permission.AdminMenuView}">
			<li id="adminMenuId" class="menuLiIndex"><a class="home menuhideright headerClass" onmouseover="hoverMegaMenu('#adminMegaMenu')" onmouseout="clearTipTimer()" href="javascript:showMegaMenu('#adminMegaMenu')">Admin</a>
				<div class="megamenu admin inActive" id="adminMegaMenu">
					<table class="mmtable room_rack"><tr>
					<td style="vertical-align:top" nowrap="nowrap"><span class="megamenuSection">Administration</span><br />
						<ul >
							<li><g:link class="mmlink" controller="admin" action="home" onclick="hideMegaMenu('adminMegaMenu')">Admin Portal</g:link> </li>
							<tds:hasPermission permission="${Permission.RolePermissionView}">
							<li><g:link class="mmlink" controller="permissions" action="show" onclick="hideMegaMenu('adminMegaMenu')">Role Permissions</g:link> </li>
							</tds:hasPermission>
							<li><g:link class="mmlink" controller="assetEntity" action="assetOptions" onclick="hideMegaMenu('adminMegaMenu')">Asset Options</g:link> </li>
							<tds:hasPermission permission="${Permission.HelpMenuView}">
							<li><a class="mmlink" href="javascript:window.open('https://ops.tdsops.com/twiki/bin/view/Main/DataCenterMoves/TMAdminPortal?cover=print','help');" onclick="hideMegaMenu('adminMegaMenu')">help</a></li>
							</tds:hasPermission>

						</ul>
					</td>
					<td style="vertical-align:top"><span class="megamenuSection">Manage Clients</span><br />
						<ul >
							<li><g:link class="mmlink" controller="partyGroup" action="list" params="[active:'active',tag_s_2_name:'asc']" id="${partyGroup}" onclick="hideMegaMenu('adminMegaMenu')">List Companies</g:link></li>
							<li><g:link class="mmlink" controller="person" id="${partyGroup}" onclick="hideMegaMenu('adminMegaMenu')">List Staff</g:link></li>
							<tds:hasPermission permission="${Permission.UserView}">
							<li><g:link class="mmlink" controller="userLogin" id="${partyGroup}" onclick="hideMegaMenu('adminMegaMenu')">List Users</g:link></li>
							</tds:hasPermission>
							<tds:hasPermission permission="${Permission.PersonImport}">
							<li><g:link class="mmlink" controller="admin" action="importAccounts" onclick="hideMegaMenu('adminMegaMenu')">Import Accounts</g:link></li>
							</tds:hasPermission>
							<tds:hasPermission permission="${Permission.PersonExport}">
							<li><g:link class="mmlink" controller="admin" action="exportAccounts" onclick="hideMegaMenu('adminMegaMenu')">Export Accounts</g:link></li>
							</tds:hasPermission>
							<tds:hasPermission permission="${Permission.HelpMenuView}">
							<li><a class="mmlink" href="javascript:window.open('https://ops.tdsops.com/twiki/bin/view/Main/DataCenterMoves/TMCreatePerson?cover=print','help');" onclick="hideMegaMenu('adminMegaMenu')">help</a></li>
							</tds:hasPermission>
						</ul>
					</td>
					<td style="vertical-align:top"><span class="megamenuSection">Manage Workflows</span><br />
						<ul >
							<li><g:link class="mmlink" controller="workflow" action="home" onclick="hideMegaMenu('adminMegaMenu')">List Workflows </g:link> </li>
							<tds:hasPermission permission="${Permission.HelpMenuView}">
							<li><a class="mmlink" href="javascript:window.open('https://ops.tdsops.com/twiki/bin/view/Main/DataCenterMoves/TMManageWorkflows?cover=print','help');" onclick="hideMegaMenu('adminMegaMenu')">help</a></li>
							</tds:hasPermission>
						</ul>
					</td>
					<td style="vertical-align:top"><span class="megamenuSection">Manage Model Library</span><br />
						<ul >
							<li><g:link class="mmlink" controller="manufacturer" id="${partyGroup}" onclick="hideMegaMenu('adminMegaMenu')">List Manufacturers</g:link></li>
							<li><g:link class="mmlink" controller="model" id="${partyGroup}" onclick="hideMegaMenu('adminMegaMenu')">List Models</g:link></li>
							<li><g:link class="mmlink" controller="model" action="importExport" onclick="hideMegaMenu('adminMegaMenu')">Sync Libraries</g:link></li>
							<tds:hasPermission permission="${Permission.HelpMenuView}">
							<li><a class="mmlink" href="javascript:window.open('https://ops.tdsops.com/twiki/bin/view/Main/DataCenterMoves/TMModelLibrary?cover=print','help');" onclick="hideMegaMenu('adminMegaMenu')">help</a></li>
							</tds:hasPermission>
						</ul>
					</td>
					</tr></table>
				</div>
			</li>
			</tds:hasPermission>

			<li id="projectMenuId" class="menuLiIndex" style="position:relative; float: left;" ><a class="home headerClass" onmouseover="hoverMegaMenu('#projectMegaMenu')" onmouseout="clearTipTimer()" href="javascript:showMegaMenu('#projectMegaMenu')">Projects</a>
				<div class="megamenu client inActive" id="projectMegaMenu">
					<table class="mmtable"><tr>
						<td style="vertical-align:top">
							<ul>
								<li><g:link class="mmlink" controller="project" action="list" params="[active:'active']" onclick="hideMegaMenu('projectMegaMenu')">List Projects</g:link></li>
							</ul>
					<g:if test="${currProject}">
						<span class="megamenuSection"> </span>
							<ul >
								<li><g:link class="mmlink" controller="projectUtil" onclick="hideMegaMenu('projectMegaMenu')"><g:if test="${currProject.name.size()>20}">${currProject.name.substring(0,20)+'...'}</g:if><g:else>${currProject.name}</g:else> Details</g:link></li>
								<li><g:link class="mmlink" controller="person" action="manageProjectStaff"  onclick="hideMegaMenu('projectMegaMenu')">Project Staff</g:link></li>
								<tds:hasPermission permission="${Permission.UserSendActivations}">
									<li><g:link class="mmlink" controller="project" action="userActivationEmailsForm" onclick="hideMegaMenu('projectMegaMenu')">User Activation Emails</g:link> </li>
								</tds:hasPermission>
								<li><g:link class="mmlink" controller="module" action="fieldsettings" id="list" onclick="hideMegaMenu('projectMegaMenu')">Asset Field Settings</g:link> </li>
					</g:if>
					<g:else>
						<span class="megamenuSection">No Project Selected</strong></span><br />
							<ul>
					</g:else>
							<tds:hasPermission permission="${Permission.HelpMenuView}">
							<li><a class="mmlink" href="javascript:window.open('https://ops.tdsops.com/twiki/bin/view/Main/DataCenterMoves/TMProjectStaff?cover=print','help');"  onclick="hideMegaMenu('projectMegaMenu')">help</a></li>
							</tds:hasPermission>
						</ul>
					</td>
					</tr></table>
				</div>
			</li>
			<li id="roomMenuId" class="menuLiIndex" style="position:relative; float: left;"><a class="home headerClass" onmouseover="hoverMegaMenu('#racksMegaMenu')" onmouseout="clearTipTimer()" href="javascript:showMegaMenu('#racksMegaMenu')">Locations</a>
				<div class="megamenu rooms inActive" id="racksMegaMenu">
					<table class="mmtable room_rack" ><tr>
					<td style="vertical-align:top">
						<ul >
							<li><g:link class="mmlink" params="[viewType:'list']" controller="room"  onclick="hideMegaMenu('racksMegaMenu')">List Locations</g:link></li>
							<g:if test="${roomId}">
								<li><g:link class="mmlink" params="[roomId:roomId]" controller="room" onclick="hideMegaMenu('racksMegaMenu')">Room ${room?.location}/${room?.roomName}</g:link></li>
							</g:if>
							<tds:hasPermission permission="${Permission.AssetEdit}">
							<li><g:link class="mmlink" controller="rackLayouts" action="create" onclick="hideMegaMenu('racksMegaMenu')">Rack Elevations</g:link></li>
							</tds:hasPermission>
							<tds:hasPermission permission="${Permission.HelpMenuView}">
								<li><a class="mmlink" href="javascript:window.open('https://ops.tdsops.com/twiki/bin/view/Main/DataCenterMoves/TMRooms?cover=print','help');">help</a></li>
							</tds:hasPermission>
						</ul>
					</td>
					</tr></table>
				</div>
			</li>
	        <tds:hasPermission permission="${Permission.AssetMenuView}">
			<li id="assetMenuId" class="menuLiIndex" style="position:relative; float:left;"><a class="home headerClass" onmouseover="hoverMegaMenu('#assetMegaMenu')" onmouseout="clearTipTimer()" href="javascript:showMegaMenu('#assetMegaMenu')" >Assets</a>
				<div class="megamenu rooms inActive" id="assetMegaMenu" >
					<table class="mmtable room_rack"><tr>
					<td style="vertical-align:top"><span class="megamenuSection">Assets</span><br />
						<ul>
							<li>
								<g:link class="mmlink" controller="assetEntity" action="assetSummary">
									<g:img uri="${resource(dir:'icons',file:'application_view_columns.png')}" width="16" height="16"/>
									<div>Summary Table</div>
								</g:link>
							</li>
							<li>
								<g:link class="mmlink" controller="module" action="asset" id="views">
									<g:img uri="${resource(dir:'icons',file:'magnifier.png')}" width="16" height="16"/>
									<div>View Manager</div>
								</g:link>
							</li>
							<li>
								<g:link class="mmlink" controller="application" action="list" onclick="hideMegaMenu('assetMegaMenu')">
									<tds:svgIcon name="application" width="16" height="16" />
									<div>Applications</div>
								</g:link>
							</li>
							<li>
								<g:link class="mmlink" controller="assetEntity" action="list" params="[filter:'server']" onclick="hideMegaMenu('assetMegaMenu')">
									<tds:svgIcon name="serverPhysical" width="16" height="16" />
									<div>Servers</div>
								</g:link>
							</li>
							<li>
								<g:link class="mmlink" controller="assetEntity" action="list" params="[filter:'all']" onclick="hideMegaMenu('assetMegaMenu')">
									<tds:svgIcon name="other" width="16" height="16" />
									<div>All Devices</div>
								</g:link>
							</li>
							<li>
								<g:link class="mmlink" controller="database"    action="list" onclick="hideMegaMenu('assetMegaMenu')">
									<tds:svgIcon name="database" width="16" height="16" />
									<div>Databases</div>
								</g:link>
							</li>
							<li>
								<g:link class="mmlink" controller="assetEntity" action="list" params="[filter:'storage']" onclick="hideMegaMenu('assetMegaMenu')">
									<tds:svgIcon name="storagePhysical" width="16" height="16" />
									<div>Storage-Devices</div>
								</g:link>
							</li>
							<li>
								<g:link class="mmlink" controller="files"       action="list" onclick="hideMegaMenu('assetMegaMenu')">
									<tds:svgIcon name="storageLogical" width="16" height="16" />
									<div>Storage-Logical</div>
								</g:link>
							</li>
							<li>
								<g:link class="mmlink" controller="assetEntity" action="listComment" onclick="hideMegaMenu('assetMegaMenu')">
									<g:img uri="${resource(dir:'icons',file:'comments.png')}" width="16" height="16"/>
									<div>Comments</div>
								</g:link>
							</li>
							<li>
								<g:link class="mmlink" controller="assetEntity" action="listDependencies" onclick="hideMegaMenu('assetMegaMenu')">
									<g:img uri="${resource(dir:'icons',file:'bricks.png')}" width="16" height="16"/>
									<div>Dependencies</div>
								</g:link>
							</li>
							<tds:hasPermission permission="${Permission.DepAnalyzerView}">
							  <li>
							  	<g:link class="mmlink" controller="moveBundle" action="dependencyConsole" onclick="hideMegaMenu('assetMegaMenu')">
									<asset:image src="icons/brick_magnify.png" width="16" height="16"/>
										<div>Dependency Analyzer</div>
							  	</g:link>
							  </li>
							</tds:hasPermission>
							<tds:hasPermission permission="${Permission.ArchitectureView}">
							  <li>
							  	<g:link class="mmlink" controller="assetEntity" action="architectureViewer" onclick="hideMegaMenu('assetMegaMenu')">
							  		<asset:image src="icons/chart_organisation.png" width="16" height="16"/>
									<div>Architecture Graph</div>
							  	</g:link>
							  </li>
							</tds:hasPermission>
							<tds:hasPermission permission="${Permission.HelpMenuView}">
							<li><a class="mmlink" href="javascript:window.open('https://ops.tdsops.com/twiki/bin/view/Main/DataCenterMoves/TMAssetOverview?cover=print','help');" onclick="hideMegaMenu('assetMegaMenu')">help</a></li>
							</tds:hasPermission>
						</ul>
					</td>
					<td style="vertical-align:top"><span class="megamenuSection">Manage Data</span><br />

						<ul>
							<tds:hasPermission permission="${Permission.AssetImport}">
							<li><g:link class="mmlink" controller="assetEntity" action="assetImport"  onclick="hideMegaMenu('assetMegaMenu')">Import Assets</g:link></li>
							</tds:hasPermission>
							<tds:hasPermission permission="${Permission.AssetImport}">
							<li><g:link class="mmlink" controller="dataTransferBatch" action="index" onclick="hideMegaMenu('assetMegaMenu')">Manage Batches</g:link></li>
							</tds:hasPermission>
							<tds:hasPermission permission="${Permission.AssetExport}">
							<li><g:link class="mmlink" controller="assetEntity" action="exportAssets"  onclick="hideMegaMenu('assetMegaMenu')">Export Assets</g:link></li>
							</tds:hasPermission>
							<tds:hasPermission permission="${Permission.HelpMenuView}">
							<li><a class="mmlink" href="javascript:window.open('https://ops.tdsops.com/twiki/bin/view/Main/DataCenterMoves/TMImportExport?cover=print','help');" onclick="hideMegaMenu('assetMegaMenu')">help</a></li>
							</tds:hasPermission>
						</ul>
					</td>
					</tr></table>
				</div>
			</li>
			</tds:hasPermission>
			<tds:hasPermission permission="${Permission.EventMenuView}">
			<li id="eventMenuId" class="menuLiIndex" style="position:relative; float: left;"><a class="home headerClass" onmouseover="hoverMegaMenu('#bundleMegaMenu')" onmouseout="clearTipTimer()" href="javascript:showMegaMenu('#bundleMegaMenu')">Planning</a>
				<div class="megamenu rooms inActive" id="bundleMegaMenu">
					<table class="mmtable " ><tr>
					<td style="vertical-align:top"><span class="megamenuSection">Events</span><br />
						<ul>
							<li><g:link class="mmlink" controller="moveEvent" action="list" onclick="hideMegaMenu('bundleMegaMenu')" >List Events</g:link> </li>
							<g:if test="${currProject && moveEvent}">
								<span class="megamenuSection"> </span>
								<li style="white-space:nowrap;"><g:link class="mmlink" controller="moveEvent" action="show" id="${moveEvent.id}" onclick="hideMegaMenu('bundleMegaMenu')">${moveEvent.name} Event Details</g:link></li>
							</g:if>
							<tds:hasPermission permission="${Permission.ShowListNews}">
							<li><g:link class="mmlink" controller="newsEditor"  onclick="hideMegaMenu('consoleMegaMenu')">List Event News</g:link></li>
							</tds:hasPermission>
							<tds:hasPermission permission="${Permission.ReportViewEventPrep}">
							<li style="white-space:nowrap;"><g:link class="mmlink" controller="reports" action="preMoveCheckList" onclick="hideMegaMenu('bundleMegaMenu')">Pre-event Checklist</g:link></li>
							</tds:hasPermission>
							<li style="white-space:nowrap;"><g:link class="mmlink" controller="moveEvent" action="exportRunbook" onclick="hideMegaMenu('bundleMegaMenu')">Export Runbook</g:link></li>
							<tds:hasPermission permission="${Permission.HelpMenuView}">
							<li><a class="mmlink" href="javascript:window.open('https://ops.tdsops.com/twiki/bin/view/Main/DataCenterMoves/TMEvents?cover=print','help');" onclick="hideMegaMenu('bundleMegaMenu')">help</a></li>
							</tds:hasPermission>
						</ul>
					</td>
					<td style="vertical-align:top"><span class="megamenuSection">Bundles</span><br />
						<ul>
							<li><g:link class="mmlink" controller="moveBundle" action="list">List Bundles</g:link> </li>
					<g:if test="${currProject && moveBundle}">
							<span class="megamenuSection"> </span>
							<li><g:link class="mmlink" controller="moveBundle" action="show"  onclick="hideMegaMenu('bundleMegaMenu')">${moveBundle.name} Bundle Details</g:link></li>
					</g:if>
							<tds:hasPermission permission="${Permission.HelpMenuView}">
							<li><a class="mmlink" href="javascript:window.open('https://ops.tdsops.com/twiki/bin/view/Main/DataCenterMoves/TMBundles?cover=print','help');" onclick="hideMegaMenu('bundleMegaMenu')">help</a></li>
							</tds:hasPermission>
						</ul>
					</td>
					</tr></table>
				</div>
			</li>
			</tds:hasPermission>

			<%-- Task Menu --%>
			<tds:hasPermission permission="${Permission.TaskView}">
			<li id="teamMenuId" class="menuLiIndex" style="position:relative; float:left;"><a class="home headerClass" onmouseover="hoverMegaMenu('#teamMegaMenu')" onmouseout="clearTipTimer()" href="javascript:showMegaMenu('#teamMegaMenu')">Tasks</a>
				<div class="megamenu rooms inActive" id="teamMegaMenu" >
					<table class="mmtable"><tr>
					<td style="vertical-align:top"><span class="megamenuSection">Tasks</span><br />
						<ul>
							<li><a class="mmlink" id="MyTasksMenuId" href="/tdstm/task/listUserTasks" onclick="hideMegaMenu('teamMegaMenu')">My Tasks
							(<span id="todoCountProjectId">&nbsp;</span>)</a></li>
							<tds:hasPermission permission="${Permission.TaskManagerView}">
							<li><g:link class="mmlink" controller="assetEntity" action="listTasks"  params="[initSession:true]" onclick="hideMegaMenu('teamMegaMenu')">Task Manager</g:link></li>
							</tds:hasPermission>

							<tds:hasPermission permission="${Permission.TaskGraphView}">
							<li><g:link class="mmlink" controller="task" action="taskGraph"  params="[initSession:true]" onclick="hideMegaMenu('teamMegaMenu')">Task Graph</g:link></li>
							</tds:hasPermission>

							<tds:hasPermission permission="${Permission.TaskTimelineView}">
							<li><g:link class="mmlink" controller="task" action="taskTimeline" onclick="hideMegaMenu('teamMegaMenu')">Task Timeline</g:link></li>
							</tds:hasPermission>

							<tds:hasPermission permission="${Permission.RecipeView}">
							<li><g:link class="mmlink" controller="cookbook" action="index" onclick="hideMegaMenu('teamMegaMenu')">Cookbook</g:link></li>
							</tds:hasPermission>

							<tds:hasPermission permission="${Permission.RecipeGenerateTasks}">
							<li><a href="/tdstm/cookbook/index#/generationHistory">Generation History</a></li>
							</tds:hasPermission>

							<tds:hasPermission permission="${Permission.HelpMenuView}">
							<li><a class="mmlink" href="javascript:window.open('https://ops.tdsops.com/twiki/bin/view/Main/DataCenterMoves/TMTaskOverview?cover=print','help');" onclick="hideMegaMenu('consoleMegaMenu')">help</a></li>
							</tds:hasPermission>
						</ul>
					</td>
					</tr></table>
				</div>
			</li>
            </tds:hasPermission>

			<tds:hasPermission permission="${Permission.DashboardMenuView}">
			<li id="dashboardMenuId" class="menuLiIndex" style="position:relative; float:left;"><a class="home headerClass" onmouseover="hoverMegaMenu('#dashboardMegaMenu')" onmouseout="clearTipTimer()" href="javascript:showMegaMenu('#dashboardMegaMenu')">Dashboards</a>
				<div class="megamenu rooms inActive" id="dashboardMegaMenu">
					<table class="mmtable"><tr>
					<td style="vertical-align:top"><span class="megamenuSection">Live Dashboards</span><br />
						<ul>
							<li><g:link class="home mmlink" controller="dashboard" action="userPortal" onclick="hideMegaMenu('dashboardMegaMenu')">User Dashboard</g:link></li>
							<tds:hasPermission permission="${Permission.ReportViewPlanning}">
							<li><g:link class="home mmlink" controller="moveBundle" action="planningStats" onclick="hideMegaMenu('dashboardMegaMenu')">Planning Dashboard</g:link></li>
							</tds:hasPermission>
							<li><g:link class="home mmlink" controller="dashboard" onclick="hideMegaMenu('dashboardMegaMenu')">Event Dashboard</g:link></li>
							<%-- Removed until this report will be implemented using tasks
							<tds:hasPermission permission="${Permission.howCartTracker}">
							<li><g:link class="mmlink" controller="cartTracking" action="cartTracking"  onclick="hideMegaMenu('consoleMegaMenu')">Cart Tracker</g:link></li>
							</tds:hasPermission-->
							--%>
							<tds:hasPermission permission="${Permission.HelpMenuView}">
							<li><a class="mmlink" href="javascript:window.open('https://ops.tdsops.com/twiki/bin/view/Main/DataCenterMoves/TMDashboardOverview?cover=print','help');" onclick="hideMegaMenu('dashboardMegaMenu')">help</a></li>
							</tds:hasPermission>
						</ul>
					</td>
					</tr></table>
				</div>
			</li>
			</tds:hasPermission>

			<tds:hasPermission permission="${Permission.ReportMenuView}">
			<li id="reportsMenuId" class="menuLiIndex" style="position:relative; float: left;"><a class="home headerClass" onmouseover="hoverMegaMenu('#reportsMegaMenu')" onmouseout="clearTipTimer()" href="javascript:showMegaMenu('#reportsMegaMenu')">Reports</a>
				<div class="megamenu reports inActive" id="reportsMegaMenu">
					<table class="mmtable "><tr>
					<tds:hasPermission permission="${Permission.ReportViewDiscovery}">
					<td style="vertical-align:top"><span class="megamenuSection">Discovery</span><br />
						<ul>
							<li><a href="/tdstm/reports/retrieveBundleListForReportDialog?reportId=CablingConflict" class="home mmlink" onclick="hideMegaMenu('reportsMegaMenu')">Cabling Conflict</a> </li>
							<li><a href="/tdstm/reports/retrieveBundleListForReportDialog?reportId=CablingData" class="home mmlink" onclick="hideMegaMenu('reportsMegaMenu')">Cabling Data</a> </li>
							<li><a href="/tdstm/reports/powerReport" class="home mmlink" onclick="hideMegaMenu('reportsMegaMenu')">Power</a> </li>
							<tds:hasPermission permission="${Permission.HelpMenuView}">
							<li><a class="mmlink" href="javascript:window.open('https://ops.tdsops.com/twiki/bin/view/Main/DataCenterMoves/TranManHelp?cover=print','help');" onclick="hideMegaMenu('reportsMegaMenu')">help</a></li>
							</tds:hasPermission>
						</ul>
					</td>
					</tds:hasPermission>
					<td style="vertical-align:top"><span class="megamenuSection">Planning</span><br />
						<ul>
							<li><a href="/tdstm/reports/applicationProfiles" class="home mmlink" onclick="hideMegaMenu('reportsMegaMenu')">Application Profiles</a> </li>
							<li><a href="/tdstm/reports/applicationConflicts" class="home mmlink" onclick="hideMegaMenu('reportsMegaMenu')">Application Conflicts</a> </li>
							<li><a href="/tdstm/reports/serverConflicts" class="home mmlink" onclick="hideMegaMenu('reportsMegaMenu')">Server Conflicts</a> </li>
							<li><a href="/tdstm/reports/databaseConflicts" class="home mmlink" onclick="hideMegaMenu('reportsMegaMenu')">Database Conflicts</a> </li>
							<tds:hasPermission permission="${Permission.ReportViewPlanning}">
								<li><a href="/tdstm/reports/retrieveBundleListForReportDialog?reportId=Task+Report"  class="home mmlink" onclick="hideMegaMenu('reportsMegaMenu')">Task Report</a> </li>
							</tds:hasPermission>
							<tds:hasPermission permission="${Permission.ReportViewProjectDailyMetrics}">
								<li><a href="/tdstm/reports/projectActivityMetrics" class="home mmlink" onclick="hideMegaMenu('reportsMegaMenu')">Activity Metrics</a> </li>
							</tds:hasPermission>
						</ul>
					</td>
					<tds:hasPermission permission="${Permission.ReportViewEventPrep}">
					<td style="vertical-align:top"><span class="megamenuSection">Event Prep</span><br />
						<ul >
							<tds:hasPermission permission="${Permission.ReportViewEventPrep}">
							<li><a href="/tdstm/reports/preMoveCheckList" class="home mmlink" onclick="hideMegaMenu('reportsMegaMenu')">Pre-event Checklist</a> </li>
							</tds:hasPermission>
							<li><a href="/tdstm/reports/retrieveBundleListForReportDialog?reportId=Asset+Tag" class="home mmlink" onclick="hideMegaMenu('reportsMegaMenu')">Asset Tags</a> </li>
							<li><a href="/tdstm/reports/retrieveBundleListForReportDialog?reportId=Transportation+Asset+List" class="home mmlink" onclick="hideMegaMenu('reportsMegaMenu')">Transport Worksheets</a></li>
							<tds:hasPermission permission="${Permission.HelpMenuView}">
							<li><a class="mmlink" href="javascript:window.open('https://ops.tdsops.com/twiki/bin/view/Main/DataCenterMoves/TranManHelp?cover=print','help');" onclick="hideMegaMenu('reportsMegaMenu')">help</a></li>
							</tds:hasPermission>
						</ul>
					</td>
					</tds:hasPermission>
					<tds:hasPermission permission="${Permission.ReportViewEventDay}">
					<td style="vertical-align:top"><span class="megamenuSection">Event Day</span><br />
						<ul>
							<li><a href="/tdstm/reports/applicationMigrationReport" class="home mmlink" onclick="hideMegaMenu('reportsMegaMenu')">Application Event Results</a> </li>
							<li><a href="/tdstm/reports/retrieveBundleListForReportDialog?reportId=Issue+Report" class="home mmlink" onclick="hideMegaMenu('reportsMegaMenu')">Issue Report</a> </li>
							<li><a href="/tdstm/reports/retrieveBundleListForReportDialog?reportId=CablingQA" class="home mmlink" onclick="hideMegaMenu('reportsMegaMenu')">Cabling QA</a> </li>
							<tds:hasPermission permission="${Permission.HelpMenuView}">
							<li><a class="mmlink" href="javascript:window.open('https://ops.tdsops.com/twiki/bin/view/Main/DataCenterMoves/TranManHelp?cover=print','help');" onclick="hideMegaMenu('reportsMegaMenu')">help</a></li>
							</tds:hasPermission>
						</ul>
					</td>
					</tds:hasPermission>
					</tr></table>
				</div>
	        </li>
	        </tds:hasPermission>

	      </ul>
		</div>
		<div class="megamenu inActive" id="userMegaMenu" style="width:255px">
			<table class="mmtable"><tr>
			<td style="vertical-align:top"><span class="megamenuSection">${tds.currentPersonName()}</span><br />
				<ul>
					<li><g:remoteLink controller="person" action="retrievePersonDetails" id="${tds.currentPersonId()}" onComplete="updatePersonDetails(XMLHttpRequest)">Account Details...</g:remoteLink></li>
					<li><a href="#" style="cursor: pointer;" id="editTimezoneId" name="${userLogin.username}" onclick="UserPreference.editDateAndTimezone()">Date and Timezone</a></li>
					<li><a href="#" style="cursor: pointer;" id="resetPreferenceId" name="${userLogin.username}" onclick="UserPreference.editPreference()">Edit preferences</a></li>
					<li><g:link class="home mmlink" controller="task" action="listUserTasks" params="[viewMode:'mobile',tab:tab]">Use Mobile Site</g:link></li>
				</ul>
			</td>
			<td style="vertical-align:top">
				<ul>
					<li><g:link class="mmlink" controller="auth" action="signOut">Sign out</g:link></li>
				</ul>
			</td>
			</tr></table>
		</div>

		<%-- Include the News crawler if there is an event in progress --%>
		<g:if test="${currProject?.runbookOn && moveEvent && (moveEvent?.newsBarMode == 'on' || (moveEvent?.newsBarMode == 'auto' && moveEvent?.estStartTime))}">
			<g:if test="${moveEvent?.newsBarMode == 'auto'}">
				<g:if test="${moveEvent?.estStartTime.getTime() < new Date().getTime()}">
					<g:set var="showNewsBar" value="true" />
				</g:if>
			</g:if>
			<g:elseif test="moveEvent?.newsBarMode == 'on'">
				<g:set var="showNewsBar" value="true" />
			</g:elseif>

			<g:if test="${showNewsBar}">
				<div class="menu3" id="head_crawler" >
					<div id="crawlerHead">${moveEvent.name} Event Status <span id="moveEventStatus"></span>. News: </div>
					<div id="head_mycrawler"><div id="head_mycrawlerId" style="width: 1200px; height:25px; vertical-align:bottom" > </div></div>
				</div>
				<script type="text/javascript">

				${remoteFunction(controller:'moveEvent', action:'retrieveMoveEventNewsAndStatus', params:'\'id=' + moveEvent.id + '\'',onComplete:'updateEventHeader(XMLHttpRequest)')}
				</script>
			</g:if>

		</g:if>


	</g:if>
	<div class="main_bottom"><div id="messageDiv" class="message" style="display:none"></div><g:layoutBody /></div>

	</div>

    </div>


		<%-- DIV for editing User Profile --%>
		<g:render template="../person/personEdit" model="[user: userLogin, minPasswordLength: minPasswordLength]" />

		<%-- DIV for editing User Preferences --%>
		<div id="userPrefDivId" style="display: none;min-width:250px;" title="${tds.currentPersonName()} Preferences"></div>

		<%-- DIV for editing User date and timezone --%>
		<div id="userTimezoneForm" style="display: none;min-width:250px;" title="${tds.currentPersonName()} Date and Timezone"></div>
	</body>
</html>
