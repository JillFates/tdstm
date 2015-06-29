<div style="margin-top: 10px; float: left;">
	<div class="compactClass">
		<input type="hidden" id="tabTypeId" name="tabType" value="${asset}" />
		<div style="margin-left: 20px; margin-bottom: 10px;">
        	<div class="message" id="messageId" style="display:none">${flash.message}</div>
			<h3>
				<b>Dependency Groups</b>&nbsp;&nbsp;&nbsp;<input  type="button"  class="submit" value="Regenerate..." onclick="showDependencyControlDiv()"  />
			</h3>
			 <div class="planBundleSel">
				<g:form name="bundleForm" action="dependencyConsole">	
					<input type="hidden" name="assinedGroup" id="assinedGroup" value="${isAssigned}" />
					Move Bundle: <g:select id="planningBundleSelectId" name="bundle" from="${moveBundle}" noSelection="${['':'All Planning']}"
					 				optionKey="id" value="${moveBundleId}" onchange="this.form.submit()"/>&nbsp;&nbsp;
					 <input type="checkbox" id="assinedGroupCB" ${isAssigned == '1' ? 'checked="checked"': ''} onclick="assignedCheckbox( this )" />
					 <label>Show ONLY Work In Progress</label>
				</g:form>
			</div>
			&nbsp;Dependency Analysis last run by ${ depGrpCrt?.modifiedBy } on &nbsp;${date} and ${dependencyBundleCount} dependency group(s) were discovered
		</div>
		<div id="processDiv" style="display: none;">
			<img src="../images/processing.gif" />
		</div>
	</div>
	<div style="border: 1px solid #63A242; margin-left: 20px;">
		<div id="dependencyDivId" style="overflow-x:scroll;">
			<table id="dependencyTableId" cellpadding="4" cellspacing="0" style="border: 0px;">
				<tr class="odd">
					<td class="labelColumn"><b>Group</b></td>
					<td id="span_all" class="pointer" onclick="getList($('#tabTypeId').val(), null)">
						<span class="depGroup">All</span>
					</td>
					<td id="span_onePlus" class="pointer" onclick="getList( $('#tabTypeId').val(), 'onePlus')">
						<span class="depGroup">1+</span>
					</td>
					<g:each in="${dependencyConsoleList}" var="asset">
						<td id="span_${asset.dependencyBundle}" class="${asset.statusClass} pointer" onclick="getList( $('#tabTypeId').val().toLowerCase(), ${asset.dependencyBundle})">
							<span class="depGroup">${asset.dependencyBundle}</span>
						</td>
					</g:each>
				</tr>
				<tr class="even">
					<td class="labelColumn"><b>Applications</b></td>
					<td id="app_all" class="app_count">
						<span id="allAppCount">${gridStats.app[0] ?: '&nbsp;' }</span>
					</td>
					<td id="app_onePlus">${gridStats.app[1]}</td>
					<g:each in="${dependencyConsoleList}" var="asset">
						<td id="app_${asset.dependencyBundle}">
							${asset.appCount ?: '&nbsp;' }
						</td>
					</g:each>
				</tr>
				<tr class="odd compactClass">
					<td class="labelColumn"><b>Servers Physical</b></td>
					<td id="server_all" class="server_count">
						<span id="allServerCount">${gridStats.server[0] ?: '&nbsp;' }</span></td>
					<td id="server_onePlus">${gridStats.server[1] ?: '&nbsp;' }</td>
					<g:each in="${dependencyConsoleList}" var="asset">
						<td id="server_${asset.dependencyBundle}">
							${asset.serverCount ?: '&nbsp;' }
						</td>
					</g:each>
				</tr>
				<tr class="even compactClass">
					<td class="labelColumn"><b>Servers Virtual</b></td>
					<td id="vm_all" class="vm_count">
						<span id="allVirtualCount">${gridStats.vm[0] ?: '&nbsp;' }</span>
					</td>
					<td id="vm_onePlus">${gridStats.vm[1] ?: '&nbsp;' }</td>
					<g:each in="${dependencyConsoleList}" var="asset">
						<td id="vm_${asset.dependencyBundle}">
							${asset.vmCount ?: '&nbsp;' }
						</td>
					</g:each>
				</tr>
				<tr class="odd compactClass">
					<td class="labelColumn"><b>Databases</b></td>
					<td id="db_all" class="db_count">
						<span id="allDatabaseCount">${gridStats.db[0] ?: '&nbsp;' }</span>
					</td>
					<td id="db_onePlus">${gridStats.db[1] ?: '&nbsp;' }</td>
					<g:each in="${dependencyConsoleList}" var="asset">
						<td id="db_${asset.dependencyBundle}">
							${asset.dbCount ?: '&nbsp;' }
						</td>
					</g:each>
				</tr>
				<tr class="even compactClass">
					<td class="labelColumn"><b>Storage (all)</b></td>
					<td id="file_all"  class="file_count">
						<span id="allFileCount">${gridStats.storage[0] ?: '&nbsp;' }</span>
					</td>
					<td id="file_onePlus">${gridStats.storage[1] ?: '&nbsp;' }</td>
					<g:each in="${dependencyConsoleList}" var="asset">
						<td id="file_${asset.dependencyBundle ?: 0 }">
							${asset.storageCount ?: '&nbsp;' }
						</td>
					</g:each>
				</tr>
			</table>
		</div>
	</div>
</div>

