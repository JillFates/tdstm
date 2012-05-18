<div style="margin-top: 10px; float:left;">
			<div>
				<input type="hidden" id="tabTypeId" name="tabType" value="${asset}" />
				<div style="margin-left: 20px; margin-bottom: 10px;">
					<h3>
						<b>Dependency Groups</b>
					</h3>
					&nbsp;Dependency Analysis Run&nbsp;${date}: There were ${dependencyBundleCount} dependency groups discovered
				</div>
				<div id="processDiv" style="display:none;">
		          		<img src="../images/processing.gif"/>
		          		
		        </div>
				<table id="dependencyTableId" border="0" cellpadding="4" cellspacing="0"
					style="margin-left: 20px; width: 500px;">
					<tr class="odd">
						<td><b>Dependency Group</b></td>
						<g:if test="${planningDashboard!='planningDashboard'}">
							<td id="span_all" class="highlightSpan"  >
								<span style="cursor: pointer;" onclick="getList($('#tabTypeId').val(),'all')" >All</span>
							</td>
						</g:if> 
						<g:each in="${planningConsoleList}" var="asset">
							<td id="span_${asset.dependencyBundle}">
								<span style="cursor: pointer; color: grey;" onclick="getList( $('#tabTypeId').val() , ${asset.dependencyBundle})" >${asset.dependencyBundle}</span>
							</td>
						</g:each>
						</tr>
					<tr class="even">
						<td><b>Applications</b></td>
						<g:if test="${planningDashboard!='planningDashboard'}">
							<td id="app_all"><span id="allAppCount">${applicationListSize }</span></td>
						</g:if> 
						<g:each in="${planningConsoleList}" var="asset"><td id="app_${asset.dependencyBundle}">${asset.appCount }</td>
						</g:each>
					</tr>
					<tr class="odd">
						<td><b>Physical Servers</b></td>
						<g:if test="${planningDashboard!='planningDashboard'}">
							<td id="server_all"><span id="allServerCount">${physicalListSize }</span></td>
						</g:if> 
						<g:each in="${planningConsoleList}" var="asset"><td id="server_${asset.dependencyBundle}">${asset.serverCount }</td>
					    </g:each>
					</tr>
					<tr class="even">
						<td><b>Virtual Servers</b></td>
						<g:if test="${planningDashboard!='planningDashboard'}">
							<td id="vm_all"><span id="allVirtualCount">${virtualListSize }</span></td>
						</g:if>
						<g:each in="${planningConsoleList}" var="asset"><td id="vm_${asset.dependencyBundle}">${asset.vmCount }</td>
					    </g:each>
					</tr>
				</table>
			</div>
</div>

