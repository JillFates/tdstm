

<html>
  <head>
    <meta http-equiv="Content-Type" content="text/html; charset=UTF-8"/>
    <meta name="layout" content="main" />
    <title>Show Project</title>
    <g:javascript library="prototype" />
    <g:javascript library="jquery"/>

    <link type="text/css" rel="stylesheet" href="${createLinkTo(dir:'css',file:'ui.accordion.css')}"  />
    <link type="text/css" rel="stylesheet" href="${createLinkTo(dir:'css',file:'ui.core.css')}" />
    <link type="text/css" rel="stylesheet" href="${createLinkTo(dir:'css',file:'ui.dialog.css')}" />
    <link type="text/css" rel="stylesheet" href="${createLinkTo(dir:'css',file:'ui.resizable.css')}"  />
    <link type="text/css" rel="stylesheet" href="${createLinkTo(dir:'css',file:'ui.slider.css')}"  />
    <link type="text/css" rel="stylesheet" href="${createLinkTo(dir:'css',file:'ui.tabs.css')}"  />
    <link type="text/css" rel="stylesheet" href="${createLinkTo(dir:'css',file:'ui.theme.css')}" />

    <jq:plugin name="ui.core"/>
    <jq:plugin name="ui.draggable"/>
    <jq:plugin name="ui.resizable"/>
    <jq:plugin name="ui.dialog"/>
    
    <script type="text/javascript">
      $(document).ready(function(){
        $("#dialog").dialog({ autoOpen: false });
      });
    </script>
    <g:javascript>
      function editProject(){
      $("#dialog").dialog('option', 'width', 500)
      $("#dialog").dialog( "open" );
      var pmObj = document.getElementById("projectManagerId")
      var mmObj = document.getElementById("moveManagerId")
      var partnerObj = document.getElementById("projectPartnerId")
      <% if( projectPartner != null){ %>
      partnerObj.value = "${projectPartner?.partyIdTo.id}"
      <%}
      if ( projectManager != null ) {
        %>
      pmObj.value = "${projectManager?.partyIdTo.id}"
      <% }
        if ( moveManager != null ) { %>
      mmObj.value = "${moveManager?.partyIdTo.id}"
      <% } %>

      }
      function appendPartnerStaff(e) {
      // The response comes back as a bunch-o-JSON
      //alert("make sure that the project isn't saved with a staff member from the previous partner");
      if(confirm(" Partner has been changed, Make sure that do you want to change the staff members ")){
      
      // evaluate JSON
      var rselect = document.getElementById('projectManagerId')
      var mselect = document.getElementById('moveManagerId')
      var projectPartner = document.getElementById('projectPartnerId');
      var projectPartnerVal = projectPartner[document.getElementById('projectPartnerId').selectedIndex].innerHTML;

      var pmExeOptgroup = document.getElementById('pmGroup')
      var mmExeOptgroup = document.getElementById('mmGroup')
      var pmOptgroup
      var mmOptgroup

      if(pmExeOptgroup == null){
      pmOptgroup = document.createElement('optgroup');
      }else{
      pmOptgroup = pmExeOptgroup
      }
      if(mmExeOptgroup == null){
      mmOptgroup = document.createElement('optgroup');
      }else{
      mmOptgroup = mmExeOptgroup
      }

      if(projectPartnerVal != "None" ){
      pmOptgroup.label = projectPartnerVal;
      pmOptgroup.id = "pmGroup";
      mmOptgroup.label = projectPartnerVal;
      mmOptgroup.id = "mmGroup";
      } else {
      pmOptgroup.label = "";
      mmOptgroup.label = "";
      }
      try {
      rselect.appendChild(pmOptgroup, null) // standards compliant; doesn't work in IE
      mselect.appendChild(mmOptgroup, null)
      } catch(ex) {
      rselect.appendChild(pmOptgroup) // IE only
      mselect.appendChild(mmOptgroup)
      }
      // Clear all previous options
      var l = rselect.length
      var compSatff = document.getElementById('companyManagersId').value
      while (l > compSatff) {
      l--
      rselect.remove(l)
      mselect.remove(l)
      }
      
      var managers = eval("(" + e.responseText + ")")
      // Rebuild the select
      if (managers) {

      var length = managers.items.length
      for (var i=0; i < length; i++) {
      var manager = managers.items[i]
      var popt = document.createElement('option');
      popt.innerHTML = manager.name
      popt.value = manager.id
      var mopt = document.createElement('option');
      mopt.innerHTML = manager.name
      mopt.value = manager.id
      try {
      pmOptgroup.appendChild(popt, null) // standards compliant; doesn't work in IE
      mmOptgroup.appendChild(mopt, null)
      } catch(ex) {
      pmOptgroup.appendChild(popt) // IE only
      mmOptgroup.appendChild(mopt)
      }
      }
      }
      }else{
      var partnerObj = document.getElementById("projectPartnerId")
      <% if( projectPartner != null){ %>
      partnerObj.value = "${projectPartner?.partyIdTo.id}"
      <%} %>
      }
      }
      function setCompletionDate(startDate){
      var completionDateObj = document.editProjectForm.completionDate;
      if(completionDateObj.value == ""){
      completionDateObj.value = startDate;
      }
      }
    </g:javascript>
  </head>
  <body>
    <div class="menu2">
      <ul>
        <li><g:link class="home" controller="projectUtil">Project </g:link> </li>
        <li><g:link class="home" controller="person" action="projectStaff" params="[projectId:projectInstance?.id]" >Staff</g:link></li>
        <li><g:link class="home" controller="asset">Assets </g:link></li>
        <li><g:link class="home" controller="asset" action="assetImport" >Import/Export</g:link> </li>
        <li><g:link class="home" controller="projectTeam" action="list" params="[projectId:projectInstance?.id]" >Team </g:link></li>
        <li><a href="#">Contacts </a></li>
        <li><a href="#">Applications </a></li>
        <li><g:link class="home" controller="moveBundle">Move Bundles</g:link> </li>
      </ul>
    </div>
    <div class="body">
    <h1>Show Project</h1>
    <div class="nav" style="border: 1px solid #CCCCCC; height: 11px">
      <span class="menuButton"><g:link class="list" action="list">Project List</g:link></span>
      <jsec:hasRole name="ADMIN">
        <span class="menuButton"><g:link class="create" action="create">New Project</g:link></span>
      </jsec:hasRole>
    </div>
    <br>
    <g:if test="${flash.message}">
      <div class="message">${flash.message}</div>
    </g:if>
    <div class="dialog" id="updateShow">
      <table>
        <tbody>

          <tr class="prop">
            <td valign="top" class="name">Associated Client:</td>

            <td valign="top" class="value">${projectInstance?.client}</td>

          </tr>

          <tr class="prop">
            <td valign="top" class="name">Project Code:</td>

            <td valign="top" class="value">${fieldValue(bean:projectInstance, field:'projectCode')}</td>

          </tr>

          <tr class="prop">
            <td valign="top" class="name">Project Name:</td>

            <td valign="top" class="value">${fieldValue(bean:projectInstance, field:'name')}</td>

          </tr>

          <tr class="prop">
            <td valign="top" class="name">Description:</td>

            <td valign="top" class="value">${fieldValue(bean:projectInstance, field:'description')}</td>

          </tr>

          <tr class="prop">
            <td valign="top" class="name">Comment:</td>

            <td valign="top" class="value">${fieldValue(bean:projectInstance, field:'comment')}</td>

          </tr>

          <tr class="prop">
            <td valign="top" class="name">Start Date:</td>

            <td valign="top" class="value"><tds:convertDate date="${projectInstance?.startDate}" /></td>

          </tr>

          <tr class="prop">
            <td valign="top" class="name">Completion Date:</td>

            <td valign="top" class="value"><tds:convertDate date="${projectInstance?.completionDate}" /></td>

          </tr>

          <tr class="prop">
            <td valign="top" class="name">Associated Partner:</td>

            <td valign="top" class="value">${projectPartner?.partyIdTo}</td>

          </tr>

          <tr class="prop">
            <td valign="top" class="name">Project Manager:</td>

            <td valign="top" class="value">${projectManager?.partyIdTo?.lastName}, ${projectManager?.partyIdTo?.firstName} - ${projectManager?.partyIdTo?.title}</td>

          </tr>

          <tr class="prop">
            <td valign="top" class="name">Move Manager:</td>

            <td valign="top" class="value">${moveManager?.partyIdTo?.lastName}, ${moveManager?.partyIdTo?.firstName} - ${moveManager?.partyIdTo?.title}</td>

          </tr>

          <tr class="prop">
            <td valign="top" class="name">Track Changes:</td>

            <td valign="top" class="value">${fieldValue(bean:projectInstance, field:'trackChanges')}</td>

          </tr>

          <tr class="prop">
            <td valign="top" class="name">
              <label for="dateCreated">Date Created:</label>
            </td>
            <td valign="top" class="value"><tds:convertDateTime date="${projectInstance?.dateCreated}" /> </td>
          </tr>

          <tr class="prop">
            <td valign="top" class="name">
              <label for="lastUpdated">Last Updated:</label>
            </td>
            <td valign="top" class="value"><tds:convertDateTime date="${projectInstance?.lastUpdated}" /> </td>
          </tr>

        </tbody>
      </table>
    </div>
    <div id="dialog" title="Edit Project" style="display:none;">

      <g:form name="editForm" method="post" action="update" name="editProjectForm">

        <input type="hidden" name="id" value="${projectInstance?.id}" />
        <div class="dialog">
          <table>
            <tbody>
              <tr class="prop">
                <td valign="top" class="name">
                  <label for="name">Project Name:</label>
                </td>
                <td valign="top" class="value ${hasErrors(bean:projectInstance,field:'name','errors')}">
                  <input type="text" id="name" name="name" value="${fieldValue(bean:projectInstance,field:'name')}" maxlength="64"/>
                  <g:hasErrors bean="${projectInstance}" field="name">
                    <div class="errors">
                      <g:renderErrors bean="${projectInstance}" as="list" field="name"/>
                    </div>
                  </g:hasErrors>
                </td>
              </tr>

              <tr class="prop">
                <td valign="top" class="name"><label for="comment">Comment:</label>
                </td>
                <td valign="top"
class="value ${hasErrors(bean:projectInstance,field:'comment','errors')}">
                  <textarea cols="40"  rows="3" name="comment" onkeydown="textCounter(document.createProjectForm.comment,200);" onkeyup="textCounter(document.createProjectForm.comment,200);">${fieldValue(bean:projectInstance,field:'comment')}</textarea>
                  <g:hasErrors
                    bean="${projectInstance}" field="comment">
                    <div class="errors"><g:renderErrors bean="${projectInstance}"
                             as="list" field="comment" /></div>
                </g:hasErrors></td>
              </tr>

              <tr class="prop">
                <td valign="top" class="name"><label for="startDate">Start
                Date:</label></td>
                <td valign="top"
class="value ${hasErrors(bean:projectInstance,field:'startDate','errors')}">
                  <link rel="stylesheet" href="${createLinkTo(dir:'css',file:'ui.datepicker.css')}" />
                  <g:javascript library="ui.datepicker" />
                  <script type="text/javascript" charset="utf-8">
                    jQuery(function($){$('.dateRange').datepicker({showOn: 'both', buttonImage: '${createLinkTo(dir:'images',file:'calendar.gif')}', buttonImageOnly: true,beforeShow: customRange});function customRange(input) {return null;}});
                  </script>
                  <input type="text" class="dateRange" size="15" style="width:112px;height:14px;" name="startDate" value="<tds:convertDate date="${projectInstance?.startDate}"/>" onchange="setCompletionDate(this.value)">
<g:hasErrors
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
                  <input type="text" class="dateRange" size="15" style="width:112px;height:14px;" id="completionDateId" name="completionDate" value="<tds:convertDate date="${projectInstance?.completionDate}"/>">

<g:hasErrors bean="${projectInstance}" field="completionDate">
                    <div class="errors"><g:renderErrors bean="${projectInstance}"
                             as="list" field="completionDate" /></div>
                </g:hasErrors></td>
              </tr>

              <tr class="prop">
                <td valign="top" class="name"><label for="projectPartner">Partner:</label>
                </td>
                <td valign="top" class="value"><select id="projectPartnerId"
                                 name="projectPartner"
                                 onchange="${remoteFunction(action:'getPartnerStaffList', params:'\'partner=\' + this.value', onComplete:'appendPartnerStaff(e)' )}">
                    <option value="" selected="selected">None</option>
                    <g:each status="i" in="${companyPartners}" var="companyPartners">
                      <option value="${companyPartners?.partyIdTo.id}">${companyPartners?.partyIdTo}</option>
                    </g:each>
                </select></td>
              </tr>

              <tr class="prop">
                <td valign="top" class="name"><label for="projectManager">Project
                Manager:</label></td>
                <td valign="top" class="value"><select id="projectManagerId"
                                 name="projectManager">
                    <option value="" selected="selected">Please Select</option>
                    <optgroup label="TDS" >
                      <g:each status="i" in="${companyStaff}" var="companyStaff">
                        <option value="${companyStaff.partyIdTo.id}">${companyStaff?.partyIdTo?.lastName},
                        ${companyStaff?.partyIdTo?.firstName} - ${companyStaff?.partyIdTo?.title}</option>
                      </g:each>
                    </optgroup>
                    <optgroup label="${projectPartner?.partyIdTo}" id="pmGroup">
                      <g:each status="i" in="${partnerStaff}" var="partnerStaff">
                        <option value="${partnerStaff?.partyIdTo.id}">${partnerStaff?.partyIdTo?.lastName}, ${partnerStaff?.partyIdTo?.firstName} - ${partnerStaff?.partyIdTo?.title}</option>
                      </g:each>
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
                      <g:each status="i" in="${companyStaff}" var="companyStaff">
                        <option value="${companyStaff?.partyIdTo.id}">${companyStaff?.partyIdTo.lastName},
                        ${companyStaff?.partyIdTo?.firstName} - ${companyStaff?.partyIdTo.title}</option>
                      </g:each>
                    </optgroup>
                    <optgroup label="${projectPartner?.partyIdTo}" id="mmGroup">
                      <g:each status="i" in="${partnerStaff}" var="partnerStaff">
                        <option value="${partnerStaff?.partyIdTo.id}">${partnerStaff?.partyIdTo?.lastName}, ${partnerStaff?.partyIdTo?.firstName} - ${partnerStaff?.partyIdTo?.title}</option>
                      </g:each>
                    </optgroup>
                  </select>
                  <input type="hidden" id="companyManagersId" value="${companyStaff.size()}">
                </td>
              </tr>

              <tr class="prop">
                <td valign="top" class="name">
                  <label for="trackChanges">Track Changes:</label>
                </td>
                <td valign="top" class="value ${hasErrors(bean:projectInstance,field:'trackChanges','errors')}">
                  <g:select id="trackChanges" name="trackChanges" from="${projectInstance.constraints.trackChanges.inList}" value="${projectInstance.trackChanges}" ></g:select>
                  <g:hasErrors bean="${projectInstance}" field="trackChanges">
                    <div class="errors">
                      <g:renderErrors bean="${projectInstance}" as="list" field="trackChanges"/>
                    </div>
                  </g:hasErrors>
                </td>
              </tr>
              <tr class="prop">
                <td valign="top" class="name">
                  <label for="dateCreated">Date Created:</label>
                </td>
                <td valign="top" class="value"><tds:convertDateTime date="${projectInstance?.dateCreated}" /> </td>
              </tr>

              <tr class="prop">
                <td valign="top" class="name">
                  <label for="lastUpdated">Last Updated:</label>
                </td>
                <td valign="top" class="value"><tds:convertDateTime date="${projectInstance?.lastUpdated}" /> </td>
              </tr>

            </tbody>
          </table>
        </div>
        <div class="buttons">
          <span class="button">
            <input class="save" type="submit" value="Update" />
          </span>
        </div>
      </g:form>
    </div>

    <div class="buttons">
      <g:form>
        <input type="hidden" name="id" value="${projectInstance?.id}" />
        <jsec:hasRole name="PROJECT_ADMIN">
          <span class="button">
            <input type="button" class="edit" value="Edit" onClick="return editProject()"/>
          </span>
        </jsec:hasRole>
        <jsec:hasRole name="ADMIN">
          <span class="button"><g:actionSubmit class="delete" onclick="return confirm('Are you sure?');" value="Delete" /></span>
        </jsec:hasRole>
      </g:form>
    </div>
  </body>
  </div>
  </body>
</html>
