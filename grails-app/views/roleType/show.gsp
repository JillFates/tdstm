<html>
  <head>
    <meta http-equiv="Content-Type" content="text/html; charset=UTF-8"/>
    <meta name="layout" content="topNav" />
    <title>Show RoleType</title>
  </head>
  <body>
    <tds:subHeader title="Show RoleType" crumbs="['Admin','Portal','Role Type','Show']"/><br/>
    <div class="body">
      <div class="nav" style="border: 1px solid #CCCCCC; height: 24px">
	            <span class="menuButton"><g:link class="list" action="list">RoleType List</g:link></span>
	          <tds:hasPermission permission='CreateRoleType'>
	            <span class="menuButton"><g:link class="create" action="create">Create RoleType</g:link></span>
	          </tds:hasPermission>
        	</div>
        	<br/>
      <g:if test="${flash.message}">
        <div class="message">${flash.message}</div>
      </g:if>
      <div class="dialog">
        <table>
          <tbody>

           <tr class="prop">
              <td valign="top" class="name">Type:</td>

              <td valign="top" class="value">${fieldValue(bean:roleTypeInstance, field:'type')}</td>

            </tr>

          <g:if test="${roleTypeInstance.type == 'SECURITY'}">
           <tr class="prop">
              <td valign="top" class="name">Level:</td>

              <td valign="top" class="value">${fieldValue(bean:roleTypeInstance, field:'level')}</td>

            </tr>
          </g:if>


            <tr class="prop">
              <td valign="top" class="name">Code:</td>

              <td valign="top" class="value">${fieldValue(bean:roleTypeInstance, field:'id')}</td>

            </tr>

            <tr class="prop">
              <td valign="top" class="name">Description:</td>

              <td valign="top" class="value">${fieldValue(bean:roleTypeInstance, field:'description')}</td>

            </tr>
              <tr class="prop">
              <td valign="top" class="name">Help:</td>

              <td valign="top" class="value">${fieldValue(bean:roleTypeInstance, field:'help')}</td>
            </tr>

          </tbody>
        </table>
      </div>
      <tds:hasPermission permission='RoleTypeEditView'>
      <div class="buttons">
        <g:form>
          <input type="hidden" name="id" value="${roleTypeInstance?.id}" />
          <span class="button"><g:actionSubmit class="edit" value="Edit" /></span>
          <span class="button"><g:actionSubmit class="delete" onclick="return confirm('Are you sure?');" value="Delete" /></span>
        </g:form>
      </div>
      </tds:hasPermission>
    </div>
  </body>
</html>