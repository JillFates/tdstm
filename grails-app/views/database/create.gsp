<%@ page import="com.tdsops.tm.enums.domain.ValidationType" %>
<script type="text/javascript">
	$("#db_assetName").val($('#gs_assetName').val())
	$("#db_dbFormat").val($('#gs_dbFormat').val())
	$("#db_planStatus").val($('#gs_planStatus').val())
	$("#db_moveBundle").val($('#gs_moveBundle').val())
</script>
<g:form method="post" action="save" name="createEditAssetForm">
	<input type="hidden" id="db_assetName" name="assetNameFilter" value="" />
	<input type="hidden" id="db_dbFormat" name="dbFormatFilter" value="" />
	<input type="hidden" id="db_planStatus" name="planStatusFilter" value="" />
	<input type="hidden" id="db_moveBundle" name="moveBundleFilter" value="" />

	<input name="showView" id="showView" type="hidden" value=""/>

	<%-- Used to track dependencies added and deleted --%>
	<g:render template="/assetEntity/dependentHidden" />


<table style="border: 0" class="asset-entities-dialog-table-content">
	<tr>
		<td colspan="2">
			<div class="dialog">
				<table>
					<tbody>
						<tr>
							<tds:inputLabel field="${standardFieldSpecs.assetName}" value="${databaseInstance.assetName}"/>
							<td colspan="2" style="font-weight:bold;">
								<tds:inputControl field="${standardFieldSpecs.assetName}" tabindex="11" value="${databaseInstance.assetName}"/>
							</td>

							<tds:inputLabel field="${standardFieldSpecs.description}" value="${databaseInstance.description}"/>
							<td colspan="2" style="font-weight:bold;">
								<tds:inputControl field="${standardFieldSpecs.description}" tabindex="11" value="${databaseInstance.description}" tooltipDataPlacement="bottom"/>
							</td>
						</tr>
						<tr>
							<tds:inputLabelAndField field="${standardFieldSpecs.dbFormat}" value="${databaseInstance.dbFormat}" tabindex="13"/>

							<tds:inputLabelAndField field="${standardFieldSpecs.supportType}" value="${databaseInstance.supportType}" tabindex="26"/>

							<tds:inputLabel field="${standardFieldSpecs.environment}" value="${databaseInstance.environment}"/>
							<td >
								<tds:tooltipSpan field="${standardFieldSpecs.environment}" tooltipDataPlacement="bottom">
									<g:select id="environment" name="environment" from="${environmentOptions}"
										  noSelection="${['':' Please Select']}" tabindex="32"
										  data-toggle="popover" data-trigger="hover" data-content="${standardFieldSpecs.environment.tip}"/>
								</tds:tooltipSpan>
							</td>
						</tr>
						<tr>
							<td class="label ${standardFieldSpecs.size.imp?:''}" nowrap="nowrap">
								<label for="size" data-toggle="popover" data-trigger="hover" data-content="${standardFieldSpecs.size.tip?:standardFieldSpecs.size.label}">
									${standardFieldSpecs.size.label}/${standardFieldSpecs.scale.label}
								</label>
							</td>
							<td nowrap="nowrap" class="sizeScale">
								<tds:inputControl field="${standardFieldSpecs.size}" size="3" tabindex="14" value="${databaseInstance.size}"/>
								<tds:tooltipSpan field="${standardFieldSpecs.scale}">
									<g:select from="${databaseInstance.constraints.scale.inList}" id="scale" name="scale"
											  value="${databaseInstance.scale}" optionValue="value" tabindex="40" noSelection="${['':' Please Select']}"/>
								</tds:tooltipSpan>
							</td>

							<tds:inputLabel field="${standardFieldSpecs.retireDate}" value="${databaseInstance.retireDate}"/>
							<td valign="top" class="value ${hasErrors(bean:databaseInstance,field:'retireDate','errors')}">
							    <script type="text/javascript" charset="utf-8">
									jQuery(function($){ $(".dateRange").kendoDatePicker({ animation: false, format:tdsCommon.kendoDateFormat() }); });
							    </script>
							    <input type="text" class="dateRange" size="15" style="width: 138px;" name="retireDate" id="retireDate"
									   data-toggle="popover" data-trigger="hover" data-content="${standardFieldSpecs.retireDate.tip?:standardFieldSpecs.retireDate.label}"
										value="<tds:convertDate date="${databaseInstance?.retireDate}" />" onchange="tdsCommon.isValidDate(this.value);" tabindex="27" >
							</td>

							<tds:inputLabel field="${standardFieldSpecs.moveBundle}" value="${databaseInstance.moveBundle}"/>
							<td >
								<tds:tooltipSpan field="${standardFieldSpecs.moveBundle}" tooltipDataPlacement="bottom">
									<g:select from="${moveBundleList}" id="moveBundle"  name="moveBundle.id" value="${project.defaultBundle.id}" optionKey="id" optionValue="name" tabindex="34"/>
								</tds:tooltipSpan>
							</td>
						</tr>
						<tr>
							<tds:inputLabel field="${standardFieldSpecs.rateOfChange}" value="${databaseInstance?.rateOfChange}"/>
							<td>
								<tds:inputControl field="${standardFieldSpecs.rateOfChange}" size="3" value="${databaseInstance.rateOfChange}"/>
							</td>

							<tds:inputLabel field="${standardFieldSpecs.maintExpDate}" value="${databaseInstance?.maintExpDate}"/>
							<td valign="top" class="value ${hasErrors(bean:databaseInstance,field:'maintExpDate','errors')}">
							    <input type="text" class="dateRange" size="15" style="width: 138px;" name="maintExpDate" id="maintExpDate"
									   data-toggle="popover" data-trigger="hover" data-content="${standardFieldSpecs.maintExpDate.tip?:standardFieldSpecs.maintExpDate.label}"
										value="<tds:convertDate date="${databaseInstance?.maintExpDate}" />" onchange="tdsCommon.isValidDate(this.value);" tabindex="28" >
							</td>
							<tds:inputLabel field="${standardFieldSpecs.planStatus}" value="${databaseInstance?.planStatus}"/>
							<td>
								<tds:tooltipSpan field="${standardFieldSpecs.planStatus}" tooltipDataPlacement="bottom">
									<g:select from="${planStatusOptions}" id="planStatus" name="planStatus" value="${databaseInstance.planStatus}" tabindex="35" />
								</tds:tooltipSpan>
							</td>
						</tr>
						<tr>
							<td></td>
							<td></td>

							<tds:inputLabelAndField field="${standardFieldSpecs.externalRefId}" value="${databaseInstance.externalRefId}" tabindex="11"/>

							<tds:inputLabel field="${standardFieldSpecs.validation}" value="${databaseInstance?.validation}"/>
							<td>
								<tds:tooltipSpan field="${standardFieldSpecs.validation}" tooltipDataPlacement="bottom">
									<g:select from="${databaseInstance.constraints.validation.inList}" id="validation" name="validation" value="${ValidationType.UNKNOWN}"/>
								</tds:tooltipSpan>
							</td>
						</tr>
						<tbody class="customTemplate">
							<g:render template="/assetEntity/customEdit" model="[assetEntityInstance:databaseInstance]"></g:render>
						</tbody>
						<g:render template="/comment/assetTagsEdit"></g:render>
					</tbody>
				</table>
			</div>
		</td>
	</tr>
	<tr>
		<g:render template="/assetEntity/dependentCreateEdit" model="[whom:'create',supportAssets:[],dependentAssets:[]]"></g:render>
	</tr>
	<tr>
		<td colspan="2">
			<g:render template="/assetEntity/createButtons" model="[assetClass: databaseInstance.assetClass]"></g:render>
		</td>
	</tr>
</table>
</g:form>
<script>
    $(document).ready(function() {
        $('[data-toggle="popover"]').popover();
    });
	currentMenuId = "#assetMenu";
	$("#assetMenuId a").css('background-color','#003366');

	$(document).ready(function() {
		EntityCrud.loadAssetTags();
	});
</script>
