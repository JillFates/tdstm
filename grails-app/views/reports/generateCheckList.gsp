<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
<meta name="layout" content="topNav" />
<title>Pre-Event Checklist</title>
<script>
	$(document).ready(function() {
	    currentMenuId = "#reportsMenu";
		$('.menu-reports-pre-checklist').addClass('active');
		$('.menu-parent-reports').addClass('active');
	});
</script>
</head>
<body>
	<tds:subHeader title="Pre-Event Checklist" crumbs="['Reports','Pre-Event Checklist']"/> <br />
	<div class="generated-check-list">
		<div style="margin-top: 20px; color: black; font-size: 20px;text-align: center;" >
			<b>${project.name} : ${moveEvent.name}</b>
		</div>
		<div style="color: black; font-size: 15px;text-align: center;">
			${time}
		</div>
		${raw(eventErrorString)}

		<table>
			<tr>
				<td><g:if test="${allErrors.contains('Project')}">
						<span style="color: red;"><b><h2>Project</h2></b></span>
					</g:if> <g:else>
						<span style="color: green;"><b><h2>Project</h2></b></span>
					</g:else></td>
			</tr>
			<tr>
				<td></td>
				<td>
					${raw(errorForEventTime)}
				</td>
			</tr>
			<tr>
				<td></td>
				<td>
					${raw(newsBarModeError)}
				</td>
			</tr>
			<tr>
				<td></td>
				<td><span style="color: green"><b>Staff</b></span>:${raw(clientAccess.toString().replace('[','').replace(']',''))}
				</td>
			</tr>
			<tr>
				<td></td>
				<td>
					${raw(userLoginError)}
				</td>
			</tr>
			<tr>
				<td></td>
				<td>
					<table style="width: auto; margin-left: 100px;">
						<thead>
							<th>Staff Name</th>
							<th>Company</th>
							<th>Team</th>
						</thead>
						<tbody>
							<g:each in="${list}" var="staff" status="i">
								<g:if test="${ list.size()>0}">
									<tr class="${(i % 2) == 0 ? 'odd' : 'even'}">
										<td>
											${staff.name}
										</td>
										<td>
											${staff.company}
										</td>
										<td>
											${staff.role}
										</td>
									</tr>
								</g:if>
								<g:else>
									<tr>
										<td>No Staff for the Project</td>
									</tr>
								</g:else>
							</g:each>
						</tbody>
					</table>
				</td>
			</tr>
			<tr>
				<td><g:if test="${allErrors.contains('EventsBundle')}">
						<span style="color: red;"><h2>
								<b>Event/Bundle</b>
							</h2></span>
					</g:if> <g:else>
						<span style="color: green;"><h2>
								<b>Event/Bundle</b>
							</h2></span>
					</g:else></td>
			</tr>
			<tr>
				<td></td>
				<td><span style="color: green;"><b>Bundles: OK
							&nbsp;&nbsp; ${moveBundleSize} Bundles;&nbsp;${moveBundles.toString().replace('[','').replace(']','')}
					</b></span></td>
			</tr>
			<tr>
				<td></td>
				<td><span style="color: green;"><b>WorkFlow: OK </b></span></td>
			</tr>
			<tr>
				<td></td>
				<td><g:each in="${workFlowCodeSelected}" var="workFlow"
						status="i">
						<span style="color: green; margin-left: 50px;">
							${workFlow.key}:${workFlow.value}
						</span>
						<br></br>
					</g:each></td>

			</tr>
			<tr>
				<td></td>

				<td><g:if
						test="${dashBoardOk[0]?.contains('No steps created')}">
						<span style="color: red;"><b>Dashboard </b><br></br></span>
					</g:if> <g:else>
						<span style="color: green;"><b>Dashboard OK</b><br></br></span>
					</g:else> <span> <g:each in="${steps}" var="workFlow">
							<g:if test="${workFlow.value=='No steps created'}">
								<span style="color: red; margin-left: 50px;">
									${workFlow.key}: ${workFlow.value}
								</span>
								<br></br>
							</g:if>
							<g:else>
								<span style="color: green; margin-left: 50px;">
									${workFlow.key}: ${workFlow.value}
								</span>
								<br></br>
							</g:else>
						</g:each>
				</span></td>

			</tr>
			<tr>
				<td><g:if test="${allErrors.contains('Assets')}">
						<span style="color: red;"><b><h2>Assets</h2></b></span>
					</g:if> <g:else>
						<span style="color: green;"><b><h2>Assets</h2></b></span>
					</g:else></td>
			</tr>
			<tr>
				<td></td>
				<td><span style="color: green;"><b>Summary: OK <br></br>
							<g:each in="${summaryOk}" var="summary">
								<span style="color: green; margin-left: 50px;">
									${summary.key}:&nbsp;${summary.value}
								</span>
								<br></br>
							</g:each>
					</b></span></td>
			</tr>
			<tr>
				<td></td>
				<td>
					${raw(blankAssets)}
					 <g:if test="${nullAssetname.size()>0 }">
						<span style="color: red;margin-left: 50px;"> Blank names: ${nullAssetname.size()} Assets with no name-${nullAssetname.tag.toString().replace('[','').replace(']','')}</span>
					 </g:if>
					<br/>
				</td>
			</tr>
			<tr>
				<td></td>
				<td>
					${raw(duplicates)} <g:each in="${duplicatesAssetNames}" var="duplicate">
						<span style="margin-left: 50px;">
							${duplicate.counts} duplicates Named "${duplicate.assetName} "- (${duplicate.type})
						</span>
						<br></br>
					</g:each>
				</td>
			</tr>
			<tr>
				<td></td>
				<td>
					${raw(blankAssetTag)}
					<g:if test="${nullAssetTag.size()>0 }">
						<span style="color: red;margin-left: 50px;"> Blank names: ${nullAssetTag.size()} Assets with no tag- ${nullAssetTag.assetName.toString().replace('[','').replace(']','')}</span>
					</g:if>
						<br />
				</td>
			</tr>
			<tr>
				<td></td>
				<td>
					${raw(duplicatesTag)} <g:each in="${duplicatesAssetTagNames}"
						var="duplicate">
						<span style="margin-left: 50px"> ${duplicate.counts}
							duplicates Named "${duplicate.tag}"
						</span>
						<br></br>
					</g:each>
				</td>
			</tr>
			<tr>
				<td>&nbsp;</td>
				<td>
					${raw(missedRacks)}<br />
					<div style="margin-left: 50px; text-align: left;">
						${missingRacks.toString().replace('[','').replace(']','')}
					</div>
				</td>
			</tr>
			<tr>
				<td>&nbsp;</td>
				<td>
					${raw(dependenciesOk)}
				</td>
			</tr>
			<tr>
				<td>&nbsp;</td>
				<td>
					${raw(questioned)}
					<div style="margin-left: 50px;">
						${questionedDependency.toString().replace('[','').replace(']','') }
					</div>
				</td>
			</tr>
			<tr>
				<td>&nbsp;</td>
				<td>
					${raw(dependenciesNotValid)}
				</td>
			</tr>
			<tr>
				<td>&nbsp;</td>
				<td>
					${raw(issue)}<br />
					<table style="width: auto; margin-left: 100px;">
						<tr>
							<th>AssetName</th>
							<th>Comment</th>
							<th>Assigned To</th>
						</tr>
						<tbody>
							<g:if test="${issueMap.size()>0}">
								<g:each in="${issueMap}" var="issue" status="i">
									<tr class="${(i % 2) == 0 ? 'odd' : 'even'}">
										<td>
											${issue.assetEntity.assetName}
										</td>
										<td>
											${issue.comment}
										</td>
										<td>
											${issue.assignedTo}
										</td>
									</tr>
								</g:each>
							</g:if>
							<g:else>
								<tr>
									<td>No Issues</td>
								</tr>
							</g:else>
						</tbody>

					</table>
				</td>
			</tr>
			<tr>
				<td>&nbsp;</td>
				<td>
					${raw(eventIssues)}<br />
					<table style="width: auto; margin-left: 100px;">
						<tr>
							<th>Due Date</th>
							<th>Assigned To</th>
							<th>Status</th>
							<th>Category</th>
							<th>Comment</th>
						</tr>
						<tbody>
							<g:if test="${nonAssetIssue.size()>0}">
								<g:each in="${nonAssetIssue}" var="issue" status="i">
									<tr class="${(i % 2) == 0 ? 'odd' : 'even'}">
										<td>
											<tds:convertDate date="${issue.dueDate}"/>
										</td>
										<td>
											${issue.assignedTo}
										</td>
										<td>
											${issue.status}
										</td>
										<td>
											${issue.category}
										</td>
										<td>
											${issue.comment}
										</td>
									</tr>
								</g:each>
							</g:if>
							<g:else>
								<tr>
									<td colspan="2">No Special Instruction</td>
								</tr>
							</g:else>
						</tbody>
					</table>
				</td>
			</tr>
			<tr >
			  <td>&nbsp;</td>
				<td style="margin-left: 50px;"><g:if test="${allErrors.contains('Model')}">
							<span style="color: red;"><b>Model Check</b></span>
						</g:if> <g:else>
							<span style="color: green;"><b>Model Check</b></span>
						</g:else></td>
				</tr>
				<tr>
					<td>&nbsp;</td>
					<td>
						${raw(modelError)}
						<div style="margin-left: 50px">
							<b>${modelList.toString().replace('[','').replace(']','')}</b>
						</div>
					</td>
				</tr>
			<tr>
				<td><g:if test="${allErrors.contains('Teams')}">
						<span style="color: red;"><b><h2>Teams</h2></b></span>
					</g:if> <g:else>
						<span style="color: green;"><b><h2>Teams</h2></b></span>
					</g:else></td>
			</tr>
			<g:if test="${project.runbookOn == 0}">
			<tr>
				<td></td>
				<td><span style="color: green;"><b>Summary OK: <g:each
								in="${bundleMap}" var="bundle">
								${bundle.name}: ${bundle.size} teams.</g:each></b></span></td>
			</tr>
			</g:if>
			<tr>
				<td></td>
				<td>
					<g:render template="${project.runbookOn == 0  ? 'workflowTeamList' : 'functionTasks' }" model="[bundleMap:bundleMap]"> </g:render>
				</td>
			</tr>
			<%--<tr>
				<td>&nbsp;</td>
				<td>
					${teamAssignment}<br />
					<div style="margin-left: 50px">
						<g:each in="${notAssignedToTeam}" var="asset">
							${asset[0].toString()+','}
						</g:each>
						<b><g:if test="${notAssignedToTeam.size()>0}"> Assets Not Assigned . </g:if>
					</div> </b>
				</td>

			</tr>
			--%>
			<tr>
				<td>&nbsp;</td>
				<td>
					${raw(userLogin)}<br />
				<div style="margin-left: 50px">
						<g:each in="${inValidUsers}" var="user">
							<b>
								${user[0]}
							</b>
						</g:each>
					</div>
				</td>
			</tr>
			<tr>
				<td><g:if test="${allErrors.contains('Transport')}">
						<span style="color: red;"><b><h2>Transport</h2></b></span>
					</g:if> <g:else>
						<span style="color: green;"><b><h2>Transport</h2></b></span>
					</g:else></td>
			</tr>
			<tr>
				<td>&nbsp;</td>
				<td>
					${raw(truckError)}
					<div style="margin-left: 50px">
						${truck.toString().replace('[','').replace(']','')}
					</div>
				</td>
			</tr>
			<tr>
				<td>&nbsp;</td>
				<td>
					${raw(cartError)}
					<div style="margin-left: 50px">
							${cart.toString().replace('[','').replace(']','')}
					</div>
				</td>
			</tr>
			<tr>
				<td>&nbsp;</td>
				<td>
					${raw(shelfError)}
					<div style="margin-left: 50px">
							${shelf.toString().replace('[','').replace(']','')}
					</div>
				</td>
			</tr>

			<tr>
				<td>
					<g:if test="${allErrors.contains('Tasks')}">
						<span style="color: green;"><b><h2>Tasks</h2></b></span>
					</g:if>
					<g:else>
						<span style="color: red;"><b><h2>Tasks</h2></b></span>
					</g:else>
				</td>
			</tr>
			<tr>
				<td>&nbsp;</td>
				<td>
					<g:if test="${taskerrMsg}">
						<span style="color: green;"><b><h2>${taskerrMsg}</h2></b></span>
					</g:if>
					${cyclicalsError}
					<div style="margin-left: 50px">
						${cyclicalsRef.toString()}
					</div>
				</td>
			</tr>
			<tr>
				<td>&nbsp;</td>
				<td>
					${startsError}
					<div style="margin-left: 50px">
						${startsRef.toString()}
					</div>
				</td>
			</tr>
			<tr>
				<td>&nbsp;</td>
				<td>
					${sinksError}
					<div style="margin-left: 50px">
						${sinksRef.toString()}
					</div>
				</td>
			</tr>
			<tr>
				<td>&nbsp;</td>
				<td>
					${personAssignErr}
					<g:if test="${personTasks && personTasks.size()>0}">
						<table style="width: auto; margin-left: 100px;">
							<tr>
								<th>Task number</th>
								<th>Title</th>
								<th>TaskSpec</th>
							</tr>
							<tbody>
								<g:each in="${personTasks}" var="task" status="i">
									<tr class="${(i % 2) == 0 ? 'odd' : 'even'}">
										<td>
											${task.taskNumber}
										</td>
										<td>
											${task.comment}
										</td>
										<td>
											${task.taskSpec}
										</td>
									</tr>
								</g:each>
							</tbody>
						</table>
					</g:if>
				</td>
			</tr>
		</table>

	</div>
</body>
</html>
