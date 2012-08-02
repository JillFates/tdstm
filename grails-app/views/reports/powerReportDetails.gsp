<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
<title>Rack Elevation Report</title>
<script type="text/javascript">
	$(document).ready(function() {
	    currentMenuId = "#reportsMenu";
	    $("#reportsMenuId a").css('background-color','#003366')
	});
</script>
</head>
<body>
	<table>
		<thead>
			<tr>
				<th>Location</th>
				<th>Room</th>
				<th>Rack</th>
				<th>Devices</th>
				<th>A</th>
				<th>B</th>
				<th>C</th>
				<th>TBD</th>
				<th>Totals</th>
			</tr>
		</thead>
		<tbody>
			<g:each in="${reportDetails}" var="details" status="i" >
				<tr class="${(i % 2) == 0 ? 'odd' : 'even'}">
					<td>${details.location}</td>
					<td>${details.room}</td>
					<td>${details.rack}</td>
					<td>${details.devices}</td>
					<td>${details.powerA}</td>
					<td>${details.powerB}</td>
					<td>${details.powerC}</td>
					<td>${details.powerTBD}</td>
					<td>${details.totalPower}</td>
				</tr>
			</g:each>
		</tbody>
	</table>
</body>
</html>