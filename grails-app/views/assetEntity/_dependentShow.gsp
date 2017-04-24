<%@page defaultCodec="html" %> 

<td valign="top">
			<div>
				<h1>Supports:</h1>
				<table style="min-width: 400px;" class="planning-application-table table-responsive">
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
									<td style="background-color: lightpink;min-width:140px;">
										<div class="" style="padding: 5px;float:left;max-width:100px;">${support?.asset?.moveBundle}</div>
										<div class="text-center customHoover" style="padding: 5px;float:left;"><img src="${resource(dir:'icons', file:'error.png')}" width="17" height="17" alt="..." data-toggle="tooltip" title="The linked assets have conflicting bundles."/></div>
									</td>
								</g:if>
								<g:elseif test="${support?.asset?.moveBundle!=assetEntity.moveBundle }" >
									<td class="dep-${support.status}" style="min-width:140px;">
										<b>
											<div class="" style="padding: 5px;float:left;max-width:100px;">${support?.asset?.moveBundle}</div>
											<div class="text-center customHoover" style="padding: 5px;float:left;"><img src="${resource(dir:'icons', file:'error.png')}" width="17" height="17" alt="..." data-toggle="tooltip" title="The linked assets have conflicting bundles."/></div>
										</b>
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
				<table style="min-width: 400px;" class="planning-application-table table-responsive">
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
									<td style="background-color: lightpink;min-width:140px;">
										<div class="col-xs-9" style="padding: 5px;float:left;max-width:100px;">${dependent.dependent?.moveBundle}</div>
										<div class="col-xs-3 text-center customHoover" style="padding: 5px;float:left;"><img src="${resource(dir:'icons', file:'error.png')}" width="17" height="18" alt="..." data-toggle="tooltip" title="The linked assets have conflicting bundles."/></div>
									</td>
								</g:if>
								<g:elseif test="${dependent.dependent?.moveBundle!=assetEntity.moveBundle }" >
									<td class="dep-${dependent.status}" style="min-width:140px;">
										<b>
											<div class="" style="padding: 5px;float:left;max-width:100px;">${dependent.dependent?.moveBundle}</div>
											<div class="text-center customHoover" style="padding: 5px;float:left;"><img src="${resource(dir:'icons', file:'error.png')}" width="17" height="18" alt="..." data-toggle="tooltip" title="The linked assets have conflicting bundles."/></div>
										</b>
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