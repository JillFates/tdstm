

<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8"/>
        <meta name="layout" content="main" />
        <title>Edit Project</title>
    </head>
    <body>
        <div class="menu2">
          <ul>
            <li><g:link class="home" controller="projectUtil">Project </g:link> </li>
            <li><g:link class="home" controller="asset">Assets </g:link></li>
            <li><g:link class="home" controller="asset" action="assetImport" >Import/Export</g:link> </li>
            <li><a href="#">Team </a></li>
            <li><a href="#">Contacts </a></li>
            <li><a href="#">Applications </a></li>
            <li><a href="#">Move Bundles </a></li>
          </ul>
        </div>
        <div class="body">
            <h1>Edit Project</h1>
            <div class="nav" style="border: 1px solid #CCCCCC; height: 11px">
            <span class="menuButton"><g:link class="list" action="list">Project List</g:link></span>
            <span class="menuButton"><g:link class="create" action="create">New Project</g:link></span>
	        </div>
	        <br>
            <g:if test="${flash.message}">
            <div class="message">${flash.message}</div>
            </g:if>
            <g:form method="post" >
                <input type="hidden" name="id" value="${projectInstance?.id}" />
                <div class="dialog">
                    <table>
                        <tbody>
                        
                                                    
                            <tr class="prop">
                                <td valign="top" class="name">
                                    <label for="name">Name:</label>
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
                                    <label for="comment">Comment:</label>
                                </td>
                                <td valign="top" class="value ${hasErrors(bean:projectInstance,field:'comment','errors')}">
                                    <textarea cols="40"  rows="3" name="comment" onkeydown="textCounter(document.createProjectForm.comment,200);" onkeyup="textCounter(document.createProjectForm.comment,200);">${fieldValue(bean:projectInstance,field:'comment')}</textarea>
                                <g:hasErrors bean="${projectInstance}" field="comment">
					            <div class="errors">
					                <g:renderErrors bean="${projectInstance}" as="list" field="comment"/>
					            </div>
					            </g:hasErrors>
                                </td>
                            </tr> 
                        
                            <tr class="prop">
                                <td valign="top" class="name">
                                    <label for="projectCode">Project Code:</label>
                                </td>
                                <td valign="top" class="value ${hasErrors(bean:projectInstance,field:'projectCode','errors')}">
                                    <input type="text" id="projectCode" name="projectCode" value="${fieldValue(bean:projectInstance,field:'projectCode')}"/>
                                <g:hasErrors bean="${projectInstance}" field="projectCode">
					            <div class="errors">
					                <g:renderErrors bean="${projectInstance}" as="list" field="projectCode"/>
					            </div>
					            </g:hasErrors>
                                </td>
                            </tr> 
                        
                            <tr class="prop">
                                <td valign="top" class="name">
                                    <label for="description">Description:</label>
                                </td>
                                <td valign="top" class="value ${hasErrors(bean:projectInstance,field:'description','errors')}">
                                    <input type="text" id="description" name="description" value="${fieldValue(bean:projectInstance,field:'description')}"/>
                                <g:hasErrors bean="${projectInstance}" field="description">
					            <div class="errors">
					                <g:renderErrors bean="${projectInstance}" as="list" field="description"/>
					            </div>
					            </g:hasErrors>
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
                                    <label for="startDate">Start Date:</label>
                                </td>
                                <td valign="top" class="value ${hasErrors(bean:projectInstance,field:'startDate','errors')}">
                                    <g:datePicker name="startDate" value="${projectInstance?.startDate}" noSelection="['':'']"></g:datePicker>
                                <g:hasErrors bean="${projectInstance}" field="startDate">
					            <div class="errors">
					                <g:renderErrors bean="${projectInstance}" as="list" field="startDate"/>
					            </div>
					            </g:hasErrors>
                                </td>
                            </tr> 
                        
                            <tr class="prop">
                                <td valign="top" class="name">
                                    <label for="completionDate">Completion Date:</label>
                                </td>
                                <td valign="top" class="value ${hasErrors(bean:projectInstance,field:'completionDate','errors')}">
                                    <g:datePicker name="completionDate" value="${projectInstance?.completionDate}" noSelection="['':'']"></g:datePicker>
                                <g:hasErrors bean="${projectInstance}" field="completionDate">
					            <div class="errors">
					                <g:renderErrors bean="${projectInstance}" as="list" field="completionDate"/>
					            </div>
					            </g:hasErrors>
                                </td>
                            </tr> 
                        
                        </tbody>
                    </table>
                </div>
                <div class="buttons">
                    <span class="button"><g:actionSubmit class="save" value="Update" /></span>
                    <span class="button"><g:actionSubmit class="delete" onclick="return confirm('Are you sure?');" value="Delete" /></span>
                </div>
            </g:form>
        </div>
    </body>
</html>
