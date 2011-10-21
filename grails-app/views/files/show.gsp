<table style="border: 0">
	<tr>
		<td colspan="2"><div class="dialog">
				<table>
					<tbody>
						<tr class="prop">
							<td class="label" nowrap="nowrap"><label for="assetName">App
									Name</label></td>
							<td style="font-weight:bold;">${filesInstance.assetName}</td>

							<td class="label" nowrap="nowrap"><label for="description">Description</label></td>
							<td colspan="3">${filesInstance.description}</td>

						</tr>

						<tr class="prop">
							<td class="label" nowrap="nowrap"><label for="assetType">App
									Type</label></td>
							<td>${filesInstance.assetType}</td>

							<td class="label" nowrap="nowrap"><label for="supportType">Support
							</label></td>
							<td>${filesInstance.supportType}</td>

							<td class="label" nowrap="nowrap"><label for="fileFormat">
									Format </label></td>
							<td>${filesInstance.fileFormat}</td>

						</tr>

						<tr class="prop">

							<td class="label" nowrap="nowrap"><label for="environment">Enviorn
							</label></td>
							<td>${filesInstance.environment}</td>

							<td class="label" nowrap="nowrap"><label for="fileSize">Size
							</label></td>
							<td>${filesInstance.fileSize}</td>
							<td class="label" nowrap="nowrap"><label for="moveBundle">Bundle</label>
							</td>
							<td>${filesInstance.moveBundle}</td>
						</tr>

						<tr class="prop">
							<td class="label" nowrap="nowrap"><label for="planStatus">Plan
									Status</label>
							</td>
							<td>${filesInstance.planStatus}</td>
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
						<th>Name</th>
						<th>Type</th>
						<th>Status</th>
					</tr>
				</thead>
				<tbody>
					<g:each in="${supportAssets}" var="support">
						<tr>
							<td class="dep-${support.status}">${support?.dataFlowFreq}</td>
							<td class="dep-${support.status}">${support?.asset}</td>
							<td class="dep-${support.status}">${support.type}</td>
							<td class="dep-${support.status}">${support.status}</td>
						</tr>
					</g:each>
				</tbody>
			</table>
		</div></td>
	<td valign="top">
		<div>
			<h1>Is dependent on:</h1>
			<table style="width: 400px;">
				<thead>
					<tr>
						<th>Frequency</th>
						<th>Name</th>
						<th>Type</th>
						<th>Status</th>
					</tr>
				</thead>
				<tbody>
					<g:each in="${dependentAssets}" var="dependent">
						<tr>
							<td class="dep-${dependent.status}">${dependent.dataFlowFreq}</td>
							<td class="dep-${dependent.status}">${dependent.dependent}</td>
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
					<input type="hidden" name="id" value="${filesInstance?.id}" />
					<span class="button"><input type="button" class="edit"
						value="Edit" onclick="editFile(${filesInstance?.id})" /> </span>
					<span class="button"><g:actionSubmit class="delete"
							onclick="return confirm('Are you sure?');" value="Delete" /> </span>
				</g:form>
			</div>
		</td>
	</tr>
</table>