<script type="text/javascript">
	$("#file_assetName").val($('#gs_assetName').val())
	$("#file_fileFormat").val($('#gs_fileFormat').val())
	$("#file_fileSize").val($('#gs_fileSize').val())
	$("#file_planStatus").val($('#gs_planStatus').val())
	$("#file_moveBundle").val($('#gs_moveBundle').val())
	
	$(document).ready(function() { 
		// Ajax to populate dependency selects in edit pages
		var assetId = '${fileInstance.id}'
		populateDependency(assetId,'files')
	})
</script>
<g:form method="post" action="update" name="editAssetsFormId">
	<input type="hidden" id="file_assetName" name="assetNameFilter" value="" />
	<input type="hidden" id="file_fileFormat" name="fileFormatFilter" value="" />
	<input type="hidden" id="file_fileSize" name="fileSizeFilter" value="" />
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
								<td><g:select id="environment" class="${config.environment}" name="environment" from="${com.tds.asset.AssetEntity.constraints.environment.inList}" value="${fileInstance.environment}" />
								</td>
								<td class="label ${config.fileSize}" nowrap="nowrap"><label for="fileSize">Size<span style="color: red;">*</span></label>
								</td>
								<td><input type="text" id="fileSize" name="fileSize" class="${config.fileSize}" value="${fileInstance.fileSize}" size="10"/>&nbsp;
								<g:select from="${com.tds.asset.Files.constraints.sizeUnit.inList}" name="sizeUnit" id="sizeUnit" value="${fileInstance.sizeUnit}"/>
								</td>
								<td class="label ${config.validation}"><label for="validation">Validation</label></td>
							<td><g:select from="${fileInstance.constraints.validation.inList}" id="validation" class="${config.validation}" name="validation" onChange="assetFieldImportance(this.value,'Files')" value="${fileInstance.validation}"/>	
							</tr>
							<tr>
								<td class="label ${config.externalRefId}" nowrap="nowrap"><label for="externalRefId">External Ref Id</label></td>
								<td><input type="text" id="externalRefId" class="${config.externalRefId}" name="externalRefId" value="${fileInstance.externalRefId}" tabindex="11" /></td>
							</tr>
							<tbody class="customTemplate">
							 	<g:render template="../assetEntity/customEdit" model="[assetEntityInstance:fileInstance]"></g:render>
							</tbody>
						</tbody>
					</table>
				</div>
			</td>
		</tr>
		<tr id="filesDependentId">
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
					<g:if test="${redirectTo!='dependencyConsole'}">
					  <g:if test="${redirectTo=='listTask'}">
					  	<span class="button"><input type="button" class="save updateDep" value="Update/Close" onclick="updateToRefresh()" /></span>
					  </g:if>
					  <g:else>
					  	<span class="button"><g:actionSubmit class="save updateDep" value="Update/Close" action="Update" /></span>
					  </g:else>
					  <span class="button"><input type="button" class="save updateDep" value="Update/View" onclick="updateToShow()" /> </span>
					  <span class="button"><g:actionSubmit class="delete"	onclick=" return confirm('Are you sure?');" value="Delete" /> </span>
					</g:if>
					<g:else>
					  <span class="button"><input id="updatedId" name="updatedId" type="button" class="save updateDep" value="Update/Close" onclick="submitRemoteForm()"> </span>
					  <span class="button"><input type="button" class="save updateDep" value="Update/View" onclick="updateToShow()" /> </span>
					  <span class="button"><input id="deleteId"	 name="deleteId"  class="delete" value="Delete" onclick=" deleteAsset($('#filesId').val(),'files')" value="Delete" /> </span>
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
