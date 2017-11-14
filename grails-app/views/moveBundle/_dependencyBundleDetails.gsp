<%@page defaultCodec="none" %>
<%@page import="net.transitionmanager.security.Permission"%>
<div style="margin-top: 10px; float: left;">
	<div class="compactClass">
		<input type="hidden" id="tabTypeId" name="tabType" value="${asset}" />
		<div style="margin-left: 0px; margin-bottom: 10px;">
			<div class="message" id="messageId" style="display:none">${flash.message}</div>
        	<div style="width: 250px; margin-bottom: 3px">
				<b>Dependency Groups</b>&nbsp;&nbsp;&nbsp;
				<tds:hasPermission permission="${Permission.DepAnalyzerGenerate}">
				<input  type="button"  class="submit pointer" value="Regenerate..." onclick="showDependencyControlDiv()"  />
				</tds:hasPermission>
			</div>
			<div class="planBundleSel">
				<g:form name="bundleForm" action="dependencyConsole">	
					<input type="hidden" name="assinedGroup" id="assinedGroup" value="${isAssigned}" />
					Move Bundle: <g:select id="planningBundleSelectId" name="bundle" from="${moveBundle}" noSelection="${['':'All Planning']}"
					 				optionKey="id" value="${moveBundleId}" onchange="this.form.submit()"/>&nbsp;&nbsp;
					<span class="checkboxContainer">
						<input type="checkbox" id="assinedGroupCB" class="pointer" ${isAssigned == '1' ? 'checked="checked"' : ''} onclick="assignedCheckbox( this )" />
						<label for="assinedGroupCB" class="pointer">&nbsp;Show ONLY Work In Progress</label>
					</span>
					<div style="position: absolute; margin: -25px 409px 0;">
						<g:link controller="moveBundle" action="dependencyConsole" class="ui-button ui-widget ui-state-default ui-corner-all ui-button-text-icon-primary">
							<img src="${resource(dir:'icons',file:'arrow_refresh.png')}" title="Refresh Data">
						</g:link>
					</div>
				</g:form>
			</div>
			&nbsp;Dependency Analysis last run by ${ depGrpCrt?.modifiedBy } on &nbsp;${date} and ${dependencyBundleCount} dependency group(s) were discovered
		</div>
		<div id="processDiv" style="display: none;">
			<img src="${resource(dir:'images',file:'processing.gif')}" />
		</div>
	</div>
	<div style="border: 1px solid #63A242; margin-left: 0px;">
		<div id="dependencyDivId" style="overflow-x:auto;">
			<table id="dependencyTableId" cellpadding="4" cellspacing="0" style="border: 0px;">
				<tr id="dependencyGroupsRowId" class="odd">

					<td class="labelColumn"><b>Group</b> <i class="fa fa-fw fa-question-circle" style="font-size: 14px; cursor: pointer;" onclick="openStatusColors()"></i></td>

					<td id="span_all" class="pointer" onclick="getList($('#tabTypeId').val(), null)" 
						title="All Assets:&#013;Apps: &Tab;${gridStats.app[0]}&#013;Servers: &Tab;${gridStats.server[0] + gridStats.vm[0]}&#013;DBs: &Tab;${gridStats.db[0]}&#013;Storage: &Tab;${gridStats.storage[0]}">
						<span class="depGroup">All</span>
					</td>

					<g:each in="${dependencyConsoleList}" var="asset" status="depIndex">
						<g:if test="${depIndex == 0}">
							<td id="span_${asset.dependencyBundle}" class="pointer" 
								
								onclick="getList( $('#tabTypeId').val().toLowerCase(), ${asset.dependencyBundle})" 
								title="Assets with no Dependencies:&#013;Apps: &Tab;${asset.appCount}&#013;Servers: &Tab;${asset.serverCount + asset.vmCount}&#013;DBs: &Tab;${asset.dbCount}&#013;Storage: &Tab;${asset.storageCount}">
								<span class="depGroup">Orphans</span>
							</td>
							<td id="span_onePlus" class="pointer" 
								onclick="getList( $('#tabTypeId').val(), 'onePlus')" 
								title="All Grouped Assets:&#013;Apps: &Tab;${gridStats.app[1]}&#013;Servers: &Tab;${gridStats.server[1] + gridStats.vm[1]}&#013;DBs: &Tab;${gridStats.db[1]}&#013;Storage: &Tab;${gridStats.storage[1]}">
								<span class="depGroup">Grouped</span>
							</td>
						</g:if>
						<g:else>
							<td id="span_${asset.dependencyBundle}" class="${asset.statusClass} pointer" 
								onclick="getList( $('#tabTypeId').val().toLowerCase(), ${asset.dependencyBundle})" 
								title="Group ${asset.dependencyBundle}:&#013;Apps: &Tab;${asset.appCount}&#013;Servers: &Tab;${asset.serverCount + asset.vmCount}&#013;DBs: &Tab;${asset.dbCount}&#013;Storage: &Tab;${asset.storageCount}">
								<span class="depGroup">${asset.dependencyBundle}</span>
							</td>
						</g:else>

					</g:each>
				</tr>

				<tr class="even">
					<td class="labelColumn"><b>Applications</b></td>
					<td id="app_all">
						<span id="allAppCount">${gridStats.app[0] ?: '&nbsp;' }</span>
					</td>
					<g:each in="${dependencyConsoleList}" var="asset">
						<td id="app_${asset.dependencyBundle}">
							<span>${asset.appCount ?: '&nbsp;' }</span>
						</td>
						<g:if test="${asset.dependencyBundle == 0}">
							<td id="app_onePlus">
								<span>${gridStats.app[1] ?: '&nbsp;' }</span>
							</td>
						</g:if>
					</g:each>
				</tr>

				<tr class="odd compactClass">
					<td class="labelColumn"><b>Servers Physical</b></td>
					<td id="server_all">
						<span id="allServerCount">${gridStats.server[0] ?: '&nbsp;' }</span>
					</td>
					<g:each in="${dependencyConsoleList}" var="asset">
						<td id="server_${asset.dependencyBundle}">
							<span>${asset.serverCount ?: '&nbsp;' }</span>
						</td>
						<g:if test="${asset.dependencyBundle == 0}">
							<td id="server_onePlus">
								<span>${gridStats.server[1] ?: '&nbsp;' }</span>
							</td>
						</g:if>
					</g:each>
				</tr>

				<tr class="even compactClass">
					<td class="labelColumn"><b>Servers Virtual</b></td>
					<td id="vm_all">
						<span id="allVirtualCount">${gridStats.vm[0] ?: '&nbsp;' }</span>
					</td>
					<g:each in="${dependencyConsoleList}" var="asset">
						<td id="vm_${asset.dependencyBundle}">
							<span>${asset.vmCount ?: '&nbsp;' }</span>
						</td>
						<g:if test="${asset.dependencyBundle == 0}">
							<td id="server_onePlus">
								<span>${gridStats.vm[1] ?: '&nbsp;' }</span>
							</td>
						</g:if>
					</g:each>
				</tr>

				<tr class="odd compactClass">
					<td class="labelColumn"><b>Databases</b></td>
					<td id="db_all">
						<span id="allDatabaseCount">${gridStats.db[0] ?: '&nbsp;' }</span>
					</td>
					<g:each in="${dependencyConsoleList}" var="asset">
						<td id="db_${asset.dependencyBundle}">
							<span>${asset.dbCount ?: '&nbsp;' }</span>
						</td>
						<g:if test="${asset.dependencyBundle == 0}">
							<td id="db_onePlus">
								<span>${gridStats.db[1] ?: '&nbsp;' }</span>
							</td>
						</g:if>
					</g:each>
				</tr>

				<tr class="even compactClass">
					<td class="labelColumn"><b>Storage (all)</b></td>
					<td id="file_all">
						<span id="allFileCount">${gridStats.storage[0] ?: '&nbsp;' }</span>
					</td>
					<g:each in="${dependencyConsoleList}" var="asset">
						<td id="file_${asset.dependencyBundle ?: 0 }">
							<span>${asset.storageCount ?: '&nbsp;' }</span>
						</td>
						<g:if test="${asset.dependencyBundle == 0}">
							<td id="file_onePlus">
								<span>${gridStats.storage[1] ?: '&nbsp;' }</span>
							</td>
						</g:if>
					</g:each>
				</tr>
			</table>
		</div>
	</div>
</div>

