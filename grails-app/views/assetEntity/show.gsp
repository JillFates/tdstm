<%@page import="com.tdsops.tm.enums.domain.SizeScale"%>
<%@page import="net.transitionmanager.security.Permission"%>
<%@page defaultCodec="html" %>

<g:form method="post">
	<table style="border:0;width:1000px;" class="assetEntity" data-id="${assetEntity?.id}">
		<tr>
			<td colspan="2">
				<div class="dialog"
				<tds:hasPermission permission="${Permission.AssetEdit}">ondblclick="EntityCrud.showAssetEditView('${assetEntity.assetClass}', ${assetEntity?.id})"</tds:hasPermission>
				>
				<g:if test="${errors}">
					<div id="messageDivId" class="message">${errors}</div>
				</g:if>
				<table>
					<tbody>
					<tr  class="prop">
						<tds:inputLabel field="${standardFieldSpecs.assetName}" value="${assetEntity.assetName}"/>
						<td colspan="2" style="font-weight:bold;" class="${standardFieldSpecs.validation.imp}"><span data-toggle="popover" data-trigger="hover" data-content="${raw(standardFieldSpecs.assetName.tip)}">${assetEntity.assetName}</span></td>
						
						<tds:inputLabel field="${standardFieldSpecs.description}" value="${assetEntity.description}"/>
						<td colspan="3" class="${standardFieldSpecs.validation.imp?:''}"><span data-toggle="popover" data-trigger="hover" data-content="${raw(standardFieldSpecs.description.tip)}">${assetEntity.description}</span></td>
					</tr>
					<tr class="prop">
						<tds:showLabelAndField field="${standardFieldSpecs.assetType}" value="${assetEntity.assetType}"/>
						
						<tds:showLabelAndField field="${standardFieldSpecs.environment}" value="${assetEntity.environment}"/>
						
						<td></td>
						<td class="label_sm">Source</td>
						<td class="label_sm">Target</td>
					</tr>
					<tr class="prop">
						<tds:inputLabel field="${standardFieldSpecs.manufacturer}" value="${assetEntity.manufacturer}"/>
						<td class="valueNW ${standardFieldSpecs.manufacturer.imp?:''}">
							<span data-toggle="popover" data-trigger="hover" data-content="${raw(standardFieldSpecs.manufacturer.tip)}">
								<a href='javascript:showManufacturer(${assetEntity.manufacturer?.id})' style='color:#00E'>${assetEntity.manufacturer}</a>
							</span>
						</td>
						
						<tds:showLabelAndField field="${standardFieldSpecs.priority}" value="${assetEntity.priority}"/>

						<td class="label ${standardFieldSpecs.sourceLocation.imp?:''}" nowrap="nowrap"><label for="sourceLocation">Location</label></td>

						<td class="valueNW ${standardFieldSpecs.sourceLocation.imp?:''}">${assetEntity.sourceLocation}</td>
						<td class="valueNW ${standardFieldSpecs.sourceLocation.imp?:''}">${assetEntity.targetLocation}</td>


					</tr>
					<tr class="prop">
						<tds:inputLabel field="${standardFieldSpecs.model}" value="${assetEntity.model}"/>
						<td class="valueNW ${standardFieldSpecs.model.imp?:''}">
						<span data-toggle="popover" data-trigger="hover" data-content="${raw(standardFieldSpecs.model.tip)}">
						<a href='javascript:showModel(${assetEntity.model?.id})' style='color:#00E'>${assetEntity.model}</a>
							<g:if test="${! assetEntity.model?.isValid()}"> <span style="color: red;"><b>?</b></span></g:if>
						</span>
						</td>
						
						<tds:showLabelAndField field="${standardFieldSpecs.ipAddress}" value="${assetEntity.ipAddress}"/>

						<td class="label ${standardFieldSpecs.sourceRoom.imp?:''}" nowrap="nowrap">
							<label for="sourceRoom">Room</label>
						</td>
						<tds:labelForShowField field="${standardFieldSpecs.sourceRoom}" value="${roomSource?.roomName}"/>
						<tds:labelForShowField field="${standardFieldSpecs.targetRoom}" value="${roomTarget?.roomName}"/>

					</tr>
					<tr class="prop">
						<tds:showLabelAndField field="${standardFieldSpecs.shortName}" value="${assetEntity.shortName}"/>

						<tds:showLabelAndField field="${standardFieldSpecs.os}" value="${assetEntity.os}"/>

						<%-- The following fields will be displayed based on the assetType --%>
						<%-- rackable --%>
						<td class="label rackLabel ${standardFieldSpecs.sourceRack.imp?:''}"  nowrap="nowrap" id="rackId">
							<label for="sourceRackId">Rack/Cab</label>
						</td>
						<td class="rackLabel ${standardFieldSpecs.sourceRack.imp?:''}" data-toggle="popover" data-trigger="hover" data-content="${raw(standardFieldSpecs.sourceRack.tip)}">${assetEntity.rackSource?.tag}</td>
						<td class="rackLabel ${standardFieldSpecs.targetRack.imp?:''}" data-toggle="popover" data-trigger="hover" data-content="${raw(standardFieldSpecs.targetRack.tip)}">${assetEntity.rackTarget?.tag}</td>
						<%-- blade --%>
						<td class="label bladeLabel ${standardFieldSpecs.sourceChassis.imp?:''}" nowrap="nowrap" id="bladeId" style="display: none">
							<label for="sourceChassisId">Blade Chassis</label>
						</td>
						<td class="bladeLabel ${standardFieldSpecs.sourceChassis.imp?:''}" style="display: none" data-toggle="popover" data-trigger="hover" data-content="${raw(standardFieldSpecs.sourceChassis.tip)}">
							${sourceChassis}
						</td>
						<td class="bladeLabel ${standardFieldSpecs.targetChassis.imp?:''}" style="display: none" data-toggle="popover" data-trigger="hover" data-content="${raw(standardFieldSpecs.targetChassis.tip)}">
							${targetChassis}
						</td>

					</tr>
					<tr class="prop">
						<tds:showLabelAndField field="${standardFieldSpecs.serialNumber}" value="${assetEntity.serialNumber}"/>

						<tds:showLabelAndField field="${standardFieldSpecs.supportType}" value="${assetEntity.supportType}"/>

						<td class="label ${standardFieldSpecs.sourceRackPosition.imp?:''}" nowrap="nowrap"><label for="sourceRack">Position</label></td>
						<td class="rackLabel valueNW ${standardFieldSpecs.sourceRackPosition.imp?:''}">${assetEntity.sourceRackPosition}</td>
						<td class="rackLabel valueNW ${standardFieldSpecs.targetRackPosition.imp?:''}">${assetEntity.targetRackPosition}</td>
						<td class="bladeLabel ${standardFieldSpecs.sourceBladePosition.imp?:''}" style="display: none" >${assetEntity.sourceBladePosition}</td>
						<td class="bladeLabel ${standardFieldSpecs.targetBladePosition.imp?:''}" style="display: none" >${assetEntity.targetBladePosition}</td>

					</tr>
					<tr class="prop">
						<tds:showLabelAndField field="${standardFieldSpecs.assetTag}" value="${assetEntity.assetTag}"/>

						<tds:inputLabel field="${standardFieldSpecs.retireDate}" value="${assetEntity.retireDate}"/>
						<td class="valueNW ${standardFieldSpecs.retireDate.imp?:''}">
						<span data-toggle="popover" data-trigger="hover" data-content="${raw(standardFieldSpecs.retireDate.tip)}">
							<tds:convertDate date="${assetEntity?.retireDate}" />
						</span>
						</td>
						<td class="label ${standardFieldSpecs.moveBundle.imp?:''}" nowrap="nowrap"><label for="moveBundle" data-toggle="popover" data-trigger="hover" data-content="${raw(standardFieldSpecs.moveBundle.tip)}">Bundle : Dep. Group</label></td>
						<td class="valueNW ${standardFieldSpecs.moveBundle.imp?:''}">
							<span data-toggle="popover" data-trigger="hover" data-content="${raw(standardFieldSpecs.moveBundle.tip)}">
								${assetEntity.moveBundle}${(dependencyBundleNumber != null)?' : ' : ''}${dependencyBundleNumber}
							</span>
						</td>

						<td class="label ${standardFieldSpecs.size.imp?:''}" nowrap="nowrap"><label for="size" data-toggle="popover" data-trigger="hover" data-content="${raw(standardFieldSpecs.size.tip)}">Size/Scale </label></td>
						<td nowrap="nowrap" class="sizeScale ${standardFieldSpecs.size.imp?:''}">
							<span data-toggle="popover" data-trigger="hover" data-placement="bottom" data-content="${raw(standardFieldSpecs.size.tip)}">
								${assetEntity.size} ${assetEntity.scale?.value()}
							</span>
						</td>
					</tr>
					<tr class="prop">
						<tds:showLabelAndField field="${standardFieldSpecs.railType}" value="${assetEntity.railType}"/>

						<tds:inputLabel field="${standardFieldSpecs.maintExpDate}" value="${assetEntity.maintExpDate}"/>
						<td class="valueNW ${standardFieldSpecs.maintExpDate.imp?:''}">
							<span data-toggle="popover" data-trigger="hover" data-content="${raw(standardFieldSpecs.maintExpDate.tip)}">
								<tds:convertDate date="${assetEntity?.maintExpDate}" />
							</span>
						</td>

						<tds:showLabelAndField field="${standardFieldSpecs.planStatus}" value="${assetEntity.planStatus}"/>

						<tds:showLabelAndField field="${standardFieldSpecs.rateOfChange}" value="${assetEntity.rateOfChange}"/>

					</tr>
					<tr>
						<tds:showLabelAndField field="${standardFieldSpecs.externalRefId}" value="${assetEntity.externalRefId}"/>

						<g:if test="! assetEntity.isVM()">
							<td class="label ${standardFieldSpecs.truck.imp?:''}" nowrap="nowrap">
								<label for="truck" data-toggle="popover" data-trigger="hover" data-content="${raw(standardFieldSpecs.truck.tip)}">Truck/Cart/Shelf</label>
							</td>
							<td class="valueNW ${standardFieldSpecs.truck.imp?:''}">
								<span data-toggle="popover" data-trigger="hover" data-content="${raw(standardFieldSpecs.truck.tip)}">
									${assetEntity.truck ?: '   '} / ${assetEntity.cart ?: '   '} / ${assetEntity.shelf ?: '   '}
								</span>
							</td>
						</g:if>
						<tds:showLabelAndField field="${standardFieldSpecs.validation}" value="${assetEntity.validation}"/>
						<td>&nbsp;</td>
					</tr>
					<g:render template="customShow" ></g:render>
					</tbody>
				</table>
				</div>
			</td>
		</tr>
		<tr id="deps">
			<g:render template="dependentShow" model="[dependent:dependentAssets, support:supportAssets]"></g:render>
		</tr>
		<tr id="commentListId">
			<g:render template="commentList" model="['asset':assetEntity, 'prefValue': prefValue, 'viewUnpublishedValue': viewUnpublishedValue, 'hasPublishPermission':hasPublishPermission, 'canEdit': canEdit]" ></g:render>
		</tr>
		<tr>
			<td colspan="2">
				<div class="buttons">
					<input name="attributeSet.id" type="hidden" value="1">
					<input name="project.id" type="hidden" value="${projectId}">
					<input type="hidden" name="id" id="assetsId" value="${assetEntity?.id}" />
					<input type ="hidden" id = "dstPath" name = "dstPath" value ="${redirectTo}"/>
					<g:render template="showButtons" model="[assetEntity:assetEntity, escapedName:escapedName]" />
				</div>
			</td>
		</tr>
	</table>
</g:form>

<script type="text/javascript">
	$(document).ready(function() {
		var assetType = "${assetEntity.assetType}"

		EntityCrud.toggleAssetTypeFields( assetType );

		changeDocTitle('${escapedName}');
	});
</script>
