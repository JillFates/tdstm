<%@page import="net.transitionmanager.project.ProjectTeam" %>
<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8"/>
        <meta name="layout" content="projectHeader" />
        <title>Move Bundle Team List</title>
    </head>
    <body>

        <div class="body">
            <h1>Move Bundle Team List</h1>
            <g:if test="${flash.message}">
            <div class="message">${flash.message}</div>
            </g:if>
            <div class="list">
                <table>
                    <thead>
                        <tr>
				<th>Team Code</th>
				<th>Team Name</th>
				<th>Role</th>
				<th>Team Members</th>
				<th>Date Created</th>
				<th>Last Updated</th>
				<th>Comment</th>
                        </tr>
                    </thead>
                    <tbody>
                    <g:each in="${projectTeamInstanceList}" status="i" var="projectTeamInstance">
                        <tr class="${(i % 2) == 0 ? 'odd' : 'even'}">
				<td><g:link action="show" id="${projectTeamInstance?.projectTeam.id}" params="[bundleId:bundleInstance?.id]">${projectTeamInstance?.projectTeam?.teamCode}</g:link></td>
				<td>${projectTeamInstance?.projectTeam?.name}</td>
				<td><g:if test="${projectTeamInstance?.projectTeam?.role}"> <g:message code="ProjectTeam.role.${projectTeamInstance?.projectTeam?.role}" /></g:if></td>
				<td>
				<g:each in="${projectTeamInstance?.teamMembers}" var="teamMember">
					<g:if test="${teamMember.company[0]}">${teamMember.company[0]}:</g:if>
					${teamMember?.staff?.lastNameFirstAndTitle}
					<br/>
				</g:each>
				</td>
				<td><tds:convertDateTime date="${projectTeamInstance?.projectTeam?.dateCreated}" /></td>
				<td><tds:convertDateTime date="${projectTeamInstance?.projectTeam?.lastUpdated}" /></td>
				<td>${projectTeamInstance?.projectTeam?.comment}</td>
			</tr>
                    </g:each>
                    </tbody>
                </table>
            </div>
            <div class="buttons">
		<g:form>
			<input type="hidden" name="bundleId" value="${bundleInstance?.id}" />
			<span class="button"><g:actionSubmit class="create" action="Create" value="Create Project Team" /></span>
		</g:form>
            </div>
	</div>
	<script>
		currentMenuId = "#eventMenu";
		$("#eventMenuId a").css('background-color','#003366')
   </script>
    </body>
</html>
