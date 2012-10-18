<%@page import="com.tds.asset.Application;"%>
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=ISO-8859-1" />
<meta name="layout" content="projectHeader" />
<g:javascript src="entity.crud.js" />
<title>Planning Dashboard</title>
<script type="text/javascript">
$(document).ready(function() { 
	currentMenuId = "#bundleMenu";
	$("#bundleMenuId a").css('background-color','#003366')
});
</script>
</head>
<body>
	<div class="body">
		<div>
		    <g:set var="appToValid" value="${applicationCount ? Math.round((appToValidate/applicationCount)*100) : 0}"/>
		    <g:set var="bundleRdy" value="${applicationCount ? Math.round((bundleReady/applicationCount)*100) : 0}"/>
		    
			<h1>Planning Dashboard</h1>
			<div style="float:left;margin-top: 10px; margin-left: 5px;width:250px;">
					<h3 style="color:#63A242">
						<b>Discovery</b>
					</h3>
					<table style="margin-bottom: 10px;border-spacing:0px;">
						<tr>
							<td style="padding:0px; height:24px; background-color: lightyellow;box-shadow: 2px 3px 3px lightgray inset;">
							<g:if test="${applicationCount=0}">
								<div style="position:relative; top:0px;height:0px;margin-left:5px;"><b>0% Applications Validated</b></div>

							</g:if><g:elseif test="${applicationCount=100}">

								<div class="task_completed" style="z-index:-1; height:24px; width: 100%"></div>
								<div class="task_completed" style="position:relative; top:-20px;height:0px;margin-left:5px;"><b>100% Applications Validated</b></div>

							</g:elseif><g:else>

								<div style="background-color:#BFF3A5; z-index:-1; height:24px; border-right-width: 1px;border-color: lightgray;border-right-style: inset; width: ${100 - appToValid}%"></div>
								<div style="position:relative; top:-20px;height:0px;margin-left:5px;"><b>${100 - appToValid}%</b>
									<g:link controller="application" action="list" params="[validation:'Discovery']">Applications Validated</g:link>
								</div>
							</g:else>
							</td>
						</tr>
					</table>
					<h4>
						<b>Total Discovered</b>
					</h4>
					<table style="float:left; border: 0px; margin-left: 10px; margin-bottom: 10px;">
						<tr>
							<td style="width: 10px;text-align: right;">${applicationCount}</td>
							<td><g:link controller="application" action="list" params="[filter:'applicationCount']">Applications</g:link><br />
							(<g:link controller="application" action="list" params="[filter:'appToValidate']">
							${appToValidate} to validate</g:link>)
							</td>
						</tr>
						<tr>
							<td style="width: 10px;text-align: right;">${physicalCount}</td>
							<td><g:link controller="assetEntity" action="list" params="[filter:'physicalServer']">Physical Servers</g:link></td>
						</tr>
						<tr>
							<td style="width: 10px;text-align: right;">${virtualCount}</td>
							<td><g:link controller="assetEntity" action="list" params="[filter:'virtual']">Virtual Servers</g:link></td>
						</tr>
						<tr>
							<td style="width: 10px;text-align: right;">${dbCount}</td>
							<td><g:link controller="database" action="list" >Databases</g:link></td>
						</tr>
						<tr>
							<td style="width: 10px;text-align: right;">${fileCount}</td>
							<td><g:link controller="files" action="list" >Files</g:link></td>
						</tr>
						<tr>
							<td style="width: 10px;text-align: right;">${otherAssetCount}</td>
							<td><g:link controller="assetEntity" action="list" params="[filter:'otherAsset']">Other Assets</g:link></td>
						</tr>
					</table>
					<br />
					<h4>
						<b>Discovery Issues</b>
					</h4>
					<table style="float:left; border: 0px; margin-left: 10px;">
						<tr>
							<td style="width: 10px;text-align: right;">${openIssue}</td>
							<td><g:link controller="assetEntity" action="listTasks" params="[filter:'Discovery']">Open Tasks</g:link></td>
						</tr>
						<g:if test="${dueOpenIssue>0}">
						<tr>
						    <td style="width: 10px;text-align: right;color: red;"><b>${dueOpenIssue}</b></td>
							<td><g:link controller="assetEntity" action="listTasks" params="[filter:'dueOpenIssue']">Overdue</g:link></td>
						</tr>
						</g:if>
					</table>
	
			</div>

			<div style="float:left;margin-top: 10px; margin-left: 5px;width:250px;">
					<h3 style="color:#63A242">
						<b>Analysis</b>
					</h3>
					<table style="margin-bottom: 10px;border-spacing:0px;">
						<tr>
							<td style="padding:0px; height:24px; background-color: lightyellow;box-shadow: 2px 3px 3px lightgray inset;">
							<g:if test="${bundleRdy=0}">
								<div style="position:relative; top:0px;height:0px;margin-left:5px;"><b>0% Applications Ready</b></div>

							</g:if><g:elseif test="${bundleRdy=100}">

								<div class="task_completed" style="z-index:-1; height:24px; width: 100%"></div>
								<div class="task_completed" style="position:relative; top:-20px;height:0px;margin-left:5px;"><b>100% Applications Ready</b></div>

							</g:elseif><g:else>

								<div style="background-color:#BFF3A5; z-index:-1; height:24px; border-right-width: 1px;border-color: lightgray;border-right-style: inset; width: ${bundleRdy}%"></div>
								<div style="position:relative; top:-20px;height:0px;margin-left:5px;"><b>${bundleRdy}%</b>
									<g:link controller="application" action="list" params="[validation:'BundleReady']">Applications Ready</g:link>
								</div>
							</g:else>
							</td>
						</tr>
					</table>
				<div style="float:left; margin-left: 0px; margin-top: 10px;">
					<h4>
						<b>App Reviews</b>
					</h4>
					<table style="float:left; border: 0px; margin-left: 10px;">
						<tr>
							<td style="width: 10px;text-align: right;">${dependencyScan}</td>
							<td><g:link controller="application" action="list" params="[tag_f_validation:'DependencyScan']">DependencyScan</g:link></td>
						</tr>
						<tr>
							<td style="width: 10px;text-align: right;">${validated}</td>
							<td><g:link controller="application" action="list" params="[tag_f_validation:'Validated']">Validated</g:link></td>
						</tr>
						<tr>
							<td style="width: 10px;text-align: right;">${dependencyReview}</td>
							<td><g:link controller="application" action="list" params="[tag_f_validation:'DependencyReview']">Dependency Review</g:link></td>
						</tr>
						<tr>
							<td style="width: 10px;text-align: right;">${bundleReady}</td>
							<td><g:link controller="application" action="list" params="[tag_f_validation:'BundleReady']">Bundle Ready</g:link></td>
						</tr>
					</table>
				</div>
				<div style="float:left; margin-left: 0px; margin-top: 10px;">
					<h4>
						<b>Dependencies</b>
					</h4>
					<table style="float:left; border: 0px;">
						<tr>
							<td style="width: 10px;text-align: right;">${pendingAppDependenciesCount}</td>
							<td style="width: 150px;">App Dependencies to validate<br />
								<g:if test="${appDependenciesCount > 0 }">
								(${appDependenciesCount ? Math.round((pendingAppDependenciesCount/appDependenciesCount)*100) : 0}% of the
								${appDependenciesCount} total)
								</g:if></td>
						</tr>
						<tr>
							<td style="width: 10px;text-align: right;">${pendingServerDependenciesCount}</td>
							<td style="width: 150px;">Server Dependencies to validate<br />
								<g:if test="${serverDependenciesCount > 0 }">
								(${serverDependenciesCount ? Math.round((pendingServerDependenciesCount/serverDependenciesCount)*100) : 0}% of the
								 ${serverDependenciesCount} total)
								</g:if></td>
						</tr>
						<tr>
							<td style="width: 10px;text-align: right;">${issuesCount}</td>
							<td style="width: 150px;"><g:link controller="assetEntity" action="listTasks" params="[filter:'openIssue']">Open Tasks</g:link></td>
						</tr>
						<g:if test="${generalOverDue>0}">
						<tr>
						    <td style="width: 10px;text-align: right;color: red;"><b>${generalOverDue}</b></td>
							<td><g:link controller="assetEntity" action="listTasks" params="[filter:'generalOverDue']">Overdue</g:link></td>
						</tr>
						</g:if>
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
						<b>Assignment and Execution</b>
					</h3>
					<table style="margin-bottom: 10px;border-spacing:0px;">
						<tr>
							<td style="padding:0px; height:24px; background-color: lightyellow;box-shadow: 2px 3px 3px lightgray inset;">
							<g:if test="${percentageAppCount=0}">
								<div style="position:relative; top:0px;height:0px;margin-left:5px;"><b>0% Applications Assigned</b></div>

							</g:if><g:elseif test="${percentageAppCount=100}">

								<div class="task_completed" style="z-index:-1; height:24px; width: 100%"></div>
								<div class="task_completed" style="position:relative; top:-20px;height:0px;margin-left:5px;"><b>100% Applications Assigned</b></div>

							</g:elseif><g:else>

								<div style="background-color:#BFF3A5; z-index:-1; height:24px; border-right-width: 1px;border-color: lightgray;border-right-style: inset; width: ${percentageAppCount}%"></div>
								<div style="position:relative; top:-20px;height:0px;margin-left:5px;"><b>${percentageAppCount}%</b>
									<g:link controller="application" action="list" params="[validation:'BundleReady']">Applications Assigned</g:link>
								</div>
							</g:else>
							</td>
						</tr>
					</table>
				<table style="border: 0px;">
					<thead>
						<tr>
							<th style="background-color: white;width:80px;">&nbsp;</th>
							<th style="background-color: white;width:80px;">&nbsp;</th>
							<g:each in="${moveBundle}" var="bundle">
								<th style="color: Blue; background-color: white;text-align: center;">
									<b><g:link controller="application" action="list" params="[moveEvent:bundle.id]">${bundle}</g:link></b>
								</th>
							</g:each>
							<th style="background-color: white;">Done</th>
						</tr>
						<tr>
							<td style="background-color: white;width:80px;">&nbsp;</td>
							<td style="color: Blue; background-color: white;width:45px;text-align: left;"><g:link controller="application" action="list" params="[moveEvent:'unAssigned']">To be</g:link></td>
							<g:each in="${bundleStartDate}" var="startdate">
								<td style="text-align: right;font-size: 10px"><b>${startdate}</b></td>
							</g:each>
							<td style="background-color: white;">&nbsp;</td>
						</tr>
						<tr>
							<td style="background-color: white;width:80px;">&nbsp;</td>
							<td style="color: Blue; background-color: white;width:45px;text-align: left;"><g:link controller="application" action="list" params="[moveEvent:'unAssigned']">Assigned</g:link></td>
							<g:each in="${moveBundle}" var="bundle">
								<td style="text-align: center;font-size: 10px"><b> ${bundle.runbookStatus ?: ''}</b></td>
							</g:each>
						</tr>
					</thead>
					<tbody>
						<tr>
							<td style="color: black"><b>Apps</b></td>
							<td style="text-align: left;">
							<g:set var="unassinAppCount" value="${applicationCount ? (unassignedAppCount/applicationCount)*100 : 0}"/>
							<g:if test="${unassignedAppCount = 0 }">
								0
							</g:if>
							<g:else>
							<b>

								<g:link controller="application" action="list" params="[tag_f_planStatus:'unassigned']">
									${unassignedAppCount} (${(unassinAppCount > 0 && unassinAppCount < 1) ? 1 : Math.round(unassinAppCount)}%)
								</g:link>
							</b>
							</g:else>
							</td>
							<g:each in="${appList}" var="appCount">
								<td style="text-align: right;"><b>
									<g:link controller="application" action="list" params="[moveEvent:appCount.moveEvent]">${appCount.count}</g:link>
								</b></td>
							</g:each>
							<td style="text-align: right;"><b>
								<g:link controller="application" action="list" params="[tag_f_planStatus:'moved']">${percentageAppCount}%</g:link>
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
							<td style="text-align: left;"><b>
									<g:link controller="assetEntity" action="list" params="[moveEvent:'unAssigned',filter:'All']">${unassignedAssetCount}</g:link>
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
							<td style="text-align: left;">
							<g:set var="unassinPhyCount" value="${physicalCount ? (unassignedPhysialAssetCount/physicalCount)*100 : 0}"/>
							<g:if test="${unassignedPhysialAssetCount = 0 }">
								0
							</g:if>
							<g:else>
							<b>
								<g:link controller="assetEntity" action="list" params="[moveEvent:'unAssigned',filter:'physical']">
								   ${unassignedPhysialAssetCount} (${(unassinPhyCount > 0 && unassinPhyCount < 1) ? 1 : Math.round(unassinPhyCount)}%)
								</g:link>
							</b>
							</g:else>
							</td>
								<g:each in="${assetList}" var="assetCount">
									<td style="text-align: right;"><b>
											<g:link controller="assetEntity" action="list" params="[moveEvent:assetCount.moveEvent,filter:'physical']">${assetCount.physicalCount}</g:link>
									</b></td>
								</g:each>
							<td style="text-align: right;"><b>
								<g:link controller="assetEntity" action="list" params="[tag_f_planStatus:'moved']">${percentagePhysicalAssetCount}%</g:link>
							</b></td>
						</tr>
						<tr>
							<td style="color: black">Virtual</td>
							<td style="text-align: left;">
							<g:set var="unassinVirtualCount" value="${virtualCount ? (unassignedVirtualAssetCount/virtualCount)*100 : 0}"/>
							<g:if test="${unassignedVirtualAssetCount = 0 }">
								0
							</g:if>
							<g:else>
							<b>
								<g:link controller="assetEntity" action="list" params="[moveEvent:'unAssigned',filter:'virtual']">
								   ${unassignedVirtualAssetCount} (${(unassinVirtualCount > 0 && unassinVirtualCount < 1) ? 1 : Math.round(unassinVirtualCount)}%)
								</g:link>
							</b>
							</g:else>
							</td>
								<g:each in="${assetList}" var="assetCount">
									<td style="text-align: right;"><b>
										<g:link controller="assetEntity" action="list" params="[moveEvent:assetCount.moveEvent,filter:'virtual']">	${assetCount.virtualAssetCount}</g:link>
									</b></td>
								</g:each>
							<td style="text-align: right;"><b>
								<g:link controller="assetEntity" action="list" params="[tag_f_planStatus:'moved',tag_f_assetType:'vm']">${percentagevirtualAssetCount}%</g:link>
							</b></td>
						</tr>
						<tr>
							<td style="color: black"><b>Databases</b></td>
							<td style=" text-align: left;">
							<g:set var="unassinDbCount" value="${dbCount ? (unassignedDbCount/dbCount)*100 : 0}"/>
							<g:if test="${unassignedDbCount = 0 }">
								0
							</g:if>
							<g:else>
							<b>
								<g:link controller="database" action="list" params="[moveEvent:'unAssigned']">
									${unassignedDbCount} (${(unassinDbCount > 0 && unassinDbCount < 1) ? 1 : Math.round(unassinDbCount)}%)
								</g:link>
							</b>
							</g:else>
							</td>
								<g:each in="${dbList}" var="dbCount">
									<td style="text-align: right;"><b>
										<g:link controller="database" action="list" params="[moveEvent:dbCount.moveEvent,filter:'virtual']">${dbCount.count}</g:link>
									</b></td>
								</g:each>
							<td style="text-align: right;"><b>
								<g:link controller="database" action="list" params="[tag_f_planStatus:'moved']">${percentageDBCount}%</g:link>
							</b></td>
						</tr>
						<tr>
							<td style="color: black"><b>Files</b></td>
							<td style=" text-align: left;">
							<g:set var="unassinFilesCount" value="${fileCount ? (unassignedFilesCount/fileCount)*100 : 0}"/>
							<g:if test="${unassignedFilesCount = 0 }">
								0
							</g:if>
							<g:else>
                            <b>
								<g:link controller="files" action="list" params="[moveEvent:'unAssigned']">
								${unassignedFilesCount} (${(unassinFilesCount > 0 && unassinFilesCount < 1) ? 1 : Math.round(unassinFilesCount)}%)
								</g:link>
							</b>
							</g:else>
							</td>
								<g:each in="${filesList}" var="filesCount">
									<td style="text-align: right;"><b>
										<g:link controller="files" action="list" params="[moveEvent:filesCount.moveEvent]">	${filesCount.count}</g:link>
									</b></td>
								</g:each>
							<td style="text-align: right;"><b>
								<g:link controller="files" action="list" params="[tag_f_planStatus:'moved']">${percentageFilesCount}%</g:link>
							</b></td>
                        </tr>
                        <tr>
                            <td style="color: black"><b>Other</b></td>
                            <td style=" text-align: left;">
                            <g:set var="unassinOtherCount" value="${otherAssetCount ? (unassignedOtherCount/otherAssetCount)*100 : 0}"/>
							<g:if test="${unassignedOtherCount = 0 }">
								0
							</g:if>
							<g:else>
                            <b>
								<g:link controller="assetEntity" action="list" params="[moveEvent:'unAssigned',filter:'other']">
								   ${unassignedOtherCount}	(${(unassinOtherCount > 0 && unassinOtherCount < 1) ? 1 : Math.round(unassinOtherCount)}%)
								</g:link>
							</b>
							</g:else>
							</td>
							<g:each in="${otherTypeList}" var="otherCount">
								<td style="text-align: right;"><b>
									<g:link controller="assetEntity" action="list" params="[moveEvent:otherCount.moveEvent,filter:'other']">${otherCount.count}</g:link>
								</b></td>
							</g:each>
							<td style="text-align: right;"><b>
								<g:link controller="assetEntity" action="list" params="[tag_f_planStatus:'moved']">${percentageOtherCount}%</g:link>
							</b></td>
						</tr>
					</tbody>
				</table>
			</div>
			</div>
			</div>
</body>
</html>