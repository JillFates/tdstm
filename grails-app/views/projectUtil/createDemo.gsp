<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
<meta name="layout" content="projectHeader" />
<title>Create Demo Project</title>

</head>
<body>

<div class="body">
<h1>Create Demo Project</h1>
<div class="nav" style="border: 1px solid #CCCCCC; height: 11px">
<span class="menuButton"><g:link controller="project" class="list" action="list">Project List</g:link></span>
</div>
<br/>
<g:if test="${flash.message}">
	<div class="message">${flash.message}</div>
</g:if> 
<g:hasErrors bean="${projectInstance}" >
					<div class="errors"><g:renderErrors bean="${projectInstance}"
						as="list" /></div>
				</g:hasErrors>
<g:form action="saveDemoProject" method="post" name="createDemoProjectForm">
	<div class="dialog">
	<table>
		<tbody>	
			<tr>
			<td colspan="2"><div class="required"> Fields marked ( * ) are mandatory </div> </td>
			</tr>
			<tr class="prop">
				<td valign="top" class="name"><label for="name"><b>Demo Project Name:&nbsp;<span style="color: red">*</span></b></label></td>
				<td valign="top" class="value">
					<input type="text" id="name" name="name" maxlength="20" value="${name}" />
					<g:if test="${nameError}">
						<div class="errors">${nameError}</div>
					</g:if> 
				</td>
			</tr>
			<tr class="prop">
				<td valign="top" class="name"><label for="template"><b>Demo Project Template:&nbsp;<span style="color: red">*</span></b></label></td>
				<td valign="top" class="value">
					<g:select id="template" name="template" from="${Project.findAllByProjectType('Template')}" optionKey="id" value="${template}"/>
				</td>
			</tr>

			<tr class="prop">
				<td valign="top" class="name"><label for="startDate"><b>Demo start date/time:&nbsp;<span style="color: red">*</span></b></label></td>
				<td valign="top" class="value">
					<script type="text/javascript" charset="utf-8">
					 $(document).ready(function(){
	                      $("#startDateId").datetimepicker();
	                    });
                  	</script> 
                  	<input type="text" class="dateRange" size="15" style="width: 132px; height: 14px;"  name="startDate" id="startDateId" value="${startDate}"/> 
                  	<g:if test="${startDateError}">
						<div class="errors">${startDateError}</div>
					</g:if> 
				</td>
			</tr>

			<tr class="prop">
				<td valign="top" class="name"><label for="completionDate">Demo Cleanup:</label></td>
				<td valign="top" class="value">
				<script type="text/javascript" charset="utf-8">
				$(document).ready(function(){
                    $("#cleanupDateId").datetimepicker();
                  });
                </script> 
                <input type="text" class="dateRange" size="15" style="width: 132px; height: 14px;"  id="cleanupDateId"	name="cleanupDate" value="${cleanupDate}"/>
				</td>
			</tr>

		</tbody>
	</table>
	</div>
	<div class="buttons">
		<span class="button">
			<input class="save" type="submit" value="Save" />
		</span> 
		<span class="button">
			<input type="button" class="delete" onclick="javascript:location.href='../project/list'" value="Cancel" />
		</span>
	</div>
</g:form></div>
</body>
</html>
