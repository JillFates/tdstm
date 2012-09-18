<html>
  <head>
    <meta http-equiv="Content-Type" content="text/html; charset=UTF-8"/>
    <meta name="layout" content="projectHeader" />
    <title>Show Project</title>

    <link type="text/css" rel="stylesheet" href="${createLinkTo(dir:'css',file:'ui.accordion.css')}"  />
    <link type="text/css" rel="stylesheet" href="${createLinkTo(dir:'css',file:'ui.resizable.css')}"  />
    <link type="text/css" rel="stylesheet" href="${createLinkTo(dir:'css',file:'ui.slider.css')}"  />
    <link type="text/css" rel="stylesheet" href="${createLinkTo(dir:'css',file:'ui.tabs.css')}"  />
    <link type="text/css" rel="stylesheet" href="${createLinkTo(dir:'css',file:'ui.datepicker.css')}" />

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
      <table>
        <tbody>

          <tr class="prop">
            <td valign="top" class="name">Associated Client:</td>

            <td valign="top" class="value">${projectInstance?.client}</td>

            <td valign="top" class="name">Project Code:</td>

            <td valign="top" class="value">${fieldValue(bean:projectInstance, field:'projectCode')}</td>

          </tr>

          <tr class="prop">
            <td valign="top" class="name">Project Name:</td>

            <td valign="top" class="value">${fieldValue(bean:projectInstance, field:'name')}</td>

	       	<td valign="top" class="name">Project Type:</td>

          	<td valign="top" class="value">${fieldValue(bean:projectInstance, field:'projectType')}</td>
          </tr>

          <tr class="prop">
            <td valign="top" class="name">Description:</td>

            <td valign="top" class="value"><textarea cols="40"  rows="3" readOnly="true" >${fieldValue(bean:projectInstance, field:'description')}</textarea></td>

            <td valign="top" class="name">Comment:</td>

            <td valign="top" class="value"><textarea cols="40"  rows="3" readOnly="true" >${fieldValue(bean:projectInstance, field:'comment')}</textarea></td>

          </tr>

          <tr class="prop">
            <td valign="top" class="name">Start Date:</td>

            <td valign="top" class="value"><tds:convertDate date="${projectInstance?.startDate}" timeZone="${request.getSession().getAttribute('CURR_TZ')?.CURR_TZ}"/></td>

            <td valign="top" class="name">Completion Date:</td>

            <td valign="top" class="value"><tds:convertDate date="${projectInstance?.completionDate}" timeZone="${request.getSession().getAttribute('CURR_TZ')?.CURR_TZ}"/></td>

          </tr>

          <tr class="prop">
            <td valign="top" class="name">Associated Partner:</td>

            <td valign="top" class="value">${projectPartner?.partyIdTo}</td>

            <td valign="top" class="name">Partner Image:</td>

			<g:if test="${projectLogoForProject}">

            <td valign="top" class="value"><img src="${createLink(controller:'project', action:'showImage', id:projectLogoForProject.id)}" style="height: 30px;"/></td>

			</g:if>

          </tr>

          <tr class="prop">
            <td valign="top" class="name">Project Manager:</td>
            <td valign="top" class="value">

                <g:if test="${projectManager?.partyIdTo?.lastName}">${projectManager?.partyIdTo?.lastName},</g:if>

                <g:if test="${projectManager?.partyIdTo?.firstName}"> ${projectManager?.partyIdTo?.firstName}</g:if>

                <g:if test="${projectManager?.partyIdTo?.title}"> - ${projectManager?.partyIdTo?.title}</g:if>

            </td>

            <td valign="top" class="name">Move Manager:</td>
            <td valign="top" class="value">

	            <g:if test="${moveManager?.partyIdTo?.lastName}">${moveManager?.partyIdTo?.lastName},</g:if>

	            <g:if test="${moveManager?.partyIdTo?.firstName}"> ${moveManager?.partyIdTo?.firstName}</g:if>

	            <g:if test="${moveManager?.partyIdTo?.title}"> - ${moveManager?.partyIdTo?.title}</g:if>

            </td>
          </tr>

		  <tr class="prop">
		  
          	<td valign="top" class="name"><g:message code="project.custom1.label" default="Custom1 Label" />:</td>
                            
            <td valign="top" class="value">${fieldValue(bean: projectInstance, field: "custom1")}</td>
            
            <td valign="top" class="name"><g:message code="project.custom2.label" default="Custom2 Label" />:</td>
                            
            <td valign="top" class="value">${fieldValue(bean: projectInstance, field: "custom2")}</td>
            
          </tr>
          <tr class="prop">
		  
          	<td valign="top" class="name"><g:message code="project.custom3.label" default="Custom3 Label" />:</td>
                            
            <td valign="top" class="value">${fieldValue(bean: projectInstance, field: "custom3")}</td>
            
            <td valign="top" class="name"><g:message code="project.custom4.label" default="Custom4 Label" />:</td>
                            
            <td valign="top" class="value">${fieldValue(bean: projectInstance, field: "custom4")}</td>
            
          </tr>
          <tr class="prop">
		  
          	<td valign="top" class="name"><g:message code="project.custom5.label" default="Custom5 Label" />:</td>
                            
            <td valign="top" class="value">${fieldValue(bean: projectInstance, field: "custom5")}</td>
            
            <td valign="top" class="name"><g:message code="project.custom6.label" default="Custom6 Label" />:</td>
                            
            <td valign="top" class="value">${fieldValue(bean: projectInstance, field: "custom6")}</td>
            
          </tr>
          <tr class="prop">
		  
          	<td valign="top" class="name"><g:message code="project.custom7.label" default="Custom7 Label" />:</td>
                            
            <td valign="top" class="value">${fieldValue(bean: projectInstance, field: "custom7")}</td>
            
            <td valign="top" class="name"><g:message code="project.custom8.label" default="Custom8 Label" />:</td>
                            
            <td valign="top" class="value">${fieldValue(bean: projectInstance, field: "custom8")}</td>
            
          </tr>
          <tr class="prop">

            <td valign="top" class="name">Workflow Code:</td>

            <td valign="top" class="value">${fieldValue(bean:projectInstance, field:'workflowCode')} &nbsp;&nbsp;
            
	            <span class="name">Runbook Driven: </span>&nbsp;
	
	            <span class="value"><input type="checkbox" name="runbookOn" id="runbookOn" ${ (projectInstance.runbookOn==1 ? 'checked="checked"':'') } disabled="disabled" /></span>
            
            </td>
            
			<td valign="top" class="name">Display Transitions in Status bar:</td>

            <td valign="top" class="value"><g:message code="project.trackChanges.${bean:projectInstance?.trackChanges}" /></td>

          </tr>

          <tr class="prop">
            <td valign="top" class="name">
              <label for="dateCreated">Date Created:</label>
            </td>
            <td valign="top" class="value"><tds:convertDateTime date="${projectInstance?.dateCreated}" timeZone="${request.getSession().getAttribute('CURR_TZ')?.CURR_TZ}"/> </td>
            <td valign="top" class="name">
              <label for="lastUpdated">Last Updated:</label>
            </td>
            <td valign="top" class="value"><tds:convertDateTime date="${projectInstance?.lastUpdated}" timeZone="${request.getSession().getAttribute('CURR_TZ')?.CURR_TZ}"/> </td>
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
</script>
</body>
</html>
