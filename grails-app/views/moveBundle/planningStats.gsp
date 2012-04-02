<%@page import="com.tds.asset.Application;"%>
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=ISO-8859-1" />
<meta name="layout" content="projectHeader" />
<title>Planning Stats</title>
</head>
<body>
	<div class="body">
		<div>
			<h1>Move Planning Dashboard</h1>
			<div>
				<table style="border: 0px; width: 1000px;">
					<thead>
						<th style="background-color: white;">&nbsp;</th>
						<th style="background-color: white;">&nbsp;</th>
						<th style="background-color: white;">
							<g:each in="${moveBundleList}" var="bundle">
								<th style="color: Blue; background-color: white;">
									<b>${bundle.name}</b>
								</th>
							</g:each>
						</th>
						<th style="color: Blue; background-color: white;">To Be Assigned</th>
					</thead>
					<tbody>
						<tr>
							<td style="color: blue"><b>Applications</b></td>
							<td style="color: black"><b>Assigned</b></td>
							<td><g:each in="${appList}" var="appCount">
									<td><b>
											${appCount.count}
									</b></td>
								</g:each></td>
							<td style="color: red;"><b>
									${unassignedAppCount}
							</b></td>
							<td><b>
									${percentageAppCount}%&nbsp;&nbsp;&nbsp;assigned
							</b></td>
						</tr>
						<tr>
							<td style="color: black"><b>
									${applicationCount}
							</b></td>
							<td style="color: grey"><b>Optional</b></td>
							<td><g:each in="${assetList}" var="appCount">
									<td><b>
											${appCount.optional}
									</b></td>
								</g:each></td>
						</tr>
						<tr>
							<td style="color: blue"><b></b></td>
							<td style="color: grey"><b>Potential</b></td>
							<td><g:each in="${assetList}" var="appCount">
									<td><b>
											${appCount.potential}
									</b></td>
								</g:each></td>
						</tr>
						<tr>
							<td colspan="3" height="20"></td>
						</tr>
						<tr>
							<td style="color: blue"><b>Servers</b></td>
							<td style="color: black"><b>Assigned</b></td>
							<td><g:each in="${assetList}" var="assetCount">
									<td><b>
											${assetCount.count}
									</b></td>
								</g:each></td>
							<td style="color: red;"><b>
									${unassignedAssetCount}
							</b></td>
							<td>&nbsp;</td>
						</tr>
						<tr>
							<td style="color: black"><b>
									${physicalCount}&nbsp; Physical
							</b></td>
							<td style="color: grey">&nbsp;</td>
							<td><g:each in="${assetList}" var="assetCount">
									<td><b>
											${assetCount.physicalCount}
									</b></td>
								</g:each></td>
							<td style="color: red;"><b>
									${unassignedPhysialAssetCount}
							</b></td>
							<td><b>
									${percentagePhysicalAssetCount}%&nbsp;&nbsp;&nbsp;assigned
							</b></td>
						</tr>
						<tr>
							<td style="color: black"><b>
									${virtualCount}&nbsp; Virtual
							</b></td>
							<td style="color: grey">&nbsp;</td>
							<td><g:each in="${assetList}" var="assetCount">
									<td><b>
											${assetCount.virtualAssetCount}
									</b></td>
								</g:each></td>
							<td style="color: red;"><b>
									${unassignedVirtualAssetCount}
							</b></td>
							<td><b>
									${percentagevirtualAssetCount}%&nbsp;&nbsp;&nbsp;assigned
							</b></td>
						</tr>

					</tbody>
				</table>
			</div>
			<div style="margin-top: 40px; margin-left: 100px;">
				<div style="float: left;">
					<h3>
						<b>Dependency Resolutions</b>
					</h3>
					<table style="border: 0px;">
						<tr>
							<td style="width: 170px;text-align: right;">App Dependencies</td>
							<td><g:if test="${appDependenciesCount > 0 }">
									${appDependenciesCount} &nbsp;
								${100 - Math.round((pendingAppDependenciesCount/appDependenciesCount)*100)}%&nbsp;
								(${pendingAppDependenciesCount}&nbsp;connections to resolve)
								</g:if></td>
						</tr>
						<tr>
							<td style="width: 170px;text-align: right;">Server Dependencies</td>
							<td><g:if test="${serverDependenciesCount > 0 }">
									${serverDependenciesCount} &nbsp;
								${100 - Math.round((pendingServerDependenciesCount/serverDependenciesCount)*100)}%&nbsp;
								(${pendingServerDependenciesCount}&nbsp;connections to resolve)
								</g:if></td>
						</tr>
						<tr>
							<td style="width: 170px;text-align: right;">
							<g:link controller="assetEntity" action="listComment" params="[projectId:currProjObj?.id]">
								Open Issues</g:link></td>
							<td>${issuesCount}</td>
						</tr>
					</table>
				</div>
				<div style="float: left;margin-left: 50px;">
					<h3>
						<b>Latency Grouping</b>
					</h3>
					<table style="border: 0px; margin-left: 10px;">
						<tr>
							<td style="width: 10px;">${likelyLatencyCount}</td>
							<td>Likely</td>
						</tr>
						<tr>
							<td>${unknownLatencyCount}</td>
							<td>UnKnown</td>
						</tr>
						<tr>
							<td>${unlikelyLatencyCount }</td>
							<td>UnLikely</td>
						</tr>
					</table>
	
				</div>
				<div style="float: left;margin-left: 50px;">
					<h3>
						<b>Appl Evals</b>
					</h3>
					<table style="border: 0px; margin-left: 10px;">
						<tr>
							<td style="width: 10px;">${com.tds.asset.AssetEntity.findAll('from AssetEntity where assetType = ? and project = ? and validation = ? ',['Application', project,'Discovery']).size()}</td>
							<td><g:link controller="application" action="list" params="[validation:'Discovery']">Discovery</g:link></td>
						</tr>
						<tr>
							<td>${com.tds.asset.AssetEntity.findAll('from AssetEntity where assetType = ? and project = ? and validation = ?',['Application', project , 'DependencyReview']).size()}</td>
							<td><g:link controller="application" action="list" params="[validation:'DependencyReview']">DependencyReview</g:link></td>
						</tr>
						<tr>
							<td>${com.tds.asset.AssetEntity.findAll('from AssetEntity where assetType = ? and project = ? and validation = ?',['Application', project , 'DependencyScan']).size()}</td>
							<td><g:link controller="application" action="list" params="[validation:'DependencyScan']">DependencyScan</g:link></td>
						</tr>
						<tr>
							<td>${com.tds.asset.AssetEntity.findAll('from AssetEntity where assetType = ? and project = ? and validation = ?',['Application', project , 'BundleReady']).size()}</td>
							<td><g:link controller="application" action="list" params="[validation:'BundleReady']">BundleReady</g:link></td>
						</tr>
					</table>
	
				</div>
			</div>
			<div style="clear: both;float: left;margin-top: 40px; margin-left: 100px;">
			   <g:render template="dependencyBundleDetails" />
			</div>
		</div>
</body>
</html>