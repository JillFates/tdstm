<table style="border: 0">
	<tr>
		<td colspan="2">
			<div class="dialog" ondblclick="editApp(${databaseInstance?.id})">
				<table>
					<tbody>
						<tr class="prop">
							<td class="label" nowrap="nowrap"><label for="assetName">Name</label></td>
							<td style="font-weight:bold;">${databaseInstance?.assetName}</td>
							<td class="label" nowrap="nowrap">Description</td>
							<td colspan="3">${databaseInstance.description}</td>

						</tr>
						<tr class="prop">
							<td class="label" nowrap="nowrap"><label for="assetType">Type</label></td>
							<td>${databaseInstance?.assetType}</td>
							<td class="label" nowrap="nowrap"><label for="supportType">Support</label></td>
							<td>${databaseInstance?.supportType}</td>
							<td class="label" nowrap="nowrap"><label for="environment">Environment</label></td>
							<td>${databaseInstance?.environment}</td>
						</tr>
						<tr class="prop">
							<td class="label" nowrap="nowrap"><label for="dbFormat">Format</label></td>
							<td>${databaseInstance?.dbFormat}</td>
							<td class="label" nowrap="nowrap">Retire</td>
							<td><tds:convertDate date="${databaseInstance?.retireDate}" timeZone="${request.getSession().getAttribute('CURR_TZ')?.CURR_TZ}" />
							</td>
							<td class="label" nowrap="nowrap"><label for="moveBundle">Bundle</label></td>
							<td>${databaseInstance?.moveBundle}</td>
						</tr>
						<tr class="prop">
							<td class="label" nowrap="nowrap"><label for="dbSize">Size</label></td>
							<td>${databaseInstance?.dbSize}</td>
							<td class="label" nowrap="nowrap">Maint Exp.</td>
							<td><tds:convertDate date="${databaseInstance?.maintExpDate}" timeZone="${request.getSession().getAttribute('CURR_TZ')?.CURR_TZ}" /></td>
							<td class="label" nowrap="nowrap"><label for="planStatus">Plan Status</label></td>
							<td>${databaseInstance?.planStatus}</td>
						</tr>
					</tbody>
				</table>
			</div></td>
	</tr>
	<tr>
	<td valign="top">
		<div>
			<h1>Supports:</h1>
			<table style="width: 400px;">
				<thead>
					<tr>
						<th>Frequency</th>
						<th>Entity Type</th>
						<th>Name</th>
						<th>Type</th>
						<th>Status</th>
					</tr>
				</thead>
				<tbody>
					<g:each in="${supportAssets}" var="support" status="i">
						<tr onclick="getDbDetails('${support?.asset?.assetType}', ${support?.asset?.id})" class="${i%2? 'odd':'even' }" style="cursor: pointer;">
							<td class="dep-${support.status}">${support?.dataFlowFreq}</td>
							<td class="dep-${support.status}">${support?.asset?.assetType}</td>
							<td class="dep-${support.status}">${support?.asset?.assetName}</td>
							<td class="dep-${support.status}">${support.type}</td>
							<td class="dep-${support.status}">${support.status}</td>
						
						</tr>
					</g:each>
				</tbody>
			</table>
		</div></td>
	<td valign="top">
		<div>
			<h1>Depends on:</h1>
			<table style="width: 400px;">
				<thead>
					<tr>
						<th>Frequency</th>
						<th>Entity Type</th>
						<th>Name</th>
						<th>Type</th>
						<th>Status</th>
					</tr>
				</thead>
				<tbody>
					<g:each in="${dependentAssets}" var="dependent" status="i">
						<tr onclick="getDbDetails('${dependent.dependent?.assetType}', ${dependent.dependent?.id})" class="${i%2? 'odd':'even' }" style="cursor: pointer;">
							<td class="dep-${dependent.status}">${dependent.dataFlowFreq}</td>
							<td class="dep-${dependent.status}">${dependent.dependent?.assetType}</td>
							<td class="dep-${dependent.status}">${dependent.dependent?.assetName}</td>
							<td class="dep-${dependent.status}">${dependent.type}</td>
							<td class="dep-${dependent.status}">${dependent.status}</td>
						</tr>
					</g:each>
				</tbody>
			</table>
		</div></td>
	</tr>
	<tr>
		<td colspan="2">
			<div class="buttons">
				<g:form>
					<input type="hidden" name="id" value="${databaseInstance?.id}" />
					<span class="button"><input type="button" class="edit" value="Edit" onclick="editDb(${databaseInstance?.id})" /> </span>
					<span class="button"><g:actionSubmit class="delete" onclick="return confirm('Are you sure?');" value="Delete" /> </span>
				</g:form>
			</div>
		</td>
	</tr>
</table>