<%@page import="com.tds.asset.AssetEntity"%>
<g:form method="post">
	<input type="hidden" name="id" value="${fileInstance?.id}" />
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
									value="${fileInstance.assetName}" />
								</td>
								<td class="label" nowrap="nowrap">Description</td>
								<td colspan="3"><input type="text" id="description"
									name="description"
									value="${fileInstance.description}" size="50" />
								</td>
							</tr>
							<tr>
								<td class="label" nowrap="nowrap"><label for="assetType">App
										Type</label>
								</td>
								<td><g:select from="${assetTypeOptions}" id="assetType"
										name="assetType" value="${fileInstance.assetType}" /></td>
								<td class="label" nowrap="nowrap"><label for="supportType">Support</label>
								</td>
								<td><input type="text" id="supportType" name="supportType"
									value="${fileInstance.supportType}" /></td>
								<td class="label" nowrap="nowrap"><label for="fileFormat">
										Format</label>
								</td>
								<td><input type="text" id="fileFormat" name="fileFormat"
									value="${fileInstance.fileFormat}" /></td>
							</tr>

							<tr>

								<td class="label" nowrap="nowrap"><label for="environment">Enviorn</label>
								</td>
								<td><g:select id="environment" name="environment" from="${AssetEntity.constraints.environment.inList}" value="${fileInstance.environment}" />
								</td>
								<td class="label" nowrap="nowrap"><label for="fileSize">Size</label>
								</td>
								<td><input type="text" id="fileSize" name="fileSize"
									value="${fileInstance.fileSize}" /></td>
									<td class="label" nowrap="nowrap"><label for="moveBundle">Bundle</label>
								</td>
								<td><g:select from="${moveBundleList}" id="moveBundle"
										name="moveBundle.id" value="${fileInstance.moveBundle}"
										optionKey="id" optionValue="name" />
								</td>
							</tr>

							<tr>
								<td class="label" nowrap="nowrap"><label for="planStatus">Plan
										Status</label></td>
								<td><g:select from="${planStatusOptions}" id="planStatus"
										name="planStatus" value="${fileInstance.planStatus}" />
								</td>
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
