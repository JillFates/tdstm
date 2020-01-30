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
	<li id="graphli" class="${graphTabClass} pointer" onclick="getList('graph',${bundle})"><a>Map</a></li>
	<li id="allli" class="${allTabClass} pointer" onclick="getList('all',${bundle})"><a>All</a></li>
	<li id="appli" class="${appTabClass} pointer" onclick="getList('apps',${bundle})"><a>Apps(${stats.app[0]})</a></li>
	<li id="serverli" class="${serverTabClass} pointer" onclick="getList('server',${bundle})"><a>Servers(${stats.server[0] + stats.vm[0]})</a></li>
	<li id="dbli" class="${dbTabClass} pointer" onclick="getList('database',${bundle})"><a>Databases(${stats.db[0]})</a></li>
	<li id="fileli" class="${fileTabClass} pointer" onclick="getList('files',${bundle})"><a>Storage(${stats.storage[0]})</a></li>
</ul>
