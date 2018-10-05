<%@page defaultCodec="html" %>

<td valign="top">
			<div>
				<h1>Supports:</h1>
				<table style="min-width: 400px;" class="planning-application-table table-responsive">
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
								<td class="dep-${support.status}" onclick="EntityCrud.showAssetDetailView('${support?.asset?.assetClass}', ${support?.asset?.id})">
									${support?.asset?.assetType}
								</td>
								<td class="dep-${support.status}" style="min-width:80px" onclick="EntityCrud.showAssetDetailView('${support?.asset?.assetClass}', ${support?.asset?.id})">
									${support?.asset?.assetName}
								</td>
								<g:if test="${support?.asset?.moveBundle!=assetEntity.moveBundle && support.status == 'Validated' }" >
									<td style="background-color: lightpink;position:relative;" onclick="EntityCrud.showAssetDetailView('${support?.asset?.assetClass}', ${support?.asset?.id})">
										<div style="padding: 5px 25px 5px 0px;">${support?.asset?.moveBundle}</div>
										<div class="text-center" style="position:absolute;right:5px;top:20%;"><img src="${resource(dir:'icons', file:'error.png')}" width="17" height="17" alt="..." data-toggle="popover" data-trigger="hover"  data-content="The linked assets have conflicting bundles."/></div>
									</td>
								</g:if>
								<g:elseif test="${support?.asset?.moveBundle!=assetEntity.moveBundle }" >
									<td class="dep-${support.status}" style="position:relative;" onclick="EntityCrud.showAssetDetailView('${support?.asset?.assetClass}', ${support?.asset?.id})">
										<b>
											<div style="padding: 5px 25px 5px 0px;">${support?.asset?.moveBundle}</div>
											<div class="text-center" style="position:absolute;right:5px;top:20%;"><img src="${resource(dir:'icons', file:'error.png')}" width="17" height="17" alt="..." data-toggle="popover" data-trigger="hover" data-content="The linked assets have conflicting bundles."/></div>
										</b>
									</td>
								</g:elseif>
								<g:else>
									<td class="dep-${support.status}" onclick="EntityCrud.showAssetDetailView('${support?.asset?.assetClass}', ${support?.asset?.id})">
										${support?.asset?.moveBundle}
								    </td>
								</g:else>
								<td class="dep-${support.status}" nowrap="nowrap" onclick="EntityCrud.showAssetDependencyEditView({ id: '${support.asset.id}' }, { id: '${support.dependent.id}' }, 'view');" >
									${support.type} &nbsp;
									<g:render template="../assetEntity/dependentComment" model= "[dependency:support, type:'support', forWhom:'show']"></g:render>
								</td>
								<td class="dep-${support.status}" onclick="EntityCrud.showAssetDependencyEditView({ id: '${support.asset.id}' }, { id: '${support.dependent.id}' }, 'view');">
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
								<td class="dep-${dependent.status}" onclick="EntityCrud.showAssetDetailView('${dependent.dependent?.assetClass}', ${dependent.dependent?.id})">
									${dependent.dependent?.assetType}
								</td>
								<td class="dep-${dependent.status}" style="min-width:80px" onclick="EntityCrud.showAssetDetailView('${dependent.dependent?.assetClass}', ${dependent.dependent?.id})">
									${dependent.dependent?.assetName}
								</td>
								<g:if test="${dependent.dependent?.moveBundle!=assetEntity.moveBundle && dependent.status == 'Validated' }" >
									<td style="background-color: lightpink;position:relative;" onclick="EntityCrud.showAssetDetailView('${dependent.dependent?.assetClass}', ${dependent.dependent?.id})">
										<div style="padding: 5px 25px 5px 0px;">${dependent.dependent?.moveBundle}</div>
										<div class="text-center" style="position:absolute;right:5px;top:20%;"><img src="${assetPath('icons/error.png')}" width="17" height="18" alt="..." data-toggle="popover" data-trigger="hover"  data-content="The linked assets have conflicting bundles."/></div>
									</td>
								</g:if>
								<g:elseif test="${dependent.dependent?.moveBundle!=assetEntity.moveBundle }" >
									<td class="dep-${dependent.status}" style="position:relative;" onclick="EntityCrud.showAssetDetailView('${dependent.dependent?.assetClass}', ${dependent.dependent?.id})">
										<b>
											<div style="padding: 5px 25px 5px 0px;">${dependent.dependent?.moveBundle}</div>
											<div class="text-center" style="position:absolute;right:5px;top:20%;"><img src="${assetPath('icons/error.png')}" width="17" height="18" alt="..." data-toggle="popover" data-trigger="hover"  data-content="The linked assets have conflicting bundles."/></div>
										</b>
									</td>
								 </g:elseif>
								 <g:else>
									<td class="dep-${dependent.status}" onclick="EntityCrud.showAssetDetailView('${dependent.dependent?.assetClass}', ${dependent.dependent?.id})">
										${dependent.dependent?.moveBundle}
									</td>
								 </g:else>
								<td class="dep-${dependent.status}" nowrap="nowrap" onclick="EntityCrud.showAssetDependencyEditView({ id: '${assetEntity.id}' }, { id: '${dependent.dependent.id}' }, 'view');" >
									${dependent.type}&nbsp;
									<g:render template="../assetEntity/dependentComment" model= "[dependency:dependent, type:'dependent', forWhom:'show']"></g:render>
								</td>
								<td class="dep-${dependent.status}" onclick="EntityCrud.showAssetDependencyEditView({ id: '${assetEntity.id}' }, { id: '${dependent.dependent.id}' }, 'view');" >
									${dependent.status}
								</td>
							</tr>
						</g:each>
					</tbody>
				</table>
			</div>
		</td>