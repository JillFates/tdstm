<%@page import="com.tdsops.tm.enums.domain.SizeScale"%>
<%@page import="net.transitionmanager.security.Permission"%>
<%@page defaultCodec="html" %>

<div tds-autocenter tds-autofocus tds-handle-escape (escPressed)="cancelCloseDialog()" class="tds-modal-content tds-angular-component-content">
	<div class="modal-header">
		<button aria-label="Close" class="close" type="button" (click)="cancelCloseDialog()"><span  aria-hidden="true">×</span></button>
		<h4 class="modal-title">Logical Storage Detail</h4>
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
									<tds:clrRowDetail field="${standardFieldSpecs.fileFormat}" value="${asset.fileFormat}" />
									<tds:clrRowDetail field="${standardFieldSpecs.LUN}" value="${asset.LUN}" />
									<tds:clrRowDetail field="${standardFieldSpecs.supportType}" value="${asset.supportType}" />



									<tr>
										<th>									
											${standardFieldSpecs.moveBundle.label} : Dep. Group
										</th>
										<td>
											${filesInstance?.moveBundle}
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
                            <g:render template="/angular/common/dependentShow" model="[assetEntity:filesInstance]" ></g:render>
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
								asset:filesInstance, 
								prefValue: prefValue, 
								viewUnpublishedValue: viewUnpublishedValue, 
								currentUserId: currentUserId,
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