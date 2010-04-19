

<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8"/>
        <meta name="layout" content="projectHeader" />
        <link type="text/css" rel="stylesheet" href="${createLinkTo(dir:'css',file:'ui.datepicker.css')}" />
        <title>Edit Project</title>
         <% def currProj = session.getAttribute("CURR_PROJ");
		    def projectId = currProj.CURR_PROJ ;
		    def currProjObj;
		    if( projectId != null){
		      currProjObj = Project.findById(projectId);
		    }
    	%>
    </head>
    <body>
        
        <div class="body">
            <h1>Edit Project</h1>
            <div class="nav" style="border: 1px solid #CCCCCC; height: 11px">
            <span class="menuButton"><g:link class="list" action="list">Project List</g:link></span>
            <span class="menuButton"><g:link class="create" action="create">New Project</g:link></span>
	        </div>
	        <br/>
            <g:if test="${flash.message}">
            <div class="message">${flash.message}</div>
            </g:if>
            <g:form method="post" name="editProjectForm" enctype="multipart/form-data">
                <input type="hidden" name="id" value="${projectInstance?.id}" />
                <div class="dialog">
                    <table>
                        <tbody>
		                        <tr>
								<td colspan="2"><div class="required"> Fields marked ( * ) are mandatory </div> </td>
								</tr>
                                                    
                            <tr class="prop">
                                <td valign="top" class="name">
                                    <label for="name"><b>Project Name:&nbsp;<span style="color: red">*</span></b>:</label>
                                </td>
                                <td valign="top" class="value ${hasErrors(bean:projectInstance,field:'name','errors')}">
                                    <input type="text" id="name" name="name" value="${fieldValue(bean:projectInstance,field:'name')}"/>
                                <g:hasErrors bean="${projectInstance}" field="name">
					            <div class="errors">
					                <g:renderErrors bean="${projectInstance}" as="list" field="name"/>
					            </div>
					            </g:hasErrors>
                                </td>
                            </tr> 
                        	<tr class="prop">
                                <td valign="top" class="name">
                                    <label for="description">Description:</label>
                                </td>
                                <td valign="top" class="value ${hasErrors(bean:projectInstance,field:'description','errors')}">
                                    <textarea cols="40"  rows="3" id="description" name="description" onkeydown="textCounter(document.editProjectForm.description,200);" onkeyup="textCounter(document.editProjectForm.description,200);">${fieldValue(bean:projectInstance,field:'description')}</textarea>
                                <g:hasErrors bean="${projectInstance}" field="description">
					            <div class="errors">
					                <g:renderErrors bean="${projectInstance}" as="list" field="description"/>
					            </div>
					            </g:hasErrors>
                                </td>
                            </tr>
                            <tr class="prop">
                                <td valign="top" class="name">
                                    <label for="comment">Comment:</label>
                                </td>
                                <td valign="top" class="value ${hasErrors(bean:projectInstance,field:'comment','errors')}">
                                    <textarea cols="40"  rows="3" name="comment" onkeydown="textCounter(document.editProjectForm.comment,200);" onkeyup="textCounter(document.editProjectForm.comment,200);">${fieldValue(bean:projectInstance,field:'comment')}</textarea>
                                <g:hasErrors bean="${projectInstance}" field="comment">
					            <div class="errors">
					                <g:renderErrors bean="${projectInstance}" as="list" field="comment"/>
					            </div>
					            </g:hasErrors>
                                </td>
                            </tr> 
                        <tr class="prop">
			                <td valign="top" class="name"><label for="startDate">Start Date:</label></td>
			                <td valign="top" class="value ${hasErrors(bean:projectInstance,field:'startDate','errors')}">
			                  <g:javascript library="ui.datepicker" />
			                  <script type="text/javascript" charset="utf-8">
			                    jQuery(function($){$('.dateRange').datepicker({showOn: 'both', buttonImage: '${createLinkTo(dir:'images',file:'calendar.gif')}', buttonImageOnly: true,beforeShow: customRange});function customRange(input) {return null;}});
			                  </script>
			                  <input type="text" class="dateRange" size="15" style="width:112px;height:14px;" name="startDate" id="startDateId"
			                   value="<tds:convertDate date="${projectInstance?.startDate}" timeZone="${request.getSession().getAttribute('CURR_TZ')?.CURR_TZ}"/>" onchange="setCompletionDate(this.value);isValidDate(this.value);"/>
							<g:hasErrors bean="${projectInstance}" field="startDate">
			                    <div class="errors"><g:renderErrors bean="${projectInstance}" as="list" field="startDate" /></div>
			                </g:hasErrors></td>
			              </tr>
			
			              <tr class="prop">
			                <td valign="top" class="name"><label for="completionDate">Completion Date:</label></td>
			                <td valign="top" class="value ${hasErrors(bean:projectInstance,field:'completionDate','errors')}">
			                  <script type="text/javascript" charset="utf-8">
			                    jQuery(function($){$('.dateRange').datepicker({showOn: 'both', buttonImage: '${createLinkTo(dir:'images',file:'calendar.gif')}', buttonImageOnly: true,beforeShow: customRange});function customRange(input) {return null;}});
			                  </script>
			                  <input type="text" class="dateRange" size="15" style="width:112px;height:14px;" id="completionDateId" 
			                  name="completionDate" value="<tds:convertDate date="${projectInstance?.completionDate}" timeZone="${request.getSession().getAttribute('CURR_TZ')?.CURR_TZ}"/>" onchange="isValidDate(this.value)"/>
			
							<g:hasErrors bean="${projectInstance}" field="completionDate">
			                    <div class="errors"><g:renderErrors bean="${projectInstance}" as="list" field="completionDate" /></div>
			                </g:hasErrors></td>
			              </tr>
			
			              <tr class="prop">
			                <td valign="top" class="name"><label for="projectPartner">Partner:</label>
			                </td>
			                <td valign="top" class="value"><select id="projectPartnerId" name="projectPartner"
			                                 onchange="${remoteFunction(action:'getPartnerStaffList', params:'\'partner=\' + this.value', onComplete:'appendPartnerStaff(e)' )}">
			                    <option value="" selected="selected">None</option>
			                    <g:each status="i" in="${companyPartners}" var="companyPartners">
			                      <option value="${companyPartners?.partyIdTo.id}">${companyPartners?.partyIdTo}</option>
			                    </g:each>
			                </select></td>
			              </tr>
			              
			              <tr class="prop">
							<td valign="top" class="name"><label for="client">Partner Image:</label>
							</td>
							<g:if test="${projectLogoForProject}">
							<td valign="top" class="value"><g:link  action="deleteImage" params='["id":projectInstance?.id]'><img src="${createLink(controller:'project', action:'showImage', id:projectLogoForProject.id)}" style="height: 30px;border:0px;"/><img src="${createLinkTo(dir:'images',file:'delete.png' )}" style="border:0px;padding:6px;"/></g:link></td>
							</g:if>
							<g:else>				
							<td valign="top" class="value">
							<input type="file" name="partnerImage" id="partnerImage"/>
							</td>				
							</g:else>
						  </tr>
                           <tr class="prop">
			                <td valign="top" class="name"><label for="projectManager">Project
			                Manager:</label></td>
			                <td valign="top" class="value"><select id="projectManagerId"
			                                 name="projectManager">
			                    <option value="" selected="selected">Please Select </option>
			                    <optgroup label="TDS" >
			                      <g:each status="i" in="${companyStaff}" var="companyStaff">
			                        <option value="${companyStaff.partyIdTo.id}"><g:if test="${companyStaff?.partyIdTo?.lastName}">${companyStaff?.partyIdTo?.lastName},</g:if>
			                        <g:if test="${companyStaff?.partyIdTo?.firstName}"> ${companyStaff?.partyIdTo?.firstName}</g:if><g:if test="${companyStaff?.partyIdTo?.title}"> - ${companyStaff?.partyIdTo?.title}</g:if></option>
			                      </g:each>
			                    </optgroup>
			                    <optgroup label="${projectInstance?.client}">
			                      <g:each status="i" in="${clientStaff}" var="clientStaff">
			                        <option value="${clientStaff?.partyIdTo.id}"><g:if test="${clientStaff?.partyIdTo?.lastName}"> ${clientStaff?.partyIdTo?.lastName},</g:if>
			                        <g:if test="${clientStaff?.partyIdTo?.firstName}"> ${clientStaff?.partyIdTo?.firstName}</g:if> <g:if test="${clientStaff?.partyIdTo.title}"> - ${clientStaff?.partyIdTo.title}</g:if></option>
			                      </g:each>
			                    </optgroup>
			                    <optgroup label="${projectPartner?.partyIdTo}" id="pmGroup">
			                      <g:each status="i" in="${partnerStaff}" var="partnerStaff">
			                        <option value="${partnerStaff?.partyIdTo.id}"><g:if test="${partnerStaff?.partyIdTo?.lastName}">${partnerStaff?.partyIdTo?.lastName},</g:if>
			                        <g:if test="${partnerStaff?.partyIdTo?.firstName}"> ${partnerStaff?.partyIdTo?.firstName}</g:if> <g:if test="${partnerStaff?.partyIdTo?.title}"> - ${partnerStaff?.partyIdTo?.title}</g:if></option>
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
			                        <option value="${companyStaff?.partyIdTo.id}">
			                       <g:if test="${companyStaff?.partyIdTo?.lastName}"> ${companyStaff?.partyIdTo.lastName},</g:if>
			                        <g:if test="${companyStaff?.partyIdTo?.firstName}"> ${companyStaff?.partyIdTo?.firstName}</g:if> <g:if test="${companyStaff?.partyIdTo.title}"> - ${companyStaff?.partyIdTo.title}</g:if></option>
			                      </g:each>
			                    </optgroup>
			                    <optgroup label="${projectInstance?.client}">
			                      <g:each status="i" in="${clientStaff}" var="clientStaff">
			                        <option value="${clientStaff?.partyIdTo.id}"><g:if test="${clientStaff?.partyIdTo?.lastName}">${clientStaff?.partyIdTo.lastName},</g:if>
			                        <g:if test="${clientStaff?.partyIdTo?.firstName}">${clientStaff?.partyIdTo?.firstName}</g:if><g:if test="${clientStaff?.partyIdTo.title}"> - ${clientStaff?.partyIdTo.title}</g:if></option>
			                      </g:each>
			                    </optgroup>
			                    <optgroup label="${projectPartner?.partyIdTo}" id="mmGroup">
			                      <g:each status="i" in="${partnerStaff}" var="partnerStaff">
			                        <option value="${partnerStaff?.partyIdTo.id}"><g:if test="${partnerStaff?.partyIdTo?.lastName}">${partnerStaff?.partyIdTo?.lastName},</g:if>
			                        <g:if test="${partnerStaff?.partyIdTo?.firstName}"> ${partnerStaff?.partyIdTo?.firstName}</g:if> <g:if test="${partnerStaff?.partyIdTo?.title}"> - ${partnerStaff?.partyIdTo?.title}</g:if></option>
			                      </g:each>
			                    </optgroup>
			                  </select>
			                  <input type="hidden" id="companyManagersId" value="${companyStaff.size()+clientStaff.size()+ 1}" />
			                </td>
			              </tr>
			
			              <tr class="prop">
			                <td valign="top" class="name">
			                  <label for="trackChanges"><b>Track Changes:&nbsp;<span style="color: red">*</span></b></label>
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
			                <td valign="top" class="value"><tds:convertDateTime date="${projectInstance?.dateCreated}" timeZone="${request.getSession().getAttribute('CURR_TZ')?.CURR_TZ}"/> </td>
			              </tr>
			
			              <tr class="prop">
			                <td valign="top" class="name">
			                  <label for="lastUpdated">Last Updated:</label>
			                </td>
			                <td valign="top" class="value"><tds:convertDateTime date="${projectInstance?.lastUpdated}" timeZone="${request.getSession().getAttribute('CURR_TZ')?.CURR_TZ}"/> </td>
			              </tr>
                        
                        </tbody>
                    </table>
                </div>
                <div class="buttons">
                    <span class="button"><g:actionSubmit class="save" value="Update" onclick="return validateDates()"/></span>
                    <span class="button"><g:actionSubmit class="delete" onclick="return confirm('Are you sure?');" value="Delete" /></span>
                </div>
            </g:form>
        </div>
        <script type="text/javascript">
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
  	
  	      var length = managers.partnerStaff.length
  	      for (var i=0; i < length; i++) {
  	      var manager = managers.partnerStaff[i]
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
        
        function editProject(){
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
        editProject();
        function setCompletionDate(startDate){
  	      var completionDateObj = document.editProjectForm.completionDate;
  	      if(completionDateObj.value == ""){
  	      completionDateObj.value = startDate;
  	      }
        }
        var dateRegExp  = /^(0[1-9]|1[012])[/](0[1-9]|[12][0-9]|3[01])[/](19|20)\d\d$/;
        function isValidDate( date ){
            var returnVal = true;
          	if( date && !dateRegExp.test(date) ){
              	alert("Date should be in 'mm/dd/yyyy' format");
              	returnVal  =  false;
          	} 
          	return returnVal;
		}
        function validateDates(){
        	var returnval = false
            var startDateId = $("#startDateId").val();
            var completionDateId = $("#completionDateId").val();
            if(isValidDate(startDateId) && isValidDate(completionDateId)){
            	returnval = true;
			} 
            return returnval;
		}
        </script>
    </body>
</html>
