<html>
  <head>
    <meta http-equiv="Content-Type" content="text/html; charset=UTF-8"/>
    <meta name="layout" content="main" />
    <title>Edit Bundle</title>
    <link rel="stylesheet" href="${resource(dir:'css',file:'ui.datepicker.css')}" />

    <g:javascript>
      function callBundleChange(val){

      document.editForm.id.value = val.value
      document.editForm.submit()

      }

      function saveMoveBundleAsset(val){

      var moveBundleId = val.value +","+ val.name

      ${remoteFunction(action:'addMoveBundleAsset', params:'\'moveBundleId=\' + moveBundleId' )}

      }

      function saveMoveBundleSourceTeamAsset(val){

      var moveBundleId = val.value +","+ val.name

      ${remoteFunction(action:'addMoveBundleSourceAsset', params:'\'moveBundleId=\' + moveBundleId' )}

      }

      function saveMoveBundleTargetTeamAsset(val){

      var moveBundleId = val.value +","+ val.name

      ${remoteFunction(action:'addMoveBundleTargetAsset', params:'\'moveBundleId=\' + moveBundleId' )}

      }

    </g:javascript>

  </head>
  <body>
    <div class="nav">

      <span class="menuButton"><g:link class="list" action="list">List Bundles</g:link></span>
      <span class="menuButton"><g:link class="create" action="create">Create Bundle</g:link></span>
    </div>
    <div class="body">

      <g:if test="${flash.message}">
        <div class="message">${flash.message}</div>
      </g:if>
      <g:hasErrors bean="${moveBundleInstance}">
        <div class="errors">
          <g:renderErrors bean="${moveBundleInstance}" as="list" />
        </div>
      </g:hasErrors>
      <g:form method="post"  action="moveBundleEdit" name="editForm">
        <input type="hidden"  name="id" value="${moveBundleInstance?.id}" />
        <div class="dialog">
          <table style="border:0px;">
            <tbody>


              <tr class="prop">

              <td valign="top" class="name">
                <label for="moveBundle">Select Bundle:</label>
              </td>
              <td valign="top" class="bundle" >

                <select id="moveBundle.id" name="moveBundle.id" onchange="callBundleChange(this)">
                  <option value="${moveBundleInstance?.id}" selected="selected">${moveBundleInstance?.name}</option>
                  <g:each in="${bundlesRelatedToproject}" var="moveBundles">
                    <g:if test="${moveBundleInstance?.id != moveBundles.id}">
                      <option value="${moveBundles.id}">${moveBundles.name}</option>
                    </g:if>
                  </g:each>

                </select>
              </td>

              <td valign="top" class="name">
                <label for="startTime">Start Time:</label>
              </td>

              <td valign="top"
                  class="bundle ${hasErrors(bean:moveBundleInstance,field:'startTime','errors')}">
                <script type="text/javascript" charset="utf-8">
                  jQuery(function($){$('.dateRange').datepicker({showOn: 'both', buttonImage: '${resource(dir:'images',file:'calendar.gif')}', buttonImageOnly: true,beforeShow: customRange});function customRange(input) {return null;}});
                </script> <input type="text" class="dateRange" size="15"
                                 style="width: 112px; height: 14px;" name="startTime"
                                 value="<tds:convertDate date="${moveBundleInstance?.startTime}" timeZone="${request.getSession().getAttribute('CURR_TZ')?.CURR_TZ}"/>">

                                 <g:hasErrors
                  bean="${moveBundleInstance}" field="startTime">
                  <div class="errors"><g:renderErrors bean="${moveBundleInstance}"
                                                      as="list" field="startTime" /></div>
              </g:hasErrors></td>

              <tr>

              <tr class="prop">

                <td valign="top" class="name">
                  <label for="name">Bundle Name:</label>
                </td>
                <td valign="top" class="bundle ${hasErrors(bean:moveBundleInstance,field:'name','errors')}">
                  <input type="text" id="name" name="name" value="${fieldValue(bean:moveBundleInstance,field:'name')}"/>
                </td>



                <td valign="top" class="name">
                  <label for="finishTime">Finish Time:</label>
                </td>

                <td valign="top"
                    class="bundle ${hasErrors(bean:moveBundleInstance,field:'finishTime','errors')}">
                <script type="text/javascript" charset="utf-8">
                  jQuery(function($){$('.datetRange').datepicker({showOn: 'both', buttonImage: '${resource(dir:'images',file:'calendar.gif')}', buttonImageOnly: true,beforeShow: customRange});function customRange(input) {return null;}});
                </script> <input type="text" class="dateRange" size="15"
                                 style="width: 112px; height: 14px;" name="startTime"
                                 value="<tds:convertDate date="${moveBundleInstance?.finishTime}" timeZone="${request.getSession().getAttribute('CURR_TZ')?.CURR_TZ}"/>">

                                 <g:hasErrors
                  bean="${moveBundleInstance}" field="finishTime">
                  <div class="errors"><g:renderErrors bean="${moveBundleInstance}"
                                                      as="list" field="finishTime" /></div>
                </g:hasErrors><td>

              </tr>
              <tr class="prop">
              <td></td>
              <td></td>

              <td valign="top" class="name">
                <label>Operation Sequence:</label>
              </td>
              <td></td>

              <tr>


            </tbody>
          </table>


          <h1>Assets in Bundle</h1>

          <div style="overflow:scroll; height:300px;width:1000px;">
            <table style="border:0px;">
              <thead>
                <tr>
                  <th>Asset Id</th>

                  <th>Asset Name</th>

                  <th>Bundle</th>

                  <th>Source Team</th>

                  <th>Target Team</th>

                  <th>Priority</th>

                  <th></th>

                </tr>
              </thead>
              <tbody>
                <g:each in="${moveBundleAssetInstanceEditList}" var="moveBundleAssetEditInstance">
                  <tr>


                    <td>${moveBundleAssetEditInstance?.asset?.id}</td>

                    <td>${moveBundleAssetEditInstance?.asset?.assetName}</td>

                    <td>
                      <select id="moveBundleAsset.id" name="${moveBundleAssetEditInstance?.id}" onchange="saveMoveBundleAsset(this);">
                        <option value="${moveBundleAssetEditInstance?.moveBundle?.id}" selected="selected">${moveBundleAssetEditInstance?.moveBundle}</option>
                        <g:each in="${bundlesRelatedToproject}" var="moveBundles">
                          <g:if test="${moveBundleInstance?.id != moveBundles.id}">
                            <option value="${moveBundles.id}">${moveBundles.name}</option>
                          </g:if>
                        </g:each>

                      </select>
                    </td>

                    <td><g:select optionKey="id" from="${ProjectTeam.list()}" name="${moveBundleAssetEditInstance?.id}" value="${moveBundleAssetEditInstance?.sourceTeamMt?.id}" onchange="saveMoveBundleSourceTeamAsset(this);"></g:select></td>

                    <td><g:select optionKey="id" from="${ProjectTeam.list()}" name="${moveBundleAssetEditInstance?.id}" value="${moveBundleAssetEditInstance?.targetTeamMt?.id}" onchange="saveMoveBundleTargetTeamAsset(this);"></g:select></td>

                    <td><g:checkBox name="myCheckbox" value="${true}" /></td>

                    <td><g:link>Unassigned</g:link></td>
                  </tr>
                </g:each>
              </tbody>
            </table>
          </div>
        </div>

        <div>
          <br>
          <label for="name">Filter:</label><select name="tdsMaster">
            <option>Not in bundles</option>
            <option>All assigned to project</option>
            <option>Not assigned to project</option>
          </select>
          <input type="text" size="4" value="web"/>
          <input type="button" value="search"/>
        </div>
        <div class="buttons">
          <span class="button"><g:actionSubmit class="save" value="Update" /></span>
          <span class="button"><g:actionSubmit class="delete" onclick="return confirm('Are you sure?');" value="Delete" /></span>
        </div>
      </g:form>
    </div>
    <script>
    currentMenuId = "#eventMenu";
	$("#eventMenuId a").css('background-color','#003366')
    </script>
  </body>
</html>
