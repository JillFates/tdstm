
<tr>
	<td >
	</td>
	<td>
		<g:each var="appList" in="${appList}" var="assetEntity" status="i">
			<table class="conflictApp planning-application-table">
				<thead>
					<tr>
						<th colspan="${columns}">
							<a href="javascript:void(0);" data-asset-class="${assetEntity.app.assetClass}" data-asset-id="${assetEntity.app.id}" class="inlineLink">${assetEntity.app.assetName}</a>
							<g:if test="${assetEntity.app.moveBundle.useForPlanning}"> (${assetEntity.app.moveBundle})</g:if> 
								- Supports ${assetEntity.supportsList.size()} , Depends on ${assetEntity.dependsOnList.size()} 
								<span style="color: red;">${assetEntity.header?' - '+assetEntity.header:''}</span>
						</th>
					</tr>
				</thead>
				<tbody class="conflictAppBody">

					<g:if test="${assetEntity.supportsList.size() > 0}">
						<tr>
							<td class="leftCells"></td>
							<td colspan="${columns-1}">Supports (${assetEntity.supportsIssueCount} Issues)</td>
						</tr>
						<tr class="headRow">
							<td class="leftCells"></td>
							<td class="leftCells"></td>
							<td>Type</td>
							<td>Class</td>
							<td>Name</td>
							<td>Frequency</td>
							<td>Bundle</td>
							<td>Status</td>
						</tr>
						<g:each in="${assetEntity.supportsList}" var="supports" status="j">
							<% supportAsset = org.grails.orm.hibernate.cfg.GrailsHibernateUtil.unwrapIfProxy(supports.asset) %>
							<tr class="${(j % 2) == 0 ? 'odd' : 'even'}">
								<td class="leftCells"></td>
								<td class="leftCells"></td>
								<td>
									${supports.type}
								</td>
								<td>
									${supportAsset.assetType}
								</td>
								<td>
									${supportAsset.assetName}
								</td>
								<td>
									${supports.dataFlowFreq}
								</td>
								<td>
									<g:if test="${supportAsset.moveBundle != assetEntity.app.moveBundle}"><b style="color:red;">${supportAsset.moveBundle}</b></g:if>
									<g:else>${supportAsset.moveBundle}</g:else>
								</td>
								<td>
									<g:if test="${supports.status in ['Questioned','Unknown']}"><b style="color:red;">${supports.status}</b></g:if>
									<g:else>${supports.status}</g:else>
								</td>
							</tr>
						</g:each>
					</g:if>

					<g:if test="${assetEntity.dependsOnList.size() > 0}">
						<tr>
							<td class="leftCells"></td>
							<td colspan="${columns-1}">Dependencies (${assetEntity.dependsOnIssueCount} Issues)</td>
						</tr>
						<tr class="headRow">
							<td class="leftCells"></td>
							<td class="leftCells"></td>
							<td>Type</td>
							<td>Class</td>
							<td>Name</td>
							<td>Frequency</td>
							<td>Bundle</td>
							<td>Status</td>
						</tr>
						<g:each in="${assetEntity.dependsOnList}" var="depOn" status="j">
							<% dependentAsset = org.grails.orm.hibernate.cfg.GrailsHibernateUtil.unwrapIfProxy(depOn.dependent) %>
							<tr class="${(j % 2) == 0 ? 'odd' : 'even'}">
								<td class="leftCells"></td>
								<td class="leftCells"></td>
								<td>
									${depOn.type}
								</td>
								<td>
									${dependentAsset.assetType}
								</td>
								<td>
									${dependentAssetassetName}
								</td>
								<td>
									${depOn.dataFlowFreq}
								</td>
								<td>
									<g:if test="${dependentAsset.moveBundle != assetEntity.app.moveBundle}"><b style="color:red;">${dependentAsset.moveBundle}</b></g:if>
									<g:else>${dependentAsset.moveBundle}</g:else>
								</td>
								<td>
									<g:if test="${depOn.status in ['Questioned','Unknown']}"><b style="color:red;">${depOn.status}</b></g:if>
									<g:else>${depOn.status}</g:else>
								</td>
							</tr>
						</g:each>
					</g:if>

				</tbody>
			</table>
		</g:each>
	</td>
</tr>

