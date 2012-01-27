<html>
  <head>
    <meta http-equiv="Content-Type" content="text/html; charset=UTF-8"/>
    <meta name="layout" content="projectHeader" />
    <title>Show Role Permissions</title>
  </head>
  <body>
  <g:if test="${flash.message}">
      <div class="message">${flash.message}</div>
    </g:if>
    <div class="body">
    <h1>Role Permissions</h1>
    <div class="list" id="updateShow">
			<table>
				<thead>
					<tr style="border: ; text-align: left">
						<th>Group</th>
						<th>Permission Item</th>
						<g:each in="${Permissions.Roles.values()}">
							<th>
								${it}
							</th>
						</g:each>
					</tr>
				</thead>
				<tbody>
					<g:each var="permissionGroup" in="${permissions}" var="permission" status="i">
						<tr class="${(i % 2) == 0 ? 'odd' : 'even'}" align="center">
							<td style="text-align: center;">
								${permission.permissionGroup.key}
							</td>
							<td style="text-align: center;">
								${permission.permissionItem}
							</td>
							<g:each in="${Permissions.Roles.values()}" var='role'>
								<td style="text-align: center;">
									${RolePermissions.findByRoleAndPermission(role.toString(), permission) ? 'yes' :'-'}
								</td>
							</g:each>
						</tr>
					</g:each>
				</tbody>
			</table>
		</div>   
    <div class="buttons">
      <g:form action="edit">
          <span class="button">
            <g:actionSubmit type="button" class="edit" value="Edit"/>
          </span>
      </g:form>
    </div></div>
<script>
	currentMenuId = "#adminMenu";
	$("#adminMenuId a").css('background-color','#003366')
</script>
</body>
</html>
