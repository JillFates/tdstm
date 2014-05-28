<g:each in="${appList}" var="app">
	<tr class="cursor" onclick="getEntityDetails('myIssues','${app.assetType}',${app.id})">

		<g:if test="${project=='All' }">
			<td>
				${app.project.name}
			</td>
		</g:if>
		<td>
			<a href="javascript:#"> ${app.assetName}</a>
		</td>
		<td>
			${app.planStatus}
		</td>
		<td>
			${app.moveBundle}
		</td>
		<td>
			${relationList[app.id]}
		</td>
	</tr>
</g:each>
