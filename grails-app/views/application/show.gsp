<table style="border: 0">
	<tr>
		<td colspan="2"><div class="dialog">
				<table>
					<tbody>
						<tr>
							<td class="label" nowrap="nowrap"><label for="assetName">App
									Name</label>
							</td>
							<td>
								${applicationInstance.assetName}
							</td>
							<td class="label" nowrap="nowrap">Description</td>
							<td colspan="3">${applicationInstance.description}</td>
						</tr>
						<tr>
							<td class="label" nowrap="nowrap"><label for="assetType">Type</label>
							</td>
							<td>
								${applicationInstance.assetType}
							</td>
							<td class="label" nowrap="nowrap"><label for="supportType">Support</label>
							</td>
							<td>
								${applicationInstance.supportType}
							</td>
							<td class="label" nowrap="nowrap"><label for="appFunction">Function</label>
							</td>
							<td>
								${applicationInstance.appFunction}
							</td>
							<td class="label" nowrap="nowrap"><label
								for="userConcurrent">Users</label>
							</td>
							<td>${applicationInstance.userCount}</td>
						</tr>
						<tr>
							<td class="label" nowrap="nowrap"><label for="appVendor">Vendor</label>
							</td>
							<td>
								${applicationInstance.appVendor}
							</td>
							<td class="label" nowrap="nowrap"><label for="sme">SME1</label>
							</td>
							<td>
								${applicationInstance.sme}
							</td>
							<td class="label" nowrap="nowrap"><label for="environment">Environment</label>
							</td>
							<td>
								${applicationInstance.environment}
							</td>
							<td class="label" nowrap="nowrap"><label for="userLocations">User
									Location</label></td>
							<td>
								${applicationInstance.userLocations}
							</td>
						</tr>
						<tr>
							<td class="label" nowrap="nowrap"><label for="appVersion">Version</label>
							</td>
							<td>
								${applicationInstance.appVersion}
							</td>
							<td class="label" nowrap="nowrap"><label for="sme2">SME2</label>
							</td>
							<td>
								${applicationInstance.sme2}
							</td>
							<td class="label" nowrap="nowrap"><label for="criticality">Criticality</label>
							</td>
							<td>
								${applicationInstance.criticality}
							</td>
							<td class="label" nowrap="nowrap"><label
								for="userConcurrent">Concurrent</label>
							</td>
							<td>
								${applicationInstance.userConcurrent}
							</td>
						</tr>
						<tr>
							<td class="label" nowrap="nowrap"><label for="appTech">Tech.</label>
							</td>
							<td>
								${applicationInstance.appTech}
							</td>
							<td class="label" nowrap="nowrap"><label for="businessUnit">Bus
									Unit</label></td>
							<td>
								${applicationInstance.businessUnit}
							</td>
							<td class="label" nowrap="nowrap"><label for="moveBundle">Bundle</label>
							</td>
							<td>
								${applicationInstance.moveBundle}
							</td>
							<td class="label" nowrap="nowrap"><label for="useFrequency">Use
									Frequency</label></td>
							<td>
								${applicationInstance.userConcurrent}
							</td>
						</tr>
						<tr>
							<td class="label" nowrap="nowrap"><label for="appSource">Source</label>
							</td>
							<td>
								${applicationInstance.appSource}
							</td>
							<td class="label" nowrap="nowrap"><label for="appOwner">App
									Owner</label>
							</td>
							<td>
								${applicationInstance.appOwner}
							</td>
							<td class="label" nowrap="nowrap"><label for="planStatus">Plan
									Status</label></td>
							<td>
								${applicationInstance.planStatus}
							</td>
							<td class="label" nowrap="nowrap"><label for="drRpoDesc">DR
									RPO</label></td>
							<td>
								${applicationInstance.drRpoDesc}
							</td>
						</tr>
						<tr>
							<td class="label" nowrap="nowrap"><label for="license">License</label>
							</td>
							<td>
								${applicationInstance.license}
							</td>
							<td class="label" nowrap="nowrap">Retire</td>
							<td><tds:convertDateTime
									date="${applicationInstance?.retireDate}" formate="12hrs"
									timeZone="${request.getSession().getAttribute('CURR_TZ')?.CURR_TZ}" /></td>
							<td class="label" nowrap="nowrap">Maint Exp.</td>
							<td><tds:convertDateTime
									date="${applicationInstance?.maintExpDate}" formate="12hrs"
									timeZone="${request.getSession().getAttribute('CURR_TZ')?.CURR_TZ}" />
							</td>
							<td class="label" nowrap="nowrap"><label for="drRtoDesc">DR
									RTO</label></td>
							<td>
								${applicationInstance.drRtoDesc}
							</td>
						</tr>
					</tbody>
				</table>
			</div>
		</td>
	</tr>
	<tr>
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
						<g:each in="${supportAssets}" var="support" status="i">
							<tr onclick="getAppDetails(${support?.asset?.id})" class="${i%2? 'odd':'even' }">
								<td>
									${support?.dataFlowFreq}
								</td>
								<td>
									${support?.asset?.assetName}
								</td>
								<td>
									${support.type}
								</td>
								<td>
									${support.status}
								</td>
							</tr>
						</g:each>
					</tbody>
				</table>
			</div>
		</td>
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
						<g:each in="${dependentAssets}" var="dependent" status="i">
							<tr onclick="getAppDetails(${dependent.dependent?.id})" class="${i%2? 'odd':'even' }">
								<td>
									${dependent.dataFlowFreq}
								</td>
								<td>
									${dependent.dependent?.assetName}
								</td>
								<td>
									${dependent.type}
								</td>
								<td>
									${dependent.status}
								</td>
							</tr>
						</g:each>
					</tbody>
				</table>
			</div>
		</td>
	</tr>
	<tr>
		<td colspan="2">
			<div class="buttons">
				<g:form>
					<input type="hidden" name="id" value="${applicationInstance?.id}" />
					<span class="button"><input type="button" class="edit"
						value="Edit" onclick="editApp(${applicationInstance?.id})" /> </span>
					<span class="button"><g:actionSubmit class="delete"
							onclick="return confirm('Are you sure?');" value="Delete" /> </span>
				</g:form>
			</div></td>
	</tr>
</table>
