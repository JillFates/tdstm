<g:each in="${recentLogin}" var="loginInfo">
	<tr class="cursor" onclick="getUserDetails(${loginInfo.personId})">
		<td>
			${loginInfo.projectName}
		</td>
		<td>
			${loginInfo.personName}
		</td>
	</tr>
</g:each>