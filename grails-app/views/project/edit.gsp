<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
<meta name="layout" content="projectHeader" />
<link type="text/css" rel="stylesheet"
	href="${resource(dir:'css',file:'ui.datepicker.css')}" />
<g:javascript src="project.js"></g:javascript>
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

		 <br/>
            <g:if test="${flash.message}">
            <div class="message">${flash.message}</div>
            </g:if>
            <g:form method="post" action="update" name="editProjectForm" enctype="multipart/form-data">
                <div class="dialog">
                    <table>
                        <tbody>
		                        <tr>
								<td colspan="4"><div class="required"> Fields marked ( * ) are mandatory </div> </td>
								</tr>
                            <tr class="prop">
					            <td valign="top" class="name">Associated Client:</td>
					
					            <td valign="top" class="value">${projectInstance?.client}</td>
					
					            <td valign="top" class="name">Project Code:</td>
					
					            <td valign="top" class="value">${fieldValue(bean:projectInstance, field:'projectCode')}</td>
					
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
								<td valign="top" class="name"><label for="projectType"><b>Project Type:&nbsp;<span style="color: red">*</span></b></label></td>
								<td valign="top" class="value ${hasErrors(bean:projectInstance,field:'projectType','errors')}">
									<g:select id="projectType" name="projectType" from="${projectInstance.constraints.projectType.inList}" value="${projectInstance.projectType}"></g:select>
									<g:hasErrors bean="${projectInstance}" field="projectType">
										<div class="errors"><g:renderErrors bean="${projectInstance}" as="list" field="projectType" /></div>
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
			                  <script type="text/javascript" charset="utf-8">
			                    jQuery(function($){$('.dateRange').datepicker({showOn: 'both', buttonImage: '${resource(dir:'images',file:'calendar.gif')}', buttonImageOnly: true,beforeShow: customRange});function customRange(input) {return null;}});
			                  </script>
			                  <input type="text" class="dateRange" size="15" style="width:112px;height:14px;" name="startDate" id="startDateId"
			                   value="<tds:convertDate date="${projectInstance?.startDate}" timeZone="${request.getSession().getAttribute('CURR_TZ')?.CURR_TZ}"/>" onchange="setCompletionDate(this.value);isValidDate(this.value);"/>
							<g:hasErrors bean="${projectInstance}" field="startDate">
			                    <div class="errors"><g:renderErrors bean="${projectInstance}" as="list" field="startDate" /></div>
			                </g:hasErrors>
			                </td>
			                <td valign="top" class="name"><label for="completionDate"><b>Completion Date:&nbsp;<span style="color: red">*</span></b></label></td>
			                <td valign="top" class="value ${hasErrors(bean:projectInstance,field:'completionDate','errors')}">
			                  <script type="text/javascript" charset="utf-8">
			                    jQuery(function($){$('.dateRange').datepicker({showOn: 'both', buttonImage: '${resource(dir:'images',file:'calendar.gif')}', buttonImageOnly: true,beforeShow: customRange});function customRange(input) {return null;}});
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
			                </select>
			                </td>
							<td valign="top" class="name"><label for="client">Partner Image:</label>
							</td>
							<g:if test="${projectLogoForProject}">
							<td valign="top" class="value"><g:link  action="deleteImage" params='["id":projectInstance?.id]'><img src="${createLink(controller:'project', action:'showImage', id:projectLogoForProject.id)}" style="height: 30px;border:0px;"/><img src="${resource(dir:'images',file:'delete.png' )}" style="border:0px;padding:6px;"/></g:link></td>
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
						<tr>
							<td valign="top" class="name"><label for="customFieldCount">
									Custom Fields Shown: </label></td>
							<td valign="top" class="value"><g:select id="customcount" name="customFieldsShown" from="${projectInstance.constraints.customFieldsShown.inList}"
									 value="${projectInstance.customFieldsShown}" onchange="showCustomFields(this.value, 2);" /></td>
						</tr>
						<g:each in="${ (1..24) }" var="i">
							<g:if test="${i % 2 == 1}">
								<tr class="prop custom_table" id="custom_count_${i}" style="display: none;">
							</g:if>
								<td valign="top" class="name" nowrap="nowrap">
									<label for="custom$i"><g:message code="project.(custom${i}).label" default="Custom${i} Label" /></label>:
								</td>
								<td valign="top" class="value ${hasErrors(bean: projectInstance, field: 'custom'+i, 'errors')}">
									<g:textField name="custom${i}" value="${projectInstance?.('custom'+i)}" />
								</td>
							<g:if test="${i % 2 == 0}">
								</tr>
							</g:if>
					    </g:each>
						<tr class="prop">

							<td valign="top" class="name">Workflow Code:</td>

							<td valign="top"
								class="value ${hasErrors(bean:projectInstance,field:'workflowCode','errors')}">
								<g:select id="workflowCode" name="workflowCode"
									from="${workflowCodes}"
									value="${projectInstance?.workflowCode}"
									noSelection="['':'Please Select']" onChange="warnForWorkflow()"></g:select>
								&nbsp;&nbsp; <span class="name"> <label for="runbookOn">Runbook
										Driven:</label>
							</span>&nbsp; <span
								class="value ${hasErrors(bean: projectInstance, field: 'runbookOn', 'errors')}">
									<input type="checkbox" name="runbookOn" id="runbookOn"
									${ (projectInstance.runbookOn == 1 ? 'checked="checked"':'') } />
							</span> <g:hasErrors bean="${projectInstance}" field="workflowCode">
									<div class="errors">
										<g:renderErrors bean="${projectInstance}" as="list"
											field="workflowCode" />
									</div>
								</g:hasErrors>
							</td>
							<td valign="top" class="name"><label for="inProgress">Display
									Transitions in Status bar:</label></td>
							<td valign="top"><g:select id="trackChanges"
									name="trackChanges"
									from="${projectInstance.constraints.trackChanges.inList}"
									value="${projectInstance.trackChanges}"
									valueMessagePrefix="project.trackChanges"></g:select> <g:hasErrors
									bean="${projectInstance}" field="trackChanges">
									<div class="errors">
										<g:renderErrors bean="${projectInstance}" as="list"
											field="trackChanges" />
									</div>
								</g:hasErrors></td>
						</tr>
						<tr class="prop">
							<td valign="top" class="name"><label for="dateCreated">Date
									Created:</label></td>
							<td valign="top" class="value"><tds:convertDateTime
									date="${projectInstance?.dateCreated}"
									timeZone="${request.getSession().getAttribute('CURR_TZ')?.CURR_TZ}" />
							</td>
							<td valign="top" class="name"><label for="lastUpdated">Last
									Updated:</label></td>
							<td valign="top" class="value"><tds:convertDateTime
									date="${projectInstance?.lastUpdated}"
									timeZone="${request.getSession().getAttribute('CURR_TZ')?.CURR_TZ}" />
							</td>
						</tr>

					</tbody>
				</table>
			</div>
			<div class="buttons">
				<span class="button"><g:actionSubmit class="save"
						value="Update" onclick="return validateDates()" /></span> <span
					class="button"><g:actionSubmit class="delete"
						onclick="if(confirm('Warning: This will delete the ${projectInstance?.name} project and all of the assets, events, bundles, and any historic data?')){document.editProjectForm.action = 'delete'};"
						value="Delete" /></span>
			</div>
		</g:form>
	</div>
	<script type="text/javascript">
	 $(document).ready(function() {
		var customCol = ${projectInstance.customFieldsShown}
		showCustomFields(customCol, 2);
	 });
        
       
        
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
        function warnForWorkflow(){
            alert("Warning: Changing the workflow for a project underway can create problems!")
         return true
       }
       </script>
<script>
	currentMenuId = "#projectMenu";
	$("#projectMenuId a").css('background-color','#003366')
</script>
</body>
</html>
