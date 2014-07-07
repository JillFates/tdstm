<g:each in="${recentLogin.keySet()}" var="per">
	<tr class="cursor" onclick="getUserDetails(${recentLogin[per].name.id})">
		<td>
			${recentLogin[per].project.name}
		</td>
		<td>
			${recentLogin[per].name.lastNameFirst}
		</td>
	</tr>
</g:each>