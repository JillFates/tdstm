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
							<tds:inputLabel field="${standardFieldSpecs.assetName}" value="${databaseInstance?.assetName}"/>
							<td colspan="3" style="font-weight:bold;">
								<tds:inputControl field="${standardFieldSpecs.assetName}" tabindex="11" value="${databaseInstance.assetName}"/>
							</td>

							<tds:inputLabel field="${standardFieldSpecs.description}" value="${databaseInstance?.description}"/>
							<td colspan="3">
								<tds:inputControl field="${standardFieldSpecs.description}" size="50" tabindex="12" value="${databaseInstance.description}"/>
							</td>
						</tr>
						<tr>
							<tds:inputLabel field="${standardFieldSpecs.assetType}" value="${databaseInstance?.assetType}"/>
							<td>
								<input type="text" id="assetType" name="assetType" readonly="readonly" value="${databaseInstance.assetType}" tabindex="12" />
							</td>

							<tds:inputLabelAndField field="${standardFieldSpecs.supportType}" value="${databaseInstance?.supportType}"/>

							<tds:inputLabel field="${standardFieldSpecs.environment}" value="${databaseInstance?.environment}"/>
							<td>
								<g:select id="environment" name="environment" from="${environmentOptions}" value="${databaseInstance.environment}" noSelection="${['':' Please Seb lect']}" tabindex="32" />
							</td>
						</tr>
						<tr>
							<tds:inputLabelAndField field="${standardFieldSpecs.dbFormat}" value="${databaseInstance.dbFormat}" tabindex="13"/>

							<tds:inputLabel field="${standardFieldSpecs.retireDate}" value="${databaseInstance?.retireDate}"/>
							<td valign="top" class="value ${hasErrors(bean:databaseInstance,field:'retireDate','errors')}">
							    <script type="text/javascript" charset="utf-8">
									jQuery(function($){ $(".dateRange").kendoDatePicker({ animation: false, format:tdsCommon.kendoDateFormat() }); });
					            </script>
					            <input type="text" class="dateRange" size="15" style="width: 138px;" name="retireDate" id="retireDate"
					                value="<tds:convertDate date="${databaseInstance?.retireDate}" />" onchange="tdsCommon.isValidDate(this.value);" tabindex="22" >
							</td>

							<tds:inputLabel field="${standardFieldSpecs.moveBundle}" value="${databaseInstance?.moveBundle}"/>
							<td>
								<g:select from="${moveBundleList}" id="moveBundle" name="moveBundle.id" value="${databaseInstance?.moveBundle?.id}" optionKey="id" optionValue="name" tabindex="33" />
							</td>
						</tr>
						<tr>
							<td class="label ${standardFieldSpecs.size.imp?:''}" nowrap="nowrap"><label for="size" data-toggle="popover" data-trigger="hover" data-content="${standardFieldSpecs.size.tip}">${standardFieldSpecs.size.label}/${standardFieldSpecs.scale.label}</label></td>
							<td nowrap="nowrap" class="sizeScale">
								<tds:inputControl field="${standardFieldSpecs.size}" size="4" tabindex="14" value="${databaseInstance.size}"/>
								<g:select from="${databaseInstance.constraints.scale.inList}" id="scale" name="scale" value="${databaseInstance.scale}" optionValue="value" tabindex="15" noSelection="${['':'']}"/>
							</td>

							<tds:inputLabel field="${standardFieldSpecs.maintExpDate}" value="${databaseInstance?.maintExpDate}"/>
							<td valign="top" class="value ${hasErrors(bean:databaseInstance,field:'maintExpDate','errors')}">
					           	<input type="text" class="dateRange" size="15" style="width: 138px;" name="maintExpDate" id="maintExpDate"
					                value="<tds:convertDate date="${databaseInstance?.maintExpDate}" />" onchange="tdsCommon.isValidDate(this.value);" tabindex="23" >
							</td>

							<tds:inputLabel field="${standardFieldSpecs.planStatus}" value="${databaseInstance?.planStatus}"/>
							<td>
								<g:select from="${planStatusOptions}" id="planStatus" name="planStatus" value="${databaseInstance.planStatus}" tabindex="34" />
							</td>
						</tr>
						<tr>
							<tds:inputLabel field="${standardFieldSpecs.rateOfChange}" value="${databaseInstance?.rateOfChange}"/>
							<td>
								<tds:inputControl field="${standardFieldSpecs.rateOfChange}" size="4" tabindex="17" value="${databaseInstance.rateOfChange}"/>
							</td>

							<tds:inputLabelAndField field="${standardFieldSpecs.externalRefId}" value="${databaseInstance.externalRefId}" tabindex="22"/>

							<tds:inputLabel field="${standardFieldSpecs.validation}" value="${databaseInstance?.validation}"/>
							<td>
								<g:select from="${databaseInstance.constraints.validation.inList}" id="validation" name="validation" onChange="assetFieldImportance(this.value,'Database');highlightCssByValidation(this.value,'Database','${databaseInstance.id}');" value="${databaseInstance.validation}" tabindex="35	"/>
							</td>
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
