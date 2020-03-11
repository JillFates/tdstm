<%@page import="org.grails.orm.hibernate.cfg.GrailsHibernateUtil" defaultCodec="html" %>

<div id="supports" class="tds-table">
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
				<% supportAsset = org.grails.orm.hibernate.cfg.GrailsHibernateUtil.unwrapIfProxy(support?.asset) %>
				<tr>
					<td 
						data-asset-class="${supportAsset.assetClass}" data-asset-id="${supportAsset.id}"
						onclick="EntityCrud.showAssetDetailView('${supportAsset.assetClass}', ${supportAsset.id})">
						${supportAsset.assetType}
					</td>
					<td
						data-asset-class="${supportAsset.assetClass}" data-asset-id="${supportAsset.id}"
						onclick="EntityCrud.showAssetDetailView('${supportAsset.assetClass}', ${supportAsset.id})">
						${supportAsset.assetName}
					</td>
					<g:if test="${supportAsset.moveBundle!=assetEntity.moveBundle && support.status == 'Validated' }">
						<td style="position:relative;"
							data-asset-class="${supportAsset.assetClass}" data-asset-id="${supportAsset.id}"
							onclick="EntityCrud.showAssetDetailView('${supportAsset.assetClass}', ${supportAsset.id})">
							<div data-asset-class="${supportAsset.assetClass}" data-asset-id="${supportAsset.id}">${supportAsset.moveBundle}</div>
							<div class="text-center" style="position:absolute;right:5px;top:2px" title="The linked assets have conflicting bundles.">
								<span class="status status-Warning">
									<i class="fas fa-exclamation-triangle"></i>							
								</span>
							</div>
						</td>
					</g:if>
					<g:elseif test="${supportAsset.moveBundle!=assetEntity.moveBundle }">
						<td style="position:relative;"
							data-asset-class="${supportAsset.assetClass}" data-asset-id="${supportAsset.id}"
							onclick="EntityCrud.showAssetDetailView('${supportAsset.assetClass}', ${supportAsset.id})">
							<div data-asset-class="${supportAsset.assetClass}" data-asset-id="${supportAsset.id}">${supportAsset.moveBundle}</div>
							<div class="text-center" style="position:absolute;right:5px;top:2px" title="The linked assets have conflicting bundles.">
								<span class="status status-Warning">
									<i class="fas fa-exclamation-triangle"></i>							
								</span>
							</div>
						</td>
					</g:elseif>
					<g:else>
						<td 
							data-asset-class="${supportAsset.assetClass}" data-asset-id="${supportAsset.id}"
							onclick="EntityCrud.showAssetDetailView('${supportAsset.assetClass}', ${supportAsset.id})">
							${supportAsset.moveBundle}
						</td>
					</g:else>
					<td nowrap="nowrap"
						data-asset-id="${supportAsset.id}" data-dep-id="${support.dependent.id}"
						onclick="EntityCrud.showAssetDependencyEditView({ id: '${support.asset.id}' }, { id: '${support.dependent.id}' }, 'view');">
						${support.type} &nbsp;
						<g:render template="/assetEntity/dependentComment" model= "[dependency:support, type:'support', forWhom:'show']"></g:render>
					</td>
					<td 
						data-asset-id="${supportAsset.id}" data-dep-id="${support.dependent.id}"
						onclick="EntityCrud.showAssetDependencyEditView({ id: '${support.asset.id}' }, { id: '${support.dependent.id}' }, 'view');">
						<g:if test="${support.status == 'Validated'}">
							<span class="status status-Ready">
								<i class="fas fa-thumbs-up"></i>
							</span>
						</g:if>
						<g:if test="${support.status == 'Ready'}">
							<span class="status status-Ready">
								<i class="fas fa-thumbs-up"></i>
							</span>
						</g:if>
						<g:if test="${support.status == 'Started'}">
							<span class="status status-Started">
								<i class="fas fa-circle-notch"></i>
							</span>
						</g:if>
						<g:if test="${support.status == 'Hold'}">
							<span class="status status-Hold">
								<i class="fas fa-pause"></i>
							</span>
						</g:if>
						<g:if test="${support.status == 'Completed'}">
							<span class="status status-Completed">
								<i class="fas fa-check"></i>
							</span>
						</g:if>
						<g:if test="${support.status == 'Pending'}">
							<span class="status status-Pending">
								<i class="fas fa-minus"></i>
							</span>
						</g:if>
						${support.status}
					</td>
				</tr>
			</g:each>
		</tbody>
	</table>
</div>

<div id="depends" class="tds-table">
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
				<% depedentAsset = org.grails.orm.hibernate.cfg.GrailsHibernateUtil.unwrapIfProxy(dependent?.asset) %>
				<tr>
					<td 
						data-asset-class="${depedentAsset?.assetClass}" data-asset-id="${depedentAsset?.id}"
						onclick="EntityCrud.showAssetDetailView('${depedentAsset?.assetClass}', ${depedentAsset?.id})">
						${depedentAsset?.assetType}
					</td>
					<td
						data-asset-class="${depedentAsset?.assetClass}" data-asset-id="${depedentAsset?.id}"
						onclick="EntityCrud.showAssetDetailView('${depedentAsset?.assetClass}', ${depedentAsset?.id})">
						${depedentAsset?.assetName}
					</td>
					<g:if test="${depedentAsset?.moveBundle!=assetEntity.moveBundle && dependent.status == 'Validated' }">
						<td style="position:relative;"
							data-asset-class="${depedentAsset?.assetClass}" data-asset-id="${depedentAsset?.id}"
							onclick="EntityCrud.showAssetDetailView('${depedentAsset?.assetClass}', ${depedentAsset?.id})">
							<div data-asset-class="${depedentAsset?.assetClass}" data-asset-id="${depedentAsset?.id}">${depedentAsset?.moveBundle}</div>
							<div class="text-center" style="position:absolute;right:5px;top:2px;" title="The linked assets have conflicting bundles.">
								<span class="status status-Warning">
									<i class="fas fa-exclamation-triangle"></i>							
								</span>
							</div>
						</td>
					</g:if>
					<g:elseif test="${depedentAsset?.moveBundle!=assetEntity.moveBundle }">
						<td style="position:relative;"
							data-asset-class="${depedentAsset?.assetClass}" data-asset-id="${depedentAsset?.id}"
							onclick="EntityCrud.showAssetDetailView('${depedentAsset?.assetClass}', ${depedentAsset?.id})">
							<b>
								<div data-asset-class="${depedentAsset?.assetClass}" data-asset-id="${depedentAsset?.id}">${depedentAsset?.moveBundle}</div>
								<div class="text-center" style="position:absolute;right:5px;top:2px;" title="The linked assets have conflicting bundles.">
									<span class="status status-Warning">
										<i class="fas fa-exclamation-triangle"></i>							
									</span>
								</div>
							</b>
						</td>
					</g:elseif>
					<g:else>
						<td 
							data-asset-class="${depedentAsset?.assetClass}" data-asset-id="${depedentAsset?.id}"
							onclick="EntityCrud.showAssetDetailView('${depedentAsset?.assetClass}', ${depedentAsset?.id})">
							${depedentAsset?.moveBundle}
						</td>
					</g:else>
					<td nowrap="nowrap"
						data-asset-id="${assetEntity.id}" data-dep-id="${depedentAsset?.id}"
						onclick="EntityCrud.showAssetDependencyEditView({ id: '${assetEntity.id}' }, { id: '${depedentAsset?.id}' }, 'view');">
						${dependent.type}&nbsp;
						<g:render template="/assetEntity/dependentComment" model= "[dependency:dependent, type:'dependent', forWhom:'show']"></g:render>
					</td>
					<td 
						data-asset-id="${assetEntity.id}" data-dep-id="${depedentAsset?.id}"
						onclick="EntityCrud.showAssetDependencyEditView({ id: '${assetEntity.id}' }, { id: '${depedentAsset?.id}' }, 'view');">
						<g:if test="${dependent.status == 'Validated'}">
							<span class="status status-Ready">
								<i class="fas fa-thumbs-up"></i>
							</span>
						</g:if>
						<g:if test="${dependent.status == 'Ready'}">
							<span class="status status-Ready">
								<i class="fas fa-thumbs-up"></i>
							</span>
						</g:if>
						<g:if test="${dependent.status == 'Started'}">
							<span class="status status-Started">
								<i class="fas fa-circle-notch"></i>
							</span>
						</g:if>
						<g:if test="${dependent.status == 'Hold'}">
							<span class="status status-Hold">
								<i class="fas fa-pause"></i>
							</span>
						</g:if>
						<g:if test="${dependent.status == 'Completed'}">
							<span class="status status-Completed">
								<i class="fas fa-check"></i>
							</span>
						</g:if>
						<g:if test="${dependent.status == 'Pending'}">
							<span class="status status-Pending">
								<i class="fas fa-minus"></i>
							</span>
						</g:if>
						${dependent.status}
					</td>
				</tr>
			</g:each>
		</tbody>
	</table>
</div>