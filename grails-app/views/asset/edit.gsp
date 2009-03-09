

<html>
  <head>
    <meta http-equiv="Content-Type" content="text/html; charset=UTF-8"/>
    <meta name="layout" content="main" />
    <title>Edit Asset</title>
  </head>
  <body>
    <div class="menu2">
      <ul>
        <li><g:link class="home" controller="projectUtil">Project </g:link> </li>
        <li><g:link class="home" controller="asset">Assets </g:link></li>
        <li><g:link class="home" controller="asset" action="assetImport" >Import/Export</g:link> </li>
        <li><a href="#">Team </a></li>
        <li><a href="#">Contacts </a></li>
        <li><a href="#">Applications </a></li>
        <li><a href="#">Move Bundles </a></li>
      </ul>
    </div>

    <div class="body">
      <h1>Edit Asset</h1>
      <g:if test="${flash.message}">
        <div class="message">${flash.message}</div>
      </g:if>
      <g:hasErrors bean="${assetInstance}">
        <div class="errors">
          <g:renderErrors bean="${assetInstance}" as="list" />
        </div>
      </g:hasErrors>
      <g:form method="post" >
        <input type="hidden" name="id" value="${assetInstance?.id}" />
        <div class="dialog">
          <table>
            <tbody>

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
          <span class="button"><g:actionSubmit class="delete" onclick="return confirm('Are you sure?');" value="Delete" /></span>
        </div>
      </g:form>
    </div>
  </body>
</html>
