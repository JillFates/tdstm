<div style="margin-top: 10px;">
			<div>
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
						<td><b>Dependency Bundle</b></td>
						<g:each in="${planningConsoleList}" var="asset"><td><span id="serverIds" style="cursor: pointer; color: grey;" onclick="getList('server',${asset.dependencyBundle})"><b>${asset.dependencyBundle}</b>
						</span></td></g:each>
						</tr>
					<tr class="even">
						<td><b>Applications</b></td>
						<g:each in="${planningConsoleList}" var="assets"><td>${assets.appCount }</td>
						</g:each>
					</tr>
					<tr class="odd">
						<td><b>Physical Servers</b></td>
						<g:each in="${planningConsoleList}" var="assets"><td>${assets.serverCount }</td>
					    </g:each>
					</tr>
					<tr class="even">
						<td><b>Virtual Servers</b></td>
						<g:each in="${planningConsoleList}" var="assets"><td>${assets.vmCount }</td>
					    </g:each>
					</tr>
				</table>
			</div>
</div>