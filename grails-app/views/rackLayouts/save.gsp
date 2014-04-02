<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
<link type="text/css" rel="stylesheet" href="${resource(dir:'css',file:'ui.core.css')}" />
<link type="text/css" rel="stylesheet" href="${resource(dir:'css',file:'ui.dialog.css')}" />
<link type="text/css" rel="stylesheet" href="${resource(dir:'css',file:'ui.resizable.css')}" />
<link type="text/css" rel="stylesheet" href="${resource(dir:'css',file:'ui.slider.css')}" />
<link type="text/css" rel="stylesheet" href="${resource(dir:'css',file:'ui.tabs.css')}" />
<link type="text/css" rel="stylesheet" href="${resource(dir:'css',file:'ui.theme.css')}" />
<link type="text/css" rel="stylesheet" href="${resource(dir:'css',file:'rackLayout.css')}" />
<title>Rack Elevation Report</title>
</head>
<body>
	<g:if test="${params.commit == 'Generate'}">
		<table style="border: none; width: auto">
			<tr>
	</g:if>
	<g:else>
		<div class="body">
	</g:else>

	<g:if test="${rackLayout}">
	<g:each in="${rackLayout}" var="rackLayout">
		<g:if test="${frontView}">
		<g:if test="${params.commit == 'Generate'}">
			<td class="rack_elevation_td">
		</g:if>
			
		<table cellpadding=2 class="rack_elevation front">
			<tr>
				<td colspan="4" style="border:0px;text-align:center">
					<div class="close-icon" onclick="$(this).parents('td.rack_elevation_td').remove(); $(this).parents('table.rack_elevation').remove();"></div>
					<h2>${rackLayout?.rack} in ${rackLayout?.room}</h2>
				</td>
			</tr>
			<tr>
				<th></th>
				<th>Device</th>
				<th>Bundle</th>
				<%--<th>U</th>--%>
			</tr>
			${rackLayout?.frontViewRows}
		</table>
		<hr style="page-break-after:always"/>
		<g:if test="${params.commit == 'Generate'}">
		</td>
		</g:if>

		</g:if>
		<g:if test="${backView}">
		<g:if test="${params.commit == 'Generate'}">
			<td class="rack_elevation_td">
		</g:if>
		<table cellpadding=2 class="rack_elevation back">
			<tr>
				<td colspan="4" style="border:0px;text-align:center">
					<div class="close-icon" onclick="$(this).parents('td.rack_elevation_td').remove(); $(this).parents('table.rack_elevation').remove();"></div>
					<h2>${rackLayout?.rack} in ${rackLayout?.room} (Back)
					<g:if test="${params.commit != 'Print View'}">
					 <tds:hasPermission permission="AssetEdit">
						<g:if test="${showIconPref =='true'}">
							<span id="span_${rackLayout?.rackId}" onclick="disableCreateIcon(${rackLayout?.rackId})"> 
								<img src='../images/minus.gif' /></span>
						</g:if>
						<g:else>
							<span id="span_${rackLayout?.rackId}" onclick="enableCreateIcon(${rackLayout?.rackId})"> 
								<img src="../images/plus.gif" /></span>
						</g:else>
						<span id="anchor_${rackLayout?.rackId}" onclick="assignPowers(${rackLayout?.rackId})"> 
						</span>
					</tds:hasPermission>
					</g:if>
					 </h2>
				</td>
			</tr>
			<tr>
				<th></th>
				<th>Device</th>
				<th>Bundle</th>
				<th>Cabling</th>
			</tr>
			${rackLayout?.backViewRows}
		</table>
		<br class="page-break-after"/>
		<g:if test="${params.commit == 'Generate'}">
		</td>
		</g:if>
		</g:if>
	</g:each>
	</g:if>
	<g:else>
	<table><tr><td class="no_records">No reports found</td></tr></table>
	</g:else>
	</div>

	<g:if test="${params.commit == 'Generate'}">
		</tr>
		</table>
	</g:if>
	<g:else>
		</div>
	</g:else>

<script>
	currentMenuId = "";
	$("#rackMenuId a").css('background-color','#003366')
</script>
</body>
</html>
