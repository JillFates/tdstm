<%@page import="com.tdsops.tm.enums.domain.SizeScale"%>
<%@page import="net.transitionmanager.security.Permission"%>
<%@page defaultCodec="html" %>

<div class="modal-content tds-angular-component-content">
	<div class="modal-header">
		<button aria-label="Close" class="close" type="button" (click)="cancelCloseDialog()"><span  aria-hidden="true">×</span></button>
		<h4 class="modal-title">Device Detail</h4>
	</div>
	<div class="modal-body">
		<div>
			<table style="border:0;" class="assetEntity" data-id="${assetEntity?.id}">
				<tr>
					<td colspan="2">
						<div class="dialog">
						<g:if test="${errors}">
							<div id="messageDivId" class="message">${errors}</div>
						</g:if>
						<table>
							<tbody>
							<tr  class="prop">
								<tdsAngular:inputLabel field="${standardFieldSpecs.assetName}" value="${assetEntity.assetName}"/>
								<td colspan="2" style="font-weight:bold;" class="${standardFieldSpecs.validation.imp}">
								<tdsAngular:tooltipSpan field="${standardFieldSpecs.assetName}">
										${assetEntity.assetName}
								</tdsAngular:tooltipSpan>
								</td>

								<tdsAngular:inputLabel field="${standardFieldSpecs.description}" value="${assetEntity.description}"/>
								<td colspan="3" class="${standardFieldSpecs.validation.imp?:''}">
									<tdsAngular:tooltipSpan field="${standardFieldSpecs.description}">
										${assetEntity.description}
									</tdsAngular:tooltipSpan>
								</td>
							</tr>
							<tr class="prop">
								<tdsAngular:showLabelAndField field="${standardFieldSpecs.assetType}" value="${assetEntity.assetType}"/>

								<tdsAngular:showLabelAndField field="${standardFieldSpecs.environment}" value="${assetEntity.environment}"/>

								<td></td>
								<td class="label_sm">Source</td>
								<td class="label_sm">Target</td>
							</tr>
							<tr class="prop">
								<tdsAngular:inputLabel field="${standardFieldSpecs.manufacturer}" value="${assetEntity.manufacturer}"/>
								<td class="valueNW ${standardFieldSpecs.manufacturer.imp?:''}">
									<tdsAngular:tooltipSpan field="${standardFieldSpecs.manufacturer}">
										<a href='javascript:showManufacturer(${assetEntity.manufacturer?.id})' style='color:#00E'>${assetEntity.manufacturer}</a>
									</tdsAngular:tooltipSpan>
								</td>

								<tdsAngular:showLabelAndField field="${standardFieldSpecs.priority}" value="${assetEntity.priority}"/>

								<td class="label ${standardFieldSpecs.sourceLocation.imp?:''}" nowrap="nowrap">
									<label for="sourceLocation" data-toggle="popover" data-trigger="hover" data-content="Location">Location</label>
								</td>
								<td class="valueNW nonVMLabel ${standardFieldSpecs.sourceLocation.imp?:''}" >
									<tdsAngular:tooltipSpan field="${standardFieldSpecs.sourceLocation}" >
										${assetEntity.sourceLocation}
									</tdsAngular:tooltipSpan>
								</td>
								<td class="valueNW nonVMLabel ${standardFieldSpecs.targetLocation.imp?:''}" >
									<tdsAngular:tooltipSpan field="${standardFieldSpecs.targetLocation}" >
										${assetEntity.targetLocation}
									</tdsAngular:tooltipSpan>
								</td>

							</tr>
							<tr class="prop">
								<tdsAngular:inputLabel field="${standardFieldSpecs.model}" value="${assetEntity.model}"/>
								<td class="valueNW ${standardFieldSpecs.model.imp?:''}">
								<tdsAngular:tooltipSpan field="${standardFieldSpecs.model}">
								<a href='javascript:showModel(${assetEntity.model?.id})' style='color:#00E'>${assetEntity.model}</a>
									<g:if test="${! assetEntity.model?.isValid()}"> <span style="color: red;"><b>?</b></span></g:if>
								</tdsAngular:tooltipSpan>
								</td>

								<tdsAngular:showLabelAndField field="${standardFieldSpecs.ipAddress}" value="${assetEntity.ipAddress}"/>

								<td class="label nonVMLabel ${standardFieldSpecs.sourceRoom.imp?:''}" nowrap="nowrap">
									<label for="sourceRoom" data-toggle="popover" data-trigger="hover" data-content="Room">Room</label>
								</td>

								<td class="valueNW nonVMLabel ${standardFieldSpecs.sourceRoom.imp?:''}" >
									<tdsAngular:tooltipSpan field="${standardFieldSpecs.sourceRoom}" >
										${roomSource?.roomName}
									</tdsAngular:tooltipSpan>
								</td>
								<td class="valueNW nonVMLabel ${standardFieldSpecs.targetRoom.imp?:''}">
									<tdsAngular:tooltipSpan tooltipDataPlacement="bottom" field="${standardFieldSpecs.targetRoom}" >
										${roomTarget?.roomName}
									</tdsAngular:tooltipSpan>
								</td>

							</tr>
							<tr class="prop">
								<tdsAngular:showLabelAndField field="${standardFieldSpecs.shortName}" value="${assetEntity.shortName}"/>

								<tdsAngular:showLabelAndField field="${standardFieldSpecs.os}" value="${assetEntity.os}"/>

								<%-- The following fields will be displayed based on the assetType --%>
								<%-- rackable --%>
								<td class="label rackLabel ${standardFieldSpecs.sourceRack.imp?:''}"  nowrap="nowrap" id="rackId">
									<label for="sourceRackId" data-toggle="popover" data-trigger="hover" data-content="Rack/Cab">Rack/Cab</label>
								</td>
								<td class="valueNW nonVMLabel ${standardFieldSpecs.sourceRack.imp?:''}" >
									<tdsAngular:tooltipSpan field="${standardFieldSpecs.sourceRack}" >
										${assetEntity.rackSource?.tag}
									</tdsAngular:tooltipSpan>
								</td>
								<td class="valueNW nonVMLabel ${standardFieldSpecs.targetRack.imp?:''}" >
									<tdsAngular:tooltipSpan field="${standardFieldSpecs.targetRack}" >
										${assetEntity.rackTarget?.tag}
									</tdsAngular:tooltipSpan>
								</td>

								<%-- blade --%>
								<td class="label bladeLabel ${standardFieldSpecs.sourceChassis.imp?:''}" nowrap="nowrap" id="bladeId" style="display: none">
									<label for="sourceChassisId" data-toggle="popover" data-trigger="hover" data-content="Blade Chassis">Blade Chassis</label>
								</td>
								<td class="bladeLabel ${standardFieldSpecs.sourceChassis.imp?:''}" style="display: none" data-toggle="popover" data-trigger="hover" data-placement="bottom" data-content="${standardFieldSpecs.sourceChassis.tip?: standardFieldSpecs.sourceChassis.label}">
									${sourceChassis}
								</td>
								<td class="bladeLabel ${standardFieldSpecs.targetChassis.imp?:''}" style="display: none" data-toggle="popover" data-trigger="hover" data-placement="bottom" data-content="${standardFieldSpecs.targetChassis.tip?: standardFieldSpecs.targetChassis.label}">
									${targetChassis}
								</td>

							</tr>
							<tr class="prop">
								<tdsAngular:showLabelAndField field="${standardFieldSpecs.serialNumber}" value="${assetEntity.serialNumber}"/>

								<tdsAngular:showLabelAndField field="${standardFieldSpecs.supportType}" value="${assetEntity.supportType}"/>

								<td class="label positionLabel ${standardFieldSpecs.sourceRackPosition.imp?:''}" nowrap="nowrap">
									<label for="sourceRack" data-toggle="popover" data-trigger="hover" data-placement="bottom" data-content="${standardFieldSpecs.sourceRackPosition.tip?: standardFieldSpecs.sourceRackPosition.label}">
										Position
									</label>
								</td>
								<td class="rackLabel valueNW ${standardFieldSpecs.sourceRackPosition.imp?:''}">
									<tdsAngular:tooltipSpan tooltipDataPlacement="bottom" field="${standardFieldSpecs.sourceRackPosition}">
										${assetEntity.sourceRackPosition}
									</tdsAngular:tooltipSpan>
								</td>
								<td class="rackLabel valueNW ${standardFieldSpecs.targetRackPosition.imp?:''}">
									<tdsAngular:tooltipSpan tooltipDataplacement="bottom" field="${standardFieldSpecs.targetRackPosition}">
										${assetEntity.targetRackPosition}
									</tdsAngular:tooltipSpan>
								</td>
								<td class="bladeLabel ${standardFieldSpecs.sourceBladePosition.imp?:''}" style="	display: none" >
									<tdsAngular:tooltipSpan tooltipDataPlacement="bottom" field="${standardFieldSpecs.sourceBladePosition}">
										${assetEntity.sourceBladePosition}
									</tdsAngular:tooltipSpan>
								</td>
								<td class="bladeLabel ${standardFieldSpecs.targetBladePosition.imp?:''}" style="display: none" >
									<tdsAngular:tooltipSpan tooltipDataPlacement="bottom" field="${standardFieldSpecs.targetBladePosition}">
										${assetEntity.targetBladePosition}
									</tdsAngular:tooltipSpan>
								</td>

							</tr>
							<tr class="prop">
								<tdsAngular:showLabelAndField field="${standardFieldSpecs.assetTag}" value="${assetEntity.assetTag}"/>

								<tdsAngular:inputLabel field="${standardFieldSpecs.retireDate}" value="${assetEntity.retireDate}"/>
								<td class="valueNW ${standardFieldSpecs.retireDate.imp?:''}">
								<tdsAngular:tooltipSpan field="${standardFieldSpecs.retireDate}">
									${assetEntity?.retireDate}
								</tdsAngular:tooltipSpan>
								</td>
								<td class="label ${standardFieldSpecs.moveBundle.imp?:''}" nowrap="nowrap">
									<label for="moveBundle" data-toggle="popover" data-trigger="hover" data-content="${standardFieldSpecs.moveBundle.tip?: standardFieldSpecs.moveBundle.label}">
										Bundle : Dep. Group
									</label>
								</td>
								<td class="valueNW ${standardFieldSpecs.moveBundle.imp?:''}">
									<tdsAngular:tooltipSpan field="${standardFieldSpecs.moveBundle}">
										${assetEntity.moveBundle}${(dependencyBundleNumber != null)?' : ' : ''}${dependencyBundleNumber}
									</tdsAngular:tooltipSpan>
								</td>

								<td class="label ${standardFieldSpecs.size.imp?:''}" nowrap="nowrap">
									<label for="size" data-toggle="popover" data-trigger="hover" data-content="${standardFieldSpecs.size.tip?: standardFieldSpecs.size.label}">
										Size/Scale
									</label>
								</td>
								<td nowrap="nowrap" class="sizeScale ${standardFieldSpecs.size.imp?:''}">
									<tdsAngular:tooltipSpan tooltipDataPlacement="bottom" field="${standardFieldSpecs.size}">
										${assetEntity.size} ${assetEntity.scale?.value()}
									</tdsAngular:tooltipSpan>
								</td>
							</tr>
							<tr class="prop">
								<tdsAngular:showLabelAndField field="${standardFieldSpecs.railType}" value="${assetEntity.railType}"/>

								<tdsAngular:inputLabel field="${standardFieldSpecs.maintExpDate}" value="${assetEntity.maintExpDate}"/>
								<td class="valueNW ${standardFieldSpecs.maintExpDate.imp?:''}">
									<tdsAngular:tooltipSpan field="${standardFieldSpecs.maintExpDate}">
										${assetEntity?.maintExpDate}
									</tdsAngular:tooltipSpan>
								</td>

								<tdsAngular:showLabelAndField field="${standardFieldSpecs.planStatus}" value="${assetEntity.planStatus}"/>

								<tdsAngular:showLabelAndField field="${standardFieldSpecs.rateOfChange}" value="${assetEntity.rateOfChange}"/>

							</tr>
							<tr>
								<tdsAngular:showLabelAndField field="${standardFieldSpecs.externalRefId}" value="${assetEntity.externalRefId}"/>

								<g:if test="! assetEntity.isVM()">
									<td class="label ${standardFieldSpecs.truck.imp?:''}" nowrap="nowrap">
										<label for="truck" data-toggle="popover" data-trigger="hover" data-content="${standardFieldSpecs.truck.tip?: standardFieldSpecs.truck.label}">
											Truck/Cart/Shelf
										</label>
									</td>
									<td class="valueNW ${standardFieldSpecs.truck.imp?:''}">
										<tdsAngular:tooltipSpan field="${standardFieldSpecs.truck}">
											${assetEntity.truck ?: '   '} / ${assetEntity.cart ?: '   '} / ${assetEntity.shelf ?: '   '}
										</tdsAngular:tooltipSpan>
									</td>
								</g:if>
								<tdsAngular:showLabelAndField field="${standardFieldSpecs.validation}" value="${assetEntity.validation}"/>
								<td>&nbsp;</td>
							</tr>
							<g:render template="/angular/common/customShow" ></g:render>
							</tbody>
						</table>
						</div>
					</td>
				</tr>
			</table>

		</div>
	</div>
	<div class="modal-footer form-group-center">
		<button class="btn btn-default pull-right" (click)="cancelCloseDialog()" type="button"><span  class="glyphicon glyphicon-ban-circle"></span> Cancel</button>
	</div>
</div>