<%@page import="com.tds.asset.Database"%>
<%-- <g:set var="assetClass" value="${(new Database()).assetClass}" /> --%>
<g:set var="assetClass" value="Database" />

<script type="text/javascript">
	$("#db_assetName").val($('#gs_assetName').val())
	$("#db_dbFormat").val($('#gs_dbFormat').val())
	$("#db_planStatus").val($('#gs_planStatus').val())
	$("#db_moveBundle").val($('#gs_moveBundle').val())
	
	$(document).ready(function() { 
		// Ajax to populate dependency selects in edit pages
		var assetId = '${databaseInstance.id}'
		populateDependency(assetId, 'database','edit')
		changeDocTitle('${escapedName}');
	})
</script>
<g:form method="post" action="update" name="createEditAssetForm">
	<input type="hidden" name="id" value="${databaseInstance?.id}" />
	<input type="hidden" id="db_assetName" name="assetNameFilter" value="" />
	<input type="hidden" id="db_dbFormat" name="dbFormatFilter" value="" />
	<input type="hidden" id="db_planStatus" name="planStatusFilter" value="" />
	<input type="hidden" id="db_moveBundle" name="moveBundleFilter" value="" />

	<input type="hidden" id="dbId" value ="${databaseInstance.id}"/>
	<input type="hidden" id="tabType" name="tabType" value =""/>
	<input type="hidden" name="updateView" id="updateView" value=""/>
		
	<input type="hidden" id="edit_supportAddedId" name="addedSupport" value ="0"/>
	<input type="hidden" id="edit_dependentAddedId" name="addedDep" value ="0"/>

	<%-- Key field and optimistic locking var --%>
	<input type="hidden" id="assetId" 	name="id" value="${assetId}"/>
	<input type="hidden" id="version" 	name="version" value="${version}"/>

	<%-- Used to track dependencies added and deleted --%>
	<g:render template="../assetEntity/dependentHidden" />


<table style="border: 0">
	<tr>
		<td colspan="2">
			<div class="dialog">
				<table>
					<tbody>
						<tr>
							<td class="label ${config.assetName} ${standardFieldSpecs.assetName.imp?:''}" nowrap="nowrap"><label for="assetType" data-toggle="popover" data-trigger="hover" data-content="${standardFieldSpecs.assetName.tip}">${standardFieldSpecs.assetName.label}<span style="color: red;">*</span></label></td>
							<td colspan="3" style="font-weight:bold;"><input type="text" id="assetName" class="${config.assetName}" name="assetName" value="${databaseInstance.assetName}" tabindex="11" /></td>
							<td class="label ${config.description} ${standardFieldSpecs.description.imp?:''}" nowrap="nowrap"><label for="description" data-toggle="popover" data-trigger="hover" data-content="${standardFieldSpecs.description.tip}">${standardFieldSpecs.description.label}</label></td>
							<td colspan="3"><input type="text" id="description" class="${config.description}" name="description" value="${databaseInstance.description}" size="50" tabindex="21" /></td>
						</tr>
						<tr>
							<td class="label" nowrap="nowrap"><label for="assetType" data-toggle="popover" data-trigger="hover" data-content="${standardFieldSpecs.assetType.tip}">${standardFieldSpecs.assetType.label}</label></td>
							<td><input type="text" id="assetType" name="assetType" readonly="readonly" value="${databaseInstance.assetType}" tabindex="12" /></td>
							<td class="label ${config.supportType} ${standardFieldSpecs.supportType.imp?:''}" nowrap="nowrap"><label for="supportType" data-toggle="popover" data-trigger="hover" data-content="${standardFieldSpecs.supportType.tip}">${standardFieldSpecs.supportType.label}</label></td>
							<td><input type="text" id="supportType" class="${config.supportType}" name="supportType" value="${databaseInstance.supportType}" tabindex="26" /></td>
							<td class="label ${config.environment} ${standardFieldSpecs.environment.imp?:''}" nowrap="nowrap"><label for="environment" data-toggle="popover" data-trigger="hover" data-content="${standardFieldSpecs.environment.tip}">${standardFieldSpecs.environment.label}</label></td>
							<td ><g:select id="environment" class="${config.environment}" name="environment" from="${environmentOptions}" value="${databaseInstance.environment}" noSelection="${['':' Please Select']}" tabindex="32" /></td>
						</tr>
						<tr>
							<td class="label ${config.dbFormat} ${standardFieldSpecs.dbFormat.imp?:''}" nowrap="nowrap"><label for="dbFormat" data-toggle="popover" data-trigger="hover" data-content="${standardFieldSpecs.dbFormat.tip}">${standardFieldSpecs.dbFormat.label}</label></td>
							<td><input type="text" id="dbFormat" class="${config.dbFormat}" name="dbFormat" value="${databaseInstance.dbFormat}" tabindex="13" /></td>
							<td class="label ${config.retireDate} ${standardFieldSpecs.retireDate.imp?:''}}"><label for="retireDate" data-toggle="popover" data-trigger="hover" data-content="${standardFieldSpecs.retireDate.tip}">${standardFieldSpecs.retireDate.label}</label></td>
							<td valign="top" class="value ${hasErrors(bean:databaseInstance,field:'retireDate','errors')}">
							    <script type="text/javascript" charset="utf-8">
									jQuery(function($){ $(".dateRange").kendoDatePicker({ animation: false, format:tdsCommon.kendoDateFormat() }); });
					            </script>
					            <input type="text" class="dateRange ${config.retireDate}" size="15" style="width: 138px;" name="retireDate" id="retireDate"
					                    value="<tds:convertDate date="${databaseInstance?.retireDate}" />" onchange="tdsCommon.isValidDate(this.value);" tabindex="27" >
							</td>
							<td class="label ${config.moveBundle} ${standardFieldSpecs.moveBundle.imp?:''}" nowrap="nowrap"><label for="moveBundle" data-toggle="popover" data-trigger="hover" data-content="${standardFieldSpecs.moveBundle.tip}">${standardFieldSpecs.moveBundle.label}</label></td>
							<td ><g:select from="${moveBundleList}" id="moveBundle" class="${config.moveBundle}" name="moveBundle.id" value="${databaseInstance?.moveBundle?.id}" optionKey="id" optionValue="name" tabindex="34" /></td>
						</tr>
						<tr>
							<td class="label ${config.size} ${standardFieldSpecs.scale.imp?:''}" nowrap="nowrap"><label for="size" data-toggle="popover" data-trigger="hover" data-content="${standardFieldSpecs.size.tip}">${standardFieldSpecs.size.label}/${standardFieldSpecs.scale.label}</label></td>
							<td nowrap="nowrap" class="sizeScale">
								<input type="text" id="size" size="3" class="${config.size}" name="size" value="${databaseInstance.size}" tabindex="14" />
								<g:select from="${databaseInstance.constraints.scale.inList}" class="${config.scale}" id="scale" name="scale" value="${databaseInstance.scale}" optionValue="value" tabindex="40" noSelection="${['':' Please Select']}"/>
							</td>
							<td  class="label ${config.maintExpDate} ${standardFieldSpecs.maintExpDate.imp?:''}"><label for="maintExpDate" data-toggle="popover" data-trigger="hover" data-content="${standardFieldSpecs.maintExpDate.tip}">${standardFieldSpecs.maintExpDate.label}</label></td>
							<td valign="top" class="value ${hasErrors(bean:databaseInstance,field:'maintExpDate','errors')}">
					           	<input type="text" class="dateRange ${config.maintExpDate}" size="15" style="width: 138px;" name="maintExpDate" id="maintExpDate"
					                    value="<tds:convertDate date="${databaseInstance?.maintExpDate}" />" onchange="tdsCommon.isValidDate(this.value);" tabindex="28" >
							</td>
							<td class="label ${config.planStatus} ${standardFieldSpecs.planStatus.imp?:''}" nowrap="nowrap"><label for="planStatus" data-toggle="popover" data-trigger="hover" data-content="${standardFieldSpecs.planStatus.tip}">${standardFieldSpecs.planStatus.label}</label></td>
							<td ><g:select from="${planStatusOptions}" id="planStatus" class="${config.planStatus}" name="planStatus" value="${databaseInstance.planStatus}" tabindex="35" /></td>
						</tr>
						<tr>
							<td class="label ${config.rateOfChange} ${standardFieldSpecs.rateOfChange.imp?:''}" nowrap="nowrap"><label for="rateOfChange" data-toggle="popover" data-trigger="hover" data-content="${standardFieldSpecs.rateOfChange.tip}">${standardFieldSpecs.rateOfChange.label}</label></td>
							<td><input type="text" id="rateOfChange" size="3" class="${config?.rateOfChange}" name="rateOfChange" value="${databaseInstance.rateOfChange}" tabindex="11" /></td>
							<td class="label ${config.externalRefId} ${standardFieldSpecs.externalRefId.imp?:''}" nowrap="nowrap"><label for="externalRefId" data-toggle="popover" data-trigger="hover" data-content="${standardFieldSpecs.externalRefId.tip}">${standardFieldSpecs.externalRefId.label}</label></td>
							<td><input type="text" id="externalRefId" class="${config.externalRefId}" name="externalRefId" value="${databaseInstance.externalRefId}" tabindex="11" /></td>
							<td class="label ${config.validation} ${standardFieldSpecs.validation.imp?:''}"><label for="validation" data-toggle="popover" data-trigger="hover" data-content="${standardFieldSpecs.validation.tip}">${standardFieldSpecs.validation.label}</label></td>
							<td ><g:select from="${databaseInstance.constraints.validation.inList}" id="validation" class="${config.validation}" name="validation" onChange="assetFieldImportance(this.value,'Database');highlightCssByValidation(this.value,'Database','${databaseInstance.id}');" value="${databaseInstance.validation}"/></td>
						</tr>
						<tbody class="customTemplate">
							<g:render template="../assetEntity/customEdit" model="[assetEntityInstance:databaseInstance]"></g:render>
						</tbody>
					</tbody>
				</table>
			</div>
		</td>
	</tr>
	<tr id="databaseDependentId" class="assetDependent">
		<td class="depSpin"><span><img alt="" src="${resource(dir:'images',file:'processing.gif')}"/> </span></td>
	</tr>
	<tr>
		<td colspan="2">
			<div class="buttons">
				<g:render template="../assetEntity/editButtons" model="[assetEntity:databaseInstance]"></g:render>
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
	$('#tabType').val($('#assetTypesId').val());
</script>
