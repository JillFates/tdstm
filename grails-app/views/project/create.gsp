<html>
  <head>
    <meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
    <meta name="layout" content="main" />
    <title>Create Project</title>
    <g:javascript library="prototype" />
    <g:javascript library="jquery" />
    <g:javascript library="ui.datepicker" />
    <g:javascript>

      function appendPartnerStaff(e) {
      // The response comes back as a bunch-o-JSON

      var managers = eval("(" + e.responseText + ")")
      // evaluate JSON
      var rselect = document.getElementById('projectManagerId')
      var mselect = document.getElementById('moveManagerId')
      // Clear all previous options
      var l = rselect.length
      var compSatff = document.getElementById('companyManagersId').value
      while (l > compSatff) {
      l--
      rselect.remove(l)
      mselect.remove(l)
      }
      // Rebuild the select
      if (managers) {
      var pmOptGroup = document.getElementById('pmOptGroupId')
      var mmOptGroup = document.getElementById('mmOptGroupId')
      var projectPartner = document.getElementById('projectPartnerId');
      var projectPartnerVal = projectPartner[document.getElementById('projectPartnerId').selectedIndex].innerHTML;
      pmOptGroup.style.visibility="visible";
      mmOptGroup.style.visibility="visible";
      if(projectPartnerVal != "None" ){
        pmOptGroup.label = projectPartnerVal;
        mmOptGroup.label = projectPartnerVal;
      } else {
        pmOptGroup.label = "";
        mmOptGroup.label = "";
      }

      var length = managers.items.length
      for (var i=0; i < length; i++) {
      var manager = managers.items[i]
      var popt = document.createElement('option');
      popt.text = manager.name
      popt.value = manager.id
      var mopt = document.createElement('option');
      mopt.text = manager.name
      mopt.value = manager.id
      try {
      rselect.add(popt, null) // standards compliant; doesn't work in IE
      mselect.add(mopt, null)
      } catch(ex) {
      rselect.add(popt) // IE only
      mselect.add(mopt)
      }
      }
      }
      }
      function initialize(){
      // This is called when the page loads to initialize Managers
      var partnerselect = document.getElementById('projectPartnerId')
      ${remoteFunction(action:'getPartnerStaffList', params:'\'partner=\' + partnerselect.value', onComplete:'appendPartnerStaff(e)')}
      }
      function textCounter(field, maxlimit)
      {
      if (field.value.length > maxlimit) // if too long...trim it!
      {
      field.value = field.value.substring(0, maxlimit);
      return false;
      }
      else
      {
      return true;
      }
      }
    </g:javascript>
  </head>
  <body>
    <div class="body">
      <h1>Create Project</h1>
      <div class="nav" style="border: 1px solid #CCCCCC; height: 11px">
        <span class="menuButton"><g:link class="list" action="list">Project List</g:link></span>
      </div>
      <br>
      <g:if test="${flash.message}">
        <div class="message">${flash.message}</div>

      </g:if> <g:form action="save" method="post" name="createProjectForm">
        <div class="dialog">
          <table>
            <tbody>
              <tr class="prop">
                <td valign="top" class="name"><label for="projectClient">Client:</label>
                </td>
                <td valign="top" class="value"><select id="projectClient"
                                             name="projectClient">
                    <g:each status="i" in="${clients}" var="clients">
                      <option value="${clients.partyIdTo.id}">${clients.partyIdTo}</option>
                    </g:each>
                </select></td>
              </tr>

              <tr class="prop">
                <td valign="top" class="name"><label for="projectCode">Project
                Code:</label></td>
                <td valign="top"
        class="value ${hasErrors(bean:projectInstance,field:'projectCode','errors')}">
                  <input type="text" id="projectCode" name="projectCode" maxlength="20"
             value="${fieldValue(bean:projectInstance,field:'projectCode')}" />
                  <g:hasErrors bean="${projectInstance}" field="projectCode">
                    <div class="errors"><g:renderErrors bean="${projectInstance}"
                                         as="list" field="projectCode" /></div>
                </g:hasErrors></td>
              </tr>
              <tr class="prop">
                <td valign="top" class="name"><label for="name">Project Name:</label></td>
                <td valign="top"
        class="value ${hasErrors(bean:projectInstance,field:'name','errors')}">
                  <input type="text" id="name" name="name" maxlength="64"
             value="${fieldValue(bean:projectInstance,field:'name')}" /> <g:hasErrors
                    bean="${projectInstance}" field="name">
                    <div class="errors"><g:renderErrors bean="${projectInstance}"
                                         as="list" field="name" /></div>
                </g:hasErrors></td>
              </tr>

              <tr class="prop">
                <td valign="top" class="name"><label for="description">Description:</label>
                </td>
                <td valign="top"
        class="value ${hasErrors(bean:projectInstance,field:'description','errors')}">
                  <input type="text" id="description" name="description" maxlength="64"
             value="${fieldValue(bean:projectInstance,field:'description')}" />
                  <g:hasErrors bean="${projectInstance}" field="description">
                    <div class="errors"><g:renderErrors bean="${projectInstance}"
                                         as="list" field="description" /></div>
                </g:hasErrors></td>
              </tr>

              <tr class="prop">
                <td valign="top" class="name"><label for="comment">Comment:</label>
                </td>
                <td valign="top"
        class="value ${hasErrors(bean:projectInstance,field:'comment','errors')}">
                  <textarea rows="5" cols="40" name="comment"	onkeydown="textCounter(document.createProjectForm.comment,200);" onkeyup="textCounter(document.createProjectForm.comment,200);">
${fieldValue(bean:projectInstance,field:'comment')}
                  </textarea>
                  <g:hasErrors
                    bean="${projectInstance}" field="comment">
                    <div class="errors"><g:renderErrors bean="${projectInstance}" as="list" field="comment" /></div>
                </g:hasErrors></td>
              </tr>

              <tr class="prop">
                <td valign="top" class="name"><label for="startDate">Start
                Date:</label></td>
                <td valign="top"
        class="value ${hasErrors(bean:projectInstance,field:'startDate','errors')}">
                  <link rel="stylesheet" href="${createLinkTo(dir:'css',file:'ui.datepicker.css')}" />
                  <script type="text/javascript" charset="utf-8">
                    jQuery(function($){$('.dateRange').datepicker({showOn: 'both', buttonImage: '${createLinkTo(dir:'images',file:'calendar.gif')}', buttonImageOnly: true,beforeShow: customRange});function customRange(input) {return null;}});
                  </script>
                  <input type="text" class="dateRange" size="15" style="width:112px;height:14px;" name="startDate" value="<my:convertDate date="${projectInstance?.startDate}"/>">
       <!--  <g:datePicker name="startDate" value="${projectInstance?.startDate}"
       noSelection="['':'']"></g:datePicker> --><g:hasErrors
                    bean="${projectInstance}" field="startDate">
                    <div class="errors"><g:renderErrors bean="${projectInstance}"
                                         as="list" field="startDate" /></div>
                </g:hasErrors></td>
              </tr>

              <tr class="prop">
                <td valign="top" class="name"><label for="completionDate">Completion
                Date:</label></td>
                <td valign="top"
        class="value ${hasErrors(bean:projectInstance,field:'completionDate','errors')}">
                  <link rel="stylesheet" href="${createLinkTo(dir:'css',file:'ui.datepicker.css')}" />
                  <script type="text/javascript" charset="utf-8">
                    jQuery(function($){$('.dateRange').datepicker({showOn: 'both', buttonImage: '${createLinkTo(dir:'images',file:'calendar.gif')}', buttonImageOnly: true,beforeShow: customRange});function customRange(input) {return null;}});
                  </script>
                  <input type="text" class="dateRange" size="15" style="width:112px;height:14px;" name="completionDate" value="<my:convertDate date="${projectInstance?.completionDate}"/>">
       <!--  <g:datePicker name="completionDate"
                    value="${projectInstance?.completionDate}" noSelection="['':'']"></g:datePicker> -->
       <g:hasErrors bean="${projectInstance}" field="completionDate">
                    <div class="errors"><g:renderErrors bean="${projectInstance}"
                                         as="list" field="completionDate" /></div>
                </g:hasErrors></td>
              </tr>

              <tr class="prop">
                <td valign="top" class="name"><label for="projectPartner">Partner:</label>
                </td>
                <td valign="top" class="value">
                  <select id="projectPartnerId" name="projectPartner" onchange="${remoteFunction(action:'getPartnerStaffList', params:'\'partner=\' + this.value', onComplete:'appendPartnerStaff(e)' )}">
                    <option value="" selected="selected">None</option>
                    <g:each status="i" in="${partners}" var="partners">
                      <option value="${partners.partyIdTo.id}">${partners.partyIdTo}</option>
                    </g:each>
                  </select>
               </td>
              </tr>

              <tr class="prop">
                <td valign="top" class="name"><label for="projectManager">Project
                Manager:</label></td>
                <td valign="top" class="value"><select id="projectManagerId"
                                             name="projectManager">
                    <option value="" selected="selected">Please Select</option>
                    <optgroup label="TDS">
                      <g:each status="i" in="${managers}" var="managers">
                        <option value="${managers.partyIdTo.id}">${managers.partyIdTo.lastName},
                        ${managers.partyIdTo.firstName} - ${managers.partyIdTo.title}</option>
                      </g:each>
                    </optgroup>
                    <optgroup id="pmOptGroupId" style="visibility: hidden;">
                    </optgroup>
                </select></td>
              </tr>

              <tr class="prop">
                <td valign="top" class="name"><label for="moveManager">Move
                Manager:</label></td>
                <td valign="top" class="value"><select id="moveManagerId"
                                             name="moveManager">
                    <option value="" selected="selected">Please Select</option>
                    <optgroup label="TDS">
                      <g:each status="i" in="${managers}" var="managers">
                        <option value="${managers.partyIdTo.id}">${managers.partyIdTo.lastName},
                        ${managers.partyIdTo.firstName} - ${managers.partyIdTo.title}</option>
                      </g:each>
                    </optgroup>
                    <optgroup id="mmOptGroupId" style="visibility: hidden;">
                    </optgroup>
                  </select>
                  <input type="hidden" id="companyManagersId" value="${managers.size()}">
                </td>
              </tr>

              <tr class="prop">
                <td valign="top" class="name"><label for="trackChanges">Track
                Changes:</label></td>
                <td valign="top"
        class="value ${hasErrors(bean:projectInstance,field:'trackChanges','errors')}">
                  <g:select id="trackChanges" name="trackChanges"
                from="${projectInstance.constraints.trackChanges.inList}"
                value="${projectInstance.trackChanges}"></g:select> <g:hasErrors
                    bean="${projectInstance}" field="trackChanges">
                    <div class="errors"><g:renderErrors bean="${projectInstance}"
                                         as="list" field="trackChanges" /></div>
                </g:hasErrors></td>
              </tr>


            </tbody>
          </table>
        </div>
        <div class="buttons">
          <span class="button"><input class="save" type="submit" value="Create" /></span>
          <span class="button"><g:actionSubmit class="delete" action="Cancel" value="Cancel" /></span>
        </div>
    </g:form></div>
    <g:javascript>
      initialize();
    </g:javascript>
  </body>
</html>
