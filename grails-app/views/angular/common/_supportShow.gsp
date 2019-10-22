<%@page defaultCodec="html" %>

<h1>Supports:</h1>
<table class="planning-application-table">
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
			<tr class="${i%2? 'odd':'even' }" style="cursor: pointer;">
				<td class="dep-${support.status}" (click)="showAssetDetailView('${support?.asset?.assetClass}', ${support?.asset?.id})">
					${support?.asset?.assetType}
				</td>
				<td class="dep-${support.status}" (click)="showAssetDetailView('${support?.asset?.assetClass}', ${support?.asset?.id})" style="min-width:80px">
					${support?.asset?.assetName}
				</td>
				<g:if test="${support?.asset?.moveBundle!=asset.moveBundle && support.status == 'Validated' }">
					<td style="background-color: lightpink;position:relative;" (click)="showAssetDetailView('${support?.asset?.assetClass}', ${support?.asset?.id})">
						<div style="padding: 5px 25px 5px 0px;">${support?.asset?.moveBundle}</div>
						<div class="text-center" style="position:absolute;right:5px;top:20%;">
							<asset:image src="icons/error.png" width="19" height="19" alt="..." data-toggle="popover" data-trigger="hover" data-content="The linked assets have conflicting bundles." />
						</div>
					</td>
				</g:if>
				<g:elseif test="${support?.asset?.moveBundle!=asset.moveBundle }">
					<td class="dep-${support.status}" style="position:relative;" (click)="showAssetDetailView('${support?.asset?.assetClass}', ${support?.asset?.id})">
						<b>
							<div style="padding: 5px 25px 5px 0px;">${support?.asset?.moveBundle}</div>
							<div class="text-center" style="position:absolute;right:5px;top:20%;">
								<asset:image src="icons/error.png" width="19" height="19" alt="..." data-toggle="popover" data-trigger="hover" data-content="The linked assets have conflicting bundles." />
							</div>
						</b>
					</td>
				</g:elseif>
				<g:else>
					<td class="dep-${support.status}" (click)="showAssetDetailView('${support?.asset?.assetClass}', ${support?.asset?.id})">
						${support?.asset?.moveBundle}
					</td>
				</g:else>
				<td class="dep-${support.status}" nowrap="nowrap" (click)="showDependencyView(${support.asset.id},${support.dependent.id})">
					${support.type} &nbsp;
					<g:render template="/angular/common/dependentComment" model="[dependency:support, type:'support', forWhom:'show']"></g:render>
				</td>
				<td class="dep-${support.status}" (click)="showDependencyView(${support.asset.id},${support.dependent.id})">
					${support.status}
				</td>
			</tr>
		</g:each>
	</tbody>
</table>
