

<html>
  <head>
    <meta http-equiv="Content-Type" content="text/html; charset=UTF-8"/>
    <meta name="layout" content="main" />
    <title>Show RoleType</title>
  </head>
  <body>
    <div class="body">
      <h1>Show RoleType</h1>
      <g:if test="${flash.message}">
        <div class="message">${flash.message}</div>
      </g:if>
      <div class="dialog">
        <table>
          <tbody>


            <tr class="prop">
              <td valign="top" class="name">Code:</td>

              <td valign="top" class="value">${fieldValue(bean:roleTypeInstance, field:'id')}</td>

            </tr>

            <tr class="prop">
              <td valign="top" class="name">Description:</td>

              <td valign="top" class="value">${fieldValue(bean:roleTypeInstance, field:'description')}</td>

            </tr>

          </tbody>
        </table>
      </div>
      <div class="buttons">
        <g:form>
          <input type="hidden" name="id" value="${roleTypeInstance?.id}" />
          <span class="button"><g:actionSubmit class="edit" value="Edit" /></span>
          <span class="button"><g:actionSubmit class="delete" onclick="return confirm('Are you sure?');" value="Delete" /></span>
        </g:form>
      </div>
    </div>
  </body>
</html>
