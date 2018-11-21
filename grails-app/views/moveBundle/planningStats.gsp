<%@ page import="com.tdsops.tm.enums.domain.ValidationType" %>
<html>
<head>
	<title>Transition Planning Dashboard</title>
	<meta name="layout" content="topNav" />

<script type="text/javascript">
$(document).ready(function() {
	currentMenuId = "#dashboardMenu";

	$(".menu-parent-dashboard-planning-dashboard").addClass('active');
	$(".menu-parent-dashboard").addClass('active');

	var percentageAppToValidate = 100 - ${percentageAppToValidate}
			$("#discoverybar").animate({width: percentageAppToValidate + "%"}, 1000);
	$("#applicationbar").animate({width: percentageAppToValidate + "%"}, 1000);

	var percentagePlanReady = "${percentagePlanReady}";
	$("#analysisbar").animate({width: percentagePlanReady + "%"}, 1000);

	$("#confirmedbar").animate({width: "${confirmedAppPerc}%"}, 1000);

	$("#appmovedbar").animate({width: "${movedAppPerc}%"}, 1000);

	$("#serversmovedbar").animate({width: "${movedServersPerc}%"}, 1000);

	$("#assignmentbar").animate({width: "${assignedAppPerc}%"}, 1000);

	var percentagePSToValidate = 100 - "${percentagePSToValidate}";
	$("#physicalbar").animate({width: percentagePSToValidate + "%"}, 1000);

	var percentageVMToValidate = 100 - "${percentageVMToValidate}";
	$("#virtualbar").animate({width: percentageVMToValidate + "%"}, 1000);

	var percentageDBToValidate = 100 - "${percentageDBToValidate}";
	$("#dbbar").animate({width: percentageDBToValidate + "%"}, 1000);

	var percentageStorToValidate = 100 - "${percentageStorToValidate}";
	$("#filebar").animate({width: percentageStorToValidate + "%"}, 1000);

	var percentageOtherToValidate = 100 - "${percentageOtherToValidate}";
	$("#assetbar").animate({
		width: percentageOtherToValidate + "%"
	}, 1000);

	$('[data-toggle="popover"]').popover();

});
</script>
<g:javascript src="entity.crud.js" />
<style>

	table.dashboard_right_table th {
		padding: 0px;
	}

	table.dashboard_right_table td {
		height: 27px;
	}

</style>
</head>
<body>
<tds:subHeader title="Planning Dashboard" crumbs="['Dashboard','Planning']"/>
		<div class="execution-phase-wrapper">

<%-- Analysis Phase --%>
			<div class="planning-dashboard">
				<div class="dashboard_div discovery-phase">
					<span class="dashboard_head">Discovery Phase</span>
					<table style="margin-bottom: 10px; border-spacing: 0px;">
                        <!-- Applications Validated Progressbar -->
						<tr>
							<td class="dashboard_bar_base">
                                <g:if test="${percentageAppToValidate == 100}">
									<div class="dashboard_bar_graph0">
										<b>0% Applications Validated</b>
									</div>
								</g:if>
                                <g:elseif test="${percentageAppToValidate == 0}">
									<div class="task_completed"
										style="z-index: -1; height: 24px; width: 100%"></div>
									<div class="task_completed"
										style="position: relative; top: -20px; height: 0px; margin-left: 5px;">
										<b>100% Applications Validated</b>
									</div>
								</g:elseif>
                                <g:else>
									<div class="dashboard_bar_graph" id="discoverybar"style="width: 0%;"></div>
									<div style="position: relative; top: -18px; height: 0px; margin-left: 5px;">
										<b> ${100 - percentageAppToValidate}%</b>
										<g:link controller="application" action="list"
                                                params="[filter:'application', toValidate:'Unknown']">
                                            Applications Validated
                                        </g:link>
									</div>
								</g:else>
                            </td>
						</tr>
                        <!-- Applications Ready Progressbar --->
                        <tr>
                            <td class="dashboard_bar_base">
                                <g:if test="${percentagePlanReady == 0}">
                                    <div class="dashboard_bar_graph0">
                                        <b>0% Applications Ready</b>
                                    </div>

                                </g:if>
                                <g:elseif test="${percentagePlanReady == 100}">
                                    <div class="task_completed"style="z-index: -1; height: 24px; width: 100%"></div>
                                    <div class="task_completed" style="position: relative; top: -20px; height: 0px; margin-left: 5px;">
                                        <b>100% Applications Ready</b>
                                    </div>
                                </g:elseif>
                                <g:else>
                                    <div class="dashboard_bar_graph" id="analysisbar"style="width: 0%;"></div>
                                    <div style="position: relative; top: -18px; height: 0px; margin-left: 5px;">
                                        <b> ${percentagePlanReady}%</b>
                                    <g:link controller="application" action="list"
                                            params="[filter:'application', toValidate:ValidationType.PLAN_READY]">Applications Ready</g:link>
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
							<g:render template="discoveryGraph"
								model="[assetCount:applicationCount,filter:'application',assetType:'application',title:'Applications',validate:appToValidate,barId:'applicationbar',iconName:'application']">
							</g:render>
						</tr>
						<tr>
							<g:render template="discoveryGraph"
								model="[assetCount: phyServerCount,filter:'physicalServer',assetType:'assetEntity',title:'Physical Servers',validate:psToValidate,barId:'physicalbar',iconName:'serverPhysical']"></g:render>
						</tr>
						<tr>
							<g:render template="discoveryGraph"
								model="[assetCount: virtServerCount,filter:'virtualServer',assetType:'assetEntity',title:'Virtual Servers',validate:vsToValidate,barId:'virtualbar',iconName:'serverVirtual']"></g:render>
						</tr>
						<tr>
							<g:render template="discoveryGraph"
								model="[assetCount:dbCount,filter:'db',assetType:'database',title:'Databases',validate:dbToValidate,barId:'dbbar',iconName:'database']"></g:render>
						</tr>
						<tr>
							<g:render template="discoveryGraph"
								model="[assetCount:phyStorageCount,filter:'storage',assetType:'assetEntity',title:'Physical Storage',validate:phyStorageToValidate,barId:'filebar',iconName:'storagePhysical']"></g:render>
						</tr>
						<tr>
							<g:render template="discoveryGraph"
								model="[assetCount:fileCount,filter:'storage',assetType:'files',title:'Logical Storage',validate:fileToValidate,barId:'filebar',iconName:'storageLogical']"></g:render>
						</tr>
						<tr>
							<g:render template="discoveryGraph"
								model="[assetCount:otherAssetCount,filter:'other',assetType:'assetEntity',title:'Other Devices',validate:otherToValidate,barId:'assetbar',iconName:'other']"></g:render>
						</tr>
					</table>
					<br />
					<h4>
						<b>Open Tasks</b>
					</h4>
					<table class="dashboard_stat_table">
						<tr>
							<td class="dashboard_stat_td">
                                <g:link controller="assetEntity" action="listTasks"
									params="[filter:'openIssue', moveEvent:'0', justRemaining:1]"
									class="links">
									${openIssue}
								</g:link></td>
							<td><g:link controller="assetEntity" action="listTasks"
									params="[filter:'openIssue', moveEvent:'0', justRemaining:1]"
									class="links">Active Tasks</g:link></td>
						</tr>
						<g:if test="${dueOpenIssue>0}">
							<tr>
								<td class="dashboard_stat_td">
                                    <g:link controller="assetEntity" action="listTasks"
                                            params="[filter:'dueOpenIssue', moveEvent:'0', justRemaining:1]"
                                            class="links"><b style="color: red;">${dueOpenIssue}</b></g:link>
                                </td>
								<td><g:link controller="assetEntity" action="listTasks"
										params="[filter:'dueOpenIssue', moveEvent:'0', justRemaining:1]"
										class="links">Overdue</g:link></td>
							</tr>
						</g:if>
					</table>
				</div>

	            <%-- Analysis Phase --%>
				<div class="dashboard_div analysis-phase">
					<span class="dashboard_head">Analysis & Planning Phase</span>
					<table style="margin-bottom: 10px; border-spacing: 0px;">
						<%-- Applications Assigned Bar --%>
						<tr>
							<td class="dashboard_bar_base">
								<g:if test="${assignedAppPerc == 0}">
                                    <div class="dashboard_bar_graph0">
                                        <b>0% Applications Assigned</b>
                                    </div>
                                </g:if>
                                <g:elseif test="${assignedAppPerc == 100}">
                                    <div class="task_completed" style="z-index: -1; height: 24px; width: 100%"></div>
                                    <div class="task_completed" style="position: relative; top: -20px; height: 0px; margin-left: 5px;">
                                        <b>100% Applications Assigned</b>
                                    </div>
                                </g:elseif>
                                <g:else>
                                    <div class="dashboard_bar_graph" id="assignmentbar" style="width: 0%;"></div>
                                    <div style="position: relative; top: -18px; height: 0px; margin-left: 5px;">
                                        <b> ${assignedAppPerc}% </b>
                                        <g:link controller="application" action="list"
                                                params="[filter:'application',plannedStatus:'Assigned']">Applications Assigned</g:link>
                                    </div>
                                </g:else>
							</td>
						</tr>
						<%-- Applications Confirmed Bar --%>
						<tr>
							<td class="dashboard_bar_base">
								<g:if test="${confirmedAppPerc == 0}">
                                    <div class="dashboard_bar_graph0">
                                        <b>0% Applications Confirmed</b>
                                    </div>
                                </g:if>
                                <g:elseif test="${confirmedAppPerc == 100}">
                                    <div class="task_completed" style="z-index: -1; height: 24px; width: 100%"></div>
                                        <div class="task_completed" style="position: relative; top: -20px; height: 0px; margin-left: 5px;">
                                            <b>100% Applications Confirmed</b>
                                        </div>
                                </g:elseif>
                                <g:else>
                                    <div class="dashboard_bar_graph" id="confirmedbar" style="width: 0%;"></div>
                                        <div style="position: relative; top: -18px; height: 0px; margin-left: 5px;">
                                            <b> ${confirmedAppPerc}%</b>
                                            <g:link controller="application" action="list"
                                            params="[filter:'application',plannedStatus:'Confirmed']">Applications Confirmed</g:link>
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
								<g:link
									controller="application" action="list"
									params="[filter:'application', toValidate:'Validated']"
									class="links">${validated}
								</g:link>
							</td>
							<td>
								<g:link controller="application" action="list"
									params="[filter:'application', toValidate:'Validated']"
									class="links">Validated</g:link>
							</td>
						</tr>
						<tr>
							<td class="dashboard_stat_td"><g:link
									controller="application" action="list"
									params="[filter:'application', toValidate: ValidationType.PLAN_READY]"
									class="links">
									${planReady}
								</g:link></td>
							<td><g:link controller="application" action="list"
									params="[filter:'application', toValidate:ValidationType.PLAN_READY]"
									class="links">Ready</g:link></td>
						</tr>
					</table>
					<br />
					<h4>
						<b>Dependencies</b>
					</h4>
					<table class="dashboard_stat_table">
						<tr>
							<td class="dashboard_stat_td">
								${pendingAppDependenciesCount}
							</td>
							<td>App Dependencies to validate<br /> <g:if
									test="${appDependenciesCount > 0 }">
									(${appDependenciesCount ? Math.round((pendingAppDependenciesCount/appDependenciesCount)*100) : 0}% of the
									${appDependenciesCount} total)
									</g:if>
							</td>
						</tr>
						<tr>
							<td class="dashboard_stat_td">
								${pendingServerDependenciesCount}
							</td>
							<td>Server Dependencies to validate<br /> <g:if
									test="${serverDependenciesCount > 0 }">
									(${serverDependenciesCount ? Math.round((pendingServerDependenciesCount/serverDependenciesCount)*100) : 0}% of the
									 ${serverDependenciesCount} total)
									</g:if>
							</td>
						</tr>
						<tr>
							<td class="dashboard_stat_td"><g:link
									controller="assetEntity" action="listTasks"
									params="[filter:'analysisIssue', justRemaining:0, moveEvent:0]"
									class="links">
									${issuesCount}
								</g:link></td>
							<td><g:link controller="assetEntity" action="listTasks"
									params="[filter:'analysisIssue', justRemaining:0, moveEvent:0]"
									class="links">Active Tasks</g:link></td>
						</tr>
						<g:if test="${generalOverDue>0}">
							<tr>
								<td class="dashboard_stat_td">
                                    <g:link controller="assetEntity" action="listTasks"
                                            params="[filter:'generalOverDue', justRemaining:1, moveEvent:0]"
                                            class="links"><b style="color: red;"> ${generalOverDue}</b></g:link>
                                </td>
								<td><g:link controller="assetEntity" action="listTasks"
										params="[filter:'generalOverDue', justRemaining:1, moveEvent:0]"
										class="links">Overdue</g:link></td>
							</tr>
						</g:if>
					</table>

					<br />
					<h4>
						<b>Application Plans</b>
					</h4>
					<table class="dashboard_stat_table">
					<g:each in="${groupPlanMethodologyCount}" var="counter">
						<tr>
							<td class="dashboard_stat_td">
								<g:link controller="application" action="list"
										params="[filter:'application', planMethodology:counter.key, ufp:'true']" class="links">
									${counter.value}
								</g:link>
							</td>
							<td>
								<g:link controller="application" action="list"
										params="[filter:'application', planMethodology:counter.key, ufp:'true']" class="links">
									${counter.key}
								</g:link>
							</td>
						</tr>
					</g:each>
					</table>
				</div>

	<%-- Execution Phase Section --%>

				<div class="dashboard_div execution-phase">
					<span class="dashboard_head">Execution Phase</span>
					<table style="margin-bottom: 10px; border-spacing: 0px;">

						<%-- Applications Completed Bar --%>
						<tr>
							<td class="dashboard_bar_base">
								<g:if test="${movedAppPerc == 0}">
									<div class="dashboard_bar_graph0">
										<b>0% Applications Completed</b>
									</div>
								</g:if>
								<g:elseif test="${movedAppPerc == 100}">
									<div class="task_completed" style="z-index: -1; height: 24px; width: 100%"></div>
									<div class="task_completed" style="position: relative; top: -20px; height: 0px; margin-left: 5px;">
										<b>100% Applications Completed</b>
									</div>
								</g:elseif>
								<g:else>
									<div class="dashboard_bar_graph" id="appmovedbar" style="width: 0%;"></div>
									<div style="position: relative; top: -18px; height: 0px; margin-left: 5px;">
										<b> ${movedAppPerc}% </b>
										<g:link controller="application" action="list" params="[filter:'application', plannedStatus:'Moved']">Applications Completed</g:link>
									</div>
								</g:else>
							</td>
						</tr>

                        <%-- Servers Completed Bar --%>
                        <tr>
                            <td class="dashboard_bar_base">
                                <g:if test="${movedServersPerc == 0}">
                                    <div class="dashboard_bar_graph0">
                                        <b>0% Servers Completed</b>
                                    </div>
                                </g:if>
                                <g:elseif test="${movedServersPerc == 100}">
                                    <div class="task_completed" style="z-index: -1; height: 24px; width: 100%"></div>
                                    <div class="task_completed" style="position: relative; top: -20px; height: 0px; margin-left: 5px;">
                                        <b>100% Servers Completed</b>
                                    </div>
                                </g:elseif>
                                <g:else>
                                    <div class="dashboard_bar_graph" id="serversmovedbar" style="width: 0%;"></div>
                                    <div style="position: relative; top: -18px; height: 0px; margin-left: 5px;">
                                        <b> ${movedServersPerc}% </b>
                                        <g:link controller="application" action="list" params="[filter:'application', plannedStatus:'Moved']">Servers Completed</g:link>
                                    </div>
                                </g:else>
                            </td>
                        </tr>

					</table>

					<div class="container-fluid" style="padding-left: 0px;">
						<div class="row-fluid">
							<div class="col-md-2" style="padding-left: 0px;">
								<div>
									<table id="eventHeaderTableId" class="dashboard_right_table" style="border: none;">
										<g:if test="${moveEventList.size() > 0}">
											<thead>
											<tr><th style="background-color: transparent; line-height: 100px;" class="">&nbsp;</th></tr>
											</thead>
										</g:if>
										<tbody>
										<tr>
											<td class="dashboard_stat_icon_td"><tds:svgIcon name="application_menu" width="17" height="17" /></td>
											<td style="vertical-align: middle;"> <g:link controller="application" action="list" class="links">Applications</g:link></td>
										</tr>

										<tr>
											<td class="dashboard_stat_icon_td"><tds:svgIcon name="serverPhysical_menu" width="17" height="17" /></td>
											<td>
												<g:link controller="assetEntity"
														params="[filter:'physicalServer']"
														action="list" class="links">Physical Server</g:link>
											</td>
										</tr>

										<tr>
											<td class="dashboard_stat_icon_td"><tds:svgIcon name="serverVirtual_menu" width="17" height="17" /></td>
											<td>
												<g:link controller="assetEntity"
														params="[filter:'virtualServer']"
														action="list" class="links">Virtual Server</g:link>
											</td>
										</tr>
										<tr>
											<td class="dashboard_stat_icon_td"><tds:svgIcon name="database_menu" width="17" height="17" /></td>
											<td><g:link controller="database" action="list"
														class="links">Databases</g:link></td>
										</tr>
										<tr>
											<td class="dashboard_stat_icon_td"><tds:svgIcon name="storagePhysical_menu" width="17" height="17" /></td>
											<td nowrap="nowrap"><g:link controller="assetEntity" action="list"
																		params="[filter:'storage']"
																		class="links">Physical Storage</g:link>
											</td>
										</tr>
										<tr>
											<td class="dashboard_stat_icon_td"><tds:svgIcon name="storageLogical_menu" width="17" height="17" /></td>
											<td><g:link controller="files" action="list"
														class="links">Logical Storage</g:link>
											</td>
										</tr>

										<tr>
											<td class="dashboard_stat_icon_td"><tds:svgIcon name="other_menu" width="17" height="17" /></td>
											<td><g:link controller="assetEntity"
														params="[filter:'other']"
														action="list"
														class="links">Other</g:link>
											</td>
										</tr>
										<tr>
											<td class="dashboard_stat_icon_td">&nbsp;</td>
											<td><b>Open Tasks</b></td>
										</tr>
										</tbody>
									</table>
								</div>
							</div>
							<div class="col-md-10 col-xs-10" style="padding-left: 0px; padding-right: 0px;">
								<div id="eventDataTableId" style="overflow-y: hidden;">
									<table class="dashboard_right_table dashboard_stat_table" style="border-spacing: 5px 0px; border-collapse: separate;">
										<g:if test="${moveEventList.size() > 0}">
											<thead>
												<tr>
													<th rowspan="3" class="dashboard_stat_exec_td "  valign="bottom">
														<div style="padding-bottom: 5px; font-size: 10px; text-align: right;"><b>Unassigned</b></div>
													</th>

													<g:each in="${moveEventList}" var="event">
														<th class="dashboard_stat_exec_tdmc" style="text-align: right !important; padding-right: 0px !important;">
															<div class="dashboard_stat_exec_tdmc_title">
																<g:link controller="application" action="list" params="[moveEvent:event.id]" data-toggle="popover" data-trigger="hover" data-content="${event}" data-placement="top">
																	${event.toString().length() > 18 ? event.toString().substring(0,15) + '...' : event}
																</g:link>
															</div>
														</th>
													</g:each>
													<th class="dashboard_stat_exec_tdmc_title"></th>
												</tr>
												<tr>
													<g:each in="${moveEventList}" var="event">
														<td class="dashboard_stat_exec_tdmc" style="font-size: 10px; text-align: right;" nowrap>
															<b>${eventStartDate[event.id]}</b>
														</td>
													</g:each>
													<td></td>
												</tr>
												<tr>
													<g:each in="${moveEventList}" var="event">
														<td class="dashboard_stat_exec_tdmc" style="font-size: 10px; text-align: right">
															<b> ${event.runbookStatus ?: ''}</b>
														</td>
													</g:each>
													<td class="dashboard_stat_exec_tdmc" style="font-size: 10px; text-align: right;">
														<b>Done</b>
													</td>
												</tr>
										</thead>
										</g:if>
										<tbody>

                                            <%-- Applications --%>
                                            <g:render template="planningStatsExecRow"
                                                      model="[assetCount:applicationCount, unassignedCount:unassignedAppCount, percDone:percAppDoneCount, controller:'application', filter:'application', list:appList]"
                                                    />

                                            <%-- Physical Servers --%>
                                            <g:render template="planningStatsExecRow"
                                                      model="[assetCount:phyServerCount, unassignedCount:unassignedPhysicalServerCount, percDone:percentagePhysicalServerCount, controller:'assetEntity', filter:'physicalServer', list:phyServerList]"
                                                    />

                                            <%-- Virtual Servers --%>
                                            <g:render template="planningStatsExecRow"
                                                      model="[assetCount:virtServerCount, unassignedCount:unassignedVirtualServerCount, percDone:percVirtualServerCount, controller:'assetEntity', filter:'virtualServer', list:virtServerList]"
                                                    />

                                            <%-- Databases --%>
                                            <g:render template="planningStatsExecRow"
                                                      model="[assetCount:dbCount, unassignedCount:unassignedDbCount, percDone:percentageDBCount, controller:'database', filter:'db', list:dbList]"
                                                    />

                                            <%-- Physical Storage --%>
                                            <g:render template="planningStatsExecRow"
                                                      model="[assetCount:phyStorageCount, unassignedCount:unAssignedPhyStorageCount, percDone:percentagePhyStorageCount, controller:'assetEntity', filter:'storage', list:phyStorageList]"
                                                    />

                                            <%-- Logical Storage --%>
                                            <g:render template="planningStatsExecRow"
                                                      model="[assetCount:fileCount, unassignedCount:unassignedFilesCount, percDone:percentageFilesCount, controller:'files', filter:'storage', list:filesList]"
                                                    />

                                            <%-- Other Devices --%>
                                            <g:render template="planningStatsExecRow"
                                                      model="[assetCount:otherAssetCount, unassignedCount:unassignedOtherCount, percDone:percentageOtherCount, controller:'assetEntity', filter:'other', list:otherTypeList]"
                                                    />

                                            <%-- Open Tasks --%>
                                            <tr>
                                                <td></td>
                                                <g:each in="${openTasks}" var="tasks">
                                                    <td style="text-align: right;"><g:if
                                                            test="${tasks.count== 0 }">
                                                        <span class='colorGrey'>0</span>
                                                    </g:if> <g:else>
                                                        <g:link controller="assetEntity" action="listTasks"
                                                                params="[moveEvent:tasks.moveEvent, justRemaining:1]"
                                                                class="links">
                                                            ${tasks.count}
                                                        </g:link>
                                                    </g:else></td>
                                                </g:each>
                                                <td></td>
                                            </tr>
										</tbody>
									</table>
								</div>
							</div>
						</div>
					</div>

				</div>
			</div>
		</div>

</body>
</html>
