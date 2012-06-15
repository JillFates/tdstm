<%@page import="com.tds.asset.Application;"%>
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=ISO-8859-1" />
<meta name="layout" content="projectHeader" />
<g:javascript src="entity.crud.js" />
<title>Planning Stats</title>
</head>
<body>
	<div class="body">
		<div>
			<h1>Move Planning Dashboard</h1>
			<div style="float:left;margin-top: 10px; margin-left: 5px;width:280px;">
					<h3>
						<b>Discovery</b>
					</h3>
					<table style="float:left; border: 0px; margin-left: 10px;">
						<tr>
							<td style="width: 10px;">${applicationCount}</td>
							<td><g:link controller="application" action="list" params="[validation:'Discovery']">Applications</g:link></td>
						</tr>
						<tr>
							<td style="width: 10px;">${physicalCount}</td>
							<td><g:link controller="assetEntity" action="list" params="[validation:'Discovery']">Physical Servers</g:link></td>
						</tr>
						<tr>
							<td style="width: 10px;">${virtualCount}</td>
							<td><g:link controller="assetEntity" action="list" params="[validation:'Discovery']">Virtual Servers</g:link></td>
						</tr>
					</table>
					<br />
					<h4>
						<b>App Validations</b>
					</h4>
					<table style="float:left; border: 0px; margin-left: 10px;">
						<tr>
							<td style="width: 10px;">${com.tds.asset.AssetEntity.findAll('from AssetEntity as ae where assetType = ? and project = ? and validation = ? and ae.moveBundle.useOfPlanning = ? ',['Application', project,'Discovery',true]).size()}</td>
							<td><g:link controller="application" action="list" params="[validation:'Discovery']">Discovery</g:link></td>
						</tr>
						<tr>
							<td>${com.tds.asset.AssetEntity.findAll('from AssetEntity as ae where assetType = ? and project = ? and validation = ? and ae.moveBundle.useOfPlanning = ?',['Application', project , 'DependencyReview',true]).size()}</td>
							<td><g:link controller="application" action="list" params="[validation:'DependencyReview']">DependencyReview</g:link></td>
						</tr>
						<tr>
							<td>${com.tds.asset.AssetEntity.findAll('from AssetEntity as ae where assetType = ? and project = ? and validation = ? and ae.moveBundle.useOfPlanning = ?',['Application', project , 'DependencyScan',true]).size()}</td>
							<td><g:link controller="application" action="list" params="[validation:'DependencyScan']">DependencyScan</g:link></td>
						</tr>
						<tr>
							<td>${com.tds.asset.AssetEntity.findAll('from AssetEntity as ae where assetType = ? and project = ? and validation = ? and ae.moveBundle.useOfPlanning = ?',['Application', project , 'BundleReady',true]).size()}</td>
							<td><g:link controller="application" action="list" params="[validation:'BundleReady']">BundleReady</g:link></td>
						</tr>
					</table>
	
			</div>

			<div style="float:none;margin-top: 10px; margin-left: 5px;width:280px;">
					<h3>
						<b>Analysis</b>
					</h3>
					<table style="float:left; border: 0px;">
						<tr>
							<td>${100 - Math.round((com.tds.asset.AssetEntity.findAll('from AssetEntity as ae where assetType = ? and project = ? and validation = ? and ae.moveBundle.useOfPlanning = ?',['Application', project , 'BundleReady',true]).size())*100)}%</td>
							<td><g:link controller="application" action="list" params="[validation:'BundleReady']">Applications Ready</g:link></td>
						</tr>
					</table>
				<div style="float:left;">
					<h4>
						<b>Dependencies</b>
					</h4>
					<table style="float:left; border: 0px;">
						<tr>
							<td style="width: 150px;text-align: right;">App Dependencies</td>
							<td><g:if test="${appDependenciesCount > 0 }">
									${appDependenciesCount} &nbsp;
								${100 - Math.round((pendingAppDependenciesCount/appDependenciesCount)*100)}%&nbsp;
								(${pendingAppDependenciesCount}&nbsp; to resolve)
								</g:if></td>
						</tr>
						<tr>
							<td style="width: 150px;text-align: right;">Server Dependencies</td>
							<td><g:if test="${serverDependenciesCount > 0 }">
									${serverDependenciesCount} &nbsp;
								${100 - Math.round((pendingServerDependenciesCount/serverDependenciesCount)*100)}%&nbsp;
								(${pendingServerDependenciesCount}&nbsp; to resolve)
								</g:if></td>
						</tr>
						<tr>
							<td style="width: 150px;text-align: right;">
							<g:link controller="assetEntity" action="listComment" params="[projectId:currProjObj?.id]">
								Open Issues</g:link></td>
							<td>${issuesCount}</td>
						</tr>
					</table>
				</div>
				<div style="float:left; margin-left: 0px;">
					<h4>
						<b>Latency Evals</b>
					</h4>
					<table style="border: 0px; margin-left: 10px;">
						<tr>
							<td style="width: 10px;"><g:link controller="application" action="list" params="[latency:'likely']">${likelyLatency}</g:link></td>
							<td><g:link controller="application" action="list" params="[latency:'likely']">Likely</g:link></td>
						</tr>
						<tr>
							<td><g:link controller="application" action="list" params="[latency:'UnKnown']">${unknownLatency}</g:link></td>
							<td><g:link controller="application" action="list" params="[latency:'UnKnown']">UnKnown</g:link></td>
						</tr>
						<tr>
							<td><g:link controller="application" action="list" params="[latency:'UnLikely']">${unlikelyLatency }</g:link></td>
							<td><g:link controller="application" action="list" params="[latency:'UnLikely']">UnLikely</g:link></td>
						</tr>
					</table>
	
				</div>
			</div>
			<div style="float:right;margin-top: 10px; margin-left: 5px;width:800px;">
					<h3>
						<b>Assignment</b>
					</h3>
				<table style="border: 0px; width: 700px;">
					<thead>
						<th style="background-color: white;">&nbsp;</th>
						<th style="color: Blue; background-color: white;width:45px;">Unassigned</th>
							<g:each in="${moveBundle}" var="bundle">
								<th style="color: Blue; background-color: white;">
									<b>${bundle}</b>
								</th>
							</g:each>
						<th style="background-color: white;">&nbsp;</th>
					</thead>
					<tbody>
						<tr>
							<td style="color: black"><b>Apps</b></td>
							<td style="color: red;"><b>
									${unassignedAppCount}
							</b></td>
							<g:each in="${appList}" var="appCount">
									<td><b>
											${appCount.count}
									</b></td>
								</g:each>
							<td><b>
									${percentageAppCount}%&nbsp;assigned
							</b></td>
						</tr>
						<tr>
							<td style="color: grey"><b>Optional</b></td>
							<td>&nbsp;</td>
							<g:each in="${assetList}" var="appCount">
									<td style="color: grey">
											${appCount.optional}
									</td>
								</g:each>
							<td>&nbsp;</td>
						</tr>
						<tr>
							<td style="color: grey"><b>UnKnown</b></td>
							<td>&nbsp;</td>
							<g:each in="${assetList}" var="appCount">
									<td style="color: grey">
											${appCount.potential}
									</td>
								</g:each>
							<td>&nbsp;</td>
						</tr>
						<tr>
							<td style="color: black"><b>Servers</b></td>
							<td style="color: red;"><b>
									${unassignedAssetCount}
							</b></td>
								<g:each in="${assetList}" var="assetCount">
									<td><b>
											${assetCount.count}
									</b></td>
								</g:each>
							<td>&nbsp;</td>
						</tr>
						<tr>
							<td style="color: black">Physical</td>
							<td style="color: red;"><b>
									${unassignedPhysialAssetCount}
							</b></td>
								<g:each in="${assetList}" var="assetCount">
									<td><b>
											${assetCount.physicalCount}
									</b></td>
								</g:each>
							<td><b>
									${percentagePhysicalAssetCount}%&nbsp;assigned
							</b></td>
						</tr>
						<tr>
							<td style="color: black">Virtual</td>
							<td style="color: red;"><b>
									${unassignedVirtualAssetCount}
							</b></td>
								<g:each in="${assetList}" var="assetCount">
									<td><b>
											${assetCount.virtualAssetCount}
									</b></td>
								</g:each>
							<td><b>
									${percentagevirtualAssetCount}%&nbsp;assigned
							</b></td>
						</tr>

					</tbody>
				</table>
			</div>
			<div style="clear: both;float: left;margin-top: 40px; margin-left: 100px;">
			   <g:render template="dependencyBundleDetails" />
			</div>
</body>
</html>