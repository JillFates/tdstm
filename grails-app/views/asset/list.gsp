

<html>
  <head>
    <meta http-equiv="Content-Type" content="text/html; charset=UTF-8"/>
    <meta name="layout" content="main" />
    <title>Asset List</title>

    <g:javascript library="prototype"/>
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
        $("#dialog1").dialog({ autoOpen: false })

      })

    </script>

    <g:javascript>
      function showAssetDialog( e ) {

      var asset = eval('(' + e.responseText + ')')

      //set values to dialog from response

      document.getElementById('id').value= asset.id
      if ( asset.assetType != null ) {
      document.getElementById('assetTypes').value = asset.assetTypeId
      document.getElementById('assetType.id').value = asset.assetTypeId
      }else{
      document.getElementById('assetTypes').value = ""
      document.getElementById('assetType.id').value = null
      }
      document.getElementById('assetNames').value = asset.assetName
      document.getElementById('assetName').value = asset.assetName
      document.getElementById('assetTags').value = asset.assetTag
      document.getElementById('assetTag').value = asset.assetTag
      document.getElementById('serialNumbers').value = asset.serialNumber
      document.getElementById('serialNumber').value = asset.serialNumber
      document.getElementById('deviceFunctions').value = asset.deviceFunction
      document.getElementById('deviceFunction').value = asset.deviceFunction

      $("#dialog").dialog('option', 'width', 400)
      $("#dialog").dialog("open")

      }

      function editAssetDialog() {

      $("#dialog").dialog("close")
      $("#dialog1").dialog('option', 'width', 500)
      $("#dialog1").dialog("open")

      }

      function showEditAsset(e) {
      var asset = eval('(' + e.responseText + ')')
      if ( asset.assetType != null ) {
      var type = document.getElementById('type_'+asset.id)
      type.value = asset.assetTypeId
      type.style.color = '#0366B0'
      }else{
      document.getElementById('type_'+asset.id).value = ""
      }
      var name = document.getElementById('name_'+asset.id)
      name.value = asset.assetName
      name.style.color = '#0366B0'
      var tag = document.getElementById('tag_'+asset.id)
      tag.value = asset.assetTag
      tag.style.color = '#0366B0'
      var sno = document.getElementById('sno_'+asset.id)
      sno.value = asset.serialNumber
      sno.style.color = '#0366B0'

      }

      function callUpdateDialog() {

      var assetId = document.getElementById('id')
      var assetType = document.getElementById('assetType.id')
      var assetName = document.getElementById('assetName')
      var assetTag = document.getElementById('assetTag')
      var serialNumber = document.getElementById('serialNumber')
      var deviceFunction = document.getElementById('deviceFunction')

      var assetNameDialog = new Array()
      assetNameDialog[0]=assetId.value
      assetNameDialog[1]=assetType.value
      assetNameDialog[2]=assetName.value
      assetNameDialog[3]=assetTag.value
      assetNameDialog[4]=serialNumber.value
      assetNameDialog[5]=deviceFunction.value
      assetNameDialog[6]="null"

      ${remoteFunction(action:'updateAsset', params:'\'assetDialog=\' + assetNameDialog', onComplete:'showEditAsset(e)')}
      return true
      }

    </g:javascript>

  </head>
  <body>

    <div class="body">
      <h1>Asset List</h1>
      <g:if test="${flash.message}">
        <div class="message">${flash.message}</div>
      </g:if>
      <div>
        <table>
          <thead>
            <tr>

              <g:sortableColumn property="id" title="Id" />

              <th>Asset Type</th>

              <g:sortableColumn property="assetName" title="Asset Name" />

              <g:sortableColumn property="assetTag" title="Asset Tag" />

              <g:sortableColumn property="serialNumber" title="Serial Number" />

            </tr>
          </thead>
          <tbody>
            <g:each in="${assetInstanceList}" status="i" var="assetInstance">

              <tr>
                <td><g:remoteLink controller="asset" action="editShow" id="${assetInstance.id}"  onComplete ="showAssetDialog( e );">${fieldValue(bean:assetInstance, field:'id')}</g:remoteLink></td>

                <td><input type="text" id="type_${assetInstance.id}" value="${fieldValue(bean:assetInstance, field:'assetType')}" style="border: 0px" readonly/></td>

                <td><input type="text" id="name_${assetInstance.id}" value="${fieldValue(bean:assetInstance, field:'assetName')}" style="border: 0px" readonly/></td>

                <td><input type="text" id="tag_${assetInstance.id}" value="${fieldValue(bean:assetInstance, field:'assetTag')}" style="border: 0px" readonly/></td>

                <td><input type="text" id="sno_${assetInstance.id}" value="${fieldValue(bean:assetInstance, field:'serialNumber')}" style="border: 0px" readonly/></td>
              </tr>

            </g:each>
          </tbody>
        </table>
      </div>
      <div class="paginateButtons">
        <g:paginate total="${Asset.count()}" />
      </div>
      <div class="buttons">
        <g:form>
          <span class="button"><g:actionSubmit type="button" value="New Asset" class="create" action="create"/></span>
        </g:form>
      </div>
    </div>

    <div id="dialog" title="Show Asset">
      <div class="dialog">
        <table>
          <tbody>

            <tr class="prop">
              <td valign="top" class="name">Asset Type:</td>

              <td valign="top" class="value"><input type="text" id="assetTypes" name="assetTypes" value="" style="border: 0px" readonly></td>

            </tr>

            <tr class="prop">
              <td valign="top" class="name">Asset Name:</td>

              <td valign="top" class="value"><input type="text" id="assetNames" name="assetNames" value="" style="border: 0px" readonly></td>

            </tr>

            <tr class="prop">
              <td valign="top" class="name">Asset Tag:</td>

              <td valign="top" class="value"><input type="text" id="assetTags" name="assetTags" value="" style="border: 0px" readonly></td>

            </tr>

            <tr class="prop">
              <td valign="top" class="name">Serial Number:</td>

              <td valign="top" class="value"><input type="text" id="serialNumbers" name="serialNumbers" value="" style="border: 0px" readonly></td>

            </tr>

            <tr class="prop">
              <td valign="top" class="name">Device Function:</td>

              <td valign="top" class="value"><input type="text" id="deviceFunctions" name="deviceFunctions" value="" style="border: 0px" readonly></td>

            </tr>

          </tbody>
        </table>
      </div>
      <div class="buttons">
        <g:form>
          <span class="button"><input type="button" class="edit" value="Edit" onClick="return editAssetDialog()"/></span>
        </g:form>
      </div>
    </div>

    <div id="dialog1" title="Edit Asset">
      <g:form method="post">
        <input type="hidden" id="id" name="id" value="" />
        <div class="dialog">
          <table>
            <tbody>

              <tr class="prop">
                <td valign="top" class="name">
                  <label for="assetType">Asset Type:</label>
                </td>
                <td valign="top" class="value ${hasErrors(bean:assetInstance,field:'assetType','errors')}">
                  <g:select optionKey="id" from="${AssetType.list()}" id="assetType.id" name="assetType.id" value="" noSelection="['null':'']"></g:select>
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
                  <input type="text" id="assetName" name="assetName" value=""/>
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
                  <input type="text" id="assetTag" name="assetTag" value=""/>
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
                  <input type="text" id="serialNumber" name="serialNumber" value=""/>
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
                  <input type="text" id="deviceFunction" name="deviceFunction" value=""/>
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
          <span class="button"><input type="button" class="save" value="Update Asset" onClick="return callUpdateDialog()"/></span>
          <span class="button"><g:actionSubmit class="delete" onclick="return confirm('Are you sure?');" value="Delete" /></span>
        </div>
      </g:form>
    </div>

  </body>
</html>
