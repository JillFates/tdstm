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
							<g:checkBox name="checkBox" id="checkId_${database.id}" ></g:checkBox>
							<a href="javascript:editEntity('dependencyConsole','Database', ${database.id})"><img
									src="/tdstm/images/skin/database_edit.png" border="0px" />
							</a> <span id="icon_15651"> <g:if test="${AssetComment.find('from AssetComment where assetEntity = '+database.id+' and commentType = ? and isResolved = ?',['issue',0])}">
							   <g:remoteLink controller="assetEntity" action="listComments" id="${database.id}" before="setAssetId('${database.id}');" onComplete="listCommentsDialog(e,'never');">
							      <img id="comment_${database.id}" src="${resource(dir:'i',file:'db_table_red.png')}" border="0px" />
							   </g:remoteLink>
				             </g:if>
						     <g:elseif test="${AssetComment.find('from AssetComment where assetEntity = '+database.id)}">
						     <g:remoteLink controller="assetEntity" action="listComments" id="${database.id}" before="setAssetId('${database.id}');" onComplete="listCommentsDialog(e,'never');">
							      <img id="comment_${database.id}" src="${resource(dir:'i',file:'db_table_bold.png')}" border="0px" />
							 </g:remoteLink>
						     </g:elseif>
						     <g:else>
						     <a href="javascript:createNewAssetComment(${database.id},'${database.assetName}','${database.assetType}');">
							    <img src="${resource(dir:'i',file:'db_table_light.png')}" border="0px" onclick="createNewAssetComment(${database.id},'${database.assetName}','${database.assetType}');"/>
							 </a>
							    
						    </g:else> </span>
							</td>
							<td><span
								onclick="getEntityDetails('dependencyConsole','Database', ${database.id} )">${database.assetName}</span>
							</td>
							<td><span
								onclick="getEntityDetails('dependencyConsole','Database', ${database.id} )">${database.dbFormat}</span>
							</td>
							<td><span
								onclick="getEntityDetails('dependencyConsole','Database', ${database.id} )">${database.validation}</span>
							</td>
							<td><span
								onclick="getEntityDetails('dependencyConsole','Database', ${database.id} )">${database.moveBundle}</span>
							</td>
							<td><span
								onclick="getEntityDetails('dependencyConsole','Database', ${database.id} )">${database.planStatus}</span>

							</td>
							<td><span
								onclick="getEntityDetails('dependencyConsole','Database', ${database.id} )">${database?.depToResolve?:''}</span>
							</td>
							<td><span
								onclick="getEntityDetails('dependencyConsole','Database', ${database.id} )">${database?.depToConflict?:''}</span>
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
		$('#tabTypeId').val('database')
	</script>
</div>
