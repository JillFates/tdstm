<%@page defaultCodec="html" %>
<h1>Is dependent on:</h1>
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
		<g:each in="${dependentAssets}" var="dependent" status="i">
			<tr class="${i%2? 'odd':'even' }" style="cursor: pointer;">
				<td class="dep-${dependent.status}" (click)="showAssetDetailView('${dependent.dependent?.assetClass}', ${dependent.dependent?.id})">
					${dependent.dependent?.assetType}
				</td>
				<td class="dep-${dependent.status}" style="min-width:80px" (click)="showAssetDetailView('${dependent.dependent?.assetClass}', ${dependent.dependent?.id})">
					${dependent.dependent?.assetName}
				</td>
				<g:if test="${dependent.dependent?.moveBundle!=asset.moveBundle && dependent.status == 'Validated' }">
					<td style="background-color: lightpink;position:relative;" (click)="showAssetDetailView('${dependent.dependent?.assetClass}', ${dependent.dependent?.id})">
						<div style="padding: 5px 25px 5px 0px;">${dependent.dependent?.moveBundle}</div>
						<div class="text-center" style="position:absolute;right:5px;top:20%;">
							<asset:image src="icons/error.png" width="19" height="19" alt="..." data-toggle="popover" data-trigger="hover" data-content="The linked assets have conflicting bundles." />
						</div>
					</td>
				</g:if>
				<g:elseif test="${dependent.dependent?.moveBundle!=asset.moveBundle }">
					<td class="dep-${dependent.status}" style="position:relative;" (click)="showAssetDetailView('${dependent.dependent?.assetClass}', ${dependent.dependent?.id})">
						<b>
							<div style="padding: 5px 25px 5px 0px;">${dependent.dependent?.moveBundle}</div>
							<div class="text-center" style="position:absolute;right:5px;top:20%;">
								<asset:image src="icons/error.png" width="19" height="19" alt="..." data-toggle="popover" data-trigger="hover" data-content="The linked assets have conflicting bundles." />
							</div>
						</b>
					</td>
				</g:elseif>
				<g:else>
					<td class="dep-${dependent.status}" (click)="showAssetDetailView('${dependent.dependent?.assetClass}', ${dependent.dependent?.id})">
						${dependent.dependent?.moveBundle}
					</td>
				</g:else>
				<td class="dep-${dependent.status}" nowrap="nowrap" (click)="showDependencyView(${assetEntity.id}, ${dependent.dependent.id})">
					${dependent.type}&nbsp;
					<g:render template="/angular/common/dependentComment" model="[dependency:dependent, type:'dependent', forWhom:'show']"></g:render>
				</td>
				<td class="dep-${dependent.status}" (click)="showDependencyView(${assetEntity.id}, ${dependent.dependent.id})">
					${dependent.status}
				</td>
			</tr>
		</g:each>
	</tbody>
</table>

