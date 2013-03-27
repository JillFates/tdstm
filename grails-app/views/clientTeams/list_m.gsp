<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
	<title>Teams List</title>
	<link type="text/css" rel="stylesheet" href="${resource(dir:'css',file:'main.css')}" />
	<link type="text/css" rel="stylesheet" href="${resource(dir:'css',file:'qvga.css')}" />
	<link type="text/css" rel="stylesheet" href="${resource(dir:'css',file:'tds.css')}" />
	<link rel="shortcut icon" href="${resource(dir:'images',file:'tds.ico')}" type="image/x-icon" />
	<g:javascript library="application" />
	<meta name="viewport" content="height=device-height,width=320" />
</head>
<body>
	<div id="spinner" class="spinner" style="display: none;"><img src="${resource(dir:'images',file:'spinner.gif')}" alt="Spinner" /></div>
	<div class="mainbody">
		<div id="mobtitle">TransitionManager&trade; - Mobile</div>
	<div class="menu4">
		<ul>
			<li><a href="#" class="mobmenu mobselect">Teams</a></li>
			<li><a href="#" class="mobmenu">Home</a></li>
			<li><a href="#" class="mobmenu">Tasks</a></li>
			<li><a href="#" class="mobmenu">Asset</a></li>
		</ul>
	</div>
	<span style="font: bold 13px arial; float:left; margin-top: 10px;">Select <button type="button" class="${cssTodo}" onclick="window.location.href='/tdstm/clientTeams/listComment?viewMode=mobile'">Your Task(${todo})</button> or a Team to use:</span>
	<div class="mobbody">
	<g:form action="home" name="loginForm">
        <input type="hidden" name="bundleId" id="bundleId"/>
        <input type="hidden" name="teamId" id="teamId"/>
        <input type="hidden" name="location" id="locationId"/>
        <input type="hidden" name="projectId" value="${projectId}"/>
        <input type="hidden" name="username" id="usernameId" value="${projectId}"/>
	<g:if test="${flash.message}">
		<div style="width: 200px;" class="message">${flash.message}</div>
	</g:if>
	</div>

 		<div style="float: left; width: 100%; margin: 4px 0; text-align: center;">
        <table style="border: 0px;">
                <tbody><tr><td style="height: 2px;" nowrap="nowrap">
                  <g:link controller="auth" action="signOut" class="mobbutton">Log out</g:link>
                </td></tr></tbody>
        </table>
	<span style="font: bold 13px arial; float:left;">Select Team to use:</span>
	<table>
		<thead><tr>
			<th class="sort_column">Team (loc)</th>
			<th class="sort_column">Role</th>
			<th class="sort_column">Members</th>
			<th class="sort_column">Bundle</th>
		</tr></thead>
		<tbody>
            	<g:each in="${sourceTeams}" status="i" var="projectTeamInstance">
			<tr class="teamstatus_${projectTeamInstance?.cssClass}" onclick="submitLoginForm('${projectTeamInstance?.team?.projectTeam?.moveBundle.id}','${projectTeamInstance?.team?.projectTeam?.id}','${projectTeamInstance?.team?.projectTeam?.role}','source')">
				<td><b>${projectTeamInstance?.team?.projectTeam?.name} (S)</b></td>
				<td><g:if test="${projectTeamInstance?.team?.projectTeam?.role}"><g:message code="ProjectTeam.role.${projectTeamInstance?.team?.projectTeam?.role}" /></g:if></td>
				<td>
				<g:each in="${projectTeamInstance?.team?.teamMembers}" var="teamMember">
					<g:if test="${teamMember.company[0]}">${teamMember.company[0]}:</g:if><g:if test="${teamMember?.staff?.lastName}">${teamMember?.staff?.lastName}</g:if><br/>
				</g:each>
				</td>
				<td><b>${projectTeamInstance?.moveBundle}</b></td>
            		</tr>
		</g:each>
		<g:each in="${targetTeams}" status="i" var="projectTeamInstance">
			<tr class="teamstatus_${projectTeamInstance?.cssClass}" onclick="submitLoginForm('${projectTeamInstance?.team?.projectTeam?.moveBundle.id}','${projectTeamInstance?.team?.projectTeam?.id}','${projectTeamInstance?.team?.projectTeam?.role}','target')">
				<td><b>${projectTeamInstance?.team?.projectTeam?.name} (T)</b></td>
                        	<td><g:if test="${projectTeamInstance?.team?.projectTeam?.role}"><g:message code="ProjectTeam.role.${projectTeamInstance?.team?.projectTeam?.role}" /></g:if></td>
				<td>
				<g:each in="${projectTeamInstance?.team?.teamMembers}" var="teamMember">
			 		<g:if test="${teamMember.company[0]}">${teamMember.company[0]}:</g:if><g:if test="${teamMember?.staff?.lastName}">${teamMember?.staff?.lastName}</g:if> 
				</g:each>
				</td>
				<td><b>${projectTeamInstance?.moveBundle}</b></td>
            		</tr>
		</g:each>
			<g:if test="${sourceTeams?.size() == 0 && targetTeams?.size() == 0}">
			<tr><td colspan="3" class="no_records">There are no active teams for you.</td></tr>
			</g:if>
		</tbody>
		</table>
	</div>
        </div>
        </g:form>
		<g:link class="mobfooter" controller="clientTeams" style="color:white;" params="[viewMode:'web']">Use Full Site</g:link>

<script type="text/javascript">
   function submitLoginForm( bundleId,teamId,role,location){
	var form = document.forms["loginForm"] ;
	form.bundleId.value = bundleId;
	form.teamId.value = teamId;
	form.location.value = location;
	if(role == "CLEANER"){
		form.action = "logisticsHome";
	}
	form.submit();
}
</script>
</body>
</html>
