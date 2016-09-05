<html>
	<head>
		<meta http-equiv="Content-Type" content="text/html; charset=UTF-8"/>
		<meta name="layout" content="topNav" />
		<title>Project Details</title>

		<link type="text/css" rel="stylesheet" href="${resource(dir:'css',file:'ui.accordion.css')}" />
		<link type="text/css" rel="stylesheet" href="${resource(dir:'css',file:'ui.resizable.css')}" />
		<link type="text/css" rel="stylesheet" href="${resource(dir:'css',file:'ui.slider.css')}" />
		<link type="text/css" rel="stylesheet" href="${resource(dir:'css',file:'ui.tabs.css')}" />
		<link type="text/css" rel="stylesheet" href="${resource(dir:'css',file:'ui.datepicker.css')}" />

	</head>
	<body>
		
		<g:if test="${flash.message}">
			<div class="message">${flash.message}</div>
		</g:if>
		 
		<div class="body">
			<h1>Project Details</h1>
					
				<div class="nav" style="border: 1px solid #CCCCCC; height: 20px">
					<tds:hasPermission permission='MoveBundleEditView'>
						<span class="menuButton"><g:link class="create" controller="moveBundle" action="planningStats">Planning Dashboard</g:link></span>
					</tds:hasPermission>
				</div>
			<br/>
			
			<div class="dialog" id="updateShow">
				<table class="show-project-table" style="border-style:solid solid none solid;">
					<tbody>
						<tr class="prop">
							<td class="name">Client:</td>
							<td class="valueNW">${projectInstance?.client}</td>
							<td class="name">Project Code:</td>
							<td class="valueNW">${fieldValue(bean:projectInstance, field:'projectCode')}</td>
						</tr>
						<tr class="prop">
							<td class="name">Project Name:</td>
							<td class="valueNW"><b>${fieldValue(bean:projectInstance, field:'name')}</b></td>
							<td class="name">Project Type:</td>
							<td class="valueNW">${fieldValue(bean:projectInstance, field:'projectType')}</td>
						</tr>
						<tr class="prop">
							<td class="name">Description:</td>
							<td class="valueNW"><textarea cols="40"	rows="3" readOnly="true" >${fieldValue(bean:projectInstance, field:'description')}</textarea></td>
							<td class="name">Comment:</td>
							<td class="valueNW"><textarea cols="40"	rows="3" readOnly="true" >${fieldValue(bean:projectInstance, field:'comment')}</textarea></td>
						</tr>
						<tr class="prop">
							<td class="name">Start Date:</td>
							<td class="valueNW"><tds:convertDate date="${projectInstance?.startDate}" /></td>
							<td class="name">Completion Date:</td>
							<td class="valueNW"><tds:convertDate date="${projectInstance?.completionDate}" /></td>
						</tr>
						<tr class="prop">
							<td class="name">Associated Partner(s):</td>
							<td class="valueNW">
								<ul>
								<g:each status="i" in="${projectPartners}" var="partner">
									<li>${partner.name}</li>
								</g:each>
								</ul>
							</td>
							<td class="name">Project Logo:</td>
							<td class="valueNW">
								<g:if test="${projectLogoForProject}"><img src="${createLink(controller:'project', action:'showImage', id:projectLogoForProject.id)}" style="height: 30px;"/></g:if>
							</td>
						</tr>
						<tr class="prop">
							<td class="name">Project Manager:</td>
							<td class="valueNW">
								<ul>
									<g:each status="i" in="${projectManagers}" var="manager">
									<li>${manager.toString() + (manager.title ? ', '+manager.title : '') }</li>
									</g:each>
								</ul>
								<br>
								<tds:hasPermission permission='EditProjectStaff'>
								<g:link class="mmlink" controller="person" action="manageProjectStaff" params="[role: 'PROJ_MGR', clientStaff: '1']">Manage</g:link>
								</tds:hasPermission>
							</td>
							<td class="name">Default Bundle:</td>
							<td class="valueNW">${fieldValue(bean:projectInstance, field:'defaultBundle')}</td>
						</tr>
						<tr class="prop">
							<td class="name">Workflow:</td>
							<td class="valueNW" nowrap="nowrap">
								${fieldValue(bean:projectInstance, field:'workflowCode')} 
							</td>
							<td class="name">Time Zone:</td>
							<td class="valueNW">${projectInstance.timezone?projectInstance.timezone.code:''}</td>
						</tr>
						<tr class="prop">
							<td class="name"><label for="dateCreated">Date Created:</label></td>
							<td class="valueNW"><tds:convertDateTime date="${projectInstance?.dateCreated}" /> </td>
						</tr>
						<tr>
							<td class="name"><label for="lastUpdated">Last Updated:</label></td>
							<td class="valueNW" colspan="3"><tds:convertDateTime date="${projectInstance?.lastUpdated}" /> </td>
						</tr>
					</tbody>
				</table>
			</div>

			<div class="buttons">
				<g:form>
					<input type="hidden" name="id" value="${projectInstance?.id}" />
					<tds:hasPermission permission='ProjectEditView'>
						<span class="button">
							<g:actionSubmit type="button" class="edit" value="Edit"/>
						</span>
					</tds:hasPermission>
					<tds:hasPermission permission='ProjectDelete'>
						<g:if test="${isDeleteable}">
						<span class="button">
							<g:actionSubmit class="delete" onclick="return confirm('Warning: This will delete the ${projectInstance?.name} project and all of the assets, events, bundles, and any historic data?');" value="Delete" />
						</span>
						</g:if>
					</tds:hasPermission>
					<span class="button">
						<g:actionSubmit class="show" value="Field Settings" action="fieldImportance" />
					</span>
				</g:form>
			</div>
		</div>
		<script>
			currentMenuId = "#projectMenu";
			$('.menu-projects-current-project').addClass('active');
			$('.menu-parent-projects').addClass('active');
			
			$(document).ready(function() {
				var customCol = ${projectInstance.customFieldsShown}
				showCustomFields(customCol, 4);
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
		</script>
	</body>
</html>
