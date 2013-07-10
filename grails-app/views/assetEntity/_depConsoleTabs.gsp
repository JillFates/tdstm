<%-- 
	This template is used by the various tabs in the dependency console
--%>
<g:set var="appTabClass" value="${entity=='apps' ? 'active' : ''}" />
<g:set var="serverTabClass" value="${entity=='server' ? 'active' : ''}" />
<g:set var="dbTabClass" value="${entity=='database' ? 'active' : ''}" />
<g:set var="fileTabClass" value="${entity=='files' ? 'active' : ''}" />
<g:set var="graphTabClass" value="${entity=='graph' ? 'active' : ''}" />

<ul>	
	<li id="appli" class="${appTabClass}"><a href="javascript:getList('apps',${dependencyBundle})">Apps(${stats.appCount})</a></li>
	<li id="serverli" class="${serverTabClass}"><a href="javascript:getList('server',${dependencyBundle})">Servers(${stats.serverCount + stats.vmCount})</a></li>
	<li id="dbli" class="${dbTabClass}"><a href="javascript:getList('database',${dependencyBundle})">DB(${stats.dbCount})</a></li>
	<li id="fileli" class="${fileTabClass}"><a href="javascript:getList('files',${dependencyBundle})">Storage(${stats.storageCount})</a></li>
	<li id="graphli" class="${graphTabClass}"><a href="javascript:getList('graph',${dependencyBundle})">Map</a></li>
</ul>