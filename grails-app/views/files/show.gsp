<%@page import="com.tdsops.tm.enums.domain.SizeScale"%>
<%@page import="net.transitionmanager.security.Permission"%>
<%@page defaultCodec="html" %>

<div class="legacy-modal-dialog">
	<div class="legacy-modal-content">
		<g:render template="/assetEntity/showHeader" model="[assetEntity:filesInstance, mode: 'show']"></g:render>
		<div id="modalBody" class="legacy-modal-body">
			<div class="legacy-modal-body-content">
				<div class="clr-row" style="padding-right:20px;">
					<div id="details" class="clr-col-6">
						<div <tds:hasPermission permission="${Permission.AssetEdit}"> ondblclick="EntityCrud.showAssetEditView('${filesInstance?.assetClass}',${filesInstance?.id})"</tds:hasPermission>>
							<g:if test="${errors}">
								<div id="messageDivId" class="message">${errors}</div>
							</g:if>
							<table id="detailsTable" class="tds-detail-list">
								<tbody id="detailsBody" class="one-column">
									<tds:clrRowDetail style="order: 15" field="${standardFieldSpecs.fileFormat}" value="${filesInstance.fileFormat}"/>
									<tds:clrRowDetail style="order: 20" field="${standardFieldSpecs.LUN}" value="${filesInstance.LUN}"/>
									<tds:clrRowDetail style="order: 25" field="${standardFieldSpecs.supportType}" value="${filesInstance.supportType}"/>
									<tr style="order: 30">
										<th class="${standardFieldSpecs.size.imp?:''}">
												${standardFieldSpecs.size.label}/${standardFieldSpecs.scale.label}
										</th>
										<td>
											<tds:tooltipSpan field="${standardFieldSpecs.size}">
												${filesInstance.size}&nbsp;&nbsp;${filesInstance.scale?.value()}
											</tds:tooltipSpan>
										</td>
									</tr>

									<tds:clrRowDetail style="order: 35" field="${standardFieldSpecs.externalRefId}" value="${filesInstance.externalRefId}"/>
									<tds:clrRowDetail style="order: 40" field="${standardFieldSpecs.environment}" value="${filesInstance.environment}"/>
									<tds:clrRowDetail style="order: 45" field="${standardFieldSpecs.planStatus}" value="${filesInstance.planStatus}" tooltipDataPlacement="bottom"/>
									<tds:clrRowDetail style="order: 50" field="${standardFieldSpecs.rateOfChange}" value="${filesInstance.rateOfChange}"/>
									<tds:clrRowDetail style="order: 55" field="${standardFieldSpecs.validation}" value="${filesInstance.validation}" tooltipDataPlacement="bottom"/>
									
									<g:render template="/assetEntity/customShow" model="[assetEntity:filesInstance]"></g:render>
									<g:render template="/comment/assetTagsShow"></g:render>
								</tbody>
							</table>
						</div>
						<g:render template="/assetEntity/showHideLink"></g:render>
					</div>
				</div>
				<g:render template="/assetEntity/dependentShow" model="[assetEntity:filesInstance]" ></g:render>
				<div id="commentListId">
					<g:render template="/assetEntity/commentList" model="['asset':filesInstance, 'prefValue': prefValue, 'viewUnpublishedValue': viewUnpublishedValue]"></g:render>
				</div>
			</div>
		</div>
	</div>
	<g:form>
		<input type="hidden" name="id" id="filedeleteId" value="${filesInstance?.id}" />
		<g:render template="/assetEntity/showButtons" model="[assetEntity:filesInstance]" />
	</g:form>
</div>
<script>
	currentMenuId = "#assetMenu";
	$("#assetMenuId a").css('background-color','#003366')

	$(document).ready(function() {
		changeDocTitle('${raw(escapedName)}');
		EntityCrud.loadAssetTags(${filesInstance?.id});
	});
</script>

