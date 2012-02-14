<g:form method="post">
	<table style="border: 0;">
		<tr>
			<td colspan="2">
				<div class="dialog">
					<table>
						<tbody>
							<tr>
								<td class="label" nowrap="nowrap"><label for="assetName">Name<span style="color: red;">*</span></label>
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
									value="Files" readonly="readonly" />
								</td>
								<td class="label" nowrap="nowrap"><label for="supportType">Support</label>
								</td>
								<td><input type="text" id="supportType" name="supportType"
									value="${fileInstance.supportType}" /></td>
								<td class="label" nowrap="nowrap"><label for="fileFormat">Format<span style="color: red;">*</span></label>
								</td>
								<td><input type="text" id="fileFormat" name="fileFormat"
									value="${fileInstance.fileFormat}" />
								</td>
							</tr>

							<tr>
								<td class="label" nowrap="nowrap"><label for="environment">Environment
								</label>
								</td>
								<td><g:select id="environment" name="environment" from="${com.tds.asset.AssetEntity.constraints.environment.inList}"/>
								</td>
								<td class="label" nowrap="nowrap"><label for="fileSize">Size<span style="color: red;">*</span>
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
								<td class="label" nowrap="nowrap"><label for="custom1">${project.custom1 ?: 'Custom1'}</label></td>
								<td><input type="text" id="custom1" name="custom1"  value="${fileInstance?.custom1}"  /></td>
								<td class="label" nowrap="nowrap"><label for="custom1">${project.custom2 ?: 'Custom2'}</label></td>
								<td><input type="text" id="custom2" name="custom2"  value="${fileInstance?.custom2}"  /></td>
								<td class="label" nowrap="nowrap"><label for="custom3">${project.custom3 ?: 'Custom3'}</label></td>
								<td><input type="text" id="custom3" name="custom3"  value="${fileInstance?.custom3}"  /></td>
							</tr>
							<tr>
								<td class="label" nowrap="nowrap"><label for="custom4">${project.custom4 ?: 'Custom4'}</label></td>
								<td><input type="text" id="custom4" name="custom4"  value="${fileInstance?.custom4}"  /></td>
								<td class="label" nowrap="nowrap"><label for="custom5">${project.custom5 ?: 'Custom5'}</label></td>
								<td><input type="text" id="custom5" name="custom5"  value="${fileInstance?.custom5}"  /></td>
								<td class="label" nowrap="nowrap"><label for="custom6">${project.custom6 ?: 'Custom6'}</label></td>
								<td><input type="text" id="custom6" name="custom6"  value="${fileInstance?.custom6}"  /></td>
							</tr>
							<tr>
								<td class="label" nowrap="nowrap"><label for="custom7">${project.custom7 ?: 'Custom7'}</label></td>
								<td><input type="text" id="custom7" name="custom7"  value="${fileInstance?.custom7}"  /></td>
								<td class="label" nowrap="nowrap"><label for="custom8">${project.custom8 ?: 'Custom8'}</label></td>
								<td><input type="text" id="custom8" name="custom8"  value="${fileInstance?.custom8}"  /></td>
								<td class="label" nowrap="nowrap"><label for="planStatus">PlanStatus</label></td>
								<td><g:select from="${planStatusOptions}" id="planStatus"name="planStatus" value="${fileInstance.planStatus}" /></td>
							</tr>
						</tbody>
					</table>
				</div>
			</td>
		</tr>
		<tr>
			<td valign="top">
				<div style="width: auto;">
					<span style="float: left;"><h1>Supports:</h1></span>
					<span style="float: right;"><input type='button' value='Add' onclick="addAssetDependency('support')"></span>
					<br/>
					<table style="width: 100%;">
						<thead>
							<tr>
								<th>Frequency</th>
								<th>Entity Type</th>
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
				<div  style="width: auto;">
					<span style="float: left;"><h1>Is dependent on:</h1></span>
					<span style="float: right;"><input type='button' value='Add' onclick="addAssetDependency('dependent')"></span>
					<br/>
					<table style="width: 100%;">
						<thead>
							<tr>
								<th>Frequency</th>
								<th>Entity Type</th>
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
<script>
	currentMenuId = "#assetMenu";
	$("#assetMenuId a").css('background-color','#003366')
</script>
