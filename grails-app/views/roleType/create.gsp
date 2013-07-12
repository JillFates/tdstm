<html>
  <head>
    <meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
    <meta name="layout" content="projectHeader" />
    <title>Create RoleType</title>
  </head>
  <body>
    <div class="body">
      <h1>Create RoleType</h1>
      <div class="nav" style="border: 1px solid #CCCCCC; height: 11px">
            	<span class="menuButton"><g:link class="list" action="list">RoleType List</g:link></span>
	  </div>
	  <br/>
      <g:if test="${flash.message}">
        <div class="message">${flash.message}</div>
      </g:if> <g:form action="save" method="post">
        <div class="dialog">
          <table>
            <tbody>
				<tr>
					<td colspan="2"><div class="required"> Fields marked ( * ) are mandatory </div> </td>
				</tr>
              <tr class="prop">
                <td valign="top" class="name"><label for="id"><b>Code:&nbsp;<span style="color: red">*</span></b></label></td>
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
        <div class="buttons"><span class="button"><input class="save" type="submit" value="Save" /></span></div>
    </g:form></div>
  </body>
</html>