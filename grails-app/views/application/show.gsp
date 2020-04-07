<%@page import="net.transitionmanager.asset.Application"%>
<%@page import="net.transitionmanager.security.Permission"%>
<%@page defaultCodec="html" %> 

<g:set var="assetClass" value="${(new Application()).assetClass}" />

<div class="legacy-modal-dialog">
	<div class="legacy-modal-content">
		<g:render template="/assetEntity/showHeader" model="[assetEntity:applicationInstance, mode: 'show']"></g:render>
		<div id="modalBody" class="legacy-modal-body">
			<div class="legacy-modal-body-content">
				<div class="clr-row" style="padding-right:20px;">
					<div id="details" class="clr-col-6">
						<div <tds:hasPermission permission="${Permission.AssetEdit}"> ondblclick="EntityCrud.showAssetEditView('${assetClass}', ${applicationInstance?.id});"</tds:hasPermission>>
							<g:if test="${errors}">
								<div id="messageDivId" class="message">${errors}</div>
							</g:if>
							<g:render template="show" model="[applicationInstance:applicationInstance, shutdownBy: shutdownBy,
											shutdownById: shutdownById, startupBy: startupBy, startupById: startupById,
											testingBy: testingBy, testingById: testingById]" >
							</g:render>
							<%-- TODO: Get tags to render properly... --%>
							<%-- <g:render template="/comment/assetTagsShow" model="[tagAssetList: tagAssetList, tagAssetsFromServer: tagAssetsFromServer]"></g:render> --%>
						</div>
						<g:render template="/assetEntity/showHideLink"></g:render>
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
</script>
