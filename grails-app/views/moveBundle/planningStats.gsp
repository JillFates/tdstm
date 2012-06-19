<%@page import="com.tds.asset.Application;"%>
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=ISO-8859-1" />
<meta name="layout" content="projectHeader" />
<g:javascript src="entity.crud.js" />
<title>Planning Dashboard</title>
</head>
<body>
	<div class="body">
		<div>
			<h1>Move Planning Dashboard</h1>
			<div style="float:left;margin-top: 10px; margin-left: 5px;width:280px;">
					<h3 style="color:#63A242">
						<b>Discovery</b>
					</h3>
					<table style="margin-bottom: 10px;">
						<tr>
							<td><b><g:if test="${applicationCount>0}">${100 - Math.round((com.tds.asset.AssetEntity.findAll('from AssetEntity as ae where assetType = ? and project = ? and validation = ? and ae.moveBundle.useOfPlanning = ?',['Application', project , 'Discovery',true]).size()/applicationCount)*100)}%</g:if></b></td>
							<td><g:link controller="application" action="list" params="[validation:'BundleReady']">Applications Validated</g:link></td>
						</tr>
					</table>
					<h4>
						<b>Totals</b>
					</h4>
					<table style="float:left; border: 0px; margin-left: 10px; margin-bottom: 10px;">
						<tr>
							<td style="width: 10px;text-align: right;">${applicationCount}</td>
							<td><g:link controller="application" action="list" params="[filter:'applicationCount']">Applications</g:link></td>
						</tr>
						<tr>
							<td style="width: 10px;text-align: right;">${physicalCount}</td>
							<td><g:link controller="assetEntity" action="list" params="[filter:'physicalServer']">Physical Servers</g:link></td>
						</tr>
						<tr>
							<td style="width: 10px;text-align: right;">${virtualCount}</td>
							<td><g:link controller="assetEntity" action="list" params="[filter:'virtual']">Virtual Servers</g:link></td>
						</tr>
					</table>
					<br />
					<h4>
						<b>App Validations</b>
					</h4>
					<table style="float:left; border: 0px; margin-left: 10px;">
						<tr>
							<td style="width: 10px;text-align: right;">${com.tds.asset.AssetEntity.findAll('from AssetEntity as ae where assetType = ? and project = ? and validation = ? and ae.moveBundle.useOfPlanning = ? ',['Application', project,'Discovery',true]).size()}</td>
							<td><g:link controller="application" action="list" params="[validation:'Discovery']">Discovery</g:link></td>
						</tr>
						<tr>
							<td style="width: 10px;text-align: right;">${com.tds.asset.AssetEntity.findAll('from AssetEntity as ae where assetType = ? and project = ? and validation = ? and ae.moveBundle.useOfPlanning = ?',['Application', project , 'DependencyScan',true]).size()}</td>
							<td><g:link controller="application" action="list" params="[validation:'DependencyScan']">DependencyScan</g:link></td>
						</tr>
						<tr>
							<td style="width: 10px;text-align: right;">${com.tds.asset.AssetEntity.findAll('from AssetEntity as ae where assetType = ? and project = ? and validation = ? and ae.moveBundle.useOfPlanning = ?',['Application', project , 'Validated',true]).size()}</td>
							<td><g:link controller="application" action="list" params="[validation:'DependencyScan']">Validated</g:link></td>
						</tr>
						<tr>
							<td style="width: 10px;text-align: right;">${com.tds.asset.AssetEntity.findAll('from AssetEntity as ae where assetType = ? and project = ? and validation = ? and ae.moveBundle.useOfPlanning = ?',['Application', project , 'DependencyReview',true]).size()}</td>
							<td><g:link controller="application" action="list" params="[validation:'DependencyReview']">Dependency Review</g:link></td>
						</tr>
						<tr>
							<td style="width: 10px;text-align: right;">${com.tds.asset.AssetEntity.findAll('from AssetEntity as ae where assetType = ? and project = ? and validation = ? and ae.moveBundle.useOfPlanning = ?',['Application', project , 'BundleReady',true]).size()}</td>
							<td><g:link controller="application" action="list" params="[validation:'BundleReady']">Bundle Ready</g:link></td>
						</tr>
					</table>
	
			</div>

			<div style="float:left;margin-top: 10px; margin-left: 5px;width:280px;">
					<h3 style="color:#63A242">
						<b>Analysis</b>
					</h3>
					<table style="margin-bottom: 10px;">
						<tr>
							<td><b><g:if test="${applicationCount>0}">${Math.round((com.tds.asset.AssetEntity.findAll('from AssetEntity as ae where assetType = ? and project = ? and validation = ? and ae.moveBundle.useOfPlanning = ?',['Application', project , 'BundleReady',true]).size()/applicationCount)*100)}%</g:if></b></td>
							<td><g:link controller="application" action="list" params="[validation:'BundleReady']">Applications Ready</g:link></td>
						</tr>
					</table>
				<div style="float:left; margin-left: 0px; margin-top: 10px;">
					<h4>
						<b>Dependencies</b>
					</h4>
					<table style="float:left; border: 0px;">
						<tr>
							<td style="width: 10px;text-align: right;">${appDependenciesCount}</td>
							<td style="width: 150px;">App Dependencies <br />
								<g:if test="${appDependenciesCount > 0 }">
								(${100 - Math.round((pendingAppDependenciesCount/appDependenciesCount)*100)}%,&nbsp;
								${pendingAppDependenciesCount}&nbsp; to resolve)
								</g:if></td>
						</tr>
						<tr>
							<td style="width: 10px;text-align: right;">${serverDependenciesCount}</td>
							<td style="width: 150px;">Server Dependencies <br />
								<g:if test="${serverDependenciesCount > 0 }">
								(${100 - Math.round((pendingServerDependenciesCount/serverDependenciesCount)*100)}%,&nbsp;
								${pendingServerDependenciesCount}&nbsp; to resolve)
								</g:if></td>
						</tr>
						<tr>
							<td style="width: 10px;text-align: right;">${issuesCount}</td>
							<td style="width: 150px;">
							<g:link controller="assetEntity" action="listComment" params="[projectId:currProjObj?.id]">
								Open Issues</g:link></td>
						</tr>
					</table>
				</div>
				<div style="float:left; margin-left: 0px; margin-top: 10px;">
					<h4>
						<b>App Latency Evaluations</b>
					</h4>
					<table style="border: 0px;">
						<tr>
							<td style="width: 10px;text-align: right;"><g:link controller="application" action="list" params="[latency:'likely']">${likelyLatency}</g:link></td>
							<td><g:link controller="application" action="list" params="[latency:'likely']">Likely</g:link></td>
						</tr>
						<tr>
							<td style="width: 10px;text-align: right;"><g:link controller="application" action="list" params="[latency:'UnKnown']">${unknownLatency}</g:link></td>
							<td><g:link controller="application" action="list" params="[latency:'UnKnown']">Unknown</g:link></td>
						</tr>
						<tr>
							<td style="width: 10px;text-align: right;"><g:link controller="application" action="list" params="[latency:'UnLikely']">${unlikelyLatency }</g:link></td>
							<td><g:link controller="application" action="list" params="[latency:'UnLikely']">UnLikely</g:link></td>
						</tr>
					</table>
	
				</div>
			</div>
			<div style="float:right;margin-top: 10px; margin-left: 5px;">
					<h3 style="color:#63A242">
						<b>Assignment</b>
					</h3>
					<table style="margin-bottom: 10px;">
						<tr>
							<td><b>${percentageAppCount}%</b></td>
							<td><g:link controller="application" action="list" params="[validation:'BundleReady']">Applications Assigned</g:link></td>
						</tr>
					</table>
				<table style="border: 0px;">
					<thead>
						<th style="background-color: white;width:80px;">&nbsp;</th>
						<th style="color: Blue; background-color: white;width:45px;text-align: right;"><g:link controller="application" action="list" params="[moveEvent:'unAssigned']">Unassigned</g:link></th>
							<g:each in="${moveBundle}" var="bundle">
								<th style="color: Blue; background-color: white;text-align: center;">
									<b><g:link controller="application" action="list" params="[moveEvent:bundle.id]">${bundle}</g:link></b>
								</th>
							</g:each>
						<th style="background-color: white;">&nbsp;</th>
						<tr>
					    <td style="background-color: white;width:80px;">&nbsp;</td>
					    <th style="background-color: white;width:45px;text-align: right;">&nbsp;</td>
					    <g:each in="${bundleStartDate}" var="startdate">
									<td style="text-align: center;font-size: 10px"><b>
									${startdate}
									</b></td>
						</g:each>
						<td style="background-color: white;">&nbsp;</td>
					    </tr>
					</thead>
					<tbody>
						<tr>
							<td style="color: black"><b>Apps</b></td>
							<td style="color: red;text-align: right;"><b>
									<g:link controller="application" action="list" params="[moveEvent:'unAssigned']"><span style="color: red">${unassignedAppCount}</span></g:link>
							</b></td>
							<g:each in="${appList}" var="appCount">
									<td style="text-align: right;"><b>
											<g:link controller="application" action="list" params="[moveEvent:appCount.moveEvent]">${appCount.count}</g:link>
									</b></td>
								</g:each>
							<td><b>
									${percentageAppCount}%&nbsp;
							</b></td>
						</tr>
						<tr>
							<td style="color: grey">Optional</td>
							<td>&nbsp;</td>
							<g:each in="${assetList}" var="appCount">
									<td style="color: grey; text-align: right;">
											${appCount.optional}
									</td>
								</g:each>
							<td>&nbsp;</td>
						</tr>
						<tr>
							<td style="color: grey">Unknown</td>
							<td>&nbsp;</td>
							<g:each in="${assetList}" var="appCount">
									<td style="color: grey;text-align: right;">
											${appCount.potential}
									</td>
								</g:each>
							<td>&nbsp;</td>
						</tr>
						<tr>
							<td style="color: black"><b>Servers</b></td>
							<td style="color: red; text-align: right;""><b>
									<g:link controller="assetEntity" action="list" params="[moveEvent:'unAssigned',filter:'All']"><span style="color: red">${unassignedAssetCount}</span></g:link>
							</b></td>
								<g:each in="${assetList}" var="assetCount">
									<td style="text-align: right;"><b>
										<g:link controller="assetEntity" action="list" params="[moveEvent:assetCount.moveEvent,filter:'All']">	${assetCount.count}</g:link>
									</b></td>
								</g:each>
							<td>&nbsp;</td>
						</tr>
						<tr>
							<td style="color: black">Physical</td>
							<td style="color: red;text-align: right;""><b>
									<g:link controller="assetEntity" action="list" params="[moveEvent:'unAssigned',filter:'physical']"><span style="color: red">${unassignedPhysialAssetCount}</span></g:link>
							</b></td>
								<g:each in="${assetList}" var="assetCount">
									<td style="text-align: right;"><b>
											<g:link controller="assetEntity" action="list" params="[moveEvent:assetCount.moveEvent,filter:'physical']">${assetCount.physicalCount}</g:link>
									</b></td>
								</g:each>
							<td><b>
									${percentagePhysicalAssetCount}%&nbsp;
							</b></td>
						</tr>
						<tr>
							<td style="color: black">Virtual</td>
							<td style="color: red;text-align: right;""><b>
									<g:link controller="assetEntity" action="list" params="[moveEvent:'unAssigned',filter:'virtual']"><span style="color: red">${unassignedVirtualAssetCount}</span></g:link>
							</b></td>
								<g:each in="${assetList}" var="assetCount">
									<td style="text-align: right;"><b>
										<g:link controller="assetEntity" action="list" params="[moveEvent:assetCount.moveEvent,filter:'virtual']">	${assetCount.virtualAssetCount}</g:link>
									</b></td>
								</g:each>
							<td><b>
									${percentagevirtualAssetCount}%&nbsp;
							</b></td>
						</tr>
						<tr>
							<td style="color: black"><b>Databases</b></td>
							<td style="color: red; text-align: right;""><b>
									<g:link controller="database" action="list" params="[moveEvent:'unAssigned']"><span style="color: red">${unassignedDbCount}</span></g:link>
							</b></td>
								<g:each in="${dbList}" var="dbCount">
									<td style="text-align: right;"><b>
										<g:link controller="database" action="list" params="[moveEvent:dbCount.moveEvent,filter:'virtual']">${dbCount.count}</g:link>
									</b></td>
								</g:each>
							<td><b>${percentageDBCount}%</b></td>
						</tr>
						<tr>
							<td style="color: black"><b>Files</b></td>
							<td style="color: red; text-align: right;""><b>
									<g:link controller="files" action="list" params="[moveEvent:'unAssigned']"><span style="color: red">${unassignedFilesCount}</span></g:link>
							</b></td>
								<g:each in="${filesList}" var="filesCount">
									<td style="text-align: right;"><b>
										<g:link controller="files" action="list" params="[moveEvent:filesCount.moveEvent]">	${filesCount.count}</g:link>
									</b></td>
								</g:each>
							<td><b>${percentageFilesCount}%</b></td>
						</tr>
						<tr>
							<td style="color: black"><b>Other</b></td>
							<td style="color: red; text-align: right;""><b>
									<g:link controller="assetEntity" action="list" params="[moveEvent:'unAssigned',filter:'other']"><span style="color: red">${unassignedOtherCount}</span></g:link>
							</b></td>
								<g:each in="${otherTypeList}" var="otherCount">
									<td style="text-align: right;"><b>
											<g:link controller="assetEntity" action="list" params="[moveEvent:otherCount.moveEvent,filter:'other']">${otherCount.count}</g:link>
									</b></td>
								</g:each>
							<td><b>${percentageOtherCount}%</b></td>
						</tr>
					</tbody>
				</table>
			</div>
			<div style="clear: both;float: left;margin-top: 30px; margin-left: 10px; width:900px; overflow:scroll;">
			   <g:render template="dependencyBundleDetails" />
			</div>
</body>
</html>