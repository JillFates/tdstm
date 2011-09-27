<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
  <head>
    <meta http-equiv="Content-Type" content="text/html; charset=iso-8859-1" />
    <meta http-equiv="X-UA-Compatible" content="IE=EmulateIE7" />
    <title><g:layoutTitle default="Grails" /></title>
    <link rel="stylesheet" href="${createLinkTo(dir:'css',file:'main.css')}" type="text/css"/>
    <link rel="stylesheet" href="${createLinkTo(dir:'css',file:'tds.css')}" type="text/css"/>
    <link type="text/css" rel="stylesheet" href="${createLinkTo(dir:'css',file:'dropDown.css')}" />  
    <link rel="shortcut icon" href="${createLinkTo(dir:'images',file:'tds.ico')}" type="image/x-icon" />
    <link type="text/css" rel="stylesheet" href="${createLinkTo(dir:'css',file:'ui.core.css')}" />
    <link type="text/css" rel="stylesheet" href="${createLinkTo(dir:'css',file:'ui.dialog.css')}" />
    <link type="text/css" rel="stylesheet" href="${createLinkTo(dir:'css',file:'ui.theme.css')}" />
    <link rel="stylesheet" href="${createLinkTo(dir:'css',file:'ui.datetimepicker.css')}" type="text/css"/>
    
    <g:javascript library="prototype" />
    <jq:plugin name="jquery.combined" />
    <g:layoutHead />
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

       $(document).ready(function() {
      		$("#personDialog").dialog({ autoOpen: false })
      		${remoteFunction(controller:'userLogin', action:'updateLastPageLoad')}
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
    if( projectId != null){
      currProjObj = Project.findById(projectId)
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
      	<g:if test="${moveEventId}"> : ${MoveEvent.findById( moveEventId )?.name}</g:if>
      	<g:if test="${moveBundleId}"> : ${MoveBundle.findById( moveBundleId )?.name}</g:if>
       </div>
        <div class="header_right"><br />
          <div style="font-weight: bold;">
          <jsec:isLoggedIn>
          	<g:if test="${isIE6}">
				<span><img title="Note: MS IE6 has limited capability so functions have been reduced." src="${createLinkTo(dir:'images/skin',file:'warning.png')}" style="width: 14px;height: 14px;float: left;padding-right: 3px;"/></span>
			</g:if>
            <g:remoteLink controller="person" action="getPersonDetails" id="${session.getAttribute('LOGIN_PERSON').id}" onComplete="updatePersonDetails(e)">
			<strong>
			<div style="float: left;">
				Welcome,&nbsp;<span id="loginUserId">${session.getAttribute("LOGIN_PERSON").name } </span>
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
	              	</strong>
              </g:remoteLink>
              &nbsp;<g:link controller="auth" action="signOut">sign out</g:link>
          </jsec:isLoggedIn>
          </div>
        </div>
      </div>

      <%-- <div class="top_menu_layout">
        <div class="menu1">
          <ul>
            <li><g:link class="home" controller="projectUtil">Project Manager</g:link></li>
            <jsec:hasRole name="ADMIN">
              <li><g:link class="home" controller="auth" action="home">Administration </g:link> </li>
            </jsec:hasRole>
          </ul>
        </div>
      </div> --%>
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
			<jsec:hasRole name="ADMIN">
			<li><g:link class="home" controller="auth" action="home">Admin</g:link> </li>
	            </jsec:hasRole>
			<li><g:link class="home" controller="projectUtil">Project </g:link> </li>
			<li><g:link class="home" controller="workflow" action="home">Workflows </g:link> </li>
			<li><g:link class="home" controller="partyGroup" id="${partyGroup}">Company</g:link></li>
			<li><g:link class="home" controller="person" id="${partyGroup}">Staff</g:link></li>
			<li><g:link class="home" controller="userLogin" id="${partyGroup}">Users</g:link></li>
			<li><g:link class="home" controller="manufacturer" id="${partyGroup}">Manufacturers</g:link></li>
			<li><g:link class="home" controller="model" id="${partyGroup}">Models</g:link></li>
			<li><g:link class="home" controller="application" id="${partyGroup}">Applications </g:link></li>
			<li><g:link class="home" controller="model" action="importExport">Sync</g:link></li>
			<!--<li><a href="#">Locations </a></li>
			<li><a href="#">Rooms </a></li> -->
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
	  	function setPower( p ){
			${remoteFunction(controller:'project', action:'setPower', params:'\'p=\' + p ',	onComplete:'updateTimeZone( e )')}
		}
	</script>
  </body>
</html>
