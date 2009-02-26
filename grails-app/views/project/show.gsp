

<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8"/>
        <meta name="layout" content="main" />
        <title>Show Project</title>
        <g:javascript library="jquery"/>
	    <link type="text/css" rel="stylesheet" href="http://ui.jquery.com/testing/themes/base/ui.all.css" />
	    <script type="text/javascript" src="http://ui.jquery.com/testing/jquery-1.3.1.js"></script>
	    <script type="text/javascript" src="http://ui.jquery.com/testing/ui/ui.core.js"></script>
	    <script type="text/javascript" src="http://ui.jquery.com/testing/ui/ui.draggable.js"></script>
	    <script type="text/javascript" src="http://ui.jquery.com/testing/ui/ui.resizable.js"></script>
	    <script type="text/javascript" src="http://ui.jquery.com/testing/ui/ui.dialog.js"></script>
	    <script type="text/javascript">
	    $(document).ready(function(){
		        $("#dialog").dialog({ autoOpen: false });
	      	});
	    </script>
		<g:javascript>
			function editProject(){
        		$("#dialog").dialog("open");
      		}
		</g:javascript>
    </head>
    <body>
        <div class="body">
            <h1>Show Project</h1>
             <div class="nav" style="border: 1px solid #CCCCCC; height: 11px">
            <span class="menuButton"><g:link class="list" action="list">Project List</g:link></span>
            <jsec:hasRole name="ADMIN">
            <span class="menuButton"><g:link class="create" action="create">New Project</g:link></span>
            </jsec:hasRole>
        	</div>
            <br>
            <g:if test="${flash.message}">
            <div class="message">${flash.message}</div>
            </g:if>
            <div class="dialog" id="updateShow">
                <table>
                    <tbody>

                    	<tr class="prop">
                            <td valign="top" class="name">Id:</td>
                            
                            <td valign="top" class="value">${fieldValue(bean:projectInstance, field:'id')}</td>
                            
                        </tr>
                    	<tr class="prop">
                            <td valign="top" class="name">Project Code:</td>
                            
                            <td valign="top" class="value">${fieldValue(bean:projectInstance, field:'projectCode')}</td>
                            
                        </tr>
                    
                        <tr class="prop">
                            <td valign="top" class="name">Name:</td>
                            
                            <td valign="top" class="value">${fieldValue(bean:projectInstance, field:'name')}</td>
                            
                        </tr>
                    	
                    	<tr class="prop">
                            <td valign="top" class="name">Track Changes:</td>
                            
                            <td valign="top" class="value">${fieldValue(bean:projectInstance, field:'trackChanges')}</td>
                            
                        </tr>
                        
                        <tr class="prop">
                            <td valign="top" class="name">Start Date:</td>
                            
                            <td valign="top" class="value">${fieldValue(bean:projectInstance, field:'startDate')}</td>
                            
                        </tr>
                    
                        <tr class="prop">
                            <td valign="top" class="name">Completion Date:</td>
                            
                            <td valign="top" class="value">${fieldValue(bean:projectInstance, field:'completionDate')}</td>
                            
                        </tr>
                        
                        <tr class="prop">
                            <td valign="top" class="name">Comment:</td>
                            
                            <td valign="top" class="value">${fieldValue(bean:projectInstance, field:'comment')}</td>
                            
                        </tr>
                    
                       
                    
                        <tr class="prop">
                            <td valign="top" class="name">Description:</td>
                            
                            <td valign="top" class="value">${fieldValue(bean:projectInstance, field:'description')}</td>
                            
                        </tr>

                        <tr class="prop">
                            <td valign="top" class="name">Associated Client:</td>
                            
                            <td valign="top" class="value">${projectClient?.partyIdTo}</td>
                            
                        </tr>
                        
                        <tr class="prop">
                            <td valign="top" class="name">Associated Partner:</td>
                            
                            <td valign="top" class="value">${projectPartner?.partyIdTo}</td>
                            
                        </tr>
                        
                        <tr class="prop">
                            <td valign="top" class="name">Project Manager:</td>
                            
                            <td valign="top" class="value">${projectManager?.partyIdTo?.lastName}, ${projectManager?.partyIdTo?.firstName} - ${projectManager?.partyIdTo?.title}</td>
                            
                        </tr>
                        
                        <tr class="prop">
                            <td valign="top" class="name">Move Manager:</td>
                            
                            <td valign="top" class="value">${moveManager?.partyIdTo?.lastName}, ${moveManager?.partyIdTo?.firstName} - ${moveManager?.partyIdTo?.title}</td>
                            
                        </tr>
                        
                        
                    
                    </tbody>
                </table>
            </div>
            <div id="dialog" title="Edit Project" >
			      <g:form name="editForm" method="post" >

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
                                <td valign="top" class="value ${hasErrors(bean:projectInstance,field:'startDate','errors')}" nowrap="nowrap">
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
			          <span class="button">
			          <g:actionSubmit class="save" value="Update" />
			          </span>
			        </div>
			     </g:form>
			     </div>
           
            <div class="buttons">
                <g:form>
                    <input type="hidden" name="id" value="${projectInstance?.id}" />
                    <jsec:hasRole name="PROJECT_ADMIN">
                    <span class="button">
                    <input type="button" class="edit" value="Edit" onClick="return editProject()"/>
                    </span>
                    </jsec:hasRole>
                    <jsec:hasRole name="ADMIN">
                    <span class="button"><g:actionSubmit class="delete" onclick="return confirm('Are you sure?');" value="Delete" /></span>
                    </jsec:hasRole>
                </g:form>
            </div>
        </div>
    </body>
</html>
