<div class="tabs">
	<ul>
		<li id="serverli" ><a
			href="javascript:getList('server')"><span>Servers(10)</span> </a></li>
		<li id="appli" ><a href="javascript:getList('Apps')">Apps(10)</a>
		</li>
		<li id="dbli" ><a href="#item3"><a href="javascript:getList('database')">DB(10)</a></li>
		<li id="fileli" class="active"><a href="javascript:getList('files')">Files(10)</a></li>
	</ul>
	<div class="tabInner">
		<div id="item1">
			<table id="tagApp" border="0" cellpadding="0" cellspacing="0"
				style="border-collapse: separate" class="table">
				<thead>
					<tr class="header">
						<th>Actions</th>
						<th>Name</th>
						<th>File Format</th>
						<th>File Size</th>
						<th>Move Bundle</th>
						<th>Plan Status</th>
						<th>Dep Up</th>
						<th>Dep Down</th>
					</tr>

				</thead>
				<tbody class="tbody">
					<g:each in="${filesList}" var="files" status="i">
						<tr id="tag_row1" style="cursor: pointer;"
							class="${(i % 2) == 0 ? 'odd' : 'even'}">
							<td><a
								href="javascript:editEntity('planningConsole','Files', ${files.id})"><img
									src="/tdstm/images/skin/database_edit.png" border="0px" />
							</a> <span id="icon_15651"> <a
									href="javascript:createNewAssetComment(15651);"> <img
										src="/tdstm/i/db_table_light.png" border="0px" /> </a> </span>
							</td>
							<td><span
								onclick="getEntityDetails('planningConsole','Files', ${files.id} )">${files.assetName}</span>
							</td>
							<td><span
								onclick="getEntityDetails('planningConsole','Files', ${files.id} )">${files.fileFormat}</span>
							</td>
                            <td><span
								onclick="getEntityDetails('planningConsole','Files', ${files.id} )">${files.fileSize}</span>
							</td>

							<td><span
								onclick="getEntityDetails('planningConsole','Files', ${files.id} )">${files.moveBundle}</span>
							</td>
							<td><span
								onclick="getEntityDetails('planningConsole','Files', ${files.id} )">${files.planStatus}</span>

							</td>
							<td><span
								onclick="getEntityDetails('planningConsole','Files', ${files.id} )">0</span>
							</td>
							<td><span
								onclick="getEntityDetails('planningConsole','Files', ${files.id} )">4</span>
							</td>
						</tr>
					</g:each>
				</tbody>
			</table>
		</div>
	</div>
</div>