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
						<td colspan="2" style="font-weight:bold;" class="${standardFieldSpecs.assetName.imp}">
							<tds:tooltipSpan field="${standardFieldSpecs.assetName}">
								${assetEntity.assetName}
							</tds:tooltipSpan>
						</td>
						
						<tds:inputLabel field="${standardFieldSpecs.description}" value="${assetEntity.description}"/>
						<td colspan="3" class="${standardFieldSpecs.description.imp?:''}">
							<tds:tooltipSpan field="${standardFieldSpecs.description}">
								${assetEntity.description}
							</tds:tooltipSpan>
						</td>
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
							<tds:tooltipSpan field="${standardFieldSpecs.manufacturer}">
								<a href='javascript:showManufacturer(${assetEntity.manufacturer?.id})' style='color:#00E'>${assetEntity.manufacturer}</a>
							</tds:tooltipSpan>
						</td>
						
						<tds:showLabelAndField field="${standardFieldSpecs.priority}" value="${assetEntity.priority}"/>

						<td class="label ${standardFieldSpecs.locationSource.imp?:''}" nowrap="nowrap">
							<label for="locationSource" data-toggle="popover" data-trigger="hover" data-content="Location">Location</label>
						</td>
												<td class="valueNW ${standardFieldSpecs.locationSource.imp?:''}" >
														<tds:tooltipSpan field="${standardFieldSpecs.locationSource}" >
																${assetEntity.sourceLocationName}
														</tds:tooltipSpan>
												</td>
												<td class="valueNW ${standardFieldSpecs.locationTarget.imp?:''}" >
														<tds:tooltipSpan field="${standardFieldSpecs.locationTarget}" >
																${assetEntity.targetLocationName}
														</tds:tooltipSpan>
												</td>

					</tr>
					<tr class="prop">
						<tds:inputLabel field="${standardFieldSpecs.model}" value="${assetEntity.model}"/>
						<td class="valueNW ${standardFieldSpecs.model.imp?:''}">
						<tds:tooltipSpan field="${standardFieldSpecs.model}">
						<a href='javascript:showModel(${assetEntity.model?.id})' style='color:#00E'>${assetEntity.model}</a>
							<g:if test="${! assetEntity.model?.isValid()}"> <span style="color: red;"><b>?</b></span></g:if>
						</tds:tooltipSpan>
						</td>
						
						<tds:showLabelAndField field="${standardFieldSpecs.ipAddress}" value="${assetEntity.ipAddress}"/>

						<td class="label nonVMLabel ${standardFieldSpecs.roomSource.imp?:''}" nowrap="nowrap">
							<label for="roomSource" data-toggle="popover" data-trigger="hover" data-content="Room">Room</label>
						</td>

						<td class="valueNW nonVMLabel ${standardFieldSpecs.roomSource.imp?:''}" >
							<tds:tooltipSpan field="${standardFieldSpecs.roomSource}" >
								${roomSource?.roomName}
							</tds:tooltipSpan>
						</td>
						<td class="valueNW nonVMLabel ${standardFieldSpecs.roomTarget.imp?:''}">
							<tds:tooltipSpan tooltipDataPlacement="bottom" field="${standardFieldSpecs.roomTarget}" >
								${roomTarget?.roomName}
							</tds:tooltipSpan>
						</td>

					</tr>
					<tr class="prop">
						<tds:showLabelAndField field="${standardFieldSpecs.shortName}" value="${assetEntity.shortName}"/>

						<tds:showLabelAndField field="${standardFieldSpecs.os}" value="${assetEntity.os}"/>

						<%-- The following fields will be displayed based on the assetType --%>
						<%-- rackable --%>
						<td class="label rackLabel ${standardFieldSpecs.rackSource.imp?:''}"  nowrap="nowrap" id="rackId">
							<label for="rackSourceId" data-toggle="popover" data-trigger="hover" data-content="Rack/Cab">Rack/Cab</label>
						</td>
												<td class="valueNW rackLabel nonVMLabel ${standardFieldSpecs.rackSource.imp?:''}" >
														<tds:tooltipSpan field="${standardFieldSpecs.rackSource}" >
																${assetEntity.rackSource?.tag}
														</tds:tooltipSpan>
												</td>
												<td class="valueNW rackLabel nonVMLabel ${standardFieldSpecs.rackTarget.imp?:''}" >
														<tds:tooltipSpan field="${standardFieldSpecs.rackTarget}" >
																${assetEntity.rackTarget?.tag}
														</tds:tooltipSpan>
												</td>

						<%-- blade --%>
						<td class="label bladeLabel ${standardFieldSpecs.sourceChassis.imp?:''}" nowrap="nowrap" id="bladeId" style="display: none">
							<label for="sourceChassisId" data-toggle="popover" data-trigger="hover" data-content="Blade Chassis">Blade Chassis</label>
						</td>
						<td class="bladeLabel ${standardFieldSpecs.sourceChassis.imp?:''}" >
														<tds:tooltipSpan field="${standardFieldSpecs.sourceChassis}" >
																${sourceChassis}
														</tds:tooltipSpan>
						</td>
						<td class="bladeLabel ${standardFieldSpecs.targetChassis.imp?:''}">
														<tds:tooltipSpan field="${standardFieldSpecs.targetChassis}" >
																${targetChassis}
														</tds:tooltipSpan>
						</td>

					</tr>
					<tr class="prop">
						<tds:showLabelAndField field="${standardFieldSpecs.serialNumber}" value="${assetEntity.serialNumber}"/>

						<tds:showLabelAndField field="${standardFieldSpecs.supportType}" value="${assetEntity.supportType}"/>

						<td class="label positionLabel ${standardFieldSpecs.sourceRackPosition.imp?:''}" nowrap="nowrap">
							<label for="rackSource" data-toggle="popover" data-trigger="hover" data-placement="bottom" data-content="${standardFieldSpecs.sourceRackPosition.tip?: standardFieldSpecs.sourceRackPosition.label}">
								Position
							</label>
						</td>
						<td class="rackLabel valueNW ${standardFieldSpecs.sourceRackPosition.imp?:''}">
							<tds:tooltipSpan tooltipDataPlacement="bottom" field="${standardFieldSpecs.sourceRackPosition}">
								${assetEntity.sourceRackPosition}
							</tds:tooltipSpan>
						</td>
						<td class="rackLabel valueNW ${standardFieldSpecs.targetRackPosition.imp?:''}">
							<tds:tooltipSpan tooltipDataplacement="bottom" field="${standardFieldSpecs.targetRackPosition}">
								${assetEntity.targetRackPosition}
							</tds:tooltipSpan>
						</td>
						<td class="bladeLabel ${standardFieldSpecs.sourceBladePosition.imp?:''}" style="	display: none" >
							<tds:tooltipSpan tooltipDataPlacement="bottom" field="${standardFieldSpecs.sourceBladePosition}">
								${assetEntity.sourceBladePosition}
							</tds:tooltipSpan>
						</td>
						<td class="bladeLabel ${standardFieldSpecs.targetBladePosition.imp?:''}" style="display: none" >
							<tds:tooltipSpan tooltipDataPlacement="bottom" field="${standardFieldSpecs.targetBladePosition}">
								${assetEntity.targetBladePosition}
							</tds:tooltipSpan>
						</td>

					</tr>
					<tr class="prop">
						<tds:showLabelAndField field="${standardFieldSpecs.assetTag}" value="${assetEntity.assetTag}"/>

						<tds:inputLabel field="${standardFieldSpecs.retireDate}" value="${assetEntity.retireDate}"/>
						<td class="valueNW ${standardFieldSpecs.retireDate.imp?:''}">
						<tds:tooltipSpan field="${standardFieldSpecs.retireDate}">
							<tds:convertDate date="${assetEntity?.retireDate}" />
						</tds:tooltipSpan>
						</td>




						<td class="label ${standardFieldSpecs.moveBundle.imp?:''}" nowrap="nowrap">
							<label for="moveBundle" data-toggle="popover" data-trigger="hover" data-content="${standardFieldSpecs.moveBundle.tip?: standardFieldSpecs.moveBundle.label}">
								${standardFieldSpecs.moveBundle.label} : Dep. Group
							</label>
						</td>
					<td class="valueNW ${standardFieldSpecs.moveBundle.imp?:''}">
						<tds:tooltipSpan tooltipDataPlacement="bottom" field="${standardFieldSpecs.moveBundle}">
							${assetEntity?.moveBundle}
						</tds:tooltipSpan>
						<tds:showDependencyGroup groupId="${dependencyBundleNumber}" assetName="${assetEntity.assetName}"/>
					</td>
						<td class="label ${standardFieldSpecs.size.imp?:''}" nowrap="nowrap">
							<label for="size" data-toggle="popover" data-trigger="hover" data-content="${standardFieldSpecs.size.tip?: standardFieldSpecs.size.label}">
								Size/Scale
							</label>
						</td>
						<td nowrap="nowrap" class="sizeScale ${standardFieldSpecs.size.imp?:''}">
							<tds:tooltipSpan tooltipDataPlacement="bottom" field="${standardFieldSpecs.size}">
								${assetEntity.size} ${assetEntity.scale?.value()}
							</tds:tooltipSpan>
						</td>
					</tr>
					<tr class="prop">
						<tds:showLabelAndField field="${standardFieldSpecs.railType}" value="${assetEntity.railType}"/>

						<tds:inputLabel field="${standardFieldSpecs.maintExpDate}" value="${assetEntity.maintExpDate}"/>
						<td class="valueNW ${standardFieldSpecs.maintExpDate.imp?:''}">
							<tds:tooltipSpan field="${standardFieldSpecs.maintExpDate}">
								<tds:convertDate date="${assetEntity?.maintExpDate}" />
							</tds:tooltipSpan>
						</td>

						<tds:showLabelAndField field="${standardFieldSpecs.planStatus}" value="${assetEntity.planStatus}"/>

						<tds:showLabelAndField field="${standardFieldSpecs.rateOfChange}" value="${assetEntity.rateOfChange}"/>

					</tr>
					<tr>
						<tds:showLabelAndField field="${standardFieldSpecs.externalRefId}" value="${assetEntity.externalRefId}"/>

						<g:if test="! assetEntity.isVM()">
							<td class="label ${standardFieldSpecs.truck.imp?:''}" nowrap="nowrap">
								<label for="truck" data-toggle="popover" data-trigger="hover" data-content="${standardFieldSpecs.truck.tip?: standardFieldSpecs.truck.label}">
									Truck/Cart/Shelf
								</label>
							</td>
							<td class="valueNW ${standardFieldSpecs.truck.imp?:''}">
								<tds:tooltipSpan field="${standardFieldSpecs.truck}">
									${assetEntity.truck ?: '   '} / ${assetEntity.cart ?: '   '} / ${assetEntity.shelf ?: '   '}
								</tds:tooltipSpan>
							</td>
						</g:if>
						<tds:showLabelAndField field="${standardFieldSpecs.validation}" value="${assetEntity.validation}"/>
						<td>&nbsp;</td>
					</tr>
					<g:render template="customShow" ></g:render>
					<g:render template="/comment/assetTagsShow"></g:render>
					</tbody>
				</table>
				</div>
			</td>
		</tr>
		<tr>
			<td colspan="2" class="dates-info-container">
				<table class="dates-info" >
					<tr>
						<td class="date-created date-info">Date created: ${dateCreated}</td>
						<td class="last-updated date-info">Last updated: ${lastUpdated}</td>
					</tr>
				</table>
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
		EntityCrud.loadAssetTags(${assetEntity?.id});
		changeDocTitle('${raw(escapedName)}');
	});
</script>
