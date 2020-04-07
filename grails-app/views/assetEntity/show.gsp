<%@page import="com.tdsops.tm.enums.domain.SizeScale"%>
<%@page import="net.transitionmanager.security.Permission"%>
<%@page defaultCodec="html" %>

<g:form method="post">
	<div class="legacy-modal-dialog">
		<div class="legacy-modal-content">
		<g:render template="/assetEntity/showHeader" model="[assetEntity:assetEntity, mode: 'show']"></g:render>
			<div id="modalBody" class="legacy-modal-body">
				<div class="legacy-modal-body-content">
					<div class="clr-row" style="padding-right:20px;">
						<div id="details" class="clr-col-6">
							<div <tds:hasPermission permission="${Permission.AssetEdit}">ondblclick="EntityCrud.showAssetEditView('${assetEntity.assetClass}', ${assetEntity?.id})"</tds:hasPermission>>
								<g:if test="${errors}">
									<div id="messageDivId" class="message">${errors}</div>
								</g:if>
								<table id="detailsTable" class="tds-detail-list">
									<tbody id="detailsBody" class="one-column">
										<tds:clrRowDetail style="order: 15" field="${standardFieldSpecs.assetType}" value="${assetEntity.assetType}"/>	
										<tds:clrRowDetail style="order: 20" field="${standardFieldSpecs.environment}" value="${assetEntity.environment}"/>
										
										<%-- TODO: Fix this. Divs not showing in markup, but inner content is. --%>
										<div style="order: 25" class="source-target-wrapper">
											<tr>
												<th class="${standardFieldSpecs.locationSource.imp?:''} header-label">Source</th>
											</tr>
											<tr>
												<th class="${standardFieldSpecs.locationSource.imp?:''}">
													Location
												</th>
												<td>
													<tds:tooltipSpan field="${standardFieldSpecs.locationSource}" >
														${assetEntity.sourceLocationName}
													</tds:tooltipSpan>
												</td>
											</tr>
											<tr>
												<th class="${standardFieldSpecs.roomSource.imp?:''}">
													Room
												</th>
												<td>
													<tds:tooltipSpan field="${standardFieldSpecs.roomSource}" >
														${roomSource?.roomName}
													</tds:tooltipSpan>
												</td>
											</tr>
											<tr>
												<th class="${standardFieldSpecs.rackSource.imp?:''}">
													Rack/Cab
												</th>
												<td>
													<tds:tooltipSpan field="${standardFieldSpecs.rackSource}" >
														${assetEntity.rackSource?.tag}
													</tds:tooltipSpan>
												</td>
											</tr>
											<tr>
												<%-- TODO: display: none??? --%>
												<th class="${standardFieldSpecs.sourceChassis.imp?:''}" nowrap="nowrap" id="bladeId" style="display: none">
													Blade Chassis
												</th>
												<td>
													<tds:tooltipSpan field="${standardFieldSpecs.sourceChassis}" >
														${sourceChassis}
													</tds:tooltipSpan>
												</td>
											</tr>
											<tr>
												<th class="${standardFieldSpecs.sourceRackPosition.imp?:''}">
													Position
												</th>
												<td>
													<tds:tooltipSpan tooltipDataPlacement="bottom" field="${standardFieldSpecs.sourceRackPosition}">
														${assetEntity.sourceRackPosition}
													</tds:tooltipSpan>
												</td>
											</tr>
											<%-- TODO: Update this? --%>
											<tr>
												<td class="bladeLabel ${standardFieldSpecs.sourceBladePosition.imp?:''}" style="display: none" >
													<tds:tooltipSpan tooltipDataPlacement="bottom" field="${standardFieldSpecs.sourceBladePosition}">
														${assetEntity.sourceBladePosition}
													</tds:tooltipSpan>
												</td>
											</tr>
										</div>

										<div style="order: 30" class="source-target-wrapper">
											<tr>
												<th class="${standardFieldSpecs.locationTarget.imp?:''} header-label">Target</th>
											</tr>
											<tr>
												<th class="${standardFieldSpecs.locationTarget.imp?:''}">
													Location
												</th>
												<td>
													<tds:tooltipSpan field="${standardFieldSpecs.locationTarget}" >
														${assetEntity.targetLocationName}
													</tds:tooltipSpan>
												</td>
											</tr>
											<tr>
												<th class="${standardFieldSpecs.roomTarget.imp?:''}">
													Room
												</th>
												<td>
													<tds:tooltipSpan tooltipDataPlacement="bottom" field="${standardFieldSpecs.roomTarget}" >
														${roomTarget?.roomName}
													</tds:tooltipSpan>
												</td>
											</tr>
											<tr>
												<th class="${standardFieldSpecs.rackTarget.imp?:''}">
													Rack/Cab
												</th>
												<td>
													<tds:tooltipSpan field="${standardFieldSpecs.rackTarget}" >
														${assetEntity.rackTarget?.tag}
													</tds:tooltipSpan>
												</td>
											</tr>
											<tr>
												<th class="${standardFieldSpecs.targetChassis.imp?:''}" nowrap="nowrap" id="bladeId" style="display: none">
													Blade Chassis
												</th>
												<td>
													<tds:tooltipSpan field="${standardFieldSpecs.targetChassis}">
														${targetChassis}
													</tds:tooltipSpan>
												</td>
											</tr>
											<tr>
												<th class="${standardFieldSpecs.targetRackPosition.imp?:''}">
													Position
												</th>
												<td>
													<tds:tooltipSpan tooltipDataplacement="bottom" field="${standardFieldSpecs.targetRackPosition}">
														${assetEntity.targetRackPosition}
													</tds:tooltipSpan>
												</td>
											</tr>
											<%-- TODO: Update this? --%>
											<tr>
												<td class="bladeLabel ${standardFieldSpecs.targetBladePosition.imp?:''}" style="display: none" >
													<tds:tooltipSpan tooltipDataPlacement="bottom" field="${standardFieldSpecs.targetBladePosition}">
														${assetEntity.targetBladePosition}
													</tds:tooltipSpan>
												</td>
											</tr>
										</div>

										<tr style="order: 35">
											<tds:clrInputLabel field="${standardFieldSpecs.manufacturer}" value="${assetEntity.manufacturer}"/>
											<td class="valueNW ${standardFieldSpecs.manufacturer.imp?:''}">
												<tds:tooltipSpan field="${standardFieldSpecs.manufacturer}">
													<a href='javascript:showManufacturer(${assetEntity.manufacturer?.id})' style='color:#00E'>${assetEntity.manufacturer}</a>
												</tds:tooltipSpan>
											</td>
										</tr>
											
										<tds:clrRowDetail style="order: 40" field="${standardFieldSpecs.priority}" value="${assetEntity.priority}"/>

										<tr style="order: 45">
											<tds:clrInputLabel field="${standardFieldSpecs.model}" value="${assetEntity.model}"/>
											<td>
												<tds:tooltipSpan field="${standardFieldSpecs.model}">
												<a href='javascript:showModel(${assetEntity.model?.id})' style='color:#00E'>${assetEntity.model}</a>
													<g:if test="${! assetEntity.model?.isValid()}"> <span style="color: red;"><b>?</b></span></g:if>
												</tds:tooltipSpan>
											</td>
										</tr>
											
										<tds:clrRowDetail style="order: 50" field="${standardFieldSpecs.ipAddress}" value="${assetEntity.ipAddress}"/>
										<tds:clrRowDetail style="order: 55" field="${standardFieldSpecs.shortName}" value="${assetEntity.shortName}"/>
										<tds:clrRowDetail style="order: 60" field="${standardFieldSpecs.os}" value="${assetEntity.os}"/>
										<tds:clrRowDetail style="order: 65" field="${standardFieldSpecs.serialNumber}" value="${assetEntity.serialNumber}"/>
										<tds:clrRowDetail style="order: 70" field="${standardFieldSpecs.supportType}" value="${assetEntity.supportType}"/>
										<tds:clrRowDetail style="order: 80" field="${standardFieldSpecs.assetTag}" value="${assetEntity.assetTag}"/>

										<tr style="order: 85">
											<tds:clrInputLabel field="${standardFieldSpecs.retireDate}" value="${assetEntity.retireDate}"/>
											<td>
												<tds:tooltipSpan field="${standardFieldSpecs.retireDate}">
													<tds:convertDate date="${assetEntity?.retireDate}" />
												</tds:tooltipSpan>
											</td>
										</tr>

										<tr style="order: 75">
											<td class="${standardFieldSpecs.size.imp?:''}">
												<label for="size" data-toggle="popover" data-trigger="hover" data-content="${standardFieldSpecs.size.tip?: standardFieldSpecs.size.label}">
													Size/Scale
												</label>
											</td>
											<td>
												<tds:tooltipSpan tooltipDataPlacement="bottom" field="${standardFieldSpecs.size}">
													${assetEntity.size} ${assetEntity.scale?.value()}
												</tds:tooltipSpan>
											</td>
										</tr>

										<tds:clrRowDetail style="order: 95" field="${standardFieldSpecs.railType}" value="${assetEntity.railType}"/>
										
										<tr style="order: 100">
											<tds:clrInputLabel field="${standardFieldSpecs.maintExpDate}" value="${assetEntity.maintExpDate}"/>
											<td>
												<tds:tooltipSpan field="${standardFieldSpecs.maintExpDate}">
													<tds:convertDate date="${assetEntity?.maintExpDate}" />
												</tds:tooltipSpan>
											</td>
										</tr>

										<tds:clrRowDetail style="order: 90" field="${standardFieldSpecs.planStatus}" value="${assetEntity.planStatus}"/>
										<tds:clrRowDetail style="order: 110" field="${standardFieldSpecs.rateOfChange}" value="${assetEntity.rateOfChange}"/>
										<tds:clrRowDetail style="order: 115" field="${standardFieldSpecs.externalRefId}" value="${assetEntity.externalRefId}"/>

										<g:if test="! assetEntity.isVM()">
											<tr style="order: 120">
												<th class="${standardFieldSpecs.truck.imp?:''}">
													<label for="truck" data-toggle="popover" data-trigger="hover" data-content="${standardFieldSpecs.truck.tip?: standardFieldSpecs.truck.label}">
														Truck/Cart/Shelf
													</label>
												</th>
												<td>
													<tds:tooltipSpan field="${standardFieldSpecs.truck}">
														${assetEntity.truck ?: '   '} / ${assetEntity.cart ?: '   '} / ${assetEntity.shelf ?: '   '}
													</tds:tooltipSpan>
												</td>
											</tr>
										</g:if>

										<tds:clrRowDetail style="order: 105" field="${standardFieldSpecs.validation}" value="${assetEntity.validation}"/>

										<g:render template="customShow"></g:render>
										<g:render template="/comment/assetTagsShow"></g:render>
									</tbody>
								</table>
							</div>
							<g:render template="/assetEntity/showHideLink"></g:render>
						</div>
					</div>

					<g:render template="dependentShow" model="[dependent:dependentAssets, support:supportAssets]"></g:render>
					<div id="commentListId">
						<g:render template="commentList" model="['asset':assetEntity, 'prefValue': prefValue, 'viewUnpublishedValue': viewUnpublishedValue, 'hasPublishPermission':hasPublishPermission, 'canEdit': canEdit]" ></g:render>
					</div>
<%-- 
					<tr>
						<td colspan="2">
							<div class="buttons">
								<input name="attributeSet.id" type="hidden" value="1">
								<input name="project.id" type="hidden" value="${projectId}">
								<input type="hidden" name="id" id="assetsId" value="${assetEntity?.id}" />
								<input type ="hidden" id = "dstPath" name = "dstPath" value ="${redirectTo}"/>
							</div>
						</td>
					</tr> --%>
				</div>
			</div>
		</div>
		<g:render template="showButtons" model="[assetEntity:assetEntity, escapedName:escapedName]" />
	</div>
</g:form>

<script type="text/javascript">
	$(document).ready(function() {
		var assetType = "${assetEntity.assetType}"

		EntityCrud.toggleAssetTypeFields( assetType );
		EntityCrud.loadAssetTags(${assetEntity?.id});
		changeDocTitle('${raw(escapedName)}');
	});
</script>
