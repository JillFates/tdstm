<%@page import="net.transitionmanager.asset.Files"%>
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

		changeDocTitle('${raw(escapedName)}');
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
	<g:render template="/assetEntity/dependentHidden" />

	<table style="border: 0;" class="asset-entities-dialog-table-content">
		<tr>
			<td colspan="2">
				<div class="dialog">
					<table>
						<tbody>
						<tr>
							<tds:inputLabel field="${standardFieldSpecs.assetName}" value="${fileInstance?.assetName}"/>
							</td>
							<td colspan="3">
								<tds:inputControl field="${standardFieldSpecs.assetName}" tabindex="10" value="${fileInstance?.assetName}"/>
							</td>
							<tds:inputLabel field="${standardFieldSpecs.description}" value="${fileInstance?.description}"/>
							<td colspan="3">
								<tds:inputControl field="${standardFieldSpecs.description}" tabindex="11" value="${fileInstance?.description}" tooltipDataPlacement="bottom"/>
							</td>

						</tr>
						<tr>
							<tds:inputLabelAndField field="${standardFieldSpecs.fileFormat}" value="${fileInstance.fileFormat}" tabindex="12"/>
							<tds:inputLabelAndField field="${standardFieldSpecs.LUN}" value="${fileInstance.LUN}" tabindex="22"/>
							<tds:inputLabelAndField field="${standardFieldSpecs.supportType}" value="${fileInstance?.supportType}" tabindex="32"/>
							<tds:inputLabel field="${standardFieldSpecs.moveBundle}" value="${fileInstance?.moveBundle}"/>
							<td>
								<tds:tooltipSpan field="${standardFieldSpecs.moveBundle}" tooltipDataPlacement="bottom">
									<g:select class="${standardFieldSpecs.moveBundle.imp?:''}" from="${moveBundleList}" id="moveBundle" name="moveBundle.id" value="${fileInstance?.moveBundle?.id}" tabindex="42" optionKey="id" optionValue="name" tabindex="34" />
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
								<tds:inputControl field="${standardFieldSpecs.size}" tabindex="13" value="${fileInstance?.size}"/>&nbsp;
								<tds:tooltipSpan field="${standardFieldSpecs.scale}">
									<g:select class="${standardFieldSpecs.scale.imp?:''}" from="${com.tdssrc.grails.GormUtil.getConstrainedProperties(fileInstance.class).scale.inList}" name="scale" id="scale" tabindex="13"
											  value="${fileInstance.scale}" optionValue="value" noSelection="${['':' Please Select']}"/>
								</tds:tooltipSpan>
							</td>

							<tds:inputLabelAndField field="${standardFieldSpecs.externalRefId}" value="${fileInstance.externalRefId}" tabindex="23"/>

							<tds:inputLabel field="${standardFieldSpecs.environment}" value="${fileInstance?.environment}"/>
							<td>
								<tds:tooltipSpan field="${standardFieldSpecs.environment}">
									<g:select class="${standardFieldSpecs.environment.imp?:''}" id="environment" name="environment" from="${environmentOptions}"
											  value="${fileInstance.environment}" tabindex="33" noSelection="${['':' Please Select']}" />
								</tds:tooltipSpan>
							</td>

							<tds:inputLabel field="${standardFieldSpecs.planStatus}" value="${fileInstance?.planStatus}"/>
							<td>
								<tds:tooltipSpan field="${standardFieldSpecs.planStatus}" tooltipDataPlacement="bottom">
									<g:select class="${standardFieldSpecs.planStatus.imp?:''}" from="${planStatusOptions}" id="planStatus" name="planStatus" value="${fileInstance.planStatus}"  tabindex="43"/>
								</tds:tooltipSpan>
							</td>
						</tr>
						<tr>
							<tds:inputLabel field="${standardFieldSpecs.rateOfChange}" value="${fileInstance?.rateOfChange}"/>
							<td>
								<tds:tooltipSpan field="${standardFieldSpecs.rateOfChange}">
									<tds:inputControl field="${standardFieldSpecs.rateOfChange}" size="4" value="${fileInstance?.rateOfChange}" tabindex="14"/>
								</tds:tooltipSpan>
							</td>

							<td colspan="2">
							<td colspan="2">

							<tds:inputLabel field="${standardFieldSpecs.validation}" value="${fileInstance?.validation}"/>
							<td>
								<tds:tooltipSpan field="${standardFieldSpecs.validation}" tooltipDataPlacement="bottom">
									<g:select class="${standardFieldSpecs.validation.imp?:''}" from="${com.tdssrc.grails.GormUtil.getConstrainedProperties(fileInstance.class).validation.inList}" id="validation" name="validation" tabindex="44" value="${fileInstance.validation}"/>
								</tds:tooltipSpan>
							</td>
						</tr>
							<tbody class="customTemplate">
							<g:render template="/assetEntity/customEdit" model="[assetEntityInstance:fileInstance]"></g:render>
							</tbody>

							<g:render template="/comment/assetTagsEdit"></g:render>
						</tbody>
					</table>
				</div>
			</td>
		</tr>



		<tr id="filesDependentId" class="assetDependent">
			<td class="depSpin"><span><asset:image src="images/processing.gif"/> </span></td>
		</tr>
		<tr>
			<td colspan="2">
				<div class="buttons">
					<g:render template="/assetEntity/editButtons" model="[assetEntity:fileInstance]"></g:render>
				</div>
			</td>
		</tr>
	</table>
</g:form>
<script>
    $(document).ready(function() {
        $('[data-toggle="popover"]').popover();
		EntityCrud.loadAssetTags();
    });
	currentMenuId = "#assetMenu";
	$("#assetMenuId a").css('background-color','#003366')
	$('#tabType').val($('#assetTypesId').val())
</script>
