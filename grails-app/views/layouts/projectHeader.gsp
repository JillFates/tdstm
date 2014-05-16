<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
  <head>
    <meta http-equiv="Content-Type" content="text/html; charset=iso-8859-1" />
    <title><g:layoutTitle default="Grails" /></title>
    <link rel="stylesheet" href="${resource(dir:'css',file:'main.css')}" type="text/css"/>
    <link rel="stylesheet" href="${resource(dir:'css',file:'tds.css')}" type="text/css"/>
    <link rel="shortcut icon" href="${resource(dir:'images',file:'tds.ico')}" type="image/x-icon" />
    <link type="text/css" rel="stylesheet" href="${resource(dir:'css',file:'ui.core.css')}" />
    <link type="text/css" rel="stylesheet" href="${resource(dir:'css',file:'ui.dialog.css')}" />
    <link type="text/css" rel="stylesheet" href="${resource(dir:'css',file:'ui.theme.css')}" />
    <%-- 
    <link type="text/css" rel="stylesheet" href="${resource(dir:'css',file:'bootstrap.min.css')}" />
    <link type="text/css" rel="stylesheet" href="${resource(dir:'css',file:'bootstrap.responsive.min.css')}" />
    --%>
    <link rel="stylesheet" href="${resource(dir:'css',file:'ui.datetimepicker.css')}" type="text/css"/>
   	<link type="text/css" rel="stylesheet" href="${resource(dir:'css',file:'jquery-ui-smoothness.css')}" />
	<link id="jquery-ui-theme" media="screen, projection" rel="stylesheet" type="text/css" 
		href="${resource(dir:'plugins/jquery-ui-1.8.15/jquery-ui/themes/ui-lightness',file:'jquery-ui-1.8.15.custom.css')}"/>
	<link type="text/css" rel="stylesheet" href="${resource(dir:'css',file:'combox.css')}" />
	<link type="text/css" rel="stylesheet" href="${resource(dir:'css',file:'select2.css')}" />
	<link type="text/css" rel="stylesheet" href="${resource(dir:'css',file:'combox.css')}" />
    <g:javascript library="prototype" />
    <g:javascript src="jquery-1.9.1.js"/>	
	<g:javascript src="jquery-1.9.1-ui.js"/>
	<g:javascript src="datetimepicker.js"/>
	<g:javascript src="jquery-migrate-1.0.0.js"/>
    <g:javascript src="crawler.js" />
	<g:javascript src="select2.js"/>
	<g:javascript src="jquery.combox.js"/>	
    <%--
    <g:javascript src="bootstrap/bootstrap.min.js" /> 
    --%>
    <g:layoutHead />
   
    <link type="text/css" rel="stylesheet" href="${resource(dir:'css',file:'dropDown.css')}" />    

   <script type="text/javascript">
   		$(document).ready(function() {
      		$("#personDialog").dialog({ autoOpen: false })
      		$("#userPrefDivId").dialog({ autoOpen: false })
      		var currentURL = window.location.pathname
      		${remoteFunction(controller:'userLogin', action:'updateLastPageLoad', params:'\'url=\' + currentURL ')}
      		// Due to some issue with textarea overriding the value at intial load
      		$('textarea').each(function(){
      			$(this).val($(this).text())
      		});
      		$('.tzmenu').click(function(){
      			 $(".tzmenu ul").toggle();
      		});
      		$(".headerClass").mouseover(function(){
      			$(this).parent().find('a').addClass('mouseover');
      			$(this).parent().find('a').removeClass('mouseout');
      		})
      		$(".headerClass").mouseout(function(){
      			if(!$(this).parent().find(".megamenu").is(":visible")){
      				$(this).parent().find('a').removeClass('mouseover');
      			} else {
      				$('.headerClass').removeClass('mouseover');
      			}
      		})
     	})
     	var emailRegExp = /^([0-9a-zA-Z]+([_.-]?[0-9a-zA-Z]+)*@[0-9a-zA-Z]+[0-9,a-z,A-Z,.,-]+\.[a-zA-Z]{2,4})+$/
     	var dateRegExpForExp  = /^(0[1-9]|1[012])[/](0[1-9]|[12][0-9]|3[01])[/](19|20)\d\d ([0-1][0-9]|[2][0-3])(:([0-5][0-9])){1,2} ([APap][Mm])$/;
     	var currentMenuId = "";
     	var B1 = []
     	var B2 = []
     	var taskManagerTimePref = "60"
		var contextPath = "${request.contextPath}"
		var isIE7OrLesser  = jQuery.browser.msie && parseInt(jQuery.browser.version) < 8 ? true : false  

   </script>
  </head>
	<% def currProj = session.getAttribute("CURR_PROJ");
	   def setImage = session.getAttribute("setImage");
    def projectId = currProj?.CURR_PROJ ;
    def moveEventId = session.getAttribute("MOVE_EVENT")?.MOVE_EVENT ;
    def moveEventName = moveEventId ? MoveEvent.findById( moveEventId ) : ''
    def moveBundleId = session.getAttribute("CURR_BUNDLE")?.CURR_BUNDLE ;
    def roomId = session.getAttribute( "CURR_ROOM" )?.CURR_ROOM
    def room = Room.get(roomId)
	def moveBundleName = moveBundleId ? MoveBundle.findById( moveBundleId ) : ''
    def currProjObj;
    def moveEvent;
	def personId = session.getAttribute("LOGIN_PERSON").id
	def person = Person.get(personId)
    if( projectId != null){
      currProjObj = Project.findById(projectId)
    }
    if( moveEventId != null){
    	moveEvent = MoveEvent.findById(moveEventId)
    }
    def partyGroup = session.getAttribute("PARTYGROUP")?.PARTYGROUP ;
    def isIE6 = request.getHeader("User-Agent").contains("MSIE 6");
	def isIE7 = request.getHeader("User-Agent").contains("MSIE 7");
	def isMDev = request.getHeader("User-Agent").contains("Mobile");
	
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
     			<a href="http://www.transitionaldata.com/" target="new"><img src="${resource(dir:'images',file:'tds3.png')}" style="float: left;border: 0px;height: 30px;"/></a>      	    	 
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
			&nbsp;<span id="loginUserId">${session.getAttribute("LOGIN_PERSON").name }</span></a>
			</div>
			<div class="tzmenu">&nbsp;-&nbsp;using <span id="tzId">${session.getAttribute("CURR_TZ")?.CURR_TZ ? session.getAttribute("CURR_TZ")?.CURR_TZ : 'EDT' }</span>
				time<ul>
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
							<li><g:link class="mmlink" controller="admin" action="bootstrap" target="_blank"  onclick="hideMegaMenu('adminMegaMenu')">Bootstrap Menus</g:link> </li>
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

			<li id="projectMenuId" class="menuLiIndex" style="position:relative; float: left;" ><a class="home headerClass" onmouseover="hoverMegaMenu('#projectMegaMenu')" onmouseout="clearTipTimer()" href="javascript:showMegaMenu('#projectMegaMenu')">Client/Project</a>
				<div class="megamenu client inActive" id="projectMegaMenu">
					<table class="mmtable"><tr>
						<td style="vertical-align:top">
							<ul>
								<li><g:link class="mmlink" controller="project" action="list" params="[active:'active']" onclick="hideMegaMenu('projectMegaMenu')">List Projects</g:link></li>
							</ul>
					<g:if test="${currProjObj}">
						<span class="megamenuSection"> </span><br />
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

			<li id="roomMenuId" class="menuLiIndex" style="position:relative; float: left;"><a class="home headerClass" onmouseover="hoverMegaMenu('#racksMegaMenu')" onmouseout="clearTipTimer()" href="javascript:showMegaMenu('#racksMegaMenu')">Rooms/Racks</a>
				<div class="megamenu rooms inActive" id="racksMegaMenu">
					<table class="mmtable room_rack" ><tr>
					<td style="vertical-align:top"><span class="megamenuSection">Rooms</span><br />
						<ul >
							<li><g:link class="mmlink" params="[viewType:'list']" controller="room"  onclick="hideMegaMenu('racksMegaMenu')">List Rooms</g:link></li>
							<g:if test="${roomId}">
								<li><g:link class="mmlink" params="[roomId:roomId]" controller="room" onclick="hideMegaMenu('racksMegaMenu')">Room ${room?.location}/${room?.roomName}</g:link></li>
							</g:if>
							<tds:hasPermission permission='HelpMenuView'>
							<li><a class="mmlink" href="javascript:window.open('https://ops.tdsops.com/twiki/bin/view/Main/DataCenterMoves/TranManHelp?cover=print','help');">help</a></li>
							</tds:hasPermission>
						</ul>
					</td>
					<td style="vertical-align:top"><span class="megamenuSection">Racks</span><br />
						<ul >
							<li><g:link class="mmlink" controller="rackLayouts" action="create" onclick="hideMegaMenu('racksMegaMenu')">Racks</g:link></li>
							<tds:hasPermission permission='HelpMenuView'>
							<li><a class="mmlink" href="javascript:window.open('https://ops.tdsops.com/twiki/bin/view/Main/DataCenterMoves/TranManHelp?cover=print','help');" onclick="hideMegaMenu('racksMegaMenu')">help</a></li>
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
							<li><g:link class="mmlink" controller="assetEntity" action="assetSummary">Summary</g:link></li>	
							<li><g:link class="mmlink" controller="application" action="list"  onclick="hideMegaMenu('assetMegaMenu')">List Apps</g:link></li>
							<li><g:link class="mmlink" params="[listType:'server']" controller="assetEntity"  onclick="hideMegaMenu('assetMegaMenu')">List Servers</g:link></li>
							<li><g:link class="mmlink" params="[listType:'physical']" controller="assetEntity"  onclick="hideMegaMenu('assetMegaMenu')">List Physical</g:link></li>
							<li><g:link class="mmlink" controller="database"  onclick="hideMegaMenu('assetMegaMenu')">List DBs</g:link></li>
							<li><g:link class="mmlink" controller="files"  onclick="hideMegaMenu('assetMegaMenu')">List Storage</g:link></li>
							<li><g:link class="mmlink" controller="assetEntity" action="listDependencies" onclick="hideMegaMenu('assetMegaMenu')">List Dependencies</g:link></li>
							<tds:hasPermission permission='MoveBundleEditView'>
							  <li><g:link class="mmlink" controller="moveBundle" action="dependencyConsole" onclick="hideMegaMenu('assetMegaMenu')">Dependency Analyzer</g:link></li>
							</tds:hasPermission>
							<tds:hasPermission permission='HelpMenuView'>
							<li><a class="mmlink" href="javascript:window.open('https://ops.tdsops.com/twiki/bin/view/Main/DataCenterMoves/TranManHelp?cover=print','help');" onclick="hideMegaMenu('assetMegaMenu')">help</a></li>
							</tds:hasPermission>
						</ul>
					</td>
					<tds:hasPermission permission='AssetEdit'>
					<td style="vertical-align:top"><span class="megamenuSection">Manage Data</span><br />
					
						<ul>
							<tds:hasPermission permission='Import'>
							<li><g:link class="mmlink" controller="assetEntity" action="assetImport"  onclick="hideMegaMenu('assetMegaMenu')">Import</g:link></li>
							</tds:hasPermission>
							<tds:hasPermission permission='AssetEdit'>
							<li><g:link class="mmlink" controller="dataTransferBatch" action="index" onclick="hideMegaMenu('assetMegaMenu')">Manage Batches</g:link></li>
							</tds:hasPermission>
							<tds:hasPermission permission='Export'>
							<li><g:link class="mmlink" controller="assetEntity" action="exportAssets"  onclick="hideMegaMenu('assetMegaMenu')">Export</g:link></li>
							</tds:hasPermission>
							<tds:hasPermission permission='AssetMenuView'>
							<li><g:link class="mmlink" controller="assetEntity" action="listComment"  onclick="hideMegaMenu('assetMegaMenu')">Asset Comments</g:link></li>
							</tds:hasPermission>
							<tds:hasPermission permission='HelpMenuView'>
							<li><a class="mmlink" href="javascript:window.open('https://ops.tdsops.com/twiki/bin/view/Main/DataCenterMoves/TranManHelp?cover=print','help');" onclick="hideMegaMenu('assetMegaMenu')">help</a></li>
							</tds:hasPermission>
						</ul>
					</td>
					</tds:hasPermission>
					</tr></table>
				</div>	
			</li>
			</tds:hasPermission>
			<tds:hasPermission permission='EventMenuView'>
			<li id="eventMenuId" class="menuLiIndex" style="position:relative; float: left;"><a class="home headerClass" onmouseover="hoverMegaMenu('#bundleMegaMenu')" onmouseout="clearTipTimer()" href="javascript:showMegaMenu('#bundleMegaMenu')">Events/Bundles </a>
				<div class="megamenu rooms inActive" id="bundleMegaMenu">
					<table class="mmtable " ><tr>
					<td style="vertical-align:top"><span class="megamenuSection">Events</span><br />
						<ul>
							<li><g:link class="mmlink" controller="moveEvent" action="list" onclick="hideMegaMenu('bundleMegaMenu')" >List Events</g:link> </li>
							<g:if test="${currProjObj && moveEvent}">
								<span class="megamenuSection"> </span><br />
								<li style="white-space:nowrap;"><g:link class="mmlink" controller="moveEvent" action="show" id="${moveEventId}" onclick="hideMegaMenu('bundleMegaMenu')">${moveEventName} Event Details</g:link></li>
							</g:if>
							<tds:hasPermission permission="ShowMovePrep">
							<li style="white-space:nowrap;"><g:link class="mmlink" controller="reports" action="preMoveCheckList" onclick="hideMegaMenu('bundleMegaMenu')">Pre-event Checklist</g:link></li>
							</tds:hasPermission>
							<li style="white-space:nowrap;"><g:link class="mmlink" controller="moveEvent" action="exportRunbook" onclick="hideMegaMenu('bundleMegaMenu')">Export Runbook</g:link></li>
							<tds:hasPermission permission='HelpMenuView'>
							<li><a class="mmlink" href="javascript:window.open('https://ops.tdsops.com/twiki/bin/view/Main/DataCenterMoves/TranManHelp?cover=print','help');" onclick="hideMegaMenu('bundleMegaMenu')">help</a></li>
							</tds:hasPermission>
						</ul>
					</td>
					<td style="vertical-align:top"><span class="megamenuSection">Bundles</span><br />
						<ul>
							<li><g:link class="mmlink" controller="moveBundle" action="list">List Bundles</g:link> </li>
					<g:if test="${currProjObj && moveBundleId}">
							<span class="megamenuSection"> </span><br />
							<li><g:link class="mmlink" controller="moveBundle" action="show"  onclick="hideMegaMenu('bundleMegaMenu')">${moveBundleName} Bundle Details</g:link></li>
							<li><g:link class="mmlink" controller="moveBundleAsset" action="assignAssetsToBundle" params="[bundleId:moveBundleId]" onclick="hideMegaMenu('bundleMegaMenu')">Bundled Assets</g:link> </li>
					</g:if>
							<tds:hasPermission permission='HelpMenuView'>
							<li><g:link class="mmlink" controller="projectTeam" action="list" params="[bundleId:moveBundleId]" onclick="hideMegaMenu('bundleMegaMenu')">List Teams (old)</g:link></li>
							<li style="white-space:nowrap;" ><g:link class="mmlink" controller="moveBundleAsset" action="bundleTeamAssignment" params="[bundleId:moveBundleId, rack:'UnrackPlan']" onclick="hideMegaMenu('bundleMegaMenu')">Assign Assets... (old)</g:link> </li>
							<li><a class="mmlink" href="javascript:window.open('https://ops.tdsops.com/twiki/bin/view/Main/DataCenterMoves/TranManHelp?cover=print','help');" onclick="hideMegaMenu('bundleMegaMenu')">help</a></li>
							</tds:hasPermission>
						</ul>
					</td>
					</tr></table>
				</div>
			</li>
			</tds:hasPermission>
			<tds:hasPermission permission='BundleMenuView'>
			<li id="teamMenuId" class="menuLiIndex" style="position:relative; float:left;"><a class="home headerClass" onmouseover="hoverMegaMenu('#teamMegaMenu')" onmouseout="clearTipTimer()" href="javascript:showMegaMenu('#teamMegaMenu')">Tasks</a>
				<div class="megamenu rooms inActive" id="teamMegaMenu" >
					<table class="mmtable"><tr>
					<td style="vertical-align:top"><span class="megamenuSection">Tasks</span><br />
						<ul>
							<li><a class="mmlink" id="MyTasksMenuId" href="/tdstm/clientTeams/listTasks" onclick="hideMegaMenu('teamMegaMenu')">My Tasks 
							(<span id="todoCountProjectId">&nbsp;</span>)</a></li>
							<tds:hasPermission permission='ShowMoveTechsAndAdmins'>
							<li><g:link class="mmlink" controller="assetEntity" action="listTasks"  params="[initSession:true]" onclick="hideMegaMenu('teamMegaMenu')">Task Manager</g:link></li>
							</tds:hasPermission>
							<tds:hasPermission permission='HelpMenuView'>
							<li><a class="mmlink" href="javascript:window.open('https://ops.tdsops.com/twiki/bin/view/Main/DataCenterMoves/TranManHelp?cover=print','help');" onclick="hideMegaMenu('consoleMegaMenu')">help</a></li>
							</tds:hasPermission>
						</ul>
					</td>
					</tr></table>
				</div>
			</li>
            </tds:hasPermission>
	        <tds:hasPermission permission='ConsoleMenuView'>
			<li id="consoleMenuId" class="menuLiIndex" style="position:relative; float:left;"><a class="home headerClass" onmouseover="hoverMegaMenu('#consoleMegaMenu')" onmouseout="clearTipTimer()" href="javascript:showMegaMenu('#consoleMegaMenu')">Console</a>
			    <div class="megamenu rooms inActive" id="consoleMegaMenu">
					<table class="mmtable room_rack"><tr>
					<td style="vertical-align:top"  ><span class="megamenuSection">Supervisor Console</span><br />
						<ul>
							<tds:hasPermission permission='ShowMoveTechsAndAdmins'>
							<li><g:link class="mmlink" controller="assetEntity" action="dashboardView" params="[ 'showAll':'show','teamType':'MOVE']" onclick="hideMegaMenu('consoleMegaMenu')">Supervise Techs (old)</g:link></li>
							<li><g:link class="mmlink" controller="assetEntity" action="dashboardView" params="['showAll':'show','teamType':'ADMIN']" onclick="hideMegaMenu('consoleMegaMenu')">Supervise Admins (old)</g:link></li>
							<li><a class="mmlink" href="/tdstm/clientTeams/list" onclick="hideMegaMenu('teamMegaMenu')">Team Tasks (old)</a></li>
							</tds:hasPermission>
							<tds:hasPermission permission='HelpMenuView'>
							<li><a class="mmlink" href="javascript:window.open('https://ops.tdsops.com/twiki/bin/view/Main/DataCenterMoves/TranManHelp?cover=print','help');" onclick="hideMegaMenu('consoleMegaMenu')">help</a></li>
							</tds:hasPermission>
						</ul>
					</td>
					<td style="vertical-align:top"><span class="megamenuSection">News</span><br />
						<ul>
							<tds:hasPermission permission='ShowListNews'>
							<li><g:link class="mmlink" controller="newsEditor"  onclick="hideMegaMenu('consoleMegaMenu')">List News</g:link></li>
							</tds:hasPermission>
							<tds:hasPermission permission='CreateNews'>
							<li><a class="mmlink" href="javascript:openCreateNewsDialog()" onclick="hideMegaMenu('consoleMegaMenu')">Create News</a></li>
							</tds:hasPermission>
							<tds:hasPermission permission='HelpMenuView'>
							<li><a class="mmlink" href="javascript:window.open('https://ops.tdsops.com/twiki/bin/view/Main/DataCenterMoves/TranManHelp?cover=print','help');" onclick="hideMegaMenu('consoleMegaMenu')">help</a></li>
							</tds:hasPermission>
						</ul>
					</td>
					<td style="vertical-align:top"><span class="megamenuSection">Cart Tracker</span><br />
						<ul>
							<tds:hasPermission permission='ShowCartTracker'>
							<li><g:link class="mmlink" controller="cartTracking" action="cartTracking"  onclick="hideMegaMenu('consoleMegaMenu')">Cart Status</g:link></li>
							<li><a class="mmlink" href="#" onclick="hideMegaMenu('consoleMegaMenu')">Truck GPS Tracking</a></li>
							</tds:hasPermission>
							<tds:hasPermission permission='HelpMenuView'>
							<li><a class="mmlink" href="javascript:window.open('https://ops.tdsops.com/twiki/bin/view/Main/DataCenterMoves/TranManHelp?cover=print','help');" onclick="hideMegaMenu('consoleMegaMenu')">help</a></li>
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
							<li><g:link class="home mmlink" controller="dashboard" action="userPortal" onclick="hideMegaMenu('dashboardMegaMenu')">Home</g:link></li>
							<tds:hasPermission permission='MoveBundleShowView'>
							<li><g:link class="home mmlink" controller="moveBundle" action="planningStats" onclick="hideMegaMenu('dashboardMegaMenu')">Planning Dashboard</g:link></li>
							</tds:hasPermission>
							<li><g:link class="home mmlink" controller="dashboard" onclick="hideMegaMenu('dashboardMegaMenu')">Event Dashboard</g:link></li>
							<tds:hasPermission permission='AssetTrackerMenuView'>
							<li><g:link class="home mmlink" controller="clientConsole" onclick="hideMegaMenu('dashboardMegaMenu')">Asset Tracker</g:link></li>
							</tds:hasPermission>
							<tds:hasPermission permission='HelpMenuView'>
							<li><a class="mmlink" href="javascript:window.open('https://ops.tdsops.com/twiki/bin/view/Main/DataCenterMoves/TranManHelp?cover=print','help');" onclick="hideMegaMenu('dashboardMegaMenu')">help</a></li>
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
							<li><a href="/tdstm/reports/getBundleListForReportDialog?reportId=Rack+Layout" class="home mmlink" onclick="hideMegaMenu('reportsMegaMenu')">Racks (old)</a> </li>
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
							<li><a href="/tdstm/reports/getBundleListForReportDialog?reportId=Team+Worksheets" class="home mmlink" onclick="hideMegaMenu('reportsMegaMenu')">Event Team Worksheets</a> </li>
							<li><a href="/tdstm/reports/getBundleListForReportDialog?reportId=cart+Asset" class="home mmlink" onclick="hideMegaMenu('reportsMegaMenu')">Logistics Team Worksheets</a></li>
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
    <div id="personDialog" title="Edit Person" style="display:none;">
      <div class="dialog">
          <div class="dialog">
            <table>
              <tbody>
				<tr>
					<td colspan="2"><div class="required"> Fields marked ( * ) are mandatory </div> </td>
				</tr>

				<tr class="prop">
					<td valign="top" class="name">
						<label for="firstName"><b>First Name:&nbsp;<span style="color: red">*</span></b></label>
					</td>
					<td valign="top" class="value">
						<input type="text" maxlength="64" id="firstNameId" name="firstName"/>
					</td>
				</tr>
				
				<tr class="prop" style="display:none;">
					<td valign="top" class="name">
						<label for="username"><b>User Name:&nbsp;<span style="color: red">*</span></b></label>
					</td>
					<td valign="top" class="value">
						<input type="text" maxlength="64" id="prefUsernameId" name="username" value="${user.username}"/>
					</td>
				</tr>

				<tr class="prop">
				  <td valign="top" class="name">
					<label for="middleName">Middle Name:</label>
				  </td>
				  <td valign="top" class="value">
					<input type="text" maxlength="64" id="middleNameId" name="middleName"/>
	              </td>
                </tr>

                <tr class="prop">
                  <td valign="top" class="name">
                    <label for="lastName">Last Name:</label>
                  </td>
                  <td valign="top" class="value">
                    <input type="text" maxlength="64" id="lastNameId" name="lastName"/>
                  </td>
                </tr>

                <tr class="prop">
                  <td valign="top" class="name">
                    <label for="nickName">Nick Name:</label>
                  </td>
                  <td valign="top" class="value">
                    <input type="text" maxlength="64" id="nickNameId" name="nickName"/>
                  </td>
                </tr>
                <tr class="prop">
                  <td valign="top" class="name">
                    <label for="title">Title:</label>
                  </td>
                  <td valign="top" class="value">
                    <input type="text" maxlength="34" id="titleId" name="title"/>
                  </td>
                </tr>
                <tr class="prop">
                  <td valign="top" class="name">
                    <label for="nickName">Email:</label>
                  </td>
                  <td valign="top" class="value">
                    <input type="text" maxlength="64" id="emailId" name="email"/>
                  </td>
                </tr>
                
                <tds:hasPermission permission='PersonExpiryDate'>
	                <tr class="prop">
						<td valign="top" class="name">
							<label for="nickName"><b>Expiry Date:<span style="color: red">*</span></label>
						</td>
						<td valign="top" class="value">
						<script type="text/javascript">
							$(document).ready(function(){
					        	$("#expiryDateId").datetimepicker();
					        });
						</script>
	        	        <input type="text" maxlength="64" id="expiryDateId" name="expiryDate"/>
						<input type="text" maxlength="64" id="expiryDateId" name="expiryDate" readonly="readonly" style="background: none;border: 0"/>
						</td>
					</tr>
                </tds:hasPermission>
                
                <tr class="prop">
					<td valign="top" class="name">
						<label for="title">Time Zone:</label>
					</td>
					<td valign="top" class="value">
						<g:select name="timeZone" id="timeZoneId" from="${['GMT','PST','PDT','MST','MDT','CST','CDT','EST','EDT']}" 
						value="${session.getAttribute('CURR_TZ')?.CURR_TZ}"/>
					</td>
				</tr>
				
					<tr class="prop">
						<td valign="top" class="name">
							<label for="startPage">Start Page:</label>
						</td>
						<td valign="top" class="value">
						<g:if test="${RolePermissions.hasPermission('AdminMenuView')}">
							<g:select name="startPage" id="startPage" from="${['Project Settings','Current Dashboard','Admin Portal']}" 
							value="${session.getAttribute('START_PAGE')?.START_PAGE}"/>
						</g:if>
						<g:else>
						<g:select name="startPage" id="startPage" from="${['Project Settings','Current Dashboard']}" 
							value="${session.getAttribute('START_PAGE')?.START_PAGE}"/>
						</g:else>
						</td>
					</tr>
				<tr class="prop">
					<td valign="top" class="name">
                       <label for="title">Power In:</label>
					</td>
					<td valign="top" class="value">
						<g:select name="powerType" id="powerTypeId" from="${['Watts','Amps']}" 
						value="${session.getAttribute('CURR_POWER_TYPE')?.CURR_POWER_TYPE}"/>
					</td>
				</tr>
				<tr class="prop">
					<td valign="top" class="name">
                       <label for="title">Model Score:</label>
					</td>
					<td valign="top" class="value">
                       <input type="text" name ="modelScore" id ="modelScoreId" readonly="readonly" value="${person?.modelScore}"/>
					</td>
				</tr>
				<tr>
					<td>
						Hide password:
					</td>
					<td>
						<input type="checkbox" onchange="togglePasswordVisibility(this)" id="showPasswordId"/>
					</td>
				</tr>
              	<tr class="prop">
                	<td valign="top" class="name">
                    	<label for="password">Old Password:&nbsp;</label>
					</td>
                    <td valign="top" class="value">
                    	<input type="hidden" id="personId" name="personId" value=""/>
						<input type="text" maxlength="25" name="oldPassword" id="oldPasswordId" value=""/>
					</td>
				</tr>
              	<tr class="prop">
                	<td valign="top" class="name">
                    	<label for="password">New Password:&nbsp;</label>
					</td>
                    <td valign="top" class="value">
						<input type="text" maxlength="25" name="newPassword" onkeyup="checkPassword(this)" id="newPasswordId" value=""/>
					</td>
				</tr>
				<tr>
					<td>
						Requirements:
					</td>
					<td>
						<em id="usernameRequirementId">Password must not contain the username</em><br/>
						<em id="lengthRequirementId">Password must be at least 8 characters long</em><br/>
						<b id="passwordRequirementsId">Password must contain at least 3 of these requirements: </b><br/>
						<ul>
							<li><em id="uppercaseRequirementId">Uppercase characters</em></li>
							<li><em id="lowercaseRequirementId">Lowercase characters</em></li>
							<li><em id="numericRequirementId">Numeric characters</em></li>
							<li><em id="symbolRequirementId">Nonalphanumeric characters</em></li>
						</ul>
					</td>
				</tr>
              </tbody>
            </table>
          </div>
          <div class="buttons">
            <span class="button"><input type="button" class="edit" value="Update" onclick="changePersonDetails()"/></span>
            <span class="button"><input type="button" class="delete" onclick="jQuery('#personDialog').dialog('close')" value="Cancel" /></span>
          </div>
          <g:render template="../newsEditor/newsEditor"></g:render>
      </div>
    </div>
    <g:javascript src="tdsmenu.js" />
    <div id="userPrefDivId" style="display: none;min-width:250px;" title="${session.getAttribute("LOGIN_PERSON").name } Preferences">
    </div>
  </body>
</html>
