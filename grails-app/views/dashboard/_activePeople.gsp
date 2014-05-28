<g:each in="${recentLogin.keySet()}" var="per">
	<tr>
		<td>
			${recentLogin[per].project.name}
		</td>
		<td>
			${recentLogin[per].name.lastNameFirst}
		</td>
	</tr>
</g:each>