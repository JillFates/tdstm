<%@page import="com.tds.asset.AssetComment"%>
<div class="tabs">
	<ul>
		
		<li id="appli" ><a href="javascript:getList('Apps',${dependencyBundle})">Apps(${appDependentListSize})</a>
		<li id="serverli" class="active"><a
			href="javascript:getList('server',${dependencyBundle})"><span>Servers(${assetEntityListSize})</span> </a></li>
		</li>
		<li id="dbli" ><a href="#item3"><a href="javascript:getList('database',${dependencyBundle})">DB(${dbDependentListSize})</a></li>
		<li id="fileli" ><a href="javascript:getList('files',${dependencyBundle})">Files(${filesDependentListSize})</a></li>
		<li id="graphli"><a href="javascript:getList('graph',${dependencyBundle})">Map</a></li>
	</ul>
    <div id ="selectionId" >
   		<input type="hidden" id="assetTypeId" name="assetType" value="${asset}" />
   		<input type="hidden" id="assetTypesId" name="assetType" value="server" />
   		<tds:hasPermission permission='MoveBundleEditView'>
			<input id="state" type="button"  class="submit" value="Assignment" onclick="changeMoveBundle($('#assetTypeId').val(),${assetList.id})"  />
		</tds:hasPermission>
	</div>
	<div class="tabInner">
		<div id="item1">
			<table id="tag" border="0" cellpadding="0" cellspacing="0"
				style="border-collapse: separate" class="table">
				<thead>
					<tr class="header">
						<th nowrap="nowrap"><input id="selectId" type="checkbox" onclick="selectAll()" title="Select All" />&nbsp;Actions</th>
						<th>Application</th>
						<th>Asset Name</th>
						<th>Model</th>
						<th>Source Location</th>
						<th>Source Rack</th>
						<th>Target Location</th>
						<th>Target rack</th>
						<th>Asset Type</th>
						<th>Asset Tag</th>
						<th>Serial #</th>
						<th>Move Bundle</th>
						<th>Dep Up</th>
						<th>Dep Down</th>
					</tr>

				</thead>
				<tbody class="tbody">
					<g:each in="${assetList}" var="asset" status="i">
						<tr id="tag_row1" style="cursor: pointer;"
							class="${(i % 2) == 0 ? 'odd' : 'even'}">
							<td nowrap="nowrap">
							<g:checkBox name="checkBox" id="checkId_${asset.id}" ></g:checkBox>
							<a href="javascript:editEntity('planningConsole','Server', ${asset.id})"><img
									src="/tdstm/images/skin/database_edit.png" border="0px" />
							</a> <span id="icon_15651">
							 <g:if test="${AssetComment.find('from AssetComment where assetEntity = '+asset.id+' and commentType = ? and isResolved = ?',['issue',0])}">
							   <g:remoteLink controller="assetEntity" action="listComments" id="${asset.id}" before="setAssetId('${asset.id}');" onComplete="listCommentsDialog(e,'never');">
							      <img id="comment_${asset.id}" src="${createLinkTo(dir:'i',file:'db_table_red.png')}" border="0px" />
							   </g:remoteLink>
				             </g:if>
						     <g:elseif test="${AssetComment.find('from AssetComment where assetEntity = '+asset.id)}">
						     <g:remoteLink controller="assetEntity" action="listComments" id="${asset.id}" before="setAssetId('${asset.id}');" onComplete="listCommentsDialog(e,'never');">
							      <img id="comment_${asset.id}" src="${createLinkTo(dir:'i',file:'db_table_bold.png')}" border="0px" />
							 </g:remoteLink>
						     </g:elseif>
						     <g:else>
						     <a href="javascript:createNewAssetComment(${asset.id});">
							    <img src="${createLinkTo(dir:'i',file:'db_table_light.png')}" border="0px" onclick="createNewAssetComment(${asset.id});"/>
							 </a>
							    
						    </g:else>
			 				</span>
							</td>
							<td><span
								onclick="getEntityDetails('planningConsole','Server', ${asset.id} )">${asset.application}</span>
							</td>
							<td><span
								onclick="getEntityDetails('planningConsole','Server', ${asset.id} )">${asset.assetName}</span>
							</td>
							<td><span
								onclick="getEntityDetails('planningConsole','Server', ${asset.id} )">${asset.model}</span>
							</td>
							<td><span
								onclick="getEntityDetails('planningConsole','Server', ${asset.id} )">${asset.sourceLocation}</span>
							</td>
							<td><span
								onclick="getEntityDetails('planningConsole','Server', ${asset.id} )">${asset.sourceRack}</span>
							</td>
							<td><span
								onclick="getEntityDetails('planningConsole','Server', ${asset.id} )">${asset.targetLocation}</span>
							</td>
							<td><span
								onclick="getEntityDetails('planningConsole','Server', ${asset.id} )">${asset.targetRack}</span>
							</td>
							<td><span
								onclick="getEntityDetails('planningConsole','Server', ${asset.id} )">${asset.assetType}</span>
							</td>
							<td><span
								onclick="getEntityDetails('planningConsole','Server', ${asset.id} )">${asset.assetTag}</span>
							</td>
							<td><span
								onclick="getEntityDetails('planningConsole','Server', ${asset.id} )">${asset.serialNumber}</span>
							</td>
							<td><span
								onclick="getEntityDetails('planningConsole','Server', ${asset.id} )">${asset.moveBundle}</span>
							</td>
							<td><span
								onclick="getEntityDetails('planningConsole','Server', ${asset.id} )">${ com.tds.asset.AssetDependency.countByDependentAndStatusNotEqual(com.tds.asset.AssetEntity.get(asset.id),'Validated') }</span>
							</td>
							<td><span
								onclick="getEntityDetails('planningConsole','Server', ${asset.id} )">${ com.tds.asset.AssetDependency.countByAssetAndStatusNotEqual(com.tds.asset.AssetEntity.get(asset.id),'Validated') }</span>
							</td>
						</tr>
					</g:each>
				</tbody>

			</table>
		</div>
	</div>
	<script type="text/javascript">
		$('#tabTypeId').val('server')
	</script>
</div>

