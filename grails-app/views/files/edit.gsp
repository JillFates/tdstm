<script type="text/javascript">
	$("#file_assetName").val($('#gs_assetName').val())
	$("#file_fileFormat").val($('#gs_fileFormat').val())
	$("#file_size").val($('#gs_size').val())
	$("#file_planStatus").val($('#gs_planStatus').val())
	$("#file_moveBundle").val($('#gs_moveBundle').val())
	
	$(document).ready(function() { 
		// Ajax to populate dependency selects in edit pages
		var assetId = '${fileInstance.id}'
		populateDependency(assetId,'files','edit')
		
		changeDocTitle('${fileInstance.assetName}');
	})
</script>
<g:form method="post" action="update" name="editAssetsFormId">
	<input type="hidden" id="file_assetName" name="assetNameFilter" value="" />
	<input type="hidden" id="file_fileFormat" name="fileFormatFilter" value="" />
	<input type="hidden" id="file_size" name="sizeFilter" value="" />
	<input type="hidden" id="file_planStatus" name="planStatusFilter" value="" />
	<input type="hidden" id="file_moveBundle" name="moveBundleFilter" value="" />
	<input type="hidden" name="id" value="${fileInstance?.id}" />
	<table style="border: 0;">
		<tr>
			<td colspan="2">
				<div class="dialog">
					<table>
						<tbody>
							<tr>
								<td class="label ${config.assetName}" nowrap="nowrap"><label for="assetName">Name<span style="color: red;">*</span></label>
								</td>
								<td><input type="text" id="assetName" class="${config.assetName}" name="assetName"
									value="${fileInstance.assetName}" />
								</td>
								<td class="label ${config.description}" nowrap="nowrap"><label for="description">Description</label></td>
								<td colspan="2"><input type="text" id="description" class="${config.description}"
									name="description"
									value="${fileInstance.description}" size="50" />
								</td>
								<td></td>
								<td class="label ${config.moveBundle}" nowrap="nowrap"><label for="moveBundle">Bundle</label></td>
								<td><g:select from="${moveBundleList}" id="moveBundle" class="${config.moveBundle}" name="moveBundle.id" value="${fileInstance?.moveBundle?.id}" optionKey="id" optionValue="name" tabindex="34" />
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
								<td class="label ${config.supportType}" nowrap="nowrap"><label for="supportType">Support</label></td>
								<td><input type="text" id="supportType" name="supportType" class="${config.supportType}"
									value="${fileInstance.supportType}" /></td>
								<td class="label ${config.planStatus}" nowrap="nowrap"><label for="planStatus">PlanStatus</label></td>
								<td><g:select from="${planStatusOptions}" id="planStatus" class="${config.planStatus}" name="planStatus" value="${fileInstance.planStatus}" />
								</td>
							</tr>

							<tr>
								<td class="label ${config.fileFormat}" nowrap="nowrap"><label for="fileFormat">
										Format<span style="color: red;">*</span></label>
								</td>
								<td><input type="text" id="fileFormat" class="${config.fileFormat}" name="fileFormat"
									value="${fileInstance.fileFormat}" /></td>
								<td class="label ${config.environment}" nowrap="nowrap"><label for="environment">Environment</label>
								</td>
								<td><g:select id="environment" class="${config.environment}" name="environment" from="${environmentOptions}" value="${fileInstance.environment}" noSelection="${['':' Please Select']}" />
								</td>
								<td class="label ${config.externalRefId}" nowrap="nowrap"><label for="externalRefId">External Ref Id</label></td>
								<td><input type="text" id="externalRefId" class="${config.externalRefId}" name="externalRefId" value="${fileInstance.externalRefId}" tabindex="11" /></td>
								<td class="label ${config.validation}"><label for="validation">Validation</label></td>
							<td><g:select from="${fileInstance.constraints.validation.inList}" id="validation" class="${config.validation}" name="validation" onChange="assetFieldImportance(this.value,'Files')" value="${fileInstance.validation}"/>	
							</tr>
							<tr>
								<td class="label ${config.size}" nowrap="nowrap"><label for="size">Size<span style="color: red;">*</span></label>
								</td>
								<td nowrap="nowrap" class="sizeScale">
									<input type="text" id="size" name="size" class="${config.size}" value="${fileInstance.size}" size="10"/>&nbsp;
									<g:select from="${fileInstance.constraints.scale.inList}" class="${config.scale}" name="scale" id="scale" value="${fileInstance.scale}" optionValue="value" noSelection="${['':' Please Select']}"/>
								</td>
								<td class="label ${config.rateOfChange}" nowrap="nowrap"><label for="rateOfChange">Rate of Change (%)</label></td>
								<td><input type="text" class="${config.rateOfChange}" size="3" name="rateOfChange" id="rateOfChange" value="${fileInstance.rateOfChange}"></td>
							</tr>
							<tbody class="customTemplate">
							 	<g:render template="../assetEntity/customEdit" model="[assetEntityInstance:fileInstance]"></g:render>
							</tbody>
						</tbody>
					</table>
				</div>
			</td>
		</tr>
		<tr id="filesDependentId" class="assetDependent">
			<td class="depSpin"><span><img alt="" src="${resource(dir:'images',file:'processing.gif')}"/> </span></td>
		</tr>
		<tr>
			<td colspan="2">
				<div class="buttons">
				<input name="dependentCount" id="edit_dependentCount" type="hidden" value="${dependentAssets.size()}">
					<input  name="supportCount"  id="edit_supportCount" type="hidden" value="${supportAssets.size()}">
					<input name="redirectTo" type="hidden" value="${redirectTo}">
					<input type = "hidden" id ="filesId"  value ="${fileInstance.id}"/>
					<input type = "hidden" id = "tabType" name="tabType" value =""/>
					<input name="updateView" id="updateView" type="hidden" value=""/>
					<input type="hidden" id="deletedDepId" name="deletedDep" value =""/>
					
					<input type="hidden" id="edit_supportAddedId" name="addedSupport" value ="0"/>
					<input type="hidden" id="edit_dependentAddedId" name="addedDep" value ="0"/>
					<g:render template="../assetEntity/editButtons" model="[redirectTo:redirectTo,value:fileInstance.id,whom:'files']"></g:render>
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
