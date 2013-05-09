<html>
  <head>
    <meta http-equiv="Content-Type" content="text/html; charset=UTF-8"/>
    <meta name="layout" content="projectHeader" />
    <g:javascript src="project.js"></g:javascript>
    <title>Show Project</title>

    <link type="text/css" rel="stylesheet" href="${resource(dir:'css',file:'ui.accordion.css')}"  />
    <link type="text/css" rel="stylesheet" href="${resource(dir:'css',file:'ui.resizable.css')}"  />
    <link type="text/css" rel="stylesheet" href="${resource(dir:'css',file:'ui.slider.css')}"  />
    <link type="text/css" rel="stylesheet" href="${resource(dir:'css',file:'ui.tabs.css')}"  />
    <link type="text/css" rel="stylesheet" href="${resource(dir:'css',file:'ui.datepicker.css')}" />

  </head>
  <body>

  <g:if test="${flash.message}">
      <div class="message">${flash.message}</div>
    </g:if>
   
    <div class="body">
    <h1>Project</h1>
        
     <tds:hasPermission permission='CreateProject'>
    <div class="nav" style="border: 1px solid #CCCCCC; height: 11px">
        <span class="menuButton"><g:link class="create" action="create">New Project</g:link></span>
        <tds:hasPermission permission='MoveBundleEditView'>
          <span class="menuButton"><g:link class="create" controller="moveBundle" action="planningConsole">Planning Console</g:link></span>
        </tds:hasPermission>
    </div>
      </tds:hasPermission>
    <br/>
  	
    <div class="dialog" id="updateShow">
      <table style="border-style:solid solid none solid;">
        <tbody>
		<tr class="prop">
			<td valign="top" class="name">Associated Client:</td>
			<td valign="top" class="valueNW">${projectInstance?.client}</td>
			<td valign="top" class="name">Project Code:</td>
			<td valign="top" class="valueNW">${fieldValue(bean:projectInstance, field:'projectCode')}</td>
		</tr>
		<tr class="prop">
			<td valign="top" class="name">Project Name:</td>
			<td valign="top" class="valueNW">${fieldValue(bean:projectInstance, field:'name')}</td>
			<td valign="top" class="name">Project Type:</td>
			<td valign="top" class="valueNW">${fieldValue(bean:projectInstance, field:'projectType')}</td>
		</tr>
		<tr class="prop">
			<td valign="top" class="name">Description:</td>
			<td valign="top" class="valueNW"><textarea cols="40"  rows="3" readOnly="true" >${fieldValue(bean:projectInstance, field:'description')}</textarea></td>
			<td valign="top" class="name">Comment:</td>
			<td valign="top" class="valueNW"><textarea cols="40"  rows="3" readOnly="true" >${fieldValue(bean:projectInstance, field:'comment')}</textarea></td>
		</tr>
		<tr class="prop">
			<td valign="top" class="name">Start Date:</td>
			<td valign="top" class="valueNW"><tds:convertDate date="${projectInstance?.startDate}" timeZone="${request.getSession().getAttribute('CURR_TZ')?.CURR_TZ}"/></td>
			<td valign="top" class="name">Completion Date:</td>
			<td valign="top" class="valueNW"><tds:convertDate date="${projectInstance?.completionDate}" timeZone="${request.getSession().getAttribute('CURR_TZ')?.CURR_TZ}"/></td>
		</tr>
		<tr class="prop">
          	<td valign="top" class="name">Associated Partner:</td>
			<td valign="top" class="valueNW">${projectPartner?.partyIdTo}</td>
			<td valign="top" class="name">Partner Image:</td>
			<td valign="top" class="valueNW">
				<g:if test="${projectLogoForProject}"><img src="${createLink(controller:'project', action:'showImage', id:projectLogoForProject.id)}" style="height: 30px;"/></g:if>
			</td>
		</tr>
		<tr class="prop">
			<td valign="top" class="name">Project Manager:</td>
			<td valign="top" class="valueNW">
				<g:if test="${projectManager?.partyIdTo?.lastName}">${projectManager?.partyIdTo?.lastName},</g:if>
				<g:if test="${projectManager?.partyIdTo?.firstName}"> ${projectManager?.partyIdTo?.firstName}</g:if>
				<g:if test="${projectManager?.partyIdTo?.title}"> - ${projectManager?.partyIdTo?.title}</g:if>
			</td>
			<td valign="top" class="name">Event Manager:</td>
			<td valign="top" class="valueNW">
				<g:if test="${moveManager?.partyIdTo?.lastName}">${moveManager?.partyIdTo?.lastName},</g:if>
				<g:if test="${moveManager?.partyIdTo?.firstName}"> ${moveManager?.partyIdTo?.firstName}</g:if>
				<g:if test="${moveManager?.partyIdTo?.title}"> - ${moveManager?.partyIdTo?.title}</g:if>
			</td>
		</tr>
		<tr class="prop">
			<td valign="top" class="name"><g:message code="project.customFieldsShown.label" default="Custom Fields Shown" />:</td>
			<td valign="top" class="valueNW" colspan="3">${fieldValue(bean: projectInstance, field: "customFieldsShown")}</td>
		</tr>
		</tbody>
      </table>
      <table style="border-style:none solid none solid;">
      <tbody>
		<g:each in="${ (1..24) }" var="i">
			<g:if test="${i % 4 == 1}">
				<tr class="prop custom_table" id="custom_count_${i}">
			</g:if>
				<td valign="top" class="name" nowrap="nowrap"><g:message code="project.(custom${i}).label" default="Custom${i} Label" />:</td>
				<td valign="top" class="valueNW" nowrap="nowrap">${projectInstance.('custom'+i)}</td>
			<g:if test="${i % 4 == 0}">
				</tr>
			</g:if>
	    </g:each>
        <tr class="prop">
			<td valign="top" class="name" colspan="2">Workflow Code:</td>
			<td valign="top" class="value" colspan="2" nowrap="nowrap">${fieldValue(bean:projectInstance, field:'workflowCode')} &nbsp;&nbsp;
				<span class="name">Runbook Driven: </span>&nbsp;
				<span class="valueNW"><input type="checkbox" name="runbookOn" id="runbookOn" ${ (projectInstance.runbookOn==1 ? 'checked="checked"':'') } disabled="disabled" /></span>
            </td>
			<td valign="top" class="name" colspan="2">Display Transitions in Status bar:</td>
            <td valign="top" class="value" colspan="2"><g:message code="project.trackChanges.${bean:projectInstance?.trackChanges}" /></td>
		</tr>
		<tr class="prop">
			<td valign="top" class="name" colspan="2">
				<label for="dateCreated">Date Created:</label>
			</td>
			<td valign="top" class="value" colspan="2"><tds:convertDateTime date="${projectInstance?.dateCreated}" timeZone="${request.getSession().getAttribute('CURR_TZ')?.CURR_TZ}"/> </td>
			<td valign="top" class="name" colspan="2">
				<label for="lastUpdated">Last Updated:</label>
			</td>
			<td valign="top" class="value" colspan="2"><tds:convertDateTime date="${projectInstance?.lastUpdated}" timeZone="${request.getSession().getAttribute('CURR_TZ')?.CURR_TZ}"/> </td>
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
          <span class="button"><g:actionSubmit class="delete" onclick="return confirm('Warning: This will delete the ${projectInstance?.name} project and all of the assets, events, bundles, and any historic data?');" value="Delete" /></span>
        </tds:hasPermission>
      </g:form>
    </div></div>
<script>
	currentMenuId = "#projectMenu";
	$("#projectMenuId a").css('background-color','#003366')
	
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
