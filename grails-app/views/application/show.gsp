<%@page import="net.transitionmanager.asset.Application"%>
<%@page import="net.transitionmanager.security.Permission"%>
<%@page defaultCodec="html" %> 

<g:set var="assetClass" value="${(new Application()).assetClass}" />

<div class="legacy-modal-dialog">
	<div class="legacy-modal-content">
		<g:render template="/assetEntity/showHeader" model="[assetEntity:applicationInstance]"></g:render>

		<%-- TODO: Add style properties to modal body class. Calc width and height. Add Padding to modal body. --%>
		<div id="modalBody" class="legacy-modal-body">
			<div class="legacy-modal-body-content">
				<div class="clr-row">
					<%-- <div [ngClass]="{'clr-col-12':showDetails, 'clr-col-6':!showDetails}"> --%>
					<div id="details" class="clr-col-6">
						<div
						<tds:hasPermission permission="${Permission.AssetEdit}">
							ondblclick="EntityCrud.showAssetEditView('${assetClass}', ${applicationInstance?.id});"
						</tds:hasPermission>>
							<g:if test="${errors}">
								<div id="messageDivId" class="message">${errors}</div>
							</g:if>
							<g:render template="show" model="[applicationInstance:applicationInstance, shutdownBy: shutdownBy,
											shutdownById: shutdownById, startupBy: startupBy, startupById: startupById,
											testingBy: testingBy, testingById: testingById]" >
							</g:render>
							<%-- TODO: Get tages to render properly... --%>
							<%-- <g:render template="/comment/assetTagsShow" model="[tagAssetList: tagAssetList, tagAssetsFromServer: tagAssetsFromServer]"></g:render> --%>
						</div>
						

						<a class="show-hide-link" id="showHide" onClick="toggleDetailList()">
							View All Fields
						</a>
					</div>
					
				</div>

				<g:render template="/assetEntity/dependentShow" model="[assetEntity:applicationInstance]"></g:render>
				<div id="commentListId">
					<g:render template="/assetEntity/commentList" model="[asset:applicationInstance, 'prefValue': prefValue, 'viewUnpublishedValue': viewUnpublishedValue]" ></g:render>
				</div>
			</div>
		</div>
	</div>

	<g:render template="/assetEntity/showButtons" model="[assetEntity:applicationInstance]"/>
</div>

<script>
	currentMenuId = "#assetMenu";
	$("#assetMenuId a").css('background-color','#003366')
	
	$(document).ready(function() { 
		changeDocTitle('${raw(escapedName)}');
	})

	var showDetails = false;

	function toggleDetailList() {
		// toggle showDetails value
		showDetails = !showDetails;
		
		if (showDetails) {
			$("#showHide").html("Hide Additional Fields");
			$("#details").removeClass("clr-col-6");
			$("#details").addClass("clr-col-12");
			$("#detailsTable").addClass("all-details");
			$("#detailsBody").removeClass("one-column");
			$("#detailsBody").addClass("two-column");
			$("#tab1").html("Details");
		} else {
			$("#showHide").html("View All Fields");
			$("#details").removeClass("clr-col-12");
			$("#details").addClass("clr-col-6");
			$("#detailsTable").removeClass("all-details");
			$("#detailsBody").removeClass("two-column");
			$("#detailsBody").addClass("one-column");
			$("#tab1").html("Summary");

		}
	}
</script>
