

<html>
  <head>
    <meta http-equiv="Content-Type" content="text/html; charset=UTF-8"/>
    <meta name="layout" content="main" />
    <title>Asset List</title>

    <g:javascript library="prototype"/>    
    
    <link type="text/css" rel="stylesheet" href="${createLinkTo(dir:'css',file:'ui.accordion.css')}"  />
    <link type="text/css" rel="stylesheet" href="${createLinkTo(dir:'css',file:'ui.core.css')}" />
    <link type="text/css" rel="stylesheet" href="${createLinkTo(dir:'css',file:'ui.dialog.css')}" />
    <link type="text/css" rel="stylesheet" href="${createLinkTo(dir:'css',file:'ui.resizable.css')}"  />
    <link type="text/css" rel="stylesheet" href="${createLinkTo(dir:'css',file:'ui.slider.css')}"  />
    <link type="text/css" rel="stylesheet" href="${createLinkTo(dir:'css',file:'ui.tabs.css')}"  />
    <link type="text/css" rel="stylesheet" href="${createLinkTo(dir:'css',file:'ui.theme.css')}" />
    <script type="text/javascript" src="${createLinkTo(dir:'js',file:'jquery-1.3.1.js')}"></script>
    <script type="text/javascript" src="${createLinkTo(dir:'js',file:'ui.core.js')}"></script>
    <script type="text/javascript" src="${createLinkTo(dir:'js',file:'ui.draggable.js')}"></script>
    <script type="text/javascript" src="${createLinkTo(dir:'js',file:'ui.resizable.js')}"></script>
    <script type="text/javascript" src="${createLinkTo(dir:'js',file:'ui.dialog.js')}"></script>

    <script>

      $(document).ready(function() {

        $("#dialog").dialog({ autoOpen: false })
        $("#dialog1").dialog({ autoOpen: false })
        $("#dialog2").dialog({ autoOpen: false })

      })

    </script>

    <g:javascript>
      var rowId
      function showAssetDialog( e ) {

      var asset = eval('(' + e.responseText + ')')     

      document.getElementById('id').value = asset.id
      if ( asset.assetType != null ) {
      document.getElementById('assetTypes').value = asset.assetTypeId
      document.getElementById('assetTypeD.id').value = asset.assetTypeId
      }else{
      document.getElementById('assetTypes').value = ""
      document.getElementById('assetTypeD.id').value = null
      }
      document.getElementById('assetNames').value = asset.assetName
      document.getElementById('assetNameD').value = asset.assetName
      document.getElementById('assetTags').value = asset.assetTag
      document.getElementById('assetTagD').value = asset.assetTag
      document.getElementById('serialNumbers').value = asset.serialNumber
      document.getElementById('serialNumberD').value = asset.serialNumber
      document.getElementById('deviceFunctions').value = asset.deviceFunction
      document.getElementById('deviceFunctionD').value = asset.deviceFunction

      $("#dialog").dialog('option', 'width', 400)
      $("#dialog").dialog("open")

      }

      function editAssetDialog() {

      $("#dialog").dialog("close")
      $("#dialog1").dialog('option', 'width', 500)
      $("#dialog1").dialog("open")

      }

      function showEditAsset(e) {

      $("#dialog1").dialog("close")
      var asset = eval('(' + e.responseText + ')')

      var x=document.getElementById('assetTable').rows
      var y=x[rowId].cells
      x[rowId].style.background = '#65a342'
      if(asset.assetTypeId == null) {
      y[1].innerHTML = ""
      }else{
      y[1].innerHTML = asset.assetTypeId
      }
      y[2].innerHTML = asset.assetName
      y[3].innerHTML = asset.assetTag
      y[4].innerHTML = asset.serialNumber

      }

      function callUpdateDialog() {

      var assetId = document.getElementById('id')
      var assetType = document.getElementById('assetTypeD.id')
      var assetName = document.getElementById('assetNameD')
      var assetTag = document.getElementById('assetTagD')
      var serialNumber = document.getElementById('serialNumberD')
      var deviceFunction = document.getElementById('deviceFunctionD')

      var assetNameDialog = new Array()
      assetNameDialog[0] = assetId.value
      assetNameDialog[1] = assetType.value
      assetNameDialog[2] = assetName.value
      assetNameDialog[3] = assetTag.value
      assetNameDialog[4] = serialNumber.value
      assetNameDialog[5] = deviceFunction.value
      assetNameDialog[6] = "null"

      ${remoteFunction(action:'updateAsset', params:'\'assetDialog=\' + assetNameDialog', onComplete:'showEditAsset(e)')}
      return true
      }

      function createDialog(){

      $("#dialog2").dialog('option', 'width', 500)
      $("#dialog2").dialog("open")

      }

      function setRowId(val){

      rowId = val.id

      }

    </g:javascript>

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
      <h1>Asset List</h1>
      <g:if test="${flash.message}">
        <div class="message">${flash.message}</div>
      </g:if>
      <div>
        <table id="assetTable">
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
            <%  int k = 1 %>
            <g:each in="${assetInstanceList}" status="i" var="assetInstance">

              <tr id="${k}" onClick="setRowId(this)" onmouseover="style.backgroundColor='#87CEEE';" onmouseout="style.backgroundColor='white';">
                <td><g:remoteLink controller="asset" action="editShow" id="${assetInstance.id}"  onComplete ="showAssetDialog( e );">${fieldValue(bean:assetInstance, field:'id')}</g:remoteLink></td>

                <td>${fieldValue(bean:assetInstance, field:'assetType')}</td>

                <td>${fieldValue(bean:assetInstance, field:'assetName')}</td>

                <td>${fieldValue(bean:assetInstance, field:'assetTag')}</td>

                <td>${fieldValue(bean:assetInstance, field:'serialNumber')}</td>
              </tr>
              <%  k = ++k %>
            </g:each>
          </tbody>
        </table>
      </div>
      <div class="paginateButtons">
        <g:paginate total="${Asset.count()}" />
      </div>
      <div class="buttons">
        <g:form>
          <span class="button"><input type="button" value="New Asset" class="create" onClick="createDialog()"/></span>
        </g:form>
      </div>
    </div>

    <div id="dialog" title="Show Asset" style="display:none;">
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

    <div id="dialog1" title="Edit Asset" style="display:none;">
      <g:form method="post">
        <input type="hidden" id="id" name="id" value="" />
        <div class="dialog">
          <table>
            <tbody>

              <tr class="prop">
                <td valign="top" class="name">
                  <label for="assetType">Asset Type:</label>
                </td>
                <td valign="top">
                  <g:select optionKey="id" from="${AssetType.list()}" id="assetTypeD.id" name="assetTypeD.id" value="" noSelection="['null':'']"></g:select>
                </td>
              </tr>

              <tr class="prop">
                <td valign="top" class="name">
                  <label for="assetName">Asset Name:</label>
                </td>
                <td valign="top">
                  <input type="text" id="assetNameD" name="assetNameD" value=""/>
                </td>
              </tr>

              <tr class="prop">
                <td valign="top" class="name">
                  <label for="assetTag">Asset Tag:</label>
                </td>
                <td valign="top">
                  <input type="text" id="assetTagD" name="assetTagD" value=""/>
                </td>
              </tr>

              <tr class="prop">
                <td valign="top" class="name">
                  <label for="serialNumber">Serial Number:</label>
                </td>
                <td valign="top">
                  <input type="text" id="serialNumberD" name="serialNumberD" value=""/>
                </td>
              </tr>

              <tr class="prop">
                <td valign="top" class="name">
                  <label for="deviceFunction">Device Function:</label>
                </td>
                <td valign="top">
                  <input type="text" id="deviceFunctionD" name="deviceFunctionD" value=""/>
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

    <div id="dialog2" title="Create Asset" style="display:none;">
      <div class="dialog">
        <h1>Create Asset</h1>
        <g:if test="${flash.message}">
          <div class="message">${flash.message}</div>
        </g:if>
        <g:form action="save" method="post" >
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
            <span class="button"><input class="save" type="submit" value="Create" /></span>
          </div>
        </g:form>
      </div>
      </div>

  </body>
</html>
