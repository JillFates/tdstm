<%-- 
	This template is used by the various tabs in the dependency console
--%>
<g:set var="graphTabClass" value="${entity=='graph' ? 'active' : ''}" />
<g:set var="allTabClass" value="${entity=='all' ? 'active' : ''}" />
<g:set var="appTabClass" value="${entity=='apps' ? 'active' : ''}" />
<g:set var="serverTabClass" value="${entity=='server' ? 'active' : ''}" />
<g:set var="dbTabClass" value="${entity=='database' ? 'active' : ''}" />
<g:set var="fileTabClass" value="${entity=='files' ? 'active' : ''}" />

<g:set var="appTabBadgeClass" value="${entity=='apps' ? 'badge-blue' : ''}" />
<g:set var="serverTabBadgeClass" value="${entity=='server' ? 'badge-blue' : ''}" />
<g:set var="dbTabBadgeClass" value="${entity=='database' ? 'badge-blue' : ''}" />
<g:set var="fileTabBadgeClass" value="${entity=='files' ? 'badge-blue' : ''}" />

<g:set var="bundle" value="${dependencyBundle=='onePlus' ? '\'onePlus\'': dependencyBundle}" />

<ul id="depConsoleTabsId" class="nav">
	<li id="graphli" class="nav-item" onclick="getList('graph',${bundle})">
		<button class="btn btn-link nav-link ${graphTabClass}">Map</button>
	</li>
	<li id="allli" class="nav-item" onclick="getList('all',${bundle})">
		<button class="btn btn-link nav-link ${allTabClass}">All</button>
	</li>
	<li id="appli" class="nav-item" onclick="getList('apps',${bundle})">
		<button class="btn btn-link nav-link ${appTabClass}">Apps
			<span class="badge ${appTabBadgeClass}">
				<g:if test="${stats.app[0] > 99}">
					99+
				</g:if>
				<g:else>
					${stats.app[0]}
				</g:else>
			</span>
		</button>
	</li>
	<li id="serverli" class="nav-item" onclick="getList('server',${bundle})">
		<button class="btn btn-link nav-link ${serverTabClass}">Servers
			<span class="badge ${serverTabBadgeClass}">
				<g:if test="${(stats.server[0] + stats.vm[0]) > 99}">
					99+
				</g:if>
				<g:else>
					${stats.server[0] + stats.vm[0]}
				</g:else>
			</span>
		</button>
	</li>
	<li id="dbli" class="nav-item" onclick="getList('database',${bundle})">
		<button class="btn btn-link nav-link ${dbTabClass}">Databases
			<span class="badge ${dbTabBadgeClass}">
				<g:if test="${stats.db[0] > 99}">
					99+
				</g:if>
				<g:else>
					${stats.db[0]}
				</g:else>
			</span>
		</button>
	</li>
	<li id="fileli" class="nav-item" onclick="getList('files',${bundle})">
		<button class="btn btn-link nav-link ${fileTabClass}">Storage
			<span class="badge ${fileTabBadgeClass}">
				<g:if test="${stats.storage[0] > 99}">
					99+
				</g:if>
				<g:else>
					${stats.storage[0]}
				</g:else>
			</span>
		</button>
	</li>
</ul>
