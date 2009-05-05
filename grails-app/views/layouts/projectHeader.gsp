<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
  <head>
    <meta http-equiv="Content-Type" content="text/html; charset=iso-8859-1" />
    <title><g:layoutTitle default="Grails" /></title>
    <link rel="stylesheet" href="${createLinkTo(dir:'css',file:'main.css')}" />
    <link rel="stylesheet" href="${createLinkTo(dir:'css',file:'tds.css')}" />
    <link rel="shortcut icon"
          href="${createLinkTo(dir:'images',file:'tds.ico')}" type="image/x-icon" />
         
    <g:layoutHead />
   
    <link type="text/css" rel="stylesheet" href="${createLinkTo(dir:'css',file:'dropDown.css')}" />    
   
  </head>
	<% def currProj = session.getAttribute("CURR_PROJ");
    def projectId = currProj.CURR_PROJ ;
    def currProjObj;
    if( projectId != null){
      currProjObj = Project.findById(projectId)
    }
    %>
  <body>
   
    <div class="main_body">

      <div class="header"><img
          src="${createLinkTo(dir:'images',file:'tds.jpg')}" style="float: left;">
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
            <li><g:link class="home" controller="projectUtil">Project Manager</g:link></li>
            <jsec:hasRole name="ADMIN">
              <li><g:link class="home" controller="auth" action="home">Administration </g:link> </li>
            </jsec:hasRole>
            <li><g:link class="home" controller="moveTech" action="moveTechLogin">Move Tech</g:link></li>
            <li><g:link class="home" controller="moveTech" action="moveTechLogin">Cleaning</g:link></li>
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
      <div class="menu2">
      <ul>
        <li><g:link class="home" controller="projectUtil">Project </g:link> </li>
        <li><g:link class="home" controller="person" action="projectStaff" params="[projectId:currProjObj?.id]" >Staff</g:link></li>
        <li>  
          	<div id="menubar" style="border-right:1px solid #ffffff; width:65px;">
 			<div id="menu1" class="menu_new">Assets<ul>    	      
    	      <li><g:link class="home" controller="assetEntity" params="[projectId:currProjObj?.id]">List Assets</g:link></li>
    	      <li><g:link class="home" controller="assetEntity" action="assetImport" params="[projectId:currProjObj?.id]">Import/Export</g:link> </li>
              </ul>
            </div>
            </div>
                	
        </li>  
		
        <li><g:link class="home" controller="moveBundle" params="[projectId:currProjObj?.id]">Move Bundles</g:link></li>
        <li><g:link class="home" controller="assetEntity" action="dashboardView" params="[projectId:currProjObj?.id]">Dashboard</g:link></li>
         <li><g:link class="home" controller="clientConsole" params="[projectId:currProjObj?.id]">Client Console</g:link> </li>
        <li>  
          	<div id="menubar">
 			<div id="menu1" class="menu_new">Reports<ul>    	      
    	      <li><g:link class="home" controller="moveBundleAsset" action="getBundleListForReportDialog" params="[reportId:'Team Worksheets']">Team Worksheets</g:link> </li>
    	      <li><g:link class="home" controller="moveBundleAsset" action="getBundleListForReportDialog" params="[reportId:'Rack Layout']">Rack Layout</g:link></li>
    	       <li><g:link class="home" controller="moveBundleAsset" action="getBundleListForReportDialog" params="[reportId:'cart Asset']">Cart Asset List</g:link></li>
    	       <li><g:link class="home" controller="moveBundleAsset" action="getBundleListForReportDialog" params="[reportId:'Issue Report']">Issue Report</g:link></li>
    	       <li><g:link class="home" controller="moveBundleAsset" action="getBundleListForReportDialog" params="[reportId:'Transportation Asset List']">Transportation Asset List</g:link></li>
              </ul>
            </div>
            </div>
                	
        </li>
        
      </ul>
    </div>
      
      <div class="main_bottom"><g:layoutBody /></div>
    </div>
  </body>
</html>
