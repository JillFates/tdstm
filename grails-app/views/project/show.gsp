<%@page import="net.transitionmanager.domain.Project" %>
<%@page import="net.transitionmanager.security.Permission"%>
<html>
	<head>
		<meta http-equiv="Content-Type" content="text/html; charset=UTF-8"/>
		<meta name="layout" content="topNav" />
		<title>Project Details</title>

		<asset:stylesheet href="css/ui.accordion.css" />
		<asset:stylesheet href="css/ui.resizable.css" />
		<asset:stylesheet href="css/ui.slider.css" />
		<asset:stylesheet href="css/ui.tabs.css" />
		<asset:stylesheet href="css/ui.datepicker.css" />

	</head>
	<body>
	<tds:subHeader title="Project Details" crumbs="['Project', 'Detail']"/>
		<g:if test="${flash.message}">
			<div class="message">${flash.message}</div>
		</g:if>

		<div class="body">

				<div class="nav" style="border: 1px solid #CCCCCC; height: 20px">
					<tds:hasPermission permission="${Permission.BundleEdit}">
						<span class="menuButton"><g:link class="create" controller="moveBundle" action="planningStats">Planning Dashboard</g:link></span>
					</tds:hasPermission>
				</div>
			<br/>

			<div class="dialog" id="updateShow">
				<table class="show-project-table" style="border-style:solid solid none solid;">
					<tbody>
						<tr class="prop">
							<td class="name"><label>Client:</label></td>
							<td class="valueNW">${projectInstance?.client}</td>
							<td class="name"><label>Project Code:</label></td>
							<td class="valueNW">${fieldValue(bean:projectInstance, field:'projectCode')}</td>
						</tr>
						<tr class="prop">
							<td class="name"><label>Project Name:</label></td>
							<td class="valueNW"><b>${fieldValue(bean:projectInstance, field:'name')}</b></td>
							<td class="name"><label>Project Type:</label></td>
							<td class="valueNW">${fieldValue(bean:projectInstance, field:'projectType')}</td>
						</tr>
						<tr class="prop">
							<td class="name"><label>Description:</label></td>
							<td class="valueNW"><textarea cols="40"	rows="3" readOnly="true" >${fieldValue(bean:projectInstance, field:'description')}</textarea></td>
							<td class="name"><label>Comment:</label></td>
							<td class="valueNW"><textarea cols="40"	rows="3" readOnly="true" >${fieldValue(bean:projectInstance, field:'comment')}</textarea></td>
						</tr>
						<tr class="prop">
							<td class="name"><label>Start Date:</label></td>
							<td class="valueNW"><tds:convertDate date="${projectInstance?.startDate}" /></td>
							<td class="name"><label>Completion Date:</td>
							<td class="valueNW"><tds:convertDate date="${projectInstance?.completionDate}" /></td>
						</tr>
						<tr class="prop">
							<td class="name"><label>Associated Partner(s):</label></td>
							<td class="valueNW">
								<ul>
								<g:each status="i" in="${projectPartners}" var="partner">
									<li>${partner.name}</li>
								</g:each>
								</ul>
							</td>
							<td class="name"><label>Project Logo:</label></td>
							<td class="valueNW">
								<g:if test="${projectLogoForProject}"><img src="${createLink(controller:'project', action:'showImage', id:projectLogoForProject.id)}" style="height: 30px;"/></g:if>
							</td>
						</tr>
						<tr class="prop">
							<td class="name"><label>Project Manager:</label></td>
							<td class="valueNW">
								<ul>
									<g:each status="i" in="${projectManagers}" var="manager">
									<li>${manager.toString() + (manager.title ? ', '+manager.title : '') }</li>
									</g:each>
								</ul>
								<br>
								<tds:hasPermission permission="${Permission.ProjectStaffEdit}">
								<g:link class="mmlink" controller="person" action="manageProjectStaff" params="[role: 'PROJ_MGR', clientStaff: '1']">Manage</g:link>
								</tds:hasPermission>
							</td>
							<td class="name"><label>Default Bundle:</label></td>
							<td class="valueNW">${fieldValue(bean:projectInstance, field:'defaultBundle')}</td>
						</tr>
						<tr class="prop">
							<td class="name"><label>Workflow:</label></td>
							<td class="valueNW" nowrap="nowrap">
								${fieldValue(bean:projectInstance, field:'workflowCode')}
							</td>
							<td class="name"><label >Time Zone:</label></td>
							<td class="valueNW">${projectInstance.timezone?projectInstance.timezone.code:''}</td>
						</tr>
						<tr class="prop">
							<td class="name"><label >Date Created:</label></label></td>
							<td class="valueNW"><tds:convertDateTime date="${projectInstance?.dateCreated}" /> </td>
							<td class="name">
								<label>Collect Reporting Metrics:</label></td>
							<td valign="top">
								<g:field type="checkbox" name="collectMetrics" checked="${projectInstance?.collectMetrics == 1}" disabled="true"/>
							</td>
						</tr>
						<tr>
							<td class="name"><label for="lastUpdated">Last Updated:</label></td>
							<td class="valueNW"><tds:convertDateTime date="${projectInstance?.lastUpdated}" /> </td>
							<td class="name"><label >GUID:</label></td>
							<td class="valueNW"> ${projectInstance.guid} </td>
						</tr>
						<tr>
							<td></td>
							<td></td>
							<td class="name"><label>Plan Methodology:</label></td>
							<td class="valueNW">${planMethodology?.label}</td>
						</tr>
					</tbody>
				</table>
			</div>

			<div class="buttons">
				<g:form>
					<input type="hidden" name="id" value="${projectInstance?.id}" />
					<tds:hasPermission permission="${Permission.ProjectEdit}">
						<span class="button">
							<g:actionSubmit type="button" class="edit" value="Edit"/>
						</span>
					</tds:hasPermission>
					<tds:hasPermission permission="${Permission.ProjectDelete}">
						<g:if test="${isDeleteable}">
						<span class="button">
							<g:actionSubmit class="delete" onclick="return confirm('Warning: This will delete the ${projectInstance?.name} project and all of the assets, events, bundles, and any historic data?');" value="Delete" />
						</span>
						</g:if>
					</tds:hasPermission>
					<span class="button">
						<input class="show" type="button" value="Field Settings" onclick="window.location='${createLink(controller:'module', action: 'fieldsettings')}/list';" />
					</span>
				</g:form>
			</div>
		</div>
		<script>
			currentMenuId = "#projectMenu";
			$('.menu-projects-current-project').addClass('active');
			$('.menu-parent-projects').addClass('active');

		</script>
	</body>
</html>
