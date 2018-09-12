<%--
    This is used by the dependency Console
--%>
<%@page defaultCodec="html" %> 
<%@page import="com.tds.asset.AssetComment"%>
<%@page import="com.tds.asset.AssetEntity" %>
<%@page import="net.transitionmanager.security.Permission"%>

<g:set var="assetClass" value="${(new AssetEntity()).assetClass}" />

<div class="tabs">
	<g:render template="depConsoleTabs" model="${[entity:entity, stats:stats, dependencyBundle:dependencyBundle]}"/>
	<div id="selectionId" class="tabControls">
		<input type="hidden" id="assetTypeId" name="assetType" value="${asset}" />
		<input type="hidden" id="assetTypesId" name="assetType" value="server" />
		<tds:hasPermission permission="${Permission.AssetEdit}">
			<input id="state" type="button"  class="submit pointer" value="Assignment" onclick="changeMoveBundle($('#assetTypeId').val(),${assetList?.asset?.id},'${session.ASSIGN_BUNDLE}', ${session.SELECTED_TAG_IDS ?: [] as grails.converters.JSON})"  />
		</tds:hasPermission>
	</div>
	<div class="tabInner">
		<div id="item1">
			<table id="tag" border="0" cellpadding="0" cellspacing="0" style="border-collapse: separate" class="table">
				<thead>
					<tr class="header">
						<th nowrap="nowrap"><input id="selectId" type="checkbox" onclick="selectAll()" title="Select All" />&nbsp;Actions</th>
						<th class="Arrowcursor ${sortBy == 'assetName' ? orderBy :''}" onclick="javascript:getListBySort('server','${dependencyBundle}','assetName')">Name</th>
						<th class="Arrowcursor ${sortBy == 'model' ? orderBy :''}" onclick="javascript:getListBySort('server','${dependencyBundle}','model')">Model</th>
						<th class="Arrowcursor ${sortBy == 'sourceLocationName' ? orderBy :''}" onclick="javascript:getListBySort('server','${dependencyBundle}','sourceLocationName')">Loc/Room</th>
						<th class="Arrowcursor ${sortBy == 'sourceRackName' ? orderBy :''}" onclick="javascript:getListBySort('server','${dependencyBundle}','sourceRackName')">Rack</th>
						<th class="Arrowcursor ${sortBy == 'targetLocationName' ? orderBy :''}" onclick="javascript:getListBySort('server','${dependencyBundle}','targetLocationName')">Target Location</th>
						<th class="Arrowcursor ${sortBy == 'targetRackName' ? orderBy :''}" onclick="javascript:getListBySort('server','${dependencyBundle}','targetRackName')">Target rack</th>
						<th class="Arrowcursor ${sortBy == 'assetType' ? orderBy :''}" onclick="javascript:getListBySort('server','${dependencyBundle}','assetType')">Device Type</th>
						<th class="Arrowcursor ${sortBy == 'validation' ? orderBy :''}" onclick="javascript:getListBySort('server','${dependencyBundle}','validation')">Validation</th>
						<th class="Arrowcursor ${sortBy == 'moveBundle' ? orderBy :''}" onclick="javascript:getListBySort('server','${dependencyBundle}','moveBundle')">Bundle</th>
						<th class="Arrowcursor ${sortBy == 'depGroup' ? orderBy :''}" onclick="javascript:getListBySort('server','${dependencyBundle}','depGroup')">Dep. Group</th>
						<th class="Arrowcursor ${sortBy == 'planStatus' ? orderBy :''}" onclick="javascript:getListBySort('server','${dependencyBundle}','planStatus')">Plan Status</th>
						<th class="Arrowcursor ${sortBy == 'depToResolve' ? orderBy :''}" onclick="javascript:getListBySort('server','${dependencyBundle}','depToResolve')">TBD</th>
						<th class="Arrowcursor ${sortBy == 'depToConflict' ? orderBy :''}" onclick="javascript:getListBySort('server','${dependencyBundle}','depToConflict')">Conflict</th>
					</tr>

				</thead>
				<tbody class="tbody">
					<g:each in="${assetList}" var="asset" status="i">
						<tr id="tag_row1" style="cursor: pointer;" class="${(i % 2) == 0 ? 'odd' : 'even'}">
							<td nowrap="nowrap">
								<g:checkBox name="checkBox" id="checkId_${asset.asset.id}" ></g:checkBox>
								<g:if test="${haveAssetEditPerm}">
								<a href="javascript:EntityCrud.showAssetEditView('${assetClass}', ${asset.asset.id})" title="Edit Asset">
									<img src="${resource(dir:'icons',file:'database_edit.png')}" border="0px" />
								</a> 
								</g:if>
								<grid-buttons asset-id="${asset.asset?.id}" asset-type="${asset.asset?.assetType}" tasks="${asset.tasksStatus}" comments="${asset.commentsStatus}" can-edit-tasks="true" can-edit-comments="${haveAssetEditPerm}"></grid-buttons>
							</td>
							<td>
								<span onclick="EntityCrud.showAssetDetailView('${assetClass}', ${asset.asset.id} )">${asset.asset.assetName}</span>
							</td>
							<td>
								<span onclick="EntityCrud.showAssetDetailView('${assetClass}', ${asset.asset.id} )">${asset.asset.model}</span>
							</td>
							<td>
								<span onclick="EntityCrud.showAssetDetailView('${assetClass}', ${asset.asset.id} )">${asset.locRoom}</span>
							</td>
							<td>
								<span onclick="EntityCrud.showAssetDetailView('${assetClass}', ${asset.asset.id} )">${asset.asset.sourceRackName}</span>
							</td>
							<td>
								<span onclick="EntityCrud.showAssetDetailView('${assetClass}', ${asset.asset.id} )">${asset.asset.rackTarget?.location}</span>
							</td>
							<td>
								<span onclick="EntityCrud.showAssetDetailView('${assetClass}', ${asset.asset.id} )">${asset.asset.targetRackName}</span>
							</td>
							<td>
								<span onclick="EntityCrud.showAssetDetailView('${assetClass}', ${asset.asset.id} )">${asset.asset.assetType}</span>
							</td>
							<td>
								<span onclick="EntityCrud.showAssetDetailView('${assetClass}', ${asset.asset.id} )">${asset.asset.validation}</span>
							</td>
							<td>
								<span onclick="EntityCrud.showAssetDetailView('${assetClass}', ${asset.asset.id} )">${asset.asset.moveBundle}</span>
							</td>
							<td>
								<span onclick="EntityCrud.showAssetDetailView('${assetClass}', ${asset.asset.id} )">${asset.depGroup}</span>
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
		$('#tabTypeId').val('server');
		recompileDOM('item1');
	</script>
</div>

