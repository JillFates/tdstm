<%@page import="com.tds.asset.Files"%>
<%-- <g:set var="assetClass" value="${(new Files()).assetClass}" /> --%>
<g:set var="assetClass" value="Files" />

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

		changeDocTitle('${escapedName}');
	})
</script>
<g:form method="post" action="update" name="createEditAssetForm">
	<input type="hidden" id="file_assetName" name="assetNameFilter" value="" />
	<input type="hidden" id="file_fileFormat" name="fileFormatFilter" value="" />
	<input type="hidden" id="file_size" name="sizeFilter" value="" />
	<input type="hidden" id="file_planStatus" name="planStatusFilter" value="" />
	<input type="hidden" id="file_moveBundle" name="moveBundleFilter" value="" />
	<input type="hidden" name="id" value="${fileInstance?.id}" />

	<input type="hidden" id ="filesId"  value ="${fileInstance.id}"/>
	<input type="hidden" id = "tabType" name="tabType" value =""/>
	<input name="updateView" id="updateView" type="hidden" value=""/>

	<%-- Key field and optimistic locking var --%>
	<input type="hidden" id="assetId" 	name="id" value="${assetId}"/>
	<input type="hidden" id="version" 	name="version" value="${version}"/>

	<%-- Used to track dependencies added and deleted --%>
	<g:render template="../assetEntity/dependentHidden" />

	<table style="border: 0;">
		<tr>
			<td colspan="2">
				<div class="dialog">
					<table>
						<tbody>
						<tr>
							<td class="label ${config.assetName} ${standardFieldSpecs.assetName.imp?:''}" nowrap="nowrap"><label for="assetName" data-toggle="popover" data-trigger="hover" data-content="${standardFieldSpecs.assetName.tip}">${standardFieldSpecs.assetName.label}<span style="color: red;">*</span></label>
							</td>
							<td colspan="3"><input type="text" id="assetName" class="${config.assetName}" name="assetName"
												   value="${fileInstance.assetName}" />
							</td>
							<td class="label ${config.description} ${standardFieldSpecs.description.imp?:''}" nowrap="nowrap"><label for="description" data-toggle="popover" data-trigger="hover" data-content="${standardFieldSpecs.description.tip}">${standardFieldSpecs.description.label}</label></td>
							<td colspan="3"><input type="text" id="description" class="${config.description}"
												   name="description"
												   value="${fileInstance.description}" size="50" />
							</td>

						</tr>
						<tr>
							<td class="label" nowrap="nowrap"><label for="assetType" data-toggle="popover" data-trigger="hover" data-content="${standardFieldSpecs.assetType.tip}">
								${standardFieldSpecs.assetType.label}</label>
							</td>
							<td><input type="text" id="assetType" name="assetType"
									   value="Logical Storage"  readonly="readonly"/></td>
							<td class="label" nowrap="nowrap"><label for="LUN" data-toggle="popover" data-trigger="hover" data-content="${standardFieldSpecs.LUN.tip}">${standardFieldSpecs.LUN.label}</label>
							</td>
							<td><input type="text" id="lun" name="LUN"
									   value="${fileInstance.LUN}" />
							</td>
							<td colspan="2"></td>
							<td class="label ${config.moveBundle} ${standardFieldSpecs.moveBundle.imp?:''}" nowrap="nowrap"><label for="moveBundle" data-toggle="popover" data-trigger="hover" data-content="${standardFieldSpecs.moveBundle.tip}">${standardFieldSpecs.moveBundle.label}</label></td>
							<td><g:select from="${moveBundleList}" id="moveBundle" class="${config.moveBundle}" name="moveBundle.id" value="${fileInstance?.moveBundle?.id}" optionKey="id" optionValue="name" tabindex="34" />
							</td>
						</tr>

						<tr>
							<td class="label ${config.fileFormat} ${standardFieldSpecs.fileFormat.imp?:''}" nowrap="nowrap"><label for="fileFormat" data-toggle="popover" data-trigger="hover" data-content="${standardFieldSpecs.fileFormat.tip}">
								${standardFieldSpecs.fileFormat.label}</label>
							</td>
							<td><input type="text" id="fileFormat" class="${config.fileFormat}" name="fileFormat"
									   value="${fileInstance.fileFormat}" /></td>
							<td class="label ${config.environment} ${standardFieldSpecs.environment.imp?:''}" nowrap="nowrap"><label for="environment" data-toggle="popover" data-trigger="hover" data-content="${standardFieldSpecs.environment.tip}">${standardFieldSpecs.environment.label}</label>
							</td>
							<td><g:select id="environment" class="${config.environment}" name="environment" from="${environmentOptions}" value="${fileInstance.environment}" noSelection="${['':' Please Select']}" />
							</td>
							<td class="label ${config.supportType} ${standardFieldSpecs.supportType.imp?:''}" nowrap="nowrap" ><label for="supportType" data-toggle="popover" data-trigger="hover" data-content="${standardFieldSpecs.supportType.tip}">${standardFieldSpecs.supportType.label}</label></td>
							<td><input type="text" id="supportType" name="supportType" class="${config.supportType}"
									   value="${fileInstance.supportType}" /></td>
							<td class="label ${config.planStatus} ${standardFieldSpecs.planStatus.imp?:''}" nowrap="nowrap"><label for="planStatus" data-toggle="popover" data-trigger="hover" data-content="${standardFieldSpecs.planStatus.tip}">${standardFieldSpecs.planStatus.label}</label></td>
							<td><g:select from="${planStatusOptions}" id="planStatus" class="${config.planStatus}" name="planStatus" value="${fileInstance.planStatus}" />
							</td>
						</tr>
						<tr>
							<td class="label ${config.size} ${standardFieldSpecs.size.imp?:''}" nowrap="nowrap"><label for="size" data-toggle="popover" data-trigger="hover" data-content="${standardFieldSpecs.size.tip}">${standardFieldSpecs.size.label}</label>
							</td>
							<td nowrap="nowrap" class="sizeScale">
								<input type="text" id="size" name="size" class="${config.size}" value="${fileInstance.size}" size="10"/>&nbsp;
								<g:select from="${fileInstance.constraints.scale.inList}" class="${config.scale}" name="scale" id="scale" value="${fileInstance.scale}" optionValue="value" noSelection="${['':' Please Select']}"/>
							</td>
							<td class="label ${config.rateOfChange} ${standardFieldSpecs.rateOfChange.imp?:''}" nowrap="nowrap"><label for="rateOfChange" data-toggle="popover" data-trigger="hover" data-content="${standardFieldSpecs.rateOfChange.tip}">${standardFieldSpecs.rateOfChange.label}</label></td>
							<td><input type="text" class="${config.rateOfChange}" size="3" name="rateOfChange" id="rateOfChange" value="${fileInstance.rateOfChange}"></td>

							<td class="label ${config.externalRefId} ${standardFieldSpecs.externalRefId.imp?:''}" nowrap="nowrap"><label for="externalRefId" data-toggle="popover" data-trigger="hover" data-content="${standardFieldSpecs.externalRefId.tip}">${standardFieldSpecs.externalRefId.label}</label></td>
							<td><input type="text" id="externalRefId" class="${config.externalRefId}" name="externalRefId" value="${fileInstance.externalRefId}" tabindex="11" /></td>
							<td class="label ${config.validation} ${standardFieldSpecs.validation.imp?:''}"><label for="validation" data-toggle="popover" data-trigger="hover" data-content="${standardFieldSpecs.validation.tip}">${standardFieldSpecs.validation.label}</label></td>
							<td><g:select from="${fileInstance.constraints.validation.inList}" id="validation" class="${config.validation}" name="validation" onChange="assetFieldImportance(this.value,'Files');highlightCssByValidation(this.value,'Files','${fileInstance.id}');" value="${fileInstance.validation}"/>
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
					<g:render template="../assetEntity/editButtons" model="[assetEntity:fileInstance]"></g:render>
				</div>
			</td>
		</tr>
	</table>
</g:form>
<script>
    $(document).ready(function() {
        $('[data-toggle="popover"]').popover();
    });
	currentMenuId = "#assetMenu";
	$("#assetMenuId a").css('background-color','#003366')
	$('#tabType').val($('#assetTypesId').val())
</script>
