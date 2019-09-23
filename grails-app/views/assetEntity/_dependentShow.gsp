<%@page import="org.grails.orm.hibernate.cfg.GrailsHibernateUtil" defaultCodec="html" %>

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
							<% supportAsset = org.grails.orm.hibernate.cfg.GrailsHibernateUtil.unwrapIfProxy(support?.asset) %>
							<tr class="${i%2? 'odd':'even' }" style="cursor: pointer;">
								<td class="dep-${support.status} subAssetLink"
                                    data-asset-class="${supportAsset.assetClass}" data-asset-id="${supportAsset.id}"
                                    onclick="EntityCrud.showAssetDetailView('${supportAsset.assetClass}', ${supportAsset.id})">
									${supportAsset.assetType}
								</td>
								<td class="dep-${support.status} subAssetLink" style="min-width:80px"
                                    data-asset-class="${supportAsset.assetClass}" data-asset-id="${supportAsset.id}"
                                    onclick="EntityCrud.showAssetDetailView('${supportAsset.assetClass}', ${supportAsset.id})">
									${supportAsset.assetName}
								</td>
								<g:if test="${supportAsset.moveBundle!=assetEntity.moveBundle && support.status == 'Validated' }" >
									<td class="subAssetLink" style="background-color: lightpink;position:relative;"
                                        data-asset-class="${supportAsset.assetClass}" data-asset-id="${supportAsset.id}"
                                        onclick="EntityCrud.showAssetDetailView('${supportAsset.assetClass}', ${supportAsset.id})">
										<div data-asset-class="${supportAsset.assetClass}" data-asset-id="${supportAsset.id}"
                                             style="padding: 5px 25px 5px 0px;">${supportAsset.moveBundle}</div>
										<div class="text-center" style="position:absolute;right:5px;top:20%;"><img src="/tdstm/assets/icons/error.png" width="17" height="17" alt="..." data-toggle="popover" data-trigger="hover"  data-content="The linked assets have conflicting bundles."/></div>
									</td>
								</g:if>
								<g:elseif test="${supportAsset.moveBundle!=assetEntity.moveBundle }" >
									<td class="dep-${support.status} subAssetLink" style="position:relative;"
                                        data-asset-class="${supportAsset.assetClass}" data-asset-id="${supportAsset.id}"
                                        onclick="EntityCrud.showAssetDetailView('${supportAsset.assetClass}', ${supportAsset.id})">
										<b>
											<div data-asset-class="${supportAsset.assetClass}" data-asset-id="${supportAsset.id}" style="padding: 5px 25px 5px 0px;">${supportAsset.moveBundle}</div>
											<div class="text-center" style="position:absolute;right:5px;top:20%;"><img src="/tdstm/assets/icons/error.png" width="17" height="17" alt="..." data-toggle="popover" data-trigger="hover" data-content="The linked assets have conflicting bundles."/></div>
										</b>
									</td>
								</g:elseif>
								<g:else>
									<td class="dep-${support.status} subAssetLink"
                                        data-asset-class="${supportAsset.assetClass}" data-asset-id="${supportAsset.id}"
                                        onclick="EntityCrud.showAssetDetailView('${supportAsset.assetClass}', ${supportAsset.id})">
										${supportAsset.moveBundle}
								    </td>
								</g:else>
								<td class="dep-${support.status} assetDepLink" nowrap="nowrap"
                                    data-asset-id="${supportAsset.id}" data-dep-id="${support.dependent.id}"
                                    onclick="EntityCrud.showAssetDependencyEditView({ id: '${support.asset.id}' }, { id: '${support.dependent.id}' }, 'view');" >
									${support.type} &nbsp;
									<g:render template="/assetEntity/dependentComment" model= "[dependency:support, type:'support', forWhom:'show']"></g:render>
								</td>
								<td class="dep-${support.status} assetDepLink"
                                    data-asset-id="${supportAsset.id}" data-dep-id="${support.dependent.id}"
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
							<% depedentAsset = org.grails.orm.hibernate.cfg.GrailsHibernateUtil.unwrapIfProxy(depedentAsset) %>
							<tr class="${i%2? 'odd':'even' }" style="cursor: pointer;">
								<td class="dep-${dependent.status} subAssetLink"
                                    data-asset-class="${depedentAsset?.assetClass}" data-asset-id="${depedentAsset?.id}"
                                    onclick="EntityCrud.showAssetDetailView('${depedentAsset?.assetClass}', ${depedentAsset?.id})">
									${depedentAsset?.assetType}
								</td>
								<td class="dep-${dependent.status} subAssetLink" style="min-width:80px"
                                    data-asset-class="${depedentAsset?.assetClass}" data-asset-id="${depedentAsset?.id}"
                                    onclick="EntityCrud.showAssetDetailView('${depedentAsset?.assetClass}', ${depedentAsset?.id})">
									${depedentAsset?.assetName}
								</td>
								<g:if test="${depedentAsset?.moveBundle!=assetEntity.moveBundle && dependent.status == 'Validated' }" >
									<td style="background-color: lightpink;position:relative;"
                                        class="subAssetLink"
                                        data-asset-class="${depedentAsset?.assetClass}" data-asset-id="${depedentAsset?.id}"
                                        onclick="EntityCrud.showAssetDetailView('${depedentAsset?.assetClass}', ${depedentAsset?.id})">
										<div data-asset-class="${depedentAsset?.assetClass}" data-asset-id="${depedentAsset?.id}" style="padding: 5px 25px 5px 0px;">${depedentAsset?.moveBundle}</div>
										<div class="text-center" style="position:absolute;right:5px;top:20%;"><img src="/tdstm/assets/icons/error.png" width="19" height="19" alt="..." data-toggle="popover" data-trigger="hover" data-content="The linked assets have conflicting bundles." /></div>
									</td>
								</g:if>
								<g:elseif test="${depedentAsset?.moveBundle!=assetEntity.moveBundle }" >
									<td class="dep-${dependent.status} subAssetLink" style="position:relative;"
                                        data-asset-class="${depedentAsset?.assetClass}" data-asset-id="${depedentAsset?.id}"
                                        onclick="EntityCrud.showAssetDetailView('${depedentAsset?.assetClass}', ${depedentAsset?.id})">
										<b>
											<div data-asset-class="${depedentAsset?.assetClass}" data-asset-id="${depedentAsset?.id}" style="padding: 5px 25px 5px 0px;">${depedentAsset?.moveBundle}</div>
											<div class="text-center" style="position:absolute;right:5px;top:20%;"><img src="/tdstm/assets/icons/error.png" width="19" height="19" alt="..." data-toggle="popover" data-trigger="hover" data-content="The linked assets have conflicting bundles." /></div>
										</b>
									</td>
								 </g:elseif>
								 <g:else>
									<td class="dep-${dependent.status} subAssetLink"
                                        data-asset-class="${depedentAsset?.assetClass}" data-asset-id="${depedentAsset?.id}"
                                        onclick="EntityCrud.showAssetDetailView('${depedentAsset?.assetClass}', ${depedentAsset?.id})">
										${depedentAsset?.moveBundle}
									</td>
								 </g:else>
								<td class="dep-${dependent.status} assetDepLink" nowrap="nowrap"
                                    data-asset-id="${assetEntity.id}" data-dep-id="${depedentAsset?.id}"
                                    onclick="EntityCrud.showAssetDependencyEditView({ id: '${assetEntity.id}' }, { id: '${depedentAsset?.id}' }, 'view');" >
									${dependent.type}&nbsp;
									<g:render template="/assetEntity/dependentComment" model= "[dependency:dependent, type:'dependent', forWhom:'show']"></g:render>
								</td>
								<td class="dep-${dependent.status} assetDepLink"
                                    data-asset-id="${assetEntity.id}" data-dep-id="${depedentAsset?.id}"
                                    onclick="EntityCrud.showAssetDependencyEditView({ id: '${assetEntity.id}' }, { id: '${depedentAsset?.id}' }, 'view');" >
									${dependent.status}
								</td>
							</tr>
						</g:each>
					</tbody>
				</table>
			</div>
		</td>
