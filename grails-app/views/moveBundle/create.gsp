

<html>
  <head>
    <meta http-equiv="Content-Type" content="text/html; charset=UTF-8"/>
    <meta name="layout" content="main" />
    <title>Create MoveBundle</title>
  </head>
  <body>
    <div class="nav">
      <span class="menuButton"><a class="home" href="${createLinkTo(dir:'')}">Home</a></span>
      <span class="menuButton"><g:link class="list" action="list">MoveBundle List</g:link></span>
    </div>
    <div class="body">
      <h1>Create MoveBundle</h1>
      <g:if test="${flash.message}">
        <div class="message">${flash.message}</div>
      </g:if>
      <g:hasErrors bean="${moveBundleInstance}">
        <div class="errors">
          <g:renderErrors bean="${moveBundleInstance}" as="list" />
        </div>
      </g:hasErrors>
      <g:form action="save" method="post" >

        <div class="dialog">
          <table>
            <tbody>


              <tr class="prop">
                <td valign="top" class="name">
                  <label for="name">Name:</label>
                </td>
                <td valign="top" class="value ${hasErrors(bean:moveBundleInstance,field:'name','errors')}">
                  <input type="text" id="name" name="name" value="${fieldValue(bean:moveBundleInstance,field:'name')}"/>
                </td>
              </tr>

              <tr class="prop">
                <td valign="top" class="name">
                  <label for="description">Description:</label>
                </td>
                <td valign="top" class="value ${hasErrors(bean:moveBundleInstance,field:'description','errors')}">
                  <input type="text" id="description" name="description" value="${fieldValue(bean:moveBundleInstance,field:'description')}"/>
                </td>
              </tr>

              <tr class="prop">
                <td valign="top" class="name">
                  <label for="startTime">Start Time:</label>
                </td>
                <td valign="top" class="value ${hasErrors(bean:moveBundleInstance,field:'startTime','errors')}">
                  <g:datePicker name="startTime" value="${moveBundleInstance?.startTime}" noSelection="['':'']"></g:datePicker>
                </td>
              </tr>

              <tr class="prop">
                <td valign="top" class="name">
                  <label for="finishTime">Finish Time:</label>
                </td>
                <td valign="top" class="value ${hasErrors(bean:moveBundleInstance,field:'finishTime','errors')}">
                  <g:datePicker name="finishTime" value="${moveBundleInstance?.finishTime}" noSelection="['':'']"></g:datePicker>
                </td>
              </tr>

              <tr class="prop">
                <td valign="top" class="name">
                  <label for="bundleOrder">Bundle Order:</label>
                </td>
                <td valign="top" class="value ${hasErrors(bean:moveBundleInstance,field:'bundleOrder','errors')}">
                  <input type="text" id="bundleOrder" name="bundleOrder" value="${fieldValue(bean:moveBundleInstance,field:'bundleOrder')}" />
                </td>
              </tr>

            </tbody>
          </table>
        </div>
        <div class="buttons">
          <span class="button"><input class="save" type="submit" value="Create" /></span>
        </div>
      </g:form>
    </div>
  </body>
</html>
