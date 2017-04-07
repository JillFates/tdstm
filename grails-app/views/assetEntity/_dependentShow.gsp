<%@page defaultCodec="html" %> 

<td valign="top">
			<div>
				<h1>Supports:</h1>
				<table style="min-width: 400px;" class="planning-application-table">
					<thead>
						<tr>
							<th>Frequency</th>
							<th>Class</th>
							<th>Name</th>
							<th>Bundle</th>
							<th>Type</th>
							<th>Status</th>
						</tr>
					</thead>
					<tbody>
						<g:each in="${supportAssets}" var="support" status="i">
							<tr onclick="EntityCrud.showAssetDetailView('${support?.asset?.assetClass}', ${support?.asset?.id})" class="${i%2? 'odd':'even' }" style="cursor: pointer;">
								<td class="dep-${support.status}">
									${support?.dataFlowFreq}
								</td>
								<td class="dep-${support.status}">
									${support?.asset?.assetType}
								</td>
								<td class="dep-${support.status}">
									${support?.asset?.assetName}
								</td>
								<g:if test="${support?.asset?.moveBundle!=assetEntity.moveBundle && support.status == 'Validated' }" >
									<td style="background-color: lightpink">
										${support?.asset?.moveBundle}&nbsp;<img src="${resource(dir:'icons', file:'error.png')}" style="margin-top:-8px;" width="17" height="17" alt="..." data-toggle="tooltip" title="The two assets of this dependency are assigned to different bundles which may be a concern if they need to be migrated together. The background will be highlighted if the status is Validated."/>
									</td>
								</g:if>
								<g:elseif test="${support?.asset?.moveBundle!=assetEntity.moveBundle }" >
									<td class="dep-${support.status}">
										<b>${support?.asset?.moveBundle}&nbsp;<img src="${resource(dir:'icons', file:'error.png')}" style="margin-top:-8px;" width="17" height="17" alt="..." data-toggle="tooltip" title="The two assets of this dependency are assigned to different bundles which may be a concern if they need to be migrated together. The background will be highlighted if the status is Validated."/></b>
									</td>
								</g:elseif>
								<g:else>
									<td class="dep-${support.status}">
										${support?.asset?.moveBundle}
								    </td>
								</g:else>
								<td class="dep-${support.status}" nowrap="nowrap">
									${support.type} &nbsp;
									<g:render template="../assetEntity/dependentComment" model= "[dependency:support, type:'support', forWhom:'show']"></g:render>
								</td>
								<td class="dep-${support.status}">
									${support.status}
								</td>
							</tr>
						</g:each>
					</tbody>
				</table>
			</div>
		</td>
		<td valign="top">
			<div>
				<h1>Is dependent on:</h1>
				<table style="min-width: 400px;" class="planning-application-table">
					<thead>
						<tr>
							<th>Frequency</th>
							<th>Class</th>
							<th>Name</th>
							<th>Bundle</th>
							<th>Type</th>
							<th>Status</th>
						</tr>
					</thead>
					<tbody>
						<g:each in="${dependentAssets}" var="dependent" status="i">
							<tr onclick="EntityCrud.showAssetDetailView('${dependent.dependent?.assetClass}', ${dependent.dependent?.id})" class="${i%2? 'odd':'even' }" style="cursor: pointer;">
								<td class="dep-${dependent.status}">
									${dependent.dataFlowFreq}
								</td>
								<td class="dep-${dependent.status}">
									${dependent.dependent?.assetType}
								</td>
								<td class="dep-${dependent.status}">
									${dependent.dependent?.assetName}
								</td>
								<g:if test="${dependent.dependent?.moveBundle!=assetEntity.moveBundle && dependent.status == 'Validated' }" >
									<td style="background-color: lightpink">
										${dependent.dependent?.moveBundle}&nbsp;<img src="${resource(dir:'icons', file:'error.png')}" style="margin-top:-8px;" width="17" height="18" alt="..." data-toggle="tooltip" title="The two assets of this dependency are assigned to different bundles which may be a concern if they need to be migrated together. The background will be highlighted if the status is Validated."/>
									</td>
								</g:if>
								<g:elseif test="${dependent.dependent?.moveBundle!=assetEntity.moveBundle }" >
									<td class="dep-${dependent.status}">
										<b>${dependent.dependent?.moveBundle}&nbsp;<img src="${resource(dir:'icons', file:'error.png')}" style="margin-top: -8px;" width="17" height="18" alt="..." data-toggle="tooltip" title="The two assets of this dependency are assigned to different bundles which may be a concern if they need to be migrated together. The background will be highlighted if the status is Validated."/></b>
									</td>
								 </g:elseif>
								 <g:else>
									<td class="dep-${dependent.status}">
										${dependent.dependent?.moveBundle}
									</td>
								 </g:else>
								<td class="dep-${dependent.status}" nowrap="nowrap">
									${dependent.type}&nbsp;
									<g:render template="../assetEntity/dependentComment" model= "[dependency:dependent, type:'dependent', forWhom:'show']"></g:render>
								</td>
								<td class="dep-${dependent.status}">
									${dependent.status}
								</td>
							</tr>
						</g:each>
					</tbody>
				</table>
			</div>
		</td>