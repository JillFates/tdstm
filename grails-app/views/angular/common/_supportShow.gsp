<%@page defaultCodec="html" %>

<h1>Supports:</h1>
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
		<g:each in="${supportAssets}" var="support" status="i">
			<tr>
				<td (click)="showAssetDetailView('${support?.asset?.assetClass}', ${support?.asset?.id})">
					${support?.asset?.assetType}
				</td>
				<td (click)="showAssetDetailView('${support?.asset?.assetClass}', ${support?.asset?.id})">
					${support?.asset?.assetName}
				</td>
				<g:if test="${support?.asset?.moveBundle!=asset.moveBundle && support.status == 'Validated' }">
					<td style="position:relative;" (click)="showAssetDetailView('${support?.asset?.assetClass}', ${support?.asset?.id})">
						<div style="padding: 5px 25px 5px 0px;">${support?.asset?.moveBundle}</div>
						<div class="text-center" style="position:absolute;right:5px;top:20%;">
							<asset:image src="icons/error.png" width="19" height="19" alt="..." data-toggle="popover" data-trigger="hover" data-content="The linked assets have conflicting bundles." />
						</div>
					</td>
				</g:if>
				<g:elseif test="${support?.asset?.moveBundle!=asset.moveBundle }">
					<td style="position:relative;" (click)="showAssetDetailView('${support?.asset?.assetClass}', ${support?.asset?.id})">
						<strong>
							<div style="padding: 5px 25px 5px 0px;">${support?.asset?.moveBundle}</div>
							<div class="text-center" style="position:absolute;right:5px;top:20%;">
								<asset:image src="icons/error.png" width="19" height="19" alt="..." data-toggle="popover" data-trigger="hover" data-content="The linked assets have conflicting bundles." />
							</div>
						</strong>
					</td>
				</g:elseif>
				<g:else>
					<td (click)="showAssetDetailView('${support?.asset?.assetClass}', ${support?.asset?.id})">
						${support?.asset?.moveBundle}
					</td>
				</g:else>
				<td nowrap="nowrap" (click)="showDependencyView(${support.asset.id},${support.dependent.id})">
					${support.type} &nbsp;
					<g:render template="/angular/common/dependentComment" model="[dependency:support, type:'support', forWhom:'show']"></g:render>
				</td>
				<td (click)="showDependencyView(${support.asset.id},${support.dependent.id})">
					<g:if test="${support.status == 'Validated'}">
						<span class="status status-Ready">
							<clr-icon shape="thumbs-up" class="is-solid"></clr-icon>
						</span>
					</g:if>
					<g:if test="${support.status == 'Ready'}">
						<span class="status status-Ready">
							<clr-icon *ngSwitchCase="'Ready'" shape="thumbs-up" class="is-solid"></clr-icon>
						</span>
					</g:if>
					<g:if test="${support.status == 'Started'}">
						<span class="status status-Started">
							<clr-spinner *ngSwitchCase="'Started'" clrInline class="static"></clr-spinner>
						</span>
					</g:if>
					<g:if test="${support.status == 'Hold'}">
						<span class="status status-Hold">
							<clr-icon *ngSwitchCase="'Hold'" shape="pause" class="is-solid"></clr-icon>
						</span>
					</g:if>
					<g:if test="${support.status == 'Completed'}">
						<span class="status status-Completed">
							<clr-icon *ngSwitchCase="'Completed'" shape="check" class="is-solid"></clr-icon>
						</span>
					</g:if>
					<g:if test="${support.status == 'Pending'}">
						<span class="status status-Pending">
							<clr-icon *ngSwitchDefault shape="minus" class="is-solid"></clr-icon>
						</span>
					</g:if>
					${support.status}
				</td>
			</tr>
		</g:each>
	</tbody>
</table>
