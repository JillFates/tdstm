<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
        <meta name="layout" content="projectHeader" />
        <title>Teams List</title>
    </head>
    <body>
    <g:form action="home" name="loginForm">
        <input type="hidden" name="bundleId" id="bundleId"/>
        <input type="hidden" name="teamId" id="teamId"/>
        <input type="hidden" name="location" id="locationId"/>
        <input type="hidden" name="projectId" value="${projectId}"/>
    <div class="body">
            <h1>Teams List</h1>
            <g:if test="${flash.message}">
            <div class="message">${flash.message}</div>
            </g:if>
            <div class="nav" style="border: 1px solid #CCCCCC; height: 11px">
           		<span class="menuButton"><g:link class="list" action="list" params="[projectId:projectId, viewMode:'mobile']">Mobile Site</g:link></span>
        	</div>
            <div style="margin-top: 5px;" class='list'>
            <table>
        	<thead>
				<tr>
					<th>Team Name</th>
					<th>Role</th>
					<th>Team Members</th>
              	</tr>
			</thead>
            <tbody>
            	<g:each in="${projectTeams}" status="i" var="projectTeamInstance">
					<tr class="${(i % 2) == 0 ? 'odd' : 'even'}"onclick="submitLoginForm('${projectTeamInstance?.projectTeam?.moveBundle.id}','${projectTeamInstance?.projectTeam?.id}','source')">
                        <td><b>${projectTeamInstance?.projectTeam?.name} src</b></td>
                        <td><g:if test="${projectTeamInstance?.projectTeam?.role}"><g:message code="ProjectTeam.role.${projectTeamInstance?.projectTeam?.role}" /></g:if></td>
						<td>
							<g:each in="${projectTeamInstance?.teamMembers}" var="teamMember">
			 					<g:if test="${teamMember.company[0]}">${teamMember.company[0]}:</g:if><g:if test="${teamMember?.staff?.lastName}">${teamMember?.staff?.lastName},</g:if> ${teamMember?.staff?.firstName} <g:if test="${teamMember?.staff?.title}">- ${teamMember?.staff?.title}</g:if> <br/>
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
			 					<g:if test="${teamMember.company[0]}">${teamMember.company[0]}:</g:if><g:if test="${teamMember?.staff?.lastName}">${teamMember?.staff?.lastName},</g:if> ${teamMember?.staff?.firstName} <g:if test="${teamMember?.staff?.title}">- ${teamMember?.staff?.title}</g:if> <br/>
							</g:each>
						</td>
            		</tr>
				</g:each>
				<g:if test="${projectTeams.size() == 0}">
				<tr><td colspan="3" class="no_records">No records found</td></tr>
				</g:if>
			</tbody>
        	</table>
            </div>
        </div>
        </g:form>
 <script type="text/javascript">
    function submitLoginForm( bundleId,teamId,location){
	var form = document.forms["loginForm"] ;
	form.bundleId.value = bundleId;
	form.teamId.value = teamId;
	form.location.value = location;
	form.submit();
}
</script>
    </body>
</html>
