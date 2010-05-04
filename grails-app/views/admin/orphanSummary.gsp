<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
<meta name="layout" content="main" />
<title>JsecUser List</title>
<style type="text/css">
a:hover {
	text-decoration: underline;
}
</style>
</head>
<body>
<div class="body">
<div>&nbsp;</div>
	<div>
	<h1 style="margin: 0px;"><b>Summary Report</b></h1>
 	<g:if test="${flash.message}">
            <div class="message">${flash.message}</div>
 	</g:if>
<table>
	<thead>
		<tr>
			<th>Table</th>
			<th>Ref. Column</th>
			<th>Type</th>
			<th>Total Orphan Records</th>
		</tr>
	</thead>
	<tbody>
		<g:set var="counter" value="${0}" />
		<g:each in="${summaryRecords}" var="summaryRecord">
			<g:each in="${summaryRecord}" var="summary">
			<tr class="${(counter % 2) == 0 ? 'even' : 'odd'}">
				<td>${summary.mainTable}</td>
				<td>${summary.refId}</td>
				<td>${summary.type}</td>
				<td style="text-align: center;"><a href='javascript:getOrphanDetails("${summary.mainTable}","${summary.refId}","${summary.type}")'>${summary.totalCount}</a></td>
				<g:set var="counter" value="${counter + 1}" />
			</tr>
			</g:each>
		</g:each>
	</tbody>
</table>
</div>
<div id="orphanDetailed" style="display: none;">
<h1><b>Detailed Report</b></h1>
<div style="float: left;">
	<ul><li><b>Table name : </b><span id="tableName"></span></li>
	<li><b>Ref. Column name : </b><span id="columnName"></span></li></ul>
		<br/>
	</div>
	<div style="float: left;margin-left: 10px">
	<a href="javascript:alert('Implementation in progress')">Download SQL script</a>
	</div>

	<table>
		<thead>
			<tr>
				<th>Table index Id</th>
				<th>Ref. Column Id</th>
			</tr>
		</thead>
		<tbody  id="orphanDetailsTbodyId"></tbody>
	</table>
</div>
</div>
<script type="text/javascript">
	function getOrphanDetails( table, column, type ){
		$("#orphanDetailed").hide()
		$("#tableName").html(table);
		$("#columnName").html(column);
		${remoteFunction(action:'orphanDetails', params:'\'table=\' + table +\'&column=\'+column +\'&type=\'+type', onComplete:'showOrphanDetails(e)')}
	}
	function showOrphanDetails(e){
		var orphanDetails = eval('(' + e.responseText + ')');
		var totalRecords = orphanDetails.length;
		var tbody =""
		if(totalRecords != 0){
			for( i = 0; i < totalRecords; i++){
				var cssClass = 'odd'
				if(i % 2 == 0){
					cssClass = 'even'
				}
				var orphanRecord = orphanDetails[i]
				tbody +="<tr class='"+cssClass+"'><td>"+orphanRecord.tableId+"</td><td>"+orphanRecord.refId+"</td></tr>"
			}
		}
		$("#orphanDetailsTbodyId").html(tbody)
		$("#orphanDetailed").show();
	}
</script>
</body>
</html>
