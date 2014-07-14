<%--
    This is used by the dependency Console
--%>
<%@page import="com.tds.asset.AssetComment"%>
<div class="tabs">
	<g:render template="depConsoleTabs" model="${[entity:entity, stats:stats, dependencyBundle:dependencyBundle]}"/>
	<div id ="selectionDBId">
		<input type="hidden" id="assetTypeId" name="assetType" value="${asset}" />
		<input type="hidden" id="assetTypesId" name="assetType" value="database" />
		<tds:hasPermission permission='MoveBundleEditView'>
			<input id="state" type="button"  class="submit" value="Assignment" onclick="changeMoveBundle($('#assetTypeId').val(),${databaseList.id},'${session.ASSIGN_BUNDLE}')"  />
		</tds:hasPermission>
	</div>
	<div class="tabInner">
		<div id="item1">
			<table id="tagApp" border="0" cellpadding="0" cellspacing="0"
				style="border-collapse: separate" class="table">
				<thead>
					<tr class="header">
						<th nowrap="nowrap"><input id="selectId" type="checkbox"   onclick="selectAll()" title="Select All" />&nbsp;Actions</th>
						<th class="Arrowcursor ${sortBy == 'assetName' ? orderBy :''}" onclick="javascript:getListBySort('database','${dependencyBundle}','assetName')">Name</th>
						<th class="Arrowcursor ${sortBy == 'dbFormat' ? orderBy :''}" onclick="javascript:getListBySort('database','${dependencyBundle}','dbFormat')">DB Format</th>
						<th class="Arrowcursor ${sortBy == 'validation' ? orderBy :''}" onclick="javascript:getListBySort('database','${dependencyBundle}','validation')">Validation</th>
						<th class="Arrowcursor ${sortBy == 'moveBundle' ? orderBy :''}" onclick="javascript:getListBySort('database','${dependencyBundle}','moveBundle')">Bundle</th>
						<th class="Arrowcursor ${sortBy == 'planStatus' ? orderBy :''}" onclick="javascript:getListBySort('database','${dependencyBundle}','planStatus')">Plan Status</th>
						<th class="Arrowcursor ${sortBy == 'depToResolve' ? orderBy :''}" onclick="javascript:getListBySort('database','${dependencyBundle}','depToResolve')">TBD</th>
						<th class="Arrowcursor ${sortBy == 'depToConflict' ? orderBy :''}" onclick="javascript:getListBySort('database','${dependencyBundle}','depToConflict')">Conflict</th>
					</tr>

				</thead>
				<tbody class="tbody">
					<g:each in="${databaseList}" var="database" status="i">
						<tr id="tag_row1" style="cursor: pointer;"
							class="${(i % 2) == 0 ? 'odd' : 'even'}">
							<td>
							<g:checkBox name="checkBox" id="checkId_${database.asset.id}" ></g:checkBox>
							<a href="javascript:editEntity('dependencyConsole','Database', ${database.asset.id})"><img
									src="/tdstm/icons/database_edit.png" border="0px" />
							</a> 
							<grid-buttons asset-id="${database.asset?.id}" asset-type="${database.asset?.assetType}" tasks="${database.tasksStatus}" comments="${database.commentsStatus}"></grid-buttons>
							</td>
							<td><span
								onclick="getEntityDetails('dependencyConsole','Database', ${database.asset.id} )">${database.asset.assetName}</span>
							</td>
							<td><span
								onclick="getEntityDetails('dependencyConsole','Database', ${database.asset.id} )">${database.asset.dbFormat}</span>
							</td>
							<td><span
								onclick="getEntityDetails('dependencyConsole','Database', ${database.asset.id} )">${database.asset.validation}</span>
							</td>
							<td><span
								onclick="getEntityDetails('dependencyConsole','Database', ${database.asset.id} )">${database.asset.moveBundle}</span>
							</td>
							<td><span
								onclick="getEntityDetails('dependencyConsole','Database', ${database.asset.id} )">${database.asset.planStatus}</span>

							</td>
							<td><span
								onclick="getEntityDetails('dependencyConsole','Database', ${database.asset.id} )">${database.asset?.depToResolve?:''}</span>
							</td>
							<td><span
								onclick="getEntityDetails('dependencyConsole','Database', ${database.asset.id} )">${database.asset?.depToConflict?:''}</span>
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
		$('#tabTypeId').val('database');
		recompileDOM('item1');
	</script>
</div>
