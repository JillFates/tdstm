<g:each in="${recentLogin}" var="loginInfo">
	<tr class="cursor" onclick="Person.showPersonDialog(${loginInfo.personId},'generalInfoShow')">
		<td>
			${loginInfo.projectName}
		</td>
		<td>
			${loginInfo.personName}
		</td>
		<td>
			${loginInfo.lastActivity}
		</td>
	</tr>
</g:each>
