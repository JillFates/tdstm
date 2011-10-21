<g:form method="post">
	<table style="border: 0;width:1000px">
		<tr>
			<td colspan="2">
				<div class="dialog">
					<table>
						<tbody>
							<tr>
								<td class="label" nowrap="nowrap"><label for="assetName">File
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
								<td class="label" nowrap="nowrap"><label for="assetType">
										Type</label>
								</td>
								<td><input type="text" id="assetType" name="assetType"
									value="File" readonly="readonly" />
								</td>
								<td class="label" nowrap="nowrap"><label for="supportType">Support</label>
								</td>
								<td><input type="text" id="supportType" name="supportType"
									value="${fileInstance.supportType}" /></td>
								<td class="label" nowrap="nowrap"><label for="fileFormat">Format</label>
								</td>
								<td><input type="text" id="fileFormat" name="fileFormat"
									value="${fileInstance.fileFormat}" />
								</td>
							</tr>

							<tr>
								<td class="label" nowrap="nowrap"><label for="environment">Enviorn
								</label>
								</td>
								<td><g:select id="environment" name="environment" from="${com.tds.asset.AssetEntity.constraints.environment.inList}"/>
								</td>
								<td class="label" nowrap="nowrap"><label for="fileSize">Size
								</label>
								</td>
								<td><input type="text" id="fileSize" name="fileSize"
									value="${fileInstance.fileSize}" />
								</td>
								<td class="label" nowrap="nowrap"><label for="moveBundle">Bundle</label>
								</td>
								<td><g:select from="${moveBundleList}" id="moveBundle"
										name="moveBundle.id" value="${fileInstance.moveBundle}"
										optionKey="id" optionValue="name" /></td>
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
			<td valign="top">
				<div style="width: 400px;">
					<span style="float: left;"><h1>Supports:</h1></span>
					<span style="float: right;"><input type='button' value='Add' onclick="addAssetDependency('support')"></span>
					<br/>
					<table style="width: 100%;">
						<thead>
							<tr>
								<th>Frequency</th>
								<th>Name</th>
								<th>Type</th>
								<th>Status</th>
								<th>&nbsp;</th>
							</tr>
						</thead>
						<tbody id="createSupportsList">
						</tbody>
					</table>
				</div></td>
			<td valign="top">
				<div  style="width: 400px;">
					<span style="float: left;"><h1>Is dependent on:</h1></span>
					<span style="float: right;"><input type='button' value='Add' onclick="addAssetDependency('dependent')"></span>
					<br/>
					<table style="width: 100%;">
						<thead>
							<tr>
								<th>Frequency</th>
								<th>Name</th>
								<th>Type</th>
								<th>Status</th>
								<th>&nbsp;</th>
							</tr>
						</thead>
						<tbody id="createDependentsList">
						</tbody>
					</table>
				</div></td>
		</tr>
		<tr>
			<td colspan="2">
				<div class="buttons">
				<input name="dependentCount" id="dependentCount" type="hidden" value="0">
					<input  name="supportCount"  id="supportCount" type="hidden" value="0">
					<input name="attributeSet.id" type="hidden" value="1"> <input
						name="project.id" type="hidden" value="${projectId}"> <span
						class="button"><g:actionSubmit class="save" value="Save" />
					</span>
				</div>
			</td>
		</tr>
	</table>
</g:form>
