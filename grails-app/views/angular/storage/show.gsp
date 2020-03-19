<%@page import="com.tdsops.tm.enums.domain.SizeScale"%>
<%@page import="net.transitionmanager.security.Permission"%>
<%@page defaultCodec="html" %>

<div >
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
                            <span class="badge" *ngIf='commentCount'>
                                {{ commentCount > 99 ? '99+' : commentCount }}
                            </span>
                        </button>
                    </tds-scroller-item>
                </tds-tab-scroller>
            </div>
        </div>
	</div>
	<div class="asset-crud" [ngClass]="{'has-description': ${!!asset.description?.trim()}, 'no-description': ${!asset.description?.trim()}}" tdsScrollContainer style="position: relative">
		<div tdsScrollSection class="clr-row">
			<div [ngClass]="{'clr-col-12':showDetails, 'clr-col-6':!showDetails}">
				<g:if test="${errors}">
					<div id="messageDivId" class="message">${errors}</div>
				</g:if>
				<table class="tdr-detail-list" [ngClass]="{'all-details':showDetails}">
					<tbody [ngClass]="{'one-column':!showDetails, 'three-column':showDetails}">
						<tds:clrRowDetail style="order: 15" field="${standardFieldSpecs.fileFormat}" value="${asset.fileFormat}" />
						<tds:clrRowDetail style="order: 20" field="${standardFieldSpecs.LUN}" value="${asset.LUN}" />
						<tds:clrRowDetail style="order: 25" field="${standardFieldSpecs.supportType}" value="${asset.supportType}" />
						
						<tr style="order: 30">
							<th class="${standardFieldSpecs.moveBundle.imp?:''}">									
								${standardFieldSpecs.moveBundle.label} : Dep. Group
							</th>
							<td>
								${filesInstance?.moveBundle}
								<g:if test="${dependencyBundleNumber}">:</g:if>
								<tds:showDependencyGroup groupId="${dependencyBundleNumber}" assetName="${filesInstance.assetName}"/>
							</td>
						</tr>
						
						<tds:clrRowDetail style="order: 35" field="${standardFieldSpecs.size}" value="${asset.size}" />
						<tds:clrRowDetail style="order: 40" field="${standardFieldSpecs.externalRefId}" value="${asset.externalRefId}" />
						<tds:clrRowDetail style="order: 45" field="${standardFieldSpecs.environment}" value="${asset.environment}" />
						<tds:clrRowDetail style="order: 50" field="${standardFieldSpecs.planStatus}" value="${asset.planStatus}" />
						<tds:clrRowDetail style="order: 55" field="${standardFieldSpecs.rateOfChange}" value="${asset.rateOfChange}" />
						<tds:clrRowDetail style="order: 60" field="${standardFieldSpecs.validation}" value="${asset.validation}" />

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
