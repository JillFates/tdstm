<%@page import="net.transitionmanager.security.Permission"%>
<%@page import="com.tdsops.tm.enums.domain.StartPageEnum"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
  <head>
    <meta http-equiv="Content-Type" content="text/html; charset=iso-8859-1" />
    <title><g:layoutTitle default="Grails" /></title>
   	<asset:stylesheet href="css/tds-main.css" />
	<asset:stylesheet href="css/tds.css" />
    <asset:stylesheet href="css/dropDown.css" />
    <asset:link rel="shortcut icon" type="image/x-icon" href="images/favicon.ico" />

	<asset:stylesheet href="css/ui.core.css" />
    <asset:stylesheet href="css/ui.dialog.css" />
    <asset:stylesheet href="css/ui.theme.css" />
    <asset:stylesheet href="css/resources/ui.datetimepicker.css" />
    <g:javascript src="prototype/prototype.js" />
    <g:layoutHead />

    <script type="text/javascript">
   		$(document).ready(function() {
      		$("#personDialog").dialog({ autoOpen: false })
     	})
     	var dateRegExpForExp  = /^(0[1-9]|1[012])[/](0[1-9]|[12][0-9]|3[01])[/](19|20)\d\d ([0-1][0-9]|[2][0-3])(:([0-5][0-9])){1,2} ([APap][Mm])$/;
		function clearStorage() {
            stateManagement.destroyState();
		}
   </script>
   </head>
<%
def setImage = tds.setImage()
def moveEvent = tds.currentMoveEvent()
def moveBundle = tds.currentMoveBundle()
def currProject = tds.currentProject()
def person = tds.currentPerson()
boolean isIE6 = tds.isIE6()
%>

  <body>
    <div class="main_body">

      <div class="tds_header">
      	<div class="header_left">
      		<g:if test="${setImage}">
    	  		<img src="${createLink(controller:'project', action:'showImage', id:setImage)}" style="height: 30px;"/>
    	  	</g:if>
	      	<g:else>
     			<a href="http://www.transitionaldata.com/service/transitionmanager" target="new"><asset:image src="images/TMMenuLogo.png" height="30" style="float: left;border: 0px"/></a>
    		</g:else>
    	</div>
      	<div class="title">&nbsp;TransitionManager&trade;
      	<g:if test="${currProject}"> - ${currProject.name} </g:if>
      	<g:if test="${moveEvent}"> : ${moveEvent.name}</g:if>
      	<g:if test="${moveBundle}"> : ${moveBundle.name}</g:if>
      </div>
        <div class="header_right"><br />
          <div style="font-weight: bold;">
          <sec:ifLoggedIn>
          	<g:if test="${isIE6}">
				<span><asset:image src="images/skin/warning.png" style="width: 14px;height: 14px;float: left;padding-right: 3px;"/></span>
			</g:if>
              	<g:remoteLink controller="person" action="retrievePersonDetails" id="${tds.currentPersonId()}" onComplete="updatePersonDetails(XMLHttpRequest)">
			<strong>

			<div style="float: left;">
	              		Welcome,&nbsp;<span id="loginUserId">${person?.firstName} </span>
	              	</div>
	              	&nbsp;|
	              </strong>
              </g:remoteLink>
              &nbsp;<g:link controller="auth" action="signOut" onclick="clearStorage()">sign out</g:link>
          </sec:ifLoggedIn>
          </div>
        </div>
      </div>

      <%--<div class="top_menu_layout">
         <div class="menu1">
          <ul>
            <li><g:link class="home" controller="projectUtil">Project Manager</g:link></li>
            <sec:ifAllGranted roles='ROLE_ADMIN'>
              <li><g:link class="home" controller="auth" action="home">Administration </g:link> </li>
            </sec:ifAllGranted>
          </ul>
        </div>
        </div>--%>
        <div class="menu2">
      	<ul>
           <tds:hasPermission permission="${Permission.AdminMenuView}">
		<li><g:link class="home" controller="auth" action="home">Admin</g:link> </li>
            </tds:hasPermission>
		<li><g:link class="home" controller="projectUtil">Project </g:link> </li>
      </ul>
    </div>
<%--
<div class="menu1">
<ul>
    <li><g:link class="home" controller="projectUtil">Main</g:link></li>
    <li><g:link class="home" controller="projectUtil"
        action="searchList">Search</g:link></li>
    <sec:ifAllGranted roles='ROLE_CLIENT_ADMIN'>
        <li><g:link class="home" controller="project" action="create">Add</g:link>
        </li>
    </sec:ifAllGranted>
    <li><a href="#">Import/Export</a></li>
</ul>
</div>
--%>
      <div class="main_bottom"><g:layoutBody /></div>
    </div>
    <div id="personDialog" title="Edit Person" style="display:none;" class="old-legacy-content">
      <div class="dialog">
          <div class="dialog">
            <table>
              <tbody>
                <tr>
                    <td colspan="2"><div class="required"> Fields marked ( * ) are mandatory </div> </td>
                </tr>

                <tr class="prop">
                    <td valign="top" class="name">
                        <label for="firstNameId"><b>First Name:&nbsp;<span style="color: red">*</span></b></label>
                    </td>
                    <td valign="top" class="value">
                        <input type="text" maxlength="64" id="firstNameId" name="firstName"/>
                    </td>
                </tr>

                <tr class="prop">
                  <td valign="top" class="name">
                    <label for="lastNameId">Last Name:</label>
                  </td>
                  <td valign="top" class="value">
                    <input type="text" maxlength="64" id="lastNameId" name="lastName"/>
                  </td>
                </tr>

                <tr class="prop">
                  <td valign="top" class="name">
                    <label for="lastNameId">Nick Name:</label>
                  </td>
                  <td valign="top" class="value">
                    <input type="text" maxlength="64" id="nickNameId" name="nickName"/>
                  </td>
                </tr>
                <tr class="prop">
                  <td valign="top" class="name">
                    <label for="titleId">Title:</label>
                  </td>
                  <td valign="top" class="value">
                    <input type="text" maxlength="34" id="titleId" name="title"/>
                  </td>
                </tr>
                <tr class="prop">
                  <td valign="top" class="name">
                    <label for="emailId">Email:</label>
                  </td>
                  <td valign="top" class="value">
                    <input type="text" maxlength="64" id="emailId" name="email"/>
                  </td>
                </tr>

                <tds:hasPermission permission="${Permission.PersonExpiryDate}">
                    <tr class="prop">
                        <td valign="top" class="name">
                            <label for="expiryDateId"><b>Expiry Date:<span style="color: red">*</span></b></label>
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
                            <label for="startPage">Start Page:</label>
                        </td>
                        <td valign="top" class="value">
	                     <tds:hasPermission permission="${Permission.AdminMenuView}">
                            <g:select name="startPage" from="${[StartPageEnum.PROJECT_SETTINGS.value,StartPageEnum.PLANNING_DASHBOARD.value, StartPageEnum.ADMIN_PORTAL.value]}"
                            value="${tds.startPage()}"/>
                        </tds:hasPermission>
	                     <tds:lacksPermission permission="${Permission.AdminMenuView}">
		                     <g:select name="startPage" from="${[StartPageEnum.PROJECT_SETTINGS.value, StartPageEnum.PLANNING_DASHBOARD.value]}" value="${tds.startPage()}"/>
	                     </tds:lacksPermission>
                        </td>
                    </tr>
                <tr class="prop">
                    <td valign="top" class="name">
                       <label for="powerTypeId">Power In:</label>
                    </td>
                    <td valign="top" class="value">
                        <g:select name="powerType" id="powerTypeId" from="${['Watts','Amps']}" value="${tds.powerType()}"/>
                    </td>
                </tr>
                <tr class="prop">
                    <td valign="top" class="name">
                        <label for="oldPasswordId">Old Password:&nbsp;</label>
                    </td>
                    <td valign="top" class="value">
                        <input type="hidden" id="personId" name="personId" value=""/>
                        <input type="password" maxlength="25" name="oldPassword" id="oldPasswordId" value=""/>
                    </td>
                </tr>

                <tr class="prop">
                    <td valign="top" class="name">
                        <label for="newPasswordId">New Password:&nbsp;</label>
                    </td>
                    <td valign="top" class="value">
                        <input type="password" maxlength="25" name="newPassword" id="newPasswordId" value=""/>
                    </td>
                </tr>

                <tr class="prop">
                    <td valign="top" class="name">
                        <label for="newPasswordConfirmId">New Password (confirm):&nbsp;</label>
                    </td>
                    <td valign="top" class="value">
                        <input type="password" maxlength="25" name="newPasswordConfirm" id="newPasswordConfirmId" value=""/>
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
    <g:javascript src="tdsmenu.js" />
  </body>
</html>
