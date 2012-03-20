<%@ page contentType="text/html;charset=ISO-8859-1"%>
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=ISO-8859-1" />
<meta name="layout" content="projectHeader" />
<title>Planning Console</title>
</head>
<body>
	<div class="body">
		<div style="float: left;">
		<h1>Dependency Console</h1>
		</div>
		<div style="float: left;margin-left: 50px;">
			<h1>Dependency Analysis</h1>
			<div style="float: left;">
				<h3>Connection Status</h3>
				<g:each in="${dependencyType}" var="dependency">
					<input type="checkbox" id="dType_${dependency.id}"
						name="dType_${dependency.id}" />&nbsp;&nbsp;<span
						id="dependecy_${dependency.id}"> ${dependency.value}
					</span>
					<br></br>
				</g:each>
			</div>
			<div style="float: left;" class="buttonR">
				<input type="submit" class="submit" style="float: right; margin-top: 50px" value="Generate" />
			</div>

		</div>
		<div style="clear: both;"></div>
		<div style="margin-top: 40px;">
			<div>
				<div style="margin-left: 20px; margin-bottom: 10px;">
				<h3><b>Dependency Bundling</b></h3>&nbsp;03/13/2012: There were	${unassignedAppCount} Dependency-bundles discovered
				</div>
				<table border="0" cellpadding="4" cellspacing="0"
					style="margin-left: 20px;">
					<tr class="odd">
						<td><b>Dependency Bundles</b></td>
						<td><g:remoteLink action="dependencyBundleDetails" onComplete="jQuery('#dependencyBundleDetailsId').html(e.responseText)" >1</g:remoteLink></td>
						<td><g:remoteLink action="dependencyBundleDetails" onComplete="jQuery('#dependencyBundleDetailsId').html(e.responseText)" >2</g:remoteLink></td>
						<td><g:remoteLink action="dependencyBundleDetails" onComplete="jQuery('#dependencyBundleDetailsId').html(e.responseText)" >3</g:remoteLink></td>
						<td><g:remoteLink action="dependencyBundleDetails" onComplete="jQuery('#dependencyBundleDetailsId').html(e.responseText)" >4</g:remoteLink></td>
						<td><g:remoteLink action="dependencyBundleDetails" onComplete="jQuery('#dependencyBundleDetailsId').html(e.responseText)" >5</g:remoteLink></td>
						<td><g:remoteLink action="dependencyBundleDetails" onComplete="jQuery('#dependencyBundleDetailsId').html(e.responseText)" >6</g:remoteLink></td>
						<td><g:remoteLink action="dependencyBundleDetails" onComplete="jQuery('#dependencyBundleDetailsId').html(e.responseText)" >7</g:remoteLink></td>
						<td><g:remoteLink action="dependencyBundleDetails" onComplete="jQuery('#dependencyBundleDetailsId').html(e.responseText)" >8</g:remoteLink></td>
						<td><g:remoteLink action="dependencyBundleDetails" onComplete="jQuery('#dependencyBundleDetailsId').html(e.responseText)" >9</g:remoteLink></td>
						<td><g:remoteLink action="dependencyBundleDetails" onComplete="jQuery('#dependencyBundleDetailsId').html(e.responseText)" >10</g:remoteLink></td>
					</tr>
					<tr class="even">
						<td><b>Applications</b></td>
						<td>10</td>
						<td>20</td>
						<td>3</td>
						<td>14</td>
						<td>5</td>
						<td>2</td>
						<td>6</td>
						<td>2</td>
						<td>3</td>
						<td>1</td>
					</tr>
					<tr class="odd">
						<td><b>Physical Servers</b></td>
						<td>41</td>
						<td>12</td>
						<td>3</td>
						<td>14</td>
						<td>15</td>
						<td>16</td>
						<td>7</td>
						<td>8</td>
						<td>1</td>
						<td>0</td>
					</tr>
					<tr class="even">
						<td><b>Virtual Servers</b></td>
						<td>11</td>
						<td>52</td>
						<td>13</td>
						<td>04</td>
						<td>15</td>
						<td>16</td>
						<td>07</td>
						<td>18</td>
						<td>0</td>
						<td>0</td>
					</tr>
				</table>
			</div>
		</div>
		<div style="clear: both;"></div>
		<div id="dependencyBundleDetailsId">
		</div>
	</div>
</body>
</html>