<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8"/>
        <meta name="layout" content="topNav" />
        <title>TransitionManager&trade; Reports Home</title>
    </head>
<body>
<div class="body">
<div>&nbsp;</div>
<div>
<g:if test="${flash.message}"><div class="message">${flash.message}</div>
</g:if>
<table style="border: 0">
	<tr>
	<tds:hasPermission permission='ShowDiscovery'>
		<td style="vertical-align:top">
			<div>
			<h1 style="margin-right: 0px;"><b>Discovery</b></h1>
			<table>
				<thead>
					<tr>
						<th>Report</th>
						<th>W</th>
						<th>E</th>
						<th>P</th>
					</tr>
				</thead>
				<tbody>
				<tr><td><a href="#">admin report1</a></td><td style="width:12px">W</td><td style="width:12px">E</td><td style="width:12px">P</td></tr>
				</tbody>
			</table>
			</div>
		</td>
	</tds:hasPermission>
	<tds:hasPermission permission='ShowMovePrep'>
		<td style="vertical-align:top">
			<div>
			<h1 style="margin-right: 0px;"><b>Move Prep</b></h1>
			<table>
				<thead>
					<thead>
						<tr>
						<th>Report</th>
						<th>W</th>
						<th>E</th>
						<th>P</th>
					</tr>
				</thead>
				<tbody>
				<tds:hasPermission permission="ShowMovePrep">
				<tr><td><a href="/tdstm/reports/preMoveCheckList" class="home mmlink" onclick="hideMegaMenu('reportsMegaMenu')">Pre-move Checklist</a> </td><td style="width:12px">W</td><td style="width:12px"> </td><td style="width:12px">P</td></tr>
				</tds:hasPermission>
				<tr><td><a href="/tdstm/reports/retrieveBundleListForReportDialog?reportId=Login+Badges" class="home">Login Badges</a> </td><td style="width:12px">W</td><td style="width:12px"> </td><td style="width:12px"> </td></tr>
				<tr><td><a href="/tdstm/reports/retrieveBundleListForReportDialog?reportId=Asset+Tag" class="home">Asset Tags</a> </td><td style="width:12px">W</td><td style="width:12px"> </td><td style="width:12px"> </td></tr>
				<tr><td><a href="/tdstm/reports/retrieveBundleListForReportDialog?reportId=Transportation+Asset+List" class="home">Transport Worksheets</a></td><td style="width:12px"> </td><td style="width:12px"> </td><td style="width:12px">P</td></tr>
				</tbody>
			</table>
			</div>
		</td>
		</tds:hasPermission>
		<tds:hasPermission permission='ShowMoveDay'>
		<td style="vertical-align:top">
			<div>
			<h1 style="margin-right: 0px;"><b>Move Day</b></h1>
			<table class="planning-application-table">
				<thead>
					<thead>
						<tr>
						<th>Report</th>
						<th>W</th>
						<th>E</th>
						<th>P</th>
					</tr>
				</thead>
				<tbody>
			<tr><td><a href="/tdstm/reports/retrieveBundleListForReportDialog?reportId=Issue+Report" class="home">Issue Report</a> </td><td style="width:12px"> </td><td style="width:12px">E</td><td style="width:12px">P</td></tr>
			<tr><td><a href="/tdstm/reports/retrieveBundleListForReportDialog?reportId=CablingQA" class="home">Cabling QA</a> </td><td style="width:12px"> </td><td style="width:12px"> </td><td style="width:12px">P</td></tr>
				</tbody>
			</table>
			</div>
		</td>
		</tds:hasPermission>
		<td style="vertical-align:top">
			<div>
			<h1 style="margin-right: 0px;"><b>Application</b></h1>
			<table class="planning-application-table">
				<thead>
					<thead>
						<tr>
						<th>Report</th>
						<th>W</th>
						<th>E</th>
						<th>P</th>
					</tr>
				</thead>
				<tbody>
				<tr>
				<tr><td><a href="#">app report1</a></td><td style="width:12px">W</td><td style="width:12px">E</td><td style="width:12px">P</td>
				</tr>
				</tbody>
			</table>
			</div>
		</td>
		<td style="vertical-align:top">
			<div>
			<h1 style="margin-right: 0px;"><b>Infrastructure</b></h1>
			<table class="planning-application-table">
				<thead>
					<thead>
						<tr>
						<th>Report</th>
						<th>W</th>
						<th>E</th>
						<th>P</th>
					</tr>
				</thead>
				<tbody>
			<tr><td><a href="/tdstm/reports/retrieveBundleListForReportDialog?reportId=CablingConflict" class="home">Cabling Conflict</a> </td><td style="width:12px"> </td><td style="width:12px"> </td><td style="width:12px">P</td></tr>
			<tr><td><a href="/tdstm/reports/retrieveBundleListForReportDialog?reportId=CablingData" class="home">Cabling Data</a> </td><td style="width:12px"> </td><td style="width:12px"> </td><td style="width:12px">P</td></tr>
			<tr><td><a href="/tdstm/reports/powerReport" class="home">Power</a> </td><td style="width:12px">W</td><td style="width:12px">E</td><td style="width:12px">P</td></tr>
				</tbody>
			</table>
			</div>
		</td>
	</tr>
</table>
</div>
</div>
<script>
	$(document).ready(function() {
	    currentMenuId = "#reportsMenu";
		$('.menu-reports-report-summary').addClass('active');
		$('.menu-parent-reports').addClass('active');
	});
</script>
</body>
</html>
