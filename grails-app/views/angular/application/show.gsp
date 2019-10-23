<%@page import="net.transitionmanager.asset.Application"%>
<%@page import="net.transitionmanager.security.Permission"%>
<%@page defaultCodec="html" %>

<g:set var="assetClass" value="${(new Application()).assetClass}" />

<div tds-autocenter tds-autofocus tds-handle-escape (escPressed)="cancelCloseDialog()" class="tds-modal-content tds-angular-component-content">
	<div class="modal-header">
		<tds-button-close aria-label="Close" class="close" icon="close" [flat]="true" (click)="cancelCloseDialog()"></tds-button-close>
		<h4 class="modal-title">Application Detail</h4>
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
							<g:render template="/angular/application/show" model="[asset:applicationInstance]" ></g:render>
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
								model="[dependentAssets:dependentAssets, assetEntity: applicationInstance]" >
							</g:render>
						</div>
					</div>
				</clr-tab-content>
			</clr-tab>
			<clr-tab>
				<button clrTabLink>Tasks</button>
				<clr-tab-content *clrIfActive>
					<div class="clr-row">
						<div  class="clr-col-12">
							<g:render 
								template="/angular/common/commentList" 
								model="[
									asset:applicationInstance, 
									'prefValue': prefValue, 
									'viewUnpublishedValue': viewUnpublishedValue, 
									currentUserId: currentUserId,
									showTask:true,
									showComment:false,
								]" >
							</g:render>
						</div>
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
									asset:applicationInstance, 
									'prefValue': prefValue, 
									'viewUnpublishedValue': viewUnpublishedValue, 
									currentUserId: currentUserId,
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