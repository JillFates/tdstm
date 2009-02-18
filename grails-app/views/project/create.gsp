

<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8"/>
        <meta name="layout" content="main" />
        <title>Create Project</title>         
    </head>
    <body>
        <div class="body">
            <h1>Create Project</h1>
             <div class="nav" style="border: 1px solid #CCCCCC; height: 11px">
            <span class="menuButton"><g:link class="list" action="list">Project List</g:link></span>
        	</div>
        	<br>
            <g:if test="${flash.message}">
            <div class="message">${flash.message}</div>
            </g:if>
            <g:form action="save" method="post" >
                <div class="dialog">
                    <table>
                        <tbody>
                        
                            <tr class="prop">
                                <td valign="top" class="name">
                                    <label for="dateCreated">Date Created:</label>
                                </td>
                                <td valign="top" class="value ${hasErrors(bean:projectInstance,field:'dateCreated','errors')}">
                                    <g:datePicker name="dateCreated" value="${projectInstance?.dateCreated}" ></g:datePicker>
                                <g:hasErrors bean="${projectInstance}" field="dateCreated">
					            <div class="errors">
					                <g:renderErrors bean="${projectInstance}" as="list" field="dateCreated"/>
					            </div>
					            </g:hasErrors>
                                </td>
                            </tr> 
                        
                            <tr class="prop">
                                <td valign="top" class="name">
                                    <label for="lastUpdated">Last Updated:</label>
                                </td>
                                <td valign="top" class="value ${hasErrors(bean:projectInstance,field:'lastUpdated','errors')}">
                                    <g:datePicker name="lastUpdated" value="${projectInstance?.lastUpdated}" noSelection="['':'']"></g:datePicker>
                                <g:hasErrors bean="${projectInstance}" field="lastUpdated">
					            <div class="errors">
					                <g:renderErrors bean="${projectInstance}" as="list" field="lastUpdated"/>
					            </div>
					            </g:hasErrors>
                                </td>
                            </tr> 
                        
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
                                    <input type="text" id="comment" name="comment" value="${fieldValue(bean:projectInstance,field:'comment')}"/>
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
                    <span class="button"><input class="save" type="submit" value="Create" /></span>
                </div>
            </g:form>
        </div>
    </body>
</html>
