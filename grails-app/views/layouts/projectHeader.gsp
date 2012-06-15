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
      <div class="title">&nbsp;TransitionManager&trade; 
      	<g:if test="${currProjObj}"> - ${currProjObj.name} </g:if>
      	<g:if test="${moveEvent}"> : ${moveEvent?.name}</g:if>
      	<g:if test="${moveBundleId}"> : ${moveBundleName}</g:if>
      </div>
        <div class="header_right" id="userMenuId"><br />
          <div style="font-weight: bold;">
          <jsec:isLoggedIn>
			<strong>
			<div style="float: left;">
			<g:if test="${isIE6}">
				<span><img title="Note: MS IE6 has limited capability so functions have been reduced." src="${createLinkTo(dir:'images/skin',file:'warning.png')}" style="width: 14px;height: 14px;float: left;padding-right: 3px;"/></span>
			</g:if>
			<g:remoteLink controller="person" action="getPersonDetails" id="${session.getAttribute('LOGIN_PERSON').id}" onmouseover="showMegaMenu('#userMegaMenu')" onmouseout="mclosetime()" onComplete="updatePersonDetails(e)" style="float:left;display:inline">
			&nbsp;<span id="loginUserId">${session.getAttribute("LOGIN_PERSON").name }
				<a id="userAnchor" class="ui-icon ui-icon-triangle-1-s" onmouseover="showMegaMenu('#userMegaMenu')" href="javascript:showMegaMenu('#userMegaMenu')" style="float:left;display:inline"></a> </span>
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
              &nbsp;&nbsp;<g:link controller="auth" action="signOut"></g:link>
          </jsec:isLoggedIn>
          </div>
        </div>
      </div>

      <g:if test="${currProj}">
	      <div class="menu2">
	      <ul>
			<tds:hasPermission permission='AdminMenuView'>
			<li id="adminMenuId"><g:link class="home menuhideright" onmouseover="showMegaMenu('#adminMegaMenu')" onmouseout="mclosetime()" controller="auth" action="home">Admin
				<a id="adminAnchor" class="ui-icon ui-icon-triangle-1-s" onmouseover="showMegaMenu('#adminMegaMenu')" href="javascript:showMegaMenu('#adminMegaMenu')" style="display: inline"></a></g:link>
    		    <div class="megamenu admin" id="adminMegaMenu" onmouseover="showMegaMenu('#adminMegaMenu')" onmouseout="mclosetime()" style="display: none;">
					<table class="mmtable room_rack"><tr>
					<td style="vertical-align:top"><span class="megamenuSection">Administration</span><br />
						<ul >
							<li><g:link class="mmlink" controller="auth" action="home" onclick="hideMegaMenu('adminMegaMenu')">Admin Portal</g:link> </li>
							<tds:hasPermission permission='HelpMenuView'>
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
							<li><g:link class="mmlink" controller="partyGroup" id="${partyGroup}" onclick="hideMegaMenu('adminMegaMenu')">List Companies</g:link></li>
							<li><g:link class="mmlink" controller="person" id="${partyGroup}" onclick="hideMegaMenu('adminMegaMenu')">List Staff</g:link></li>
							<li><g:link class="mmlink" controller="userLogin" id="${partyGroup}" onclick="hideMegaMenu('adminMegaMenu')">List Users</g:link></li>
							<tds:hasPermission permission='HelpMenuView'>
							<li><a class="mmlink" href="javascript:window.open('https://ops.tdsops.com/twiki/bin/view/Main/DataCenterMoves/TMCreatePerson?cover=print','help');" onclick="hideMegaMenu('adminMegaMenu')">help</a></li>
							</tds:hasPermission>
						</ul>
					</td>
					<td style="vertical-align:top"><span class="megamenuSection">Manage Workflows</span><br />
						<ul >
							<li><g:link class="mmlink" controller="workflow" action="home" onclick="hideMegaMenu('adminMegaMenu')">List Workflows </g:link> </li>
							<li>&nbsp;</li>
							<li>&nbsp;</li>
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

			<li id="projectMenuId" style="position:relative; float: left;" ><g:link class="home" onmouseover="showMegaMenu('#projectMegaMenu')" onmouseout="mclosetime()" controller="projectUtil">Client/Project
				<a id="projectAnchor" class="ui-icon ui-icon-triangle-1-s" onmouseover="showMegaMenu('#projectMegaMenu')" href="javascript:showMegaMenu('#projectMegaMenu')" style="display: inline"></a></g:link>
				<div class="megamenu client" id="projectMegaMenu" onmouseover="showMegaMenu('#projectMegaMenu')" onmouseout="mclosetime()" style="display: none;">
					<table class="mmtable"><tr>
					<td style="vertical-align:top"><span class="megamenuSection">Client/Project</span><br />
						<ul >
							<li><g:link class="mmlink" controller="project" action="list" params="[active:'active']" onclick="hideMegaMenu('projectMegaMenu')">List Projects</g:link></li>
							<tds:hasPermission permission='CreateProject'>
							<li><g:link class="mmlink" controller="project" action="create"  onclick="hideMegaMenu('projectMegaMenu')">Create Project</g:link></li>
							</tds:hasPermission>
							<li>&nbsp;</li>
							<tds:hasPermission permission='HelpMenuView'>
							<li><a class="mmlink" href="javascript:window.open('https://ops.tdsops.com/twiki/bin/view/Main/DataCenterMoves/TMClientProjectSettings?cover=print','help');"  onclick="hideMegaMenu('projectMegaMenu')">help</a></li>
							</tds:hasPermission>
						</ul>
					</td>
					<g:if test="${currProjObj}">
					<td style="vertical-align:top"><span class="megamenuSection">For <strong>${currProjObj.name}</strong></span><br />
						<ul >
							<li><g:link class="mmlink" controller="projectUtil"  onclick="hideMegaMenu('projectMegaMenu')">Project Settings</g:link></li>
							<li><g:link class="mmlink" controller="person" action="projectStaff"  onclick="hideMegaMenu('projectMegaMenu')">Project Staff</g:link></li>
							<li>&nbsp;</li>
							<tds:hasPermission permission='HelpMenuView'>
							<li><a class="mmlink" href="javascript:window.open('https://ops.tdsops.com/twiki/bin/view/Main/DataCenterMoves/TMProjectStaff?cover=print','help');"  onclick="hideMegaMenu('projectMegaMenu')">help</a></li>
							</tds:hasPermission>
						</ul>
					</td>
					</g:if>
					</tr></table>
				</div>
			</li>

			<li id="roomMenuId" style="position:relative; float: left;"><g:link class="home" onmouseover="showMegaMenu('#racksMegaMenu')" onmouseout="mclosetime()" controller="room">Rooms</g:link>
				<div class="megamenu rooms" id="racksMegaMenu" onmouseover="showMegaMenu('#racksMegaMenu')" onmouseout="mclosetime()" style="display: none;">
					<table class="mmtable room_rack" ><tr>
					<td style="vertical-align:top"><span class="megamenuSection">Rooms</span><br />
						<ul >
							<li><g:link class="mmlink" params="[viewType:'list']" controller="room"  onclick="hideMegaMenu('racksMegaMenu')">List Rooms</g:link></li>
							<li>&nbsp;</li>
							<li>&nbsp;</li>
							<tds:hasPermission permission='HelpMenuView'>
							<li><a class="mmlink" href="javascript:window.open('https://ops.tdsops.com/twiki/bin/view/Main/DataCenterMoves/TranManHelp?cover=print','help');">help</a></li>
							</tds:hasPermission>
						</ul>
					</td>
					<td style="vertical-align:top"><span class="megamenuSection">Racks</span><br />
						<ul >
							<li><g:link class="mmlink" controller="rackLayouts" action="create" onclick="hideMegaMenu('racksMegaMenu')">Racks</g:link></li>
							<li>&nbsp;</li>
							<li>&nbsp;</li>
							<tds:hasPermission permission='HelpMenuView'>
							<li><a class="mmlink" href="javascript:window.open('https://ops.tdsops.com/twiki/bin/view/Main/DataCenterMoves/TranManHelp?cover=print','help');" onclick="hideMegaMenu('racksMegaMenu')">help</a></li>
							</tds:hasPermission>
						</ul>
					</td>
					</tr></table>
				</div>
			</li>
			<tds:hasPermission permission='RackMenuView'>
			<li id="rackMenuId" ><g:link class="home" onmouseover="showMegaMenu('#racksMegaMenu')" onmouseout="mclosetime()" controller="rackLayouts" action="create">Racks
				<a id="rackAnchor" class="ui-icon ui-icon-triangle-1-s" onmouseover="showMegaMenu('#racksMegaMenu')" href="javascript:showMegaMenu('#racksMegaMenu')" style="float: left"></a></g:link>
				</li>
            </tds:hasPermission>
	        <tds:hasPermission permission='AssetMenuView'>
			<li id="assetMenuId" style="position:relative; float:left;"><g:link class="home" onmouseover="showMegaMenu('#assetMegaMenu')" onmouseout="mclosetime()" controller="assetEntity" action="assetSummary" >Assets
				<a id="assetAnchor" class="ui-icon ui-icon-triangle-1-s" onmouseover="showMegaMenu('#assetMegaMenu')" href="javascript:showMegaMenu('#assetMegaMenu')" style="display: inline"></a></g:link>
				<div class="megamenu rooms" id="assetMegaMenu" onmouseover="showMegaMenu('#assetMegaMenu')" onmouseout="mclosetime()" style="display: none;">
					<table class="mmtable room_rack"><tr>
					<tds:hasPermission permission='EditAndDelete'>
					<td style="vertical-align:top"><span class="megamenuSection">Manage Data</span><br />
					
						<ul>
							<tds:hasPermission permission='EditAndDelete'>
							<li><g:link class="mmlink" controller="assetEntity" action="assetImport"  onclick="hideMegaMenu('assetMegaMenu')">Import Assets</g:link></li>
							<li><g:link class="mmlink" controller="dataTransferBatch" action="index" onclick="hideMegaMenu('assetMegaMenu')">Manage Batches</g:link></li>
							</tds:hasPermission>
							<tds:hasPermission permission='AssetMenuView'>
							<li><g:link class="mmlink" controller="assetEntity" action="exportAssets"  onclick="hideMegaMenu('assetMegaMenu')">Export Assets</g:link></li>
							<li><g:link class="mmlink" controller="assetEntity" action="listComment"  onclick="hideMegaMenu('assetMegaMenu')">List Comments</g:link></li>
							</tds:hasPermission>
							<tds:hasPermission permission='HelpMenuView'>
							<li><a class="mmlink" href="javascript:window.open('https://ops.tdsops.com/twiki/bin/view/Main/DataCenterMoves/TranManHelp?cover=print','help');" onclick="hideMegaMenu('assetMegaMenu')">help</a></li>
							</tds:hasPermission>
						</ul>
					</td>
					</tds:hasPermission>
					<td style="vertical-align:top"><span class="megamenuSection">Assets</span><br />
						<ul>
							<li><g:link class="mmlink" controller="assetEntity"  onclick="hideMegaMenu('assetMegaMenu')">List Assets</g:link> </li>
							<li><g:link class="mmlink" controller="application" action="list"  onclick="hideMegaMenu('assetMegaMenu')">List Apps</g:link></li>
							<li><g:link class="mmlink" controller="database"  onclick="hideMegaMenu('assetMegaMenu')">List DBs</g:link></li>
							<li><g:link class="mmlink" controller="files"  onclick="hideMegaMenu('assetMegaMenu')">List Files</g:link></li>
							<tds:hasPermission permission='HelpMenuView'>
							<li><a class="mmlink" href="javascript:window.open('https://ops.tdsops.com/twiki/bin/view/Main/DataCenterMoves/TranManHelp?cover=print','help');" onclick="hideMegaMenu('assetMegaMenu')">help</a></li>
							</tds:hasPermission>
						</ul>
					</td>
					</tr></table>
				</div>	
			</li>
			</tds:hasPermission>
			<tds:hasPermission permission='EventMenuView'>
			<li id="eventMenuId" style="position:relative; float: left;"><g:link class="home" onmouseover="showMegaMenu('#bundleMegaMenu')" onmouseout="mclosetime()" controller="moveEvent" action="show" >Events </g:link>
			
				<div class="megamenu rooms" id="bundleMegaMenu" onmouseover="showMegaMenu('#bundleMegaMenu')" onmouseout="mclosetime()" style="display: none;">
					<table class="mmtable " ><tr>
					<td style="vertical-align:top"><span class="megamenuSection">Events</span><br />
						<ul>
							<li><g:link class="mmlink" controller="moveEvent" action="list" onclick="hideMegaMenu('bundleMegaMenu')" >List Events</g:link> </li>
							<li><g:link class="mmlink" controller="moveEvent" action="create" onclick="hideMegaMenu('bundleMegaMenu')">Create Event</g:link></li>
							<tds:hasPermission permission="ShowMovePrep">
							<li style="white-space:nowrap;"><g:link class="mmlink" controller="reports" action="preMoveCheckList" onclick="hideMegaMenu('bundleMegaMenu')">Pre-move Checklist</g:link></li>
							</tds:hasPermission>
							<li>&nbsp;</li>
							<li>&nbsp;</li>
							<tds:hasPermission permission='HelpMenuView'>
							<li><a class="mmlink" href="javascript:window.open('https://ops.tdsops.com/twiki/bin/view/Main/DataCenterMoves/TranManHelp?cover=print','help');" onclick="hideMegaMenu('bundleMegaMenu')">help</a></li>
							</tds:hasPermission>
						</ul>
					</td>
					<td style="vertical-align:top"><span class="megamenuSection">Bundles</span><br />
						<ul>
							<li><g:link class="mmlink" controller="moveBundle" action="list" onclick="hideMegaMenu('bundleMegaMenu')" >List Bundles</g:link> </li>
							<li><g:link class="mmlink" controller="moveBundle" action="create"  params="[projectId:currProjObj?.id]" onclick="hideMegaMenu('bundleMegaMenu')">Create Bundle</g:link></li>
							<tds:hasPermission permission='MoveBundleShowView'>
							  <li style="white-space:nowrap;"><g:link class="home mmlink" controller="moveBundle" action="planningStats"   onclick="hideMegaMenu('bundleMegaMenu')">Planning Dashboard</g:link></li>
							</tds:hasPermission>
							<tds:hasPermission permission='MoveBundleEditView'>
							  <li><g:link class="mmlink" controller="moveBundle" action="planningConsole"   onclick="hideMegaMenu('bundleMegaMenu')">Planning Console</g:link></li>
							</tds:hasPermission>
							<tds:hasPermission permission='HelpMenuView'>
							<li><a class="mmlink" href="javascript:window.open('https://ops.tdsops.com/twiki/bin/view/Main/DataCenterMoves/TranManHelp?cover=print','help');" onclick="hideMegaMenu('bundleMegaMenu')">help</a></li>
							</tds:hasPermission>
						</ul>
					</td>
					<g:if test="${currProjObj}">
					<td style="vertical-align:top"><span class="megamenuSection">For <strong>${moveBundleName}</strong>:</span><br />
						<ul>
							<li><g:link class="mmlink" controller="moveBundle" action="show"  onclick="hideMegaMenu('bundleMegaMenu')">Bundle Settings</g:link></li>
							</br><li><g:link class="mmlink" controller="moveBundleAsset" action="assignAssetsToBundle" params="[bundleId:moveBundleId]" onclick="hideMegaMenu('bundleMegaMenu')">Bundled Assets</g:link> </li>
							<li><g:link class="mmlink" controller="projectTeam" action="list" params="[bundleId:moveBundleId]" onclick="hideMegaMenu('bundleMegaMenu')">List Teams</g:link></li>
							<li style="white-space:nowrap;" ><g:link class="mmlink" controller="moveBundleAsset" action="bundleTeamAssignment" params="[bundleId:moveBundleId, rack:'UnrackPlan']" onclick="hideMegaMenu('bundleMegaMenu')">Assign Assets to Teams</g:link> </li>
							<tds:hasPermission permission='HelpMenuView'>
							<li><a class="mmlink" href="javascript:window.open('https://ops.tdsops.com/twiki/bin/view/Main/DataCenterMoves/TranManHelp?cover=print','help');" onclick="hideMegaMenu('bundleMegaMenu')">help</a></li>
							</tds:hasPermission>
						</ul>
					</td>
					</g:if>
					</tr></table>
				</div>
			</li>
			</tds:hasPermission>
			<tds:hasPermission permission='BundleMenuView'>
			<li id="bundleMenuId" style="position:relative; float:left;"><g:link class="home" onmouseover="showMegaMenu('#bundleMegaMenu')" onmouseout="mclosetime()" controller="moveBundle" action="show" params="[projectId:currProjObj?.id]">Bundles
				<a id="bundleAnchor" class="ui-icon ui-icon-triangle-1-s" onmouseover="showMegaMenu('#bundleMegaMenu')" href="javascript:showMegaMenu('#bundleMegaMenu')" style="display: inline"></a></g:link>
			</li>

			<li id="teamMenuId" style="position:relative; float:left;"><g:link class="home" onmouseover="showMegaMenu('')" onmouseout="mclosetime()" controller="clientTeams"  onclick="hideMegaMenu('bundleMegaMenu')">Teams</g:link></li>
            </tds:hasPermission>
	        <tds:hasPermission permission='ConsoleMenuView'>
			<li id="consoleMenuId" style="position:relative; float:left;"><g:link class="home" onmouseover="showMegaMenu('#consoleMegaMenu')" onmouseout="mclosetime()" controller="assetEntity" action="dashboardView" params="['showAll':'show']">Console
				<a id="consoleAnchor" class="ui-icon ui-icon-triangle-1-s" onmouseover="showMegaMenu('#consoleMegaMenu')" href="javascript:showMegaMenu('#consoleMegaMenu')" style="display: inline"></a></g:link>
			    <div class="megamenu rooms" id="consoleMegaMenu" onmouseover="showMegaMenu('#consoleMegaMenu')" onmouseout="mclosetime()" style="display: none;">
					<table class="mmtable room_rack"><tr>
					<td style="vertical-align:top"  ><span class="megamenuSection">Supervisor Console</span><br />
						<ul>
							<tds:hasPermission permission='ShowMoveTechsAndAdmins'>
							<li><g:link class="mmlink" controller="assetEntity" action="dashboardView" params="[ 'showAll':'show','teamType':'MOVE']" onclick="hideMegaMenu('consoleMegaMenu')">Supervise Move Techs</g:link></li>
							<li><g:link class="mmlink" controller="assetEntity" action="dashboardView" params="['showAll':'show','teamType':'ADMIN']" onclick="hideMegaMenu('consoleMegaMenu')">Supervise Admins</g:link></li>
							<li>&nbsp;</li>
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
							<li>&nbsp;</li>
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
							<li>&nbsp;</li>
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
			   <li id="dashboardMenuId" style="position:relative; float:left;"><g:link class="home" onmouseover="showMegaMenu('')" controller="dashboard" >Dashboard</g:link></li>
			</tds:hasPermission>
			<tds:hasPermission permission='AssetTrackerMenuView'>
			   <li id="assetTrackerMenuId" style="position:relative; float:left;"><g:link class="home" onmouseover="showMegaMenu('')" controller="clientConsole" >Asset Tracker</g:link></li>
			</tds:hasPermission>
	        <tds:hasPermission permission='ReportMenuView'>
			<li id="reportsMenuId" style="position:relative; float: left;"><g:link class="home" onmouseover="showMegaMenu('#reportsMegaMenu')" onmouseout="mclosetime()" controller="reports" params="[projectId:currProjObj?.id]">Reports
	        	<a id="reportAnchor" class="ui-icon ui-icon-triangle-1-s" onmouseover="showMegaMenu('#reportsMegaMenu')" href="javascript:('#reportsMegaMenu')" style="display: inline"></a></g:link>
				<div  class="megamenu reports" id="reportsMegaMenu" onmouseover="showMegaMenu('#reportsMegaMenu')" onmouseout="mclosetime()" style="display: none;">
					<table class="mmtable "><tr>
					<tds:hasPermission permission='ShowDiscovery'>
					<td style="vertical-align:top"><span class="megamenuSection">Discovery</span><br />
						<ul>
							<li><a href="/tdstm/reports/getBundleListForReportDialog?reportId=Rack+Layout" class="home mmlink" onclick="hideMegaMenu('reportsMegaMenu')">Racks (old)</a> </li>
							<li><a href="/tdstm/reports/getBundleListForReportDialog?reportId=CablingConflict" class="home mmlink" onclick="hideMegaMenu('reportsMegaMenu')">Cabling Conflict</a> </li>
							<li><a href="/tdstm/reports/getBundleListForReportDialog?reportId=CablingData" class="home mmlink" onclick="hideMegaMenu('reportsMegaMenu')">Cabling Data</a> </li>
							<li><a href="/tdstm/reports/powerReport" class="home mmlink" onclick="hideMegaMenu('reportsMegaMenu')">Power</a> </li>
							<li>&nbsp;</li>
							<tds:hasPermission permission='HelpMenuView'>
							<li><a class="mmlink" href="javascript:window.open('https://ops.tdsops.com/twiki/bin/view/Main/DataCenterMoves/TranManHelp?cover=print','help');" onclick="hideMegaMenu('reportsMegaMenu')">help</a></li>
							</tds:hasPermission>
						</ul>
					</td>
					</tds:hasPermission>
					<tds:hasPermission permission='ShowMovePrep'>
					<td style="vertical-align:top"><span class="megamenuSection">Move Prep</span><br />
						<ul >
						    <tds:hasPermission permission="ShowMovePrep">
						    <li><a href="/tdstm/reports/preMoveCheckList" class="home mmlink" onclick="hideMegaMenu('reportsMegaMenu')">Pre-move Checklist</a> </li>
						    </tds:hasPermission>
							<li><a href="/tdstm/reports/getBundleListForReportDialog?reportId=Login+Badges" class="home mmlink" onclick="hideMegaMenu('reportsMegaMenu')">Login Badges</a> </li>
							<li><a href="/tdstm/reports/getBundleListForReportDialog?reportId=Asset+Tag" class="home mmlink" onclick="hideMegaMenu('reportsMegaMenu')">Asset Tags</a> </li>
							<li><a href="/tdstm/reports/getBundleListForReportDialog?reportId=Team+Worksheets" class="home mmlink" onclick="hideMegaMenu('reportsMegaMenu')">Move Team Worksheets</a> </li>
							<li><a href="/tdstm/reports/getBundleListForReportDialog?reportId=cart+Asset" class="home mmlink" onclick="hideMegaMenu('reportsMegaMenu')">Logistics Team Worksheets</a></li>
							<li><a href="/tdstm/reports/getBundleListForReportDialog?reportId=Transportation+Asset+List" class="home mmlink" onclick="hideMegaMenu('reportsMegaMenu')">Transport Worksheets</a></li>
							<tds:hasPermission permission='HelpMenuView'>
							<li><a class="mmlink" href="javascript:window.open('https://ops.tdsops.com/twiki/bin/view/Main/DataCenterMoves/TranManHelp?cover=print','help');" onclick="hideMegaMenu('reportsMegaMenu')">help</a></li>
							</tds:hasPermission>
						</ul>
					</td>
					</tds:hasPermission>
					<tds:hasPermission permission='ShowMoveDay'>
					<td style="vertical-align:top"><span class="megamenuSection">Move Day</span><br />
						<ul>
							<li><a href="/tdstm/reports/getBundleListForReportDialog?reportId=Issue+Report" class="home mmlink" onclick="hideMegaMenu('reportsMegaMenu')">Issue Report</a> </li>
							<li><a href="/tdstm/reports/getBundleListForReportDialog?reportId=MoveResults" class="home mmlink" onclick="hideMegaMenu('reportsMegaMenu')">Move Results</a> </li>
							<li><a href="/tdstm/reports/getBundleListForReportDialog?reportId=CablingQA" class="home mmlink" onclick="hideMegaMenu('reportsMegaMenu')">Cabling QA</a> </li>
							<li>&nbsp;</li>
							<li>&nbsp;</li>
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
		<div class="megamenu" id="userMegaMenu" onmouseover="showMegaMenu('#userMegaMenu')" onmouseout="mclosetime()" style="display: none; width:370px">
			<table class="mmtable"><tr>
			<td style="vertical-align:top"><span class="megamenuSection">${session.getAttribute("LOGIN_PERSON").name }</span><br />
				<ul>
					<li><g:remoteLink controller="person" action="getPersonDetails" id="${session.getAttribute('LOGIN_PERSON').id}" onComplete="updatePersonDetails(e)">Account settings...</g:remoteLink></li>
					<li><a href="#" style="cursor: pointer;" id="resetPreferenceId" name="${session.getAttribute('LOGIN_PERSON').id}" onclick="resetPreference(this.name)">Reset preferences</a><a href="#" id="newpreferenceId" style="display:none;" >Preferences were reset</a></li>
					<li>&nbsp;</li>
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
					<tds:hasPermission permission='PersonExpiryDate'>
						<script type="text/javascript">
						$(document).ready(function(){
				        	$("#expiryDateId").datetimepicker();
				        });
					    </script>
        	            <input type="text" maxlength="64" id="expiryDateId" name="expiryDate"/>
                    </tds:hasPermission>
                    <tds:hasPermission permission='PersonExpiryDate'>
						<input type="text" maxlength="64" id="expiryDateId" name="expiryDate" readonly="readonly" style="background: none;border: 0"/>
                    </tds:hasPermission>
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
			resetmenu2();
	  		if(e!=""){
		  		$(e).show();
				mcancelclosetime();	// cancel close timer
				megamenuitem = e;
				if(e == "#adminMegaMenu"){
					$("#adminMenuId a").css('background-color','lightblue');
					$("#adminMenuId a").css('color','#0366b0');
					$("#adminAnchor").css("display","inline")
				}
				if(e == "#projectMegaMenu"){
					$("#projectMenuId a").css('background-color','lightblue');
					$("#projectMenuId a").css('color','#0366b0');
					$("#projectAnchor").css("display","inline")
				}
				if(e == "#racksMegaMenu"){
					$("#roomMenuId a").css('background-color','lightblue');
					$("#roomMenuId a").css('color','#0366b0');
					$("#rackMenuId a").css('background-color','lightblue');
					$("#rackMenuId a").css('color','#0366b0');
					$("#rackAnchor").css("display","inline")
				}
				if(e == "#assetMegaMenu"){
					$("#assetMenuId a").css('background-color','lightblue');
					$("#assetMenuId a").css('color','#0366b0');
					$("#assetAnchor").css("display","inline")
					
				}
				if(e == "#bundleMegaMenu"){
					$("#eventMenuId a").css('background-color','lightblue');
					$("#eventMenuId a").css('color','#0366b0');
					$("#bundleMenuId a").css('background-color','lightblue');
					$("#bundleMenuId a").css('color','#0366b0');
					$("#bundleAnchor").css("display","inline")
				}
				if(e == "#consoleMegaMenu"){
					$("#consoleMenuId a").css('background-color','lightblue');
					$("#consoleMenuId a").css('color','#0366b0');
					$("#consoleAnchor").css("display","inline")
				}
				if(e == "#reportsMegaMenu"){
					$("#reportsMenuId a").css('background-color','lightblue');
					$("#reportsMenuId a").css('color','#0366b0');
					$("#reportAnchor").css("display","inline")
				}
				if(e == "#userMegaMenu"){
					$("#userMenuId").css('background-color','lightblue');
				}
	  		}
	  	}
	  	function hideMegaMenu( id ){
	  		$("#"+id).hide()
	  	}
	  	function closeMegaMenu() {
			if(megamenuitem) $(megamenuitem).hide();
			resetmenu2();
		}
	  	function resetmenu2 () {
			$("#adminMenuId a").css('background-color','#0366b0');
			$("#adminMenuId a").css('color','#9ACAEE');
			$("#projectMenuId a").css('background-color','#0366b0');
			$("#projectMenuId a").css('color','#9ACAEE');
			$("#roomMenuId a").css('background-color','#0366b0');
			$("#roomMenuId a").css('color','#9ACAEE');
			$("#rackMenuId a").css('background-color','#0366b0');
			$("#rackMenuId a").css('color','#9ACAEE');
			$("#assetMenuId a").css('background-color','#0366b0');
			$("#assetMenuId a").css('color','#9ACAEE');
			$("#eventMenuId a").css('background-color','#0366b0');
			$("#eventMenuId a").css('color','#9ACAEE');
			$("#bundleMenuId a").css('background-color','#0366b0');
			$("#bundleMenuId a").css('color','#9ACAEE');
			$("#consoleMenuId a").css('background-color','#0366b0');
			$("#consoleMenuId a").css('color','#9ACAEE');
			$("#reportsMenuId a").css('background-color','#0366b0');
			$("#reportsMenuId a").css('color','#9ACAEE');
			$("#userMenuId").css('background-color','');
			$("#adminAnchor").css('color','#9ACAEE')
			$("#projectAnchor").css('color','#9ACAEE')
			$("#rackAnchor").css('color','#9ACAEE')
			$("#assetAnchor").css('color','#9ACAEE')
			$("#bundleAnchor").css('color','#9ACAEE')
			$("#consoleAnchor").css('color','#9ACAEE')
			$("#reportAnchor").css('color','#9ACAEE')
			if(currentMenuId == "#adminMenu"){$("#adminMenuId a").css('background-color','#003366')}
			if(currentMenuId == "#projectMenu"){$("#projectMenuId a").css('background-color','#003366')}
			if(currentMenuId == "#roomsMenu"){$("#roomMenuId a").css('background-color','#003366')}
			if(currentMenuId == "#racksMenu"){$("#rackMenuId a").css('background-color','#003366')}
			if(currentMenuId == "#assetMenu"){$("#assetMenuId a").css('background-color','#003366')}
			if(currentMenuId == "#eventMenu"){$("#eventMenuId a").css('background-color','#003366')}
			if(currentMenuId == "#bundleMenu"){$("#bundleMenuId a").css('background-color','#003366')}
			if(currentMenuId == "#consoleMenu"){$("#consoleMenuId a").css('background-color','#003366')}
			if(currentMenuId == "#reportsMenu"){$("#reportsMenuId a").css('background-color','#003366')}
			if(currentMenuId == "#userMenu"){$("#userMenuId").css('background-color','')}
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
		  	//temp disable of submenu...
		  	//	$(e).show();
	  		}
	  	}

		function setPower( p ){
			${remoteFunction(controller:'project', action:'setPower', params:'\'p=\' + p ',	onComplete:'updateTimeZone( e )')}
		}
		function resetPreference(user){
			${remoteFunction(controller:'person', action:'resetPreferences', params:'\'user=\' + user ',onComplete:'changeResetMessage()')}
	   }
	    function changeResetMessage(){
               $("#resetPreferenceId").fadeTo("fast", .5).removeAttr("onClick"); 
			   $("#newpreferenceId").css("display","inline")
		}
	  	
	  	//page load startup stuff
	  	
		showSubMenu(currentMenuId);
		
		//set up mega menus to align with menu2 items
		//admin and user mega menus are to the edges
		document.getElementById('#projectMegaMenu').style.left = document.getElementById('#projectMenuId').style.left;
		document.getElementById('#racksMegaMenu').style.left = document.getElementById('#roomMenuId').style.left;
		document.getElementById('#assetMegaMenu').style.left = document.getElementById('#assetMenuId').style.left;
		document.getElementById('#bundleMegaMenu').style.left = document.getElementById('#eventMenuId').style.left;
		document.getElementById('#consoleMegaMenu').style.left = document.getElementById('#consoleMenuId').style.left;
		document.getElementById('#reportsMegaMenu').style.left = document.getElementById('#reportsMenuId').style.left;

		var timeout = 500;
		var closetimer = 0;
		var megamenuitem = 0;
		document.onclick = closeMegaMenu;// close mega when click-out

	</script>
  </body>
</html>
