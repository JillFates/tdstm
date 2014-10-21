<g:each in="${appList}" var="app">
	<tr class="cursor" onclick="EntityCrud.showAssetDetailView('${app.assetClass}',${app.id});">
		<g:if test="${project=='All' }">
			<td>${app.project.name}</td>
		</g:if>
		<td>${app.assetName}</td>
		<td>${app.planStatus}</td>
		<td>${app.moveBundle}</td>
		<td>${relationList[app.id]}</td>
	</tr>
</g:each>
