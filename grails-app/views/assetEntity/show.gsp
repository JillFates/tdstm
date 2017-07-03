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
						<td colspan="2" style="font-weight:bold;" class="${standardFieldSpecs.validation.imp}">${assetEntity.assetName}</td>
						
						<tds:inputLabel field="${standardFieldSpecs.description}" value="${assetEntity.description}"/>
						<td colspan="3" class="${standardFieldSpecs.validation.imp?:''}" >${assetEntity.description}</td>
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
						<td class="valueNW ${standardFieldSpecs.manufacturer.imp?:''}"><a href='javascript:showManufacturer(${assetEntity.manufacturer?.id})' style='color:#00E'>${assetEntity.manufacturer}</a></td>
						
						<tds:showLabelAndField field="${standardFieldSpecs.priority}" value="${assetEntity.priority}"/>
						
						<tds:inputLabel field="${standardFieldSpecs.sourceLocation}" value="${assetEntity.sourceLocation}"/>
						<tds:labelForShowField field="${standardFieldSpecs.sourceLocation}" value="${assetEntity.sourceLocation}"/>
						<tds:labelForShowField field="${standardFieldSpecs.targetLocation}" value="${assetEntity.targetLocation}"/>

					</tr>
					<tr class="prop">
						<tds:inputLabel field="${standardFieldSpecs.model}" value="${assetEntity.model}"/>
						<td class="valueNW ${standardFieldSpecs.model.imp?:''}"><a href='javascript:showModel(${assetEntity.model?.id})' style='color:#00E'>${assetEntity.model}</a>
							<g:if test="${! assetEntity.model?.isValid()}"> <span style="color: red;"><b>?</b></span></g:if>
						</td>
						
						<tds:showLabelAndField field="${standardFieldSpecs.ipAddress}" value="${assetEntity.ipAddress}"/>
						
						<tds:inputLabel field="${standardFieldSpecs.sourceRoom}" value="${assetEntity.sourceRoom}"/>
						<tds:labelForShowField field="${standardFieldSpecs.sourceRoom}" value="${roomSource?.roomName}"/>
						<tds:labelForShowField field="${standardFieldSpecs.targetRoom}" value="${roomTarget?.roomName}"/>

					</tr>
					<tr class="prop">
						<tds:showLabelAndField field="${standardFieldSpecs.shortName}" value="${assetEntity.shortName}"/>

						<tds:showLabelAndField field="${standardFieldSpecs.os}" value="${assetEntity.os}"/>

						<%-- The following fields will be displayed based on the assetType --%>
						<%-- rackable --%>
						<tds:inputLabel field="${standardFieldSpecs.sourceRack}" value="${assetEntity.sourceRack}"/>

						<tds:labelForShowField field="${standardFieldSpecs.sourceRack}" value="${assetEntity.rackSource?.tag}"/>
						<tds:labelForShowField field="${standardFieldSpecs.targetRack}" value="${assetEntity.rackTarget?.tag}"/>

						<%-- blade --%>
						<tds:inputLabel field="${standardFieldSpecs.sourceChassis}" value="${assetEntity.sourceChassis}"/>
						<td class="bladeLabel ${standardFieldSpecs.sourceChassis.imp?:''}" style="display: none">
							${sourceChassis}
						</td>
						<td class="bladeLabel ${standardFieldSpecs.targetChassis.imp?:''}" style="display: none" >
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
							<tds:convertDate date="${assetEntity?.retireDate}" />
						</td>
						<td class="label ${standardFieldSpecs.retireDate.imp?:''}" nowrap="nowrap"><label for="moveBundle">Bundle : Dep. Group</label></td>
						<td class="valueNW ${standardFieldSpecs.moveBundle.imp?:''}">${assetEntity.moveBundle}${(dependencyBundleNumber != null)?' : ' : ''}${dependencyBundleNumber}</td>
						
						<td class="label ${standardFieldSpecs.moveBundle.imp?:''}" nowrap="nowrap"><label for="size">Size/Scale </label></td>
						<td nowrap="nowrap" class="sizeScale ${standardFieldSpecs.size.imp?:''}">
							${assetEntity.size} ${assetEntity.scale?.value()}
						</td>
					</tr>
					<tr class="prop">
						<tds:showLabelAndField field="${standardFieldSpecs.railType}" value="${assetEntity.railType}"/>

						<tds:inputLabel field="${standardFieldSpecs.maintExpDate}" value="${assetEntity.maintExpDate}"/>
						<td class="valueNW ${standardFieldSpecs.maintExpDate.imp?:''}">
							<tds:convertDate date="${assetEntity?.maintExpDate}" />
						</td>

						<tds:showLabelAndField field="${standardFieldSpecs.planStatus}" value="${assetEntity.planStatus}"/>

						<tds:showLabelAndField field="${standardFieldSpecs.rateOfChange}" value="${assetEntity.rateOfChange}"/>

					</tr>
					<tr>
						<tds:showLabelAndField field="${standardFieldSpecs.externalRefId}" value="${assetEntity.externalRefId}"/>

						<g:if test="! assetEntity.isVM()">
							<td class="label ${standardFieldSpecs.truck.imp?:''}" nowrap="nowrap">
								<label for="truck">Truck/Cart/Shelf</label>
							</td>
							<td class="valueNW ${standardFieldSpecs.truck.imp?:''}">
								${assetEntity.truck ?: '   '} / ${assetEntity.cart ?: '   '} / ${assetEntity.shelf ?: '   '}
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
