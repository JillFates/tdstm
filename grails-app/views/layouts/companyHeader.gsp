<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
  <head>
    <meta http-equiv="Content-Type" content="text/html; charset=iso-8859-1" />
    <title><g:layoutTitle default="Grails" /></title>
    <link rel="stylesheet" href="${createLinkTo(dir:'css',file:'main.css')}" type="text/css"/>
    <link rel="stylesheet" href="${createLinkTo(dir:'css',file:'tds.css')}" type="text/css"/>
    <link rel="shortcut icon" href="${createLinkTo(dir:'images',file:'tds.ico')}" type="image/x-icon" />
    
    <g:javascript library="prototype" />
     <g:javascript library="jquery" />
     
    <link type="text/css" rel="stylesheet" href="${createLinkTo(dir:'css',file:'ui.core.css')}" />
    <link type="text/css" rel="stylesheet" href="${createLinkTo(dir:'css',file:'ui.dialog.css')}" />
    <link type="text/css" rel="stylesheet" href="${createLinkTo(dir:'css',file:'ui.theme.css')}" />
	
    <jq:plugin name="ui.core"/>
    <jq:plugin name="ui.draggable"/>
    <jq:plugin name="ui.dialog"/>
    
    <g:layoutHead />
    <script type="text/javascript">
   		$(document).ready(function() {
      		$("#personDialog").dialog({ autoOpen: false })
     	})
   </script>
    <% def currProj = session.getAttribute("CURR_PROJ");
       def setImage = session.getAttribute("setImage");
    def projectId = currProj.CURR_PROJ ;
    def currProjObj;
    if( projectId != null){
      currProjObj = Project.findById(projectId)
    }
    def partyGroup = request.getSession(false).getAttribute("PARTYGROUP") 
    
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
              <strong>Welcome &nbsp;&nbsp;
              	<g:remoteLink controller="person" action="getPersonDetails" id="${session.getAttribute('LOGIN_PERSON').id}" style="color: #0000FF;" onComplete="updatePersonDetails(e)">
              		<span id="loginUserId">${session.getAttribute("LOGIN_PERSON").name }</span>
              	</g:remoteLink>&nbsp;! </strong>
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
			<li><g:link class="home" controller="partyGroup" action="show" id="${partyGroup?.id}">Company</g:link></li>
			<li><g:link class="home" controller="person" id="${partyGroup?.id}">Staff</g:link></li>
			<li><g:link class="home" controller="application" id="${partyGroup?.id}">Applications </g:link></li>
			<li><a href="#">Locations </a></li>
			<li><a href="#">Rooms </a></li>
		</ul>
	</div>
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
						params:'\'id=\' + $(\'#personId\').val() +\'&firstName=\'+$(\'#firstNameId\').val() +\'&lastName=\'+$(\'#lastNameId\').val()+\'&nickName=\'+$(\'#nickNameId\').val()+\'&title=\'+$(\'#titleId\').val()+\'&password=\'+$(\'#passwordId\').val()', 
						onComplete:'updateWelcome(e)')}
		}
	  	function updateWelcome( e ){
		  	$("#loginUserId").html(e.responseText)
		  	$("#personDialog").dialog('close')
	  	}
	</script>
  </body>
</html>
