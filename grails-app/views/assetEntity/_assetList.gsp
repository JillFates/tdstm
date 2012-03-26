<div class="tabs">
	<ul>
		<li id="serverli" class="active"><a
			href="javascript:getList('server')"><span>Servers(10)</span> </a></li>
		<li id="appli" ><a href="javascript:getList('Apps')">Apps(10)</a>
		</li>
		<li id="dbli" ><a href="#item3"><a href="javascript:getList('database')">DB(10)</a></li>
		<li id="fileli" ><a href="javascript:getList('files')">Files(10)</a></li>
	</ul>
	<div class="tabInner">
		<div id="item1">
			<table id="tag" border="0" cellpadding="0" cellspacing="0"
				style="border-collapse: separate" class="table">
				<thead>
					<tr class="header">
						<th>Actions</th>
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
							<td><a
								href="javascript:editEntity('planningConsole','Server', ${asset.id})"><img
									src="/tdstm/images/skin/database_edit.png" border="0px" />
							</a> <span id="icon_15651"> <a
									href="javascript:createNewAssetComment(15651);"> <img
										src="/tdstm/i/db_table_light.png" border="0px" /> </a> </span>
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
								onclick="getEntityDetails('planningConsole','Server', ${asset.id} )">0</span>
							</td>
							<td><span
								onclick="getEntityDetails('planningConsole','Server', ${asset.id} )">4</span>
							</td>
						</tr>
					</g:each>
				</tbody>

			</table>
		</div>
	</div>
</div>
