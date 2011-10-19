<table style="border: 0">
	<tr>
		<td colspan="2"><div class="dialog">
				<table>
					<tbody>
						<tr class="prop">
							<td class="label" nowrap="nowrap"><label for="assetName">App
									Name</label></td>
							<td>${databaseInstance?.assetName}</td>

							<td class="label" nowrap="nowrap">Description</td>
							<td colspan="3">This Application the XYZ Business</td>

						</tr>

						<tr class="prop">
							<td class="label" nowrap="nowrap"><label for="assetType">App
									Type</label></td>
							<td>${databaseInstance?.assetType}</td>

							<td class="label" nowrap="nowrap"><label for="supportType">Support
							</label></td>
							<td>${databaseInstance?.supportType}</td>

							<td class="label" nowrap="nowrap"><label for="dbFormat">
									Format </label></td>
							<td>${databaseInstance?.dbFormat}</td>
						</tr>

						<tr class="prop">

							<td class="label" nowrap="nowrap"><label for="environment">Enviorn
							</label></td>
							<td>${databaseInstance?.environment}</td>

							<td class="label" nowrap="nowrap"><label for="dbSize">Size
							</label></td>
							<td>${databaseInstance?.dbSize}</td>
							<td class="label" nowrap="nowrap">Retire</td>
							<td><tds:convertDateTime
									date="${databaseInstance?.retireDate}" formate="12hrs"
									timeZone="${request.getSession().getAttribute('CURR_TZ')?.CURR_TZ}" />
							</td>
						</tr>

						<tr class="prop">
							<td class="label" nowrap="nowrap"><label for="moveBundle">Bundle</label>
							</td>
							<td>${databaseInstance?.moveBundle}</td>
							<td class="label" nowrap="nowrap">Maint Exp.</td>
							<td><tds:convertDateTime
									date="${databaseInstance?.maintExpDate}" formate="12hrs"
									timeZone="${request.getSession().getAttribute('CURR_TZ')?.CURR_TZ}" />
							</td>
							<td class="label" nowrap="nowrap"><label for="planStatus">Plan
									Status</label>
							</td>
							<td>${databaseInstance?.planStatus}</td>

						</tr>
					</tbody>
				</table>
			</div></td>
	</tr>
	<td valign="top">
		<div>
			<h1>Supports:</h1>
			<table style="width: 400px;">
				<thead>
					<tr>
						<th>Frequency</th>
						<th>Asset</th>
						<th>Type</th>
						<th>Status</th>
					</tr>
				</thead>
				<tbody>
					<g:each in="${supportAssets}" var="support">
						<tr>
							<td>${support?.dataFlowFreq}</td>
							<td>${support?.asset}</td>
							<td>${support.type}</td>
							<td>${support.status}</td>
						
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
						<th>Asset</th>
						<th>Type</th>
						<th>Status</th>
					</tr>
				</thead>
				<tbody>
					<g:each in="${dependentAssets}" var="dependent">
						<tr>
							<td>${dependent.dataFlowFreq}</td>
							<td>${dependent.dependent}</td>
							<td>${dependent.type}</td>
							<td>${dependent.status}</td>
						
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
					<span class="button"><input type="button" class="edit"
						value="Edit" onclick="editDb(${databaseInstance?.id})" /> </span>
					<span class="button"><g:actionSubmit class="delete"
							onclick="return confirm('Are you sure?');" value="Delete" /> </span>
				</g:form>
			</div>
		</td>
	</tr>
</table>