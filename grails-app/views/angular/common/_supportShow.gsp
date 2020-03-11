<%@page defaultCodec="html" %>
<div class="tds-table">
	<div class="clr-row">
		<div class="grid-label clr-col-4">
			<strong>Supports</strong>
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
			<g:each in="${supportAssets}" var="support" status="i">
				<g:set var="supportRow">${support.asset.id}${support.dependent.id}</g:set>
				<tr id="${supportRow}" class="asset-detail-support-row">
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
								<span class="status status-Warning">
									<clr-icon shape="exclamation-triangle" class="is-solid"></clr-icon>
								</span>
							</div>
						</td>
					</g:if>
					<g:elseif test="${support?.asset?.moveBundle!=asset.moveBundle }">
						<td style="position:relative;" (click)="showAssetDetailView('${support?.asset?.assetClass}', ${support?.asset?.id})">
							<strong>
								<div style="padding: 5px 25px 5px 0px;">${support?.asset?.moveBundle}</div>
								<div class="text-center" style="position:absolute;right:5px;top:20%;">
									<span class="status status-Warning">
										<clr-icon shape="exclamation-triangle" class="is-solid"></clr-icon>
									</span>
								</div>
							</strong>
						</td>
					</g:elseif>
					<g:else>
						<td (click)="showAssetDetailView('${support?.asset?.assetClass}', ${support?.asset?.id})">
							${support?.asset?.moveBundle}
						</td>
					</g:else>
					<td nowrap="nowrap" (click)="showDependencyView('support', ${support.asset.id},${support.dependent.id}, ${supportRow})">
						${support.type} &nbsp;
						<g:render template="/angular/common/dependentComment" model="[dependency:support, type:'support', forWhom:'show']"></g:render>
					</td>
					<td (click)="showDependencyView('support',  ${support.asset.id},${support.dependent.id}, ${supportRow})">
						<g:if test="${support.status == 'Validated'}">
							<span class="status-icon status-Ready">
								<fa-icon [icon]="['fas', 'thumbs-up']"></fa-icon>
							</span>
						</g:if>
						<g:if test="${support.status == 'Ready'}">
							<span class="status-icon status-Ready">
								<fa-icon [icon]="['fas', 'thumbs-up']"></fa-icon>
							</span>
						</g:if>
						<g:if test="${support.status == 'Started'}">
							<span class="status-icon status-Started">
								<fa-icon [icon]="['fas', 'circle-notch']"></fa-icon>
							</span>
						</g:if>
						<g:if test="${support.status == 'Hold'}">
							<span class="status-icon status-Hold">
								<fa-icon [icon]="['fas', 'pause']"></fa-icon>
							</span>
						</g:if>
						<g:if test="${support.status == 'Completed'}">
							<span class="status-icon status-Completed">
								<fa-icon [icon]="['fas', 'check']"></fa-icon>
							</span>
						</g:if>
						<g:if test="${support.status == 'Pending'}">
							<span class="status-icon status-Pending">
								<fa-icon [icon]="['fas', 'hourglass-start']"></fa-icon>
							</span>
						</g:if>
						<g:if test="${support.status == 'Unknown'}">
							<span class="status-icon status-Unknown">
								<fa-icon [icon]="['fas', 'question-circle']"></fa-icon>
							</span>
						</g:if>
						<g:if test="${support.status == 'Archived'}">
							<span class="status-icon status-Archived">
								<fa-icon [icon]="['fas', 'archive']"></fa-icon>
							</span>
						</g:if>
						<g:if test="${support.status == 'Future'}">
							<span class="status-icon status-Future">
								<fa-icon [icon]="['far', 'share-square']"></fa-icon>
							</span>
						</g:if>
						<g:if test="${support.status == 'Validated_NA'}">
							<span class="status-icon status-Validated_NA">
								<fa-icon [icon]="['fas', 'square']"></fa-icon>
							</span>
						</g:if>
						<g:if test="${support.status == 'Not Applicable'}">
							<span class="status-icon status-Not-Applicable">
								<fa-icon [icon]="['fas', 'ban']"></fa-icon>
							</span>
						</g:if>
						<g:if test="${support.status == 'Questioned'}">
							<span class="status-icon status-Questioned">
								<fa-icon [icon]="['fas', 'exclamation-triangle']"></fa-icon>
							</span>
						</g:if>
						${support.status}
					</td>
				</tr>
			</g:each>
		</tbody>
	</table>
</div>
