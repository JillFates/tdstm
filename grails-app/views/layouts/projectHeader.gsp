<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
  <head>
    <meta http-equiv="Content-Type" content="text/html; charset=iso-8859-1" />
    <meta http-equiv="X-UA-Compatible" content="IE=EmulateIE7" />
    <title><g:layoutTitle default="Grails" /></title>
    <link rel="stylesheet" href="${createLinkTo(dir:'css',file:'main.css')}" type="text/css"/>
    <link rel="stylesheet" href="${createLinkTo(dir:'css',file:'tds.css')}" type="text/css"/>
    <link rel="shortcut icon" href="${createLinkTo(dir:'images',file:'tds.ico')}" type="image/x-icon" />
    <link type="text/css" rel="stylesheet" href="${createLinkTo(dir:'css',file:'ui.core.css')}" />
    <link type="text/css" rel="stylesheet" href="${createLinkTo(dir:'css',file:'ui.dialog.css')}" />
    <link type="text/css" rel="stylesheet" href="${createLinkTo(dir:'css',file:'ui.theme.css')}" />
    <link rel="stylesheet" href="${createLinkTo(dir:'css',file:'ui.datetimepicker.css')}" type="text/css"/>
    
    <g:javascript library="prototype" />
    <jq:plugin name="jquery.combined" />
    <g:javascript src="crawler.js" />
    <g:layoutHead />
   
    <link type="text/css" rel="stylesheet" href="${createLinkTo(dir:'css',file:'dropDown.css')}" />    

   <script type="text/javascript">
   		$(document).ready(function() {
      		$("#personDialog").dialog({ autoOpen: false })
      		${remoteFunction(controller:'userLogin', action:'updateLastPageLoad')}
     	})
     	var emailRegExp = /^([0-9a-zA-Z]+([_.-]?[0-9a-zA-Z]+)*@[0-9a-zA-Z]+[0-9,a-z,A-Z,.,-]+\.[a-zA-Z]{2,4})+$/
     	var dateRegExpForExp  = /^(0[1-9]|1[012])[/](0[1-9]|[12][0-9]|3[01])[/](19|20)\d\d ([0-1][0-9]|[2][0-3])(:([0-5][0-9])){1,2} ([APap][Mm])$/;
     	var currentMenuId = "";
   </script>
  </head>
	<% def currProj = session.getAttribute("CURR_PROJ");
	   def setImage = session.getAttribute("setImage");
    def projectId = currProj.CURR_PROJ ;
    def moveEventId = session.getAttribute("MOVE_EVENT")?.MOVE_EVENT ;
    def moveBundleId = session.getAttribute("CURR_BUNDLE")?.CURR_BUNDLE ;
	def moveBundleName = moveBundleId ? MoveBundle.findById( moveBundleId ) : 'UNSELECTED'
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
    %>
  <body>
    <div class="main_body">

      <div class="tds_header">
      	<div class="header_left">
      		<g:if test="${setImage}">
    	  		<img src="${createLink(controller:'project', action:'showImage', id:setImage)}" style="height: 30px;"/>
    	  	</g:if>
	      	<g:else>      	
     			<a href="http://www.transitionaldata.com/" target="new"><img src="${createLinkTo(dir:'images',file:'tds.jpg')}" style="float: left;border: 0px"/></a>      	    	 
    		</g:else>
    	</div>
      <div class="title">&nbsp;Transition Manager
      	<g:if test="${currProjObj}"> - ${currProjObj.name} </g:if>
      	<g:if test="${moveEvent}"> : ${moveEvent?.name}</g:if>
      	<g:if test="${moveBundleId}"> : ${moveBundleName}</g:if>
      </div>
        <div class="header_right"><br />
          <div style="font-weight: bold;">
          <jsec:isLoggedIn>
			<strong>
			<div style="float: left;">
			<g:if test="${isIE6}">
				<span><img title="Note: MS IE6 has limited capability so functions have been reduced." src="${createLinkTo(dir:'images/skin',file:'warning.png')}" style="width: 14px;height: 14px;float: left;padding-right: 3px;"/></span>
			</g:if>
			<g:remoteLink controller="person" action="getPersonDetails" id="${session.getAttribute('LOGIN_PERSON').id}" onmouseover="showMegaMenu('#userMegaMenu')" onmouseout="mclosetime()" onComplete="updatePersonDetails(e)">
			&nbsp;<span id="loginUserId">${session.getAttribute("LOGIN_PERSON").name } <g:if test="${person?.modelScore}"><span style="font-weight:normal"> (${person?.modelScore})</span></g:if> </span>
			</g:remoteLink>
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
              &nbsp;<g:link controller="auth" action="signOut">sign out</g:link>
          </jsec:isLoggedIn>
          </div>
        </div>
      </div>

      <g:if test="${currProj}">
	      <div class="menu2">
	      <ul>
		<jsec:hasRole name="ADMIN">
			<li id="adminMenuId"><g:link class="home" onmouseover="showMegaMenu('#adminMegaMenu')" onmouseout="mclosetime()" controller="auth" action="home">Admin <apan class="ui-icon ui-icon-triangle-1-s"></span></g:link></li>
		</jsec:hasRole>
			<li id="projectMenuId"><g:link class="home" onmouseover="showMegaMenu('#projectMegaMenu')" onmouseout="mclosetime()" controller="projectUtil">Client/Project</g:link></li>
			<li id="roomMenuId"><g:link class="home" onmouseover="showMegaMenu('#racksMegaMenu')" onmouseout="mclosetime()" controller="room">Rooms</g:link></li>
			<li id="rackMenuId"><g:link class="home" onmouseover="showMegaMenu('#racksMegaMenu')" onmouseout="mclosetime()" controller="rackLayouts" action="create">Racks</g:link></li>
	        <jsec:lacksAllRoles in="['MANAGER','OBSERVER']"> 
			<li id="assetMenuId"><g:link class="home" onmouseover="showMegaMenu('#assetMegaMenu')" onmouseout="mclosetime()" controller="assetEntity" action="assetImport">Assets</g:link></li>
			<li id="eventMenuId"><g:link class="home" onmouseover="showMegaMenu('#bundleMegaMenu')" onmouseout="mclosetime()" controller="moveEvent" action="show" >Events</g:link></li>
			<li id="bundleMenuId"><g:link class="home" onmouseover="showMegaMenu('#bundleMegaMenu')" onmouseout="mclosetime()" controller="moveBundle" action="show" params="[projectId:currProjObj?.id]">Bundles</g:link></li>
			<li id="teamMenuId"><g:link class="home" onmouseover="showMegaMenu('')" onmouseout="mclosetime()" controller="clientTeams" params="[projectId:currProjObj?.id]">Teams</g:link></li>
			<li>&nbsp;</li>
	        </jsec:lacksAllRoles>
	        <jsec:hasAnyRole in="['ADMIN','SUPERVISOR','MANAGER']">
			<li id="consoleMenuId"><g:link class="home" onmouseover="showMegaMenu('#consoleMegaMenu')" onmouseout="mclosetime()" controller="assetEntity" action="dashboardView" params="[projectId:currProjObj?.id, 'showAll':'show']">Console</g:link></li>
	        </jsec:hasAnyRole>
			<li id="dashboardMenuId"><g:link class="home" onmouseover="showMegaMenu('')" controller="dashboard" params="[projectId:currProjObj?.id]">Dashboard</g:link> </li>
			<li id="assetTrackerMenuId"><g:link class="home" onmouseover="showMegaMenu('')" controller="clientConsole" params="[projectId:currProjObj?.id]">Asset Tracker</g:link> </li>
	        <jsec:lacksAllRoles in="['MANAGER','OBSERVER']">
	        	<li id="reportsMenuId"><g:link class="home" onmouseover="showMegaMenu('#reportsMegaMenu')" onmouseout="mclosetime()" controller="reports" params="[projectId:currProjObj?.id]">Reports</g:link></li>
	        </jsec:lacksAllRoles>
	      </ul>
	    </div>
	    
		<div class="megamenu" id="adminMegaMenu" onmouseover="showMegaMenu('#adminMegaMenu')" onmouseout="mclosetime()" style="display: none;">
			<jsec:hasRole name="ADMIN">
			<table class="mmtable"><tr>
			<td style="vertical-align:top"><span class="megamenuSection">Administration</span><br />
				<ul >
					<li><g:link class="mmlink" controller="auth" action="home">Admin Portal</g:link> </li>
					<li>&nbsp;</li>
					<li><a class="mmlink" href="javascript:window.open('https://ops.tdsops.com/twiki/bin/view/Main/DataCenterMoves/TranManReleaseNotes?cover=print');">Release Notes</a></li>
					<li><a class="mmlink" href="javascript:window.open('https://ops.tdsops.com/twiki/bin/view/Main/DataCenterMoves/TMAdminPortal?cover=print');">help</a></li>
				</ul>
			</td>
			<td style="vertical-align:top"><span class="megamenuSection">Manage Clients</span><br />
				<ul >
					<li><g:link class="mmlink" controller="partyGroup" id="${partyGroup}">List Companies</g:link></li>
					<li><g:link class="mmlink" controller="person" id="${partyGroup}">List Staff</g:link></li>
					<li><g:link class="mmlink" controller="userLogin" id="${partyGroup}">List Users</g:link></li>
					<li><a class="mmlink" href="javascript:window.open('https://ops.tdsops.com/twiki/bin/view/Main/DataCenterMoves/TMCreatePerson?cover=print');">help</a></li>
				</ul>
			</td>
			<td style="vertical-align:top"><span class="megamenuSection">Manage Workflows</span><br />
				<ul >
					<li><g:link class="mmlink" controller="workflow" action="home">List Workflows </g:link> </li>
					<li>&nbsp;</li>
					<li>&nbsp;</li>
					<li><a class="mmlink" href="javascript:window.open('https://ops.tdsops.com/twiki/bin/view/Main/DataCenterMoves/TMManageWorkflows?cover=print');">help</a></li>
				</ul>
			</td>
			<td style="vertical-align:top"><span class="megamenuSection">Manage Model Library</span><br />
				<ul >
					<li><g:link class="mmlink" controller="manufacturer" id="${partyGroup}">Manufacturers</g:link></li>
					<li><g:link class="mmlink" controller="model" id="${partyGroup}">Models</g:link></li>
					<li><g:link class="mmlink" controller="model" action="importExport">Sync</g:link></li>
					<li><a class="mmlink" href="javascript:window.open('https://ops.tdsops.com/twiki/bin/view/Main/DataCenterMoves/TMModelLibrary?cover=print');">help</a></li>
				</ul>
			</td>
			</tr></table>
			</jsec:hasRole>
		</div>
		<div class="menu2" id="adminMenu" style="background-color:#003366;display: none;">
		<ul >
			<jsec:hasRole name="ADMIN">
			<li><g:link class="home" controller="auth" action="home">Admin</g:link> </li>
			<li><g:link class="home" controller="workflow" action="home">Workflows </g:link> </li>
			<li><g:link class="home" controller="partyGroup" id="${partyGroup}">Company</g:link></li>
			<li><g:link class="home" controller="person" id="${partyGroup}">Staff</g:link></li>
			<li><g:link class="home" controller="userLogin" id="${partyGroup}">Users</g:link></li>
			<li><g:link class="home" controller="manufacturer" id="${partyGroup}">Manufacturers</g:link></li>
			<li><g:link class="home" controller="model" id="${partyGroup}">Models</g:link></li>
			<li><g:link class="home" controller="model" action="importExport">Sync</g:link></li>
		</jsec:hasRole>
		</ul>
		</div>

		<div class="megamenu" id="projectMegaMenu" onmouseover="showMegaMenu('#projectMegaMenu')" onmouseout="mclosetime()" style="display: none;">
			<table class="mmtable"><tr>
			<td style="vertical-align:top"><span class="megamenuSection">Client/Project</span><br />
				<ul >
					<li><g:link class="mmlink" controller="project" action="list">List Projects</g:link></li>
					<jsec:hasRole name="ADMIN">
					<li><g:link class="mmlink" controller="project" action="create">Create Project</g:link></li>
					</jsec:hasRole>
					<li>&nbsp;</li>
					<jsec:hasRole name="ADMIN">
					<li><a class="mmlink" href="javascript:window.open('javascript:window.open('https://ops.tdsops.com/twiki/bin/view/Main/DataCenterMoves/TMClientProjectSettings?cover=print');">help</a></li>
					</jsec:hasRole>
				</ul>
			</td>
			<g:if test="${currProjObj}">
			<td style="vertical-align:top"><span class="megamenuSection">For <strong>${currProjObj.name}</strong></span><br />
				<ul >
					<li><g:link class="mmlink" controller="projectUtil">Project Settings</g:link></li>
					<li><g:link class="mmlink" controller="person" action="projectStaff" params="[projectId:currProjObj?.id]" >Project Staff</g:link></li>
					<li>&nbsp;</li>
					<jsec:hasRole name="ADMIN">
					<li><a class="mmlink" href="javascript:window.open('javascript:window.open('https://ops.tdsops.com/twiki/bin/view/Main/DataCenterMoves/TMProjectStaff?cover=print');">help</a></li>
					</jsec:hasRole>
				</ul>
			</td>
			</g:if>
			</tr></table>
		</div>
		<div class="menu2" id="projectMenu" style="background-color:#003366;display: none;">
		<ul >
			<li><g:link class="home" controller="project" action="list">List Projects</g:link> </li>
			<g:if test="${currProjObj}"><li><g:link class="home" controller="projectUtil">Project: ${currProjObj.name}</g:link></li></g:if>
			<jsec:lacksAllRoles in="['MANAGER','OBSERVER']"> 
			<li><g:link class="home" controller="person" action="projectStaff" params="[projectId:currProjObj?.id]" >Staff</g:link></li>
			</jsec:lacksAllRoles>
		</ul>
		</div>

		<div class="megamenu" id="racksMegaMenu" onmouseover="showMegaMenu('#racksMegaMenu')" onmouseout="mclosetime()" style="display: none;">
			<table class="mmtable"><tr>
			<td style="vertical-align:top"><span class="megamenuSection">Rooms</span><br />
				<ul >
					<li><g:link class="mmlink" params="[viewType:'list']" controller="room">List Rooms</g:link></li>
					<li>&nbsp;</li>
					<li>&nbsp;</li>
					<jsec:hasRole name="ADMIN">
					<li><a class="mmlink" href="javascript:window.open('javascript:window.open('https://ops.tdsops.com/twiki/bin/view/Main/DataCenterMoves/TranManHelp?cover=print');">help</a></li>
					</jsec:hasRole>
				</ul>
			</td>
			<td style="vertical-align:top"><span class="megamenuSection">Racks</span><br />
				<ul >
					<li><g:link class="mmlink" controller="rackLayouts" action="create">Racks</g:link></li>
					<li>&nbsp;</li>
					<li>&nbsp;</li>
					<jsec:hasRole name="ADMIN">
					<li><a class="mmlink" href="javascript:window.open('javascript:window.open('https://ops.tdsops.com/twiki/bin/view/Main/DataCenterMoves/TranManHelp?cover=print');">help</a></li>
					</jsec:hasRole>
				</ul>
			</td>
			</tr></table>
		</div>

		<div class="megamenu" id="assetMegaMenu" onmouseover="showMegaMenu('#assetMegaMenu')" onmouseout="mclosetime()" style="display: none;">
			<table class="mmtable"><tr>
			<td style="vertical-align:top"><span class="megamenuSection">Manage Data</span><br />
				<ul >
					<li><g:link class="mmlink" controller="assetEntity" action="assetImport" params="[projectId:currProjObj?.id]">Import/Export</g:link></li>
					<li><g:link class="mmlink" controller="assetEntity" action="assetImport" params="[projectId:currProjObj?.id]">Manage Batches</g:link></li>
					<li><g:link class="mmlink" controller="assetEntity" action="listComment" params="[projectId:currProjObj?.id]">List Comments</g:link></li>
					<jsec:hasRole name="ADMIN">
					<li><a class="mmlink" href="javascript:window.open('javascript:window.open('https://ops.tdsops.com/twiki/bin/view/Main/DataCenterMoves/TranManHelp?cover=print');">help</a></li>
					</jsec:hasRole>
				</ul>
			</td>
			<td style="vertical-align:top"><span class="megamenuSection">Assets</span><br />
				<ul >
					<li><g:link class="mmlink" controller="assetEntity" params="[projectId:currProjObj?.id]">List Assets</g:link> </li>
					<li>&nbsp;</li>
					<li>&nbsp;</li>
					<jsec:hasRole name="ADMIN">
					<li><a class="mmlink" href="javascript:window.open('javascript:window.open('https://ops.tdsops.com/twiki/bin/view/Main/DataCenterMoves/TranManHelp?cover=print');">help</a></li>
					</jsec:hasRole>
				</ul>
			</td>
			<td style="vertical-align:top"><span class="megamenuSection">Applications</span><br />
				<ul >
					<li><g:link class="mmlink" controller="application" action="list"  params="[projectId:currProjObj?.id]">List Apps</g:link></li>
					<li>&nbsp;</li>
					<li>&nbsp;</li>
					<jsec:hasRole name="ADMIN">
					<li><a class="mmlink" href="javascript:window.open('javascript:window.open('https://ops.tdsops.com/twiki/bin/view/Main/DataCenterMoves/TranManHelp?cover=print');">help</a></li>
					</jsec:hasRole>
				</ul>
			</td>
			<td style="vertical-align:top"><span class="megamenuSection">Databases</span><br />
				<ul >
					<li><g:link class="mmlink" controller="database" params="[projectId:currProjObj?.id]">List DBs</g:link></li>
					<li>&nbsp;</li>
					<li>&nbsp;</li>
					<jsec:hasRole name="ADMIN">
					<li><a class="mmlink" href="javascript:window.open('javascript:window.open('https://ops.tdsops.com/twiki/bin/view/Main/DataCenterMoves/TranManHelp?cover=print');">help</a></li>
					</jsec:hasRole>
				</ul>
			</td>
			<td style="vertical-align:top"><span class="megamenuSection">Files</span><br />
				<ul >
					<li><g:link class="mmlink" controller="files" params="[projectId:currProjObj?.id]">List Files</g:link></li>
					<li>&nbsp;</li>
					<li>&nbsp;</li>
					<jsec:hasRole name="ADMIN">
					<li><a class="mmlink" href="javascript:window.open('javascript:window.open('https://ops.tdsops.com/twiki/bin/view/Main/DataCenterMoves/TranManHelp?cover=print');">help</a></li>
					</jsec:hasRole>
				</ul>
			</td>
			</tr></table>
		</div>
		<div class="menu2" id="assetMenu" style="background-color:#003366;display: none;">
		<ul >
			<li><g:link class="home" controller="assetEntity" action="assetImport" params="[projectId:currProjObj?.id]">Import/Export</g:link> </li>
			<li><g:link class="home" controller="assetEntity" params="[projectId:currProjObj?.id]">List Assets</g:link></li>
			<li><g:link class="home" controller="application" action="list"  params="[projectId:currProjObj?.id]">List Apps</g:link></li>
			<li><g:link class="home" controller="database" params="[projectId:currProjObj?.id]">List DBs</g:link></li>
			<li><g:link class="home" controller="files" params="[projectId:currProjObj?.id]">List Files</g:link></li>
			<li><g:link class="home" controller="assetEntity" action="listComment" params="[projectId:currProjObj?.id]">List Comments</g:link></li>
		</ul>
		</div>

		<div class="megamenu" id="bundleMegaMenu" onmouseover="showMegaMenu('#bundleMegaMenu')" onmouseout="mclosetime()" style="display: none;">
			<table class="mmtable"><tr>
			<td style="vertical-align:top"><span class="megamenuSection">Events</span><br />
				<ul >
					<li><g:link class="mmlink" controller="moveEvent" action="list" >List Events</g:link> </li>
					<li><g:link class="mmlink" controller="project" action="create">Create Event</g:link></li>
					<li>&nbsp;</li>
					<li>&nbsp;</li>
					<jsec:hasRole name="ADMIN">
					<li><a class="mmlink" href="javascript:window.open('javascript:window.open('https://ops.tdsops.com/twiki/bin/view/Main/DataCenterMoves/TranManHelp?cover=print');">help</a></li>
					</jsec:hasRole>
				</ul>
			</td>
			<td style="vertical-align:top"><span class="megamenuSection">Bundles</span><br />
				<ul >
					<li><g:link class="mmlink" controller="moveBundle" action="list" >List Bundles</g:link> </li>
					<li><g:link class="mmlink" controller="moveBundle" action="create">Create Bundle</g:link></li>
					<li>&nbsp;</li>
					<li>&nbsp;</li>
					<jsec:hasRole name="ADMIN">
					<li><a class="mmlink" href="javascript:window.open('javascript:window.open('https://ops.tdsops.com/twiki/bin/view/Main/DataCenterMoves/TranManHelp?cover=print');">help</a></li>
					</jsec:hasRole>
				</ul>
			</td>
			<g:if test="${currProjObj}">
			<td style="vertical-align:top"><span class="megamenuSection">For <strong>${moveBundleName}</strong>:</span><br />
				<ul >
					<li><g:link class="mmlink" controller="moveBundle" action="show" params="[projectId:currProjObj?.id]">Bundle Settings</g:link></li>
					<li><g:link controller="moveBundleAsset" action="assignAssetsToBundle" params="[bundleId:moveBundleId]" >Bundled Assets</g:link> </li>
					<li><g:link class="mmlink" controller="projectTeam" action="list" params="[bundleId:moveBundleId]" >List Teams</g:link></li>
					<li><g:link class="mmlink" controller="moveBundleAsset" action="bundleTeamAssignment" params="[bundleId:moveBundleId, rack:'UnrackPlan']" >Assign Assets to Teams</g:link> </li>
					<jsec:hasRole name="ADMIN">
					<li><a class="mmlink" href="javascript:window.open('javascript:window.open('https://ops.tdsops.com/twiki/bin/view/Main/DataCenterMoves/TranManHelp?cover=print');">help</a></li>
					</jsec:hasRole>
				</ul>
			</td>
			</g:if>
			</tr></table>
		</div>
		<div class="menu2" id="bundleMenu" style="background-color:#003366;display:none;">
		<ul >
				<li><g:link class="home" controller="moveBundle" action="list">List</g:link></li>
				<li><g:link class="home" controller="moveBundle" action="show" params="[projectId:currProjObj?.id]">Bundle: ${moveBundleName}</g:link></li>
				<li><g:link class="home" controller="projectTeam" action="list" params="[bundleId:moveBundleId]" >Team</g:link></li>
				<li><g:link controller="moveBundleAsset" action="assignAssetsToBundle" params="[bundleId:moveBundleId]" >Bundle Asset Assignment</g:link> </li>
				<li><g:link class="home" controller="moveBundleAsset" action="bundleTeamAssignment" params="[bundleId:moveBundleId, rack:'UnrackPlan']" >Bundle Team Assignment </g:link> </li>
				<li><g:link class="home" controller="walkThrough" >Walkthrough</g:link></li>
			</ul>
		</div>

		<div class="megamenu" id="consoleMegaMenu" onmouseover="showMegaMenu('#consoleMegaMenu')" onmouseout="mclosetime()" style="display: none;">
			<table class="mmtable"><tr>
			<td style="vertical-align:top"><span class="megamenuSection">Supervisor Console</span><br />
				<jsec:hasAnyRole in="['ADMIN','SUPERVISOR','MANAGER']">
				<ul >
					<li><g:link class="mmlink" controller="assetEntity" action="dashboardView" params="[projectId:currProjObj?.id, 'showAll':'show','teamType':'MOVE']">Supervise Move Techs</g:link></li>
					<li><g:link class="mmlink" controller="assetEntity" action="dashboardView" params="[projectId:currProjObj?.id, 'showAll':'show','teamType':'ADMIN']">Supervise Admins</g:link></li>
					<li>&nbsp;</li>
					<jsec:hasRole name="ADMIN">
					<li><a class="mmlink" href="javascript:window.open('javascript:window.open('https://ops.tdsops.com/twiki/bin/view/Main/DataCenterMoves/TranManHelp?cover=print');">help</a></li>
					</jsec:hasRole>
				</ul>
				</jsec:hasAnyRole>
			</td>
			<td style="vertical-align:top"><span class="megamenuSection">News</span><br />
		        <jsec:hasAnyRole in="['ADMIN']">
				<ul >
					<li><g:link class="mmlink" controller="newsEditor" params="[projectId:currProjObj?.id]">List News Items</g:link></li>
					<li><a class="mmlink" href="#">Create News Item</a></li>
					<li>&nbsp;</li>
					<jsec:hasRole name="ADMIN">
					<li><a class="mmlink" href="javascript:window.open('javascript:window.open('https://ops.tdsops.com/twiki/bin/view/Main/DataCenterMoves/TranManHelp?cover=print');">help</a></li>
					</jsec:hasRole>
				</ul>
		        </jsec:hasAnyRole>
			</td>
			<td style="vertical-align:top"><span class="megamenuSection">Cart Tracker</span><br />
				<jsec:hasAnyRole in="['ADMIN','SUPERVISOR','PROJECT_ADMIN']">
				<ul >
					<li><g:link class="mmlink" controller="cartTracking" action="cartTracking" params="[projectId:currProjObj?.id]">Cart Status</g:link></li>
					<li><a class="mmlink" href="#">Truck GPS Tracking</a></li>
					<li>&nbsp;</li>
					<jsec:hasRole name="ADMIN">
					<li><a class="mmlink" href="javascript:window.open('javascript:window.open('https://ops.tdsops.com/twiki/bin/view/Main/DataCenterMoves/TranManHelp?cover=print');">help</a></li>
					</jsec:hasRole>
				</ul>
				</jsec:hasAnyRole>
			</td>
			</tr></table>
		</div>
	    <div class="menu2" id="consoleMenu" style="background-color:#003366;display: none;">
		<ul >
	        <jsec:hasAnyRole in="['ADMIN','SUPERVISOR','MANAGER']">
			<li><g:link class="home" controller="assetEntity" action="dashboardView" params="[projectId:currProjObj?.id, 'showAll':'show']">Console</g:link></li>
	        </jsec:hasAnyRole>
	        <jsec:hasAnyRole in="['ADMIN']">
			<li><g:link class="home" controller="newsEditor" params="[projectId:currProjObj?.id]">News</g:link></li>
	        </jsec:hasAnyRole>
	        <jsec:hasAnyRole in="['ADMIN','SUPERVISOR','PROJECT_ADMIN']">
			<li><g:link class="home" controller="cartTracking" action="cartTracking" params="[projectId:currProjObj?.id]">Carts</g:link></li>
	        </jsec:hasAnyRole>
		</ul>
	    </div>

		<div class="megamenu" id="reportsMegaMenu" onmouseover="showMegaMenu('#reportsMegaMenu')" onmouseout="mclosetime()" style="display: none;">
			<table class="mmtable"><tr>
			<td style="vertical-align:top"><span class="megamenuSection">Discovery</span><br />
				<jsec:hasAnyRole in="['ADMIN','SUPERVISOR','MANAGER']">
				<ul >
					<li><g:link class="mmlink" controller="assetEntity" action="dashboardView" params="[projectId:currProjObj?.id, 'showAll':'show','teamType':'MOVE']">Supervise Move Techs</g:link></li>
					<li><g:link class="mmlink" controller="assetEntity" action="dashboardView" params="[projectId:currProjObj?.id, 'showAll':'show','teamType':'ADMIN']">Supervise Admins</g:link></li>
					<li>&nbsp;</li>
					<jsec:hasRole name="ADMIN">
					<li><a class="mmlink" href="javascript:window.open('javascript:window.open('https://ops.tdsops.com/twiki/bin/view/Main/DataCenterMoves/TranManHelp?cover=print');">help</a></li>
					</jsec:hasRole>
				</ul>
				</jsec:hasAnyRole>
			</td>
			<td style="vertical-align:top"><span class="megamenuSection">Planning</span><br />
		        <jsec:hasAnyRole in="['ADMIN']">
				<ul >
					<li><g:link class="mmlink" controller="newsEditor" params="[projectId:currProjObj?.id]">List News Items</g:link></li>
					<li><a class="mmlink" href="#">Create News Item</a></li>
					<li>&nbsp;</li>
					<jsec:hasRole name="ADMIN">
					<li><a class="mmlink" href="javascript:window.open('javascript:window.open('https://ops.tdsops.com/twiki/bin/view/Main/DataCenterMoves/TranManHelp?cover=print');">help</a></li>
					</jsec:hasRole>
				</ul>
		        </jsec:hasAnyRole>
			</td>
			<td style="vertical-align:top"><span class="megamenuSection">Move Day</span><br />
				<jsec:hasAnyRole in="['ADMIN','SUPERVISOR','PROJECT_ADMIN']">
				<ul >
					<li><g:link class="mmlink" controller="cartTracking" action="cartTracking" params="[projectId:currProjObj?.id]">Cart Status</g:link></li>
					<li><a class="mmlink" href="#">Truck GPS Tracking</a></li>
					<li>&nbsp;</li>
					<jsec:hasRole name="ADMIN">
					<li><a class="mmlink" href="javascript:window.open('javascript:window.open('https://ops.tdsops.com/twiki/bin/view/Main/DataCenterMoves/TranManHelp?cover=print');">help</a></li>
					</jsec:hasRole>
				</ul>
				</jsec:hasAnyRole>
			</td>
			</tr></table>
		</div>
		<div></div>
		<div class="megamenu" id="userMegaMenu" onmouseover="showMegaMenu('#userMegaMenu')" onmouseout="mclosetime()" style="display: none;">
			<table class="mmtable"><tr>
			<td style="vertical-align:top"><span class="megamenuSection">${session.getAttribute("LOGIN_PERSON").name }</span><br />
				<ul >
					<li>users name</li>
					<li>Account settings...</li>
					<li>&nbsp;</li>
					<li>Model Score: ${person?.modelScore} </li>
				</ul>
			</td>
			<td style="vertical-align:top"><span class="megamenuSection">Sign out</span><br />
			</td>
			</tr></table>
		</div>
				<g:if test="${moveEvent && moveEvent?.inProgress == 'true'}">
			<div class="menu3" id="head_crawler" >
				<div id="crawlerHead">${moveEvent.name} Move Event Status <span id="moveEventStatus"></span>. News: </div>
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
      <div class="main_bottom"><g:layoutBody /></div>
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
                    	<label for="password">Password:&nbsp;</label>
					</td>
                    <td valign="top" class="value">
                    	<input type="hidden" id="personId" name="personId" value=""/>
						<input type="password" maxlength="25" name="password" id="passwordId" value=""/>
					</td>
				</tr>

                <tr class="prop">
                  <td valign="top" class="name">
                    <label for="firstName"><b>First Name:&nbsp;<span style="color: red">*</span></b></label>
                  </td>
                  <td valign="top" class="value">
                    <input type="text" maxlength="64" id="firstNameId" name="firstName"/>
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
                <tr class="prop">
                  <td valign="top" class="name">
                    <label for="nickName"><b>Expiry Date:<span style="color: red">*</span></label>
                  </td>
                  <td valign="top" class="value">
                   <jsec:hasAnyRole in="['ADMIN','SUPERVISOR','PROJECT_ADMIN']">
                   <script type="text/javascript">
						$(document).ready(function(){
				        	$("#expiryDateId").datetimepicker();
				        });
				    </script>
				    
                    <input type="text" maxlength="64" id="expiryDateId" name="expiryDate"/>
                    </jsec:hasAnyRole>
                    <jsec:lacksAllRoles in="['ADMIN','SUPERVISOR','PROJECT_ADMIN']">
                    <input type="text" maxlength="64" id="expiryDateId" name="expiryDate" readonly="readonly" style="background: none;border: 0"/>
                    </jsec:lacksAllRoles>
                  </td>
                </tr>
                <tr class="prop">
                  <td valign="top" class="name">
                    <label for="title">Time Zone:</label>
                  </td>
                  <td valign="top" class="value">
                    <g:select name="timeZone" id="timeZoneId" from="${['GMT','PST','PDT','MST','MDT','CST','CDT','EST','EDT']}" 
                    value="${session.getAttribute('CURR_TZ')?.CURR_TZ}"/>
                  </td>
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
                </tr>
              </tbody>
            </table>
          </div>
          <div class="buttons">
            <span class="button"><input type="button" class="edit" value="Update" onclick="changePersonDetails()"/></span>
            <span class="button"><input type="button" class="delete" onclick="jQuery('#personDialog').dialog('close')" value="Cancel" /></span>
          </div>
          
      </div>
    </div>
    <script type="text/javascript">
	    var timerId;
	    timerId = window.setTimeout("timeOut()",(60000 * 120));
	    
	    function resetTimer() {
	        window.clearTimeout(timerId);
	        timerId = window.setTimeout("timeOut()",(60000 * 120));
	    }
	    function timeOut()
	    {
	         ${remoteFunction(controller:'auth',action:'signOut',onComplete:'sessionExpireOverlay()')};
	    }
	    function sessionExpireOverlay()
	    {
	    	window.parent.location = self.location;
	    }
	       $(document).keydown(function(){ resetTimer(); });
	       $(document).mousedown(function(){ resetTimer(); });
       
   
	    /*---------------------------------------------------
		* Script to load the marquee to scroll the live news
		*--------------------------------------------------*/
		marqueeInit({
			uniqueid: 'head_mycrawler',
			inc: 8, //speed - pixel increment for each iteration of this marquee's movement
			mouse: 'cursor driven', //mouseover behavior ('pause' 'cursor driven' or false)
			moveatleast: 4,
			neutral: 150,
			savedirection: false
		});

	  	// Update person details 
		function updatePersonDetails( e ){
			var personDetails = eval("(" + e.responseText + ")");
			$("#personId").val(personDetails.person.id)
			$("#firstNameId").val(personDetails.person.firstName);
			$("#lastNameId").val(personDetails.person.lastName);
			$("#nickNameId").val(personDetails.person.nickName);
			$("#emailId").val(personDetails.person.email);
			$("#titleId").val(personDetails.person.title);
			$("#expiryDateId").val(personDetails.expiryDate);
			$("#personDialog").dialog('option', 'width', 500)
		    $("#personDialog").dialog("open")
	  	}
		function changePersonDetails(){
			var returnVal = true 
	    	var firstName = $("#firstNameId").val()
	        var email = $("#emailId").val()
	        var expiryDate = $("#expiryDateId").val()
	        if(!firstName) {
	            alert("First Name should not be blank ")
	            returnVal = false
	        } else if( email && !emailRegExp.test(email)){
	        	 alert(email +" is not a valid e-mail address ")
	             returnVal = false
	        } else if(!expiryDate){
	        	alert("Expiry Date should not be blank ")
	            returnVal = false
	        } else  if(!dateRegExpForExp.test(expiryDate)){
		        alert("Expiry Date should be in 'mm/dd/yyyy HH:MM AM/PM' format")
		        returnVal = false
	        }
	        if(returnVal){
				${remoteFunction(controller:'person', action:'updatePerson', 
						params:'\'id=\' + $(\'#personId\').val() +\'&firstName=\'+$(\'#firstNameId\').val() +\'&lastName=\'+$(\'#lastNameId\').val()+\'&nickName=\'+$(\'#nickNameId\').val()+\'&title=\'+$(\'#titleId\').val()+\'&password=\'+$(\'#passwordId\').val()+\'&timeZone=\'+$(\'#timeZoneId\').val()+\'&powerType=\'+$(\'#powerTypeId\').val()+\'&email=\'+$(\'#emailId\').val()+\'&expiryDate=\'+$(\'#expiryDateId\').val()', 
						onComplete:'updateWelcome(e)')}
	        }
		}
	  	function updateWelcome( e ){
		  	var ret = eval("(" + e.responseText + ")");
		  	$("#loginUserId").html(ret[0].name)
		  	$("#tzId").html(ret[0].tz)
		  	$("#personDialog").dialog('close')
		  	window.location.reload()
	  	}
	  	function setUserTimeZone( tz ){
	  		${remoteFunction(controller:'project', action:'setUserTimeZone', 
					params:'\'tz=\' + tz ',	onComplete:'updateTimeZone(e)')}
	  	}
	  	function updateTimeZone( e ){
		  	var sURL = unescape(window.location);
		  	window.location.href = sURL;
	  	}
	  	function showMegaMenu(e){
	  		$('#adminMegaMenu').hide();
	  		$('#projectMegaMenu').hide();
	  		$('#racksMegaMenu').hide();
	  		$('#assetMegaMenu').hide();
	  		$('#bundleMegaMenu').hide();
	  		$('#consoleMegaMenu').hide();
	  		$('#reportsMegaMenu').hide();
	  		$('#userMegaMenu').hide();
	  		if(e!=""){
		  		$(e).show();
				mcancelclosetime();	// cancel close timer
				megamenuitem = e;
				if(e == "#adminMegaMenu"){$("#adminMenuId a").css('background-color','lightblue')}
				if(e == "#projectMegaMenu"){$("#projectMenuId a").css('background-color','lightblue')}
				if(e == "#racksMegaMenu"){$("#racksMenuId a").css('background-color','lightblue')}
				if(e == "#assetMegaMenu"){$("#assetMenuId a").css('background-color','lightblue')}
				if(e == "#bundleMegaMenu"){$("#bundleMenuId a").css('background-color','lightblue')}
				if(e == "#consoleMegaMenu"){$("#consoleMenuId a").css('background-color','lightblue')}
				if(e == "#reportsMegaMenu"){$("#reportsMenuId a").css('background-color','lightblue')}
				if(e == "#userMegaMenu"){$("#userMenuId a").css('background-color','lightblue')}
	  		}
	  	}
	  	function closeMegaMenu() {
			if(megamenuitem) $(megamenuitem).hide();
			$("#adminMenuId a").css('background-color','#0366b0');
			$("#projectMenuId a").css('background-color','#0366b0');
			$("#racksMenuId a").css('background-color','#0366b0');
			$("#assetMenuId a").css('background-color','#0366b0');
			$("#bundleMenuId a").css('background-color','#0366b0');
			$("#consoleMenuId a").css('background-color','#0366b0');
			$("#reportsMenuId a").css('background-color','#0366b0');
			$("#userMenuId a").css('background-color','#0366b0');
			if(currentMenuId == "#adminMenu"){$("#adminMenuId a").css('background-color','#003366')}
			if(currentMenuId == "#projectMenu"){$("#projectMenuId a").css('background-color','#003366')}
			if(currentMenuId == "#racksMenu"){$("#racksMenuId a").css('background-color','#003366')}
			if(currentMenuId == "#assetMenu"){$("#assetMenuId a").css('background-color','#003366')}
			if(currentMenuId == "#bundleMenu"){$("#bundleMenuId a").css('background-color','#003366')}
			if(currentMenuId == "#consoleMenu"){$("#consoleMenuId a").css('background-color','#003366')}
			if(currentMenuId == "#reportsMenu"){$("#reportsMenuId a").css('background-color','#003366')}
			if(currentMenuId == "#userMenu"){$("#userMenuId a").css('background-color','#003366')}
	  	}
	  	// set close timer
		function mclosetime() {
			closetimer = window.setTimeout(closeMegaMenu, timeout);
		}
		// cancel close timer
		function mcancelclosetime() {
		if(closetimer) {
			window.clearTimeout(closetimer);
			closetimer = null;
			}
		}
	  	function showSubMenu(e) {
	  		$('#adminMenu').hide();
	  		$('#projectMenu').hide();
	  		$('#assetMenu').hide();
	  		$('#bundleMenu').hide();
	  		$('#consoleMenu').hide();
	  		showMegaMenu('');
	  		if(e!=""){
		  		$(e).show();
	  		}
	  	}
		showSubMenu(currentMenuId);

		var timeout = 500;
		var closetimer = 0;
		var megamenuitem = 0;
		document.onclick = closeMegaMenu;// close mega when click-out

		function setPower( p ){
			${remoteFunction(controller:'project', action:'setPower', params:'\'p=\' + p ',	onComplete:'updateTimeZone( e )')}
		}
	</script>
  </body>
</html>
