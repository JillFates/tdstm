<g:form method="post" action="update" name="editAssetsFormId">
	<input type="hidden" name="id" value="${fileInstance?.id}" />
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
									value="Storage"  readonly="readonly"/></td>
								<td class="label" nowrap="nowrap"><label for="lun">LUN</label>
								</td>
								<td><input type="text" id="lun" name="LUN"
									value="${fileInstance.LUN}" />
								</td>
								<td class="label" nowrap="nowrap"><label for="supportType">Support</label></td>
								<td><input type="text" id="supportType" name="supportType"
									value="${fileInstance.supportType}" /></td>
							</tr>

							<tr>
								<td class="label" nowrap="nowrap"><label for="fileFormat">
										Format<span style="color: red;">*</span></label>
								</td>
								<td><input type="text" id="fileFormat" name="fileFormat"
									value="${fileInstance.fileFormat}" /></td>
								<td class="label" nowrap="nowrap"><label for="environment">Environment</label>
								</td>
								<td><g:select id="environment" name="environment" from="${com.tds.asset.AssetEntity.constraints.environment.inList}" value="${fileInstance.environment}" />
								</td>
								<td class="label" nowrap="nowrap"><label for="fileSize">Size<span style="color: red;">*</span></label>
								</td>
								<td><input type="text" id="fileSize" name="fileSize" value="${fileInstance.fileSize}" size="10"/>&nbsp;
								<g:select from="${com.tds.asset.Files.constraints.sizeUnit.inList}" name="sizeUnit" id="sizeUnit" value="${fileInstance.sizeUnit}"/>	
								</td>
							</tr>
							<tr>
							   <td class="label" nowrap="nowrap"><label for="moveBundle">Bundle</label>
								</td>
								<td><g:select from="${moveBundleList}" id="moveBundle"
										name="moveBundle.id" value="${fileInstance.moveBundle?.id}"
										optionKey="id" optionValue="name" />
								</td>
								<td class="label" nowrap="nowrap"><label for="custom1">${fileInstance.project.custom1 ?: 'Custom1'}</label></td>
								<td><input type="text" id="custom1" name="custom1"  value="${fileInstance?.custom1}"  /></td>
								<td class="label" nowrap="nowrap"><label for="custom1">${fileInstance.project.custom2 ?: 'Custom2'}</label></td>
								<td><input type="text" id="custom2" name="custom2"  value="${fileInstance?.custom2}"  /></td>
							</tr>
							<tr>
								<td class="label" nowrap="nowrap"><label for="custom3">${fileInstance.project.custom3 ?: 'Custom3'}</label></td>
								<td><input type="text" id="custom3" name="custom3"  value="${fileInstance?.custom3}"  /></td>
								<td class="label" nowrap="nowrap"><label for="custom4">${fileInstance.project.custom4 ?: 'Custom4'}</label></td>
								<td><input type="text" id="custom4" name="custom4"  value="${fileInstance?.custom4}"  /></td>
								<td class="label" nowrap="nowrap"><label for="custom5">${fileInstance.project.custom5 ?: 'Custom5'}</label></td>
								<td><input type="text" id="custom5" name="custom5"  value="${fileInstance?.custom5}"  /></td>
							</tr>
							<tr>
								<td class="label" nowrap="nowrap"><label for="custom6">${fileInstance.project.custom6 ?: 'Custom6'}</label></td>
								<td><input type="text" id="custom6" name="custom6"  value="${fileInstance?.custom6}"  /></td>
								<td class="label" nowrap="nowrap"><label for="custom7">${fileInstance.project.custom7 ?: 'Custom7'}</label></td>
								<td><input type="text" id="custom7" name="custom7"  value="${fileInstance?.custom7}"  /></td>
								<td class="label" nowrap="nowrap"><label for="custom8">${fileInstance.project.custom8 ?: 'Custom8'}</label></td>
								<td><input type="text" id="custom8" name="custom8"  value="${fileInstance?.custom8}"  /></td>
							</tr>
							<tr>
							<td class="label" nowrap="nowrap"><label for="planStatus">PlanStatus</label></td>
							<td><g:select from="${planStatusOptions}" id="planStatus" name="planStatus" value="${fileInstance.planStatus}" />
							</td>
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
								<th>Type</th>
								<th>Name</th>
								<th>Type</th>
								<th>Status</th>
								<th>&nbsp;</th>
							</tr>
						</thead>
						<tbody id="createSupportsList">
							<g:each in="${supportAssets}" var="support" status="i">
								<tr id='row_s_${i}'>
									<td><g:select name="dataFlowFreq_support_${i}" value="${support.dataFlowFreq}" from="${support.constraints.dataFlowFreq.inList}" /></td>
									<td><g:select name="entity_support_${i}" from="['Server','Application','Database','Storage']" onchange='updateAssetsList(this.name, this.value)' value="${support?.asset?.assetType == 'Files' ? 'Storage' : support?.asset?.assetType}"></g:select></td>
									<g:if test="${support?.asset.assetType=='Server'|| support?.asset.assetType=='Blade' || support?.asset.assetType=='VM'}">
								        <td class="dep-${support.status}"><g:select name="asset_support_${i}" from="${com.tds.asset.AssetEntity.findAll('from AssetEntity where assetType in (\'Server\',\'VM\',\'Blade\') and project = ? order by assetName asc ',[project])}" value="${support?.asset?.id}" optionKey="id" optionValue="assetName"  style="width:105px;"></g:select></td>
									</g:if>
									<g:else>
										<td class="dep-${support.status}"><g:select name="asset_support_${i}" from="${com.tds.asset.AssetEntity.findAll('from AssetEntity where assetType = ? and project =? order by assetName asc',[support?.asset?.assetType, project])}" value="${support?.asset?.id}" optionKey="id" optionValue="assetName"  style="width:105px;"></g:select></td>
									</g:else>
									<td><g:select name="dtype_support_${i}" value="${support.type}" from="${dependencyType.value}" optionValue="value"  />
									</td>
									<td><g:select name="status_support_${i}" value="${support.status}" from="${dependencyStatus.value}" optionValue="value" />
									</td>
									<td><a href="javascript:deleteRow('row_s_${i}')"><span class='clear_filter'><u>X</u></span></a></td>
								</tr>
							</g:each>
						</tbody>
					</table>
				</div></td>
			<td valign="top">
				<div style="width: auto;">
					<span style="float: left;"><h1>Is dependent on:</h1></span>
					<span style="float: right;"><input type='button' value='Add' onclick="addAssetDependency('dependent')"></span>
					<br/>
					<table style="width: 100%;">
						<thead>
							<tr>
								<th>Frequency</th>
								<th>Type</th>
								<th>Name</th>
								<th>Type</th>
								<th>Status</th>
								<th>&nbsp;</th>
							</tr>
						</thead>
						<tbody id="createDependentsList">
						<g:each in="${dependentAssets}" var="dependent" status="i">
							<tr id='row_d_${i}'>
								<td><g:select name="dataFlowFreq_dependent_${i}" value="${dependent.dataFlowFreq}" from="${dependent.constraints.dataFlowFreq.inList}" /></td>
								<td><g:select name="entity_dependent_${i}" from="['Server','Application','Database','Storage']" onchange='updateAssetsList(this.name, this.value)' value="${dependent?.dependent?.assetType == 'Files' ? 'Storage' : dependent?.dependent?.assetType}"></g:select></td>
								<g:if test="${dependent?.dependent?.assetType=='Server'|| dependent?.dependent?.assetType=='Blade' || dependent?.dependent?.assetType=='VM'}">
								  <td><g:select name="asset_dependent_${i}" from="${com.tds.asset.AssetEntity.findAll('from AssetEntity where assetType in (\'Server\',\'VM\',\'Blade\') and project = ? order by assetName asc ',[project])}" value="${dependent?.dependent?.id}" optionKey="id" optionValue="assetName"  style="width:105px;"></g:select></td>
								</g:if>
								<g:else>
								  <td><g:select name="asset_dependent_${i}" from="${com.tds.asset.AssetEntity.findAll('from AssetEntity where assetType = ? and project = ? order by assetName asc ',[dependent?.dependent?.assetType, project])}" value="${dependent?.dependent?.id}" optionKey="id" optionValue="assetName"  style="width:105px;"></g:select></td>
								</g:else>
								<td><g:select name="dtype_dependent_${i}" value="${dependent.type}" from="${dependencyType.value}" optionValue="value" />
								</td>
								<td><g:select name="status_dependent_${i}" value="${dependent.status}" from="${dependencyStatus.value}" optionValue="value" />
								</td>
								<td><a href="javascript:deleteRow('row_d_${i}')"><span class='clear_filter'><u>X</u></span></a></td>
							</tr>
						</g:each>
						</tbody>
					</table>
				</div></td>
		</tr>
		<tr>
			<td colspan="2">
				<div class="buttons">
				<input name="dependentCount" id="dependentCount" type="hidden" value="${dependentAssets.size()}">
					<input  name="supportCount"  id="supportCount" type="hidden" value="${supportAssets.size()}">
					<input name="redirectTo" type="hidden" value="${redirectTo}">
					<input type = "hidden" id ="filesId"  value ="${fileInstance.id}"/>
					<input type = "hidden" id = "tabType" name="tabType" value =""/>
					<input name="updateView" id="updateView" type="hidden" value=""/>
					<g:if test="${redirectTo!='planningConsole'}">
					  <span class="button"><g:actionSubmit class="save" value="Update/Close" action="Update" /> </span>
					  <span class="button"><input type="button" class="save" value="Update/View" onclick="updateToShow()" /> </span>
					  <span class="button"><g:actionSubmit class="delete"	onclick=" return confirm('Are you sure?');" value="Delete" /> </span>
					</g:if>
					<g:else>
					  <span class="button"><input id="updatedId" name="updatedId" type="button" class="save" value="Update/Close" onclick="submitRemoteForm()"> </span>
					  <span class="button"><input type="button" class="save" value="Update/View" onclick="updateToShow()" /> </span>
					  <span class="button"><input id="deleteId"	 name="deleteId"  class="save" value="Delete" onclick=" deleteAsset($('#filesId').val(),'files')" value="Delete" /> </span>
					</g:else>
				</div>
			</td>
		</tr>
	</table>
</g:form>
<script>
	currentMenuId = "#assetMenu";
	$("#assetMenuId a").css('background-color','#003366')
	$('#tabType').val($('#assetTypesId').val())
</script>
