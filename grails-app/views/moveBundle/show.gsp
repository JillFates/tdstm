

<html>
  <head>
    <meta http-equiv="Content-Type" content="text/html; charset=UTF-8"/>
    <meta name="layout" content="moveBundleHeader" />
   
    <title>Show MoveBundle</title>
  </head>
  <body>   
    
    <div class="body">
      <h1>Show MoveBundle</h1>
      <g:if test="${flash.message}">
        <div class="message">${flash.message}</div>
      </g:if>
      <div class="dialog">
        <table>
          <tbody>

            <tr class="prop">
              <td valign="top" class="name">Name:</td>

              <td valign="top" class="value">${fieldValue(bean:moveBundleInstance, field:'name')}</td>

            </tr>

            <tr class="prop">
              <td valign="top" class="name">Description:</td>

              <td valign="top" class="value">${fieldValue(bean:moveBundleInstance, field:'description')}</td>

            </tr>

            <tr class="prop">
              <td valign="top" class="name">Start Time:</td>

              <td valign="top" class="value"><tds:convertDateTime date="${moveBundleInstance?.startTime}" /></td>

            </tr>

            <tr class="prop">
              <td valign="top" class="name">Completion Time:</td>

              <td valign="top" class="value"><tds:convertDateTime date="${moveBundleInstance?.completionTime}" /></td>

            </tr>

            <tr class="prop">
              <td valign="top" class="name">Project Manager:</td>

              <td valign="top" class="value">
                <g:if test="${projectManager}">
                  ${projectManager?.partyIdTo?.lastName}, ${projectManager?.partyIdTo?.firstName} - ${projectManager?.partyIdTo?.title}
                </g:if>
              </td>

            </tr>

            <tr class="prop">
              <td valign="top" class="name">Move Manager:</td>

              <td valign="top" class="value">
                <g:if test="${moveManager}">
                  ${moveManager?.partyIdTo?.lastName}, ${moveManager?.partyIdTo?.firstName} - ${moveManager?.partyIdTo?.title}
                </g:if>
              </td>

            </tr>

            <tr class="prop">
              <td valign="top" class="name">Operational Order:</td>

              <td valign="top" class="value">${fieldValue(bean:moveBundleInstance, field:'operationalOrder')}</td>

            </tr>

          </tbody>
        </table>
      </div>
      <div class="buttons">
        <g:form>
          <input type="hidden" name="id" value="${moveBundleInstance?.id}" />
          <input type="hidden" name="projectId" value="${projectId}" />
          <span class="button"><g:actionSubmit class="edit" value="Edit" /></span>
          <span class="button"><g:actionSubmit class="delete" onclick="return confirm('Are you sure?');" value="Delete" /></span>
        </g:form>
      </div>
    </div>
  </body>
</html>
