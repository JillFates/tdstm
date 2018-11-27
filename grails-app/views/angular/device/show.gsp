<!-- device-show.component.ts -->
<%@page import="com.tdsops.tm.enums.domain.SizeScale"%>
<%@page import="net.transitionmanager.security.Permission"%>
<%@page defaultCodec="html" %>

<div tds-autofocus tds-handle-escape (escPressed)="cancelCloseDialog()" class="modal-content tds-angular-component-content">
    <div class="modal-header">
        <button aria-label="Close" class="close" type="button" (click)="cancelCloseDialog()">
            <span aria-hidden="true">×</span>
        </button>
        <h4 class="modal-title">Device Detail</h4>
    </div>
    <div class="modal-body">
        <div>
            <table style="border:0;" class="assetEntity" data-id="${assetEntity?.id}">

                <tr>
                    <td colspan="2" class="dialog-container">
                        <div class="dialog">
                            <g:if test="${errors}">
                                <div id="messageDivId" class="message">${errors}</div>
                            </g:if>
                            <table>
                                <tbody>
                                <tr class="prop">
                                    <tdsAngular:inputLabel field="${standardFieldSpecs.assetName}" value="${assetEntity.assetName}" />
                                    <td colspan="3" style="font-weight:bold;" class="${standardFieldSpecs.assetName.imp}">${assetEntity.assetName}</td>
                                    <tdsAngular:inputLabel field="${standardFieldSpecs.description}" value="${assetEntity.description}" />
                                    <td colspan="3" class="${standardFieldSpecs.description.imp?:''}">${assetEntity.description}</td>
                                </tr>
                                <tr class="prop">
                                    <tdsAngular:showLabelAndField field="${standardFieldSpecs.assetType}" value="${assetEntity.assetType}" />
                                    <tdsAngular:showLabelAndField field="${standardFieldSpecs.environment}" value="${assetEntity.environment}" />
                                    <td></td>
                                    <td class="label_sm">Source</td>
                                    <td class="label_sm">Target</td>
                                </tr>
                                <tr class="prop">
                                    <tdsAngular:inputLabel field="${standardFieldSpecs.manufacturer}" value="${assetEntity.manufacturer}" />
									<td class="valueNW ${standardFieldSpecs.manufacturer.imp?:''}">
										<tdsAngular:tooltipSpan field="${standardFieldSpecs.manufacturer}">
											<a (click)="showManufacturer('${assetEntity.manufacturer?.id}')">{{getManufacturer('${assetEntity.manufacturer}')}}</a>
										</tdsAngular:tooltipSpan>
									</td>

                                    <tdsAngular:showLabelAndField field="${standardFieldSpecs.priority}" value="${assetEntity.priority}" />
                                    <td class="label ${standardFieldSpecs.locationSource.imp?:''}"
                                        [ngClass]="{'highField': <tdsAngular:highlightedField fieldSpec="${standardFieldSpecs}" asset="${assetEntity}" fieldName="locationSource" domainField="sourceLocationName" />}"
                                        nowrap="nowrap">
                                        <label for="locationSource" data-toggle="popover" data-trigger="hover" data-content="Location">Location</label>
                                    </td>
                                    <td class="valueNW nonVMLabel ${standardFieldSpecs.locationSource.imp?:''}">${assetEntity.sourceLocationName}</td>
                                    <td class="valueNW nonVMLabel ${standardFieldSpecs.locationTarget.imp?:''}">${assetEntity.targetLocationName}</td>
                                </tr>
                                <tr class="prop">
                                    <tdsAngular:inputLabel field="${standardFieldSpecs.model}" value="${assetEntity.model}" />
									<td class="valueNW ${standardFieldSpecs.model.imp?:''}">
										<tdsAngular:tooltipSpan field="${standardFieldSpecs.model}">
											<a (click)="showModel('${assetEntity.model?.id}')">${assetEntity.model}</a>
											<g:if test="${! assetEntity.model?.isValid()}">
												<span style="color: red;">
													<b>?</b>
												</span>
											</g:if>
										</tdsAngular:tooltipSpan>
									</td>
                                    <tdsAngular:showLabelAndField field="${standardFieldSpecs.ipAddress}" value="${assetEntity.ipAddress}" />

                                   <g:if test="${!(assetEntity.assetType in ['VM'])}">
                                        <td class="label nonVMLabel ${standardFieldSpecs.roomSource.imp?:''}"
                                            [ngClass]="{'highField': <tdsAngular:highlightedField fieldSpec="${standardFieldSpecs}" asset="${assetEntity}" fieldName="roomSource" />}"
                                            nowrap="nowrap">
                                            <label for="roomSource" data-toggle="popover" data-trigger="hover" data-content="Room">Room</label>
                                        </td>
                                        <td class="valueNW nonVMLabel ${standardFieldSpecs.roomSource.imp?:''}">${roomSource?.roomName}</td>
                                        <td class="valueNW nonVMLabel ${standardFieldSpecs.roomTarget.imp?:''}">${roomTarget?.roomName}</td>
                                   </g:if>

                                </tr>
                                <tr class="prop">
                                    <tdsAngular:showLabelAndField field="${standardFieldSpecs.shortName}" value="${assetEntity.shortName}" />
                                    <tdsAngular:showLabelAndField field="${standardFieldSpecs.os}" value="${assetEntity.os}" />
                                    <%-- The following fields will be displayed based on the assetType --%>
                                    <%-- rackable --%>
									<g:if test="${!(assetEntity.assetType in ['Blade','VM'])}">
                                        <td class="label rackLabel ${standardFieldSpecs.rackSource.imp?:''}"
                                            [ngClass]="{'highField': <tdsAngular:highlightedField fieldSpec="${standardFieldSpecs}" asset="${assetEntity}" fieldName="rackSource" />}"
                                            nowrap="nowrap" id="rackId">
                                            <label for="rackSourceId" data-toggle="popover" data-trigger="hover" data-content="Rack/Cab">Rack/Cab</label>
                                        </td>
                                        <td class="valueNW nonVMLabel ${standardFieldSpecs.rackSource.imp?:''}">${assetEntity.rackSource?.tag}</td>
                                        <td class="valueNW nonVMLabel ${standardFieldSpecs.rackTarget.imp?:''}">${assetEntity.rackTarget?.tag}</td>
                                    </g:if>

                                    <%-- blade --%>
                                    <g:if test="${assetEntity.assetType in ['Blade']}">
                                        <td class="label bladeLabel ${standardFieldSpecs.sourceChassis.imp?:''}" nowrap="nowrap" id="bladeId">
                                            <label for="sourceChassisId" data-toggle="popover" data-trigger="hover" data-content="Blade Chassis">Blade Chassis</label>
                                        </td>
                                        <td class="bladeLabel ${standardFieldSpecs.sourceChassis.imp?:''}">${sourceChassis}</td>
                                        <td class="bladeLabel ${standardFieldSpecs.targetChassis.imp?:''}">${targetChassis}</td>
                                    </g:if>
                                </tr>
                                <tr class="prop">
                                    <tdsAngular:showLabelAndField field="${standardFieldSpecs.serialNumber}" value="${assetEntity.serialNumber}" />
                                    <tdsAngular:showLabelAndField field="${standardFieldSpecs.supportType}" value="${assetEntity.supportType}" />

                                    <g:if test="${!(assetEntity.assetType in ['Blade','VM'])}">
                                        <td class="label positionLabel ${standardFieldSpecs.sourceRackPosition.imp?:''}"
                                            [ngClass]="{'highField': <tdsAngular:highlightedField fieldSpec="${standardFieldSpecs}" asset="${assetEntity}" fieldName="sourceRackPosition" />}"
                                            nowrap="nowrap">
                                            <label for="rackSource" data-toggle="popover" data-trigger="hover" data-placement="bottom" data-content="${standardFieldSpecs.sourceRackPosition.tip?: standardFieldSpecs.sourceRackPosition.label}">
                                                Position
                                            </label>
                                        </td>
                                        <td class="rackLabel valueNW ${standardFieldSpecs.sourceRackPosition.imp?:''}">${assetEntity.sourceRackPosition}</td>
                                        <td class="rackLabel valueNW ${standardFieldSpecs.targetRackPosition.imp?:''}">${assetEntity.targetRackPosition}</td>
                                    </g:if>


                                    <g:if test="${assetEntity.assetType in ['Blade']}">
                                        <td class="label positionLabel ${standardFieldSpecs.sourceBladePosition.imp?:''}"
                                            [ngClass]="{'highField': <tdsAngular:highlightedField fieldSpec="${standardFieldSpecs}" asset="${assetEntity}" fieldName="sourceBladePosition" />}"
                                            nowrap="nowrap">Position</td>
                                        <td class="bladeLabel ${standardFieldSpecs.sourceBladePosition.imp?:''}">${assetEntity.sourceBladePosition}</td>
                                        <td class="bladeLabel ${standardFieldSpecs.targetBladePosition.imp?:''}">${assetEntity.targetBladePosition}</td>
                                    </g:if>
                                </tr>
                                <tr class="prop">
                                    <tdsAngular:showLabelAndField field="${standardFieldSpecs.assetTag}" value="${assetEntity.assetTag}" />
                                    <tdsAngular:inputLabel field="${standardFieldSpecs.retireDate}" value="${assetEntity.retireDate}" />
                                    <td class="valueNW ${standardFieldSpecs.retireDate.imp?:''}"><tds:convertDate date="${assetEntity?.retireDate}" endian="${dateFormat}"/></td>
                                    <td class="label ${standardFieldSpecs.moveBundle.imp?:''}" nowrap="nowrap">
                                        <label for="moveBundle" data-toggle="popover" data-trigger="hover" data-content="${standardFieldSpecs.moveBundle.tip?:standardFieldSpecs.moveBundle.label}">
                                            ${standardFieldSpecs.moveBundle.label} : Dep. Group
                                        </label>
                                    </td>
                                    <td class="valueNW ${standardFieldSpecs.moveBundle.imp?:''}">
                                        ${assetEntity?.moveBundle}
                                        <tds:showDependencyGroup groupId="${dependencyBundleNumber}" assetName="${assetEntity.assetName}"/>
                                    </td>
                                    <td class="label ${standardFieldSpecs.size.imp?:''}"
                                        [ngClass]="{'highField': <tdsAngular:highlightedField fieldSpec="${standardFieldSpecs}" asset="${assetEntity}" fieldName="size" /> }"
                                        nowrap="nowrap">
                                        <label for="size" data-toggle="popover" data-trigger="hover" data-content="${standardFieldSpecs.size.tip?: standardFieldSpecs.size.label}">
                                            Size/Scale
                                        </label>
                                    </td>
                                    <td nowrap="nowrap" class="sizeScale ${standardFieldSpecs.size.imp?:''}">${assetEntity.size} ${assetEntity.scale?.value()}</td>
                                </tr>
                                <tr class="prop">
                                    <tdsAngular:showLabelAndField field="${standardFieldSpecs.railType}" value="${assetEntity.railType}" />
                                    <tdsAngular:inputLabel field="${standardFieldSpecs.maintExpDate}" value="${assetEntity.maintExpDate}" />
                                    <td class="valueNW ${standardFieldSpecs.maintExpDate.imp?:''}"><tds:convertDate date="${assetEntity?.maintExpDate}" endian="${dateFormat}" /></td>
                                    <tdsAngular:showLabelAndField field="${standardFieldSpecs.planStatus}" value="${assetEntity.planStatus}" />
                                    <tdsAngular:showLabelAndField field="${standardFieldSpecs.rateOfChange}" value="${assetEntity.rateOfChange}" />
                                </tr>
                                <tr>
                                    <tdsAngular:showLabelAndField field="${standardFieldSpecs.externalRefId}" value="${assetEntity.externalRefId}" />
                                    <g:if test="! assetEntity.isVM()">
                                        <td class="label ${standardFieldSpecs.truck.imp?:''}"
                                            [ngClass]="{'highField': <tdsAngular:highlightedField fieldSpec="${standardFieldSpecs}" asset="${assetEntity}" fieldName="truck" />}"
                                            nowrap="nowrap">
                                            <label for="truck" data-toggle="popover" data-trigger="hover" data-content="${standardFieldSpecs.truck.tip?: standardFieldSpecs.truck.label}">
                                                Truck/Cart/Shelf
                                            </label>
                                        </td>
                                        <td class="valueNW ${standardFieldSpecs.truck.imp?:''}">${assetEntity.truck ?: ' '} / ${assetEntity.cart ?: ' '} / ${assetEntity.shelf ?: ' '}</td>
                                    </g:if>
                                    <tdsAngular:showLabelAndField field="${standardFieldSpecs.validation}" value="${assetEntity.validation}" />
                                    <td>&nbsp;</td>
                                </tr>
                                <g:render template="/angular/common/customShow"></g:render>
                                <g:render template="/angular/common/assetTags"></g:render>
                                </tbody>
                            </table>
                        </div>
                    </td>
                </tr>

                <tr>
                    <td colspan="2" class="dates-info-container">
                        <table class="dates-info" >
                            <tr>
                                <td class="date-created">Date created: ${dateCreated}</td>
                                <td class="last-updated">Last updated: ${lastUpdated}</td>
                            </tr>
                        </table>
                    </td>
                </tr>

                <tr id="deps">
                    <g:render template="/angular/common/dependentShow" model="[dependent:dependentAssets, support:supportAssets]"></g:render>
                </tr>
                <tr id="commentListId">
                    <g:render template="/angular/common/commentList" model="['asset':assetEntity, 'prefValue': prefValue, 'viewUnpublishedValue': viewUnpublishedValue, 'hasPublishPermission':hasPublishPermission, 'canEdit': canEdit, currentUserId: currentUserId]"></g:render>
                </tr>
            </table>

        </div>
    </div>
    <div class="modal-footer form-group-center">
        <button class="btn btn-default pull-right" (click)="cancelCloseDialog()" type="button"><span class="glyphicon glyphicon-ban-circle"></span> Close</button>
        <tds:hasPermission permission="${Permission.AssetDelete}">
            <button class="btn btn-danger" (click)="onDeleteAsset()" type="button"><span class="glyphicon glyphicon-trash"></span> Delete</button>
        </tds:hasPermission>
        <button class="btn btn-primary pull-left" (click)="showAssetEditView()" type="button"><span class="glyphicon glyphicon-pencil"></span> Edit</button>
        <button class="btn btn-default pull-left" (click)="onCloneAsset()" type="button"><span class="glyphicon glyphicon-duplicate"></span> Clone </button>
        <a [href]="getGraphUrl()" class="btn btn-default pull-left"><i class="fa fa-fw fa-sitemap"></i> Arch Graph </a>
    </div>
</div>