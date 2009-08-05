<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
<title>Rack Elevation Report</title>
<style type="text/css">

br.page-break-after {
	page-break-after: always;
}

.empty {
	background: #e7e7e7;
}

.rack_current {
	background: #7CFE80;
	font-weight: bold;
	font-size: 1.0em;
	border: 2px solid black;
}

.rack_past {
	background: #ffffff;
}

.rack_future {
	background: #F7D375;
}

.rack_error {
	background: #D80B01 !important;
	color: white !important;
	font-size: 1.0em;
	border: 2px dashed black !important;
}

h2 {
	text-size:1.1em;
	font-weight:bold;
	margin:1px;
}

table {
	border: 1px solid #000000;
	empty-cells: show;
}

td {
	text-align: center;
	font-size: .8em;
	border: 1px solid #999999;
	padding:0px 2px 0px 2px;
}

th {
	font-family: arial, helivetica, san-serif;
	font-size: 1.0em;
	font-weight: bold;
	text-decoration: underline;
	padding:0px 2px 0px 2px;

}

.rack_elevation {
	font-family: arial, helivetica, san-serif;
	font-size: 10px;
}

</style>
</head>
<body>
<div class="body">
<g:each in="${rackLayout}" var="rackLayout">
	<g:if test="${frontView}">
	<table cellpadding=2 class="rack_elevation">
		<tr>
			<td colspan="13" style="border:0px;"><h2>Room: ${rackLayout?.room} - Rack: ${rackLayout?.rack}</h2></td>
		</tr>
		<tr>
			<th>U</th>
			<th>Device</th>
			<th>Bundle</th>
			<th>U</th>
		</tr>
		${rackLayout?.frontViewRows}
	</table>
	<br class="page-break-after"/>
	</g:if>
	<g:if test="${backView}">
	<table cellpadding=2 class="rack_elevation">
		<tr>
			<td colspan="13" style="border:0px;"><h2>Room: ${rackLayout?.room} - Rack: ${rackLayout?.rack}</h2></td>
		</tr>
		<tr>
			<th>U</th>
			<th>Device</th>
			<th>Bundle</th>
			<th>Cabling</th>
			<th>U</th>
			<th>Pos</th>
			<th>PDU</th>
			<th>NIC</th>
			<th>Mgmt</th>
			<th>KVM</th>
			<th>Fiber</th>
			<th>Amber</th>
			<th>OK</th>
		</tr>
		${rackLayout?.backViewRows}
	</table>
	<br class="page-break-after"/>
	</g:if>
</g:each>
</div>
</body>
</html>
