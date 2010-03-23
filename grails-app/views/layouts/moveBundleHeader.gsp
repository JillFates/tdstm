<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
  <head>
    <meta http-equiv="Content-Type" content="text/html; charset=iso-8859-1" />
    <meta http-equiv="X-UA-Compatible" content="IE=EmulateIE7" />
    <title><g:layoutTitle default="Grails" /></title>
    <link rel="stylesheet" href="${createLinkTo(dir:'css',file:'main.css')}" />
    <link rel="stylesheet" href="${createLinkTo(dir:'css',file:'tds.css')}" />
    <link rel="shortcut icon"
          href="${createLinkTo(dir:'images',file:'tds.ico')}" type="image/x-icon" />
            
    <g:layoutHead />
     <g:javascript library="jquery" />
	<link type="text/css" rel="stylesheet" href="${createLinkTo(dir:'css',file:'dropDown.css')}" />
    
    <% def currProj = session.getAttribute("CURR_PROJ");
    def setImage = session.getAttribute("setImage");
    def projectId = currProj.CURR_PROJ ;
    def currProjObj;
    if( projectId != null){
      currProjObj = Project.findById(projectId)
    }
    
    def moveBundle = request.getSession(false).getAttribute("MOVEBUNDLE")    
    
    %>    
   
  </head>

  <body>
    
    <div class="main_body">

      <div class="header">
      <g:if test="${setImage}">
      	<div class="header_left">
    	  <img src="${createLink(controller:'project', action:'showImage', id:setImage)} " style="height: 30px;"/>
        </div> 
      </g:if>
      <g:else>
     	 <a href="http://www.transitionaldata.com/" target="new"><img src="${createLinkTo(dir:'images',file:'tds.jpg')}" style="float: left;border: 0px"/></a>
     </g:else>
        <div class="header_right"><br />
          <div style="font-weight: bold; color: #0000FF"><jsec:isLoggedIn>
              <strong>Welcome &nbsp;&nbsp;<jsec:principal />&nbsp;! </strong>
              &nbsp;<g:link controller="auth" action="signOut"
                            style="color: #328714">sign out</g:link>
          </jsec:isLoggedIn></div>
        </div>
      </div>

      <div class="top_menu_layout">
        <div class="menu1">
          <ul>
          <jsec:lacksAllRoles in="['MANAGER','OBSERVER']"> 
            <li><g:link class="home" controller="projectUtil">Project Manager</g:link></li>
            <jsec:hasRole name="ADMIN">
              <li><g:link class="home" controller="auth" action="home">Administration </g:link> </li>
            </jsec:hasRole>
            <li><g:link class="home" controller="moveTech" action="moveTechLogin">Move Tech</g:link></li>
            <li><g:link class="home" controller="moveTech" action="moveTechLogin">Cleaning</g:link></li>
            </jsec:lacksAllRoles>
          </ul>
        </div>
      </div>
      <div class="title">&nbsp;Transition Manager <g:if test="${currProjObj}"> - ${currProjObj.name} ( ${currProjObj.projectCode} ) </g:if></div>
      <!--
<div class="menu1">
<ul>
    <li><g:link class="home" controller="projectUtil">Main</g:link></li>
    <li><g:link class="home" controller="projectUtil"
        action="searchList">Search</g:link></li>
    <jsec:hasRole name="PROJECT_ADMIN">
        <li><g:link class="home" controller="project" action="create">Add</g:link>
        </li>
    </jsec:hasRole>
    <li><a href="#">Import/Export</a></li>
</ul>
</div>
      -->
      <g:if test="${currProj}">
      <div class="menu2">
      	<ul>
        	<li><g:link class="home" controller="projectUtil">Project </g:link> </li>
        	<jsec:lacksAllRoles in="['MANAGER','OBSERVER']"> 
        		<li><g:link class="home" controller="person" action="projectStaff" params="[projectId:currProjObj?.id]" >Staff</g:link></li>
        		<li><a href="#" onclick="$('#assetMenu').show();$('#reportsMenu').hide();$('#bundleMenu').hide();">Assets</a></li> 
        		<li><g:link class="home" controller="moveBundle" params="[projectId:currProjObj?.id]">Move Bundles</g:link></li>
        	</jsec:lacksAllRoles>
        	<jsec:hasAnyRole in="['ADMIN']">
        		<li><g:link class="home" controller="newsEditor" params="[projectId:currProjObj?.id]">News Editor</g:link></li>
        	</jsec:hasAnyRole>
       		<jsec:hasAnyRole in="['ADMIN','SUPERVISOR','MANAGER']">
        		<li><g:link class="home" controller="assetEntity" action="dashboardView" params="[projectId:currProjObj?.id, 'showAll':'show']">Console</g:link></li>
        	</jsec:hasAnyRole>
        	<jsec:hasAnyRole in="['ADMIN','SUPERVISOR','PROJECT_ADMIN']">
        		<li><g:link class="home" controller="cartTracking" action="cartTracking" params="[projectId:currProjObj?.id]">Carts</g:link></li>
        	</jsec:hasAnyRole>
        	<li><g:link class="home" controller="dashboard" params="[projectId:currProjObj?.id]">Dashboard</g:link> </li>
        	<jsec:hasAnyRole in="['ADMIN','MANAGER','OBSERVER','SUPERVISOR']">
        		<li><g:link class="home" controller="clientConsole" params="[projectId:currProjObj?.id]">Asset Tracking</g:link> </li>
        	</jsec:hasAnyRole>
         	<jsec:lacksAllRoles in="['MANAGER','OBSERVER']">
         		<li><a href="#" onclick="$('#reportsMenu').show();$('#assetMenu').hide();$('#bundleMenu').hide();">Reports</a></li>
         	</jsec:lacksAllRoles>
		</ul>
    </div>
	<div class="menu2" id="bundleMenu" style="background-color:#003366;">
		<ul>
			<li class="title1">Move Bundle: ${moveBundle?.name}</li>
			<li><g:link class="home" controller="projectTeam" action="list" params="[bundleId:moveBundle?.id]" >Team </g:link> </li>
			<li><g:link controller="moveBundleAsset" action="assignAssetsToBundle" params="[bundleId:moveBundle?.id]" >Bundle Asset Assignment</g:link> </li>
			<li><g:link class="home" controller="moveBundleAsset" action="bundleTeamAssignment" params="[bundleId:moveBundle?.id, rack:'UnrackPlan']" >Bundle Team Assignment </g:link> </li>
			<jsec:hasAnyRole in="['ADMIN','SUPERVISOR','PROJECT_ADMIN']">
				<li><g:link class="home" controller="moveEvent" action="list" params="[bundleId:moveBundle?.id]" >Move Event</g:link> </li>
			</jsec:hasAnyRole>
			<li><g:link class="home" controller="walkThrough" >Walkthrough</g:link> </li>
		</ul>
	</div>
	<div class="menu2" id="reportsMenu" style="background-color:#003366;display: none;">
		<ul>
			<li><g:link class="home" controller="moveBundleAsset" action="getBundleListForReportDialog" params="[reportId:'Login Badges']">Login Badges</g:link> </li>    	      
    		<li><g:link class="home" controller="moveBundleAsset" action="getBundleListForReportDialog" params="[reportId:'Team Worksheets']">Move Team Worksheets</g:link> </li>
			<li><g:link class="home" controller="moveBundleAsset" action="getBundleListForReportDialog" params="[reportId:'cart Asset']">Cleaning Team Worksheets</g:link></li>
    		<li><g:link class="home" controller="moveBundleAsset" action="getBundleListForReportDialog" params="[reportId:'Transportation Asset List']">Transport Worksheets</g:link></li>
    		<li><g:link class="home" controller="moveBundleAsset" action="getBundleListForReportDialog" params="[reportId:'Issue Report']">Issue Report</g:link></li>
    		<li><g:link class="home" controller="moveBundleAsset" action="getBundleListForReportDialog" params="[reportId:'Rack Layout']">Rack Elevations</g:link></li>
		</ul>
	</div>
	<div class="menu2" id="assetMenu" style="background-color:#003366;display: none;">
		<ul>
			<li><g:link class="home" controller="assetEntity" params="[projectId:currProjObj?.id]">List Assets</g:link></li>
          	<li><g:link class="home" controller="assetEntity" action="assetImport" params="[projectId:currProjObj?.id]">Import/Export</g:link> </li>
		</ul>
	</div>
    </g:if>
    <div class="main_bottom"><g:layoutBody /></div>
    </div>
  </body>
</html>
