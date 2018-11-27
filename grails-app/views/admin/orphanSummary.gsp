<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
<meta name="layout" content="topNav" />
<title>Orphan records list</title>
<g:javascript src="orphanData.js" />
<style type="text/css">
a:hover {
	text-decoration: underline;
}
</style>
</head>
<body>
	<tds:subHeader title="Summary Report" crumbs="['Admin','Portal','Summary Report']"/>
<div class="body">
<div>&nbsp;</div>
	<div>
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
				<td><g:if test="${summary.type && summary.type != 'Null'}">${summary.type}</g:if></td>
				<td style="text-align: center;"><a href='javascript:getOrphanDetails("${summary.mainTable}","${summary.refId}","${summary.type}")'>${summary.totalCount}</a></td>
				<g:set var="counter" value="${counter + 1}" />
			</tr>
			</g:each>
		</g:each>
	</tbody>
</table>
</div>
</div>
<br/><br/><br/><br/><br/><br/><br/><br/><br/><br/>
<div style="margin: 0 15px 10px;">
<div id="orphanDetailed" style="display: none;">
<h1><b>Detailed Report</b></h1>
<div style="float: left;">
	<ul><li><b>Table name : </b><span id="tableName"></span></li>
	<li><b>Ref. Column name : </b><span id="columnName"></span></li></ul>
		<br/>
	</div>
	<div style="float: left;margin-left: 10px">
	<a href="javascript:showQuery()">Download SQL script</a>
	</div>

	<table id="orphanDetailsTableId">
	<tr>
		<td><asset:image src="images/processing.gif"/></td>
	</tr>
	</table>
</div>
</div>
<div id="queryTextDialog" title="SQL Script" style="display:none;">
	<table>
         <tbody>
            <tr>
				<td id="queryText" style="font-size: 12px;"></td>
			</tr>
         </tbody>
	</table>
</div>
<script type="text/javascript">
   	$(document).ready(function() {
    	$("#queryTextDialog").dialog({ autoOpen: false })
    })
	function getOrphanDetails( table, column, type ){
   		$("#orphanDetailed").show()
   		$("#tableName").html(table)
   		$("#columnName").html(column)
   		$("#queryText").html("")
   		$("#orphanDetailsTableId").html("<tr><td><asset:image src='images/processing.gif'/></td></tr>")
		$("#tableName").html(table);
		$("#columnName").html(column);
		${remoteFunction(action:'orphanDetails', params:'\'table=\' + table +\'&column=\'+column +\'&type=\'+type', onComplete:'showOrphanDetails(XMLHttpRequest, table )')}
	}
	function showOrphanDetails(e, table){
		var records = eval('(' + e.responseText + ')');
		var tableBody = getDeatiledReport( records.orphanDeatils, table );
		$("#orphanDetailsTableId").html(replaceNullsAndUndefined(tableBody));
		$("#orphanDetailed").show();
		$("#queryText").html(records.query)
	}
	function showQuery(){
		$("#queryTextDialog").dialog('option', 'width', 500);
		$('#queryTextDialog').dialog('option', 'modal', 'true');
		$('#queryTextDialog').dialog('open');
		$('div.ui-dialog.ui-widget');
	}
	function replaceNullsAndUndefined(tableBody) {
        return tableBody.replace(/(null|undefined)/mg, "");
	}
</script>
<script>
	currentMenuId = "#adminMenu";
	$('.menu-admin-portal').addClass('active');
	$('.menu-parent-admin').addClass('active');
</script>
</body>
</html>
