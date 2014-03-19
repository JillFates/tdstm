<%--
    This is used by the dependency Console
--%>
<%@page import="com.tds.asset.AssetComment"%>
<div class="tabs">
	<g:render template="depConsoleTabs" model="${[entity:entity, stats:stats, dependencyBundle:dependencyBundle]}"/>
    <div id ="selectionId" >
		<input type="hidden" id="assetTypeId" name="assetType" value="${asset}" />
		<input type="hidden" id="assetTypesId" name="assetType" value="server" />
		<tds:hasPermission permission='MoveBundleEditView'>
			<input id="state" type="button"  class="submit" value="Assignment" onclick="changeMoveBundle($('#assetTypeId').val(),${assetList.id},'${session.ASSIGN_BUNDLE}')"  />
		</tds:hasPermission>
	</div>
	<div class="tabInner">
		<div id="item1">
			<table id="tag" border="0" cellpadding="0" cellspacing="0"
				style="border-collapse: separate" class="table">
				<thead>
					<tr class="header">
						<th nowrap="nowrap"><input id="selectId" type="checkbox" onclick="selectAll()" title="Select All" />&nbsp;Actions</th>
						<th class="Arrowcursor ${sortBy == 'assetName' ? orderBy :''}" onclick="javascript:getListBySort('server','${dependencyBundle}','assetName')">Asset Name</th>
						<th class="Arrowcursor ${sortBy == 'model' ? orderBy :''}" onclick="javascript:getListBySort('server','${dependencyBundle}','model')">Model</th>
						<th class="Arrowcursor ${sortBy == 'sourceLocation' ? orderBy :''}" onclick="javascript:getListBySort('server','${dependencyBundle}','sourceLocation')">Source Location</th>
						<th class="Arrowcursor ${sortBy == 'sourceRack' ? orderBy :''}" onclick="javascript:getListBySort('server','${dependencyBundle}','sourceRack')">Source Rack</th>
						<th class="Arrowcursor ${sortBy == 'targetLocation' ? orderBy :''}" onclick="javascript:getListBySort('server','${dependencyBundle}','targetLocation')">Target Location</th>
						<th class="Arrowcursor ${sortBy == 'targetRack' ? orderBy :''}" onclick="javascript:getListBySort('server','${dependencyBundle}','targetRack')">Target rack</th>
						<th class="Arrowcursor ${sortBy == 'assetType' ? orderBy :''}" onclick="javascript:getListBySort('server','${dependencyBundle}','assetType')">Asset Type</th>
						<th class="Arrowcursor ${sortBy == 'assetTag' ? orderBy :''}" onclick="javascript:getListBySort('server','${dependencyBundle}','assetTag')">Asset Tag</th>
						<th class="Arrowcursor ${sortBy == 'validation' ? orderBy :''}" onclick="javascript:getListBySort('server','${dependencyBundle}','validation')">Validation</th>
						<th class="Arrowcursor ${sortBy == 'moveBundle' ? orderBy :''}" onclick="javascript:getListBySort('server','${dependencyBundle}','moveBundle')">Bundle</th>
						<th class="Arrowcursor ${sortBy == 'planStatus' ? orderBy :''}" onclick="javascript:getListBySort('server','${dependencyBundle}','planStatus')">Plan Status</th>
						<th class="Arrowcursor ${sortBy == 'depToResolve' ? orderBy :''}" onclick="javascript:getListBySort('server','${dependencyBundle}','depToResolve')">TBD</th>
						<th class="Arrowcursor ${sortBy == 'depToConflict' ? orderBy :''}" onclick="javascript:getListBySort('server','${dependencyBundle}','depToConflict')">Conflict</th>
					</tr>

				</thead>
				<tbody class="tbody">
					<g:each in="${assetList}" var="asset" status="i">
						<tr id="tag_row1" style="cursor: pointer;"
							class="${(i % 2) == 0 ? 'odd' : 'even'}">
							<td nowrap="nowrap">
							<g:checkBox name="checkBox" id="checkId_${asset.id}" ></g:checkBox>
							<a href="javascript:editEntity('dependencyConsole','Server', ${asset.id})"><img
									src="/tdstm/images/skin/database_edit.png" border="0px" />
							</a> <span id="icon_15651">
							 <g:if test="${AssetComment.find('from AssetComment where assetEntity = '+asset.id+' and commentType = ? and isResolved = ?',['issue',0])}">
							   <g:remoteLink controller="assetEntity" action="listComments" id="${asset.id}" before="setAssetId('${asset.id}');" onComplete="listCommentsDialog(e,'never');">
							      <img id="comment_${asset.id}" src="${resource(dir:'i',file:'db_table_red.png')}" border="0px" />
							   </g:remoteLink>
				             </g:if>
						     <g:elseif test="${AssetComment.find('from AssetComment where assetEntity = '+asset.id)}">
						     <g:remoteLink controller="assetEntity" action="listComments" id="${asset.id}" before="setAssetId('${asset.id}');" onComplete="listCommentsDialog(e,'never');">
							      <img id="comment_${asset.id}" src="${resource(dir:'i',file:'db_table_bold.png')}" border="0px" />
							 </g:remoteLink>
						     </g:elseif>
						     <g:else>
						     <a href="javascript:createNewAssetComment(${asset.id},'${asset.assetName}','${asset.assetType}');">
							    <img src="${resource(dir:'i',file:'db_table_light.png')}" border="0px" onclick="createNewAssetComment(${asset.id},'${asset.assetName}','${asset.assetType}');"/>
							 </a>
							    
						    </g:else>
			 				</span>
							</td>
							<td><span
								onclick="getEntityDetails('dependencyConsole','Server', ${asset.id} )">${asset.assetName}</span>
							</td>
							<td><span
								onclick="getEntityDetails('dependencyConsole','Server', ${asset.id} )">${asset.model}</span>
							</td>
							<td><span
								onclick="getEntityDetails('dependencyConsole','Server', ${asset.id} )">${asset.sourceLocation}</span>
							</td>
							<td><span
								onclick="getEntityDetails('dependencyConsole','Server', ${asset.id} )">${asset.sourceRack}</span>
							</td>
							<td><span
								onclick="getEntityDetails('dependencyConsole','Server', ${asset.id} )">${asset.targetLocation}</span>
							</td>
							<td><span
								onclick="getEntityDetails('dependencyConsole','Server', ${asset.id} )">${asset.targetRack}</span>
							</td>
							<td><span
								onclick="getEntityDetails('dependencyConsole','Server', ${asset.id} )">${asset.assetType}</span>
							</td>
							<td><span
								onclick="getEntityDetails('dependencyConsole','Server', ${asset.id} )">${asset.assetTag}</span>
							</td>
							<td><span
								onclick="getEntityDetails('dependencyConsole','Server', ${asset.id} )">${asset.validation}</span>
							</td>
							<td><span
								onclick="getEntityDetails('dependencyConsole','Server', ${asset.id} )">${asset.moveBundle}</span>
							</td>
							<td><span
								onclick="getEntityDetails('dependencyConsole','Server', ${asset.id} )">${asset.planStatus}</span>
							</td>
							<td><span
								onclick="getEntityDetails('dependencyConsole','Server', ${asset.id} )">${asset?.depToResolve?:''}</span>
							</td>
							<td><span
								onclick="getEntityDetails('dependencyConsole','Server', ${asset.id} )">${asset?.depToConflict?:''}</span>
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
		$('#tabTypeId').val('server')
	</script>
</div>

