<div style="margin-top: 10px; float: left;">
	<div>
		<input type="hidden" id="tabTypeId" name="tabType" value="${asset}" />
		<div style="margin-left: 20px; margin-bottom: 10px;">
			<h3>
				<b>Dependency Groups</b>
			</h3>
			&nbsp;Dependency Analysis Run&nbsp;${date}: There were
			${dependencyBundleCount}
			dependency groups discovered
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
							<td><b>Dependency Group</b></td>
						<tr class="even">
							<td><b>Applications</b></td>
						</tr>
						<tr class="odd">
							<td><b>Physical Servers</b></td>
						</tr>
						<tr class="even">
							<td><b>Virtual Servers</b></td>
						</tr>
					</table>
					</div>
				</td>
				<td style="padding: 0px;">
					<div style="overflow-x:scroll; max-width: 1350px;">
						<table id="dependencyTableId" cellpadding="4" cellspacing="0" style="border: 0px;">
							<tr class="odd">
								<g:if test="${planningDashboard!='planningDashboard'}">
									<td id="span_all" class="highlightSpan"><span
										style="cursor: pointer;"
										onclick="getList($('#tabTypeId').val(),null)">All</span></td>
								</g:if>
								<g:each in="${planningConsoleList}" var="asset">
									<td id="span_${asset.dependencyBundle}"><span
										style="cursor: pointer; color: grey;"
										onclick="getList( $('#tabTypeId').val() , ${asset.dependencyBundle})">
											${asset.dependencyBundle}
									</span></td>
								</g:each>
							</tr>
							<tr class="even">
								<g:if test="${planningDashboard!='planningDashboard'}">
									<td id="app_all" class="app_count"><span id="allAppCount">
											${applicationListSize }
									</span></td>
								</g:if>
								<g:each in="${planningConsoleList}" var="asset">
									<td id="app_${asset.dependencyBundle}">
										${asset.appCount }
									</td>
								</g:each>
							</tr>
							<tr class="odd">
								<g:if test="${planningDashboard!='planningDashboard'}">
									<td id="server_all"  class="server_count"><span id="allServerCount">
											${physicalListSize }
									</span></td>
								</g:if>
								<g:each in="${planningConsoleList}" var="asset">
									<td id="server_${asset.dependencyBundle}">
										${asset.serverCount }
									</td>
								</g:each>
							</tr>
							<tr class="even">
								<g:if test="${planningDashboard!='planningDashboard'}">
									<td id="vm_all"  class="vm_count"><span id="allVirtualCount">
											${virtualListSize }
									</span></td>
								</g:if>
								<g:each in="${planningConsoleList}" var="asset">
									<td id="vm_${asset.dependencyBundle}">
										${asset.vmCount }
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

