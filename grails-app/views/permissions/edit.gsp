<%@page import="net.transitionmanager.security.Permissions" %>
<%@page import="net.transitionmanager.security.RolePermissions" %>
<%@page import="net.transitionmanager.security.Permission"%>
<html>
<head>
	<meta http-equiv="Content-Type" content="text/html; charset=UTF-8"/>
	<meta name="layout" content="topNav" />
	<title>Update Role Permissions</title>
	<g:javascript src="jqgrid-support.js" />
</head>
<body>
	
	<g:if test="${flash.message}">
		<div class="message">${flash.message}</div>
	</g:if>
	<tds:hasPermission permission="${Permission.AdminMenuView}">
		<tds:subHeader title="Role Permissions" crumbs="['Admin','Roles','Edit']"/><br/>
		<div class="body">
			<g:form action="update">
				<div class="list" id="updateShow">
					<table id="editPermissionsTableId">
						<thead class="needHeaderBorder">
							<tr id="headerRowId" style="border: ; text-align: left">
								<th>Permission Item</th>
								<g:each in="${Permissions.Roles.values()}">
									<th>
										${it}
									</th>
								</g:each>
								<th>Description</th>
							</tr>
						</thead>
						<tbody>
							<g:each in="${permissions}" var="permission" status="i">
							<input type="hidden" name="column" value="${permission.id}"/>
								<tr class="${(i % 2) == 0 ? 'odd' : 'even'}" align="center">
									<td style="text-align: left;">
										${permission.permissionItem}
									</td>
									<g:each in="${Permissions.Roles.values()}" var='role'>
										<g:if test="${permission.rolePermissions.find {it.role == role?.name()}}">
											<td style="text-align: center;background-color:lightGreen;">
												<input type="checkbox" name="role_${permission.id}_${role.toString()}" checked="checked">
											</td>
										</g:if>
										<g:else>
											<td style="text-align: center;">
												<input type="checkbox" name="role_${permission.id}_${role.toString()}">
											</td>
										</g:else>
									</g:each>
									<td style="text-align: center;">
									  <input type="text" name="description_${permission.id}" value="${permission.description}" style="width:350px">
									</td>
								</tr>
							</g:each>
						</tbody>
					</table>
				</div>
				<div id="buttonsDivId" class="buttons">
					<span class="button">
						<g:actionSubmit type="button" class="save" value="Update"/>
						<g:actionSubmit type="button" class="delete" value="Cancel" action="show"/>
					</span>
				</div>
			</g:form>
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
		// for IE
		$('#editPermissionsTableId tr').each(function (i, o) {
			$(o).height($(o).height());
		});

		headTable = $('#editPermissionsTableId');
		scrollLimit = headTable.offset().top;
		header = $('#headerRowId');
		leftOffset = headTable.offset().left;
	});
</script>
</body>
</html>
