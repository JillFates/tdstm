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
			</h1></span> <span style="text-align: center; color: black;"><h3>
				${time}
			</h3></span>

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
				<td><span style="color: red"><b> ${moveEvent.name } :
							${inProgressError}
					</b></span></td>
			</tr>
			<tr>
				<td></td>
				<td><span style="color: green"><b>Staff</b></span>: <span
					style="color: red"><b> ${clientAccess}
					</b></span></td>
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
				<td><span style="color: green;"><b><h2>Event/Bundle</h2></b></span></td>
			</tr>
			<tr>
				<td></td>
				<td><span style="color: green;"><b>Bundels:OK ${moveBundleSize}:${moveBundles}</b></span></td>
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
						</g:each></b></span></td>

			</tr>
			<tr>
				<td></td>

				<td><span style="color: green;"><b>DashBoard OK</b><br></br></span><span>
						<g:each in="${steps}" var="workFlow">
							${workFlow.key}:${workFlow.value}<br></br>
						</g:each></b>
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
					${duplicates} <g:each in="${duplicatesAssetNames}" var="duplicate">
						${duplicate.counts} duplicates Named "${duplicate.assetName}"<br></br>
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
					${issue}<br /> <g:each in="${issueMap}" var="issue">
						<b>
							${issue.assetEntity.assetName}:"${issue.comment}"
						</b>
						<br />
					</g:each>
				</td>
			</tr>
		</table>

	</div>
</body>
</html>
