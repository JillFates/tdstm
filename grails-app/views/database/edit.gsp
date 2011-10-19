<%@page import="com.tds.asset.AssetEntity"%>
<g:form method="post">
	<input type="hidden" name="id" value="${databaseInstance?.id}" />
	<table style="border: 0">
		<tr>
			<td colspan="2">
				<div class="dialog">
					<table>
						<tbody>
							<tr>
								<td class="label" nowrap="nowrap"><label for="assetName">App
										Name</label>
								</td>
								<td><input type="text" id="assetName" name="assetName"
									value="${databaseInstance.assetName}" />
								</td>
								<td class="label" nowrap="nowrap">Description</td>
								<td colspan="3"><input type="text" id="description"
									name="description"
									value="This Application Support the XYZ Business" size="50" />
								</td>
							</tr>
							<tr>
								<td class="label" nowrap="nowrap"><label for="assetType">App
										Type</label>
								</td>
								<td><g:select from="${assetTypeOptions}" id="assetType"
										name="assetType" value="${databaseInstance?.assetType}" /></td>
								<td class="label" nowrap="nowrap"><label for="supportType">Support</label>
								</td>
								<td><input type="text" id="supportType" name="supportType"
									value="${databaseInstance.supportType}" /></td>
								<td class="label" nowrap="nowrap"><label for="dbFormat">
										Format</label>
								</td>
								<td><input type="text" id="dbFormat" name="dbFormat"
									value="${databaseInstance.dbFormat}" />
							</tr>

							<tr>

								<td class="label" nowrap="nowrap"><label for="environment">Enviorn</label>
								</td>
								<td><g:select id="environment" name="environment" from="${AssetEntity.constraints.environment.inList}" value="${databaseInstance.environment}" />
								</td>
								<td class="label" nowrap="nowrap"><label for="dbSize">Size</label>
								</td>
								<td><input type="text" id="dbSize" name="dbSize"
									value="${databaseInstance.dbSize}" /></td>
									<td class="label" nowrap="nowrap">Retire</td>
								<td><script type="text/javascript">
									$(document).ready(function() {
										$("#retireDate").datetimepicker();
									});
								</script> <input type="text" class="dateRange" size="15"
									style="width: 132px; height: 14px;" id="retireDate"
									name="retireDate"
									value="<tds:convertDateTime date="${databaseInstance?.retireDate}" formate="12hrs" timeZone="${request.getSession().getAttribute('CURR_TZ')?.CURR_TZ}"/>"
									onchange="isValidDate(this.value)" /></td>
							</tr>
							<tr>
								
								<td class="label" nowrap="nowrap"><label for="moveBundle">Bundle</label>
								</td>
								<td><g:select from="${moveBundleList}" id="moveBundle"
										name="moveBundle.id" value="${databaseInstance?.moveBundle}"
										optionKey="id" optionValue="name" /></td>
								<td class="label" nowrap="nowrap">Maint Exp.</td>
								<td><script type="text/javascript">
									$(document).ready(function() {
										$("#maintExpDate").datetimepicker();
									});
								</script> <input type="text" class="dateRange" size="15"
									style="width: 132px; height: 14px;" id="maintExpDate"
									name="maintExpDate"
									value="<tds:convertDateTime date="${databaseInstance?.maintExpDate}" formate="12hrs" timeZone="${request.getSession().getAttribute('CURR_TZ')?.CURR_TZ}"/>"
									onchange="isValidDate(this.value)" /></td>
									<td class="label" nowrap="nowrap"><label for="planStatus">Plan
										Status</label></td>
								<td><g:select from="${planStatusOptions}" id="planStatus"
										name="planStatus" value="${databaseInstance.planStatus}" /></td>
							</tr>
						</tbody>
					</table>
				</div>
			</td>
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
							<g:each in="${supportAssets}" var="support">
								<tr>
									<td>${support?.dataFlowFreq}</td>
									<td>${support?.asset}</td>
									<td><g:select id="selectId" value=${support.type
											}
											from="${AssetDependency.constraints.type.inList}" /></td>
									<td><g:select id="typeId" value=${support.status
											}
											from="${AssetDependency.constraints.status.inList}" /></td>
								</tr>
							</g:each>
						</tbody>
					</table>
				</div>
			</td>
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
							<g:each in="${supportAssets}" var="support">
								<tr>
									<td>${support?.dataFlowFreq}</td>
									<td>${support?.dependent}</td>
									<td><g:select id="selectId" value=${support.type
											}
											from="${AssetDependency.constraints.type.inList}" /></td>
									<td><g:select id="typeId" value=${support.status
											}
											from="${AssetDependency.constraints.status.inList}" /></td>
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
					<span class="button"><g:actionSubmit class="save"
							value="Update" /> </span> <span class="button"><g:actionSubmit
							class="delete" onclick="return confirm('Are you sure?');"
							value="Delete" /> </span>
				</div>
			</td>
		</tr>
	</table>
</g:form>
