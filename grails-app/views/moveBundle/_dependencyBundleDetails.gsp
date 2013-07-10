<div style="margin-top: 10px; float: left;">
	<div>
		<input type="hidden" id="tabTypeId" name="tabType" value="${asset}" />
		<div style="margin-left: 20px; margin-bottom: 10px;">
		<g:if test="${flash.message}">
        	<div class="message">${flash.message}</div>
     	 </g:if>
			<h3>
				<b>Dependency Groups</b>&nbsp;&nbsp;&nbsp;<input  type="button"  class="submit" value="Regenerate..." onclick="showDependencyControlDiv()"  />
			</h3>
			&nbsp;Dependency Analysis last run on &nbsp;${date} and ${dependencyBundleCount} dependency group(s) were discovered
		</div>
		<div id="processDiv" style="display: none;">
			<img src="../images/processing.gif" />

		</div>
	</div>
	<div style="border: 1px solid #63A242; margin-left: 20px;">
		<table id="dependencyTableId" cellpadding="0" cellspacing="0" style="border: 0px;">
			<tr>
				<td style="width: 137px; padding: 0px;">
				<div style="overflow-x:scroll; max-width: 137px;">
					<table cellpadding="4" cellspacing="0" style="border: 0px; width: 137px">
						<tr class="odd">
							<td><b>Group</b></td>
						<tr class="even">
							<td><b>Applications</b></td>
						</tr>
						<tr class="odd">
							<td><b>Phys Servers</b></td>
						</tr>
						<tr class="even">
							<td><b>Virt. Servers</b></td>
						</tr>
						<tr class="odd">
							<td><b>DB</b></td>
						</tr>
						<tr class="even">
							<td><b>Storage</b></td>
						</tr>
					</table>
				</div>
				</td>
				<td style="padding: 0px;">
					<div style="overflow-x:scroll; max-width: 1350px;">
						<table id="dependencyTableId" cellpadding="4" cellspacing="0" style="border: 0px;">
							<tr class="odd">
								<td id="span_all" class="">
									<span style="cursor: pointer;" onclick="getList($('#tabTypeId').val(), null)">All</span>
								</td>
								<td id="span_onePlus">
									<span style="cursor: pointer; color: grey;" onclick="getList( $('#tabTypeId').val(), 'onePlus')">1+</span>
								</td>
								<g:each in="${dependencyConsoleList}" var="asset">
									<td id="span_${asset.dependencyBundle}" class="${asset.statusClass}">
										<span style="cursor: pointer; color: grey;"
											onclick="getList( $('#tabTypeId').val().toLowerCase(), ${asset.dependencyBundle})">${asset.dependencyBundle}</span>
									</td>
								</g:each>
							</tr>
							<tr class="even">
								<td id="app_all" class="app_count">
									<span id="allAppCount">${gridStats.app[0]}</span>
								</td>
								<td id="app_onePlus">${gridStats.app[1]}</td>
								<g:each in="${dependencyConsoleList}" var="asset">
									<td id="app_${asset.dependencyBundle}">
										${asset.appCount }
									</td>
								</g:each>
							</tr>
							<tr class="odd">
								<td id="server_all" class="server_count">
									<span id="allServerCount">${gridStats.server[0]}</span></td>
								<td id="server_onePlus">${gridStats.server[1]}</td>
								<g:each in="${dependencyConsoleList}" var="asset">
									<td id="server_${asset.dependencyBundle}">
										${asset.serverCount }
									</td>
								</g:each>
							</tr>
							<tr class="even">
								<td id="vm_all" class="vm_count">
									<span id="allVirtualCount">${gridStats.vm[0]}</span>
								</td>
								<td id="vm_onePlus">${gridStats.vm[1]}</td>
								<g:each in="${dependencyConsoleList}" var="asset">
									<td id="vm_${asset.dependencyBundle}">
										${asset.vmCount }
									</td>
								</g:each>
							</tr>
							<tr class="odd">
								<td id="db_all" class="db_count">
									<span id="allDatabaseCount">${gridStats.db[0]}</span>
								</td>
								<td id="db_onePlus">${gridStats.db[1]}</td>
								<g:each in="${dependencyConsoleList}" var="asset">
									<td id="db_${asset.dependencyBundle}">
										${asset.dbCount }
									</td>
								</g:each>
							</tr>
							<tr class="even">
								<td id="file_all"  class="file_count">
									<span id="allFileCount">${gridStats.storage[0]}</span>
								</td>
								<td id="file_onePlus">${gridStats.storage[1]}</td>
								<g:each in="${dependencyConsoleList}" var="asset">
									<td id="file_${asset.dependencyBundle}">
										${asset.storageCount }
									</td>
								</g:each>
							</tr>
						</table>
					</div>
				</td>
			</tr>
		</table>
	</div>
</div>

