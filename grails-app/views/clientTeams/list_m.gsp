<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<head>

<title>Teams List</title>
<link type="text/css" rel="stylesheet" href="${createLinkTo(dir:'css',file:'main.css')}" />
<link type="text/css" rel="stylesheet" href="${createLinkTo(dir:'css',file:'qvga.css')}" />
<link type="text/css" rel="stylesheet" href="${createLinkTo(dir:'css',file:'tds.css')}" />
<link rel="shortcut icon" href="${createLinkTo(dir:'images',file:'tds.ico')}" type="image/x-icon" />
<g:javascript library="application" />
<meta name="viewport" content="height=device-height,width=220" />
</head>
<body>
<div id="spinner" class="spinner" style="display: none;">
<img src="${createLinkTo(dir:'images',file:'spinner.gif')}" alt="Spinner" />
</div>
<div class="mainbody" style="width: auto;">
<div class="colum_techlogin_login" style="float:left;">
<div class="w_techlog_login" style="height: auto">
	<g:form action="home" name="loginForm">
        <input type="hidden" name="bundleId" id="bundleId"/>
        <input type="hidden" name="teamId" id="teamId"/>
        <input type="hidden" name="location" id="locationId"/>
        <input type="hidden" name="projectId" value="${projectId}"/>
        <input type="hidden" name="username" id="usernameId" value="${projectId}"/>
        <g:if test="${flash.message}">
        <div style="width: 200px;" class="message">${flash.message}</div>
        </g:if>
        <div style="float: left; padding-left: 2px; width: 98%; margin-top: 2px;">
		 <td style="text-align: center;">
        <span style="color: #328714; font: bold 15px arial;">Transition Manager - Mobile</span>
         </td>
		</div>

 		<div style="float: left; width: 100%; margin: 4px 0; text-align: center;">
        <table style="border: 0px;">
                <tbody>
                        <tr>
                                <td style="height: 2px;" nowrap="nowrap">
                                <g:link class="home" controller="clientTeams" params="[projectId:projectId, viewMode:'web']" class="mobbutton" style="width:75px;">Use full site</g:link>
                                <g:link controller="auth" action="signOut" class="mobbutton">Log out</g:link>
                                </td>
                        </tr>
                </tbody>
        </table>
        <span style="color:#328714; font: bold 13px arial; float:left;">Select Team To Use:</span>
        <table>
        	<thead>
				<tr>
					<th>Team (loc)</th>
					<th>Role</th>
					<th>Members</th>
              	</tr>
			</thead>
            <tbody>
            	<g:each in="${sourceTeams}" status="i" var="projectTeamInstance">
					<tr class="teamstatus_${projectTeamInstance?.cssClass}" onclick="submitLoginForm('${projectTeamInstance?.team?.projectTeam?.moveBundle.id}','${projectTeamInstance?.team?.projectTeam?.id}','${projectTeamInstance?.team?.projectTeam?.role}','source')">
                        <td><b>${projectTeamInstance?.team?.projectTeam?.name} (S)</b></td>
                        <td><g:if test="${projectTeamInstance?.team?.projectTeam?.role}"><g:message code="ProjectTeam.role.${projectTeamInstance?.team?.projectTeam?.role}" /></g:if></td>
						<td>
							<g:each in="${projectTeamInstance?.team?.teamMembers}" var="teamMember">
			 					<g:if test="${teamMember.company[0]}">${teamMember.company[0]}:</g:if><g:if test="${teamMember?.staff?.lastName}">${teamMember?.staff?.lastName},</g:if> ${teamMember?.staff?.firstName}  <br/>
							</g:each>
						</td>
            		</tr>
				</g:each>
				<g:each in="${targetTeams}" status="i" var="projectTeamInstance">
					<tr class="teamstatus_${projectTeamInstance?.cssClass}" onclick="submitLoginForm('${projectTeamInstance?.team?.projectTeam?.moveBundle.id}','${projectTeamInstance?.team?.projectTeam?.id}','${projectTeamInstance?.team?.projectTeam?.role}','target')">
                        <td><b>${projectTeamInstance?.team?.projectTeam?.name} (T)</b></td>
                        <td><g:if test="${projectTeamInstance?.team?.projectTeam?.role}"><g:message code="ProjectTeam.role.${projectTeamInstance?.team?.projectTeam?.role}" /></g:if></td>
						<td>
							<g:each in="${projectTeamInstance?.team?.teamMembers}" var="teamMember">
			 					<g:if test="${teamMember.company[0]}">${teamMember.company[0]}:</g:if><g:if test="${teamMember?.staff?.lastName}">${teamMember?.staff?.lastName},</g:if> ${teamMember?.staff?.firstName}  <br/>
							</g:each>
						</td>
            		</tr>
				</g:each>
				<g:if test="${sourceTeams?.size() == 0 && targetTeams?.size()== 0}">
				<tr><td colspan="3" class="no_records">There are no active teams for you.</td></tr>
				</g:if>
			</tbody>
        </table>
        </div>
</g:form></div>
<div class="left_bcornerlog"></div>
<div class="right_bcornerlog"></div>
</div>
</div>
<div class="logo"></div>
<script type="text/javascript">
function submitLoginForm( bundleId,teamId,role,location){
	var form = document.forms["loginForm"]
	form.bundleId.value = bundleId
	form.teamId.value = teamId
	form.location.value = location
	if(role == "CLEANER"){
		form.username.value = "ct-"+ bundleId+"-"+teamId+"-s"
		form.action = "../moveTech/signIn"
	}
	form.submit();
}
</script>
</body>
</html>
