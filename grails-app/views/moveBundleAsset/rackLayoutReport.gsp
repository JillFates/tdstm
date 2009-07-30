<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
<title>Rack Elevation Report</title>
<style type="text/css">
.page-break {
	display: block;
	page-break-before: always;
}

.empty {
	background: #e7e7e7;
}

.rack_current {
	background: #7CFE80;
	font-weight: bold;
	font-size: 1.1em;
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
	font-size: 1.1em;
	border: 2px dashed black !important;
}

table {
	border: 1px solid #000000;
}

td {
	text-align: center;
	font-size: .9em;
	border: 1px solid #999999;
}

th {
	font-family: arial, helivetica, san-serif;
	font-size: 1.0em;
	font-weight: bold;
	text-decoration: underline;
}

.footer {
	font-family: arial, helivetica, san-serif;
	font-size: 1.4em;
	font-weight: bold;
}

}
td {
	border: 1px solid #95999A;
}

.rack_elevation {
	font-family: arial, helivetica, san-serif;
	font-size: 10px;
}
</style>
</head>
<body>
<div class="body"><g:each in="${rackLayout}" var="rackLayout">
	<div class="page-break">
	<table cellpadding=2 class="rack_elevation">
		<tr>
			<td colspan="13">
			<h2>Room: ${rackLayout?.room} - Rack: ${rackLayout?.rack}</h2>
			</td>
		</tr>
		<tr>
			<th>U</th>
			<th>Device</th>
			<th>Bundle</th>
			<g:if test="${rackLayout?.backView}">
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
			</g:if>
		</tr>
		${rackLayout?.rows}
	</table>
	</div>
</g:each>
</div>
</body>
</html>
