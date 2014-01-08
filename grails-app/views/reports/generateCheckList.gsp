<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
<meta name="layout" content="projectHeader" />
<title>Pre-Event Checklist</title>
<script>
	$(document).ready(function() {
	    currentMenuId = "#reportsMenu";
	    $("#reportsMenuId a").css('background-color','#003366')
	});
</script>
</head>
<body>
	<div>
		<div style="margin-top: 20px; color: black; font-size: 20px;text-align: center;" >
			<b>Pre-Event Checklist - ${project.name} : ${moveEvent.name }</b>
		</div> 
		<div style="color: black; font-size: 15px;text-align: center;">
			${time}
		</div>
		${eventErrorString}




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
					${errorForEventTime}
				</td>
			</tr>
			<tr>
				<td></td>
				<td>
					${inProgressError}
				</td>
			</tr>
			<tr>
				<td></td>
				<td><span style="color: green"><b>Staff</b></span>:${clientAccess.toString().replace('[','').replace(']','')}
				</td>
			</tr>
			<tr>
				<td></td>
				<td>
					${userLoginError}
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
											${staff.company[0]}
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
					${blankAssets} 
					 <g:if test="${nullAssetname.size()>0 }">
						<span style="color: red;margin-left: 50px;"> Blank names: ${nullAssetname.size()} Assets with no name-${nullAssetname.tag.toString().replace('[','').replace(']','')}</span>
					 </g:if>
					<br/>
				</td>
			</tr>
			<tr>
				<td></td>
				<td>
					${duplicates} <g:each in="${duplicatesAssetNames}" var="duplicate">
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
					${blankAssetTag} 
					<g:if test="${nullAssetTag.size()>0 }">
						<span style="color: red;margin-left: 50px;"> Blank names: ${nullAssetTag.size()} Assets with no tag- ${nullAssetTag.assetName.toString().replace('[','').replace(']','')}</span>
					</g:if>
						<br />
				</td>
			</tr>
			<tr>
				<td></td>
				<td>
					${duplicatesTag} <g:each in="${duplicatesAssetTagNames}"
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
					${missedRacks}<br />
					<div style="margin-left: 50px; text-align: left;">
						${missingRacks.toString().replace('[','').replace(']','')}
					</div>
				</td>
			</tr>
			<tr>
				<td>&nbsp;</td>
				<td>
					${dependenciesOk}
				</td>
			</tr>
			<tr>
				<td>&nbsp;</td>
				<td>
					${questioned}
					<div style="margin-left: 50px;">
						${questionedDependency.toString().replace('[','').replace(']','') }
					</div>
				</td>
			</tr>
			<tr>
				<td>&nbsp;</td>
				<td>
					${dependenciesNotValid}
				</td>
			</tr>
			<tr>
				<td>&nbsp;</td>
				<td>
					${issue}<br />
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
					${eventIssues}<br />
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
						${modelError}
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
			--%><tr>
				<td>&nbsp;</td>
				<td>
					${userLogin}<br />
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
					${truckError}
					<div style="margin-left: 50px">
						${truck.toString().replace('[','').replace(']','')}
					</div>
				</td>
			</tr>
			<tr>
				<td>&nbsp;</td>
				<td>
					${cartError}
					<div style="margin-left: 50px">
							${cart.toString().replace('[','').replace(']','')}
					</div>
				</td>
			</tr>
			<tr>
				<td>&nbsp;</td>
				<td>
					${shelfError}
					<div style="margin-left: 50px">
							${shelf.toString().replace('[','').replace(']','')}
					</div>
				</td>
			</tr>
			
			
			
			
		</table>

	</div>
</body>
</html>
