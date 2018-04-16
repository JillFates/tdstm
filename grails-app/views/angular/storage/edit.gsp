<%@page import="com.tds.asset.Files"%>
<%-- <g:set var="assetClass" value="${(new Files()).assetClass}" /> --%>
<g:set var="assetClass" value="Files" />

<script type="text/javascript">
	/*
	$("#file_assetName").val($('#gs_assetName').val())
	$("#file_fileFormat").val($('#gs_fileFormat').val())
	$("#file_size").val($('#gs_size').val())
	$("#file_planStatus").val($('#gs_planStatus').val())
	$("#file_moveBundle").val($('#gs_moveBundle').val())

	$(document).ready(function() {
		// Ajax to populate dependency selects in edit pages
		var assetId = '${filesInstance.id}'
		populateDependency(assetId,'files','edit')
		changeDocTitle('${escapedName}');
	})
	*/
</script>

<div>
	<input type="hidden" id="file_assetName" name="assetNameFilter" value="" />
	<input type="hidden" id="file_fileFormat" name="fileFormatFilter" value="" />
	<input type="hidden" id="file_size" name="sizeFilter" value="" />
	<input type="hidden" id="file_planStatus" name="planStatusFilter" value="" />
	<input type="hidden" id="file_moveBundle" name="moveBundleFilter" value="" />
	<input type="hidden" name="id" value="${filesInstance?.id}" />

	<input type="hidden" id ="filesId"  value ="${filesInstance.id}"/>
	<input type="hidden" id = "tabType" name="tabType" value =""/>
	<input name="updateView" id="updateView" type="hidden" value=""/>

	<%-- Key field and optimistic locking var --%>
	<input type="hidden" id="assetId" 	name="id" value="${assetId}"/>
	<input type="hidden" id="version" 	name="version" value="${version}"/>

	<%-- Used to track dependencies added and deleted --%>

	<table style="border: 0;">
		<tr>
			<td colspan="2">
				<div class="dialog">
					<table>
						<tbody>
						<tr>
							<tds:inputLabel field="${standardFieldSpecs.assetName}" value="${filesInstance?.assetName}"/>
							<td colspan="3">
								<tds:inputControl field="${standardFieldSpecs.assetName}" tabindex="10" value="${filesInstance?.assetName}"/>
							</td>
							<tds:inputLabel field="${standardFieldSpecs.description}" value="${filesInstance?.description}"/>
							<td colspan="3">
								<tds:inputControl field="${standardFieldSpecs.description}" tabindex="11" value="${filesInstance?.description}" tooltipDataPlacement="bottom"/>
							</td>

						</tr>
						<tr>
							<tds:inputLabelAndField field="${standardFieldSpecs.fileFormat}" value="${filesInstance.fileFormat}" tabindex="12"/>
							<tds:inputLabelAndField field="${standardFieldSpecs.LUN}" value="${filesInstance.LUN}" tabindex="22"/>
							<tds:inputLabelAndField field="${standardFieldSpecs.supportType}" value="${filesInstance?.supportType}" tabindex="32"/>
							<tds:inputLabel field="${standardFieldSpecs.moveBundle}" value="${filesInstance?.moveBundle}"/>
							<td>
								<tds:tooltipSpan field="${standardFieldSpecs.moveBundle}" tooltipDataPlacement="bottom">
									<g:select from="${moveBundleList}" id="moveBundle" name="moveBundle.id" value="${filesInstance?.moveBundle?.id}" tabindex="42" optionKey="id" optionValue="name" tabindex="34" />
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
								<tds:inputControl field="${standardFieldSpecs.size}" tabindex="13" value="${filesInstance?.size}"/>&nbsp;
								<tds:tooltipSpan field="${standardFieldSpecs.scale}">
									<g:select from="${filesInstance.constraints.scale.inList}" name="scale" id="scale" tabindex="13"
											  value="${filesInstance.scale}" optionValue="value" noSelection="${['':' Please Select']}"/>
								</tds:tooltipSpan>
							</td>

							<tds:inputLabelAndField field="${standardFieldSpecs.externalRefId}" value="${filesInstance.externalRefId}" tabindex="23"/>

							<tds:inputLabel field="${standardFieldSpecs.environment}" value="${filesInstance?.environment}"/>
							<td>
								<tds:tooltipSpan field="${standardFieldSpecs.environment}">
									<g:select id="environment" name="environment" from="${environmentOptions}"
											  value="${filesInstance.environment}" tabindex="33" noSelection="${['':' Please Select']}" />
								</tds:tooltipSpan>
							</td>

							<tds:inputLabel field="${standardFieldSpecs.planStatus}" value="${filesInstance?.planStatus}"/>
							<td>
								<tds:tooltipSpan field="${standardFieldSpecs.planStatus}" tooltipDataPlacement="bottom">
									<g:select from="${planStatusOptions}" id="planStatus" name="planStatus" value="${filesInstance.planStatus}"  tabindex="43"/>
								</tds:tooltipSpan>
							</td>
						</tr>
						<tr>
							<tds:inputLabel field="${standardFieldSpecs.rateOfChange}" value="${filesInstance?.rateOfChange}"/>
							<td>
								<tds:tooltipSpan field="${standardFieldSpecs.rateOfChange}">
									<tds:inputControl field="${standardFieldSpecs.rateOfChange}" size="4" value="${filesInstance?.rateOfChange}" tabindex="14"/>
								</tds:tooltipSpan>
							</td>

							<td colspan="2">
							<td colspan="2">

							<tds:inputLabel field="${standardFieldSpecs.validation}" value="${filesInstance?.validation}"/>
							<td>
								<tds:tooltipSpan field="${standardFieldSpecs.validation}" tooltipDataPlacement="bottom">
									<g:select from="${filesInstance.constraints.validation.inList}" id="validation" name="validation" tabindex="44" value="${filesInstance.validation}"/>
								</tds:tooltipSpan>
							</td>
						</tr>
						<tbody class="customTemplate">
							<g:render template="/angular/common/customEdit" model="[assetEntityInstance:filesInstance]"></g:render>
						</tbody>
					</table>
				</div>
			</td>
		</tr>
		<tr id="filesDependentId" class="assetDependent">
			%{--<td class="depSpin"><span><img alt="" src="${resource(dir:'images',file:'processing.gif')}"/> </span></td>--}%
		</tr>
		<tr>
			<td colspan="2">
				<div class="buttons">
					%{--<g:render template="../assetEntity/editButtons" model="[assetEntity:filesInstance]"></g:render>--}%
				</div>
			</td>
		</tr>
	</table>
</div>

<script>
    /*
    $(document).ready(function() {
        $('[data-toggle="popover"]').popover();
    });
	currentMenuId = "#assetMenu";
	$("#assetMenuId a").css('background-color','#003366')
	$('#tabType').val($('#assetTypesId').val())
	*/
</script>
