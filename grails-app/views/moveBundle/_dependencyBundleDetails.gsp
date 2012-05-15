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
							<td><span id="allDependencyBundle" class="highlightSpan"  style="cursor: pointer; background-color: yellow;font-weight:bold;" onclick="getList($('#tabTypeId').val(),null)" >All</span></td>
						</g:if> 
					<g:each in="${planningConsoleList}" var="asset"><td><span id="serverIds"  style="cursor: pointer; color: grey;" onclick="getList( $('#tabTypeId').val() , ${asset.dependencyBundle})" ><span id="span_${asset.dependencyBundle}" class="highlightSpan" >${asset.dependencyBundle}</span>
						</span></td></g:each>
						</tr>
					<tr class="even">
						<td><b>Applications</b></td>
						<g:if test="${planningDashboard!='planningDashboard'}">
							<td><span id="allAppCount">${applicationListSize }</span></td>
						</g:if> 
						<g:each in="${planningConsoleList}" var="assets"><td>${assets.appCount }</td>
						</g:each>
					</tr>
					<tr class="odd">
						<td><b>Physical Servers</b></td>
						<g:if test="${planningDashboard!='planningDashboard'}">
							<td><span id="allServerCount">${physicalListSize }</span></td>
						</g:if> 
						<g:each in="${planningConsoleList}" var="assets"><td>${assets.serverCount }</td>
					    </g:each>
					</tr>
					<tr class="even">
						<td><b>Virtual Servers</b></td>
						<g:if test="${planningDashboard!='planningDashboard'}">
							<td><span id="allVirtualCount">${virtualListSize }</span></td>
						</g:if>
						<g:each in="${planningConsoleList}" var="assets"><td>${assets.vmCount }</td>
					    </g:each>
					</tr>
				</table>
			</div>
</div>

