<%@page import="net.transitionmanager.asset.Application"%>
<%@page import="net.transitionmanager.security.Permission"%>
<%@page defaultCodec="html" %>

<g:set var="assetClass" value="${(new Application()).assetClass}" />

<div tds-handle-double-click (doubleClick)="onDoubleClick()" tds-autocenter tds-autofocus tds-handle-escape (escPressed)="cancelCloseDialog()" class="modal-content tds-angular-component-content">
	<div class="modal-header">
		<button aria-label="Close" class="close" type="button" (click)="cancelCloseDialog()"><span  aria-hidden="true">×</span></button>
		<h4 class="modal-title">Application Detail</h4>
	</div>
	<div class="modal-body">
		<div>
			<table style="border: 0;" class="tds-asset-view-content-table">
				<tr>

					<td colspan="2" class="dialog-container"><div class="dialog">
							<g:if test="${errors}">
								<div id="messageDivId" class="message">${errors}</div>
							</g:if>
							<g:render template="/angular/application/show" model="[asset:applicationInstance]" ></g:render>
						</div>
					</td>
				</tr>
				<tr>
					<td colspan="2">
						<div class="dates-info-container">
							<table class="dates-info">
								<tr>
									<td class="date-created">Date created: ${dateCreated}</td>
									<td class="last-updated">Last updated: ${lastUpdated}</td>
								</tr>
							</table>
						</div>
					</td>
				</tr>
				<tr id="deps">
					<g:render 
						template="/angular/common/dependentShow" 
						model="[supportAssets:supportAssets, dependentAssets:dependentAssets, assetEntity: applicationInstance]" >
					</g:render>
				</tr>
				<tr id="commentListId">
					<g:render 
						template="/angular/common/commentList" 
						model="[asset:applicationInstance, 'prefValue': prefValue, 'viewUnpublishedValue': viewUnpublishedValue, currentUserId: currentUserId]" >
					</g:render>
				</tr>
			</table>
		</div>
	</div>
	<div class="modal-footer form-group-center">
		<div class="asset-commands pull-left">
			<tds-button-edit
					class="btn-primary"
					tooltip="Edit Asset"
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
</div>
