<html>
  <head>
    <meta http-equiv="Content-Type" content="text/html; charset=UTF-8"/>
    <meta name="layout" content="projectHeader" />
    <title>Update Role Permissions</title>
  </head>
  <body>
  <g:if test="${flash.message}">
      <div class="message">${flash.message}</div>
    </g:if>
    <tds:hasPermission permission='AdminMenuView'>
    <div class="body">
    <h1>Role Permissions</h1>
    <g:form action="update">
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
						<th>Description</th>
					</tr>
				</thead>
				<tbody>
					<g:each var="permissionGroup" in="${permissions}" var="permission" status="i">
					<input type="hidden" name="column" value="${permission.id}"/>
						<tr class="${(i % 2) == 0 ? 'odd' : 'even'}" align="center">
							<td style="text-align: left;">
								${permission.permissionGroup.key}
							</td>
							<td style="text-align: left;">
								${permission.permissionItem}
							</td>
							<g:each in="${Permissions.Roles.values()}" var='role'>
								<g:if test="${RolePermissions.findByRoleAndPermission(role.toString(), permission)}">
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
							  <input type="text" name="description_${permission.id}" value="${permission.description}" style="width:250px"> 
							</td>
						</tr>
					</g:each>
				</tbody>
			</table>
		</div>   
    <div class="buttons">
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
	$("#adminMenuId a").css('background-color','#003366')
</script>
</body>
</html>
