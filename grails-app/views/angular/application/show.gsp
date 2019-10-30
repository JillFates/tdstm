<%@page import="net.transitionmanager.asset.Application"%>
<%@page import="net.transitionmanager.security.Permission"%>
<%@page defaultCodec="html" %>
<%@page import="grails.converters.JSON"%>

<g:set var="assetClass" value="${(new Application()).assetClass}" />

<div tds-autocenter tds-autofocus tds-handle-escape (escPressed)="cancelCloseDialog()" class="tds-modal-content tds-angular-component-content">
	<div class="modal-header">
		<tds-button-close aria-label="Close" class="close" icon="close" [flat]="true" (click)="cancelCloseDialog()"></tds-button-close>
		<%-- TODO: Implement badge with correct color and rounded corners. --%>
		<div class="modal-title-container">
			<div class="badge modal-badge" style="">A</div>
			<h4 class="modal-title">${asset.assetName}</h4>
			<%-- TODO: Update Subtitle content with field --%>
			<div class="modal-subtitle">Subtitle content</div>
			<div class="badge modal-subbadge">9</div>
		</div>
		<p class="modal-description">${asset.description}</p>
		<ul class="nav">
			<li class="nav-item"><button (click)="scroll($event, details, scrolling)" class="btn btn-link nav-link active">Details</button></li>
			<li class="nav-item">
				<button (click)="scroll($event, supports, scrolling)" class="btn btn-link nav-link">Supports 					
					<span class="badge">
						<g:if test="${supportAssets.size() > 99}">
							99+
						</g:if>
						<g:else>
							${supportAssets.size()}
						</g:else>
					</span>
				</button>
			</li>
			<li class="nav-item">
				<button (click)="scroll($event, depends, scrolling)" class="btn btn-link nav-link">Depends On
					<span class="badge">
						<g:if test="${dependentAssets.size() > 99}">
							99+
						</g:if>
						<g:else>
							${dependentAssets.size()}
						</g:else>
					</span>
				</button>
			</li>
			<li class="nav-item"><button (click)="scroll($event, tasks, scrolling)" class="btn btn-link nav-link">Tasks</button></li>
			<li class="nav-item"><button (click)="scroll($event, comments, scrolling)" class="btn btn-link nav-link">Comments</button></li>
		</ul>
	</div>

	<div class="modal-body" #scrolling>
		<div #details class="clr-row">
			<div class="clr-col-12">
				<g:if test="${errors}">
					<div id="messageDivId" class="message">${errors}</div>
				</g:if>
				<g:render template="/angular/application/show" model="[asset:applicationInstance]" ></g:render>
				<g:render template="/angular/common/assetTags"></g:render>
			</div>
		</div>
		<div #supports class="clr-row">
			<div class="clr-col-12">
				<g:render 
					template="/angular/common/supportShow" 
					model="[supportAssets:supportAssets]" >
				</g:render>
			</div>
		</div>
		<div #depends class="clr-row">
			<div class="clr-col-12">
				<g:render 
					template="/angular/common/dependentShow" 
					model="[dependentAssets:dependentAssets, assetEntity: applicationInstance]" >
				</g:render>
			</div>
		</div>
		<div #tasks class="clr-row">
			<div  class="clr-col-12">
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
		</div>
		<div #comments class="clr-row">
			<div  class="clr-col-12">
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