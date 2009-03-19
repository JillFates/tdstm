

<html>
  <head>
    <meta http-equiv="Content-Type" content="text/html; charset=UTF-8"/>
    <meta name="layout" content="main" />
    <title>Show MoveBundle</title>
  </head>
  <body>
    <div class="nav">
      <span class="menuButton"><a class="home" href="${createLinkTo(dir:'')}">Home</a></span>
      <span class="menuButton"><g:link class="list" action="list">MoveBundle List</g:link></span>
      <span class="menuButton"><g:link class="create" action="create">New MoveBundle</g:link></span>
    </div>
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

              <td valign="top" class="value">${fieldValue(bean:moveBundleInstance, field:'startTime')}</td>

            </tr>

            <tr class="prop">
              <td valign="top" class="name">Finish Time:</td>

              <td valign="top" class="value">${fieldValue(bean:moveBundleInstance, field:'finishTime')}</td>

            </tr>

            <tr class="prop">
              <td valign="top" class="name">Bundle Order:</td>

              <td valign="top" class="value">${fieldValue(bean:moveBundleInstance, field:'bundleOrder')}</td>

            </tr>

          </tbody>
        </table>
      </div>
      <div class="buttons">
        <g:form>
          <input type="hidden" name="id" value="${moveBundleInstance?.id}" />
          <span class="button"><g:actionSubmit class="edit" value="moveBundleEdit" /></span>
          <span class="button"><g:actionSubmit class="delete" onclick="return confirm('Are you sure?');" value="Delete" /></span>
        </g:form>
      </div>
    </div>
  </body>
</html>
