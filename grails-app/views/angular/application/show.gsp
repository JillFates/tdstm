<%@page import="net.transitionmanager.asset.Application"%>
<%@page import="net.transitionmanager.security.Permission"%>
<%@page defaultCodec="html" %>
<%@page import="grails.converters.JSON"%>

<g:set var="assetClass" value="${(new Application()).assetClass}" />

<div tds-autocenter tds-autofocus tds-handle-escape (escPressed)="cancelCloseDialog()" class="tds-modal-content has-side-nav tds-angular-component-content">
	<div class="modal-header">
		<tds-button-close aria-label="Close" class="close" icon="close" [flat]="true" (click)="cancelCloseDialog()"></tds-button-close>

        <div class="clr-row">
            <div class="clr-col-9">
                <%-- TODO: Implement badge with correct color and rounded corners. --%>
                <div class="modal-title-container">
                    <div class="badge modal-badge" style="">A</div>
                    <h4 class="modal-title">${asset.assetName}</h4>
                    <%-- TODO: Update Subtitle content with field --%>
                    <div class="modal-subtitle">${asset?.moveBundle}</div>
                    <div class="badge modal-subbadge"><tds:showDependencyGroup groupId="${dependencyBundleNumber}" assetName="${asset.assetName}"/></div>
                </div>

                <div class="modal-description" [ngClass]="{'modal-description-sized':showDetails, 'modal-description-height':${!!asset.description?.trim()}}">
                    <div *ngIf="readMore">
                        <p>${asset.description} <a (click)="readMore = !readMore">Read Less</a></p>
                    </div>
                    <div *ngIf="!readMore" class="readMore">
                        <g:if test="${asset.description?.length() > 80}">
                            <div class="truncated-description">${asset.description.substring(0,80)}...</div>
                            <a (click)="readMore = !readMore">Read More</a>
                        </g:if>
                        <g:else>
                            <div class="truncated-description">${asset.description}</div>
                        </g:else>
                    </div>
                </div>
            </div>

            <div  class="clr-col-3">
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
                            <g:set var="supportsCounter" value="${supportAssets.size() as Integer}"/>
                            <g:if test="${supportsCounter > 0}">
                                <span class="badge" id="asset-detail-support-counter">
                                    <g:if test="${supportsCounter > 99}">
                                        99+
                                    </g:if>
                                    <g:else>
                                        ${supportsCounter}
                                    </g:else>
                                </span>
                            </g:if>
                        </button>
                    </tds-scroller-item>
                    <tds-scroller-item>
                        <button tdsScrollerLink>Depends On
                            <g:set var="dependentCounter" value="${dependentAssets.size() as Integer}"/>
                            <g:if test="${dependentCounter > 0}">
                                <span class="badge" id="asset-detail-dependent-counter">
                                    <g:if test="${dependentCounter > 99}">
                                        99+
                                    </g:if>
                                    <g:else>
                                        ${dependentCounter}
                                    </g:else>
                                </span>
                            </g:if>
                        </button>
                    </tds-scroller-item>
                    <tds-scroller-item>
                        <button tdsScrollerLink>Tasks
                            <span class="badge" *ngIf="taskCount">
                                {{ taskCount > 99 ? '99+' : taskCount }}
                            </span>
                        </button>
                    </tds-scroller-item>
                    <tds-scroller-item>
                        <button tdsScrollerLink>Comments
                            <span class="badge" *ngIf="commentCount">
                                {{ commentCount > 99 ? '99+' : commentCount }}
                            </span>
                        </button>
                    </tds-scroller-item>
                </tds-tab-scroller>
            </div>
        </div>
	</div>

	<div class="modal-body asset-crud" [ngClass]="{'has-description': ${!!asset.description?.trim()}, 'no-description': ${!asset.description?.trim()}}" tdsScrollContainer style="position: relative">
		<div tdsScrollSection class="clr-row">
			<div [ngClass]="{'clr-col-12':showDetails, 'clr-col-6':!showDetails}">
				<g:if test="${errors}">
					<div id="messageDivId" class="message">${errors}</div>
				</g:if>
				<g:render template="/angular/application/show" model="[asset:applicationInstance]"></g:render>
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
					model="[supportAssets:supportAssets]" >
				</g:render>
			</div>
		</div>
		<div tdsScrollSection class="clr-row">
			<div class="clr-col-12">
				<g:render 
					template="/angular/common/dependentShow" 
					model="[dependentAssets:dependentAssets, assetEntity: applicationInstance]" >
				</g:render>
			</div>
		</div>
		<div tdsScrollSection class="clr-row">
			<g:render
				template="/angular/common/commentList"
				model="[
					asset:applicationInstance,
					prefValue: prefValue,
					viewUnpublishedValue: viewUnpublishedValue,
					currentUserId: currentUserId,
					showTask:true,
					showComment:false,
				]" >
			</g:render>
		</div>
		<div tdsScrollSection class="clr-row">
			<g:render
				template="/angular/common/commentList"
				model="[
					asset:applicationInstance,
					prefValue: prefValue,
					viewUnpublishedValue: viewUnpublishedValue,
					currentUserId: currentUserId,
					showTask:false,
					showComment:true,
				]" >
			</g:render>
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
