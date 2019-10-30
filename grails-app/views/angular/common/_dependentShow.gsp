<%@page defaultCodec="html" %>
<div class="clr-row">
	<div class="grid-label clr-col-4">
		<strong>Is dependent on</strong>
	</div>
</div>
<table class="support-depends-table" style="border-collapse:collapse">
	<thead>
		<tr>
			<th>Class</th>
			<th>Name</th>
			<th>Bundle</th>
			<th>Type</th>
			<th>Status</th>
		</tr>
	</thead>
	<tbody>
		<g:each in="${dependentAssets}" var="dependent" status="i">
			<tr>
				<td (click)="showAssetDetailView('${dependent.dependent?.assetClass}', ${dependent.dependent?.id})">
					${dependent.dependent?.assetType}
				</td>
				<td (click)="showAssetDetailView('${dependent.dependent?.assetClass}', ${dependent.dependent?.id})">
					${dependent.dependent?.assetName}
				</td>
				<g:if test="${dependent.dependent?.moveBundle!=asset.moveBundle && dependent.status == 'Validated' }">
					<td style="position:relative;" (click)="showAssetDetailView('${dependent.dependent?.assetClass}', ${dependent.dependent?.id})">
						<div style="padding: 5px 25px 5px 0px;">${dependent.dependent?.moveBundle}</div>
						<div class="text-center" style="position:absolute;right:5px;top:20%;">
							<asset:image src="icons/error.png" width="19" height="19" alt="..." data-toggle="popover" data-trigger="hover" data-content="The linked assets have conflicting bundles." />
						</div>
					</td>
				</g:if>
				<g:elseif test="${dependent.dependent?.moveBundle!=asset.moveBundle }">
					<td style="position:relative;" (click)="showAssetDetailView('${dependent.dependent?.assetClass}', ${dependent.dependent?.id})">
						<strong>
							<div style="padding: 5px 25px 5px 0px;">${dependent.dependent?.moveBundle}</div>
							<div class="text-center" style="position:absolute;right:5px;top:20%;">
								<asset:image src="icons/error.png" width="19" height="19" alt="..." data-toggle="popover" data-trigger="hover" data-content="The linked assets have conflicting bundles." />
							</div>
						</strong>
					</td>
				</g:elseif>
				<g:else>
					<td (click)="showAssetDetailView('${dependent.dependent?.assetClass}', ${dependent.dependent?.id})">
						${dependent.dependent?.moveBundle}
					</td>
				</g:else>
				<td nowrap="nowrap" (click)="showDependencyView(${assetEntity.id}, ${dependent.dependent.id})">
					${dependent.type}&nbsp;
					<g:render template="/angular/common/dependentComment" model="[dependency:dependent, type:'dependent', forWhom:'show']"></g:render>
				</td>
				<td (click)="showDependencyView(${assetEntity.id}, ${dependent.dependent.id})">
					<g:if test="${dependent.status == 'Validated'}">
						<span class="status status-Ready">
							<clr-icon shape="thumbs-up" class="is-solid"></clr-icon>
						</span>
					</g:if>
					<g:if test="${dependent.status == 'Ready'}">
						<span class="status status-Ready">
							<clr-icon *ngSwitchCase="'Ready'" shape="thumbs-up" class="is-solid"></clr-icon>
						</span>
					</g:if>
					<g:if test="${dependent.status == 'Started'}">
						<span class="status status-Started">
							<clr-spinner *ngSwitchCase="'Started'" clrInline class="static"></clr-spinner>
						</span>
					</g:if>
					<g:if test="${dependent.status == 'Hold'}">
						<span class="status status-Hold">
							<clr-icon *ngSwitchCase="'Hold'" shape="pause" class="is-solid"></clr-icon>
						</span>
					</g:if>
					<g:if test="${dependent.status == 'Completed'}">
						<span class="status status-Completed">
							<clr-icon *ngSwitchCase="'Completed'" shape="check" class="is-solid"></clr-icon>
						</span>
					</g:if>
					<g:if test="${dependent.status == 'Pending'}">
						<span class="status status-Pending">
							<clr-icon *ngSwitchDefault shape="minus" class="is-solid"></clr-icon>
						</span>
					</g:if>
					${dependent.status}
				</td>
			</tr>
		</g:each>
	</tbody>
</table>

