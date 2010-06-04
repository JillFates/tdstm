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
	
	<g:javascript library="prototype" />
    <jq:plugin name="jquery.combined" />
    
     <g:layoutHead />
     <script type="text/javascript">
   		$(document).ready(function() {
      		$("#personDialog").dialog({ autoOpen: false })
     	})
   	</script>
	<link type="text/css" rel="stylesheet" href="${createLinkTo(dir:'css',file:'dropDown.css')}" />
    
    <% def currProj = session.getAttribute("CURR_PROJ");
    def setImage = session.getAttribute("setImage");
    def moveEvent = session.getAttribute("MOVE_EVENT");
    def moveBundle = session.getAttribute("MOVEBUNDLE");
    def projectId = currProj.CURR_PROJ ;
    def currProjObj;
    if( projectId != null){
      currProjObj = Project.findById(projectId)
    }
    
    
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
      <div class="title">&nbsp;Transition Manager <g:if test="${currProjObj}"> - ${currProjObj.name} : : ${moveBundle.name} </g:if></div>
        <div class="header_right"><br />
          <div style="font-weight: bold; color: #0000FF">
          <jsec:isLoggedIn>
			<strong>
			<div style="float: left;">
			<g:remoteLink controller="person" action="getPersonDetails" id="${session.getAttribute('LOGIN_PERSON').id}" style="color: #0000FF;" onComplete="updatePersonDetails(e)">
				Welcome,&nbsp;<span id="loginUserId">${session.getAttribute("LOGIN_PERSON").name } </span>
			</g:remoteLink>
			</div>
			<div class="tzmenu">&nbsp;-&nbsp;using <span id="tzId">${session.getAttribute("CURR_TZ")?.CURR_TZ ? session.getAttribute("CURR_TZ")?.CURR_TZ : 'EDT' }</span>
				time<ul>
			    	      <li><a href="javascript:setUserTimeZone('GMT')">GMT </a></li>
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
	              	&nbsp;| 
	              </strong>
              &nbsp;<g:link controller="auth" action="signOut" style="color: #328714">sign out</g:link>
          </jsec:isLoggedIn>
          </div>
        </div>
      </div>

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
            <jsec:hasRole name="ADMIN">
                <li><g:link class="home" controller="auth" action="home">Admin</g:link> </li>
            </jsec:hasRole>
        	<li><g:link class="home" controller="projectUtil">Project </g:link> </li>
        	<jsec:lacksAllRoles in="['MANAGER','OBSERVER']"> 
        		<li><g:link class="home" controller="person" action="projectStaff" params="[projectId:currProjObj?.id]" >Staff</g:link></li>
                        <li><g:link class="home" controller="assetEntity" action="list" >Assets</g:link> </li>
        		<li><a href="#" onclick="$('#assetMenu').show();$('#reportsMenu').hide();$('#bundleMenu').hide();">Assets</a></li> 
                        <li><g:link class="home" controller="moveEvent" action="list" >Events</g:link> </li>
        		<li><g:link class="home" controller="moveBundle" params="[projectId:currProjObj?.id]">Bundles</g:link></li>
        	</jsec:lacksAllRoles>
        	<jsec:hasAnyRole in="['ADMIN']">
        		<li><g:link class="home" controller="newsEditor" params="[projectId:currProjObj?.id]">News Editor</g:link></li>
        	</jsec:hasAnyRole>
       		<jsec:hasAnyRole in="['ADMIN','SUPERVISOR','MANAGER']">
        		<li><g:link class="home" controller="assetEntity" action="dashboardView" params="[projectId:currProjObj?.id, 'showAll':'show']">Console</g:link></li>
        	</jsec:hasAnyRole>
        	<jsec:hasAnyRole in="['ADMIN','SUPERVISOR','PROJECT_ADMIN']">
        		<li><g:link class="home" controller="cartTracking" action="cartTracking" params="[projectId:currProjObj?.id]">Carts</g:link></li>
        		<li><g:link class="home" controller="dashboard" params="[projectId:currProjObj?.id]">Dashboard</g:link> </li>
        	</jsec:hasAnyRole>
        	<jsec:hasAnyRole in="['ADMIN','MANAGER','OBSERVER','SUPERVISOR']">
        		<li><g:link class="home" controller="clientConsole" params="[projectId:currProjObj?.id]">PMO Asset Tracking</g:link> </li>
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
			<li><g:link class="home" controller="walkThrough" >Walkthrough</g:link> </li>
		</ul>
	</div>
	<div class="menu2" id="reportsMenu" style="background-color:#003366;display: none;">
		<ul>
			<li><g:link class="home" controller="moveBundleAsset" action="getBundleListForReportDialog" params="[reportId:'Login Badges']">Login Badges</g:link> </li>
			<li><g:link class="home" controller="moveBundleAsset" action="getBundleListForReportDialog" params="[reportId:'Asset Tag']">Asset Tags</g:link> </li>     	      
    		<li><g:link class="home" controller="moveBundleAsset" action="getBundleListForReportDialog" params="[reportId:'Team Worksheets']">Move Team Worksheets</g:link> </li>
			<li><g:link class="home" controller="moveBundleAsset" action="getBundleListForReportDialog" params="[reportId:'cart Asset']">Cleaning Team Worksheets</g:link></li>
    		<li><g:link class="home" controller="moveBundleAsset" action="getBundleListForReportDialog" params="[reportId:'Transportation Asset List']">Transport Worksheets</g:link></li>
    		<li><g:link class="home" controller="moveBundleAsset" action="getBundleListForReportDialog" params="[reportId:'Issue Report']">Issue Report</g:link></li>
    		<li><g:link class="home" controller="moveBundleAsset" action="getBundleListForReportDialog" params="[reportId:'Rack Layout']">Rack Elevations</g:link></li>
    		<li><g:link class="home" controller="moveBundleAsset" action="getBundleListForReportDialog" params="[reportId:'MoveResults']">Move Results</g:link></li>
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
                    <label for="title">Time Zone:</label>
                  </td>
                  <td valign="top" class="value">
                    <g:select name="timeZone" id="timeZoneId" from="${['GMT','PST','PDT','MST','MDT','CST','CDT','EST','EDT']}" 
                    value="${session.getAttribute('CURR_TZ')?.CURR_TZ}"/>
                  </td>
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
		function updatePersonDetails( e ){
			var person = eval("(" + e.responseText + ")");
			$("#personId").val(person.id)
			$("#firstNameId").val(person.firstName);
			$("#lastNameId").val(person.lastName);
			$("#nickNameId").val(person.nickName);
			$("#titleId").val(person.title);
			$("#personDialog").dialog('option', 'width', 500)
		    $("#personDialog").dialog("open")
	  	}
		function changePersonDetails(){
			${remoteFunction(controller:'person', action:'updatePerson', 
					params:'\'id=\' + $(\'#personId\').val() +\'&firstName=\'+$(\'#firstNameId\').val() +\'&lastName=\'+$(\'#lastNameId\').val()+\'&nickName=\'+$(\'#nickNameId\').val()+\'&title=\'+$(\'#titleId\').val()+\'&password=\'+$(\'#passwordId\').val()+\'&timeZone=\'+$(\'#timeZoneId\').val()', 
					onComplete:'updateWelcome(e)')}
		}
	  	function updateWelcome( e ){
		  	var ret = eval("(" + e.responseText + ")");
		  	$("#loginUserId").html(ret[0].name)
		  	$("#tzId").html(ret[0].tz)
		  	$("#personDialog").dialog('close')
	  	}
	  	function setUserTimeZone( tz ){
	  		${remoteFunction(controller:'project', action:'setUserTimeZone', 
					params:'\'tz=\' + tz ',	onComplete:'updateTimeZone(e)')}
	  	}
	  	function updateTimeZone( e ){
		  	var sURL = unescape(window.location);
		  	window.location.href = sURL;
	  	}
	</script>
  </body>
</html>
