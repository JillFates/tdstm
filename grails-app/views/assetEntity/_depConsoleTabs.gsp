<%-- 
	This template is used by the various tabs in the dependency console
--%>
<g:set var="graphTabClass" value="${entity=='graph' ? 'active' : ''}" />
<g:set var="allTabClass" value="${entity=='all' ? 'active' : ''}" />
<g:set var="appTabClass" value="${entity=='apps' ? 'active' : ''}" />
<g:set var="serverTabClass" value="${entity=='server' ? 'active' : ''}" />
<g:set var="dbTabClass" value="${entity=='database' ? 'active' : ''}" />
<g:set var="fileTabClass" value="${entity=='files' ? 'active' : ''}" />
<g:set var="bundle" value="${dependencyBundle=='onePlus' ? '\'onePlus\'': dependencyBundle}" />

<ul id="depConsoleTabsId" class="nav">
	<li id="graphli" class="nav-item" onclick="getList('graph',${bundle})">
		<button class="btn btn-link nav-link ${graphTabClass}">Map</button>
	</li>
	<li id="allli" class="nav-item" onclick="getList('all',${bundle})">
		<button class="btn btn-link nav-link ${allTabClass}">All</button>
	</li>
	<li id="appli" class="nav-item" onclick="getList('apps',${bundle})">
		<button class="btn btn-link nav-link ${appTabClass}">Apps(${stats.app[0]})</button>
	</li>
	<li id="serverli" class="nav-item" onclick="getList('server',${bundle})">
		<button class="btn btn-link nav-link ${serverTabClass}">Servers(${stats.server[0] + stats.vm[0]})</button>
	</li>
	<li id="dbli" class="nav-item" onclick="getList('database',${bundle})">
		<button class="btn btn-link nav-link ${dbTabClass}">Databases(${stats.db[0]})</button>
	</li>
	<li id="fileli" class="nav-item" onclick="getList('files',${bundle})">
		<button class="btn btn-link nav-link ${fileTabClass}">Storage(${stats.storage[0]})</button>
	</li>
</ul>
