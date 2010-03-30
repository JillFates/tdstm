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
	
    <jq:plugin name="ui.resizable"/>

  </head>
  <body>
  
  <g:if test="${flash.message}">
      <div class="message">${flash.message}</div>
    </g:if>
   
    <div class="body">
    <h1>Show Project</h1>
        
    <div class="nav" style="border: 1px solid #CCCCCC; height: 11px">
      <span class="menuButton"><g:link class="list" action="list">Project List</g:link></span>
      <jsec:hasRole name="ADMIN">
        <span class="menuButton"><g:link class="create" action="create">New Project</g:link></span>
      </jsec:hasRole>
    </div>
    <br/>
   
   
    
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

            <td valign="top" class="value"><textarea cols="40"  rows="3" readOnly="true" >${fieldValue(bean:projectInstance, field:'description')}</textarea></td>

          </tr>

          <tr class="prop">
            <td valign="top" class="name">Comment:</td>

            <td valign="top" class="value"><textarea cols="40"  rows="3" readOnly="true" >${fieldValue(bean:projectInstance, field:'comment')}</textarea></td>

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
          </tr>

          <tr class="prop">
            <td valign="top" class="name">Move Manager:</td>
            <td valign="top" class="value">
            <g:if test="${moveManager?.partyIdTo?.lastName}">${moveManager?.partyIdTo?.lastName},</g:if>
            <g:if test="${moveManager?.partyIdTo?.firstName}"> ${moveManager?.partyIdTo?.firstName}</g:if>
            <g:if test="${moveManager?.partyIdTo?.title}"> - ${moveManager?.partyIdTo?.title}</g:if>
            
            </td>
          </tr>

          <tr class="prop">
            <td valign="top" class="name">Track Changes:</td>

            <td valign="top" class="value">${fieldValue(bean:projectInstance, field:'trackChanges')}</td>

          </tr>
          
          <tr class="prop">
            <td valign="top" class="name">Workflow Code:</td>

            <td valign="top" class="value">${fieldValue(bean:projectInstance, field:'workflowCode')}</td>

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
      <g:form>
        <input type="hidden" name="id" value="${projectInstance?.id}" />
        <jsec:hasRole name="PROJECT_ADMIN">
          <span class="button">
            <g:actionSubmit type="button" class="edit" value="Edit"/>
          </span>
        </jsec:hasRole>
        <jsec:hasRole name="ADMIN">
          <span class="button"><g:actionSubmit class="delete" onclick="return confirm('Are you sure?');" value="Delete" /></span>
        </jsec:hasRole>
      </g:form>
    </div></div>
  </body>
</html>
