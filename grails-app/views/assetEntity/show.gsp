<g:form method="post">
	<table style="border:0;width:1000px;">
		<tr>
			<td colspan="2">
				<div class="dialog">
					<table>
						<tbody>
							<tr class="prop">
								<td class="label" nowrap="nowrap"><label for="assetName">App Name</label></td>
								<td >${assetEntityInstance.assetName}</td>
								
								<td class="label" nowrap="nowrap"><label for="application">Application</label></td>
								<td >${assetEntityInstance.application}</td>
								
								<td ><td class="label">Source</td>
								     <td class="label">Target</td>
								</td>
								<td class="label" nowrap="nowrap"><label for="custom1">Cust1</label>
								</td>
								<td >${assetEntityInstance.custom1}
								</td>
								
								
							</tr>
							<tr class="prop">
								<td class="label" nowrap="nowrap"><label for="assetType">Type</label></td>
								<td >${assetEntityInstance.assetType}</td>
								<td class="label" nowrap="nowrap"><label for="priority">Priority</label>
								</td>
								<td >${assetEntityInstance.priority}
								</td>
								<td class="label" nowrap="nowrap"><label for="sourceLocation">Location</label>
								<td>${assetEntityInstance.sourceLocation}</td>
									<td>${assetEntityInstance.targetLocation}</td>
								</td>
								<td class="label" nowrap="nowrap"><label for="custom2">Cust2</label>
								</td>
								<td >${assetEntityInstance.custom2}
								</td>
								
								
							</tr>
							<tr class="prop">
								<td class="label" nowrap="nowrap"><label for="manufacturer">Manufacturer</label>
								</td>
								 <td >
								   ${assetEntityInstance.manufacturer} 
								</td>
								<td class="label" nowrap="nowrap"><label for="ipAddress">IP1</label></td>
								<td >${assetEntityInstance.ipAddress}
								</td>
								<td class="label" nowrap="nowrap"><label for="sourceRoom">Room</label>
								<td>${assetEntityInstance.sourceRoom}</td>
									<td>${assetEntityInstance.targetRoom}</td>
								</td>
								<td class="label" nowrap="nowrap"><label for="custom3">Cust3</label>
								</td>
								<td >${assetEntityInstance.custom3}
								</td>
								
								
							</tr>
							<tr class="prop">
								<td class="label" nowrap="nowrap"><label for="model">Model</label>
								</td>
								<td>
								${assetEntityInstance.model}
								</td>
								
								<td class="label" nowrap="nowrap"><label for="assetTag">Tag</label></td>
								<td >${assetEntityInstance.assetTag}</td>
								
								<td class="label" nowrap="nowrap"><label for="sourceRack">Rack/Cab</label>
								<td>${assetEntityInstance.sourceRack}</td>
									<td>${assetEntityInstance.targetRack}</td>
								</td>
								<td class="label" nowrap="nowrap"><label for="custom4">Cust4</label>
								</td>
								<td >${assetEntityInstance.custom4}
								</td>
								
								
							</tr>
							<tr class="prop">
								<td class="label" nowrap="nowrap"><label for="shortName">Alt Name</label></td>
								<td >${assetEntityInstance.shortName}
								</td>
								<td class="label" nowrap="nowrap"><label for="os">OS</label></td>
								<td >${assetEntityInstance.os}
								</td>
								<td class="label" nowrap="nowrap"><label for="sourceRack">Position</label>
								<td>${assetEntityInstance.sourceRackPosition}</td>
									<td>${assetEntityInstance.targetRackPosition}</td>
								</td>
								<td class="label" nowrap="nowrap"><label for="custom5">Cust5</label>
								</td>
								<td >${assetEntityInstance.custom5}
								</td>
								
								
							</tr>
							<tr class="prop">
								<td class="label" nowrap="nowrap"><label for="serialNumber">S/N</label></td>
								<td >${assetEntityInstance.serialNumber}
								</td>
								<td class="label" nowrap="nowrap"><label for="maintContract">Maint</label></td>
								<td >${assetEntityInstance.maintContract}
								</td>
								<td class="label" nowrap="nowrap"><label for="sourceBladeChassis">Blade</label>
								<td>${assetEntityInstance.sourceBladeChassis}</td>
									<td>${assetEntityInstance.targetBladeChassis}</td>
								</td>
								<td class="label" nowrap="nowrap"><label for="custom6">Cust6</label>
								</td>
								 <td >${assetEntityInstance.custom6}
								</td>
								
								
								
								
							</tr>
							<tr class="prop">
								<td class="label" nowrap="nowrap"><label for="appOwner">owner</label></td>
								<td >${assetEntityInstance.owner}</td>
								<td class="label"><label for="retireDate">Retire
									Date:</label>
								</td>
								<td><tds:convertDate
									date="${assetEntityInstance?.retireDate}"
									timeZone="${request.getSession().getAttribute('CURR_TZ')?.CURR_TZ}" />
							</td>
								 
								<td class="label" nowrap="nowrap"><label for="sourceBladePosition">Blade Position</label>
								<td>${assetEntityInstance.sourceBladePosition}</td>
									<td>${assetEntityInstance.targetBladePosition}</td>
								</td>
								<td class="label" nowrap="nowrap"><label for="custom7">Cust7</label>
								</td>
								 <td >${assetEntityInstance.custom7}
								</td>
								
								
								
							</tr>
							<tr class="prop">
								<td  class="label"><label for="maintExpDate">Maint Exp.
									</label></td>
									<td><tds:convertDate
									date="${assetEntityInstance?.maintExpDate}"
									timeZone="${request.getSession().getAttribute('CURR_TZ')?.CURR_TZ}" />
							        </td>
								<td class="label" nowrap="nowrap"><label for="railType">Rail Type</label>
								</td>
								 <td >${assetEntityInstance.railType}
								</td>
								
								
								
								<td>&nbsp</td>
								<td>&nbsp</td>
								<td>&nbsp</td>
								<td class="label" nowrap="nowrap"><label for="custom8">Cust8</label>
								</td>
								 <td >${assetEntityInstance.custom8}
								</td>
								
							</tr>
							<tr class="prop">
							       <td class="label" nowrap="nowrap"><label for="moveBundle">Bundle</label></td>
								<td >${assetEntityInstance.moveBundle}
								</td>
							   <td class="label" nowrap="nowrap"><label for="planStatus">Plan Status</label></td>
								<td >${assetEntityInstance.planStatus}
								</td>
								<td>&nbsp</td>
								<td>&nbsp</td>
								<td>&nbsp</td>
							    
								<td class="label" nowrap="nowrap"><label for="truck">Truck</label>
								</td>
								<td >${assetEntityInstance.truck}
								</td>
							
							</tr>
							
							<tr class="prop">
							    <td class="label" nowrap="nowrap"><label for="cart">Cart</label>
								</td>
								 <td >${assetEntityInstance.cart}
								</td>
								<td class="label" nowrap="nowrap"><label for="shelf">Shelf</label>
								</td>
								 <td >${assetEntityInstance.shelf}
								</td>
								<td>&nbsp</td>
								<td>&nbsp</td>
								<td>&nbsp</td>
							    <td class="label" nowrap="nowrap"><label for="appSme">SME</label>
								</td>
								 <td >${assetEntityInstance.appSme}
								</td>
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
							<tr onclick="getAppDetails('${support?.asset?.assetType}', ${support?.asset?.id})" class="${i%2? 'odd':'even' }" style="cursor: pointer;">
								<td class="dep-${support.status}">
									${support?.dataFlowFreq}
								</td>
								<td class="dep-${support.status}">
									${support?.asset?.assetType}
								</td>
								<td class="dep-${support.status}">
									${support?.asset?.assetName}
								</td>
								<td class="dep-${support.status}">
									${support.type}
								</td>
								<td class="dep-${support.status}">
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
							<th>Entity Type</th>
							<th>Name</th>
							<th>Type</th>
							<th>Status</th>
						</tr>
					</thead>
					<tbody>
						<g:each in="${dependentAssets}" var="dependent" status="i">
							<tr onclick="getAppDetails('${dependent.dependent?.assetType}', ${dependent.dependent?.id})" class="${i%2? 'odd':'even' }" style="cursor: pointer;">
								<td class="dep-${dependent.status}">
									${dependent.dataFlowFreq}
								</td>
								<td class="dep-${dependent.status}">
									${dependent.dependent?.assetType}
								</td>
								<td class="dep-${dependent.status}">
									${dependent.dependent?.assetName}
								</td>
								<td class="dep-${dependent.status}">
									${dependent.type}
								</td>
								<td class="dep-${dependent.status}">
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
					<input name="attributeSet.id" type="hidden" value="1">
					<input name="project.id" type="hidden" value="${projectId}">
					<input type="hidden" name="id" value="${assetEntityInstance?.id}" />
					<span class="button"><input type="button" class="edit"
						value="Edit" onclick="createEditPage(${assetEntityInstance?.id})" /> </span>
					<span class="button"><g:actionSubmit class="delete"
							onclick="return confirm('Are you sure?');" value="Delete" /> </span>
				</div></td>
		</tr>
	</table>
</g:form>
