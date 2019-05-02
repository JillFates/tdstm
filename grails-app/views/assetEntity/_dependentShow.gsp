<%@page defaultCodec="html" %>

<td valign="top">
			<div>
				<h1>Supports:</h1>
				<table style="min-width: 400px;" class="planning-application-table table-responsive">
					<thead>
						<tr>
							<th>Class</th>
							<th>Name</th>
							<th>Bundle h</th>
							<th>Type</th>
							<th>Status</th>
						</tr>
					</thead>
					<tbody>
						<g:each in="${supportAssets}" var="support" status="i">
							<tr class="${i%2? 'odd':'even' }" style="cursor: pointer;">
								<td class="dep-${support.status} subAssetLink"
                                    data-asset-class="${support?.asset?.assetClass}" data-asset-id="${support?.asset?.id}"
                                    onclick="EntityCrud.showAssetDetailView('${support?.asset?.assetClass}', ${support?.asset?.id})">
									${support?.asset?.assetType}
								</td>
								<td class="dep-${support.status} subAssetLink" style="min-width:80px"
                                    data-asset-class="${support?.asset?.assetClass}" data-asset-id="${support?.asset?.id}"
                                    onclick="EntityCrud.showAssetDetailView('${support?.asset?.assetClass}', ${support?.asset?.id})">
									${support?.asset?.assetName}
								</td>
								<g:if test="${support?.asset?.moveBundle!=assetEntity.moveBundle && support.status == 'Validated' }" >
									<td class="subAssetLink" style="background-color: lightpink;position:relative;"
                                        data-asset-class="${support?.asset?.assetClass}" data-asset-id="${support?.asset?.id}"
                                        onclick="EntityCrud.showAssetDetailView('${support?.asset?.assetClass}', ${support?.asset?.id})">
										<div data-asset-class="${support?.asset?.assetClass}" data-asset-id="${support?.asset?.id}"
                                             style="padding: 5px 25px 5px 0px;">${support?.asset?.moveBundle}</div>
										<div class="text-center" style="position:absolute;right:5px;top:20%;"><img src="/tdstm/assets/icons/error.png" width="17" height="17" alt="..." data-toggle="popover" data-trigger="hover"  data-content="The linked assets have conflicting bundles."/></div>
									</td>
								</g:if>
								<g:elseif test="${support?.asset?.moveBundle!=assetEntity.moveBundle }" >
									<td class="dep-${support.status} subAssetLink" style="position:relative;"
                                        data-asset-class="${support?.asset?.assetClass}" data-asset-id="${support?.asset?.id}"
                                        onclick="EntityCrud.showAssetDetailView('${support?.asset?.assetClass}', ${support?.asset?.id})">
										<b>
											<div data-asset-class="${support?.asset?.assetClass}" data-asset-id="${support?.asset?.id}" style="padding: 5px 25px 5px 0px;">${support?.asset?.moveBundle}</div>
											<div class="text-center" style="position:absolute;right:5px;top:20%;"><img src="/tdstm/assets/icons/error.png" width="17" height="17" alt="..." data-toggle="popover" data-trigger="hover" data-content="The linked assets have conflicting bundles."/></div>
										</b>
									</td>
								</g:elseif>
								<g:else>
									<td class="dep-${support.status} subAssetLink"
                                        data-asset-class="${support?.asset?.assetClass}" data-asset-id="${support?.asset?.id}"
                                        onclick="EntityCrud.showAssetDetailView('${support?.asset?.assetClass}', ${support?.asset?.id})">
										${support?.asset?.moveBundle}
								    </td>
								</g:else>
								<td class="dep-${support.status} assetDepLink" nowrap="nowrap"
                                    data-asset-id="${support?.asset?.id}" data-dep-id="${support.dependent.id}"
                                    onclick="EntityCrud.showAssetDependencyEditView({ id: '${support.asset.id}' }, { id: '${support.dependent.id}' }, 'view');" >
									${support.type} &nbsp;
									<g:render template="/assetEntity/dependentComment" model= "[dependency:support, type:'support', forWhom:'show']"></g:render>
								</td>
								<td class="dep-${support.status} assetDepLink"
                                    data-asset-id="${support?.asset?.id}" data-dep-id="${support.dependent.id}"
                                    onclick="EntityCrud.showAssetDependencyEditView({ id: '${support.asset.id}' }, { id: '${support.dependent.id}' }, 'view');">
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
								<td class="dep-${dependent.status} subAssetLink"
                                    data-asset-class="${dependent.dependent?.assetClass}" data-asset-id="${dependent.dependent?.id}"
                                    onclick="EntityCrud.showAssetDetailView('${dependent.dependent?.assetClass}', ${dependent.dependent?.id})">
									${dependent.dependent?.assetType}
								</td>
								<td class="dep-${dependent.status} subAssetLink" style="min-width:80px"
                                    data-asset-class="${dependent.dependent?.assetClass}" data-asset-id="${dependent.dependent?.id}"
                                    onclick="EntityCrud.showAssetDetailView('${dependent.dependent?.assetClass}', ${dependent.dependent?.id})">
									${dependent.dependent?.assetName}
								</td>
								<g:if test="${dependent.dependent?.moveBundle!=assetEntity.moveBundle && dependent.status == 'Validated' }" >
									<td style="background-color: lightpink;position:relative;"
                                        class="subAssetLink"
                                        data-asset-class="${dependent.dependent?.assetClass}" data-asset-id="${dependent.dependent?.id}"
                                        onclick="EntityCrud.showAssetDetailView('${dependent.dependent?.assetClass}', ${dependent.dependent?.id})">
										<div data-asset-class="${dependent.dependent?.assetClass}" data-asset-id="${dependent.dependent?.id}" style="padding: 5px 25px 5px 0px;">${dependent.dependent?.moveBundle}</div>
										<div class="text-center" style="position:absolute;right:5px;top:20%;"><img src="/tdstm/assets/icons/error.png" width="19" height="19" alt="..." data-toggle="popover" data-trigger="hover" data-content="The linked assets have conflicting bundles." /></div>
									</td>
								</g:if>
								<g:elseif test="${dependent.dependent?.moveBundle!=assetEntity.moveBundle }" >
									<td class="dep-${dependent.status} subAssetLink" style="position:relative;"
                                        data-asset-class="${dependent.dependent?.assetClass}" data-asset-id="${dependent.dependent?.id}"
                                        onclick="EntityCrud.showAssetDetailView('${dependent.dependent?.assetClass}', ${dependent.dependent?.id})">
										<b>
											<div data-asset-class="${dependent.dependent?.assetClass}" data-asset-id="${dependent.dependent?.id}" style="padding: 5px 25px 5px 0px;">${dependent.dependent?.moveBundle}</div>
											<div class="text-center" style="position:absolute;right:5px;top:20%;"><img src="/tdstm/assets/icons/error.png" width="19" height="19" alt="..." data-toggle="popover" data-trigger="hover" data-content="The linked assets have conflicting bundles." /></div>
										</b>
									</td>
								 </g:elseif>
								 <g:else>
									<td class="dep-${dependent.status} subAssetLink"
                                        data-asset-class="${dependent.dependent?.assetClass}" data-asset-id="${dependent.dependent?.id}"
                                        onclick="EntityCrud.showAssetDetailView('${dependent.dependent?.assetClass}', ${dependent.dependent?.id})">
										${dependent.dependent?.moveBundle}
									</td>
								 </g:else>
								<td class="dep-${dependent.status} assetDepLink" nowrap="nowrap"
                                    data-asset-id="${assetEntity.id}" data-dep-id="${dependent.dependent.id}"
                                    onclick="EntityCrud.showAssetDependencyEditView({ id: '${assetEntity.id}' }, { id: '${dependent.dependent.id}' }, 'view');" >
									${dependent.type}&nbsp;
									<g:render template="/assetEntity/dependentComment" model= "[dependency:dependent, type:'dependent', forWhom:'show']"></g:render>
								</td>
								<td class="dep-${dependent.status} assetDepLink"
                                    data-asset-id="${assetEntity.id}" data-dep-id="${dependent.dependent.id}"
                                    onclick="EntityCrud.showAssetDependencyEditView({ id: '${assetEntity.id}' }, { id: '${dependent.dependent.id}' }, 'view');" >
									${dependent.status}
								</td>
							</tr>
						</g:each>
					</tbody>
				</table>
			</div>
		</td>
