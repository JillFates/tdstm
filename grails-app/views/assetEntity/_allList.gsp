<%--
    This is used by the dependency Console
--%>
<%@page import="com.tds.asset.AssetComment"%>

<%@page import="com.tds.asset.AssetEntity" %>
<g:set var="assetClass" value="${(new AssetEntity()).assetClass}" />

<div class="tabs">
	<g:render template="depConsoleTabs" model="${[entity:entity, stats:stats, dependencyBundle:dependencyBundle]}"/>
	<div id="selectionId" class="tabControls">
		<input type="hidden" id="assetTypeId" name="assetType" value="${asset}" />
		<input type="hidden" id="assetTypesId" name="assetType" value="all" />
		<tds:hasPermission permission='MoveBundleEditView'>
			<input id="state" type="button"  class="submit" value="Assignment" onclick="changeMoveBundle($('#assetTypeId').val(),${assetList?.asset?.id},'${session.ASSIGN_BUNDLE}')"  />
		</tds:hasPermission>
	</div>
	<div class="tabInner">
		<div id="item1">
			<table id="tag" border="0" cellpadding="0" cellspacing="0" style="border-collapse: separate" class="table">
				<thead>
					<tr class="header">
						<th nowrap="nowrap"><input id="selectId" type="checkbox" onclick="selectAll()" title="Select All" />&nbsp;Actions</th>
						<th class="Arrowcursor ${sortBy == 'assetName' ? orderBy :''}" onclick="javascript:getListBySort('all','${dependencyBundle}','assetName')">Name</th>
						<th class="Arrowcursor ${sortBy == 'type' ? orderBy :''}" onclick="javascript:getListBySort('all','${dependencyBundle}','type')">Type</th>
						<th class="Arrowcursor ${sortBy == 'validation' ? orderBy :''}" onclick="javascript:getListBySort('all','${dependencyBundle}','validation')">Validation</th>
						<th class="Arrowcursor ${sortBy == 'moveBundle' ? orderBy :''}" onclick="javascript:getListBySort('all','${dependencyBundle}','moveBundle')">Bundle</th>
						<th class="Arrowcursor ${sortBy == 'planStatus' ? orderBy :''}" onclick="javascript:getListBySort('all','${dependencyBundle}','planStatus')">Plan Status</th>
						<th class="Arrowcursor ${sortBy == 'depToResolve' ? orderBy :''}" onclick="javascript:getListBySort('all','${dependencyBundle}','depToResolve')">TBD</th>
						<th class="Arrowcursor ${sortBy == 'depToConflict' ? orderBy :''}" onclick="javascript:getListBySort('all','${dependencyBundle}','depToConflict')">Conflict</th>
					</tr>

				</thead>
				<tbody class="tbody">
					<g:each in="${assetList}" var="asset" status="i">
						<tr id="tag_row1" style="cursor: pointer;" class="${(i % 2) == 0 ? 'odd' : 'even'}">
							<td nowrap="nowrap">
								<g:checkBox name="checkBox" id="checkId_${asset.asset.id}" ></g:checkBox>
							</td>
							<td>
								<span onclick="EntityCrud.showAssetDetailView('${asset.asset.assetClass}', ${asset.asset.id} )">${asset.asset.assetName}</span>
							</td>
							<td>
								<span onclick="EntityCrud.showAssetDetailView('${assetClass}', ${asset.asset.id} )">${asset.type}</span>
							</td>
							<td>
								<span onclick="EntityCrud.showAssetDetailView('${assetClass}', ${asset.asset.id} )">${asset.asset.validation}</span>
							</td>
							<td>
								<span onclick="EntityCrud.showAssetDetailView('${assetClass}', ${asset.asset.id} )">${asset.asset.moveBundle}</span>
							</td>
							<td>
								<span onclick="EntityCrud.showAssetDetailView('${assetClass}', ${asset.asset.id} )">${asset.asset.planStatus}</span>
							</td>
							<td>
								<span onclick="EntityCrud.showAssetDetailView('${assetClass}', ${asset.asset.id} )">${asset.asset?.depToResolve?:''}</span>
							</td>
							<td>
								<span onclick="EntityCrud.showAssetDetailView('${assetClass}', ${asset.asset.id} )">${asset.asset?.depToConflict?:''}</span>
							</td>
						</tr>
					</g:each>
				</tbody>

			</table>
			<input type="hidden" id="orderBy" value="${orderBy?:'asc'}">
			<input type="hidden" id="sortBy" value="${sortBy?:'asc'}">
		</div>
	</div>
	<script type="text/javascript">
		$('#tabTypeId').val('all');
		recompileDOM('item1');
	</script>
</div>

