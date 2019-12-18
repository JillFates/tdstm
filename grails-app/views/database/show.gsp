<%@page import="com.tdsops.tm.enums.domain.SizeScale"%>
<%@page import="net.transitionmanager.security.Permission"%>
<%@page defaultCodec="html" %>

<div class="legacy-modal-dialog">
	<div class="legacy-modal-content">
		<g:render template="/assetEntity/showHeader" model="[assetEntity:databaseInstance, mode: 'show']"></g:render>
		<div id="modalBody" class="legacy-modal-body">
			<div class="legacy-modal-body-content">
				<div class="clr-row" style="padding-right:20px;">
					<div id="details" class="clr-col-6">
						<div <tds:hasPermission permission="${Permission.AssetEdit}"> ondblclick="EntityCrud.showAssetEditView('${databaseInstance.assetClass}',${databaseInstance?.id})" </tds:hasPermission>>
							<g:if test="${errors}">
								<div id="messageDivId" class="message">${errors}</div>
							</g:if>
							<table id="detailsTable" class="tds-detail-list">
								<tbody id="detailsBody" class="one-column">
									<tds:clrRowDetail style="order: 15" field="${standardFieldSpecs.dbFormat}" value="${databaseInstance.dbFormat}"/>
									<tds:clrRowDetail style="order: 20" field="${standardFieldSpecs.supportType}" value="${databaseInstance.supportType}"/>
									<tds:clrRowDetail style="order: 25" field="${standardFieldSpecs.environment}" value="${databaseInstance.environment}"/>
									<tr style="order: 30">
										<th class="${standardFieldSpecs.size.imp?:''}">
											${standardFieldSpecs.size.label}/${standardFieldSpecs.scale.label}
										</th>
										<td>
											<tds:tooltipSpan field="${standardFieldSpecs.size}">
												${databaseInstance?.size}&nbsp;${databaseInstance.scale?.value()}
											</tds:tooltipSpan>
										</td>
									</tr>
									<tr style="order: 35">
										<tds:clrInputLabel field="${standardFieldSpecs.retireDate}" value="${databaseInstance?.retireDate}"/>
										<td>
											<tds:tooltipSpan field="${standardFieldSpecs.retireDate}">
												<tds:convertDate date="${databaseInstance?.retireDate}"/>
											</tds:tooltipSpan>
										</td>
									</tr>
									<tds:clrRowDetail style="order: 40" field="${standardFieldSpecs.rateOfChange}" value="${databaseInstance.rateOfChange}"/>
									<tr style="order: 45">							
										<tds:clrInputLabel field="${standardFieldSpecs.maintExpDate}" value="${databaseInstance?.maintExpDate}"/>
										<td>
											<tds:tooltipSpan field="${standardFieldSpecs.maintExpDate}">
												<tds:convertDate date="${databaseInstance?.maintExpDate}"/>
											</tds:tooltipSpan>
										</td>
									</tr>
									<tr style="order: 50">							
										<tds:clrInputLabel field="${standardFieldSpecs.planStatus}" value="${databaseInstance?.planStatus}"/>
										<td>
											<tds:tooltipSpan field="${standardFieldSpecs.planStatus}">
												${databaseInstance.planStatus}
											</tds:tooltipSpan>
										</td>
									</tr>
									<tds:clrRowDetail style="order: 55" field="${standardFieldSpecs.externalRefId}" value="${databaseInstance.externalRefId}"/>
									<tr style="order: 60">
										<tds:clrInputLabel field="${standardFieldSpecs.validation}" value="${databaseInstance?.validation}"/>
										<td>
											<tds:tooltipSpan field="${standardFieldSpecs.validation}">
												${databaseInstance.validation}
											</tds:tooltipSpan>
										</td>
									</tr>
									<g:render template="/assetEntity/customShow" model="[assetEntity:databaseInstance, 'project':project]"></g:render>
									<g:render template="/comment/assetTagsShow"></g:render>
								</tbody>
							</table>
						</div>
						<g:render template="/assetEntity/showHideLink"></g:render>
					</div>
				</div>
				<g:render template="/assetEntity/dependentShow" model="[assetEntity:databaseInstance]"></g:render>
				<div id="commentListId">
					<g:render template="/assetEntity/commentList" model="['asset':databaseInstance, 'prefValue': prefValue, 'viewUnpublishedValue': viewUnpublishedValue]" ></g:render>
				</div>
			</div>
		</div>
	</div>
	<g:render template="/assetEntity/showButtons" model="[assetEntity:databaseInstance]"/>
</div>

<script>
	currentMenuId = "#assetMenu";
	$("#assetMenuId a").css('background-color','#003366')

	$(document).ready(function() {
		EntityCrud.loadAssetTags(${databaseInstance?.id});
		changeDocTitle('${raw(escapedName)}');
	})
</script>
