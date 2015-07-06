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

<ul id="depConsoleTabsId">
	<li id="graphli" class="${graphTabClass}"><a href="javascript:getList('graph',${bundle})">Map</a></li>
	<li id="allli" class="${allTabClass}"><a href="javascript:getList('all',${bundle})">All</a></li>
	<li id="appli" class="${appTabClass}"><a href="javascript:getList('apps',${bundle})">Apps(${stats.app[0]})</a></li>
	<li id="serverli" class="${serverTabClass}"><a href="javascript:getList('server',${bundle})">Servers(${stats.server[0] + stats.vm[0]})</a></li>
	<li id="dbli" class="${dbTabClass}"><a href="javascript:getList('database',${bundle})">Databases(${stats.db[0]})</a></li>
	<li id="fileli" class="${fileTabClass}"><a href="javascript:getList('files',${bundle})">Storage(${stats.storage[0]})</a></li>
</ul>