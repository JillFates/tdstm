<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
	<head>
		<meta http-equiv="Content-Type" content="text/html; charset=iso-8859-1" />
		<title><g:layoutTitle default="Grails" /></title>

		<g:render template="../layouts/standardResources" />

		<g:layoutHead />

		<link type="text/css" rel="stylesheet" href="${resource(dir:'css',file:'dropDown.css')}" />    
		<link type="text/css" rel="stylesheet" href="${resource(dir:'css',file:'daterangepicker-bs2.css')}" />
		
		<script type="text/javascript">
			$(document).ready(function() {
				$("#personDialog").dialog({ autoOpen: false });
				$("#userPrefDivId").dialog({ autoOpen: false });

				${remoteFunction(controller:'userLogin', action:'updateLastPageLoad', params:'\'url=\' + currentURL ')};
				// Due to some issue with textarea overriding the value at intial load
				$('textarea').each(function(){
					$(this).val($(this).text());
				});
				$('.tzmenu').click(function(){
					$(".tzmenu ul").toggle();
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
			});

		</script>
	</head>
	<% 
		def currProj = session.getAttribute("CURR_PROJ");

		def projectId = currProj?.CURR_PROJ ;
		def moveEventId = session.getAttribute("MOVE_EVENT")?.MOVE_EVENT ;
		def moveEventName = moveEventId ? MoveEvent.findById( moveEventId ) : ''
		def moveBundleId = session.getAttribute("CURR_BUNDLE")?.CURR_BUNDLE ;
		def roomId = session.getAttribute("CURR_ROOM")?.CURR_ROOM;
		def room = Room.get(roomId)
		def moveBundleName = moveBundleId ? MoveBundle.findById( moveBundleId ) : ''
		def currProjObj;
		def moveEvent;
		def personId = session.getAttribute("LOGIN_PERSON").id
		def person = Person.get(personId)
		if (projectId != null) {
			currProjObj = Project.findById(projectId)
		}
		if (moveEventId != null) {
			moveEvent = MoveEvent.findById(moveEventId)
		}
		def partyGroup = session.getAttribute("PARTYGROUP")?.PARTYGROUP ;
		def isIE6 = request.getHeader("User-Agent").contains("MSIE 6");
		def isIE7 = request.getHeader("User-Agent").contains("MSIE 7");
		def isMDev = request.getHeader("User-Agent").contains("Mobile");

		def setImage = session.getAttribute("setImage")?:(currProjObj ? ProjectLogo.findByProject(currProjObj)?.id:'');
		def user = UserLogin.findByPerson( person )
		def username = user.username
		def userPrefs = UserPreference.findAllByUserLogin(user)

	%>
	<body>
		<div class="main_body">
			<input id="contextPath" type="hidden" value="${request.contextPath}"/>
			<div class="tds_header">
				<div class="header_left">
					<g:if test="${setImage}">
						<img src="${createLink(controller:'project', action:'showImage', id:setImage)}" style="height: 30px;"/>
					</g:if>
					<g:else>      	
						<a href="http://www.transitionaldata.com/service/transitionmanager" target="new"><img src="${resource(dir:'images',file:'TMMenuLogo.png')}" style="float: left;border: 0px;height: 30px;"/></a>
					</g:else>
				</div>
				<div class="title">&nbsp;TransitionManager&trade; 
					<g:if test="${currProjObj}"> - ${currProjObj.name} </g:if>
					<g:if test="${moveEvent}"> : ${moveEvent?.name}</g:if>
					<g:if test="${moveBundleId}"> : ${moveBundleName}</g:if>
				</div>
				<div class="header_right" id="userMenuId"><br />
					<div style="font-weight: bold;">
						<shiro:isLoggedIn>
							<strong>
								<div style="float: left;">
									<g:if test="${isIE6 || isIE7}">
										<span><img title="Note: MS IE6 and MS IE7 has limited capability so functions have been reduced." src="${resource(dir:'images/skin',file:'warning.png')}" style="width: 14px;height: 14px;float: left;padding-right: 3px;"/></span>
									</g:if>
									<a class="headerClass" onmouseover="hoverMegaMenu('#userMegaMenu')" href="javascript:showMegaMenu('#userMegaMenu')" style="float:left;text-decoration:none;display:inline">
										&nbsp;<span id="loginUserId">${session.getAttribute("LOGIN_PERSON").name }</span>
									</a>
								</div>
								<div class="tzmenu">:&nbsp; <span id="tzId">${session.getAttribute("CURR_TZ")?.CURR_TZ ? session.getAttribute("CURR_TZ")?.CURR_TZ : 'EDT' } time zone</span>
									<ul>
										<li><a href="javascript:setUserTimeZone('GMT')">GMT</a></li>
										<li><a href="javascript:setUserTimeZone('PST')">PST</a></li>
										<li><a href="javascript:setUserTimeZone('PDT')">PDT</a></li>
										<li><a href="javascript:setUserTimeZone('MST')">MST</a></li>
										<li><a href="javascript:setUserTimeZone('MDT')">MDT</a></li>
										<li><a href="javascript:setUserTimeZone('CST')">CST</a></li>
										<li><a href="javascript:setUserTimeZone('CDT')">CDT</a></li>
										<li><a href="javascript:setUserTimeZone('EST')">EST</a></li>
										<li><a href="javascript:setUserTimeZone('EDT')">EDT</a></li>
									</ul>
								</div>
							</strong>
							&nbsp;&nbsp;<g:link controller="auth" action="signOut"></g:link>
						</shiro:isLoggedIn>
					</div>
				</div>
			</div>

      <g:if test="${currProj}">
	      <div class="menu2">
	      <ul>
			<tds:hasPermission permission='AdminMenuView'>
			<li id="adminMenuId" class="menuLiIndex"><a class="home menuhideright headerClass" onmouseover="hoverMegaMenu('#adminMegaMenu')" onmouseout="clearTipTimer()" href="javascript:showMegaMenu('#adminMegaMenu')">Admin</a>
				<div class="megamenu admin inActive" id="adminMegaMenu">
					<table class="mmtable room_rack"><tr>
					<td style="vertical-align:top" nowrap="nowrap"><span class="megamenuSection">Administration</span><br />
						<ul >
							<li><g:link class="mmlink" controller="auth" action="home" onclick="hideMegaMenu('adminMegaMenu')">Admin Portal</g:link> </li>
							<tds:hasPermission permission='RolePermissionView'>
							<li><g:link class="mmlink" controller="permissions" action="show" onclick="hideMegaMenu('adminMegaMenu')">Role Permissions</g:link> </li>
							</tds:hasPermission>
							<li><g:link class="mmlink" controller="assetEntity" action="assetOptions" onclick="hideMegaMenu('adminMegaMenu')">Asset Options</g:link> </li>
							<li><a class="mmlink" href="javascript:window.open('https://ops.tdsops.com/twiki/bin/view/Main/DataCenterMoves/TMReleaseNotes?cover=print','help');" onclick="hideMegaMenu('adminMegaMenu')">Release Notes</a></li>
							<tds:hasPermission permission='HelpMenuView'>
							<li><a class="mmlink" href="javascript:window.open('https://ops.tdsops.com/twiki/bin/view/Main/DataCenterMoves/TMAdminPortal?cover=print','help');" onclick="hideMegaMenu('adminMegaMenu')">help</a></li>
							</tds:hasPermission>
							
						</ul>
					</td>
					<td style="vertical-align:top"><span class="megamenuSection">Manage Clients</span><br />
						<ul >
							<li><g:link class="mmlink" controller="partyGroup" action="list" params="[active:'active',tag_s_2_name:'asc']" id="${partyGroup}" onclick="hideMegaMenu('adminMegaMenu')">List Companies</g:link></li>
							<li><g:link class="mmlink" controller="person" id="${partyGroup}" onclick="hideMegaMenu('adminMegaMenu')">List Staff</g:link></li>
							<li><g:link class="mmlink" controller="userLogin" id="${partyGroup}" onclick="hideMegaMenu('adminMegaMenu')">List Users</g:link></li>
							<li><g:link class="mmlink" controller="admin" action="importAccounts" onclick="hideMegaMenu('adminMegaMenu')">Import Accounts</g:link></li>
							<tds:hasPermission permission='HelpMenuView'>
							<li><a class="mmlink" href="javascript:window.open('https://ops.tdsops.com/twiki/bin/view/Main/DataCenterMoves/TMCreatePerson?cover=print','help');" onclick="hideMegaMenu('adminMegaMenu')">help</a></li>
							</tds:hasPermission>
						</ul>
					</td>
					<td style="vertical-align:top"><span class="megamenuSection">Manage Workflows</span><br />
						<ul >
							<li><g:link class="mmlink" controller="workflow" action="home" onclick="hideMegaMenu('adminMegaMenu')">List Workflows </g:link> </li>
							<tds:hasPermission permission='HelpMenuView'>
							<li><a class="mmlink" href="javascript:window.open('https://ops.tdsops.com/twiki/bin/view/Main/DataCenterMoves/TMManageWorkflows?cover=print','help');" onclick="hideMegaMenu('adminMegaMenu')">help</a></li>
							</tds:hasPermission>
						</ul>
					</td>
					<td style="vertical-align:top"><span class="megamenuSection">Manage Model Library</span><br />
						<ul >
							<li><g:link class="mmlink" controller="manufacturer" id="${partyGroup}" onclick="hideMegaMenu('adminMegaMenu')">List Manufacturers</g:link></li>
							<li><g:link class="mmlink" controller="model" id="${partyGroup}" onclick="hideMegaMenu('adminMegaMenu')">List Models</g:link></li>
							<li><g:link class="mmlink" controller="model" action="importExport" onclick="hideMegaMenu('adminMegaMenu')">Sync Libraries</g:link></li>
							<tds:hasPermission permission='HelpMenuView'>
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
					<g:if test="${currProjObj}">
						<span class="megamenuSection"> </span>
							<ul >
								<li><g:link class="mmlink" controller="projectUtil" onclick="hideMegaMenu('projectMegaMenu')"><g:if test="${currProjObj.name.size()>20}">${currProjObj.name.substring(0,20)+'...'}</g:if><g:else>${currProjObj.name}</g:else> Details</g:link></li>
								<li><g:link class="mmlink" controller="person" action="manageProjectStaff"  onclick="hideMegaMenu('projectMegaMenu')">Project Staff</g:link></li>
								<li><g:link class="mmlink" controller="project" action="fieldImportance" onclick="hideMegaMenu('projectMegaMenu')">Field Settings</g:link> </li>
					</g:if>
					<g:else>
						<span class="megamenuSection">No Project Selected</strong></span><br />
							<ul>
					</g:else>
							<tds:hasPermission permission='HelpMenuView'>
							<li><a class="mmlink" href="javascript:window.open('https://ops.tdsops.com/twiki/bin/view/Main/DataCenterMoves/TMProjectStaff?cover=print','help');"  onclick="hideMegaMenu('projectMegaMenu')">help</a></li>
							</tds:hasPermission>
						</ul>
					</td>
					</tr></table>
				</div>
			</li>
			<li id="roomMenuId" class="menuLiIndex" style="position:relative; float: left;"><a class="home headerClass" onmouseover="hoverMegaMenu('#racksMegaMenu')" onmouseout="clearTipTimer()" href="javascript:showMegaMenu('#racksMegaMenu')">Data Centers</a>
				<div class="megamenu rooms inActive" id="racksMegaMenu">
					<table class="mmtable room_rack" ><tr>
					<td style="vertical-align:top">
						<span class="megamenuSection">Rooms and Racks</span><br />
						<ul >
							<li><g:link class="mmlink" params="[viewType:'list']" controller="room"  onclick="hideMegaMenu('racksMegaMenu')">List Rooms</g:link></li>
							<g:if test="${roomId}">
								<li><g:link class="mmlink" params="[roomId:roomId]" controller="room" onclick="hideMegaMenu('racksMegaMenu')">Room ${room?.location}/${room?.roomName}</g:link></li>
							</g:if>
							<li><g:link class="mmlink" controller="rackLayouts" action="create" onclick="hideMegaMenu('racksMegaMenu')">Racks</g:link></li>
							<tds:hasPermission permission='HelpMenuView'>
								<li><a class="mmlink" href="javascript:window.open('https://ops.tdsops.com/twiki/bin/view/Main/DataCenterMoves/TMRooms?cover=print','help');">help</a></li>
							</tds:hasPermission>
						</ul>
					</td>
					</tr></table>
				</div>
			</li>
	        <tds:hasPermission permission='AssetMenuView'>
			<li id="assetMenuId" class="menuLiIndex" style="position:relative; float:left;"><a class="home headerClass" onmouseover="hoverMegaMenu('#assetMegaMenu')" onmouseout="clearTipTimer()" href="javascript:showMegaMenu('#assetMegaMenu')" >Assets</a>
				<div class="megamenu rooms inActive" id="assetMegaMenu" >
					<table class="mmtable room_rack"><tr>
					<td style="vertical-align:top"><span class="megamenuSection">Assets</span><br />
						<ul>
							<li><g:link class="mmlink" controller="assetEntity" action="assetSummary">Summary Table</g:link></li>	
							<li><g:link class="mmlink" controller="application" action="list" onclick="hideMegaMenu('assetMegaMenu')"> Applications</g:link></li>
							<li><g:link class="mmlink" controller="assetEntity" action="list" params="[filter:'server']" onclick="hideMegaMenu('assetMegaMenu')"> Servers</g:link></li>
							<li><g:link class="mmlink" controller="assetEntity" action="list" params="[filter:'all']" onclick="hideMegaMenu('assetMegaMenu')"> All Devices</g:link></li>
							<li><g:link class="mmlink" controller="database"    action="list" onclick="hideMegaMenu('assetMegaMenu')"> Databases</g:link></li>
							<li><g:link class="mmlink" controller="assetEntity" action="list" params="[filter:'storage']" onclick="hideMegaMenu('assetMegaMenu')"> Storage Devices</g:link></li>
							<li><g:link class="mmlink" controller="files"       action="list" onclick="hideMegaMenu('assetMegaMenu')"> Logical Storage</g:link></li>
							<li><g:link class="mmlink" controller="assetEntity" action="listComment" onclick="hideMegaMenu('assetMegaMenu')">Comments</g:link></li>
							<li><g:link class="mmlink" controller="assetEntity" action="listDependencies" onclick="hideMegaMenu('assetMegaMenu')"> Dependencies</g:link></li>
							<tds:hasPermission permission='MoveBundleEditView'>
							  <li><g:link class="mmlink" controller="moveBundle" action="dependencyConsole" onclick="hideMegaMenu('assetMegaMenu')">Dependency Analyzer</g:link></li>
							</tds:hasPermission>
							<tds:hasPermission permission='HelpMenuView'>
							<li><a class="mmlink" href="javascript:window.open('https://ops.tdsops.com/twiki/bin/view/Main/DataCenterMoves/TMAssetOverview?cover=print','help');" onclick="hideMegaMenu('assetMegaMenu')">help</a></li>
							</tds:hasPermission>
						</ul>
					</td>
					<tds:hasPermission permission='AssetEdit'>
					<td style="vertical-align:top"><span class="megamenuSection">Manage Data</span><br />
					
						<ul>
							<tds:hasPermission permission='Import'>
							<li><g:link class="mmlink" controller="assetEntity" action="assetImport"  onclick="hideMegaMenu('assetMegaMenu')">Import Assets</g:link></li>
							</tds:hasPermission>
							<tds:hasPermission permission='AssetEdit'>
							<li><g:link class="mmlink" controller="dataTransferBatch" action="index" onclick="hideMegaMenu('assetMegaMenu')">Manage Batches</g:link></li>
							</tds:hasPermission>
							<tds:hasPermission permission='Export'>
							<li><g:link class="mmlink" controller="assetEntity" action="exportAssets"  onclick="hideMegaMenu('assetMegaMenu')">Export Assets</g:link></li>
							</tds:hasPermission>
							<tds:hasPermission permission='HelpMenuView'>
							<li><a class="mmlink" href="javascript:window.open('https://ops.tdsops.com/twiki/bin/view/Main/DataCenterMoves/TMImportExport?cover=print','help');" onclick="hideMegaMenu('assetMegaMenu')">help</a></li>
							</tds:hasPermission>
						</ul>
					</td>
					</tds:hasPermission>
					</tr></table>
				</div>	
			</li>
			</tds:hasPermission>
			<tds:hasPermission permission='EventMenuView'>
			<li id="eventMenuId" class="menuLiIndex" style="position:relative; float: left;"><a class="home headerClass" onmouseover="hoverMegaMenu('#bundleMegaMenu')" onmouseout="clearTipTimer()" href="javascript:showMegaMenu('#bundleMegaMenu')">Planning</a>
				<div class="megamenu rooms inActive" id="bundleMegaMenu">
					<table class="mmtable " ><tr>
					<td style="vertical-align:top"><span class="megamenuSection">Events</span><br />
						<ul>
							<li><g:link class="mmlink" controller="moveEvent" action="list" onclick="hideMegaMenu('bundleMegaMenu')" >List Events</g:link> </li>
							<g:if test="${currProjObj && moveEvent}">
								<span class="megamenuSection"> </span>
								<li style="white-space:nowrap;"><g:link class="mmlink" controller="moveEvent" action="show" id="${moveEventId}" onclick="hideMegaMenu('bundleMegaMenu')">${moveEventName} Event Details</g:link></li>
							</g:if>
							<tds:hasPermission permission='ShowListNews'>
							<li><g:link class="mmlink" controller="newsEditor"  onclick="hideMegaMenu('consoleMegaMenu')">List Event News</g:link></li>
							</tds:hasPermission>
							<tds:hasPermission permission="ShowMovePrep">
							<li style="white-space:nowrap;"><g:link class="mmlink" controller="reports" action="preMoveCheckList" onclick="hideMegaMenu('bundleMegaMenu')">Pre-event Checklist</g:link></li>
							</tds:hasPermission>
							<li style="white-space:nowrap;"><g:link class="mmlink" controller="moveEvent" action="exportRunbook" onclick="hideMegaMenu('bundleMegaMenu')">Export Runbook</g:link></li>
							<tds:hasPermission permission='HelpMenuView'>
							<li><a class="mmlink" href="javascript:window.open('https://ops.tdsops.com/twiki/bin/view/Main/DataCenterMoves/TMEvents?cover=print','help');" onclick="hideMegaMenu('bundleMegaMenu')">help</a></li>
							</tds:hasPermission>
						</ul>
					</td>
					<td style="vertical-align:top"><span class="megamenuSection">Bundles</span><br />
						<ul>
							<li><g:link class="mmlink" controller="moveBundle" action="list">List Bundles</g:link> </li>
					<g:if test="${currProjObj && moveBundleId}">
							<span class="megamenuSection"> </span>
							<li><g:link class="mmlink" controller="moveBundle" action="show"  onclick="hideMegaMenu('bundleMegaMenu')">${moveBundleName} Bundle Details</g:link></li>
							<li><g:link class="mmlink" controller="moveBundleAsset" action="assignAssetsToBundle" params="[bundleId:moveBundleId]" onclick="hideMegaMenu('bundleMegaMenu')">Bundled Assets</g:link> </li>
					</g:if>
							<tds:hasPermission permission='HelpMenuView'>
							<li><a class="mmlink" href="javascript:window.open('https://ops.tdsops.com/twiki/bin/view/Main/DataCenterMoves/TMBundles?cover=print','help');" onclick="hideMegaMenu('bundleMegaMenu')">help</a></li>
							</tds:hasPermission>
						</ul>
					</td>
					</tr></table>
				</div>
			</li>
			</tds:hasPermission>

			<%-- Task Menu --%>
			<tds:hasPermission permission='BundleMenuView'>
			<li id="teamMenuId" class="menuLiIndex" style="position:relative; float:left;"><a class="home headerClass" onmouseover="hoverMegaMenu('#teamMegaMenu')" onmouseout="clearTipTimer()" href="javascript:showMegaMenu('#teamMegaMenu')">Tasks</a>
				<div class="megamenu rooms inActive" id="teamMegaMenu" >
					<table class="mmtable"><tr>
					<td style="vertical-align:top"><span class="megamenuSection">Tasks</span><br />
						<ul>
							<li><a class="mmlink" id="MyTasksMenuId" href="/tdstm/clientTeams/listTasks" onclick="hideMegaMenu('teamMegaMenu')">My Tasks 
							(<span id="todoCountProjectId">&nbsp;</span>)</a></li>
							<tds:hasPermission permission='ViewTaskManager'>
							<li><g:link class="mmlink" controller="assetEntity" action="listTasks"  params="[initSession:true]" onclick="hideMegaMenu('teamMegaMenu')">Task Manager</g:link></li>
							</tds:hasPermission>

							<tds:hasPermission permission='ViewTaskGraph'>
							<li><g:link class="mmlink" controller="task" action="taskGraph"  params="[initSession:true]" onclick="hideMegaMenu('teamMegaMenu')">Task Graph</g:link></li>
							</tds:hasPermission>

							<tds:hasPermission permission='ViewTaskTimeline'>
							<li><g:link class="mmlink" controller="task" action="taskTimeline" onclick="hideMegaMenu('teamMegaMenu')">Task Timeline</g:link></li>
							</tds:hasPermission>

							<tds:hasPermission permission="ViewRecipe">
							<li><g:link class="mmlink" controller="cookbook" action="index" onclick="hideMegaMenu('teamMegaMenu')">Cookbook</g:link></li>
							</tds:hasPermission>

							<tds:hasPermission permission="GenerateTasks">
							<li><a href="/tdstm/cookbook/index#/generationHistory">Generation History</a></li>
							</tds:hasPermission>

							<tds:hasPermission permission='HelpMenuView'>
							<li><a class="mmlink" href="javascript:window.open('https://ops.tdsops.com/twiki/bin/view/Main/DataCenterMoves/TMTaskOverview?cover=print','help');" onclick="hideMegaMenu('consoleMegaMenu')">help</a></li>
							</tds:hasPermission>
						</ul>
					</td>
					</tr></table>
				</div>
			</li>
            </tds:hasPermission>

			<tds:hasPermission permission='DashBoardMenuView'> 
			<li id="dashboardMenuId" class="menuLiIndex" style="position:relative; float:left;"><a class="home headerClass" onmouseover="hoverMegaMenu('#dashboardMegaMenu')" onmouseout="clearTipTimer()" href="javascript:showMegaMenu('#dashboardMegaMenu')">Dashboards</a>
				<div class="megamenu rooms inActive" id="dashboardMegaMenu">
					<table class="mmtable"><tr>
					<td style="vertical-align:top"><span class="megamenuSection">Live Dashboards</span><br />
						<ul>
							<li><g:link class="home mmlink" controller="dashboard" action="userPortal" onclick="hideMegaMenu('dashboardMegaMenu')">User Dashboard</g:link></li>
							<tds:hasPermission permission='ShowPlanning'>
							<li><g:link class="home mmlink" controller="moveBundle" action="planningStats" onclick="hideMegaMenu('dashboardMegaMenu')">Planning Dashboard</g:link></li>
							</tds:hasPermission>
							<li><g:link class="home mmlink" controller="dashboard" onclick="hideMegaMenu('dashboardMegaMenu')">Event Dashboard</g:link></li>
							<tds:hasPermission permission='ShowCartTracker'>
							<li><g:link class="mmlink" controller="cartTracking" action="cartTracking"  onclick="hideMegaMenu('consoleMegaMenu')">Cart Tracker</g:link></li>
							</tds:hasPermission>
							<tds:hasPermission permission='HelpMenuView'>
							<li><a class="mmlink" href="javascript:window.open('https://ops.tdsops.com/twiki/bin/view/Main/DataCenterMoves/TMDashboardOverview?cover=print','help');" onclick="hideMegaMenu('dashboardMegaMenu')">help</a></li>
							</tds:hasPermission>
						</ul>
					</td>
					</tr></table>
				</div>
			</li>
			</tds:hasPermission>

			<tds:hasPermission permission='ReportMenuView'>
			<li id="reportsMenuId" class="menuLiIndex" style="position:relative; float: left;"><a class="home headerClass" onmouseover="hoverMegaMenu('#reportsMegaMenu')" onmouseout="clearTipTimer()" href="javascript:showMegaMenu('#reportsMegaMenu')">Reports</a>
				<div class="megamenu reports inActive" id="reportsMegaMenu">
					<table class="mmtable "><tr>
					<tds:hasPermission permission='ShowDiscovery'>
					<td style="vertical-align:top"><span class="megamenuSection">Discovery</span><br />
						<ul>
							<li><a href="/tdstm/reports/getBundleListForReportDialog?reportId=CablingConflict" class="home mmlink" onclick="hideMegaMenu('reportsMegaMenu')">Cabling Conflict</a> </li>
							<li><a href="/tdstm/reports/getBundleListForReportDialog?reportId=CablingData" class="home mmlink" onclick="hideMegaMenu('reportsMegaMenu')">Cabling Data</a> </li>
							<li><a href="/tdstm/reports/powerReport" class="home mmlink" onclick="hideMegaMenu('reportsMegaMenu')">Power</a> </li>
							<tds:hasPermission permission='HelpMenuView'>
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
							<tds:hasPermission permission='ShowPlanning'>
								<li><a href="/tdstm/reports/getBundleListForReportDialog?reportId=Task+Report"  class="home mmlink" onclick="hideMegaMenu('reportsMegaMenu')">Task Report</a> </li>
							</tds:hasPermission>
							<li><g:link class="home mmlink" controller="reports" params="[projectId:currProjObj?.id]" onclick="hideMegaMenu('reportsMegaMenu')">Report Summary</g:link></li>
						</ul>
					</td>
					<tds:hasPermission permission='ShowMovePrep'>
					<td style="vertical-align:top"><span class="megamenuSection">Event Prep</span><br />
						<ul >
							<tds:hasPermission permission="ShowMovePrep">
							<li><a href="/tdstm/reports/preMoveCheckList" class="home mmlink" onclick="hideMegaMenu('reportsMegaMenu')">Pre-event Checklist</a> </li>
							</tds:hasPermission>
							<li><a href="/tdstm/reports/getBundleListForReportDialog?reportId=Login+Badges" class="home mmlink" onclick="hideMegaMenu('reportsMegaMenu')">Login Badges</a> </li>
							<li><a href="/tdstm/reports/getBundleListForReportDialog?reportId=Asset+Tag" class="home mmlink" onclick="hideMegaMenu('reportsMegaMenu')">Asset Tags</a> </li>
							<li><a href="/tdstm/reports/getBundleListForReportDialog?reportId=Transportation+Asset+List" class="home mmlink" onclick="hideMegaMenu('reportsMegaMenu')">Transport Worksheets</a></li>
							<tds:hasPermission permission='HelpMenuView'>
							<li><a class="mmlink" href="javascript:window.open('https://ops.tdsops.com/twiki/bin/view/Main/DataCenterMoves/TranManHelp?cover=print','help');" onclick="hideMegaMenu('reportsMegaMenu')">help</a></li>
							</tds:hasPermission>
						</ul>
					</td>
					</tds:hasPermission>
					<tds:hasPermission permission='ShowMoveDay'>
					<td style="vertical-align:top"><span class="megamenuSection">Event Day</span><br />
						<ul>
							<li><a href="/tdstm/reports/applicationMigrationReport" class="home mmlink" onclick="hideMegaMenu('reportsMegaMenu')">Application Migration Results</a> </li>
							<li><a href="/tdstm/reports/getBundleListForReportDialog?reportId=Issue+Report" class="home mmlink" onclick="hideMegaMenu('reportsMegaMenu')">Issue Report</a> </li>
							<li><a href="/tdstm/reports/getBundleListForReportDialog?reportId=MoveResults" class="home mmlink" onclick="hideMegaMenu('reportsMegaMenu')">Event Results</a> </li>
							<li><a href="/tdstm/reports/getBundleListForReportDialog?reportId=CablingQA" class="home mmlink" onclick="hideMegaMenu('reportsMegaMenu')">Cabling QA</a> </li>
							<tds:hasPermission permission='HelpMenuView'>
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
			<td style="vertical-align:top"><span class="megamenuSection">${session.getAttribute("LOGIN_PERSON").name }</span><br />
				<ul>
					<li><g:remoteLink controller="person" action="getPersonDetails" id="${session.getAttribute('LOGIN_PERSON').id}" onComplete="updatePersonDetails(e)">Account Details...</g:remoteLink></li>
					<li><a href="#" style="cursor: pointer;" id="resetPreferenceId" name="${user}" onclick="editPreference()">Edit preferences</a></li>
					<li><g:link class="home mmlink" controller="clientTeams" action="listTasks" params="[viewMode:'mobile',tab:tab]">Use Mobile Site</g:link></li>
					<g:if test="${person?.modelScore}">
					<li><a href="/tdstm/person/list/18?maxRows=25&tag_tr_=true&tag_p_=1&tag_mr_=25&tag_s_5_modelScore=desc">Model Score: ${person?.modelScore}</a></li>
					</g:if>
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
		<g:if test="${currProjObj?.runbookOn && moveEvent && moveEvent?.inProgress == 'true'}">
			<div class="menu3" id="head_crawler" >
				<div id="crawlerHead">${moveEvent.name} Event Status <span id="moveEventStatus"></span>. News: </div>
				<div id="head_mycrawler"><div id="head_mycrawlerId" style="width: 1200px; height:25px; vertical-align:bottom" > </div></div>
			</div>
			<script type="text/javascript">
			function updateEventHeader( e ){
				var newsAndStatus = eval("(" + e.responseText + ")")
				$("#head_mycrawlerId").html(newsAndStatus[0].news);
				$("#head_crawler").addClass(newsAndStatus[0].cssClass)
				$("#moveEventStatus").html(newsAndStatus[0].status)
			}
			${remoteFunction(controller:'moveEvent', action:'getMoveEventNewsAndStatus', params:'\'id='+moveEventId+'\'',onComplete:'updateEventHeader(e)')}
			</script>
		</g:if>


	</g:if>
	<div class="main_bottom"><div id="messageDiv" class="message" style="display:none"></div><g:layoutBody /></div>
	
	</div>

    </div>
		<g:javascript src="tdsmenu.js" />

		<%-- DIV for editing User Profile --%> 
		<g:render template="../person/personEdit" model="[user:user]" />
 
		<%-- DIV for editing User Preferences --%>
		<div id="userPrefDivId" style="display: none;min-width:250px;" title="${session.getAttribute("LOGIN_PERSON").name } Preferences"></div>
	</body>
</html>
