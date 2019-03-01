<%@ page import="com.tdsops.tm.enums.domain.ValidationType" %>
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
	<g:render template="/assetEntity/dependentHidden" />

	<table style="border: 0;" class="asset-entities-dialog-table-content">
		<tr>
			<td colspan="2">
				<div class="dialog">
					<table>
						<tbody>
						<tr>
							<tds:inputLabel field="${standardFieldSpecs.assetName}" value="${fileInstance.assetName}"/>
							</td>
							<td colspan="3">
								<tds:inputControl field="${standardFieldSpecs.assetName}" value="${fileInstance?.assetName}" tabindex="10"/>
							</td>
							<tds:inputLabel field="${standardFieldSpecs.description}" value="${fileInstance?.description}"/>
							<td colspan="3">
								<tds:inputControl field="${standardFieldSpecs.description}" size="50" value="${fileInstance?.description}" tabindex="11" tooltipDataPlacement="bottom"/>
							</td>
						</tr>

						<tr>
							<tds:inputLabelAndField field="${standardFieldSpecs.fileFormat}" value="${fileInstance.fileFormat}" tabindex="12"/>
							<tds:inputLabelAndField field="${standardFieldSpecs.LUN}" value="${fileInstance.LUN}" tabindex="22"/>
							<tds:inputLabelAndField field="${standardFieldSpecs.supportType}" value="${fileInstance?.supportType}" tabindex="32"/>
							<tds:inputLabel field="${standardFieldSpecs.moveBundle}" value="${fileInstance?.moveBundle}"/>
							<td>
								<tds:tooltipSpan field="${standardFieldSpecs.moveBundle}" tooltipDataPlacement="bottom">
									<g:select from="${moveBundleList}" id="moveBundle" name="moveBundle.id" value="${project.defaultBundle.id}" optionKey="id" optionValue="name" tabindex="42" />
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
								<tds:inputControl field="${standardFieldSpecs.size}" size="10" value="${fileInstance?.size}" tabindex="13"/>
								<tds:tooltipSpan field="${standardFieldSpecs.scale}">
									<g:select from="${com.tdssrc.grails.GormUtil.getConstrainedProperties(fileInstance.class).scale.inList}" optionValue="value" name="scale" id="scale" value="GB" tabindex="13"/>
								</tds:tooltipSpan>
							</td>

							<tds:inputLabelAndField field="${standardFieldSpecs.externalRefId}" value="${fileInstance.externalRefId}" tabindex="23"/>
							<tds:inputLabel field="${standardFieldSpecs.environment}" value="${fileInstance?.environment}"/>
							<td>
								<tds:tooltipSpan field="${standardFieldSpecs.environment}">
									<g:select id="environment" name="environment" from="${environmentOptions}" noSelection="${['':' Please Select']}"  tabindex="33"/>
								</tds:tooltipSpan>
							</td>

							<tds:inputLabel field="${standardFieldSpecs.planStatus}" value="${fileInstance?.planStatus}"/>
							<td>
								<tds:tooltipSpan field="${standardFieldSpecs.planStatus}" tooltipDataPlacement="bottom">
									<g:select from="${planStatusOptions}" id="planStatus" name="planStatus" value="${fileInstance.planStatus}"  tabindex="43"/>
								</tds:tooltipSpan>
							</td>
						</tr>
						<tr>
							<tds:inputLabel field="${standardFieldSpecs.rateOfChange}" value="${fileInstance?.rateOfChange}"/>
							<td>
								<tds:inputControl field="${standardFieldSpecs.rateOfChange}" size="3" value="${fileInstance?.rateOfChange}" tabindex="14"/>
							</td>

							<td colspan="2">
							<td colspan="2">

							<tds:inputLabel field="${standardFieldSpecs.validation}" value="${fileInstance?.validation}"/>
							<td>
								<tds:tooltipSpan field="${standardFieldSpecs.validation}" tooltipDataPlacement="bottom">
									<g:select from="${com.tdssrc.grails.GormUtil.getConstrainedProperties(fileInstance.class).validation.inList}" id="validation" name="validation" value="${ValidationType.UNKNOWN}" tabindex="44"/>
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
		<tr>
			<g:render template="/assetEntity/dependentCreateEdit" model="[whom:'create',supportAssets:[],dependentAssets:[]]"></g:render>
		</tr>
		<tr>
			<td colspan="2">
				<g:render template="/assetEntity/createButtons" model="[assetClass: fileInstance.assetClass]"></g:render>
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
