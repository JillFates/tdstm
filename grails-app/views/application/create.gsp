<g:form method="post">
	<table style="border: 0">
		<tr>
			<td colspan="2">
				<div class="dialog">
					<table>
						<tbody>
							<tr>
								<td class="label" nowrap="nowrap"><label for="assetName">App Name</label></td>
								<td ><input type="text" id="assetName" name="assetName" value="${applicationInstance.assetName}" /></td>
								<td class="label" nowrap="nowrap">Description</td>
								<td colspan="3"><input type="text" id="description" name="description"value="This Application Support the XYZ Business" size="50"/>
								</td>
							</tr>
							<tr>
								<td class="label" nowrap="nowrap"><label for="assetType">App Type</label></td>
								<td ><g:select from="${assetTypeOptions}" id="assetType" name="assetType" value="${applicationInstance.assetType}" /></td>
								<td class="label" nowrap="nowrap"><label for="supportType">Support</label>
								</td>
								<td ><input type="text" id="supportType"
									name="supportType" value="${applicationInstance.supportType}" />
								</td>
								<td class="label" nowrap="nowrap"><label for="appFunction">Function</label>
								</td>
								<td ><input type="text" id="appFunction"
									name="appFunction" value="${applicationInstance.appFunction}" />
								</td>
								<td class="label" nowrap="nowrap"><label for="userConcurrent">Users</label>
								</td>
								<td ><input type="text" id="userId" name="userCount" value="${applicationInstance.userCount}" />
								</td>
							</tr>
							<tr>
								<td class="label" nowrap="nowrap"><label for="appVendor">Vendor</label></td>
								<td ><input type="text" id="appVendor"
									name="appVendor" value="${applicationInstance.appVendor}" />
								</td>
								<td class="label" nowrap="nowrap"><label for="sme">SME1</label></td>
								<td ><input type="text" id="sme" name="sme"
									value="${applicationInstance.sme}" />
								</td>
								<td class="label" nowrap="nowrap"><label for="environment">Environment</label>
								</td>
								<td ><g:select id="environment" name="environment" from="${applicationInstance.constraints.environment.inList}" value="${applicationInstance.environment}"></g:select>
								</td>
								<td class="label" nowrap="nowrap"><label for="userLocations">User Location</label>
								</td>
								<td ><input type="text" id="userLocations"
									name="userLocations"
									value="${applicationInstance.userLocations}" />
								</td>
							</tr>
							<tr>
								<td class="label" nowrap="nowrap"><label for="appVersion">Version</label>
								</td>
								<td ><input type="text" id="appVersion"
									name="appVersion" value="${applicationInstance.appVersion}" />
								</td>
								<td class="label" nowrap="nowrap"><label for="sme2">SME2</label></td>
								<td ><input type="text" id="sme2" name="sme2"
									value="${applicationInstance.sme2}" />
								</td>
								<td class="label" nowrap="nowrap"><label for="criticality">Criticality</label>
								</td>
								<td ><g:select id="criticality" name="criticality" from="${applicationInstance.constraints.criticality.inList}" value="${applicationInstance.criticality}"></g:select></td>
								<td class="label" nowrap="nowrap"><label for="userConcurrent">Concurrent</label>
								</td>
								<td ><input type="text" id="userConcurrent"	name="userConcurrent" value="${applicationInstance.userConcurrent}" />
								</td>
							</tr>
							<tr>
								<td class="label" nowrap="nowrap"><label for="appTech">Tech.</label></td>
								<td ><input type="text" id="appTech" name="appTech" value="${applicationInstance.appTech}" />
								</td>
								<td class="label" nowrap="nowrap"><label for="businessUnit">Bus	Unit</label>
								</td>
								<td ><input type="text" id="businessUnit" name="businessUnit" value="${applicationInstance.businessUnit}" />
								</td>
								<td class="label" nowrap="nowrap"><label for="moveBundle">Bundle</label></td>
								<td ><g:select from="${moveBundleList}" id="moveBundle" name="moveBundle.id" value="${applicationInstance.moveBundle}" optionKey="id" optionValue="name"/>
								</td>
								<td class="label" nowrap="nowrap"><label for="useFrequency">Use	Frequency</label>
								</td>
								<td ><input type="text" id="useFrequency" name="useFrequency" value="${applicationInstance.userConcurrent}" />
								</td>
							</tr>
							<tr>
								<td class="label" nowrap="nowrap"><label for="appSource">Source</label></td>
								<td ><input type="text" id="appSource"	name="appSource" value="${applicationInstance.appSource}" />
								</td>
								<td class="label" nowrap="nowrap"><label for="appOwner">App Owner</label></td>
								<td ><input type="text" id="appOwner" name="appOwner"	value="${applicationInstance.appOwner}" />
								</td>
								<td class="label" nowrap="nowrap"><label for="planStatus">Plan Status</label>
								</td>
								<td ><g:select from="${planStatusOptions}" id="planStatus" name="planStatus" value="${applicationInstance.planStatus}" />
								</td>
								<td class="label" nowrap="nowrap"><label for="drRpoDesc">DR RPO</label>
								</td>
								<td ><input type="text" id="drRpoDesc"	name="drRpoDesc" value="${applicationInstance.drRpoDesc}" />
								</td>
							</tr>
							<tr>
								<td class="label" nowrap="nowrap"><label for="license">License</label></td>
								<td ><input type="text" id="license" name="license" value="${applicationInstance.license}" />
								</td>
								<td class="label" nowrap="nowrap">Retire</td>
								<td ><script type="text/javascript">
				                    $(document).ready(function(){
				                      $("#retireDate").datetimepicker();
				                    });
								</script> 
								<input type="text" class="dateRange" size="15" style="width: 132px; height: 14px;" id="retireDate" name="retireDate"
                                   value="<tds:convertDateTime date="${applicationInstance?.retireDate}" formate="12hrs" timeZone="${request.getSession().getAttribute('CURR_TZ')?.CURR_TZ}"/>" 
                                   onchange="isValidDate(this.value)"/></td>
								<td class="label" nowrap="nowrap">Maint Exp.</td>
								<td >
								<script type="text/javascript">
				                    $(document).ready(function(){
				                      $("#maintExpDate").datetimepicker();
				                    });
								</script> 
								<input type="text" class="dateRange" size="15" style="width: 132px; height: 14px;" id="maintExpDate" name="maintExpDate"
                                   value="<tds:convertDateTime date="${applicationInstance?.maintExpDate}" formate="12hrs" timeZone="${request.getSession().getAttribute('CURR_TZ')?.CURR_TZ}"/>" 
                                   onchange="isValidDate(this.value)"/>
								</td>
								<td class="label" nowrap="nowrap"><label for="drRtoDesc">DR RTO</label>
								</td>
								<td ><input type="text" id="drRtoDesc"	name="drRtoDesc" value="${applicationInstance.drRtoDesc}" />
								</td>
							</tr>
						</tbody>
					</table>
				</div></td>
		</tr>
		<tr>
			<td>
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
						</tbody>
					</table>
				</div></td>
			<td>
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
						</tbody>
					</table>
				</div></td>
		</tr>
		<tr>
			<td colspan="2">
				<div class="buttons">
					<input name="attributeSet.id" type="hidden" value="1">
					<input name="project.id" type="hidden" value="${projectId}">
					<span class="button"><g:actionSubmit class="save" value="Save" /> </span>
				</div></td>
		</tr>
	</table>
</g:form>
