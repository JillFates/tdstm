<div class="tabs">
	<ul>
		<li id="serverli" ><a
			href="javascript:getList('server',${dependencyBundle})"><span>Servers(${assetEntityListSize})</span> </a></li>
		<li id="appli" class="active"><a href="javascript:getList('Apps',${dependencyBundle})">Apps(${appDependentListSize})</a>
		</li>
		<li id="dbli" ><a href="#item3"><a href="javascript:getList('database',${dependencyBundle})">DB(${dbDependentListSize})</a></li>
		<li id="fileli" ><a href="javascript:getList('files',${dependencyBundle})">Files(${filesDependentListSize})</a></li>
	</ul>
	<div class="tabInner">
		<div id="item1">
			<table id="tagApp" border="0" cellpadding="0" cellspacing="0"
				style="border-collapse: separate" class="table">
				<thead>
					<tr class="header">
						<th>Actions</th>
						<th>Name</th>
						<th>App Owner</th>

						<th>App Sme</th>
						<th>Move Bundle</th>
						<th>Plan Status</th>
						<th>Dep Up</th>
						<th>Dep Down</th>
					</tr>

				</thead>
				<tbody class="tbody">
					<g:each in="${appList}" var="app" status="i">
						<tr id="tag_row1" style="cursor: pointer;"
							class="${(i % 2) == 0 ? 'odd' : 'even'}">
							<td><a
								href="javascript:editEntity('planningConsole','Application', ${app.id})"><img
									src="/tdstm/images/skin/database_edit.png" border="0px" />
							</a> <span id="icon_15651"> <a
									href="javascript:createNewAssetComment(15651);"> <img
										src="/tdstm/i/db_table_light.png" border="0px" /> </a> </span>
							</td>
							<td><span
								onclick="getEntityDetails('planningConsole','Application', ${app.id} )">${app.assetName}</span>
							</td>
							<td><span
								onclick="getEntityDetails('planningConsole','Application', ${app.id} )">${app.owner}</span>
							</td>

							<td><span
								onclick="getEntityDetails('planningConsole','Application', ${app.id} )">${app.appSme}</span>
							</td>
							<td><span
								onclick="getEntityDetails('planningConsole','Application', ${app.id} )">${app.moveBundle}</span>
							</td>
							<td><span
								onclick="getEntityDetails('planningConsole','Application', ${app.id} )">${app.planStatus}</span>

							</td>
							<td><span
								onclick="getEntityDetails('planningConsole','Application', ${app.id} )">${ com.tds.asset.AssetDependency.countByDependentAndStatusNotEqual(com.tds.asset.AssetEntity.get(app.id),'Validated') }</span>
							</td>
							<td><span
								onclick="getEntityDetails('planningConsole','Application', ${app.id} )">${ com.tds.asset.AssetDependency.countByAssetAndStatusNotEqual(com.tds.asset.AssetEntity.get(app.id),'Validated') }</span>
							</td>
						</tr>
					</g:each>
				</tbody>
			</table>
		</div>
	</div>
</div>