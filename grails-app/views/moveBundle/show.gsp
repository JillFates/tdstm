

<html>
  <head>
    <meta http-equiv="Content-Type" content="text/html; charset=UTF-8"/>
    <meta name="layout" content="main" />
    <title>Show MoveBundle</title>
  </head>
  <body>
    <div class="menu2">
      <ul>
        <li><g:link class="home" controller="projectUtil">Project </g:link> </li>
        <li><g:link class="home" controller="person" action="projectStaff" params="[projectId:projectId]" >Staff</g:link></li>
        <li><g:link class="home" controller="assetEntity" params="[projectId:projectId]">Assets </g:link></li>
	<li><g:link class="home" controller="assetEntity" action="assetImport" params="[projectId:projectId]">Import/Export</g:link> </li>
        <li><a href="#">Contacts </a></li>
        <li><a href="#">Applications </a></li>
        <li><g:link class="home" controller="moveBundle" params="[projectId:projectId]">Move Bundles</g:link> </li>
      </ul>
    </div>
    <div class="menu2" style="background-color:#003366;">
      <ul>
      <li class="title1">Move Bundle: ${moveBundleInstance?.name}</li>
        <li><g:link class="home" controller="projectTeam" action="list" params="[bundleId:moveBundleInstance?.id]" >Team </g:link> </li>
        <li><g:link controller="moveBundleAsset" action="assignAssetsToBundle" params="[bundleId:moveBundleInstance?.id]" >Bundle Asset Assignment</g:link> </li>
        <li><g:link class="home" controller="moveBundleAsset" action="bundleTeamAssignment" params="[bundleId:moveBundleInstance?.id, rack:'UnrackPlan']" >Bundle Team Assignment </g:link> </li>
      </ul>
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
