<%--
    This is used by the dependency Console
--%>
<%@page import="com.tds.asset.AssetComment"%>
<div class="tabs">
	<g:render template="depConsoleTabs" model="${[entity:entity, stats:stats, dependencyBundle:dependencyBundle]}"/>
	<div id ="selectionFilesId">
		<input type="hidden" id="assetTypeId" name="assetType" value="${asset}" />
		<input type="hidden" id="assetTypesId" name="assetType" value="files" />
		<tds:hasPermission permission='MoveBundleEditView'>
			<input id="state" type="button"  class="submit" value="Assignment" onclick="changeMoveBundle($('#assetTypeId').val(),${filesList.id},'${session.ASSIGN_BUNDLE}')"  />
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
						<th class="Arrowcursor ${sortBy == 'validation' ? orderBy :''}" onclick="javascript:getListBySort('files','${dependencyBundle}','validation')">Validation</th>
						<th class="Arrowcursor ${sortBy == 'moveBundle' ? orderBy :''}" onclick="javascript:getListBySort('files','${dependencyBundle}','moveBundle')">Bundle</th>
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
							<a href="javascript:editEntity('dependencyConsole','${files.asset.storageType}', ${files.asset.id})"><img
									src="/tdstm/icons/database_edit.png" border="0px" />
							</a>
							<grid-buttons asset-id="${files.asset?.id}" asset-type="${files.asset?.assetType}" tasks="${files.tasksStatus}" comments="${files.commentsStatus}"></grid-buttons>
							</td>
							<td><span
								onclick="getEntityDetails('dependencyConsole','${files.asset.storageType}', ${files.asset?.id} )">${files.asset.assetName}</span>
							</td>
							<td><span
								onclick="getEntityDetails('dependencyConsole','${files.asset.storageType}', ${files.asset?.id} )">${files.asset.fileFormat}</span>
							</td>
							<td><span
								onclick="getEntityDetails('dependencyConsole','${files.asset.storageType}', ${files.asset?.id} )">${files.asset.validation}</span>
							</td>
   							<td><span
								onclick="getEntityDetails('dependencyConsole','${files.asset.storageType}', ${files.asset?.id} )">${files.asset.moveBundle}</span>
							</td>
							<td><span
								onclick="getEntityDetails('dependencyConsole','${files.asset.storageType}', ${files.asset?.id} )">${files.asset.planStatus}</span>
							</td>
							<td><span
								onclick="getEntityDetails('dependencyConsole','${files.asset.storageType}', ${files.asset?.id} )">${files.asset?.depToResolve?:''}</span>
							</td>
							<td><span
								onclick="getEntityDetails('dependencyConsole','${files.asset.storageType}', ${files.asset?.id} )">${files.asset?.depToConflict?:''}</span>
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
