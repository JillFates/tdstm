<%@ page contentType="text/html;charset=ISO-8859-1"%>
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=ISO-8859-1" />
<meta name="layout" content="projectHeader" />
<title>Planning Stats</title>
</head>
<body>
	<div class="body">
		<div>
			<h1>Move Planning DashBoard</h1>

			<div>
				<table style="border: 0px; width: 1000px;">
					<thead>
						<th style="background-color: white;">&nbsp;</th>
						<th style="background-color: white;">&nbsp;</th>
						<th style="background-color: white;"><g:each
								in="${moveBundleList}" var="bundle">
								<th
									style="color: Blue; background-color: white; text-decoration: underline;"><b>${bundle.name}</b>
								</th>
							</g:each></th>
						<th
							style="color: Blue; background-color: white; text-decoration: underline;">To
							Be Assigned</th>
					</thead>
					<tbody>
						<tr>
							<td style="color: blue"><b>Applications</b>
							</td>
							<td style="color: black"><b>Assigned</b>
							</td>
							<td><g:each in="${AppList}" var="appCount">
									<td><b>${appCount.count}</b>
									</td>
								</g:each></td>
							<td style="color: red;"><b>${unassignedAppCount}</b>
							</td>
							<td><b>${percentageAppCount}%&nbsp;&nbsp;&nbsp;assigned
							</b>
							</td>
						</tr>
						<tr>
							<td style="color: black"><b>${applicationCount}</b>
							</td>
							<td style="color: grey"><b>Optional</b>
							</td>
						</tr>
						<tr>
							<td style="color: blue"><b></b>
							</td>
							<td style="color: grey"><b>Potential</b>
							</td>
						</tr>

						<tr>
							<td style="color: blue"><b>Servers</b>
							</td>
							<td style="color: black"><b>Assigned</b>
							</td>
							<td><g:each in="${AssetList}" var="assetCount">
									<td><b>${assetCount.count}</b>
									</td>
								</g:each></td>
							<td style="color: red;"><b>${unassignedPhysicalAssetCount}</b>
							</td>
							<td><b>${percentageAssetCount}%&nbsp;&nbsp;&nbsp;assigned
							</b>
							</td>
						</tr>
						<tr>
							<td style="color: black"><b>${physicalCount}&nbsp;
									Physical</b>
							</td>
							<td style="color: grey">&nbsp;</td>
							<td><g:each in="${AssetList}" var="assetCount">
									<td><b>${assetCount.physicalCount}</b>
									</td>
								</g:each></td>
							<td style="color: red;"><b>${unassignedPhysicalAssetCount}</b>
							</td>
						</tr>
						<tr>
							<td style="color: black"><b>${virtualCount}&nbsp;
									Virtual</b>
							</td>
							<td style="color: grey">&nbsp;</td>
							<td><g:each in="${AssetList}" var="assetCount">
									<td><b>${assetCount.virtualAssetCount}</b>
									</td>
								</g:each></td>
							<td style="color: red;"><b>${unassignedVirtualAssetCount}</b>
							</td>
						</tr>

					</tbody>
				</table>
			</div>
			<div style="margin-top: 40px; margin-left: 100px;">
				<h3>
					<b>Dependency Resolutions</b>
				</h3>
				<table style="border: 0px; margin-left: 100px;">
					<tr>
						<td style="width: 150px;">App Dependencies</td>
						<td>${appDependencies}</td>
					</tr>
					<tr>
						<td>Server Dependencies</td>
						<td>${serverDependenciesCount}</td>
					</tr>
					<tr>
						<td>Open Issue</td>
						<td>${appDependencies}</td>
					</tr>
				</table>
			</div>
			<div style="margin-top:-90px; margin-left: 550px;">
				<h3>
					<b>Latency Grouping</b>
				</h3>
				<table style="border: 0px; margin-left: 50px;">
					<tr>
						<td style="width: 50px;">0 </td>
						<td>impact likely</td>
					</tr>
					<tr>
						<td>163</td>
						<td>UnKnown</td>
					</tr>
					<tr>  	
						<td>13</td>
						<td>UnLikely</td>
					</tr>
				</table>

			</div>
		</div>
</body>
</html>