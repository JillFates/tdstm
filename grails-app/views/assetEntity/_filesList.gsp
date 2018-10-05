<%--
    This is used by the dependency Console
--%>
<%@page defaultCodec="html" %>
<%@page import="com.tds.asset.AssetComment"%>
<%@page import="com.tds.asset.Files" %>
<%@page import="net.transitionmanager.security.Permission"%>

<g:set var="assetClass" value="${(new Files()).assetClass}" />

<div class="tabs">
	<g:render template="depConsoleTabs" model="${[entity:entity, stats:stats, dependencyBundle:dependencyBundle]}"/>
	<div id="selectionFilesId" class="tabControls">
		<input type="hidden" id="assetTypeId" name="assetType" value="${asset}" />
		<input type="hidden" id="assetTypesId" name="assetType" value="files" />
		<tds:hasPermission permission="${Permission.AssetEdit}">
			<input id="state" type="button"  class="submit pointer" value="Assignment" onclick="changeMoveBundle($('#assetTypeId').val(),${filesList?.asset?.id},'${session.ASSIGN_BUNDLE}', ${session.SELECTED_TAG_IDS ?: [] as grails.converters.JSON})"  />
		</tds:hasPermission>
	</div>
	<div class="tabInner">
		<div id="item1">
			<table id="tagApp" border="0" cellpadding="0" cellspacing="0" style="border-collapse: separate" class="table">
				<thead>
					<tr class="header">
						<th nowrap="nowrap"><input id="selectId" type="checkbox"   onclick="selectAll()" title="Select All" />&nbsp;Actions</th>
						<th class="Arrowcursor ${sortBy == 'assetName' ? orderBy :''}" onclick="javascript:getListBySort('files','${dependencyBundle}','assetName')">Name</th>
						<th class="Arrowcursor ${sortBy == 'fileFormat' ? orderBy :''}" onclick="javascript:getListBySort('files','${dependencyBundle}','fileFormat')">Storage Format</th>
						<th class="Arrowcursor ${sortBy == 'type' ? orderBy :''}" onclick="javascript:getListBySort('files','${dependencyBundle}','type')">Asset Type</th>
						<th class="Arrowcursor ${sortBy == 'validation' ? orderBy :''}" onclick="javascript:getListBySort('files','${dependencyBundle}','validation')">Validation</th>
						<th class="Arrowcursor ${sortBy == 'moveBundle' ? orderBy :''}" onclick="javascript:getListBySort('files','${dependencyBundle}','moveBundle')">Bundle</th>
						<th class="Arrowcursor ${sortBy == 'depGroup' ? orderBy :''}" onclick="javascript:getListBySort('files','${dependencyBundle}','depGroup')">Dep Group</th>
						<th class="Arrowcursor ${sortBy == 'planStatus' ? orderBy :''}" onclick="javascript:getListBySort('files','${dependencyBundle}','planStatus')">Plan Status</th>
						<th class="Arrowcursor ${sortBy == 'depToResolve' ? orderBy :''}" onclick="javascript:getListBySort('files','${dependencyBundle}','depToResolve')">TBD</th>
						<th class="Arrowcursor ${sortBy == 'depToConflict' ? orderBy :''}" onclick="javascript:getListBySort('files','${dependencyBundle}','depToConflict')">Conflict</th>
					</tr>
				</thead>
				<tbody class="tbody">
					<g:each in="${filesList}" var="files" status="i">
						<tr id="tag_row1" style="cursor: pointer;" class="${(i % 2) == 0 ? 'odd' : 'even'}">
							<td>
								<g:checkBox name="checkBox" id="checkId_${files.asset?.id}" ></g:checkBox>
								<g:if test="${haveAssetEditPerm}">
								<a href="javascript:EntityCrud.showAssetEditView('${files.asset.assetClass}', ${files.asset.id})" title="Edit Asset">
									<asset:image src="icons/database_edit.png" border="0px" />
								</a>
								</g:if>
								<grid-buttons asset-id="${files.asset?.id}" asset-type="${files.asset?.assetType}" tasks="${files.tasksStatus}" comments="${files.commentsStatus}" can-edit-tasks="true" can-edit-comments="${haveAssetEditPerm}"></grid-buttons>
							</td>
							<td>
								<span onclick="EntityCrud.showAssetDetailView('${files.asset.assetClass}', ${files.asset?.id} )">${files.asset.assetName}</span>
							</td>
							<td>
								<span onclick="EntityCrud.showAssetDetailView('${files.asset.assetClass}', ${files.asset?.id} )">${files.asset.fileFormat}</span>
							</td>
							<td>
								<span onclick="EntityCrud.showAssetDetailView('${files.asset.assetClass}', ${files.asset?.id} )">${files.asset.type}</span>
							</td>
							<td>
								<span onclick="EntityCrud.showAssetDetailView('${files.asset.assetClass}', ${files.asset?.id} )">${files.asset.validation}</span>
							</td>
   							<td>
								<span onclick="EntityCrud.showAssetDetailView('${files.asset.assetClass}', ${files.asset?.id} )">${files.asset.moveBundle}</span>
							</td>
							<td>
								<span onclick="EntityCrud.showAssetDetailView('${files.asset.assetClass}', ${files.asset?.id} )">${files.asset.depGroup}</span>
							</td>
							<td>
								<span onclick="EntityCrud.showAssetDetailView('${files.asset.assetClass}', ${files.asset?.id} )">${files.asset.planStatus}</span>
							</td>
							<td>
								<span onclick="EntityCrud.showAssetDetailView('${files.asset.assetClass}', ${files.asset?.id} )">${files.asset?.depToResolve?:''}</span>
							</td>
							<td>
								<span onclick="EntityCrud.showAssetDetailView('${files.asset.assetClass}', ${files.asset?.id} )">${files.asset?.depToConflict?:''}</span>
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
		$('#tabTypeId').val('files');
		recompileDOM('item1');
	</script>
</div>
