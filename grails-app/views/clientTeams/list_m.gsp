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
                                <g:link class="home" controller="clientTeams" params="[projectId:projectId, viewMode:'web']" class="sign_out" style="width:75px;">Use full site</g:link>
                                <g:link controller="auth" action="signOut" class="sign_out">Log out</g:link>
                                </td>
                        </tr>
                </tbody>
        </table>
        <span style="color:#328714; font: bold 13px arial; float:left;">Select Team To Use:</span>
        <table>
        	<thead>
				<tr>
					<th>Team</th>
					<th>Role</th>
					<th>Members</th>
              	</tr>
			</thead>
            <tbody>
            	<g:each in="${projectTeams}" status="i" var="projectTeamInstance">
					<tr class="${(i % 2) == 0 ? 'odd' : 'even'}" onclick="submitLoginForm('${projectTeamInstance?.projectTeam?.moveBundle.id}','${projectTeamInstance?.projectTeam?.id}','source')">
                        <td><b>${projectTeamInstance?.projectTeam?.name} src</b></td>
                        <td><g:if test="${projectTeamInstance?.projectTeam?.role}"><g:message code="ProjectTeam.role.${projectTeamInstance?.projectTeam?.role}" /></g:if></td>
						<td>
							<g:each in="${projectTeamInstance?.teamMembers}" var="teamMember">
			 					<g:if test="${teamMember.company[0]}">${teamMember.company[0]}:</g:if><g:if test="${teamMember?.staff?.lastName}">${teamMember?.staff?.lastName},</g:if> ${teamMember?.staff?.firstName}  <br/>
							</g:each>
						</td>
            		</tr>
				</g:each>
				<g:each in="${projectTeams}" status="i" var="projectTeamInstance">
					<tr class="${(i % 2) == 0 ? 'odd' : 'even'}" onclick="submitLoginForm('${projectTeamInstance?.projectTeam?.moveBundle.id}','${projectTeamInstance?.projectTeam?.id}','target')">
                        <td><b>${projectTeamInstance?.projectTeam?.name} trg</b></td>
                        <td><g:if test="${projectTeamInstance?.projectTeam?.role}"><g:message code="ProjectTeam.role.${projectTeamInstance?.projectTeam?.role}" /></g:if></td>
						<td>
							<g:each in="${projectTeamInstance?.teamMembers}" var="teamMember">
			 					<g:if test="${teamMember.company[0]}">${teamMember.company[0]}:</g:if><g:if test="${teamMember?.staff?.lastName}">${teamMember?.staff?.lastName},</g:if> ${teamMember?.staff?.firstName}  <br/>
							</g:each>
						</td>
            		</tr>
				</g:each>
				<g:if test="${projectTeams.size() == 0}">
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
function submitLoginForm( bundleId,teamId,location){
	var form = document.forms["loginForm"]
	form.bundleId.value = bundleId
	form.teamId.value = teamId
	form.location.value = location
	form.submit();
}
</script>
</body>
</html>
