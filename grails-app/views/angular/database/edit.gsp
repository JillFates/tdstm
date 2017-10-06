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
		var assetId = '${asset.id}'
		populateDependency(assetId, 'database','edit')
		changeDocTitle('${escapedName}');
	})
</script>
<g:form method="post" action="update" name="createEditAssetForm" controller="/ws/asset">
	<input type="hidden" name="id" value="${asset?.id}" />
	<input type="hidden" id="db_assetName" name="assetNameFilter" value="" />
	<input type="hidden" id="db_dbFormat" name="dbFormatFilter" value="" />
	<input type="hidden" id="db_planStatus" name="planStatusFilter" value="" />
	<input type="hidden" id="db_moveBundle" name="moveBundleFilter" value="" />

	<input type="hidden" id="dbId" value ="${asset.id}"/>
	<input type="hidden" id="tabType" name="tabType" value =""/>
	<input type="hidden" name="updateView" id="updateView" value=""/>

	<input type="hidden" id="edit_supportAddedId" name="addedSupport" value ="0"/>
	<input type="hidden" id="edit_dependentAddedId" name="addedDep" value ="0"/>

	<%-- Key field and optimistic locking var --%>
	<input type="hidden" id="assetId" 	name="id" value="${assetId}"/>
	<input type="hidden" id="version" 	name="version" value="${version}"/>

	<%-- Used to track dependencies added and deleted --%>
	<g:render template="/angular/common/dependentHidden" />


<table style="border: 0">
	<tr>
		<td colspan="2">
			<div class="dialog">
				<table>
					<tbody>
						<tr>
							<tds-angular:inputLabel field="${standardFieldSpecs.assetName}" value="${asset?.assetName}"/>
							<td colspan="3" style="font-weight:bold;">
								<tds-angular:inputControl field="${standardFieldSpecs.assetName}" tabindex="11" value="${asset.assetName}"/>
							</td>

							<tds-angular:inputLabel field="${standardFieldSpecs.description}" value="${asset?.description}"/>
							<td colspan="3">
								<tds-angular:inputControl field="${standardFieldSpecs.description}" size="50" tabindex="12" value="${asset.description}" tooltipDataPlacement="bottom"/>
							</td>
						</tr>
						<tr>
							<tds-angular:inputLabelAndField field="${standardFieldSpecs.dbFormat}" value="${asset.dbFormat}" tabindex="13"/>

							<tds-angular:inputLabelAndField field="${standardFieldSpecs.supportType}" value="${asset?.supportType}"/>

							<tds-angular:inputLabel field="${standardFieldSpecs.environment}" value="${asset?.environment}"/>
							<td data-toggle="popover" data-trigger="hover" data-content="${standardFieldSpecs.environment.tip}">
								<tds-angular:tooltipSpan field="${standardFieldSpecs.environment}">
									<g:select id="environment" name="environment" from="${environmentOptions}" value="${asset.environment}" noSelection="${['':' Please Seb lect']}" tabindex="32" />
								</tds-angular:tooltipSpan>
							</td>
						</tr>
						<tr>
							<td class="label ${standardFieldSpecs.size.imp?:''}" nowrap="nowrap">
								<label for="size" data-toggle="popover" data-trigger="hover" data-content="${standardFieldSpecs.size.tip?:standardFieldSpecs.size.label}">
									${standardFieldSpecs.size.label}/${standardFieldSpecs.scale.label}
								</label>
							</td>
							<td nowrap="nowrap" class="sizeScale">
								<tds-angular:inputControl field="${standardFieldSpecs.size}" size="4" tabindex="14" value="${asset.size}"/>
								<tds-angular:tooltipSpan field="${standardFieldSpecs.scale}">
									<g:select from="${asset.constraints.scale.inList}" id="scale" name="scale"
											  value="${asset.scale}" optionValue="value" tabindex="15" noSelection="${['':'']}"/>
								</tds-angular:tooltipSpan>
							</td>

							<tds-angular:inputLabel field="${standardFieldSpecs.retireDate}" value="${asset?.retireDate}"/>
							<td valign="top" class="value ${hasErrors(bean:asset,field:'retireDate','errors')}">
							    <script type="text/javascript" charset="utf-8">
									jQuery(function($){ $(".dateRange").kendoDatePicker({ animation: false, format:tdsCommon.kendoDateFormat() }); });
					            </script>
					            <input type="text" class="dateRange" size="15" style="width: 138px;" name="retireDate" id="retireDate"
									   data-toggle="popover" data-trigger="hover" data-content="${standardFieldSpecs.retireDate.tip?:standardFieldSpecs.retireDate.label}"
					                	value="<tds-angular:convertDate date="${asset?.retireDate}" />" onchange="tdsCommon.isValidDate(this.value);" tabindex="22" >
							</td>

							<tds-angular:inputLabel field="${standardFieldSpecs.moveBundle}" value="${asset?.moveBundle}"/>
							<td>
								<tds-angular:tooltipSpan field="${standardFieldSpecs.moveBundle}">
									<g:select from="${moveBundleList}" id="moveBundle" name="moveBundle.id" value="${asset?.moveBundle?.id}" optionKey="id" optionValue="name" tabindex="33" data-toggle="popover" data-trigger="hover" data-content="${standardFieldSpecs.moveBundle.tip}"/>
								</tds-angular:tooltipSpan>
							</td>
						</tr>
						<tr>
							<tds-angular:inputLabel field="${standardFieldSpecs.rateOfChange}" value="${asset?.rateOfChange}"/>
							<td>
								<tds-angular:inputControl field="${standardFieldSpecs.rateOfChange}" size="4" tabindex="17" value="${asset.rateOfChange}"/>
							</td>

							<tds-angular:inputLabel field="${standardFieldSpecs.maintExpDate}" value="${asset?.maintExpDate}"/>
							<td valign="top" class="value ${hasErrors(bean:asset,field:'maintExpDate','errors')}">
					           	<input type="text" class="dateRange" size="15" style="width: 138px;" name="maintExpDate" id="maintExpDate"
									   data-toggle="popover" data-trigger="hover" data-content="${standardFieldSpecs.maintExpDate.tip?:standardFieldSpecs.maintExpDate.label}"
					                	value="<tds-angular:convertDate date="${asset?.maintExpDate}" />" onchange="tdsCommon.isValidDate(this.value);" tabindex="23" >
							</td>

							<tds-angular:inputLabel field="${standardFieldSpecs.planStatus}" value="${asset?.planStatus}"/>
							<td>
								<tds-angular:tooltipSpan field="${standardFieldSpecs.planStatus}">
									<g:select from="${planStatusOptions}" id="planStatus" name="planStatus" value="${asset.planStatus}" tabindex="34"/>
								</tds-angular:tooltipSpan>
							</td>
						</tr>
						<tr>
							<td></td>
							<td></td>

							<tds-angular:inputLabelAndField field="${standardFieldSpecs.externalRefId}" value="${asset.externalRefId}" tabindex="22"/>

							<tds-angular:inputLabel field="${standardFieldSpecs.validation}" value="${asset?.validation}"/>
							<td>
								<tds-angular:tooltipSpan field="${standardFieldSpecs.validation}">
									<g:select from="${asset.constraints.validation.inList}" id="validation" name="validation"
										   value="${asset.validation}" tabindex="35"/>
								</tds-angular:tooltipSpan>
							</td>
						</tr>
						<tbody class="customTemplate">
							<g:render template="/angular/common/customEdit" model="[assetEntityInstance:asset]"></g:render>
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
				<g:render template="/angular/common/assetEditButtons" model="[assetEntity:asset]"></g:render>
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
