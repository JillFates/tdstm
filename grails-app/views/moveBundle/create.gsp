<html>
  <head>
    <meta http-equiv="Content-Type" content="text/html; charset=UTF-8"/>
    <meta name="layout" content="topNav" />
    <title>Create Bundle</title>

    <style>
      .dialog select {
        width: 180px;
      }
    </style>
     <script type="text/javascript">
      function initialize(){

      // This is called when the page loads to initialize Managers
      $('#moveManagerId').val('${moveManager}')
      $('#projectManagerId').val('${projectManager}')

      }
      function isValidDate( date ){
        var returnVal = true;
        if( date && !tdsCommon.isValidDateTime(date) ){
          alert("Date should be in '" + tdsCommon.defaultDateTimeFormat() + "' format");
          returnVal  =  false;
      	}
      	return returnVal;
      }
      function validateDates(){
          var returnval = false
          var startTime = $("#startTime").val();
          var completionTime = $("#completionTime").val();
          if(isValidDate(startTime) && isValidDate(completionTime)){
        	  returnval = true;
          }
          return returnval;
      }
    </script>
  </head>
  <body>
  <tds:subHeader title="Create Bundle" crumbs="['Planning','Bundles', 'List']"/>
  <div class="body">
      <!-- <h1>Create Bundle</h1> -->
      <div class="nav" style="border: 1px solid #CCCCCC; height: 24px;">
			<span class="menuButton"><g:link class="list" action="list">Bundle List</g:link></span>
		</div>
		<br/>
      <g:if test="${flash.message}">
        <div class="message">${flash.message}</div>
      </g:if>

      <g:form action="save" method="post" name="bundleCreateForm">
        <div class="dialog">
          <table>
            <tbody>
			<tr>
			<td colspan="2"><div class="required"> Fields marked ( * ) are mandatory </div> </td>
			</tr>
              <tr class="prop">
                <td valign="top" class="name">
                  <label for="name"><b>Name:&nbsp;<span style="color: red">*</span></b></label>
                </td>
                <td valign="top" class="value ${hasErrors(bean:moveBundleInstance,field:'name','errors')}">
                  <input type="text" id="name" name="name" value="${fieldValue(bean:moveBundleInstance,field:'name')}"/>
                  <g:hasErrors bean="${moveBundleInstance}" field="name">
                    <div class="errors">
                      <g:renderErrors bean="${moveBundleInstance}" as="list" field="name"/>
                    </div>
                  </g:hasErrors>
                </td>
              </tr>

              <tr class="prop">
                <td valign="top" class="name">
                  <label for="description">Description:</label>
                </td>
                <td valign="top" class="value ${hasErrors(bean:moveBundleInstance,field:'description','errors')}">
                  <input type="text" id="description" name="description" value="${fieldValue(bean:moveBundleInstance,field:'description')}"/>
                  <g:hasErrors bean="${moveBundleInstance}" field="description">
                    <div class="errors">
                      <g:renderErrors bean="${moveBundleInstance}" as="list" field="description"/>
                    </div>
                  </g:hasErrors>
                </td>
              </tr>
              <tr class="prop">
                <td valign="top" class="name">
                  <label for="sourceRoom">From:</label>
                </td>
                <td valign="top" class="value ${hasErrors(bean:moveBundleInstance,field:'sourceRoom','errors')}">
                 <g:select from="${rooms}" id="sourceRoomId" name="sourceRoom" optionKey="id" noSelection="['':'Please Select']" />
                  <g:hasErrors bean="${moveBundleInstance}" field="sourceRoom">
                    <div class="errors">
                      <g:renderErrors bean="${moveBundleInstance}" as="list" field="sourceRoom"/>
                    </div>
                  </g:hasErrors>
                </td>
              </tr>
              <tr class="prop">
                <td valign="top" class="name">
                  <label for="targetRoom">To:</label>
                </td>
                <td valign="top" class="value ${hasErrors(bean:moveBundleInstance,field:'targetRoom','errors')}">
                 <g:select id="targetRoomId" name="targetRoom" from="${rooms}" optionKey="id" noSelection="['':'Please Select']" />
                  <g:hasErrors bean="${moveBundleInstance}" field="targetRoom">
                    <div class="errors">
                      <g:renderErrors bean="${moveBundleInstance}" as="list" field="targetRoom"/>
                    </div>
                  </g:hasErrors>
                </td>
              </tr>

              <tr class="prop">
                <td valign="top" class="name">
                  <label for="startTime">Start Time:</label>
                </td>
                <td valign="top" class="value ${hasErrors(bean:moveBundleInstance,field:'startTime','errors')}">
                  <script type="text/javascript">
                    $(document).ready(function(){
                      $("#startTime").kendoDateTimePicker({ animation: false, format:tdsCommon.kendoDateTimeFormat() });
                    });
                  </script> <input type="text" class="dateRange" size="15" style="width: 180px;" id="startTime" name="startTime"
                                   value="<tds:convertDateTime date="${moveBundleInstance?.startTime}" format="12hrs" />"
                                   onchange="isValidDate(this.value)"/>
                                   <g:hasErrors bean="${moveBundleInstance}" field="startTime">
                    <div class="errors">
                      <g:renderErrors bean="${moveBundleInstance}" as="list" field="startTime"/>
                    </div>
                  </g:hasErrors>
                </td>
              </tr>

              <tr class="prop">
                <td valign="top" class="name">
                  <label for="completionTime">Completion Time:</label>
                </td>
                <td valign="top" class="value ${hasErrors(bean:moveBundleInstance,field:'completionTime','errors')}">
                  <script type="text/javascript">
                    $(document).ready(function(){
                      $("#completionTime").kendoDateTimePicker({ animation: false, format:tdsCommon.kendoDateTimeFormat() });
                    });
                  </script> <input type="text" class="dateRange" size="15" style="width: 180px;" id="completionTime" name="completionTime"
                                   value="<tds:convertDateTime date="${moveBundleInstance?.completionTime}" format="12hrs" />"
                                   onchange="isValidDate(this.value)"/>
                                   <g:hasErrors bean="${moveBundleInstance}" field="completionTime">
                    <div class="errors">
                      <g:renderErrors bean="${moveBundleInstance}" as="list" field="completionTime"/>
                    </div>
                  </g:hasErrors>
                </td>
              </tr>

              <tr class="prop">
                <td valign="top" class="name"><label for="projectManager">Project
                Manager:</label></td>
                <td valign="top" class="value"><select id="projectManagerId"
                                                         name="projectManager">
                    <option value="" selected="selected">Please Select</option>

                    <g:each status="i" in="${managers}" var="managers">
                      <option value="${managers?.staff?.id}">${managers?.staff?.lastNameFirstAndTitle}</option>
                    </g:each>

                </select></td>
              </tr>

              <tr class="prop">
                <td valign="top" class="name"><label for="moveManager">Event Manager:</label></td>
                <td valign="top" class="value"><select id="moveManagerId" name="moveManager">
                    <option value="" selected="selected">Please Select</option>

                    <g:each status="i" in="${managers}" var="managers">
                      <option value="${managers?.staff?.id}">${managers?.staff?.lastNameFirstAndTitle}</option>
                    </g:each>

                </select></td>
              </tr>

              <tr class="prop">
                <td valign="top" class="name">
                  <label for="operationalOrder">Order:</label>
                </td>
                <td valign="top" class="value ${hasErrors(bean:moveBundleInstance,field:'operationalOrder','errors')}">
                  <g:select from="${1..25}" id="operationalOrder" name="operationalOrder" value="${moveBundleInstance?.operationalOrder}" ></g:select>
                  <g:hasErrors bean="${moveBundleInstance}" field="operationalOrder">
                    <div class="errors">
                      <g:renderErrors bean="${moveBundleInstance}" as="list" field="operationalOrder"/>
                    </div>
                  </g:hasErrors>
                </td>
              </tr>
              <tr>
              <td valign="top" class="name"><label for="workflowCode"><b>WorkFlow	Code:&nbsp;<span style="color: red">*</span></b></label></td>
				<td valign="top"
					class="value ${hasErrors(bean:moveBundleInstance,field:'workflowCode','errors')}">
				<g:select id="workflowCode" name="workflowCode"
					from="${workflowCodes}"
					value="${projectInstance?.workflowCode}" noSelection="['':'Please Select']"></g:select>
					<g:hasErrors bean="${moveBundleInstance}" field="workflowCode">
					<div class="errors"><g:renderErrors bean="${moveBundleInstance}"
						as="list" field="workflowCode" /></div>
				</g:hasErrors></td>
              </tr>

               <tr class="prop">
                <td valign="top" class="name"><label for="useForPlanning">Use for Planning:</label></td>
               <td> <input type="checkbox" name="useForPlanning" id="useForPlanning" value="true" checked="checked"/></td>
              </tr>
            </tbody>
          </table>
        </div>
        <div class="buttons">
          <input type="hidden"  name="project.id" value="${tds.currentProjectId()}"/>
          <input type="hidden"  name="projectId" value="${tds.currentProjectId()}"/>
          <span class="button"><input class="save" type="submit" value="Save" onclick="return validateDates()"/></span>
        </div>
      </g:form>
      <script>
        initialize();
      </script>
    </div>
    <script>
    currentMenuId = "#eventMenu";
    $(".menu-parent-planning-list-bundles").addClass('active');
    $(".menu-parent-planning").addClass('active');
   </script>
</html>
