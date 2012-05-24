<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
<meta name="layout" content="projectHeader" />
<title>Pre-Move Check List</title>
</head>
<body>
	<div>
		<span style="text-align: center; color: black;"><h1>
				<b>Pre-Move CheckList - ${project.name} : ${moveEvent.name }</b>
			</h1></span> <span style="text-align: center; color: black;">
			<h3>
				${time}
			</h3></span>${eventErrorString}
			
			
			

		<table>
			<tr>
				<td><span style="color: red;"><b><h2>Project</h2></b></span></td>
			</tr>
			<tr>
				<td></td>
				<td>
					${errorForEventTime}
				</td>
			</tr>
			<tr>
				<td></td>
				<td>${inProgressError}</td>
			</tr>
			<tr>
				<td></td>
				<td><span style="color: green"><b>Staff</b></span>:${clientAccess} </td>
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
					<table style="width: auto">
						<thead>
							<th>Staff Name</th>
							<th>Company</th>
							<th>Role</th>
						</thead>
						<tbody>
							<g:each in="${list}" var="staff">
								<tr>
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
							</g:each>
						</tbody>
					</table>
				</td>
			</tr>
			<tr>
				<td><span style="color: green;"><h2><b>Event/Bundle</b></h2></span></td>
			</tr>
			<tr>
				<td></td>
				<td><span style="color: green;"><b>Bundles:OK &nbsp;&nbsp; ${moveBundleSize}: &nbsp;&nbsp;${moveBundles}</b></span></td>
			</tr>
			<tr>
				<td></td>
				<td><span style="color: green;"><b>WorkFlow:OK </b></span></td>
			</tr>
			<tr>
				<td></td>
				<td><span style="color: green;"> <g:each
							in="${workFlowCodeSelected}" var="workFlow" status="i">
							${workFlow.key}:${workFlow.value}<br></br>
						</g:each></span></td>

			</tr>
			<tr>
				<td></td>

				<td><span style="color: green;"><b>DashBoard OK</b><br></br></span><span>
						<g:each in="${steps}" var="workFlow">
						  <g:if test="${workFlow.value=='No steps created'}"> 
							 	<span style="color:red">${workFlow.key}:${workFlow.value}</span><br></br>
						  </g:if>
						  <g:else>
						  		<span style="color:green">${workFlow.key}:${workFlow.value}</span><br></br>
						  </g:else>
						</g:each>
				</span></td>

			</tr>
			<tr>
				<td><span style="color: red;"><b><h2>Assets</h2></b></span></td>
			</tr>
			<tr>
				<td></td>
				<td><span style="color: green;"><b>Summary : OK <br></br>
							<g:each in="${summaryOk}" var="summary">
								${summary.key}:${summary.value}<br></br>
							</g:each>
					</b></span></td>
			</tr>
			<tr>
			   <td></td>
				<td>
				${blankAssets}
				<g:each in="${nullAssetname}" var="nullName">
				    <span style="color:red;"> Blank names: ${nullName.counts}-${nullName.type}</span><br/>
				</g:each>
				</td>
			</tr>
			<tr>
				<td></td>
				<td>
					${duplicates} 
					<g:each in="${duplicatesAssetNames}" var="duplicate">
						<span style="color:red;" >${duplicate.counts} duplicates Named "${duplicate.assetName} "-(${duplicate.type})</span><br></br>
					</g:each>
				</td>
			</tr>
			<tr>
				<td></td>
				<td>
					${duplicatesTag} <g:each in="${duplicatesAssetTagNames}"
						var="duplicate">
						${duplicate.counts} duplicates Named "${duplicate.tag}"<br></br>
					</g:each>
				</td>
			</tr>
			<tr>
				<td>&nbsp;</td>
				<td>
					${missedRacks}<br /> ${missingRacks}
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
					${questioned} - <span style="color: red"> <b>${questionedDependency}</b></span>
				</td>
			</tr>
			<tr>
				<td>&nbsp;</td>
				<td>
					${issue}<br /> <g:each in="${issueMap}" var="issue">
						<b> ${issue.assetEntity.assetName}:${issue.comment}
						</b>
						<br />
					</g:each>
				</td>
			</tr>
			<tr>
				<td>&nbsp;</td>
				<td>
					${importantInstruction}<br /> <g:each in="${specialInstruction}" var="instruction">
						<b> ${instruction.assetEntity.assetName}:${instruction.comment}
						</b>
						<br />
					</g:each>
				</td>
			</tr>
			<tr>
				<td><span style="color: red;"><b><h2>Teams</h2></b></span></td>
			</tr>
			<tr>
				<td></td>
				<td><span style="color: green;"><b>Summary OK : <g:each
								in="${bundleMap}" var="bundle">
								${bundle.name}:${bundle.size} teams.</g:each></b></span></td>
			</tr>
			<tr>
				<td></td>
				<td><g:each in="${bundleMap}" var="bundle">
				<g:if test="${bundle.teamList.size()>0}">
						<h3>
							<b>
								${bundle.name}
							</b>
						</h3>
						<br />
						<table style="width:700px">
							<thead>
								<tr>
									<th width="100px;">Team</th>
									<th width="100px;">Role</th>
									<th width="150px;">Team Members</th>
									<th width="50px;">Assets</th>
								</tr>
							</thead>
							<tbody>
								<g:each in="${bundle.teamList}" var="teamList">
									<tr>
										<td>
											${teamList.name[0]}
										</td>
										<td>
											${teamList.role[0]}
										</td>
										<td>
											<table style="border: 0px;" >
												<tr>
													<td><g:each in="${teamList.teamList[0]}" var="teamListtaff">
															<tr>
																<td >
																	${teamListtaff.company[0]}:${teamListtaff.name}
																</td>
															</tr>
														</g:each></td>
												</tr>
											</table>
										</td>
										<td>
											${teamList.assetSize[0]}
										</td>
									</tr>
								</g:each>
							</tbody>
						</table>
						</g:if>
					</g:each></td>
					</tr>
					<tr>
						<td>&nbsp;</td>
						<td>
							${teamAssignment}<br /> <g:each in="${notAssignedToTeam}" var="asset">
								${asset[0]}, 
							</g:each><b> Not Assigned .</b>
						</td>
				
					</tr>
					<tr>
						<td>&nbsp;</td>
						<td>
							${userLogin}<br /> <g:each in="${inValidUsers}" var="user">
								<b>${user[0]}</b>
							</g:each>
						</td>
					</tr>
					<tr>
					  <td><span style="color: green;"><b><h2>Transport</h2></b></span></td>
					</tr>
					<tr>
					  <td>&nbsp;</td>
					  <td>${truckError} <g:each in="${truck}" var="truck">${truck}</g:each> </td>
					</tr>
					<tr>
					  <td>&nbsp;</td>
					  <td>${cartError} <g:each in="${cart}" var="cart">${cart}</g:each> </td>
					</tr>
					<tr>
					  <td>&nbsp;</td>
					  <td>${shelfError}  <g:each in="${shelf}" var="shelf">${shelf}</g:each> </td>
					</tr>
		</table>

	</div>
</body>
</html>
