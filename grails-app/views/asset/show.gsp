

<html>
  <head>
    <meta http-equiv="Content-Type" content="text/html; charset=UTF-8"/>
    <meta name="layout" content="main" />
    <title>Show Asset</title>

    <g:javascript library="jquery"/>

    <link type="text/css" rel="stylesheet" href="http://ui.jquery.com/testing/themes/base/ui.all.css" />
    <script type="text/javascript" src="http://ui.jquery.com/testing/jquery-1.3.1.js"></script>
    <script type="text/javascript" src="http://ui.jquery.com/testing/ui/ui.core.js"></script>
    <script type="text/javascript" src="http://ui.jquery.com/testing/ui/ui.draggable.js"></script>
    <script type="text/javascript" src="http://ui.jquery.com/testing/ui/ui.resizable.js"></script>
    <script type="text/javascript" src="http://ui.jquery.com/testing/ui/ui.dialog.js"></script>

    <script>

      $(document).ready(function() {

        $("#dialog").dialog({ autoOpen: false })

      })

    </script>

    <g:javascript>

      function showEdit() {

      $("#dialog").dialog("open")

      }
    </g:javascript>

  </head>
  <body>
    <div class="nav">
      <span class="menuButton"><g:link class="home" controller="auth" action="home">Home</g:link></span>
      <span class="menuButton"><g:link class="list" action="list">Asset List</g:link></span>
      <span class="menuButton"><g:link class="create" action="create">New Asset</g:link></span>
    </div>
    <div class="body">
      <h1>Show Asset</h1>
      <g:if test="${flash.message}">
        <div class="message">${flash.message}</div>
      </g:if>
      <div class="dialog">
        <table>
          <tbody>


            <tr class="prop">
              <td valign="top" class="name">Id:</td>

              <td valign="top" class="value">${fieldValue(bean:assetInstance, field:'id')}</td>

            </tr>

            <tr class="prop">
              <td valign="top" class="name">Project Name:</td>

              <td valign="top" class="value"><g:link controller="project" action="show" id="${assetInstance?.project?.id}">${assetInstance?.project?.encodeAsHTML()}</g:link></td>

            </tr>

            <tr class="prop">
              <td valign="top" class="name">Asset Type:</td>

              <td valign="top" class="value"><g:link controller="assetType" action="show" id="${assetInstance?.assetType?.id}">${assetInstance?.assetType?.encodeAsHTML()}</g:link></td>

            </tr>

            <tr class="prop">
              <td valign="top" class="name">Asset Name:</td>

              <td valign="top" class="value">${fieldValue(bean:assetInstance, field:'assetName')}</td>

            </tr>

            <tr class="prop">
              <td valign="top" class="name">Asset Tag:</td>

              <td valign="top" class="value">${fieldValue(bean:assetInstance, field:'assetTag')}</td>

            </tr>

            <tr class="prop">
              <td valign="top" class="name">Serial Number:</td>

              <td valign="top" class="value">${fieldValue(bean:assetInstance, field:'serialNumber')}</td>

            </tr>

            <tr class="prop">
              <td valign="top" class="name">Device Function:</td>

              <td valign="top" class="value">${fieldValue(bean:assetInstance, field:'deviceFunction')}</td>

            </tr>

          </tbody>
        </table>
      </div>
      <div class="buttons">
        <g:form>
          <input type="hidden" name="id" value="${assetInstance?.id}" />
          <span class="button"><input type="button" class="edit" value="Edit" onClick="return showEdit()"/></span>
          <span class="button"><g:actionSubmit class="delete" onclick="return confirm('Are you sure?');" value="Delete" /></span>
        </g:form>
      </div>
    </div>

    <div id="dialog" title="Edit Asset">

      <g:form method="post" >
        <input type="hidden" name="id" value="${assetInstance?.id}" />
        <div class="dialog">
          <table>
            <tbody>

              <tr class="prop">
                <td valign="top" class="name">
                  <label for="project">Project :</label>
                </td>
                <td valign="top" class="value ${hasErrors(bean:assetInstance,field:'project','errors')}">
                  <g:select optionKey="id" from="${Project.list()}" name="project.id" value="${assetInstance?.project?.id}" ></g:select>
                  <g:hasErrors bean="${assetInstance}" field="project">
                    <div class="errors">
                      <g:renderErrors bean="${assetInstance}" as="list" field="project"/>
                    </div>
                  </g:hasErrors>
                </td>
              </tr>

              <tr class="prop">
                <td valign="top" class="name">
                  <label for="assetType">Asset Type:</label>
                </td>
                <td valign="top" class="value ${hasErrors(bean:assetInstance,field:'assetType','errors')}">
                  <g:select optionKey="id" from="${AssetType.list()}" name="assetType.id" value="${assetInstance?.assetType?.id}" noSelection="['null':'']"></g:select>
                  <g:hasErrors bean="${assetInstance}" field="assetType">
                    <div class="errors">
                      <g:renderErrors bean="${assetInstance}" as="list" field="assetType"/>
                    </div>
                  </g:hasErrors>
                </td>
              </tr>

              <tr class="prop">
                <td valign="top" class="name">
                  <label for="assetName">Asset Name:</label>
                </td>
                <td valign="top" class="value ${hasErrors(bean:assetInstance,field:'assetName','errors')}">
                  <input type="text" id="assetName" name="assetName" value="${fieldValue(bean:assetInstance,field:'assetName')}"/>
                  <g:hasErrors bean="${assetInstance}" field="assetName">
                    <div class="errors">
                      <g:renderErrors bean="${assetInstance}" as="list" field="assetName"/>
                    </div>
                  </g:hasErrors>
                </td>
              </tr>

              <tr class="prop">
                <td valign="top" class="name">
                  <label for="assetTag">Asset Tag:</label>
                </td>
                <td valign="top" class="value ${hasErrors(bean:assetInstance,field:'assetTag','errors')}">
                  <input type="text" id="assetTag" name="assetTag" value="${fieldValue(bean:assetInstance,field:'assetTag')}"/>
                  <g:hasErrors bean="${assetInstance}" field="assetTag">
                    <div class="errors">
                      <g:renderErrors bean="${assetInstance}" as="list" field="assetTag"/>
                    </div>
                  </g:hasErrors>
                </td>
              </tr>

              <tr class="prop">
                <td valign="top" class="name">
                  <label for="serialNumber">Serial Number:</label>
                </td>
                <td valign="top" class="value ${hasErrors(bean:assetInstance,field:'serialNumber','errors')}">
                  <input type="text" id="serialNumber" name="serialNumber" value="${fieldValue(bean:assetInstance,field:'serialNumber')}"/>
                  <g:hasErrors bean="${assetInstance}" field="serialNumber">
                    <div class="errors">
                      <g:renderErrors bean="${assetInstance}" as="list" field="serialNumber"/>
                    </div>
                  </g:hasErrors>
                </td>
              </tr>

              <tr class="prop">
                <td valign="top" class="name">
                  <label for="deviceFunction">Device Function:</label>
                </td>
                <td valign="top" class="value ${hasErrors(bean:assetInstance,field:'deviceFunction','errors')}">
                  <input type="text" id="deviceFunction" name="deviceFunction" value="${fieldValue(bean:assetInstance,field:'deviceFunction')}"/>
                  <g:hasErrors bean="${assetInstance}" field="deviceFunction">
                    <div class="errors">
                      <g:renderErrors bean="${assetInstance}" as="list" field="deviceFunction"/>
                    </div>
                  </g:hasErrors>
                </td>
              </tr>

            </tbody>
          </table>
        </div>
        <div class="buttons">
          <span class="button"><g:actionSubmit class="save" value="Update" /></span>

        </div>
      </g:form>

    </div>

  </body>
</html>
