

<html>
  <head>
    <meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
    <meta name="layout" content="main" />
    <title>Create RoleType</title>
  </head>
  <body>
    <div class="body">
      <h1>Create RoleType</h1>
      <g:if test="${flash.message}">
        <div class="message">${flash.message}</div>
      </g:if> <g:form action="save" method="post">
        <div class="dialog">
          <table>
            <tbody>

              <tr class="prop">
                <td valign="top" class="name"><label for="id">Code:</label></td>
                <td valign="top"
                    class="value ${hasErrors(bean:roleTypeInstance,field:'id','errors')}">
                  <input type="text" id="id" name="id"
                         value="${fieldValue(bean:roleTypeInstance,field:'id')}" /> <g:hasErrors
                    bean="${roleTypeInstance}" field="id">
                    <div class="errors"><g:renderErrors
                      bean="${roleTypeInstance}" as="list" field="id" /></div>
                </g:hasErrors></td>
              </tr>
              <tr class="prop">
                <td valign="top" class="name"><label for="description">Description:</label>
                </td>
                <td valign="top"
                    class="value ${hasErrors(bean:roleTypeInstance,field:'description','errors')}">
                  <input type="text" id="description" name="description"
                         value="${fieldValue(bean:roleTypeInstance,field:'description')}" />
                  <g:hasErrors bean="${roleTypeInstance}" field="description">
                    <div class="errors"><g:renderErrors
                      bean="${roleTypeInstance}" as="list" field="description" /></div>
                </g:hasErrors></td>
              </tr>

            </tbody>
          </table>
        </div>
        <div class="buttons"><span class="button"><input
          class="save" type="submit" value="Create" /></span></div>
    </g:form></div>
  </body>
</html>
