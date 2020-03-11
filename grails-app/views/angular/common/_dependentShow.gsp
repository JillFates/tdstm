<%@page defaultCodec="html" %>
<div class="tds-table">
	<div class="clr-row">
		<div class="grid-label clr-col-4">
			<strong>Depends On</strong>
		</div>
	</div>
	<table>
		<thead>
			<tr>
				<th style="width: 20%;">Class</th>
				<th style="width: 20%;">Name</th>
				<th style="width: 20%;">Bundle</th>
				<th style="width: 20%;">Type</th>
				<th style="width: 20%;">Status</th>
			</tr>
		</thead>
		<tbody>
			<g:each in="${dependentAssets}" var="dependent" status="i">
				<g:set var="dependentRow">${assetEntity.id}${dependent.dependent.id}</g:set>
				<tr id="${dependentRow}" class="asset-detail-dependent-row">
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
								<span class="status status-Warning">
									<clr-icon shape="exclamation-triangle" class="is-solid"></clr-icon>									
								</span>
							</div>
						</td>
					</g:if>
					<g:elseif test="${dependent.dependent?.moveBundle!=asset.moveBundle }">
						<td style="position:relative;" (click)="showAssetDetailView('${dependent.dependent?.assetClass}', ${dependent.dependent?.id})">
							<strong>
								<div style="padding: 5px 25px 5px 0px;">${dependent.dependent?.moveBundle}</div>
								<div class="text-center" style="position:absolute;right:5px;top:20%;">
									<span class="status status-Warning">
										<clr-icon shape="exclamation-triangle" class="is-solid"></clr-icon>
									</span>
								</div>
							</strong>
						</td>
					</g:elseif>
					<g:else>
						<td (click)="showAssetDetailView('${dependent.dependent?.assetClass}', ${dependent.dependent?.id})">
							${dependent.dependent?.moveBundle}
						</td>
					</g:else>
					<td nowrap="nowrap" (click)="showDependencyView('dependent', ${assetEntity.id}, ${dependent.dependent.id}, ${dependentRow})">
						${dependent.type}&nbsp;
						<g:render template="/angular/common/dependentComment" model="[dependency:dependent, type:'dependent', forWhom:'show']"></g:render>
					</td>
					<td (click)="showDependencyView('dependent',  ${assetEntity.id}, ${dependent.dependent.id}, ${dependentRow})">
						<g:if test="${dependent.status == 'Validated'}">
							<span class="status-icon status-Ready">
								<fa-icon [icon]="['fas', 'thumbs-up']"></fa-icon>
							</span>
						</g:if>
						<g:if test="${dependent.status == 'Ready'}">
							<span class="status-icon status-Ready">
								<fa-icon [icon]="['fas', 'thumbs-up']"></fa-icon>
							</span>
						</g:if>
						<g:if test="${dependent.status == 'Started'}">
							<span class="status-icon status-Started">
								<fa-icon [icon]="['fas', 'circle-notch']"></fa-icon>
							</span>
						</g:if>
						<g:if test="${dependent.status == 'Hold'}">
							<span class="status-icon status-Hold">
								<fa-icon [icon]="['fas', 'pause']"></fa-icon>
							</span>
						</g:if>
						<g:if test="${dependent.status == 'Completed'}">
							<span class="status-icon status-Completed">
								<fa-icon [icon]="['fas', 'check']"></fa-icon>
							</span>
						</g:if>
						<g:if test="${dependent.status == 'Pending'}">
							<span class="status-icon status-Pending">
								<fa-icon [icon]="['fas', 'hourglass-start']"></fa-icon>
							</span>
						</g:if>
						<g:if test="${dependent.status == 'Unknown'}">
							<span class="status-icon status-Unknown">
								<fa-icon [icon]="['fas', 'question-circle']"></fa-icon>
							</span>
						</g:if>
						<g:if test="${dependent.status == 'Archived'}">
							<span class="status-icon status-Archived">
								<fa-icon [icon]="['fas', 'archive']"></fa-icon>
							</span>
						</g:if>
						<g:if test="${dependent.status == 'Future'}">
							<span class="status-icon status-Future">
								<fa-icon [icon]="['far', 'share-square']"></fa-icon>
							</span>
						</g:if>
						<g:if test="${dependent.status == 'Validated_NA'}">
							<span class="status-icon status-Validated-NA">
								<fa-icon [icon]="['fas', 'square']"></fa-icon>
							</span>
						</g:if>
						<g:if test="${dependent.status == 'Not Applicable'}">
							<span class="status-icon status-Not-Applicable">
								<fa-icon [icon]="['fas', 'ban']" class="status-Not-Applicable"></fa-icon>
							</span>
						</g:if>
						<g:if test="${dependent.status == 'Questioned'}">
							<span class="status-icon status-Questioned">
								<fa-icon [icon]="['fas', 'exclamation-triangle']"></fa-icon>
							</span>
						</g:if>
						${dependent.status}
					</td>
				</tr>
			</g:each>
		</tbody>
	</table>
</div>
