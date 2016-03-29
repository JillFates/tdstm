<html>
<head>
	<meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
	<meta name="layout" content="projectHeader" />
	<link type="text/css" rel="stylesheet"
		href="${resource(dir:'css',file:'ui.datepicker.css')}" />
	<title>Edit Project</title>
	<% def currProj = session.getAttribute("CURR_PROJ");
				def projectId = currProj.CURR_PROJ ;
				def currProjObj;
				if( projectId != null) {
				currProjObj = Project.findById(projectId);
				}
		%>
	<script src="${resource(dir:'js',file:'project.js')}"></script>
</head>
<body>
	<div class="body">
		<h1>Edit Project</h1>
		<br/>
			<g:if test="${flash.message}">
				<div class="message">${flash.message}</div>
			</g:if>
			<g:form action="update" method="post" name="editProjectForm" enctype="multipart/form-data">
			<div class="dialog">
				<table>
					<tbody>
						<tr>
							<td colspan="4"><div class="required"> Fields marked ( * ) are mandatory </div> </td>
						</tr>
						<tr class="prop">
							<td class="name">Client:</td>
					
							<td class="valueNW">${projectInstance?.client}</td>
					
							<td class="name">Project Code:</td>
					
							<td class="valueNW ${hasErrors(bean:projectInstance, field:'projectCode','errors')}">
								<input type="text" id="projectCode" name="projectCode" indextab="110" value="${fieldValue(bean:projectInstance, field:'projectCode')}" />
								<g:hasErrors bean="${projectInstance}" field="projectCode">
									<div class="errors">
										<g:renderErrors bean="${projectInstance}" as="list" field="projectCode" />
									</div>
								</g:hasErrors>
							</td>
						</tr>           
						<tr class="prop">
							<td class="name">
								<label for="name"><b>Project Name:&nbsp;<span style="color: red">*</span></b></label>
							</td>
							<td class="valueNW ${hasErrors(bean:projectInstance,field:'name','errors')}">
								<input type="text" id="name" name="name" maxlength="64" indextab="120" value="${fieldValue(bean:projectInstance,field:'name')}" />
								<g:hasErrors bean="${projectInstance}" field="name">
									<div class="errors">
										<g:renderErrors bean="${projectInstance}" as="list" field="name" />
									</div>
								</g:hasErrors>
							</td>
							<td class="name">
								<label for="projectType"><b>Project Type:&nbsp;<span style="color: red">*</span></b></label>
							</td>
							<td class="valueNW ${hasErrors(bean:projectInstance,field:'projectType','errors')}">
								<g:select id="projectType" name="projectType" indextab="130" from="${projectInstance.constraints.projectType.inList}" value="${projectInstance.projectType}"></g:select>
								<g:hasErrors bean="${projectInstance}" field="projectType">
									<div class="errors"><g:renderErrors bean="${projectInstance}" as="list" field="projectType" /></div>
								</g:hasErrors>
							</td>
						</tr>
						<tr class="prop">
							<td class="name">
								<label for="description">Description:</label>
							</td>
							<td class="valueNW ${hasErrors(bean:projectInstance,field:'description','errors')}">
								<textarea rows="3" cols="40" id="description" name="description"
									indextab="140" 
									placeholder="Enter a short description of the project"
									onkeydown="Project.textCounter(this.id,200);"
									onkeyup="Project.textCounter(this.id,200);">${fieldValue(bean:projectInstance,field:'description')}</textarea>
								<g:hasErrors bean="${projectInstance}" field="description">
									<div class="errors">
										<g:renderErrors bean="${projectInstance}" as="list" field="description" />
									</div>
								</g:hasErrors>
							</td>
							<td class="name">
								<label for="comment">Comment:</label>
							</td>
							<td class="valueNW ${hasErrors(bean:projectInstance,field:'comment','errors')}">
								<textarea rows="3" cols="40" id="comment" name="comment"
									indextab="150" 
									onkeydown="Project.textCounter(this.id,200);"
									onkeyup="Project.textCounter(this.id,200);">${fieldValue(bean:projectInstance,field:'comment')}</textarea>
								<g:hasErrors bean="${projectInstance}" field="comment">
									<div class="errors">
										<g:renderErrors bean="${projectInstance}" as="list" field="comment" />
									</div>
								</g:hasErrors>
							</td>
						</tr> 
						<tr class="prop">
							<td class="name">
								<label for="startDate">Start Date:</label>
							</td>
							<td class="valueNW ${hasErrors(bean:projectInstance,field:'startDate','errors')}">
								<script type="text/javascript" charset="utf-8">
									jQuery(function($){$('.dateRange').datepicker({showOn: 'both', buttonImage: '${resource(dir:'images',file:'calendar.gif')}', buttonImageOnly: true,beforeShow: customRange});function customRange(input) {return null;}});
								</script>
								<input type="text" class="dateRange" indextab="160" size="15" style="width: 112px; height: 14px;" name="startDate" id="startDateId"
									value="<tds:convertDate date="${prevParam?.startDate?: projectInstance?.startDate}" />" onchange="setCompletionDate(this.value);isValidDate(this.value);" />
								<g:hasErrors bean="${projectInstance}" field="startDate">
									<div class="errors">
										<g:renderErrors bean="${projectInstance}" as="list" field="startDate" />
									</div>
								</g:hasErrors>
							</td>
							<td class="name">
								<label for="completionDate">
									<b>Completion Date:&nbsp;<span style="color: red">*</span></b>
								</label>
							</td>
							<td class="valueNW ${hasErrors(bean:projectInstance,field:'completionDate','errors')}">
								<script type="text/javascript" charset="utf-8">
									jQuery(function($){$('.dateRange').datepicker({showOn: 'both', buttonImage: '${resource(dir:'images',file:'calendar.gif')}', buttonImageOnly: true,beforeShow: customRange});function customRange(input) {return null;}});
								</script>
								<input type="text" class="dateRange" indextab="170" size="15" style="width: 112px; height: 14px;" id="completionDateId" 
									name="completionDate" value="<tds:convertDate date="${prevParam?.completionDate?: projectInstance?.completionDate}" />" onchange="isValidDate(this.value);" />
								<g:hasErrors bean="${projectInstance}" field="completionDate">
									<div class="errors"><g:renderErrors bean="${projectInstance}" as="list" field="completionDate" /></div>
								</g:hasErrors>
							</td>
						</tr>
		
						<tr class="prop">
							<td class="name">
								<label for="projectPartners">Associated Partner(s):</label>
							</td>
							<td class="valueNW">
								<input type="button" value="Add Partner" indextab="180" onclick="Project.addPartnerSelect('#partnersContainer');">
								<div id="partnersContainer"></div>
							</td>
							<td class="name">
								<label for="client">Project Logo:</label>
							</td>
							<g:if test="${projectLogoForProject}">
								<td class="valueNW">
									<g:link action="deleteImage" indextab="200" params='["id":projectInstance?.id]'><img src="${createLink(controller:'project', action:'showImage', id:projectLogoForProject.id)}" style="height: 30px;border:0px;"/><img src="${resource(dir:'icons',file:'delete.png' )}" style="border:0px;padding:6px;"/></g:link>
								</td>
							</g:if>
							<g:else>				
								<td class="valueNW">
									<input type="file" name="projectLogo" indextab="200" id="projectLogo" />
									<br>
									<span class="footnote">Select a jpg or gif file smaller than 50KB to appear in header</span> 
								</td>				
							</g:else>
						</tr>
						<tr class="prop">
							<td class="name">
								<label for="projectManager">Project Managers:</label>
							</td>
							<td class="valueNW">
								<ul>
									<g:each status="i" in="${projectManagers}" var="manager">
									<li>${manager.toString() + (manager.title ? ', '+manager.title : '') }</li>
									</g:each>
								</ul>
							</td>
							<td class="name">Default Bundle:</td>
							<td class="valueNW ${hasErrors(bean:projectInstance,field:'defaultBundle','errors')}">
								<g:select id="defaultBundle" name="defaultBundle.id"
									indextab="220" 
									from="${moveBundles}" optionKey="id" optionValue="name"
									value="${projectInstance?.defaultBundle.id}"></g:select>
							</td>
						</tr>
						<tr class="prop">
							<td class="name">
								<label for="workflowCode"><b>Workflow:&nbsp;<span style="color: red">*</span></b></label>
							</td>
							<td class="valueNW ${hasErrors(bean:projectInstance,field:'workflowCode','errors')}">
								<g:select id="workflowCode" name="workflowCode"
									indextab="230" 
									from="${workflowCodes}"
									value="${projectInstance?.workflowCode}"
									noSelection="['':'Please Select']" onChange="warnForWorkflow()">
								</g:select>
								<g:hasErrors bean="${projectInstance}" field="workflowCode">
									<div class="errors">
										<g:renderErrors bean="${projectInstance}" as="list" field="workflowCode" />
									</div>
								</g:hasErrors>
							</td>

							<td class="name">Time Zone:</td>
							<td class="valueNW">
								<input type="text" id="timezone" name="timezone" value="${projectInstance.timezone?projectInstance.timezone.code:''}" readonly style="width: 200px; padding-right: 20px">
								<input type="button" value="Change" indextab="240" onclick="Project.showTimeZoneSelect('timezone');">
							</td>
							
						</tr>
						<tr class="prop">
							<td class="name"><label for="dateCreated">Date Created:</label></td>
							<td class="valueNW">
								<tds:convertDateTime date="${projectInstance?.dateCreated}" />
							</td>
						</tr>
						<tr>
							<td class="name"><label for="lastUpdated">Last Updated:</label></td>
							<td class="valueNW" colspan="3">
								<tds:convertDateTime date="${projectInstance?.lastUpdated}" />
							</td>
						</tr>
					</tbody>
				</table>
			</div>
			<div class="buttons">
				<span class="button">
					<g:actionSubmit class="save" value="Update" onclick="return validateForm();" />
				</span>
				<span class="button">
					<input type="button" class="cancel" value="Cancel" id="cancelButtonId" onclick="window.location = contextPath + '/project/show/${projectInstance?.id}'" />
				</span>
			</div>
		</g:form>

		<%-- DIV for select time zone --%>
		<div id="timeZoneSelectPopup" style="display: none;min-width:250px;" title="Time Zone Select"></div>

	</div>
	<script type="text/javascript">
	$(document).ready(function() {

		$("#timeZoneSelectPopup").dialog({ autoOpen: false });
		
		var customCol = (${prevParam?.customFieldsShown?: projectInstance.customFieldsShown})?(${prevParam?.customFieldsShown?: projectInstance.customFieldsShown}):'0'
		showCustomFields(customCol, 2);

		$("#workflowCode").select2({
			placeholder: "Please Select",
			width: "75%"
		});
		$("#projectType").select2({
			placeholder: "Please Select",
			width: "75%"
		});
		$("#defaultBundle").select2({
			placeholder: "Please Select",
			width: "75%"
		});
		$("#projectManagerId").select2({
			placeholder: "Please Select",
			width: "75%"
		});
		$("#moveManagerId").select2({
			placeholder: "Please Select",
			width: "75%"
		});
		// Initialize company partners
		var companyPartners = []
		<g:each status="i" in="${companyPartners}" var="partner">
		companyPartners.push({"id": ${partner.id}, "text": "${partner.name}" });
		</g:each>

		Project.loadCompanyPartners(companyPartners);

		editProject();
	});

	 function showCustomFields(value, columnCount) {
	  $(".custom_table").hide();
	  if(value=='0'){
		  $("#custom_table").hide();
	  } else {
			 for(i=1;i<=value;){
			$("#custom_table").show();
			$("#custom_count_"+i).show();
			i=i+parseInt(columnCount)
		 }
		}  
	 }
		
		function editProject(){
			var projectPartners = []
			<g:each status="i" in="${projectPartners}" var="partner">
			projectPartners.push("${partner.id}")
			</g:each>
			Project.initCompanyPartnersSelects("#partnersContainer", projectPartners);
			Project.setActiveClientId(${projectInstance?.client.id});
		}
		function setCompletionDate(startDate){
		  var completionDateObj = document.editProjectForm.completionDate;
		  if(completionDateObj.value == ""){
		  completionDateObj.value = startDate;
		  }
		}
		function isValidDate( date ){
			var returnVal = true;
			if( date && !tdsCommon.isValidDate(date) ){
				alert("Date should be in '" + tdsCommon.defaultDateFormat() + "' format");
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
		function validateForm() {
			return validateDates() && Project.validSelectedPartners();
		}
	   </script>
<script>
	currentMenuId = "#projectMenu";
	$("#projectMenuId a").css('background-color','#003366')
</script>
</body>
</html>
