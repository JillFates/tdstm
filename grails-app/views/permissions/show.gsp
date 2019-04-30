<%@page import="net.transitionmanager.security.Permissions" %>
<%@page import="net.transitionmanager.security.RolePermissions" %>
<%@page import="net.transitionmanager.security.Permission"%>
<html>
<head>
	<meta http-equiv="Content-Type" content="text/html; charset=UTF-8"/>
	<meta name="layout" content="topNav" />
	<title>Show Role Permissions</title>
	<g:javascript src="jqgrid-support.js" />
</head>
<body>
	<g:if test="${flash.message}">
		<div class="message">${flash.message}</div>
	</g:if>
	<tds:hasPermission permission="${Permission.AdminMenuView}">
		<tds:subHeader title="Role Permissions" crumbs="['Admin','Roles']"/>
	<div class="body">

		<div id="buttonsDivId" class="buttons">
			<g:form action="edit">
				<span class="button">
					<g:actionSubmit type="button" class="edit" value="Edit"/>
				</span>
			</g:form>
		</div>
		<div class="list" id="updateShow">
			<table id="showPermissionsTableId">
				<thead class="needHeaderBorder">
					<tr id="headerRowId" class="needsBorder" style="border: ; text-align: left">
						<th>Permission Item</th>
						<g:each in="${Permissions.Roles.values()}">
							<th>${it}</th>
						</g:each>
						<th>Description</th>
					</tr>
				</thead>
				<tbody>
					<g:each in="${permissions}" var="permission" status="i">
						<tr class="${(i % 2) == 0 ? 'odd' : 'even'}" align="center">
							<td style="text-align: left;">
								${permission.permissionItem}
							</td>
							<g:each in="${Permissions.Roles.values()}" var='role'>
								<g:if test="${permission.rolePermissions.find {it.role == role?.name()}}">
									<td style="text-align: center;background-color:lightGreen;">Yes</td>
								</g:if>
								<g:else>
									<td style="text-align: center;">-</td>
								</g:else>
							</g:each>
							<td style="text-align: left;">${permission.description}</td>
						</tr>
					</g:each>
				</tbody>
			</table>
		</div>
	</div>
	</tds:hasPermission>
	<script>
		currentMenuId = "#adminMenu";
		$('.menu-admin-role').addClass('active');
		$('.menu-parent-admin').addClass('active');

		// handle the scrolling header
		var headTable;
		var scrollLimit;
		var header;
		var leftOffset;
		$(window).scroll( function() {
			handleHeaderPositionGeneral(scrollLimit, header, 0, leftOffset);
		});

		$(document).ready(function() {
			headTable = $('#buttonsDivId');
			scrollLimit = headTable.offset().top + headTable.height();
			header = $('#headerRowId');
			leftOffset = $('#showPermissionsTableId').offset().left;
		});
	</script>
</body>
</html>
