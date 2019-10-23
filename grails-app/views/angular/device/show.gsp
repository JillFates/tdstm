<!-- device-show.component.ts -->
<%@page import="com.tdsops.tm.enums.domain.SizeScale"%>
<%@page import="net.transitionmanager.security.Permission"%>
<%@page defaultCodec="html" %>

<div tds-autocenter tds-autofocus tds-handle-escape (escPressed)="cancelCloseDialog()" class="tds-modal-content tds-angular-component-content">
    <div class="modal-header">
        <button aria-label="Close" class="close" type="button" (click)="cancelCloseDialog()">
            <span aria-hidden="true">×</span>
        </button>
        <h4 class="modal-title">Device Detail</h4>
    </div>
    <div class="modal-body">
        <clr-tabs>
			<clr-tab>
				<button clrTabLink id="link1">Details</button>
				<clr-tab-content id="content1" *clrIfActive>
                    <div class="clr-row">
						<div class="clr-col-10">
							<g:if test="${errors}">
								<div id="messageDivId" class="message">${errors}</div>
							</g:if>
                            <a (click)="showDetails = !showDetails">Toggle All Details</a>
                            <table class="tdr-detail-list" [ngClass]="{'all-details':showDetails}">
                                <tbody>
                                    <tds:clrRowDetail field="${standardFieldSpecs.assetName}" value="${asset.assetName}" />
                                    <tds:clrRowDetail field="${standardFieldSpecs.description}" value="${asset.description}" />
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

                                    <tr>
                                        <th>Location</th>
                                        <td>${assetEntity.sourceLocationName} | ${assetEntity.targetLocationName}</td>
                                    </tr>

                                    <tr>
                                        <tds:clrInputLabel field="${standardFieldSpecs.model}" value="${asset.model}"/>
                                        <td>
                                            <a *ngIf="isModelLinkAvailable()"
                                                    (click)="showModel('${assetEntity.model?.id}','${assetEntity.manufacturer?.id}')" [innerText]="getModelName('${assetEntity.model}')"></a>
                                            <span *ngIf="!isModelLinkAvailable()">${assetEntity.model}</span>
                                        </td>
                                    </tr>

                                    <tds:clrRowDetail field="${standardFieldSpecs.ipAddress}" value="${asset.ipAddress}" />

                                    <g:if test="${!(assetEntity.assetType in ['VM'])}">
                                        <tr>
                                            <th>Room</th>
                                            <td>${roomSource?.roomName}| ${roomTarget?.roomName}</td>
                                        </tr>
                                   </g:if>

                                    <tds:clrRowDetail field="${standardFieldSpecs.shortName}" value="${asset.shortName}" />
                                    <tds:clrRowDetail field="${standardFieldSpecs.os}" value="${asset.os}" />

                                    <g:if test="${!(assetEntity.assetType in ['Blade','VM'])}">
                                        <tr>
                                            <th>Rack/Cab</th>
                                            <td>${assetEntity.rackSource?.tag} | ${assetEntity.rackTarget?.tag}</td>
                                        </tr>
                                    </g:if>

                                    <g:if test="${assetEntity.assetType in ['Blade']}">
                                        <tr>
                                            <th>Blade Chassis</th>
                                            <td class="bladeLabel ${standardFieldSpecs.sourceChassis.imp?:''}">${sourceChassis} | ${targetChassis}</td>
                                        </tr>
                                    </g:if>

                                    <tds:clrRowDetail field="${standardFieldSpecs.serialNumber}" value="${asset.serialNumber}" />
                                    <tds:clrRowDetail field="${standardFieldSpecs.supportType}" value="${asset.supportType}" />

                                    <g:if test="${!(assetEntity.assetType in ['Blade','VM'])}">
                                        <tr>
                                            <th>Position</th>
                                            <td>${assetEntity.sourceRackPosition} | ${assetEntity.targetRackPosition}</td>
                                        </tr>
                                    </g:if>

                                    <tds:clrRowDetail field="${standardFieldSpecs.assetTag}" value="${asset.assetTag}" />

                                    <tr>
                                        <tds:clrInputLabel field="${standardFieldSpecs.retireDate}" value="${assetEntity.retireDate}" />
                                        <td>
                                            {{ '${assetEntity?.retireDate}' | tdsDate: userDateFormat }}
                                        </td>
                                    </tr>

                                    <tr>
                                        <th>
                                            ${standardFieldSpecs.moveBundle.label} : Dep. Group
                                        </th>

                                        <td>
                                            ${assetEntity?.moveBundle}
                                            <tds:showDependencyGroup groupId="${dependencyBundleNumber}" assetName="${assetEntity.assetName}"/>
                                        </td>
                                    </tr>

                                    <tr>
                                        <th>Size/Scale</th>
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
                                            <th>Truck/Cart/Shelf</th>
                                            <td>${assetEntity.truck ?: ' '} / ${assetEntity.cart ?: ' '} / ${assetEntity.shelf ?: ' '}</td>
                                        </tr>
                                    </g:if>

                                    <tds:clrRowDetail field="${standardFieldSpecs.validation}" value="${asset.validation}" />

                                    <g:render template="/angular/common/customShow"></g:render>
                                    <g:render template="/angular/common/assetTags"></g:render>
                                </tbody>
                            </table>
						</div>
						<div class="clr-col-12">
							<table class="dates-info">
								<tr>
									<td class="date-created">Date created: ${dateCreated}</td>
									<td class="last-updated">Last updated: ${lastUpdated}</td>
								</tr>
							</table>
						</div>
					</div>
				</clr-tab-content>
			</clr-tab>
            <clr-tab>
				<button clrTabLink>Supports</button>
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
				<button clrTabLink>Depends On</button>
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
    <div class="modal-footer form-group-center">
        <div class="asset-commands pull-left">
            <tds-button-edit
                    tooltip="Edit Asset"
                    class="btn-primary"
                    [permissions]="['${Permission.AssetEdit}']"
                    (click)="showAssetEditView()">
            </tds-button-edit>

            <tds-button-clone
                    (click)="onCloneAsset()"
                    [permissions]="['${Permission.AssetCreate}']">
            </tds-button-clone>

            <tds-button-custom
                    icon="sitemap"
                    title="Arch Graph"
                    (click)="openGraphUrl()">
            </tds-button-custom>
        </div>

        <tds:hasPermission permission="${Permission.AssetDelete}">
            <tds-button-delete
                    tooltip="Delete Asset"
                    class="btn-danger"
                    [permissions]="['${Permission.AssetDelete}']"
                    (click)="onDeleteAsset()">
            </tds-button-delete>
        </tds:hasPermission>

        <tds-button-close
                class="pull-right"
                (click)="cancelCloseDialog()">
        </tds-button-close>
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