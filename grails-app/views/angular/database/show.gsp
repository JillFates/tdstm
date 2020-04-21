<!-- database-show.component.ts -->
<%@page import="com.tdsops.tm.enums.domain.SizeScale"%>
<%@page import="net.transitionmanager.security.Permission"%>
<%@page defaultCodec="html" %>

<div>
    <div>
		<div class="clr-row">
			<div class="clr-col-11">
				<tds-tab-scroller>
					<tds-scroller-item>
						<button tdsScrollerLink>
							{{ showDetails ? "Details" : "Summary"}}
						</button>
					</tds-scroller-item>
					<tds-scroller-item>
						<ng-template [ngIf]="metadata?.supports?.length > 0">
							<span class="badge" id="asset-detail-support-counter">
								{{ (metadata.supports.length > 99)? '99+': metadata.supports.length }}
							</span>
						</ng-template>
					</tds-scroller-item>
					<tds-scroller-item>
						<button tdsScrollerLink>Depends On
							<ng-template [ngIf]="metadata?.dependents?.length > 0">
								<span class="badge" id="asset-detail-dependent-counter">
									{{ (metadata.dependents.length > 99)? '99+': metadata.dependents.length }}
								</span>
							</ng-template>
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

    <div class="asset-crud" tdsScrollContainer style="position: relative">
		<div tdsScrollSection class="clr-row">
			<div [ngClass]="{'clr-col-12':showDetails, 'clr-col-6':!showDetails}">
				<g:if test="${errors}">
					<div id="messageDivId" class="message">${errors}</div>
				</g:if>
				<table class="tdr-detail-list" [ngClass]="{'all-details':showDetails}">
					<tbody [ngClass]="{'one-column':!showDetails, 'three-column':showDetails}">
						<tds:clrRowDetail style="order: 15" field="${standardFieldSpecs.dbFormat}" value="${asset.dbFormat}" />
						<tds:clrRowDetail style="order: 20" field="${standardFieldSpecs.supportType}" value="${asset.supportType}" />
						<tds:clrRowDetail style="order: 25" field="${standardFieldSpecs.environment}" value="${asset.environment}" />

						<tr style="order: 30">
							<th class="${standardFieldSpecs.size.imp?:''}">
								${standardFieldSpecs.size.label}/${standardFieldSpecs.scale.label}
							</th>
							<td>${asset?.size}&nbsp;${asset.scale?.value()}</td>
						</tr>

						<tr style="order: 35">
							<tds:clrInputLabel field="${standardFieldSpecs.retireDate}" value="${asset.retireDate}"/>
							<td>{{ '${asset?.retireDate}' | tdsDate: userDateFormat }}</td>
						</tr>

						<tr style="order: 40">
							<th class="${standardFieldSpecs.moveBundle.imp?:''}">
								${standardFieldSpecs.moveBundle.label} : Dep. Group
							</th>
							<td>
								${asset?.moveBundle}
								<g:if test="${dependencyBundleNumber}">:</g:if>
								<tds:showDependencyGroup groupId="${dependencyBundleNumber}" assetName="${asset.assetName}"/>
							</td>
						</tr>
						
						<tds:clrRowDetail style="order: 45" field="${standardFieldSpecs.rateOfChange}" value="${asset.rateOfChange}" />

						<tr style="order: 50">
							<tds:clrInputLabel field="${standardFieldSpecs.maintExpDate}" value="${asset.maintExpDate}"/>
							<td>
								{{ '${asset?.maintExpDate}' | tdsDate: userDateFormat }}
							</td>
						</tr>

						<tr style="order: 55">
							<tds:clrInputLabel field="${standardFieldSpecs.planStatus}" value="${asset.planStatus}"/>
							<td>${asset.planStatus}</td>
						</tr>

						<tds:clrRowDetail style="order: 60" field="${standardFieldSpecs.externalRefId}" value="${asset.externalRefId}" />

						<tr style="order: 65">
							<tds:clrInputLabel field="${standardFieldSpecs.validation}" value="${asset.validation}"/>
							<td>${asset.validation}</td>
						</tr>

						<g:render template="/angular/common/customShow" model="[asset:asset, project:project]"></g:render>
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
			<div class="clr-col-12 table-data-section">
				<g:render 
					template="/angular/common/supportShow" 
					model="[supportAssets:supportAssets]" >
				</g:render>
			</div>
		</div>
				
		<div tdsScrollSection class="clr-row">
			<div class="clr-col-12 table-data-section">
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
