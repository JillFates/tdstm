<html>
  <head>
    <meta http-equiv="Content-Type" content="text/html; charset=UTF-8"/>
    <meta name="layout" content="main" />
    <title>Edit MoveBundleAsset</title>
  </head>
  <body>
    <div class="nav">
      <span class="menuButton"><a class="home" href="${resource(dir:'')}">Home</a></span>
      <span class="menuButton"><g:link class="list" action="list">MoveBundleAsset List</g:link></span>
      <span class="menuButton"><g:link class="create" action="create">Create MoveBundleAsset</g:link></span>
    </div>
    <div class="body">
      <h1>Edit MoveBundleAsset</h1>
      <g:if test="${flash.message}">
        <div class="message">${flash.message}</div>
      </g:if>
      <g:hasErrors bean="${moveBundleAssetInstance}">
        <div class="errors">
          <g:renderErrors bean="${moveBundleAssetInstance}" as="list" />
        </div>
      </g:hasErrors>
      <g:form method="post" >
        <input type="hidden" name="id" value="${moveBundleAssetInstance?.id}" />
        <div class="dialog">
          <table>
            <tbody>

              <tr class="prop">
                <td valign="top" class="name">
                  <label for="moveBundle">Bundle:</label>
                </td>
                <td valign="top" class="value ${hasErrors(bean:moveBundleAssetInstance,field:'moveBundle','errors')}">
                  <g:select optionKey="id" from="${MoveBundle.list()}" name="moveBundle.id" value="${moveBundleAssetInstance?.moveBundle?.id}" ></g:select>
                </td>
              </tr>

              <tr class="prop">
                <td valign="top" class="name">
                  <label for="asset">Asset:</label>
                </td>
                <td valign="top" class="value ${hasErrors(bean:moveBundleAssetInstance,field:'asset','errors')}">
                  <g:select optionKey="id" from="${Asset.list()}" name="asset.id" value="${moveBundleAssetInstance?.asset?.id}" ></g:select>
                </td>
              </tr>

              <tr class="prop">
                <td valign="top" class="name">
                  <label for="sourceTeamMt">Source Team:</label>
                </td>
                <td valign="top" class="value ${hasErrors(bean:moveBundleAssetInstance,field:'sourceTeamMt','errors')}">
                  <g:select optionKey="id" from="${ProjectTeam.list()}" name="sourceTeamMt.id" value="${moveBundleAssetInstance?.sourceTeamMt?.id}" ></g:select>
                </td>
              </tr>

              <tr class="prop">
                <td valign="top" class="name">
                  <label for="targetTeamMt">Target Team:</label>
                </td>
                <td valign="top" class="value ${hasErrors(bean:moveBundleAssetInstance,field:'targetTeamMt','errors')}">
                  <g:select optionKey="id" from="${ProjectTeam.list()}" name="targetTeamMt.id" value="${moveBundleAssetInstance?.targetTeamMt?.id}" ></g:select>
                </td>
              </tr>

            </tbody>
          </table>
        </div>
        <div class="buttons">
          <span class="button"><g:actionSubmit class="save" value="Update" /></span>
          <span class="button"><g:actionSubmit class="delete" onclick="return confirm('Are you sure?');" value="Delete" /></span>
        </div>
      </g:form>
    </div>
  </body>
</html>
