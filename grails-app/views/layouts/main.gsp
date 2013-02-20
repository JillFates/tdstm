<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
  <head>
    <meta http-equiv="Content-Type" content="text/html; charset=iso-8859-1" />
    <title><g:layoutTitle default="Grails" /></title>
    <link rel="stylesheet" href="${resource(dir:'css',file:'main.css')}" type="text/css"/>
    <link rel="stylesheet" href="${resource(dir:'css',file:'tds.css')}" type="text/css"/>
    <link type="text/css" rel="stylesheet" href="${resource(dir:'css',file:'dropDown.css')}" />  
    <link rel="shortcut icon" href="${resource(dir:'images',file:'tds.ico')}" type="image/x-icon" />
	
	<link type="text/css" rel="stylesheet" href="${resource(dir:'css',file:'ui.core.css')}" />
    <link type="text/css" rel="stylesheet" href="${resource(dir:'css',file:'ui.dialog.css')}" />
    <link type="text/css" rel="stylesheet" href="${resource(dir:'css',file:'ui.theme.css')}" />
    <link rel="stylesheet" href="${resource(dir:'css',file:'ui.datetimepicker.css')}" type="text/css"/>
    <g:javascript library="prototype" />
    <jq:plugin name="jquery.combined" />
    <g:layoutHead />
	
    <script type="text/javascript">
   		$(document).ready(function() {
      		$("#personDialog").dialog({ autoOpen: false })
     	})
     	     	var emailRegExp = /^([0-9a-zA-Z]+([_.-]?[0-9a-zA-Z]+)*@[0-9a-zA-Z]+[0-9,a-z,A-Z,.,-]+\.[a-zA-Z]{2,4})+$/
     	var dateRegExpForExp  = /^(0[1-9]|1[012])[/](0[1-9]|[12][0-9]|3[01])[/](19|20)\d\d ([0-1][0-9]|[2][0-3])(:([0-5][0-9])){1,2} ([APap][Mm])$/;
   </script>
   </head>
    <% def currProj = session.getAttribute("CURR_PROJ");
    def setImage = session.getAttribute("setImage");
    def projectId = currProj.CURR_PROJ ;
    def moveEventId = session.getAttribute("MOVE_EVENT")?.MOVE_EVENT ;
    def moveBundleId = session.getAttribute("CURR_BUNDLE")?.CURR_BUNDLE ;
    def currProjObj;
	def personId = session.getAttribute("LOGIN_PERSON").id
	def person = Person.get(personId)
    if( projectId != null){
      currProjObj = Project.findById(projectId)
    }
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
     			<a href="http://www.transitionaldata.com/" target="new"><img src="${resource(dir:'images',file:'tds.jpg')}" style="float: left;border: 0px"/></a>      	    	 
    		</g:else>
    	</div>
      	<div class="title">&nbsp;TransitionManager&trade; 
      	<g:if test="${currProjObj}"> - ${currProjObj.name} </g:if>
      	<g:if test="${moveEventId}"> : ${MoveEvent.findById( moveEventId )?.name}</g:if>
      	<g:if test="${moveBundleId}"> : ${MoveBundle.findById( moveBundleId )?.name}</g:if>
      </div>
        <div class="header_right"><br />
          <div style="font-weight: bold;">
          <shiro:isLoggedIn>
          	<g:if test="${isIE6}">
				<span><img title="Note: MS IE6 has limited capability so functions have been reduced." src="${resource(dir:'images/skin',file:'warning.png')}" style="width: 14px;height: 14px;float: left;padding-right: 3px;"/></span>
			</g:if>
              	<g:remoteLink controller="person" action="getPersonDetails" id="${session.getAttribute('LOGIN_PERSON').id}" onComplete="updatePersonDetails(e)">
			<strong>
		
			<div style="float: left;">
	              		Welcome,&nbsp;<span id="loginUserId">${session.getAttribute("LOGIN_PERSON").name }(${person?.modelScore}) </span>
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
              </g:remoteLink>
              &nbsp;<g:link controller="auth" action="signOut">sign out</g:link>
          </shiro:isLoggedIn>
          </div>
        </div>
      </div>

      <%--<div class="top_menu_layout">
         <div class="menu1">
          <ul>
            <li><g:link class="home" controller="projectUtil">Project Manager</g:link></li>
            <shiro:hasRole name="ADMIN">
              <li><g:link class="home" controller="auth" action="home">Administration </g:link> </li>
            </shiro:hasRole>
          </ul>
        </div> 
        </div>--%>
        <div class="menu2">
      	<ul>
           <tds:hasPermission permission='AdminMenuViews '>
		<li><g:link class="home" controller="auth" action="home">Admin</g:link> </li>
            </tds:hasPermission>
		<li><g:link class="home" controller="projectUtil">Project </g:link> </li>
      </ul>
    </div>
      <!--
<div class="menu1">
<ul>
    <li><g:link class="home" controller="projectUtil">Main</g:link></li>
    <li><g:link class="home" controller="projectUtil"
        action="searchList">Search</g:link></li>
    <shiro:hasRole name="CLIENT_ADMIN">
        <li><g:link class="home" controller="project" action="create">Add</g:link>
        </li>
    </shiro:hasRole>
    <li><a href="#">Import/Export</a></li>
</ul>
</div>
      -->
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
                <tr class="prop">
                    <td valign="top" class="name">
                        <label for="password">Old Password:&nbsp;</label>
                    </td>
                    <td valign="top" class="value">
                        <input type="hidden" id="personId" name="personId" value=""/>
                        <input type="password" maxlength="25" name="oldPassword" id="oldPasswordId" value=""/>
                    </td>
                </tr>

                <tr class="prop">
                    <td valign="top" class="name">
                        <label for="password">New Password:&nbsp;</label>
                    </td>
                    <td valign="top" class="value">
                        <input type="password" maxlength="25" name="newPassword" id="newPasswordId" value=""/>
                    </td>
                </tr>

                <tr class="prop">
                    <td valign="top" class="name">
                        <label for="password">New Password (confirm):&nbsp;</label>
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
