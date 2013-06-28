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
    <link rel="stylesheet" href="${resource(dir:'css',file:'ui.datetimepicker.css')}" type="text/css"/>

    <g:javascript library="prototype" />
    <jq:plugin name="jquery.combined" />
    <g:javascript src="crawler.js" />
    <g:layoutHead />
   
    <link type="text/css" rel="stylesheet" href="${resource(dir:'css',file:'dropDown.css')}" />    

   <script type="text/javascript">
   		$(document).ready(function() {
      		$("#personDialog").dialog({ autoOpen: false })
      		$("#userPrefDivId").dialog({ autoOpen: false })
      		var currentURL = window.location.pathname
      		${remoteFunction(controller:'userLogin', action:'updateLastPageLoad', params:'\'url=\' + currentURL ')}
     	})
     	var emailRegExp = /^([0-9a-zA-Z]+([_.-]?[0-9a-zA-Z]+)*@[0-9a-zA-Z]+[0-9,a-z,A-Z,.,-]+\.[a-zA-Z]{2,4})+$/
     	var dateRegExpForExp  = /^(0[1-9]|1[012])[/](0[1-9]|[12][0-9]|3[01])[/](19|20)\d\d ([0-1][0-9]|[2][0-3])(:([0-5][0-9])){1,2} ([APap][Mm])$/;
     	var currentMenuId = "";
     	var B1 = []
     	var B2 = []
     	var taskManagerTimePref = "60"
		var contextPath = "${request.contextPath}"
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
			<g:if test="${isIE6}">
				<span><img title="Note: MS IE6 has limited capability so functions have been reduced." src="${resource(dir:'images/skin',file:'warning.png')}" style="width: 14px;height: 14px;float: left;padding-right: 3px;"/></span>
			</g:if>
			<g:remoteLink controller="person" action="getPersonDetails" id="${session.getAttribute('LOGIN_PERSON').id}" onmouseover="waitForMenu('#userMegaMenu')" onmouseout="waitForMenu('', '${isMDev}')" onComplete="updatePersonDetails(e)" style="float:left;display:inline">
			&nbsp;<span id="loginUserId">${session.getAttribute("LOGIN_PERSON").name }
				<a id="userAnchor" class="ui-icon ui-icon-triangle-1-s" onmouseover="waitForMenu('#userMegaMenu')" href="javascript:showMegaMenu('#userMegaMenu')" style="float:left;display:inline"></a> </span>
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
          </shiro:isLoggedIn>
          </div>
        </div>
      </div>

      <g:if test="${currProj}">
	      <div class="menu2">
	      <ul>
			<tds:hasPermission permission='AdminMenuView'>
			<li id="adminMenuId"><g:link class="home menuhideright" onmouseover="waitForMenu('#adminMegaMenu', '${isMDev}')" onmouseout="waitForMenu('', '${isMDev}')" controller="auth" action="home">Admin
				<a id="adminAnchor" class="ui-icon ui-icon-triangle-1-s" onmouseover="waitForMenu('#adminMegaMenu')" onmouseout="waitForMenu('', '${isMDev}')" href="javascript:showMegaMenu('#adminMegaMenu')" style="display: inline"></a></g:link>
    		    <div class="megamenu admin" id="adminMegaMenu" onmouseover="showMegaMenu('#adminMegaMenu')" onmouseout="waitForMenu('', '${isMDev}')" style="display: none;">
					<table class="mmtable room_rack"><tr>
					<td style="vertical-align:top" nowrap="nowrap"><span class="megamenuSection">Administration</span><br />
						<ul >
							<li><g:link class="mmlink" controller="auth" action="home" onclick="hideMegaMenu('adminMegaMenu')">Admin Portal</g:link> </li>
							<tds:hasPermission permission='HelpMenuView'>
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

			<li id="projectMenuId" style="position:relative; float: left;" ><g:link class="home" onmouseover="waitForMenu('#projectMegaMenu','${isMDev}')" onmouseout="waitForMenu('', '${isMDev}')" controller="projectUtil">Client/Project
				<a id="projectAnchor" class="ui-icon ui-icon-triangle-1-s" onmouseover="waitForMenu('#projectMegaMenu')" onmouseout="waitForMenu('', '${isMDev}')" href="javascript:showMegaMenu('#projectMegaMenu')" style="display: inline"></a></g:link>
				<div class="megamenu client" id="projectMegaMenu" onmouseover="showMegaMenu('#projectMegaMenu')" onmouseout="waitForMenu('', '${isMDev}')" style="display: none;">
					<table class="mmtable"><tr>
					<g:if test="${currProjObj}">
					<td style="vertical-align:top"><span class="megamenuSection">For <strong><g:if test="${currProjObj.name.size()>25}">${currProjObj.name.substring(0,25)+'...'}</g:if><g:else>${currProjObj.name}</g:else></strong></span><br />
						<ul >
							<li><g:link class="mmlink" controller="project" action="list" params="[active:'active']" onclick="hideMegaMenu('projectMegaMenu')">List Projects</g:link></li>
							<li><g:link class="mmlink" controller="projectUtil" onclick="hideMegaMenu('projectMegaMenu')"><g:if test="${currProjObj.name.size()>20}">${currProjObj.name.substring(0,20)+'...'}</g:if><g:else>${currProjObj.name}</g:else> Settings</g:link></li>
							<li><g:link class="mmlink" controller="person" action="manageProjectStaff"  onclick="hideMegaMenu('projectMegaMenu')">Project Staff</g:link></li>
					</g:if>
					<g:else>
					<td style="vertical-align:top"><span class="megamenuSection">No Project Selected</strong></span><br />
						<ul>
							<li><g:link class="mmlink" controller="project" action="list" params="[active:'active']" onclick="hideMegaMenu('projectMegaMenu')">List Projects</g:link></li>
					</g:else>
							<li><g:link class="mmlink" controller="project" action="assetFields" onclick="hideMegaMenu('adminMegaMenu')">Field Importance</g:link> </li>
							<tds:hasPermission permission='HelpMenuView'>
							<li><a class="mmlink" href="javascript:window.open('https://ops.tdsops.com/twiki/bin/view/Main/DataCenterMoves/TMProjectStaff?cover=print','help');"  onclick="hideMegaMenu('projectMegaMenu')">help</a></li>
							</tds:hasPermission>
						</ul>
					</td>
					</tr></table>
				</div>
				
			</li>

			<li id="roomMenuId" style="position:relative; float: left;"><g:link class="home" onmouseover="waitForMenu('#racksMegaMenu', '${isMDev}')" onmouseout="waitForMenu('', '${isMDev}')" controller="room">Rooms</g:link>
				<div class="megamenu rooms" id="racksMegaMenu" onmouseover="showMegaMenu('#racksMegaMenu')" onmouseout="waitForMenu('', '${isMDev}')" style="display: none;">
					<table class="mmtable room_rack" ><tr>
					<td style="vertical-align:top"><span class="megamenuSection">Rooms</span><br />
						<ul >
							<li><g:link class="mmlink" params="[viewType:'list']" controller="room"  onclick="hideMegaMenu('racksMegaMenu')">List Rooms</g:link></li>
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
			<tds:hasPermission permission='RackMenuView'>
			<li id="rackMenuId" ><g:link class="home" onmouseover="waitForMenu('#racksMegaMenu', '${isMDev}')" onmouseout="waitForMenu('', '${isMDev}')" controller="rackLayouts" action="create">Racks
				<a id="rackAnchor" class="ui-icon ui-icon-triangle-1-s" onmouseover="waitForMenu('#racksMegaMenu')" onmouseout="waitForMenu('', '${isMDev}')" href="javascript:showMegaMenu('#racksMegaMenu')" style="float: left"></a></g:link>
				</li>
            </tds:hasPermission>
	        <tds:hasPermission permission='AssetMenuView'>
			<li id="assetMenuId" style="position:relative; float:left;"><g:link class="home" onmouseover="waitForMenu('#assetMegaMenu', '${isMDev}')" onmouseout="waitForMenu('', '${isMDev}')" controller="assetEntity" action="assetSummary" >Assets
				<a id="assetAnchor" class="ui-icon ui-icon-triangle-1-s" onmouseover="waitForMenu('#assetMegaMenu')" onmouseout="waitForMenu('', '${isMDev}')" href="javascript:showMegaMenu('#assetMegaMenu')" style="display: inline"></a></g:link>
				<div class="megamenu rooms" id="assetMegaMenu" onmouseover="showMegaMenu('#assetMegaMenu')" onmouseout="waitForMenu('', '${isMDev}')" style="display: none;">
					<table class="mmtable room_rack"><tr>
					<tds:hasPermission permission='EditAndDelete'>
					<td style="vertical-align:top"><span class="megamenuSection">Manage Data</span><br />
					
						<ul>
							<tds:hasPermission permission='Import'>
							<li><g:link class="mmlink" controller="assetEntity" action="assetImport"  onclick="hideMegaMenu('assetMegaMenu')">Import</g:link></li>
							</tds:hasPermission>
							<tds:hasPermission permission='EditAndDelete'>
							<li><g:link class="mmlink" controller="dataTransferBatch" action="index" onclick="hideMegaMenu('assetMegaMenu')">Manage Batches</g:link></li>
							</tds:hasPermission>
							<tds:hasPermission permission='Export'>
							<li><g:link class="mmlink" controller="assetEntity" action="exportAssets"  onclick="hideMegaMenu('assetMegaMenu')">Export</g:link></li>
							</tds:hasPermission>
							<tds:hasPermission permission='AssetMenuView'>
							<li><g:link class="mmlink" controller="assetEntity" action="listComment"  params="[tag_s_2_lastUpdated:'desc']" onclick="hideMegaMenu('assetMegaMenu')">Asset Comments</g:link></li>
							</tds:hasPermission>
							<tds:hasPermission permission='HelpMenuView'>
							<li><a class="mmlink" href="javascript:window.open('https://ops.tdsops.com/twiki/bin/view/Main/DataCenterMoves/TranManHelp?cover=print','help');" onclick="hideMegaMenu('assetMegaMenu')">help</a></li>
							</tds:hasPermission>
						</ul>
					</td>
					</tds:hasPermission>
					<td style="vertical-align:top"><span class="megamenuSection">Assets</span><br />
						<ul>
							<li><g:link class="mmlink" controller="assetEntity" action="assetSummary">Summary</g:link></li>	
							<tds:hasPermission permission='MoveBundleEditView'>
							  <li><g:link class="mmlink" controller="moveBundle" action="dependencyConsole" onclick="hideMegaMenu('assetMegaMenu')">Dependencies</g:link></li>
							</tds:hasPermission>
							<li><g:link class="mmlink" controller="application" action="list"  onclick="hideMegaMenu('assetMegaMenu')">List Apps</g:link></li>
							<li><g:link class="mmlink" controller="assetEntity"  onclick="hideMegaMenu('assetMegaMenu')">List Servers</g:link></li>
							<li><g:link class="mmlink" controller="database"  onclick="hideMegaMenu('assetMegaMenu')">List DBs</g:link></li>
							<li><g:link class="mmlink" controller="files"  onclick="hideMegaMenu('assetMegaMenu')">List Storage</g:link></li>
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
			<li id="eventMenuId" style="position:relative; float: left;"><g:link class="home" onmouseover="waitForMenu('#bundleMegaMenu', '${isMDev}')" onmouseout="waitForMenu('', '${isMDev}')" controller="moveEvent" action="show" id="${moveEventId}">Events </g:link>
			
				<div class="megamenu rooms" id="bundleMegaMenu" onmouseover="showMegaMenu('#bundleMegaMenu')" onmouseout="waitForMenu('', '${isMDev}')" style="display: none;">
					<table class="mmtable " ><tr>
					<td style="vertical-align:top"><span class="megamenuSection">Events</span><br />
						<ul>
							<li><g:link class="mmlink" controller="moveEvent" action="list" onclick="hideMegaMenu('bundleMegaMenu')" >List Events</g:link> </li>
							<li><g:link class="mmlink" controller="moveEvent" action="create" onclick="hideMegaMenu('bundleMegaMenu')">Create Event</g:link></li>
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
							<li><g:link class="mmlink" controller="moveBundle" action="list" onclick="waitForMenu('bundleMegaMenu')" >List Bundles</g:link> </li>
							<li><g:link class="mmlink" controller="moveBundle" action="create"  params="[projectId:currProjObj?.id]" onclick="hideMegaMenu('bundleMegaMenu')">Create Bundle</g:link></li>
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
							<li><g:link class="mmlink" controller="projectTeam" action="list" params="[bundleId:moveBundleId]" onclick="hideMegaMenu('bundleMegaMenu')">List Teams (old)</g:link></li>
							<li style="white-space:nowrap;" ><g:link class="mmlink" controller="moveBundleAsset" action="bundleTeamAssignment" params="[bundleId:moveBundleId, rack:'UnrackPlan']" onclick="hideMegaMenu('bundleMegaMenu')">Assign Assets... (old)</g:link> </li>
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
			<li id="bundleMenuId" style="position:relative; float:left;"><g:link class="home" onmouseover="waitForMenu('#bundleMegaMenu', '${isMDev}')" onmouseout="waitForMenu('', '${isMDev}')" controller="moveBundle" action="show" params="[projectId:currProjObj?.id]" id="${moveBundleId}">Bundles
				<a id="bundleAnchor" class="ui-icon ui-icon-triangle-1-s" onmouseover="waitForMenu('#bundleMegaMenu')" onmouseout="waitForMenu('', '${isMDev}')" href="javascript:showMegaMenu('#bundleMegaMenu')" style="display: inline"></a></g:link>
			</li>

			<li id="teamMenuId" style="position:relative; float:left;"><a class="home" onmouseover="waitForMenu('#teamMegaMenu','${isMDev}')" onmouseout="waitForMenu('', '${isMDev}')" href="/tdstm/clientTeams/listTasks" >Tasks</a>
				<a id="teamMenuAnchor" class="ui-icon ui-icon-triangle-1-s" onmouseover="waitForMenu('#teamMegaMenu')" onmouseout="waitForMenu('', '${isMDev}')" href="javascript:showMegaMenu('#teamMegaMenu')" style="display: inline"></a>
				<div class="megamenu rooms" id="teamMegaMenu" onmouseover="showMegaMenu('#teamMegaMenu')" onmouseout="waitForMenu('', '${isMDev}')" style="display: none;">
					<table class="mmtable"><tr>
					<td style="vertical-align:top"><span class="megamenuSection">Tasks</span><br />
						<ul>
							<li><a class="mmlink" id="MyTasksMenuId" href="/tdstm/clientTeams/listTasks" onclick="hideMegaMenu('teamMegaMenu')">My Tasks</a></li>
							<tds:hasPermission permission='ShowMoveTechsAndAdmins'>
							<li><g:link class="mmlink" controller="assetEntity" action="listTasks"  params="[initSession:true]" onclick="hideMegaMenu('assetMegaMenu')">Task Manager</g:link></li>
							<li><a class="mmlink" href="/tdstm/clientTeams/list" onclick="hideMegaMenu('teamMegaMenu')">Team Tasks (old)</a></li>
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
			<li id="consoleMenuId" style="position:relative; float:left;"><g:link class="home" onmouseover="waitForMenu('#consoleMegaMenu', '${isMDev}')" onmouseout="waitForMenu('', '${isMDev}')" controller="assetEntity" action="dashboardView" params="['showAll':'show']">Console
				<a id="consoleAnchor" class="ui-icon ui-icon-triangle-1-s" onmouseover="waitForMenu('#consoleMegaMenu')" onmouseout="waitForMenu('', '${isMDev}')" href="javascript:showMegaMenu('#consoleMegaMenu')" style="display: inline"></a></g:link>
			    <div class="megamenu rooms" id="consoleMegaMenu" onmouseover="showMegaMenu('#consoleMegaMenu')" onmouseout="waitForMenu('', '${isMDev}')" style="display: none;">
					<table class="mmtable room_rack"><tr>
					<td style="vertical-align:top"  ><span class="megamenuSection">Supervisor Console</span><br />
						<ul>
							<tds:hasPermission permission='ShowMoveTechsAndAdmins'>
							<li><g:link class="mmlink" controller="assetEntity" action="dashboardView" params="[ 'showAll':'show','teamType':'MOVE']" onclick="hideMegaMenu('consoleMegaMenu')">Supervise Techs (old)</g:link></li>
							<li><g:link class="mmlink" controller="assetEntity" action="dashboardView" params="['showAll':'show','teamType':'ADMIN']" onclick="hideMegaMenu('consoleMegaMenu')">Supervise Admins (old)</g:link></li>
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
			<li id="dashboardMenuId" style="position:relative; float:left;"><g:link class="home" onmouseover="waitForMenu('#dashboardMegaMenu', '${isMDev}')" onmouseout="waitForMenu('', '${isMDev}')" controller="moveBundle" action="planningStats"  >Dashboards</g:link>
				<a id="dashboardAnchor" class="ui-icon ui-icon-triangle-1-s" onmouseover="waitForMenu('#dashboardMegaMenu')" onmouseout="waitForMenu('', '${isMDev}')" href="javascript:showMegaMenu('#dashboardMegaMenu')" style="display: inline"></a>
				<div class="megamenu rooms" id="dashboardMegaMenu" onmouseover="showMegaMenu('#dashboardMegaMenu')" onmouseout="waitForMenu('', '${isMDev}')" style="display: none;">
					<table class="mmtable"><tr>
					<td style="vertical-align:top"><span class="megamenuSection">Live Dashboards</span><br />
						<ul>
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
			<li id="reportsMenuId" style="position:relative; float: left;"><g:link class="home" onmouseover="waitForMenu('#reportsMegaMenu', '${isMDev}')" onmouseout="waitForMenu('', '${isMDev}')" controller="reports" params="[projectId:currProjObj?.id]">Reports
	        	<a id="reportAnchor" class="ui-icon ui-icon-triangle-1-s" onmouseover="waitForMenu('#reportsMegaMenu')" onmouseout="waitForMenu('', '${isMDev}')" href="javascript:('#reportsMegaMenu')" style="display: inline"></a></g:link>
				<div  class="megamenu reports" id="reportsMegaMenu" onmouseover="showMegaMenu('#reportsMegaMenu')" onmouseout="waitForMenu('', '${isMDev}')" style="display: none;">
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
		<div class="megamenu" id="userMegaMenu" onmouseover="showMegaMenu('#userMegaMenu')" onmouseout="waitForMenu('', '${isMDev}')" style="display: none; width:255px">
			<table class="mmtable"><tr>
			<td style="vertical-align:top"><span class="megamenuSection">${session.getAttribute("LOGIN_PERSON").name }</span><br />
				<ul>
					<li><g:remoteLink controller="person" action="getPersonDetails" id="${session.getAttribute('LOGIN_PERSON').id}" onComplete="updatePersonDetails(e)">Account settings...</g:remoteLink></li>
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
				<g:if test="${false && moveEvent && moveEvent?.inProgress == 'true'}">
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
    <div id="userPrefDivId" style="display: none" title="${session.getAttribute("LOGIN_PERSON").name } Preferences">
    </div>
  </body>
</html>
