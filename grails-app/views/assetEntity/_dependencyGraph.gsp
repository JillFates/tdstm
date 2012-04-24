<div class="tabs">
	<ul>
		<li id="serverli"><a href="javascript:getList('server',${dependencyBundle})"><span>Servers(${assetEntityListSize})</span> </a></li>
		<li id="appli"><a href="javascript:getList('Apps',${dependencyBundle})">Apps(${appDependentListSize})</a></li>
		<li id="dbli"><a href="#item3"><a href="javascript:getList('database',${dependencyBundle})">DB(${dbDependentListSize})</a></li>
		<li id="fileli"><a href="javascript:getList('files',${dependencyBundle})">Files(${filesDependentListSize})</a></li>
		<li id="graphli" class="active"><a href="javascript:getList('graph',${dependencyBundle})">Map</a></li>
	</ul>
	<div class="tabInner">
		<div id="item1">
		<iframe src="../d3/force/force.html" width="1000" height="600"></iframe>
		</div>
	</div>
</div>

