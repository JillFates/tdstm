<%@page import="com.tds.asset.Application;"%>
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=ISO-8859-1" />
<meta name="layout" content="projectHeader" />
<g:javascript src="entity.crud.js" />
<title>Transition Planning Dashboard</title>
<script type="text/javascript">
$(document).ready(function() { 
	currentMenuId = "#dashboardMenu";
	$("#dashboardMenuId a").css('background-color','#003366')
	
	var percentageAppToValidate=100-"${applicationCount ? Math.round((appToValidate/applicationCount)*100) :100}";
	$("#discoverybar").animate({width: percentageAppToValidate+"%" }, 1000);
	$("#applicationbar").animate({width: percentageAppToValidate+"%" }, 1000);
	
	var percentageBundleReady="${applicationCount ? Math.round((bundleReady/applicationCount)*100) : 0}";
	$("#analysisbar").animate({width: percentageBundleReady+"%" }, 1000);
	
	$("#confirmedbar").animate({width: "${confirmedAppCount}%" }, 1000);
	
	$("#appmovedbar").animate({width: "${movedAppCount}%" }, 1000);

	$("#assignmentbar").animate({width: "${assignedAppCount}%"}, 1000);
	
	var percentagePSToValidate=100-"${physicalCount ? Math.round((psToValidate/physicalCount)*100) :100}";
	$("#physicalbar").animate({width: percentagePSToValidate+"%" }, 1000);
	
	var percentageVMToValidate=100-"${virtualCount ? Math.round((vsToValidate/virtualCount)*100) :100}";
	$("#virtualbar").animate({width: percentageVMToValidate+"%" }, 1000);
	
	var percentageDBToValidate=100-"${dbCount ? Math.round((dbToValidate/dbCount)*100) :100}";
	$("#dbbar").animate({width: percentageDBToValidate+"%" }, 1000);
	
	var percentageStorToValidate=100-"${fileCount ? Math.round((fileToValidate/fileCount)*100) :100}";
	$("#filebar").animate({width: percentageStorToValidate+"%" }, 1000);
	
	var percentageOtherToValidate=100-"${otherAssetCount ? Math.round((otherToValidate/otherAssetCount)*100) :100}";
	$("#assetbar").animate({width: percentageOtherToValidate+"%" }, 1000);
});
</script>
</head>
<body>
	<div class="body">
		<div>
		    <g:set var="percentageAppToValidate" value="${applicationCount ? Math.round((appToValidate/applicationCount)*100) :100}" />
		    <g:set var="percentagePSToValidate" value="${physicalCount ? Math.round((psToValidate/physicalCount)*100) :100}" />
		    <g:set var="percentageVMToValidate" value="${virtualCount ? Math.round((vsToValidate/virtualCount)*100) :100}" />
		    <g:set var="percentageDBToValidate" value="${dbCount ? Math.round((dbToValidate/dbCount)*100) :100}" />
		    <g:set var="percentageStorToValidate" value="${fileCount ? Math.round((fileToValidate/fileCount)*100) :100}" />
		    <g:set var="percentageOtherToValidate" value="${otherAssetCount ? Math.round((otherToValidate/otherAssetCount)*100) :100}" />

		    <g:set var="percentageBundleReady" value="${applicationCount ? Math.round((bundleReady/applicationCount)*100) : 0}" />
			<g:set var="percentageUnassignedAppCount" value="${applicationCount ? Math.round((unassignedAppCount/applicationCount)*100) :100}" />
			<h1>Planning Dashboard</h1>
			<div class="dashboard_div" style="float:left; width:250px;">
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

								<div class="dashboard_bar_graph" id="discoverybar" style="width:0%;"></div>
								<div style="position:relative; top:-18px;height:0px;margin-left:5px;"><b>${100 - percentageAppToValidate}%</b>
									<g:link controller="application" action="list" params="[filter:'applicationCount', toValidate:'Discovery']">Applications Validated</g:link>
								</div>
							</g:else>
							</td>
						</tr>
					</table>
					<h4>
						<b>Total Discovered</b>
					</h4>
					<table class="dashboard_stat_table">
						<tr>
							<g:render template="discoveryGraph" model="[assetCount:applicationCount,filter:'applicationCount',assetType:'application',title:'Applications',validate:appToValidate,barId:'applicationbar',iconName:'iconApp.png']" ></g:render>
						</tr>
						<tr>
							<g:render template="discoveryGraph" model="[assetCount:physicalCount,filter:'physical',assetType:'assetEntity',title:'Physical Servers',validate:psToValidate,barId:'physicalbar',iconName:'iconServer.png']" ></g:render>
						</tr>
						<tr>
							<g:render template="discoveryGraph" model="[assetCount:virtualCount,filter:'virtual',assetType:'assetEntity',title:'Virtual Servers',validate:vsToValidate,barId:'virtualbar',iconName:'iconServer.png']" ></g:render>
						</tr>
						<tr>
							<g:render template="discoveryGraph" model="[assetCount:dbCount,filter:'db',assetType:'database',title:'Databases',validate:dbToValidate,barId:'dbbar',iconName:'iconDB.png']" ></g:render>
						</tr>
						<tr>
							<g:render template="discoveryGraph" model="[assetCount:fileCount,filter:'storage',assetType:'files',title:'Storage',validate:fileToValidate,barId:'filebar',iconName:'iconStorage.png']" ></g:render>
						</tr>
						<tr>
							<g:render template="discoveryGraph" model="[assetCount:otherAssetCount,filter:'other',assetType:'assetEntity',title:'Other Assets',validate:otherToValidate,barId:'assetbar',iconName:'iconNetwork.png']" ></g:render>
						</tr>
					</table>
					<br />
					<h4>
						<b>Discovery Issues</b>
					</h4>
					<table class="dashboard_stat_table">
						<tr>
							<td class="dashboard_stat_td">
								<g:link controller="assetEntity" action="listTasks" params="[filter:'openIssue', moveEvent:'0', justRemaining:1]" class="links">${openIssue}</g:link>
							</td>
							<td>
								<g:link controller="assetEntity" action="listTasks" params="[filter:'openIssue', moveEvent:'0', justRemaining:1]" class="links">Active Tasks</g:link>
							</td>
						</tr>
						<g:if test="${dueOpenIssue>0}">
						<tr>
						    <td class="dashboard_stat_td" style="color: red;"><b>${dueOpenIssue}</b></td>
							<td><g:link controller="assetEntity" action="listTasks" params="[filter:'dueOpenIssue', moveEvent:'0', justRemaining:1]" class="links">Overdue</g:link></td>
						</tr>
						</g:if>
					</table>
			</div>
			<div class="dashboard_div" style="float:left; width:250px;">
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

								<div class="dashboard_bar_graph" id="analysisbar" style="width:0%;"></div>
								<div style="position:relative; top:-18px;height:0px;margin-left:5px;"><b>${percentageBundleReady}%</b>
									<g:link controller="application" action="list" params="[filter:'applicationCount', toValidate:'BundleReady']">Applications Ready</g:link>
								</div>
							</g:else>
							</td>
						</tr>
					</table>
					<h4>
						<b>Application Review Status</b>
					</h4>
					<table class="dashboard_stat_table">
						<tr>
							<td class="dashboard_stat_td">
								<g:link controller="application" action="list" params="[filter:'applicationCount', toValidate:'Validated']" class="links">${validated}</g:link>
							</td>
							<td>
								<g:link controller="application" action="list" params="[filter:'applicationCount', toValidate:'Validated']" class="links">Validated</g:link>
							</td>
						</tr>
						<tr>
                            <td class="dashboard_stat_td">
                            	<g:link controller="application" action="list" params="[filter:'applicationCount', toValidate:'DependencyScan']" class="links">${dependencyScan}</g:link>
                            </td>
                            <td>
                            	<g:link controller="application" action="list" params="[filter:'applicationCount', toValidate:'DependencyScan']" class="links">Dependency Scan</g:link>
                            </td>
                        </tr>
						<tr>
							<td class="dashboard_stat_td">
								<g:link controller="application" action="list" params="[filter:'applicationCount', toValidate:'DependencyReview']" class="links">${dependencyReview}</g:link>
							</td>
							<td>
								<g:link controller="application" action="list" params="[filter:'applicationCount', toValidate:'DependencyReview']" class="links">Dependency Review</g:link>
							</td>
						</tr>
						<tr>
							<td class="dashboard_stat_td">
								<g:link controller="application" action="list" params="[filter:'applicationCount', toValidate:'BundleReady']" class="links">${bundleReady}</g:link>
							</td>
							<td>
								<g:link controller="application" action="list" params="[filter:'applicationCount', toValidate:'BundleReady']" class="links">Ready</g:link>
							</td>
						</tr>
					</table>
					<br />
					<h4>
						<b>Dependencies</b>
					</h4>
					<table class="dashboard_stat_table">
						<tr>
							<td class="dashboard_stat_td">${pendingAppDependenciesCount}</td>
							<td>App Dependencies to validate<br />
								<g:if test="${appDependenciesCount > 0 }">
								(${appDependenciesCount ? Math.round((pendingAppDependenciesCount/appDependenciesCount)*100) : 0}% of the
								${appDependenciesCount} total)
								</g:if>
							</td>
						</tr>
						<tr>
							<td class="dashboard_stat_td">${pendingServerDependenciesCount}</td>
							<td>Server Dependencies to validate<br />
								<g:if test="${serverDependenciesCount > 0 }">
								(${serverDependenciesCount ? Math.round((pendingServerDependenciesCount/serverDependenciesCount)*100) : 0}% of the
								 ${serverDependenciesCount} total)
								</g:if>
							</td>
						</tr>
						<tr>
							<td class="dashboard_stat_td">
								<g:link controller="assetEntity" action="listTasks" params="[filter:'analysisIssue', justRemaining:0, moveEvent:0]" class="links">${issuesCount}</g:link>
							</td>
							<td>
								<g:link controller="assetEntity" action="listTasks" params="[filter:'analysisIssue', justRemaining:0, moveEvent:0]" class="links">Active Tasks</g:link>
							</td>
						</tr>
						<g:if test="${generalOverDue>0}">
						<tr>
						    <td class="dashboard_stat_td" style="color: red;"><b>${generalOverDue}</b></td>
							<td>
								<g:link controller="assetEntity" action="listTasks" params="[filter:'generalOverDue', justRemaining:1, moveEvent:0]" class="links">Overdue</g:link>
							</td>
						</tr>
						</g:if>
					</table>
					<br />
					<h4>
						<b>Application Latency Evaluations</b>
					</h4>
					<table class="dashboard_stat_table">
						<tr>
							<td class="dashboard_stat_td"><g:link controller="application" action="list" params="[filter:'applicationCount', latencys:'N']" class="links">${likelyLatency}</g:link></td>
							<td><g:link controller="application" action="list" params="[filter:'applicationCount', latencys:'N']" class="links">Likely</g:link></td>
						</tr>
						<tr>
							<td class="dashboard_stat_td"><g:link controller="application" action="list" params="[filter:'applicationCount',latencys:'unknown']" class="links">${unknownLatency}</g:link></td>
							<td><g:link controller="application" action="list" params="[filter:'applicationCount',latencys:'unknown']" class="links">Unknown</g:link></td>
						</tr>
						<tr>
							<td class="dashboard_stat_td"><g:link controller="application" action="list" params="[filter:'applicationCount',latencys:'Y']" class="links">${unlikelyLatency }</g:link></td>
							<td><g:link controller="application" action="list" params="[filter:'applicationCount',latencys:'Y']" class="links">UnLikely</g:link></td>
						</tr>
					</table>
			</div>
			<div class="dashboard_div" style="float:left;">
					<span class="dashboard_head">Execution Phase</span>
					<table style="margin-bottom: 10px;border-spacing:0px;">
						<tr>
							<td class="dashboard_bar_base" >
							<g:if test="${assignedAppCount == 0}">
								<div class="dashboard_bar_graph0" ><b>0% Applications Assigned</b></div>

							</g:if><g:elseif test="${assignedAppCount == 100}">

								<div class="task_completed" style="z-index:-1; height:24px; width: 100%"></div>
								<div class="task_completed" style="position:relative; top:-20px;height:0px;margin-left:5px;"><b>100% Applications Assigned</b></div>

							</g:elseif><g:else>

								<div class="dashboard_bar_graph" id="assignmentbar" style="width:0%;"></div>
								<div style="position:relative; top:-18px;height:0px;margin-left:5px;"><b>${assignedAppCount}%</b>
									<g:link controller="application" action="list" params="[filter:'applicationCount',plannedStatus:'Assigned']">Applications Assigned</g:link>
								</div>
							</g:else>
							</td>
						</tr>
						<tr>
							<td class="dashboard_bar_base" >
							<g:if test="${confirmedAppCount == 0}">
								<div class="dashboard_bar_graph0" ><b>0% Applications Confirmed</b></div>

							</g:if><g:elseif test="${confirmedAppCount == 100}">

								<div class="task_completed" style="z-index:-1; height:24px; width: 100%"></div>
								<div class="task_completed" style="position:relative; top:-20px;height:0px;margin-left:5px;"><b>100% Applications Confirmed</b></div>

							</g:elseif><g:else>

								<div class="dashboard_bar_graph" id="confirmedbar" style="width:0%;"></div>
								<div style="position:relative; top:-18px;height:0px;margin-left:5px;"><b>${confirmedAppCount}%</b>
									<g:link controller="application" action="list" params="[filter:'applicationCount',plannedStatus:'Confirmed']">Applications Confirmed</g:link>
								</div>
							</g:else>
							</td>
						</tr>
						
						<tr>
							<td class="dashboard_bar_base" >
							<g:if test="${movedAppCount == 0}">
								<div class="dashboard_bar_graph0" ><b>0% Applications Moved</b></div>

							</g:if><g:elseif test="${movedAppCount == 100}">

								<div class="task_completed" style="z-index:-1; height:24px; width: 100%"></div>
								<div class="task_completed" style="position:relative; top:-20px;height:0px;margin-left:5px;"><b>100% Applications Moved</b></div>

							</g:elseif><g:else>

								<div class="dashboard_bar_graph" id="appmovedbar"style="width:0%;"></div>
								<div style="position:relative; top:-18px;height:0px;margin-left:5px;"><b>${movedAppCount}%</b>
						 			 <g:link controller="application" action="list" params="[filter:'applicationCount', plannedStatus:'Moved']">Applications Moved</g:link>
								</div>
							</g:else>
							</td>
						</tr>
					</table>
				<table class="dashboard_stat_table">
					<thead>
						<tr>
							<th class="dashboard_stat_icon_td">&nbsp;</th>
							<th class="dashboard_stat_exec_td">&nbsp;</th>
							<th class="dashboard_stat_exec_td">&nbsp;</th>
							<g:each in="${moveEventList}" var="event">
								<th class="dashboard_stat_exec_tdmc">
									<g:link controller="application" action="list" params="[moveEvent:event.id]">${event}</g:link>
								</th>
							</g:each>
							<th class="dashboard_stat_exec_tdmc" style="background-color:white;"></th>
						</tr>
						<tr>
							<td class="dashboard_stat_icon_td">&nbsp;</td>
							<td class="dashboard_stat_exec_td">&nbsp;</td>
							<td class="dashboard_stat_exec_td">
								<g:link controller="application" action="list" params="[filter:'applicationCount', plannedStatus:'Unassigned']">To be</g:link>
							</td>
							<g:each in="${moveEventList}" var="event">
								<td class="dashboard_stat_exec_tdmc" style="font-size: 10px"><b>${eventStartDate[event.id]}</b></td>
							</g:each>
							<td>&nbsp;</td>
						</tr>
						<tr>
							<td class="dashboard_stat_icon_td">&nbsp;</td>
							<td class="dashboard_stat_exec_td">&nbsp;</td>
							<td class="dashboard_stat_exec_td">
								<g:link controller="application" action="list" params="[filter:'applicationCount', plannedStatus:'Unassigned']">Assigned</g:link>
							</td>
							<g:each in="${moveEventList}" var="event">
								<td class="dashboard_stat_exec_tdmc" style="font-size: 10px"><b> ${event.runbookStatus ?: ''}</b></td>
							</g:each>
							<td>Done</td>
						</tr>
					</thead>
					<tbody>
						<tr>
							<td class="dashboard_stat_icon_td"><img src="${resource(dir:'images',file:'iconApp.png')}" height="14" /></td>
							<td>
								<g:link controller="application" action="list" class="links">Applications</g:link>
							</td>
							<td>
							<g:if test="${unassignedAppCount == 0 }">
								<span class='colorGrey'>0</span>
							</g:if>
							<g:else>

								<g:link controller="application" action="list" params="[filter:'applicationCount', plannedStatus:'Unassigned']" class="links">
									${unassignedAppCount} (${(percentageUnassignedAppCount > 0 && percentageUnassignedAppCount < 1) ? 1 : Math.round(percentageUnassignedAppCount)}%)
								</g:link>
							</g:else>
							</td>
							<g:each in="${appList}" var="appCount">
								<td class="dashboard_stat_exec_tdmx">
									<g:if test="${appCount.count == 0 }">
										<span class='colorGrey'>0</span>
									</g:if>
									<g:else>
										<g:link controller="application" action="list" params="[filter:'applicationCount',moveEvent:appCount.moveEvent]" class="links">${appCount.count}</g:link>
									</g:else>
								</td>
							</g:each>
							<td class="dashboard_stat_exec_tdmx">
								<g:if test="${percAppDoneCount == 0 }">
										<span class='colorGrey'>0%</span>
									</g:if>
								<g:else>
									<g:link controller="application" action="list" params="[filter:'applicationCount', runbook:'Done']" class="links">${percAppDoneCount}%</g:link>
								</g:else>
							</td>
						</tr>
						<tr>
							<td class="dashboard_stat_icon_td">&nbsp;</td>
							<td style="color: grey">Optional</td>
							<td>&nbsp;</td>
							<g:each in="${assetList}" var="appCount">
								<td class="dashboard_stat_exec_tdmx" class='colorGrey'>
									${appCount.optional}
								</td>
							</g:each>
							<td>&nbsp;</td>
						</tr>
						<tr>
							<td class="dashboard_stat_icon_td">&nbsp;</td>
							<td style="color: grey">Unknown</td>
							<td>&nbsp;</td>
							<g:each in="${assetList}" var="appCount">
								<td class="dashboard_stat_exec_tdmx" class='colorGrey'>
									${appCount.potential}
								</td>
							</g:each>
							<td>&nbsp;</td>
						</tr>
						<tr>
							<td class="dashboard_stat_icon_td"><img src="${resource(dir:'images',file:'iconServer.png')}" height="14" /></td>
							<td>
								<g:link controller="assetEntity" params="[filter:'All']" action="list" class="links">Servers</g:link>
							</td>
							<td>
								<g:if test="${unassignedAssetCount == 0 }">
									<span class='colorGrey'>0</span>
								</g:if>
								<g:else>
									<g:link controller="assetEntity" action="list" params="[filter:'All',listType:'server', plannedStatus:'Unassigned']" class="links">${unassignedAssetCount}</g:link>
								</g:else>
							</td>
							<g:each in="${assetList}" var="assetCount">
								<td style="text-align: right;">
								<g:if test="${assetCount.count == 0 }">
									<span class='colorGrey'>0</span>
								</g:if>
								<g:else>
									<g:link controller="assetEntity" action="list" params="[moveEvent:assetCount.moveEvent,filter:'All',listType:'server']" class="links" >${assetCount.count}</g:link>
								</g:else>
								</td>
							</g:each>
							<td>&nbsp;</td>
						</tr>
						<tr>
							<td class="dashboard_stat_icon_td">&nbsp;</td>
							<td>
								<g:link controller="assetEntity" params="[filter:'physical',listType:'server']" action="list" class="links">Physical</g:link>
							</td>
							<td>
							<g:set var="percentageUnassignedPhysicalAssetCount" value="${physicalCount ? (unassignedPhysialAssetCount/physicalCount)*100 : 0}" />
							<g:if test="${unassignedPhysialAssetCount == 0 }">
								<span class='colorGrey'>0</span>
							</g:if>
							<g:else>
								<g:link controller="assetEntity" action="list" params="[filter:'physical', plannedStatus:'Unassigned',listType:'server']" class="links">
								   ${unassignedPhysialAssetCount} (${(percentageUnassignedPhysicalAssetCount > 0 && percentageUnassignedPhysicalAssetCount < 1) ? 1 : Math.round(percentageUnassignedPhysicalAssetCount)}%)
								</g:link>
							</g:else>
							</td>
							<g:each in="${assetList}" var="assetCount">
								<td style="text-align: right;">
									<g:if test="${assetCount.physicalCount == 0 }">
										<span class='colorGrey'>0</span>
									</g:if>
									<g:else>
										<g:link controller="assetEntity" action="list" params="[moveEvent:assetCount.moveEvent,filter:'physical',listType:'server']" class="links">${assetCount.physicalCount}</g:link>
									</g:else>
								</td>
							</g:each>
							<td style="text-align: right;">
								<g:if test="${percentagePhysicalAssetCount== 0 }">
									<span class='colorGrey'>0%</span>
								</g:if>
								<g:else>
									<g:link controller="assetEntity" action="list" params="[filter:'physical', plannedStatus:'Moved',listType:'server']" class="links">${percentagePhysicalAssetCount}%</g:link>
								</g:else>
							</td>
						</tr>
						<tr>
							<td class="dashboard_stat_icon_td">&nbsp;</td>
							<td>
								<g:link controller="assetEntity" params="[filter:'virtual',listType:'server']" action="list" class="links">Virtual</g:link>
							</td>
							<td>
							<g:set var="percentageUnassignedVirtualCount" value="${virtualCount ? (unassignedVirtualAssetCount/virtualCount)*100 : 0}" />
							<g:if test="${unassignedVirtualAssetCount == 0 }">
								<span class='colorGrey'>0</span>
							</g:if>
							<g:else>
								<g:link controller="assetEntity" action="list" params="[filter:'virtual', plannedStatus:'Unassigned',listType:'server']" class="links">
								   ${unassignedVirtualAssetCount} (${(percentageUnassignedVirtualCount > 0 && percentageUnassignedVirtualCount < 1) ? 1 : Math.round(percentageUnassignedVirtualCount)}%)
								</g:link>
							</g:else>
							</td>
							<g:each in="${assetList}" var="assetCount">
								<td style="text-align: right;">
									<g:if test="${assetCount.virtualAssetCount == 0 }">
										<span class='colorGrey'>0</span>
									</g:if>
									<g:else>
										<g:link controller="assetEntity" action="list" params="[moveEvent:assetCount.moveEvent,filter:'virtual',listType:'server']" class="links">${assetCount.virtualAssetCount}</g:link>
									</g:else>
								</td>
							</g:each>
							<td style="text-align: right;">
								<g:if test="${percentagevirtualAssetCount== 0 }">
									<span class='colorGrey'>0%</span>
								</g:if>
								<g:else>
									<g:link controller="assetEntity" action="list" params="[filter:'virtual', plannedStatus:'Moved',listType:'server']" class="links">${percentagevirtualAssetCount}%</g:link>
								</g:else>
							</td>
						</tr>
						<tr>
							<td class="dashboard_stat_icon_td"><img src="${resource(dir:'images',file:'iconDB.png')}" height="14" /></td>
							<td>
								<g:link controller="database" action="list" class="links">Databases</g:link>
							</td>
							<td>
							<g:set var="percentageUnassignedDbCount" value="${dbCount ? (unassignedDbCount/dbCount)*100 : 0}" />
							<g:if test="${unassignedDbCount == 0 }">
								<span class='colorGrey'>0</span>
							</g:if>
							<g:else>
								<g:link controller="database" action="list" params="[filter:'db', plannedStatus:'Unassigned']" class="links">
									${unassignedDbCount} (${(percentageUnassignedDbCount > 0 && percentageUnassignedDbCount < 1) ? 1 : Math.round(percentageUnassignedDbCount)}%)
								</g:link>
							</g:else>
							</td>
							<g:each in="${dbList}" var="dbCount">
								<td style="text-align: right;">
									<g:if test="${dbCount.count == 0 }">
										<span class='colorGrey'>0</span>
									</g:if>
									<g:else>
										<g:link controller="database" action="list" params="[filter:'db',moveEvent:dbCount.moveEvent]" class="links">${dbCount.count}</g:link>
									</g:else>
								</td>
							</g:each>
							<td style="text-align: right;">
								<g:if test="${percentageDBCount== 0 }">
									<span class='colorGrey'>0%</span>
								</g:if>
								<g:else>
									<g:link controller="database" action="list" params="[filter:'db', plannedStatus:'Moved']" class="links">${percentageDBCount}%</g:link>
								</g:else>
							</td>
						</tr>
						<tr>
							<td class="dashboard_stat_icon_td"><img src="${resource(dir:'images',file:'iconStorage.png')}" height="14" /></td>
							<td>
								<g:link controller="files" action="list" class="links">Storage</g:link>
							</td>
							<td>
							<g:set var="percentageUnassignedFilesCount" value="${fileCount ? (unassignedFilesCount/fileCount)*100 : 0}" />
							<g:if test="${unassignedFilesCount == 0 }">
								<span class='colorGrey'>0</span>
							</g:if>
							<g:else>
								<g:link controller="files" action="list" params="[filter:'storage', plannedStatus:'Unassigned']" class="links">
									${unassignedFilesCount} (${(percentageUnassignedFilesCount > 0 && percentageUnassignedFilesCount < 1) ? 1 : Math.round(percentageUnassignedFilesCount)}%)
								</g:link>
							</g:else>
							</td>
							<g:each in="${filesList}" var="filesCount">
								<td style="text-align: right;">
									<g:if test="${filesCount.count == 0 }">
										<span class='colorGrey'>0</span>
									</g:if>
									<g:else>
										<g:link controller="files" action="list" params="[filter:'storage', moveEvent:filesCount.moveEvent]" class="links">${filesCount.count}</g:link>
									</g:else>
								</td>
							</g:each>
							<td style="text-align: right;">
								<g:if test="${percentageFilesCount== 0 }">
									<span class='colorGrey'>0%</span>
								</g:if>
								<g:else>
									<g:link controller="files" action="list" params="[filter:'storage', plannedStatus:'Moved']" class="links">${percentageFilesCount}%</g:link>
								</g:else>
							</td>
                        </tr>
                        <tr>
							<td class="dashboard_stat_icon_td"><img src="${resource(dir:'images',file:'iconNetwork.png')}" height="14" /></td>
                            <td><b>Other</b></td>
                            <td>
                            <g:set var="percentageUnassignedOtherCount" value="${otherAssetCount ? (unassignedOtherCount/otherAssetCount)*100 : 0}" />
							<g:if test="${unassignedOtherCount == 0 }">
								<span class='colorGrey'>0</span>
							</g:if>
							<g:else>
								<g:link controller="assetEntity" action="list" params="[filter:'other', plannedStatus:'Unassigned',listType:'physical']" class="links">
									${unassignedOtherCount}	(${(percentageUnassignedOtherCount > 0 && percentageUnassignedOtherCount < 1) ? 1 : Math.round(percentageUnassignedOtherCount)}%)
								</g:link>
							</g:else>
							</td>
							<g:each in="${otherTypeList}" var="otherCount">
								<td style="text-align: right;">
									<g:if test="${otherCount.count== 0 }">
										<span class='colorGrey'>0</span>
									</g:if>
									<g:else>
										<g:link controller="assetEntity" action="list" params="[moveEvent:otherCount.moveEvent,filter:'other',listType:'physical']" class="links">${otherCount.count}</g:link>
									</g:else>
								</td>
							</g:each>
							<td style="text-align: right;">
								<g:if test="${percentageOtherCount== 0 }">
									<span class='colorGrey'>0%</span>
								</g:if>
								<g:else>
									<g:link controller="assetEntity" action="list" params="[filter:'other', plannedStatus:'Moved',listType:'physical']" class="links">${percentageOtherCount}%</g:link>
							</g:else>
							</td>
						</tr>
						<tr>
							<td class="dashboard_stat_icon_td">&nbsp;</td>
                            <td ><b>Open Tasks</b></td>
                            <td></td>
							<g:each in="${openTasks}" var="tasks">
								<td style="text-align: right;">
									<g:if test="${tasks.count== 0 }">
										<span class='colorGrey'>0</span>
									</g:if>
									<g:else>
										<g:link controller="assetEntity" action="listTasks" params="[moveEvent:tasks.moveEvent, justRemaining:1]" class="links">${tasks.count}</g:link>
									</g:else>
								</td>
							</g:each>
							<td></td>
						</tr>
					</tbody>
				</table>
			</div>
		</div>
	</div>
</body>
</html>