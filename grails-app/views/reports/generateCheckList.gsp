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
							in="${workFlowCodeSelected}" var="workFlow">
							${workFlow.key}:${workFlow.value}<br></br>
						</g:each></b></span></td>

			</tr>
			<tr>
				<td></td>

				<td><span style="color: green;"><b>DashBoard OK</b><br></br></span><span
					style="color: green;"> <g:each in="${steps}" var="workFlow">
							${workFlow.key}:${workFlow.value}<br></br>
						</g:each></b></span></td>

			</tr>
		</table>

	</div>
</body>
</html>
