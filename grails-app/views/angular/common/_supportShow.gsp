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
				<tr id="${supportRow}" class="asset-detail-supports-row">
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
					<td nowrap="nowrap" (click)="showDependencyView(${support.asset.id},${support.dependent.id}, ${supportRow})">
						${support.type} &nbsp;
						<g:render template="/angular/common/dependentComment" model="[dependency:support, type:'support', forWhom:'show']"></g:render>
					</td>
					<td (click)="showDependencyView(${support.asset.id},${support.dependent.id}, ${supportRow})">
						<g:if test="${support.status == 'Validated'}">
							<span class="status status-Ready">
								<clr-icon shape="thumbs-up" class="is-solid"></clr-icon>
							</span>
						</g:if>
						<g:if test="${support.status == 'Ready'}">
							<span class="status status-Ready">
								<clr-icon shape="thumbs-up" class="is-solid"></clr-icon>
							</span>
						</g:if>
						<g:if test="${support.status == 'Started'}">
							<span class="status status-Started">
								<clr-spinner clrInline class="static"></clr-spinner>
							</span>
						</g:if>
						<g:if test="${support.status == 'Hold'}">
							<span class="status status-Hold">
								<clr-icon shape="pause" class="is-solid"></clr-icon>
							</span>
						</g:if>
						<g:if test="${support.status == 'Completed'}">
							<span class="status status-Completed">
								<clr-icon shape="check" class="is-solid"></clr-icon>
							</span>
						</g:if>
						<g:if test="${support.status == 'Pending'}">
							<span class="status status-Pending">
								<clr-icon shape="minus" class="is-solid"></clr-icon>
							</span>
						</g:if>
						${support.status}
					</td>
				</tr>
			</g:each>
		</tbody>
	</table>
</div>
