<%@page import="com.tdsops.tm.enums.domain.SizeScale"%>
<%@page import="net.transitionmanager.security.Permission"%>
<%@page defaultCodec="html" %>

<div tds-autocenter tds-autofocus tds-handle-escape (escPressed)="cancelCloseDialog()" class="tds-modal-content tds-angular-component-content">
	<div class="modal-header">
		<button aria-label="Close" class="close" type="button" (click)="cancelCloseDialog()"><span aria-hidden="true">×</span></button>

		<div class="clr-row">
			<div class="clr-col-6">
				<div class="modal-title-container">
					<div class="badge modal-badge" style="">S</div>
					<h4 class="modal-title">${asset.assetName}</h4>
					<div class="modal-subtitle">${asset?.moveBundle}</div>
					<div class="badge modal-subbadge"><tds:showDependencyGroup groupId="${dependencyBundleNumber}" assetName="${asset.assetName}"/></div>
				</div>
				<p class="modal-description">${asset.description}</p>
			</div>

			<div  class="clr-col-6">
				<div class="minimized-arch-thumbnail" *ngIf="!!showDetails">
					<tds-lib-diagram-layout [data]="data$ | async" [layout]="diagramLayout$ | async" [linkTemplate]="linkTemplate$ | async" (expandActionDispatched)="onExpandActionDispatched()" [hideExpand]="false" [hideOverview]="true" [hideControlButtons]="true" *ngIf="!!showDetails" class="header-graph" #graph></tds-lib-diagram-layout>
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
						<button tdsScrollerLink>Tasks</button>
					</tds-scroller-item>
					<tds-scroller-item>
						<button tdsScrollerLink>Comments</button>
					</tds-scroller-item>
				</tds-tab-scroller>
			</div>
		</div>
	</div>
	<div class="modal-body" [ngClass]="{'has-description': ${!!asset.description?.trim()}, 'no-description': ${!asset.description?.trim()}}" tdsScrollContainer style="position: relative">
		<div tdsScrollSection class="clr-row">
			<div [ngClass]="{'clr-col-12':showDetails, 'clr-col-6':!showDetails}">
				<g:if test="${errors}">
					<div id="messageDivId" class="message">${errors}</div>
				</g:if>
				<table class="tdr-detail-list" [ngClass]="{'all-details':showDetails}">
					<tbody [ngClass]="{'one-column':!showDetails, 'two-column':showDetails}">
						<tds:clrRowDetail field="${standardFieldSpecs.fileFormat}" value="${asset.fileFormat}" />
						<tds:clrRowDetail field="${standardFieldSpecs.LUN}" value="${asset.LUN}" />
						<tds:clrRowDetail field="${standardFieldSpecs.supportType}" value="${asset.supportType}" />
						
						<tr>
							<th class="${standardFieldSpecs.moveBundle.imp?:''}">									
								${standardFieldSpecs.moveBundle.label} : Dep. Group
							</th>
							<td>
								${filesInstance?.moveBundle}
								<g:if test="${dependencyBundleNumber}">:</g:if>
								<tds:showDependencyGroup groupId="${dependencyBundleNumber}" assetName="${filesInstance.assetName}"/>
							</td>
						</tr>
						
						<tds:clrRowDetail field="${standardFieldSpecs.size}" value="${asset.size}" />
						<tds:clrRowDetail field="${standardFieldSpecs.externalRefId}" value="${asset.externalRefId}" />
						<tds:clrRowDetail field="${standardFieldSpecs.environment}" value="${asset.environment}" />
						<tds:clrRowDetail field="${standardFieldSpecs.planStatus}" value="${asset.planStatus}" />
						<tds:clrRowDetail field="${standardFieldSpecs.rateOfChange}" value="${asset.rateOfChange}" />
						<tds:clrRowDetail field="${standardFieldSpecs.validation}" value="${asset.validation}" />

						<g:render template="/angular/common/customShow" model="[assetEntity:filesInstance]"></g:render>
					</tbody>
				</table>
				<g:render template="/angular/common/assetTags"></g:render>
				<a (click)="showDetails = !showDetails" class="show-hide-link">
					<span *ngIf="!showDetails">View All Fields</span>
					<span *ngIf="showDetails">Hide Additional Fields</span>
				</a>
			</div>
			<div class="clr-col-6 modal-body-graph" *ngIf="!showDetails">
				<tds-lib-diagram-layout [data]="data$ | async" [layout]="diagramLayout$ | async" [linkTemplate]="linkTemplate$ | async" (expandActionDispatched)="onExpandActionDispatched()" [hideExpand]="false" [hideOverview]="true" [hideControlButtons]="true" #graph></tds-lib-diagram-layout>
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
				<g:render template="/angular/common/dependentShow" model="[assetEntity:filesInstance]" ></g:render>
			</div>
		</div>
		<div tdsScrollSection class="clr-row">
			<g:render 
				template="/angular/common/commentList" 
				model="[
					asset:filesInstance, 
					prefValue: prefValue, 
					viewUnpublishedValue: viewUnpublishedValue, 
					currentUserId: currentUserId,
					showTask:true,
					showComment:false,
				]" >
			</g:render>
		</div>
		<div tdsScrollSection class="clr-row">
			<div  class="clr-col-12">
				<g:render 
					template="/angular/common/commentList" 
					model="[
						asset:filesInstance, 
						prefValue: prefValue, 
						viewUnpublishedValue: viewUnpublishedValue, 
						currentUserId: currentUserId,
						showTask:false,
						showComment:true,
					]" >
				</g:render>
			</div>
		</div>
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
