<!-- device-show.component.ts -->
<%@ page import="com.tdsops.tm.enums.domain.SizeScale" %>
<%@ page import="net.transitionmanager.security.Permission" %>
<%@ page defaultCodec="html" %>

<div tds-autocenter tds-autofocus tds-handle-escape (escPressed)="cancelCloseDialog()"
     class="tds-modal-content has-side-nav tds-angular-component-content">
    <div class="modal-header">
        <button aria-label="Close" class="close" type="button" (click)="cancelCloseDialog()">
            <clr-icon aria-hidden="true" shape="close"></clr-icon>
        </button>

        <div class="clr-row">
            <div class="clr-col-6">
                <div class="modal-title-container">
                    <div class="badge modal-badge" style="">D</div>
                    <h4 class="modal-title">${asset.assetName}</h4>

                    <div class="modal-subtitle">${asset?.moveBundle}</div>

                    <div class="badge modal-subbadge"><tds:showDependencyGroup groupId="${dependencyBundleNumber}"
                                                                               assetName="${asset.assetName}"/></div>
                </div>

                <div class="modal-description"
                     [ngClass]="{'modal-description-sized':showDetails, 'modal-description-height':${!!asset.description?.trim()}}">
                    <div *ngIf="readMore">
                        <p>${asset.description} <a (click)="readMore = !readMore">Read Less</a></p>
                    </div>

                    <div *ngIf="!readMore" class="readMore">
                        <g:if test="${asset.description?.length() > 80}">
                            <div class="truncated-description">${asset.description.substring(0, 80)}...</div>
                            <a (click)="readMore = !readMore">Read More</a>
                        </g:if>
                        <g:else>
                            <div class="truncated-description">${asset.description}</div>
                        </g:else>
                    </div>
                </div>
            </div>

            <div class="clr-col-6">
                <div class="minimized-arch-thumbnail" *ngIf="!!showDetails">
                    <tds-lib-diagram-layout
                            [data]="data$ | async"
                            [layout]="diagramLayout$ | async"
                            [linkTemplate]="linkTemplate$ | async"
                            (expandActionDispatched)="onExpandActionDispatched()"
                            [hideExpand]="false"
                            [hideOverview]="true"
                            [hideControlButtons]="true" #graph></tds-lib-diagram-layout>
                </div>
            </div>

            <div class="clr-col-12">
                <tds-tab-scroller>
                    <tds-scroller-item>
                        <button tdsScrollerLink>
                            {{ showDetails ? "Details" : "Summary"}}
                        </button>
                    </tds-scroller-item>
                    <tds-scroller-item>
                        <button tdsScrollerLink>Supports
                            <span class="badge">
                                <g:if test="${supportAssets.size() > 99}">
                                    99+
                                </g:if>
                                <g:else>
                                    ${supportAssets.size()}
                                </g:else>
                            </span>
                        </button>
                    </tds-scroller-item>
                    <tds-scroller-item>
                        <button tdsScrollerLink>Depends On
                            <span class="badge">
                                <g:if test="${dependentAssets.size() > 99}">
                                    99+
                                </g:if>
                                <g:else>
                                    ${dependentAssets.size()}
                                </g:else>
                            </span>
                        </button>
                    </tds-scroller-item>
                    <tds-scroller-item>
                        <button tdsScrollerLink>Tasks
                            <span class="badge">
                                {{ taskCount > 99 ? '99+' : taskCount }}
                            </span>
                        </button>
                    </tds-scroller-item>
                    <tds-scroller-item>
                        <button tdsScrollerLink>Comments
                            <span class="badge">
                                {{ commentCount > 99 ? '99+' : commentCount }}
                            </span>
                        </button>
                    </tds-scroller-item>
                </tds-tab-scroller>
            </div>
        </div>
    </div>

    <div class="modal-body asset-crud"
         [ngClass]="{'has-description': ${!!asset.description?.trim()}, 'no-description': ${!asset.description?.trim()}}"
         tdsScrollContainer style="position: relative">
        <div tdsScrollSection class="clr-row">
            <div [ngClass]="{'clr-col-12':showDetails, 'clr-col-6':!showDetails}">
                <g:if test="${errors}">
                    <div id="messageDivId" class="message">${errors}</div>
                </g:if>
                <table class="tdr-detail-list device-grid" [ngClass]="{'all-details':showDetails}">
                    <tbody [ngClass]="{'one-column':!showDetails, 'three-column':showDetails}">

                    <!-- Row 1 -->
                    <tds:clrRowDetail style="order: 1" field="${standardFieldSpecs.assetType}"
                                      value="${assetEntity.assetType}"/>
                    <tds:clrRowDetail style="order: 2" field="${standardFieldSpecs.environment}"
                                      value="${asset.environment}"/>
                    <tr style="order: 3">
                        <tds:clrInputLabel field="${standardFieldSpecs.manufacturer}" value="${asset.manufacturer}"/>
                        <td>
                            <a *ngIf="isManufacturerLinkAvailable()"
                               (click)="showManufacturer('${assetEntity.manufacturer?.id}')">
                                {{getManufacturer('${assetEntity.manufacturer}')}}
                            </a>
                            <span *ngIf="!isManufacturerLinkAvailable()">{{getManufacturer('${assetEntity.manufacturer}')}}</span>
                        </td>
                    </tr>

                    <!-- Row 2 -->
                    <tr [ngStyle]="{'order': showDetails ? 4 : 3}" class="header-label">
                        <td colspan="2" class="${standardFieldSpecs.locationSource.imp ?: ''}">
                            <label>Source</label>
                        </td>
                    </tr>
                    <tr [ngStyle]="{'order': showDetails ? 5 : 4}" class="header-label">
                        <td colspan="2" class="${standardFieldSpecs.locationTarget.imp?:''}">
                            <label>Target</label>
                        </td>
                    </tr>
                    <tds:clrRowDetail style="order: 6" field="${standardFieldSpecs.priority}"
                                      value="${asset.priority}"/>

                    <!-- Row 3 -->
                    <tr [ngStyle]="{'order': showDetails ? 7 : 3}">
                        <th class="${standardFieldSpecs.locationSource.imp ?: ''}">Source Location</th>
                        <td>${assetEntity.sourceLocationName}</td>
                    </tr>
                    <tr [ngStyle]="{'order': showDetails ? 8 : 4}">
                        <th class="${standardFieldSpecs.locationTarget.imp?:''}">Target Location</th>
                        <td>${assetEntity.targetLocationName}</td>
                    </tr>
                    <tr style="order: 9">
                        <tds:clrInputLabel field="${standardFieldSpecs.model}" value="${asset.model}"/>
                        <td>
                            <a *ngIf="isModelLinkAvailable()"
                               (click)="showModel('${assetEntity.model?.id}','${assetEntity.manufacturer?.id}')"
                               [innerText]="getModelName('${assetEntity.model}')"></a>
                            <span *ngIf="!isModelLinkAvailable()">${assetEntity.model}</span>
                        </td>
                    </tr>

                    <!-- Row 4 -->
                    <g:if test="${!(assetEntity.assetType in ['VM'])}">
                        <tr [ngStyle]="{'order': showDetails ? 10 : 3}">
                            <th class="${standardFieldSpecs.roomSource.imp ?: ''}">Source Room</th>
                            <td>${roomSource?.roomName}</td>
                        </tr>
                    </g:if>
                    <g:if test="${!(assetEntity.assetType in ['VM'])}">
                        <tr [ngStyle]="{'order': showDetails ? 11 : 4}">
                            <th class="${standardFieldSpecs.roomTarget.imp?:''}">Target Room</th>
                            <td>${roomTarget?.roomName}</td>
                        </tr>
                    </g:if>
                    <tds:clrRowDetail style="order: 12" field="${standardFieldSpecs.ipAddress}"
                                      value="${asset.ipAddress}"/>


                    <!-- Row 6 -->
                    <g:if test="${!(assetEntity.assetType in ['Blade', 'VM'])}">
                        <tr [ngStyle]="{'order': showDetails ? 13 : 3}">
                            <th class="${standardFieldSpecs.rackSource.imp ?: ''}">Source Rack/Cab</th>
                            <td>${assetEntity.rackSource?.tag}</td>
                        </tr>
                    </g:if>
                    <g:if test="${!(assetEntity.assetType in ['Blade','VM'])}">
                        <tr [ngStyle]="{'order': showDetails ? 14 : 4}">
                            <th class="${standardFieldSpecs.rackTarget.imp?:''}">Target Rack/Cab</th>
                            <td>${assetEntity.rackTarget?.tag}</td>
                        </tr>
                    </g:if>
                    <tds:clrRowDetail style="order: 15" field="${standardFieldSpecs.shortName}"
                                      value="${asset.shortName}"/>

                    <!-- Row 7 -->
                    <g:if test="${!(assetEntity.assetType in ['Blade', 'VM'])}">
                        <tr [ngStyle]="{'order': showDetails ? 16 : 3}">
                            <th class="${standardFieldSpecs.sourceRackPosition.imp ?: ''}">Source Position</th>
                            <td>${assetEntity.sourceRackPosition}</td>
                        </tr>
                    </g:if>
                    <g:if test="${!(assetEntity.assetType in ['Blade','VM'])}">
                        <tr [ngStyle]="{'order': showDetails ? 17 : 4}">
                            <th class="${standardFieldSpecs.targetRackPosition.imp?:''}">Target Position</th>
                            <td>${assetEntity.targetRackPosition}</td>
                        </tr>
                    </g:if>
                    <tds:clrRowDetail style="order: 18"
                                      field="${standardFieldSpecs.os}"
                                      value="${asset.os}"/>

                    <!-- Row 8 -->
                    <g:if test="${assetEntity.assetType in ['Blade']}">
                        <tr [ngStyle]="{'order': showDetails ? 19 : 3}">
                            <th class="${standardFieldSpecs.sourceChassis.imp ?: ''}">Source Blade Chassis</th>
                            <td class="bladeLabel ${standardFieldSpecs.sourceChassis.imp ?: ''}">${sourceChassis}</td>
                        </tr>
                    </g:if>
                    <g:if test="${assetEntity.assetType in ['Blade']}">
                        <tr [ngStyle]="{'order': showDetails ? 20 : 4}">
                            <th class="${standardFieldSpecs.sourceChassis.imp?:''}">Target Blade Chassis</th>
                            <td class="bladeLabel ${standardFieldSpecs.sourceChassis.imp?:''}">${targetChassis}</td>
                        </tr>
                    </g:if>
                    <tds:clrRowDetail style="order: 21" field="${standardFieldSpecs.serialNumber}"
                                      value="${asset.serialNumber}"/>


                    <tds:clrRowDetail style="order: 70" field="${standardFieldSpecs.supportType}"
                                      value="${asset.supportType}"/>
                    <tds:clrRowDetail style="order: 85" field="${standardFieldSpecs.assetTag}"
                                      value="${asset.assetTag}"/>

                    <tr style="order: 90">
                        <tds:clrInputLabel field="${standardFieldSpecs.retireDate}" value="${assetEntity.retireDate}"/>
                        <td>
                            {{ '${assetEntity?.retireDate}' | tdsDate: userDateFormat }}
                        </td>
                    </tr>

                    <tr style="order: 75">
                        <th class="${standardFieldSpecs.moveBundle.imp ?: ''}">
                            ${standardFieldSpecs.moveBundle.label} : Dep. Group
                        </th>

                        <td>
                            ${assetEntity?.moveBundle}
                            <g:if test="${dependencyBundleNumber}">:</g:if>
                            <tds:showDependencyGroup groupId="${dependencyBundleNumber}"
                                                     assetName="${assetEntity.assetName}"/>
                        </td>
                    </tr>

                    <tr style="order: 80">
                        <th class="${standardFieldSpecs.size.imp ?: ''}">Size/Scale</th>
                        <td>${assetEntity.size} ${assetEntity.scale?.value()}</td>
                    </tr>

                    <tds:clrRowDetail style="order: 100" field="${standardFieldSpecs.railType}"
                                      value="${asset.railType}"/>

                    <tr style="order: 105">
                        <tds:clrInputLabel field="${standardFieldSpecs.maintExpDate}"
                                           value="${assetEntity.maintExpDate}"/>
                        <td>
                            {{ '${assetEntity?.maintExpDate}' | tdsDate: userDateFormat }}
                        </td>
                    </tr>

                    <tds:clrRowDetail style="order: 95" field="${standardFieldSpecs.planStatus}"
                                      value="${asset.planStatus}"/>
                    <tds:clrRowDetail style="order: 115" field="${standardFieldSpecs.rateOfChange}"
                                      value="${asset.rateOfChange}"/>
                    <tds:clrRowDetail style="order: 120" field="${standardFieldSpecs.externalRefId}"
                                      value="${asset.externalRefId}"/>

                    <g:if test="! assetEntity.isVM()">
                        <tr style="order: 125">
                            <th class="${standardFieldSpecs.truck.imp ?: ''}">Truck/Cart/Shelf</th>
                            <td>${assetEntity.truck ?: ' '} / ${assetEntity.cart ?: ' '} / ${assetEntity.shelf ?: ' '}</td>
                        </tr>
                    </g:if>

                    <tds:clrRowDetail style="order: 110" field="${standardFieldSpecs.validation}"
                                      value="${asset.validation}"/>

                    <g:render template="/angular/common/customShow"></g:render>
                    </tbody>
                </table>
                <g:render template="/angular/common/assetTags"></g:render>
                <a (click)="showDetails = !showDetails" class="show-hide-link">
                    <span *ngIf="!showDetails">View All Fields</span>
                    <span *ngIf="showDetails">Hide Additional Fields</span>
                </a>
            </div>

            <div class="clr-col-6 modal-body-graph" *ngIf="!showDetails">
                <tds-lib-diagram-layout
                        [data]="data$ | async"
                        [layout]="diagramLayout$ | async"
                        [linkTemplate]="linkTemplate$ | async"
                        (expandActionDispatched)="onExpandActionDispatched()"
                        [hideExpand]="false"
                        [hideOverview]="true"
                        [hideControlButtons]="true" #graph></tds-lib-diagram-layout>
            </div>
        </div>

        <div tdsScrollSection class="clr-row">
            <div class="clr-col-12">
                <g:render
                        template="/angular/common/supportShow"
                        model="[supportAssets: supportAssets]">
                </g:render>
            </div>
        </div>

        <div tdsScrollSection class="clr-row">
            <div class="clr-col-12">
                <g:render
                        template="/angular/common/dependentShow"
                        model="[dependent: dependentAssets, support: supportAssets]">
                </g:render>
            </div>
        </div>

        <div tdsScrollSection class="clr-row">
            <g:render
                    template="/angular/common/commentList"
                    model="[
                            asset               : assetEntity,
                            prefValue           : prefValue,
                            viewUnpublishedValue: viewUnpublishedValue,
                            hasPublishPermission: hasPublishPermission,
                            currentUserId       : currentUserId,
                            canEdit             : canEdit,
                            showTask            : true,
                            showComment         : false,
                    ]">
            </g:render>
        </div>

        <div tdsScrollSection class="clr-row">
            <div class="clr-col-12">
                <g:render
                        template="/angular/common/commentList"
                        model="[
                                asset               : assetEntity,
                                prefValue           : prefValue,
                                viewUnpublishedValue: viewUnpublishedValue,
                                hasPublishPermission: hasPublishPermission,
                                currentUserId       : currentUserId,
                                canEdit             : canEdit,
                                showTask            : false,
                                showComment         : true,
                        ]">
                </g:render>
            </div>
        </div>
    </div>

    <div class="modal-sidenav form-group-center">
        <nav class="modal-sidenav btn-link">
            <tds-button-edit (click)="showAssetEditView()" tooltip="Edit" icon="pencil"></tds-button-edit>
            <tds-button-clone (click)="onCloneAsset()" tooltip="Clone" icon="copy"></tds-button-clone>
            <tds:hasPermission permission="${Permission.AssetDelete}">
                <tds-button-delete
                        tooltip="Delete Asset"
                        class="btn-danger"
                        [permissions]="['${Permission.AssetDelete}']"
                        (click)="onDeleteAsset()">
                </tds-button-delete>
            </tds:hasPermission>
            <tds-button-close
                    tooltip="Close"
                    (click)="cancelCloseDialog()">
            </tds-button-close>
        </nav>
    </div>
</div>
