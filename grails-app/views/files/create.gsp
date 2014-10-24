<script type="text/javascript">
	$("#file_assetName").val($('#gs_assetName').val())
	$("#file_fileFormat").val($('#gs_fileFormat').val())
	$("#file_size").val($('#gs_size').val())
	$("#file_planStatus").val($('#gs_planStatus').val())
	$("#file_moveBundle").val($('#gs_moveBundle').val())
</script>

<g:form method="post" action="save" name="createEditAssetForm" onsubmit="return validateFileFormat()">
	<input type="hidden" id="file_assetName" name="assetNameFilter" value="" />
	<input type="hidden" id="file_fileFormat" name="fileFormatFilter" value="" />
	<input type="hidden" id="file_size" name="sizeFilter" value="" />
	<input type="hidden" id="file_planStatus" name="planStatusFilter" value="" />
	<input type="hidden" id="file_moveBundle" name="moveBundleFilter" value="" />

	<input name="showView" id="showView" type="hidden" value=""/>

	<%-- Used to track dependencies added and deleted --%>
	<g:render template="../assetEntity/dependentHidden" />

	<table style="border: 0;">
		<tr>
			<td colspan="2">
				<div class="dialog">
					<table>
						<tbody>
							<tr>
								<td class="label ${config.assetName} ${highlightMap.assetName?:''}" nowrap="nowrap"><label for="assetName">Name<span style="color: red;">*</span></label>
								</td>
								<td><input type="text" id="assetName" class="${config.assetName}" name="assetName"
									value="${fileInstance.assetName}" />
								</td>
								<td class="label ${config.description} ${highlightMap.description?:''}" nowrap="nowrap"><label for="description">Description</label></td>
								<td colspan="2"><input type="text" id="description" class="${config.description}"
									name="description"
									value="${fileInstance.description}" size="50" />
								</td>
								<td>&nbsp;</td>
								<td class="label ${config.moveBundle} ${highlightMap.moveBundle?:''}" nowrap="nowrap"><label for="moveBundle">Bundle</label></td>
								<td><g:select from="${moveBundleList}" id="moveBundle" class="${config.moveBundle}" name="moveBundle.id" value="${project.defaultBundle.id}" optionKey="id" optionValue="name" tabindex="34" />
								</td>
							</tr>

							<tr>
								<td class="label" nowrap="nowrap"><label for="assetType">
										Class</label>
								</td>
								<td><input type="text" id="assetType" name="assetType" 
									value="Logical Storage" readonly="readonly" />
								</td>
								<td class="label" nowrap="nowrap"><label for="lun">LUN</label>
								</td>
								<td><input type="text" id="lun" name="LUN" 
									value="${fileInstance.LUN}"/> 
								</td>
								<td class="label ${config.supportType} ${highlightMap.supportType?:''}" nowrap="nowrap"><label for="supportType">Support</label>
								</td>
								<td><input type="text" id="supportType" class="${config.supportType}" name="supportType"
									value="${fileInstance.supportType}" /></td>
								<td class="label ${config.planStatus} ${highlightMap.planStatus?:''}" nowrap="nowrap"><label for="planStatus">PlanStatus</label></td>
								<td>
								<g:select from="${planStatusOptions}" id="planStatus" class="${config.planStatus}" name="planStatus" value="${fileInstance.planStatus}" /></td>
							</tr>

							<tr>
								<td class="label ${config.fileFormat} ${highlightMap.fileFormat?:''}" nowrap="nowrap"><label for="fileFormat">Format<span style="color: red;">*</span></label>
								</td>
								<td><input type="text" id="fileFormat" class="${config.fileFormat}" name="fileFormat"
									value="${fileInstance.fileFormat}" />
								</td>
								<td class="label ${config.environment} ${highlightMap.environment?:''}" nowrap="nowrap"><label for="environment">Environment
								</label>
								</td>
								<td><g:select id="environment" class="${config.environment}" name="environment" from="${environmentOptions}" noSelection="${['':' Please Select']}"/>
								</td>
								<td class="label ${config.externalRefId} ${highlightMap.externalRefId?:''}" nowrap="nowrap"><label for="externalRefId">External Ref Id</label></td>
								<td><input type="text" id="externalRefId" class="${config.externalRefId}" name="externalRefId" value="${fileInstance.externalRefId}" tabindex="11" /></td>
								<td class="label ${config.validation} ${highlightMap.validation?:''}"><label for="validation">Validation</label></td>
								<td>
									<g:select from="${fileInstance.constraints.validation.inList}" class="${config.validation}" id="validation" name="validation"  onChange="assetFieldImportance(this.value,'Files');highlightCssByValidation(this.value,'Files','');" value="Discovery"/>
								</td>
							</tr>
							<tr><td class="label ${config.size} ${highlightMap.size?:''}" nowrap="nowrap"><label for="size">Size<span style="color: red;">*</span>
								</label>
								</td>
								<td nowrap="nowrap" class="sizeScale">
									<input type="text" id="size" class="${config.size}" name="size" size="10"
									value="${fileInstance.size}" /> &nbsp;
									<g:select from="${fileInstance.constraints.scale.inList}" class="${config.scale}" optionValue="value" name="scale" id="scale" value="GB"/>
								</td>
							<td class="label ${config.rateOfChange} ${highlightMap.rateOfChange?:''}" nowrap="nowrap"><label for="rateOfChange">Rate of Change (%)</label></td>
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
		<tr>
			<g:render template="../assetEntity/dependent" model="[whom:'create',supportAssets:[],dependentAssets:[]]"></g:render>
		</tr>
		<tr>
			<td colspan="2">
				<g:render template="../assetEntity/createButtons" model="[assetClass: fileInstance.assetClass]"></g:render>
			</td>
		</tr>
	</table>
</g:form>
<script>
	currentMenuId = "#assetMenu";
	$("#assetMenuId a").css('background-color','#003366')
	
</script>
