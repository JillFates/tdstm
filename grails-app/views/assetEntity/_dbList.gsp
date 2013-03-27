<%@page import="com.tds.asset.AssetComment"%>
<div class="tabs">
	<ul>
		
		<li id="appli" ><a href="javascript:getList('Apps',${dependencyBundle})">Apps(${appDependentListSize})</a>
		</li>
		<li id="serverli" ><a
			href="javascript:getList('server',${dependencyBundle})"><span>Servers(${assetEntityListSize})</span> </a></li>
		<li id="dbli" class="active" ><a href="#item3"><a href="javascript:getList('database',${dependencyBundle})">DB(${dbDependentListSize})</a></li>
		<li id="fileli" ><a href="javascript:getList('files',${dependencyBundle})">Storage(${filesDependentListSize})</a></li>
		<li id="graphli"><a href="javascript:getList('graph',${dependencyBundle})">Map</a></li>
	</ul>
	 <div id ="selectionDBId">
	    <input type="hidden" id="assetTypeId" name="assetType" value="${asset}" />
	    <input type="hidden" id="assetTypesId" name="assetType" value="database" />
	    <tds:hasPermission permission='MoveBundleEditView'>
		<input id="state" type="button"  class="submit" value="Assignment" onclick="changeMoveBundle($('#assetTypeId').val(),${databaseList.id})"  />
		</tds:hasPermission>
	 </div>
	<div class="tabInner">
		<div id="item1">
			<table id="tagApp" border="0" cellpadding="0" cellspacing="0"
				style="border-collapse: separate" class="table">
				<thead>
					<tr class="header">
						<th nowrap="nowrap"><input id="selectId" type="checkbox"   onclick="selectAll()" title="Select All" />&nbsp;Actions</th>
						<th>Name</th>
						<th>DB Format</th>
						<th>Bundle</th>
						<th>Plan Status</th>
						<th>Dep Up</th>
						<th>Dep Down</th>
					</tr>

				</thead>
				<tbody class="tbody">
					<g:each in="${databaseList}" var="database" status="i">
						<tr id="tag_row1" style="cursor: pointer;"
							class="${(i % 2) == 0 ? 'odd' : 'even'}">
							<td>
							<g:checkBox name="checkBox" id="checkId_${database.id}" ></g:checkBox>
							<a href="javascript:editEntity('planningConsole','Database', ${database.id})"><img
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
						     <a href="javascript:createNewAssetComment(${database.id},'${database.assetName}');">
							    <img src="${resource(dir:'i',file:'db_table_light.png')}" border="0px" onclick="createNewAssetComment(${database.id},'${database.assetName}');"/>
							 </a>
							    
						    </g:else> </span>
							</td>
							<td><span
								onclick="getEntityDetails('planningConsole','Database', ${database.id} )">${database.assetName}</span>
							</td>
							<td><span
								onclick="getEntityDetails('planningConsole','Database', ${database.id} )">${database.dbFormat}</span>
							</td>

							<td><span
								onclick="getEntityDetails('planningConsole','Database', ${database.id} )">${database.moveBundle}</span>
							</td>
							<td><span
								onclick="getEntityDetails('planningConsole','Database', ${database.id} )">${database.planStatus}</span>

							</td>
							<td><span
								onclick="getEntityDetails('planningConsole','Database', ${database.id} )">${ com.tds.asset.AssetDependency.countByDependentAndStatusNotEqual(com.tds.asset.AssetEntity.get(database.id),'Validated') }</span>
							</td>
							<td><span
								onclick="getEntityDetails('planningConsole','Database', ${database.id} )">${ com.tds.asset.AssetDependency.countByAssetAndStatusNotEqual(com.tds.asset.AssetEntity.get(database.id),'Validated') }</span>
							</td>
						</tr>
					</g:each>
				</tbody>
			</table>
		</div>
	</div>
	<script type="text/javascript">
		$('#tabTypeId').val('database')
	</script>
</div>
