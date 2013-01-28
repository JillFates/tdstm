<%@page import="com.tds.asset.Application;"%>
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=ISO-8859-1" />
<meta name="layout" content="projectHeader" />
<g:javascript src="entity.crud.js" />
<title>Transition Planning Dashboard</title>
<script type="text/javascript">
$(document).ready(function() { 
	currentMenuId = "#bundleMenu";
	$("#bundleMenuId a").css('background-color','#003366')
});
</script>
</head>
<body>
	<div class="body">
		<div style="width: 1330px !important;">
		    <g:set var="percentageAppToValidate" value="${applicationCount ? Math.round((appToValidate/applicationCount)*100) :100}"/>
		    <g:set var="percentageBundleReady" value="${applicationCount ? Math.round((bundleReady/applicationCount)*100) : 0}"/>
			<g:set var="percentageUnassignedAppCount" value="${applicationCount ? Math.round((unassignedAppCount/applicationCount)*100) :100}"/>
			<h1>Planning Dashboard</h1>
			<div class="dashboard dashboard_div" style="float:left; width:250px;">
					<span class="dashboard_head">Discovery Phase</span>
					<table style="margin-bottom: 10px;border-spacing:0px;">
						<tr>
							<td class="dashboard_bar_base" >
							<g:if test="${percentageAppToValidate == 100}">
								<div class="dashboard_bar_graph0" ><b>0% Applications Validated</b></div>

							</g:if><g:elseif test="${percentageAppToValidate == 0}">

								<div class="task_completed" style="z-index:-1; height:24px; width: 100%"></div>
								<div class="task_completed" style="position:relative; top:-20px;height:0px;margin-left:5px;"><b>100% Applications Validated</b></div>

							</g:elseif><g:else>

								<div class="dashboard_bar_graph" style="width: ${100 - percentageAppToValidate}%"></div>
								<div style="position:relative; top:-18px;height:0px;margin-left:5px;"><b>${100 - percentageAppToValidate}%</b>
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
							<td><g:link controller="application" action="list" params="[filter:'applicationCount']" class="links">Applications</g:link><br />
							<g:if test="${ appToValidate > 0 }">
							     (<g:link controller="application" action="list" params="[filter:'appToValidate']" class="links">${appToValidate} to validate</g:link>)
							</g:if>
							</td>
						</tr>
						<tr>
							<td style="width: 10px;text-align: right;">${physicalCount}</td>
							<td><g:link controller="assetEntity" action="list" params="[filter:'physical']" class="links">Physical Servers</g:link><br />
							<g:if test="${ psToValidate > 0 }">
							     (<g:link controller="assetEntity" action="list" params="[filter:'toValidate', type:'physical']" class="links">${psToValidate} to validate</g:link>)
						    </g:if>
							</td>
						</tr>
						<tr>
							<td style="width: 10px;text-align: right;">${virtualCount}</td>
							<td><g:link controller="assetEntity" action="list" params="[filter:'virtual']" class="links">Virtual Servers</g:link><br />
							<g:if test="${ vsToValidate > 0 }">
							     (<g:link controller="assetEntity" action="list" params="[filter:'toValidate', type:'virtual']" class="links">${vsToValidate} to validate</g:link>)
							</g:if>
							</td>
						</tr>
						<tr>
							<td style="width: 10px;text-align: right;">${dbCount}</td>
							<td><g:link controller="database" action="list" class="links">Databases</g:link><br />
							<g:if test="${ dbToValidate > 0 }">
							     (<g:link controller="database" action="list" params="[filter:'toValidate']" class="links">${dbToValidate} to validate</g:link>)
						    </g:if>
							</td>
						</tr>
						<tr>
							<td style="width: 10px;text-align: right;">${fileCount}</td>
							<td><g:link controller="files" action="list" class="links">Storage</g:link><br />
							<g:if test="${ fileToValidate > 0 }">
							     (<g:link controller="files" action="list" params="[filter:'toValidate']" class="links">${fileToValidate} to validate</g:link>)
						    </g:if>
							</td>
						</tr>
						<tr>
							<td style="width: 10px;text-align: right;">${otherAssetCount}</td>
							<td><g:link controller="assetEntity" action="list" params="[filter:'other']" class="links">Other Assets</g:link><br />
							<g:if test="${ otherToValidate > 0 }">
							     (<g:link controller="assetEntity" action="list" params="[filter:'toValidate', type:'other']" class="links">${otherToValidate} to validate</g:link>)
					        </g:if>
							</td>
						</tr>
					</table>
					<br />
					<h4>
						<b>Discovery Issues</b>
					</h4>
					<table style="float:left; border: 0px; margin-left: 10px;">
						<tr>
							<td style="width: 10px;text-align: right;">${openIssue}</td>
							<td><g:link controller="assetEntity" action="listTasks" params="[filter:'openIssue', tag_f_category:'discovery', section:'dashBoard']" class="links">Open Tasks</g:link></td>
						</tr>
						<g:if test="${dueOpenIssue>0}">
						<tr>
						    <td style="width: 10px;text-align: right;color: red;"><b>${dueOpenIssue}</b></td>
							<td><g:link controller="assetEntity" action="listTasks" params="[filter:'dueOpenIssue']" class="links">Overdue</g:link></td>
						</tr>
						</g:if>
					</table>
			</div>

			<div class="dashboard dashboard_div" style="float:left; width:250px;">
					<span class="dashboard_head">Analysis Phase</span>
					<table style="margin-bottom: 10px;border-spacing:0px;">
						<tr>
							<td class="dashboard_bar_base" >
							<g:if test="${percentageBundleReady == 0}">
								<div class="dashboard_bar_graph0" ><b>0% Applications Ready</b></div>

							</g:if><g:elseif test="${percentageBundleReady == 100}">

								<div class="task_completed" style="z-index:-1; height:24px; width: 100%"></div>
								<div class="task_completed" style="position:relative; top:-20px;height:0px;margin-left:5px;"><b>100% Applications Ready</b></div>

							</g:elseif><g:else>

								<div class="dashboard_bar_graph" style="width: ${percentageBundleReady}%"></div>
								<div style="position:relative; top:-18px;height:0px;margin-left:5px;"><b>${percentageBundleReady}%</b>
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
							<td style="width: 10px;text-align: right;">${validated}</td>
							<td><g:link controller="application" action="list" params="[tag_f_validation:'Validated']" class="links">Validated</g:link></td>
						</tr>
						<tr>
                            <td style="width: 10px;text-align: right;">${dependencyScan}</td>
                            <td><g:link controller="application" action="list" params="[tag_f_validation:'DependencyScan']" class="links">DependencyScan</g:link></td>
                        </tr>
						<tr>
							<td style="width: 10px;text-align: right;">${dependencyReview}</td>
							<td><g:link controller="application" action="list" params="[tag_f_validation:'DependencyReview']" class="links">Dependency Review</g:link></td>
						</tr>
						<tr>
							<td style="width: 10px;text-align: right;">${bundleReady}</td>
							<td><g:link controller="application" action="list" params="[tag_f_validation:'BundleReady']" class="links">Bundle Ready</g:link></td>
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
							<td style="width: 150px;"><g:link controller="assetEntity" action="listTasks" params="[filter:'analysisIssue', justRemaining:0, moveEvent:0]" class="links">Open Tasks</g:link></td>
						</tr>
						<g:if test="${generalOverDue>0}">
						<tr>
						    <td style="width: 10px;text-align: right;color: red;"><b>${generalOverDue}</b></td>
							<td><g:link controller="assetEntity" action="listTasks" params="[filter:'generalOverDue']" class="links">Overdue</g:link></td>
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
							<td style="width: 10px;text-align: right;"><g:link controller="application" action="list" params="[latency:'likely']" class="links">${likelyLatency}</g:link></td>
							<td><g:link controller="application" action="list" params="[latency:'likely']" class="links">Likely</g:link></td>
						</tr>
						<tr>
							<td style="width: 10px;text-align: right;"><g:link controller="application" action="list" params="[latency:'UnKnown']" class="links">${unknownLatency}</g:link></td>
							<td><g:link controller="application" action="list" params="[latency:'UnKnown']" class="links">Unknown</g:link></td>
						</tr>
						<tr>
							<td style="width: 10px;text-align: right;"><g:link controller="application" action="list" params="[latency:'UnLikely']" class="links">${unlikelyLatency }</g:link></td>
							<td><g:link controller="application" action="list" params="[latency:'UnLikely']" class="links">UnLikely</g:link></td>
						</tr>
					</table>
				</div>
			</div>
			<div class="dashboard dashboard_div" style="float:left;">
					<span class="dashboard_head">Assignment and Execution Phases</span>
					<table style="margin-bottom: 10px;border-spacing:0px;">
						<tr>
							<td class="dashboard_bar_base" >
							<g:if test="${percentageUnassignedAppCount == 100}">
								<div class="dashboard_bar_graph0" ><b>0% Applications Assigned</b></div>

							</g:if><g:elseif test="${percentageUnassignedAppCount == 0}">

								<div class="task_completed" style="z-index:-1; height:24px; width: 100%"></div>
								<div class="task_completed" style="position:relative; top:-20px;height:0px;margin-left:5px;"><b>100% Applications Assigned</b></div>

							</g:elseif><g:else>

								<div class="dashboard_bar_graph" style="width: ${100-percentageUnassignedAppCount}%"></div>
								<div style="position:relative; top:-18px;height:0px;margin-left:5px;"><b>${100-percentageUnassignedAppCount}%</b>
									<g:link controller="application" action="list" params="[validation:'BundleReady']">Applications Assigned</g:link>
								</div>
							</g:else>
							</td>
						</tr>
						<tr>
							<td class="dashboard_bar_base" >
							<g:if test="${percentageAppCount == 0}">
								<div class="dashboard_bar_graph0" ><b>0% Applications Moved</b></div>

							</g:if><g:elseif test="${percentageAppCount == 100}">

								<div class="task_completed" style="z-index:-1; height:24px; width: 100%"></div>
								<div class="task_completed" style="position:relative; top:-20px;height:0px;margin-left:5px;"><b>100% Applications Moved</b></div>

							</g:elseif><g:else>

								<div class="dashboard_bar_graph" style="width: ${percentageAppCount}%"></div>
								<div style="position:relative; top:-18px;height:0px;margin-left:5px;"><b>${percentageAppCount}%</b>
									<g:link controller="application" action="list" params="[tag_f_planStatus:'moved']">Applications Moved</g:link>
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
							<g:if test="${unassignedAppCount == 0 }">
								0
							</g:if>
							<g:else>
							<b>

								<g:link controller="application" action="list" params="[tag_f_planStatus:'unassigned']" class="links">
									${unassignedAppCount} (${(percentageUnassignedAppCount > 0 && percentageUnassignedAppCount < 1) ? 1 : Math.round(percentageUnassignedAppCount)}%)
								</g:link>
							</b>
							</g:else>
							</td>
							<g:each in="${appList}" var="appCount">
								<td style="text-align: right;"><b>
									<g:link controller="application" action="list" params="[moveEvent:appCount.moveEvent]" class="links">${appCount.count}</g:link>
								</b></td>
							</g:each>
							<td style="text-align: right;"><b>
								<g:link controller="application" action="list" params="[tag_f_planStatus:'moved']" class="links">${percentageAppCount}%</g:link>
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
									<g:link controller="assetEntity" action="list" params="[tag_f_planStatus:'unassigned']" class="links">${unassignedAssetCount}</g:link>
							</b></td>
								<g:each in="${assetList}" var="assetCount">
									<td style="text-align: right;"><b>
										<g:link controller="assetEntity" action="list" params="[moveEvent:assetCount.moveEvent,filter:'All']" class="links" >${assetCount.count}</g:link>
									</b></td>
								</g:each>
							<td>&nbsp;</td>
						</tr>
						<tr>
							<td style="color: black">Physical</td>
							<td style="text-align: left;">
							<g:set var="percentageUnassignedPhysicalAssetCount" value="${physicalCount ? (unassignedPhysialAssetCount/physicalCount)*100 : 0}"/>
							<g:if test="${unassignedPhysialAssetCount == 0 }">
								0
							</g:if>
							<g:else>
							<b>
								<g:link controller="assetEntity" action="list" params="[tag_f_planStatus:'unassigned']" class="links">
								   ${unassignedPhysialAssetCount} (${(percentageUnassignedPhysicalAssetCount > 0 && percentageUnassignedPhysicalAssetCount < 1) ? 1 : Math.round(percentageUnassignedPhysicalAssetCount)}%)
								</g:link>
							</b>
							</g:else>
							</td>
								<g:each in="${assetList}" var="assetCount">
									<td style="text-align: right;"><b>
											<g:link controller="assetEntity" action="list" params="[moveEvent:assetCount.moveEvent,filter:'physical']" class="links">${assetCount.physicalCount}</g:link>
									</b></td>
								</g:each>
							<td style="text-align: right;"><b>
								<g:link controller="assetEntity" action="list" params="[tag_f_planStatus:'moved']" class="links">${percentagePhysicalAssetCount}%</g:link>
							</b></td>
						</tr>
						<tr>
							<td style="color: black">Virtual</td>
							<td style="text-align: left;">
							<g:set var="percentageUnassignedVirtualCount" value="${virtualCount ? (unassignedVirtualAssetCount/virtualCount)*100 : 0}"/>
							<g:if test="${unassignedVirtualAssetCount == 0 }">
								0
							</g:if>
							<g:else>
							<b>
								<g:link controller="assetEntity" action="list" params="[tag_f_planStatus:'unassigned']" class="links">
								   ${unassignedVirtualAssetCount} (${(percentageUnassignedVirtualCount > 0 && percentageUnassignedVirtualCount < 1) ? 1 : Math.round(percentageUnassignedVirtualCount)}%)
								</g:link>
							</b>
							</g:else>
							</td>
								<g:each in="${assetList}" var="assetCount">
									<td style="text-align: right;"><b>
										<g:link controller="assetEntity" action="list" params="[moveEvent:assetCount.moveEvent,filter:'virtual']" class="links">${assetCount.virtualAssetCount}</g:link>
									</b></td>
								</g:each>
							<td style="text-align: right;"><b>
								<g:link controller="assetEntity" action="list" params="[tag_f_planStatus:'moved',tag_f_assetType:'vm']" class="links">${percentagevirtualAssetCount}%</g:link>
							</b></td>
						</tr>
						<tr>
							<td style="color: black"><b>Databases</b></td>
							<td style=" text-align: left;">
							<g:set var="percentageUnassignedDbCount" value="${dbCount ? (unassignedDbCount/dbCount)*100 : 0}"/>
							<g:if test="${unassignedDbCount == 0 }">
								0
							</g:if>
							<g:else>
							<b>
								<g:link controller="database" action="list" params="[moveEvent:'unAssigned']" class="links">
									${unassignedDbCount} (${(percentageUnassignedDbCount > 0 && percentageUnassignedDbCount < 1) ? 1 : Math.round(percentageUnassignedDbCount)}%)
								</g:link>
							</b>
							</g:else>
							</td>
								<g:each in="${dbList}" var="dbCount">
									<td style="text-align: right;"><b>
										<g:link controller="database" action="list" params="[moveEvent:dbCount.moveEvent,filter:'virtual']" class="links">${dbCount.count}</g:link>
									</b></td>
								</g:each>
							<td style="text-align: right;"><b>
								<g:link controller="database" action="list" params="[tag_f_planStatus:'moved']" class="links">${percentageDBCount}%</g:link>
							</b></td>
						</tr>
						<tr>
							<td style="color: black"><b>Storage</b></td>
							<td style=" text-align: left;">
							<g:set var="percentageUnassignedFilesCount" value="${fileCount ? (unassignedFilesCount/fileCount)*100 : 0}"/>
							<g:if test="${unassignedFilesCount == 0 }">
								0
							</g:if>
							<g:else>
                            <b>
								<g:link controller="files" action="list" params="[moveEvent:'unAssigned']" class="links">
								${unassignedFilesCount} (${(percentageUnassignedFilesCount > 0 && percentageUnassignedFilesCount < 1) ? 1 : Math.round(percentageUnassignedFilesCount)}%)
								</g:link>
							</b>
							</g:else>
							</td>
								<g:each in="${filesList}" var="filesCount">
									<td style="text-align: right;"><b>
										<g:link controller="files" action="list" params="[moveEvent:filesCount.moveEvent]" class="links">${filesCount.count}</g:link>
									</b></td>
								</g:each>
							<td style="text-align: right;"><b>
								<g:link controller="files" action="list" params="[tag_f_planStatus:'moved']" class="links">${percentageFilesCount}%</g:link>
							</b></td>
                        </tr>
                        <tr>
                            <td style="color: black"><b>Other</b></td>
                            <td style=" text-align: left;">
                            <g:set var="percentageUnassignedOtherCount" value="${otherAssetCount ? (unassignedOtherCount/otherAssetCount)*100 : 0}"/>
							<g:if test="${unassignedOtherCount == 0 }">
								0
							</g:if>
							<g:else>
                            <b>
								<g:link controller="assetEntity" action="list" params="[tag_f_planStatus:'unAssigned']" class="links">
								   ${unassignedOtherCount}	(${(percentageUnassignedOtherCount > 0 && percentageUnassignedOtherCount < 1) ? 1 : Math.round(percentageUnassignedOtherCount)}%)
								</g:link>
							</b>
							</g:else>
							</td>
							<g:each in="${otherTypeList}" var="otherCount">
								<td style="text-align: right;"><b>
									<g:link controller="assetEntity" action="list" params="[moveEvent:otherCount.moveEvent,filter:'other']" class="links">${otherCount.count}</g:link>
								</b></td>
							</g:each>
							<td style="text-align: right;"><b>
								<g:link controller="assetEntity" action="list" params="[tag_f_planStatus:'moved']" class="links">${percentageOtherCount}%</g:link>
							</b></td>
						</tr>
						<tr>
                            <td style="color: black"><b>Open Tasks</b></td>
                            <td style=" text-align: left;">
							</td>
							<g:each in="${openTasks}" var="tasks">
								<td style="text-align: right;"><b>
									<g:link controller="assetEntity" action="listTasks" params="[moveEvent:tasks.moveEvent, justRemaining:1]" class="links">${tasks.count}</g:link>
								</b></td>
							</g:each>
							<td style="text-align: right;"></td>
						</tr>
					</tbody>
				</table>
			</div>
			</div>
			</div>
</body>
</html>