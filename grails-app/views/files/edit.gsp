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

	<div class="legacy-modal-dialog">
		<div class="legacy-modal-content">
			<%-- Header Content Here --%>
			<g:render template="/assetEntity/showHeader" model="[assetEntity:fileInstance]"></g:render>
			<div id="modalBody" class="legacy-modal-body">
				<div class="legacy-modal-body-content">
					<div class="grid-form">
						<div class="clr-form-control">
							<tds:inputLabel field="${standardFieldSpecs.assetName}" value="${fileInstance?.assetName}"/>
							<tds:inputControl field="${standardFieldSpecs.assetName}" tabindex="10" value="${fileInstance?.assetName}"/>
						</div>

						<div class="clr-form-control">
							<tds:inputLabel field="${standardFieldSpecs.description}" value="${fileInstance?.description}"/>
							<tds:inputControl field="${standardFieldSpecs.description}" tabindex="11" value="${fileInstance?.description}" tooltipDataPlacement="bottom"/>
						</div>

						<tds:inputLabelAndField field="${standardFieldSpecs.fileFormat}" value="${fileInstance.fileFormat}" tabindex="12"/>
						<tds:inputLabelAndField field="${standardFieldSpecs.LUN}" value="${fileInstance.LUN}" tabindex="22"/>
						<tds:inputLabelAndField field="${standardFieldSpecs.supportType}" value="${fileInstance?.supportType}" tabindex="32"/>

						<div class="clr-form-control">
							<tds:inputLabel field="${standardFieldSpecs.moveBundle}" value="${fileInstance?.moveBundle}"/>
							<div class="clr-control-container">
  								<div class="clr-select-wrapper">
									<g:select class="clr-select" from="${moveBundleList}" id="moveBundle" name="moveBundle.id" value="${fileInstance?.moveBundle?.id}" tabindex="42" optionKey="id" optionValue="name" tabindex="34" />	
								</div>
							</div>
						</div>

						<div class="clr-form-control">	
							<label for="size" class="clr-control-label ${standardFieldSpecs.size.imp?:''}" data-toggle="popover" data-trigger="hover" data-content="${standardFieldSpecs.size.tip?:standardFieldSpecs.size.label}">
								${standardFieldSpecs.size.label}/${standardFieldSpecs.scale.label}
							</label>
							<tds:inputControl field="${standardFieldSpecs.size}" tabindex="13" value="${fileInstance?.size}"/>&nbsp;
							<div class="clr-control-container" style="padding-left: 30px;">
  								<div class="clr-select-wrapper">
									<g:select class="clr-select" from="${com.tdssrc.grails.GormUtil.getConstrainedProperties(fileInstance.class).scale.inList}" name="scale" id="scale" tabindex="13"
											value="${fileInstance.scale}" optionValue="value" noSelection="${['':' Please Select']}"/>
								</div>
							</div>
						</div class="clr-form-control">

						<tds:inputLabelAndField field="${standardFieldSpecs.externalRefId}" value="${fileInstance.externalRefId}" tabindex="23"/>

						<div class="clr-form-control">
							<tds:inputLabel field="${standardFieldSpecs.environment}" value="${fileInstance?.environment}"/>
							<div class="clr-control-container">
  								<div class="clr-select-wrapper">
								<g:select class="clr-select" id="environment" name="environment" from="${environmentOptions}"
									value="${fileInstance.environment}" tabindex="33" noSelection="${['':' Please Select']}" />
								</div>
							</div>
						</div>

						<div class="clr-form-control">
							<tds:inputLabel field="${standardFieldSpecs.planStatus}" value="${fileInstance?.planStatus}"/>
							<div class="clr-control-container">
  								<div class="clr-select-wrapper">
									<g:select class="clr-select" from="${planStatusOptions}" id="planStatus" name="planStatus" value="${fileInstance.planStatus}"  tabindex="43"/>
								</div>
							</div>
						</div>

						<div class="clr-form-control">
							<tds:inputLabel field="${standardFieldSpecs.rateOfChange}" value="${fileInstance?.rateOfChange}"/>
							<tds:inputControl field="${standardFieldSpecs.rateOfChange}" value="${fileInstance?.rateOfChange}" tabindex="14"/>
						</div>

						<div class="clr-form-control">
							<tds:inputLabel field="${standardFieldSpecs.validation}" value="${fileInstance?.validation}"/>
							<div class="clr-control-container">
  								<div class="clr-select-wrapper">
									<g:select class="clr-select" from="${com.tdssrc.grails.GormUtil.getConstrainedProperties(fileInstance.class).validation.inList}" id="validation" name="validation" tabindex="44" value="${fileInstance.validation}"/>
								</div>
							</div>
						</div>

						<g:render template="/assetEntity/customEdit" model="[assetEntityInstance:fileInstance]"></g:render>
					</div>
					<g:render template="/comment/assetTagsEdit"></g:render>


					<table style="border: 0;" class="asset-entities-dialog-table-content">
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
				</div>
			</div>
		</div>
		<g:render template="/assetEntity/editButtons" model="[assetEntity:fileInstance]"></g:render>
	</div>
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
