<!-- database-show.component.ts -->
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
		<p class="modal-description">${asset.description}</p>
		<tds-tab-scroller>
			<tds-scroller-item>
				<button tdsScrollerLink>Details</button>
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
    <div class="modal-body show" tdsScrollContainer style="position: relative">
		<div tdsScrollSection class="clr-row">
			<div class="clr-col-12">
				<g:if test="${errors}">
					<div id="messageDivId" class="message">${errors}</div>
				</g:if>
				<a (click)="showDetails = !showDetails">Toggle All Details</a>
				<table class="tdr-detail-list" [ngClass]="{'all-details':showDetails}">
					<tbody [ngClass]="{'one-column':!showDetails, 'two-column':showDetails}">
						<tds:clrRowDetail field="${standardFieldSpecs.dbFormat}" value="${asset.dbFormat}" />
						<tds:clrRowDetail field="${standardFieldSpecs.supportType}" value="${asset.supportType}" />
						<tds:clrRowDetail field="${standardFieldSpecs.environment}" value="${asset.environment}" />

						<tr>
							<th class="${standardFieldSpecs.size.imp?:''}">
								${standardFieldSpecs.size.label}/${standardFieldSpecs.scale.label}
							</th>
							<td>${asset?.size}&nbsp;${asset.scale?.value()}</td>
						</tr>

						<tr>
							<tds:clrInputLabel field="${standardFieldSpecs.retireDate}" value="${asset.retireDate}"/>
							<td>{{ '${asset?.retireDate}' | tdsDate: userDateFormat }}</td>
						</tr>

						<tr>
							<th class="${standardFieldSpecs.moveBundle.imp?:''}">
								${standardFieldSpecs.moveBundle.label} : Dep. Group
							</th>
							<td>
								${asset?.moveBundle}
								<tds:showDependencyGroup groupId="${dependencyBundleNumber}" assetName="${asset.assetName}"/>
							</td>
						</tr>

						<tds:clrRowDetail field="${standardFieldSpecs.rateOfChange}" value="${asset.rateOfChange}" />

						<tr>
							<tds:clrInputLabel field="${standardFieldSpecs.maintExpDate}" value="${asset.maintExpDate}"/>
							<td>
								{{ '${asset?.maintExpDate}' | tdsDate: userDateFormat }}
							</td>
						</tr>

						<tr>
							<tds:clrInputLabel field="${standardFieldSpecs.planStatus}" value="${asset.planStatus}"/>
							<td>${asset.planStatus}</td>
						</tr>

						<tds:clrRowDetail field="${standardFieldSpecs.externalRefId}" value="${asset.externalRefId}" />

						<tr>
							<tds:clrInputLabel field="${standardFieldSpecs.validation}" value="${asset.validation}"/>
							<td>${asset.validation}</td>
						</tr>

						<g:render template="/angular/common/customShow" model="[asset:asset, project:project]"></g:render>
					</tbody>
				</table>
				<g:render template="/angular/common/assetTags"></g:render>
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
					model="[assetEntity:asset]" >
				</g:render>
			</div>
		</div>
	
		<div tdsScrollSection class="clr-row">
			<g:render 
				template="/angular/common/commentList" 
				model="[
					asset:asset, 
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
						asset:asset, 
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