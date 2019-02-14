<%@page import="net.transitionmanager.domain.ProjectTeam" %>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
	<head>
		<meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
		<meta name="layout" content="projectHeader" />
		<title>Teams List</title>
		<asset:stylesheet href="css/tds.css" />
		<asset:stylesheet href="css/qvga.css" />
	</head>
	<body>
		<g:form action="home" name="loginForm">
			<input type="hidden" name="bundleId" id="bundleId"/>
			<input type="hidden" name="teamId" id="teamId"/>
			<input type="hidden" name="location" id="locationId"/>
			<input type="hidden" name="projectId" value="${projectId}"/>
			<div class="mainbody">
				<div class="menu4">
					<ul>
						<li><a href="#" class="mobmenu mobselect">Teams</a></li>
						<li><a href="#" class="mobmenu">Home</a></li>
						<li><a href="#" class="mobmenu">Tasks</a></li>
						<li><a href="#" class="mobmenu">Asset</a></li>
					</ul>
				</div>
				<g:if test="${flash.message}">
					<div class="message">${flash.message}</div>
				</g:if>
				<span style="font: bold 13px arial; float:left; margin-top: 10px;">Select <button type="button"   class="${cssTodo}" onclick="window.location.href='/tdstm/task/listUserTasks?viewMode=web'">Your Task(${todo})</button> or a Team to use:</span>

				<div class='list'>
					<table>
						<thead><tr>
							<th>Team Name (loc)</th>
							<th>Role</th>
							<th>Team Members</th>
							<th>Bundle</th>
						</tr></thead>
						<tbody>
							<g:each in="${sourceTeams}" status="i" var="projectTeamInstance">
								<tr class="teamstatus_${projectTeamInstance?.cssClass}" onclick="submitLoginForm('${projectTeamInstance?.team?.projectTeam?.moveBundle.id}','${projectTeamInstance?.team?.projectTeam?.id}','${projectTeamInstance?.team?.projectTeam?.role}','source')">
									<td><b>${projectTeamInstance?.team?.projectTeam?.name} (src)</b></td>
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
									<td><b>${projectTeamInstance?.team?.projectTeam?.name} (trg)</b></td>
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
		<g:link class="mobfooter" action="list" style="color:white;" params="[ viewMode:'mobile']">Use Mobile Site</g:link>

		<script type="text/javascript">
			function submitLoginForm( bundleId,teamId,role,location){
				var form = document.forms["loginForm"] ;
				form.bundleId.value = bundleId;
				form.teamId.value = teamId;
				form.location.value = location;
				if (role == "ROLE_CLEANER") {
					form.action = "logisticsHome";
				}
				form.submit();
			}
		</script>
		<script>
			currentMenuId = "#consoleMenu";
			$("#consoleMenuId a").css('background-color','#003366')
		</script>
	</body>
</html>
