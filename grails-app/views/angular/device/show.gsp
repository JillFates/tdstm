<!-- device-show.component.ts -->
<%@page import="com.tdsops.tm.enums.domain.SizeScale"%>
<%@page import="net.transitionmanager.security.Permission"%>
<%@page defaultCodec="html" %>

<div tds-autocenter tds-autofocus tds-handle-escape (escPressed)="cancelCloseDialog()" class="tds-modal-content tds-angular-component-content">
    <div class="modal-header">
        <button aria-label="Close" class="close" type="button" (click)="cancelCloseDialog()">
            <clr-icon aria-hidden="true" shape="close"></clr-icon>
        </button>
        <div class="modal-title-container">
            <div class="badge modal-badge" style="">D</div>
			<h4 class="modal-title">${asset.assetName}</h4>
			<%-- TODO: Update Subtitle content with field --%>
			<div class="modal-subtitle">Subtitle content</div>
			<div class="badge modal-subbadge">9</div>
		</div>
    </div>
    <div class="modal-body">
        <clr-tabs>
			<clr-tab>
				<button clrTabLink id="link1">Details</button>
				<clr-tab-content id="content1" *clrIfActive>
                    <div class="clr-row">
						<div class="clr-col-12">
							<g:if test="${errors}">
								<div id="messageDivId" class="message">${errors}</div>
							</g:if>
                            <a (click)="showDetails = !showDetails">Toggle All Details</a>
                            <table class="tdr-detail-list" [ngClass]="{'all-details':showDetails}">
                                <tbody [ngClass]="{'one-column':!showDetails, 'two-column':showDetails}">
                                    <tds:clrRowDetail field="${standardFieldSpecs.assetType}" value="${assetEntity.assetType}" />
                                    <tds:clrRowDetail field="${standardFieldSpecs.environment}" value="${asset.environment}" />

                                    <tr>
                                        <tds:clrInputLabel field="${standardFieldSpecs.manufacturer}" value="${asset.manufacturer}"/>
                                        <td>
                                            <a *ngIf="isManufacturerLinkAvailable()" (click)="showManufacturer('${assetEntity.manufacturer?.id}')">
                                                {{getManufacturer('${assetEntity.manufacturer}')}}
                                            </a>
                                            <span *ngIf="!isManufacturerLinkAvailable()">{{getManufacturer('${assetEntity.manufacturer}')}}</span>
                                        </td>
                                    </tr>

                                    <tds:clrRowDetail field="${standardFieldSpecs.priority}" value="${asset.priority}" />

                                    <tr [ngStyle]="{'order': showDetails ? 1 : unset}">
                                        <th class="${standardFieldSpecs.locationSource.imp?:''}">Source Location</th>
                                        <td>${assetEntity.sourceLocationName}</td>
                                    </tr>

                                    <g:if test="${!(assetEntity.assetType in ['VM'])}">
                                        <tr [ngStyle]="{'order': showDetails ? 3 : unset}">
                                            <th class="${standardFieldSpecs.roomSource.imp?:''}">Source Room</th>
                                            <td>${roomSource?.roomName}</td>
                                        </tr>
                                    </g:if>

                                    
                                    <g:if test="${!(assetEntity.assetType in ['Blade','VM'])}">
                                        <tr [ngStyle]="{'order': showDetails ? 5 : unset}">
                                            <th class="${standardFieldSpecs.rackSource.imp?:''}">Source Rack/Cab</th>
                                            <td>${assetEntity.rackSource?.tag}</td>
                                        </tr>
                                    </g:if>

                                    <g:if test="${!(assetEntity.assetType in ['Blade','VM'])}">
                                        <tr [ngStyle]="{'order': showDetails ? 7 : unset}">
                                            <th class="${standardFieldSpecs.sourceRackPosition.imp?:''}">Source Position</th>
                                            <td>${assetEntity.sourceRackPosition}</td>
                                        </tr>
                                    </g:if>

                                    <tr [ngStyle]="{'order': showDetails ? 2 : unset}">
                                        <th class="${standardFieldSpecs.locationSource.imp?:''}">Target Location</th>
                                        <td>${assetEntity.targetLocationName}</td>
                                    </tr>

                                    <g:if test="${!(assetEntity.assetType in ['VM'])}">
                                        <tr [ngStyle]="{'order': showDetails ? 4 : unset}">
                                            <th class="${standardFieldSpecs.roomSource.imp?:''}">Target Room</th>
                                            <td>${roomTarget?.roomName}</td>
                                        </tr>
                                    </g:if>

                                    <g:if test="${!(assetEntity.assetType in ['Blade','VM'])}">
                                        <tr [ngStyle]="{'order': showDetails ? 6 : unset}">
                                            <th class="${standardFieldSpecs.rackSource.imp?:''}">Target Rack/Cab</th>
                                            <td>${assetEntity.rackTarget?.tag}</td>
                                        </tr>
                                    </g:if>

                                    <g:if test="${!(assetEntity.assetType in ['Blade','VM'])}">
                                        <tr [ngStyle]="{'order': showDetails ? 8 : unset}">
                                            <th class="${standardFieldSpecs.sourceRackPosition.imp?:''}">Target Position</th>
                                            <td>${assetEntity.targetRackPosition}</td>
                                        </tr>
                                    </g:if>

                                    <tr>
                                        <tds:clrInputLabel field="${standardFieldSpecs.model}" value="${asset.model}"/>
                                        <td>
                                            <a *ngIf="isModelLinkAvailable()"
                                                    (click)="showModel('${assetEntity.model?.id}','${assetEntity.manufacturer?.id}')" [innerText]="getModelName('${assetEntity.model}')"></a>
                                            <span *ngIf="!isModelLinkAvailable()">${assetEntity.model}</span>
                                        </td>
                                    </tr>

                                    <tds:clrRowDetail field="${standardFieldSpecs.ipAddress}" value="${asset.ipAddress}" />
                                    <tds:clrRowDetail field="${standardFieldSpecs.shortName}" value="${asset.shortName}" />
                                    <tds:clrRowDetail field="${standardFieldSpecs.os}" value="${asset.os}" />


                                    <g:if test="${assetEntity.assetType in ['Blade']}">
                                        <tr>
                                            <th class="${standardFieldSpecs.sourceChassis.imp?:''}">Source Blade Chassis</th>
                                            <td class="bladeLabel ${standardFieldSpecs.sourceChassis.imp?:''}">${sourceChassis}</td>
                                        </tr>
                                    </g:if>

                                    <g:if test="${assetEntity.assetType in ['Blade']}">
                                        <tr>
                                            <th class="${standardFieldSpecs.sourceChassis.imp?:''}">Target Blade Chassis</th>
                                            <td class="bladeLabel ${standardFieldSpecs.sourceChassis.imp?:''}">${targetChassis}</td>
                                        </tr>
                                    </g:if>

                                    <tds:clrRowDetail field="${standardFieldSpecs.serialNumber}" value="${asset.serialNumber}" />
                                    <tds:clrRowDetail field="${standardFieldSpecs.supportType}" value="${asset.supportType}" />
                                    <tds:clrRowDetail field="${standardFieldSpecs.assetTag}" value="${asset.assetTag}" />

                                    <tr>
                                        <tds:clrInputLabel field="${standardFieldSpecs.retireDate}" value="${assetEntity.retireDate}" />
                                        <td>
                                            {{ '${assetEntity?.retireDate}' | tdsDate: userDateFormat }}
                                        </td>
                                    </tr>

                                    <tr>
                                        <th class="${standardFieldSpecs.moveBundle.imp?:''}">
                                            ${standardFieldSpecs.moveBundle.label} : Dep. Group
                                        </th>

                                        <td>
                                            ${assetEntity?.moveBundle}
                                            <tds:showDependencyGroup groupId="${dependencyBundleNumber}" assetName="${assetEntity.assetName}"/>
                                        </td>
                                    </tr>

                                    <tr>
                                        <th [ngClass]="{'highField': <tdsAngular:highlightedField fieldSpec="${standardFieldSpecs}" asset="${assetEntity}" fieldName="size" /> }">Size/Scale</th>
                                        <td>${assetEntity.size} ${assetEntity.scale?.value()}</td>
                                    </tr>

                                    <tds:clrRowDetail field="${standardFieldSpecs.railType}" value="${asset.railType}" />

                                    <tr>
                                        <tds:clrInputLabel field="${standardFieldSpecs.maintExpDate}" value="${assetEntity.maintExpDate}" />
                                        <td>
                                            {{ '${assetEntity?.maintExpDate}' | tdsDate: userDateFormat }}
                                        </td>
                                    </tr>

                                    <tds:clrRowDetail field="${standardFieldSpecs.planStatus}" value="${asset.planStatus}" />
                                    <tds:clrRowDetail field="${standardFieldSpecs.rateOfChange}" value="${asset.rateOfChange}" />
                                    <tds:clrRowDetail field="${standardFieldSpecs.externalRefId}" value="${asset.externalRefId}" />

                                    <g:if test="! assetEntity.isVM()">
                                        <tr>
                                            <th class="${standardFieldSpecs.truck.imp?:''}">Truck/Cart/Shelf</th>
                                            <td>${assetEntity.truck ?: ' '} / ${assetEntity.cart ?: ' '} / ${assetEntity.shelf ?: ' '}</td>
                                        </tr>
                                    </g:if>

                                    <tds:clrRowDetail field="${standardFieldSpecs.validation}" value="${asset.validation}" />

                                    <g:render template="/angular/common/customShow"></g:render>       
                                </tbody>
                            </table>
                            <g:render template="/angular/common/assetTags"></g:render>
						</div>
					</div>
				</clr-tab-content>
			</clr-tab>
            <clr-tab>
                <button clrTabLink>Supports
					<span class="badge">
						<g:if test="${supportAssets.size() > 99}">
							99+
						</g:if>
						<g:else>
							${supportAssets.size()}
						</g:else>
					 </span>
				</button>
				<clr-tab-content *clrIfActive>
					<div class="clr-row">
						<div class="clr-col-12">
							<g:render 
								template="/angular/common/supportShow" 
								model="[supportAssets:supportAssets]" >
							</g:render>
						</div>
					</div>
				</clr-tab-content>
			</clr-tab>
			<clr-tab>
                <button clrTabLink>Depends On 
					<span class="badge">
						<g:if test="${dependentAssets.size() > 99}">
							99+
						</g:if>
						<g:else>
							${dependentAssets.size()}
						</g:else>
					 </span>
				</button>
				<clr-tab-content *clrIfActive>
                    <div class="clr-row">
						<div class="clr-col-12">
                            <g:render 
                                template="/angular/common/dependentShow" 
                                model="[dependent:dependentAssets, support:supportAssets]">
                            </g:render>
						</div>
					</div>
				</clr-tab-content>
			</clr-tab>
			<clr-tab>
				<button clrTabLink>Tasks</button>
				<clr-tab-content *clrIfActive>
					<div class="clr-row">
                        <g:render 
                            template="/angular/common/commentList" 
                            model="[
                                asset:assetEntity, 
                                prefValue: prefValue, 
                                viewUnpublishedValue: viewUnpublishedValue,
                                hasPublishPermission:hasPublishPermission,  
                                currentUserId: currentUserId,
                                canEdit: canEdit,
                                showTask:true,
                                showComment:false,
                            ]" >
                        </g:render>
					</div>
				</clr-tab-content>
			</clr-tab>
			<clr-tab>
				<button clrTabLink>Comments</button>
				<clr-tab-content *clrIfActive>
                    <div class="clr-row">
						<div  class="clr-col-12">
                            <g:render 
								template="/angular/common/commentList" 
								model="[
									asset:assetEntity, 
									prefValue: prefValue, 
									viewUnpublishedValue: viewUnpublishedValue,
                                    hasPublishPermission:hasPublishPermission,  
									currentUserId: currentUserId,
                                    canEdit: canEdit,
									showTask:false,
									showComment:true,
								]" >
							</g:render>
						</div>
					</div>
				</clr-tab-content>
			</clr-tab>
		</clr-tabs>
    </div>

    <div class="modal-sidenav form-group-center">
		<nav class="modal-sidenav btn-link">
			<tds-button-edit (click)="showAssetEditView()" tooltip="Edit" icon="pencil"></tds-button-edit>
			<tds-button-clone (click)="onCloneAsset()" tooltip="Clone" icon="copy"></tds-button-clone>
			<tds-button-custom (click)="openGraphUrl()" tooltip="Graph" icon="sitemap"></tds-button-custom>
			<tds:hasPermission permission="${Permission.AssetDelete}">
				<tds-button-delete
						tooltip="Delete Asset"
						class="btn-danger"
						[permissions]="['${Permission.AssetDelete}']"
						(click)="onDeleteAsset()">
				</tds-button-delete>
			</tds:hasPermission>
		</nav>
	</div>
</div>