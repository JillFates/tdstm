<%--
    This is used by the dependency Console
--%>
<%@page import="com.tds.asset.AssetComment" %>
<%@page import="com.tds.asset.Application" %>
<g:set var="assetClass" value="${(new Application()).assetClass}" />

<div class="tabs">

	<g:render template="depConsoleTabs" model="${[entity:entity, stats:stats, dependencyBundle:dependencyBundle]}"/>
	<div id ="selectionAppId">
		<input type="hidden" id="assetTypeId" name="assetType" value="${asset}" />
		<input type="hidden" id="assetTypesId" name="assetType" value="apps" />
		<tds:hasPermission permission='MoveBundleEditView'>
			<input id="state" type="button"  class="submit" value="Assignment" onclick="changeMoveBundle($('#assetTypeId').val(),${appList?.asset?.id},'${session.ASSIGN_BUNDLE}')"  />
		</tds:hasPermission>
	</div>
	<div class="tabInner">
		<div id="item1">
			<table id="tagApp" border="0" cellpadding="0" cellspacing="0"
				style="border-collapse: separate" class="table">
				<thead>
					<tr class="header">
						<th nowrap="nowrap"><input id="selectId" type="checkbox"  onclick="selectAll()" title="Select All" />&nbsp;Actions</th>
						<th class="Arrowcursor ${sortBy == 'assetName' ? orderBy :''}" onclick="javascript:getListBySort('apps','${dependencyBundle}','assetName')">Name</th>
						<th class="Arrowcursor ${sortBy == 'sme' ? orderBy :''}" onclick="javascript:getListBySort('apps','${dependencyBundle}','sme')">App Sme</th>
						<th class="Arrowcursor ${sortBy == 'sme2' ? orderBy :''}" onclick="javascript:getListBySort('apps','${dependencyBundle}','sme2')">Sme2</th>
						<th class="Arrowcursor ${sortBy == 'validation' ? orderBy :''}" onclick="javascript:getListBySort('apps','${dependencyBundle}','validation')">Validation</th>
						<th class="Arrowcursor ${sortBy == 'moveBundle' ? orderBy :''}" onclick="javascript:getListBySort('apps','${dependencyBundle}','moveBundle')">Bundle</th>
						<th class="Arrowcursor ${sortBy == 'planStatus' ? orderBy :''}" onclick="javascript:getListBySort('apps','${dependencyBundle}','planStatus')">Plan Status</th>
						<th class="Arrowcursor ${sortBy == 'depToResolve' ? orderBy :''}" onclick="javascript:getListBySort('apps','${dependencyBundle}','depToResolve')">TBD</th>
						<th class="Arrowcursor ${sortBy == 'depToConflict' ? orderBy :''}" onclick="javascript:getListBySort('apps','${dependencyBundle}','depToConflict')">Conflict</th>
					</tr>

				</thead>
				<tbody class="tbody">
					<g:each in="${appList}" var="app" status="i">
						<tr id="tag_row1" style="cursor: pointer;" class="${(i % 2) == 0 ? 'odd' : 'even'}">
							<td>
								<g:checkBox name="checkBox" id="checkId_${app.asset?.id}"></g:checkBox>
								<a href="javascript:EntityCrud.showAssetEditView('${assetClass}', ${app.asset?.id})">
									<img src="/tdstm/icons/database_edit.png" border="0px" />
								</a>
								<grid-buttons asset-id="${app.asset?.id}" asset-type="${app.asset?.assetType}" tasks="${app.tasksStatus}" comments="${app.commentsStatus}"></grid-buttons>
							</td>
							<td>
								<span onclick="EntityCrud.showAssetDetailView('${assetClass}', ${app.asset?.id});">${app.asset?.assetName}</span>
							</td> 
							<td>
								<span onclick="EntityCrud.showAssetDetailView('${assetClass}', ${app.asset?.id});">${app.asset?.sme}</span>
							</td>
							<td>
								<span onclick="EntityCrud.showAssetDetailView('${assetClass}', ${app.asset?.id});">${app.asset?.sme2}</span>
							</td>
							<td>
								<span onclick="EntityCrud.showAssetDetailView('${assetClass}', ${app.asset?.id});">${app.asset?.validation}</span>
							</td>
							<td>
								<span onclick="EntityCrud.showAssetDetailView('${assetClass}', ${app.asset?.id});">${app.asset?.moveBundle}</span>
							</td>
							<td>
								<span onclick="EntityCrud.showAssetDetailView('${assetClass}', ${app.asset?.id});">${app.asset?.planStatus}</span>
							</td>
							<td>
								<span onclick="EntityCrud.showAssetDetailView('${assetClass}', ${app.asset?.id});">${app.asset?.depToResolve?:''}</span>
							</td>
							<td>
								<span onclick="EntityCrud.showAssetDetailView('${assetClass}', ${app.asset?.id});">${app.asset?.depToConflict?:''}</span>
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
		$('#tabTypeId').val('apps');
		recompileDOM('item1');
	</script>
</div>
