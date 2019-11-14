<%@page import="net.transitionmanager.asset.Database"%>
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
		changeDocTitle('${raw(escapedName)}');
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
	<g:render template="/assetEntity/dependentHidden" />

	<div class="legacy-modal-dialog">
		<div class="legacy-modal-content">
			<%-- Header Content Here --%>
			<g:render template="/assetEntity/showHeader" model="[assetEntity:databaseInstance]"></g:render>
			<div id="modalBody" class="legacy-modal-body">
				<div class="legacy-modal-body-content">
					<div class="grid-form" id="details">
						<div class="clr-form-control">
							<tds:inputLabel field="${standardFieldSpecs.assetName}" value="${databaseInstance?.assetName}"/>
							<tds:inputControl field="${standardFieldSpecs.assetName}" tabindex="11" value="${databaseInstance.assetName}"/>
						</div>
						
						<div class="clr-form-control">
							<tds:inputLabel field="${standardFieldSpecs.description}" value="${databaseInstance?.description}"/>
							<tds:inputControl field="${standardFieldSpecs.description}" tabindex="12" value="${databaseInstance.description}" tooltipDataPlacement="bottom"/>
						</div>

						<tds:inputLabelAndField field="${standardFieldSpecs.dbFormat}" value="${databaseInstance.dbFormat}" tabindex="13"/>
						<tds:inputLabelAndField field="${standardFieldSpecs.supportType}" value="${databaseInstance?.supportType}"/>

						<div class="clr-form-control">
							<tds:inputLabel field="${standardFieldSpecs.environment}" value="${databaseInstance?.environment}"/>
							<div class="clr-control-container">
  								<div class="clr-select-wrapper">
									<g:select class="clr-select" id="environment" name="environment" from="${environmentOptions}" value="${databaseInstance.environment}" noSelection="${['':' Please Select']}" tabindex="32" />
								</div>
							</div>
						</div>

						<div class="clr-form-control">
							<label for="size" class="clr-control-label ${standardFieldSpecs.size.imp?:''}" data-toggle="popover" data-trigger="hover" data-content="${standardFieldSpecs.size.tip?:standardFieldSpecs.size.label}">
								${standardFieldSpecs.size.label}/${standardFieldSpecs.scale.label}
							</label>
							<tds:inputControl field="${standardFieldSpecs.size}" tabindex="14" value="${databaseInstance.size}"/>

							<div class="clr-control-container" style="padding-left: 30px;">
  								<div class="clr-select-wrapper">
									<g:select from="${com.tdssrc.grails.GormUtil.getConstrainedProperties(databaseInstance.class).scale.inList}" id="scale" name="scale"
										value="${databaseInstance.scale}" optionValue="value" tabindex="15" noSelection="${['':'']}" class="clr-select"/>
								</div>
							</div>
						</div>

						<div class="clr-form-control">
							<tds:inputLabel field="${standardFieldSpecs.retireDate}" value="${databaseInstance?.retireDate}"/>
							<script type="text/javascript" charset="utf-8">
								jQuery(function($){ $(".dateRange").kendoDatePicker({ animation: false, format:tdsCommon.kendoDateFormat() }); });
							</script>
							<input type="text" class="dateRange" name="retireDate" id="retireDate"
								data-toggle="popover" data-trigger="hover" data-content="${standardFieldSpecs.retireDate.tip?:standardFieldSpecs.retireDate.label}"
									value="<tds:convertDate date="${databaseInstance?.retireDate}" />" onchange="tdsCommon.isValidDate(this.value);" tabindex="22" >
						</div>

						<div class="clr-form-control">
							<tds:inputLabel field="${standardFieldSpecs.moveBundle}" value="${databaseInstance?.moveBundle}"/>
							<div class="clr-control-container">
  								<div class="clr-select-wrapper">
									<g:select class="clr-select" from="${moveBundleList}" id="moveBundle" name="moveBundle.id" value="${databaseInstance?.moveBundle?.id}" optionKey="id" optionValue="name" tabindex="33" data-toggle="popover" data-trigger="hover" data-content="${standardFieldSpecs.moveBundle.tip}"/>
								</div>
							</div>
						</div>

						<div class="clr-form-control">
							<tds:inputLabel field="${standardFieldSpecs.rateOfChange}" value="${databaseInstance?.rateOfChange}"/>
							<tds:inputControl field="${standardFieldSpecs.rateOfChange}" tabindex="17" value="${databaseInstance.rateOfChange}"/>
						</div>

						<div class="clr-form-control">
							<tds:inputLabel field="${standardFieldSpecs.maintExpDate}" value="${databaseInstance?.maintExpDate}"/>
							<input type="text" class=" ${standardFieldSpecs.maintExpDate.imp?:''} dateRange" name="maintExpDate" id="maintExpDate"
								data-toggle="popover" data-trigger="hover" data-content="${standardFieldSpecs.maintExpDate.tip?:standardFieldSpecs.maintExpDate.label}"
									value="<tds:convertDate date="${databaseInstance?.maintExpDate}" />" onchange="tdsCommon.isValidDate(this.value);" tabindex="23" >
						</div>

						<div class="clr-form-control">
							<tds:inputLabel field="${standardFieldSpecs.planStatus}" value="${databaseInstance?.planStatus}"/>
							<div class="clr-control-container">
  								<div class="clr-select-wrapper">
									<g:select class="clr-select" from="${planStatusOptions}" id="planStatus" name="planStatus" value="${databaseInstance.planStatus}" tabindex="34"/>
								</div>
							</div>
						</div>

							<tds:inputLabelAndField field="${standardFieldSpecs.externalRefId}" value="${databaseInstance.externalRefId}" tabindex="22"/>

						<div class="clr-form-control">
							<tds:inputLabel field="${standardFieldSpecs.validation}" value="${databaseInstance?.validation}"/>
							<div class="clr-control-container">
  								<div class="clr-select-wrapper">
									<g:select class="clr-select" from="${com.tdssrc.grails.GormUtil.getConstrainedProperties(databaseInstance.class).validation.inList}" id="validation" name="validation"
										value="${databaseInstance.validation}" tabindex="35"/>
								</div>
							</div>
							
						</div>

						<g:render template="/assetEntity/customEdit" model="[assetEntityInstance:databaseInstance]"></g:render>
					</div>

					<g:render template="/comment/assetTagsEdit"></g:render>

					<table style="border: 0" class="asset-entities-dialog-table-content">
						<tr id="databaseDependentId" class="assetDependent">
							<td class="depSpin"><span><asset:image src="images/processing.gif"/> </span></td>
						</tr>
					</table>
				</div>
			</div>
		</div>
		<g:render template="/assetEntity/editButtons" model="[assetEntity:databaseInstance]"></g:render>
	</div>
</g:form>
<script>

    $(document).ready(function() {
        $('[data-toggle="popover"]').popover();
		EntityCrud.loadAssetTags();
    });

	currentMenuId = "#assetMenu";
	$("#assetMenuId a").css('background-color','#003366')
	$('#tabType').val($('#assetTypesId').val());
</script>
